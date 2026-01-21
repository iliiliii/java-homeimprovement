<template>
  <!-- 根据配置选择不同的预览方式 -->
  <NativeImageViewer 
    v-if="useNative"
    :visible="visible"
    :images="images"
    :start-index="startIndex"
    :url-key="urlKey"
    :name-key="nameKey"
    :desc-key="descKey"
    @update:visible="$emit('update:visible', $event)"
    @close="$emit('close')"
    @change="$emit('change', $event)"
    @save="$emit('save', $event)"
  />
  
  <SimpleImageViewer 
    v-else
    :visible="visible"
    :images="images"
    :start-index="startIndex"
    :url-key="urlKey"
    :name-key="nameKey"
    :desc-key="descKey"
    :show-info="showInfo"
    :show-thumbnail="showThumbnail"
    :close-on-click="closeOnClick"
    @update:visible="$emit('update:visible', $event)"
    @close="$emit('close')"
    @change="$emit('change', $event)"
    @save="$emit('save', $event)"
  />
</template>

<script setup>
import { computed } from 'vue'
import NativeImageViewer from '@/components/NativeImageViewer/index.vue'
import SimpleImageViewer from '@/components/SimpleImageViewer/index.vue'
import { getImageViewerConfig } from '@/config/imageViewer.js'

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
    default: undefined // 使用 undefined 以便使用全局配置
  },
  // 是否显示缩略图导航
  showThumbnail: {
    type: Boolean,
    default: undefined // 使用 undefined 以便使用全局配置
  },
  // 点击图片关闭
  closeOnClick: {
    type: Boolean,
    default: undefined // 使用 undefined 以便使用全局配置
  },
  // 是否使用原生预览（可覆盖全局配置）
  useNativePreview: {
    type: Boolean,
    default: undefined // 使用 undefined 以便使用全局配置
  }
})

const emit = defineEmits(['update:visible', 'close', 'change', 'save'])

// 获取全局配置
const globalConfig = getImageViewerConfig()

// 计算是否使用原生预览（优先使用 props，其次使用全局配置）
const useNative = computed(() => {
  return props.useNativePreview !== undefined 
    ? props.useNativePreview 
    : globalConfig.useNativePreview
})

// 计算自定义预览配置（优先使用 props，其次使用全局配置）
const showInfo = computed(() => {
  return props.showInfo !== undefined 
    ? props.showInfo 
    : globalConfig.customPreview.showInfo
})

const showThumbnail = computed(() => {
  return props.showThumbnail !== undefined 
    ? props.showThumbnail 
    : globalConfig.customPreview.showThumbnail
})

const closeOnClick = computed(() => {
  return props.closeOnClick !== undefined 
    ? props.closeOnClick 
    : globalConfig.customPreview.closeOnClick
})

// 暴露方法给父组件
const goToImage = (index) => {
  // 这里需要通过ref来调用子组件的方法
  console.log('goToImage:', index)
}

const close = () => {
  emit('update:visible', false)
  emit('close')
}

const save = () => {
  console.log('save current image')
}

defineExpose({
  goToImage,
  close,
  save
})
</script>

<style lang="scss" scoped>
// 无需样式，由子组件处理
</style>