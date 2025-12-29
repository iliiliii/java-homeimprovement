<template>
  <view class="login-container">
    <!-- 顶部装饰背景 -->
    <view class="header-bg">
      <view class="bg-blob blob-1"></view>
      <view class="bg-blob blob-2"></view>
    </view>
    
    <!-- Logo 区域 -->
    <view class="logo-section">
      <view class="logo-wrapper">
        <image 
          class="logo-image" 
          src="@/styles/logo.png" 
          mode="aspectFit"
        />
      </view>
       <!-- 
      <text class="app-title">{{ APP_CONFIG.name }}</text>
      <text class="app-subtitle">{{ APP_CONFIG.subtitle }}</text>
      -->
    </view>
    
    <!-- 登录表单 -->
    <view class="glass-card login-form">
      <!-- 微信一键登录 -->
      <view v-if="loginMode === 'wechat'" class="login-mode-content">
        <view v-if="false" class="mode-header">
          <view class="mode-title">微信一键登录</view>
          <view class="mode-desc">快速安全，无需输入密码</view>
        </view>
        
        <button 
          class="glass-btn primary-btn wechat-btn" 
          open-type="getPhoneNumber" 
          @getphonenumber="handleWechatLogin"
        >
          <SvgIcon name="brand-wechat" size="42rpx" style="margin-right: 12rpx;" color="#fff"/>
          微信一键登录
        </button>
        
        <view class="switch-mode">
          <text @click="switchLoginMode('sms')">使用短信验证码登录</text>
          <text class="divider">|</text>
          <text @click="switchLoginMode('password')">使用密码登录</text>
        </view>
      </view>
      
      <!-- 短信验证码登录 -->
      <view v-if="loginMode === 'sms'" class="login-mode-content">
        <view class="form-item">
          <input 
            type="tel" 
            class="glass-input" 
            v-model="smsForm.phone"
            placeholder="请输入手机号码"
            maxlength="11"
          />
        </view>
        
        <view class="form-item">
          <view class="code-input-wrapper">
            <input 
              type="number" 
              class="glass-input code-input" 
              v-model="smsForm.code"
              placeholder="请输入验证码"
              maxlength="6"
            />
            <button 
              class="code-btn" 
              @click="handleSendCode"
              :disabled="countdown > 0"
            >
              {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
            </button>
          </view>
        </view>
        
        <button 
          class="glass-btn secondary-btn" 
          @click="handleSmsLogin"
          :loading="loading"
        >
          登录
        </button>
        
        <view class="switch-mode">
          <text @click="switchLoginMode('wechat')">使用微信登录</text>
          <text class="divider">|</text>
          <text @click="switchLoginMode('password')">使用密码登录</text>
        </view>
      </view>
      
      <!-- 密码登录 -->
      <view v-if="loginMode === 'password'" class="login-mode-content">
        <view class="form-item">
          <input 
            type="tel" 
            class="glass-input" 
            v-model="passwordForm.phone"
            placeholder="请输入手机号码"
            maxlength="11"
          />
        </view>
        
        <view class="form-item">
          <input 
            type="password" 
            class="glass-input" 
            v-model="passwordForm.password"
            placeholder="请输入密码"
          />
        </view>
        
        <button 
          class="glass-btn secondary-btn" 
          @click="handlePasswordLogin"
          :loading="loading"
        >
          登录
        </button>
        
        <view class="switch-mode">
          <text @click="switchLoginMode('wechat')">使用微信登录</text>
          <text class="divider">|</text>
          <text @click="switchLoginMode('sms')">使用短信验证码登录</text>
        </view>
      </view>

      <!-- 协议勾选 -->
      <view class="agreement-section" @click="toggleAgreement">
        <view class="checkbox" :class="{ checked: agreed }">
          <SvgIcon name="check" color="#fff" size="20rpx" v-if="agreed" />
        </view>
        <text class="agreement-text">
          我已阅读并同意 
          <text class="link" @click.stop="openProtocol">《用户协议》</text> 
          和 
          <text class="link" @click.stop="openPrivacy">《隐私政策》</text>
        </text>
      </view>
    </view>
    
    <!-- 开发者快速入口 -->
    <!-- #ifdef MP-WEIXIN -->
    <view class="dev-mode" v-if="showDevMode">
      <view class="dev-mode-header" @click="toggleDevPanel">
        <text>🔧 开发者模式</text>
        <text class="arrow">{{ devPanelOpen ? '▼' : '▶' }}</text>
      </view>
      
      <view class="dev-mode-panel" v-if="devPanelOpen">
        <view class="dev-mode-tips">
          <text>⚠️ 仅用于开发调试，生产环境请移除</text>
        </view>
        
        <view class="dev-mode-buttons">
          <button class="dev-btn customer-btn" @click="devLoginAsCustomer">
            👤 模拟客户登录
          </button>
          <button class="dev-btn staff-btn" @click="devLoginAsStaff">
            👨‍💼 模拟员工登录
          </button>
          <button class="dev-btn skip-btn" @click="devSkipToHome">
            🚀 直接进入首页
          </button>
        </view>
      </view>
    </view>
    <!-- #endif -->
    
    <!-- 版权信息 -->
    <view class="copyright">
      <text>{{ APP_CONFIG.copyright.text }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/user'
import { wechatLogin, smsLogin, passwordLogin, sendCode } from '@/api/auth'
import { getDeviceId } from '@/utils/device'
import SvgIcon from '@/components/SvgIcon.vue'
import { APP_CONFIG } from '@/config/app.js'

const userStore = useUserStore()

// 登录模式：wechat | sms | password
const loginMode = ref('wechat')
const loading = ref(false)
const agreed = ref(false)

// 开发者模式
const showDevMode = ref(false)  // 开发时设为true，生产时设为false
const devPanelOpen = ref(false)

// 短信登录表单
const smsForm = reactive({
  phone: '',
  code: ''
})

// 密码登录表单
const passwordForm = reactive({
  phone: '',
  password: ''
})

// 验证码倒计时
const countdown = ref(0)
let countdownTimer = null

onMounted(() => {
  // 尝试静默登录
  attemptSilentLogin()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})

// 尝试静默登录
const attemptSilentLogin = async () => {
  const token = uni.getStorageSync('token')
  if (token) {
    // 如果有token，验证是否有效
    try {
      await userStore.validateToken()
      // token有效，跳转首页
      navigateToHome()
    } catch (error) {
      // token无效，清除
      userStore.logout()
    }
  }
}

// 切换登录方式
const switchLoginMode = (mode) => {
  loginMode.value = mode
}

// 协议勾选
const toggleAgreement = () => {
  agreed.value = !agreed.value
}

const openProtocol = () => {
  uni.navigateTo({ url: '/pages/protocol/index' })
}

const openPrivacy = () => {
  uni.navigateTo({ url: '/pages/privacy/index' })
}

// ==================== 微信登录 ====================
const handleWechatLogin = async (e) => {
  if (!agreed.value) {
    uni.showToast({ title: '请阅读并同意用户协议和隐私政策', icon: 'none' })
    return
  }

  if (e.detail.errMsg === 'getPhoneNumber:ok') {
    const phoneCode = e.detail.code
    
    try {
      loading.value = true
      uni.showLoading({ title: '登录中...', mask: true })
      
      // 1. 获取微信登录凭证
      const loginRes = await uni.login({ provider: 'weixin' })
      const wxCode = loginRes.code
      
      // 2. 调用后端登录接口
      const result = await wechatLogin({
        code: wxCode,
        phoneCode: phoneCode,
        deviceId: getDeviceId()
      })
      
      // 3. 保存登录信息
      userStore.setLoginInfo(result)
      
      // 4. 跳转首页
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        navigateToHome()
      }, 500)
      
    } catch (error) {
      console.error('微信登录失败', error)
      uni.showToast({ 
        title: error.message || '登录失败，请重试', 
        icon: 'none' 
      })
    } finally {
      loading.value = false
      uni.hideLoading()
    }
  } else if (e.detail.errMsg === 'getPhoneNumber:fail user deny') {
    uni.showToast({ title: '需要授权手机号才能登录', icon: 'none' })
  } else {
    uni.showToast({ title: '获取手机号失败', icon: 'none' })
  }
}

// ==================== 短信验证码登录 ====================
const handleSendCode = async () => {
  if (!smsForm.phone) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(smsForm.phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  
  if (countdown.value > 0) {
    return
  }
  
  try {
    await sendCode(smsForm.phone)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    
    // 开始倒计时
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000)
  } catch (error) {
    uni.showToast({ 
      title: error.message || '发送失败，请重试', 
      icon: 'none' 
    })
  }
}

const handleSmsLogin = async () => {
  if (!agreed.value) {
    uni.showToast({ title: '请阅读并同意用户协议和隐私政策', icon: 'none' })
    return
  }
  
  if (!smsForm.phone) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(smsForm.phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  
  if (!smsForm.code) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  
  try {
    loading.value = true
    
    const result = await smsLogin({
      phone: smsForm.phone,
      code: smsForm.code,
      deviceId: getDeviceId()
    })
    
    userStore.setLoginInfo(result)
    
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      navigateToHome()
    }, 500)
    
  } catch (error) {
    uni.showToast({ 
      title: error.message || '登录失败，请重试', 
      icon: 'none' 
    })
  } finally {
    loading.value = false
  }
}

// ==================== 密码登录 ====================
const handlePasswordLogin = async () => {
  if (!agreed.value) {
    uni.showToast({ title: '请阅读并同意用户协议和隐私政策', icon: 'none' })
    return
  }
  
  if (!passwordForm.phone) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(passwordForm.phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  
  if (!passwordForm.password) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  
  try {
    loading.value = true
    
    const result = await passwordLogin({
      phone: passwordForm.phone,
      password: passwordForm.password,
      deviceId: getDeviceId()
    })
    
    userStore.setLoginInfo(result)
    
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      navigateToHome()
    }, 500)
    
  } catch (error) {
    uni.showToast({ 
      title: error.message || '登录失败，请重试', 
      icon: 'none' 
    })
  } finally {
    loading.value = false
  }
}

// 跳转首页
const navigateToHome = () => {
  uni.switchTab({ url: '/pages/dashboard/index' })
}

// ==================== 开发者模式 ====================
const toggleDevPanel = () => {
  devPanelOpen.value = !devPanelOpen.value
}

// 模拟客户登录
const devLoginAsCustomer = () => {
  const mockData = {
    accessToken: 'dev_customer_token_' + Date.now(),
    refreshToken: 'dev_customer_refresh_token_' + Date.now(),
    expiresIn: 7200,
    userType: 'customer',
    userInfo: {
      id: 'C001',
      name: '张三（测试客户）',
      phone: '138****8000',
      avatar: ''
    },
    projects: [
      {
        id: 'P001',
        code: 'P2025001',
        name: '万科城市花园A栋1001',
        status: 'construction',
        phase: 'construction'
      },
      {
        id: 'P002',
        code: 'P2025002',
        name: '碧桂园B栋2002',
        status: 'design',
        phase: 'design'
      }
    ]
  }
  
  userStore.setLoginInfo(mockData)
  uni.showToast({ title: '已模拟客户登录', icon: 'success' })
  
  setTimeout(() => {
    navigateToHome()
  }, 500)
}

// 模拟员工登录
const devLoginAsStaff = () => {
  const mockData = {
    accessToken: 'dev_staff_token_' + Date.now(),
    refreshToken: 'dev_staff_refresh_token_' + Date.now(),
    expiresIn: 7200,
    userType: 'staff',
    userInfo: {
      id: 'S001',
      name: '李工（测试员工）',
      phone: '139****9000',
      avatar: ''
    },
    projects: [
      {
        id: 'P001',
        code: 'P2025001',
        name: '万科城市花园A栋1001',
        status: 'construction',
        phase: 'construction'
      },
      {
        id: 'P003',
        code: 'P2025003',
        name: '恒大御景C栋3003',
        status: 'construction',
        phase: 'construction'
      }
    ]
  }
  
  userStore.setLoginInfo(mockData)
  uni.showToast({ title: '已模拟员工登录', icon: 'success' })
  
  setTimeout(() => {
    navigateToHome()
  }, 500)
}

// 直接跳转首页（不设置登录状态）
const devSkipToHome = () => {
  // 设置最小登录状态，避免路由守卫拦截
  const mockData = {
    accessToken: 'dev_skip_token_' + Date.now(),
    refreshToken: 'dev_skip_refresh_token_' + Date.now(),
    expiresIn: 7200,
    userType: 'customer',
    userInfo: {
      id: 'DEV001',
      name: '开发者',
      phone: '100****0000',
      avatar: ''
    },
    projects: [
      {
        id: 'DEV_P001',
        code: 'DEV2025001',
        name: '开发测试项目',
        status: 'construction',
        phase: 'construction'
      }
    ]
  }
  
  userStore.setLoginInfo(mockData)
  uni.showToast({ title: '已跳过登录', icon: 'success' })
  
  setTimeout(() => {
    navigateToHome()
  }, 500)
}
</script>


<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, $color-gray-50 0%, $color-white 100%);
  position: relative;
  overflow: hidden;
}

// 顶部装饰背景
.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 400rpx;
  background: linear-gradient(135deg, $color-yee-400 0%, $color-yee-800 100%);
  border-radius: 0 0 60rpx 60rpx;
  z-index: 0;
  
  &::after {
    content: '';
    position: absolute;
    bottom: -40rpx;
    left: 50%;
    transform: translateX(-50%);
    width: 80%;
    height: 80rpx;
    background: linear-gradient(135deg, $color-yee-500 0%, $color-yee-700 100%);
    filter: blur(40rpx);
    opacity: 0.3;
  }
}

// 背景装饰球
.bg-blob {
  position: absolute;
  border-radius: 50%;
  z-index: 1;
}

.blob-1 {
  width: 300rpx;
  height: 300rpx;
  background: $color-white-alpha-10;
  top: -80rpx;
  right: -60rpx;
}

.blob-2 {
  width: 200rpx;
  height: 200rpx;
  background: $color-white-alpha-10;
  top: 200rpx;
  left: -60rpx;
}

// Logo 区域
.logo-section {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 180rpx;
  padding-bottom: 40rpx;
}

.logo-wrapper {
  position: relative;
  width: 180rpx;
  height: 350rpx;
  overflow: visible;
  animation: float 3s ease-in-out infinite;
  
  // 底部阴影（模拟地面投影）
  &::after {
    content: '';
    position: absolute;
    bottom: -40rpx;
    left: 50%;
    transform: translateX(-50%);
    width: 140rpx;
    height: 20rpx;
    background: radial-gradient(ellipse, rgba(0, 0, 0, 0.5) 0%, rgba(0, 0, 0, 0) 70%);
    animation: shadow 3s ease-in-out infinite;
  }
}

.logo-image {
  width: 180rpx;
  height: 350rpx;
  filter: 
    drop-shadow(0 0 3rpx rgba(255, 255, 255, 1))
    drop-shadow(0 0 8rpx rgba(255, 255, 255, 0.6))
    drop-shadow(0 0 16rpx rgba(196, 0, 22, 0.6))
    drop-shadow(0 0 32rpx rgba(196, 0, 22, 0.4))
    drop-shadow(0 12rpx 24rpx rgba(196, 0, 22, 0.35))
    drop-shadow(0 24rpx 48rpx rgba(0, 0, 0, 0.5))
    drop-shadow(0 48rpx 80rpx rgba(0, 0, 0, 0.4));
}

// 悬浮动画
@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-16rpx);
  }
}

// 阴影跟随动画
@keyframes shadow {
  0%, 100% {
    width: 140rpx;
    opacity: 1;
  }
  50% {
    width: 120rpx;
    opacity: 0.6;
  }
}

.app-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: $color-white;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
  margin-bottom: 8rpx;
}

.app-subtitle {
  display: block;
  font-size: 26rpx;
  color: $color-white-alpha-80;
}

// 登录表单
.login-form {
  position: relative;
  z-index: 2;
  margin: 0 48rpx;
  margin-top: 40rpx;
  background: $color-white;
  border-radius: $radius-2xl;
  padding: $spacing-2xl;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.08);
  
  .login-mode-content {
    margin-bottom: 24rpx;
  }
  
  .mode-header {
    text-align: center;
    margin-bottom: 40rpx;
  }
  
  .mode-title {
    font-size: 36rpx;
    font-weight: 600;
    color: $color-text-primary;
    margin-bottom: 12rpx;
  }
  
  .mode-desc {
    font-size: 26rpx;
    color: $color-text-tertiary;
  }
  
  .form-item {
    margin-bottom: 28rpx;
  }
  
  .glass-input {
    width: 100%;
    box-sizing: border-box;
    padding: 24rpx 28rpx;
    height: 88rpx;
    background: $color-gray-50;
    border: 2rpx solid $color-border;
    border-radius: $radius-l;
    font-size: 28rpx;
    transition: all 0.3s ease;
    
    &:focus {
      border-color: $color-brand;
      background: $color-white;
      box-shadow: 0 0 0 4rpx rgba(196, 0, 22, 0.08);
    }
  }
  
  .code-input-wrapper {
    display: flex;
    gap: 16rpx;
  }
  
  .code-input {
    flex: 1;
  }
  
  .code-btn {
    padding: 0 24rpx;
    height: 88rpx;
    line-height: 88rpx;
    background: $color-yee-50;
    border: 2rpx solid $color-yee-200;
    border-radius: $radius-l;
    font-size: 24rpx;
    color: $color-yee-500;
    white-space: nowrap;
    font-weight: 500;
    
    &:disabled {
      opacity: 0.5;
      background: $color-gray-100;
      border-color: $color-border;
      color: $color-text-quaternary;
    }
  }
  
  .glass-btn {
    margin-top: 24rpx;
    padding: 0 48rpx;
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30rpx;
    font-weight: 600;
    border-radius: $radius-full;
    
    &.primary-btn {
      background: linear-gradient(135deg, $color-yee-500 0%, $color-yee-700 100%);
      border: none;
      color: $color-white;
      box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.3);
      
      &:active {
        transform: translateY(2rpx);
        box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.3);
      }
    }
    
    &.secondary-btn {
      background: linear-gradient(135deg, $color-yee-500 0%, $color-yee-700 100%);
      border: none;
      color: $color-white;
      box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.3);
      
      &:active {
        transform: translateY(2rpx);
      }
    }
  }
  
  .switch-mode {
    margin-top: 32rpx;
    text-align: center;
    font-size: 26rpx;
    color: $color-text-tertiary;
    
    text {
      color: $color-gray-800;
      
      &.divider {
        margin: 0 20rpx;
        color: $color-border-medium;
      }
    }
  }
}

// 协议勾选
.agreement-section {
  display: flex;
  align-items: flex-start;
  margin-top: 28rpx;
  padding: 0 8rpx;
}

.checkbox {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid $color-border-medium;
  border-radius: 8rpx;
  margin-right: 16rpx;
  margin-top: 2rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  flex-shrink: 0;
  
  &.checked {
    background: $color-brand;
    border-color: $color-brand;
  }
}

.agreement-text {
  font-size: 24rpx;
  color: $color-text-tertiary;
  flex: 1;
  line-height: 1.6;
}

.link {
  color: $color-gray-500;
  display: inline;
}

// 开发者模式 - 黑红白主题
.dev-mode {
  position: fixed;
  bottom: 120rpx;
  left: 32rpx;
  right: 32rpx;
  background: $color-black-alpha-80;
  border: 2rpx solid $color-brand-600;
  border-radius: 16rpx;
  overflow: hidden;
  backdrop-filter: blur(20rpx);
  z-index: 999;
}

.dev-mode-header {
  padding: 24rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: $color-brand-900;
  
  text {
    font-size: 28rpx;
    font-weight: 600;
    color: $color-white;
  }
  
  .arrow {
    font-size: 24rpx;
    color: $color-brand-300;
  }
}

.dev-mode-panel {
  padding: 24rpx;
}

.dev-mode-tips {
  padding: 16rpx 24rpx;
  background: $color-brand-900;
  border-radius: 12rpx;
  margin-bottom: 24rpx;
  
  text {
    font-size: 24rpx;
    color: $color-brand-200;
    line-height: 1.5;
  }
}

.dev-mode-buttons {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.dev-btn {
  padding: 24rpx 32rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
  
  &.customer-btn {
    background: linear-gradient(135deg, $color-gray-700 0%, $color-gray-800 100%);
    color: $color-white;
    border: 1rpx solid $color-gray-600;
  }
  
  &.staff-btn {
    background: linear-gradient(135deg, $color-brand-700 0%, $color-brand-800 100%);
    color: $color-white;
  }
  
  &.skip-btn {
    background: linear-gradient(135deg, $color-white 0%, $color-gray-100 100%);
    color: $color-gray-900;
  }
  
  &:active {
    opacity: 0.8;
    transform: scale(0.98);
  }
}

// 版权信息
.copyright {
  margin-top: auto;
  padding: 40rpx 0;
  padding-bottom: calc(40rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  text-align: center;
  font-size: 24rpx;
  color: $color-text-quaternary;
}
</style>
