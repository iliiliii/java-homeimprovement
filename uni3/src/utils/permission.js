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
  if (isGuestMode()) return false
  const userStore = useUserStore()
  return userStore.userType === 'staff'
}

/**
 * 是否客户
 * @returns {Boolean}
 */
export const isCustomer = () => {
  if (isGuestMode()) return true // 游客模式视为客户
  const userStore = useUserStore()
  return userStore.userType === 'customer'
}

/**
 * 是否有页面权限（游客可访问所有页面）
 * @param {String} pagePath - 页面路径
 * @returns {Boolean}
 */
export const hasPagePermission = (pagePath) => {
  // 游客模式可以访问所有页面
  if (isGuestMode()) {
    return true
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
 * 检查是否已登录（游客模式也视为已登录）
 * @returns {Boolean}
 */
export const isLoggedIn = () => {
  // 游客模式视为已登录
  if (isGuestMode()) return true
  
  // 正式用户检查token
  const userStore = useUserStore()
  return !!userStore.token
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
 * 检查功能权限（游客模式下某些功能受限）
 * @param {String} feature - 功能名称
 * @returns {Boolean}
 */
export const hasFeaturePermission = (feature) => {
  // 游客模式下，所有功能都可以尝试访问
  // 但会显示示例数据或提示导入历史数据
  return true
}

/**
 * 提示导入历史数据
 */
export const promptImportData = (message = '导入历史数据后可查看完整信息') => {
  uni.showModal({
    title: '提示',
    content: message,
    confirmText: '去导入',
    cancelText: '稍后',
    success: (res) => {
      if (res.confirm) {
        // 跳转到个人中心
        uni.switchTab({ url: '/pages/profile/index' })
      }
    }
  })
}
