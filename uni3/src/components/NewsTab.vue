<template>
  <view class="news-tab">
    <!-- Tab 头部 -->
    <view class="tab-header">
      <view 
        v-for="tab in tabs" 
        :key="tab.key" 
        :class="['tab-item', { active: current === tab.key }]"
        @click="handleTabClick(tab.key)"
      >
        <text class="tab-text">{{ tab.label }}</text>
        <view v-if="current === tab.key" class="tab-indicator"></view>
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

const handleTabClick = (key) => {
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
  gap: 48rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
  margin-bottom: 24rpx;
}

.tab-item {
  position: relative;
  padding: 16rpx 0;
  
  .tab-text {
    font-size: 30rpx;
    color: $glass-text-muted;
    transition: color 0.3s ease;
  }
  
  &.active {
    .tab-text {
      font-weight: 600;
      color: $glass-text-main;
    }
  }
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 6rpx;
  background: $glass-accent;
  border-radius: 3rpx;
}

.tab-content {
  min-height: 300rpx;
}
</style>
