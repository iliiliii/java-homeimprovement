/**
 * 认证相关API
 */
import { post } from '@/utils/request'

/**
 * 微信登录
 * @param {Object} data - { code, phoneCode, deviceId }
 * @returns {Promise}
 */
export const wechatLogin = (data) => {
  return post('/app/auth/wechat-login', data)
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
