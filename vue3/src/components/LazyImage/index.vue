<template>
  <div class="lazy-image-container" :style="containerStyle">
    <!-- 加载占位符 -->
    <div v-if="!imageLoaded && !imageError" class="image-placeholder" :style="placeholderStyle">
      <el-icon class="placeholder-icon"><Picture /></el-icon>
      <span class="placeholder-text">{{ placeholderText }}</span>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="imageError" class="image-error" :style="placeholderStyle">
      <el-icon class="error-icon"><PictureFilled /></el-icon>
      <span class="error-text">{{ errorText }}</span>
      <el-button v-if="showRetry" type="text" size="small" @click="retryLoad">
        重试
      </el-button>
    </div>

    <!-- 实际图片 -->
    <img
      v-show="imageLoaded"
      ref="imageRef"
      :src="currentSrc"
      :alt="alt"
      :style="imageStyle"
      class="lazy-image"
      @load="handleImageLoad"
      @error="handleImageError"
    />

    <!-- 渐进式加载过渡层 -->
    <div v-if="useProgressive && imageLoaded" class="image-overlay" :style="overlayStyle">
      <div class="progressive-blur" :style="blurStyle"></div>
    </div>
  </div>
</template>

<script setup>
import { Picture, PictureFilled } from '@element-plus/icons-vue'

const props = defineProps({
  // 图片URL
  src: {
    type: String,
    required: true
  },
  // 备选图片URL（当主图片加载失败时使用）
  fallbackSrc: {
    type: String,
    default: ''
  },
  // 图片宽度
  width: {
    type: [String, Number],
    default: '100%'
  },
  // 图片高度
  height: {
    type: [String, Number],
    default: 'auto'
  },
  // 对象适配方式
  objectFit: {
    type: String,
    default: 'cover',
    validator: (value) => ['fill', 'contain', 'cover', 'none', 'scale-down'].includes(value)
  },
  // 圆角
  borderRadius: {
    type: [String, Number],
    default: '4px'
  },
  // 占位符文字
  placeholderText: {
    type: String,
    default: '图片加载中...'
  },
  // 错误文字
  errorText: {
    type: String,
    default: '图片加载失败'
  },
  // 是否显示重试按钮
  showRetry: {
    type: Boolean,
    default: true
  },
  // 是否使用渐进式加载
  useProgressive: {
    type: Boolean,
    default: true
  },
  // 懒加载配置
  lazy: {
    type: Boolean,
    default: true
  },
  // 根边距（Intersection Observer rootMargin）
  rootMargin: {
    type: String,
    default: '50px'
  },
  // 交叉阈值
  threshold: {
    type: Number,
    default: 0.1
  },
  // 是否启用缓存
  enableCache: {
    type: Boolean,
    default: true
  },
  // 图片加载超时时间（毫秒）
  timeout: {
    type: Number,
    default: 10000
  }
})

const emit = defineEmits(['load', 'error', 'intersect', 'retry'])

const imageRef = ref(null)
const isVisible = ref(false)
const imageLoaded = ref(false)
const imageError = ref(false)
const currentSrc = ref('')
const loadingTimer = ref(null)

// Intersection Observer
let observer = null

// 图片缓存
const imageCache = new Map()

// 计算样式
const containerStyle = computed(() => ({
  width: typeof props.width === 'number' ? `${props.width}px` : props.width,
  height: typeof props.height === 'number' ? `${props.height}px` : props.height,
  borderRadius: typeof props.borderRadius === 'number' ? `${props.borderRadius}px` : props.borderRadius,
  overflow: 'hidden',
  position: 'relative'
}))

const placeholderStyle = computed(() => ({
  width: '100%',
  height: '100%',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  backgroundColor: '#f5f7fa',
  color: '#909399',
  fontSize: '14px'
}))

const imageStyle = computed(() => ({
  width: '100%',
  height: '100%',
  objectFit: props.objectFit,
  transition: 'opacity 0.3s ease-in-out'
}))

const overlayStyle = computed(() => ({
  position: 'absolute',
  top: 0,
  left: 0,
  right: 0,
  bottom: 0,
  opacity: 0,
  transition: 'opacity 0.3s ease-out',
  pointerEvents: 'none'
}))

const blurStyle = computed(() => ({
  width: '100%',
  height: '100%',
  backgroundImage: currentSrc.value ? `url(${currentSrc.value})` : 'none',
  backgroundSize: 'cover',
  backgroundPosition: 'center',
  filter: 'blur(20px)',
  transform: 'scale(1.1)'
}))

// 设置Intersection Observer
function setupObserver() {
  if (!props.lazy || !imageRef.value) {
    isVisible.value = true
    loadImage()
    return
  }

  observer = new IntersectionObserver(
    (entries) => {
      const entry = entries[0]
      if (entry.isIntersecting) {
        isVisible.value = true
        emit('intersect', entry)
        loadImage()
        observer.unobserve(imageRef.value)
        observer.disconnect()
        observer = null
      }
    },
    {
      rootMargin: props.rootMargin,
      threshold: props.threshold
    }
  )

  observer.observe(imageRef.value)
}

// 加载图片
function loadImage() {
  if (!props.src || imageLoaded.value) return

  // 检查缓存
  if (props.enableCache && imageCache.has(props.src)) {
    const cachedResult = imageCache.get(props.src)
    if (cachedResult.success) {
      currentSrc.value = props.src
      imageLoaded.value = true
      emit('load', props.src)
      return
    } else if (props.fallbackSrc && !cachedResult.fallbackTried) {
      loadImageWithFallback()
      return
    }
  }

  currentSrc.value = props.src
  setupTimeout()
}

function loadImageWithFallback() {
  if (!props.fallbackSrc) {
    markImageError()
    return
  }

  console.log('主图片加载失败，尝试加载备选图片:', props.fallbackSrc)
  currentSrc.value = props.fallbackSrc
  setupTimeout()
}

function setupTimeout() {
  if (loadingTimer.value) {
    clearTimeout(loadingTimer.value)
  }

  loadingTimer.value = setTimeout(() => {
    if (!imageLoaded.value && !imageError.value) {
      console.warn('图片加载超时:', currentSrc.value)
      if (currentSrc.value === props.src && props.fallbackSrc) {
        loadImageWithFallback()
      } else {
        markImageError()
      }
    }
  }, props.timeout)
}

// 图片加载成功处理
function handleImageLoad(event) {
  if (loadingTimer.value) {
    clearTimeout(loadingTimer.value)
    loadingTimer.value = null
  }

  imageLoaded.value = true
  imageError.value = false

  // 缓存成功结果
  if (props.enableCache) {
    imageCache.set(props.src, { success: true, timestamp: Date.now() })
  }

  emit('load', {
    src: currentSrc.value,
    width: event.target?.naturalWidth,
    height: event.target?.naturalHeight
  })
}

// 图片加载失败处理
function handleImageError(error) {
  if (loadingTimer.value) {
    clearTimeout(loadingTimer.value)
    loadingTimer.value = null
  }

  // 如果是主图片失败且存在备选图片，尝试备选图片
  if (currentSrc.value === props.src && props.fallbackSrc && !imageCache.has(props.src)) {
    loadImageWithFallback()
    return
  }

  markImageError()
}

function markImageError() {
  imageError.value = true
  imageLoaded.value = false

  // 缓存失败结果
  if (props.enableCache) {
    imageCache.set(props.src, {
      success: false,
      fallbackTried: currentSrc.value !== props.src,
      timestamp: Date.now()
    })
  }

  emit('error', {
    src: currentSrc.value,
    fallbackTried: currentSrc.value !== props.src
  })
}

// 重试加载
function retryLoad() {
  imageError.value = false
  imageLoaded.value = false
  currentSrc.value = ''
  emit('retry')
  loadImage()
}

// 清理缓存
function clearCache() {
  imageCache.clear()
}

// 清理过期缓存（超过24小时）
function cleanupExpiredCache() {
  const now = Date.now()
  const expiredTime = 24 * 60 * 60 * 1000 // 24小时

  for (const [src, data] of imageCache.entries()) {
    if (now - data.timestamp > expiredTime) {
      imageCache.delete(src)
    }
  }
}

// 生命周期
onMounted(() => {
  nextTick(() => {
    setupObserver()
  })

  // 定期清理过期缓存
  const cleanupInterval = setInterval(cleanupExpiredCache, 60 * 60 * 1000) // 每小时清理一次

  onUnmounted(() => {
    if (observer) {
      observer.disconnect()
      observer = null
    }
    if (loadingTimer.value) {
      clearTimeout(loadingTimer.value)
    }
    clearInterval(cleanupInterval)
  })
})

// 监听src变化
watch(() => props.src, (newSrc) => {
  if (newSrc && isVisible.value) {
    imageLoaded.value = false
    imageError.value = false
    loadImage()
  }
})

// 暴露方法
defineExpose({
  loadImage,
  retryLoad,
  clearCache,
  cleanupExpiredCache,
  isLoaded: () => imageLoaded.value,
  hasError: () => imageError.value,
  isVisible: () => isVisible.value
})
</script>

<style scoped lang="scss">
.lazy-image-container {
  .image-placeholder,
  .image-error {
    .placeholder-icon,
    .error-icon {
      font-size: 32px;
      margin-bottom: 8px;
    }

    .placeholder-text,
    .error-text {
      font-size: 12px;
      text-align: center;
      line-height: 1.4;
    }
  }

  .image-error {
    .error-icon {
      color: #f56c6c;
    }

    .error-text {
      color: #f56c6c;
      margin-bottom: 8px;
    }
  }

  .lazy-image {
    opacity: 0;

    &[v-show="true"] {
      opacity: 1;
    }
  }

  .image-overlay {
    background-color: rgba(255, 255, 255, 0.1);

    .progressive-blur {
      width: 100%;
      height: 100%;
      transition: opacity 0.3s ease-out;
    }
  }
}
</style>