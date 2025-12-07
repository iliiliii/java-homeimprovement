<template>
  <view class="design-page">
    <!-- 固定头部区域 -->
    <view class="fixed-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <!-- 标题 -->
      <view class="header-title">
        <text class="page-title">设计方案</text>
      </view>
      
      <!-- 空间分类 -->
      <view class="space-tabs">
        <scroll-view scroll-x class="tabs-scroll">
          <view class="tabs-container">
            <view 
              class="tab-item"
              :class="{ active: currentSpace === space.key }"
              v-for="space in spaces"
              :key="space.key"
              @click="switchSpace(space.key)"
            >
              {{ space.name }}
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
    
    <!-- 头部占位 -->
    <view :style="{ height: headerHeight + 'px' }"></view>
    
    <!-- 设计图展示 -->
    <view class="design-gallery">
      <view 
        class="design-card"
        v-for="(item, index) in currentDesigns"
        :key="index"
        @click="openViewer(index)"
      >
        <image class="design-image" :src="item.image" mode="aspectFill" />
        <view class="design-info">
          <text class="design-name">{{ item.name }}</text>
          <text class="design-version">{{ item.version }}</text>
        </view>
        <view class="design-meta flex-between">
          <text class="design-date">{{ item.updateTime }}</text>
          <view class="design-status" :class="item.status">
            {{ getStatusText(item.status) }}
          </view>
        </view>
      </view>
    </view>
    
    <!-- 空状态 -->
    <view v-if="currentDesigns.length === 0" class="empty-state">
      <u-icon name="photo" size="100" color="#ccc" />
      <text class="empty-text">暂无设计图</text>
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
      url-key="image"
      name-key="name"
      desc-key="updateTime"
      :show-thumbnail="true"
    />
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import CustomTabBar from '@/components/CustomTabBar.vue'
import ImageViewer from '@/components/ImageViewer/index.vue'
import { getStatusBarHeight } from '@/utils/system.js'

const statusBarHeight = ref(0)
const headerHeight = ref(0)
const currentSpace = ref('all')
const viewerVisible = ref(false)
const viewerIndex = ref(0)

const spaces = ref([
  { key: 'all', name: '全部' },
  { key: 'living', name: '客厅' },
  { key: 'bedroom', name: '卧室' },
  { key: 'kitchen', name: '厨房' },
  { key: 'bathroom', name: '卫生间' },
  { key: 'balcony', name: '阳台' }
])

const designs = ref([
  {
    id: 1,
    name: '客厅效果图',
    space: 'living',
    version: 'v2.0',
    updateTime: '今天 09:30',
    status: 'confirmed',
    image: 'https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
  },
  {
    id: 2,
    name: '主卧效果图',
    space: 'bedroom',
    version: 'v1.5',
    updateTime: '昨天 14:20',
    status: 'reviewing',
    image: 'https://images.unsplash.com/photo-1616594039964-ae9021a400a0?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
  },
  {
    id: 3,
    name: '厨房效果图',
    space: 'kitchen',
    version: 'v2.1',
    updateTime: '12.01',
    status: 'confirmed',
    image: 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
  },
  {
    id: 4,
    name: '卫生间效果图',
    space: 'bathroom',
    version: 'v1.0',
    updateTime: '11.28',
    status: 'draft',
    image: 'https://images.unsplash.com/photo-1552321554-5fefe8c9ef14?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
  }
])

const currentDesigns = computed(() => {
  if (currentSpace.value === 'all') {
    return designs.value
  }
  return designs.value.filter(d => d.space === currentSpace.value)
})

// 用于查看器的图片列表
const viewerImages = computed(() => currentDesigns.value)

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

const switchSpace = (key) => {
  currentSpace.value = key
}

const getStatusText = (status) => {
  const map = {
    confirmed: '已确认',
    reviewing: '待确认',
    draft: '草稿'
  }
  return map[status] || ''
}

// 打开图片查看器
const openViewer = (index) => {
  viewerIndex.value = index
  viewerVisible.value = true
}
</script>

<style lang="scss" scoped>
.design-page {
  min-height: 100vh;
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
  font-size: 36rpx;
  font-weight: 600;
  color: $glass-text-main;
}

// 空间分类
.space-tabs {
  padding: 0 48rpx 24rpx;
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs-container {
  display: inline-flex;
  gap: 24rpx;
}

.tab-item {
  padding: 16rpx 32rpx;
  border-radius: 100rpx;
  background: white;
  color: $glass-text-muted;
  font-size: 28rpx;
  flex-shrink: 0;
  
  &.active {
    background: $glass-accent;
    color: white;
  }
}

// 设计图展示
.design-gallery {
  padding: 0 48rpx;
}

.design-card {
  background: white;
  border-radius: 32rpx;
  overflow: hidden;
  margin-bottom: 32rpx;
  box-shadow: $shadow-card;
}

.design-image {
  width: 100%;
  height: 400rpx;
}

.design-info {
  padding: 24rpx 24rpx 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.design-name {
  font-weight: 600;
  font-size: 32rpx;
  color: $glass-text-main;
}

.design-version {
  font-size: 24rpx;
  color: $glass-text-muted;
  background: $glass-bg;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
}

.design-meta {
  padding: 16rpx 24rpx 24rpx;
}

.design-date {
  font-size: 24rpx;
  color: $glass-text-muted;
}

.design-status {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  
  &.confirmed {
    background: rgba(0, 194, 178, 0.1);
    color: $glass-success;
  }
  
  &.reviewing {
    background: rgba(255, 176, 32, 0.1);
    color: $glass-warning;
  }
  
  &.draft {
    background: $glass-bg;
    color: $glass-text-muted;
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
