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
          <text class="project-status">{{ currentProject?.statusText || '暂无项目' }}</text>
        </view>
      </view>
      
      <!-- 项目卡片滑动区域 -->
      <view class="project-cards-section" v-if="projects.length > 0">
        <view class="swipe-hint" v-if="projects.length > 1">
          <text>← 左右滑动切换项目 →</text>
        </view>
        <scroll-view 
          scroll-x 
          class="project-cards-scroll"
          :scroll-left="scrollLeft"
          @scroll="onScroll"
          @scrollend="onScrollEnd"
          scroll-with-animation
          :enhanced="true"
          :show-scrollbar="false"
        >
          <view class="project-cards-container">
            <view 
              class="project-card"
              :class="[project.cardType, { active: index === currentIndex }]"
              v-for="(project, index) in projects"
              :key="project.id"
              @click="$emit('switch-project', index)"
            >
              <view class="card-header">
                <text class="card-name">{{ project.name }}</text>
                <view class="card-status" :class="project.cardType">
                  {{ project.statusText }}
                </view>
              </view>
              <view class="card-stage">
                <text>当前阶段: {{ project.currentStageText }}</text>
              </view>
              <view class="card-progress">
                <view class="progress-bar">
                  <view class="progress-fill" :style="{ width: project.progressPercent + '%' }"></view>
                </view>
                <view class="progress-info">
                  <text>进度 {{ project.progressPercent }}%</text>
                  <text v-if="project.nextMilestone">预计 {{ project.nextMilestone }} 完工</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
        
        <!-- 指示器 -->
        <view class="card-indicators" v-if="projects.length > 1">
          <view 
            class="indicator"
            :class="{ active: index === currentIndex }"
            v-for="(_, index) in projects"
            :key="index"
          ></view>
        </view>
      </view>
      
      <!-- 无项目提示 -->
      <view class="no-project" v-else>
        <text>暂无关联项目</text>
      </view>
    </view>
    
    <!-- 头部占位 -->
    <view :style="{ height: headerHeight + 'px' }"></view>
    
    <!-- 可滚动内容区域 -->
    <view class="scroll-content">
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
import { ref, computed, onMounted, nextTick, watch } from 'vue'
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
const scrollLeft = ref(0)
const currentIndex = ref(0)

// 计算当前是否设计阶段
const isDesignPhase = computed(() => {
  return props.currentProject?.status === 'DESIGN' || 
         props.currentProject?.currentStage === 'DESIGN'
})

// 监听currentProject变化，更新currentIndex
watch(() => props.currentProject, (newVal) => {
  if (newVal && props.projects.length > 0) {
    const index = props.projects.findIndex(p => p.id === newVal.id)
    if (index >= 0) {
      currentIndex.value = index
    }
  }
}, { immediate: true })

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  nextTick(() => {
    const query = uni.createSelectorQuery()
    query.select('.fixed-header').boundingClientRect(rect => {
      if (rect) {
        headerHeight.value = rect.height
      }
    }).exec()
  })
})

// 滚动事件处理
const onScroll = (e) => {
  const scrollX = e.detail.scrollLeft
  const cardWidth = 620 // 卡片宽度600 + 间距20
  const newIndex = Math.round(scrollX / cardWidth)
  if (newIndex !== currentIndex.value && newIndex >= 0 && newIndex < props.projects.length) {
    currentIndex.value = newIndex
    emit('switch-project', newIndex)
  }
}

// 滚动到指定卡片
const scrollToCard = (index) => {
  const cardWidth = 620
  scrollLeft.value = index * cardWidth
}

// 滚动结束时自动对齐到最近的卡片
const onScrollEnd = (e) => {
  const scrollX = e.detail.scrollLeft
  const cardWidth = 620
  const targetIndex = Math.round(scrollX / cardWidth)
  if (targetIndex >= 0 && targetIndex < props.projects.length) {
    // 自动对齐
    scrollLeft.value = targetIndex * cardWidth
    if (targetIndex !== currentIndex.value) {
      currentIndex.value = targetIndex
      emit('switch-project', targetIndex)
    }
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
}

// 固定头部
.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: $glass-bg;
  padding-bottom: 24rpx;
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
  }
}

// 滑动提示
.swipe-hint {
  text-align: center;
  padding: 8rpx 0;
  
  text {
    font-size: 22rpx;
    color: $glass-text-muted;
  }
}

// 项目卡片滑动区域
.project-cards-scroll {
  white-space: nowrap;
}

.project-cards-container {
  display: inline-flex;
  gap: 20rpx;
  padding: 16rpx 48rpx;
  // 让第一个和最后一个卡片可以居中
  &::before, &::after {
    content: '';
    flex-shrink: 0;
    width: 28rpx;
  }
}

.project-card {
  width: 600rpx;
  padding: 32rpx;
  border-radius: 32rpx;
  flex-shrink: 0;
  transition: all 0.3s ease;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  
  // 设计阶段 - 紫色
  &.design {
    background: linear-gradient(145deg, #F3E8FF 0%, #E9D5FF 100%);
    
    .progress-fill {
      background: linear-gradient(90deg, #A855F7 0%, #7C3AED 100%);
    }
    
    .card-status {
      background: rgba(168, 85, 247, 0.15);
      color: #7C3AED;
    }
  }
  
  // 施工阶段 - 蓝色
  &.construction {
    background: linear-gradient(145deg, #DBEAFE 0%, #BFDBFE 100%);
    
    .progress-fill {
      background: linear-gradient(90deg, #3B82F6 0%, #2563EB 100%);
    }
    
    .card-status {
      background: rgba(59, 130, 246, 0.15);
      color: #2563EB;
    }
  }
  
  // 已完工 - 绿色
  &.completed {
    background: linear-gradient(145deg, #D1FAE5 0%, #A7F3D0 100%);
    
    .progress-fill {
      background: linear-gradient(90deg, #10B981 0%, #059669 100%);
    }
    
    .card-status {
      background: rgba(16, 185, 129, 0.15);
      color: #059669;
    }
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
  
  text {
    opacity: 0.9;
  }
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
  margin-top: 16rpx;
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
  
  text {
    font-size: 28rpx;
    color: $glass-text-muted;
  }
}

// 可滚动内容
.scroll-content {
  padding-top: 32rpx;
}

// 功能菜单
.menu-section {
  padding: 0 48rpx;
  margin-bottom: 48rpx;
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

.section-header {
  margin-bottom: 32rpx;
  
  .section-title {
    margin-bottom: 0;
  }
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
