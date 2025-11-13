import Cookies from 'js-cookie'
import { encrypt, decrypt } from './jsencrypt'

// Cookie 键名
const TOKEN_KEY = 'Admin-Token'
const USERNAME_KEY = 'username'
const PASSWORD_KEY = 'password'
const REMEMBER_ME_KEY = 'rememberMe'

// Token 相关
export function getToken(): string {
  return Cookies.get(TOKEN_KEY) || ''
}

export function setToken(token: string): void {
  Cookies.set(TOKEN_KEY, token)
}

export function removeToken(): void {
  Cookies.remove(TOKEN_KEY)
}

// 用户名相关
export function getUsername(): string {
  return Cookies.get(USERNAME_KEY) || ''
}

export function setUsername(username: string, expires?: number): void {
  const options = expires ? { expires } : undefined
  Cookies.set(USERNAME_KEY, username, options)
}

export function removeUsername(): void {
  Cookies.remove(USERNAME_KEY)
}

// 密码相关
export function getPassword(): string {
  const encryptedPassword = Cookies.get(PASSWORD_KEY) || ''
  return encryptedPassword ? decrypt(encryptedPassword) : ''
}

export function setPassword(password: string, expires?: number): void {
  const encryptedPassword = encrypt(password)
  const options = expires ? { expires } : undefined
  Cookies.set(PASSWORD_KEY, encryptedPassword, options)
}

export function removePassword(): void {
  Cookies.remove(PASSWORD_KEY)
}

// 记住我相关
export function getRememberMe(): boolean {
  const rememberMe = Cookies.get(REMEMBER_ME_KEY)
  return rememberMe === 'true'
}

export function setRememberMe(rememberMe: boolean, expires?: number): void {
  const options = expires ? { expires } : undefined
  Cookies.set(REMEMBER_ME_KEY, String(rememberMe), options)
}

export function removeRememberMe(): void {
  Cookies.remove(REMEMBER_ME_KEY)
}

// 清除所有认证相关的 Cookie
export function clearAuthCookies(): void {
  removeToken()
  removeUsername()
  removePassword()
  removeRememberMe()
}

// 设置记住密码相关 Cookie
export function setRememberMeCookies(username: string, password: string, rememberMe: boolean): void {
  if (rememberMe) {
    setUsername(username, 30) // 30天过期
    setPassword(password, 30)
    setRememberMe(true, 30)
  } else {
    removeUsername()
    removePassword()
    removeRememberMe()
  }
}

// 获取记住密码相关的登录信息
export function getRememberMeCookies(): {
  username: string
  password: string
  rememberMe: boolean
} {
  return {
    username: getUsername() || 'admin',
    password: getPassword() || 'admin123',
    rememberMe: getRememberMe()
  }
}