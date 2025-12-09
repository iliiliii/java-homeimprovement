<template>
  <view class="staff-dashboard">
    <!-- 固定头部区域 -->
    <view class="fixed-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <!-- 头部信息 -->
      <view class="header-content">
        <view class="header-info">
          <text class="greeting">工作台</text>
          <text class="sub-title">管理您负责的项目</text>
        </view>
      </view>
      
      <!-- 待办统计 -->
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
    
    <!-- 头部占位 -->
    <view :style="{ height: headerHeight + 'px' }"></view>
    
    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="action-btn primary" @click="$emit('navigate', '/pages/inspection/create')">
        <SvgIcon name="plus" size="36rpx" color="#fff" />
        <text>新建巡检</text>
      </view>
      <view class="action-btn" @click="$emit('navigate', '/pages/issue/create')">
        <SvgIcon name="warning" size="36rpx" color="#E8B44C" />
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
import { ref, onMounted, nextTick } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'
import { getStatusBarHeight } from '@/utils/system.js'

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

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  nextTick(() => {
    const query = uni.createSelectorQuery()
    query.select('.fixed-header').boundingClientRect(rect => {
      if (rect) {
        headerHeight.value = rect.height
      }
    }).exec()
  })
})

// 获取状态样式类
const getStatusClass = (status) => {
  const map = {
    'DESIGN': 'design',
    'IN_PROGRESS': 'progress',
    'CONSTRUCTION': 'progress',
    'COMPLETED': 'completed',
    'PENDING': 'pending'
  }
  return map[status] || 'default'
}
</script>

<style lang="scss" scoped>
.staff-dashboard {
  min-height: 100vh;
  background: $glass-bg;
}

// 固定头部
.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, #2D5BFF 0%, #5B7FFF 100%);
  padding-bottom: 32rpx;
  border-radius: 0 0 40rpx 40rpx;
}

.header-content {
  padding: 24rpx 48rpx;
}

.header-info {
  .greeting {
    display: block;
    font-size: 40rpx;
    font-weight: 700;
    color: white;
    margin-bottom: 8rpx;
  }
  
  .sub-title {
    display: block;
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.8);
  }
}

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
    color: #FFD93D;
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
    background: rgba(124, 77, 255, 0.1);
    color: #7C4DFF;
  }
  
  &.progress {
    background: rgba(45, 91, 255, 0.1);
    color: #2D5BFF;
  }
  
  &.completed {
    background: rgba(0, 194, 178, 0.1);
    color: #00C2B2;
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
  background: $glass-bg;
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
  background: #FF6B6B;
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
