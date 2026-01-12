<template>
  <el-drawer
    v-model="drawerVisible"
    :title="drawerTitle"
    :size="drawerSize"
    direction="rtl"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <template #header>
      <div class="drawer-header">
        <div class="header-title">
          <el-icon style="color: #52c41a; margin-right: 8px;"><Check /></el-icon>
          <span>{{ drawerTitle }}</span>
        </div>
        <div class="header-stats" v-if="issueFixes.length > 0">
          <el-tag type="info" size="small">{{ issueFixes.length }}条整改记录</el-tag>
          <el-tag
            :type="getFinalStatusType()"
            size="small"
            style="margin-left: 8px;"
          >
            {{ getFinalStatusText() }}
          </el-tag>
        </div>
      </div>
    </template>

    <div class="drawer-content" v-loading="loading">
      <!-- 问题信息区域 -->
      <div v-if="currentIssue" class="issue-info-section">
        <div class="section-header">
          <el-icon :style="{ color: getIssueIconColor(currentIssue.status) }"><WarningFilled /></el-icon>
          <span>问题信息</span>
        </div>
        <div class="issue-card">
          <!-- 头部：标题 + 状态标签 -->
          <div class="card-header">
            <h3 class="card-title">{{ currentIssue.title || '质量问题' }}</h3>
            <div class="card-tags">
              <el-tag :type="getIssueCategoryType(currentIssue.category)" size="small" style="margin-left: 6px;">
                {{ getIssueCategoryText(currentIssue.category) }}
              </el-tag>
            </div>
          </div>

          <!-- 基本信息区块 -->
          <div class="info-section">
            <div class="section-title">基本信息</div>
            <div class="field-grid">
              <div class="field-item full-width">
                <span class="field-label">问题描述</span>
                <span class="field-value">{{ currentIssue.description || '暂无描述' }}</span>
              </div>
              <div class="field-item">
                <span class="field-label">问题位置</span>
                <span class="field-value">{{ currentIssue.location || '未指定' }}</span>
              </div>
              <div class="field-item">
                <span class="field-label">上报人</span>
                <span class="field-value">{{ getReportedBy(currentIssue) }}</span>
              </div>
            </div>
          </div>

          <!-- 时间信息区块 -->
          <div class="info-section">
            <div class="section-title">时间信息</div>
            <div class="field-grid">
              <div class="field-item">
                <span class="field-label">上报时间</span>
                <span class="field-value">{{ parseTime(currentIssue.createdAt, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
              </div>
              <div class="field-item">
                <span class="field-label">整改期限</span>
                <span class="field-value" :class="{ 'overdue': isOverdue(currentIssue.dueDate) }">
                  {{ currentIssue.dueDate ? parseTime(currentIssue.dueDate, '{y}-{m}-{d}') : '未设定' }}
                  <el-tag v-if="isOverdue(currentIssue.dueDate)" type="danger" size="small" style="margin-left: 4px;">已逾期</el-tag>
                </span>
              </div>
            </div>
          </div>

          <!-- 现场图片 -->
          <div class="info-section" v-if="hasIssueImages">
            <div class="section-title">现场照片</div>
            <div
              class="images-preview"
              v-viewer="getViewerOptions(getIssueImages())"
            >
              <img
                v-for="(img, index) in getIssueImages()"
                :key="index"
                :src="getImageUrl(img)"
                class="preview-image"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 整改历史时间轴 -->
      <div class="fixes-section">
        <div class="section-header">
          <el-icon style="color: #52c41a;"><Clock /></el-icon>
          <span>整改时间轴</span>
        </div>

        <div v-if="issueFixes.length === 0" class="empty-fixes">
          <el-empty
            description="暂无整改记录"
            :image-size="100"
          >
            <template #description>
              <div class="empty-description">
                <p class="empty-text">该问题还没有整改记录</p>
                <p class="empty-hint">点击下方"提交整改"按钮开始记录整改过程</p>
              </div>
            </template>
            <el-button
              v-if="currentIssue && (currentIssue.status === 'OPEN' || currentIssue.status === 'IN_PROGRESS')"
              type="primary"
              size="small"
              @click="handleAddFix"
            >
              <el-icon style="margin-right: 4px;"><Plus /></el-icon>
              提交整改
            </el-button>
          </el-empty>
        </div>

        <el-timeline v-else>
          <el-timeline-item
            v-for="(fix, index) in issueFixes"
            :key="fix.id || index"
            :color="getFixTimelineColor(fix.status)"
            :icon="getFixTimelineIcon(fix.status)"
            size="large"
            :timestamp="parseTime(fix.createdAt, '{y}-{m}-{d} {h}:{i}:{s}')"
            placement="top"
          >
            <div class="fix-timeline-content">
              <div class="fix-header">
                <div class="fix-title">
                  <el-tag :type="getFixStatusType(fix.status)" size="small">
                    {{ getFixStatusText(fix.status) }}
                  </el-tag>
                  <span class="fix-id" v-if="fix.id">记录 #{{ String(fix.id).substring(0, 8) }}</span>
                </div>
                <div class="fix-meta">
                  <span class="fix-creator" v-if="fix.createdBy">{{ fix.createdBy }}</span>
                  <el-popconfirm
                    title="确定要删除这条整改记录吗？"
                    :width="200"
                    confirm-button-text="确定"
                    cancel-button-text="取消"
                    @confirm="handleDeleteFix(fix)"
                  >
                    <template #reference>
                      <el-button type="danger" size="small" text style="margin-left: 8px;">
                        删除
                      </el-button>
                    </template>
                  </el-popconfirm>
                </div>
              </div>

              <div class="fix-description">
                {{ fix.fixDescription || '无详细描述' }}
              </div>

              <!-- 修复时间 -->
              <div v-if="fix.fixedAt" class="fix-completed-time">
                <el-icon style="color: #52c41a; margin-right: 4px;"><Check /></el-icon>
                修复时间：{{ parseTime(fix.fixedAt, '{y}-{m}-{d} {h}:{i}:{s}') }}
              </div>

              <!-- 整改图片 -->
              <div v-if="hasFixImages(fix)" class="fix-images">
                <div class="images-label">整改图片：</div>
                <div
                  class="images-grid"
                  v-viewer="getViewerOptions(getFixImages(fix))"
                >
                  <img
                    v-for="(img, imgIndex) in getFixImages(fix)"
                    :key="imgIndex"
                    :src="getImageUrl(img)"
                    class="preview-image"
                  />
                </div>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

    <!-- 底部操作 -->
    <template #footer>
      <div class="drawer-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button
          v-if="currentIssue && (currentIssue.status === 'OPEN' || currentIssue.status === 'IN_PROGRESS')"
          type="primary"
          @click="handleAddFix"
        >
          <el-icon style="margin-right: 4px;"><Plus /></el-icon>
          提交整改
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup name="QualityIssueDetailDrawer">
import { ref, computed, watch, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { getQualityFixesByIssueId } from '@/api/evs/qualityFixes'
import { Check, WarningFilled, Clock, Plus } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  issue: {
    type: Object,
    default: null
  },
  title: {
    type: String,
    default: '问题详情'
  }
})

const emit = defineEmits(['update:visible', 'submit-fix', 'delete-fix', 'refresh'])

const { proxy } = getCurrentInstance()

// 获取字典数据
const { decoration_issue_severity } = proxy.useDict('decoration_issue_severity')

// v-viewer 配置选项
function getViewerOptions(images) {
  return {
    toolbar: true,
    navbar: images && images.length > 1,
    title: false,
    tooltip: true,
    movable: true,
    zoomable: true,
    rotatable: true,
    scalable: true,
    transition: true,
    keyboard: true,
    loop: true,
    minZoomRatio: 0.1,
    maxZoomRatio: 5,
    zIndex: 9999
  }
}

// 响应式数据
const drawerVisible = ref(false)
const loading = ref(false)
const currentIssue = ref(null)
const issueFixes = ref([])
const windowWidth = ref(window.innerWidth)

// 计算抽屉标题
const drawerTitle = computed(() => props.title)

// 响应式抽屉尺寸
const drawerSize = computed(() => {
  if (windowWidth.value >= 1200) {
    return '50%'
  } else if (windowWidth.value >= 768) {
    return '70%'
  } else {
    return '90%'
  }
})

// 计算属性
const hasIssueImages = computed(() => {
  if (!currentIssue.value?.images) return false

  try {
    let images = []
    const imageData = currentIssue.value.images

    if (typeof imageData === 'string') {
      images = JSON.parse(imageData || '[]')
    } else if (Array.isArray(imageData)) {
      images = imageData
    } else if (typeof imageData === 'object' && imageData !== null) {
      if (imageData.url) {
        images = [imageData.url]
      } else if (imageData.images) {
        images = Array.isArray(imageData.images) ? imageData.images : []
      } else {
        images = Object.values(imageData).filter(img => img)
      }
    }

    return images.filter(img => img && (typeof img === 'string')).length > 0
  } catch (error) {
    console.warn('检查问题图片数据失败:', error)
    return false
  }
})

// 监听属性变化
watch(() => props.visible, (newVal) => {
  drawerVisible.value = newVal
  if (newVal && props.issue) {
    currentIssue.value = props.issue
    loadIssueFixes()
  }
})

watch(() => props.issue, (newVal) => {
  if (newVal) {
    currentIssue.value = newVal
  }
})

watch(drawerVisible, (newVal) => {
  emit('update:visible', newVal)
})

// 加载问题整改记录
async function loadIssueFixes() {
  if (!currentIssue.value?.id) return

  loading.value = true
  try {
    const response = await getQualityFixesByIssueId(currentIssue.value.id)
    const fixes = response.data || response.rows || []

    // 按创建时间倒序排序
    fixes.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))

    issueFixes.value = fixes
  } catch (error) {
    console.error('加载整改记录失败:', error)
    proxy.$modal.msgError('加载整改记录失败')
    issueFixes.value = []
  } finally {
    loading.value = false
  }
}

// 获取问题图片
function getIssueImages() {
  if (!currentIssue.value?.images) return []

  try {
    let images = []
    const imageData = currentIssue.value.images

    if (typeof imageData === 'string') {
      images = JSON.parse(imageData || '[]')
    } else if (Array.isArray(imageData)) {
      images = imageData
    } else if (typeof imageData === 'object' && imageData !== null) {
      if (imageData.url) {
        images = [imageData.url]
      } else if (imageData.images) {
        images = Array.isArray(imageData.images) ? imageData.images : []
      } else {
        images = Object.values(imageData).filter(img => img)
      }
    }

    return images.filter(img => img && (typeof img === 'string'))
  } catch (error) {
    console.warn('解析图片数据失败:', error)
    return []
  }
}

function getIssueImageUrls() {
  return getIssueImages().map(img => getImageUrl(img))
}

// 获取整改图片
function hasFixImages(fix) {
  if (!fix?.images) return false

  try {
    const images = getFixImages(fix)
    return images.length > 0
  } catch {
    return false
  }
}

function getFixImages(fix) {
  if (!fix?.images) return []

  try {
    let images = []
    const imageData = fix.images

    if (typeof imageData === 'string') {
      images = JSON.parse(imageData || '[]')
    } else if (Array.isArray(imageData)) {
      images = imageData
    } else if (typeof imageData === 'object' && imageData !== null) {
      if (imageData.url) {
        images = [imageData.url]
      } else if (imageData.images) {
        images = Array.isArray(imageData.images) ? imageData.images : []
      } else {
        images = Object.values(imageData).filter(img => img)
      }
    }

    return images.filter(img => img && (typeof img === 'string'))
  } catch (error) {
    console.warn('解析整改图片数据失败:', error)
    return []
  }
}

function getFixImageUrls(fix) {
  return getFixImages(fix).map(img => getImageUrl(img))
}

// 获取图片URL
function getImageUrl(img) {
  if (!img) return ''

  if (typeof img === 'string' && img.startsWith('http')) {
    return img
  }

  const basePath = import.meta.env.VITE_APP_BASE_API
  if (img.startsWith('/')) {
    return basePath + img
  } else {
    return basePath + '/' + img
  }
}

// 检查是否逾期
function isOverdue(dueDate) {
  if (!dueDate) return false
  return new Date(dueDate) < new Date()
}

// 获取上报人信息
function getReportedBy(issue) {
  if (!issue) return '未知'
  return issue.reportedBy || issue.createdBy || issue.createBy || '未知'
}

// 时间格式化
function parseTime(time, pattern) {
  return proxy.parseTime(time, pattern)
}

// 状态相关函数
function getIssueIconColor(status) {
  const colorMap = {
    'OPEN': '#ff4d4f',
    'IN_PROGRESS': '#e6a23c',
    'RESOLVED': '#52c41a',
    'CLOSED': '#909399'
  }
  return colorMap[status] || '#ff4d4f'
}

function getIssueStatusType(status) {
  const typeMap = {
    'OPEN': 'danger',
    'IN_PROGRESS': 'warning',
    'RESOLVED': 'success',
    'CLOSED': 'info'
  }
  return typeMap[status] || 'info'
}

function getIssueStatusText(status) {
  const textMap = {
    'OPEN': '待处理',
    'IN_PROGRESS': '整改中',
    'RESOLVED': '已解决',
    'CLOSED': '已关闭'
  }
  return textMap[status] || '未知'
}

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

function getFixStatusType(status) {
  const typeMap = {
    'OPEN': 'info',
    'IN_PROGRESS': 'warning',
    'RESOLVED': 'success',
    'CLOSED': 'info'
  }
  return typeMap[status] || 'info'
}

function getFixStatusText(status) {
  const textMap = {
    'OPEN': '未解决',
    'IN_PROGRESS': '解决中',
    'RESOLVED': '已解决',
    'CLOSED': '已关闭'
  }
  return textMap[status] || '未知'
}

function getFixTimelineColor(status) {
  const colorMap = {
    'OPEN': '#d9d9d9',
    'IN_PROGRESS': '#e6a23c',
    'RESOLVED': '#52c41a',
    'CLOSED': '#909399'
  }
  return colorMap[status] || '#d9d9d9'
}

function getFixTimelineIcon(status) {
  if (status === 'RESOLVED') {
    return 'Check'
  } else if (status === 'IN_PROGRESS') {
    return 'Clock'
  }
  return ''
}

function getFinalStatusType() {
  if (issueFixes.value.length === 0) {
    return getIssueStatusType(currentIssue.value?.status)
  }

  const lastFix = issueFixes.value[0]
  return getFixStatusType(lastFix.status)
}

function getFinalStatusText() {
  if (issueFixes.value.length === 0) {
    return getIssueStatusText(currentIssue.value?.status)
  }

  const lastFix = issueFixes.value[0]
  return getFixStatusText(lastFix.status)
}

// 操作函数
function handleAddFix() {
  if (!currentIssue.value) return
  emit('submit-fix', currentIssue.value)
}

function handleDeleteFix(fix) {
  if (!fix) return
  emit('delete-fix', fix)
}

// 提供刷新方法供外部调用
function refreshFixes() {
  loadIssueFixes()
}

function handleClose() {
  drawerVisible.value = false
  currentIssue.value = null
  issueFixes.value = []
}

// 窗口大小变化处理
function handleWindowResize() {
  windowWidth.value = window.innerWidth
}

// 生命周期钩子
onMounted(() => {
  window.addEventListener('resize', handleWindowResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleWindowResize)
})

// 暴露方法
defineExpose({
  loadIssueFixes,
  refreshFixes
})
</script>

<style scoped lang="scss">
@import './styles.scss';
</style>
