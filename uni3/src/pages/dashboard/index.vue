<template>
  <view class="dashboard-page">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-mask">
      <view class="loading-spinner"></view>
    </view>
    
    <!-- 客户视图 -->
    <CustomerDashboard 
      v-else-if="userType === 'customer'"
      :projects="projects"
      :current-project="currentProject"
      :loading="loading"
      @switch-project="handleSwitchProject"
      @navigate="navigateTo"
    />
    
    <!-- 员工视图 -->
    <StaffDashboard 
      v-else-if="userType === 'staff'"
      :projects="staffProjects"
      :loading="loading"
      :selected-project-id="selectedStaffProjectId"
      @view-project="handleViewProject"
      @select-project="handleSelectStaffProject"
    />
    
    <!-- 未登录或未知用户类型 -->
    <view v-else class="empty-state">
      <text class="empty-text">请先登录</text>
      <view class="login-btn" @click="goLogin">去登录</view>
    </view>
    
    <!-- 底部占位 (仅员工端需要，客户端使用内部滚动) -->
    <view class="tab-bar-placeholder" v-if="userType !== 'customer'"></view>
    
    <!-- 自定义TabBar -->
    <CustomTabBar :current="0" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getCustomerDashboard, getStaffDashboard } from '@/api/dashboard.js'
import CustomTabBar from '@/components/CustomTabBar.vue'
import CustomerDashboard from './components/CustomerDashboard.vue'
import StaffDashboard from './components/StaffDashboard.vue'

const userStore = useUserStore()

const loading = ref(true)
const userType = ref('')
const projects = ref([])
const staffProjects = ref([])
const currentProjectIndex = ref(0)
const selectedStaffProjectId = ref('')

// 计算属性
const currentProject = computed(() => projects.value[currentProjectIndex.value] || null)

// 初始化
onMounted(async () => {
  // 先从本地存储恢复用户状态
  userStore.initFromStorage()
  userType.value = userStore.userType
  
  console.log('[Dashboard] 用户类型:', userType.value)
  console.log('[Dashboard] Token:', userStore.token ? '存在' : '不存在')
  
  // 如果没有登录，跳转登录页
  if (!userStore.token) {
    loading.value = false
    return
  }
  
  await loadDashboardData()
})

// 加载首页数据
const loadDashboardData = async () => {
  loading.value = true
  try {
    if (userType.value === 'customer') {
      console.log('[Dashboard] 加载客户首页数据...')
      const data = await getCustomerDashboard()
      console.log('[Dashboard] 客户数据:', data)
      projects.value = data?.projects || []
      // 恢复上次选择的项目
      const savedProjectId = uni.getStorageSync('currentProjectId')
      if (savedProjectId) {
        const index = projects.value.findIndex(p => p.id === savedProjectId)
        if (index >= 0) {
          currentProjectIndex.value = index
        }
      }
    } else if (userType.value === 'staff') {
      console.log('[Dashboard] 加载员工首页数据...')
      const data = await getStaffDashboard()
      console.log('[Dashboard] 员工数据:', data)
      staffProjects.value = data?.projects || []
      // 将员工项目列表保存到store，以便其他页面访问
      userStore.setProjects(staffProjects.value)
      // 恢复或设置默认选中的项目
      const savedProjectId = uni.getStorageSync('currentProjectId')
      if (savedProjectId && staffProjects.value.find(p => p.id === savedProjectId)) {
        selectedStaffProjectId.value = savedProjectId
        userStore.switchProject(savedProjectId)
      } else if (staffProjects.value.length > 0) {
        selectedStaffProjectId.value = staffProjects.value[0].id
        userStore.switchProject(staffProjects.value[0].id)
      }
    } else {
      console.log('[Dashboard] 未知用户类型:', userType.value)
    }
  } catch (error) {
    console.error('[Dashboard] 加载首页数据失败:', error)
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 跳转登录
const goLogin = () => {
  uni.reLaunch({ url: '/pages/login/index' })
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
  if (userType.value === 'customer' && currentProject.value) {
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

// 选择项目（员工）
const handleSelectStaffProject = (projectId) => {
  selectedStaffProjectId.value = projectId
  userStore.switchProject(projectId)
}

// 下拉刷新
onPullDownRefresh(async () => {
  await loadDashboardData()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss" scoped>
.dashboard-page {
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

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 48rpx;
}

.empty-text {
  font-size: 32rpx;
  color: $glass-text-muted;
  margin-bottom: 32rpx;
}

.login-btn {
  padding: 24rpx 64rpx;
  background: $glass-accent;
  color: white;
  border-radius: 48rpx;
  font-size: 28rpx;
}

// 底部TabBar占位
.tab-bar-placeholder {
  height: 100rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
