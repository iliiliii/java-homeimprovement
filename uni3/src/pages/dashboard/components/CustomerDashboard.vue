<template>
  <view class="customer-dashboard">
    <!-- 固定头部区域 -->
    <!-- 固定头部区域 -->
    <PageHeader 
      title="项目概况"
      :subtitle="currentProject?.name"
      :show-back="false"
    />
    
    <!-- 可滚动内容区域 -->
    <scroll-view 
      class="scroll-content" 
      scroll-y
      :show-scrollbar="false"
    >
      <!-- 头部占位：优先使用测量高度，失败则使用粗略估算高度 (状态栏 + 导航栏 + 副标题 + 边距) -->
      <!-- 头部占位：状态栏 + 导航栏 (56px) -->
      <view :style="{ height: (statusBarHeight + 56) + 'px' }"></view>
      
      <!-- Banner 轮播 -->
      <BannerSwiper 
        :banners="bannerList" 
        @click="handleBannerClick"
      />
      
      <!-- 项目卡片滑动区域（仅在有项目且非游客用户时显示） -->
      <ProjectCardSwiper
        v-if="projects.length > 0 && !isGuestMode"
        :projects="projects"
        :user-info="userInfo"
        :current="currentIndex"
        @update:current="index => currentIndex = index"
        @change="handleSwiperChange"
        @click="handleCardClick"
      />
      
      <!-- 项目团队成员卡片（仅在有当前项目时显示） -->
      <ProjectTeamCard
        v-if="currentProject && !isGuestMode"
        ref="teamCardRef"
        :project-id="currentProject.id"
      />
      
      <!-- 项目数据加载中（已登录客户但项目数据还在加载） -->
      <view v-else-if="!isGuestMode && props.loading" class="project-loading">
        <view class="loading-spinner"></view>
        <text>加载项目数据中...</text>
      </view>
      
      <!-- 游客用户提示（已登录游客或未登录游客）
      <view v-else-if="isGuestMode" class="no-project-tip">
        <view class="tip-icon">🏠</view>
        <text class="tip-text">{{ userStore.isLoggedIn ? '导入历史数据后可查看您的项目' : '登录后可查看您的项目进度' }}</text>
        <view v-if="!userStore.isLoggedIn" class="tip-action" @click="goToLogin">
          <text>立即登录</text>
        </view>
        <view v-else class="tip-action" @click="goToProfile">
          <text>去导入数据</text>
        </view>
      </view>
       -->
      <!-- 已登录客户但无项目的提示
      <view v-else-if="!isGuestMode && projects.length === 0" class="no-project-tip">
        <view class="tip-icon">📋</view>
        <text class="tip-text">暂无关联项目，请联系管理员</text>
      </view>
       -->
      <!-- 资讯 Tab -->
      <view class="news-section">
        <NewsTab :current="currentTab" @change="handleTabChange">
          <!-- 资讯列表 -->
          <view class="news-list">
            <NewsItem 
              v-for="item in newsList" 
              :key="item.id" 
              :item="item" 
              @click="handleNewsClick"
            />
            
            <!-- 加载更多 -->
            <view v-if="newsLoading" class="loading-more">
              <text>加载中...</text>
            </view>
            <view v-else-if="hasMoreNews" class="load-more" @click="loadMoreNews">
              <text>加载更多</text>
            </view>
            <view v-else-if="newsList.length > 0" class="no-more">
              <text>没有更多了</text>
            </view>
            <view v-else class="empty-news">
              <text>暂无资讯</text>
            </view>
          </view>
        </NewsTab>
      </view>
      
      <!-- 底部安全距离占位，确保内容不被 TabBar 遮挡
      <view style="height: 160rpx;"></view> -->
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, watch, getCurrentInstance, nextTick } from 'vue'
import { useUserStore } from '@/store/user.js'
import { getBannerNews, getNewsList } from '@/api/news.js'
import UserAvatar from '@/components/UserAvatar.vue'
import BannerSwiper from '@/components/BannerSwiper.vue'
import ProjectCardSwiper from '@/components/ProjectCardSwiper.vue'
import ProjectTeamCard from '@/components/ProjectTeamCard.vue'
import NewsTab from '@/components/NewsTab.vue'
import NewsItem from '@/components/NewsItem.vue'
import PageHeader from '@/components/PageHeader.vue'
import { getStatusBarHeight } from '@/utils/system.js'

const props = defineProps({
  projects: {
    type: Array,
    default: () => []
  },
  currentProject: {
    type: Object,
    default: null
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['switch-project', 'navigate', 'refresh'])

const userStore = useUserStore()

// 状态管理
const statusBarHeight = ref(0)
const currentIndex = ref(0)

// Banner 数据
const bannerList = ref([])

// 资讯数据
const currentTab = ref('home')
const newsList = ref([])
const newsLoading = ref(false)
const hasMoreNews = ref(true)
const newsPageNum = ref(1)
const newsPageSize = 20

// 团队卡片引用
const teamCardRef = ref(null)

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 游客模式判断（包括未登录游客和已登录游客用户）
const isGuestMode = computed(() => {
  // 未登录的游客模式
  const guestMode = uni.getStorageSync('guestMode') === true
  // 已登录的游客用户
  const isGuestUser = userStore.userType === 'guest'
  
  const result = guestMode || isGuestUser
  
  // 调试日志
  console.log('[CustomerDashboard] 游客模式判断:')
  console.log('[CustomerDashboard] guestMode:', guestMode)
  console.log('[CustomerDashboard] userType:', userStore.userType)
  console.log('[CustomerDashboard] isGuestUser:', isGuestUser)
  console.log('[CustomerDashboard] isGuestMode:', result)
  console.log('[CustomerDashboard] projects.length:', props.projects.length)
  console.log('[CustomerDashboard] 项目卡片显示条件:', props.projects.length > 0 && !result)
  
  return result
})

// 监听 currentProject 变化，同步更新 currentIndex
watch(() => props.currentProject, (newProject) => {
  if (newProject && props.projects.length > 0) {
    const index = props.projects.findIndex(project => project.id === newProject.id)
    if (index >= 0 && index !== currentIndex.value) {
      currentIndex.value = index
    }
  }
  
  // 当项目切换时，刷新团队成员数据
  if (newProject && teamCardRef.value) {
    nextTick(() => {
      teamCardRef.value.refresh()
    })
  }
}, { immediate: true })

// 监听 projects 变化
watch(() => props.projects, (newProjects) => {
  console.log('[CustomerDashboard] projects 数据变化:', newProjects)
  console.log('[CustomerDashboard] 新项目数量:', newProjects.length)
  console.log('[CustomerDashboard] isGuestMode:', isGuestMode.value)
  console.log('[CustomerDashboard] 项目卡片显示条件:', newProjects.length > 0 && !isGuestMode.value)
}, { immediate: true, deep: true })

// 生命周期
onMounted(async () => {
  // 获取系统状态栏高度
  statusBarHeight.value = getStatusBarHeight()
  
  // 调试日志
  console.log('[CustomerDashboard] onMounted 开始')
  console.log('[CustomerDashboard] 接收到的项目数据:', props.projects)
  console.log('[CustomerDashboard] 项目数量:', props.projects.length)
  console.log('[CustomerDashboard] 当前项目:', props.currentProject)
  
  // 并行加载数据
  await Promise.all([
    loadBannerNews(),
    loadNewsList()
  ])
})

// 项目切换处理
const handleSwiperChange = (index) => {
  // 组件内部已处理去重逻辑，直接通知父组件
  emit('switch-project', index)
}

// 项目卡片点击处理
const handleCardClick = (project) => {
  const index = props.projects.findIndex(p => p.id === project.id)
  if (index >= 0) {
    handleSwiperChange(index)
  }
}

// 加载 Banner 资讯数据
const loadBannerNews = async () => {
  try {
    const data = await getBannerNews()
    bannerList.value = data || []
  } catch (error) {
    console.error('[CustomerDashboard] 加载 Banner 失败:', error)
    bannerList.value = []
  }
}

// 加载资讯列表数据
const loadNewsList = async (reset = true) => {
  if (newsLoading.value) return
  
  if (reset) {
    newsPageNum.value = 1
    hasMoreNews.value = true
  }
  
  newsLoading.value = true
  
  try {
    const data = await getNewsList(currentTab.value, newsPageNum.value, newsPageSize)
    
    if (reset) {
      newsList.value = data?.list || []
    } else {
      newsList.value = [...newsList.value, ...(data?.list || [])]
    }
    
    hasMoreNews.value = data?.hasMore ?? false
    newsPageNum.value++
    
  } catch (error) {
    console.error('[CustomerDashboard] 加载资讯失败:', error)
    if (reset) {
      newsList.value = []
    }
  } finally {
    newsLoading.value = false
  }
}

// 资讯 Tab 切换
const handleTabChange = (tab) => {
  if (currentTab.value !== tab) {
    currentTab.value = tab
    loadNewsList(true)
  }
}

// 加载更多资讯
const loadMoreNews = () => {
  if (hasMoreNews.value && !newsLoading.value) {
    loadNewsList(false)
  }
}

// Banner 点击跳转（组件内部已处理）
const handleBannerClick = (item) => {
  console.log('[CustomerDashboard] Banner 点击:', item)
}

// 资讯点击跳转（组件内部已处理）
const handleNewsClick = (item) => {
  console.log('[CustomerDashboard] 资讯点击:', item)
}

// 跳转到登录页面
const goToLogin = () => {
  uni.navigateTo({ url: '/pages/login/index-new' })
}

// 跳转到个人中心导入数据
const goToProfile = () => {
  uni.switchTab({ url: '/pages/profile/index' })
}

// 刷新所有数据
const refreshAll = async () => {
  console.log('[CustomerDashboard] 刷新所有数据')
  await Promise.all([
    loadBannerNews(),
    loadNewsList(true)
  ])
  
  // 刷新团队成员数据
  if (teamCardRef.value) {
    teamCardRef.value.refresh()
  }
}

// 暴露刷新方法给父组件
defineExpose({
  refreshTeamCard: () => {
    if (teamCardRef.value) {
      teamCardRef.value.refresh()
    }
  },
  refreshAll
})
</script>

<style lang="scss" scoped>
.customer-dashboard {
  background: $color-white;
  min-height: 100vh;
  position: relative;
}

// 可滚动内容
.scroll-content {
  box-sizing: border-box;
  background: $color-white;
}

// 项目加载状态
.project-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 32rpx;
  margin: $spacing-l;
  background: $color-gray-50;
  border-radius: $radius-xl;
  border: 2rpx dashed $color-border;
  
  .loading-spinner {
    width: 60rpx;
    height: 60rpx;
    border: 4rpx solid #f3f3f3;
    border-top: 4rpx solid $color-brand;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 24rpx;
  }
  
  text {
    font-size: 26rpx;
    color: $color-text-tertiary;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

// 资讯区域
.news-section {
  padding-top: $spacing-m;
  background: $color-white;
}

.news-list {
  min-height: 400rpx;
  padding: 0 $spacing-s;
}

// 加载状态样式
.loading-more,
.load-more,
.no-more,
.empty-news {
  padding: $spacing-l;
  text-align: center;
  
  text {
    font-size: 26rpx;
    color: $color-text-tertiary;
    line-height: 1.5;
  }
}

.load-more {
  cursor: pointer;
  transition: all 0.3s ease;
  
  text {
    color: $color-brand;
    font-weight: 500;
  }
  
  &:active {
    opacity: 0.7;
    transform: scale(0.98);
  }
}

.empty-news {
  padding: $spacing-xl;
  
  text {
    color: $color-text-quaternary;
    font-size: 28rpx;
  }
}

// 无项目提示
.no-project {
  padding: 200rpx $spacing-xl;
  text-align: center;
  background: $color-white;
  margin: $spacing-l;
  
  text {
    font-size: 32rpx;
    color: $color-text-tertiary;
    line-height: 1.6;
  }
  
  &::before {
    content: '';
    display: block;
    width: 120rpx;
    height: 120rpx;
    margin: 0 auto $spacing-l;
    background: $color-gray-200;
    border-radius: 50%;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%23999'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4'/%3E%3C/svg%3E");
    background-size: 60rpx;
    background-repeat: no-repeat;
    background-position: center;
  }
}

// 无项目提示（新样式，更友好）
.no-project-tip {
  margin: $spacing-l $spacing-l $spacing-m;
  padding: $spacing-xl;
  background: linear-gradient(135deg, $color-gray-50 0%, $color-white 100%);
  border-radius: $radius-2xl;
  border: 2rpx dashed $color-border;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-m;
}

.tip-icon {
  font-size: 80rpx;
  line-height: 1;
  opacity: 0.6;
}

.tip-text {
  font-size: 28rpx;
  color: $color-text-secondary;
  line-height: 1.6;
}

.tip-action {
  margin-top: $spacing-s;
  padding: 20rpx 48rpx;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  border-radius: $radius-full;
  
  text {
    font-size: 28rpx;
    color: $color-white;
    font-weight: 500;
  }
  
  &:active {
    opacity: 0.8;
    transform: scale(0.98);
  }
}

// 响应式优化
@media (max-width: 750rpx) {
  .no-project {
    margin: $spacing-s;
    padding: 160rpx $spacing-l;
    
    text {
      font-size: 28rpx;
    }
  }
}
</style>
