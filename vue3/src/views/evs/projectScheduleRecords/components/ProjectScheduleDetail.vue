<template>
  <el-col :span="16" class="right-col">
    <el-card shadow="never" v-if="project" class="project-detail-card">
      <!-- 固定区域：项目头部、统计卡片、整体进度条 -->
      <div class="project-detail-fixed">
        <!-- 项目头部信息 -->
        <div class="project-detail-header">
          <div class="project-title-section">
            <h3 class="project-title">{{ project.name }}</h3>
            <dict-tag :options="decoration_project_status" :value="project.status" />
          </div>
          <div class="project-info">
            <div class="info-item">
              <span class="info-label">工地地址：</span>
              <span class="info-value">{{ project.address || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">开工日期：</span>
              <span class="info-value">{{ parseTime(project.startDate, '{y}-{m}-{d}') }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">预计完工：</span>
              <span class="info-value">{{ parseTime(project.endDate, '{y}-{m}-{d}') }}</span>
            </div>
          </div>
        </div>

        <!-- 进度统计卡片 -->
        <!-- <el-row :gutter="16" class="stat-cards-row">
          <el-col :span="8">
            <el-card shadow="never" class="stat-card stat-card-blue">
              <div class="stat-content">
                <div class="stat-label">总进度</div>
                <div class="stat-value">{{ getProjectProgress(project) }}%</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="stat-card stat-card-green">
              <div class="stat-content">
                <div class="stat-label">已完成</div>
                <div class="stat-value">{{ getCompletedCount(project) }}/{{ getTotalCount(project) }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="stat-card stat-card-orange">
              <div class="stat-content">
                <div class="stat-label">进行中</div>
                <div class="stat-value">{{ getInProgressCount(project) }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row> 

        <div class="overall-progress-section">
          <div class="progress-header">
            <span class="progress-label">整体进度</span>
            <span class="progress-percentage">{{ getProjectProgress(project) }}%</span>
          </div>
          <el-progress
            :percentage="getProjectProgress(project)"
            :stroke-width="12"
            :show-text="false"
            style="margin-bottom: 8px;"
          />
          <div class="progress-footer">
            <span>{{ getProjectProgress(project) }}%</span>
          </div>
        </div>
      -->
      </div>

      <!-- 滚动区域：进度时间轴 -->
      <div class="timeline-section timeline-scrollable">
        <div class="timeline-title">项目进度时间轴</div>
        <div v-loading="loading" class="timeline-content">
          <el-timeline v-if="scheduleItems.length > 0">
            <el-timeline-item
              v-for="item in scheduleItems"
              :key="item.id"
              :color="getTimelineColor(item.status)"
              :icon="getTimelineIcon(item.status)"
              size="large"
            >
              <div class="timeline-item-content">
                <div class="timeline-item-header">
                  <div class="timeline-item-title-section">
                    <el-tag 
                      :type="item.stageType === 'DESIGN' ? 'warning' : 'primary'" 
                      size="small"
                      class="stage-type-tag"
                    >
                      {{ item.stageType === 'DESIGN' ? '设计' : '施工' }}
                    </el-tag>
                    <span class="timeline-item-title">
                      {{ getScheduleStageName(item.stage, item.stageType) }}
                    </span>
                  </div>
                  <el-tag :type="getTimelineTagType(item.status)" size="small">
                    {{ getTimelineStatusLabel(item.status) }}
                  </el-tag>
                </div>
                <div class="timeline-item-description">{{ item.description || '暂无描述' }}</div>

                <!-- 验收记录展示 -->
                <div class="acceptance-records-section">
                  <div class="acceptance-records-header">
                    <!-- 左侧：标题和统计 -->
                    <div class="header-left">
                      <span class="acceptance-label">验收记录</span>
                      <el-tag v-if="getAcceptanceRecords(item.id).length > 0" size="small" type="success">
                        {{ getAcceptanceRecords(item.id).length }} 次验收
                      </el-tag>
                      <span v-else class="no-acceptance-text">暂无验收</span>
                    </div>

                    <!-- 右侧：操作按钮，与标签平级 -->
                    <div class="header-actions">
                      <el-button
                        type="primary"
                        size="small"
                        text
                        @click="handleViewAcceptanceRecords(item)"
                      >
                        <el-icon style="margin-right: 2px;"><View /></el-icon>
                        查看详情
                      </el-button>
                      <el-button
                        type="primary"
                        size="small"
                        @click="$emit('acceptance-report', item)"
                      >
                        <el-icon style="margin-right: 2px;"><Upload /></el-icon>
                        验收上报
                      </el-button>
                    </div>
                  </div>

                  <!-- 简化的统计信息 -->
                  <div v-if="getAcceptanceRecords(item.id).length > 0" class="acceptance-stats">
                    <el-icon class="acceptance-icon"><DocumentChecked /></el-icon>
                    <span class="acceptance-text">最近验收：{{ parseTime(getLatestAcceptanceTime(item.id), '{y}-{m}-{d} {h}:{i}') }}</span>
                  </div>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无项目进度" :image-size="100" />
        </div>
      </div>
    </el-card>

    <!-- 未选择项目时的提示 -->
    <el-card shadow="never" v-else class="project-detail-card project-detail-card-empty">
      <el-empty description="请从左侧选择一个项目查看详情" :image-size="120" />
    </el-card>

    <!-- 验收记录抽屉 -->
    <AcceptanceRecordsDrawer
      v-model:visible="acceptanceDrawerVisible"
      :schedule-item="selectedScheduleItem"
      :upload-url="uploadUrl"
      @acceptance-success="handleAcceptanceSuccess"
      @acceptance-error="handleAcceptanceError"
    />
  </el-col>
</template>

<script setup name="ProjectScheduleDetail">
import { Calendar, Plus, Check, Close, Edit, Delete, Picture, Clock, User, DocumentChecked, View, Upload } from "@element-plus/icons-vue"
import { parseTime } from "@/utils/ruoyi"
import { listProjectScheduleRecords, delProjectScheduleRecords } from "@/api/evs/projectScheduleRecords"
import { getCurrentInstance } from "vue"
import AcceptanceRecordsDrawer from "./AcceptanceRecordsDrawer.vue"

const { proxy } = getCurrentInstance()
const { decoration_project_status, decoration_construction_stage, decoration_design_stage } = proxy.useDict('decoration_project_status', 'decoration_construction_stage', 'decoration_design_stage')

const props = defineProps({
  project: {
    type: Object,
    default: null
  },
  scheduleItems: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['acceptance-report', 'edit-acceptance', 'delete-acceptance', 'records-updated', 'refresh-complete', 'refresh-error'])

// 验收记录数据
const acceptanceRecords = ref([])
const recordsLoading = ref(false)

// 验收记录抽屉相关数据
const acceptanceDrawerVisible = ref(false)
const selectedScheduleItem = ref(null)
const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/common/upload'

// 工具函数
function getProjectSchedules(project) {
  if (!project) return []
  return props.scheduleItems
}

// 创建图片解析的计算属性缓存
const recordImageCache = new Map()

function getParsedRecordImages(imagesJson) {
  if (recordImageCache.has(imagesJson)) {
    return recordImageCache.get(imagesJson)
  }

  try {
    const images = JSON.parse(imagesJson)
    recordImageCache.set(imagesJson, images)
    return images
  } catch (error) {
    console.error('解析图片JSON失败:', error)
    return []
  }
}

function getProjectProgress(project) {
  if (!project) return 0
  const schedules = getProjectSchedules(project)
  if (!schedules.length) return 0
  const total = schedules.length
  const completed = schedules.filter(item => item.status === 'COMPLETED').length
  return total > 0 ? Math.round((completed / total) * 100) : 0
}

function getTimelineColor(status) {
  const colorMap = {
    'COMPLETED': '#52c41a',
    'IN_PROGRESS': '#1677ff',
    'PLANNED': '#d9d9d9'
  }
  return colorMap[status] || '#d9d9d9'
}

function getTimelineIcon(status) {
  if (status === 'COMPLETED') {
    return 'Check'
  } else if (status === 'IN_PROGRESS') {
    return 'Clock'
  }
  return ''
}

function getTimelineTagType(status) {
  const typeMap = {
    'COMPLETED': 'success',
    'IN_PROGRESS': 'primary',
    'PLANNED': 'info'
  }
  return typeMap[status] || 'info'
}

function getTimelineStatusLabel(status) {
  const labelMap = {
    'COMPLETED': '已完成',
    'IN_PROGRESS': '进行中',
    'PLANNED': '待开始'
  }
  return labelMap[status] || '待开始'
}

function getScheduleStageName(stage, stageType) {
  // 根据阶段类型选择对应的字典
  const dictData = stageType === 'DESIGN' ? decoration_design_stage.value : decoration_construction_stage.value
  const stageDict = dictData.find(dict => dict.value === stage)
  return stageDict?.label || stage
}

/** 加载验收记录 - 增强版本 */
async function loadAcceptanceRecords(projectId, forceRefresh = false, cacheKey = null) {
  if (!projectId) return

  // 添加请求标识符
  const requestId = cacheKey || `load-acceptance-${projectId}-${Date.now()}`

  // ✅ 优化：每次都清空旧数据，确保数据新鲜
  console.log(`[验收记录] 清空旧数据，项目ID: ${projectId}`)
  acceptanceRecords.value = []
  recordImageCache.clear()

  console.log(`[验收记录] 开始加载验收记录，项目ID: ${projectId}，请求ID: ${requestId}`)
  recordsLoading.value = true

  try {
    const response = await listProjectScheduleRecords({
      projectId: projectId,
      recordType: 'ACCEPTANCE',
      pageNum: 1,
      pageSize: 100,
      _t: Date.now(),  // 添加时间戳防止缓存
      _requestId: requestId  // 添加请求标识符
    })

    console.log('[验收记录] API响应成功，记录数量:', response.rows?.length || 0)
    console.log('[验收记录] API返回数据:', response.rows)

    acceptanceRecords.value = response.rows || []

    // 输出验收记录详情
    console.log('[验收记录] 加载完成，验收记录详情:', acceptanceRecords.value.map(r => ({
      id: r.id,
      scheduleId: r.scheduleId,
      title: r.acceptanceTitle || '无标题',
      acceptor: r.acceptor || '未知',
      time: r.acceptanceTime
    })))

    // 通知父组件数据已更新
    emit('records-updated', acceptanceRecords.value)
    emit('refresh-complete', { requestId, newRecords: acceptanceRecords.value })

  } catch (error) {
    console.error('[验收记录] 加载验收记录失败:', error)
    proxy.$modal.msgError('加载验收记录失败: ' + (error.message || error.msg))

    emit('refresh-error', { requestId, error })

  } finally {
    recordsLoading.value = false
    console.log(`[验收记录] 请求完成，请求ID: ${requestId}`)
  }
}

/** 获取指定进度的验收记录 */
function getAcceptanceRecords(scheduleId) {
  const records = acceptanceRecords.value
    .filter(record => record.scheduleId === scheduleId)
    .sort((a, b) => new Date(b.acceptanceTime) - new Date(a.acceptanceTime))
  return records
}

/** 获取指定进度的最新验收时间 */
function getLatestAcceptanceTime(scheduleId) {
  const records = getAcceptanceRecords(scheduleId)
  return records.length > 0 ? records[0].acceptanceTime : null
}

/** 获取图片URL */
function getImageUrl(imgPath) {
  if (!imgPath) return ''
  if (imgPath.startsWith('http')) {
    return imgPath
  }

  const baseUrl = import.meta.env.VITE_APP_BASE_API
  // 检查路径是否已经包含 baseUrl，避免重复拼接
  if (imgPath.startsWith(baseUrl)) {
    return imgPath
  }

  // 确保路径以/开头
  let path = imgPath
  if (!path.startsWith('/')) {
    path = '/' + path
  }

  // 拼接baseUrl（移除末尾的/）
  const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
  return cleanBaseUrl + path
}

/** 删除验收记录 */
function handleDeleteAcceptance(record) {
  // 参数验证
  if (!record || !record.id) {
    proxy.$modal.msgError('无效的验收记录')
    return
  }

  // 显示加载状态
  const loading = proxy.$loading({
    lock: true,
    text: '正在删除...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  proxy.$modal.confirm(`确定要删除验收记录"${record.acceptanceTitle || '未命名'}"吗？`)
    .then(async () => {
      try {
        // 调用删除API
        await delProjectScheduleRecords(record.id)
        proxy.$modal.msgSuccess('删除成功')

        // 刷新验收记录列表
        if (props.project?.id) {
          await loadAcceptanceRecords(props.project.id)
        }

        // 详细操作日志
        console.log('验收记录删除成功', {
          recordId: record.id,
          recordTitle: record.acceptanceTitle,
          timestamp: new Date().toISOString()
        })

      } catch (error) {
        console.error('删除验收记录失败:', error)

        // 更详细的错误信息处理
        let errorMsg = '删除失败'
        if (error.response) {
          // 服务器返回的错误
          errorMsg = error.response.data?.msg || error.response.data?.message || `服务器错误(${error.response.status})`
        } else if (error.request) {
          // 网络错误
          errorMsg = '网络连接失败，请检查网络后重试'
        } else if (error.msg) {
          // 业务逻辑错误
          errorMsg = error.msg
        } else if (error.message) {
          // 其他错误
          errorMsg = error.message
        }

        proxy.$modal.msgError(errorMsg)

        // 详细错误记录
        console.error('删除验收记录详细错误信息:', {
          recordId: record.id,
          errorType: error.constructor.name,
          errorMessage: error.message,
          errorResponse: error.response?.data,
          timestamp: new Date().toISOString()
        })

      } finally {
        // 确保loading状态被重置
        loading.close()
      }
    })
    .catch(() => {
      // 用户取消删除，也要关闭loading
      loading.close()
      console.log('用户取消删除验收记录:', {
        recordId: record.id,
        recordTitle: record.acceptanceTitle,
        timestamp: new Date().toISOString()
      })
    })
}

/** 获取验收记录图片列表（用于ImagePreview组件） */
function getRecordImages(imagesJson) {
  try {
    const images = JSON.parse(imagesJson)
    if (!images || images.length === 0) {
      return []
    }
    // 返回处理后的完整URL列表
    return images.map(img => getImageUrl(img))
  } catch (error) {
    console.error('解析验收记录图片失败:', error)
    return []
  }
}

/** 获取单个验收记录的v-viewer配置选项 */
function getImageViewerOptions(imagesJson) {
  const images = getRecordImages(imagesJson)
  return {
    // 工具栏
    toolbar: true,
    // 显示缩放按钮
    zoomOn: true,
    // 显示缩小按钮
    zoomOff: true,
    // 显示旋转按钮
    rotateOn: true,
    // 显示翻转按钮
    flipHOn: true,
    // 显示全屏按钮
    fullScreen: true,
    // 显示上一张按钮
    prev: true,
    // 显示下一张按钮
    next: true,
    // 显示重置按钮
    reset: true,
    // 显示下载按钮
    download: true,

    // 导航栏
    navbar: true,
    // 标题
    title: false,
    // 按钮提示
    tooltip: true,

    // 可移动
    movable: true,
    // 可缩放
    zoomable: true,
    // 可旋转
    rotatable: true,
    // 可翻转
    flip: true,

    // 动画
    transition: true,

    // 键盘导航
    keyboard: true,

    // 循环浏览
    loop: true,

    // 最小缩放比例
    minZoomRatio: 0.1,
    // 最大缩放比例
    maxZoomRatio: 5,

    // z-index
    zIndex: 3000,

    // URL 默认是当前激活的图片
    url: (image) => image.src || image,

    // 确保图片正确传递
    images: images
  }
}

// 监听项目变化，自动加载验收记录
watch(() => props.project?.id, (newVal, oldVal) => {
  console.log(`[项目切换] 从 ${oldVal} 切换到 ${newVal}`)
  if (newVal) {
    // ✅ 优化：每次切换项目都重新获取验收记录
    loadAcceptanceRecords(newVal, false, 'watch-' + Date.now())
  } else {
    acceptanceRecords.value = []
  }
}, { immediate: true })

// 查看验收记录详情
function handleViewAcceptanceRecords(scheduleItem) {
  selectedScheduleItem.value = scheduleItem
  acceptanceDrawerVisible.value = true
}

// 验收记录成功回调
function handleAcceptanceSuccess(acceptanceData) {
  console.log('验收记录成功:', acceptanceData)
  // 重新加载验收记录
  if (props.project?.id) {
    loadAcceptanceRecords(props.project.id)
  }
}

// 验收记录错误回调
function handleAcceptanceError(error) {
  console.error('验收记录失败:', error)
}

// 暴露给父组件使用
defineExpose({
  refreshAcceptanceRecords: () => {
    if (props.project?.id) {
      console.log('[验收记录] 手动刷新验收记录，项目ID:', props.project.id)
      // ✅ 优化：每次刷新都重新获取数据
      loadAcceptanceRecords(props.project.id, false, 'manual-' + Date.now())
    } else {
      console.warn('无法刷新验收记录：项目ID为空')
    }
  },

  // 添加强制刷新方法，绕过缓存
  forceRefreshAcceptanceRecords: () => {
    if (props.project?.id) {
      console.log('[验收记录] 强制刷新验收记录')
      // ✅ 优化：每次都重新获取数据
      loadAcceptanceRecords(props.project.id, false, 'force-' + Date.now())
    }
  },

  // 添加等待新记录的方法
  waitForNewRecord: (recordId, timeout = 5000) => {
    return new Promise((resolve, reject) => {
      const startTime = Date.now()
      const checkInterval = setInterval(() => {
        // 刷新数据
        loadAcceptanceRecords(props.project.id, false, 'wait-' + Date.now())

        // 检查是否包含新记录
        const newRecord = acceptanceRecords.value.find(r => r.id === recordId)
        if (newRecord) {
          clearInterval(checkInterval)
          resolve(newRecord)
        } else if (Date.now() - startTime > timeout) {
          clearInterval(checkInterval)
          reject(new Error('等待新记录超时'))
        }
      }, 500) // 每500ms检查一次
    })
  }
})
</script>

<style scoped lang="scss">
.right-col {
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  overflow: hidden;
}

.project-detail-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  overflow: hidden;

  :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    padding: 20px;
    box-sizing: border-box;
    overflow: hidden;
    position: relative;
    height: 0;
  }

  &.project-detail-card-empty {
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .project-detail-fixed {
    flex-shrink: 0;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e8e8e8;
    overflow: visible;
  }

  .project-detail-header {
    margin-bottom: 16px;
    padding-bottom: 0;
    border-bottom: none;

    .project-title-section {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;

      .project-title {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #303133;
      }
    }

    .project-info {
      display: flex;
      flex-wrap: wrap;
      gap: 24px;

      .info-item {
        font-size: 14px;

        .info-label {
          color: #666;
        }

        .info-value {
          color: #303133;
          font-weight: 500;
        }
      }
    }
  }

  .stat-cards-row {
    margin-bottom: 16px;

    :deep(.el-col) {
      display: flex;
      height: auto;
    }
  }

  .stat-card {
    flex: 1;
    text-align: center;
    padding: 12px 8px;
    height: auto;
    min-height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;

    :deep(.el-card__body) {
      padding: 0;
      width: 100%;
    }

    &.stat-card-blue {
      background: #e6f7ff;
      border-color: #91d5ff;
    }

    &.stat-card-green {
      background: #f6ffed;
      border-color: #b7eb8f;
    }

    &.stat-card-orange {
      background: #fff7e6;
      border-color: #ffd591;
    }

    .stat-content {
      width: 100%;
      .stat-label {
        font-size: 12px;
        color: #666;
        margin-bottom: 4px;
      }

      .stat-value {
        font-size: 18px;
        font-weight: 600;
        line-height: 1.2;
      }
    }

    &.stat-card-blue .stat-content .stat-value {
      color: #1677ff;
    }

    &.stat-card-green .stat-content .stat-value {
      color: #52c41a;
    }

    &.stat-card-orange .stat-content .stat-value {
      color: #fa8c16;
    }
  }

  .overall-progress-section {
    margin-bottom: 0;
    padding: 12px 16px;
    background: #fafafa;
    border-radius: 8px;

    .progress-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .progress-label {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
      }

      .progress-percentage {
        font-size: 14px;
        font-weight: 600;
        color: #1677ff;
      }
    }

    .progress-footer {
      text-align: center;
      font-size: 12px;
      color: #666;
      margin-top: 4px;
    }
  }

  .timeline-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;
    position: relative;
    height: 0;

    &.timeline-scrollable {
      .timeline-title {
        flex-shrink: 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 12px;
        padding-bottom: 8px;
        border-bottom: 1px solid #e8e8e8;
      }

      .timeline-content {
        flex: 1;
        overflow-y: auto;
        overflow-x: hidden;
        min-height: 0;
        padding-right: 8px;
        margin-top: 0;
        height: 100%;

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
    }

    .timeline-item-content {
      background: #fafafa;
      padding: 16px;
      border-radius: 8px;
      margin-bottom: 12px;

      .timeline-item-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .timeline-item-title-section {
          display: flex;
          align-items: center;
          gap: 8px;

          .stage-type-tag {
            flex-shrink: 0;
          }

          .timeline-item-title {
            font-size: 15px;
            font-weight: 600;
            color: #303133;
          }
        }
      }

      .timeline-item-description {
        color: #666;
        font-size: 14px;
        margin-bottom: 8px;
      }

      .timeline-item-date {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #999;
        margin-bottom: 8px;
      }

      // 验收记录简化样式
      .acceptance-records-section {
        margin-top: 12px;
        padding-top: 12px;
        border-top: 1px dashed #e8e8e8;

        .acceptance-records-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          .header-left {
            display: flex;
            align-items: center;
            gap: 8px;

            .acceptance-label {
              font-size: 14px;
              font-weight: 600;
              color: #303133;
            }

            .no-acceptance-text {
              font-size: 12px;
              color: #999;
              font-style: italic;
            }
          }

          .header-actions {
            display: flex;
            gap: 8px;
            margin-left: auto;
          }
        }

        }
    }
  }
}
</style>
