<template>
  <view class="user-avatar" :style="avatarStyle" @click="$emit('click')">
    <image 
      v-if="avatar" 
      class="avatar-image" 
      :src="avatar" 
      mode="aspectFill"
    />
    <text v-else class="avatar-text" :style="textStyle">{{ initial }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** 头像URL */
  avatar: {
    type: String,
    default: ''
  },
  /** 用户姓名（用于生成首字） */
  name: {
    type: String,
    default: ''
  },
  /** 头像尺寸 */
  size: {
    type: String,
    default: '80rpx'
  },
  /** 背景色（无头像时） */
  bgColor: {
    type: String,
    default: '#C9B0D4'
  },
  /** 文字颜色 */
  textColor: {
    type: String,
    default: '#ffffff'
  }
})

defineEmits(['click'])

// 计算首字
const initial = computed(() => {
  if (!props.name) return '?'
  return props.name.charAt(0).toUpperCase()
})

// 头像容器样式
const avatarStyle = computed(() => ({
  width: props.size,
  height: props.size,
  backgroundColor: props.avatar ? 'transparent' : props.bgColor
}))

// 文字样式
const textStyle = computed(() => {
  // 根据尺寸计算字体大小（约为容器的50%）
  const sizeNum = parseInt(props.size)
  const fontSize = Math.floor(sizeNum * 0.5) + 'rpx'
  return {
    fontSize,
    color: props.textColor
  }
})
</script>

<style lang="scss" scoped>
.user-avatar {
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-image {
  width: 100%;
  height: 100%;
}

.avatar-text {
  font-weight: 600;
  text-align: center;
}
</style>
