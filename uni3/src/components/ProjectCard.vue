<template>
  <view 
    class="project-card"
    :class="[cardTypeClass, { active: active }]"
    @click="$emit('click', project)"
  >
    <!-- 动画气泡装饰 -->
    <view class="bubble bubble-1"></view>
    <view class="bubble bubble-2"></view>
    <view class="bubble bubble-3"></view>
    
    <view class="card-content">
      <!-- 左侧头像 -->
      <view class="card-avatar">
        <UserAvatar 
          :avatar="userInfo?.avatar" 
          :name="userInfo?.name" 
          size="80rpx"
          bgColor="#ffffff"
          textColor="#C40016"
        />
      </view>
      
      <!-- 右侧信息 -->
      <view class="card-info">
        <!-- 第一行：名称 + 状态 -->
        <view class="card-row">
          <text class="card-name">{{ project.name }}</text>
          <view class="card-status" :class="cardTypeClass">
            {{ project.statusText }}
          </view>
        </view>
        
        <!-- 第二行：阶段 + 元数据 -->
        <view class="card-row card-meta-row">
          <text class="card-stage">{{ project.currentStageText || '设计阶段' }}</text>
          <view class="card-meta">
            <text v-if="project.area" class="meta-item">{{ project.area }}㎡</text>
            <text v-if="project.managerName" class="meta-item">{{ project.managerName }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import UserAvatar from '@/components/UserAvatar.vue'

const props = defineProps({
  /** 项目信息 */
  project: {
    type: Object,
    required: true
  },
  /** 是否激活状态 */
  active: {
    type: Boolean,
    default: false
  },
  /** 用户信息 */
  userInfo: {
    type: Object,
    default: () => ({})
  }
})

defineEmits(['click'])

// 根据状态获取卡片类型样式
const cardTypeClass = computed(() => {
  const status = props.project?.status?.toUpperCase()
  if (status === 'DESIGN') return 'design'
  if (status === 'COMPLETED') return 'completed'
  if (status === 'PENDING') return 'pending'
  return 'construction'
})
</script>

<style lang="scss" scoped>
.project-card {
  width: calc(100vw - 64rpx);
  min-width: calc(100vw - 64rpx);
  padding: $spacing-l $spacing-l;
  border-radius: $radius-xl;
  flex-shrink: 0;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  
  // 统一背景色 - 使用品牌红色渐变
  background: linear-gradient(135deg, $color-brand 0%, $color-brand-700 100%);
  border: none;
  
  // 统一样式 - 所有状态使用同一颜色
  .card-status { 
    background: $color-white-alpha-20; 
    color: $color-white;
    border: 1rpx solid $color-white-alpha-40;
  }
  
  &::before {
    background: $color-white-alpha-20;
  }
  
  .bubble {
    background: $color-white-alpha-10;
  }
  
  // 左侧装饰条
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 8rpx;
    border-radius: 0 $radius-s $radius-s 0;
  }
  
  // 激活状态
  &.active {
    transform: scale(1.02) translateY(-4rpx);
  }
  
  // 悬停效果（H5）
  &:hover {
    transform: translateY(-4rpx);
    
    .bubble {
      animation-play-state: running;
    }
  }
  
  &.active:hover {
    transform: scale(1.02) translateY(-6rpx);
  }
}

// 动画气泡
.bubble {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
  animation-play-state: paused;
}

.bubble-1 {
  width: 120rpx;
  height: 120rpx;
  top: -30rpx;
  right: 60rpx;
  animation: float1 4s ease-in-out infinite;
}

.bubble-2 {
  width: 80rpx;
  height: 80rpx;
  bottom: -20rpx;
  right: 160rpx;
  animation: float2 5s ease-in-out infinite;
  animation-delay: 1s;
}

.bubble-3 {
  width: 60rpx;
  height: 60rpx;
  top: 50%;
  right: 20rpx;
  transform: translateY(-50%);
  animation: float3 3.5s ease-in-out infinite;
  animation-delay: 0.5s;
}

@keyframes float1 {
  0%, 100% {
    transform: translate(0, 0) scale(1);
    opacity: 0.6;
  }
  50% {
    transform: translate(-10rpx, 15rpx) scale(1.1);
    opacity: 0.8;
  }
}

@keyframes float2 {
  0%, 100% {
    transform: translate(0, 0) scale(1);
    opacity: 0.5;
  }
  50% {
    transform: translate(15rpx, -10rpx) scale(1.15);
    opacity: 0.7;
  }
}

@keyframes float3 {
  0%, 100% {
    transform: translateY(-50%) scale(1);
    opacity: 0.4;
  }
  50% {
    transform: translateY(-50%) translate(-8rpx, 8rpx) scale(1.2);
    opacity: 0.6;
  }
}

.card-content {
  display: flex;
  align-items: center;
  gap: $spacing-m;
  position: relative;
  z-index: 1;
}

.card-avatar {
  flex-shrink: 0;
  
  // 头像容器样式 - 白色背景配合边框和阴影
  :deep(.user-avatar) {
    background: $color-white !important;
    border: 3rpx solid $color-white;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.15);
  }
}

.card-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.card-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  &:first-child {
    margin-bottom: $spacing-s;
  }
}

.card-name {
  font-size: 32rpx;
  font-weight: 600;
  color: $color-white;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: $spacing-s;
  line-height: 1.3;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}

.card-status {
  font-size: 22rpx;
  padding: 8rpx $spacing-s;
  border-radius: 100rpx;
  font-weight: 500;
  flex-shrink: 0;
  backdrop-filter: blur(10rpx);
  transition: all 0.3s ease;
}

.card-meta-row {
  .card-stage {
    font-size: 26rpx;
    color: $color-white-alpha-80;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-weight: 500;
  }
  
  .card-meta {
    display: flex;
    gap: $spacing-s;
    flex-shrink: 0;
    margin-left: $spacing-s;
  }
  
  .meta-item {
    font-size: 24rpx;
    color: $color-white-alpha-60;
    font-weight: 400;
    
    &:not(:last-child)::after {
      content: '·';
      margin-left: $spacing-s;
      color: $color-white-alpha-40;
    }
  }
}

// 响应式优化
@media (max-width: 750rpx) {
  .project-card {
    padding: $spacing-m $spacing-m;
    
    .card-name {
      font-size: 30rpx;
    }
    
    .card-stage {
      font-size: 24rpx;
    }
    
    .meta-item {
      font-size: 22rpx;
    }
  }
  
  .bubble-1 {
    width: 100rpx;
    height: 100rpx;
  }
  
  .bubble-2 {
    width: 60rpx;
    height: 60rpx;
  }
  
  .bubble-3 {
    width: 40rpx;
    height: 40rpx;
  }
}

</style>
