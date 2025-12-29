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
      <!-- 节点头部 -->
      <view class="node-header" @click="toggleExpanded">
        <view class="node-title-section">
          <text class="node-title">{{ schedule.stageName }}</text>
          <view class="node-status">
            <text class="status-text" :class="`status-${schedule.status.toLowerCase()}`">
              {{ schedule.statusText }}
            </text>
            <text v-if="schedule.recordCount > 0" class="record-count">
              {{ schedule.recordCount }}条记录
            </text>
          </view>
        </view>
        <view class="expand-icon" :class="{ expanded: isExpanded }">
          <text>❯</text>
        </view>
      </view>
      
      <!-- 节点详情 -->
      <view v-if="schedule.description" class="node-desc">
        <text>{{ schedule.description }}</text>
      </view>
      
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
      <view v-if="isExpanded && records.length > 0" class="records-section">
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
import { ref, computed } from 'vue'
import ScheduleRecord from './ScheduleRecord.vue'
import { getProjectScheduleRecordList } from '@/api/projectSchedule'

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

const emit = defineEmits(['recordClick'])

const isExpanded = ref(false)
const records = ref([])
const loading = ref(false)

// 使用最新记录或空数组
const initialRecords = computed(() => props.schedule.latestRecords || [])

// 切换展开状态
const toggleExpanded = async () => {
  isExpanded.value = !isExpanded.value
  
  if (isExpanded.value && records.value.length === 0) {
    await loadRecords()
  }
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
    uni.showToast({
      title: '加载记录失败',
      icon: 'none'
    })
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
</script>

<style lang="scss" scoped>
.timeline-node {
  display: flex;
  gap: 24rpx;
  margin-bottom: 8rpx;
}

.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 32rpx;
  flex-shrink: 0;
}

.timeline-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  flex-shrink: 0;
  transition: all 0.3s ease;
  
  &.status-pending {
    background: $color-gray-400;
  }
  
  &.status-in_progress {
    background: $color-brand;
    box-shadow: 0 0 0 8rpx rgba(196, 0, 22, 0.2);
  }
  
  &.status-completed {
    background: $color-warning;
    box-shadow: 0 0 0 8rpx rgba(255, 193, 7, 0.2);
  }
  
  &.highlight {
    transform: scale(1.2);
  }
}

.timeline-connector {
  width: 4rpx;
  flex: 1;
  margin: 8rpx 0;
  transition: background-color 0.3s ease;
  
  &.status-pending {
    background: $color-gray-300;
  }
  
  &.status-in_progress {
    background: $color-brand;
  }
  
  &.status-completed {
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
  margin-bottom: 16rpx;
}

.node-title-section {
  flex: 1;
}

.node-title {
  font-weight: 600;
  font-size: 32rpx;
  color: $glass-text-main;
  display: block;
  margin-bottom: 8rpx;
}

.node-status {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.status-text {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  
  &.status-pending {
    background: rgba(156, 163, 175, 0.1);
    color: $color-gray-600;
  }
  
  &.status-in_progress {
    background: rgba(196, 0, 22, 0.1);
    color: $color-brand;
  }
  
  &.status-completed {
    background: rgba(255, 193, 7, 0.1);
    color: $color-warning;
  }
}

.record-count {
  font-size: 22rpx;
  color: $glass-text-muted;
}

.expand-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: $glass-text-muted;
  transition: transform 0.3s ease;
  
  &.expanded {
    transform: rotate(90deg);
  }
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
</style>