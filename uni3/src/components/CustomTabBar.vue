<template>
  <view class="tabbar-wrapper">
    <!-- 占位元素 -->
    <view class="tabbar-placeholder"></view>
    
    <!-- TabBar 主体 -->
    <view class="tabbar">
      <!-- 背景层 -->
      <view class="tabbar-bg">
        <!-- 中间凹槽 -->
        <view class="tabbar-notch"></view>
      </view>
      
      <!-- 按钮层 -->
      <view class="tabbar-content">
        <!-- 左侧两个按钮 -->
        <view 
          v-for="(item, index) in leftTabs" 
          :key="'left-' + index" 
          class="tabbar-item"
          :class="{ active: currentIndex === index }"
          @click="switchTab(item, index)"
        >
          <view class="tabbar-icon">
            <SvgIcon 
              :name="item.icon" 
              size="44rpx"
              :color="currentIndex === index ? '#C40016' : '#9E9E9E'"
            />
          </view>
          <text class="tabbar-text">{{ item.text }}</text>
        </view>
        
        <!-- 中间品牌按钮占位 -->
        <view class="tabbar-center-space"></view>
        
        <!-- 右侧两个按钮 -->
        <view 
          v-for="(item, index) in rightTabs" 
          :key="'right-' + index" 
          class="tabbar-item"
          :class="{ active: currentIndex === (index + 2) }"
          @click="switchTab(item, index + 2)"
        >
          <view class="tabbar-icon">
            <SvgIcon 
              :name="item.icon" 
              size="44rpx"
              :color="currentIndex === (index + 2) ? '#C40016' : '#9E9E9E'"
            />
          </view>
          <text class="tabbar-text">{{ item.text }}</text>
        </view>
      </view>
      
      <!-- 中间突出按钮 - 独立定位 -->
      <view class="center-btn-container" @click="goToBrand">
        <view class="center-btn">
          <image class="center-logo" src="@/styles/logo2.png" mode="aspectFit" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'

const props = defineProps({
  current: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['change'])

const currentIndex = computed(() => props.current)

const leftTabs = [
  { pagePath: '/pages/dashboard/index', text: '概况', icon: 'home' },
  { pagePath: '/pages/design/index', text: '设计', icon: 'photo' }
]

const rightTabs = [
  { pagePath: '/pages/log/index', text: '日志', icon: 'edit-pen' },
  { pagePath: '/pages/profile/index', text: '我的', icon: 'account' }
]

const switchTab = (item, index) => {
  if (currentIndex.value === index) return
  emit('change', index)
  uni.switchTab({ url: item.pagePath })
}

const goToBrand = () => {
  uni.navigateTo({ url: '/pages/brand/index' })
}
</script>

<style lang="scss" scoped>
$tabbar-height: 100rpx;
$center-btn-size: 96rpx;
$center-btn-offset: 24rpx; // 突出高度

.tabbar-wrapper {
  width: 100%;
}

.tabbar-placeholder {
  width: 100%;
  height: $tabbar-height;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  height: $tabbar-height;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* 背景层 */
.tabbar-bg {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  top: 0;
  background: #ffffff;
  border-top: 1rpx solid #F0F0F0;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.04);
}

/* 中间凹槽装饰 */
.tabbar-notch {
  position: absolute;
  left: 50%;
  top: -$center-btn-offset;
  transform: translateX(-50%);
  width: $center-btn-size + 24rpx;
  height: $center-btn-offset + 8rpx;
  border-radius: 50rpx 50rpx 0 0;
  
  &::before {
    content: '';
    position: absolute;
    left: -20rpx;
    bottom: 0;
    width: 20rpx;
    height: 20rpx;
    background: radial-gradient(circle at 0 0, transparent 20rpx, #ffffff 20rpx);
  }
  
  &::after {
    content: '';
    position: absolute;
    right: -20rpx;
    bottom: 0;
    width: 20rpx;
    height: 20rpx;
    background: radial-gradient(circle at 100% 0, transparent 20rpx, #ffffff 20rpx);
  }
}

/* 按钮内容层 */
.tabbar-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  height: $tabbar-height;
  padding: 0 16rpx;
}

.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  transition: all 0.2s ease;
  
  &.active {
    .tabbar-text {
      color: #C40016;
      font-weight: 600;
    }
  }
  
  &:active {
    opacity: 0.7;
    transform: scale(0.96);
  }
}

.tabbar-icon {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4rpx;
}

.tabbar-text {
  font-size: 20rpx;
  color: #9E9E9E;
  line-height: 1.2;
  transition: all 0.2s ease;
}

/* 中间占位 */
.tabbar-center-space {
  width: $center-btn-size + 32rpx;
  flex-shrink: 0;
}

/* 中间突出按钮 */
.center-btn-container {
  position: absolute;
  left: 50%;
  top: -$center-btn-offset;
  transform: translateX(-50%);
  z-index: 10;
}

.center-btn {
  width: $center-btn-size;
  height: $center-btn-size;
  border-radius: 50%;
  background: linear-gradient(145deg, #C40016 0%, #9A0010 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 6rpx 20rpx rgba(196, 0, 22, 0.35),
    0 2rpx 6rpx rgba(0, 0, 0, 0.1),
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.2);
  transition: all 0.2s ease;
  border: 4rpx solid #ffffff;
  
  &:active {
    transform: scale(0.95);
    box-shadow: 
      0 4rpx 12rpx rgba(196, 0, 22, 0.4),
      0 1rpx 4rpx rgba(0, 0, 0, 0.1);
  }
}

.center-logo {
  height: 70rpx;
}
</style>
