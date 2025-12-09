/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { validateToken as validateTokenApi, refreshToken as refreshTokenApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // Token
  const token = ref('')
  const refreshToken = ref('')
  
  // 用户类型：customer | staff
  const userType = ref('')
  
  // 用户ID
  const userId = ref('')
  
  // 用户信息
  const userInfo = ref({
    id: '',
    name: '',
    phone: '',
    avatar: ''
  })
  
  // 用户关联的项目列表
  const projects = ref([])
  
  // 当前选中的项目ID
  const currentProjectId = ref('')
  
  // 当前项目信息
  const currentProject = computed(() => {
    if (!currentProjectId.value) return null
    return projects.value.find(p => p.id === currentProjectId.value) || null
  })
  
  // 是否已登录
  const isLoggedIn = computed(() => !!token.value)
  
  // 是否客户
  const isCustomer = computed(() => userType.value === 'customer')
  
  // 是否员工
  const isStaff = computed(() => userType.value === 'staff')
  
  // 是否设计阶段
  const isDesignPhase = computed(() => {
    return currentProject.value?.phase === 'design'
  })
  
  /**
   * 设置登录信息（登录成功后调用）
   * @param {Object} data - 登录响应数据
   */
  const setLoginInfo = (data) => {
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    userType.value = data.userType
    userId.value = data.userInfo.id
    userInfo.value = data.userInfo
    projects.value = data.projects || []
    
    // 设置当前项目（默认第一个）
    if (projects.value.length > 0 && !currentProjectId.value) {
      currentProjectId.value = projects.value[0].id
    }
    
    // 保存到本地存储
    saveToStorage()
  }
  
  /**
   * 设置用户信息
   */
  const setUserInfo = (info) => {
    userInfo.value = { ...userInfo.value, ...info }
    saveToStorage()
  }
  
  /**
   * 设置项目列表
   */
  const setProjects = (projectList) => {
    projects.value = projectList
    saveToStorage()
  }
  
  /**
   * 切换当前项目
   */
  const switchProject = (projectId) => {
    currentProjectId.value = projectId
    uni.setStorageSync('currentProjectId', projectId)
  }
  
  /**
   * 设置 Token
   */
  const setToken = (newToken) => {
    token.value = newToken
    uni.setStorageSync('token', newToken)
  }
  
  /**
   * 设置 RefreshToken
   */
  const setRefreshToken = (newRefreshToken) => {
    refreshToken.value = newRefreshToken
    uni.setStorageSync('refreshToken', newRefreshToken)
  }
  
  /**
   * 保存到本地存储
   */
  const saveToStorage = () => {
    uni.setStorageSync('token', token.value)
    uni.setStorageSync('refreshToken', refreshToken.value)
    uni.setStorageSync('userType', userType.value)
    uni.setStorageSync('userId', userId.value)
    uni.setStorageSync('userInfo', userInfo.value)
    uni.setStorageSync('projects', projects.value)
    uni.setStorageSync('currentProjectId', currentProjectId.value)
  }
  
  /**
   * 从本地存储恢复
   */
  const initFromStorage = () => {
    token.value = uni.getStorageSync('token') || ''
    refreshToken.value = uni.getStorageSync('refreshToken') || ''
    userType.value = uni.getStorageSync('userType') || ''
    userId.value = uni.getStorageSync('userId') || ''
    userInfo.value = uni.getStorageSync('userInfo') || {
      id: '',
      name: '',
      phone: '',
      avatar: ''
    }
    projects.value = uni.getStorageSync('projects') || []
    currentProjectId.value = uni.getStorageSync('currentProjectId') || ''
  }
  
  /**
   * 验证Token是否有效
   */
  const validateToken = async () => {
    if (!token.value) {
      throw new Error('未登录')
    }
    
    try {
      await validateTokenApi()
      return true
    } catch (error) {
      // Token无效，尝试刷新
      if (refreshToken.value) {
        try {
          await refreshAccessToken()
          return true
        } catch (refreshError) {
          // 刷新失败，清除登录状态
          logout()
          throw new Error('登录已过期')
        }
      } else {
        logout()
        throw new Error('登录已过期')
      }
    }
  }
  
  /**
   * 刷新Access Token
   */
  const refreshAccessToken = async () => {
    if (!refreshToken.value) {
      throw new Error('RefreshToken不存在')
    }
    
    try {
      const result = await refreshTokenApi(refreshToken.value)
      token.value = result.accessToken
      refreshToken.value = result.refreshToken
      
      uni.setStorageSync('token', token.value)
      uni.setStorageSync('refreshToken', refreshToken.value)
      
      return result
    } catch (error) {
      throw new Error('刷新Token失败')
    }
  }
  
  /**
   * 登出 - 清空所有缓存
   */
  const logout = () => {
    // 重置所有状态
    token.value = ''
    refreshToken.value = ''
    userType.value = ''
    userId.value = ''
    userInfo.value = {
      id: '',
      name: '',
      phone: '',
      avatar: ''
    }
    projects.value = []
    currentProjectId.value = ''
    
    // 清除所有本地存储（包括可能遗漏的缓存）
    try {
      uni.clearStorageSync()
    } catch (e) {
      // 如果clearStorageSync失败，逐个清除
      uni.removeStorageSync('token')
      uni.removeStorageSync('refreshToken')
      uni.removeStorageSync('userType')
      uni.removeStorageSync('userId')
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('projects')
      uni.removeStorageSync('currentProjectId')
    }
    
    // 跳转登录页（注意：登录页是index-new）
    uni.reLaunch({ url: '/pages/login/index-new' })
  }
  
  return {
    // 状态
    token,
    refreshToken,
    userType,
    userId,
    userInfo,
    projects,
    currentProjectId,
    currentProject,
    
    // 计算属性
    isLoggedIn,
    isCustomer,
    isStaff,
    isDesignPhase,
    
    // 方法
    setLoginInfo,
    setUserInfo,
    setProjects,
    switchProject,
    setToken,
    setRefreshToken,
    initFromStorage,
    validateToken,
    refreshAccessToken,
    logout
  }
})

