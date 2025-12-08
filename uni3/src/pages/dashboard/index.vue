<template>
  <view class="dashboard-page">
    <!-- 固定头部区域 -->
    <view class="fixed-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <!-- 头部信息 -->
      <view class="header-content">
        <view class="header-info">
          <text class="project-name">{{ projectInfo.name }} · {{ projectInfo.area }}㎡</text>
          <text class="project-status">{{ isDesignPhase ? '方案设计中' : '施工进行中' }}</text>
        </view>
      </view>
      
      <!-- 进度卡片 -->
      <view class="progress-card">
        <view class="progress-info">
          <view class="flex-between">
            <text class="phase-text">当前阶段: {{ currentPhase }}</text>
            <view class="status-tag">{{ phaseStatus }}</view>
          </view>
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
          </view>
          <view class="flex-between progress-detail">
            <text>{{ isDesignPhase ? '方案确认度' : '总进度' }} {{ progressPercent }}%</text>
            <text>预计 {{ nextMilestone }} 节点完成</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 头部占位 -->
    <view :style="{ height: headerHeight + 'px' }"></view>
    
    <!-- 可滚动内容区域 -->
    <view class="scroll-content">
      <!-- 功能菜单 -->
      <view class="menu-section">
        <view class="menu-grid">
          <!-- 设计阶段专属 -->
          <view v-if="isDesignPhase" class="menu-item" @click="navigateTo('/pages/design/index')">
            <view class="menu-icon-box" style="background: rgba(201, 176, 212, 0.15);">
              <SvgIcon name="photo" size="48rpx" color="#C9B0D4" />
            </view>
            <text class="menu-text">设计图</text>
          </view>
          
          <!-- 施工阶段专属 -->
          <view v-if="!isDesignPhase" class="menu-item" @click="navigateTo('/pages/schedule/index')">
            <view class="menu-icon-box" style="background: rgba(167, 185, 211, 0.15);">
              <SvgIcon name="calendar" size="48rpx" color="#A7B9D3" />
            </view>
            <text class="menu-text">排期</text>
          </view>
          
          <view v-if="!isDesignPhase" class="menu-item" @click="navigateTo('/pages/log/index')">
            <view class="menu-icon-box" style="background: rgba(126, 150, 184, 0.15);">
              <SvgIcon name="file-text" size="48rpx" color="#7E96B8" />
            </view>
            <text class="menu-text">日志</text>
          </view>
          
          <!-- 文档链接 -->
          <view class="menu-item" @click="openDocLink">
            <view class="menu-icon-box" style="background: rgba(157, 193, 131, 0.15);">
              <SvgIcon name="file-text" size="48rpx" color="#9DC183" />
            </view>
            <text class="menu-text">文档</text>
          </view>
          
          <view class="menu-item" @click="navigateTo('/pages/budget/index')">
            <view class="menu-icon-box" style="background: rgba(232, 180, 76, 0.15);">
              <SvgIcon name="rmb-circle" size="48rpx" color="#E8B44C" />
            </view>
            <text class="menu-text">预算</text>
          </view>
        </view>
      </view>
      
      <!-- 设计方案展示（设计阶段） -->
      <view v-if="isDesignPhase" class="content-section">
        <text class="section-title">设计方案</text>
        <view class="glass-card design-preview" @click="navigateTo('/pages/design/index')">
          <image 
            class="design-image" 
            src="https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
            mode="aspectFill"
          />
          <view class="design-info flex-between">
            <view>
              <text class="design-title">全屋效果图 v2.0</text>
              <text class="design-update">更新于 今天 09:30</text>
            </view>
            <text class="view-link">查看 ></text>
          </view>
        </view>
      </view>
      
      <!-- 最新日志（施工阶段） -->
      <view v-if="!isDesignPhase" class="content-section">
        <view class="flex-between section-header">
          <text class="section-title">最新日志</text>
          <text class="view-all" @click="navigateTo('/pages/log/index')">查看全部</text>
        </view>
        <view class="log-preview glass-card" @click="navigateTo('/pages/log/index')">
          <image 
            class="log-image" 
            src="https://images.unsplash.com/photo-1581094794329-c8112a89af12?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&q=80"
            mode="aspectFill"
          />
          <view class="log-content">
            <view class="flex-between log-header">
              <text class="log-title">水电验收</text>
              <text class="log-time">今天 10:30</text>
            </view>
            <text class="log-desc">今日进行水电节点验收，强弱电间距符合标准，水管打压测试8kg保压30分钟无掉压。</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部占位 -->
    <view class="tab-bar-placeholder"></view>
    
    <!-- 自定义TabBar -->
    <CustomTabBar :current="0" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import CustomTabBar from '@/components/CustomTabBar.vue'
import SvgIcon from '@/components/SvgIcon.vue'
import { getStatusBarHeight, getNavBarHeight } from '@/utils/system.js'

const statusBarHeight = ref(0)
const headerHeight = ref(0)

// 项目信息
const projectInfo = ref({
  name: '御景壹号',
  area: 150
})

// 当前阶段状态
const isDesignPhase = ref(true)
const currentPhase = computed(() => isDesignPhase.value ? '深化设计' : '水电工程')
const phaseStatus = ref('正常推进')
const progressPercent = computed(() => isDesignPhase.value ? 40 : 35)
const nextMilestone = ref('12.10')



onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  // 计算固定头部高度
  nextTick(() => {
    const query = uni.createSelectorQuery()
    query.select('.fixed-header').boundingClientRect(rect => {
      if (rect) {
        headerHeight.value = rect.height
      }
    }).exec()
  })
})

// 页面导航
const navigateTo = (url) => {
  uni.navigateTo({ url })
}

// 打开外部文档链接
const openDocLink = () => {
  // 文档链接配置，在这里修改你的文档地址
  const docUrl = 'https://www.kdocs.cn/l/cgkCcqM3rO5j?from=docs'  // TODO: 替换为你的文档链接
  
  // #ifdef H5
  // H5 环境直接打开新窗口
  window.open(docUrl, '_blank')
  // #endif
  
  // #ifdef MP-WEIXIN
  // 微信小程序需要通过 webview 页面打开
  uni.navigateTo({
    url: `/pages/webview/index?url=${encodeURIComponent(docUrl)}&title=项目文档`
  })
  // #endif
  
  // #ifdef APP-PLUS
  // App 环境直接打开系统浏览器
  plus.runtime.openURL(docUrl)
  // #endif
}

</script>

<style lang="scss" scoped>
.dashboard-page {
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
  padding: 0 48rpx 32rpx;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
}

.header-info {
  .project-name {
    display: block;
    font-size: 28rpx;
    color: $glass-text-muted;
    margin-bottom: 8rpx;
  }
  
  .project-status {
    display: block;
    font-size: 40rpx;
    font-weight: 700;
    color: $glass-text-main;
  }
}

.header-icon {
  position: relative;
  width: 80rpx;
  height: 80rpx;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-card;
}

.notification-badge {
  position: absolute;
  top: -4rpx;
  right: -4rpx;
  min-width: 32rpx;
  height: 32rpx;
  background: #FF6B6B;
  color: white;
  font-size: 20rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

// 进度卡片
.progress-info {
  position: relative;
  z-index: 1;
}

.phase-text {
  font-size: 28rpx;
  opacity: 0.9;
}

.status-tag {
  background: rgba(255, 255, 255, 0.2);
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.progress-bar {
  margin: 32rpx 0 16rpx;
}

.progress-detail {
  font-size: 24rpx;
  opacity: 0.8;
}

// 可滚动内容
.scroll-content {
  padding-top: 32rpx;
}

// 功能菜单
.menu-section {
  padding: 0 48rpx;
  margin-bottom: 48rpx;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.menu-icon-box {
  width: 100rpx;
  height: 100rpx;
  background: white;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-card;
}

.menu-text {
  font-size: 24rpx;
  font-weight: 500;
  color: $glass-text-main;
}

// 内容区域
.content-section {
  padding: 0 48rpx;
  margin-bottom: 48rpx;
}

.section-header {
  margin-bottom: 32rpx;
  
  .section-title {
    margin-bottom: 0;
  }
}

.view-all {
  font-size: 24rpx;
  color: $glass-text-muted;
}

// 设计预览
.design-preview {
  padding: 0;
  overflow: hidden;
}

.design-image {
  width: 100%;
  height: 320rpx;
  display: block;
}

.design-info {
  padding: 24rpx;
}

.design-title {
  display: block;
  font-weight: 600;
  font-size: 28rpx;
  margin-bottom: 8rpx;
}

.design-update {
  display: block;
  font-size: 24rpx;
  color: $glass-text-muted;
}

.view-link {
  color: $glass-accent;
  font-size: 28rpx;
}

// 日志预览
.log-preview {
  display: flex;
  gap: 24rpx;
  padding: 24rpx;
}

.log-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.log-content {
  flex: 1;
  min-width: 0;
}

.log-header {
  margin-bottom: 8rpx;
}

.log-title {
  font-weight: 600;
  font-size: 28rpx;
}

.log-time {
  font-size: 24rpx;
  color: $glass-text-muted;
}

.log-desc {
  font-size: 24rpx;
  color: $glass-text-muted;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.6;
}
</style>
