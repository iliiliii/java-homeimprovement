import imageCompression from 'browser-image-compression'

/**
 * 图片压缩 Composable
 * 提供图片压缩和校验功能
 *
 * 重要说明：
 * 1. browser-image-compression库返回Blob对象，不是File对象
 * 2. Blob需要转换为File后才能用于FormData上传
 * 3. 压缩后的文件需要通过http-request机制才能实际生效
 */

/**
 * 修复关键说明：
 * ❌ 错误方式：file = compressResult.file
 *   - 只是修改函数内局部变量，不影响el-upload实际使用的文件
 *
 * ✅ 正确方式：
 *   1. 在before-upload中保存压缩后的File到ref/Map
 *   2. 使用http-request自定义上传逻辑
 *   3. 在http-request中从缓存取压缩后的文件上传
 */

export function useImageCompress() {
  /**
   * 判断是否为图片文件
   * @param {File} file - 文件对象
   * @returns {boolean} 是否为图片
   */
  const isImageFile = (file) => {
    return file && file.type && file.type.startsWith('image/')
  }

  /**
   * 根据文件大小智能获取压缩质量
   * @param {number} fileSize - 文件大小（字节）
   * @returns {number} 压缩质量 (0-1)
   */
  const getCompressQuality = (fileSize) => {
    // 小于 500KB: 高质量 (90%)
    if (fileSize < 500 * 1024) {
      return 0.9
    }
    // 500KB - 2MB: 适中质量 (70%)
    if (fileSize < 2 * 1024 * 1024) {
      return 0.7
    }
    // 2MB - 5MB: 低质量 (50%)
    if (fileSize < 5 * 1024 * 1024) {
      return 0.5
    }
    // 大于 5MB: 超低质量 (30%)
    return 0.3
  }

  /**
   * 压缩图片
   * @param {File} file - 原始图片文件
   * @param {Object} options - 压缩选项
   * @param {number} options.maxSizeMB - 最大文件大小(MB)
   * @param {number} options.maxWidthOrHeight - 最大宽度或高度
   * @param {number} options.initialQuality - 压缩质量 (0-1)
   * @param {boolean} options.useWebWorker - 是否使用WebWorker
   * @param {boolean} options.enableSmartCompression - 是否启用智能压缩
   * @returns {Promise<Object>} 压缩结果
   */
  const compressImage = async (file, options = {}) => {
    // 验证文件
    if (!file) {
      return {
        success: false,
        error: '文件不存在'
      }
    }

    if (!isImageFile(file)) {
      return {
        success: false,
        error: '不是有效的图片文件'
      }
    }

    try {
      // 默认选项
      const defaultOptions = {
        maxSizeMB: 1,                           // 默认最大1MB
        maxWidthOrHeight: 1920,                 // 默认最大边长1920px
        initialQuality: 0.8,                    // 默认质量80%
        useWebWorker: true,                     // 使用WebWorker
        enableSmartCompression: true,           // 启用智能压缩
        ...options
      }

      // 智能压缩：根据文件大小调整质量
      if (defaultOptions.enableSmartCompression) {
        defaultOptions.initialQuality = getCompressQuality(file.size)
      }

      // 记录原始大小
      const originalSize = file.size
      const originalSizeMB = (originalSize / 1024 / 1024).toFixed(2)

      // 执行压缩
      const compressedFile = await imageCompression(file, defaultOptions)

      // 计算压缩后大小
      const compressedSize = compressedFile.size
      const compressedSizeMB = (compressedSize / 1024 / 1024).toFixed(2)

      // 计算压缩率
      const compressionRatio = ((1 - compressedSize / originalSize) * 100).toFixed(2)

      return {
        success: true,
        file: compressedFile,
        originalSize,
        compressedSize,
        originalSizeMB: parseFloat(originalSizeMB),
        compressedSizeMB: parseFloat(compressedSizeMB),
        compressionRatio: parseFloat(compressionRatio),
        quality: defaultOptions.initialQuality,
        settings: {
          maxSizeMB: defaultOptions.maxSizeMB,
          maxWidthOrHeight: defaultOptions.maxWidthOrHeight,
          initialQuality: defaultOptions.initialQuality
        }
      }
    } catch (error) {
      return {
        success: false,
        error: error.message || '压缩失败'
      }
    }
  }

  /**
   * 格式化文件大小
   * @param {number} bytes - 字节数
   * @returns {string} 格���化后的大小
   */
  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  /**
   * 压缩图片并返回统计信息
   * @param {File} file - 原始文件
   * @param {Object} options - 压缩选项
   * @returns {Promise<Object>} 压缩结果和统计信息
   */
  const compressImageWithStats = async (file, options = {}) => {
    const result = await compressImage(file, options)

    if (result.success) {
      return {
        ...result,
        stats: {
          original: {
            size: result.originalSize,
            sizeText: formatFileSize(result.originalSize),
            quality: '100%'
          },
          compressed: {
            size: result.compressedSize,
            sizeText: formatFileSize(result.compressedSize),
            quality: Math.round(result.quality * 100) + '%'
          },
          improvement: {
            ratio: result.compressionRatio,
            savedBytes: result.originalSize - result.compressedSize,
            savedText: formatFileSize(result.originalSize - result.compressedSize)
          }
        }
      }
    }

    return result
  }

  return {
    compressImage,
    compressImageWithStats,
    isImageFile,
    getCompressQuality,
    formatFileSize
  }
}
