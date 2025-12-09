<template>
  <view class="design-page">
    <!-- 固定头部区域 -->
    <view class="fixed-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <!-- 标题 -->
      <view class="header-title">
        <text class="page-title">设计方案</text>
        <text v-if="currentProject" class="project-name">{{ currentProject.name }}</text>
      </view>
    </view>
    
    <!-- 头部占位 -->
    <view :style="{ height: headerHeight + 'px' }"></view>
    
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-state">
      <u-loading-icon size="48" color="#C9B0D4" />
      <text class="loading-text">加载中...</text>
    </view>
    
    <!-- 空状态 -->
    <view v-else-if="rooms.length === 0" class="empty-state">
      <SvgIcon name="photo" size="120rpx" color="#ccc" />
      <text class="empty-text">暂无设计图</text>
      <text class="empty-tip">设计师正在努力设计中...</text>
    </view>
    
    <!-- 房间列表 -->
    <view v-else class="room-list">
      <view 
        class="room-card"
        v-for="room in rooms"
        :key="room.id"
        @click="openRoom(room)"
      >
        <!-- 房间封面 -->
        <view class="room-cover">
          <image 
            v-if="room.images && room.images.length > 0"
            class="cover-image"
            :src="getFullUrl(room.images[0])"
            mode="aspectFill"
          />
          <view v-else class="cover-placeholder">
            <SvgIcon name="photo" size="64rpx" color="#ccc" />
          </view>
          <!-- 图片数量角标 -->
          <view v-if="room.imageCount > 0" class="image-count">
            <SvgIcon name="photo" size="24rpx" color="#fff" />
            <text>{{ room.imageCount }}</text>
          </view>
        </view>
        
        <!-- 房间信息 -->
        <view class="room-info">
          <view class="room-header">
            <text class="room-name">{{ room.roomName }}</text>
            <view class="room-type-tag">{{ room.roomTypeText }}</view>
          </view>
          <view class="room-meta">
            <text v-if="room.area" class="meta-item">{{ room.area }}㎡</text>
            <text v-if="room.floor" class="meta-item">{{ room.floor }}楼</text>
          </view>
          <text v-if="room.description" class="room-desc">{{ room.description }}</text>
        </view>
        
        <!-- 箭头 -->
        <view class="room-arrow">
          <SvgIcon name="chevron-right" size="32rpx" color="#ccc" />
        </view>
      </view>
    </view>
    
    <!-- 底部占位 -->
    <view class="tab-bar-placeholder"></view>
    
    <!-- 自定义TabBar -->
    <CustomTabBar :current="1" />
    
    <!-- 图片查看器 -->
    <ImageViewer 
      v-model:visible="viewerVisible"
      :images="viewerImages"
      :start-index="viewerIndex"
      url-key="url"
      name-key="name"
      :show-thumbnail="true"
    />
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { onShow, onBackPress } from '@dcloudio/uni-app'
import CustomTabBar from '@/components/CustomTabBar.vue'
import ImageViewer from '@/components/ImageViewer/index.vue'
import SvgIcon from '@/components/SvgIcon.vue'
import { getStatusBarHeight } from '@/utils/system.js'
import { useUserStore } from '@/store/user'
import { getProjectRooms } from '@/api/dashboard'
import { BASE_URL } from '@/utils/request'

const userStore = useUserStore()

const statusBarHeight = ref(0)
const headerHeight = ref(0)
const loading = ref(false)
const rooms = ref([])

// 图片查看器状态
const viewerVisible = ref(false)
const viewerIndex = ref(0)
const viewerImages = ref([])
const currentViewRoom = ref(null)

// 当前项目
const currentProject = computed(() => userStore.currentProject)
const currentProjectId = computed(() => userStore.currentProjectId)

// 获取完整URL
const getFullUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  // 拼接基础URL（使用request.js中的BASE_URL）
  if (path.startsWith('/')) {
    return BASE_URL + path
  }
  return BASE_URL + '/' + path
}

// 加载房间列表
const loadRooms = async () => {
  if (!currentProjectId.value) {
    rooms.value = []
    return
  }
  
  loading.value = true
  try {
    // request.js 的响应拦截器会返回 data.data，所以这里直接是数组
    const data = await getProjectRooms(currentProjectId.value)
    rooms.value = data || []
  } catch (error) {
    console.error('加载房间列表失败:', error)
    rooms.value = []
    // 错误提示已在request.js中处理
  } finally {
    loading.value = false
  }
}

// 打开房间查看设计图
const openRoom = (room) => {
  if (!room.images || room.images.length === 0) {
    uni.showToast({
      title: '该房间暂无设计图',
      icon: 'none'
    })
    return
  }
  
  currentViewRoom.value = room
  // 转换为查看器需要的格式
  viewerImages.value = room.images.map((url, index) => ({
    url: getFullUrl(url),
    name: `${room.roomName} - 设计图${index + 1}`
  }))
  viewerIndex.value = 0
  viewerVisible.value = true
}

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

// 页面显示时加载数据
onShow(() => {
  loadRooms()
})

// 监听项目切换
watch(currentProjectId, (newId, oldId) => {
  if (newId !== oldId) {
    loadRooms()
  }
})

// 监听返回键
onBackPress((e) => {
  if (viewerVisible.value) {
    viewerVisible.value = false
    return true
  }
})
</script>

<style lang="scss" scoped>
.design-page {
  min-height: 100vh;
  background: $glass-bg;
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
  padding: 24rpx 48rpx;
  text-align: center;
}

.page-title {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: $glass-text-main;
}

.project-name {
  display: block;
  font-size: 24rpx;
  color: $glass-text-muted;
  margin-top: 8rpx;
}

// 加载状态
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
}

.loading-text {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: $glass-text-muted;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 48rpx;
}

.empty-text {
  margin-top: 32rpx;
  font-size: 32rpx;
  color: $glass-text-main;
}

.empty-tip {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: $glass-text-muted;
}

// 房间列表
.room-list {
  padding: 24rpx 32rpx;
}

.room-card {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: $shadow-card;
  
  &:active {
    opacity: 0.9;
    transform: scale(0.99);
  }
}

// 房间封面
.room-cover {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  overflow: hidden;
  flex-shrink: 0;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.image-count {
  position: absolute;
  bottom: 8rpx;
  right: 8rpx;
  display: flex;
  align-items: center;
  gap: 4rpx;
  background: rgba(0, 0, 0, 0.6);
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  
  text {
    font-size: 22rpx;
    color: #fff;
  }
}

// 房间信息
.room-info {
  flex: 1;
  margin-left: 24rpx;
  overflow: hidden;
}

.room-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.room-name {
  font-size: 32rpx;
  font-weight: 600;
  color: $glass-text-main;
}

.room-type-tag {
  font-size: 22rpx;
  color: $glass-accent;
  background: rgba(201, 176, 212, 0.15);
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
}

.room-meta {
  display: flex;
  gap: 16rpx;
  margin-top: 12rpx;
}

.meta-item {
  font-size: 24rpx;
  color: $glass-text-muted;
}

.room-desc {
  display: block;
  font-size: 24rpx;
  color: $glass-text-muted;
  margin-top: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// 箭头
.room-arrow {
  flex-shrink: 0;
  margin-left: 16rpx;
}

// TabBar占位
.tab-bar-placeholder {
  height: 120rpx;
}
</style>
