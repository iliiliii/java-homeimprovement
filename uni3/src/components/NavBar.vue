<template>
  <view class="nav-bar" :style="navBarStyle">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    
    <!-- 导航栏内容 -->
    <view class="nav-content" :style="{ height: navBarHeight + 'px' }">
      <!-- 左侧返回按钮 -->
      <view class="nav-left" @click="handleBack" v-if="showBack">
        <view class="back-btn">
          <u-icon name="arrow-left" :color="iconColor" size="24" />
        </view>
      </view>
      
      <!-- 中间标题 -->
      <view class="nav-title">
        <text :style="{ color: titleColor }">{{ title }}</text>
      </view>
      
      <!-- 右侧插槽 -->
      <view class="nav-right">
        <slot name="right"></slot>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStatusBarHeight, getNavBarHeight } from '@/utils/system.js'

const props = defineProps({
  // 标题
  title: {
    type: String,
    default: ''
  },
  // 是否显示返回按钮
  showBack: {
    type: Boolean,
    default: true
  },
  // 背景色
  background: {
    type: String,
    default: 'rgba(255, 255, 255, 0.95)'
  },
  // 标题颜色
  titleColor: {
    type: String,
    default: '#1A2B3C'
  },
  // 图标颜色
  iconColor: {
    type: String,
    default: '#1A2B3C'
  },
  // 是否显示底部边框
  showBorder: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['back'])

const statusBarHeight = ref(0)
const navBarHeight = ref(0)

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  navBarHeight.value = getNavBarHeight()
})

const navBarStyle = computed(() => {
  return {
    background: props.background,
    borderBottom: props.showBorder ? '1rpx solid rgba(0, 0, 0, 0.05)' : 'none'
  }
})

const handleBack = () => {
  emit('back')
  // 如果没有自定义返回事件，默认返回上一页
  if (!emit('back')) {
    uni.navigateBack()
  }
}
</script>

<style lang="scss" scoped>
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
}

.status-bar {
  width: 100%;
}

.nav-content {
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  position: relative;
}

.nav-left {
  position: absolute;
  left: 32rpx;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  flex: 1;
  text-align: center;
  
  text {
    font-size: 32rpx;
    font-weight: 600;
    line-height: 1;
  }
}

.nav-right {
  position: absolute;
  right: 32rpx;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
}
</style>
