<template>
  <view class="news-item" @click="handleClick">
    <image 
      class="news-image" 
      :src="getFullImageUrl(item.coverImage)" 
      mode="aspectFill"
    />
    <view class="news-content">
      <text class="news-title">{{ item.title }}</text>
      <text class="news-time">{{ formatTime(item.publishTime) }}</text>
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

// 格式化时间显示
const formatTime = (time) => {
  if (!time) return ''
  // 如果是完整日期时间，只显示日期部分
  if (time.includes(' ')) {
    return time.split(' ')[0]
  }
  return time
}
</script>

<style lang="scss" scoped>
.news-item {
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  box-shadow: $shadow-card;
  overflow: hidden;
  
  &:active {
    opacity: 0.9;
    transform: scale(0.99);
  }
}

.news-image {
  width: 100%;
  height: 280rpx;
  flex-shrink: 0;
  background: #f5f5f5;
}

.news-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  gap: 16rpx;
}

.news-title {
  flex: 1;
  font-size: 28rpx;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-time {
  flex-shrink: 0;
  font-size: 24rpx;
  color: $color-text-tertiary;
}
</style>
