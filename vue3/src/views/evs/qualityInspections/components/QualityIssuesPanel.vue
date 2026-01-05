<template>
  <div class="quality-issues-panel">
    <!-- 统计概览 -->
    <!-- <div class="statistics-bar">
      <div class="stat-item stat-total">
        <div class="stat-label">全部问题</div>
        <div class="stat-value">{{ totalIssues }}</div>
      </div>
      <div class="stat-item stat-open">
        <div class="stat-label">待处理</div>
        <div class="stat-value">{{ openIssues }}</div>
      </div>
      <div class="stat-item stat-progress">
        <div class="stat-label">整改中</div>
        <div class="stat-value">{{ inProgressIssues }}</div>
      </div>
      <div class="stat-item stat-resolved">
        <div class="stat-label">已解决</div>
        <div class="stat-value">{{ resolvedIssues }}</div>
      </div>
    </div> -->

    <!-- 筛选和排序 -->
    <div class="filter-bar">
      <el-radio-group v-model="filterStatus" size="small" @change="handleFilterChange">
        <el-radio-button label="ALL">全部</el-radio-button>
        <el-radio-button label="OPEN">待处理</el-radio-button>
        <el-radio-button label="IN_PROGRESS">整改中</el-radio-button>
        <el-radio-button label="RESOLVED">已解决</el-radio-button>
      </el-radio-group>
      <el-select v-model="sortBy" size="small" style="width: 150px; margin-left: 12px;" @change="handleSortChange">
        <el-option label="按时间倒序" value="time-desc" />
        <el-option label="按时间正序" value="time-asc" />
        <el-option label="按优先级" value="priority" />
      </el-select>
    </div>

    <!-- 问题列表 -->
    <div v-loading="loading" class="issues-list">
      <div
        v-for="issue in filteredIssues"
        :key="issue.id"
        class="issue-card"
        :class="`issue-status-${issue.status.toLowerCase()}`"
      >
        <!-- 问题头部 -->
        <div class="issue-header" @click="handleViewIssue(issue)">
          <div class="issue-header-left">
            <el-icon class="issue-icon" :style="{ color: getIssueIconColor(issue.status) }">
              <WarningFilled />
            </el-icon>
            <div class="issue-title-section">
              <div class="issue-title">{{ issue.title }}</div>
              <div class="issue-meta">
                <el-tag :type="getIssueCategoryType(issue.category)" size="small">
                  {{ getIssueCategoryText(issue.category) }}
                </el-tag>
                <span class="issue-location" v-if="issue.location">
                  <el-icon><Location /></el-icon>
                  {{ issue.location }}
                </span>
                <span class="issue-date">
                  <el-icon><Calendar /></el-icon>
                  {{ parseTime(issue.createdAt, '{y}-{m}-{d}') }}
                </span>
              </div>
            </div>
          </div>
          <div class="issue-header-right">
            <el-tag :type="getIssueStatusType(issue.status)" size="default">
              {{ getIssueStatusText(issue.status) }}
            </el-tag>
            <el-button
              v-if="issue.status !== 'RESOLVED' && issue.status !== 'CLOSED'"
              type="primary"
              size="small"
              @click.stop="handleSubmitFix(issue)"
            >
              提交整改
            </el-button>
            <el-button
              type="primary"
              size="small"
              text
              @click.stop="handleViewIssue(issue)"
            >
              查看详情
            </el-button>
            <el-button
              type="danger"
              size="small"
              text
              @click.stop="handleDeleteIssue(issue)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="filteredIssues.length === 0" description="暂无质量问题" :image-size="120" />
    </div>

    <!-- 问题详情抽屉 -->
    <QualityIssueDetailDrawer
      ref="issueDetailDrawerRef"
      v-model:visible="drawerVisible"
      :issue="currentIssue"
      :title="currentIssue?.title || '问题详情'"
      @submit-fix="handleSubmitFix"
      @delete-fix="handleDeleteFixFromDrawer"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, getCurrentInstance } from 'vue'
import { WarningFilled, Location, Calendar } from '@element-plus/icons-vue'
import QualityIssueDetailDrawer from '@/components/QualityIssueDetailDrawer/index.vue'

const { proxy } = getCurrentInstance()

// 获取字典数据
const { decoration_issue_severity } = proxy.useDict('decoration_issue_severity')

// Props
const props = defineProps({
  issues: {
    type: Array,
    default: () => []
  },
  issueFixesMap: {
    type: Map,
    default: () => new Map()
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['submit-fix', 'refresh', 'delete-issue', 'delete-fix'])

// 响应式数据
const drawerVisible = ref(false)
const currentIssue = ref(null)
const filterStatus = ref('ALL')
const sortBy = ref('time-desc')
const issueDetailDrawerRef = ref(null)

// 统计数据
const totalIssues = computed(() => props.issues.length)
const openIssues = computed(() => props.issues.filter(i => i.status === 'OPEN').length)
const inProgressIssues = computed(() => props.issues.filter(i => i.status === 'IN_PROGRESS').length)
const resolvedIssues = computed(() => props.issues.filter(i => i.status === 'RESOLVED' || i.status === 'CLOSED').length)

// 筛选和排序后的问题列表
const filteredIssues = computed(() => {
  let result = [...props.issues]

  // 筛选
  if (filterStatus.value !== 'ALL') {
    result = result.filter(issue => issue.status === filterStatus.value)
  }

  // 排序
  switch (sortBy.value) {
    case 'time-desc':
      result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      break
    case 'time-asc':
      result.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
      break
    case 'priority':
      // 优先级排序：CRITICAL > URGENT > GENERAL > OTHER
      const priorityMap = { 'CRITICAL': 4, 'URGENT': 3, 'GENERAL': 2, 'OTHER': 1 }
      result.sort((a, b) => {
        const priorityA = priorityMap[a.category] || 0
        const priorityB = priorityMap[b.category] || 0
        return priorityB - priorityA
      })
      break
  }

  return result
})

/** 查看问题详情 */
function handleViewIssue(issue) {
  currentIssue.value = issue
  drawerVisible.value = true
}

/** 筛选变化 */
function handleFilterChange() {
  // 筛选变化时不需要特殊处理
}

/** 排序变化 */
function handleSortChange() {
  // 排序变化时不需要特殊处理
}



/** 提交整改 */
function handleSubmitFix(issue) {
  emit('submit-fix', issue)
}

/** 删除问题 */
function handleDeleteIssue(issue) {
  emit('delete-issue', issue)
}

/** 删除整改记录 */
function handleDeleteFix(fix) {
  emit('delete-fix', fix)
}

/** 从抽屉中删除整改记录（删除后刷新抽屉数据） */
async function handleDeleteFixFromDrawer(fix) {
  emit('delete-fix', fix)
  // 等待删除完成后刷新抽屉数据
  await nextTick()
  setTimeout(() => {
    issueDetailDrawerRef.value?.refreshFixes()
  }, 500)
}



/** 获取问题图标颜色 */
function getIssueIconColor(status) {
  const colorMap = {
    'OPEN': '#ff4d4f',
    'IN_PROGRESS': '#faad14',
    'RESOLVED': '#52c41a',
    'CLOSED': '#d9d9d9'
  }
  return colorMap[status] || '#ff4d4f'
}

/** 获取问题状态标签类型 */
function getIssueStatusType(status) {
  const typeMap = {
    'OPEN': 'danger',
    'IN_PROGRESS': 'warning',
    'RESOLVED': 'success',
    'CLOSED': 'info'
  }
  return typeMap[status] || 'info'
}

/** 获取问题状态文本 */
function getIssueStatusText(status) {
  const textMap = {
    'OPEN': '待处理',
    'IN_PROGRESS': '整改中',
    'RESOLVED': '已解决',
    'CLOSED': '已关闭'
  }
  return textMap[status] || '未知'
}

/** 获取问题分类标签类型 */
function getIssueCategoryType(category) {
  // 优先使用字典数据
  const dictItem = decoration_issue_severity.value?.find(item => item.value === category)
  if (dictItem && dictItem.listClass) {
    return dictItem.listClass
  }
  // 后备映射
  const typeMap = {
    'ENGINEERING_PROBLEM': 'primary',
    'URGENT': 'danger',
    'HIGH': 'danger',
    'MEDIUM': 'warning',
    'LOW': 'info',
    'GENERAL': 'info',
    'CRITICAL': 'danger',
    'OTHER': ''
  }
  return typeMap[category] || ''
}

/** 获取问题分类文本 */
function getIssueCategoryText(category) {
  // 优先使用字典数据
  const dictItem = decoration_issue_severity.value?.find(item => item.value === category)
  if (dictItem) {
    return dictItem.label
  }
  // 后备映射
  const textMap = {
    'ENGINEERING_PROBLEM': '工程类问题',
    'URGENT': '紧急',
    'HIGH': '严重',
    'MEDIUM': '一般',
    'LOW': '轻微',
    'GENERAL': '一般问题',
    'CRITICAL': '红线问题',
    'OTHER': '其他'
  }
  return textMap[category] || '未分类'
}





// 暴露方法
defineExpose({
  drawerVisible,
  currentIssue,
  handleViewIssue
})
</script>

<style scoped lang="scss">
.quality-issues-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 16px;
}

// 统计概览
.statistics-bar {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);

  .stat-item {
    flex: 1;
    text-align: center;
    padding: 12px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 6px;
    backdrop-filter: blur(10px);
    transition: all 0.3s;

    &:hover {
      background: rgba(255, 255, 255, 0.25);
      transform: translateY(-2px);
    }

    .stat-label {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.9);
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #fff;
    }
  }
}

// 筛选栏
.filter-bar {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

// 问题列表
.issues-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 4px;
  min-height: 0;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;

    &:hover {
      background: #a8a8a8;
    }
  }
}

// 问题卡片
.issue-card {
  margin-bottom: 12px;
  background: #fff;
  border-radius: 8px;
  border: 2px solid #e8e8e8;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    border-color: #1677ff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  // 不同状态的边框颜色
  &.issue-status-open {
    border-left: 4px solid #ff4d4f;
  }

  &.issue-status-in_progress {
    border-left: 4px solid #faad14;
  }

  &.issue-status-resolved,
  &.issue-status-closed {
    border-left: 4px solid #52c41a;
    opacity: 0.8;
  }
}

// 问题头部
.issue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  user-select: none;

  .issue-header-left {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    flex: 1;
    min-width: 0;

    .issue-icon {
      font-size: 24px;
      flex-shrink: 0;
      margin-top: 2px;
    }

    .issue-title-section {
      flex: 1;
      min-width: 0;

      .issue-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }

      .issue-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        flex-wrap: wrap;
        font-size: 13px;
        color: #666;

        .issue-location,
        .issue-date {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }

  .issue-header-right {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
    margin-left: 16px;
  }
}


</style>
