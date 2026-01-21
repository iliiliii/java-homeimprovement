<template>
  <!-- 这个组件作为原生 API 的包装器，保持页面结构一致 -->
  <view></view>
</template>

<script setup>
import { watch } from 'vue'

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
  }
})

const emit = defineEmits(['update:visible', 'close', 'change', 'save'])

// 获取图片URL
const getImageUrl = (img) => {
  if (typeof img === 'string') return img
  return img[props.urlKey] || ''
}

// 监听visible变化，调用原生API
watch(() => props.visible, (val) => {
  if (val && props.images.length > 0) {
    // 转换图片URL列表
    const imageUrls = props.images.map(img => getImageUrl(img))
    
    // 调用原生预览API
    uni.previewImage({
      current: props.startIndex,
      urls: imageUrls,
      longPressActions: {
        itemList: ['保存图片'],
        success: function (data) {
          if (data.tapIndex === 0) {
            // 触发保存事件
            emit('save', { 
              index: props.startIndex, 
              url: imageUrls[props.startIndex] 
            })
          }
        }
      },
      success: () => {
        // 预览成功
        emit('change', { index: props.startIndex, image: props.images[props.startIndex] })
      },
      fail: () => {
        uni.showToast({
          title: '预览失败',
          icon: 'none'
        })
      },
      complete: () => {
        // 预览关闭时触发
        emit('update:visible', false)
        emit('close')
      }
    })
  }
})

// 暴露方法保持API一致性
const goToImage = (index) => {
  // 原生API不支持程序化跳转，但保持接口一致
  console.log('goToImage:', index)
}

const close = () => {
  emit('update:visible', false)
  emit('close')
}

const save = () => {
  // 保存当前图片
  const currentUrl = getImageUrl(props.images[props.startIndex])
  emit('save', { index: props.startIndex, url: currentUrl })
}

defineExpose({
  goToImage,
  close,
  save
})
</script>

<style lang="scss" scoped>
// 空样式，因为使用原生API
</style>