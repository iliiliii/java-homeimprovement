<template>
  <view class="news-tab">
    <!-- Tab 头部 -->
    <view class="tab-header">
      <!-- 滑动背景 -->
      <view 
        class="tab-slider"
        :class="{ 'slider-right': current === 'commercial' }"
      ></view>
      
      <view 
        v-for="(tab, index) in tabs" 
        :key="tab.key" 
        :class="['tab-item', { active: current === tab.key }]"
        @click="handleTabClick(tab.key, index)"
      >
        <text class="tab-text">{{ tab.label }}</text>
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
  position: relative;
  background: #f5f5f5;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  border: none;
  outline: none;
}

// 滑动背景块
.tab-slider {
  position: absolute;
  top: 0;
  left: 0;
  width: 50%;
  height: 100%;
  background: $glass-accent;
  border-radius: 16rpx;
  transition: left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 0;
  border: none;
  outline: none;
  
  &.slider-right {
    left: 50%;
  }
}

.tab-item {
  flex: 1;
  position: relative;
  z-index: 1;
  padding: 20rpx 0;
  text-align: center;
  background: transparent;
  border: none;
  outline: none;
  box-sizing: border-box;
  
  .tab-text {
    font-size: 28rpx;
    color: #666;
    transition: color 0.3s ease;
    font-weight: 500;
  }
  
  &.active {
    .tab-text {
      font-weight: 600;
      color: #fff;
    }
  }
  
  &:active {
    opacity: 0.8;
  }
}

.tab-content {
  min-height: 300rpx;
}
</style>
