<template>
  <view class="log-page">
    <!-- 固定头部 -->
    <view class="fixed-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-title">
        <text class="page-title">施工日志</text>
      </view>
    </view>
    
    <!-- 头部占位 -->
    <view :style="{ height: headerHeight + 'px' }"></view>
    
    <!-- 日志时间线 -->
    <view class="timeline">
      <view 
        class="log-item"
        v-for="(log, index) in logs"
        :key="log.id"
        @click="viewLogDetail(log)"
      >
        <!-- 时间线 -->
        <view class="timeline-line">
          <view class="timeline-dot" :class="{ highlight: index === 0 }"></view>
          <view v-if="index < logs.length - 1" class="timeline-connector"></view>
        </view>
        
        <!-- 日志内容 -->
        <view class="log-content">
          <view class="log-header flex-between">
            <text class="log-title">{{ log.title }}</text>
            <text class="log-time">{{ log.time }}</text>
          </view>
          
          <text class="log-desc">{{ log.description }}</text>
          
          <!-- 日志图片 -->
          <view v-if="log.images && log.images.length > 0" class="log-images">
            <image 
              v-for="(img, imgIndex) in log.images.slice(0, 3)"
              :key="imgIndex"
              class="log-image"
              :src="img"
              mode="aspectFill"
              @click.stop="previewImage(log.images, imgIndex)"
            />
            <view v-if="log.images.length > 3" class="image-more">
              +{{ log.images.length - 3 }}
            </view>
          </view>
          
          <!-- 日志标签 -->
          <view class="log-tags">
            <view class="log-tag" :class="log.type">
              {{ getTypeText(log.type) }}
            </view>
            <view v-if="log.phase" class="log-tag phase">
              {{ log.phase }}
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 加载更多 -->
    <view class="load-more" @click="loadMore">
      <text>查看更多日志</text>
    </view>
    
    <!-- 底部占位 -->
    <view class="tab-bar-placeholder"></view>
    
    <!-- 自定义TabBar -->
    <CustomTabBar :current="2" />
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import CustomTabBar from '@/components/CustomTabBar.vue'
import { getStatusBarHeight } from '@/utils/system.js'

const statusBarHeight = ref(0)
const headerHeight = ref(0)

const logs = ref([
  {
    id: 1,
    title: '拆除工程',
    description: '今日进行水电节点验收，强弱电间距符合标准，水管打压测试8kg保压30分钟无掉压。',
    time: '今天 10:30',
    type: 'inspection',
    phase: '拆除工程',
    images: [
      'https://images.unsplash.com/photo-1581094794329-c8112a89af12?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
      'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
    ]
  },
  {
    id: 2,
    title: '水电改造',
    description: '水电验收合格，无问题。',
    time: '昨天 16:20',
    type: 'progress',
    phase: '水电改造',
    images: [
      'https://images.unsplash.com/photo-1504307651254-35680f356dfd?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
    ]
  },
  {
    id: 3,
    title: '泥瓦工程',
    description: '泥瓦工程完成，无问题。',
    time: '11.25 09:00',
    type: 'material',
    phase: '泥瓦工程',
    images: []
  },
  {
    id: 4,
    title: '木工工程',
    description: '木工工程完成，无问题。',
    time: '11.10 14:30',
    type: 'inspection',
    phase: '木工工程',
    images: [
      'https://images.unsplash.com/photo-1503387762-592deb58ef4e?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
      'https://images.unsplash.com/photo-1581094288338-2314dddb7ece?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
      'https://images.unsplash.com/photo-1504307651254-35680f356dfd?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
      'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
    ]
  }
])

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

const getTypeText = (type) => {
  const map = {
    inspection: '验收',
    progress: '进度',
    material: '材料',
    issue: '问题'
  }
  return map[type] || ''
}

const previewImage = (images, index) => {
  uni.previewImage({
    urls: images,
    current: index
  })
}

const viewLogDetail = (log) => {
  uni.showToast({
    title: `查看${log.title}详情`,
    icon: 'none'
  })
}

const loadMore = () => {
  uni.showToast({
    title: '加载更多日志',
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
.log-page {
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

// 时间线
.timeline {
  padding: 0 48rpx;
}

.log-item {
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
  background: #ddd;
  border-radius: 50%;
  flex-shrink: 0;
  
  &.highlight {
    background: $glass-accent;
    box-shadow: 0 0 0 8rpx rgba(45, 91, 255, 0.2);
  }
}

.timeline-connector {
  width: 4rpx;
  flex: 1;
  background: #eee;
  margin: 8rpx 0;
}

.log-content {
  flex: 1;
  background: white;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: $shadow-card;
  margin-bottom: 24rpx;
}

.log-header {
  margin-bottom: 12rpx;
}

.log-title {
  font-weight: 600;
  font-size: 28rpx;
  color: $glass-text-main;
}

.log-time {
  font-size: 24rpx;
  color: $glass-text-muted;
}

.log-desc {
  display: block;
  font-size: 26rpx;
  color: $glass-text-muted;
  line-height: 1.6;
  margin-bottom: 16rpx;
}

// 日志图片
.log-images {
  display: flex;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.log-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 16rpx;
  background: #eee;
}

.image-more {
  width: 140rpx;
  height: 140rpx;
  border-radius: 16rpx;
  background: $glass-bg;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: $glass-text-muted;
}

// 日志标签
.log-tags {
  display: flex;
  gap: 12rpx;
}

.log-tag {
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  
  &.inspection {
    background: rgba(45, 91, 255, 0.1);
    color: $glass-accent;
  }
  
  &.progress {
    background: rgba(0, 194, 178, 0.1);
    color: $glass-success;
  }
  
  &.material {
    background: rgba(255, 176, 32, 0.1);
    color: $glass-warning;
  }
  
  &.issue {
    background: rgba(255, 107, 107, 0.1);
    color: $glass-danger;
  }
  
  &.phase {
    background: $glass-bg;
    color: $glass-text-muted;
  }
}

// 加载更多
.load-more {
  text-align: center;
  padding: 32rpx;
  
  text {
    font-size: 26rpx;
    color: $glass-text-muted;
  }
}
</style>
