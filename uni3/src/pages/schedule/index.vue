<template>
  <view class="schedule-page">
    <!-- 头部 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="48" />
      </view>
      <text class="page-title">全案排期</text>
      <view style="width: 48rpx;"></view>
    </view>
    
    <!-- 阶段列表 -->
    <view class="phase-list">
      <view 
        class="phase-card"
        :class="phase.status"
        v-for="(phase, index) in phases"
        :key="index"
        @click="viewPhaseDetail(phase)"
      >
        <view class="phase-header flex-between">
          <text class="phase-title">{{ phase.name }}</text>
          <text 
            class="phase-status-tag"
            :class="phase.status"
          >{{ getStatusText(phase.status) }}</text>
        </view>
        <text class="phase-date">{{ phase.dateRange }}</text>
        
        <!-- 子任务（仅活动阶段显示） -->
        <view v-if="phase.status === 'active'" class="sub-tasks">
          <view 
            class="sub-task"
            :class="{ completed: task.completed }"
            v-for="(task, taskIndex) in phase.tasks"
            :key="taskIndex"
          >
            <u-icon 
              :name="task.completed ? 'checkmark' : 'circle'" 
              :size="32"
              :color="task.completed ? '#00C2B2' : '#ddd'"
            />
            <text class="task-text">{{ task.name }}</text>
          </view>
          
          <view class="view-detail">
            <text class="view-detail-text">查看详情 ></text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部占位 -->
    <view class="tab-bar-placeholder"></view>
    
    <!-- 自定义TabBar -->
    <CustomTabBar :current="1" />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import CustomTabBar from '@/components/CustomTabBar.vue'

const phases = ref([
  {
    name: '拆除阶段',
    status: 'done',
    dateRange: '11.01 - 11.10 (提前2天)',
    tasks: []
  },
  {
    name: '水电阶段',
    status: 'active',
    dateRange: '11.11 - 11.25 (延期1天)',
    tasks: [
      { name: '开槽布管', completed: true },
      { name: '强弱电箱安装', completed: true },
      { name: '水管打压测试', completed: false }
    ]
  },
  {
    name: '泥木阶段',
    status: 'pending',
    dateRange: '预计 11.26 开始',
    tasks: []
  },
  {
    name: '油漆阶段',
    status: 'pending',
    dateRange: '预计 12.10 开始',
    tasks: []
  },
  {
    name: '安装阶段',
    status: 'pending',
    dateRange: '预计 12.25 开始',
    tasks: []
  }
])

const getStatusText = (status) => {
  const map = {
    done: '已完成',
    active: '进行中',
    pending: '待开始'
  }
  return map[status] || ''
}

const goBack = () => {
  uni.navigateBack()
}

const viewPhaseDetail = (phase) => {
  uni.showToast({
    title: `查看${phase.name}详情`,
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
.schedule-page {
  min-height: 100vh;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

// 头部
.header {
  padding: 100rpx 48rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  width: 48rpx;
  display: flex;
  align-items: center;
}

// 阶段列表
.phase-list {
  padding: 0 48rpx;
}

.phase-card {
  background: white;
  border-radius: 40rpx;
  padding: 40rpx;
  margin-bottom: 32rpx;
  box-shadow: $shadow-card;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 12rpx;
    background: #eee;
  }
  
  &.done::before {
    background: $glass-success;
  }
  
  &.active::before {
    background: $glass-accent;
  }
}

.phase-header {
  margin-bottom: 8rpx;
}

.phase-title {
  font-size: 36rpx;
  font-weight: 600;
  color: $glass-text-main;
  
  .pending & {
    color: $glass-text-muted;
  }
}

.phase-status-tag {
  font-size: 24rpx;
  font-weight: 600;
  
  &.done {
    color: $glass-success;
  }
  
  &.active {
    color: $glass-accent;
  }
  
  &.pending {
    color: $glass-text-muted;
  }
}

.phase-date {
  font-size: 24rpx;
  color: $glass-text-muted;
}

// 子任务
.sub-tasks {
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid $glass-bg;
}

.sub-task {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 12rpx 0;
  
  &.completed {
    .task-text {
      color: $glass-text-muted;
    }
  }
}

.task-text {
  font-size: 28rpx;
  color: $glass-text-main;
}

.view-detail {
  margin-top: 24rpx;
  text-align: center;
}

.view-detail-text {
  color: $glass-accent;
  font-size: 24rpx;
  font-weight: 600;
}
</style>

