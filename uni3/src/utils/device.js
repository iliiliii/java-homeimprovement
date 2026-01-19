/**
 * 设备ID生成和管理
 */

/**
 * 生成设备唯一标识
 * @returns {String} 设备ID
 */
export const getDeviceId = () => {
  let deviceId = uni.getStorageSync('deviceId')
  
  if (!deviceId) {
    // 获取设备信息用于生成更安全的设备ID
    const deviceInfo = getDeviceInfo()
    const timestamp = Date.now()
    const random = generateSecureRandomString(32)
    
    // 生成设备指纹
    const fingerprint = generateDeviceFingerprint(deviceInfo)
    
    // 组合生成设备ID
    deviceId = `wx_${fingerprint}_${timestamp}_${random}`
    
    uni.setStorageSync('deviceId', deviceId)
    console.log('[Device] 生成新设备ID:', deviceId.substring(0, 20) + '...')
  }
  
  return deviceId
}

/**
 * 生成设备指纹
 * @param {Object} deviceInfo - 设备信息
 * @returns {String} 设备指纹
 */
const generateDeviceFingerprint = (deviceInfo) => {
  const {
    platform = '',
    model = '',
    brand = '',
    system = '',
    screenWidth = 0,
    screenHeight = 0
  } = deviceInfo
  
  // 组合设备特征
  const features = `${platform}_${model}_${brand}_${system}_${screenWidth}x${screenHeight}`
  
  // 简单哈希（在小程序环境中不能使用crypto）
  let hash = 0
  for (let i = 0; i < features.length; i++) {
    const char = features.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash // 转换为32位整数
  }
  
  return Math.abs(hash).toString(36)
}

/**
 * 获取设备信息
 * @returns {Object} 设备信息对象
 */
export const getDeviceInfo = () => {
  try {
    const systemInfo = uni.getSystemInfoSync()
    return {
      platform: systemInfo.platform,        // 平台：ios/android
      system: systemInfo.system,            // 系统版本
      model: systemInfo.model,              // 设备型号
      version: systemInfo.version,          // 微信版本号
      SDKVersion: systemInfo.SDKVersion,    // 基础库版本
      brand: systemInfo.brand,              // 设备品牌
      screenWidth: systemInfo.screenWidth,  // 屏幕宽度
      screenHeight: systemInfo.screenHeight // 屏幕高度
    }
  } catch (error) {
    console.error('获取设备信息失败', error)
    return {}
  }
}

/**
 * 生成安全随机字符串
 * @param {Number} length - 字符串长度
 * @returns {String} 随机字符串
 */
const generateSecureRandomString = (length) => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  
  // 使用更安全的随机数生成
  for (let i = 0; i < length; i++) {
    // 结合时间戳和Math.random()增加随机性
    const randomIndex = Math.floor((Math.random() * Date.now()) % chars.length)
    result += chars.charAt(randomIndex)
  }
  
  return result
}

/**
 * 生成随机字符串（保持向后兼容）
 * @param {Number} length - 字符串长度
 * @returns {String} 随机字符串
 */
const generateRandomString = (length) => {
  return generateSecureRandomString(length)
}

/**
 * 获取网络类型
 * @returns {Promise<String>} 网络类型
 */
export const getNetworkType = () => {
  return new Promise((resolve) => {
    uni.getNetworkType({
      success: (res) => {
        resolve(res.networkType)
      },
      fail: () => {
        resolve('unknown')
      }
    })
  })
}
