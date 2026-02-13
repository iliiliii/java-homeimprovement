<template>
  <view class="news-item" :class="{ 'is-loading': isLoading }" @click="handleClick">
    <image 
      class="news-image" 
      :src="getFullImageUrl(item.coverImage)" 
      mode="aspectFill"
    />
    <view class="news-content">
      <text class="news-title">{{ item.title }}</text>
      <text class="news-time">{{ formatTime(item.publishTime) }}</text>
    </view>
    <!-- Loading 遮罩 -->
    <view v-if="isLoading" class="loading-mask">
      <view class="loading-spinner"></view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { getFullImageUrl } from '@/utils/request.js'

const props = defineProps({
  /** 资讯项数据 */
  item: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

// Loading 状态
const isLoading = ref(false)

const handleClick = async () => {
  if (!props.item.jumpUrl || isLoading.value) return
  
  // 显示 loading
  isLoading.value = true
  
  try {
    // #ifdef MP-WEIXIN
    // 跳转到视频号
    await wx.openChannelsActivity({
      finderUserName: 'sphkoEXkY7aVrmO',
      feedId: props.item.jumpUrl,
      success: () => {
        console.log('[NewsItem] 视频号跳转成功')
      },
      fail: (err) => {
        console.error('[NewsItem] 视频号跳转失败:', err)
        // 用户取消操作（errMsg 包含 cancel）时不显示提示
        if (err.errMsg && !err.errMsg.includes('cancel')) {
          uni.showToast({
            title: '跳转失败，请稍后重试',
            icon: 'none'
          })
        }
      }
    })
    // #endif
    
    // #ifndef MP-WEIXIN
    uni.showToast({
      title: '仅支持微信小程序',
      icon: 'none'
    })
    // #endif
  } finally {
    // 延迟隐藏 loading，避免闪烁
    setTimeout(() => {
      isLoading.value = false
    }, 300)
  }
  
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
  position: relative;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  box-shadow: $shadow-card;
  overflow: hidden;
  transition: all 0.2s ease;
  
  &:active:not(.is-loading) {
    opacity: 0.9;
    transform: scale(0.99);
  }
  
  &.is-loading {
    pointer-events: none;
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

// Loading 遮罩
.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #f3f3f3;
  border-top-color: #C40016;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
