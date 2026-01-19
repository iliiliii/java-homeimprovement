<template>
  <view class="binding-guide-overlay" v-if="visible" @click="handleOverlayClick">
    <view class="binding-guide-modal" @click.stop>
      
      <!-- 头部 -->
      <view class="guide-header">
        <text class="guide-title">微信账号绑定</text>
        <text class="guide-subtitle">将您的微信账号与系统账户关联</text>
      </view>
      
      <!-- 说明内容 -->
      <view class="guide-content">
        <view class="benefit-list">
          <view class="benefit-item">
            <view class="benefit-number">1</view>
            <view class="benefit-text">
              <text class="benefit-title">快速登录</text>
              <text class="benefit-desc">下次直接微信登录，无需输入密码</text>
            </view>
          </view>
          
          <view class="benefit-item">
            <view class="benefit-number">2</view>
            <view class="benefit-text">
              <text class="benefit-title">安全保障</text>
              <text class="benefit-desc">微信官方认证，保护您的账户安全</text>
            </view>
          </view>
          
          <view class="benefit-item">
            <view class="benefit-number">3</view>
            <view class="benefit-text">
              <text class="benefit-title">身份验证</text>
              <text class="benefit-desc">仅限系统中已注册的手机号可绑定</text>
            </view>
          </view>
        </view>
        
        <!-- 重要提示 -->
        <view class="important-notice">
          <view class="notice-header">
            <text class="notice-title">重要提示</text>
          </view>
          <text class="notice-text">请确保输入的手机号已在系统中注册，如未注册请联系管理员添加</text>
        </view>
      </view>
      
      <!-- 操作按钮 -->
      <view class="guide-actions">
        <button class="guide-btn cancel-btn" @click="handleCancel">
          <text>稍后绑定</text>
        </button>
        <button class="guide-btn confirm-btn" @click="handleConfirm">
          <text>开始绑定</text>
        </button>
      </view>
      
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['confirm', 'cancel', 'close'])

const handleOverlayClick = () => {
  emit('close')
}

const handleConfirm = () => {
  emit('confirm')
}

const handleCancel = () => {
  emit('cancel')
}
</script>

<style lang="scss" scoped>
.binding-guide-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8rpx);
  z-index: 9999;
  @include flex(column);
  align-items: center;
  justify-content: center;
  padding: 60rpx 40rpx;
}

.binding-guide-modal {
  background: $color-white;
  border-radius: 24rpx;
  width: 100%;
  max-width: 640rpx;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(60rpx) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.guide-header {
  text-align: center;
  padding: 60rpx 40rpx 40rpx;
  background: linear-gradient(135deg, #F8F9FA 0%, #F1F5F9 100%);
  border-bottom: 1rpx solid $u-border-color;
}

.header-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $u-primary 0%, $u-primary-dark 100%);
  @include flex(row);
  align-items: center;
  justify-content: center;
  margin: 0 auto 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(196, 0, 22, 0.3);
}

.wechat-logo {
  font-size: 32rpx;
  font-weight: 600;
  color: $color-white;
}

.guide-title {
  display: block;
  font-size: $u-font-size-xl;
  font-weight: 600;
  color: $u-main-color;
  margin-bottom: 12rpx;
}

.guide-subtitle {
  display: block;
  font-size: $u-font-size-sm;
  color: $u-content-color;
  line-height: 1.5;
}

.guide-content {
  padding: 40rpx;
}

.benefit-list {
  margin-bottom: 32rpx;
}

.benefit-item {
  @include flex(row);
  align-items: flex-start;
  margin-bottom: 32rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.benefit-number {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: $u-primary;
  color: $color-white;
  font-size: $u-font-size;
  font-weight: 600;
  @include flex(row);
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.benefit-text {
  flex: 1;
  padding-top: 4rpx;
}

.benefit-title {
  display: block;
  font-size: $u-font-size-lg;
  font-weight: 600;
  color: $u-main-color;
  margin-bottom: 8rpx;
}

.benefit-desc {
  display: block;
  font-size: $u-font-size-sm;
  color: $u-content-color;
  line-height: 1.4;
}

.important-notice {
  padding: 24rpx;
  background: #FFF7ED;
  border-radius: $u-border-radius;
  border-left: 6rpx solid $u-warning;
}

.notice-header {
  margin-bottom: 12rpx;
}

.notice-title {
  font-size: $u-font-size;
  font-weight: 600;
  color: $u-warning-dark;
}

.notice-text {
  font-size: $u-font-size-sm;
  color: #92400E;
  line-height: 1.5;
}

.guide-actions {
  @include flex(row);
  padding: 32rpx 40rpx 40rpx;
  gap: 24rpx;
  background: #FAFAFA;
}

.guide-btn {
  flex: 1;
  height: 88rpx;
  border-radius: $u-border-radius-lg;
  font-size: $u-font-size-lg;
  font-weight: 500;
  @include flex(row);
  align-items: center;
  justify-content: center;
  border: none;
  transition: all 0.2s ease;
  
  &.cancel-btn {
    background: $color-white;
    color: $u-content-color;
    border: 2rpx solid $u-border-color;
    
    &:active {
      background: #F5F5F5;
      transform: scale(0.98);
    }
  }
  
  &.confirm-btn {
    background: linear-gradient(135deg, $u-primary 0%, $u-primary-dark 100%);
    color: $color-white;
    box-shadow: 0 8rpx 24rpx rgba(196, 0, 22, 0.3);
    
    &:active {
      transform: scale(0.98);
      box-shadow: 0 4rpx 12rpx rgba(196, 0, 22, 0.4);
    }
  }
}
</style>