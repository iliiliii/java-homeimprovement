<template>
  <view 
    class="project-card"
    :class="[cardTypeClass, { active: active }]"
    @click="$emit('click', project)"
  >
    <view class="card-header">
      <text class="card-name">{{ project.name }}</text>
      <view class="card-status" :class="cardTypeClass">
        {{ project.statusText }}
      </view>
    </view>
    <view class="card-stage">
      <text>当前阶段: {{ project.currentStageText || '设计阶段' }}</text>
    </view>
    <view class="card-progress">
      <view class="progress-bar">
        <view class="progress-fill" :style="{ width: (project.progressPercent || 0) + '%' }"></view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

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
  width: 600rpx;
  min-width: 600rpx;
  padding: 32rpx;
  border-radius: 32rpx;
  flex-shrink: 0;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  
  &.design {
    background: linear-gradient(145deg, #F3E8FF 0%, #E9D5FF 100%);
    .progress-fill { background: linear-gradient(90deg, #A855F7 0%, #7C3AED 100%); }
    .card-status { background: rgba(168, 85, 247, 0.15); color: #7C3AED; }
  }
  
  &.construction {
    background: linear-gradient(145deg, #DBEAFE 0%, #BFDBFE 100%);
    .progress-fill { background: linear-gradient(90deg, #3B82F6 0%, #2563EB 100%); }
    .card-status { background: rgba(59, 130, 246, 0.15); color: #2563EB; }
  }
  
  &.completed {
    background: linear-gradient(145deg, #D1FAE5 0%, #A7F3D0 100%);
    .progress-fill { background: linear-gradient(90deg, #10B981 0%, #059669 100%); }
    .card-status { background: rgba(16, 185, 129, 0.15); color: #059669; }
  }
  
  &.pending {
    background: linear-gradient(145deg, #F3F4F6 0%, #E5E7EB 100%);
    .progress-fill { background: linear-gradient(90deg, #9CA3AF 0%, #6B7280 100%); }
    .card-status { background: rgba(107, 114, 128, 0.15); color: #6B7280; }
  }
  
  &.active {
    transform: scale(1.02);
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.card-name {
  font-size: 34rpx;
  font-weight: 600;
  color: #1F2937;
  flex: 1;
  white-space: normal;
  word-break: break-all;
}

.card-status {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 100rpx;
  font-weight: 500;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.card-stage {
  font-size: 28rpx;
  color: #4B5563;
  margin-bottom: 24rpx;
  text { opacity: 0.9; }
}

.card-progress {
  .progress-bar {
    height: 16rpx;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 8rpx;
    overflow: hidden;
  }
  
  .progress-fill {
    height: 100%;
    border-radius: 8rpx;
    transition: width 0.3s ease;
  }
}
</style>
