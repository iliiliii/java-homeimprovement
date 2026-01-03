<template>
  <view class="timeline-node">
    <!-- 时间线 -->
    <view class="timeline-line">
      <view 
        class="timeline-dot" 
        :class="[
          `status-${schedule.status.toLowerCase()}`,
          { highlight: isLatest }
        ]"
      ></view>
      <view 
        v-if="!isLast" 
        class="timeline-connector"
        :class="`status-${schedule.status.toLowerCase()}`"
      ></view>
    </view>
    
    <!-- 节点内容 -->
    <view class="node-content">
      <!-- 节点头部 - 左侧标题，右侧状态 -->
      <view class="node-header">
        <view class="node-left">
          <!-- 阶段类型标签 -->
          <text 
            class="stage-type-tag" 
            :class="schedule.stageType === 'DESIGN' ? 'type-design' : 'type-construction'"
          >
            {{ schedule.stageType === 'DESIGN' ? '设计' : '施工' }}
          </text>
          <text class="node-title">{{ schedule.stageName }}</text>
        </view>
        <view class="node-right">
          <text class="status-text" :class="`status-${schedule.status.toLowerCase()}`">
            {{ schedule.statusText }}
          </text>
          <!-- 
          <text v-if="schedule.recordCount > 0" class="record-count">
            {{ schedule.recordCount }}条记录
          </text>
           -->
        </view>
      </view>
      
      <!-- 员工操作按钮 -->
      <view v-if="isStaff && canAcceptance" class="node-actions">
        <view class="action-btn" @click="handleAcceptance">
          <text class="action-icon">📋</text>
          <text class="action-text">验收上报</text>
        </view>
        <view class="action-btn issue-btn" @click="handleIssueReport">
          <text class="action-icon">⚠️</text>
          <text class="action-text">问题上报</text>
        </view>
      </view>
      
      <!-- 节点详情
      <view v-if="schedule.description" class="node-desc">
        <text>{{ schedule.description }}</text>
      </view>
       -->
      <!-- 时间信息 
      <view class="node-time">
        <text v-if="schedule.planStartDate" class="time-item">
          计划：{{ formatDate(schedule.planStartDate) }} - {{ formatDate(schedule.planEndDate) }}
        </text>
        <text v-if="schedule.actualStartDate" class="time-item">
          实际：{{ formatDate(schedule.actualStartDate) }}
          <template v-if="schedule.actualEndDate"> - {{ formatDate(schedule.actualEndDate) }}</template>
        </text>
      </view>
      -->
      <!-- 验收记录列表 -->
      <view v-if="records.length > 0" class="records-section">
      <!-- 
        <view class="records-title">
          <text>验收记录</text>
        </view>
         -->
        <view class="records-list">
          <ScheduleRecord
            v-for="record in records"
            :key="record.id"
            :record="record"
            @click="handleRecordClick"
            @preview-images="handlePreviewImages"
            @edit="handleEditRecord"
            @delete="handleDeleteRecord"
          />
        </view>
        
        <!-- 查看更多 -->
        <view v-if="schedule.recordCount > records.length" class="load-more-records" @click="loadMoreRecords">
          <text>查看更多记录 ({{ schedule.recordCount - records.length }})</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import ScheduleRecord from './ScheduleRecord.vue'
import { getProjectScheduleRecordList } from '@/api/projectSchedule'
import { useUserStore } from '@/store/user.js'

const props = defineProps({
  schedule: {
    type: Object,
    required: true
  },
  isLast: {
    type: Boolean,
    default: false
  },
  isLatest: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['recordClick', 'previewImages', 'acceptance', 'editRecord', 'deleteRecord', 'issueReport'])

const userStore = useUserStore()
const records = ref([])
const loading = ref(false)

// 是否员工用户
const isStaff = computed(() => userStore.isStaff)

// 是否可以验收（进行中或已完成的阶段可以验收）
const canAcceptance = computed(() => {
  const status = props.schedule.status?.toLowerCase()
  return status === 'in_progress' || status === '1' || status === 'completed' || status === '2'
})

// 使用最新记录或空数组
const initialRecords = computed(() => props.schedule.latestRecords || [])

// 切换展开状态（保留方法但不再使用）
const toggleExpanded = async () => {
  // 默认展开，不需要切换
}

// 处理验收上报点击
const handleAcceptance = () => {
  emit('acceptance', props.schedule)
}

// 处理问题上报点击
const handleIssueReport = () => {
  emit('issueReport', props.schedule)
}

// 加载验收记录
const loadRecords = async () => {
  if (loading.value) return
  
  try {
    loading.value = true
    const result = await getProjectScheduleRecordList({
      scheduleId: props.schedule.id,
      page: 1,
      pageSize: 10
    })
    records.value = result.rows || []
  } catch (error) {
    console.error('加载验收记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载更多记录
const loadMoreRecords = async () => {
  try {
    const result = await getProjectScheduleRecordList({
      scheduleId: props.schedule.id,
      page: 1,
      pageSize: props.schedule.recordCount
    })
    records.value = result.rows || []
  } catch (error) {
    console.error('加载更多记录失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

// 处理记录点击
const handleRecordClick = (record) => {
  emit('recordClick', record)
}

// 处理图片预览
const handlePreviewImages = (data) => {
  emit('previewImages', data)
}

// 处理编辑记录
const handleEditRecord = (record) => {
  emit('editRecord', record)
}

// 处理删除记录
const handleDeleteRecord = (record) => {
  emit('deleteRecord', record)
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  const recordDate = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  
  if (recordDate.getTime() === today.getTime()) {
    return `今天 ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
  } else if (recordDate.getTime() === yesterday.getTime()) {
    return `昨天 ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
  } else {
    return `${d.getMonth() + 1}.${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
  }
}

// 初始化时使用最新记录
records.value = initialRecords.value

// 组件挂载时自动加载记录
onMounted(() => {
  if (records.value.length === 0 && props.schedule.recordCount > 0) {
    loadRecords()
  }
})
</script>

<style lang="scss" scoped>
.timeline-node {
  display: flex;
  gap: 24rpx;
  position: relative;
}

.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 32rpx;
  flex-shrink: 0;
  position: relative;
}

.timeline-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  flex-shrink: 0;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
  margin-top: 28rpx; // 与卡片标题对齐
  background: $color-gray-400; // 默认颜色（待开始）
  
  &.status-pending,
  &.status-0 {
    background: $color-gray-400;
  }
  
  &.status-in_progress,
  &.status-1 {
    background: $color-brand;
    box-shadow: 0 0 0 8rpx rgba(173, 155, 75, 0.2);
  }
  
  &.status-completed,
  &.status-2 {
    background: $color-warning;
    box-shadow: 0 0 0 8rpx rgba(255, 193, 7, 0.2);
  }
  
  &.highlight {
    transform: scale(1.2);
  }
}

.timeline-connector {
  width: 4rpx;
  position: absolute;
  top: 52rpx; // 从圆点下方开始
  bottom: -24rpx; // 延伸到下一个节点
  left: 50%;
  transform: translateX(-50%);
  z-index: 1;
  transition: background-color 0.3s ease;
  background: $color-gray-300; // 默认颜色
  
  &.status-pending,
  &.status-0 {
    background: $color-gray-300;
  }
  
  &.status-in_progress,
  &.status-1 {
    background: $color-brand;
  }
  
  &.status-completed,
  &.status-2 {
    background: $color-warning;
  }
}

.node-content {
  flex: 1;
  background: white;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: $shadow-card;
  margin-bottom: 24rpx;
}

.node-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.node-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.stage-type-tag {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-weight: 500;
  
  &.type-design {
    background: rgba(250, 140, 22, 0.1);
    color: #fa8c16;
  }
  
  &.type-construction {
    background: rgba(22, 119, 255, 0.1);
    color: #1677ff;
  }
}

.node-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.node-title {
  font-weight: 600;
  font-size: 32rpx;
  color: $glass-text-main;
}

.status-text {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  background: rgba(156, 163, 175, 0.1); // 默认背景
  color: $color-gray-600; // 默认颜色
  
  &.status-pending,
  &.status-0 {
    background: rgba(156, 163, 175, 0.1);
    color: $color-gray-600;
  }
  
  &.status-in_progress,
  &.status-1 {
    background: rgba(173, 155, 75, 0.1);
    color: $color-brand;
  }
  
  &.status-completed,
  &.status-2 {
    background: rgba(255, 193, 7, 0.1);
    color: $color-warning;
  }
}

.record-count {
  font-size: 22rpx;
  color: $glass-text-muted;
}

.node-desc {
  margin-bottom: 16rpx;
  
  text {
    font-size: 26rpx;
    color: $glass-text-muted;
    line-height: 1.6;
  }
}

.node-time {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.time-item {
  font-size: 24rpx;
  color: $glass-text-muted;
}

.records-section {
  border-top: 2rpx solid $color-gray-100;
  padding-top: 24rpx;
  margin-top: 24rpx;
}

.records-title {
  margin-bottom: 16rpx;
  
  text {
    font-size: 28rpx;
    font-weight: 600;
    color: $glass-text-main;
  }
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.load-more-records {
  text-align: center;
  padding: 16rpx;
  margin-top: 16rpx;
  
  text {
    font-size: 26rpx;
    color: $color-brand;
  }
  
  &:active {
    opacity: 0.7;
  }
}

// 员工操作按钮
.node-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid $color-gray-100;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: rgba(173, 155, 75, 0.1);
  border-radius: 12rpx;
  
  &:active {
    opacity: 0.7;
  }
  
  .action-icon {
    font-size: 28rpx;
  }
  
  .action-text {
    font-size: 26rpx;
    color: $color-brand;
    font-weight: 500;
  }
  
  &.issue-btn {
    background: rgba(255, 77, 79, 0.1);
    
    .action-text {
      color: #ff4d4f;
    }
  }
}
</style>