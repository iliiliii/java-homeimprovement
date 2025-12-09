<template>
  <view class="customer-dashboard">
    <!-- 固定头部区域 -->
    <view class="fixed-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <!-- 头部信息 -->
      <view class="header-content">
        <view class="header-info">
          <text class="project-name" v-if="currentProject">
            {{ currentProject.name }} · {{ currentProject.area }}㎡
          </text>
          <text class="project-status" :class="getCardTypeClass(currentProject)">
            {{ currentProject?.statusText || '暂无项目' }}
          </text>
        </view>
      </view>
      
      <!-- 项目卡片滑动区域 -->
      <view class="project-cards-section" v-if="projects.length > 0">
        <!-- 使用touch事件实现带回弹的滑动 -->
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
            <view 
              class="project-card"
              :class="[getCardTypeClass(project), { active: index === currentIndex }]"
              v-for="(project, index) in projects"
              :key="project.id"
              @click="handleCardClick(index)"
            >
              <view class="card-header">
                <text class="card-name">{{ project.name }}</text>
                <view class="card-status" :class="getCardTypeClass(project)">
                  {{ project.statusText }}
                </view>
              </view>
              <view class="card-stage">
                <text>当前阶段: {{ project.currentStageText || '设计阶段' }}</text>
              </view>
              <view class="card-progress">
                <view class="progress-bar">
                  <view class="progress-fill" :style="{ width: (project.progressPercent || 0) + '%' }"></view>
                </view>
                <view class="progress-info">
                  <text>进度 {{ project.progressPercent || 0 }}%</text>
                  <text v-if="project.nextMilestone">预计 {{ project.nextMilestone }} 完工</text>
                </view>
              </view>
            </view>
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
      
      <!-- 无项目提示 -->
      <view class="no-project" v-else>
        <text>暂无关联项目</text>
      </view>
    </view>
    
    <!-- 头部占位 - 确保内容不被固定头部覆盖 -->
    <view class="header-placeholder" :style="{ height: headerHeight + 'px' }"></view>
    
    <!-- 可滚动内容区域 -->
    <view class="scroll-content" v-if="projects.length > 0">
      <!-- 功能菜单 -->
      <view class="menu-section">
        <view class="menu-grid">
          <!-- 设计阶段专属 -->
          <view v-if="isDesignPhase" class="menu-item" @click="$emit('navigate', '/pages/design/index')">
            <view class="menu-icon-box" style="background: rgba(201, 176, 212, 0.15);">
              <SvgIcon name="photo" size="48rpx" color="#C9B0D4" />
            </view>
            <text class="menu-text">设计图</text>
          </view>
          
          <!-- 施工阶段专属 -->
          <view v-if="!isDesignPhase" class="menu-item" @click="$emit('navigate', '/pages/schedule/index')">
            <view class="menu-icon-box" style="background: rgba(167, 185, 211, 0.15);">
              <SvgIcon name="calendar" size="48rpx" color="#A7B9D3" />
            </view>
            <text class="menu-text">排期</text>
          </view>
          
          <view v-if="!isDesignPhase" class="menu-item" @click="$emit('navigate', '/pages/log/index')">
            <view class="menu-icon-box" style="background: rgba(126, 150, 184, 0.15);">
              <SvgIcon name="file-text" size="48rpx" color="#7E96B8" />
            </view>
            <text class="menu-text">日志</text>
          </view>
          
          <!-- 文档链接 -->
          <view class="menu-item" @click="openDocLink">
            <view class="menu-icon-box" style="background: rgba(157, 193, 131, 0.15);">
              <SvgIcon name="file-text" size="48rpx" color="#9DC183" />
            </view>
            <text class="menu-text">文档</text>
          </view>
          
          <view class="menu-item" @click="$emit('navigate', '/pages/budget/index')">
            <view class="menu-icon-box" style="background: rgba(232, 180, 76, 0.15);">
              <SvgIcon name="rmb-circle" size="48rpx" color="#E8B44C" />
            </view>
            <text class="menu-text">预算</text>
          </view>
        </view>
      </view>

      <!-- 设计方案展示（设计阶段） -->
      <view v-if="isDesignPhase" class="content-section">
        <text class="section-title">设计方案</text>
        <view class="glass-card design-preview" @click="$emit('navigate', '/pages/design/index')">
          <image 
            class="design-image" 
            src="https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
            mode="aspectFill"
          />
          <view class="design-info flex-between">
            <view>
              <text class="design-title">全屋效果图</text>
              <text class="design-update">点击查看详情</text>
            </view>
            <text class="view-link">查看 ></text>
          </view>
        </view>
      </view>
      
      <!-- 最新日志（施工阶段） -->
      <view v-if="!isDesignPhase" class="content-section">
        <view class="flex-between section-header">
          <text class="section-title">最新日志</text>
          <text class="view-all" @click="$emit('navigate', '/pages/log/index')">查看全部</text>
        </view>
        <view class="log-preview glass-card" @click="$emit('navigate', '/pages/log/index')">
          <image 
            class="log-image" 
            src="https://images.unsplash.com/photo-1581094794329-c8112a89af12?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&q=80"
            mode="aspectFill"
          />
          <view class="log-content">
            <view class="flex-between log-header">
              <text class="log-title">施工进度</text>
              <text class="log-time">查看详情</text>
            </view>
            <text class="log-desc">点击查看项目施工日志和进度更新</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, getCurrentInstance } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'
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

const statusBarHeight = ref(0)
const headerHeight = ref(0)
const currentIndex = ref(0)

// 滑动相关状态
const translateX = ref(0)
const isAnimating = ref(false)
const touchStartX = ref(0)
const touchStartTranslateX = ref(0)

// 计算当前是否设计阶段
const isDesignPhase = computed(() => {
  return props.currentProject?.status === 'DESIGN' || 
         props.currentProject?.currentStage === 'DESIGN'
})

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
  nextTick(() => {
    scrollToCard(currentIndex.value)
    updateHeaderHeight()
  })
}, { deep: true })

// 更新header高度
const updateHeaderHeight = () => {
  const query = uni.createSelectorQuery().in(getCurrentInstance())
  query.select('.fixed-header').boundingClientRect(rect => {
    if (rect && rect.height > 0) {
      // 增加额外间距确保内容不被遮挡
      headerHeight.value = rect.height + 32
      console.log('[CustomerDashboard] headerHeight:', headerHeight.value, 'rect.height:', rect.height)
    }
  }).exec()
}

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  
  // 根据状态栏高度预估一个初始值，避免闪烁
  // 头部内容约 120rpx + 卡片区域约 320rpx + padding约 60rpx = 500rpx ≈ 250px + 状态栏
  const screenWidth = uni.getSystemInfoSync().windowWidth
  const estimatedHeight = (500 / 750) * screenWidth + statusBarHeight.value + 32
  headerHeight.value = estimatedHeight
  
  // 多次尝试获取精确高度
  setTimeout(updateHeaderHeight, 150)
  setTimeout(updateHeaderHeight, 400)
  setTimeout(updateHeaderHeight, 800)
})

// 获取实际卡片宽度（rpx转px）
const getCardWidthPx = () => {
  const screenWidth = uni.getSystemInfoSync().windowWidth
  return (620 / 750) * screenWidth
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

// 打开外部文档链接
const openDocLink = () => {
  const docUrl = 'https://www.kdocs.cn/l/cgkCcqM3rO5j?from=docs'
  
  // #ifdef MP-WEIXIN
  uni.navigateTo({
    url: `/pages/webview/index?url=${encodeURIComponent(docUrl)}&title=项目文档`
  })
  // #endif
  
  // #ifdef H5
  window.open(docUrl, '_blank')
  // #endif
}
</script>

<style lang="scss" scoped>
.customer-dashboard {
  min-height: 100vh;
  background: $glass-bg;
  padding-bottom: 140rpx; // 为底部TabBar留出空间
}

// 固定头部
.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: $glass-bg;
  padding-bottom: 16rpx;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 48rpx;
}

.header-info {
  .project-name {
    display: block;
    font-size: 28rpx;
    color: $glass-text-muted;
    margin-bottom: 8rpx;
  }
  
  .project-status {
    display: block;
    font-size: 40rpx;
    font-weight: 700;
    color: $glass-text-main;
    
    &.design { color: #7C3AED; }
    &.construction { color: #2563EB; }
    &.completed { color: #059669; }
    &.pending { color: #6B7280; }
  }
}

// 头部占位
.header-placeholder {
  width: 100%;
  flex-shrink: 0;
}

// 项目卡片滑动区域
.project-cards-wrapper {
  overflow: hidden;
  padding: 20rpx 0;
}

.project-cards-container {
  display: flex;
  gap: 20rpx;
  padding: 0 calc((100vw - 600rpx) / 2);
  will-change: transform;
}

.project-card {
  width: 600rpx;
  min-width: 600rpx;
  padding: 32rpx;
  border-radius: 32rpx;
  flex-shrink: 0;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  
  &.design {
    background: linear-gradient(145deg, #F3E8FF 0%, #E9D5FF 100%);
    .progress-fill { background: linear-gradient(90deg, #A855F7 0%, #7C3AED 100%); }
    .card-status { background: rgba(168, 85, 247, 0.15); color: #7C3AED; }
  }
  
  &.construction {
    background: linear-gradient(145deg, #DBEAFE 0%, #BFDBFE 100%);
    .progress-fill { background: linear-gradient(90deg, #3B82F6 0%, #2563EB 100%); }
    .card-status { background: rgba(59, 130, 246, 0.15); color: #2563EB; }
  }
  
  &.completed {
    background: linear-gradient(145deg, #D1FAE5 0%, #A7F3D0 100%);
    .progress-fill { background: linear-gradient(90deg, #10B981 0%, #059669 100%); }
    .card-status { background: rgba(16, 185, 129, 0.15); color: #059669; }
  }
  
  &.pending {
    background: linear-gradient(145deg, #F3F4F6 0%, #E5E7EB 100%);
    .progress-fill { background: linear-gradient(90deg, #9CA3AF 0%, #6B7280 100%); }
    .card-status { background: rgba(107, 114, 128, 0.15); color: #6B7280; }
  }
  
  &.active {
    transform: scale(1.02);
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.card-name {
  font-size: 34rpx;
  font-weight: 600;
  color: #1F2937;
  flex: 1;
  white-space: normal;
  word-break: break-all;
}

.card-status {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 100rpx;
  font-weight: 500;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.card-stage {
  font-size: 28rpx;
  color: #4B5563;
  margin-bottom: 24rpx;
  text { opacity: 0.9; }
}

.card-progress {
  .progress-bar {
    height: 16rpx;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 8rpx;
    overflow: hidden;
    margin-bottom: 16rpx;
  }
  
  .progress-fill {
    height: 100%;
    border-radius: 8rpx;
    transition: width 0.3s ease;
  }
  
  .progress-info {
    display: flex;
    justify-content: space-between;
    font-size: 24rpx;
    color: #6B7280;
  }
}

// 指示器
.card-indicators {
  display: flex;
  justify-content: center;
  gap: 12rpx;
  margin-top: 6rpx;
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

// 无项目提示
.no-project {
  padding: 48rpx;
  text-align: center;
  text { font-size: 28rpx; color: $glass-text-muted; }
}

// 可滚动内容
.scroll-content {
  padding-top: 24rpx;
  padding-bottom: 40rpx; // 减少这里的padding，因为父容器已经有padding-bottom
}

// 功能菜单
.menu-section {
  padding: 0 48rpx;
  margin-bottom: 32rpx;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.menu-icon-box {
  width: 100rpx;
  height: 100rpx;
  background: white;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-card;
}

.menu-text {
  font-size: 24rpx;
  font-weight: 500;
  color: $glass-text-main;
}

// 内容区域
.content-section {
  padding: 0 48rpx;
  margin-bottom: 48rpx;
}

.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: $glass-text-main;
  margin-bottom: 24rpx;
}

.section-header {
  margin-bottom: 24rpx;
  .section-title { margin-bottom: 0; }
}

.view-all {
  font-size: 24rpx;
  color: $glass-text-muted;
}

// 设计预览
.design-preview {
  padding: 0;
  overflow: hidden;
}

.design-image {
  width: 100%;
  height: 320rpx;
  display: block;
}

.design-info {
  padding: 24rpx;
}

.design-title {
  display: block;
  font-weight: 600;
  font-size: 28rpx;
  margin-bottom: 8rpx;
}

.design-update {
  display: block;
  font-size: 24rpx;
  color: $glass-text-muted;
}

.view-link {
  color: $glass-accent;
  font-size: 28rpx;
}

// 日志预览
.log-preview {
  display: flex;
  gap: 24rpx;
  padding: 24rpx;
}

.log-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.log-content {
  flex: 1;
  min-width: 0;
}

.log-header {
  margin-bottom: 8rpx;
}

.log-title {
  font-weight: 600;
  font-size: 28rpx;
}

.log-time {
  font-size: 24rpx;
  color: $glass-text-muted;
}

.log-desc {
  font-size: 24rpx;
  color: $glass-text-muted;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.6;
}
</style>
