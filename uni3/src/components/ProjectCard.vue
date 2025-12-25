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
  padding: $spacing-l $spacing-l;
  border-radius: $radius-xl;
  flex-shrink: 0;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  box-shadow: $shadow-card;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  
  // 基础背景 - 使用品牌色系的渐变
  background: linear-gradient(145deg, $color-white 0%, $color-bg-secondary 100%);
  border: 2rpx solid $color-border-light;
  
  // 不同状态的主题色彩 - 黑红白主题
  &.design {
    background: linear-gradient(145deg, $color-white 0%, $color-gray-50 100%);
    border-color: $color-brand-200;
    
    .card-status { 
      background: $color-brand-50; 
      color: $color-brand-600;
      border: 1rpx solid $color-brand-200;
    }
    
    &::before {
      background: linear-gradient(45deg, $color-brand 0%, $color-brand-600 100%);
    }
  }
  
  &.construction {
    background: linear-gradient(145deg, $color-white 0%, $color-gray-50 100%);
    border-color: $color-gray-300;
    
    .card-status { 
      background: $color-gray-100; 
      color: $color-gray-700;
      border: 1rpx solid $color-gray-300;
    }
    
    &::before {
      background: linear-gradient(45deg, $color-gray-600 0%, $color-gray-700 100%);
    }
  }
  
  &.completed {
    background: linear-gradient(145deg, $color-white 0%, $color-gray-50 100%);
    border-color: $color-success;
    
    .card-status { 
      background: rgba(16, 185, 129, 0.1); 
      color: $color-success;
      border: 1rpx solid rgba(16, 185, 129, 0.2);
    }
    
    &::before {
      background: linear-gradient(45deg, $color-success 0%, #0D9488 100%);
    }
  }
  
  &.pending {
    background: linear-gradient(145deg, $color-white 0%, $color-gray-50 100%);
    border-color: $color-gray-300;
    
    .card-status { 
      background: $color-gray-100; 
      color: $color-gray-600;
      border: 1rpx solid $color-gray-300;
    }
    
    &::before {
      background: linear-gradient(45deg, $color-gray-400 0%, $color-gray-500 100%);
    }
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
    box-shadow: $shadow-glass;
    
    &.design {
      box-shadow: 0 12rpx 32rpx rgba(196, 0, 22, 0.15);
    }
    
    &.construction {
      box-shadow: 0 12rpx 32rpx rgba(167, 185, 211, 0.2);
    }
    
    &.completed {
      box-shadow: 0 12rpx 32rpx rgba(157, 193, 131, 0.2);
    }
    
    &.pending {
      box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.08);
    }
  }
  
  // 悬停效果（H5）
  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  }
  
  &.active:hover {
    transform: scale(1.02) translateY(-6rpx);
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
  
  // 为头像添加微妙的阴影
  :deep(.user-avatar) {
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
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
  color: $color-text-primary;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: $spacing-s;
  line-height: 1.3;
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
    color: $color-text-secondary;
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
    color: $color-text-tertiary;
    font-weight: 400;
    
    &:not(:last-child)::after {
      content: '·';
      margin-left: $spacing-s;
      color: $color-text-quaternary;
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
}

// 深色模式适配（预留）
@media (prefers-color-scheme: dark) {
  .project-card {
    background: linear-gradient(145deg, $color-gray-800 0%, $color-gray-700 100%);
    border-color: $color-gray-600;
    
    .card-name {
      color: $color-white;
    }
    
    .card-stage {
      color: $color-gray-300;
    }
    
    .meta-item {
      color: $color-gray-400;
    }
    
    &.design {
      background: linear-gradient(145deg, rgba(196, 0, 22, 0.1) 0%, rgba(196, 0, 22, 0.05) 100%);
      
      .card-status {
        background: rgba(196, 0, 22, 0.2);
        color: $color-brand-300;
      }
    }
    
    &.construction {
      background: linear-gradient(145deg, rgba(167, 185, 211, 0.1) 0%, rgba(167, 185, 211, 0.05) 100%);
      
      .card-status {
        background: rgba(167, 185, 211, 0.2);
        color: #A7B9D3;
      }
    }
    
    &.completed {
      background: linear-gradient(145deg, rgba(157, 193, 131, 0.1) 0%, rgba(157, 193, 131, 0.05) 100%);
      
      .card-status {
        background: rgba(157, 193, 131, 0.2);
        color: #10B981;
      }
    }
  }
}
</style>
