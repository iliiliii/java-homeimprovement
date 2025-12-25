<template>
  <view 
    class="project-card"
    :class="[cardTypeClass, { active: active }]"
    @click="$emit('click', project)"
  >
    <view class="card-content">
      <!-- 左侧头像 -->
      <view class="card-avatar">
        <UserAvatar 
          :avatar="userInfo?.avatar" 
          :name="userInfo?.name" 
          size="80rpx"
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
  padding: 28rpx 32rpx;
  border-radius: 24rpx;
  flex-shrink: 0;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  
  &.design {
    background: linear-gradient(145deg, #F3E8FF 0%, #E9D5FF 100%);
    .card-status { background: rgba(168, 85, 247, 0.15); color: #7C3AED; }
  }
  
  &.construction {
    background: linear-gradient(145deg, #DBEAFE 0%, #BFDBFE 100%);
    .card-status { background: rgba(59, 130, 246, 0.15); color: #2563EB; }
  }
  
  &.completed {
    background: linear-gradient(145deg, #D1FAE5 0%, #A7F3D0 100%);
    .card-status { background: rgba(16, 185, 129, 0.15); color: #059669; }
  }
  
  &.pending {
    background: linear-gradient(145deg, #F3F4F6 0%, #E5E7EB 100%);
    .card-status { background: rgba(107, 114, 128, 0.15); color: #6B7280; }
  }
  
  &.active {
    transform: scale(1.01);
    box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.1);
  }
}

.card-content {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.card-avatar {
  flex-shrink: 0;
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
    margin-bottom: 16rpx;
  }
}

.card-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 16rpx;
}

.card-status {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 100rpx;
  font-weight: 500;
  flex-shrink: 0;
}

.card-meta-row {
  .card-stage {
    font-size: 26rpx;
    color: #4B5563;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .card-meta {
    display: flex;
    gap: 16rpx;
    flex-shrink: 0;
    margin-left: 16rpx;
  }
  
  .meta-item {
    font-size: 24rpx;
    color: #6B7280;
  }
}
</style>
