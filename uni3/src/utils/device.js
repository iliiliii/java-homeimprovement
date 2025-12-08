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
    // 生成UUID格式的设备ID
    deviceId = 'device_' + Date.now() + '_' + generateRandomString(16)
    uni.setStorageSync('deviceId', deviceId)
  }
  
  return deviceId
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
 * 生成随机字符串
 * @param {Number} length - 字符串长度
 * @returns {String} 随机字符串
 */
const generateRandomString = (length) => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
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
