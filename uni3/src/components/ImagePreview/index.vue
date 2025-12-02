<template>
  <view class="image-preview-wrapper">
    <!-- 图片网格/列表 -->
    <view 
      class="image-grid" 
      :class="[`grid-${columns}`]"
      :style="gridStyle"
    >
      <view 
        v-for="(img, index) in displayImages" 
        :key="index"
        class="image-item"
        @click="handlePreview(index)"
      >
        <image 
          class="preview-thumb"
          :src="getImageUrl(img)"
          :mode="imageMode"
          :lazy-load="lazyLoad"
        />
        <!-- 图片数量角标 -->
        <view 
          v-if="showMore && index === maxCount - 1 && images.length > maxCount" 
          class="image-more"
        >
          <text>+{{ images.length - maxCount }}</text>
        </view>
        <!-- 删除按钮 -->
        <view 
          v-if="deletable" 
          class="delete-btn"
          @click.stop="handleDelete(index)"
        >
          <u-icon name="close" size="24" color="#fff" />
        </view>
      </view>
      
      <!-- 添加按钮 -->
      <view 
        v-if="showAdd && displayImages.length < maxCount" 
        class="image-item add-btn"
        @click="handleAdd"
      >
        <u-icon name="plus" size="48" color="#999" />
        <text v-if="addText" class="add-text">{{ addText }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 图片列表，支持字符串数组或对象数组
  images: {
    type: Array,
    default: () => []
  },
  // 图片字段名（当images为对象数组时）
  urlKey: {
    type: String,
    default: 'url'
  },
  // 列数
  columns: {
    type: Number,
    default: 3
  },
  // 图片裁剪模式
  imageMode: {
    type: String,
    default: 'aspectFill'
  },
  // 最大显示数量
  maxCount: {
    type: Number,
    default: 9
  },
  // 超出时显示更多
  showMore: {
    type: Boolean,
    default: true
  },
  // 是否可删除
  deletable: {
    type: Boolean,
    default: false
  },
  // 是否显示添加按钮
  showAdd: {
    type: Boolean,
    default: false
  },
  // 添加按钮文字
  addText: {
    type: String,
    default: ''
  },
  // 懒加载
  lazyLoad: {
    type: Boolean,
    default: true
  },
  // 间距
  gap: {
    type: [Number, String],
    default: 16
  },
  // 图片圆角
  radius: {
    type: [Number, String],
    default: 16
  }
})

const emit = defineEmits(['preview', 'delete', 'add', 'click'])

// 显示的图片列表
const displayImages = computed(() => {
  if (props.showMore && props.images.length > props.maxCount) {
    return props.images.slice(0, props.maxCount)
  }
  return props.images
})

// 网格样式
const gridStyle = computed(() => {
  const gap = typeof props.gap === 'number' ? `${props.gap}rpx` : props.gap
  return {
    gap: gap,
    '--image-radius': typeof props.radius === 'number' ? `${props.radius}rpx` : props.radius
  }
})

// 获取图片URL
const getImageUrl = (img) => {
  if (typeof img === 'string') {
    return img
  }
  return img[props.urlKey] || ''
}

// 获取所有图片URL列表
const getAllImageUrls = () => {
  return props.images.map(img => getImageUrl(img))
}

// 预览图片
const handlePreview = (index) => {
  emit('click', { index, image: props.images[index] })
  emit('preview', { index, images: props.images })
  
  const urls = getAllImageUrls()
  
  uni.previewImage({
    urls: urls,
    current: index,
    showmenu: true,
    success: () => {
      console.log('预览成功')
    },
    fail: (err) => {
      console.error('预览失败', err)
      uni.showToast({
        title: '图片预览失败',
        icon: 'none'
      })
    }
  })
}

// 删除图片
const handleDelete = (index) => {
  emit('delete', { index, image: props.images[index] })
}

// 添加图片
const handleAdd = () => {
  emit('add')
}

// 暴露方法供外部调用
defineExpose({
  preview: handlePreview,
  previewByUrl: (url) => {
    const urls = getAllImageUrls()
    const index = urls.indexOf(url)
    if (index >= 0) {
      handlePreview(index)
    }
  }
})
</script>

<style lang="scss" scoped>
.image-preview-wrapper {
  width: 100%;
}

.image-grid {
  display: grid;
  
  &.grid-1 {
    grid-template-columns: 1fr;
  }
  
  &.grid-2 {
    grid-template-columns: repeat(2, 1fr);
  }
  
  &.grid-3 {
    grid-template-columns: repeat(3, 1fr);
  }
  
  &.grid-4 {
    grid-template-columns: repeat(4, 1fr);
  }
}

.image-item {
  position: relative;
  width: 100%;
  padding-bottom: 100%; // 1:1 比例
  overflow: hidden;
  border-radius: var(--image-radius, 16rpx);
  background: #f5f5f5;
}

.preview-thumb {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

// 更多数量角标
.image-more {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  
  text {
    font-size: 32rpx;
    font-weight: 600;
    color: #fff;
  }
}

// 删除按钮
.delete-btn {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
}

// 添加按钮
.add-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 2rpx dashed #ddd;
  background: #fafafa;
  
  &:active {
    background: #f0f0f0;
  }
}

.add-text {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}
</style>
