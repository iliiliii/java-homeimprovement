<template>
  <view class="dashboard-page">
    <!-- 客户视图 -->
    <template v-if="isCustomer">
      <CustomerDashboard 
        :projects="projects"
        :current-project="currentProject"
        :loading="loading"
        @switch-project="handleSwitchProject"
        @navigate="navigateTo"
      />
    </template>
    
    <!-- 员工视图 -->
    <template v-else-if="isStaff">
      <StaffDashboard 
        :projects="staffProjects"
        :todo-stats="todoStats"
        :loading="loading"
        @navigate="navigateTo"
        @view-project="handleViewProject"
      />
    </template>
    
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-mask">
      <view class="loading-spinner"></view>
    </view>
    
    <!-- 底部占位 -->
    <view class="tab-bar-placeholder"></view>
    
    <!-- 自定义TabBar -->
    <CustomTabBar :current="0" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user.js'
import { getCustomerDashboard, getStaffDashboard } from '@/api/dashboard.js'
import CustomTabBar from '@/components/CustomTabBar.vue'
import CustomerDashboard from './components/CustomerDashboard.vue'
import StaffDashboard from './components/StaffDashboard.vue'

const userStore = useUserStore()

const loading = ref(false)
const projects = ref([])
const staffProjects = ref([])
const todoStats = ref({ pendingInspections: 0, pendingIssues: 0, todayTasks: 0 })
const currentProjectIndex = ref(0)

// 计算属性
const isCustomer = computed(() => userStore.isCustomer)
const isStaff = computed(() => userStore.isStaff)
const currentProject = computed(() => projects.value[currentProjectIndex.value] || null)

// 初始化
onMounted(async () => {
  userStore.initFromStorage()
  await loadDashboardData()
})

// 加载首页数据
const loadDashboardData = async () => {
  loading.value = true
  try {
    if (isCustomer.value) {
      const data = await getCustomerDashboard()
      projects.value = data.projects || []
      // 恢复上次选择的项目
      const savedProjectId = uni.getStorageSync('currentProjectId')
      if (savedProjectId) {
        const index = projects.value.findIndex(p => p.id === savedProjectId)
        if (index >= 0) {
          currentProjectIndex.value = index
        }
      }
    } else if (isStaff.value) {
      const data = await getStaffDashboard()
      staffProjects.value = data.projects || []
      todoStats.value = data.todoStats || { pendingInspections: 0, pendingIssues: 0, todayTasks: 0 }
    }
  } catch (error) {
    console.error('加载首页数据失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 切换项目
const handleSwitchProject = (index) => {
  currentProjectIndex.value = index
  const project = projects.value[index]
  if (project) {
    userStore.switchProject(project.id)
  }
}

// 页面导航
const navigateTo = (url) => {
  // 如果是客户，需要带上当前项目ID
  if (isCustomer.value && currentProject.value) {
    const separator = url.includes('?') ? '&' : '?'
    url = `${url}${separator}projectId=${currentProject.value.id}`
  }
  uni.navigateTo({ url })
}

// 查看项目详情（员工）
const handleViewProject = (project) => {
  uni.navigateTo({
    url: `/pages/project/detail?projectId=${project.id}`
  })
}
</script>

<style lang="scss" scoped>
.dashboard-page {
  min-height: 100vh;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid $glass-accent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
