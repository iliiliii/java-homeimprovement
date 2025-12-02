<template>
  <view class="profile-page">
    <!-- 头部用户信息 -->
    <view class="user-header">
      <view class="user-avatar">
        <u-icon name="account" size="80" color="#2D5BFF" />
      </view>
      <view class="user-info">
        <text class="user-phone">{{ userInfo.phone }}</text>
        <text class="user-project">项目编号: {{ userInfo.projectCode }}</text>
      </view>
    </view>
    
    <!-- 项目信息卡片 -->
    <view class="glass-card project-card">
      <view class="project-header">
        <text class="project-name">{{ projectInfo.name }}</text>
        <text class="project-status">{{ projectInfo.status }}</text>
      </view>
      <view class="project-detail">
        <view class="detail-item">
          <text class="detail-label">面积</text>
          <text class="detail-value">{{ projectInfo.area }}㎡</text>
        </view>
        <view class="detail-item">
          <text class="detail-label">风格</text>
          <text class="detail-value">{{ projectInfo.style }}</text>
        </view>
        <view class="detail-item">
          <text class="detail-label">预算</text>
          <text class="detail-value">¥{{ projectInfo.budget }}</text>
        </view>
      </view>
    </view>
    
    <!-- 功能菜单 -->
    <view class="menu-list">
      <view class="menu-item" @click="handleMenu('contract')">
        <view class="menu-icon">
          <u-icon name="file-text" size="44" color="#2D5BFF" />
        </view>
        <text class="menu-text">合同文件</text>
        <u-icon name="arrow-right" size="32" color="#ccc" />
      </view>
      
      <view class="menu-item" @click="handleMenu('payment')">
        <view class="menu-icon">
          <u-icon name="rmb-circle" size="44" color="#FFB020" />
        </view>
        <text class="menu-text">付款记录</text>
        <u-icon name="arrow-right" size="32" color="#ccc" />
      </view>
      
      <view class="menu-item" @click="handleMenu('feedback')">
        <view class="menu-icon">
          <u-icon name="chat" size="44" color="#4ECDC4" />
        </view>
        <text class="menu-text">意见反馈</text>
        <u-icon name="arrow-right" size="32" color="#ccc" />
      </view>
      
      <view class="menu-item" @click="handleMenu('service')">
        <view class="menu-icon">
          <u-icon name="kefu-ermai" size="44" color="#FF6B6B" />
        </view>
        <text class="menu-text">联系客服</text>
        <u-icon name="arrow-right" size="32" color="#ccc" />
      </view>
      
      <view class="menu-item" @click="handleMenu('about')">
        <view class="menu-icon">
          <u-icon name="info-circle" size="44" color="#64748B" />
        </view>
        <text class="menu-text">关于我们</text>
        <u-icon name="arrow-right" size="32" color="#ccc" />
      </view>
    </view>
    
    <!-- 退出登录 -->
    <view class="logout-btn" @click="handleLogout">
      <text>退出登录</text>
    </view>
    
    <!-- 版本信息 -->
    <view class="version-info">
      <text>{{ APP_CONFIG.name }} {{ APP_CONFIG.version.name }}</text>
    </view>
    
    <!-- 底部占位 -->
    <view class="tab-bar-placeholder"></view>
    
    <!-- 自定义TabBar -->
    <CustomTabBar :current="3" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import CustomTabBar from '@/components/CustomTabBar.vue'
import { APP_CONFIG } from '@/config/app.js'

const userInfo = ref({
  phone: '138****3967',
  projectCode: 'PJ-20251125-01'
})

const projectInfo = ref({
  name: '御景壹号',
  status: '施工中',
  area: 150,
  style: '现代简约',
  budget: '500,000'
})

onMounted(() => {
  // 读取用户信息
  const storedInfo = uni.getStorageSync('userInfo')
  if (storedInfo) {
    userInfo.value.phone = storedInfo.phone || userInfo.value.phone
    userInfo.value.projectCode = storedInfo.projectCode || userInfo.value.projectCode
  }
})

const handleMenu = (type) => {
  const titles = {
    contract: '合同文件',
    payment: '付款记录',
    feedback: '意见反馈',
    service: '联系客服',
    about: '关于我们'
  }
  
  uni.showToast({
    title: titles[type] || '功能开发中',
    icon: 'none'
  })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.clearStorageSync()
        uni.reLaunch({
          url: '/pages/login/index'
        })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

// 用户头部
.user-header {
  display: flex;
  align-items: center;
  padding: 100rpx 48rpx 48rpx;
  gap: 32rpx;
}

.user-avatar {
  width: 140rpx;
  height: 140rpx;
  background: $glass-accent-light;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-info {
  flex: 1;
}

.user-phone {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: $glass-text-main;
  margin-bottom: 8rpx;
}

.user-project {
  display: block;
  font-size: 26rpx;
  color: $glass-text-muted;
}

// 项目卡片
.project-card {
  margin: 0 48rpx 48rpx;
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.project-name {
  font-size: 36rpx;
  font-weight: 600;
  color: $glass-text-main;
}

.project-status {
  font-size: 24rpx;
  color: $glass-accent;
  background: $glass-accent-light;
  padding: 8rpx 24rpx;
  border-radius: 100rpx;
}

.project-detail {
  display: flex;
  justify-content: space-between;
}

.detail-item {
  text-align: center;
}

.detail-label {
  display: block;
  font-size: 24rpx;
  color: $glass-text-muted;
  margin-bottom: 8rpx;
}

.detail-value {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: $glass-text-main;
}

// 功能菜单
.menu-list {
  margin: 0 48rpx;
  background: white;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: $shadow-card;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid $glass-bg;
  
  &:last-child {
    border-bottom: none;
  }
}

.menu-icon {
  width: 72rpx;
  height: 72rpx;
  background: $glass-bg;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.menu-text {
  flex: 1;
  font-size: 28rpx;
  color: $glass-text-main;
}

// 退出登录
.logout-btn {
  margin: 48rpx;
  padding: 28rpx;
  background: white;
  border-radius: 24rpx;
  text-align: center;
  box-shadow: $shadow-card;
  
  text {
    font-size: 28rpx;
    color: $glass-danger;
  }
}

// 版本信息
.version-info {
  text-align: center;
  padding: 24rpx;
  
  text {
    font-size: 24rpx;
    color: $glass-text-muted;
  }
}
</style>

