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
      v-if="projects.length > 0"
    >
      <!-- 头部占位：优先使用测量高度，失败则使用粗略估算高度 (状态栏 + 导航栏 + 副标题 + 边距) -->
      <!-- 头部占位：状态栏 + 导航栏 (56px) -->
      <view :style="{ height: (statusBarHeight + 56) + 'px' }"></view>
      <!-- Banner 轮播 -->
      <BannerSwiper 
        :banners="bannerList" 
        @click="handleBannerClick"
      />
      
       <!-- 项目卡片滑动区域 -->
       <ProjectCardSwiper
         v-if="projects.length > 0"
         :projects="projects"
         :user-info="userInfo"
         :current="currentIndex"
         @update:current="index => currentIndex = index"
         @change="handleSwiperChange"
         @click="handleCardClick"
       />
      
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
    
    <!-- 无项目提示 -->
    <view class="no-project" v-else :style="{ paddingTop: (statusBarHeight + 56) + 'px' }">
      <text>暂无关联项目</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, watch, getCurrentInstance, nextTick } from 'vue'
import { useUserStore } from '@/store/user.js'
import { getBannerNews, getNewsList } from '@/api/news.js'
import UserAvatar from '@/components/UserAvatar.vue'
import BannerSwiper from '@/components/BannerSwiper.vue'
import ProjectCardSwiper from '@/components/ProjectCardSwiper.vue'
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

const emit = defineEmits(['switch-project', 'navigate'])

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

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 监听 currentProject 变化，同步更新 currentIndex
watch(() => props.currentProject, (newProject) => {
  if (newProject && props.projects.length > 0) {
    const index = props.projects.findIndex(project => project.id === newProject.id)
    if (index >= 0 && index !== currentIndex.value) {
      currentIndex.value = index
    }
  }
}, { immediate: true })

// 生命周期
onMounted(async () => {
  // 获取系统状态栏高度
  statusBarHeight.value = getStatusBarHeight()
  
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

// Banner 点击跳转
const handleBannerClick = (item) => {
  if (!item.jumpUrl) return
  
  // #ifdef MP-WEIXIN
  uni.navigateTo({
    url: `/pages/webview/index?url=${encodeURIComponent(item.jumpUrl)}&title=${encodeURIComponent(item.title || 'Banner')}`
  })
  // #endif
  
  // #ifdef H5
  window.open(item.jumpUrl, '_blank')
  // #endif
}

// 资讯点击跳转
const handleNewsClick = (item) => {
  if (!item.jumpUrl) return
  
  // #ifdef MP-WEIXIN
  uni.navigateTo({
    url: `/pages/webview/index?url=${encodeURIComponent(item.jumpUrl)}&title=${encodeURIComponent(item.title || '资讯详情')}`
  })
  // #endif
  
  // #ifdef H5
  window.open(item.jumpUrl, '_blank')
  // #endif
}
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
