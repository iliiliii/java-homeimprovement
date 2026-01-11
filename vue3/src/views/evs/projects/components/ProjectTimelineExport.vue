<template>
  <!-- 导出长图对话框 -->
  <el-dialog
    v-model="dialogVisible"
    title="导出项目进度长图"
    width="800px"
    :close-on-click-modal="false"
    append-to-body
    class="export-dialog"
  >
    <div class="export-actions">
      <el-button type="primary" :loading="exporting" @click="handleExport">
        <el-icon><Download /></el-icon>
        {{ exporting ? '正在生成...' : '下载长图' }}
      </el-button>
      <el-button @click="handlePreview" :disabled="exporting">
        <el-icon><View /></el-icon>
        预览
      </el-button>
    </div>

    <!-- 预览区域 -->
    <div class="preview-container" v-loading="loading" element-loading-text="正在加载数据...">
      <div ref="exportContent" class="export-content">
        <!-- 头部：公司Logo和项目信息 -->
        <div class="export-header">
          <!-- 品牌栏 -->
          <div class="brand-bar">
            <img :src="logoUrl" class="company-logo" alt="公司Logo" />
            <span class="company-name">{{ companyName }}</span>
          </div>
          
          <!-- 项目名称 -->
          <div class="project-name">{{ project.name }}</div>
          
          <!-- 项目信息 -->
          <div class="project-info-list">
            <div class="info-item" v-if="project.area">
              <span class="info-label">面积</span>
              <span class="info-value">{{ project.area }}㎡</span>
            </div>
            <div class="info-item" v-if="project.address">
              <span class="info-label">地址</span>
              <span class="info-value">{{ project.address }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">工期</span>
              <span class="info-value">{{ formatDate(project.startDate) }} ~ {{ formatDate(project.actualEndDate || project.endDate) }}</span>
            </div>
          </div>
        </div>

        <!-- 时间轴内容 -->
        <div class="timeline-section">
          <div class="section-title">📋 项目进度时间轴</div>
          <div class="timeline" v-if="schedules.length > 0">
            <div 
              v-for="(schedule, index) in schedules" 
              :key="schedule.id" 
              class="timeline-node"
            >
              <!-- 时间线 -->
              <div class="timeline-line">
                <div 
                  class="timeline-dot" 
                  :class="getStatusClass(schedule.status)"
                ></div>
                <div 
                  v-if="index < schedules.length - 1" 
                  class="timeline-connector"
                  :class="getStatusClass(schedule.status)"
                ></div>
              </div>

              <!-- 节点内容 -->
              <div class="node-content">
                <div class="node-header">
                  <div class="node-left">
                    <span 
                      class="stage-type-tag" 
                      :class="schedule.stageType === 'DESIGN' ? 'type-design' : 'type-construction'"
                    >
                      {{ schedule.stageType === 'DESIGN' ? '设计' : '施工' }}
                    </span>
                    <span class="node-title" :title="getScheduleStageName(schedule)">
                      {{ getScheduleStageName(schedule) }}
                    </span>
                  </div>
                  <div class="node-right">
                    <span class="status-text" :class="getStatusClass(schedule.status)">
                      {{ getStatusText(schedule.status) }}
                    </span>
                  </div>
                </div>

                <!-- 描述信息 -->
                <div class="node-desc" v-if="schedule.description">{{ schedule.description }}</div>

                <!-- 时间信息 -->
                <div class="node-time" v-if="schedule.planStartDate || schedule.actualStartDate">
                  <span v-if="schedule.planStartDate">
                    计划：{{ formatDate(schedule.planStartDate) }}
                  </span>
                  <span v-if="schedule.actualStartDate">
                    实际：{{ formatDate(schedule.actualStartDate) }}
                    <template v-if="schedule.actualEndDate"> - {{ formatDate(schedule.actualEndDate) }}</template>
                  </span>
                </div>

                <!-- 验收记录 -->
                <div v-if="schedule.acceptanceRecords && schedule.acceptanceRecords.length > 0" class="records-section">
                  <div class="records-title">验收记录 ({{ schedule.acceptanceRecords.length }}次)</div>
                  <div class="records-list">
                    <div 
                      v-for="record in schedule.acceptanceRecords" 
                      :key="record.id" 
                      class="record-item"
                    >
                      <div class="record-header">
                        <span class="record-title">{{ record.acceptanceTitle || '验收记录' }}</span>
                        <span 
                          class="inspection-status" 
                          :class="getInspectionClass(record.acceptanceResult)"
                        >
                          {{ getInspectionText(record.acceptanceResult) }}
                        </span>
                      </div>
                      <div v-if="record.acceptanceContent" class="record-desc">{{ record.acceptanceContent }}</div>
                      <div class="record-meta">
                        <span v-if="record.acceptor" class="record-acceptor">验收人：{{ record.acceptor }}</span>
                        <span class="record-time">{{ formatDateTime(record.acceptanceTime) }}</span>
                      </div>
                      <!-- 图片展示 -->
                      <div v-if="hasRecordImages(record)" class="record-images">
                        <img 
                          v-for="(img, imgIndex) in getRecordImages(record).slice(0, 4)" 
                          :key="imgIndex" 
                          :src="getFullImageUrl(img)" 
                          class="record-image"
                        />
                        <div v-if="getRecordImages(record).length > 4" class="image-more">
                          +{{ getRecordImages(record).length - 4 }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-timeline">
            <span>暂无项目进度数据</span>
          </div>
        </div>

        <!-- 底部水印 -->
        <div class="export-footer">
          <div class="footer-line"></div>
          <div class="footer-text">
            {{ companyName }} · 项目进度报告
          </div>
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <el-dialog
      v-model="previewVisible"
      title="长图预览"
      width="90%"
      append-to-body
    >
      <div class="preview-image-container">
        <img v-if="previewImage" :src="previewImage" class="preview-image" />
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, getCurrentInstance } from 'vue'
import { Download, View } from '@element-plus/icons-vue'
import html2canvas from 'html2canvas'
import { listProjectSchedules } from '@/api/evs/projectSchedules'
import { listProjectScheduleRecords } from '@/api/evs/projectScheduleRecords'
import logoImage from '@/assets/logo/logo.png'

const { proxy } = getCurrentInstance()
const { decoration_construction_stage, decoration_design_stage } = proxy.useDict('decoration_construction_stage', 'decoration_design_stage')

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  project: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 数据
const loading = ref(false)
const exporting = ref(false)
const schedules = ref([])
const previewVisible = ref(false)
const previewImage = ref('')

// 公司信息
const companyName = ref('逅时代装饰')
const logoUrl = ref(logoImage)

// 客户名称
const customerName = computed(() => {
  return props.project?.customer?.name || ''
})

const exportContent = ref(null)

// 监听对话框打开
watch(dialogVisible, async (val) => {
  if (val && props.project?.id) {
    await loadData()
  }
})

// 加载数据
async function loadData() {
  if (!props.project?.id) return
  
  loading.value = true
  try {
    // 加载进度列表
    const scheduleRes = await listProjectSchedules({
      projectId: props.project.id,
      pageNum: 1,
      pageSize: 100
    })
    
    if (scheduleRes.code === 200) {
      const scheduleList = scheduleRes.rows || []
      
      // 按排序加载
      scheduleList.sort((a, b) => (a.stageOrder || 0) - (b.stageOrder || 0))
      
      // 为每个进度加载验收记录
      for (const schedule of scheduleList) {
        try {
          const recordRes = await listProjectScheduleRecords({
            scheduleId: schedule.id,
            recordType: 'ACCEPTANCE',
            pageNum: 1,
            pageSize: 50
          })
          if (recordRes.code === 200) {
            // 按时间倒序排序
            const records = recordRes.rows || []
            records.sort((a, b) => new Date(b.acceptanceTime) - new Date(a.acceptanceTime))
            schedule.acceptanceRecords = records
          }
        } catch (e) {
          console.error('加载验收记录失败:', e)
          schedule.acceptanceRecords = []
        }
      }
      
      schedules.value = scheduleList
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取进度阶段名称（从字典获取中文名称）
function getScheduleStageName(item) {
  if (!item) return '进度节点'
  
  // 优先使用 taskName 字段
  if (item.taskName) return item.taskName
  
  // 根据阶段类型选择对应的字典
  const dictData = item.stageType === 'DESIGN' ? decoration_design_stage.value : decoration_construction_stage.value
  const stageDict = dictData?.find(dict => dict.value === item.stage)
  return stageDict?.label || item.stage || '进度节点'
}

// 获取状态类名
function getStatusClass(status) {
  const s = String(status).toUpperCase()
  if (s === 'COMPLETED' || s === '2') return 'status-completed'
  if (s === 'IN_PROGRESS' || s === '1') return 'status-in-progress'
  return 'status-pending'
}

// 获取状态文本
function getStatusText(status) {
  const s = String(status).toUpperCase()
  if (s === 'COMPLETED' || s === '2') return '已完成'
  if (s === 'IN_PROGRESS' || s === '1') return '进行中'
  return '待开始'
}

// 获取验收状态类名
function getInspectionClass(result) {
  if (!result) return ''
  const s = String(result).toUpperCase()
  if (s === 'PASS' || s === 'QUALIFIED' || s === '1') {
    return 'tag-pass'
  }
  if (s === 'CONDITIONAL_PASS') {
    return 'tag-conditional'
  }
  return 'tag-fail'
}

// 获取验收状态文本
function getInspectionText(result) {
  if (!result) return ''
  const s = String(result).toUpperCase()
  if (s === 'PASS' || s === 'QUALIFIED' || s === '1') return '合格'
  if (s === 'FAIL' || s === 'UNQUALIFIED' || s === '0') return '不合格'
  if (s === 'CONDITIONAL_PASS') return '有条件通过'
  if (s === 'REWORK') return '需返工'
  return result
}

// 格式化日期
function formatDate(date) {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 获取项目状态类名
function getProjectStatusClass() {
  if (props.project?.actualEndDate) return 'status-done'
  const status = String(props.project?.status || '').toUpperCase()
  if (status === 'IN_PROGRESS' || status === '1') return 'status-active'
  return 'status-pending'
}

// 获取项目状态文本
function getProjectStatusText() {
  if (props.project?.actualEndDate) return '已完工'
  const status = String(props.project?.status || '').toUpperCase()
  if (status === 'IN_PROGRESS' || status === '1') return '进行中'
  if (status === 'COMPLETED' || status === '2') return '已完工'
  return '待开工'
}

// 格式化日期时间
function formatDateTime(datetime) {
  if (!datetime) return '-'
  const d = new Date(datetime)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 检查是否有图片
function hasRecordImages(record) {
  if (!record?.images) return false
  try {
    const images = getRecordImages(record)
    return images.length > 0
  } catch {
    return false
  }
}

// 获取验收记录图片
function getRecordImages(record) {
  if (!record?.images) return []
  
  try {
    let images = []
    const imageData = record.images
    
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
    
    return images.filter(img => img && typeof img === 'string')
  } catch (error) {
    console.warn('解析验收图片数据失败:', error)
    return []
  }
}

// 获取完整图片URL
function getFullImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const basePath = import.meta.env.VITE_APP_BASE_API
  if (url.startsWith('/')) {
    return basePath + url
  }
  return basePath + '/' + url
}

// 导出图片
async function handleExport() {
  if (!exportContent.value) return
  
  exporting.value = true
  try {
    const canvas = await html2canvas(exportContent.value, {
      scale: 2,
      useCORS: true,
      allowTaint: true,
      backgroundColor: '#ffffff',
      logging: false
    })
    
    // 下载图片
    const link = document.createElement('a')
    link.download = `${props.project.name || '项目'}_进度报告.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
  } catch (error) {
    console.error('导出失败:', error)
    proxy.$modal.msgError('导出失败，请重试')
  } finally {
    exporting.value = false
  }
}

// 预览图片
async function handlePreview() {
  if (!exportContent.value) return
  
  exporting.value = true
  try {
    const canvas = await html2canvas(exportContent.value, {
      scale: 2,
      useCORS: true,
      allowTaint: true,
      backgroundColor: '#ffffff',
      logging: false
    })
    
    previewImage.value = canvas.toDataURL('image/png')
    previewVisible.value = true
  } catch (error) {
    console.error('预览失败:', error)
    proxy.$modal.msgError('预览失败，请重试')
  } finally {
    exporting.value = false
  }
}
</script>


<style scoped>
.export-dialog :deep(.el-dialog__body) {
  padding: 16px;
}

.export-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.preview-container {
  max-height: 60vh;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.export-content {
  width: 375px;
  margin: 0 auto;
  padding: 24px 20px;
  background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 头部样式 */
.export-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

/* 品牌栏 */
.brand-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.company-logo {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.company-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

/* 项目名称 */
.project-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 10px;
  line-height: 1.4;
}

/* 项目信息列表 */
.project-info-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 12px;
}

.info-label {
  color: #999;
  flex-shrink: 0;
  width: 28px;
}

.info-value {
  color: #666;
}

/* 时间轴区域 */
.timeline-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

/* 时间轴节点 */
.timeline {
  padding-left: 8px;
}

.timeline-node {
  display: flex;
  gap: 12px;
  position: relative;
}

.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 16px;
  flex-shrink: 0;
  position: relative;
}

.timeline-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
  position: relative;
  z-index: 2;
  margin-top: 14px;
  background: #9ca3af;
}

.timeline-dot.status-completed {
  background: #52c41a;
  box-shadow: 0 0 0 4px rgba(82, 196, 26, 0.2);
}

.timeline-dot.status-in-progress {
  background: #1677ff;
  box-shadow: 0 0 0 4px rgba(22, 119, 255, 0.2);
}

.timeline-dot.status-pending {
  background: #d9d9d9;
}

.timeline-connector {
  width: 2px;
  position: absolute;
  top: 26px;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1;
  background: #e5e7eb;
}

.timeline-connector.status-completed {
  background: #52c41a;
}

.timeline-connector.status-in-progress {
  background: #1677ff;
}

/* 节点内容 */
.node-content {
  flex: 1;
  background: #f9fafb;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
  min-width: 0;
}

.node-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  gap: 8px;
}

.node-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.stage-type-tag {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
  flex-shrink: 0;
}

.stage-type-tag.type-design {
  background: rgba(250, 140, 22, 0.1);
  color: #fa8c16;
}

.stage-type-tag.type-construction {
  background: rgba(22, 119, 255, 0.1);
  color: #1677ff;
}

.node-title {
  font-weight: 600;
  font-size: 14px;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.node-right {
  flex-shrink: 0;
}

.status-text {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 6px;
  background: rgba(156, 163, 175, 0.1);
  color: #6b7280;
  white-space: nowrap;
}

.status-text.status-completed {
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
}

.status-text.status-in-progress {
  background: rgba(22, 119, 255, 0.1);
  color: #1677ff;
}

.node-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
  line-height: 1.5;
}

.node-time {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 11px;
  color: #9ca3af;
  margin-bottom: 8px;
}

/* 验收记录 */
.records-section {
  border-top: 1px solid #e5e7eb;
  padding-top: 10px;
  margin-top: 10px;
}

.records-title {
  font-size: 12px;
  font-weight: 600;
  color: #666;
  margin-bottom: 8px;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.record-item {
  background: #fff;
  border-radius: 8px;
  padding: 10px;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  gap: 8px;
}

.record-title {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.inspection-status {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
  flex-shrink: 0;
}

.inspection-status.tag-pass {
  background: #52c41a;
  color: #fff;
}

.inspection-status.tag-fail {
  background: #ff4d4f;
  color: #fff;
}

.inspection-status.tag-conditional {
  background: #faad14;
  color: #fff;
}

.record-desc {
  font-size: 12px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 4px;
}

.record-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: #9ca3af;
}

.record-acceptor {
  color: #666;
}

.record-images {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.record-image {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
  background: #f0f0f0;
}

.image-more {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
}

/* 空状态 */
.empty-timeline {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}

/* 底部 */
.export-footer {
  margin-top: 24px;
  text-align: center;
}

.footer-line {
  height: 1px;
  background: linear-gradient(90deg, transparent, #ddd, transparent);
  margin-bottom: 12px;
}

.footer-text {
  font-size: 11px;
  color: #bbb;
}

/* 预览图片 */
.preview-image-container {
  max-height: 70vh;
  overflow-y: auto;
  text-align: center;
}

.preview-image {
  max-width: 100%;
  height: auto;
}
</style>
