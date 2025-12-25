<template>
  <view v-if="visible" class="image-viewer" @touchmove.stop.prevent>
    <!-- 背景遮罩 -->
    <view class="viewer-mask" @click="handleClose"></view>
    
    <!-- 顶部导航栏 -->
    <view class="viewer-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-left" @click="handleClose">
        <view class="back-btn">
          <SvgIcon name="chevron-left" size="48rpx" color="#fff" />
        </view>
      </view>
      <view class="header-center">
        <text class="image-index">{{ currentIndex + 1 }} / {{ images.length }}</text>
      </view>
      <view class="header-right"></view>
    </view>
    
    <!-- 悬浮操作按钮 
    <view class="viewer-side-actions">
      <view class="side-btn" @click="handleSave">
        <SvgIcon name="download" size="44rpx" color="#fff" />
      </view>
    </view>
    -->
    <!-- 图片滑动区域 -->
    <swiper 
      class="viewer-swiper"
      :current="currentIndex"
      :duration="300"
      @change="handleSwiperChange"
    >
      <swiper-item 
        v-for="(img, index) in images" 
        :key="index"
        class="swiper-item"
      >
        <!-- 可缩放移动区域 -->
        <movable-area class="movable-area">
          <movable-view
            class="movable-view"
            :x="0"
            :y="0"
            direction="all"
            :scale="true"
            :scale-min="1"
            :scale-max="4"
            :scale-value="scaleValue"
            @scale="handleScale"
            @change="handleMoveChange"
          >
            <image 
              class="viewer-image"
              :src="getImageUrl(img)"
              mode="aspectFit"
              @load="handleImageLoad(index)"
              @error="handleImageError(index)"
              @click="handleImageClick"
              @longpress="handleLongPress"
            />
          </movable-view>
        </movable-area>
        
        <!-- 加载状态 -->
        <view v-if="loading[index]" class="loading-wrapper">
          <u-loading-icon size="48" color="#fff" />
          <text class="loading-text">加载中...</text>
        </view>
      </swiper-item>
    </swiper>
    
    <!-- 底部信息栏 -->
    <view v-if="showInfo && currentImageInfo" class="viewer-footer">
      <text v-if="currentImageInfo.name" class="image-name">{{ currentImageInfo.name }}</text>
      <text v-if="currentImageInfo.desc" class="image-desc">{{ currentImageInfo.desc }}</text>
    </view>
    
    <!-- 缩略图导航 -->
    <view v-if="showThumbnail && images.length > 1" class="thumbnail-nav">
      <scroll-view scroll-x class="thumbnail-scroll">
        <view class="thumbnail-list">
          <view 
            v-for="(img, index) in images" 
            :key="index"
            class="thumbnail-item"
            :class="{ active: currentIndex === index }"
            @click="goToImage(index)"
          >
            <image 
              class="thumbnail-image"
              :src="getImageUrl(img)"
              mode="aspectFill"
            />
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { getStatusBarHeight } from '@/utils/system.js'
import SvgIcon from '@/components/SvgIcon.vue'

const props = defineProps({
  // 是否显示
  visible: {
    type: Boolean,
    default: false
  },
  // 图片列表
  images: {
    type: Array,
    default: () => []
  },
  // 图片URL字段名
  urlKey: {
    type: String,
    default: 'url'
  },
  // 图片名称字段名
  nameKey: {
    type: String,
    default: 'name'
  },
  // 图片描述字段名
  descKey: {
    type: String,
    default: 'desc'
  },
  // 初始索引
  startIndex: {
    type: Number,
    default: 0
  },
  // 是否显示信息栏
  showInfo: {
    type: Boolean,
    default: true
  },
  // 是否显示缩略图导航
  showThumbnail: {
    type: Boolean,
    default: false
  },
  // 点击图片关闭
  closeOnClick: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:visible', 'close', 'change', 'save'])

const currentIndex = ref(0)
const scaleValue = ref(1)
const loading = ref({})
const statusBarHeight = ref(44)

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
})

// 监听visible变化，重置状态
watch(() => props.visible, (val) => {
  if (val) {
    currentIndex.value = props.startIndex
    scaleValue.value = 1
    // 初始化loading状态
    props.images.forEach((_, index) => {
      loading.value[index] = true
    })
  }
})

// 当前图片信息
const currentImageInfo = computed(() => {
  const img = props.images[currentIndex.value]
  if (!img) return null
  
  if (typeof img === 'string') {
    return { url: img }
  }
  
  return {
    url: img[props.urlKey],
    name: img[props.nameKey],
    desc: img[props.descKey]
  }
})

// 获取图片URL
const getImageUrl = (img) => {
  if (typeof img === 'string') return img
  return img[props.urlKey] || ''
}

// 滑动切换
const handleSwiperChange = (e) => {
  currentIndex.value = e.detail.current
  scaleValue.value = 1 // 重置缩放
  emit('change', { index: currentIndex.value, image: props.images[currentIndex.value] })
}

// 缩放处理
const handleScale = (e) => {
  scaleValue.value = e.detail.scale
}

// 移动处理
const handleMoveChange = (e) => {
  // 可以在这里处理移动事件
}

// 跳转到指定图片
const goToImage = (index) => {
  currentIndex.value = index
  scaleValue.value = 1
}

// 图片加载完成
const handleImageLoad = (index) => {
  loading.value[index] = false
}

// 图片加载失败
const handleImageError = (index) => {
  loading.value[index] = false
  uni.showToast({
    title: '图片加载失败',
    icon: 'none'
  })
}

// 图片点击
const handleImageClick = () => {
  // 无论是否缩放，点击都关闭
  if (props.closeOnClick) {
    handleClose()
  }
}

// 长按图片
const handleLongPress = () => {
  uni.showActionSheet({
    itemList: ['保存图片'],
    success: (res) => {
      if (res.tapIndex === 0) {
        handleSave()
      }
    }
  })
}

// 关闭预览
const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

// 保存图片
const handleSave = () => {
  const url = getImageUrl(props.images[currentIndex.value])
  
  uni.showLoading({ title: '保存中...' })
  
  // 先下载图片
  uni.downloadFile({
    url: url,
    success: (res) => {
      if (res.statusCode === 200) {
        // 保存到相册
        uni.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success: () => {
            uni.hideLoading()
            uni.showToast({
              title: '保存成功',
              icon: 'success'
            })
            emit('save', { index: currentIndex.value, url })
          },
          fail: (err) => {
            uni.hideLoading()
            if (err.errMsg.includes('auth')) {
              uni.showModal({
                title: '提示',
                content: '需要您授权保存图片到相册',
                confirmText: '去授权',
                success: (res) => {
                  if (res.confirm) {
                    uni.openSetting()
                  }
                }
              })
            } else {
              uni.showToast({
                title: '保存失败',
                icon: 'none'
              })
            }
          }
        })
      }
    },
    fail: () => {
      uni.hideLoading()
      uni.showToast({
        title: '下载失败',
        icon: 'none'
      })
    }
  })
}

// 暴露方法
defineExpose({
  goToImage,
  close: handleClose,
  save: handleSave
})
</script>

<style lang="scss" scoped>
.image-viewer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10000;
  background: rgba(10, 10, 10, 0.95); /* 非纯黑背景 */
  backdrop-filter: blur(20rpx); /* 毛玻璃效果 */
}

// 遮罩
.viewer-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

// 顶部导航栏
.viewer-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 88rpx; /* 导航栏标准高度 */
  box-sizing: content-box;
  background: linear-gradient(to bottom, rgba(0,0,0,0.5), transparent);
}

.header-left, .header-right {
  width: 100rpx;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-btn {
  width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-index {
  font-size: 30rpx;
  color: #fff;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.1);
  padding: 4rpx 24rpx;
  border-radius: 100rpx;
}

// 滑动区域
.viewer-swiper {
  width: 100%;
  height: 100%;
}

.swiper-item {
  display: flex;
  align-items: center;
  justify-content: center;
}

// 可移动区域
.movable-area {
  width: 100%;
  height: 100%;
}

.movable-view {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.viewer-image {
  width: 100%;
  height: 100%;
}

// 加载状态
.loading-wrapper {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.loading-text {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
}

// 底部信息栏
.viewer-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 10;
  padding: 24rpx 32rpx 60rpx;
  padding-bottom: calc(60rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(60rpx + env(safe-area-inset-bottom));
  background: linear-gradient(to top, rgba(0,0,0,0.5), transparent);
}

.image-name {
  display: block;
  font-size: 32rpx;
  color: #fff;
  font-weight: 500;
  margin-bottom: 8rpx;
}

.image-desc {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.5;
}

// 缩略图导航
.thumbnail-nav {
  position: absolute;
  bottom: 200rpx;
  left: 0;
  right: 0;
  z-index: 10;
  padding: 0 32rpx;
}

.thumbnail-scroll {
  white-space: nowrap;
}

.thumbnail-list {
  display: inline-flex;
  gap: 16rpx;
  padding: 16rpx 0;
}

.thumbnail-item {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  overflow: hidden;
  opacity: 0.6;
  border: 4rpx solid transparent;
  transition: all 0.3s;
  
  &.active {
    opacity: 1;
    border-color: #fff;
  }
}

.thumbnail-image {
  width: 100%;
  height: 100%;
}

// 左侧操作按钮
.viewer-side-actions {
  position: absolute;
  left: 32rpx;
  bottom: 240rpx; /* 调整位置，避免遮挡 */
  z-index: 20;
}

.side-btn {
  width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  backdrop-filter: blur(20rpx);
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  
  &:active {
    background: rgba(255, 255, 255, 0.25);
  }
}
</style>
