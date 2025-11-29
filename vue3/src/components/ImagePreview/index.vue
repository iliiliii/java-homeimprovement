<template>
  <div class="image-preview-container">
    <!-- 单张图片模式 -->
    <div
      v-if="imageList.length <= 1"
      v-viewer="viewerOptions"
      class="vue-viewer"
    >
      <img
        v-if="imageList.length > 0"
        :src="imageList[0]"
        :alt="alt"
        :style="imageStyle"
        class="vue-viewer-img"
      />
      <div v-else class="image-slot" :style="imageStyle">
        <el-icon><PictureFilled /></el-icon>
      </div>
    </div>

    <!-- 多张图片模式 -->
    <div
      v-else
      v-viewer="viewerOptions"
      class="vue-viewer"
    >
      <!-- 显示第一张作为封面 -->
      <div class="viewer-cover-container" :style="{ width: realWidth, height: realHeight }">
        <img
          :src="imageList[0]"
          :alt="alt"
          :style="imageStyle"
          class="vue-viewer-img-cover"
        />

        <!-- 图片数量徽章 -->
        <div
          v-if="imageList.length > 1"
          class="image-count-badge"
          :style="{ width: realWidth, height: realHeight }"
        >
          <span>+{{ imageList.length - 1 }}</span>
        </div>

        <!-- 触发区域 -->
        <div
          v-if="enableThumbnail"
          class="preview-trigger"
          :style="{ width: realWidth, height: realHeight }"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { isExternal } from '@/utils/validate'
import { PictureFilled } from '@element-plus/icons-vue'

const props = defineProps({
  // 原有 API 兼容
  src: {
    type: String,
    default: ''
  },
  width: {
    type: [Number, String],
    default: ''
  },
  height: {
    type: [Number, String],
    default: ''
  },
  alt: {
    type: String,
    default: ''
  },

  // 新增 API
  images: {
    type: Array,
    default: () => []
  },
  // 预览选项
  options: {
    type: Object,
    default: () => ({})
  },
  // 是否启用缩略图模式
  enableThumbnail: {
    type: Boolean,
    default: true
  },
  // z-index
  zIndex: {
    type: Number,
    default: 3000
  }
})

// 解析图片列表
const imageList = computed(() => {
  let images = props.images.length > 0 ? props.images : (props.src ? [props.src] : [])

  return images.map(item => {
    let src = item
    // 如果已经是完整URL（包含http开头或baseUrl），则不再处理
    // 如果不是外部链接且不以baseUrl开头，才添加baseUrl
    if (!isExternal(src) && !src.startsWith(import.meta.env.VITE_APP_BASE_API)) {
      const baseUrl = import.meta.env.VITE_APP_BASE_API
      // 确保路径以/开头
      if (!src.startsWith('/')) {
        src = '/' + src
      }
      // 拼接baseUrl（移除末尾的/）
      const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
      src = cleanBaseUrl + src
    }
    return src
  }).filter(url => url)
})

// 确保图片列表正确传递给 v-viewer
const viewerImages = computed(() => {
  return imageList.value.length > 0 ? imageList.value : []
})

// 计算实际宽度
const realWidth = computed(() => {
  if (!props.width) return '150px'
  return typeof props.width === 'string' ? props.width : `${props.width}px`
})

// 计算实际高度
const realHeight = computed(() => {
  if (!props.height) return '150px'
  return typeof props.height === 'string' ? props.height : `${props.height}px`
})

// 图片样式
const imageStyle = computed(() => ({
  width: realWidth.value,
  height: realHeight.value,
  objectFit: 'cover'
}))

// 默认预览选项
const defaultOptions = {
  // 工具栏
  toolbar: true,
  // 显示缩放按钮
  zoomOn: true,
  // 显示缩小按钮
  zoomOff: true,
  // 显示旋转按钮
  rotateOn: true,
  // 显示翻转按钮
  flipHOn: true,
  // 显示全屏按钮
  fullScreen: true,
  // 显示上一张按钮
  prev: true,
  // 显示下一张按钮
  next: true,
  // 显示重置按钮
  reset: true,
  // 显示下载按钮
  download: true,

  // 导航栏
  navbar: true,
  // 标题
  title: false,
  // 按钮提示
  tooltip: true,

  // 可移动
  movable: true,
  // 可缩放
  zoomable: true,
  // 可旋转
  rotatable: true,
  // 可翻转
  flip: true,

  // 动画
  transition: true,

  // 键盘导航
  keyboard: true,

  // 循环浏览
  loop: true,

  // 最小缩放比例
  minZoomRatio: 0.1,
  // 最大缩放比例
  maxZoomRatio: 5,

  // z-index
  zIndex: props.zIndex,

  // URL 默认是当前激活的图片
  url: (image) => image.src || image
}

// 合并预览选项
const viewerOptions = computed(() => {
  const options = {
    ...defaultOptions,
    ...props.options,
    // 确保图片正确传递
    images: viewerImages.value
  }
  return options
})
</script>

<style lang="scss" scoped>
.image-preview-container {
  display: inline-block;
}

.vue-viewer {
  position: relative;
  display: inline-block;
}

.vue-viewer-img,
.vue-viewer-img-cover {
  border-radius: 5px;
  background-color: #ebeef5;
  box-shadow: 0 0 5px 1px #ccc;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: scale(1.02);
    box-shadow: 0 0 8px 2px rgba(64, 158, 255, 0.3);
  }
}

.viewer-cover-container {
  position: relative;
  display: inline-block;
}

.image-count-badge {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  border-radius: 5px;
  transition: all 0.3s;
  pointer-events: none; /* 让点击事件穿透到下层 */
  z-index: 1;

  &:hover {
    background-color: rgba(0, 0, 0, 0.75);
  }
}

.preview-trigger {
  position: absolute;
  top: 0;
  left: 0;
  cursor: pointer;
  border-radius: 5px;
  pointer-events: none; /* 让点击事件穿透到下层 */
  z-index: 2;
}


.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #909399;
  font-size: 30px;
  background-color: #f5f7fa;
  border-radius: 5px;
}
</style>
