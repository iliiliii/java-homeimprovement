<template>
  <div class="project-progress-container">
    <el-space direction="vertical" :size="20" style="width: 100%;">
      <!-- 进度统计 -->
      <div
      style="
        background: #f0f5ff;
        border: 1px solid #adc6ff;
        border-radius: 8px;
        padding: 16px;
      "
    >
      <el-space :size="20" style="width: 100%; justify-content: space-around;">
        <div style="text-align: center;">
          <div style="font-size: 14px; color: #999;">总阶段</div>
          <div style="font-size: 28px; color: #1677ff; font-weight: bold; margin-top: 4px;">
            {{ timelineItems.length }}
          </div>
        </div>
        <div style="text-align: center;">
          <div style="font-size: 14px; color: #999;">已完成</div>
          <div style="font-size: 28px; color: #52c41a; font-weight: bold; margin-top: 4px;">
            {{ completedCount }}
          </div>
        </div>
        <div style="text-align: center;">
          <div style="font-size: 14px; color: #999;">进行中</div>
          <div style="font-size: 28px; color: #1677ff; font-weight: bold; margin-top: 4px;">
            {{ inProgressCount }}
          </div>
        </div>
        <div style="text-align: center;">
          <div style="font-size: 14px; color: #999;">完成度</div>
          <div style="font-size: 28px; color: #722ed1; font-weight: bold; margin-top: 4px;">
            {{ calculateTimelinePercentage(completedCount, timelineItems.length) }}%
          </div>
        </div>
      </el-space>
    </div>

    <!-- 施工时间轴 -->
    <div style="max-height: 400px; overflow-y: auto; padding: 0 16px;">
      <el-empty v-if="loading" description="加载中..." />
      <el-timeline v-else-if="timelineItems.length > 0">
        <el-timeline-item
          v-for="item in timelineItems"
          :key="item.id"
          :color="item.status === 'completed' ? '#52c41a' : item.status === 'inProgress' ? '#1677ff' : '#d9d9d9'"
          :icon="getTimelineIcon(item.status)"
        >
          <div
            style="
              background: #fafafa;
              padding: 12px;
              border-radius: 8px;
              margin-bottom: 12px;
            "
          >
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
              <div style="flex: 1;">
                <div style="margin-bottom: 8px;">
                  <span style="font-size: 15px; font-weight: bold; margin-right: 8px;">
                    {{ project_schedule.find(dict => dict.value === item.title)?.label || item.title }}
                  </span>
                  <el-tag :color="getTimelineStatusConfig(item.status).color" size="small">
                    {{ getTimelineStatusConfig(item.status).label }}
                  </el-tag>
                </div>
                <div style="color: #666; margin-bottom: 8px;">{{ item.description }}</div>
                <div style="font-size: 13px; color: #999;">
                  <el-icon style="vertical-align: middle; margin-right: 4px;"><Calendar /></el-icon>
                  {{ item.date }}
                </div>
              </div>
              <el-space>
                <el-select
                  :model-value="item.status"
                  size="small"
                  style="width: 100px;"
                  @change="(status) => handleUpdateTimelineStatus(item.id, status)"
                >
                  <el-option value="pending" label="待开始" />
                  <el-option value="inProgress" label="进行中" />
                  <el-option value="completed" label="已完成" />
                </el-select>
                <el-button
                  type="primary"
                  link
                  size="small"
                  icon="Edit"
                  @click="handleEditTimelineItem(item)"
                />
                <el-popconfirm
                  title="确认删除"
                  description="确定要删除这个施工阶段吗？"
                  @confirm="handleDeleteTimelineItem(item.id)"
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                >
                  <template #reference>
                    <el-button
                      type="danger"
                      link
                      size="small"
                      icon="Delete"
                    />
                  </template>
                </el-popconfirm>
              </el-space>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <div v-else style="text-align: center; padding: 20px 0; color: #999; font-size: 13px;">
        <el-text  title="暂无施工阶段" :column="1" class="mx-1" type="info" size="small" >
        暂无施工阶段，请点击下方按钮添加
      </el-text>
      </div>
    </div>

    <!-- 添加/编辑施工阶段表单 -->
    <div
      v-if="isAddingTimeline || editingTimelineItem"
      style="
        background: #f5f5f5;
        padding: 16px;
        border-radius: 8px;
      "
    >
      <el-form :model="timelineForm" label-position="top">
        <el-space direction="vertical" :size="16" style="width: 100%;">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #1677ff;">
            {{ editingTimelineItem ? '编辑施工阶段' : '添加施工阶段' }}
          </div>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="施工阶段" required style="margin-bottom: 0;">
                <el-select
                  v-model="timelineForm.title"
                  placeholder="选择施工阶段"
                  size="large"
                  style="width: 100%"
                >
                  <el-option
                    v-for="dict in availableStages"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  />
                  <el-option
                    v-if="availableStages.length === 0"
                    disabled
                    value=""
                    label="暂无可选施工阶段"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="计划日期" required style="margin-bottom: 0;">
                <el-date-picker
                  v-model="timelineForm.date"
                  placeholder="选择日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                  size="large"
                />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="当前状态" style="margin-bottom: 0;">
                <el-select v-model="timelineForm.status" style="width: 100%" size="large">
                  <el-option value="pending" label="待开始" />
                  <el-option value="inProgress" label="进行中" />
                  <el-option value="completed" label="已完成" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="阶段说明" required style="margin-bottom: 0;">
            <el-input
              v-model="timelineForm.description"
              type="textarea"
              :rows="3"
              placeholder="描述该阶段的工作内容"
              size="large"
            />
          </el-form-item>

          <div style="margin-top: 16px;">
            <el-space>
              <el-button type="primary" @click="handleSaveTimelineItem">
                {{ editingTimelineItem ? '确认修改' : '确认添加' }}
              </el-button>
              <el-button @click="handleCancelTimelineEdit">取消</el-button>
            </el-space>
          </div>
        </el-space>
      </el-form>
    </div>

    <!-- 添加施工阶段按钮 -->
    <el-button
      v-if="!isAddingTimeline && !editingTimelineItem"
      type="dashed"
      style="width: 100%"
      @click="handleStartAddTimeline"
    >
      <el-icon><Plus /></el-icon>
      添加施工阶段
    </el-button>
    </el-space>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useDict } from '@/utils/dict'
import { listProjectSchedules, addProjectSchedules, updateProjectSchedules, delProjectSchedules } from '@/api/evs/projectSchedules'
import { Plus, Calendar, Edit, Delete } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { project_schedule } = useDict('project_schedule')

// Props
const props = defineProps({
  project: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

// Emits
const emit = defineEmits(['save'])

// 响应式数据
const timelineItems = ref([]) // 时间轴条目数组
const isAddingTimeline = ref(false)
const editingTimelineItem = ref(null) // 当前编辑的条目
const loading = ref(false)
const timelineForm = ref({
  title: '',
  description: '',
  date: '',
  status: 'pending'
})

// 计算统计数据
const completedCount = computed(() => {
  return timelineItems.value.filter(t => t.status === 'completed').length
})

const inProgressCount = computed(() => {
  return timelineItems.value.filter(t => t.status === 'inProgress').length
})

// 计算可选的施工阶段（过滤掉已选择的，但编辑时保留当前项）
const availableStages = computed(() => {
  const selectedStages = timelineItems.value.map(item => item.title)
  let available = project_schedule.value.filter(dict => !selectedStages.includes(dict.value))

  // 编辑模式：如果当前编辑的阶段已被过滤掉，则添加回来
  if (editingTimelineItem.value) {
    const currentStage = project_schedule.value.find(dict => dict.value === editingTimelineItem.value.title)
    if (currentStage && !available.some(dict => dict.value === currentStage.value)) {
      available.push(currentStage)
    }
  }

  return available
})

// 时间轴状态配置
const timelineStatusConfig = {
  pending: { label: '待开始', color: '' },
  inProgress: { label: '进行中', color: 'primary' },
  completed: { label: '已完成', color: 'success' }
}

// 获取时间轴状态配置
function getTimelineStatusConfig(status) {
  return timelineStatusConfig[status] || timelineStatusConfig.pending
}

// 获取时间轴图标
function getTimelineIcon(status) {
  switch (status) {
    case 'completed':
      return 'Check'
    case 'inProgress':
      return 'Clock'
    default:
      return ''
  }
}

// 计算时间轴完成百分比
function calculateTimelinePercentage(completed, total) {
  if (total === 0) return 0
  const percent = (completed / total) * 100
  return Math.min(Math.round(percent), 100)
}

// 数据转换：将后端数据转换为组件所需格式
function convertFromBackendData(backendItems) {
  return backendItems.map(item => ({
    id: item.id,
    title: item.stage,
    description: item.description || '',
    date: item.planStartDate || item.actualStartDate || '未设置',
    status: mapStatusFromBackend(item.status),
    stageOrder: item.stageOrder,
    completionRate: item.completionRate || 0
  })).sort((a, b) => (a.stageOrder || 0) - (b.stageOrder || 0))
}

// 数据转换：将组件数据转换为后端所需格式
function convertToBackendData(componentItem) {
  return {
    id: componentItem.id,
    projectId: props.project.id,
    stage: componentItem.title,
    stageOrder: getStageOrder(componentItem.title),
    planStartDate: componentItem.date,
    actualStartDate: componentItem.status === 'inProgress' ? componentItem.date : null,
    actualEndDate: componentItem.status === 'completed' ? componentItem.date : null,
    status: mapStatusToBackend(componentItem.status),
    completionRate: componentItem.status === 'completed' ? 100 : componentItem.status === 'inProgress' ? 50 : 0,
    description: componentItem.description
  }
}

// 状态映射：组件状态 → 后端状态
function mapStatusToBackend(componentStatus) {
  const statusMap = {
    'pending': 'PLANNED',
    'inProgress': 'IN_PROGRESS',
    'completed': 'COMPLETED'
  }
  return statusMap[componentStatus] || 'PLANNED'
}

// 状态映射：后端状态 → 组件状态
function mapStatusFromBackend(backendStatus) {
  const statusMap = {
    'PLANNED': 'pending',
    'IN_PROGRESS': 'inProgress',
    'COMPLETED': 'completed'
  }
  return statusMap[backendStatus] || 'pending'
}

// 获取施工阶段顺序
function getStageOrder(stage) {
  const stageOrders = {
    'DISMANTLING': 1,
    'WATER_ELECTRIC': 2,
    'TILES': 3,
    'WOODWORK': 4,
    'PAINTING': 5,
    'INSTALLATION': 6,
    'SOFT_FURNISHING': 7,
    'ACCEPTANCE': 8
  }
  return stageOrders[stage] || 999
}

// 加载项目进度数据
function loadProjectSchedules() {
  if (!props.project?.id) return

  loading.value = true
  const queryParams = {
    pageNum: 1,
    pageSize: 100,
    projectId: props.project.id
  }

  listProjectSchedules(queryParams).then(response => {
    if (response.rows && response.rows.length > 0) {
      timelineItems.value = convertFromBackendData(response.rows)
    } else {
      timelineItems.value = []
    }
    loading.value = false
  }).catch(error => {
    console.error('加载项目进度失败:', error)
    proxy.$modal.msgError("加载项目进度失败")
    loading.value = false
  })
}

/** 重置时间轴表单 */
function resetTimelineForm() {
  timelineForm.value = {
    title: '',
    description: '',
    date: '',
    status: 'pending'
  }
  isAddingTimeline.value = false
  editingTimelineItem.value = null
}

/** 开始编辑时间轴条目 */
function handleEditTimelineItem(item) {
  timelineForm.value = {
    title: item.title,
    description: item.description,
    date: item.date,
    status: item.status
  }
  editingTimelineItem.value = item
  isAddingTimeline.value = false
}

/** 开始添加时间轴条目 */
function handleStartAddTimeline() {
  resetTimelineForm()
  isAddingTimeline.value = true
}

/** 取消时间轴编辑 */
function handleCancelTimelineEdit() {
  resetTimelineForm()
}

/** 保存时间轴条目（新增或编辑） */
function handleSaveTimelineItem() {
  // 验证表单
  if (!timelineForm.value.title) {
    proxy.$modal.msgError("请选择施工阶段")
    return
  }
  if (!timelineForm.value.description) {
    proxy.$modal.msgError("请输入阶段说明")
    return
  }
  if (!timelineForm.value.date) {
    proxy.$modal.msgError("请选择计划日期")
    return
  }

  // 检查是否选择了重复的施工阶段（新增和编辑都需要检查）
  const isEdit = !!editingTimelineItem.value
  const isDuplicate = timelineItems.value.some(item =>
    item.title === timelineForm.value.title &&
    item.id !== (isEdit ? editingTimelineItem.value.id : '')
  )
  if (isDuplicate) {
    const stageLabel = project_schedule.value.find(dict => dict.value === timelineForm.value.title)?.label || timelineForm.value.title
    proxy.$modal.msgError(`施工阶段"${stageLabel}"已存在，请选择其他阶段`)
    return
  }

  const action = isEdit ? '修改' : '添加'
  const apiCall = isEdit ? updateProjectSchedules : addProjectSchedules

  const backendData = convertToBackendData({
    ...timelineForm.value,
    id: isEdit ? editingTimelineItem.value.id : undefined
  })

  apiCall(backendData).then(response => {
    if (response.code === 200) {
      proxy.$modal.msgSuccess(`施工阶段已${action}`)
      resetTimelineForm()
      loadProjectSchedules() // 重新加载数据
      // 新增成功后检查是否还有可选的施工阶段
      if (!isEdit) {
        setTimeout(() => {
          const remainingStages = project_schedule.value.filter(dict =>
            !timelineItems.value.some(item => item.title === dict.value)
          )
          if (remainingStages.length === 0) {
            proxy.$modal.msgSuccess("所有施工阶段已添加完成")
          }
        }, 500)
      }
    } else {
      // 处理重复性错误
      if (response.msg && (response.msg.includes('Duplicate') || response.msg.includes('UNIQUE'))) {
        proxy.$modal.msgError("该施工阶段已存在，请选择其他阶段")
      } else {
        proxy.$modal.msgError(response.msg || `${action}失败`)
      }
    }
  }).catch(error => {
    console.error(`${action}施工阶段失败:`, error)
    // 处理网络错误和服务器错误
    if (error.response && error.response.data) {
      const errorMsg = error.response.data.msg || error.response.data.message
      if (errorMsg && (errorMsg.includes('Duplicate') || errorMsg.includes('UNIQUE'))) {
        proxy.$modal.msgError("该施工阶段已存在，请选择其他阶段")
      } else {
        proxy.$modal.msgError(`${action}施工阶段失败：${errorMsg}`)
      }
    } else {
      proxy.$modal.msgError(`${action}施工阶段失败，请稍后重试`)
    }
  })
}

/** 更新时间轴条目状态 */
function handleUpdateTimelineStatus(itemId, status) {
  const item = timelineItems.value.find(item => item.id === itemId)
  if (!item) return

  const oldStatus = item.status // 保存原状态用于回滚
  const backendData = convertToBackendData({ ...item, status })

  updateProjectSchedules(backendData).then(response => {
    if (response.code === 200) {
      proxy.$modal.msgSuccess("状态已更新")
      // 成功后更新本地数据
      const index = timelineItems.value.findIndex(item => item.id === itemId)
      if (index !== -1) {
        timelineItems.value[index].status = status
      }
    } else {
      proxy.$modal.msgError(response.msg || "状态更新失败")
    }
  }).catch(error => {
    // 失败时确保本地状态不被错误修改
    const index = timelineItems.value.findIndex(item => item.id === itemId)
    if (index !== -1 && timelineItems.value[index].status !== oldStatus) {
      timelineItems.value[index].status = oldStatus
    }
    console.error('更新状态失败:', error)
    proxy.$modal.msgError("状态更新失败")
  })
}

/** 删除时间轴条目 */
function handleDeleteTimelineItem(itemId) {
  proxy.$modal.confirm('是否确认删除该施工阶段？').then(() => {
    return delProjectSchedules(itemId)
  }).then(() => {
    proxy.$modal.msgSuccess("施工阶段已删除")
    loadProjectSchedules() // 重新加载数据
  }).catch(error => {
    console.error('删除施工阶段失败:', error)
    proxy.$modal.msgError("删除施工阶段失败")
  })
}

/** 保存时间轴条目 */
function handleSaveTimelineItems() {
  if (timelineItems.value.length === 0) {
    proxy.$modal.msgWarning("请至少添加一个施工阶段")
    return
  }

  // 计算项目总进度（基于完成的阶段）
  const totalProgress = calculateTimelinePercentage(completedCount.value, timelineItems.value.length)

  // 构建更新数据
  const updateData = {
    id: props.project.id,
    name: props.project.name,
    progress: totalProgress,
    timeline: timelineItems.value
  }

  // 触发保存事件，通知父组件
  emit('save', updateData)
}

// 监听项目变化，自动加载数据
watch(() => props.project, (newProject) => {
  if (newProject && newProject.id) {
    loadProjectSchedules()
  }
}, { immediate: true, deep: true })

// 组件挂载时加载数据
onMounted(() => {
  if (props.project && props.project.id) {
    loadProjectSchedules()
  }
})

// 暴露方法给父组件
defineExpose({
  timelineItems,
  resetTimelineForm,
  handleSaveTimelineItems,
  loadProjectSchedules
})
</script>

<style scoped>
.project-progress-container {
  width: 100%;
}

/* 确保进度对话框中的时间轴样式与 TSX 一致 */
.project-progress-container .el-timeline {
  padding-left: 0;
}

.project-progress-container .el-timeline-item__content {
  padding-left: 20px;
}
</style>
