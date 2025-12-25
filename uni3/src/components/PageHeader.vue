<template>
  <view class="page-header" :style="headerStyle">
    <view class="header-content">
      <!-- 左侧区域：返回按钮 + 左插槽 -->
      <view class="left-container">
        <!-- 返回按钮 -->
        <view 
          v-if="shouldShowBack" 
          class="back-btn" 
          @click="handleBack"
        >
          <text class="back-icon">❮</text>
        </view>
        
        <!-- 左侧插槽 (如头像、Logo) -->
        <slot name="left"></slot>
      </view>
      
      <!-- 中间区域：标题或默认插槽 -->
      <view class="center-container">
        <slot>
          <view class="title-block">
            <text class="main-title" :style="titleStyle" v-if="title">{{ title }}</text>
            <text class="sub-title" :style="titleStyle" v-if="subtitle">{{ subtitle }}</text>
          </view>
        </slot>
      </view>
      
      <view class="right-container">
        <slot name="right"></slot>
      </view>
    </view>
    
    <!-- 底部扩展区域 -->
    <view class="header-bottom">
      <slot name="bottom"></slot>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStatusBarHeight } from '@/utils/system.js'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  },
  showBack: {
    type: [Boolean, String],
    default: 'auto' // true, false, or 'auto'
  },
  bgColor: {
    type: String,
    default: ''
  },
  textColor: {
    type: String,
    default: ''
  }
})

const statusBarHeight = ref(0)
const canGoBack = ref(false)

const shouldShowBack = computed(() => {
  if (props.showBack === true) return true
  if (props.showBack === false) return false
  return canGoBack.value
})

const headerStyle = computed(() => {
  const style = {
    paddingTop: statusBarHeight.value + 'px'
  }
  if (props.bgColor) {
    style.background = props.bgColor
  }
  if (props.textColor) {
    style.color = props.textColor
  }
  return style
})

const titleStyle = computed(() => {
  return props.textColor ? { color: props.textColor } : {}
})

const handleBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  
  // 检查页面栈
  const pages = getCurrentPages()
  canGoBack.value = pages && pages.length > 1
})
</script>

<style lang="scss" scoped>
.page-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: $glass-bg;
}

.header-content {
  height: 56px; // 标准导航栏高度
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  position: relative;
}

.left-container {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;
  z-index: 2;
  min-width: 60rpx;
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  
  &:active {
    opacity: 0.7;
  }
}
// Removed color: $glass-text-main to allow inheritance or override via slot/style, but for now icon is text.
// Let's rely on currentColor or prop. Ideally use titleStyle color for back icon too or separate prop.
// For now, let's leave it as is, or use 'inherit'.


.center-container {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none; // 避免遮挡左右的点击
  z-index: 1;
}

.title-block {
  text-align: center;
}

.main-title {
  display: block;
  font-size: 34rpx;
  font-weight: 600;
  color: $glass-text-main;
}

.sub-title {
  display: block;
  font-size: 22rpx;
  color: $glass-text-muted;
  margin-top: 4rpx;
}

.right-container {
  flex-shrink: 0;
  z-index: 2;
  min-width: 60rpx; // 保持左右平衡
}
</style>
