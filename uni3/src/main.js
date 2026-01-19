import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import uviewPlus from 'uview-plus'
import { hasPagePermission, isLoggedIn } from '@/utils/permission'
import { useUserStore } from '@/store/user'

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  
  app.use(pinia)
  app.use(uviewPlus)
  
  // 初始化用户状态（从本地存储恢复）
  const userStore = useUserStore(pinia)
  userStore.initFromStorage()
  
  return {
    app,
    pinia
  }
}

// ==================== 路由守卫 ====================

// 不需要登录的页面
const whiteList = [
  '/pages/login/index',
  '/pages/protocol/index',
  '/pages/privacy/index',
  '/pages/test/wechat-binding'
]

// 检查是否在白名单中
const isInWhiteList = (url) => {
  return whiteList.some(path => url.startsWith(path))
}

// 拦截 navigateTo
uni.addInterceptor('navigateTo', {
  invoke(args) {
    const url = args.url.split('?')[0]
    
    // 白名单页面直接放行
    if (isInWhiteList(url)) {
      return true
    }
    
    // 检查登录状态
    if (!isLoggedIn()) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      uni.reLaunch({ url: '/pages/login/index' })
      return false
    }
    
    // 检查页面权限
    if (!hasPagePermission(url)) {
      uni.showToast({ title: '该功能仅员工可用', icon: 'none' })
      return false
    }
    
    return true
  }
})

// 拦截 redirectTo
uni.addInterceptor('redirectTo', {
  invoke(args) {
    const url = args.url.split('?')[0]
    
    if (isInWhiteList(url)) {
      return true
    }
    
    if (!isLoggedIn()) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      uni.reLaunch({ url: '/pages/login/index' })
      return false
    }
    
    if (!hasPagePermission(url)) {
      uni.showToast({ title: '该功能仅员工可用', icon: 'none' })
      return false
    }
    
    return true
  }
})

// 拦截 reLaunch
uni.addInterceptor('reLaunch', {
  invoke(args) {
    const url = args.url.split('?')[0]
    
    if (isInWhiteList(url)) {
      return true
    }
    
    if (!isLoggedIn()) {
      // reLaunch到登录页，不拦截
      if (url === '/pages/login/index') {
        return true
      }
      uni.showToast({ title: '请先登录', icon: 'none' })
      args.url = '/pages/login/index'
      return true
    }
    
    if (!hasPagePermission(url)) {
      uni.showToast({ title: '该功能仅员工可用', icon: 'none' })
      return false
    }
    
    return true
  }
})

// 拦截 switchTab
uni.addInterceptor('switchTab', {
  invoke(args) {
    const url = args.url.split('?')[0]
    
    if (!isLoggedIn()) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      uni.reLaunch({ url: '/pages/login/index' })
      return false
    }
    
    if (!hasPagePermission(url)) {
      uni.showToast({ title: '该功能仅员工可用', icon: 'none' })
      return false
    }
    
    return true
  }
})

