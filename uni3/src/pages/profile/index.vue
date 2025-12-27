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
              <view class="expense-value">
                <text class="value-number">{{ formatAmount(item.value).number }}</text>
                <text class="value-unit">{{ formatAmount(item.value).unit }}</text>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 底部按钮区域 -->
        <view class="bottom-buttons">
          <view class="action-btn" @click="handleContact">
            <text>联系客服</text>
          </view>
          <view class="action-btn" @click="handleAbout">
            <text>关于我们</text>
          </view>
          
          <!-- 品牌突出按钮 
          <view class="brand-btn" @click="handleBrand">
            <view class="brand-btn-bg">
              <view class="brand-bubble bubble-1"></view>
              <view class="brand-bubble bubble-2"></view>
            </view>
            <image class="brand-logo" src="@/styles/logo.png" mode="aspectFit" />
            <view class="brand-btn-text">
              <text class="brand-title">了解品牌</text>
              <text class="brand-subtitle">探索我们的故事</text>
            </view>
            <view class="brand-arrow">›</view>
          </view>
          -->
          <view class="action-btn" @click="handleLogout">
            <text>退出登录</text>
          </view>
        </view>
      </view>
    </scroll-view>
    
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
const projects = ref([])
const currentProjectIndex = ref(0)
const currentProject = computed(() => projects.value[currentProjectIndex.value] || null)

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 费用统计（测试数据）
const expenseList = ref([
  { label: '设计合同', value: 15000, url: 'https://docs.qq.com/sheet/DYmFxS0VYRFNWTVNP?tab=BB08J2' }, // url 预留，后续从接口获取
  { label: '工程合同', value: 85000, url: 'https://docs.qq.com/sheet/DYmFxS0VYRFNWTVNP?tab=BB08J2' },
  { label: '门窗合同', value: 32000, url: 'https://docs.qq.com/sheet/DYmFxS0VYRFNWTVNP?tab=BB08J2' },
  { label: '柜体合同', value: 48000, url: 'https://docs.qq.com/sheet/DYmFxS0VYRFNWTVNP?tab=BB08J2' }
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

// 格式化金额（过万显示为1.xx万或100万）- 返回数字和单位分开
const formatAmount = (amount) => {
  if (amount >= 1000000) {
    const wan = Math.floor(amount / 10000)
    return { number: wan, unit: '万' }
  } else if (amount >= 10000) {
    const wan = amount / 10000
    // 保留两位小数，去掉末尾的0
    const formatted = wan.toFixed(2).replace(/\.?0+$/, '')
    return { number: formatted, unit: '万' }
  } else {
    return { number: `¥${amount.toLocaleString()}`, unit: '' }
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

// 联系客服
const handleContact = () => {
  uni.navigateTo({
    url: '/pages/contact/index'
  })
}

// 关于我们
const handleAbout = () => {
  uni.navigateTo({
    url: '/pages/brand/index'
  })
}

// 品牌页面
const handleBrand = () => {
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
  background: $color-white;
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
  padding: 0 16rpx;
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
  min-height: 180rpx;
  box-sizing: border-box;
  cursor: pointer;
  background: linear-gradient(145deg, $color-white 0%, $color-gray-50 100%);
  border: 2rpx solid $color-border-light;
  border-radius: $radius-xl;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  
  // 装饰角标
  &::before {
    content: '';
    position: absolute;
    top: -20rpx;
    right: -20rpx;
    width: 80rpx;
    height: 80rpx;
    background: $color-brand-50;
    border-radius: 50%;
    opacity: 0.6;
    transition: all 0.3s ease;
  }
  
  // 底部装饰线
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 4rpx;
    background: linear-gradient(90deg, $color-brand 0%, $color-brand-600 100%);
    border-radius: 4rpx;
    transition: width 0.3s ease;
  }
  
  &:active {
    transform: scale(0.96);
    border-color: $color-brand-200;
    
    &::before {
      transform: scale(1.5);
      opacity: 0.8;
    }
    
    &::after {
      width: 60%;
    }
  }
}

.expense-label {
  font-size: 26rpx;
  color: $color-text-tertiary;
  margin-bottom: 16rpx;
  position: relative;
  z-index: 1;
}

.expense-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  position: relative;
  z-index: 1;
  
  .value-number {
    font-size: 40rpx;
    font-weight: 700;
    // 数字光泽效果
    background: linear-gradient(135deg, $color-brand 0%, $color-brand-600 50%, $color-brand 100%);
    background-size: 200% 100%;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .value-unit {
    font-size: 24rpx;
    font-weight: 500;
    color: $color-brand;
    margin-left: 4rpx;
    opacity: 0.8;
  }
}

// 底部按钮区域
.bottom-buttons {
  margin-top: 48rpx;
  margin-bottom: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding: 0 16rpx;
}

.action-btn {
  width: 100%;
  height: 96rpx;
  background: $color-white;
  border: 2rpx solid $color-border;
  border-radius: $radius-xl;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  color: $color-text-primary;
  font-weight: 500;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  
  // 左侧装饰条
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 6rpx;
    height: 0;
    background: linear-gradient(180deg, $color-brand 0%, $color-brand-600 100%);
    border-radius: 0 6rpx 6rpx 0;
    transition: height 0.3s ease;
  }
  
  
  &:active {
    transform: scale(0.98);
    background: $color-gray-50;
    border-color: $color-brand-200;
    
    &::before {
      height: 60%;
    }
    
    &::after {
      right: 24rpx;
      color: $color-brand;
    }
  }
  
  // 退出登录按钮特殊样式
  &:last-child {
    margin-top: 16rpx;
    background: linear-gradient(135deg, $color-brand 0%, $color-brand-600 100%);
    border: none;
    color: $color-white;
    
    &::before {
      display: none;
    }
    
    &::after {
      color: $color-white-alpha-60;
    }
    
    &:active {
      background: linear-gradient(135deg, $color-brand-600 0%, $color-brand-700 100%);
      
      &::after {
        color: $color-white;
      }
    }
  }
}

// 品牌突出按钮
.brand-btn {
  width: 100%;
  height: 140rpx;
  background: linear-gradient(135deg, $color-brand 0%, $color-brand-700 100%);
  border-radius: $radius-2xl;
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  margin: 16rpx 0;
  
  &:active {
    transform: scale(0.98);
  }
}

.brand-btn-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.brand-bubble {
  position: absolute;
  border-radius: 50%;
  background: $color-white-alpha-10;
  
  &.bubble-1 {
    width: 120rpx;
    height: 120rpx;
    top: -40rpx;
    right: 80rpx;
    animation: brandFloat1 4s ease-in-out infinite;
  }
  
  &.bubble-2 {
    width: 80rpx;
    height: 80rpx;
    bottom: -30rpx;
    right: 200rpx;
    animation: brandFloat2 5s ease-in-out infinite;
  }
}

@keyframes brandFloat1 {
  0%, 100% { transform: translate(0, 0) scale(1); opacity: 0.6; }
  50% { transform: translate(-10rpx, 15rpx) scale(1.1); opacity: 0.8; }
}

@keyframes brandFloat2 {
  0%, 100% { transform: translate(0, 0) scale(1); opacity: 0.5; }
  50% { transform: translate(15rpx, -10rpx) scale(1.15); opacity: 0.7; }
}

.brand-logo {
  width: 60rpx;
  height: 100rpx;
  margin-right: 24rpx;
  flex-shrink: 0;
  filter: drop-shadow(0 4rpx 8rpx rgba(0, 0, 0, 0.2));
}

.brand-btn-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.brand-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $color-white;
}

.brand-subtitle {
  font-size: 24rpx;
  color: $color-white-alpha-80;
}

.brand-arrow {
  font-size: 48rpx;
  color: $color-white-alpha-60;
  margin-left: 16rpx;
  transition: all 0.3s ease;
}

// 联系客户弹窗
.contact-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(16rpx);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.dialog-content {
  background: $color-white;
  border-radius: $radius-2xl;
  padding: 48rpx 32rpx;
  width: calc(100% - 64rpx);
  max-width: 600rpx;
  position: relative;
  box-shadow: 0 24rpx 64rpx rgba(0, 0, 0, 0.15);
  margin: 0 auto;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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
  background: $color-gray-100;
  font-size: 40rpx;
  color: $color-text-tertiary;
  line-height: 1;
  transition: all 0.3s ease;
  
  &:active {
    background: $color-gray-200;
    transform: scale(0.95);
  }
}

.dialog-title {
  text-align: center;
  font-size: 36rpx;
  font-weight: 700;
  color: $color-text-primary;
  margin-bottom: 32rpx;
}

.qr-code-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 32rpx;
  padding: 24rpx;
  background: $color-gray-50;
  border-radius: $radius-xl;
  border: 2rpx dashed $color-border;
}

.qr-code-image {
  width: 360rpx;
  height: 360rpx;
  border-radius: $radius-l;
}

.contact-methods {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.contact-method-item {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 24rpx;
  background: $color-gray-50;
  border-radius: $radius-xl;
  border: 2rpx solid $color-border-light;
  transition: all 0.3s ease;
  
  &:active {
    background: $color-brand-50;
    border-color: $color-brand-200;
    transform: scale(0.98);
  }
}

.method-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  background: $color-white;
  border-radius: $radius-l;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
}

.method-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.method-label {
  font-size: 24rpx;
  color: $color-text-tertiary;
}

.method-value {
  font-size: 28rpx;
  color: $color-text-primary;
  font-weight: 600;
}
</style>

