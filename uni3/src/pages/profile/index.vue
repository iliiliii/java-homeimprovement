<template>
  <view class="profile-page">
    <!-- 固定头部区域 -->
    <view class="fixed-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <!-- 头部信息 -->
      <view class="header-content">
        <!-- 左侧头像 -->
        <UserAvatar 
          :avatar="userInfo.avatar" 
          :name="userInfo.name" 
          size="80rpx"
        />
        
        <!-- 用户信息 -->
        <view class="header-info">
          <text class="user-name">
            {{ userInfo.name || '未登录' }}
          </text>
          <text class="user-phone" v-if="userInfo.phone">
            {{ userInfo.phone }}
          </text>
        </view>
      </view>
    </view>
    
    <!-- 头部占位 -->
    <view class="header-placeholder" :style="{ height: headerHeight + 'px' }"></view>
    
    <!-- 可滚动内容区域 -->
    <scroll-view class="scroll-content" scroll-y>
      <view class="content-wrapper">
        <!-- 费用统计四宫格 -->
        <view class="expense-section">
          <view class="expense-grid">
            <view 
              v-for="(item, index) in expenseList" 
              :key="index"
              class="expense-item glass-card"
              @click="handleExpenseClick(item)"
            >
              <text class="expense-label">{{ item.label }}</text>
              <text class="expense-value">{{ formatAmount(item.value) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 底部按钮区域 -->
        <view class="bottom-buttons">
          <view class="glass-btn" @click="showContactDialog = true">
            <text>联系客服</text>
          </view>
          <view class="glass-btn glass-btn--secondary" @click="handleAbout">
            <text>关于我们</text>
          </view>
          <view class="glass-btn glass-btn--outline" @click="handleLogout">
            <text>退出登录</text>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 联系客服弹窗 --> 
    <view v-if="showContactDialog" class="contact-dialog" @click.self="showContactDialog = false">
      <view class="dialog-content" @click.stop>
        <!-- 关闭按钮 -->
        <view class="dialog-close" @click="showContactDialog = false">
          <text>×</text>
        </view>
        
        <!-- 标题 -->
        <view class="dialog-title">
          <text>联系客服</text>
        </view>
        
        <!-- 二维码 -->
        <view class="qr-code-container">
          <image 
            class="qr-code-image" 
            :src="contactInfo.qrCode" 
            mode="aspectFit"
          />
        </view>
        
        <!-- 联系方式 -->
        <view class="contact-methods">
          <view class="contact-method-item" @click="handleCall(contactInfo.phone)">
            <view class="method-icon">
              <text>📞</text>
            </view>
            <view class="method-info">
              <text class="method-label">电话</text>
              <text class="method-value">{{ contactInfo.phone }}</text>
            </view>
          </view>
          
          <view class="contact-method-item" @click="handleCopy(contactInfo.wechat)">
            <view class="method-icon">
              <text>💬</text>
            </view>
            <view class="method-info">
              <text class="method-label">微信</text>
              <text class="method-value">{{ contactInfo.wechat }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    <!-- Custom TabBar -->
    <CustomTabBar :current="3" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted, getCurrentInstance } from 'vue'
import { useUserStore } from '@/store/user.js'
import { getStatusBarHeight } from '@/utils/system.js'
import UserAvatar from '@/components/UserAvatar.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'

const userStore = useUserStore()

// 状态
const statusBarHeight = ref(0)
const headerHeight = ref(0)
const showContactDialog = ref(false)

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 联系信息（测试数据）
const contactInfo = ref({
  qrCode: 'https://via.placeholder.com/300x300?text=QR+Code', // 测试二维码图片
  phone: '400-123-4567',
  wechat: 'wechat_service_001'
})

// 费用统计（测试数据）
const expenseList = ref([
  { label: '设计费用', value: 15000, url: '' }, // url 预留，后续从接口获取
  { label: '工程费用', value: 85000, url: '' },
  { label: '门窗费用', value: 32000, url: '' },
  { label: '柜体费用', value: 48000, url: '' }
])

// 费用卡片点击事件（预留跳转到http页面）
const handleExpenseClick = (item) => {
  if (item.url) {
    // #ifdef MP-WEIXIN
    uni.navigateTo({
      url: `/pages/webview/index?url=${encodeURIComponent(item.url)}&title=${encodeURIComponent(item.label)}`
    })
    // #endif
    
    // #ifdef H5
    window.open(item.url, '_blank')
    // #endif
  } else {
    // 暂时显示提示，等接口数据返回后会自动跳转
    console.log('费用详情URL未配置:', item.label)
  }
}

// 格式化金额（过万显示为1.xx万或100万）
const formatAmount = (amount) => {
  if (amount >= 1000000) {
    const wan = Math.floor(amount / 10000)
    return `${wan}万`
  } else if (amount >= 10000) {
    const wan = amount / 10000
    // 保留两位小数，去掉末尾的0
    const formatted = wan.toFixed(2).replace(/\.?0+$/, '')
    return `${formatted}万`
  } else {
    return `¥${amount.toLocaleString()}`
  }
}

// 更新header高度
const updateHeaderHeight = () => {
  const query = uni.createSelectorQuery().in(getCurrentInstance())
  query.select('.fixed-header').boundingClientRect(rect => {
    if (rect && rect.height > 0) {
      headerHeight.value = rect.height + 24
    }
  }).exec()
}

// 拨打电话
const handleCall = (phone) => {
  uni.makePhoneCall({
    phoneNumber: phone,
    fail: (err) => {
      console.error('拨打电话失败:', err)
      uni.showToast({
        title: '拨打电话失败',
        icon: 'none'
      })
    }
  })
}

// 复制微信号
const handleCopy = (text) => {
  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({
        title: '已复制到剪贴板',
        icon: 'success'
      })
    },
    fail: () => {
      uni.showToast({
        title: '复制失败',
        icon: 'none'
      })
    }
  })
}

// 关于我们
const handleAbout = () => {
  uni.navigateTo({
    url: '/pages/brand/index'
  })
}

// 退出登录
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
      }
    }
  })
}

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  
  // 预估初始高度
  const screenWidth = uni.getSystemInfoSync().windowWidth
  const estimatedHeight = (200 / 750) * screenWidth + statusBarHeight.value
  headerHeight.value = estimatedHeight
  
  // 获取精确高度
  setTimeout(updateHeaderHeight, 200)
})
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: $glass-bg;
  padding-bottom: 140rpx; // 为底部TabBar留出空间
}

// 固定头部
.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: $glass-bg;
  padding-bottom: 16rpx;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 24rpx 32rpx;
}

.header-info {
  flex: 1;
  
  .user-name {
    display: block;
    font-size: 36rpx;
    font-weight: 700;
    color: $glass-text-main;
    margin-bottom: 4rpx;
  }
  
  .user-phone {
    display: block;
    font-size: 26rpx;
    color: $glass-text-muted;
  }
}

// 头部占位
.header-placeholder {
  width: 100%;
  flex-shrink: 0;
}

// 可滚动内容
.scroll-content {
  height: calc(100vh - 140rpx); // 减去头部高度
}

.content-wrapper {
  padding: 16rpx 32rpx;
  width: 100%;
}

// 费用统计区域
.expense-section {
  margin-bottom: 48rpx;
}

.expense-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
  width: 100%;
  box-sizing: border-box;
}

.expense-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32rpx 24rpx;
  text-align: center;
  min-height: 160rpx;
  box-sizing: border-box;
  cursor: pointer;
  transition: transform 0.2s ease;
  
  &:active {
    transform: scale(0.98);
  }
}

.expense-label {
  font-size: 26rpx;
  color: $glass-text-muted;
  margin-bottom: 16rpx;
}

.expense-value {
  font-size: 36rpx;
  font-weight: 700;
  color: $glass-accent;
}

// 底部按钮区域
.bottom-buttons {
  margin-top: 48rpx;
  margin-bottom: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding: 0;
}

// 联系客户弹窗
.contact-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10rpx);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
}

.dialog-content {
  background: $glass-surface;
  backdrop-filter: blur($blur-amount);
  -webkit-backdrop-filter: blur($blur-amount);
  border: 1rpx solid $glass-border;
  border-radius: $radius-l;
  padding: 48rpx 32rpx;
  width: calc(100% - 64rpx);
  max-width: 600rpx;
  position: relative;
  box-shadow: $shadow-glass;
  margin: 0 auto;
}

.dialog-close {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.05);
  font-size: 48rpx;
  color: $glass-text-muted;
  line-height: 1;
  
  &:active {
    background: rgba(0, 0, 0, 0.1);
  }
}

.dialog-title {
  text-align: center;
  font-size: 36rpx;
  font-weight: 700;
  color: $glass-text-main;
  margin-bottom: 32rpx;
}

.qr-code-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 32rpx;
  padding: 24rpx;
  background: white;
  border-radius: $radius-m;
}

.qr-code-image {
  width: 400rpx;
  height: 400rpx;
}

.contact-methods {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.contact-method-item {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 24rpx;
  background: $glass-surface-strong;
  border-radius: $radius-m;
  border: 1rpx solid $glass-border;
  
  &:active {
    background: rgba(201, 176, 212, 0.1);
  }
}

.method-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  background: white;
  border-radius: $radius-m;
  flex-shrink: 0;
}

.method-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.method-label {
  font-size: 24rpx;
  color: $glass-text-muted;
}

.method-value {
  font-size: 28rpx;
  color: $glass-text-main;
  font-weight: 600;
}
</style>

