<template>
  <view class="staff-dashboard">
    <!-- 固定头部区域 -->
    <PageHeader 
      title="工作台" 
      subtitle="管理您负责的项目" 
      :show-back="false"
      bg-color="linear-gradient(135deg, #C40016 0%, #E33E4A 100%)"
      text-color="#ffffff"
    />
    
    <!-- 待办统计 (固定在Header下方) -->
    <view class="todo-stats-fixed" :style="{ top: (statusBarHeight + 56) + 'px' }">
      <view class="todo-stats">
        <view class="stat-item" @click="$emit('navigate', '/pages/inspection/list')">
          <text class="stat-value">{{ todoStats.pendingInspections }}</text>
          <text class="stat-label">待巡检</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item" @click="$emit('navigate', '/pages/issue/list')">
          <text class="stat-value warning">{{ todoStats.pendingIssues }}</text>
          <text class="stat-label">待整改</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-value">{{ todoStats.todayTasks }}</text>
          <text class="stat-label">今日待办</text>
        </view>
      </view>
    </view>
    
    <!-- 头部占位 - 确保内容不被固定头部覆盖 -->
    <view class="header-placeholder" :style="{ height: headerHeight + 'px' }"></view>
    
    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="action-btn primary" @click="$emit('navigate', '/pages/inspection/create')">
        <SvgIcon name="plus" size="36rpx" color="#fff" />
        <text>新建巡检</text>
      </view>
      <view class="action-btn" @click="$emit('navigate', '/pages/issue/create')">
        <SvgIcon name="warning" size="36rpx" color="#C40016" />
        <text>问题上报</text>
      </view>
    </view>
    
    <!-- 项目列表 -->
    <view class="project-section">
      <view class="section-header">
        <text class="section-title">我的项目</text>
        <text class="project-count">共 {{ projects.length }} 个</text>
      </view>
      
      <!-- 项目卡片列表 -->
      <view class="project-list">
        <view 
          class="project-card"
          v-for="project in projects"
          :key="project.id"
          @click="$emit('view-project', project)"
        >
          <view class="card-main">
            <view class="card-header">
              <text class="project-name">{{ project.name }}</text>
              <view class="project-status" :class="getStatusClass(project.status)">
                {{ project.statusText }}
              </view>
            </view>
            
            <view class="card-info">
              <view class="info-row">
                <SvgIcon name="account" size="28rpx" color="#999" />
                <text>{{ project.customerName || '未知客户' }}</text>
              </view>
              <view class="info-row">
                <SvgIcon name="location" size="28rpx" color="#999" />
                <text>{{ project.address || '未设置地址' }}</text>
              </view>
            </view>
            
            <view class="card-footer">
              <view class="progress-section">
                <view class="progress-bar">
                  <view class="progress-fill" :style="{ width: project.progressPercent + '%' }"></view>
                </view>
                <text class="progress-text">{{ project.progressPercent }}%</text>
              </view>
              
              <view class="role-tag">
                <text>{{ project.myRoleText }}</text>
              </view>
            </view>
          </view>
          
          <!-- 问题提示 -->
          <view class="issue-badge" v-if="project.pendingIssueCount > 0">
            <text>{{ project.pendingIssueCount }}个待处理</text>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-if="projects.length === 0 && !loading">
        <SvgIcon name="file-text" size="100rpx" color="#ccc" />
        <text class="empty-text">暂无负责的项目</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick, getCurrentInstance } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'
import { getStatusBarHeight } from '@/utils/system.js'
import PageHeader from '@/components/PageHeader.vue'

const props = defineProps({
  projects: {
    type: Array,
    default: () => []
  },
  todoStats: {
    type: Object,
    default: () => ({ pendingInspections: 0, pendingIssues: 0, todayTasks: 0 })
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['navigate', 'view-project'])

const statusBarHeight = ref(0)
const headerHeight = ref(0)

// 更新header高度
const updateHeaderHeight = () => {
  // Header (44) + StatusBar + Stats (approx 120rpx/60px) + Padding
  // 简单计算：Stats height 140rpx approx
  headerHeight.value = statusBarHeight.value + 44 + uni.upx2px(140) + 32
}

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  
  // 根据状态栏高度预估一个初始值，避免闪烁
  // 头部内容约 180rpx + 待办统计约 120rpx + padding约 80rpx = 380rpx ≈ 190px + 状态栏
  const screenWidth = uni.getSystemInfoSync().windowWidth
  const estimatedHeight = (380 / 750) * screenWidth + statusBarHeight.value + 32
  headerHeight.value = estimatedHeight
  
  // 多次尝试获取精确高度
  setTimeout(updateHeaderHeight, 150)
  setTimeout(updateHeaderHeight, 400)
  setTimeout(updateHeaderHeight, 800)
})

// 获取状态样式类
const getStatusClass = (status) => {
  const upperStatus = status?.toUpperCase()
  const map = {
    'DESIGN': 'design',
    'IN_PROGRESS': 'progress',
    'CONSTRUCTION': 'progress',
    'COMPLETED': 'completed',
    'PENDING': 'pending'
  }
  return map[upperStatus] || 'default'
}

</script>

<style lang="scss" scoped>
.staff-dashboard {
  min-height: 100vh;
  background: $color-white;
  padding-bottom: 140rpx; // 为底部TabBar留出空间
}

// 头部占位
.header-placeholder {
  width: 100%;
  flex-shrink: 0;
}

// 待办统计固定容器
.todo-stats-fixed {
  position: fixed;
  left: 0;
  right: 0;
  z-index: 99;
  background: linear-gradient(135deg, $color-brand 0%, $color-brand-600 100%);
  padding-bottom: 32rpx;
  border-radius: 0 0 40rpx 40rpx;
  margin-top: -1rpx; // 消除缝隙
}

// 移除原 .fixed-header 样式, 保留 .todo-stats
// .header-content 移除



// 待办统计
.todo-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: rgba(255, 255, 255, 0.15);
  margin: 0 48rpx;
  padding: 24rpx;
  border-radius: 20rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-value {
  font-size: 44rpx;
  font-weight: 700;
  color: white;
  
  &.warning {
    color: #FFFFFF;
  }
}

.stat-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4rpx;
}

.stat-divider {
  width: 1rpx;
  height: 60rpx;
  background: rgba(255, 255, 255, 0.3);
}

// 快捷操作
.quick-actions {
  display: flex;
  gap: 24rpx;
  padding: 32rpx 48rpx;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 24rpx;
  background: white;
  border-radius: 20rpx;
  box-shadow: $shadow-card;
  
  text {
    font-size: 28rpx;
    font-weight: 500;
    color: $glass-text-main;
  }
  
  &.primary {
    background: $glass-accent;
    
    text {
      color: white;
    }
  }
}

// 项目区域
.project-section {
  padding: 0 48rpx 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $glass-text-main;
}

.project-count {
  font-size: 24rpx;
  color: $glass-text-muted;
}

// 项目列表
.project-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.project-card {
  background: white;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: $shadow-card;
  position: relative;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.project-name {
  font-size: 32rpx;
  font-weight: 600;
  color: $glass-text-main;
}

.project-status {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 100rpx;
  
  &.design {
    background: rgba(196, 0, 22, 0.1);
    color: $color-brand;
  }
  
  &.progress {
    background: rgba(33, 33, 33, 0.1);
    color: $color-gray-800;
  }
  
  &.completed {
    background: rgba(16, 185, 129, 0.1);
    color: $color-success;
  }
  
  &.pending {
    background: rgba(158, 158, 158, 0.1);
    color: #9E9E9E;
  }
}

.card-info {
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 8rpx;
  
  text {
    font-size: 26rpx;
    color: $glass-text-muted;
  }
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.progress-bar {
  flex: 1;
  height: 12rpx;
  background: #f0f0f0;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: $glass-accent;
  border-radius: 6rpx;
}

.progress-text {
  font-size: 24rpx;
  color: $glass-text-muted;
  min-width: 60rpx;
}

.role-tag {
  background: $color-gray-100;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  
  text {
    font-size: 22rpx;
    color: $glass-text-muted;
  }
}

// 问题提示
.issue-badge {
  position: absolute;
  top: 28rpx;
  right: 28rpx;
  background: $color-brand;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  
  text {
    font-size: 20rpx;
    color: white;
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx;
}

.empty-text {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: $glass-text-muted;
}
</style>
