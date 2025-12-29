<template>
  <view class="schedule-record" @click="handleClick">
    <!-- 记录头部 -->
    <view class="record-header">
      <view class="record-title-section">
        <text class="record-title">{{ record.title }}</text>
        <view class="record-meta">
          <text class="record-type" :class="`type-${record.type.toLowerCase()}`">
            {{ record.typeText }}
          </text>
          <text class="record-time">{{ formatTime(record.createTime) }}</text>
        </view>
      </view>
      
      <!-- 验收状态 -->
      <view v-if="record.inspectionStatus" class="inspection-status">
        <text 
          class="status-badge" 
          :class="`status-${record.inspectionStatus.toLowerCase()}`"
        >
          {{ record.inspectionStatusText }}
        </text>
      </view>
    </view>
    
    <!-- 记录内容 -->
    <view v-if="record.description" class="record-content">
      <text>{{ record.description }}</text>
    </view>
    
    <!-- 创建人信息 -->
    <view class="record-creator">
      <text class="creator-name">{{ record.createByName }}</text>
      <text class="creator-role" :class="`role-${record.createByRole.toLowerCase()}`">
        {{ getRoleText(record.createByRole) }}
      </text>
    </view>
    
    <!-- 图片列表 -->
    <view v-if="record.images && record.images.length > 0" class="record-images">
      <image 
        v-for="(img, index) in displayImages"
        :key="index"
        class="record-image"
        :src="getFullImageUrl(img)"
        mode="aspectFill"
        @click.stop="previewImage(record.images, index)"
      />
      <view v-if="record.images.length > 3" class="image-more">
        +{{ record.images.length - 3 }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { getFullImageUrl } from '@/utils/request'

const props = defineProps({
  record: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

// 显示的图片（最多3张）
const displayImages = computed(() => {
  return props.record.images ? props.record.images.slice(0, 3) : []
})

// 处理点击事件
const handleClick = () => {
  emit('click', props.record)
}

// 预览图片
const previewImage = (images, index) => {
  const urls = images.map(img => getFullImageUrl(img))
  uni.previewImage({
    urls: urls,
    current: index
  })
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  const recordDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  
  if (recordDate.getTime() === today.getTime()) {
    return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  } else if (recordDate.getTime() === yesterday.getTime()) {
    return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  } else {
    return `${date.getMonth() + 1}.${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
}

// 获取角色文本
const getRoleText = (role) => {
  switch (role) {
    case 'STAFF':
      return '施工方'
    case 'CUSTOMER':
      return '业主'
    default:
      return role
  }
}
</script>

<style lang="scss" scoped>
.schedule-record {
  background: $color-gray-50;
  border-radius: 16rpx;
  padding: 20rpx;
  border-left: 6rpx solid transparent;
  transition: all 0.3s ease;
  
  &:active {
    background: $color-gray-100;
  }
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12rpx;
}

.record-title-section {
  flex: 1;
}

.record-title {
  font-size: 28rpx;
  font-weight: 500;
  color: $glass-text-main;
  display: block;
  margin-bottom: 8rpx;
}

.record-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.record-type {
  font-size: 22rpx;
  padding: 2rpx 12rpx;
  border-radius: 8rpx;
  
  &.type-inspection {
    background: rgba(196, 0, 22, 0.1);
    color: $color-brand;
  }
  
  &.type-progress {
    background: rgba(33, 33, 33, 0.1);
    color: $color-gray-800;
  }
  
  &.type-material {
    background: rgba(117, 117, 117, 0.1);
    color: $color-gray-600;
  }
  
  &.type-issue {
    background: rgba(220, 53, 69, 0.1);
    color: #dc3545;
  }
}

.record-time {
  font-size: 22rpx;
  color: $glass-text-muted;
}

.inspection-status {
  flex-shrink: 0;
}

.status-badge {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  
  &.status-pass {
    background: rgba(40, 167, 69, 0.1);
    color: #28a745;
  }
  
  &.status-fail {
    background: rgba(220, 53, 69, 0.1);
    color: #dc3545;
  }
  
  &.status-pending {
    background: rgba(255, 193, 7, 0.1);
    color: $color-warning;
  }
}

.record-content {
  margin-bottom: 12rpx;
  
  text {
    font-size: 26rpx;
    color: $glass-text-muted;
    line-height: 1.5;
  }
}

.record-creator {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.creator-name {
  font-size: 24rpx;
  color: $glass-text-main;
  font-weight: 500;
}

.creator-role {
  font-size: 22rpx;
  padding: 2rpx 8rpx;
  border-radius: 6rpx;
  
  &.role-staff {
    background: rgba(13, 110, 253, 0.1);
    color: #0d6efd;
  }
  
  &.role-customer {
    background: rgba(111, 66, 193, 0.1);
    color: #6f42c1;
  }
}

.record-images {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.record-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  background: $color-gray-200;
}

.image-more {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  background: $color-gray-200;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: $glass-text-muted;
}
</style>