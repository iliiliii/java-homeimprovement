/**
 * 小程序图片压缩工具
 * 使用 uni.compressImage API 进行图片压缩
 * 与后台管理的 useImageCompress 保持相似的接口设计
 */

/**
 * 根据文件大小智能获取压缩质量
 * @param {number} fileSize - 文件大小（字节）
 * @returns {number} 压缩质量 (0-100)
 */
export const getCompressQuality = (fileSize) => {
  // 小于 500KB: 高质量 (90%)
  if (fileSize < 500 * 1024) {
    return 90
  }
  // 500KB - 2MB: 适中质量 (70%)
  if (fileSize < 2 * 1024 * 1024) {
    return 70
  }
  // 2MB - 5MB: 低质量 (50%)
  if (fileSize < 5 * 1024 * 1024) {
    return 50
  }
  // 大于 5MB: 超低质量 (30%)
  return 30
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的大小
 */
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 获取图片文件信息
 * @param {string} filePath - 图片路径
 * @returns {Promise<Object>} 文件信息
 */
export const getImageInfo = (filePath) => {
  return new Promise((resolve, reject) => {
    // 获取文件信息
    uni.getFileInfo({
      filePath,
      success: (fileInfo) => {
        // 获取图片尺寸信息
        uni.getImageInfo({
          src: filePath,
          success: (imageInfo) => {
            resolve({
              size: fileInfo.size,
              width: imageInfo.width,
              height: imageInfo.height,
              type: imageInfo.type,
              path: filePath
            })
          },
          fail: (err) => {
            // 即使获取图片信息失败，也返回文件大小
            resolve({
              size: fileInfo.size,
              path: filePath
            })
          }
        })
      },
      fail: reject
    })
  })
}

/**
 * 压缩图片
 * @param {string} filePath - 原始图片路径
 * @param {Object} options - 压缩选项
 * @param {number} options.quality - 压缩质量 (0-100)，默认根据文件大小智能计算
 * @param {number} options.maxWidth - 最大宽度，默认 1920
 * @param {number} options.maxHeight - 最大高度，默认 1920
 * @param {boolean} options.enableSmartCompression - 是否启用智能压缩，默认 true
 * @returns {Promise<Object>} 压缩结果
 */
export const compressImage = async (filePath, options = {}) => {
  try {
    // 获取原始文件信息
    const originalInfo = await getImageInfo(filePath)
    const originalSize = originalInfo.size
    
    // 默认选项
    const defaultOptions = {
      quality: null, // null 表示使用智能压缩
      maxWidth: 1920,
      maxHeight: 1920,
      enableSmartCompression: true,
      ...options
    }
    
    // 智能压缩：根据文件大小调整质量
    let quality = defaultOptions.quality
    if (quality === null && defaultOptions.enableSmartCompression) {
      quality = getCompressQuality(originalSize)
    } else if (quality === null) {
      quality = 80 // 默认质量
    }
    
    // 如果文件已经很小（小于 200KB），不需要压缩
    if (originalSize < 200 * 1024) {
      return {
        success: true,
        tempFilePath: filePath,
        originalSize,
        compressedSize: originalSize,
        compressionRatio: 0,
        quality,
        skipped: true,
        message: '文件已经很小，无需压缩'
      }
    }
    
    // 执行压缩
    return new Promise((resolve) => {
      // #ifdef MP-WEIXIN
      // 微信小程序使用 compressImage
      uni.compressImage({
        src: filePath,
        quality,
        success: async (res) => {
          try {
            // 获取压缩后文件信息
            const compressedInfo = await getImageInfo(res.tempFilePath)
            const compressedSize = compressedInfo.size
            const compressionRatio = ((1 - compressedSize / originalSize) * 100).toFixed(2)
            
            resolve({
              success: true,
              tempFilePath: res.tempFilePath,
              originalSize,
              compressedSize,
              originalSizeMB: (originalSize / 1024 / 1024).toFixed(2),
              compressedSizeMB: (compressedSize / 1024 / 1024).toFixed(2),
              compressionRatio: parseFloat(compressionRatio),
              quality,
              skipped: false
            })
          } catch (e) {
            // 获取压缩后信息失败，但压缩成功
            resolve({
              success: true,
              tempFilePath: res.tempFilePath,
              originalSize,
              quality,
              skipped: false
            })
          }
        },
        fail: (err) => {
          console.warn('图片压缩失败，使用原图:', err)
          resolve({
            success: true,
            tempFilePath: filePath,
            originalSize,
            compressedSize: originalSize,
            compressionRatio: 0,
            quality,
            skipped: true,
            message: '压缩失败，使用原图'
          })
        }
      })
      // #endif
      
      // #ifndef MP-WEIXIN
      // 其他平台（H5、App）使用 canvas 压缩
      compressWithCanvas(filePath, quality, defaultOptions.maxWidth, defaultOptions.maxHeight)
        .then(async (compressedPath) => {
          try {
            const compressedInfo = await getImageInfo(compressedPath)
            const compressedSize = compressedInfo.size
            const compressionRatio = ((1 - compressedSize / originalSize) * 100).toFixed(2)
            
            resolve({
              success: true,
              tempFilePath: compressedPath,
              originalSize,
              compressedSize,
              originalSizeMB: (originalSize / 1024 / 1024).toFixed(2),
              compressedSizeMB: (compressedSize / 1024 / 1024).toFixed(2),
              compressionRatio: parseFloat(compressionRatio),
              quality,
              skipped: false
            })
          } catch (e) {
            resolve({
              success: true,
              tempFilePath: compressedPath,
              originalSize,
              quality,
              skipped: false
            })
          }
        })
        .catch(() => {
          // 压缩失败，返回原图
          resolve({
            success: true,
            tempFilePath: filePath,
            originalSize,
            compressedSize: originalSize,
            compressionRatio: 0,
            quality,
            skipped: true,
            message: '压缩失败，使用原图'
          })
        })
      // #endif
    })
  } catch (error) {
    console.error('图片压缩错误:', error)
    return {
      success: false,
      error: error.message || '压缩失败',
      tempFilePath: filePath
    }
  }
}

/**
 * 使用 Canvas 压缩图片（用于 H5 和 App 平台）
 * @param {string} filePath - 图片路径
 * @param {number} quality - 压缩质量 (0-100)
 * @param {number} maxWidth - 最大宽度
 * @param {number} maxHeight - 最大高度
 * @returns {Promise<string>} 压缩后的图片路径
 */
const compressWithCanvas = (filePath, quality, maxWidth, maxHeight) => {
  return new Promise((resolve, reject) => {
    uni.getImageInfo({
      src: filePath,
      success: (imageInfo) => {
        let { width, height } = imageInfo
        
        // 计算缩放比例
        let scale = 1
        if (width > maxWidth || height > maxHeight) {
          scale = Math.min(maxWidth / width, maxHeight / height)
          width = Math.floor(width * scale)
          height = Math.floor(height * scale)
        }
        
        // 创建 canvas 上下文
        const canvas = uni.createOffscreenCanvas({ type: '2d', width, height })
        const ctx = canvas.getContext('2d')
        
        // 加载图片
        const img = canvas.createImage()
        img.onload = () => {
          // 绘制图片
          ctx.drawImage(img, 0, 0, width, height)
          
          // 导出为 base64 或临时文件
          // #ifdef H5
          const dataUrl = canvas.toDataURL('image/jpeg', quality / 100)
          resolve(dataUrl)
          // #endif
          
          // #ifdef APP-PLUS
          canvas.toTempFilePath({
            quality: quality / 100,
            fileType: 'jpg',
            success: (res) => resolve(res.tempFilePath),
            fail: reject
          })
          // #endif
        }
        img.onerror = reject
        img.src = filePath
      },
      fail: reject
    })
  })
}

/**
 * 批量压缩图片
 * @param {Array<string>} filePaths - 图片路径数组
 * @param {Object} options - 压缩选项
 * @returns {Promise<Array>} 压缩结果数组
 */
export const compressImages = async (filePaths, options = {}) => {
  const results = []
  for (const filePath of filePaths) {
    const result = await compressImage(filePath, options)
    results.push(result)
  }
  return results
}

export default {
  compressImage,
  compressImages,
  getCompressQuality,
  formatFileSize,
  getImageInfo
}
