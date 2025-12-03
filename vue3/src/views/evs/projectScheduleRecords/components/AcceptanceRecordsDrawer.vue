<template>
  <el-drawer
    v-model="drawerVisible"
    title="验收记录详情"
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
        <div class="header-stats" v-if="acceptanceRecords.length > 0">
          <el-tag type="info" size="small">{{ acceptanceRecords.length }}次验收</el-tag>
          <el-tag
            :type="getLatestStatusType()"
            size="small"
            style="margin-left: 8px;"
          >
            {{ getLatestStatusText() }}
          </el-tag>
        </div>
      </div>
    </template>

    <div class="drawer-content" v-loading="loading">
      <!-- 进度节点信息区域 -->
      <div v-if="currentScheduleItem" class="schedule-info-section">
        <div class="section-header">
          <el-icon style="color: #409eff;"><Clock /></el-icon>
          <span>进度节点信息</span>
        </div>
        <div class="schedule-card">
          <!-- 头部：标题 + 状态标签 -->
          <div class="card-header">
            <h3 class="card-title">{{ currentScheduleItem.taskName || '进度节点' }}</h3>
            <div class="card-tags">
              <el-tag :type="getScheduleStatusType(currentScheduleItem.status)" size="small">
                {{ getScheduleStatusText(currentScheduleItem.status) }}
              </el-tag>
              <el-tag type="primary" size="small" style="margin-left: 6px;">
                {{ getSchedulePhaseText(currentScheduleItem.phase) }}
              </el-tag>
            </div>
          </div>

          <!-- 基本信息区块 -->
          <div class="info-section">
            <div class="section-title">基本信息</div>
            <div class="field-grid">
              <div class="field-item full-width">
                <span class="field-label">任务描述</span>
                <span class="field-value">{{ currentScheduleItem.description || '暂无描述' }}</span>
              </div>
              <div class="field-item">
                <span class="field-label">计划时间</span>
                <span class="field-value">{{ formatDateTimeRange(currentScheduleItem.startDate, currentScheduleItem.endDate) }}</span>
              </div>
              <div class="field-item">
                <span class="field-label">实际时间</span>
                <span class="field-value">{{ formatDateTimeRange(currentScheduleItem.actualStartDate, currentScheduleItem.actualEndDate) }}</span>
              </div>
            </div>
          </div>

          <!-- 执行信息区块 -->
          <div class="info-section">
            <div class="section-title">执行信息</div>
            <div class="field-grid">
              <div class="field-item">
                <span class="field-label">负责人</span>
                <span class="field-value">{{ currentScheduleItem.responsible || '未指定' }}</span>
              </div>
              <div class="field-item">
                <span class="field-label">进度</span>
                <div class="progress-wrapper">
                  <el-progress
                    :percentage="currentScheduleItem.progress || 0"
                    :color="getProgressColor(currentScheduleItem.progress)"
                    size="small"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 验收记录时间轴 -->
      <div class="acceptance-section">
        <div class="section-header">
          <el-icon style="color: #52c41a;"><DocumentChecked /></el-icon>
          <span>验收时间轴</span>
        </div>

        <div v-if="acceptanceRecords.length === 0" class="empty-acceptances">
          <el-empty
            description="暂无验收记录"
            :image-size="100"
          >
            <template #description>
              <div class="empty-description">
                <p class="empty-text">该进度节点还没有验收记录</p>
                <p class="empty-hint">点击下方"提交验收"按钮开始记录验收过程</p>
              </div>
            </template>
            <el-button
              v-if="currentScheduleItem"
              type="primary"
              size="small"
              @click="handleAddAcceptance"
            >
              <el-icon style="margin-right: 4px;"><Plus /></el-icon>
              提交验收
            </el-button>
          </el-empty>
        </div>

        <el-timeline v-else>
          <el-timeline-item
            v-for="(record, index) in acceptanceRecords"
            :key="record.id || index"
            :color="getAcceptanceTimelineColor(record.acceptanceResult)"
            :icon="getAcceptanceTimelineIcon(record.acceptanceResult)"
            size="large"
            :timestamp="proxy.parseTime(record.acceptanceTime, '{y}-{m}-{d} {h}:{i}')"
            placement="top"
          >
            <div class="acceptance-timeline-content">
              <div class="acceptance-header">
                <div class="acceptance-title">
                  <el-tag :type="getAcceptanceResultType(record.acceptanceResult)" size="small">
                    {{ getAcceptanceResultText(record.acceptanceResult) }}
                  </el-tag>
                  <span class="acceptance-id" v-if="record.id">记录 #{{ record.id.substring(0, 8) }}</span>
                </div>
                <div class="acceptance-meta">
                  <span class="acceptance-creator" v-if="record.acceptor">{{ record.acceptor }}</span>
                  <span class="acceptance-time">{{ proxy.parseTime(record.acceptanceTime, '{m}-{d} {h}:{i}') }}</span>
                  <!-- 管理员操作按钮 -->
                  <div v-if="isAdmin" class="timeline-actions">
                    <el-button
                      type="primary"
                      size="small"
                      text
                      @click="handleEditAcceptance(record)"
                    >
                      <el-icon style="margin-right: 2px;"><Edit /></el-icon>
                      编辑
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      text
                      @click="handleDeleteAcceptance(record)"
                    >
                      <el-icon style="margin-right: 2px;"><Delete /></el-icon>
                      删除
                    </el-button>
                  </div>
                </div>
              </div>

              <div class="acceptance-description">
                {{ record.acceptanceContent || '无详细备注' }}
              </div>

              <!-- 验收评分 -->
              <div v-if="record.score !== null && record.score !== undefined" class="acceptance-score">
                <el-icon style="color: #f5a623; margin-right: 4px;"><Star /></el-icon>
                验收评分：{{ record.score }}/100 分
              </div>

              <!-- 验收图片 -->
              <div v-if="hasAcceptanceImages(record)" class="acceptance-images">
                <div class="images-label">验收图片：</div>
                <div class="images-grid">
                  <el-image
                    v-for="(img, imgIndex) in getAcceptanceImages(record)"
                    :key="imgIndex"
                    :src="getImageUrl(img)"
                    :preview-src-list="getAcceptanceImageUrls(record)"
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
          v-if="currentScheduleItem"
          type="primary"
          @click="handleAddAcceptance"
        >
          <el-icon style="margin-right: 4px;"><Plus /></el-icon>
          提交验收
        </el-button>
      </div>
    </template>
  </el-drawer>

  <!-- 验收报告对话框 -->
  <AcceptanceReportDialog
    v-model:visible="acceptanceDialogVisible"
    :project="{ id: currentScheduleItem?.projectId }"
    :schedule-item="currentScheduleItem"
    :edit-record="currentEditingAcceptance"
    :upload-url="uploadUrl"
    :is-edit="!!currentEditingAcceptance"
    @success="handleAcceptanceSuccess"
    @error="handleAcceptanceError"
  />
</template>

<script setup name="AcceptanceRecordsDrawer">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { listProjectScheduleRecords, delProjectScheduleRecords } from '@/api/evs/projectScheduleRecords'
import { Check, Clock, DocumentChecked, Plus, Edit, Delete, Star } from '@element-plus/icons-vue'
import { getCurrentInstance } from 'vue'
import { useProjectAuth } from '@/utils/projectAuth'
import AcceptanceReportDialog from './AcceptanceReportDialog.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  scheduleItem: {
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

const emit = defineEmits(['update:visible', 'acceptance-success', 'acceptance-error'])

const { proxy } = getCurrentInstance()

// 权限控制
const { isAdmin } = useProjectAuth()

// 计算抽屉标题
const drawerTitle = computed(() => {
  return props.viewMode ? '详情查看' : '验收记录详情'
})

// 响应式数据
const drawerVisible = ref(false)
const loading = ref(false)
const acceptanceDialogVisible = ref(false)
const currentScheduleItem = ref(null)
const currentEditingAcceptance = ref(null)
const acceptanceRecords = ref([])
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

// 监听属性变化
watch(() => props.visible, (newVal) => {
  drawerVisible.value = newVal
  if (newVal && props.scheduleItem) {
    currentScheduleItem.value = props.scheduleItem
    loadAcceptanceRecords()
  }
})

watch(() => props.scheduleItem, (newVal) => {
  if (newVal) {
    currentScheduleItem.value = newVal
  }
})

watch(drawerVisible, (newVal) => {
  emit('update:visible', newVal)
})

// 加载验收记录
async function loadAcceptanceRecords() {
  if (!currentScheduleItem.value?.id) return

  loading.value = true
  try {
    console.log('🔍 [ACCEPTANCE DRAWER] 正在加载验收记录:', currentScheduleItem.value.id)
    const response = await listProjectScheduleRecords({
      scheduleId: currentScheduleItem.value.id,
      recordType: 'ACCEPTANCE',
      pageNum: 1,
      pageSize: 100
    })
    const records = response.rows || []

    // 按验收时间倒序排序
    records.sort((a, b) => new Date(b.acceptanceTime) - new Date(a.acceptanceTime))

    acceptanceRecords.value = records
    console.log('🔍 [ACCEPTANCE DRAWER] 已加载验收记录:', { count: records.length, records })
  } catch (error) {
    console.error('加载验收记录失败:', error)
    proxy.$modal.msgError('加载验收记录失败')
    acceptanceRecords.value = []
  } finally {
    loading.value = false
  }
}

// 获取验收图片
function hasAcceptanceImages(record) {
  if (!record?.images) return false

  try {
    const images = getAcceptanceImages(record)
    return images.length > 0
  } catch {
    return false
  }
}

function getAcceptanceImages(record) {
  if (!record?.images) return []

  try {
    let images = []
    const imageData = record.images

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
    console.warn('解析验收图片数据失败:', error)
    return []
  }
}

function getAcceptanceImageUrls(record) {
  return getAcceptanceImages(record).map(img => getImageUrl(img))
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

// 状态相关函数
function getScheduleStatusType(status) {
  const typeMap = {
    'NOT_STARTED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'DELAYED': 'danger',
    'ON_HOLD': ''
  }
  return typeMap[status] || 'info'
}

function getScheduleStatusText(status) {
  const textMap = {
    'NOT_STARTED': '未开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'DELAYED': '已延期',
    'ON_HOLD': '暂停'
  }
  return textMap[status] || '未知'
}

function getSchedulePhaseText(phase) {
  const textMap = {
    'DEMO': '拆除阶段',
    'WATER_ELECTRIC': '水电阶段',
    'MASONRY': '泥瓦阶段',
    'WOODWORK': '木工阶段',
    'PAINTING': '油漆阶段',
    'INSTALLATION': '安装阶段',
    'SOFT_DECORATION': '软装阶段',
    'ACCEPTANCE': '验收阶段'
  }
  return textMap[phase] || '未分类'
}

function getProgressColor(progress) {
  if (progress < 30) return '#f56c6c'
  if (progress < 70) return '#e6a23c'
  return '#67c23a'
}

function getAcceptanceResultType(result) {
  const typeMap = {
    'QUALIFIED': 'success',
    'UNQUALIFIED': 'danger',
    'PASS': 'success',
    'FAIL': 'danger',
    'CONDITIONAL_PASS': 'warning',
    'REWORK': 'info'
  }
  return typeMap[result] || 'info'
}

function getAcceptanceResultText(result) {
  const textMap = {
    'QUALIFIED': '合格',
    'UNQUALIFIED': '不合格',
    'PASS': '合格',
    'FAIL': '不合格',
    'CONDITIONAL_PASS': '有条件通过',
    'REWORK': '需返工'
  }
  return textMap[result] || '未知'
}

function getAcceptanceTimelineColor(result) {
  const colorMap = {
    'PASS': '#52c41a',
    'FAIL': '#ff4d4f',
    'CONDITIONAL_PASS': '#e6a23c',
    'REWORK': '#d9d9d9'
  }
  return colorMap[result] || '#d9d9d9'
}

function getAcceptanceTimelineIcon(result) {
  if (result === 'PASS') {
    return 'Check'
  } else if (result === 'FAIL') {
    return 'Close'
  } else if (result === 'CONDITIONAL_PASS') {
    return 'Warning'
  }
  return ''
}

function getLatestStatusType() {
  if (acceptanceRecords.value.length === 0) {
    return getScheduleStatusType(currentScheduleItem.value?.status)
  }

  const latestRecord = acceptanceRecords.value[0]
  return getAcceptanceResultType(latestRecord.acceptanceResult)
}

function getLatestStatusText() {
  if (acceptanceRecords.value.length === 0) {
    return getScheduleStatusText(currentScheduleItem.value?.status)
  }

  const latestRecord = acceptanceRecords.value[0]
  return getAcceptanceResultText(latestRecord.acceptanceResult)
}

// 操作函数
function handleAddAcceptance() {
  if (!currentScheduleItem.value) return
  currentEditingAcceptance.value = null
  acceptanceDialogVisible.value = true
}

// 编辑验收记录
function handleEditAcceptance(record) {
  if (!record) {
    proxy.$modal.msgError('数据错误')
    return
  }
  currentEditingAcceptance.value = record
  acceptanceDialogVisible.value = true
}

// 删除验收记录
async function handleDeleteAcceptance(record) {
  if (!record) {
    proxy.$modal.msgError('数据错误')
    return
  }

  try {
    await proxy.$confirm('确定要删除这条验收记录吗？此操作不可恢复！', '删除确认', {
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
      const response = await delProjectScheduleRecords([record.id])
      console.log('🔍 [DELETE ACCEPTANCE] 删除验收记录响应:', response)

      proxy.$modal.msgSuccess('验收记录删除成功')

      // 重新加载验收记录
      await loadAcceptanceRecords()

    } finally {
      loading.close()
    }

  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除验收记录失败:', error)
      proxy.$modal.msgError('删除失败：' + (error.msg || error.message))
    }
  }
}

function handleAcceptanceSuccess(acceptanceData) {
  console.log('🔍 [ACCEPTANCE DRAWER] 验收成功:', acceptanceData)
  proxy.$modal.msgSuccess('验收提交成功')
  acceptanceDialogVisible.value = false
  loadAcceptanceRecords() // 重新加载验收记录
  emit('acceptance-success', acceptanceData)
}

function handleAcceptanceError(error) {
  console.error('验收提交失败:', error)
  proxy.$modal.msgError('验收提交失败：' + (error.msg || error.message))
  emit('acceptance-error', error)
}

function handleClose() {
  drawerVisible.value = false
  currentScheduleItem.value = null
  acceptanceRecords.value = []
}

// 格式化时间范围
function formatDateTimeRange(startDate, endDate) {
  if (!startDate && !endDate) {
    return '未设定'
  }

  if (!startDate) {
    return `至 ${proxy.parseTime(endDate, '{y}-{m}-{d}')}`
  }

  if (!endDate) {
    return `从 ${proxy.parseTime(startDate, '{y}-{m}-{d}')} 开始`
  }

  return `${proxy.parseTime(startDate, '{y}-{m}-{d}')} 至 ${proxy.parseTime(endDate, '{y}-{m}-{d}')}`
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

.schedule-info-section {
  margin-bottom: 20px;

  .schedule-card {
    background: #fff;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    padding: 16px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;

      .card-title {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        flex: 1;
      }

      .card-tags {
        display: flex;
        gap: 6px;
        flex-shrink: 0;
      }
    }

    .info-section {
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .section-title {
        font-size: 12px;
        color: #666;
        font-weight: 500;
        margin-bottom: 8px;
        padding-bottom: 4px;
        border-bottom: 1px solid #f0f0f0;
      }

      .field-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px;

        .field-item {
          display: flex;
          align-items: center;

          &.full-width {
            grid-column: 1 / -1;
          }

          .field-label {
            width: 80px;
            flex-shrink: 0;
            font-size: 13px;
            color: #666;
            text-align: right;
            margin-right: 12px;
          }

          .field-value {
            flex: 1;
            font-size: 13px;
            color: #303133;
            font-weight: 500;
          }

          .progress-wrapper {
            flex: 1;
            min-width: 0; // 允许进度条自适应宽度
          }
        }
      }
    }
  }
}

.acceptance-section {
  .empty-acceptances {
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

  .acceptance-timeline-content {
    background: #fafafa;
    border-radius: 6px;
    padding: 10px;
    margin-bottom: 6px;

    .acceptance-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 6px;

      .acceptance-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 500;
        color: #303133;

        .acceptance-id {
          font-size: 12px;
          color: #999;
          background: #f0f0f0;
          padding: 2px 6px;
          border-radius: 3px;
        }
      }

      .acceptance-meta {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 4px;
        font-size: 12px;
        color: #666;

        .timeline-actions {

          .el-button {
          }
        }
      }
    }

    .acceptance-description {
      font-size: 13px;
      color: #303133;
      line-height: 1.4;
      margin-bottom: 6px;
    }

    .acceptance-score {
      display: flex;
      align-items: center;
      font-size: 12px;
      color: #f5a623;
      margin-bottom: 6px;
    }

    .acceptance-images {
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
  padding: 16px;
  border-top: 1px solid #e8e8e8;

  .el-button {
  }
}

// 响应式设计
@media (max-width: 768px) {
  .drawer-content {
    padding: 0 12px;
  }

  .schedule-info-section {
    margin-bottom: 16px;

    .schedule-card {
      padding: 12px;

      .card-header {
        margin-bottom: 12px;

        .card-title {
          font-size: 15px;
        }

        .card-tags {
          gap: 4px;
        }
      }

      .info-section {
        margin-bottom: 10px;

        .section-title {
          font-size: 11px;
          margin-bottom: 6px;
        }

        .field-grid {
          gap: 8px;
          grid-template-columns: 1fr; // 移动端改为单列布局

          .field-item {
            flex-direction: column;
            align-items: flex-start;
            gap: 4px;

            .field-label {
              width: auto;
              text-align: left;
              margin-right: 0;
              font-size: 12px;
            }

            .field-value {
              font-size: 12px;
            }

            .progress-wrapper {
              width: 100%;
            }
          }
        }
      }
    }
  }

  .acceptance-section {
    .acceptance-timeline-content {
      padding: 8px;

      .acceptance-header {
        margin-bottom: 4px;

        .acceptance-title {
          font-size: 13px;
        }

        .acceptance-meta {
          font-size: 11px;
        }
      }

      .acceptance-description {
        font-size: 12px;
        margin-bottom: 4px;
      }

      .acceptance-score {
        font-size: 11px;
        margin-bottom: 4px;
      }

      .acceptance-images {
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
  }
}

@media (max-width: 480px) {
  .schedule-info-section {
    .schedule-card {
      padding: 10px;

      .card-header {
        margin-bottom: 10px;

        .card-title {
          font-size: 14px;
        }

        .card-tags {
          gap: 3px;
        }
      }

      .info-section {
        margin-bottom: 8px;

        .section-title {
          font-size: 10px;
          margin-bottom: 4px;
        }

        .field-grid {
          gap: 6px;

          .field-item {
            .field-label {
              font-size: 11px;
            }

            .field-value {
              font-size: 11px;
            }
          }
        }
      }
    }
  }

  .acceptance-section {
    .acceptance-timeline-content {
      .acceptance-images {
        .images-grid {
          .preview-image {
            width: 40px !important;
            height: 40px !important;
          }
        }
      }
    }
  }
}
</style>