<template>
  <div class="project-progress-container">
    <el-space direction="vertical" :size="20" :fill="true" style="width: 100%;">
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

    <!-- 时间轴 -->
    <div style="max-height: 400px; overflow-y: auto; padding: 0 16px;">
      <el-empty v-if="loading" description="加载中..." />
      <el-timeline v-else-if="timelineItems.length > 0">
        <el-timeline-item
          v-for="(item, index) in timelineItems"
          :key="item.id"
          :color="getTimelineColor(item)"
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
                  <!-- 阶段类型标签 -->
                  <el-tag 
                    :type="item.stageType === 'DESIGN' ? 'warning' : 'primary'" 
                    size="small" 
                    style="margin-right: 8px;"
                  >
                    {{ item.stageType === 'DESIGN' ? '设计' : '施工' }}
                  </el-tag>
                  <span style="font-size: 15px; font-weight: bold; margin-right: 8px;">
                    {{ getStageLabel(item) }}
                  </span>
                  <el-tag :color="getTimelineStatusConfig(item.status).color" size="small">
                    {{ getTimelineStatusConfig(item.status).label }}
                  </el-tag>
                </div>
                <div style="color: #666; margin-bottom: 8px;">{{ item.description }}</div>
                <div style="font-size: 13px; color: #999;">
                  <div v-if="item.planStartDate" style="margin-bottom: 2px;">
                    <el-icon style="vertical-align: middle; margin-right: 4px;"><Calendar /></el-icon>
                    计划：{{ item.planStartDate }}
                    <template v-if="item.planEndDate"> ~ {{ item.planEndDate }}</template>
                  </div>
                  <div v-if="item.actualStartDate" style="margin-top: 2px;">
                    <el-icon style="vertical-align: middle; margin-right: 4px;"><Check /></el-icon>
                    实际：{{ item.actualStartDate }}
                    <template v-if="item.actualEndDate"> ~ {{ item.actualEndDate }}</template>
                  </div>
                </div>
              </div>
              <el-space>
                <!-- 排序按钮 -->
                <el-button
                  type="primary"
                  link
                  size="small"
                  :icon="Top"
                  :disabled="index === 0 || isMoving"
                  @click="handleMoveUp(index)"
                  title="上移"
                />
                <el-button
                  type="primary"
                  link
                  size="small"
                  :icon="Bottom"
                  :disabled="index === timelineItems.length - 1 || isMoving"
                  @click="handleMoveDown(index)"
                  title="下移"
                />
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
                  :description="'确定要删除这个' + (item.stageType === 'DESIGN' ? '设计' : '施工') + '阶段吗？'"
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
        <el-text title="暂无阶段" :column="1" class="mx-1" type="info" size="small">
          暂无阶段，请点击下方按钮添加
        </el-text>
      </div>
    </div>

    <!-- 添加/编辑阶段表单 -->
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
            {{ getFormTitle() }}
          </div>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item :label="timelineForm.stageType === 'DESIGN' ? '设计阶段' : '施工阶段'" required style="margin-bottom: 0;">
                <el-select
                  v-model="timelineForm.title"
                  :placeholder="'选择' + (timelineForm.stageType === 'DESIGN' ? '设计' : '施工') + '阶段'"
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
                    :label="'暂无可选' + (timelineForm.stageType === 'DESIGN' ? '设计' : '施工') + '阶段'"
                  />
                </el-select>
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

          <!-- 计划时间 -->
          <el-row :gutter="16">
            <el-col :span="24">
              <div style="font-weight: 600; margin-bottom: 8px; margin-top: 8px; color: #666; font-size: 14px;">
                <el-icon style="vertical-align: middle; margin-right: 4px;"><Calendar /></el-icon>
                计划时间
              </div>
            </el-col>
            <el-col :span="12">
              <el-form-item label="开始日期" required style="margin-bottom: 0;">
                <el-date-picker
                  v-model="timelineForm.planStartDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="选择计划开始日期"
                  style="width: 100%"
                  size="large"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束日期" style="margin-bottom: 0;">
                <el-date-picker
                  v-model="timelineForm.planEndDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="选择计划结束日期"
                  style="width: 100%"
                  size="large"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 实际时间 -->
          <el-row :gutter="16">
            <el-col :span="24">
              <div style="font-weight: 600; margin-bottom: 8px; margin-top: 8px; color: #666; font-size: 14px;">
                <el-icon style="vertical-align: middle; margin-right: 4px;"><Check /></el-icon>
                实际时间
              </div>
            </el-col>
            <el-col :span="12">
              <el-form-item label="开始日期" style="margin-bottom: 0;">
                <el-date-picker
                  v-model="timelineForm.actualStartDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="选择实际开始日期"
                  style="width: 100%"
                  size="large"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束日期" style="margin-bottom: 0;">
                <el-date-picker
                  v-model="timelineForm.actualEndDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="选择实际结束日期"
                  style="width: 100%"
                  size="large"
                />
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

    <!-- 添加阶段按钮 -->
    <el-space v-if="!isAddingTimeline && !editingTimelineItem" style="width: 100%;">
      <el-button
        type="dashed"
        style="flex: 1;"
        @click="handleStartAddTimeline('DESIGN')"
      >
        <el-icon><Plus /></el-icon>
        添加设计阶段
      </el-button>
      <el-button
        type="dashed"
        style="flex: 1;"
        @click="handleStartAddTimeline('CONSTRUCTION')"
      >
        <el-icon><Plus /></el-icon>
        添加施工阶段
      </el-button>
    </el-space>
    </el-space>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useDict } from '@/utils/dict'
import { listProjectSchedules, addProjectSchedules, updateProjectSchedules, delProjectSchedules, updateProjectSchedulesOrder } from '@/api/evs/projectSchedules'
import { Plus, Calendar, Edit, Delete, Top, Bottom, Check } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { decoration_construction_stage, decoration_design_stage } = useDict('decoration_construction_stage', 'decoration_design_stage')

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
const isMoving = ref(false) // 是否正在执行移动操作（防抖/节流）
const timelineForm = ref({
  title: '',
  description: '',
  planStartDate: '',      // 计划开始日期
  planEndDate: '',        // 计划结束日期
  actualStartDate: '',    // 实际开始日期
  actualEndDate: '',      // 实际结束日期
  status: 'pending',
  stageType: 'CONSTRUCTION' // 默认施工阶段
})

// 计算统计数据
const completedCount = computed(() => {
  return timelineItems.value.filter(t => t.status === 'completed').length
})

const inProgressCount = computed(() => {
  return timelineItems.value.filter(t => t.status === 'inProgress').length
})

// 根据当前表单的阶段类型获取对应的字典数据
const currentStageDict = computed(() => {
  return timelineForm.value.stageType === 'DESIGN' 
    ? decoration_design_stage.value 
    : decoration_construction_stage.value
})

// 计算可选的阶段（过滤掉已选择的，但编辑时保留当前项）
const availableStages = computed(() => {
  const currentType = timelineForm.value.stageType
  const dictData = currentType === 'DESIGN' ? decoration_design_stage.value : decoration_construction_stage.value
  
  // 获取同类型已选择的阶段
  const selectedStages = timelineItems.value
    .filter(item => item.stageType === currentType)
    .map(item => item.title)
  
  let available = dictData.filter(dict => !selectedStages.includes(dict.value))

  // 编辑模式：如果当前编辑的阶段已被过滤掉，则添加回来
  if (editingTimelineItem.value && editingTimelineItem.value.stageType === currentType) {
    const currentStage = dictData.find(dict => dict.value === editingTimelineItem.value.title)
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

// 获取时间轴颜色（根据阶段类型和状态）
function getTimelineColor(item) {
  if (item.status === 'completed') return '#52c41a'
  if (item.status === 'inProgress') {
    return item.stageType === 'DESIGN' ? '#fa8c16' : '#1677ff'
  }
  return '#d9d9d9'
}

// 获取阶段标签
function getStageLabel(item) {
  const dictData = item.stageType === 'DESIGN' 
    ? decoration_design_stage.value 
    : decoration_construction_stage.value
  return dictData.find(dict => dict.value === item.title)?.label || item.title
}

// 获取表单标题
function getFormTitle() {
  const typeLabel = timelineForm.value.stageType === 'DESIGN' ? '设计' : '施工'
  return editingTimelineItem.value ? `编辑${typeLabel}阶段` : `添加${typeLabel}阶段`
}

// 计算时间轴完成百分比
function calculateTimelinePercentage(completed, total) {
  if (total === 0) return 0
  const percent = (completed / total) * 100
  return Math.min(Math.round(percent), 100)
}

// 数据转换：将后端数据转换为组件所需格式
// 注意：后端接口已按stageOrder升序排列，无需前端再次排序
function convertFromBackendData(backendItems) {
  let converted = backendItems.map((item, index) => ({
    id: item.id,
    title: item.stage,
    description: item.description || '',
    planStartDate: item.planStartDate || '',
    planEndDate: item.planEndDate || '',
    actualStartDate: item.actualStartDate || '',
    actualEndDate: item.actualEndDate || '',
    status: mapStatusFromBackend(item.status),
    stageType: item.stageType || 'CONSTRUCTION', // 默认施工阶段
    stageOrder: item.stageOrder != null && item.stageOrder > 0 ? Number(item.stageOrder) : null, // 保持null，后续会重新分配
    completionRate: item.completionRate || 0
  }))
  
  // 如果存在无效的stageOrder（null或0），按照索引位置重新分配（100, 200, 300...）
  const hasInvalidOrder = converted.some(item => item.stageOrder == null || item.stageOrder === 0)
  if (hasInvalidOrder) {
    console.warn('检测到无效的stageOrder，重新分配排序值')
    converted = converted.map((item, index) => ({
      ...item,
      stageOrder: (index + 1) * 100 // 从100开始，每次+100
    }))
  }
  
  // 后端接口已按stageOrder升序返回，无需前端排序
  console.log('转换后的时间轴数据:', converted.map(item => ({ id: item.id, title: item.title, stageOrder: item.stageOrder })))
  return converted
}

// 数据转换：将组件数据转换为后端所需格式
function convertToBackendData(componentItem) {
  // 根据阶段类型获取对应的字典数据
  const dictData = componentItem.stageType === 'DESIGN' 
    ? decoration_design_stage.value 
    : decoration_construction_stage.value
  const stageDict = dictData.find(dict => dict.value === componentItem.title)

  // 计算新阶段的排序值：如果是新增阶段，放在最下方（最大的stageOrder值）
  let stageOrder = componentItem.stageOrder
  
  // 如果是新增阶段（没有id），计算最大排序值并+100
  if (!componentItem.id) {
    if (timelineItems.value.length === 0) {
      // 如果没有现有阶段，从100开始（第一个阶段）
      stageOrder = 100
      console.log('新增第一个阶段，stageOrder = 100')
    } else {
      // 获取现有阶段的最大排序值，新阶段+100，确保放在最后
      const validOrders = timelineItems.value
        .map(item => item.stageOrder)
        .filter(order => order != null && order > 0)
      
      if (validOrders.length === 0) {
        // 如果所有项的stageOrder都无效，从100开始
        stageOrder = 100
      } else {
        const maxOrder = Math.max(...validOrders)
        stageOrder = maxOrder + 100
      }
      console.log('新增阶段排序计算:', { 
        totalItems: timelineItems.value.length,
        validOrders,
        maxOrder: validOrders.length > 0 ? Math.max(...validOrders) : 0,
        newStageOrder: stageOrder 
      })
    }
  } else {
    // 编辑模式：使用原有的stageOrder，如果没有则使用字典排序值或默认值
    if (!stageOrder || stageOrder === 0) {
      // 编辑时如果stageOrder无效，尝试使用字典排序值，否则使用当前位置计算
      const currentIndex = timelineItems.value.findIndex(item => item.id === componentItem.id)
      if (currentIndex >= 0) {
        stageOrder = (currentIndex + 1) * 100
      } else {
        stageOrder = stageDict?.dictSort || (timelineItems.value.length + 1) * 100
      }
    }
  }

  return {
    id: componentItem.id,
    projectId: props.project.id,
    stageType: componentItem.stageType,
    stage: componentItem.title,
    stageOrder: stageOrder,
    planStartDate: componentItem.planStartDate || null,
    planEndDate: componentItem.planEndDate || null,
    actualStartDate: componentItem.actualStartDate || null,
    actualEndDate: componentItem.actualEndDate || null,
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
function resetTimelineForm(stageType = 'CONSTRUCTION') {
  timelineForm.value = {
    title: '',
    description: '',
    planStartDate: '',
    planEndDate: '',
    actualStartDate: '',
    actualEndDate: '',
    status: 'pending',
    stageType: stageType
  }
  isAddingTimeline.value = false
  editingTimelineItem.value = null
}

/** 开始编辑时间轴条目 */
function handleEditTimelineItem(item) {
  timelineForm.value = {
    title: item.title,
    description: item.description,
    planStartDate: item.planStartDate || '',
    planEndDate: item.planEndDate || '',
    actualStartDate: item.actualStartDate || '',
    actualEndDate: item.actualEndDate || '',
    status: item.status,
    stageType: item.stageType || 'CONSTRUCTION'
  }
  editingTimelineItem.value = item
  isAddingTimeline.value = false
}

/** 开始添加时间轴条目 */
function handleStartAddTimeline(stageType = 'CONSTRUCTION') {
  resetTimelineForm(stageType)
  isAddingTimeline.value = true
}

/** 取消时间轴编辑 */
function handleCancelTimelineEdit() {
  resetTimelineForm()
}

/** 保存时间轴条目（新增或编辑） */
function handleSaveTimelineItem() {
  const typeLabel = timelineForm.value.stageType === 'DESIGN' ? '设计' : '施工'
  
  // 验证表单
  if (!timelineForm.value.title) {
    proxy.$modal.msgError(`请选择${typeLabel}阶段`)
    return
  }
  if (!timelineForm.value.description) {
    proxy.$modal.msgError("请输入阶段说明")
    return
  }
  if (!timelineForm.value.planStartDate) {
    proxy.$modal.msgError("请选择计划开始日期")
    return
  }
  
  // 验证计划日期范围
  if (timelineForm.value.planEndDate && timelineForm.value.planStartDate) {
    if (new Date(timelineForm.value.planEndDate) < new Date(timelineForm.value.planStartDate)) {
      proxy.$modal.msgError("计划结束日期不能早于开始日期")
      return
    }
  }
  
  // 验证实际日期范围
  if (timelineForm.value.actualEndDate && timelineForm.value.actualStartDate) {
    if (new Date(timelineForm.value.actualEndDate) < new Date(timelineForm.value.actualStartDate)) {
      proxy.$modal.msgError("实际结束日期不能早于开始日期")
      return
    }
  }

  // 检查是否选择了重复的阶段（同类型内检查）
  const isEdit = !!editingTimelineItem.value
  const isDuplicate = timelineItems.value.some(item =>
    item.title === timelineForm.value.title &&
    item.stageType === timelineForm.value.stageType &&
    item.id !== (isEdit ? editingTimelineItem.value.id : '')
  )
  if (isDuplicate) {
    const dictData = timelineForm.value.stageType === 'DESIGN' 
      ? decoration_design_stage.value 
      : decoration_construction_stage.value
    const stageLabel = dictData.find(dict => dict.value === timelineForm.value.title)?.label || timelineForm.value.title
    proxy.$modal.msgError(`${typeLabel}阶段"${stageLabel}"已存在，请选择其他阶段`)
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
      proxy.$modal.msgSuccess(`${typeLabel}阶段已${action}`)
      resetTimelineForm()
      loadProjectSchedules() // 重新加载数据
    } else {
      // 处理重复性错误
      if (response.msg && (response.msg.includes('Duplicate') || response.msg.includes('UNIQUE'))) {
        proxy.$modal.msgError(`该${typeLabel}阶段已存在，请选择其他阶段`)
      } else {
        proxy.$modal.msgError(response.msg || `${action}失败`)
      }
    }
  }).catch(error => {
    console.error(`${action}${typeLabel}阶段失败:`, error)
    if (error.response && error.response.data) {
      const errorMsg = error.response.data.msg || error.response.data.message
      if (errorMsg && (errorMsg.includes('Duplicate') || errorMsg.includes('UNIQUE'))) {
        proxy.$modal.msgError(`该${typeLabel}阶段已存在，请选择其他阶段`)
      } else {
        proxy.$modal.msgError(`${action}${typeLabel}阶段失败：${errorMsg}`)
      }
    } else {
      proxy.$modal.msgError(`${action}${typeLabel}阶段失败，请稍后重试`)
    }
  })
}

/** 更新时间轴条目状态 */
function handleUpdateTimelineStatus(itemId, status) {
  const item = timelineItems.value.find(item => item.id === itemId)
  if (!item) return

  const oldStatus = item.status // 保存原状态用于回滚
  
  // 保留所有日期字段，不再自动覆盖
  const backendData = {
    id: item.id,
    projectId: props.project.id,
    stageType: item.stageType,
    stage: item.title,
    stageOrder: item.stageOrder, // 保留原有排序，不重新计算
    planStartDate: item.planStartDate || null,
    planEndDate: item.planEndDate || null,
    actualStartDate: item.actualStartDate || null,
    actualEndDate: item.actualEndDate || null,
    status: mapStatusToBackend(status),
    completionRate: status === 'completed' ? 100 : status === 'inProgress' ? 50 : 0,
    description: item.description
  }

  updateProjectSchedules(backendData).then(response => {
    if (response.code === 200) {
      proxy.$modal.msgSuccess("状态已更新")
      // 成功后更新本地数据，不重新加载以保持排序
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
  const item = timelineItems.value.find(item => item.id === itemId)
  const typeLabel = item?.stageType === 'DESIGN' ? '设计' : '施工'
  
  proxy.$modal.confirm(`是否确认删除该${typeLabel}阶段？`).then(() => {
    return delProjectSchedules(itemId)
  }).then(() => {
    proxy.$modal.msgSuccess(`${typeLabel}阶段已删除`)
    loadProjectSchedules() // 重新加载数据
  }).catch(error => {
    console.error(`删除${typeLabel}阶段失败:`, error)
    proxy.$modal.msgError(`删除${typeLabel}阶段失败`)
  })
}

/** 上移时间轴条目 */
async function handleMoveUp(index) {
  // 防抖/节流：如果正在执行移动操作，直接返回
  if (isMoving.value) {
    console.warn('移动操作正在进行中，请稍候...')
    return
  }
  
  if (index <= 0) return
  
  // 获取当前记录和上一条记录
  const currentItem = timelineItems.value[index]
  const prevItem = timelineItems.value[index - 1]
  
  // 获取当前记录的stageOrder
  const currentStageOrder = currentItem.stageOrder
  // 获取上一条记录的stageOrder
  const prevStageOrder = prevItem.stageOrder
  
  // 验证stageOrder值有效性
  if (!currentStageOrder || !prevStageOrder || currentStageOrder <= 0 || prevStageOrder <= 0) {
    proxy.$modal.msgError("排序值无效，请刷新后重试")
    return
  }
  
  // 交换两个stageOrder值
  // 当前记录使用上一条记录的stageOrder（更小的值，位置更靠前）
  const newCurrentStageOrder = prevStageOrder
  // 上一条记录使用当前记录的stageOrder（更大的值，位置更靠后）
  const newPrevStageOrder = currentStageOrder
  
  console.log('上移操作 - 交换stageOrder:', {
    当前记录: {
      id: currentItem.id,
      原stageOrder: currentStageOrder,
      新stageOrder: newCurrentStageOrder
    },
    上一条记录: {
      id: prevItem.id,
      原stageOrder: prevStageOrder,
      新stageOrder: newPrevStageOrder
    }
  })
  
  // 设置移动状态为true，防止重复操作
  isMoving.value = true
  
  try {
    // 发起第一条更新请求：更新当前记录的stageOrder
    await updateProjectSchedulesOrder(currentItem.id, newCurrentStageOrder)
    // 发起第二条更新请求：更新上一条记录的stageOrder
    await updateProjectSchedulesOrder(prevItem.id, newPrevStageOrder)
    
    proxy.$modal.msgSuccess("排序已更新")
    // 等待数据加载完成后再重置状态
    await loadProjectSchedules()
  } catch (error) {
    console.error('更新排序失败:', error)
    proxy.$modal.msgError("更新排序失败")
  } finally {
    // 操作完成，重置移动状态（延迟100ms，确保UI更新完成）
    setTimeout(() => {
      isMoving.value = false
    }, 100)
  }
}

/** 下移时间轴条目 */
async function handleMoveDown(index) {
  // 防抖/节流：如果正在执行移动操作，直接返回
  if (isMoving.value) {
    console.warn('移动操作正在进行中，请稍候...')
    return
  }
  
  if (index >= timelineItems.value.length - 1) return
  
  // 获取当前记录和下一条记录
  const currentItem = timelineItems.value[index]
  const nextItem = timelineItems.value[index + 1]
  
  // 获取当前记录的stageOrder
  const currentStageOrder = currentItem.stageOrder
  // 获取下一条记录的stageOrder
  const nextStageOrder = nextItem.stageOrder
  
  // 验证stageOrder值有效性
  if (!currentStageOrder || !nextStageOrder || currentStageOrder <= 0 || nextStageOrder <= 0) {
    proxy.$modal.msgError("排序值无效，请刷新后重试")
    return
  }
  
  // 交换两个stageOrder值
  // 当前记录使用下一条记录的stageOrder（更大的值，位置更靠后）
  const newCurrentStageOrder = nextStageOrder
  // 下一条记录使用当前记录的stageOrder（更小的值，位置更靠前）
  const newNextStageOrder = currentStageOrder
  
  console.log('下移操作 - 交换stageOrder:', {
    当前记录: {
      id: currentItem.id,
      原stageOrder: currentStageOrder,
      新stageOrder: newCurrentStageOrder
    },
    下一条记录: {
      id: nextItem.id,
      原stageOrder: nextStageOrder,
      新stageOrder: newNextStageOrder
    }
  })
  
  // 设置移动状态为true，防止重复操作
  isMoving.value = true
  
  try {
    // 发起第一条更新请求：更新当前记录的stageOrder
    await updateProjectSchedulesOrder(currentItem.id, newCurrentStageOrder)
    // 发起第二条更新请求：更新下一条记录的stageOrder
    await updateProjectSchedulesOrder(nextItem.id, newNextStageOrder)
    
    proxy.$modal.msgSuccess("排序已更新")
    // 等待数据加载完成后再重置状态
    await loadProjectSchedules()
  } catch (error) {
    console.error('更新排序失败:', error)
    proxy.$modal.msgError("更新排序失败")
  } finally {
    // 操作完成，重置移动状态（延迟100ms，确保UI更新完成）
    setTimeout(() => {
      isMoving.value = false
    }, 100)
  }
}

/** 保存时间轴条目 */
function handleSaveTimelineItems() {
  if (timelineItems.value.length === 0) {
    proxy.$modal.msgWarning("请至少添加一个阶段")
    return
  }

  const totalProgress = calculateTimelinePercentage(completedCount.value, timelineItems.value.length)

  const updateData = {
    id: props.project.id,
    name: props.project.name,
    progress: totalProgress,
    timeline: timelineItems.value
  }

  emit('save', updateData)
}

// 监听项目变化，自动加载数据
watch(() => props.project, (newProject) => {
  if (newProject && newProject.id) {
    loadProjectSchedules()
  }
}, { immediate: true, deep: true })

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

/* 确保 el-space 的子元素撑满宽度 */
.project-progress-container :deep(.el-space__item) {
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
