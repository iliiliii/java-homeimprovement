<template>
  <view class="login-container">
    <!-- 背景装饰 -->
    <view class="bg-blob blob-1"></view>
    <view class="bg-blob blob-2"></view>
    
    <!-- Logo 区域 -->
    <view class="logo-section">
      <view class="icon-logo">
        <SvgIcon name="home" size="80rpx" color="#fff" />
      </view>
      <text class="app-title">{{ APP_CONFIG.name }}</text>
      <text class="app-subtitle">{{ APP_CONFIG.subtitle }}</text>
    </view>
    
    <!-- 登录表单 -->
    <view class="glass-card login-form">
      <view class="form-item">
        <text class="form-label">手机号码</text>
        <input 
          type="tel" 
          class="glass-input" 
          v-model="form.phone"
          placeholder="请输入手机号码"
          maxlength="11"
        />
      </view>
      
      <view class="form-item">
        <text class="form-label">项目编号</text>
        <input 
          type="text" 
          class="glass-input" 
          v-model="form.projectCode"
          placeholder="请输入项目编号"
        />
      </view>

      <!-- 记住密码 -->
      <view class="agreement-section" @click="toggleRemember">
        <view class="checkbox" :class="{ checked: form.rememberPassword }">
          <SvgIcon name="checkmark" color="#fff" size="20rpx" v-if="form.rememberPassword" />
        </view>
        <text class="agreement-text">记住登录信息</text>
      </view>

      <!-- 协议勾选 -->
      <view class="agreement-section" @click="toggleAgreement">
        <view class="checkbox" :class="{ checked: form.agreed }">
          <SvgIcon name="checkmark" color="#fff" size="20rpx" v-if="form.agreed" />
        </view>
        <text class="agreement-text">
          我已阅读并同意 <text class="link" @click.stop="openProtocol">《用户协议》</text> 和 <text class="link" @click.stop="openPrivacy">《隐私政策》</text>
        </text>
      </view>
      
      <button 
        class="glass-btn primary-btn" 
        open-type="getPhoneNumber" 
        @getphonenumber="handleWechatLogin"
        v-if="isWechat"
      >
        <SvgIcon name="weixin-fill" color="#fff" size="24rpx" style="margin-right: 12rpx" />
        微信一键登录
      </button>

      <button class="glass-btn secondary-btn" @click="handleLogin" :loading="loading">
        {{ isWechat ? '手机号登录' : '进入项目' }}
      </button>
    </view>
    
    <!-- 开发者快速入口 -->
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
    
    <!-- 版权信息 -->
    <view class="copyright">
      <text>{{ APP_CONFIG.copyright.text }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import SvgIcon from '@/components/SvgIcon.vue'
import { APP_CONFIG } from '@/config/app.js'

const userStore = useUserStore()

const loading = ref(false)
// #ifdef MP-WEIXIN
const isWechat = ref(true)
// #endif
// #ifndef MP-WEIXIN
const isWechat = ref(false)
// #endif

const form = reactive({
  phone: '',
  projectCode: '',
  agreed: false,
  rememberPassword: false
})

// 开发者模式
const showDevMode = ref(true)  // 开发时设为true，生产时设为false
const devPanelOpen = ref(false)

// 加载已保存的登录信息
onMounted(() => {
  const savedInfo = uni.getStorageSync('savedLoginInfo')
  if (savedInfo) {
    form.phone = savedInfo.phone || ''
    form.projectCode = savedInfo.projectCode || ''
    form.rememberPassword = true
  }
})

const toggleAgreement = () => {
  form.agreed = !form.agreed
}

const toggleRemember = () => {
  form.rememberPassword = !form.rememberPassword
  // 如果取消记住密码，清除已保存的信息
  if (!form.rememberPassword) {
    uni.removeStorageSync('savedLoginInfo')
  }
}

const openProtocol = () => {
  uni.navigateTo({
    url: '/pages/protocol/index'
  })
}

const openPrivacy = () => {
  uni.navigateTo({
    url: '/pages/privacy/index'
  })
}

const handleLogin = async () => {
  if (!form.phone) {
    uni.showToast({ title: '请输入手机号码', icon: 'none' })
    return
  }

  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    uni.showToast({ title: '请输入正确的手机号码', icon: 'none' })
    return
  }
  
  if (!form.projectCode) {
    uni.showToast({ title: '请输入项目编号', icon: 'none' })
    return
  }

  if (!form.agreed) {
    uni.showToast({ title: '请阅读并同意用户协议和隐私政策', icon: 'none' })
    return
  }
  
  loading.value = true
  
  try {
    // TODO: 调用登录接口
    // 模拟登录延迟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 保存登录信息到用户信息
    uni.setStorageSync('userInfo', {
      phone: form.phone,
      projectCode: form.projectCode
    })
    
    // 如果勾选了记住密码，保存登录信息
    if (form.rememberPassword) {
      uni.setStorageSync('savedLoginInfo', {
        phone: form.phone,
        projectCode: form.projectCode
      })
    } else {
      uni.removeStorageSync('savedLoginInfo')
    }
    
    // 跳转到首页 - 使用 reLaunch 确保页面重新初始化
    uni.reLaunch({
      url: '/pages/dashboard/index',
      success: () => {
        console.log('[自动登录] 页面跳转成功，将重新初始化')
      },
      fail: (error) => {
        console.error('[自动登录] 页面跳转失败:', error)
        // 降级使用 switchTab
        uni.switchTab({ url: '/pages/dashboard/index' })
      }
    })
  } catch (error) {
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const handleWechatLogin = async (e) => {
  if (!form.agreed) {
    uni.showToast({ title: '请阅读并同意用户协议和隐私政策', icon: 'none' })
    return
  }

  if (e.detail.errMsg !== 'getPhoneNumber:ok') {
    uni.showToast({ title: '获取手机号失败', icon: 'none' })
    return
  }

  // 获取手机号的code
  const phoneCode = e.detail.code
  console.log('Phone Code:', phoneCode)
  
  uni.showLoading({ title: '登录中...' })
  
  try {
    // 1. 先获取微信登录code
    const loginRes = await new Promise((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: resolve,
        fail: reject
      })
    })
    
    const wxCode = loginRes.code
    console.log('WeChat Login Code:', wxCode)
    
    // 2. 获取设备ID
    const { getDeviceId } = await import('@/utils/device')
    const deviceId = getDeviceId()
    
    // 3. 调用后端微信登录接口
    const { wechatLogin } = await import('@/api/auth')
    const result = await wechatLogin({
      code: wxCode,
      phoneCode: phoneCode,
      deviceId: deviceId
    })
    
    // 4. 保存登录信息
    userStore.setLoginInfo(result)
    
    uni.hideLoading()
    uni.showToast({ title: '登录成功', icon: 'success' })
    
    // 5. 跳转到首页 - 使用 reLaunch 确保页面重新初始化
    setTimeout(() => {
      uni.reLaunch({ 
        url: '/pages/dashboard/index',
        success: () => {
          console.log('[登录] 页面跳转成功，将重新初始化')
        },
        fail: (error) => {
          console.error('[登录] 页面跳转失败:', error)
          // 降级使用 switchTab
          uni.switchTab({ url: '/pages/dashboard/index' })
        }
      })
    }, 500)
    
  } catch (error) {
    uni.hideLoading()
    console.error('微信登录失败:', error)
    uni.showToast({ 
      title: error.message || '登录失败，请重试', 
      icon: 'none' 
    })
  }
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
    uni.reLaunch({ 
      url: '/pages/dashboard/index',
      success: () => {
        console.log('[模拟客户登录] 页面跳转成功')
      },
      fail: (error) => {
        console.error('[模拟客户登录] 页面跳转失败:', error)
        uni.switchTab({ url: '/pages/dashboard/index' })
      }
    })
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
    uni.reLaunch({ 
      url: '/pages/dashboard/index',
      success: () => {
        console.log('[模拟员工登录] 页面跳转成功')
      },
      fail: (error) => {
        console.error('[模拟员工登录] 页面跳转失败:', error)
        uni.switchTab({ url: '/pages/dashboard/index' })
      }
    })
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
    uni.reLaunch({ 
      url: '/pages/dashboard/index',
      success: () => {
        console.log('[跳过登录] 页面跳转成功')
      },
      fail: (error) => {
        console.error('[跳过登录] 页面跳转失败:', error)
        uni.switchTab({ url: '/pages/dashboard/index' })
      }
    })
  }, 500)
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: $spacing-3xl;
  padding-top: 120rpx;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, $color-black 0%, $color-gray-900 100%);
}

// 背景装饰球 - 黑红白主题
.bg-blob {
  position: absolute;
  width: 600rpx;
  height: 600rpx;
  border-radius: 50%;
  filter: blur(160rpx);
  opacity: 0.1;
  z-index: -1;
}

.blob-1 {
  top: -100rpx;
  left: -100rpx;
  background: $color-brand;
}

.blob-2 {
  bottom: -100rpx;
  right: -100rpx;
  background: $color-white;
  opacity: 0.05;
}

// Logo 区域
.logo-section {
  margin-bottom: 80rpx;
  text-align: center;
}

.icon-logo {
  width: 128rpx;
  height: 128rpx;
  background: linear-gradient(135deg, $color-brand 0%, $color-brand-600 100%);
  border-radius: $radius-2xl;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto $spacing-2xl;
  box-shadow: $shadow-btn;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    inset: -2rpx;
    background: linear-gradient(135deg, $color-brand, $color-white);
    border-radius: $radius-2xl;
    z-index: -1;
    opacity: 0.3;
  }
}

.app-title {
  display: block;
  font-size: 56rpx;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-m;
  color: $color-white;
  text-shadow: 0 2rpx 8rpx $color-black-alpha-40;
}

.app-subtitle {
  display: block;
  font-size: 28rpx;
  color: $color-white-alpha-60;
  font-weight: $font-weight-normal;
}

// 登录表单 - 玻璃态效果
.login-form {
  background: $color-white-alpha-10;
  backdrop-filter: blur($blur-amount);
  border: 1rpx solid $color-white-alpha-20;
  border-radius: $radius-2xl;
  padding: $spacing-2xl;
  box-shadow: $shadow-glass;
  
  .form-item {
    margin-bottom: $spacing-2xl;
  }
  
  .form-label {
    display: block;
    margin-bottom: $spacing-m;
    font-size: 28rpx;
    font-weight: $font-weight-medium;
    color: $color-white;
  }
  
  .glass-input {
    width: 100%;
    box-sizing: border-box;
    padding: 22rpx 28rpx;
    height: auto;
    background: $color-white-alpha-10;
    border: 1rpx solid $color-white-alpha-20;
    border-radius: $radius-l;
    color: $color-white;
    font-size: 28rpx;
    
    &::placeholder {
      color: $color-white-alpha-40;
    }
    
    &:focus {
      border-color: $color-brand;
      background: $color-white-alpha-20;
    }
  }
  
  .glass-btn {
    margin-top: $spacing-l;
    padding: 18rpx $spacing-2xl;
    height: auto;
    line-height: 1.4;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-full;
    font-weight: $font-weight-medium;
    transition: all 0.3s ease;
    
    &.primary-btn {
      background: linear-gradient(135deg, $color-brand 0%, $color-brand-600 100%);
      border: none;
      color: $color-white;
      box-shadow: $shadow-btn;
      
      &:active {
        transform: translateY(2rpx);
        box-shadow: $shadow-light;
      }
    }
    
    &.secondary-btn {
      background: $color-white-alpha-10;
      border: 1rpx solid $color-white-alpha-40;
      color: $color-white;
      
      &:active {
        background: $color-white-alpha-20;
        transform: translateY(2rpx);
      }
    }
  }
}

// 协议勾选
.agreement-section {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-l;
  padding: 0 $spacing-s;
}

.checkbox {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid $color-white-alpha-40;
  border-radius: $radius-s;
  margin-right: $spacing-m;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  
  &.checked {
    background: $color-brand;
    border-color: $color-brand;
    box-shadow: 0 0 0 4rpx rgba(196, 0, 22, 0.2);
  }
}

.agreement-text {
  font-size: 24rpx;
  color: $color-white-alpha-80;
  flex: 1;
  line-height: 1.5;
}

.link {
  color: $color-brand-300;
  display: inline;
  text-decoration: underline;
}

// 开发者模式 - 黑红白主题
.dev-mode {
  position: fixed;
  bottom: 120rpx;
  left: $spacing-l;
  right: $spacing-l;
  background: $color-black-alpha-80;
  border: 1rpx solid $color-brand-600;
  border-radius: $radius-l;
  overflow: hidden;
  backdrop-filter: blur($blur-amount);
  z-index: 999;
}

.dev-mode-header {
  padding: $spacing-l $spacing-l;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: $color-brand-900;
  
  text {
    font-size: 28rpx;
    font-weight: $font-weight-semibold;
    color: $color-white;
  }
  
  .arrow {
    font-size: 24rpx;
    color: $color-brand-300;
  }
}

.dev-mode-panel {
  padding: $spacing-l;
}

.dev-mode-tips {
  padding: $spacing-m $spacing-l;
  background: $color-brand-900;
  border-radius: $radius-m;
  margin-bottom: $spacing-l;
  
  text {
    font-size: 24rpx;
    color: $color-brand-200;
    line-height: 1.5;
  }
}

.dev-mode-buttons {
  display: flex;
  flex-direction: column;
  gap: $spacing-m;
}

.dev-btn {
  padding: $spacing-l $spacing-l;
  border-radius: $radius-m;
  font-size: 28rpx;
  font-weight: $font-weight-semibold;
  border: none;
  transition: all 0.3s ease;
  
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
  padding: 60rpx 0;
  padding-bottom: calc(60rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(60rpx + env(safe-area-inset-bottom));
  text-align: center;
  font-size: 24rpx;
  color: $color-white-alpha-40;
}
</style>

