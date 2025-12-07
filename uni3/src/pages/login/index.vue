<template>
  <view class="login-container">
    <!-- 背景装饰 -->
    <view class="bg-blob blob-1"></view>
    <view class="bg-blob blob-2"></view>
    
    <!-- Logo 区域 -->
    <view class="logo-section">
      <view class="icon-logo">
        <u-icon name="home" size="64" color="#fff" />
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
          <u-icon name="checkmark" color="#fff" size="20" v-if="form.rememberPassword" />
        </view>
        <text class="agreement-text">记住登录信息</text>
      </view>

      <!-- 协议勾选 -->
      <view class="agreement-section" @click="toggleAgreement">
        <view class="checkbox" :class="{ checked: form.agreed }">
          <u-icon name="checkmark" color="#fff" size="20" v-if="form.agreed" />
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
        <u-icon name="weixin-fill" color="#fff" size="24" style="margin-right: 12rpx" />
        微信一键登录
      </button>

      <button class="glass-btn secondary-btn" @click="handleLogin" :loading="loading">
        {{ isWechat ? '手机号登录' : '进入项目' }}
      </button>
    </view>
    
    <!-- 版权信息 -->
    <view class="copyright">
      <text>{{ APP_CONFIG.copyright.text }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { APP_CONFIG } from '@/config/app.js'

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
    
    // 跳转到首页
    uni.switchTab({
      url: '/pages/dashboard/index'
    })
  } catch (error) {
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const handleWechatLogin = (e) => {
  if (!form.agreed) {
    uni.showToast({ title: '请阅读并同意用户协议和隐私政策', icon: 'none' })
    return
  }

  if (e.detail.errMsg === 'getPhoneNumber:ok') {
    // 获取到 code，需要传给后端换取手机号
    const code = e.detail.code
    console.log('WeChat Code:', code)
    
    uni.showLoading({ title: '登录中...' })
    
    // 模拟登录
    setTimeout(() => {
      uni.hideLoading()
      uni.setStorageSync('userInfo', {
        phone: '13800138000', // 模拟手机号
        projectCode: 'P2025001' // 模拟项目编号
      })
      uni.switchTab({
        url: '/pages/dashboard/index'
      })
    }, 1000)
  } else {
    uni.showToast({ title: '获取手机号失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 64rpx;
  padding-top: 120rpx;
  position: relative;
  overflow: hidden;
}

// 背景装饰球
.bg-blob {
  position: absolute;
  width: 600rpx;
  height: 600rpx;
  background: $glass-accent;
  border-radius: 50%;
  filter: blur(160rpx);
  opacity: 0.15;
  z-index: -1;
}

.blob-1 {
  top: -100rpx;
  left: -100rpx;
}

.blob-2 {
  bottom: -100rpx;
  right: -100rpx;
  background: $glass-success;
}

// Logo 区域
.logo-section {
  margin-bottom: 80rpx;
}

.icon-logo {
  width: 128rpx;
  height: 128rpx;
  background: $glass-accent;
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 48rpx;
  box-shadow: 0 20rpx 40rpx rgba(45, 91, 255, 0.3);
}

.app-title {
  display: block;
  font-size: 56rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
  color: $glass-text-main;
}

.app-subtitle {
  display: block;
  font-size: 28rpx;
  color: $glass-text-muted;
}

// 登录表单
.login-form {
  .form-item {
    margin-bottom: 40rpx;
  }
  
  .form-label {
    display: block;
    margin-bottom: 16rpx;
    font-size: 28rpx;
    font-weight: 500;
    color: $glass-text-main;
  }
  
  .glass-input {
    width: 100%;
    box-sizing: border-box;
    padding: 22rpx 28rpx;
    height: auto;
  }
  
  .glass-btn {
    margin-top: 24rpx;
    padding: 18rpx 48rpx;
    height: auto;
    line-height: 1.4;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.primary-btn {
      background: #07c160;
      border: none;
      
      &:active {
        background: #06ad56; // darken(#07c160, 5%)
      }
    }
    
    &.secondary-btn {
      background: rgba(255, 255, 255, 0.4);
      border: 2rpx solid rgba(255, 255, 255, 0.6);
      color: $glass-text-main;
      
      &:active {
        background: rgba(255, 255, 255, 0.5);
      }
    }
  }
}

// 协议勾选
.agreement-section {
  display: flex;
  align-items: center;
  margin-bottom: 32rpx;
  padding: 0 8rpx;
}

.checkbox {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid $glass-text-muted;
  border-radius: 8rpx;
  margin-right: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  
  &.checked {
    background: $glass-accent;
    border-color: $glass-accent;
  }
}

.agreement-text {
  font-size: 24rpx;
  color: $glass-text-muted;
  flex: 1;
}

.link {
  color: $glass-accent;
  display: inline;
}

// 版权信息
.copyright {
  margin-top: auto;
  padding: 60rpx 0;
  padding-bottom: calc(60rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(60rpx + env(safe-area-inset-bottom));
  text-align: center;
  font-size: 24rpx;
  color: $glass-text-muted;
}
</style>

