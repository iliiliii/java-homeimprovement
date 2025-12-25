<template>
  <view class="customer-dashboard">
    <!-- 固定头部区域 -->
    <view class="fixed-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <!-- 标题 -->
      <view class="header-title">
        <text class="page-title">项目概况</text>
        <text v-if="currentProject" class="project-name">{{ currentProject.name }}</text>
      </view>
    </view>
    
    <!-- 可滚动内容区域 -->
    <scroll-view 
      class="scroll-content" 
      scroll-y
      :show-scrollbar="false" 
      v-if="projects.length > 0"
    >
      <!-- 头部占位：优先使用测量高度，失败则使用粗略估算高度 (状态栏 + 导航栏 + 副标题 + 边距) -->
      <view :style="{ height: (headerHeight || (statusBarHeight + 44 + 40)) + 'px' }"></view>
      <!-- Banner 轮播 -->
      <BannerSwiper 
        :banners="bannerList" 
        @click="handleBannerClick"
      />
      
      <!-- 项目卡片滑动区域 -->
      <view class="project-cards-section">
        <view 
          class="project-cards-wrapper"
          @touchstart="onTouchStart"
          @touchmove="onTouchMove"
          @touchend="onTouchEnd"
        >
          <view 
            class="project-cards-container"
            :style="{ 
              transform: `translateX(${translateX}px)`,
              transition: isAnimating ? 'transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94)' : 'none'
            }"
          >
            <ProjectCard
              v-for="(project, index) in projects"
              :key="project.id"
              :project="project"
              :user-info="userInfo"
              :active="index === currentIndex"
              @click="handleCardClick(index)"
            />
          </view>
        </view>
        
        <!-- 指示器 -->
        <view class="card-indicators" v-if="projects.length > 1">
          <view 
            class="indicator"
            :class="{ active: index === currentIndex }"
            v-for="(_, index) in projects"
            :key="index"
            @click="scrollToCard(index)"
          ></view>
        </view>
      </view>
      
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
    <view class="no-project" v-else :style="{ paddingTop: (headerHeight || (statusBarHeight + 88)) + 'px' }">
      <text>暂无关联项目</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, watch, getCurrentInstance, nextTick } from 'vue'
import { useUserStore } from '@/store/user.js'
import { getBannerNews, getNewsList } from '@/api/news.js'
import { getStatusBarHeight } from '@/utils/system.js'
import UserAvatar from '@/components/UserAvatar.vue'
import BannerSwiper from '@/components/BannerSwiper.vue'
import ProjectCard from '@/components/ProjectCard.vue'
import NewsTab from '@/components/NewsTab.vue'
import NewsItem from '@/components/NewsItem.vue'

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

// 状态
const statusBarHeight = ref(0)
const headerHeight = ref(0)
const currentIndex = ref(0)

// 滑动相关状态
const translateX = ref(0)
const isAnimating = ref(false)
const touchStartX = ref(0)
const touchStartTranslateX = ref(0)

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

// 根据状态获取卡片类型样式
const getCardTypeClass = (project) => {
  const status = project?.status?.toUpperCase()
  if (status === 'DESIGN') return 'design'
  if (status === 'COMPLETED') return 'completed'
  if (status === 'PENDING') return 'pending'
  return 'construction'
}

// 监听currentProject变化，更新currentIndex
watch(() => props.currentProject, (newVal) => {
  if (newVal && props.projects.length > 0) {
    const index = props.projects.findIndex(p => p.id === newVal.id)
    if (index >= 0 && index !== currentIndex.value) {
      currentIndex.value = index
      scrollToCard(index)
    }
  }
}, { immediate: true })

// 监听projects变化，重新计算位置和高度
watch(() => props.projects, () => {
  scrollToCard(currentIndex.value)
  updateHeaderHeight()
}, { deep: true })

// 更新头部高度
const updateHeaderHeight = () => {
  // 立即尝试一次
  getHeaderRect()
  // 延时重试，确保渲染完成
  setTimeout(getHeaderRect, 500)
}

const getHeaderRect = () => {
  nextTick(() => {
    const query = uni.createSelectorQuery().in(getCurrentInstance())
    query.select('.fixed-header').boundingClientRect(rect => {
      if (rect && rect.height > 0) {
        headerHeight.value = rect.height
      }
    }).exec()
  })
}



onMounted(async () => {
  statusBarHeight.value = getStatusBarHeight()
  updateHeaderHeight()
  
  // 加载 Banner 和资讯数据
  
  // 加载 Banner 和资讯数据
  await Promise.all([
    loadBannerNews(),
    loadNewsList()
  ])
})

// 获取实际卡片宽度（rpx转px）
const getCardWidthPx = () => {
  const screenWidth = uni.getSystemInfoSync().windowWidth
  // 卡片宽度为 100vw - 64rpx，转换为px
  return screenWidth - (64 / 750) * screenWidth
}

// Touch事件处理 - 带回弹效果
const onTouchStart = (e) => {
  isAnimating.value = false
  touchStartX.value = e.touches[0].clientX
  touchStartTranslateX.value = translateX.value
}

const onTouchMove = (e) => {
  const deltaX = e.touches[0].clientX - touchStartX.value
  let newTranslateX = touchStartTranslateX.value + deltaX
  
  const cardWidthPx = getCardWidthPx()
  const maxTranslate = 0
  const minTranslate = -cardWidthPx * (props.projects.length - 1)
  
  // 添加阻尼效果
  if (newTranslateX > maxTranslate) {
    newTranslateX = maxTranslate + (newTranslateX - maxTranslate) * 0.3
  } else if (newTranslateX < minTranslate) {
    newTranslateX = minTranslate + (newTranslateX - minTranslate) * 0.3
  }
  
  translateX.value = newTranslateX
}

const onTouchEnd = () => {
  const cardWidthPx = getCardWidthPx()
  const deltaX = translateX.value - touchStartTranslateX.value
  
  let targetIndex = currentIndex.value
  if (Math.abs(deltaX) > cardWidthPx * 0.2) {
    if (deltaX > 0 && currentIndex.value > 0) {
      targetIndex = currentIndex.value - 1
    } else if (deltaX < 0 && currentIndex.value < props.projects.length - 1) {
      targetIndex = currentIndex.value + 1
    }
  }
  
  scrollToCard(targetIndex)
  
  if (targetIndex !== currentIndex.value) {
    currentIndex.value = targetIndex
    emit('switch-project', targetIndex)
  }
}

// 滚动到指定卡片
const scrollToCard = (index) => {
  isAnimating.value = true
  const cardWidthPx = getCardWidthPx()
  translateX.value = -index * cardWidthPx
  
  setTimeout(() => {
    isAnimating.value = false
  }, 300)
}

// 点击卡片
const handleCardClick = (index) => {
  if (index !== currentIndex.value) {
    scrollToCard(index)
    currentIndex.value = index
    emit('switch-project', index)
  }
}

// 加载 Banner 资讯
const loadBannerNews = async () => {
  try {
    const data = await getBannerNews()
    bannerList.value = data || []
  } catch (error) {
    console.error('[CustomerDashboard] 加载Banner失败:', error)
  }
}

// 加载资讯列表
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
  } finally {
    newsLoading.value = false
  }
}

// Tab 切换
const handleTabChange = (tab) => {
  currentTab.value = tab
  loadNewsList(true)
}

// 加载更多资讯
const loadMoreNews = () => {
  loadNewsList(false)
}

// Banner 点击
const handleBannerClick = (item) => {
  if (item.jumpUrl) {
    // #ifdef MP-WEIXIN
    uni.navigateTo({
      url: `/pages/webview/index?url=${encodeURIComponent(item.jumpUrl)}&title=${encodeURIComponent(item.title)}`
    })
    // #endif
    
    // #ifdef H5
    window.open(item.jumpUrl, '_blank')
    // #endif
  }
}

// 资讯点击
const handleNewsClick = (item) => {
  if (item.jumpUrl) {
    // #ifdef MP-WEIXIN
    uni.navigateTo({
      url: `/pages/webview/index?url=${encodeURIComponent(item.jumpUrl)}&title=${encodeURIComponent(item.title)}`
    })
    // #endif
    
    // #ifdef H5
    window.open(item.jumpUrl, '_blank')
    // #endif
  }
}
</script>

<style lang="scss" scoped>
.customer-dashboard {
  background: $glass-bg;
  /* 移除外部 padding-bottom，避免双重滚动条 */
}


// 固定头部
.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: $glass-bg;
}

.header-title {
  padding: 24rpx 48rpx;
  text-align: center;
}

.page-title {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: $glass-text-main;
}

.project-name {
  display: block;
  font-size: 24rpx;
  color: $glass-text-muted;
  margin-top: 8rpx;
}

// 可滚动内容
.scroll-content {
  box-sizing: border-box;
}

// 项目卡片区域
.project-cards-section {
  margin-bottom: 32rpx;
  
  .section-title {
    display: block;
    font-size: 30rpx;
    font-weight: 600;
    color: $glass-text-main;
    padding: 0 32rpx 16rpx;
  }
}

.project-cards-wrapper {
  overflow: hidden;
  padding: 16rpx 0;
}

.project-cards-container {
  display: flex;
  gap: 20rpx;
  padding: 0 32rpx;
  will-change: transform;
}

// 指示器
.card-indicators {
  display: flex;
  justify-content: center;
  gap: 12rpx;
  padding-bottom: 32rpx; // 增加底部边距
}

.indicator {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #ddd;
  transition: all 0.3s ease;
  
  &.active {
    width: 28rpx;
    border-radius: 6rpx;
    background: $glass-accent;
  }
}

// 资讯区域
.news-section {
  padding-top: 16rpx;
}

.news-list {
  min-height: 200rpx;
}

.loading-more,
.load-more,
.no-more,
.empty-news {
  padding: 32rpx;
  text-align: center;
  
  text {
    font-size: 26rpx;
    color: $glass-text-muted;
  }
}

.load-more {
  text {
    color: $glass-accent;
  }
}

// 无项目提示
.no-project {
  padding: 100rpx 48rpx;
  text-align: center;
  
  text {
    font-size: 28rpx;
    color: $glass-text-muted;
  }
}
</style>
