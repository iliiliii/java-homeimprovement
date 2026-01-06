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
      <!-- 筛选区域 -->
      <view class="filter-section">
        <view class="filter-row">
          <!-- 项目下拉选择
          <view class="filter-dropdown" @click="showProjectPicker = true">
            <text class="dropdown-text">{{ selectedFilterProject || '全部项目' }}</text>
            <SvgIcon name="arrow-down" size="24rpx" color="#999" />
          </view>
           -->
          <!-- 搜索输入框 -->
          <view class="filter-search">
            <SvgIcon name="search" size="28rpx" color="#999" />
            <input 
              class="search-input"
              type="text"
              v-model="searchKeyword"
              placeholder="搜索客户/地址/项目名"
              @input="handleSearch"
            />
            <view class="clear-btn" v-if="searchKeyword" @click="clearSearch">
              <SvgIcon name="close" size="24rpx" color="#999" />
            </view>
          </view>
        </view>
        <!-- 
        <view class="filter-result">
          <text class="result-text">共 {{ filteredProjects.length }} 个项目</text>
          <text class="reset-btn" v-if="hasFilter" @click="resetFilter">重置</text>
        </view>
        -->
      </view>
      
      <!-- 项目下拉选择器 -->
      <view class="picker-mask" v-if="showProjectPicker" @click="showProjectPicker = false">
        <view class="picker-content" @click.stop>
          <view class="picker-header">
            <text class="picker-title">选择项目</text>
            <text class="picker-close" @click="showProjectPicker = false">关闭</text>
          </view>
          <scroll-view class="picker-list" scroll-y>
            <view 
              class="picker-item"
              :class="{ active: !selectedFilterProjectId }"
              @click="selectFilterProject(null, '全部项目')"
            >
              <text>全部项目</text>
              <SvgIcon v-if="!selectedFilterProjectId" name="check" size="28rpx" color="#C40016" />
            </view>
            <view 
              class="picker-item"
              :class="{ active: selectedFilterProjectId === project.id }"
              v-for="project in projects"
              :key="project.id"
              @click="selectFilterProject(project.id, project.name)"
            >
              <text>{{ project.name }}</text>
              <SvgIcon v-if="selectedFilterProjectId === project.id" name="check" size="28rpx" color="#C40016" />
            </view>
          </scroll-view>
        </view>
      </view>
      
      <!-- 项目卡片列表 -->
      <view class="project-list">
        <view 
          class="project-card"
          :class="{ selected: selectedProjectId === project.id }"
          v-for="project in filteredProjects"
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
            <!-- 
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
            -->
          </view>
          
          <!-- 问题提示
          <view class="issue-badge" v-if="project.pendingIssueCount > 0">
            <text>{{ project.pendingIssueCount }}个待处理</text>
          </view>
           -->
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-if="filteredProjects.length === 0 && !loading">
        <SvgIcon name="file-text" size="100rpx" color="#ccc" />
        <text class="empty-text">{{ hasFilter ? '没有匹配的项目' : '暂无负责的项目' }}</text>
      </view>
      
      <!-- 加载更多提示 -->
      <view class="load-more" v-if="filteredProjects.length > 0 && !hasFilter">
        <view class="loading-indicator" v-if="loadingMore">
          <view class="loading-spinner"></view>
          <text>加载中...</text>
        </view>
        <text class="no-more" v-else-if="!hasMore">没有更多项目了</text>
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
  },
  loadingMore: {
    type: Boolean,
    default: false
  },
  hasMore: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['view-project', 'select-project'])

const statusBarHeight = ref(0)
const headerHeight = ref(0)

// 筛选相关状态
const searchKeyword = ref('')
const selectedFilterProjectId = ref(null)
const selectedFilterProject = ref('')
const showProjectPicker = ref(false)

// 是否有筛选条件
const hasFilter = computed(() => {
  return searchKeyword.value || selectedFilterProjectId.value
})

// 筛选后的项目列表
const filteredProjects = computed(() => {
  let result = [...props.projects]
  
  // 按项目ID筛选
  if (selectedFilterProjectId.value) {
    result = result.filter(p => p.id === selectedFilterProjectId.value)
  }
  
  // 按关键词筛选（客户名称、项目地址、项目名称）
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    result = result.filter(p => {
      const name = (p.name || '').toLowerCase()
      const customerName = (p.customerName || '').toLowerCase()
      const address = (p.address || '').toLowerCase()
      return name.includes(keyword) || customerName.includes(keyword) || address.includes(keyword)
    })
  }
  
  return result
})

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
    'PLANNING': 'design',
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

// 处理搜索
const handleSearch = () => {
  // 输入时自动筛选，无需额外操作
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
}

// 选择筛选项目
const selectFilterProject = (id, name) => {
  selectedFilterProjectId.value = id
  selectedFilterProject.value = id ? name : ''
  showProjectPicker.value = false
}

// 重置筛选
const resetFilter = () => {
  searchKeyword.value = ''
  selectedFilterProjectId.value = null
  selectedFilterProject.value = ''
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

// 筛选区域
.filter-section {
  margin-bottom: 24rpx;
}

.filter-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.filter-dropdown {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 20rpx;
  background: #f5f5f5;
  border-radius: 16rpx;
  min-width: 180rpx;
  
  .dropdown-text {
    font-size: 26rpx;
    color: $glass-text-main;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.filter-search {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 20rpx;
  background: #f5f5f5;
  border-radius: 16rpx;
  
  .search-input {
    flex: 1;
    font-size: 26rpx;
    color: $glass-text-main;
    background: transparent;
  }
  
  .clear-btn {
    padding: 4rpx;
  }
}

.filter-result {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .result-text {
    font-size: 24rpx;
    color: $glass-text-muted;
  }
  
  .reset-btn {
    font-size: 24rpx;
    color: $color-brand;
  }
}

// 项目选择器弹窗
.picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.picker-content {
  width: 100%;
  max-height: 70vh;
  background: white;
  border-radius: 32rpx 32rpx 0 0;
  overflow: hidden;
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .picker-title {
    font-size: 32rpx;
    font-weight: 600;
    color: $glass-text-main;
  }
  
  .picker-close {
    font-size: 28rpx;
    color: $glass-text-muted;
  }
}

.picker-list {
  max-height: 60vh;
}

.picker-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #f5f5f5;
  
  text {
    font-size: 28rpx;
    color: $glass-text-main;
  }
  
  &.active {
    background: rgba(196, 0, 22, 0.05);
    
    text {
      color: $color-brand;
      font-weight: 500;
    }
  }
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
    background:  rgba(33, 33, 33, 0.1);
    color: $color-gray-800;
  }
  
  &.progress {
    background:rgba(196, 0, 22, 0.1);
    color: $color-brand;
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

// 加载更多
.load-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32rpx 0;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 12rpx;
  
  text {
    font-size: 24rpx;
    color: $glass-text-muted;
  }
}

.loading-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid #f3f3f3;
  border-top: 3rpx solid $color-brand;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.no-more {
  font-size: 24rpx;
  color: $glass-text-muted;
}
</style>

