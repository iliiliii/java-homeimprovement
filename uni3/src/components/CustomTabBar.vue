<template>
  <view class="tabbar-wrapper">
    <!-- 占位元素 -->
    <view class="tabbar-placeholder"></view>
    
    <!-- TabBar 主体 -->
    <view class="custom-tabbar">
      <view 
        v-for="(item, index) in tabList" 
        :key="index" 
        class="tab-item"
        @click="switchTab(item, index)"
      >
        <view class="tab-icon-box">
          <u-icon 
            :name="item.icon" 
            :size="22" 
            :color="currentIndex === index ? '#2D5BFF' : '#999999'"
          />
        </view>
        <text 
          class="tab-text"
          :style="{ color: currentIndex === index ? '#2D5BFF' : '#999999' }"
        >{{ item.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  current: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['change'])

const currentIndex = computed(() => props.current)

// 4个 Tab 配置
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
.tabbar-wrapper {
  width: 100%;
}

// 占位元素
.tabbar-placeholder {
  width: 100%;
  height: 100rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

// TabBar 主体
.custom-tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 100rpx;
  background: #ffffff !important;
  box-shadow: 0 -2rpx 20rpx rgba(0, 0, 0, 0.05);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

// Tab 项
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: transparent !important;
  border: none !important;
  
  &:active {
    opacity: 0.7;
  }
}

// Tab 图标容器
.tab-icon-box {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4rpx;
  background: transparent !important;
}

// Tab 文字
.tab-text {
  font-size: 22rpx;
  line-height: 1.2;
  background: transparent !important;
}
</style>
