<template>
  <view class="project-swiper-container">
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
            @click="handleCardClick(index, project)"
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
  </view>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import ProjectCard from '@/components/ProjectCard.vue'

const props = defineProps({
  projects: {
    type: Array,
    default: () => []
  },
  current: {
    type: Number,
    default: 0
  },
  userInfo: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:current', 'change', 'click'])

// 响应式状态
const currentIndex = ref(0)
const translateX = ref(0)
const isAnimating = ref(false)

// 触摸交互状态
const touchStartX = ref(0)
const touchStartTranslateX = ref(0)
const touchStartTime = ref(0)

// 监听外部 current 变化
watch(() => props.current, (newIndex) => {
  if (newIndex !== currentIndex.value && isValidIndex(newIndex)) {
    currentIndex.value = newIndex
    scrollToCard(newIndex)
  }
}, { immediate: true })

// 监听 projects 变化，重新计算位置
watch(() => props.projects, (newProjects) => {
  if (newProjects.length > 0) {
    // 确保当前索引不超出范围
    const safeIndex = Math.min(currentIndex.value, newProjects.length - 1)
    if (safeIndex !== currentIndex.value) {
      currentIndex.value = safeIndex
      emit('update:current', safeIndex)
    }
    scrollToCard(currentIndex.value)
  }
}, { deep: true })

// 验证索引是否有效
const isValidIndex = (index) => {
  return index >= 0 && index < props.projects.length
}

// 获取实际卡片宽度（rpx转px）
const getCardWidthPx = () => {
  try {
    const systemInfo = uni.getSystemInfoSync()
    const screenWidth = systemInfo.windowWidth || 375
    // 卡片宽度为 100vw - 64rpx，转换为px
    return screenWidth - (64 / 750) * screenWidth
  } catch (error) {
    console.warn('[ProjectCardSwiper] 获取屏幕宽度失败:', error)
    return 311 // 默认宽度 (375 - 64)
  }
}

// Touch事件处理 - 优化的滑动体验
const onTouchStart = (e) => {
  if (!e.touches || !e.touches[0]) return
  
  isAnimating.value = false
  touchStartX.value = e.touches[0].clientX
  touchStartTranslateX.value = translateX.value
  touchStartTime.value = Date.now()
}

const onTouchMove = (e) => {
  if (!e.touches || !e.touches[0] || props.projects.length <= 1) return
  
  const deltaX = e.touches[0].clientX - touchStartX.value
  let newTranslateX = touchStartTranslateX.value + deltaX
  
  const cardWidthPx = getCardWidthPx()
  const maxTranslate = 0
  const minTranslate = -cardWidthPx * (props.projects.length - 1)
  
  // 添加阻尼效果，提供更好的边界反馈
  if (newTranslateX > maxTranslate) {
    newTranslateX = maxTranslate + (newTranslateX - maxTranslate) * 0.25
  } else if (newTranslateX < minTranslate) {
    newTranslateX = minTranslate + (newTranslateX - minTranslate) * 0.25
  }
  
  translateX.value = newTranslateX
}

const onTouchEnd = (e) => {
  if (props.projects.length <= 1) return
  
  const deltaX = translateX.value - touchStartTranslateX.value
  const deltaTime = Date.now() - touchStartTime.value
  const velocity = Math.abs(deltaX) / deltaTime // px/ms
  
  const cardWidthPx = getCardWidthPx()
  let targetIndex = currentIndex.value
  
  // 根据滑动距离和速度决定是否切换
  const threshold = velocity > 0.5 ? cardWidthPx * 0.15 : cardWidthPx * 0.25
  
  if (Math.abs(deltaX) > threshold) {
    if (deltaX > 0 && currentIndex.value > 0) {
      targetIndex = currentIndex.value - 1
    } else if (deltaX < 0 && currentIndex.value < props.projects.length - 1) {
      targetIndex = currentIndex.value + 1
    }
  }
  
  scrollToCard(targetIndex)
  updateCurrentIndex(targetIndex)
}

// 滚动到指定卡片 - 优化动画
const scrollToCard = (index) => {
  if (!isValidIndex(index)) return
  
  isAnimating.value = true
  const cardWidthPx = getCardWidthPx()
  const safeIndex = Math.max(0, Math.min(index, props.projects.length - 1))
  
  translateX.value = -safeIndex * cardWidthPx
  
  // 使用 requestAnimationFrame 优化动画性能
  setTimeout(() => {
    isAnimating.value = false
  }, 300)
}

// 更新当前索引并触发事件
const updateCurrentIndex = (index) => {
  if (!isValidIndex(index) || currentIndex.value === index) return
  
  currentIndex.value = index
  emit('update:current', index)
  emit('change', index)
}

// 点击卡片处理
const handleCardClick = (index, project) => {
  if (!isValidIndex(index)) return
  
  // 如果点击的不是当前卡片，先切换到该卡片
  if (index !== currentIndex.value) {
    scrollToCard(index)
    updateCurrentIndex(index)
  }
  
  // 始终触发 click 事件，让父组件决定后续行为
  emit('click', project)
}

onMounted(() => {
  scrollToCard(currentIndex.value)
})
</script>

<style lang="scss" scoped>
.project-swiper-container {
  width: 100%;
  background: transparent;
}

.project-cards-section {
  width: 100%;
  position: relative;
}

.project-cards-wrapper {
  overflow: hidden;
  padding: $spacing-s 0;
  position: relative;
  
  // 添加渐变遮罩效果，增强视觉层次
  &::before,
  &::after {
    content: '';
    position: absolute;
    top: 0;
    bottom: 0;
    width: 32rpx;
    z-index: 2;
    pointer-events: none;
  }
  
  &::before {
    left: 0;
    background: linear-gradient(to right, $color-white, transparent);
  }
  
  &::after {
    right: 0;
    background: linear-gradient(to left, $color-white, transparent);
  }
}

.project-cards-container {
  display: flex;
  gap: $spacing-m;
  padding: 0 $spacing-l;
  will-change: transform;
  
  // 优化滑动性能
  transform-style: preserve-3d;
  backface-visibility: hidden;
}

// 指示器样式优化
.card-indicators {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-s 0;
  margin-top: $spacing-xs;
}

.indicator {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: $color-border-medium;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  cursor: pointer;
  position: relative;
  
  // 增加点击区域
  &::before {
    content: '';
    position: absolute;
    top: -8rpx;
    left: -8rpx;
    right: -8rpx;
    bottom: -8rpx;
    border-radius: 50%;
  }
  
  &:hover {
    background: $color-brand-300;
    transform: scale(1.1);
  }
  
  &.active {
    width: 32rpx;
    height: 16rpx;
    border-radius: $radius-s;
    background: $color-brand;
    box-shadow: 0 2rpx 8rpx rgba(196, 0, 22, 0.3);
    
    &::before {
      top: -8rpx;
      left: -8rpx;
      right: -8rpx;
      bottom: -8rpx;
      border-radius: $radius-s;
    }
  }
  
  // 激活状态的动画效果
  &.active {
    animation: indicatorPulse 2s ease-in-out infinite;
  }
}

// 指示器脉冲动画
@keyframes indicatorPulse {
  0%, 100% {
    box-shadow: 0 2rpx 8rpx rgba(196, 0, 22, 0.3);
  }
  50% {
    box-shadow: 0 2rpx 12rpx rgba(196, 0, 22, 0.5);
  }
}

// 响应式优化
@media (max-width: 750rpx) {
  .project-cards-container {
    gap: $spacing-s;
    padding: 0 $spacing-m;
  }
  
  .card-indicators {
    gap: 6rpx;
  }
  
  .indicator {
    width: 12rpx;
    height: 12rpx;
    
    &.active {
      width: 24rpx;
      height: 12rpx;
    }
  }
}

// 深色模式适配 - 已禁用
/*
@media (prefers-color-scheme: dark) {
  .project-cards-wrapper {
    &::before {
      background: linear-gradient(to right, $color-gray-900, transparent);
    }
    
    &::after {
      background: linear-gradient(to left, $color-gray-900, transparent);
    }
  }
  
  .indicator {
    background: $color-gray-600;
    
    &:hover {
      background: $color-brand-400;
    }
    
    &.active {
      background: $color-brand;
    }
  }
}
*/
</style>
