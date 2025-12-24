<template>
  <view class="news-item" @click="handleClick">
    <image 
      class="news-image" 
      :src="getFullImageUrl(item.coverImage)" 
      mode="aspectFill"
    />
    <view class="news-content">
      <text class="news-title">{{ item.title }}</text>
      <text v-if="item.subtitle" class="news-subtitle">{{ item.subtitle }}</text>
      <text v-if="item.publishTime" class="news-time">{{ formatTime(item.publishTime) }}</text>
    </view>
  </view>
</template>

<script setup>
import { getFullImageUrl } from '@/utils/request.js'
const props = defineProps({
  /** 资讯项数据 */
  item: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click', props.item)
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day}`
}
</script>

<style lang="scss" scoped>
.news-item {
  display: flex;
  gap: 24rpx;
  padding: 24rpx;
  background: white;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: $shadow-card;
  
  &:active {
    opacity: 0.9;
    transform: scale(0.99);
  }
}

.news-image {
  width: 180rpx;
  height: 120rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
  background: #f5f5f5;
}

.news-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.news-title {
  font-size: 28rpx;
  font-weight: 500;
  color: $glass-text-main;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.news-subtitle {
  font-size: 24rpx;
  color: $glass-text-muted;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 8rpx;
}

.news-time {
  font-size: 22rpx;
  color: #9CA3AF;
  margin-top: 8rpx;
}
</style>
