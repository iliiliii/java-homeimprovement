<template>
  <view class="profile-page">
    <!-- 固定头部区域 -->
    <PageHeader 
      title="个人中心"
      :show-back="false"
    />
    
    <!-- 页面内容区域 - 使用原生滚动 -->
    <view class="page-content">
      <!-- 未登录用户引导登录 -->
      <view v-if="shouldShowLoginGuide" class="login-guide-section">
        <view class="login-guide-card">
          <view class="guide-icon">🔐</view>
          <text class="guide-desc">欢迎登录后管家</text>
          <view class="guide-btn" @click="handleGoLogin">
            <text>立即登录</text>
          </view>
        </view>
      </view>

      <!-- 项目概况卡片（已登录用户） -->
      <view class="project-brief-section" v-if="shouldShowProjects">
        <!-- 数据加载中 -->
        <view v-if="dataLoading" class="loading-placeholder">
          <view class="loading-spinner"></view>
          <text>加载项目数据中...</text>
        </view>
        <!-- 项目数据 -->
        <ProjectCardSwiper
          v-else-if="projects.length > 0"
          :projects="projects"
          :user-info="userInfo"
          :current="currentProjectIndex"
          @update:current="index => currentProjectIndex = index"
          @change="handleSwiperChange"
          @click="handleCardClick"
        />
        <!-- 无项目提示
        <view v-else class="no-projects-tip">
          <text>暂无关联项目</text>
        </view>
         -->
      </view>

      <!-- 费用统计四宫格（仅客户可见且有项目） -->
      <view class="expense-section" v-if="shouldShowExpenses">
        <!-- 数据加载中 -->
        <view v-if="dataLoading" class="loading-placeholder">
          <view class="loading-spinner"></view>
          <text>加载费用数据中...</text>
        </view>
        <!-- 费用数据 -->
        <template v-else-if="projects.length > 0">
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
          
          <!-- 总金额汇总卡片 -->
          <view class="total-amount-card">
            <text class="total-label">合同总金额</text>
            <view class="total-value">
              <text class="total-number">{{ formatTotalAmount(totalAmount).number }}</text>
              <text class="total-unit">{{ formatTotalAmount(totalAmount).unit }}</text>
            </view>
          </view>
        </template>
        <!-- 无项目提示 -->
        <view v-else class="no-projects-tip">
          <text>暂无项目费用信息</text>
        </view>
      </view>
      
      <!-- 底部按钮区域 -->
      <view class="bottom-buttons">
        <!-- 关于我们按钮 - 一直显示 -->
        <view class="action-btn" @click="handleAbout">
          <text>关于我们</text>
        </view>
        
        <!-- 导入历史数据按钮 - 只有登录成功的游客且未关联后台账户的才显示 -->
        <view v-if="shouldShowImportButton" class="action-btn import-data-btn" @click="handleImportData">
          <text>导入历史数据</text>
        </view>
        
        <!-- 退出登录按钮 - 登录后一定会有，不论什么角色 -->
        <view v-if="isLoggedIn" class="action-btn" @click="handleLogout">
          <text>退出登录</text>
        </view>
      </view>
    </view>
    
    <!-- Custom TabBar -->
    <CustomTabBar :current="3" />
    
    <!-- 导入数据相关组件 -->
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
  </view>
</template>

<script setup>
import { ref, computed, onMounted, watch, getCurrentInstance } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getCustomerDashboard, getProjectContractAmounts } from '@/api/dashboard.js'
import { bindPhoneToOpenid, checkOpenidBinding, openidLogin } from '@/api/auth.js'
import UserAvatar from '@/components/UserAvatar.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'
import ProjectCardSwiper from '@/components/ProjectCardSwiper.vue'
import PageHeader from '@/components/PageHeader.vue'
import PhoneBindingModal from '@/components/PhoneBindingModal.vue'
import WechatBindingGuide from '@/components/WechatBindingGuide.vue'

const userStore = useUserStore()

// 状态
const projects = ref([])
const currentProjectIndex = ref(0)
const currentProject = computed(() => projects.value[currentProjectIndex.value] || null)
const dataLoading = ref(false) // 添加数据加载状态

// 用户信息和状态
const userInfo = computed(() => userStore.userInfo)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const isCustomer = computed(() => userStore.isCustomer)
const isStaff = computed(() => userStore.isStaff)

// 游客模式判断
const isGuestMode = computed(() => uni.getStorageSync('guestMode') === true)

// 页面显示逻辑
const shouldShowLoginGuide = computed(() => {
  // 未登录时显示登录引导（包括游客模式）
  return !isLoggedIn.value
})

const shouldShowProjects = computed(() => {
  // 已登录用户显示项目
  return isLoggedIn.value
})

const shouldShowExpenses = computed(() => {
  // 已登录的客户显示费用统计
  return isLoggedIn.value && isCustomer.value
})

const shouldShowImportButton = computed(() => {
  // 只有登录成功的游客并且未关联后台账户的才显示导入按钮
  if (!isLoggedIn.value) {
    return false
  }
  
  // 检查是否是游客用户或从游客模式登录成功的用户
  const wasGuestMode = uni.getStorageSync('wasGuestMode') === true
  const hasWechatOpenid = !!uni.getStorageSync('wechatOpenid')
  const isGuestUser = userStore.userType === 'guest'
  
  // 检查是否已关联后台账户
  const isBackendLinked = userStore.userInfo.isBackendLinked !== undefined 
    ? userStore.userInfo.isBackendLinked 
    : !!(userStore.userInfo.phone && userStore.userInfo.name && userStore.userInfo.id && !userStore.userInfo.id.startsWith('guest_'))
  
  // 只有满足以下条件才显示导入按钮：
  // 1. 已登录
  // 2. 是游客用户或曾经是游客模式或有微信openid（说明是通过微信登录的）
  // 3. 未关联后台账户
  return isLoggedIn.value && (isGuestUser || wasGuestMode || hasWechatOpenid) && !isBackendLinked
})

console.log(uni.getStorageSync('guestMode'),'isGuestMode')

// 调试日志 - 帮助验证按钮显示逻辑
console.log('[Profile] 按钮显示状态调试:')
console.log('[Profile] isLoggedIn:', isLoggedIn.value)
console.log('[Profile] userType:', userStore.userType)
console.log('[Profile] wasGuestMode:', uni.getStorageSync('wasGuestMode'))
console.log('[Profile] hasWechatOpenid:', !!uni.getStorageSync('wechatOpenid'))
console.log('[Profile] userInfo:', userStore.userInfo)
console.log('[Profile] isBackendLinked:', userStore.userInfo.isBackendLinked)
console.log('[Profile] shouldShowImportButton:', shouldShowImportButton.value)
console.log('[Profile] shouldShowLoginGuide:', shouldShowLoginGuide.value)
console.log('[Profile] isGuestMode:', isGuestMode.value)
// 导入数据相关状态
const showBindingGuide = ref(false)
const showPhoneModal = ref(false)
const phoneRetryCount = ref(0)

// 费用统计数据（直接使用API返回的数据）
const expenseList = ref([])

// 计算总金额
const totalAmount = computed(() => {
  return expenseList.value.reduce((sum, item) => sum + (item.value || 0), 0)
})

// 解析URL字段（可能是JSON数组字符串）
const parseUrl = (url) => {
  if (!url || url === 'null') return null
  try {
    // 尝试解析JSON数组，取第一个URL
    if (url.startsWith('[')) {
      const arr = JSON.parse(url)
      return arr.length > 0 ? arr[0] : null
    }
    return url
  } catch {
    return url
  }
}

// 加载合同金额数据（仅客户角色）
const loadContractAmounts = async (projectId) => {
  // 员工角色或未登录不调用此API
  if (!isCustomer.value || !projectId || !isLoggedIn.value) {
    console.log('[Profile] 不需要加载合同金额数据，客户角色:', isCustomer.value, '项目ID:', projectId, '已登录:', isLoggedIn.value)
    expenseList.value = []
    return
  }
  
  try {
    console.log('[Profile] ===开始加载合同金额数据===')
    console.log('[Profile] 项目ID:', projectId)
    
    // 禁用网络请求的默认loading
    const data = await getProjectContractAmounts(projectId, { loading: false })
    console.log('[Profile] 合同金额API响应:', data)
    
    // 直接使用API返回的数据，API已保证返回六项
    if (data && Array.isArray(data)) {
      expenseList.value = data.map(item => ({
        category: item.category,
        label: item.label,
        value: parseFloat(item.amount) || 0,
        url: parseUrl(item.url)
      }))
      console.log('[Profile] 合同金额数据加载完成:', expenseList.value.length, '项')
      console.log('[Profile] 费用列表:', expenseList.value)
    } else {
      console.warn('[Profile] 合同金额数据格式异常:', data)
      expenseList.value = []
    }
    
    console.log('[Profile] ===合同金额数据加载完成===')
    
  } catch (error) {
    console.error('[Profile] 获取合同金额失败:', error)
    expenseList.value = []
  }
}

// 监听当前项目变化，重新加载合同金额
watch(currentProject, (newProject) => {
  if (newProject?.id) {
    loadContractAmounts(newProject.id)
  }
}, { immediate: true })

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
  // 只有已登录用户才加载项目数据
  if (!isLoggedIn.value) {
    console.log('[Profile] 未登录用户，不加载项目数据')
    projects.value = []
    return
  }
  
  console.log('[Profile] ===开始加载项目数据===')
  dataLoading.value = true
  
  try {
    // 禁用网络请求的默认loading，使用页面自己的loading
    const data = await getCustomerDashboard({ loading: false })
    console.log('[Profile] 项目API响应:', data)
    
    const list = data?.projects || []
    projects.value = list
    
    console.log('[Profile] 项目数据加载完成:', list.length, '个项目')
    console.log('[Profile] 项目列表:', list)
    
    if (list.length > 0) {
      // 优先使用本地存储的选中项目
      const savedProjectId = uni.getStorageSync('currentProjectId')
      let index = 0
      if (savedProjectId) {
         index = list.findIndex(p => p.id === savedProjectId)
         if (index === -1) index = 0
      }
      currentProjectIndex.value = index
      console.log('[Profile] 设置当前项目索引:', index, '项目ID:', list[index]?.id)
    }
    
    console.log('[Profile] ===项目数据加载完成===')
    
  } catch (error) {
    console.error('[Profile] 获取项目数据失败:', error)
    projects.value = []
  } finally {
    dataLoading.value = false
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
    return { number: `${amount.toLocaleString()}`, unit: '' }
  }
}

// 格式化总金额（保留两位小数，带千分位）
const formatTotalAmount = (amount) => {
  return formatAmount(amount)
}

// 关于我们
const handleAbout = () => {
  uni.navigateTo({
    url: '/pages/brand/index'
  })
}

// 跳转登录页面
const handleGoLogin = () => {
  console.log('[Profile] 跳转登录页面')
  uni.navigateTo({
    url: '/pages/login/index-new'
  })
}

// 退出登录
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        console.log('[Profile] 用户确认退出登录')
        
        // 清除游客模式标记和微信信息
        uni.removeStorageSync('guestMode')
        uni.removeStorageSync('wechatOpenid')
        uni.removeStorageSync('wasGuestMode')
        
        // 执行登出
        userStore.logout()
        
        // 注意：userStore.logout() 会跳转到登录页，但我们需要跳转到首页
        // 所以在这里重新跳转到首页
        setTimeout(() => {
          uni.reLaunch({
            url: '/pages/dashboard/index'
          })
        }, 100)
      }
    }
  })
}

// 导入历史数据
const handleImportData = () => {
  console.log('[导入数据] 开始导入历史数据流程')
  
  // 检查是否已登录
  if (!isLoggedIn.value) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }
  
  showBindingGuide.value = true
}

// 绑定引导确认
const handleBindingGuideConfirm = () => {
  showBindingGuide.value = false
  showPhoneModal.value = true
  phoneRetryCount.value = 0
}

// 绑定引导取消
const handleBindingGuideCancel = () => {
  showBindingGuide.value = false
  console.log('[导入数据] 用户取消导入')
}

// 绑定引导关闭
const handleBindingGuideClose = () => {
  showBindingGuide.value = false
}

// 手机号确认
const handlePhoneConfirm = async (phone) => {
  try {
    showPhoneModal.value = false
    await performPhoneBinding(phone)
  } catch (error) {
    console.error('[导入数据] 绑定失败:', error)
    
    if (error.message.includes('未在系统中注册') || error.message.includes('用户不存在')) {
      await showPhoneNotFoundDialog(phone)
    } else if (error.message.includes('账号格式')) {
      phoneRetryCount.value++
      if (phoneRetryCount.value < 3) {
        showPhoneModal.value = true
      } else {
        uni.showToast({ title: '账号格式错误次数过多', icon: 'none' })
      }
    } else {
      uni.showToast({ title: error.message || '导入失败，请重试', icon: 'none' })
    }
  }
}

// 手机号取消
const handlePhoneCancel = () => {
  showPhoneModal.value = false
  console.log('[导入数据] 用户取消输入账号')
}

// 手机号关闭
const handlePhoneClose = () => {
  showPhoneModal.value = false
}

// 执行手机号绑定
const performPhoneBinding = async (phone) => {
  try {
    console.log('[导入数据] 开始绑定账号:', phone)
    
    uni.showLoading({ title: '正在导入数据...', mask: true })
    
    // 获取保存的微信openid（不再使用code，因为code只能用一次）
    const wechatOpenid = uni.getStorageSync('wechatOpenid')
    if (!wechatOpenid) {
      throw new Error('微信登录信息已过期，请重新登录')
    }
    
    console.log('[导入数据] 使用保存的openid进行绑定')
    
    // 直接使用openid执行绑定操作
    const result = await bindPhoneToOpenid({
      openid: wechatOpenid,
      phone: phone,
      deviceId: getDeviceId()
    })
    
    console.log('[导入数据] 绑定成功:', result.userInfo)
    
    // 清除所有游客相关标记，因为用户已经关联后台账户
    uni.removeStorageSync('guestMode')
    uni.removeStorageSync('wechatOpenid')
    uni.removeStorageSync('wasGuestMode') // 清除游客标记，因为已经关联后台账户
    
    // 保存登录信息
    userStore.setLoginInfo(result)
    
    // 显示成功提示
    uni.showToast({ 
      title: '历史数据导入成功', 
      icon: 'success',
      duration: 1500
    })
    
    // 根据用户类型跳转到相应页面
    setTimeout(() => {
      console.log('[导入数据] 根据用户类型跳转页面')
      if (result.userType === 'staff') {
        // 员工跳转到员工首页
        uni.reLaunch({ 
          url: '/pages/dashboard/index',
          success: () => {
            console.log('[导入数据] 员工用户已跳转到首页')
          }
        })
      } else {
        // 客户跳转到客户首页
        uni.reLaunch({ 
          url: '/pages/dashboard/index',
          success: () => {
            console.log('[导入数据] 客户用户已跳转到首页')
          }
        })
      }
    }, 1500)
    
  } catch (error) {
    console.error('[导入数据] 绑定失败:', error)
    
    if (error.message.includes('未在系统中注册') || error.message.includes('用户不存在')) {
      await showPhoneNotFoundDialog(phone)
    } else if (error.message.includes('已绑定其他')) {
      uni.showModal({
        title: '导入失败',
        content: '该账号已绑定其他微信账号，一个账号只能绑定一个微信账号。',
        showCancel: false,
        confirmText: '我知道了'
      })
    } else if (error.message.includes('微信账号已绑定其他')) {
      uni.showModal({
        title: '导入失败',
        content: '该微信账号已绑定其他账号，请使用已绑定的账号登录。',
        showCancel: false,
        confirmText: '我知道了'
      })
    } else if (error.message.includes('登录信息已过期')) {
      // 微信信息过期，需要重新登录
      uni.showModal({
        title: '登录信息已过期',
        content: '请重新登录后再试',
        showCancel: false,
        confirmText: '重新登录',
        success: () => {
          uni.removeStorageSync('guestMode')
          uni.removeStorageSync('wechatOpenid')
          uni.reLaunch({ url: '/pages/login/index-new' })
        }
      })
    } else {
      throw error
    }
  } finally {
    uni.hideLoading()
  }
}

// 显示手机号未找到对话框
const showPhoneNotFoundDialog = async (phone) => {
  return new Promise((resolve) => {
    uni.showModal({
      title: '账号未注册',
      content: `账号 ${phone} 未在系统中注册。\n\n请联系管理员将您的账号添加到系统中，或使用其他已注册的账号。`,
      confirmText: '重新输入',
      cancelText: '取消',
      success: async (res) => {
        if (res.confirm) {
          // 重新输入手机号
          showPhoneModal.value = true
          phoneRetryCount.value = 0
          resolve()
        } else {
          resolve()
        }
      }
    })
  })
}

// 获取设备ID
const getDeviceId = () => {
  let deviceId = uni.getStorageSync('deviceId')
  if (!deviceId) {
    deviceId = 'device_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    uni.setStorageSync('deviceId', deviceId)
  }
  return deviceId
}

// 刷新所有数据
const refreshData = async () => {
  // 只有已登录用户才刷新数据
  if (!isLoggedIn.value) {
    console.log('[Profile] 未登录用户，不刷新数据')
    return
  }
  
  await loadProjectData()
  // 项目数据加载后，watch会自动触发loadContractAmounts
}

onMounted(async () => {
  console.log('[Profile] =====页面初始化开始=====')
  console.log('[Profile] 游客模式:', isGuestMode.value)
  console.log('[Profile] 已登录:', isLoggedIn.value)
  console.log('[Profile] isCustomer:', isCustomer.value)
  console.log('[Profile] userType:', userStore.userType)
  console.log('[Profile] token:', userStore.token ? '存在' : '不存在')
  console.log('[Profile] userInfo:', userStore.userInfo)
  
  // 只有已登录用户才加载项目数据
  if (isLoggedIn.value) {
    console.log('[Profile] 已登录用户，开始加载项目数据')
    try {
      await loadProjectData()
      console.log('[Profile] 项目数据加载完成')
    } catch (error) {
      console.error('[Profile] 项目数据加载失败:', error)
    }
  } else {
    console.log('[Profile] 未登录用户，不加载项目数据')
  }
  
  console.log('[Profile] =====页面初始化完成=====')
})

// 下拉刷新
onPullDownRefresh(async () => {
  await refreshData()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: $color-white;
  padding-bottom: 120rpx; // 为TabBar留出空间
}

// 页面内容区域
.page-content {
  padding: 16rpx 32rpx 32rpx;
  // 为固定头部留出空间：状态栏高度(约44px) + 导航栏高度(56px) = 100px
  margin-top: 100px;
  // padding-top: 100px;
}

// 登录引导区域
.login-guide-section {
  margin-bottom: 32rpx;
}

.login-guide-card {
  padding: 48rpx 32rpx;
  background: linear-gradient(135deg, $color-brand-50 0%, $color-white 100%);
  border: 2rpx solid $color-brand-100;
  border-radius: $radius-2xl;
  text-align: center;
  position: relative;
  overflow: hidden;
  
  // 装饰背景
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, $color-brand-100 0%, transparent 70%);
    opacity: 0.3;
    z-index: 0;
  }
}

.guide-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
  position: relative;
  z-index: 1;
}

.guide-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 16rpx;
  position: relative;
  z-index: 1;
}

.guide-desc {
  display: block;
  font-size: 26rpx;
  color: $color-text-secondary;
  line-height: 1.6;
  margin-bottom: 32rpx;
  position: relative;
  z-index: 1;
}

.guide-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 48rpx;
  background: linear-gradient(135deg, $color-brand 0%, $color-brand-600 100%);
  border-radius: $radius-full;
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
  
  text {
    font-size: 28rpx;
    font-weight: 600;
    color: $color-white;
  }
  
  &:active {
    transform: scale(0.96);
    box-shadow: 0 4rpx 12rpx rgba(196, 0, 22, 0.3);
  }
}

// 项目概况区域
.project-brief-section {
  margin-bottom: 32rpx;
  margin-left: -20rpx;
}

// 加载状态
.loading-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 32rpx;
  margin: 0 20rpx;
  background: $color-gray-50;
  border-radius: $radius-xl;
  border: 2rpx dashed $color-border;
  
  .loading-spinner {
    width: 60rpx;
    height: 60rpx;
    border: 4rpx solid #f3f3f3;
    border-top: 4rpx solid $color-brand;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 24rpx;
  }
  
  text {
    font-size: 26rpx;
    color: $color-text-tertiary;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

// 无项目提示
.no-projects-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80rpx 32rpx;
  margin: 0 20rpx;
  background: $color-gray-50;
  border-radius: $radius-xl;
  border: 2rpx dashed $color-border;
  
  text {
    font-size: 28rpx;
    color: $color-text-tertiary;
  }
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
  padding: 24rpx;
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

// 总金额汇总卡片
.total-amount-card {
  margin-top: 24rpx;
  padding: 32rpx 40rpx;
  background: linear-gradient(145deg, $color-white 0%, $color-gray-50 100%);
  border: 2rpx solid $color-border-light;
  border-radius: $radius-xl;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.total-label {
  font-size: 28rpx;
  color: $color-text-tertiary;
  font-weight: 500;
}

.total-value {
  display: flex;
  align-items: baseline;
  
  .total-number {
    font-size: 48rpx;
    font-weight: 700;
    background: linear-gradient(135deg, $color-brand 0%, $color-brand-600 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .total-unit {
    font-size: 28rpx;
    font-weight: 500;
    color: $color-brand;
    margin-left: 6rpx;
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
  
  // 导入历史数据按钮特殊样式
  &.import-data-btn {
    background: linear-gradient(135deg, #10B981 0%, #059669 100%);
    border: none;
    color: $color-white;
    font-weight: 600;
    position: relative;
    
    &::before {
      display: none;
    }
    
    // 添加闪烁动画提示
    &::after {
      content: '✨';
      position: absolute;
      right: 24rpx;
      font-size: 32rpx;
      animation: sparkle 2s ease-in-out infinite;
    }
    
    &:active {
      background: linear-gradient(135deg, #059669 0%, #047857 100%);
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

@keyframes sparkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.2); }
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

