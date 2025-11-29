<template>
  <!-- 项目详情对话框 -->
  <el-dialog
    v-model="detailOpen"
    width="1200px"
    :top="'20px'"
    append-to-body
    :show-close="true"
    class="project-detail-dialog"
  >
    <template #header>
      <div style="display: flex; align-items: center; gap: 12px;">
        <span style="font-size: 16px; font-weight: 600;">{{ currentProject.name }}</span>
        <dict-tag :options="decoration_project_status" :value="currentProject.status" />
      </div>
    </template>

    <div style="max-height: calc(90vh - 150px); overflow-y: auto; padding: 0 8px;">
      <el-space direction="vertical" :size="20" :fill="true" style="width: 100%;" class="project-detail-space">
        <!-- 项目设置操作 -->
        <el-card size="small" shadow="never" style="width: 100%; background: #fff7e6; border: 1px solid #ffd591;">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div style="flex: 1;">
              <div style="font-weight: 600; margin-bottom: 8px;">
                <el-icon style="vertical-align: middle;"><Setting /></el-icon>
                <span style="margin-left: 10px; font-size: 16px;">项目设置</span>
              </div>
              <div style="font-size: 14px; color: #666;">管理项目预算、施工进度和项目信息</div>
            </div>
            <div style="flex: 2; display: flex; justify-content: flex-end; align-items: center; gap: 16px;">
              <el-button size="default" @click="handleTeamAllocation(currentProject)" style="padding: 10px 20px;">
                <el-icon style="margin-right: 8px;"><User /></el-icon>
                分配团队
              </el-button>
              <el-button size="default" @click="handleBudgetManagement(currentProject)" style="padding: 10px 20px;">
                <el-icon style="margin-right: 8px;"><Wallet /></el-icon>
                管理预算
              </el-button>
              <el-button size="default" @click="handleProgressManagement(currentProject)" style="padding: 10px 20px;">
                <el-icon style="margin-right: 8px;"><Clock /></el-icon>
                管理进度
              </el-button>
              <el-button size="default" @click="handleDesignDrafts(currentProject)" style="padding: 10px 20px;">
                <el-icon style="margin-right: 8px;"><Picture /></el-icon>
                上传设计稿
              </el-button>
              <el-button size="default" type="primary" @click="handleUpdate(currentProject)" style="padding: 10px 24px;">
                <el-icon style="margin-right: 8px;"><Edit /></el-icon>
                编辑项目
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- 项目基本信息 -->
        <el-card size="small" shadow="never" style="width: 100%; padding: 8px;">
          <template #header>
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-icon style="color: #1677ff; font-size: 18px;"><InfoFilled /></el-icon>
              <span style="font-weight: 600; font-size: 15px;">项目基本信息</span>
            </div>
          </template>
          <el-descriptions :column="3" size="default" border>
            <el-descriptions-item label="项目名称" :span="2">
              <span style="font-weight: 600;">{{ currentProject.name }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="项目状态">
              <dict-tag :options="decoration_project_status" :value="currentProject.status" />
            </el-descriptions-item>
                      <el-descriptions-item label="工地面积" :span="2">
              {{ currentProject.area ? currentProject.area + '㎡' : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="关联客户">
              <el-link
                v-if="getCustomerName(currentProject.customerId)"
                type="primary"
                :underline="false"
                @click="goToCustomer(currentProject.customerId)"
              >
                {{ getCustomerName(currentProject.customerId) }}
              </el-link>
              <span v-else style="color: #999;">未关联客户</span>
            </el-descriptions-item>
            <el-descriptions-item label="工地地址" :span="3">
              {{ currentProject.address || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="开始日期">
              {{ parseTime(currentProject.startDate, '{y}-{m}-{d}') }}
            </el-descriptions-item>
            <el-descriptions-item label="预计完工">
              {{ parseTime(currentProject.endDate, '{y}-{m}-{d}') }}
            </el-descriptions-item>
            <el-descriptions-item label="实际完工">
              {{ currentProject.actualEndDate ? parseTime(currentProject.actualEndDate, '{y}-{m}-{d}') : '进行中' }}
            </el-descriptions-item>
            <el-descriptions-item label="项目描述" :span="3">
              {{ currentProject.description || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 项目预算 -->
        <el-card size="small" shadow="never" style="width: 100%; padding: 8px;">
          <template #header>
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-icon style="color: #faad14; font-size: 18px;"><Coin /></el-icon>
              <span style="font-weight: 600; font-size: 15px;">项目预算</span>
            </div>
          </template>
          <div v-if="currentProject.budget && currentProject.budget > 0">
            <!-- 预算统计卡片 -->
            <el-row :gutter="16" style="margin-bottom: 16px;">
              <el-col :span="8">
                <el-card size="small" shadow="never" style="background: #fafafa; height: 100%;">
                  <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 12px 0;">
                    <div style="font-size: 13px; color: #666; margin-bottom: 6px;">总预算</div>
                    <div style="font-weight: 600; color: #faad14; font-size: 18px;">
                      ¥{{ formatBudget(currentProject.budget) }}万
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card size="small" shadow="never" style="background: #fafafa; height: 100%;">
                  <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 12px 0;">
                    <div style="font-size: 13px; color: #666; margin-bottom: 6px;">已支出</div>
                    <div style="font-weight: 600; color: #faad14; font-size: 18px;">
                      ¥{{ formatBudget(currentProject.actualCost || 0) }}万
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card size="small" shadow="never" style="background: #f0f9ff; height: 100%;">
                  <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 12px 0;">
                    <div style="font-size: 13px; color: #666; margin-bottom: 6px;">使用率</div>
                    <div style="font-weight: 600; color: #1677ff; font-size: 18px;">
                      {{ calculateProgress(currentProject.actualCost || 0, currentProject.budget) }}%
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 预算明细表格 -->
            <div v-if="currentProject.budgetItems && currentProject.budgetItems.length > 0">
              <el-table
                :data="currentProject.budgetItems"
                size="small"
                style="margin-bottom: 16px;"
                :show-header="true"
              >
                <el-table-column prop="category" label="预算类别" width="30%" />
                <el-table-column prop="amount" label="预算金额" width="30%">
                  <template #default="scope">
                    <span style="color: #faad14; font-weight: bold;">
                      ¥{{ scope.row.amount.toLocaleString() }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="说明" width="40%">
                  <template #default="scope">
                    <span v-if="scope.row.description">{{ scope.row.description }}</span>
                    <span v-else style="color: #999;">-</span>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 预算执行情况 -->
              <el-card size="small" shadow="never" style="background: #fff7e6; border-color: #faad14; padding: 16px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                  <span style="font-size: 16px; font-weight: 600;">预算执行情况</span>
                  <span style="color: #faad14; font-size: 20px; font-weight: 600;">
                    ¥{{ formatBudget(currentProject.budget) }}万
                  </span>
                </div>
                <el-progress
                  :percentage="calculateProgress(currentProject.actualCost || 0, currentProject.budget)"
                  :stroke-width="14"
                  :show-text="false"
                  style="margin-bottom: 12px;"
                />
                <div style="display: flex; justify-content: space-between; font-size: 14px;">
                  <span style="color: #666;">剩余预算</span>
                  <span :style="{ color: (currentProject.budget - (currentProject.actualCost || 0)) >= 0 ? '#52c41a' : '#ff4d4f', fontWeight: 600, fontSize: '15px' }">
                    ¥{{ formatBudget((currentProject.budget || 0) - (currentProject.actualCost || 0)) }}万
                  </span>
                </div>
              </el-card>
            </div>
            <div v-else>
              <!-- 没有预算明细时的执行情况显示 -->
              <el-card size="small" shadow="never" style="background: #fff7e6; border-color: #faad14; padding: 16px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                  <span style="font-size: 16px; font-weight: 600;">预算执行情况</span>
                  <span style="color: #faad14; font-size: 20px; font-weight: 600;">
                    ¥{{ formatBudget(currentProject.budget) }}万
                  </span>
                </div>
                <el-progress
                  :percentage="calculateProgress(currentProject.actualCost || 0, currentProject.budget)"
                  :stroke-width="14"
                  :show-text="false"
                  style="margin-bottom: 12px;"
                />
                <div style="display: flex; justify-content: space-between; font-size: 14px;">
                  <span style="color: #666;">剩余预算</span>
                  <span :style="{ color: (currentProject.budget - (currentProject.actualCost || 0)) >= 0 ? '#52c41a' : '#ff4d4f', fontWeight: 600, fontSize: '15px' }">
                    ¥{{ formatBudget((currentProject.budget || 0) - (currentProject.actualCost || 0)) }}万
                  </span>
                </div>
              </el-card>
            </div>
          </div>
          <div v-else style="text-align: center; padding: 20px 0;">
            <span style="color: #999;">暂无预算信息</span>
          </div>
        </el-card>

        <!-- 项目进度 -->
        <el-card size="small" shadow="never" v-if="currentProject.progress !== undefined" style="width: 100%; padding: 8px;">
          <template #header>
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-icon style="color: #52c41a; font-size: 18px;"><TrendCharts /></el-icon>
              <span style="font-weight: 600; font-size: 15px;">项目进度</span>
            </div>
          </template>

          <!-- 进度统计卡片 -->
          <el-row :gutter="16" style="margin-bottom: 16px;">
            <el-col :span="6">
              <el-card size="small" shadow="never" style="text-align: center; background: #f0f5ff; height: 100%;">
                <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 16px 8px;">
                  <div style="font-size: 28px; font-weight: bold; color: #1677ff; margin-bottom: 8px;">{{ currentProject.progress || 0 }}%</div>
                  <div style="font-size: 13px; color: #666;">完成进度</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card size="small" shadow="never" style="text-align: center; background: #f6ffed; height: 100%;">
                <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 16px 8px;">
                  <div style="font-size: 16px; font-weight: bold; color: #52c41a; margin-bottom: 8px;">
                    {{ decoration_project_status.find(item => item.value === currentProject.status).label }}
                  </div>
                  <div style="font-size: 13px; color: #666;">当前状态</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card size="small" shadow="never" style="text-align: center; background: #fff7e6; height: 100%;">
                <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 16px 8px;">
                  <div style="font-size: 16px; font-weight: bold; color: #fa8c16; margin-bottom: 8px;">
                    {{ currentProject.actualEndDate ? parseTime(currentProject.actualEndDate, '{y}-{m}-{d}') : '进行中' }}
                  </div>
                  <div style="font-size: 13px; color: #666;">实际完工</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card size="small" shadow="never" style="text-align: center; background: #f9f0ff; height: 100%;">
                <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 16px 8px;">
                  <div style="font-size: 16px; font-weight: bold; color: #722ed1; margin-bottom: 8px;">
                    {{ calculateDaysRemaining(currentProject.endDate, currentProject.actualEndDate) }}
                  </div>
                  <div style="font-size: 13px; color: #666;">剩余天数</div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 施工进度时间轴 -->
          <div v-if="currentProject.timeline && currentProject.timeline.length > 0">
            <div style="margin-bottom: 12px;">
              <span style="font-weight: 600; font-size: 14px; color: #666;">施工进度时间轴</span>
            </div>
            <el-timeline style="max-height: 300px; overflow-y: auto;">
              <el-timeline-item
                v-for="item in currentProject.timeline"
                :key="item.id"
                :color="item.status === 'completed' ? '#52c41a' : item.status === 'inProgress' ? '#1677ff' : '#d9d9d9'"
                :icon="getTimelineIcon(item.status)"
                size="small"
              >
                <div style="margin-bottom: 8px;">
                  <span style="font-weight: 600; font-size: 14px; margin-right: 8px;">{{ item.title }}</span>
                  <el-tag :color="getTimelineStatusConfig(item.status).color" size="small">
                    {{ getTimelineStatusConfig(item.status).label }}
                  </el-tag>
                </div>
                <div style="color: #666; font-size: 13px; margin-bottom: 4px;">{{ item.description }}</div>
                <div style="font-size: 12px; color: #999;">
                  <el-icon style="vertical-align: middle; margin-right: 4px;"><Calendar /></el-icon>
                  {{ item.date }}
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
          <div v-else style="text-align: center; padding: 20px 0; color: #999; font-size: 13px;">
            暂无施工进度详情，请点击"管理进度"添加施工阶段
          </div>
        </el-card>
      </el-space>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 团队成员分配组件 -->
  <ProjectMember
    v-model="teamAllocationOpen"
    :project="currentProject"
    @success="handleTeamAllocationSuccess"
  />

  <!-- 设计稿管理组件 -->
  <ProjectDesignDrafts
    :key="`project-design-drafts-${currentProject?.id || 'default'}`"
    v-model="designDraftsOpen"
    :project="currentProject"
    @success="handleDesignDraftsSuccess"
    @rooms-updated="handleRoomsUpdated"
    @designs-updated="handleDesignsUpdated"
  />
</template>

<script setup>
import { useRouter } from 'vue-router'
import { User, Setting, Wallet, Clock, Edit, Picture } from '@element-plus/icons-vue'
import ProjectMember from './ProjectMember.vue'
import ProjectDesignDrafts from './ProjectDesignDrafts.vue'

const { proxy } = getCurrentInstance()
const router = useRouter()
const { decoration_project_status } = proxy.useDict('decoration_project_status')

const props = defineProps({
  // 无需接收 props，对外暴露方法
})

const emit = defineEmits(['update', 'budget', 'progress', 'design-drafts-updated'])

// 响应式数据
const detailOpen = ref(false)
const currentProject = ref({})

// 团队成员分配相关
const teamAllocationOpen = ref(false)

// 设计稿管理相关
const designDraftsOpen = ref(false)

// 时间轴状态配置（用于详情弹窗显示）
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

/** 格式化预算金额(转为万元) */
function formatBudget(amount) {
  if (!amount) return '0.00'
  return (amount / 10000).toFixed(2)
}

/** 计算预算进度百分比 */
function calculateProgress(actual, total) {
  if (!total || total === 0) return 0
  const percent = (actual / total) * 100
  return Math.min(Math.round(percent), 100)
}

/** 计算剩余天数 */
function calculateDaysRemaining(endDate, actualEndDate) {
  if (actualEndDate) {
    // 如果有实际完工日期，显示"已完成"
    return "已完成"
  }
  if (!endDate) {
    return "未设置"
  }
  const today = new Date()
  const end = new Date(endDate)
  const diffTime = end - today
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays < 0) {
    return `逾期${Math.abs(diffDays)}天`
  } else if (diffDays === 0) {
    return "今日到期"
  } else {
    return `${diffDays}天`
  }
}

/** 获取客户名称 */
function getCustomerName(customerId) {
  // 优先从当前项目数据中获取客户信息
  if (currentProject.value.customer && currentProject.value.customer.name) {
    return currentProject.value.customer.name
  }
  // 如果有客户ID但没有客户信息，返回客户ID
  if (customerId) {
    return `客户ID: ${customerId}`
  }
  // 默认返回值
  return '未关联客户'
}

/** 跳转到客户详情页 */
function goToCustomer(customerId) {
  if (customerId) {
    // 使用Vue Router跳转到客户管理页面，并传递客户ID参数
    router.push({
      path: '/evs/customers',
      query: { id: customerId }
    })
  }
}

/** 打开详情对话框 */
function handleView(project) {
  currentProject.value = project
  detailOpen.value = true
}

/** 预算管理 */
function handleBudgetManagement(project) {
  emit('budget', project)
}

/** 进度管理 */
function handleProgressManagement(project) {
  emit('progress', project)
}

/** 编辑项目 */
function handleUpdate(project) {
  emit('update', project)
}

/** 分配团队 */
function handleTeamAllocation(project) {
  currentProject.value = project
  teamAllocationOpen.value = true
}

/** 团队分配成功后的处理 */
function handleTeamAllocationSuccess() {
  // 这里可以添加刷新逻辑，比如触发事件通知父组件刷新项目详情
  console.log('团队分配保存成功')
}

/** 设计稿管理 */
function handleDesignDrafts(project) {
  console.log(`[ProjectDetail] 打开项目 ${project?.id} (${project?.name}) 的设计稿管理`)
  currentProject.value = project
  designDraftsOpen.value = true
}

/** 设计稿管理成功后的处理 */
function handleDesignDraftsSuccess(result) {
  console.log('设计稿管理操作成功:', result)
}

/** 房间更新处理 */
function handleRoomsUpdated(data) {
  console.log('房间列表已更新:', data)
  // 这里可以添加项目统计信息的更新逻辑
}

/** 设计稿更新处理 */
function handleDesignsUpdated(data) {
  console.log('设计稿已更新:', data)
  // 通知父组件设计稿更新
  emit('design-drafts-updated', {
    projectId: currentProject.value.id,
    ...data
  })
}

// 暴露方法给父组件
defineExpose({
  handleView
})
</script>

<style scoped>
/* 确保 el-space 中的每个 item 都占满父容器宽度 */
.project-detail-space .el-space__item {
  width: 100%;
}
</style>
