/**
 * 封装 uni.request 请求
 */
import { getDeviceId } from './device'

// 判断是否为开发环境
const isDev = process.env.NODE_ENV === 'development'

// API 基础地址配置
const getBaseUrl = () => {
  // 开发环境使用本地地址
  if (isDev) {
    // #ifdef H5
    return 'http://192.168.5.102:8080'
    // #endif
    
    // #ifdef MP-WEIXIN
    // 小程序开发环境：需要在微信开发者工具中勾选"不校验合法域名"
    return 'https://hsdlp.gzcelestial.com/prod-api'
    // return 'http://192.168.5.102:8080'
    // #endif
  }
  
  // 生产环境使用正式域名
  return 'https://hsdlp.gzcelestial.com/prod-api'
}

const BASE_URL = getBaseUrl()

console.log('[Request] 当前环境:', isDev ? '开发' : '生产', ', API地址:', BASE_URL)

// 是否正在刷新Token
let isRefreshing = false
// 刷新Token期间的请求队列
let requestQueue = []

/**
 * 验证项目ID格式（UUID格式：32个十六进制字符）
 */
const isValidProjectId = (projectId) => {
  if (!projectId || typeof projectId !== 'string') {
    return false
  }
  return /^[a-fA-F0-9]{32}$/.test(projectId)
}

/**
 * 请求拦截器
 */
const requestInterceptor = (config) => {
  // 获取 token
  const token = uni.getStorageSync('token')
  if (token && token !== '' && token !== 'null') {
    config.header = {
      ...config.header,
      'Authorization': `Bearer ${token}`
    }
  } else {
    // 未登录时，不添加Authorization头，但仍然允许请求
    console.log('[Request] 未登录用户，不添加Authorization头')
  }
  
  // 添加设备ID
  config.header['X-Device-Id'] = getDeviceId()
  
  // 添加当前项目ID（添加格式验证）
  const currentProjectId = uni.getStorageSync('currentProjectId')
  if (currentProjectId) {
    if (isValidProjectId(currentProjectId)) {
      config.header['X-Project-Id'] = currentProjectId
      console.log('[Request] 添加项目ID到请求头:', currentProjectId)
    } else {
      console.error('[安全] 无效的项目ID格式，已忽略:', currentProjectId)
      // 清除无效的项目ID
      uni.removeStorageSync('currentProjectId')
    }
  } else {
    console.warn('[Request] 当前项目ID为空')
  }
  
  console.log('[Request] 请求头:', config.header)
  
  return config
}

/**
 * 响应拦截器
 */
const responseInterceptor = async (response, originalConfig) => {
  const { statusCode, data } = response
  
  // HTTP 200
  if (statusCode === 200) {
    // 业务成功
    if (data.code === 200 || data.code === 0) {
      return data.data
    }
    
    // Token过期（后端返回500但消息包含JWT expired）
    if (data.code === 500 && data.msg && data.msg.includes('JWT expired')) {
      console.log('[Request] Token过期，尝试刷新')
      // 如果是登录接口，直接返回错误
      if (originalConfig.url.includes('/auth/')) {
        return Promise.reject(new Error('认证失败'))
      }
      // 尝试刷新Token
      return handleTokenRefresh(originalConfig)
    }
    
    // 认证失败
    if (data.code === 401) {
      // 如果是登录接口，直接返回错误
      if (originalConfig.url.includes('/auth/')) {
        return Promise.reject(new Error('认证失败'))
      }
      // 尝试刷新Token
      return handleTokenRefresh(originalConfig)
    }
    
    // 其他业务错误
    return Promise.reject(new Error(data.msg || '请求失败'))
  }
  
  // HTTP 401 - Token过期
  if (statusCode === 401) {
    // 如果是登录接口，直接返回错误
    if (originalConfig.url.includes('/auth/')) {
      return Promise.reject(new Error('认证失败'))
    }
    
    // 尝试刷新Token
    return handleTokenRefresh(originalConfig)
  }
  
  // HTTP 403 - 无权限
  if (statusCode === 403) {
    uni.showToast({ title: '无权限访问', icon: 'none' })
    return Promise.reject(new Error('无权限访问'))
  }
  
  // HTTP 500
  if (statusCode === 500) {
    uni.showToast({ title: '服务器错误', icon: 'none' })
    return Promise.reject(new Error('服务器错误'))
  }
  
  // 其他错误
  return Promise.reject(new Error(`HTTP Error: ${statusCode}`))
}

/**
 * 处理Token刷新
 */
const handleTokenRefresh = async (originalConfig) => {
  const refreshToken = uni.getStorageSync('refreshToken')
  
  if (!refreshToken) {
    // 没有refreshToken，跳转登录
    redirectToLogin()
    return Promise.reject(new Error('登录已过期'))
  }
  
  // 如果正在刷新，将请求加入队列
  if (isRefreshing) {
    return new Promise((resolve, reject) => {
      requestQueue.push({ config: originalConfig, resolve, reject })
    })
  }
  
  isRefreshing = true
  
  try {
    // 调用刷新Token接口
    const result = await refreshTokenRequest(refreshToken)
    
    // 保存新Token
    uni.setStorageSync('token', result.accessToken)
    uni.setStorageSync('refreshToken', result.refreshToken)
    
    // 重试队列中的请求
    requestQueue.forEach(({ config, resolve, reject }) => {
      request(config).then(resolve).catch(reject)
    })
    requestQueue = []
    
    // 重试原请求
    return request(originalConfig)
    
  } catch (error) {
    // 刷新失败，清空队列，跳转登录
    requestQueue = []
    redirectToLogin()
    return Promise.reject(new Error('登录已过期'))
  } finally {
    isRefreshing = false
  }
}

/**
 * 刷新Token请求（不走拦截器）
 */
const refreshTokenRequest = (refreshToken) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + '/app/auth/refresh-token',
      method: 'POST',
      data: { refreshToken },
      header: {
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.statusCode === 200 && (res.data.code === 200 || res.data.code === 0)) {
          resolve(res.data.data)
        } else {
          reject(new Error('刷新Token失败'))
        }
      },
      fail: (error) => {
        reject(error)
      }
    })
  })
}

/**
 * 跳转登录页
 */
const redirectToLogin = () => {
  // 检查是否是游客模式或未登录
  const isGuest = uni.getStorageSync('guestMode') === true
  const token = uni.getStorageSync('token')
  const isLoggedIn = !!(token && token !== '' && token !== 'null')
  
  if (isGuest || !isLoggedIn) {
    // 游客模式或未登录，不跳转登录页，只是返回错误
    console.log('[Request] 游客/未登录模式，API调用失败但不跳转登录页')
    return
  }
  
  // 清除登录信息
  uni.removeStorageSync('token')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('userType')
  uni.removeStorageSync('userId')
  uni.removeStorageSync('userInfo')
  uni.removeStorageSync('projects')
  uni.removeStorageSync('currentProjectId')
  
  // 跳转登录页
  uni.reLaunch({ url: '/pages/login/index-new' })
}

/**
 * 统一请求方法
 */
const request = (options) => {
  const config = requestInterceptor({
    url: BASE_URL + options.url,
    method: options.method || 'GET',
    data: options.data,
    header: {
      'Content-Type': 'application/json',
      ...options.header
    },
    timeout: options.timeout || 30000
  })
  
  console.log('[Request] 发起请求:', {
    url: config.url,
    method: config.method,
    data: config.data,
    header: config.header,
    timeout: config.timeout
  })
  
  return new Promise((resolve, reject) => {
    // 显示加载提示
    if (options.loading !== false) {
      uni.showLoading({ title: '加载中...', mask: true })
    }
    
    uni.request({
      ...config,
      success: (response) => {
        console.log('[Request] 请求成功响应:', {
          statusCode: response.statusCode,
          header: response.header,
          data: response.data
        })
        responseInterceptor(response, options)
          .then(resolve)
          .catch(reject)
      },
      fail: (error) => {
        console.error('[Request] 请求失败:', {
          errMsg: error.errMsg,
          errno: error.errno,
          error: error
        })
        
        let errorMessage = '网络请求失败'
        if (error.errMsg) {
          if (error.errMsg.includes('timeout')) {
            errorMessage = '请求超时，请检查网络连接'
          } else if (error.errMsg.includes('fail')) {
            errorMessage = '网络连接失败，请检查网络设置'
          } else {
            errorMessage = error.errMsg
          }
        }
        
        uni.showToast({ title: errorMessage, icon: 'none' })
        reject(new Error(errorMessage))
      },
      complete: () => {
        if (options.loading !== false) {
          uni.hideLoading()
        }
      }
    })
  })
}

// 导出请求方法
export const get = (url, data, options = {}) => {
  return request({ url, method: 'GET', data, ...options })
}

export const post = (url, data, options = {}) => {
  return request({ url, method: 'POST', data, ...options })
}

export const put = (url, data, options = {}) => {
  return request({ url, method: 'PUT', data, ...options })
}

export const del = (url, data, options = {}) => {
  return request({ url, method: 'DELETE', data, ...options })
}

// 导出BASE_URL供其他模块使用
export { BASE_URL, getBaseUrl }

/**
 * 获取完整图片URL
 * 小程序不支持相对路径，需要拼接完整域名
 * @param {string} url - 图片URL（可能是相对路径或绝对路径）
 * @returns {string} 完整的图片URL
 */
export const getFullImageUrl = (url) => {
  if (!url) return ''
  
  // 已经是完整URL（http/https开头）
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  // 相对路径，拼接BASE_URL
  // 确保路径以/开头
  const path = url.startsWith('/') ? url : `/${url}`
  return BASE_URL + path
}

export default request
