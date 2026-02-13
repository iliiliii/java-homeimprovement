/**
 * 游客模式配置
 * 用于配置游客用户可以查看的演示项目ID
 */

import { getBaseUrl } from '@/utils/request'

// 游客演示项目ID配置（从服务端获取后缓存）
let GUEST_DEMO_PROJECT_IDS = []
let DEFAULT_GUEST_PROJECT_ID = ''
let CONFIG_LOADED = false
let CONFIG_LOADING = false
let LOAD_CALLBACKS = []

/**
 * 从服务端加载游客演示项目配置
 * @returns {Promise<void>}
 */
const loadGuestConfig = async () => {
  // 如果已经加载过，直接返回
  if (CONFIG_LOADED) {
    return Promise.resolve()
  }
  
  // 如果正在加载，等待加载完成
  if (CONFIG_LOADING) {
    return new Promise((resolve) => {
      LOAD_CALLBACKS.push(resolve)
    })
  }
  
  CONFIG_LOADING = true
  
  try {
    console.log('[Guest Config] 开始加载游客演示项目配置')
    
    // 使用 request.js 中的 getBaseUrl() 获取正确的API地址
    const baseUrl = getBaseUrl()
    const apiUrl = `${baseUrl}/app/guest/demo-projects`
    
    console.log('[Guest Config] API地址:', apiUrl)
    
    const response = await uni.request({
      url: apiUrl,
      method: 'GET',
      timeout: 10000
    })
    
    if (response.statusCode === 200 && response.data.code === 200) {
      const data = response.data.data
      GUEST_DEMO_PROJECT_IDS = data.projectIds || []
      DEFAULT_GUEST_PROJECT_ID = data.defaultProjectId || ''
      CONFIG_LOADED = true
      
      console.log('[Guest Config] 配置加载成功:', {
        projectIds: GUEST_DEMO_PROJECT_IDS,
        defaultProjectId: DEFAULT_GUEST_PROJECT_ID,
        count: data.count
      })
      
      // 通知所有等待的回调
      LOAD_CALLBACKS.forEach(callback => callback())
      LOAD_CALLBACKS = []
    } else {
      console.error('[Guest Config] 配置加载失败:', response.data)
      throw new Error('加载配置失败')
    }
  } catch (error) {
    console.error('[Guest Config] 配置加载异常:', error)
    
    // ===== 临时方案：使用硬编码的项目ID =====
    // TODO: 后端部署后删除这段代码
    console.warn('[Guest Config] API调用失败，使用硬编码的演示项目ID（临时方案）')
    GUEST_DEMO_PROJECT_IDS = ['9fa800b545b445e4b699b1598bec4619'] // 替换为实际的演示项目ID
    DEFAULT_GUEST_PROJECT_ID = '9fa800b545b445e4b699b1598bec4619' // 替换为实际的演示项目ID
    CONFIG_LOADED = true
    console.log('[Guest Config] 使用硬编码配置:', {
      projectIds: GUEST_DEMO_PROJECT_IDS,
      defaultProjectId: DEFAULT_GUEST_PROJECT_ID
    })
    // ===== 临时方案结束 =====
    
    // 通知所有等待的回调
    LOAD_CALLBACKS.forEach(callback => callback())
    LOAD_CALLBACKS = []
  } finally {
    CONFIG_LOADING = false
  }
}

/**
 * 判断当前用户是否为游客（包括未登录用户）
 * @returns {boolean}
 */
export const isGuestUser = () => {
  try {
    // 检查是否已登录
    const token = uni.getStorageSync('token')
    const isLoggedIn = !!(token && token !== '' && token !== 'null')
    
    // 未登录用户视为游客
    if (!isLoggedIn) {
      return true
    }
    
    // 检查是否是未登录游客模式
    const guestMode = uni.getStorageSync('guestMode') === true
    
    // 检查是否是已登录的游客用户
    const userType = uni.getStorageSync('userType')
    const isGuestType = userType === 'guest'
    
    return guestMode || isGuestType
  } catch (error) {
    console.error('[Guest Config] 判断游客状态失败:', error)
    return true // 出错时默认为游客
  }
}

/**
 * 判断是否为未登录的游客（没有token）
 * @returns {boolean}
 */
export const isUnloggedGuest = () => {
  try {
    const token = uni.getStorageSync('token')
    const isLoggedIn = !!(token && token !== '' && token !== 'null')
    return !isLoggedIn
  } catch (error) {
    console.error('[Guest Config] 判断未登录状态失败:', error)
    return true
  }
}

/**
 * 获取游客用户应该使用的项目ID
 * 如果是游客用户，返回演示项目ID；否则返回null
 * @returns {Promise<string|null>}
 */
export const getGuestProjectId = async () => {
  if (!isGuestUser()) {
    return null
  }
  
  // 确保配置已加载
  await loadGuestConfig()
  
  if (DEFAULT_GUEST_PROJECT_ID) {
    console.log('[Guest Config] 游客用户，使用演示项目ID:', DEFAULT_GUEST_PROJECT_ID)
    return DEFAULT_GUEST_PROJECT_ID
  }
  
  console.warn('[Guest Config] 未找到默认演示项目ID')
  return null
}

/**
 * 获取所有游客演示项目ID列表
 * @returns {Promise<string[]>}
 */
export const getGuestProjectIds = async () => {
  if (!isGuestUser()) {
    return []
  }
  
  // 确保配置已加载
  await loadGuestConfig()
  
  return GUEST_DEMO_PROJECT_IDS
}

/**
 * 获取当前应该使用的项目ID
 * 优先级：URL参数 > Store中的项目ID > 游客演示项目ID
 * @param {string} urlProjectId - URL参数中的项目ID
 * @param {string} storeProjectId - Store中保存的项目ID
 * @returns {Promise<string>}
 */
export const getCurrentProjectId = async (urlProjectId, storeProjectId) => {
  // 优先使用URL参数
  if (urlProjectId) {
    return urlProjectId
  }
  
  // 其次使用Store中的项目ID
  if (storeProjectId) {
    return storeProjectId
  }
  
  // 如果是游客用户，使用演示项目ID
  const guestProjectId = await getGuestProjectId()
  if (guestProjectId) {
    return guestProjectId
  }
  
  return ''
}

/**
 * 预加载游客配置（在应用启动时调用）
 * 无论用户是否登录都会预加载，提升后续页面加载速度
 * @returns {Promise<void>}
 */
export const preloadGuestConfig = async () => {
  console.log('[Guest Config] 预加载配置开始')
  
  try {
    // 直接调用加载函数，不检查是否为游客
    // 这样可以提前缓存配置，提升后续页面加载速度
    await loadGuestConfig()
    console.log('[Guest Config] 预加载配置成功')
  } catch (error) {
    console.error('[Guest Config] 预加载配置失败:', error)
    // 预加载失败不影响应用启动
  }
}
