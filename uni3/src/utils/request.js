/**
 * 封装 uni.request 请求
 */

// API 基础地址
const BASE_URL = ''

// 请求拦截器
const requestInterceptor = (config) => {
  // 获取 token
  const token = uni.getStorageSync('token')
  if (token) {
    config.header = {
      ...config.header,
      'Authorization': `Bearer ${token}`
    }
  }
  return config
}

// 响应拦截器
const responseInterceptor = (response) => {
  const { statusCode, data } = response
  
  if (statusCode === 200) {
    // 业务成功
    if (data.code === 200 || data.code === 0) {
      return data.data
    }
    // 业务错误
    return Promise.reject(new Error(data.msg || '请求失败'))
  }
  
  // HTTP 错误
  if (statusCode === 401) {
    // 未授权，跳转登录
    uni.removeStorageSync('token')
    uni.reLaunch({ url: '/pages/login/index' })
    return Promise.reject(new Error('登录已过期'))
  }
  
  return Promise.reject(new Error(`HTTP Error: ${statusCode}`))
}

// 统一请求方法
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
  
  return new Promise((resolve, reject) => {
    // 显示加载提示
    if (options.loading !== false) {
      uni.showLoading({ title: '加载中...', mask: true })
    }
    
    uni.request({
      ...config,
      success: (response) => {
        responseInterceptor(response)
          .then(resolve)
          .catch(reject)
      },
      fail: (error) => {
        reject(new Error(error.errMsg || '网络请求失败'))
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

export default request

