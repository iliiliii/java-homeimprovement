<template>
  <view class="profile-page">
    <!-- 固定头部区域 -->
    <view class="fixed-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <!-- 标题 -->
      <view class="header-title">
        <text class="page-title">我的</text>
      </view>
      
      <!-- 用户信息 -->
      <view class="user-header">
        <view class="user-avatar">
          <SvgIcon name="account" size="80rpx" color="#C9B0D4" />
        </view>
        <view class="user-info">
          <text class="user-phone">{{ userInfo.phone }}</text>
          <text class="user-project">项目编号: {{ currentProject.code }}</text>
        </view>
      </view>
      
      <!-- 项目切换卡片 -->
      <!-- <view class="project-switcher">
        <scroll-view scroll-x class="projects-scroll" :scroll-left="scrollLeft">
          <view class="projects-container">
            <view 
              class="project-card"
              :class="{ active: currentProjectIndex === index }"
              v-for="(project, index) in projects"
              :key="project.id"
              @click="switchProject(index)"
            >
              <view class="project-card-header">
                <text class="project-card-name">{{ project.name }}</text>
                <text class="project-card-status" :class="project.statusClass">{{ project.status }}</text>
              </view>
              <view class="project-card-detail">
                <text class="project-card-info">{{ project.area }}㎡ · {{ project.style }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
        
        <view v-if="projects.length > 1" class="project-indicator">
          <view 
            class="indicator-dot"
            :class="{ active: currentProjectIndex === index }"
            v-for="(_, index) in projects"
            :key="index"
          ></view>
        </view>
      </view> -->
    </view>
    
    <!-- 头部占位 -->
    <view :style="{ height: headerHeight + 'px' }"></view>
    
    <!-- 可滚动内容 -->
    <view class="scroll-content">
      <!-- 功能菜单 -->
      <view class="menu-list">
        <!-- <view class="menu-item" @click="handleMenu('contract')">
          <view class="menu-icon" style="background: rgba(201, 176, 212, 0.15);">
            <SvgIcon name="file-text" size="40rpx" color="#C9B0D4" />
          </view>
          <text class="menu-text">合同文件</text>
          <SvgIcon name="arrow-right" size="28rpx" color="#ccc" />
        </view> -->
        
        <!-- <view class="menu-item" @click="handleMenu('payment')">
          <view class="menu-icon" style="background: rgba(232, 180, 76, 0.15);">
            <SvgIcon name="rmb-circle" size="40rpx" color="#E8B44C" />
          </view>
          <text class="menu-text">付款记录</text>
          <SvgIcon name="arrow-right" size="28rpx" color="#ccc" />
        </view> -->
        
        <!-- <view class="menu-item" @click="handleMenu('feedback')">
          <view class="menu-icon" style="background: rgba(157, 193, 131, 0.15);">
            <SvgIcon name="chat" size="40rpx" color="#9DC183" />
          </view>
          <text class="menu-text">意见反馈</text>
          <SvgIcon name="arrow-right" size="28rpx" color="#ccc" />
        </view> -->
        
        <view class="menu-item" @click="handleMenu('service')">
          <view class="menu-icon" style="background: rgba(167, 185, 211, 0.15);">
            <SvgIcon name="kefu-ermai" size="40rpx" color="#A7B9D3" />
          </view>
          <text class="menu-text">联系客服</text>
          <SvgIcon name="arrow-right" size="28rpx" color="#ccc" />
        </view>
        
        <view class="menu-item" @click="handleMenu('about')">
          <view class="menu-icon" style="background: rgba(100, 116, 139, 0.1);">
            <SvgIcon name="info-circle" size="40rpx" color="#64748B" />
          </view>
          <text class="menu-text">关于我们</text>
          <SvgIcon name="arrow-right" size="28rpx" color="#ccc" />
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
    </view>
    
    <!-- 底部占位 -->
    <view class="tab-bar-placeholder"></view>
    
    <!-- 自定义TabBar -->
    <CustomTabBar :current="3" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import CustomTabBar from '@/components/CustomTabBar.vue'
import SvgIcon from '@/components/SvgIcon.vue'
import { APP_CONFIG } from '@/config/app.js'
import { getStatusBarHeight } from '@/utils/system.js'
import { useUserStore } from '@/store/user.js'
import { logout as logoutApi } from '@/api/auth.js'

const userStore = useUserStore()

const statusBarHeight = ref(0)
const headerHeight = ref(0)
const scrollLeft = ref(0)

const userInfo = ref({
  phone: '138****3967'
})

// 多项目支持
const projects = ref([
  {
    id: 1,
    code: 'PJ-20251125-01',
    name: '御景壹号',
    status: '施工中',
    statusClass: 'active',
    area: 150,
    style: '现代简约',
    budget: '500,000'
  },
  {
    id: 2,
    code: 'PJ-20251201-02',
    name: '翡翠湾别墅',
    status: '设计中',
    statusClass: 'design',
    area: 320,
    style: '新中式',
    budget: '1,200,000'
  }
])

const currentProjectIndex = ref(0)

const currentProject = computed(() => {
  return projects.value[currentProjectIndex.value] || projects.value[0]
})

const switchProject = (index) => {
  currentProjectIndex.value = index
  // 保存当前选择的项目
  uni.setStorageSync('currentProjectIndex', index)
}

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  
  // 恢复上次选择的项目
  const savedIndex = uni.getStorageSync('currentProjectIndex')
  if (savedIndex !== '' && savedIndex < projects.value.length) {
    currentProjectIndex.value = savedIndex
  }
  
  // 读取用户信息
  const storedInfo = uni.getStorageSync('userInfo')
  if (storedInfo) {
    userInfo.value.phone = storedInfo.phone || userInfo.value.phone
  }
  
  nextTick(() => {
    const query = uni.createSelectorQuery()
    query.select('.fixed-header').boundingClientRect(rect => {
      if (rect) {
        headerHeight.value = rect.height
      }
    }).exec()
  })
})

const handleMenu = (type) => {
  if (type === 'about') {
    uni.navigateTo({
      url: '/pages/brand/index'
    })
    return
  }
  
  const titles = {
    contract: '合同文件',
    payment: '付款记录',
    feedback: '意见反馈',
    service: '联系客服'
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
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用后端退出登录接口
          await logoutApi()
        } catch (error) {
          // 即使后端调用失败，也继续清除本地状态
          console.warn('退出登录API调用失败:', error)
        }
        // 清除本地状态并跳转登录页
        userStore.logout()
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

// 固定头部
.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: $glass-bg;
}

.header-title {
  padding: 24rpx 48rpx 16rpx;
  text-align: center;
}

.page-title {
  font-size: 36rpx;
  font-weight: 600;
  color: $glass-text-main;
}

// 用户头部
.user-header {
  display: flex;
  align-items: center;
  padding: 16rpx 48rpx;
  gap: 24rpx;
}

.user-avatar {
  width: 100rpx;
  height: 100rpx;
  background: $glass-accent-light;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
}

.user-phone {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: $glass-text-main;
  margin-bottom: 4rpx;
}

.user-project {
  display: block;
  font-size: 24rpx;
  color: $glass-text-muted;
}

// 项目切换
.project-switcher {
  padding: 16rpx 0 24rpx;
}

.projects-scroll {
  white-space: nowrap;
  padding: 0 48rpx;
}

.projects-container {
  display: inline-flex;
  gap: 24rpx;
}

.project-card {
  width: 280rpx;
  background: white;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: $shadow-card;
  border: 2rpx solid transparent;
  flex-shrink: 0;
  
  &.active {
    border-color: $glass-accent;
    background: rgba(45, 91, 255, 0.05);
  }
}

.project-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.project-card-name {
  font-size: 28rpx;
  font-weight: 600;
  color: $glass-text-main;
}

.project-card-status {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 100rpx;
  
  &.active {
    background: rgba(45, 91, 255, 0.1);
    color: $glass-accent;
  }
  
  &.design {
    background: rgba(255, 176, 32, 0.1);
    color: $glass-warning;
  }
  
  &.done {
    background: rgba(0, 194, 178, 0.1);
    color: $glass-success;
  }
}

.project-card-detail {
  margin-top: 8rpx;
}

.project-card-info {
  font-size: 22rpx;
  color: $glass-text-muted;
}

// 项目指示器
.project-indicator {
  display: flex;
  justify-content: center;
  gap: 12rpx;
  margin-top: 16rpx;
}

.indicator-dot {
  width: 12rpx;
  height: 12rpx;
  background: #ddd;
  border-radius: 50%;
  
  &.active {
    background: $glass-accent;
    width: 24rpx;
    border-radius: 6rpx;
  }
}

// 可滚动内容
.scroll-content {
  padding-top: 16rpx;
}

// 功能菜单 - 修复水平布局
.menu-list {
  margin: 0 48rpx;
  background: white;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: $shadow-card;
}

.menu-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid $glass-bg;
  
  &:last-child {
    border-bottom: none;
  }
}

.menu-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
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
