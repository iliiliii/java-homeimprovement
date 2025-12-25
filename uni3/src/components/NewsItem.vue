<template>
  <view class="news-item" @click="handleClick">
    <image 
      class="news-image" 
      :src="getFullImageUrl(item.coverImage)" 
      mode="aspectFill"
    />
    <view class="news-content">
      <text class="news-title">{{ item.title }}</text>
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
</script>

<style lang="scss" scoped>
.news-item {
  display: flex;
  flex-direction: column; /* 上下结构 */
  gap: 16rpx;
  padding: 0; /* 移除内边距，图片顶格 */
  background: white;
  border-radius: 20rpx;
  margin-bottom: 24rpx; /* 增加间距 */
  box-shadow: $shadow-card;
  overflow: hidden; /* 圆角图片 */
  
  &:active {
    opacity: 0.9;
    transform: scale(0.99);
  }
}

.news-image {
  width: 100%;
  height: 280rpx; /* 增加高度 */
  border-radius: 0; /* 移除圆角，由容器控制 */
  flex-shrink: 0;
  background: #f5f5f5;
}

.news-content {
  padding: 0 24rpx 24rpx 24rpx; /* 内容区域内边距 */
  display: flex;
  flex-direction: column;
}

.news-title {
  font-size: 30rpx; /* 略微加大标题 */
  font-weight: 500;
  color: $glass-text-main;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}
</style>
