<template>
  <!-- 单张图片模式 -->
  <template v-if="!images || images.length <= 1">
    <Viewer
      ref="viewerRef"
      :options="viewerOptions"
      :images="singleImageList"
      class="vue-viewer"
      @inited="inited"
    >
      <img
        v-if="singleSrc"
        :src="singleSrc"
        :alt="alt"
        :style="imageStyle"
        class="vue-viewer-img"
        @click="open"
      />
    </Viewer>
  </template>

  <!-- 多张图片模式 -->
  <template v-else>
    <Viewer
      ref="viewerRef"
      :options="viewerOptions"
      :images="processedImages"
      class="vue-viewer"
      @inited="inited"
    >
      <!-- 封面图片 + 数量徽章 -->
      <div class="viewer-cover-container" :style="{ width: realWidth, height: realHeight }">
        <img
          :src="coverImage"
          :alt="alt"
          :style="imageStyle"
          class="vue-viewer-img-cover"
          @click="open(0)"
        />

        <!-- 图片数量徽章 -->
        <div v-if="processedImages.length > 1" class="image-count-badge">
          <span>+{{ processedImages.length - 1 }}</span>
        </div>
      </div>

      <!-- 触发区域 -->
      <div
        v-if="enableOverlay"
        class="viewer-overlay"
        :style="{ width: realWidth, height: realHeight }"
        @click="open(0)"
      />

      <!-- 底部缩略图导航 -->
      <div v-if="showThumbnail && processedImages.length > 1" class="thumbnail-nav">
        <div class="thumbnail-container">
          <div
            v-for="(img, index) in processedImages"
            :key="index"
            :class="['thumbnail-item', { active: index === 0 }]"
            @click.stop="open(index)"
          >
            <img :src="img" class="thumbnail-image" />
          </div>
        </div>
      </div>
    </Viewer>
  </template>
</template>

<script setup>
import { computed, ref, nextTick } from 'vue'
import { isExternal } from '@/utils/validate'
import Viewer from 'v-viewer'

const viewerRef = ref(null)

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
  // 是否显示缩略图导航
  showThumbnail: {
    type: Boolean,
    default: true
  },
  // 缩略图宽度
  thumbnailWidth: {
    type: [Number, String],
    default: 60
  },
  // 是否启用覆盖层点击
  enableOverlay: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['inited'])

// 解析单张图片
const singleImageList = computed(() => {
  if (!props.src) return []
  const baseUrl = import.meta.env.VITE_APP_BASE_API
  const src = isExternal(props.src) ? props.src : baseUrl + props.src
  return [src]
})

// 单张图片的 src
const singleSrc = computed(() => {
  return singleImageList.value[0] || ''
})

// 处理多张图片列表
const processedImages = computed(() => {
  let images = props.images.length > 0 ? props.images : singleImageList.value
  const baseUrl = import.meta.env.VITE_APP_BASE_API

  return images.map(item => {
    let src = item
    // 如果不是外部链接，添加 baseUrl
    if (!isExternal(src)) {
      src = baseUrl + src
    }
    return src
  })
})

// 封面图片（第一张）
const coverImage = computed(() => {
  return processedImages.value[0] || ''
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

  // 动画时长（ms）
  transition: true,
  navbar: false,
  title: false,
  // 按钮位置
  tooltip: true,
  // 动画效果
  movable: true,
  // 可缩放
  zoomable: true,
  // 可旋转
  rotatable: true,

  // 初始索引
  initialViewIndex: 0,

  // 键盘导航
  keyboard: true,

  // 循环浏览
  loop: true,

  // 最小缩放比例
  minZoomRatio: 0.1,
  // 最大缩放比例
  maxZoomRatio: 5,

  // z-index
  zIndex: 9999
}

// 合并预览选项
const viewerOptions = computed(() => {
  return {
    ...defaultOptions,
    ...props.options
  }
})

// 打开预览
function open(index = 0) {
  nextTick(() => {
    if (viewerRef.value) {
      viewerRef.value.$viewer.show()
      // 设置初始索引
      viewerRef.value.$viewer.view(index)
    }
  })
}

// 初始化完成
function inited(viewer) {
  emit('inited', viewer)
}
</script>

<style lang="scss" scoped>
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

// 封面容器
.viewer-cover-container {
  position: relative;
  display: inline-block;
}

// 图片数量徽章
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

  &:hover {
    background-color: rgba(0, 0, 0, 0.75);
  }
}

// 覆盖层
.viewer-overlay {
  position: absolute;
  top: 0;
  left: 0;
  cursor: pointer;
  border-radius: 5px;
}

// 缩略图导航
.thumbnail-nav {
  margin-top: 8px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 5px;
}

.thumbnail-container {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 4px 0;
  scrollbar-width: thin;

  &::-webkit-scrollbar {
    height: 4px;
  }

  &::-webkit-scrollbar-track {
    background: #e0e0e0;
    border-radius: 2px;
  }

  &::-webkit-scrollbar-thumb {
    background: #b0b0b0;
    border-radius: 2px;

    &:hover {
      background: #999;
    }
  }
}

.thumbnail-item {
  flex-shrink: 0;
  width: v-bind('`${typeof thumbnailWidth === "number" ? thumbnailWidth + "px" : thumbnailWidth}`');
  height: v-bind('`${typeof thumbnailWidth === "number" ? thumbnailWidth + "px" : thumbnailWidth}`');
  border: 2px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: #409eff;
    transform: translateY(-2px);
  }

  &.active {
    border-color: #409eff;
    box-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
  }
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
</style>
