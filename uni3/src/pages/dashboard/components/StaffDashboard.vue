<template>
  <view class="staff-dashboard">
    <!-- 固定头部区域 -->
    <PageHeader 
      title="工作台" 
      :subtitle="currentProjectName" 
      :show-back="false"
      bg-color="linear-gradient(135deg, #C40016 0%, #E33E4A 100%)"
      text-color="#ffffff"
    />
    
    <!-- 头部占位 - 确保内容不被固定头部覆盖 -->
    <view class="header-placeholder" :style="{ height: headerHeight + 'px' }"></view>
    
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
          :class="{ selected: selectedProjectId === project.id }"
          v-for="project in projects"
          :key="project.id"
          @click="handleProjectClick(project)"
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
import { ref, onMounted, computed } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'
import { getStatusBarHeight } from '@/utils/system.js'
import PageHeader from '@/components/PageHeader.vue'

const props = defineProps({
  projects: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  selectedProjectId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['view-project', 'select-project'])

const statusBarHeight = ref(0)
const headerHeight = ref(0)

// 当前选中项目的名称
const currentProjectName = computed(() => {
  if (!props.selectedProjectId) {
    return '请选择项目'
  }
  const project = props.projects.find(p => p.id === props.selectedProjectId)
  return project?.name || '未知项目'
})

// 更新header高度
const updateHeaderHeight = () => {
  // Header (56px) + StatusBar + Padding
  headerHeight.value = statusBarHeight.value + 56 + 32
}

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  updateHeaderHeight()
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

// 处理项目点击
const handleProjectClick = (project) => {
  // 如果点击的是已选中的项目，跳转到详情页
  if (props.selectedProjectId === project.id) {
    emit('view-project', project)
  } else {
    // 否则选中该项目
    emit('select-project', project.id)
  }
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
  transition: all 0.3s ease;
  
  &.selected {
    border: 2rpx solid $color-brand;
    box-shadow: 0 4rpx 20rpx rgba(196, 0, 22, 0.2);
    background: linear-gradient(135deg, rgba(196, 0, 22, 0.02) 0%, rgba(255, 255, 255, 1) 100%);
  }
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
