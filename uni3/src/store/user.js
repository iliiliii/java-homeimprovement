/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const userInfo = ref({
    phone: '',
    projectCode: ''
  })
  
  // 项目信息
  const projectInfo = ref({
    id: '',
    name: '',
    area: 0,
    style: '',
    status: '',
    phase: ''
  })
  
  // Token
  const token = ref('')
  
  // 是否已登录
  const isLoggedIn = computed(() => !!token.value)
  
  // 是否设计阶段
  const isDesignPhase = computed(() => {
    return projectInfo.value.phase === 'design'
  })
  
  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = { ...userInfo.value, ...info }
    uni.setStorageSync('userInfo', userInfo.value)
  }
  
  // 设置项目信息
  const setProjectInfo = (info) => {
    projectInfo.value = { ...projectInfo.value, ...info }
    uni.setStorageSync('projectInfo', projectInfo.value)
  }
  
  // 设置 Token
  const setToken = (newToken) => {
    token.value = newToken
    uni.setStorageSync('token', newToken)
  }
  
  // 初始化（从本地存储恢复）
  const initFromStorage = () => {
    const storedUserInfo = uni.getStorageSync('userInfo')
    const storedProjectInfo = uni.getStorageSync('projectInfo')
    const storedToken = uni.getStorageSync('token')
    
    if (storedUserInfo) {
      userInfo.value = storedUserInfo
    }
    if (storedProjectInfo) {
      projectInfo.value = storedProjectInfo
    }
    if (storedToken) {
      token.value = storedToken
    }
  }
  
  // 登出
  const logout = () => {
    userInfo.value = { phone: '', projectCode: '' }
    projectInfo.value = {
      id: '',
      name: '',
      area: 0,
      style: '',
      status: '',
      phase: ''
    }
    token.value = ''
    
    uni.removeStorageSync('userInfo')
    uni.removeStorageSync('projectInfo')
    uni.removeStorageSync('token')
  }
  
  return {
    userInfo,
    projectInfo,
    token,
    isLoggedIn,
    isDesignPhase,
    setUserInfo,
    setProjectInfo,
    setToken,
    initFromStorage,
    logout
  }
})

