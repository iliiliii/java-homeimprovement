/**
 * 认证相关API
 */
import { post } from '@/utils/request'
import { getDeviceId } from '@/utils/device'

/**
 * 检查openid绑定状态
 * @param {String} code - 微信登录凭证
 * @returns {Promise}
 */
export const checkOpenidBinding = (code) => {
  console.log('[API] 准备调用检查绑定接口, code:', code)
  const deviceId = getDeviceId()
  console.log('[API] 设备ID:', deviceId)
  
  const requestData = { 
    code,
    deviceId
  }
  console.log('[API] 请求数据:', requestData)
  console.log('[API] 请求URL: /app/auth/check-openid-binding')
  
  return post('/app/auth/check-openid-binding', requestData, {
    timeout: 10000,
    loading: false
  }).then(result => {
    console.log('[API] 检查绑定接口响应成功:', result)
    return result
  }).catch(error => {
    console.error('[API] 检查绑定接口错误:', error)
    console.error('[API] 错误类型:', typeof error)
    console.error('[API] 错误属性:', Object.keys(error))
    
    if (error.statusCode) {
      console.error('[API] HTTP状态码:', error.statusCode)
    }
    if (error.data) {
      console.error('[API] 错误响应数据:', error.data)
    }
    if (error.errMsg) {
      console.error('[API] 系统错误消息:', error.errMsg)
    }
    
    throw error
  })
}

/**
 * 使用openid直接登录（已绑定用户）
 * @param {Object} data - { openid, deviceId }
 * @returns {Promise}
 */
export const openidLogin = (data) => {
  return post('/app/auth/openid-login', data, {
    timeout: 15000,
    loading: false
  })
}

/**
 * 绑定手机号到openid
 * @param {Object} data - { openid, phone, deviceId }
 * @returns {Promise}
 */
export const bindPhoneToOpenid = (data) => {
  return post('/app/auth/bind-phone-to-openid', data, {
    timeout: 15000,
    loading: false
  })
}

/**
 * 检查手机号是否已在系统中注册
 * @param {String} phone - 手机号
 * @returns {Promise}
 */
export const checkPhoneExists = (phone) => {
  return post('/app/auth/check-phone', { phone }, {
    timeout: 10000,
    loading: false
  })
}

/**
 * 短信验证码登录
 * @param {Object} data - { phone, code, deviceId }
 * @returns {Promise}
 */
export const smsLogin = (data) => {
  return post('/app/auth/sms-login', data)
}

/**
 * 密码登录
 * @param {Object} data - { phone, password, deviceId }
 * @returns {Promise}
 */
export const passwordLogin = (data) => {
  return post('/app/auth/password-login', data)
}

/**
 * 发送短信验证码
 * @param {String} phone - 手机号
 * @returns {Promise}
 */
export const sendCode = (phone) => {
  return post('/app/auth/send-code', { phone })
}

/**
 * 刷新Token
 * @param {String} refreshToken - RefreshToken
 * @returns {Promise}
 */
export const refreshToken = (refreshToken) => {
  return post('/app/auth/refresh-token', { refreshToken })
}

/**
 * 退出登录
 * @returns {Promise}
 */
export const logout = () => {
  return post('/app/auth/logout')
}

/**
 * 验证Token是否有效
 * @returns {Promise}
 */
export const validateToken = () => {
  return post('/app/auth/validate-token')
}

/**
 * 解除微信绑定
 * @param {Object} data - { openid, deviceId, verifyType, phone?, code?, password? }
 * @returns {Promise}
 */
export const unbindWechat = (data) => {
  return post('/app/auth/unbind-wechat', data, {
    timeout: 15000,
    loading: false
  })
}
