<template>
  <view class="tabbar-container">
    <!-- 占位元素 -->
    <view class="tabbar-placeholder"></view>
    
    <!-- TabBar 主体 -->
    <view class="tabbar">
      <view 
        v-for="(item, index) in tabList" 
        :key="index" 
        class="tabbar-item"
        @click="switchTab(item, index)"
      >
        <view class="tabbar-icon">
          <SvgIcon 
            :name="item.icon" 
            size="48rpx"
            :color="currentIndex === index ? '#2C3E50' : '#8E8E93'"
          />
        </view>
        <text 
          class="tabbar-text"
          :class="{ 'tabbar-text-active': currentIndex === index }"
        >
          {{ item.text }}
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'

const props = defineProps({
  current: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['change'])

const currentIndex = computed(() => props.current)

// TabBar 配置
const tabList = [
  { 
    pagePath: '/pages/dashboard/index', 
    text: '概况',
    icon: 'home'
  },
  { 
    pagePath: '/pages/design/index', 
    text: '设计',
    icon: 'photo'
  },
  { 
    pagePath: '/pages/log/index', 
    text: '日志',
    icon: 'edit-pen'
  },
  { 
    pagePath: '/pages/profile/index', 
    text: '我的',
    icon: 'account'
  }
]

const switchTab = (item, index) => {
  if (currentIndex.value === index) return
  
  emit('change', index)
  
  uni.switchTab({
    url: item.pagePath
  })
}
</script>

<style lang="scss" scoped>
/* TabBar 容器 */
.tabbar-container {
  width: 100%;
}

/* 占位元素 */
.tabbar-placeholder {
  width: 100%;
  height: 100rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* TabBar 主体 */
.tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  display: flex;
  height: 100rpx;
  background-color: #ffffff;
  border-top: 1rpx solid #E5E5EA;
  box-shadow: 0 -1rpx 0 0 rgba(0, 0, 0, 0.05);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* TabBar 项目 */
.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8rpx 0;
  transition: opacity 0.2s ease;
}

.tabbar-item:active {
  opacity: 0.7;
}

/* 图标容器 */
.tabbar-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4rpx;
}

/* 文字样式 */
.tabbar-text {
  font-size: 20rpx;
  color: #8E8E93;
  line-height: 1.2;
  text-align: center;
  transition: color 0.3s ease;
}

.tabbar-text-active {
  color: #2C3E50;
  font-weight: 600;
}

/* 响应式适配 */
@media (max-width: 750rpx) {
  .tabbar {
    height: 90rpx;
  }
  
  .tabbar-placeholder {
    height: 90rpx;
  }
  
  .tabbar-icon {
    width: 44rpx;
    height: 44rpx;
  }
  
  .tabbar-text {
    font-size: 18rpx;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .tabbar {
    background-color: #1C1C1E;
    border-top-color: #38383A;
  }
  
  .tabbar-text {
    color: #8E8E93;
  }
  
  .tabbar-text-active {
    color: #FFFFFF;
  }
}
</style>