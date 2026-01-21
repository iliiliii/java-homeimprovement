/**
 * 权限判断工具
 */
import { useUserStore } from '@/store/user'

/**
 * 是否游客模式
 * @returns {Boolean}
 */
export const isGuestMode = () => {
  return uni.getStorageSync('guestMode') === true
}

/**
 * 是否员工
 * @returns {Boolean}
 */
export const isStaff = () => {
  const userStore = useUserStore()
  return userStore.userType === 'staff'
}

/**
 * 是否客户
 * @returns {Boolean}
 */
export const isCustomer = () => {
  const userStore = useUserStore()
  return userStore.userType === 'customer'
}

/**
 * 是否有页面权限
 * @param {String} pagePath - 页面路径
 * @returns {Boolean}
 */
export const hasPagePermission = (pagePath) => {
  // 游客模式可访问的页面
  const guestAllowedPages = [
    '/pages/dashboard/',   // 首页（部分功能）
    '/pages/design/',      // 设计图库
    '/pages/brand/',       // 品牌展示
    '/pages/login/'        // 登录页
  ]
  
  // 如果是游客模式
  if (isGuestMode()) {
    return guestAllowedPages.some(path => pagePath.startsWith(path))
  }
  
  // 员工专属页面列表
  const staffOnlyPages = [
    '/pages/inspection/',  // 工地巡视
    '/pages/issue/',       // 问题上报
    '/pages/repair/'       // 整改记录
  ]
  
  // 如果是客户，检查是否访问员工专属页面
  if (isCustomer()) {
    return !staffOnlyPages.some(path => pagePath.startsWith(path))
  }
  
  // 员工可以访问所有页面
  return true
}

/**
 * 检查是否已登录
 * @returns {Boolean}
 */
export const isLoggedIn = () => {
  const userStore = useUserStore()
  return !!userStore.token && !isGuestMode()
}

/**
 * 获取用户类型文本
 * @returns {String}
 */
export const getUserTypeText = () => {
  if (isGuestMode()) {
    return '游客'
  } else if (isStaff()) {
    return '员工'
  } else if (isCustomer()) {
    return '客户'
  }
  return '未知'
}

/**
 * 检查功能权限（用于页面内的功能控制）
 * @param {String} feature - 功能名称
 * @returns {Boolean}
 */
export const hasFeaturePermission = (feature) => {
  // 游客模式限制的功能
  const guestRestrictedFeatures = [
    'project-detail',     // 项目详情
    'schedule-detail',    // 施工进度
    'quality-check',      // 质检记录
    'shopping-list',      // 购物清单
    'project-log',        // 施工日志
    'profile-edit'        // 个人信息编辑
  ]
  
  if (isGuestMode()) {
    return !guestRestrictedFeatures.includes(feature)
  }
  
  return true
}

/**
 * 提示登录
 */
export const promptLogin = (message = '此功能需要登录后使用') => {
  uni.showModal({
    title: '需要登录',
    content: message,
    confirmText: '去登录',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        // 清除游客模式标记
        uni.removeStorageSync('guestMode')
        // 跳转登录页
        uni.reLaunch({ url: '/pages/login/index-new' })
      }
    }
  })
}
