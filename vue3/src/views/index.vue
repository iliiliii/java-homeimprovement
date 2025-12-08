<template>
  <div class="dashboard-container">
    <!-- 管理员视图：总览看板 -->
    <template v-if="isAdmin">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">总览看板</h1>
        <p class="page-subtitle">客户管理与项目管理全局视图</p>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-card">
            <div class="stat-icon customer">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">客户总数</div>
              <div class="stat-value customer">{{ stats.customerCount }}<span class="stat-unit">位</span></div>
              <div class="stat-extra">活跃客户 {{ stats.activeCustomerCount }} 位</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-card">
            <div class="stat-icon project">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">项目总数</div>
              <div class="stat-value project">{{ stats.projectCount }}<span class="stat-unit">个</span></div>
              <div class="stat-extra">进行中 {{ stats.inProgressCount }} · 已完成 {{ stats.completedCount }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-card">
            <div class="stat-icon budget">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">预算总额</div>
              <div class="stat-value budget">¥{{ formatMoney(stats.totalBudget) }}<span class="stat-unit">万</span></div>
              <div class="stat-extra">已支出 ¥{{ formatMoney(stats.totalCost) }}万</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-card">
            <div class="stat-icon quality">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">质检通过率</div>
              <div class="stat-value quality">{{ stats.qualityPassRate }}<span class="stat-unit">%</span></div>
              <div class="stat-extra">不通过 {{ stats.qualityFailCount }} 次</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 重点客户管理 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <div class="section-header">
            <div class="section-title">
              <el-icon class="section-icon customer"><Trophy /></el-icon>
              <span>重点客户管理</span>
            </div>
            <el-button type="primary" link @click="goToCustomers">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <div class="customer-list" v-loading="customersLoading">
          <div v-for="customer in topCustomers" :key="customer.id" class="customer-item" @click="goToCustomerDetail(customer.id)">
            <div class="customer-avatar">
              <el-icon><Avatar /></el-icon>
            </div>
            <div class="customer-info">
              <div class="customer-name">{{ customer.name }}</div>
              <div class="customer-contact">
                <el-icon><Phone /></el-icon>
                <span>{{ customer.phone || '-' }}</span>
                <el-icon style="margin-left: 12px;"><Location /></el-icon>
                <span>{{ customer.address || '-' }}</span>
              </div>
              <div class="customer-stats">
                <span>项目数量：<b>{{ customer.projectCount || 0 }}</b></span>
                <span class="divider">·</span>
                <span>合同总额：<b class="amount">¥{{ formatMoney(customer.totalBudget) }}万</b></span>
                <span v-if="customer.inProgressCount > 0" class="in-progress-tag">进行中 {{ customer.inProgressCount }} 个</span>
              </div>
            </div>
          </div>
          <el-empty v-if="!customersLoading && topCustomers.length === 0" description="暂无客户数据" :image-size="80" />
        </div>
      </el-card>

      <!-- 项目与待办 -->
      <el-row :gutter="16" class="bottom-row">
        <!-- 进行中的项目 -->
        <el-col :xs="24" :lg="14">
          <el-card class="section-card" shadow="never">
            <template #header>
              <div class="section-header">
                <div class="section-title">
                  <el-icon class="section-icon project"><FolderOpened /></el-icon>
                  <span>进行中的项目</span>
                  <el-tag type="primary" size="small" round style="margin-left: 8px;">{{ inProgressProjects.length }}</el-tag>
                </div>
                <el-button type="primary" link @click="goToProjects">
                  进度跟踪 <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </template>
            <div class="project-list" v-loading="projectsLoading">
              <div v-for="project in inProgressProjects" :key="project.id" class="project-item" @click="goToProjectDetail(project.id)">
                <div class="project-header">
                  <span class="project-name">{{ project.name }}</span>
                  <el-tag type="primary" size="small">进行中</el-tag>
                </div>
                <div class="project-meta">
                  <el-icon><User /></el-icon>
                  <span>{{ project.customer?.name || '-' }}</span>
                  <el-icon style="margin-left: 12px;"><Location /></el-icon>
                  <span>{{ project.address || '-' }}</span>
                </div>
                <div class="project-progress">
                  <div class="progress-info">
                    <span class="progress-label">施工进度</span>
                    <span class="progress-value">{{ project.progressRate || 0 }}%</span>
                  </div>
                  <el-progress 
                    :percentage="project.progressRate || 0" 
                    :stroke-width="8" 
                    :show-text="false"
                    :color="getProgressColor(project.progressRate)"
                  />
                </div>
                <div class="project-footer">
                  <span class="budget-label">预算执行</span>
                  <span class="budget-value">¥{{ formatMoney(project.budget) }}万</span>
                  <span class="date-label">{{ formatDate(project.endDate) }}</span>
                </div>
              </div>
              <el-empty v-if="!projectsLoading && inProgressProjects.length === 0" description="暂无进行中的项目" :image-size="80" />
            </div>
          </el-card>
        </el-col>

        <!-- 待办事项 -->
        <el-col :xs="24" :lg="10">
          <el-card class="section-card todo-card" shadow="never">
            <template #header>
              <div class="section-header">
                <div class="section-title">
                  <el-icon class="section-icon todo"><Clock /></el-icon>
                  <span>待办事项</span>
                </div>
              </div>
            </template>
            <div class="todo-list">
              <div v-if="todoStats.pendingProjectCount > 0" class="todo-item warning" @click="goToProjects('pending')">
                <div class="todo-icon">
                  <el-icon><Folder /></el-icon>
                </div>
                <div class="todo-content">
                  <div class="todo-title">{{ todoStats.pendingProjectCount }} 个项目待启动</div>
                  <div class="todo-desc">请及时安排项目开工计划</div>
                </div>
                <el-button type="primary" size="small" plain>查 看</el-button>
              </div>
              <div v-if="todoStats.qualityIssueCount > 0" class="todo-item danger" @click="goToQualityIssues">
                <div class="todo-icon">
                  <el-icon><Warning /></el-icon>
                </div>
                <div class="todo-content">
                  <div class="todo-title">{{ todoStats.qualityIssueCount }} 个质检问题</div>
                  <div class="todo-desc">需要整改处理</div>
                </div>
                <el-button type="danger" size="small" plain>处 理</el-button>
              </div>
              <div v-if="todoStats.pendingAcceptanceCount > 0" class="todo-item info" @click="goToAcceptance">
                <div class="todo-icon">
                  <el-icon><DocumentChecked /></el-icon>
                </div>
                <div class="todo-content">
                  <div class="todo-title">{{ todoStats.pendingAcceptanceCount }} 个待验收</div>
                  <div class="todo-desc">等待客户验收确认</div>
                </div>
                <el-button type="primary" size="small" plain>查 看</el-button>
              </div>
              <el-empty 
                v-if="todoStats.pendingProjectCount === 0 && todoStats.qualityIssueCount === 0 && todoStats.pendingAcceptanceCount === 0" 
                description="暂无待办事项" 
                :image-size="80" 
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 普通用户视图：我的项目列表 -->
    <template v-else>
      <div class="page-header">
        <h1 class="page-title">我的项目</h1>
        <p class="page-subtitle">查看您参与的所有项目</p>
      </div>

      <!-- 项目列表 -->
      <div class="my-projects-container" v-loading="projectsLoading">
        <el-row :gutter="16">
          <el-col
            v-for="project in myProjects"
            :key="project.id"
            :xs="24"
            :sm="12"
            :lg="8"
            :xl="6"
            style="margin-bottom: 16px;"
          >
            <el-card shadow="hover" class="project-card" @click="goToProjectDetail(project.id)">
              <template #header>
                <div class="card-header">
                  <div class="card-title">
                    <span class="name">{{ project.name }}</span>
                    <span class="address">{{ project.address }}</span>
                  </div>
                  <dict-tag v-if="project.status" :options="decoration_project_status" :value="project.status" />
                </div>
              </template>
              
              <div class="card-body">
                <!-- 客户信息 -->
                <div class="info-row">
                  <el-icon><User /></el-icon>
                  <span>客户：{{ project.customer?.name || '-' }}</span>
                </div>
                
                <!-- 日期 -->
                <div class="info-row">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(project.startDate) }} ~ {{ formatDate(project.endDate) }}</span>
                </div>
                
                <!-- 进度 -->
                <div class="progress-section" v-if="project.status === 'in_progress'">
                  <div class="progress-header">
                    <span>施工进度</span>
                    <span class="progress-percent">{{ project.progressRate || 0 }}%</span>
                  </div>
                  <el-progress 
                    :percentage="project.progressRate || 0" 
                    :stroke-width="6" 
                    :show-text="false"
                    :color="getProgressColor(project.progressRate)"
                  />
                </div>
                
                <!-- 预算 -->
                <div class="budget-section" v-if="project.budget">
                  <div class="budget-row">
                    <span class="label">总预算</span>
                    <span class="value primary">¥{{ formatMoney(project.budget) }}万</span>
                  </div>
                  <div class="budget-row">
                    <span class="label">已支出</span>
                    <span class="value">¥{{ formatMoney(project.actualCost || 0) }}万</span>
                  </div>
                </div>
              </div>
              
              <template #footer>
                <div class="card-footer">
                  <el-button type="primary" link size="small" @click.stop="goToProjectDetail(project.id)">
                    <el-icon><View /></el-icon> 查看详情
                  </el-button>
                  <el-button type="success" link size="small" @click.stop="goToProjectProgress(project.id)">
                    <el-icon><DataLine /></el-icon> 进度跟踪
                  </el-button>
                </div>
              </template>
            </el-card>
          </el-col>
        </el-row>
        
        <el-empty v-if="!projectsLoading && myProjects.length === 0" description="暂无项目数据" :image-size="200" />
        
        <pagination
          v-show="myProjectsTotal > 0"
          :total="myProjectsTotal"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getMyProjects"
          style="margin-top: 16px;"
        />
      </div>
    </template>
  </div>
</template>

<script setup name="Index">
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { listProjectsWithScheduleInfo, listProjectsWithCustomer } from '@/api/evs/projects'
import { listCustomers } from '@/api/evs/customers'
import { listQualityInspections } from '@/api/evs/qualityInspections'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { decoration_project_status } = proxy.useDict('decoration_project_status')
const userStore = useUserStore()

// 判断是否为管理员
const isAdmin = computed(() => {
  return userStore.roles && userStore.roles.includes('admin')
})

// ========== 管理员视图数据 ==========
const stats = ref({
  customerCount: 0,
  activeCustomerCount: 0,
  projectCount: 0,
  inProgressCount: 0,
  completedCount: 0,
  totalBudget: 0,
  totalCost: 0,
  qualityPassRate: 0,
  qualityFailCount: 0
})

const topCustomers = ref([])
const customersLoading = ref(false)
const inProgressProjects = ref([])
const projectsLoading = ref(false)

const todoStats = ref({
  pendingProjectCount: 0,
  qualityIssueCount: 0,
  pendingAcceptanceCount: 0
})

// ========== 普通用户视图数据 ==========
const myProjects = ref([])
const myProjectsTotal = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 12
})

// ========== 方法 ==========

/** 格式化金额（万元） */
function formatMoney(amount) {
  if (!amount) return '0.00'
  return (amount / 10000).toFixed(1)
}

/** 格式化日期 */
function formatDate(date) {
  if (!date) return '-'
  return proxy.parseTime(date, '{y}-{m}-{d}')
}

/** 获取进度条颜色 */
function getProgressColor(percentage) {
  if (percentage < 30) return '#e6a23c'
  if (percentage < 70) return '#409eff'
  return '#67c23a'
}

/** 加载统计数据 */
async function loadStats() {
  try {
    // 获取客户数据
    const customerRes = await listCustomers({ pageSize: 1000 })
    const customers = customerRes.rows || []
    stats.value.customerCount = customerRes.total || 0
    
    // 获取项目数据（带进度信息）
    const projectRes = await listProjectsWithScheduleInfo({ pageSize: 1000 })
    const projects = projectRes.rows || []
    stats.value.projectCount = projectRes.total || 0
    stats.value.inProgressCount = projects.filter(p => p.status === 'in_progress').length
    stats.value.completedCount = projects.filter(p => p.status === 'completed').length
    stats.value.totalBudget = projects.reduce((sum, p) => sum + (p.budget || 0), 0)
    stats.value.totalCost = projects.reduce((sum, p) => sum + (p.actualCost || 0), 0)
    
    // 计算活跃客户（有进行中项目的客户）
    const activeCustomerIds = new Set(projects.filter(p => p.status === 'in_progress').map(p => p.customerId))
    stats.value.activeCustomerCount = activeCustomerIds.size
    
    // 计算待启动项目数
    todoStats.value.pendingProjectCount = projects.filter(p => p.status === 'pending').length
    
    // 获取质检数据
    const qualityRes = await listQualityInspections({ pageSize: 1000 })
    const inspections = qualityRes.rows || []
    const passCount = inspections.filter(q => q.result === 'pass').length
    const failCount = inspections.filter(q => q.result === 'fail').length
    stats.value.qualityPassRate = inspections.length > 0 ? Math.round((passCount / inspections.length) * 100) : 100
    stats.value.qualityFailCount = failCount
    todoStats.value.qualityIssueCount = failCount
    
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

/** 加载重点客户 */
async function loadTopCustomers() {
  customersLoading.value = true
  try {
    const customerRes = await listCustomers({ pageSize: 100 })
    const customers = customerRes.rows || []
    
    // 获取所有项目
    const projectRes = await listProjectsWithCustomer({ pageSize: 1000 })
    const projects = projectRes.rows || []
    
    // 为每个客户计算项目统计
    const customerStats = customers.map(customer => {
      const customerProjects = projects.filter(p => p.customerId === customer.id)
      return {
        ...customer,
        projectCount: customerProjects.length,
        totalBudget: customerProjects.reduce((sum, p) => sum + (p.budget || 0), 0),
        inProgressCount: customerProjects.filter(p => p.status === 'in_progress').length
      }
    })
    
    // 按合同总额排序，取前5个
    topCustomers.value = customerStats
      .sort((a, b) => b.totalBudget - a.totalBudget)
      .slice(0, 5)
      
  } catch (error) {
    console.error('加载客户数据失败:', error)
  } finally {
    customersLoading.value = false
  }
}

/** 加载进行中的项目 */
async function loadInProgressProjects() {
  projectsLoading.value = true
  try {
    const res = await listProjectsWithScheduleInfo({ 
      status: 'in_progress',
      pageSize: 5 
    })
    
    // 获取客户信息
    const projectsWithCustomer = await listProjectsWithCustomer({
      status: 'in_progress',
      pageSize: 5
    })
    
    // 合并进度信息和客户信息
    inProgressProjects.value = res.rows.map(project => {
      const withCustomer = projectsWithCustomer.rows.find(p => p.id === project.id)
      return {
        ...project,
        customer: withCustomer?.customer
      }
    })
  } catch (error) {
    console.error('加载项目数据失败:', error)
  } finally {
    projectsLoading.value = false
  }
}

/** 加载我的项目（普通用户） */
async function getMyProjects() {
  projectsLoading.value = true
  try {
    const res = await listProjectsWithScheduleInfo({
      ...queryParams.value,
      includeCustomer: true
    })
    
    // 获取客户信息
    const projectsWithCustomer = await listProjectsWithCustomer(queryParams.value)
    
    // 合并数据
    myProjects.value = res.rows.map(project => {
      const withCustomer = projectsWithCustomer.rows.find(p => p.id === project.id)
      return {
        ...project,
        customer: withCustomer?.customer
      }
    })
    myProjectsTotal.value = res.total
  } catch (error) {
    console.error('加载项目数据失败:', error)
  } finally {
    projectsLoading.value = false
  }
}

// ========== 导航方法 ==========
function goToCustomers() {
  router.push('/customers')
}

function goToCustomerDetail(id) {
  router.push({ path: '/customers', query: { id } })
}

function goToProjects(status) {
  if (status) {
    router.push({ path: '/projects', query: { status } })
  } else {
    router.push('/projects')
  }
}

function goToProjectDetail(id) {
  router.push({ path: '/projects', query: { id } })
}

function goToProjectProgress(id) {
  router.push({ path: '/projectScheduleRecords', query: { projectId: id } })
}

function goToQualityIssues() {
  router.push('/qualityInspections')
}

function goToAcceptance() {
  router.push('/acceptanceRecords')
}

// ========== 初始化 ==========
onMounted(() => {
  if (isAdmin.value) {
    loadStats()
    loadTopCustomers()
    loadInProgressProjects()
  } else {
    getMyProjects()
  }
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-header {
  margin-bottom: 20px;
  
  .page-title {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }
  
  .page-subtitle {
    margin: 4px 0 0;
    font-size: 14px;
    color: #909399;
  }
}

// ========== 统计卡片 ==========
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  
  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    
    .el-icon {
      font-size: 28px;
      color: #fff;
    }
    
    &.customer { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
    &.project { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
    &.budget { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
    &.quality { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
  }
  
  .stat-content {
    flex: 1;
    
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 4px;
    }
    
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      line-height: 1.2;
      
      &.customer { color: #667eea; }
      &.project { color: #11998e; }
      &.budget { color: #f5576c; }
      &.quality { color: #4facfe; }
      
      .stat-unit {
        font-size: 14px;
        font-weight: normal;
        margin-left: 4px;
        color: #909399;
      }
    }
    
    .stat-extra {
      font-size: 12px;
      color: #c0c4cc;
      margin-top: 4px;
    }
  }
}

// ========== 通用卡片样式 ==========
.section-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: none;
  
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
  }
  
  :deep(.el-card__body) {
    padding: 0;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .section-title {
    display: flex;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    
    .section-icon {
      font-size: 20px;
      margin-right: 8px;
      
      &.customer { color: #667eea; }
      &.project { color: #11998e; }
      &.todo { color: #e6a23c; }
    }
  }
}

// ========== 客户列表 ==========
.customer-list {
  padding: 8px 0;
}

.customer-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background: #f9fafc;
  }
  
  .customer-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    
    .el-icon {
      font-size: 24px;
      color: #fff;
    }
  }
  
  .customer-info {
    flex: 1;
    
    .customer-name {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .customer-contact {
      display: flex;
      align-items: center;
      font-size: 13px;
      color: #909399;
      margin-bottom: 6px;
      
      .el-icon {
        font-size: 14px;
        margin-right: 4px;
      }
    }
    
    .customer-stats {
      font-size: 13px;
      color: #606266;
      
      b {
        color: #303133;
      }
      
      .amount {
        color: #f5576c;
      }
      
      .divider {
        margin: 0 8px;
        color: #dcdfe6;
      }
      
      .in-progress-tag {
        display: inline-block;
        margin-left: 12px;
        padding: 2px 8px;
        background: #e6f7ff;
        color: #1890ff;
        border-radius: 10px;
        font-size: 12px;
      }
    }
  }
}

// ========== 项目列表 ==========
.project-list {
  padding: 8px 0;
}

.project-item {
  padding: 16px 20px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background: #f9fafc;
  }
  
  .project-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    
    .project-name {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .project-meta {
    display: flex;
    align-items: center;
    font-size: 13px;
    color: #909399;
    margin-bottom: 12px;
    
    .el-icon {
      font-size: 14px;
      margin-right: 4px;
    }
  }
  
  .project-progress {
    margin-bottom: 12px;
    
    .progress-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 6px;
      
      .progress-label {
        font-size: 13px;
        color: #909399;
      }
      
      .progress-value {
        font-size: 13px;
        font-weight: 600;
        color: #11998e;
      }
    }
  }
  
  .project-footer {
    display: flex;
    align-items: center;
    font-size: 13px;
    
    .budget-label {
      color: #909399;
      margin-right: 4px;
    }
    
    .budget-value {
      color: #f5576c;
      font-weight: 600;
    }
    
    .date-label {
      margin-left: auto;
      color: #c0c4cc;
    }
  }
}

// ========== 待办事项 ==========
.todo-card {
  height: 100%;
}

.todo-list {
  padding: 8px 0;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background: #f9fafc;
  }
  
  .todo-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    
    .el-icon {
      font-size: 20px;
      color: #fff;
    }
  }
  
  &.warning .todo-icon {
    background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
  }
  
  &.danger .todo-icon {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }
  
  &.info .todo-icon {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }
  
  .todo-content {
    flex: 1;
    
    .todo-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 2px;
    }
    
    .todo-desc {
      font-size: 12px;
      color: #909399;
    }
  }
}

.bottom-row {
  .el-col {
    margin-bottom: 16px;
  }
}

// ========== 普通用户项目卡片 ==========
.my-projects-container {
  min-height: 400px;
}

.project-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
  height: 100%;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
  }
  
  :deep(.el-card__header) {
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;
  }
  
  :deep(.el-card__body) {
    padding: 16px;
  }
  
  :deep(.el-card__footer) {
    padding: 12px 16px;
    border-top: 1px solid #f0f0f0;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    
    .card-title {
      flex: 1;
      min-width: 0;
      
      .name {
        display: block;
        font-size: 15px;
        font-weight: 600;
        color: #303133;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        margin-bottom: 4px;
      }
      
      .address {
        display: block;
        font-size: 12px;
        color: #909399;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }
  
  .card-body {
    .info-row {
      display: flex;
      align-items: center;
      font-size: 13px;
      color: #606266;
      margin-bottom: 8px;
      
      .el-icon {
        font-size: 14px;
        margin-right: 6px;
        color: #909399;
      }
    }
    
    .progress-section {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px dashed #ebeef5;
      
      .progress-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        font-size: 13px;
        color: #909399;
        
        .progress-percent {
          font-weight: 600;
          color: #11998e;
        }
      }
    }
    
    .budget-section {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px dashed #ebeef5;
      
      .budget-row {
        display: flex;
        justify-content: space-between;
        font-size: 13px;
        margin-bottom: 4px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .label {
          color: #909399;
        }
        
        .value {
          font-weight: 500;
          color: #606266;
          
          &.primary {
            color: #409eff;
            font-weight: 600;
          }
        }
      }
    }
  }
  
  .card-footer {
    display: flex;
    justify-content: space-around;
  }
}

// ========== 响应式 ==========
@media (max-width: 768px) {
  .dashboard-container {
    padding: 12px;
  }
  
  .stat-card {
    padding: 16px;
    
    .stat-icon {
      width: 44px;
      height: 44px;
      margin-right: 12px;
      
      .el-icon {
        font-size: 22px;
      }
    }
    
    .stat-content {
      .stat-value {
        font-size: 22px;
      }
    }
  }
  
  .customer-item,
  .project-item,
  .todo-item {
    padding: 12px 16px;
  }
}
</style>
