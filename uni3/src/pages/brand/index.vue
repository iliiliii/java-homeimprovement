<template>
  <view class="brand-page">
    <!-- 统一导航栏 - 透明背景 -->
    <NavBar 
      title="关于我们" 
      background="transparent" 
      titleColor="#fff" 
      iconColor="#fff"
      :showBorder="false"
    />
    
    <!-- 英雄区域 -->
    <view class="hero-section">
      <!-- 装饰气泡 -->
      <view class="hero-bubble bubble-1"></view>
      <view class="hero-bubble bubble-2"></view>
      <view class="hero-bubble bubble-3"></view>
      
      <view class="hero-content">
        <!-- Logo -->
        <image 
          class="hero-logo"
          src="@/styles/logo.png"
          mode="aspectFit"
        />
        <text class="hero-title">{{ APP_CONFIG.name }}</text>
        <text class="hero-subtitle">{{ APP_CONFIG.company.slogan }}</text>
        
        <!-- 装饰分隔线 -->
        <view class="hero-divider">
          <view class="divider-dot"></view>
          <view class="divider-line"></view>
          <view class="divider-dot"></view>
        </view>
      </view>
    </view>
    
    <!-- 核心数据展示 -->
    <view class="stats-section">
      <view class="stats-grid">
        <view class="stat-item" v-for="(stat, index) in stats" :key="index">
          <text class="stat-value">{{ stat.value }}</text>
          <text class="stat-unit">{{ stat.unit }}</text>
          <text class="stat-label">{{ stat.label }}</text>
        </view>
      </view>
    </view>
    
    <!-- 品牌故事 -->
    <view class="section">
      <view class="section-header">
        <view class="section-icon">
          <SvgIcon name="star" size="32rpx" color="#fff" />
        </view>
        <text class="section-title">品牌故事</text>
      </view>
      
      <!-- 理念卡片 -->
      <view class="info-card">
        <view class="card-accent"></view>
        <text class="card-title">我们的理念</text>
        <text class="card-content">
          {{ APP_CONFIG.name }} 致力于为每一个家庭打造充满智慧与温度的居住空间。我们坚持"设计以人为本"，将美学与功能完美融合，通过精细化的施工管理和全流程的贴心服务，让装修变得简单、透明、愉悦。
        </text>
      </view>
      
      <!-- 服务承诺卡片 -->
      <view class="info-card">
        <view class="card-accent"></view>
        <text class="card-title">服务承诺</text>
        <view class="promise-list">
          <view class="promise-item" v-for="(promise, index) in promises" :key="index">
            <view class="promise-check">
              <SvgIcon name="checkmark" size="24rpx" color="#fff" />
            </view>
            <text class="promise-text">{{ promise }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 设计团队 -->
    <view class="section">
      <view class="section-header">
        <view class="section-icon">
          <SvgIcon name="users" size="32rpx" color="#fff" />
        </view>
        <text class="section-title">设计团队</text>
      </view>
      
      <view class="team-grid">
        <view class="info-card team-card" v-for="member in teamMembers" :key="member.name">
          <view class="card-accent"></view>
          <view class="team-avatar-wrapper">
            <image class="team-avatar" :src="member.avatar" mode="aspectFill" />
          </view>
          <text class="team-name">{{ member.name }}</text>
          <text class="team-role">{{ member.role }}</text>
        </view>
      </view>
    </view>
    
    <!-- 底部版权 -->
    <view class="footer-section">
      <image 
        class="footer-logo"
        src="@/styles/logo.png"
        mode="aspectFit"
      />
      <text class="footer-company">{{ APP_CONFIG.company.name }}</text>
      <text class="footer-copyright">{{ APP_CONFIG.copyright.text }}</text>
    </view>
    
    <!-- 底部安全区占位 -->
    <view class="safe-area-bottom"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import NavBar from '@/components/NavBar.vue'
import SvgIcon from '@/components/SvgIcon.vue'
import { APP_CONFIG } from '@/config/app.js'

// 核心数据
const stats = ref([
  { value: '10', unit: '年+', label: '行业经验' },
  { value: '5000', unit: '+', label: '服务家庭' },
  { value: '98', unit: '%', label: '客户满意度' }
])

const teamMembers = ref([
  {
    name: '张泽宇',
    role: '首席设计师',
    avatar: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&q=80'
  },
  {
    name: '李晓琳',
    role: '软装搭配师',
    avatar: 'https://images.unsplash.com/photo-1573496359-7973131e9b71?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&q=80'
  },
  {
    name: '王建国',
    role: '工程总监',
    avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&q=80'
  },
  {
    name: '陈思思',
    role: '客户管家',
    avatar: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&q=80'
  }
])

const promises = ref([
  '0增项 · 预算即决算',
  '严选材 · 环保不达标全额退款',
  '保交付 · 延期赔付'
])
</script>


<style lang="scss" scoped>
.brand-page {
  min-height: 100vh;
  background: $color-white;
}

// 英雄区域
.hero-section {
  height: 620rpx;
  position: relative;
  background: linear-gradient(135deg, $color-yee-400 0%, $color-yee-700 100%);
  border-radius: 0 0 64rpx 64rpx;
  overflow: hidden;
  margin-bottom: 0;
}

// 装饰气泡
.hero-bubble {
  position: absolute;
  border-radius: 50%;
  background: $color-white-alpha-10;
  pointer-events: none;
}

.bubble-1 {
  width: 200rpx;
  height: 200rpx;
  top: -60rpx;
  right: 40rpx;
  animation: float1 6s ease-in-out infinite;
}

.bubble-2 {
  width: 120rpx;
  height: 120rpx;
  bottom: 80rpx;
  left: -40rpx;
  animation: float2 8s ease-in-out infinite;
}

.bubble-3 {
  width: 80rpx;
  height: 80rpx;
  top: 50%;
  right: -20rpx;
  animation: float3 5s ease-in-out infinite;
}

@keyframes float1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-20rpx, 30rpx) scale(1.1); }
}

@keyframes float2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30rpx, -20rpx) scale(1.15); }
}

@keyframes float3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-15rpx, 15rpx) scale(1.2); }
}

.hero-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48rpx;
  padding-top: 160rpx;
}

.hero-logo {
  width: 120rpx;
  height: 220rpx;
  margin-bottom: 24rpx;
  filter: drop-shadow(0 8rpx 24rpx rgba(0, 0, 0, 0.2));
}

.hero-title {
  font-size: 48rpx;
  font-weight: 700;
  color: $color-white;
  margin-bottom: 12rpx;
  text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
}

.hero-subtitle {
  font-size: 26rpx;
  color: $color-white-alpha-80;
  margin-bottom: 24rpx;
}

.hero-divider {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 16rpx;
}

.divider-dot {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: $color-white-alpha-60;
}

.divider-line {
  width: 80rpx;
  height: 2rpx;
  background: $color-white-alpha-40;
}

// 核心数据区域
.stats-section {
  margin-top: -48rpx;
  padding: 0 32rpx;
  position: relative;
  z-index: 2;
}

.stats-grid {
  display: flex;
  background: $color-white;
  border-radius: $radius-2xl;
  padding: 32rpx 16rpx;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.1);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  
  &:not(:last-child)::after {
    content: '';
    position: absolute;
    right: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 1rpx;
    height: 60rpx;
    background: $color-border;
  }
}

.stat-value {
  font-size: 44rpx;
  font-weight: 700;
  color: $color-brand;
  line-height: 1;
}

.stat-unit {
  font-size: 24rpx;
  color: $color-brand;
  font-weight: 500;
  margin-left: 4rpx;
}

.stat-label {
  font-size: 22rpx;
  color: $color-text-tertiary;
  margin-top: 8rpx;
}

// 区块通用样式
.section {
  padding: 48rpx 32rpx;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.section-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: $radius-l;
  background: linear-gradient(135deg, $color-brand 0%, $color-brand-600 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.section-title {
  font-size: 36rpx;
  font-weight: 700;
  color: $color-text-primary;
}

// 统一卡片样式
.info-card {
  background: $color-white;
  border: 2rpx solid $color-border-light;
  border-radius: $radius-2xl;
  padding: 32rpx;
  margin-bottom: 24rpx;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
  }
}

.card-accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 6rpx;
  background: linear-gradient(180deg, $color-brand 0%, $color-brand-600 100%);
}

.card-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 16rpx;
}

.card-content {
  font-size: 26rpx;
  color: $color-text-secondary;
  line-height: 1.8;
}

// 承诺列表
.promise-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.promise-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.promise-check {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $color-brand 0%, $color-brand-600 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.promise-text {
  font-size: 26rpx;
  color: $color-text-primary;
  font-weight: 500;
}

// 团队网格
.team-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}

.team-card {
  text-align: center;
  padding: 32rpx 24rpx;
}

.team-avatar-wrapper {
  position: relative;
  width: 100rpx;
  height: 100rpx;
  margin: 0 auto 16rpx;
}

.team-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 4rpx solid $color-white;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.team-name {
  display: block;
  font-weight: 600;
  font-size: 28rpx;
  color: $color-text-primary;
  margin-bottom: 8rpx;
}

.team-role {
  display: block;
  font-size: 22rpx;
  color: $color-text-tertiary;
}

// 底部区域
.footer-section {
  padding: 64rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: linear-gradient(180deg, $color-white 0%, $color-gray-50 100%);
}

.footer-logo {
  width: 80rpx;
  height: 150rpx;
  margin-bottom: 16rpx;
  opacity: 0.8;
}

.footer-company {
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 8rpx;
}

.footer-copyright {
  font-size: 22rpx;
  color: $color-text-quaternary;
}

// 底部安全区
.safe-area-bottom {
  height: constant(safe-area-inset-bottom);
  height: env(safe-area-inset-bottom);
}
</style>
