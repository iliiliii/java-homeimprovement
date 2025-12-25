<template>
  <view class="news-tab">
    <!-- Tab 头部 -->
    <view class="tab-header">
      <view 
        v-for="(tab, index) in tabs" 
        :key="tab.key" 
        :class="['tab-item', { active: current === tab.key }]"
        @click="handleTabClick(tab.key, index)"
      >
        <!-- 装饰点 -->
        <view class="tab-dot left-dot"></view>
        <text class="tab-text">{{ tab.label }}</text>
        <view class="tab-dot right-dot"></view>
      </view>
    </view>
    
    <!-- Tab 内容 -->
    <view class="tab-content">
      <slot></slot>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  /** 当前选中的 tab key */
  current: {
    type: String,
    default: 'home'
  },
  /** Tab 列表 */
  tabs: {
    type: Array,
    default: () => [
      { key: 'home', label: '家装资讯' },
      { key: 'commercial', label: '工装资讯' }
    ]
  }
})

const emit = defineEmits(['change'])

// 点击Tab
const handleTabClick = (key, index) => {
  if (key !== props.current) {
    emit('change', key)
  }
}
</script>

<style lang="scss" scoped>
.news-tab {
  padding: 0 32rpx;
}

.tab-header {
  display: flex;
  gap: 20rpx;
  position: relative;
  background: $color-white;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
}

.tab-item {
  flex: 1;
  position: relative;
  padding: 24rpx 16rpx;
  text-align: center;
  background: $color-gray-50;
  border: 2rpx solid $color-border;
  border-radius: 16rpx;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  overflow: hidden;
  
  .tab-text {
    font-size: 28rpx;
    color: $color-text-tertiary;
    transition: all 0.3s ease;
    font-weight: 500;
    position: relative;
    z-index: 1;
  }
  
  // 装饰点
  .tab-dot {
    width: 8rpx;
    height: 8rpx;
    border-radius: 50%;
    background: $color-gray-300;
    transition: all 0.3s ease;
    opacity: 0;
    transform: scale(0);
  }
  
  &.active {
    background: linear-gradient(135deg, $color-brand 0%, $color-brand-600 100%);
    border-color: $color-brand;
    animation: breathe 2s ease-in-out infinite;
    
    .tab-text {
      font-weight: 600;
      color: $color-white;
      text-shadow: none;
    }
    
    .tab-dot {
      background: $color-white-alpha-60;
      opacity: 1;
      transform: scale(1);
      animation: dotPulse 1.5s ease-in-out infinite;
    }
    
    .left-dot {
      animation-delay: 0s;
    }
    
    .right-dot {
      animation-delay: 0.75s;
    }
  }
  
  &:not(.active):active {
    background: $color-gray-100;
    transform: scale(0.98);
  }
}

// 呼吸动画
@keyframes breathe {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(196, 0, 22, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8rpx rgba(196, 0, 22, 0.1);
  }
}

// 装饰点脉冲动画
@keyframes dotPulse {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.3);
  }
}

.tab-content {
  min-height: 300rpx;
}
</style>
