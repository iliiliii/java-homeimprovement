<template>
  <view class="record-detail">
    <!-- 头部信息 -->
    <view class="detail-header">
      <text class="detail-title">{{ record.title }}</text>
      <view class="detail-meta">
        <text class="detail-stage">{{ record.stageName }}</text>
        <text class="detail-type" :class="`type-${record.type.toLowerCase()}`">
          {{ record.typeText }}
        </text>
        <text v-if="record.inspectionStatus" 
              class="detail-status" 
              :class="`status-${record.inspectionStatus.toLowerCase()}`">
          {{ record.inspectionStatusText }}
        </text>
      </view>
    </view>
    
    <!-- 内容描述 -->
    <view v-if="record.description" class="detail-content">
      <text class="content-title">详细描述</text>
      <text class="content-text">{{ record.description }}</text>
    </view>
    
    <!-- 创建人信息 -->
    <view class="detail-creator">
      <text class="creator-title">记录人</text>
      <view class="creator-info">
        <text class="creator-name">{{ record.createByName }}</text>
        <text class="creator-role" :class="`role-${record.createByRole.toLowerCase()}`">
          {{ getRoleText(record.createByRole) }}
        </text>
        <text class="creator-time">{{ formatTime(record.createTime) }}</text>
      </view>
    </view>
    
    <!-- 图片列表 -->
    <view v-if="record.images && record.images.length > 0" class="detail-images">
      <text class="images-title">相关图片 ({{ record.images.length }})</text>
      <view class="images-grid">
        <image 
          v-for="(img, index) in record.images"
          :key="index"
          class="detail-image"
          :src="getFullImageUrl(img)"
          mode="aspectFill"
          @click="previewImage(record.images, index)"
        />
      </view>
    </view>
    
    <!-- 附件列表 -->
    <view v-if="record.attachments && record.attachments.length > 0" class="detail-attachments">
      <text class="attachments-title">相关附件 ({{ record.attachments.length }})</text>
      <view class="attachments-list">
        <view 
          v-for="(attachment, index) in record.attachments"
          :key="index"
          class="attachment-item"
          @click="downloadAttachment(attachment)"
        >
          <view class="attachment-icon">
            <text>📄</text>
          </view>
          <view class="attachment-info">
            <text class="attachment-name">{{ attachment.fileName }}</text>
            <text class="attachment-size">{{ formatFileSize(attachment.fileSize) }}</text>
          </view>
          <view class="attachment-action">
            <text>下载</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 备注 -->
    <view v-if="record.remark" class="detail-remark">
      <text class="remark-title">备注</text>
      <text class="remark-text">{{ record.remark }}</text>
    </view>
  </view>
</template>

<script setup>
import { getFullImageUrl } from '@/utils/request'

const props = defineProps({
  record: {
    type: Object,
    required: true
  }
})

// 预览图片
const previewImage = (images, index) => {
  const urls = images.map(img => getFullImageUrl(img))
  uni.previewImage({
    urls: urls,
    current: index
  })
}

// 下载附件
const downloadAttachment = (attachment) => {
  uni.showLoading({ title: '下载中...' })
  
  uni.downloadFile({
    url: getFullImageUrl(attachment.fileUrl),
    success: (res) => {
      uni.hideLoading()
      if (res.statusCode === 200) {
        uni.showToast({
          title: '下载成功',
          icon: 'success'
        })
        // 可以在这里处理下载后的文件
      }
    },
    fail: () => {
      uni.hideLoading()
      uni.showToast({
        title: '下载失败',
        icon: 'none'
      })
    }
  })
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
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

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return ''
  if (size < 1024) return size + 'B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + 'KB'
  return (size / (1024 * 1024)).toFixed(1) + 'MB'
}
</script>

<style lang="scss" scoped>
.record-detail {
  padding: 32rpx;
}

.detail-header {
  margin-bottom: 32rpx;
}

.detail-title {
  font-size: 36rpx;
  font-weight: 600;
  color: $glass-text-main;
  display: block;
  margin-bottom: 16rpx;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-wrap: wrap;
}

.detail-stage {
  font-size: 26rpx;
  color: $glass-text-muted;
  background: $color-gray-100;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.detail-type {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
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

.detail-status {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  
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

.detail-content,
.detail-creator,
.detail-images,
.detail-attachments,
.detail-remark {
  margin-bottom: 32rpx;
  padding: 24rpx;
  background: $color-gray-50;
  border-radius: 16rpx;
}

.content-title,
.creator-title,
.images-title,
.attachments-title,
.remark-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $glass-text-main;
  display: block;
  margin-bottom: 16rpx;
}

.content-text,
.remark-text {
  font-size: 28rpx;
  color: $glass-text-muted;
  line-height: 1.6;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-wrap: wrap;
}

.creator-name {
  font-size: 28rpx;
  color: $glass-text-main;
  font-weight: 500;
}

.creator-role {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  
  &.role-staff {
    background: rgba(13, 110, 253, 0.1);
    color: #0d6efd;
  }
  
  &.role-customer {
    background: rgba(111, 66, 193, 0.1);
    color: #6f42c1;
  }
}

.creator-time {
  font-size: 24rpx;
  color: $glass-text-muted;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.detail-image {
  width: 100%;
  height: 200rpx;
  border-radius: 12rpx;
  background: $color-gray-200;
}

.attachments-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx;
  background: white;
  border-radius: 12rpx;
  
  &:active {
    background: $color-gray-100;
  }
}

.attachment-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}

.attachment-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.attachment-name {
  font-size: 28rpx;
  color: $glass-text-main;
}

.attachment-size {
  font-size: 24rpx;
  color: $glass-text-muted;
}

.attachment-action {
  font-size: 26rpx;
  color: $color-brand;
}
</style>