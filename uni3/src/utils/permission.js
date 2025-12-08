/**
 * 权限判断工具
 */
import { useUserStore } from '@/store/user'

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
  return !!userStore.token
}

/**
 * 获取用户类型文本
 * @returns {String}
 */
export const getUserTypeText = () => {
  if (isStaff()) {
    return '员工'
  } else if (isCustomer()) {
    return '客户'
  }
  return '未知'
}
