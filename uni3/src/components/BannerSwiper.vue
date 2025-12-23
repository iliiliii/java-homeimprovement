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
        <view class="banner-item">
          <image 
            class="banner-image" 
            :src="item.coverImage" 
            mode="aspectFill"
          />
          <view class="banner-overlay">
            <text class="banner-title">{{ item.title }}</text>
            <text v-if="item.subtitle" class="banner-subtitle">{{ item.subtitle }}</text>
          </view>
        </view>
      </swiper-item>
    </swiper>
  </view>
</template>

<script setup>
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

const handleClick = (item) => {
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
</style>
