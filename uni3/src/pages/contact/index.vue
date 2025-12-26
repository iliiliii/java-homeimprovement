<template>
  <view class="contact-page">
    <!-- 导航栏 -->
    <NavBar 
      title="联系客服" 
      background="transparent" 
      titleColor="#fff" 
      iconColor="#fff"
      :showBorder="false"
    />
    
    <!-- 头部区域 -->
    <view class="header-section">
      <!-- <view class="header-bubble bubble-1"></view>
      <view class="header-bubble bubble-2"></view>
      
      <view class="header-content">
        <view class="header-icon">
          <text class="icon-emoji">💬</text>
        </view>
        <text class="header-title">专属客服</text>
        <text class="header-subtitle">7×24小时为您服务</text>
      </view> -->
    </view>
    
    <!-- 二维码区域 -->
    <view class="qrcode-section">
      <view class="qrcode-card">
        <view class="card-accent"></view>
        <text class="qrcode-title">扫码添加客服微信</text>
        <view class="qrcode-wrapper">
          <image 
            class="qrcode-image" 
            :src="contactInfo.qrCode" 
            mode="aspectFit"
            @click="previewQrCode"
          />
        </view>
        <text class="qrcode-tip">长按识别或点击放大</text>
      </view>
    </view>
    
    <!-- 联系方式 -->
    <view class="contact-section">
      <text class="section-title">其他联系方式</text>
      
      <view class="contact-card" @click="handleCall">
        <view class="card-accent"></view>
        <!-- <view class="contact-icon phone-icon">
          <text></text>
        </view> -->
        <view class="contact-info">
          <text class="contact-label">XX热线</text>
          <text class="contact-value">{{ contactInfo.phone }}</text>
        </view>
        <!-- <view class="contact-action">
          <text class="action-text">立即拨打</text>
          <text class="action-arrow">›</text>
        </view> -->
      </view>
      <view class="contact-card" @click="handleCall">
        <view class="card-accent"></view>
        <!-- <view class="contact-icon phone-icon">
          <text></text>
        </view> -->
        <view class="contact-info">
          <text class="contact-label">XX热线</text>
          <text class="contact-value">{{ contactInfo.phone }}</text>
        </view>
        <!-- <view class="contact-action">
          <text class="action-text">立即拨打</text>
          <text class="action-arrow">›</text>
        </view> -->
      </view>
      <view class="contact-card" @click="handleCall">
        <view class="card-accent"></view>
        <!-- <view class="contact-icon phone-icon">
          <text></text>
        </view> -->
        <view class="contact-info">
          <text class="contact-label">XX热线</text>
          <text class="contact-value">{{ contactInfo.phone }}</text>
        </view>
        <!-- <view class="contact-action">
          <text class="action-text">立即拨打</text>
          <text class="action-arrow">›</text>
        </view> -->
      </view>
      <view class="contact-card" @click="handleCall">
        <view class="card-accent"></view>
        <!-- <view class="contact-icon phone-icon">
          <text></text>
        </view> -->
        <view class="contact-info">
          <text class="contact-label">XX热线</text>
          <text class="contact-value">{{ contactInfo.phone }}</text>
        </view>
        <!-- <view class="contact-action">
          <text class="action-text">立即拨打</text>
          <text class="action-arrow">›</text>
        </view> -->
      </view>
      
      <!-- <view class="contact-card" @click="handleCopyWechat">
        <view class="card-accent"></view>
        <view class="contact-icon wechat-icon">
          <text></text>
        </view>
        <view class="contact-info">
          <text class="contact-label">微信号</text>
          <text class="contact-value">{{ contactInfo.wechat }}</text>
        </view>
        <view class="contact-action">
          <text class="action-text">复制</text>
          <text class="action-arrow">›</text>
        </view>
      </view> -->
      
      <!-- <view class="contact-card" @click="handleCopyEmail" v-if="contactInfo.email">
        <view class="card-accent"></view>
        <view class="contact-icon email-icon">
          <text></text>
        </view>
        <view class="contact-info">
          <text class="contact-label">邮箱</text>
          <text class="contact-value">{{ contactInfo.email }}</text>
        </view>
        <view class="contact-action">
          <text class="action-text">复制</text>
          <text class="action-arrow">›</text>
        </view>
      </view> -->
    </view>
    
    <!-- 服务时间 -->
    <view class="service-section">
      <view class="service-card">
        <view class="service-header">
          <text class="service-icon"></text>
          <text class="service-title">服务时间</text>
        </view>
        <view class="service-content">
          <view class="service-item">
            <text class="service-label">工作日</text>
            <text class="service-value">09:00 - 21:00</text>
          </view>
          <view class="service-item">
            <text class="service-label">周末/节假日</text>
            <text class="service-value">10:00 - 18:00</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部安全区 -->
    <view class="safe-area-bottom"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import NavBar from '@/components/NavBar.vue'

// 联系信息
const contactInfo = ref({
  qrCode: 'https://via.placeholder.com/300x300?text=QR+Code',
  phone: '400-123-4567',
  wechat: 'wechat_service_001',
  email: 'service@example.com'
})

// 预览二维码
const previewQrCode = () => {
  uni.previewImage({
    urls: [contactInfo.value.qrCode],
    current: contactInfo.value.qrCode
  })
}

// 拨打电话
const handleCall = () => {
  uni.makePhoneCall({
    phoneNumber: contactInfo.value.phone,
    fail: () => {
      uni.showToast({ title: '拨打电话失败', icon: 'none' })
    }
  })
}

// 复制微信号
const handleCopyWechat = () => {
  copyToClipboard(contactInfo.value.wechat, '微信号已复制')
}

// 复制邮箱
const handleCopyEmail = () => {
  copyToClipboard(contactInfo.value.email, '邮箱已复制')
}

// 复制到剪贴板
const copyToClipboard = (text, successMsg) => {
  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({ title: successMsg, icon: 'success' })
    },
    fail: () => {
      uni.showToast({ title: '复制失败', icon: 'none' })
    }
  })
}
</script>


<style lang="scss" scoped>
.contact-page {
  min-height: 100vh;
  background: $color-gray-50;
}

// 头部区域
.header-section {
  height: 320rpx;
  background: linear-gradient(135deg, $color-brand 0%, $color-brand-700 100%);
  border-radius: 0 0 48rpx 48rpx;
  position: relative;
  overflow: hidden;
  margin-bottom: -60rpx;
}

.header-bubble {
  position: absolute;
  border-radius: 50%;
  background: $color-white-alpha-10;
  pointer-events: none;
  
  &.bubble-1 {
    width: 160rpx;
    height: 160rpx;
    top: -40rpx;
    right: 40rpx;
    animation: float1 5s ease-in-out infinite;
  }
  
  &.bubble-2 {
    width: 100rpx;
    height: 100rpx;
    bottom: 60rpx;
    left: -30rpx;
    animation: float2 6s ease-in-out infinite;
  }
}

@keyframes float1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-15rpx, 20rpx) scale(1.1); }
}

@keyframes float2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20rpx, -15rpx) scale(1.15); }
}

.header-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 60rpx;
}

.header-icon {
  width: 80rpx;
  height: 80rpx;
  background: $color-white-alpha-20;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.icon-emoji {
  font-size: 40rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: $color-white;
  margin-bottom: 8rpx;
}

.header-subtitle {
  font-size: 26rpx;
  color: $color-white-alpha-80;
}

// 二维码区域
.qrcode-section {
  padding: 0 32rpx;
  position: relative;
  z-index: 2;
}

.qrcode-card {
  background: $color-white;
  border-radius: $radius-2xl;
  padding: 40rpx;
  text-align: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
}

.card-accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 6rpx;
  background: linear-gradient(180deg, $color-brand 0%, $color-brand-600 100%);
}

.qrcode-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 32rpx;
}

.qrcode-wrapper {
  display: flex;
  justify-content: center;
  padding: 24rpx;
  background: $color-gray-50;
  border-radius: $radius-xl;
  border: 2rpx dashed $color-border;
  margin-bottom: 16rpx;
}

.qrcode-image {
  width: 280rpx;
  height: 280rpx;
  border-radius: $radius-l;
}

.qrcode-tip {
  font-size: 24rpx;
  color: $color-text-quaternary;
}

// 联系方式区域
.contact-section {
  padding: 40rpx 32rpx 24rpx;
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 24rpx;
  padding-left: 8rpx;
}

.contact-card {
  background: $color-white;
  border-radius: $radius-xl;
  padding: 28rpx 32rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
    background: $color-gray-50;
  }
}

.contact-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: $radius-l;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  margin-right: 24rpx;
  flex-shrink: 0;
  
  &.phone-icon {
    background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  }
  
  &.wechat-icon {
    background: linear-gradient(135deg, #07C160 0%, #06AE56 100%);
  }
  
  &.email-icon {
    background: linear-gradient(135deg, $color-brand 0%, $color-brand-600 100%);
  }
}

.contact-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.contact-label {
  font-size: 24rpx;
  color: $color-text-tertiary;
}

.contact-value {
  font-size: 30rpx;
  font-weight: 600;
  color: $color-text-primary;
}

.contact-action {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.action-text {
  font-size: 26rpx;
  color: $color-brand;
  font-weight: 500;
}

.action-arrow {
  font-size: 32rpx;
  color: $color-brand;
}

// 服务时间区域
.service-section {
  padding: 0 32rpx 32rpx;
}

.service-card {
  background: $color-white;
  border-radius: $radius-xl;
  padding: 28rpx 32rpx;
}

.service-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid $color-border-light;
}

.service-icon {
  font-size: 28rpx;
}

.service-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text-primary;
}

.service-content {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.service-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.service-label {
  font-size: 26rpx;
  color: $color-text-tertiary;
}

.service-value {
  font-size: 26rpx;
  color: $color-text-primary;
  font-weight: 500;
}

// 底部安全区
.safe-area-bottom {
  height: 32rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
