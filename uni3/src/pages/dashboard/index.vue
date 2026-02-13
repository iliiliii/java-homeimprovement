<template>
  <view class="dashboard-page">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-mask">
      <view class="loading-spinner"></view>
    </view>
    
    <!-- 员工视图（已登录的员工） - 最高优先级 -->
    <StaffDashboard 
      v-else-if="isLoggedIn && userType === 'staff'"
      :projects="staffProjects"
      :loading="loading"
      :selected-project-id="selectedStaffProjectId"
      :loading-more="loadingMore"
      :has-more="hasMore"
      @view-project="handleViewProject"
      @select-project="handleSelectStaffProject"
    />
    
    <!-- 客户视图（已登录的客户） -->
    <CustomerDashboard 
      v-else-if="isLoggedIn && userType === 'customer'"
      ref="customerDashboardRef"
      :projects="projects"
      :current-project="currentProject"
      :loading="loading"
      @switch-project="handleSwitchProject"
      @navigate="navigateTo"
    />
    
    <!-- 游客用户视图（已登录的游客用户） -->
    <CustomerDashboard 
      v-else-if="isLoggedIn && userType === 'guest'"
      :projects="[]"
      :current-project="null"
      :loading="loading"
      @switch-project="handleSwitchProject"
      @navigate="navigateTo"
    />
    
    <!-- 游客页面（未登录用户和游客模式用户） -->
    <CustomerDashboard 
      v-else
      :projects="[]"
      :current-project="null"
      :loading="loading"
      @switch-project="handleSwitchProject"
      @navigate="navigateTo"
    />
    
    <!-- 底部占位 (仅员工端需要，客户端和游客用户使用内部滚动) -->
    <view class="tab-bar-placeholder" v-if="isLoggedIn && userType === 'staff'"></view>
    
    <!-- 自定义TabBar -->
    <CustomTabBar :current="0" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
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

// 分页相关状态
const pageNum = ref(1)
const hasMore = ref(true)
const loadingMore = ref(false)

// 组件引用
const customerDashboardRef = ref(null)

// 计算属性
const currentProject = computed(() => projects.value[currentProjectIndex.value] || null)
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 判断是否显示游客页面
const shouldShowGuestView = computed(() => {
  const isGuest = uni.getStorageSync('guestMode') === true
  const loggedIn = userStore.isLoggedIn
  
  // 只有未登录用户或处于游客模式（未进行微信登录）的用户显示游客页面
  // 已登录的游客用户应该显示对应的用户页面
  return !loggedIn || isGuest
})

// 监听用户类型变化
watch(() => userStore.userType, (newUserType, oldUserType) => {
  console.log('[Dashboard] 用户类型变化:', oldUserType, '->', newUserType)
  if (newUserType && newUserType !== userType.value) {
    console.log('[Dashboard] 更新本地用户类型:', newUserType)
    userType.value = newUserType
    
    // 如果用户类型发生变化，重新加载数据
    if (oldUserType && oldUserType !== newUserType) {
      console.log('[Dashboard] 用户类型变化，重新加载数据')
      loadDashboardData()
    }
  }
}, { immediate: true })

// 监听项目数据变化，确保组件能响应数据更新
watch(() => projects.value, (newProjects) => {
  console.log('[Dashboard] 客户项目数据变化:', newProjects?.length || 0, '个项目')
}, { deep: true })

watch(() => staffProjects.value, (newProjects) => {
  console.log('[Dashboard] 员工项目数据变化:', newProjects?.length || 0, '个项目')
}, { deep: true })

// 初始化
onMounted(async () => {
  console.log('[Dashboard] =====页面初始化开始=====')
  
  // 先从本地存储恢复用户状态
  userStore.initFromStorage()
  
  console.log('[Dashboard] 用户状态恢复完成')
  console.log('[Dashboard] 游客模式:', uni.getStorageSync('guestMode'))
  console.log('[Dashboard] 已登录:', userStore.isLoggedIn)
  console.log('[Dashboard] 用户类型:', userStore.userType)
  console.log('[Dashboard] Token存在:', !!userStore.token)
  
  // 设置用户类型
  userType.value = userStore.userType
  
  // 如果需要显示游客页面，直接显示，不加载用户数据
  if (shouldShowGuestView.value) {
    console.log('[Dashboard] 显示游客页面，不加载数据')
    loading.value = false
    return
  }
  
  // 已登录用户，验证用户类型
  console.log('[Dashboard] 已登录用户，用户类型:', userType.value)
  
  // 添加用户类型验证
  if (!userType.value || !['customer', 'staff', 'guest'].includes(userType.value)) {
    console.error('[Dashboard] 无效的用户类型:', userType.value)
    
    // 尝试从token验证获取正确的用户类型
    try {
      console.log('[Dashboard] 尝试验证Token获取用户类型')
      await userStore.validateToken()
      userType.value = userStore.userType
      console.log('[Dashboard] Token验证成功，用户类型:', userType.value)
    } catch (error) {
      console.error('[Dashboard] Token验证失败:', error)
      userStore.logout()
      loading.value = false
      return
    }
  }
  
  // 开始加载数据
  console.log('[Dashboard] 开始加载数据，用户类型:', userType.value)
  loading.value = true
  
  try {
    await loadDashboardData()
    console.log('[Dashboard] 数据加载完成')
  } catch (error) {
    console.error('[Dashboard] 数据加载失败:', error)
  } finally {
    // 使用nextTick确保DOM更新后再设置loading状态
    await nextTick()
    loading.value = false
    console.log('[Dashboard] =====页面初始化完成=====')
  }
})

// 加载首页数据
const loadDashboardData = async (isRefresh = true) => {
  if (isRefresh) {
    pageNum.value = 1
    hasMore.value = true
  }
  
  try {
    console.log('[Dashboard] ===开始加载数据===')
    console.log('[Dashboard] 用户类型:', userType.value)
    console.log('[Dashboard] 是否刷新:', isRefresh)
    
    if (userType.value === 'customer') {
      console.log('[Dashboard] 加载客户首页数据...')
      
      // 禁用网络请求的默认loading，使用页面自己的loading
      const data = await getCustomerDashboard({ loading: false })
      console.log('[Dashboard] 客户API响应:', data)
      
      const projectList = data?.projects || []
      projects.value = projectList
      
      console.log('[Dashboard] 客户项目数据设置完成')
      console.log('[Dashboard] 项目数量:', projectList.length)
      console.log('[Dashboard] 项目列表:', projectList)
      
      // 恢复上次选择的项目
      const savedProjectId = uni.getStorageSync('currentProjectId')
      if (savedProjectId && projectList.length > 0) {
        const index = projectList.findIndex(p => p.id === savedProjectId)
        if (index >= 0) {
          currentProjectIndex.value = index
          console.log('[Dashboard] 恢复选中项目索引:', index)
        }
      }
      
    } else if (userType.value === 'guest') {
      console.log('[Dashboard] 游客用户，设置空项目数据')
      projects.value = []
      
    } else if (userType.value === 'staff') {
      console.log('[Dashboard] 加载员工首页数据...')
      
      // 禁用网络请求的默认loading，使用页面自己的loading
      const data = await getStaffDashboard(pageNum.value, { loading: false })
      console.log('[Dashboard] 员工API响应:', data)
      
      const projectList = data?.projects || []
      
      if (isRefresh) {
        staffProjects.value = projectList
      } else {
        // 追加数据
        staffProjects.value = [...staffProjects.value, ...projectList]
      }
      
      console.log('[Dashboard] 员工项目数据设置完成')
      console.log('[Dashboard] 项目数量:', staffProjects.value.length)
      console.log('[Dashboard] 项目列表:', staffProjects.value)
      
      // 更新分页状态
      if (data?.pageInfo) {
        hasMore.value = data.pageInfo.hasMore
      } else {
        hasMore.value = false
      }
      
      // 将员工项目列表保存到store，以便其他页面访问
      userStore.setProjects(staffProjects.value)
      
      // 恢复或设置默认选中的项目
      const savedProjectId = uni.getStorageSync('currentProjectId')
      if (savedProjectId && staffProjects.value.find(p => p.id === savedProjectId)) {
        selectedStaffProjectId.value = savedProjectId
        userStore.switchProject(savedProjectId)
        console.log('[Dashboard] 恢复选中项目:', savedProjectId)
      } else if (staffProjects.value.length > 0 && !selectedStaffProjectId.value) {
        selectedStaffProjectId.value = staffProjects.value[0].id
        userStore.switchProject(staffProjects.value[0].id)
        console.log('[Dashboard] 设置默认选中项目:', staffProjects.value[0].id)
      }
      
    } else {
      console.error('[Dashboard] 未知用户类型:', userType.value)
      throw new Error(`未知用户类型: ${userType.value}`)
    }
    
    console.log('[Dashboard] ===数据加载完成===')
    
  } catch (error) {
    console.error('[Dashboard] 加载首页数据失败:', error)
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
    throw error
  } finally {
    loadingMore.value = false
  }
}

// 加载更多数据
const loadMoreData = async () => {
  if (!hasMore.value || loadingMore.value || userType.value !== 'staff') {
    return
  }
  
  loadingMore.value = true
  pageNum.value++
  await loadDashboardData(false)
}

// 跳转登录
const goLogin = () => {
  uni.reLaunch({ url: '/pages/login/index-new' })
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
  console.log('[Dashboard] 用户下拉刷新')
  
  try {
    // 游客页面和游客用户的刷新处理
    if (!isLoggedIn.value || userType.value === 'guest') {
      console.log('[Dashboard] 游客用户刷新，刷新 Banner 和资讯数据')
      if (customerDashboardRef.value) {
        await customerDashboardRef.value.refreshAll()
      }
      return
    }
    
    console.log('[Dashboard] 开始刷新数据')
    
    // 并行刷新项目数据和 Banner/资讯数据
    const promises = [loadDashboardData(true)]
    
    // 如果是客户，同时刷新 Banner、资讯和团队成员数据
    if (userType.value === 'customer' && customerDashboardRef.value) {
      promises.push(customerDashboardRef.value.refreshAll())
    }
    
    await Promise.all(promises)
    
    console.log('[Dashboard] 刷新完成')
  } catch (error) {
    console.error('[Dashboard] 刷新失败:', error)
  } finally {
    uni.stopPullDownRefresh()
  }
})

// 上拉加载更多
onReachBottom(() => {
  // 游客页面和游客用户不需要加载更多用户数据
  if (!isLoggedIn.value || userType.value === 'guest') {
    return
  }
  
  loadMoreData()
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

.empty-text {
  font-size: 32rpx;
  color: $glass-text-muted;
  margin-bottom: 32rpx;
}

// 底部TabBar占位
.tab-bar-placeholder {
  height: 100rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
