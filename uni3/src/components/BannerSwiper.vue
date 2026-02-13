<template>
  <view class="banner-swiper-container" v-if="banners && banners.length > 0">
    <swiper 
      class="banner-swiper"
      :autoplay="autoplay"
      :interval="interval"
      :circular="circular"
      :indicator-dots="showDots"
      indicator-color="rgba(255,255,255,0.4)"
      indicator-active-color="#ffffff"
      @change="handleChange"
    >
      <swiper-item 
        v-for="item in banners" 
        :key="item.id"
        @click="handleClick(item)"
      >
        <view class="banner-item" :class="{ 'is-loading': clickingId === item.id }">
          <image 
            class="banner-image" 
            :src="getFullImageUrl(item.coverImage)" 
            mode="aspectFill"
          />
          <view class="banner-overlay">
            <text class="banner-title">{{ item.title }}</text>
            <text v-if="item.subtitle" class="banner-subtitle">{{ item.subtitle }}</text>
          </view>
          <!-- Loading 遮罩 -->
          <view v-if="clickingId === item.id" class="loading-mask">
            <view class="loading-spinner"></view>
          </view>
        </view>
      </swiper-item>
    </swiper>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { getFullImageUrl } from '@/utils/request.js'

const props = defineProps({
  /** Banner列表 */
  banners: {
    type: Array,
    default: () => []
  },
  /** 是否自动播放 */
  autoplay: {
    type: Boolean,
    default: true
  },
  /** 自动切换间隔（毫秒） */
  interval: {
    type: Number,
    default: 4000
  },
  /** 是否循环 */
  circular: {
    type: Boolean,
    default: true
  },
  /** 是否显示指示点 */
  showDots: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['click', 'change'])

// 当前点击的 Banner ID（用于显示 loading）
const clickingId = ref(null)

const handleClick = async (item) => {
  if (!item.jumpUrl || clickingId.value) return
  
  // 显示 loading
  clickingId.value = item.id
  
  try {
    // #ifdef MP-WEIXIN
    // 跳转到视频号
    await wx.openChannelsActivity({
      finderUserName: 'sphkoEXkY7aVrmO',
      feedId: item.jumpUrl,
      success: () => {
        console.log('[BannerSwiper] 视频号跳转成功')
      },
      fail: (err) => {
        console.error('[BannerSwiper] 视频号跳转失败:', err)
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
      clickingId.value = null
    }, 300)
  }
  
  emit('click', item)
}

const handleChange = (e) => {
  emit('change', e.detail.current)
}
</script>

<style lang="scss" scoped>
.banner-swiper-container {
  padding: 0 32rpx;
  margin-bottom: 24rpx;
}

.banner-swiper {
  width: 100%;
  height: 280rpx;
  border-radius: 24rpx;
  overflow: hidden;
}

.banner-item {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.2s ease;
  
  &:active:not(.is-loading) {
    transform: scale(0.98);
  }
  
  &.is-loading {
    pointer-events: none;
  }
}

.banner-image {
  width: 100%;
  height: 100%;
}

.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
}

.banner-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.banner-subtitle {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// Loading 遮罩
.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
