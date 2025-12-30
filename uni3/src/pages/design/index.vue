<template>
  <view class="design-page">
    <!-- 固定头部区域 -->
    <PageHeader 
      title="设计方案"
      :subtitle="currentProject?.name"
      :show-back="false"
    >
      <template #bottom>
        <view class="filter-section" v-if="rooms.length > 0">
          <view class="filter-input-wrapper">
            <view class="search-icon">
              <SvgIcon name="search" size="32rpx" color="#999" />
            </view>
            <input 
              class="filter-input"
              type="text"
              placeholder="搜索或选择房间"
              v-model="searchKeyword"
              @input="handleSearchInput"
              @focus="showDropdown = true"
            />
            <view 
              v-if="searchKeyword" 
              class="clear-btn"
              @click="clearSearch"
            >
              <SvgIcon name="close" size="28rpx" color="#999" />
            </view>
            <view 
              class="dropdown-toggle"
              @click="toggleDropdown"
            >
            </view>
          </view>
          
          <!-- 下拉选项 -->
          <view v-if="showDropdown && dropdownOptions.length > 0" class="dropdown-list">
            <scroll-view scroll-y class="dropdown-scroll">
              <view 
                v-for="option in dropdownOptions"
                :key="option"
                class="dropdown-item"
                :class="{ active: searchKeyword === option }"
                @click="selectOption(option)"
              >
                <text>{{ option }}</text>
                <SvgIcon v-if="searchKeyword === option" name="check" size="28rpx" color="#C40016" />
              </view>
            </scroll-view>
          </view>
        </view>
      </template>
    </PageHeader>
    
    <!-- 头部占位 -->
    <view :style="{ height: headerHeight + 'px' }"></view>
    
    <!-- 遮罩层 -->
    <view 
      v-if="showDropdown" 
      class="dropdown-mask"
      @click="showDropdown = false"
    ></view>
    
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-state">
      <u-loading-icon size="48" color="#C40016" />
      <text class="loading-text">加载中...</text>
    </view>
    
    <!-- 空状态 -->
    <view v-else-if="rooms.length === 0" class="empty-state">
      <SvgIcon name="photo" size="120rpx" color="#ccc" />
      <text class="empty-text">暂无设计图</text>
      <text class="empty-tip">设计师正在努力设计中...</text>
    </view>
    
    <!-- 筛选无结果 -->
    <view v-else-if="filteredRooms.length === 0" class="empty-state">
      <SvgIcon name="search" size="120rpx" color="#ccc" />
      <text class="empty-text">未找到匹配的房间</text>
      <text class="empty-tip">尝试其他关键字或清空筛选</text>
      <view class="clear-filter-btn" @click="clearSearch">
        <text>清空筛选</text>
      </view>
    </view>
    
    <!-- 房间列表 -->
    <view v-else class="room-list">
      <view 
        class="room-card"
        v-for="room in filteredRooms"
        :key="room.id"
        @click="openRoom(room)"
      >
        <!-- 左侧：房间封面 -->
        <view class="room-cover">
          <image 
            v-if="room.images && room.images.length > 0"
            class="cover-image"
            :src="getFullUrl(room.images[0])"
            mode="aspectFill"
          />
          <view v-else class="cover-placeholder">
            <SvgIcon name="photo" size="40rpx" color="#ccc" />
          </view>
          <!-- 图片数量角标 -->
          <view v-if="room.imageCount > 1" class="image-count">
            <text>{{ room.imageCount }}</text>
          </view>
        </view>
        
        <!-- 右侧：房间信息 -->
        <view class="room-content">
          <!-- 第一行：房间名称 + 类型 -->
          <view class="room-title-row">
            <text class="room-name">{{ room.roomName }}</text>
            <text class="room-type">{{ room.roomTypeText }}</text>
          </view>
          
          <!-- 第二行：属性信息 -->
          <view class="room-info-row">
            <text class="room-info-text">
              <text v-if="room.area">{{ room.area }}㎡</text>
              <text v-if="room.area && (room.floor || room.orientationText || room.description)"> · </text>
              <text v-if="room.floor && room.floor != '0'">{{ room.floor }}F</text>
              <text v-if="room.floor && room.floor != '0' && (room.orientationText || room.description)"> · </text>
              <text v-if="room.orientationText">朝{{ room.orientationText }}</text>
              <text v-if="room.orientationText && room.description"> · </text>
              <text v-if="room.description">{{ room.description }}</text>
            </text>
          </view>
        </view>
        
        <!-- 箭头 -->
        <SvgIcon name="chevron-right" size="32rpx" color="#C0C0C0" class="room-arrow" />
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
import { onShow, onBackPress, onPullDownRefresh, onLoad } from '@dcloudio/uni-app'
import CustomTabBar from '@/components/CustomTabBar.vue'
import ImageViewer from '@/components/ImageViewer/index.vue'
import SvgIcon from '@/components/SvgIcon.vue'
import { getStatusBarHeight } from '@/utils/system.js'
import { useUserStore } from '@/store/user'
import PageHeader from '@/components/PageHeader.vue'
import { getProjectRooms } from '@/api/dashboard'
import { BASE_URL } from '@/utils/request'

const userStore = useUserStore()

const statusBarHeight = ref(0)
const headerHeight = ref(0)
const loading = ref(false)
const rooms = ref([])

// 筛选相关
const searchKeyword = ref('')
const showDropdown = ref(false)

// 图片查看器状态
const viewerVisible = ref(false)
const viewerIndex = ref(0)
const viewerImages = ref([])
const currentViewRoom = ref(null)

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

// 房间名称选项（去重）
const roomNameOptions = computed(() => {
  const names = rooms.value.map(room => room.roomName).filter(Boolean)
  return [...new Set(names)]
})

// 下拉选项（根据输入过滤）
const dropdownOptions = computed(() => {
  if (!searchKeyword.value) {
    return roomNameOptions.value
  }
  return roomNameOptions.value.filter(name => 
    name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 过滤后的房间列表
const filteredRooms = computed(() => {
  if (!searchKeyword.value) {
    return rooms.value
  }
  return rooms.value.filter(room => 
    room.roomName?.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

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

// 处理搜索输入
const handleSearchInput = () => {
  showDropdown.value = true
}

// 切换下拉菜单
const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

// 选择选项
const selectOption = (option) => {
  searchKeyword.value = option
  showDropdown.value = false
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
  showDropdown.value = false
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

// 更新头部高度
const updateHeaderHeight = () => {
  // PageHeader (56) + StatusBar + Filter (80+24=104rpx approx)
  // 如果有筛选区域，高度增加
  let filterHeight = rooms.value.length > 0 ? uni.upx2px(124) : 0
  headerHeight.value = statusBarHeight.value + 30 + filterHeight
}

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
  updateHeaderHeight()
})

// 页面显示时加载数据
onShow(() => {
  loadRooms()
})

// 监听项目切换
watch(currentProjectId, (newId, oldId) => {
  if (newId !== oldId) {
    searchKeyword.value = '' // 清空筛选
    loadRooms()
  }
})

// 监听rooms变化更新头部高度
watch(rooms, () => {
  updateHeaderHeight()
})

// 监听返回键
onBackPress((e) => {
  if (showDropdown.value) {
    showDropdown.value = false
    return true
  }
  if (viewerVisible.value) {
    viewerVisible.value = false
    return true
  }
})

// 下拉刷新
onPullDownRefresh(async () => {
  await loadRooms()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss" scoped>
.design-page {
  min-height: 100vh;
  background: $color-white;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}


// 移除 .fixed-header, .header-title


// 筛选区域
.filter-section {
  padding: 0 32rpx 24rpx;
  position: relative;
}

.filter-input-wrapper {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 16rpx;
  padding: 0 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.search-icon {
  flex-shrink: 0;
  margin-right: 16rpx;
}

.filter-input {
  flex: 1;
  height: 80rpx;
  font-size: 28rpx;
  color: $glass-text-main;
}

.clear-btn, .dropdown-toggle {
  flex-shrink: 0;
  padding: 16rpx;
  margin: -16rpx;
  margin-left: 8rpx;
  
  &:active {
    opacity: 0.7;
  }
}

// 下拉列表
.dropdown-list {
  position: absolute;
  left: 32rpx;
  right: 32rpx;
  top: 100%;
  margin-top: 8rpx;
  background: white;
  border-radius: 16rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
  z-index: 200;
  overflow: hidden;
}

.dropdown-scroll {
  max-height: 400rpx;
}

.dropdown-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 32rpx;
  font-size: 28rpx;
  color: $glass-text-main;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
  
  &:last-child {
    border-bottom: none;
  }
  
  &:active {
    background: rgba(0, 0, 0, 0.03);
  }
  
  &.active {
    color: $color-brand;
    font-weight: 500;
  }
}

// 遮罩层
.dropdown-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50;
}

// 清空筛选按钮
.clear-filter-btn {
  margin-top: 32rpx;
  padding: 20rpx 48rpx;
  background: $glass-accent;
  border-radius: 40rpx;
  
  text {
    font-size: 28rpx;
    color: white;
  }
  
  &:active {
    opacity: 0.9;
  }
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
  padding: 20rpx 32rpx;
}

.room-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  
  &:active {
    background: #fafafa;
  }
}

// 房间封面
.room-cover {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
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
}

.image-count {
  position: absolute;
  top: 6rpx;
  right: 6rpx;
  min-width: 32rpx;
  height: 32rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
  
  text {
    font-size: 20rpx;
    color: #fff;
    font-weight: 500;
  }
}

// 房间内容
.room-content {
  flex: 1;
  margin: 0 20rpx;
  overflow: hidden;
}

.room-title-row {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.room-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-right: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.room-type {
  font-size: 22rpx;
  color: $color-brand;
  background: rgba(196, 0, 22, 0.08);
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
  flex-shrink: 0;
}

.room-info-row {
  overflow: hidden;
}

.room-info-text {
  font-size: 26rpx;
  color: #999;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.5;
}

// 箭头
.room-arrow {
  flex-shrink: 0;
}

// TabBar占位
.tab-bar-placeholder {
  height: 120rpx;
}
</style>
