import request from '@/utils/request'

// 获取验证码
export function getCodeImg() {
  return request({
    url: '/captcha',
    method: 'get'
  })
}

// 用户登录
export function login(data: LoginFormData) {
  return request({
    url: '/login',
    method: 'post',
    data
  })
}

// 用户登出
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

// 获取用户信息
export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

// 登录表单数据类型
export interface LoginFormData {
  username: string
  password: string
  code?: string
  uuid?: string
  rememberMe?: boolean
}

// 验证码响应类型
export interface CaptchaResponse {
  captchaEnabled?: boolean
  img: string
  uuid: string
}

// 登录响应类型
export interface LoginResponse {
  token: string
}