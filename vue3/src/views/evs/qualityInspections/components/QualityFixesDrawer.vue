<template>
  <el-drawer
    v-model="drawerVisible"
    title="整改历史记录"
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
          <div class="issue-title-row">
            <h3 class="issue-title">{{ currentIssue.title || '质量问题' }}</h3>
            <div class="issue-meta">
              <el-tag :type="getIssueStatusType(currentIssue.status)" size="small">
                {{ getIssueStatusText(currentIssue.status) }}
              </el-tag>
              <el-tag :type="getIssueCategoryType(currentIssue.category)" size="small" style="margin-left: 6px;">
                {{ getIssueCategoryText(currentIssue.category) }}
              </el-tag>
            </div>
          </div>

          <div class="issue-description">
            <!-- 第一行：问题描述 -->
            <div class="detail-item detail-item-full">
              <span class="detail-label">描述：</span>
              <span class="detail-value">{{ currentIssue.description || '暂无描述' }}</span>
            </div>

            <!-- 第二行：位置 · 上报人 · 上报时间 -->
            <div class="detail-item detail-item-inline">
              <div class="detail-chunk">
                <span class="detail-label">位置：</span>
                <span class="detail-value">{{ currentIssue.location || '未指定' }}</span>
              </div>
              <div class="detail-separator">·</div>
              <div class="detail-chunk">
                <span class="detail-label">上报人：</span>
                <span class="detail-value">{{ getReportedBy(currentIssue) }}</span>
              </div>
              <div class="detail-separator">·</div>
              <div class="detail-chunk">
                <span class="detail-label">时间：</span>
                <span class="detail-value">{{ proxy.parseTime(currentIssue.createdAt, '{m}-{d} {h}:{i}') }}</span>
              </div>
            </div>

            <!-- 第三行：分类 · 状态 · 期限 -->
            <div class="detail-item detail-item-inline">
              <div class="detail-chunk">
                <span class="detail-label">分类：</span>
                <el-tag :type="getIssueCategoryType(currentIssue.category)" size="small">
                  {{ getIssueCategoryText(currentIssue.category) }}
                </el-tag>
              </div>
              <div class="detail-separator">·</div>
              <div class="detail-chunk">
                <span class="detail-label">状态：</span>
                <el-tag :type="getIssueStatusType(currentIssue.status)" size="small">
                  {{ getIssueStatusText(currentIssue.status) }}
                </el-tag>
              </div>
              <div class="detail-separator" v-if="currentIssue.dueDate">·</div>
              <div class="detail-chunk" v-if="currentIssue.dueDate">
                <span class="detail-label">期限：</span>
                <span class="detail-value" :class="{ 'overdue': isOverdue(currentIssue.dueDate) }">
                  {{ proxy.parseTime(currentIssue.dueDate, '{m}-{d}') }}
                  <el-tag v-if="isOverdue(currentIssue.dueDate)" type="danger" size="small" style="margin-left: 4px;">已逾期</el-tag>
                </span>
              </div>
            </div>
          </div>

          <!-- 问题图片 -->
          <div class="issue-images" v-if="hasIssueImages">
            <div class="images-title">现场照片：</div>
            <div class="images-preview">
              <el-image
                v-for="(img, index) in getIssueImages()"
                :key="index"
                :src="getImageUrl(img)"
                :preview-src-list="getIssueImageUrls()"
                fit="cover"
                style="width: 80px; height: 80px; margin-right: 8px; border-radius: 4px;"
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
            :timestamp="proxy.parseTime(fix.createdAt, '{y}-{m}-{d} {h}:{i}')"
            placement="top"
          >
            <div class="fix-timeline-content">
              <div class="fix-header">
                <div class="fix-title">
                  <el-tag :type="getFixStatusType(fix.status)" size="small">
                    {{ getFixStatusText(fix.status) }}
                  </el-tag>
                  <span class="fix-id" v-if="fix.id">记录 #{{ fix.id.substring(0, 8) }}</span>
                </div>
                <div class="fix-meta">
                  <span class="fix-creator" v-if="fix.createdBy">{{ fix.createdBy }}</span>
                  <span class="fix-time">{{ proxy.parseTime(fix.createdAt, '{m}-{d} {h}:{i}') }}</span>
                  <!-- 管理员操作按钮 -->
                  <div v-if="isAdmin" class="fix-actions">
                    <el-button
                      type="primary"
                      size="small"
                      text
                      @click="handleEditFix(fix)"
                      style="padding: 2px 6px;"
                    >
                      <el-icon style="margin-right: 2px;"><Edit /></el-icon>
                      编辑
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      text
                      @click="handleDeleteFix(fix)"
                      style="padding: 2px 6px; margin-left: 4px;"
                    >
                      <el-icon style="margin-right: 2px;"><Delete /></el-icon>
                      删除
                    </el-button>
                  </div>
                </div>
              </div>

              <div class="fix-description">
                {{ fix.fixDescription || '无详细描述' }}
              </div>

              <!-- 修复时间 -->
              <div v-if="fix.fixedAt" class="fix-completed-time">
                <el-icon style="color: #52c41a; margin-right: 4px;"><Check /></el-icon>
                修复时间：{{ proxy.parseTime(fix.fixedAt, '{y}-{m}-{d} {h}:{i}') }}
              </div>

              <!-- 整改图片 -->
              <div v-if="hasFixImages(fix)" class="fix-images">
                <div class="images-label">整改图片：</div>
                <div class="images-grid">
                  <el-image
                    v-for="(img, imgIndex) in getFixImages(fix)"
                    :key="imgIndex"
                    :src="getImageUrl(img)"
                    :preview-src-list="getFixImageUrls(fix)"
                    fit="cover"
                    style="width: 60px; height: 60px; margin: 4px; border-radius: 4px;"
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

  <!-- 整改提交对话框 -->
  <FixSubmissionDialog
    v-model:visible="fixDialogVisible"
    :issue="currentIssue"
    :fix-data="currentEditingFix"
    :upload-url="uploadUrl"
    :edit-mode="!!currentEditingFix"
    @success="handleFixSuccess"
    @error="handleFixError"
    @refresh="loadIssueFixes"
  />
</template>

<script setup name="QualityFixesDrawer">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { getQualityFixesByIssueId, delQualityFixes, updateQualityFixes } from '@/api/evs/qualityFixes'
import { Check, WarningFilled, Clock, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getCurrentInstance } from 'vue'
import { useProjectAuth } from '@/utils/projectAuth'
import FixSubmissionDialog from './FixSubmissionDialog.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  issue: {
    type: Object,
    default: null
  },
  uploadUrl: {
    type: String,
    default: ''
  },
  viewMode: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'fix-success', 'fix-error'])

const { proxy } = getCurrentInstance()

// 权限控制
const { isAdmin } = useProjectAuth()

// 计算抽屉标题
const drawerTitle = computed(() => {
  return props.viewMode ? '详情查看' : '整改历史记录'
})

// 响应式数据
const drawerVisible = ref(false)
const loading = ref(false)
const fixDialogVisible = ref(false)
const currentIssue = ref(null)
const currentEditingFix = ref(null)
const issueFixes = ref([])
const windowWidth = ref(window.innerWidth)

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

    // 处理字符串类型的JSON数据
    if (typeof imageData === 'string') {
      images = JSON.parse(imageData || '[]')
    }
    // 处理数组类型的数据
    else if (Array.isArray(imageData)) {
      images = imageData
    }
    // 处理对象类型的数据（可能包含url字段）
    else if (typeof imageData === 'object' && imageData !== null) {
      if (imageData.url) {
        images = [imageData.url]
      } else if (imageData.images) {
        images = Array.isArray(imageData.images) ? imageData.images : []
      } else {
        images = Object.values(imageData).filter(img => img)
      }
    }

    // 过滤有效图片并检查是否有图片
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
    console.log('🔍 [FIXES DRAWER] 正在加载整改记录:', currentIssue.value.id)
    const response = await getQualityFixesByIssueId(currentIssue.value.id)
    const fixes = response.data || response.rows || []

    // 按创建时间倒序排序
    fixes.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))

    issueFixes.value = fixes
    console.log('🔍 [FIXES DRAWER] 已加载整改记录:', { count: fixes.length, fixes })
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

    // 处理字符串类型的JSON数据
    if (typeof imageData === 'string') {
      images = JSON.parse(imageData || '[]')
    }
    // 处理数组类型的数据
    else if (Array.isArray(imageData)) {
      images = imageData
    }
    // 处理对象类型的数据（可能包含url字段）
    else if (typeof imageData === 'object' && imageData !== null) {
      if (imageData.url) {
        images = [imageData.url]
      } else if (imageData.images) {
        images = Array.isArray(imageData.images) ? imageData.images : []
      } else {
        images = Object.values(imageData).filter(img => img)
      }
    }

    // 过滤有效图片
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

    // 处理字符串类型的JSON数据
    if (typeof imageData === 'string') {
      images = JSON.parse(imageData || '[]')
    }
    // 处理数组类型的数据
    else if (Array.isArray(imageData)) {
      images = imageData
    }
    // 处理对象类型的数据（可能包含url字段）
    else if (typeof imageData === 'object' && imageData !== null) {
      if (imageData.url) {
        images = [imageData.url]
      } else if (imageData.images) {
        images = Array.isArray(imageData.images) ? imageData.images : []
      } else {
        images = Object.values(imageData).filter(img => img)
      }
    }

    // 过滤有效图片
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

  // 如果已经是完整的URL，直接返回
  if (typeof img === 'string' && img.startsWith('http')) {
    return img
  }

  // 处理相对路径
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
  // 优先级：reportedBy > createdBy > createBy > 未知
  return issue.reportedBy || issue.createdBy || issue.createBy || '未知'
}

// 状态相关函数
function getIssueIconColor(status) {
  const colorMap = {
    'OPEN': '#ff4d4f',      // 红色 - 待处理需要关注
    'IN_PROGRESS': '#e6a23c', // 橙色 - 整改中
    'RESOLVED': '#52c41a',   // 绿色 - 已解决
    'CLOSED': '#909399'     // 灰色 - 已关闭
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
  const typeMap = {
    'GENERAL': 'info',
    'CRITICAL': 'danger',
    'URGENT': 'warning',
    'OTHER': ''
  }
  return typeMap[category] || ''
}

function getIssueCategoryText(category) {
  const textMap = {
    'GENERAL': '一般问题',
    'CRITICAL': '红线问题',
    'URGENT': '紧急问题',
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
  currentEditingFix.value = null
  fixDialogVisible.value = true
}

// 编辑整改记录
function handleEditFix(fix) {
  if (!fix) {
    proxy.$modal.msgError('数据错误')
    return
  }
  currentEditingFix.value = fix
  fixDialogVisible.value = true
}

// 删除整改记录
async function handleDeleteFix(fix) {
  if (!fix) {
    proxy.$modal.msgError('数据错误')
    return
  }

  try {
    await proxy.$confirm('确定要删除这条整改记录吗？此操作不可恢复！', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const loading = proxy.$loading({
      lock: true,
      text: '正在删除...',
      background: 'rgba(0, 0, 0, 0.7)',
    })

    try {
      const response = await delQualityFixes([fix.id])
      console.log('🔍 [DELETE FIX] 删除整改记录响应:', response)

      proxy.$modal.msgSuccess('整改记录删除成功')

      // 重新加载整改记录
      await loadIssueFixes()

    } finally {
      loading.close()
    }

  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除整改记录失败:', error)
      proxy.$modal.msgError('删除失败：' + (error.msg || error.message))
    }
  }
}

function handleFixSuccess(fixData) {
  console.log('🔍 [FIXES DRAWER] 整改成功:', fixData)
  proxy.$modal.msgSuccess('整改提交成功')
  fixDialogVisible.value = false
  loadIssueFixes() // 重新加载整改记录
  emit('fix-success', fixData)
}

function handleFixError(error) {
  console.error('整改提交失败:', error)
  proxy.$modal.msgError('整改提交失败：' + (error.msg || error.message))
  emit('fix-error', error)
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
</script>

<style scoped lang="scss">
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;

  .header-title {
    display: flex;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .header-stats {
    display: flex;
    align-items: center;
  }
}

.drawer-content {
  padding: 0 16px;
  height: 100%;
  overflow-y: auto;

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    padding-bottom: 6px;
    border-bottom: 1px solid #e8e8e8;
  }
}

.issue-info-section {
  margin-bottom: 20px;

  .issue-card {
    background: #fff;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    padding: 14px;

    .issue-title-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 10px;

      .issue-title {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        flex: 1;
        margin-right: 12px;
      }

      .issue-meta {
        display: flex;
        align-items: center;
        flex-shrink: 0;
      }
    }

    .issue-description {
      .detail-item {
        margin-bottom: 6px;
        font-size: 13px;

        &.detail-item-full {
          display: flex;

          .detail-label {
            color: #666;
            margin-right: 8px;
            min-width: 50px;
            flex-shrink: 0;
          }

          .detail-value {
            color: #303133;
            flex: 1;
          }
        }

        &.detail-item-inline {
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          gap: 6px;

          .detail-chunk {
            display: flex;
            align-items: center;
            gap: 4px;
            flex-shrink: 0;

            .detail-label {
              color: #666;
              font-size: 12px;
              white-space: nowrap;
            }

            .detail-value {
              color: #303133;
              font-size: 12px;
              font-weight: 500;

              &.overdue {
                color: #ff4d4f;
                font-weight: 600;
              }
            }
          }

          .detail-separator {
            color: #ccc;
            font-size: 14px;
            margin: 0 2px;
            flex-shrink: 0;
          }
        }
      }
    }

    .issue-images {
      margin-top: 10px;

      .images-title {
        font-size: 13px;
        color: #666;
        margin-bottom: 6px;
      }

      .images-preview {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;

        .preview-image {
          cursor: pointer;
          transition: all 0.3s ease;
          border-radius: 6px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);

          &:hover {
            transform: scale(1.08);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            z-index: 10;
          }
        }
      }
    }
  }
}

.action-section {
  margin-bottom: 24px;
}

.fixes-section {
  .empty-fixes {
    padding: 20px 0;
    text-align: center;

    .empty-description {
      .empty-text {
        margin: 8px 0 4px 0;
        font-size: 14px;
        color: #606266;
        font-weight: 500;
      }

      .empty-hint {
        margin: 0 0 16px 0;
        font-size: 12px;
        color: #909399;
        line-height: 1.4;
      }
    }
  }

  .fix-timeline-content {
    background: #fafafa;
    border-radius: 6px;
    padding: 10px;
    margin-bottom: 6px;

    .fix-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 6px;

      .fix-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 500;
        color: #303133;

        .fix-id {
          font-size: 12px;
          color: #999;
          background: #f0f0f0;
          padding: 2px 6px;
          border-radius: 3px;
        }
      }

      .fix-meta {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 4px;
        font-size: 12px;
        color: #666;

        .fix-actions {
          display: flex;
          align-items: center;
          gap: 4px;
          margin-top: 2px;
        }
      }
    }

    .fix-description {
      font-size: 13px;
      color: #303133;
      line-height: 1.4;
      margin-bottom: 6px;
    }

    .fix-completed-time {
      display: flex;
      align-items: center;
      font-size: 12px;
      color: #52c41a;
      margin-bottom: 6px;
    }

    .fix-images {
      margin-top: 6px;

      .images-label {
        font-size: 12px;
        color: #666;
        margin-bottom: 4px;
      }

      .images-grid {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;

        .preview-image {
          cursor: pointer;
          transition: all 0.3s ease;
          border-radius: 6px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);

          &:hover {
            transform: scale(1.08);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            z-index: 10;
          }
        }
      }
    }
  }
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px;
  border-top: 1px solid #e8e8e8;
}

// 响应式设计
@media (max-width: 768px) {
  .drawer-content {
    padding: 0 12px;
  }

  .issue-info-section {
    margin-bottom: 16px;

    .issue-card {
      padding: 12px;

      .issue-title-row {
        margin-bottom: 8px;

        .issue-title {
          font-size: 15px;
        }
      }

      .issue-description {
        .detail-item {
          font-size: 12px;
          margin-bottom: 4px;

          &.detail-item-full {
            .detail-label {
              min-width: 45px;
            }
          }

          &.detail-item-inline {
            flex-direction: column;
            align-items: flex-start;
            gap: 3px;

            .detail-chunk {
              .detail-label {
                font-size: 11px;
              }

              .detail-value {
                font-size: 11px;
              }
            }

            .detail-separator {
              display: none; // 在移动端隐藏分隔符
            }
          }
        }
      }

      .issue-images {
        margin-top: 8px;

        .images-preview {
          .preview-image {
            width: 60px !important;
            height: 60px !important;
          }
        }
      }
    }
  }

  .fixes-section {
    .fix-timeline-content {
      padding: 8px;

      .fix-header {
        margin-bottom: 4px;

        .fix-title {
          font-size: 13px;
        }

        .fix-meta {
          font-size: 11px;
        }
      }

      .fix-description {
        font-size: 12px;
        margin-bottom: 4px;
      }

      .fix-completed-time {
        font-size: 11px;
        margin-bottom: 4px;
      }

      .fix-images {
        .images-grid {
          .preview-image {
            width: 50px !important;
            height: 50px !important;
          }
        }
      }
    }
  }

  .drawer-footer {
    padding: 12px;
    gap: 6px;
  }
}

@media (max-width: 480px) {
  .issue-card {
    .issue-images {
      .images-preview {
        .preview-image {
          width: 50px !important;
          height: 50px !important;
        }
      }
    }
  }

  .fix-timeline-content {
    .fix-images {
      .images-grid {
        .preview-image {
          width: 40px !important;
          height: 40px !important;
        }
      }
    }
  }
}
</style>