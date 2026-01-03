<template>
  <view class="log-page">
    <!-- 统一头部 -->
    <PageHeader 
      title="项目日志" 
      :subtitle="currentProject?.name"
      :show-back="false" 
    />
    
    <!-- 头部占位 -->
    <view :style="{ height: headerHeight + 'px' }"></view>
    
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text class="loading-text">加载中...</text>
    </view>
    
    <!-- 空状态 -->
    <view v-else-if="schedules.length === 0" class="empty-container">
      <text class="empty-text">暂无项目进度</text>
    </view>
    
    <!-- 进度时间线 -->
    <view v-else class="timeline">
      <TimelineNode
        v-for="(schedule, index) in schedules"
        :key="schedule.id"
        :schedule="schedule"
        :is-last="index === schedules.length - 1"
        :is-latest="index === 0"
        @record-click="handleRecordClick"
        @preview-images="handlePreviewImages"
        @acceptance="handleAcceptance"
        @issue-report="handleIssueReport"
        @edit-record="(record) => handleEditRecord(record, schedule)"
        @delete-record="handleDeleteRecord"
      />
    </view>
    
    <!-- 底部占位 -->
    <view class="tab-bar-placeholder"></view>
    
    <!-- 自定义TabBar -->
    <CustomTabBar :current="2" />
    
    <!-- 图片查看器 -->
    <ImageViewer 
      v-model:visible="viewerVisible"
      :images="viewerImages"
      :start-index="viewerIndex"
      :show-thumbnail="true"
    />
    
    <!-- 验收上报对话框（仅员工可见） -->
    <AcceptanceDialog
      v-model:visible="acceptanceDialogVisible"
      :schedule="currentSchedule"
      :edit-record="editRecord"
      @success="handleAcceptanceSuccess"
    />
    
    <!-- 问题上报对话框（仅员工可见） -->
    <IssueReportDialog
      v-model:visible="issueDialogVisible"
      @success="handleIssueSuccess"
    />
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { onPullDownRefresh, onLoad } from '@dcloudio/uni-app'
import CustomTabBar from '@/components/CustomTabBar.vue'
import ImageViewer from '@/components/ImageViewer/index.vue'
import TimelineNode from './components/TimelineNode.vue'
import AcceptanceDialog from './components/AcceptanceDialog.vue'
import IssueReportDialog from './components/IssueReportDialog.vue'
import { getStatusBarHeight } from '@/utils/system.js'
import PageHeader from '@/components/PageHeader.vue'
import { useUserStore } from '@/store/user.js'
import { getProjectScheduleList, getProjectScheduleRecordDetail, deleteAcceptanceRecord } from '@/api/projectSchedule'

const userStore = useUserStore()

// 当前项目ID（优先使用URL参数，否则使用store中的）
const urlProjectId = ref('')
const currentProjectId = computed(() => urlProjectId.value || userStore.currentProjectId)
const currentProject = computed(() => {
  if (!currentProjectId.value) return null
  // 从store的项目列表中查找
  const project = userStore.projects.find(p => p.id === currentProjectId.value)
  if (project) {
    return project
  }
  // 如果找不到，返回基本信息
  return { id: currentProjectId.value, name: '当前项目' }
})

const statusBarHeight = ref(0)
const headerHeight = ref(0)
const loading = ref(false)

// 图片查看器状态
const viewerVisible = ref(false)
const viewerIndex = ref(0)
const viewerImages = ref([])

// 验收对话框状态
const acceptanceDialogVisible = ref(false)
const currentSchedule = ref(null)
const editRecord = ref(null) // 编辑模式下的记录

// 问题上报对话框状态
const issueDialogVisible = ref(false)

// 施工进度列表
const schedules = ref([])

// 页面加载时获取URL参数
onLoad((options) => {
  if (options.projectId) {
    urlProjectId.value = options.projectId
    // 如果是员工账户，更新store中的项目ID
    if (userStore.isStaff) {
      userStore.switchProject(options.projectId)
    }
  }
})

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  headerHeight.value = statusBarHeight.value + 66
  // 延迟加载，确保项目ID已设置
  setTimeout(() => {
    loadSchedules()
  }, 100)
})

// 加载施工进度列表
const loadSchedules = async () => {
  if (loading.value) return
  
  // 如果没有项目ID，提示用户先选择项目
  if (!currentProjectId.value) {
    if (userStore.isStaff) {
      uni.showToast({
        title: '请先选择项目',
        icon: 'none'
      })
    }
    schedules.value = []
    return
  }
  
  try {
    loading.value = true
    console.log('开始调用真实API获取施工进度数据，项目ID:', currentProjectId.value)
    const data = await getProjectScheduleList()
    console.log('API返回数据:', data)
    
    if (data && Array.isArray(data)) {
      schedules.value = data
      console.log('成功设置进度数据，共', data.length, '项')
    } else {
      console.warn('API返回数据格式异常:', data)
      schedules.value = []
    }
  } catch (error) {
    console.error('加载施工进度失败:', error)
    schedules.value = []
    uni.showToast({
      title: '加载失败: ' + error.message,
      icon: 'none',
      duration: 3000
    })
  } finally {
    loading.value = false
  }
}

// 处理验收记录点击
const handleRecordClick = async (record) => {
  try {
    // 获取记录详情
    const detail = await getProjectScheduleRecordDetail(record.id)
    
    // 如果有图片，打开图片查看器
    if (detail.images && detail.images.length > 0) {
      viewerImages.value = detail.images
      viewerIndex.value = 0
      viewerVisible.value = true
    } else {
      // 显示记录详情
      uni.showModal({
        title: detail.title,
        content: detail.description || '暂无详细描述',
        showCancel: false,
        confirmText: '知道了'
      })
    }
  } catch (error) {
    console.error('获取记录详情失败:', error)
    uni.showToast({
      title: '获取详情失败',
      icon: 'none'
    })
  }
}

// 处理图片预览
const handlePreviewImages = ({ images, index }) => {
  viewerImages.value = images
  viewerIndex.value = index
  viewerVisible.value = true
}

// 处理验收上报
const handleAcceptance = (schedule) => {
  currentSchedule.value = schedule
  editRecord.value = null // 新增模式
  acceptanceDialogVisible.value = true
}

// 处理问题上报
const handleIssueReport = (schedule) => {
  currentSchedule.value = schedule
  issueDialogVisible.value = true
}

// 处理编辑验收记录
const handleEditRecord = (record, schedule) => {
  currentSchedule.value = schedule
  editRecord.value = record
  acceptanceDialogVisible.value = true
}

// 处理删除验收记录
const handleDeleteRecord = (record) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这条验收记录吗？',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '删除中...', mask: true })
          await deleteAcceptanceRecord(record.id)
          uni.hideLoading()
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadSchedules()
        } catch (error) {
          uni.hideLoading()
          console.error('删除失败:', error)
          uni.showToast({ title: error.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 验收成功回调
const handleAcceptanceSuccess = () => {
  // 刷新数据
  loadSchedules()
}

// 问题上报成功回调
const handleIssueSuccess = () => {
  // 刷新数据
  loadSchedules()
  uni.showToast({
    title: '问题上报成功',
    icon: 'success'
  })
}

// 下拉刷新
onPullDownRefresh(async () => {
  await loadSchedules()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss" scoped>
.log-page {
  min-height: 100vh;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

// 加载状态
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  gap: 24rpx;
}

.loading-text {
  font-size: 26rpx;
  color: $glass-text-muted;
}

// 空状态
.empty-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: $glass-text-muted;
}

// 时间线
.timeline {
  padding: 0 48rpx;
}
</style>
