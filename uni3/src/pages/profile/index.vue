<template>
  <view class="profile-page">
    <!-- 固定头部区域 -->
    <PageHeader 
      title="个人中心"
      :show-back="false"
    />
    <!-- 头部占位 -->
    <view class="header-placeholder" :style="{ height: headerHeight + 'px' }"></view>
    <!-- 项目概况卡片 -->
        <view class="project-brief-section" v-if="projects.length > 0">
          <ProjectCardSwiper
            :projects="projects"
            :user-info="userInfo"
            :current="currentProjectIndex"
            @update:current="index => currentProjectIndex = index"
            @change="handleSwiperChange"
            @click="handleCardClick"
          />
        </view>
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
          <view class="action-btn" @click="showContactDialog = true">
            <text>联系客服</text>
          </view>
          <view class="action-btn" @click="handleAbout">
            <text>关于我们</text>
          </view>
          <view class="action-btn" @click="handleLogout">
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
import { getCustomerDashboard } from '@/api/dashboard.js'
import UserAvatar from '@/components/UserAvatar.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'
import ProjectCardSwiper from '@/components/ProjectCardSwiper.vue'
import PageHeader from '@/components/PageHeader.vue'

const userStore = useUserStore()

// 状态
const headerHeight = ref(0)
const showContactDialog = ref(false)
const projects = ref([])
const currentProjectIndex = ref(0)
const currentProject = computed(() => projects.value[currentProjectIndex.value] || null)

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

// 加载项目数据
const loadProjectData = async () => {
  try {
    const data = await getCustomerDashboard()
    const list = data?.projects || []
    projects.value = list
    
    if (list.length > 0) {
      // 优先使用本地存储的选中项目
      const savedProjectId = uni.getStorageSync('currentProjectId')
      let index = 0
      if (savedProjectId) {
         index = list.findIndex(p => p.id === savedProjectId)
         if (index === -1) index = 0
      }
      currentProjectIndex.value = index
    }
  } catch (error) {
    console.error('获取项目数据失败:', error)
  }
}

// 切换项目
const handleSwiperChange = (index) => {
  if (index !== currentProjectIndex.value) {
    currentProjectIndex.value = index
  }
}

// 点击项目卡片
const handleCardClick = (project) => {
  // 保存当前选中的项目ID
  if (project) {
     uni.setStorageSync('currentProjectId', project.id)
  }
  
  // 跳转到首页并选中该项目
  uni.switchTab({
    url: '/pages/dashboard/index'
  })
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
  query.select('.page-header').boundingClientRect(rect => {
    if (rect && rect.height > 0) {
      // 头部高度 + 间距，确保内容不被遮挡
      headerHeight.value = rect.height
    } else {
       // 如果获取失败，使用默认高度
    if (!headerHeight.value) {
      headerHeight.value = (uni.getSystemInfoSync().statusBarHeight || 20) + 56
    }
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
  // statusBarHeight removed
  
  // 预估初始高度 (status bar + 44)
  const sys = uni.getSystemInfoSync()
  headerHeight.value = (sys.statusBarHeight || 20) + 56
  
  // 获取精确高度
  setTimeout(updateHeaderHeight, 200)
  
  // 加载项目数据
  loadProjectData()
})
</script>

<style lang="scss" scoped>
.profile-page {
  height: 100vh;
  background: $glass-bg;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 固定头部样式已移入PageHeader
// 自定义头部用户信息样式
.header-user-info {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.user-text-info {
  display: flex;
  flex-direction: column;
  
  .user-name {
    font-size: 32rpx;
    font-weight: 700;
    color: $glass-text-main;
    line-height: 1.2;
  }
  
  .user-phone {
    font-size: 24rpx;
    color: $glass-text-muted;
    margin-top: 4rpx;
  }
}

// 头部占位
.header-placeholder {
  width: 100%;
  flex-shrink: 0;
}

// 可滚动内容
.scroll-content {
  flex: 1;
  height: 0; // 关键：让scroll-view在flex容器中正确滚动
  width: 100%;
}

.content-wrapper {
  padding: 16rpx 32rpx 140rpx; // 底部padding移到这里，防止被TabBar遮挡
  width: 100%;
  box-sizing: border-box;
}

// 项目概况区域
.project-brief-section {
  margin-bottom: 32rpx;
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

.action-btn {
  width: 100%;
  height: 100rpx;
  background: white;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  color: $glass-text-main;
  font-weight: 500;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.03);
  transition: all 0.2s;
  
  &:active {
    transform: scale(0.98);
    background: #f9f9f9;
  }
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

