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
      <image 
        class="hero-bg"
        src="https://images.unsplash.com/photo-1497366216548-37526070297c?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
        mode="aspectFill"
      />
      <view class="hero-overlay">
        <text class="hero-title">{{ APP_CONFIG.name }}</text>
        <text class="hero-subtitle">{{ APP_CONFIG.company.slogan }}</text>
      </view>
    </view>
    
    <!-- 设计团队 -->
    <view class="section">
      <text class="section-title">设计团队</text>
      <view class="team-grid">
        <view class="team-card" v-for="member in teamMembers" :key="member.name">
          <image class="team-avatar" :src="member.avatar" mode="aspectFill" />
          <text class="team-name">{{ member.name }}</text>
          <text class="team-role">{{ member.role }}</text>
        </view>
      </view>
    </view>
    
    <!-- 品牌故事 -->
    <view class="section brand-section">
      <text class="section-title">品牌故事</text>
      
      <!-- 理念卡片 -->
      <view class="brand-card">
        <view class="brand-card-header">
          <view class="brand-icon" style="background: #C9B0D4;">
            <SvgIcon name="star" size="40rpx" color="#fff" />
          </view>
          <text class="brand-card-title">我们的理念</text>
        </view>
        <text class="brand-card-content">
          {{ APP_CONFIG.name }} 致力于为每一个家庭打造充满智慧与温度的居住空间。我们坚持"设计以人为本"，将美学与功能完美融合，通过精细化的施工管理和全流程的贴心服务，让装修变得简单、透明、愉悦。
        </text>
      </view>
      
      <!-- 服务承诺卡片 -->
      <view class="brand-card">
        <view class="brand-card-header">
          <view class="brand-icon" style="background: #9DC183;">
            <SvgIcon name="checkmark-circle" size="40rpx" color="#fff" />
          </view>
          <text class="brand-card-title">服务承诺</text>
        </view>
        <view class="promise-list">
          <view class="promise-item" v-for="(promise, index) in promises" :key="index">
            <SvgIcon name="checkmark" size="32rpx" color="#9DC183" />
            <text class="promise-text">{{ promise }}</text>
          </view>
        </view>
      </view>
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
}

// 英雄区域
.hero-section {
  height: 480rpx;
  position: relative;
  margin-bottom: 48rpx;
  border-radius: 0 0 64rpx 64rpx;
  overflow: hidden;
}

.hero-bg {
  width: 100%;
  height: 100%;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.1), rgba(0, 0, 0, 0.6));
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 48rpx;
}

.hero-title {
  font-size: 56rpx;
  font-weight: 700;
  color: white;
  margin-bottom: 16rpx;
}

.hero-subtitle {
  font-size: 28rpx;
  color: white;
  opacity: 0.9;
}

// 区块
.section {
  padding: 0 48rpx;
  margin-bottom: 48rpx;
}

.section-title {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: $glass-text-main;
  margin-bottom: 32rpx;
}

// 团队网格
.team-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 32rpx;
}

.team-card {
  background: $glass-bg;
  backdrop-filter: blur(40rpx);
  -webkit-backdrop-filter: blur(40rpx);
  border: 1rpx solid $glass-border;
  border-radius: 32rpx;
  padding: 32rpx;
  text-align: center;
  box-shadow: $shadow-card;
}

.team-avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  border: 4rpx solid white;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
  margin-bottom: 24rpx;
}

.team-name {
  display: block;
  font-weight: 600;
  font-size: 28rpx;
  margin-bottom: 8rpx;
  color: $glass-text-main;
}

.team-role {
  display: block;
  font-size: 24rpx;
  color: $glass-text-muted;
}

// 品牌区块
.brand-section {
  padding-bottom: 48rpx;
}

.brand-card {
  background: $glass-bg;
  backdrop-filter: blur(40rpx);
  -webkit-backdrop-filter: blur(40rpx);
  border: 1rpx solid $glass-border;
  border-radius: 48rpx;
  padding: 48rpx;
  margin-bottom: 32rpx;
}

.brand-card-header {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 32rpx;
}

.brand-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-card-title {
  font-weight: 600;
  font-size: 32rpx;
  color: $glass-text-main;
}

.brand-card-content {
  font-size: 28rpx;
  color: $glass-text-muted;
  line-height: 1.8;
}

// 承诺列表
.promise-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.promise-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.promise-text {
  font-size: 28rpx;
  color: $glass-text-main;
}

// 底部安全区
.safe-area-bottom {
  height: constant(safe-area-inset-bottom);
  height: env(safe-area-inset-bottom);
}
</style>
