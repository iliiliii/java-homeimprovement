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
      <!-- 检查中状态 -->
      <view v-if="bindingStep === 'check'" class="login-mode-content">
        <view class="loading-state">
          <view class="loading-spinner"></view>
          <text class="loading-text">正在检查登录状态...</text>
        </view>
      </view>
      
      <!-- 微信登录 -->
      <view v-else-if="bindingStep === 'normal'" class="login-mode-content">
        
        <!-- 微信登录按钮 -->
        <button 
          class="glass-btn primary-btn wechat-btn" 
          @click="handleWechatLogin"
          :disabled="loading"
        >
          <SvgIcon name="brand-wechat" size="42rpx" style="margin-right: 12rpx;" color="#fff"/>
          {{ loading ? '登录中...' : '微信登录' }}
        </button>
                <!-- 微信登录按钮 -->
        <button 
          class="back-btn" 
          @click="handleBack"
        >
          返回上一页
        </button>
        
        <view v-if="false" class="switch-mode">
          <text @click="switchLoginMode('sms')">使用短信验证码登录</text>
          <text class="divider">|</text>
          <text @click="switchLoginMode('password')">使用密码登录</text>
        </view>
        
        <view  v-if="false" class="contact-admin">
          <text @click="showContactAdminModal">没有账号？联系管理员添加</text>
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
    
    <!-- 自定义绑定组件 -->
    <WechatBindingGuide 
      :visible="showBindingGuide"
      @confirm="handleBindingGuideConfirm"
      @cancel="handleBindingGuideCancel"
      @close="handleBindingGuideClose"
    />
    
    <PhoneBindingModal 
      :visible="showPhoneModal"
      :retryCount="phoneRetryCount"
      @confirm="handlePhoneConfirm"
      @cancel="handlePhoneCancel"
      @close="handlePhoneClose"
    />
    
    <BindingSuccessModal 
      :visible="showSuccessModal"
      :userInfo="bindingResult?.userInfo"
      @close="handleSuccessModalClose"
    />
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/user'
import { smsLogin, passwordLogin, sendCode, checkPhoneExists, checkOpenidBinding, openidLogin, bindPhoneToOpenid } from '@/api/auth'
import { getDeviceId } from '@/utils/device'
import SvgIcon from '@/components/SvgIcon.vue'
import WechatBindingGuide from '@/components/WechatBindingGuide.vue'
import PhoneBindingModal from '@/components/PhoneBindingModal.vue'
import BindingSuccessModal from '@/components/BindingSuccessModal.vue'
import { APP_CONFIG } from '@/config/app.js'
import { CONTACT_CONFIG } from '@/config/contact.js'

const userStore = useUserStore()

// 登录模式：wechat | sms | password | bind
const loginMode = ref('wechat')
const loading = ref(false)
const agreed = ref(false)

// 绑定状态
const bindingStep = ref('check') // check | normal
const userOpenid = ref('')

// 自定义组件状态
const showBindingGuide = ref(false)
const showPhoneModal = ref(false)
const showSuccessModal = ref(false)
const phoneRetryCount = ref(0)
const bindingResult = ref(null)

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

// 手动绑定表单（保留用于其他登录方式）
const bindForm = reactive({
  phone: '',
  code: ''
})
const countdown = ref(0)
let countdownTimer = null

onMounted(() => {
  // 检查是否是主动退出登录
  const manualLogout = uni.getStorageSync('manualLogout')
  if (manualLogout) {
    // 清除退出标记
    uni.removeStorageSync('manualLogout')
    // 主动退出登录，不进行自动登录检查，直接显示登录界面
    console.log('[登录页面] 检测到主动退出登录，跳过自动登录检查')
    bindingStep.value = 'normal'
    return
  }
  
  // 检查是否是游客模式
  const isGuest = uni.getStorageSync('guestMode')
  if (isGuest) {
    // 游客模式用户可以进行登录，显示登录界面
    console.log('[登录页面] 检测到游客模式，显示登录界面供用户登录')
    bindingStep.value = 'normal'
    return
  }
  
  // 检查是否有token（已绑定用户）
  const token = uni.getStorageSync('token')
  if (token) {
    // 有token，验证后跳转
    console.log('[登录页面] 检测到token，验证后跳转')
    validateAndNavigate()
    return
  }
  
  // 显示登录界面
  bindingStep.value = 'normal'
})

// 验证token并跳转
const validateAndNavigate = async () => {
  try {
    await userStore.validateToken()
    navigateToHome()
  } catch (error) {
    console.error('[登录页面] Token验证失败:', error)
    userStore.logout()
    bindingStep.value = 'normal'
  }
}

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
      return
    } catch (error) {
      // token无效，清除
      userStore.logout()
    }
  }
  
  // 没有有效token，尝试微信静默登录检查
  await checkWechatLoginStatus()
}

// 检查微信登录状态和openid绑定情况
const checkWechatLoginStatus = async () => {
  try {
    bindingStep.value = 'check'
    
    // 1. 检查微信环境
    const accountInfo = uni.getAccountInfoSync()
    if (!accountInfo || !accountInfo.miniProgram) {
      console.log('[静默登录] 非微信小程序环境')
      bindingStep.value = 'normal'
      return
    }
    
    // 2. 静默获取微信登录凭证
    const loginRes = await uni.login({ provider: 'weixin' })
    if (!loginRes.code) {
      console.log('[静默登录] 获取微信凭证失败')
      bindingStep.value = 'normal'
      return
    }
    
    console.log('[静默登录] 获取微信code成功，检查绑定状态')
    
    // 3. 检查openid是否已绑定
    const checkResult = await checkOpenidBindingStatus(loginRes.code)
    
    if (checkResult.isBound) {
      // 已绑定，尝试静默登录
      console.log('[静默登录] 检测到已绑定，尝试静默登录')
      userOpenid.value = checkResult.openid
      await performSilentLogin(checkResult.openid)
    } else {
      // 未绑定，显示正常登录界面
      console.log('[静默登录] 检测到未绑定，显示登录界面')
      userOpenid.value = checkResult.openid
      bindingStep.value = 'normal'
    }
    
  } catch (error) {
    console.error('[静默登录] 检查失败:', error)
    // 静默登录失败不显示错误，直接显示登录界面
    bindingStep.value = 'normal'
  }
}

// 执行静默登录
const performSilentLogin = async (openid) => {
  try {
    console.log('[静默登录] 开始静默登录')
    
    const result = await openidLogin({
      openid: openid,
      deviceId: getDeviceId()
    })
    
    console.log('[静默登录] 静默登录成功:', result.userInfo)
    
    // 保存登录信息
    userStore.setLoginInfo(result)
    
    // 直接跳转首页，不显示提示
    navigateToHome()
    
  } catch (error) {
    console.error('[静默登录] 静默登录失败:', error)
    // 静默登录失败，显示正常登录界面
    bindingStep.value = 'normal'
  }
}

// 检查openid绑定状态
const checkOpenidBindingStatus = async (code) => {
  try {
    console.log('[API调用] 检查openid绑定状态, code:', code)
    
    const result = await checkOpenidBinding(code)
    console.log('[API调用] 检查绑定状态成功:', result)
    return result
  } catch (error) {
    console.error('[API调用] 检查绑定状态失败:', error)
    
    // 详细错误信息
    if (error.message) {
      console.error('[API调用] 错误消息:', error.message)
    }
    if (error.statusCode) {
      console.error('[API调用] HTTP状态码:', error.statusCode)
    }
    if (error.data) {
      console.error('[API调用] 错误数据:', error.data)
    }
    
    throw new Error(`检查绑定状态失败: ${error.message}`)
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

// ==================== 微信登录/绑定 ====================
const handleWechatLogin = async () => {
  if (!agreed.value) {
    uni.showToast({ title: '请阅读并同意用户协议和隐私政策', icon: 'none' })
    return
  }
  
  try {
    loading.value = true
    uni.showLoading({ title: '正在登录...', mask: true })
    
    console.log('[微信登录] 开始微信一键登录流程')
    
    // 1. 获取微信登录凭证
    const loginRes = await uni.login({ provider: 'weixin' })
    if (!loginRes.code) {
      throw new Error('获取微信登录凭证失败，请重试')
    }
    
    console.log('[微信登录] 获取微信code成功:', loginRes.code)
    
    // 2. 检查openid绑定状态
    const checkResult = await checkOpenidBindingStatus(loginRes.code)
    console.log('[微信登录] 绑定状态检查结果:', checkResult)
    
    if (checkResult.isBound) {
      // 已绑定，直接使用openid登录
      console.log('[微信登录] 检测到已绑定，执行直接登录')
      await performOpenidLogin(checkResult.openid)
    } else {
      // 未绑定，游客登录成功
      console.log('[微信登录] 检测到未绑定，游客登录成功')
      
      // 保存openid用于后续导入数据（重要：保存openid而不是code）
      userOpenid.value = checkResult.openid
      uni.setStorageSync('wechatOpenid', checkResult.openid)
      
      // 清除游客模式标记，设置已登录游客标记
      uni.removeStorageSync('guestMode')
      uni.setStorageSync('wasGuestMode', true)
      
      // 设置基本的登录状态（游客登录成功，但未关联后台账户）
      const guestLoginInfo = {
        accessToken: 'guest_token_' + Date.now(),
        refreshToken: 'guest_refresh_token_' + Date.now(),
        expiresIn: 7200,
        userType: 'guest', // 设置为游客类型
        userInfo: {
          id: 'guest_' + Date.now(),
          name: '游客用户',
          phone: '',
          avatar: '',
          isBackendLinked: false // 明确标记未关联后台账户
        },
        projects: [] // 游客没有项目数据
      }
      
      // 保存登录信息到store
      userStore.setLoginInfo(guestLoginInfo)
      
      // 显示成功提示
      uni.showToast({ 
        title: '登录成功', 
        icon: 'success',
        duration: 1500
      })
      
      // 跳转到首页（已登录游客模式）
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/dashboard/index' })
      }, 1000)
    }
    
  } catch (error) {
    console.error('[微信登录] 登录流程失败:', error)
    handleLoginError(error)
  } finally {
    loading.value = false
    uni.hideLoading()
  }
}

// 返回上一页
const handleBack = () => {
  uni.navigateBack()
}

// 执行openid直接登录
const performOpenidLogin = async (openid) => {
  try {
    console.log('[直接登录] 使用openid登录:', openid)
    
    const result = await openidLogin({
      openid: openid,
      deviceId: getDeviceId()
    })
    
    console.log('[直接登录] 登录成功:', result.userInfo)
    
    // 检查是否是从游客模式登录的
    const wasGuest = uni.getStorageSync('guestMode') === true
    
    // 清除游客模式标记
    uni.removeStorageSync('guestMode')
    
    // 如果是从游客模式登录的，设置标记用于后续显示导入按钮
    if (wasGuest) {
      uni.setStorageSync('wasGuestMode', true)
      console.log('[直接登录] 检测到从游客模式登录，设置wasGuestMode标记')
    }
    
    // 保存登录信息
    userStore.setLoginInfo(result)
    
    // 显示成功提示
    uni.showToast({ 
      title: `欢迎回来，${result.userInfo.name}`, 
      icon: 'success',
      duration: 2000
    })
    
    // 跳转首页
    setTimeout(() => {
      navigateToHome()
    }, 1000)
    
  } catch (error) {
    console.error('[直接登录] 登录失败:', error)
    throw new Error(`登录失败: ${error.message}`)
  }
}

// 显示手机号绑定流程
const showPhoneBindingFlow = async () => {
  try {
    console.log('[绑定流程] 开始手机号绑定流程')
    
    // 显示绑定引导
    showBindingGuide.value = true
    
  } catch (error) {
    console.error('[绑定流程] 绑定流程失败:', error)
    throw error
  }
}

// 绑定引导组件事件处理
const handleBindingGuideConfirm = () => {
  showBindingGuide.value = false
  showPhoneModal.value = true
  phoneRetryCount.value = 0
}

const handleBindingGuideCancel = () => {
  showBindingGuide.value = false
  console.log('[绑定流程] 用户取消绑定')
}

const handleBindingGuideClose = () => {
  showBindingGuide.value = false
}

// 手机号输入组件事件处理
const handlePhoneConfirm = async (phone) => {
  try {
    showPhoneModal.value = false
    await performPhoneBinding(phone)
  } catch (error) {
    console.error('[手机号绑定] 绑定失败:', error)
    
    // 处理特定的绑定错误
    if (error.message.includes('未在系统中注册') || error.message.includes('用户不存在')) {
      await showPhoneNotFoundDialog()
    } else if (error.message.includes('手机号格式')) {
      // 格式错误，重新显示输入框
      phoneRetryCount.value++
      if (phoneRetryCount.value < 3) {
        showPhoneModal.value = true
      } else {
        uni.showToast({ title: '手机号格式错误次数过多', icon: 'none' })
      }
    } else {
      uni.showToast({ title: error.message || '绑定失败，请重试', icon: 'none' })
    }
  }
}

const handlePhoneCancel = () => {
  showPhoneModal.value = false
  console.log('[绑定流程] 用户取消输入手机号')
}

const handlePhoneClose = () => {
  showPhoneModal.value = false
}

// 执行手机号绑定
const performPhoneBinding = async (phone) => {
  try {
    console.log('[手机号绑定] 开始绑定手机号:', phone)
    
    uni.showLoading({ title: '正在绑定...', mask: true })
    
    const result = await bindPhoneToOpenid({
      openid: userOpenid.value,
      phone: phone,
      deviceId: getDeviceId()
    })
    
    console.log('[手机号绑定] 绑定成功:', result.userInfo)
    
    // 保存登录信息
    userStore.setLoginInfo(result)
    
    // 保存绑定结果用于成功页面显示
    bindingResult.value = result
    
    // 显示成功模态框
    showSuccessModal.value = true
    
  } catch (error) {
    console.error('[手机号绑定] 绑定失败:', error)
    
    // 处理特定的绑定错误
    if (error.message.includes('未在系统中注册') || error.message.includes('用户不存在')) {
      await showPhoneNotFoundDialog(phone)
    } else if (error.message.includes('已绑定其他')) {
      uni.showModal({
        title: '绑定失败',
        content: '该手机号已绑定其他微信账号，一个手机号只能绑定一个微信账号。',
        showCancel: false,
        confirmText: '我知道了'
      })
    } else {
      throw error
    }
  } finally {
    uni.hideLoading()
  }
}

// 绑定成功模态框关闭处理
const handleSuccessModalClose = () => {
  showSuccessModal.value = false
  
  // 清除游客模式标记
  uni.removeStorageSync('guestMode')
  
  // 使用 reLaunch 重新启动小程序，刷新所有页面
  console.log('[绑定成功] 重新启动小程序以刷新所有数据')
  uni.reLaunch({ 
    url: '/pages/dashboard/index',
    success: () => {
      console.log('[绑定成功] 小程序已刷新')
    }
  })
}

// 显示手机号未找到对话框
const showPhoneNotFoundDialog = async (phone) => {
  return new Promise((resolve) => {
    uni.showModal({
      title: '手机号未注册',
      content: `手机号 ${phone} 未在系统中注册。\n\n请联系管理员将您的手机号添加到系统中，或使用其他已注册的手机号。`,
      confirmText: '联系管理员',
      cancelText: '重新输入',
      success: async (res) => {
        if (res.confirm) {
          // 联系管理员
          showContactAdminModal()
          resolve()
        } else {
          // 重新输入手机号
          showPhoneModal.value = true
          phoneRetryCount.value = 0
          resolve()
        }
      }
    })
  })
}

// 处理登录错误
const handleLoginError = (error) => {
  console.error('[错误处理] 处理登录错误:', error)
  
  let errorMessage = '登录失败，请重试'
  let showContactAdmin = false
  let showRetry = true
  
  // 根据错误类型分类处理
  if (error.message.includes('网络') || error.message.includes('超时') || error.message.includes('连接')) {
    errorMessage = '网络连接失败，请检查网络后重试'
    showRetry = true
  } else if (error.message.includes('微信') && error.message.includes('凭证')) {
    errorMessage = '微信授权失败，请重新打开小程序'
    showRetry = true
  } else if (error.message.includes('未在系统中注册') || error.message.includes('用户不存在')) {
    errorMessage = '您的手机号未在系统中注册'
    showContactAdmin = true
    showRetry = false
  } else if (error.message.includes('已绑定其他')) {
    errorMessage = '该手机号已绑定其他微信账号'
    showRetry = false
  } else if (error.message.includes('账号已禁用') || error.message.includes('已停用')) {
    errorMessage = '您的账号已被停用'
    showContactAdmin = true
    showRetry = false
  } else if (error.message.includes('账号已锁定')) {
    errorMessage = '您的账号已被锁定，请稍后重试'
    showRetry = false
  } else if (error.message) {
    errorMessage = error.message
    // 判断是否需要联系管理员
    showContactAdmin = !error.message.includes('网络') && 
                     !error.message.includes('超时') && 
                     !error.message.includes('格式')
  }
  
  // 显示错误对话框
  if (showContactAdmin) {
    uni.showModal({
      title: '登录失败',
      content: `${errorMessage}\n\n如需帮助，请联系系统管理员。`,
      confirmText: '联系管理员',
      cancelText: '我知道了',
      success: (res) => {
        if (res.confirm) {
          showContactAdminModal()
        }
      }
    })
  } else if (showRetry) {
    uni.showModal({
      title: '登录失败',
      content: errorMessage,
      confirmText: '重试',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          // 重试登录
          setTimeout(() => {
            handleWechatLogin()
          }, 500)
        }
      }
    })
  } else {
    uni.showModal({
      title: '登录失败',
      content: errorMessage,
      showCancel: false,
      confirmText: '我知道了'
    })
  }
}

// 显示联系管理员弹窗
const showContactAdminModal = () => {
  const admin = CONTACT_CONFIG.admin
  
  uni.showModal({
    title: '联系系统管理员',
    content: `管理员：${admin.name}\n电话：${admin.phone}\n工作时间：${admin.workTime}\n\n请说明您需要添加账号到系统中`,
    confirmText: '拨打电话',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        // 拨打管理员电话
        uni.makePhoneCall({
          phoneNumber: admin.phone,
          fail: () => {
            uni.showToast({ 
              title: '无法拨打电话，请手动拨打：' + admin.phone, 
              icon: 'none',
              duration: 3000
            })
          }
        })
      }
    }
  })
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
  console.log('[登录页面] 跳转首页，用户类型:', userStore.userType)
  
  // 使用 reLaunch 而不是 switchTab，确保页面重新初始化
  // 这样可以保证 onMounted 重新执行，数据能正常加载
  uni.reLaunch({ 
    url: '/pages/dashboard/index',
    success: () => {
      console.log('[登录页面] 页面跳转成功，将重新初始化')
    },
    fail: (error) => {
      console.error('[登录页面] 页面跳转失败:', error)
      // 降级使用 switchTab
      uni.switchTab({ url: '/pages/dashboard/index' })
    }
  })
}

const navigateToTest = () => {
  uni.navigateTo({ url: '/pages/test/wechat-binding' })
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
  
  .login-tips {
    margin: 32rpx 0;
    padding: 24rpx;
    background: $color-gray-50;
    border-radius: $radius-l;
    border-left: 4rpx solid $color-brand;
  }
  
  .binding-tips {
    margin: 32rpx 0;
    padding: 24rpx;
    background: $color-gray-50;
    border-radius: $radius-l;
    border-left: 4rpx solid $color-gray-500;
  }
  
  .tip-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 16rpx;
    font-size: 24rpx;
    color: $color-text-secondary;
    line-height: 1.5;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    text {
      flex: 1;
    }
  }
  
  .loading-state {
    text-align: center;
    padding: 80rpx 0;
  }
  
  .loading-spinner {
    width: 60rpx;
    height: 60rpx;
    border: 4rpx solid $color-gray-200;
    border-top: 4rpx solid $color-brand;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 24rpx;
  }
  
  .loading-text {
    font-size: 28rpx;
    color: $color-text-secondary;
  }
  
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
  
  .back-btn {
    margin-top: 24rpx;
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30rpx;
    border-radius: $radius-full;
    color: $color-gray-500;
    background: #fff;
    border: 1rpx solid $color-border-medium;
    
    &:active {
      opacity: 0.6;
    }
    &::after{
      border:none;
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
  
  .contact-admin {
    margin-top: 24rpx;
    text-align: center;
    
    text {
      font-size: 24rpx;
      color: $color-brand;
      padding: 16rpx;
      border-radius: $radius-m;
      background: $color-brand-50;
      
      &:active {
        background: $color-brand-100;
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
  font-size: 22rpx;
  color: $color-text-tertiary;
  flex: 1;
  line-height: 1.8;
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
