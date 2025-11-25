import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import { compressImage, isImageFile } from './useImageCompress'

/**
 * 图片上传压缩组合式函数
 * 统一处理图片压缩和上传逻辑，避免重复代码和错误
 *
 * 使用说明：
 * 1. 组件中定义：const { createUploadHandler, handleBeforeUpload, createValidator } = useImageUpload()
 * 2. 创建校验函数：const validator = createValidator({ fileType: ['jpg', 'png'], fileSize: 5 })
 * 3. 在before-upload中调用：if (!await handleBeforeUpload(file, compressConfig, compressedFilesRef)) return false
 * 4. 创建上传处理器：const httpRequest = createUploadHandler({ beforeUpload: validator, ... })
 * 5. 在el-upload中使用：<el-upload :http-request="httpRequest" />
 */
export function useImageUpload() {
  /**
   * 创建自定义上传请求处理器
   * @param {Object} options - 配置选项
   * @param {Function} options.beforeUpload - 上传前校验钩子
   * @param {Function} options.onSuccess - 上传成功回调
   * @param {Function} options.onError - 上传失败回调
   * @param {Object} options.compressConfig - 压缩配置
   * @param {Ref} options.compressedFilesRef - 压缩文件缓存引用
   * @param {Ref} options.uploadingFilesRef - 上传文件信息引用（可选）
   * @returns {Function} http-request处理函数
   */
  const createUploadHandler = (options) => {
    const {
      beforeUpload,
      onSuccess,
      onError,
      compressConfig = {},
      compressedFilesRef,
      uploadingFilesRef
    } = options

    /**
     * 自定义上传请求处理函数
     * @param {Object} uploadOptions - el-upload传入的上传选项
     */
    const handleHttpRequest = async (uploadOptions) => {
      const { action, file, onSuccess: elOnSuccess, onError: elOnError, data } = uploadOptions

      try {
        // 1. 上传前校验
        if (beforeUpload && !await beforeUpload(file)) {
          return
        }

        // 2. 显示上传loading
        ElMessage.loading?.({
          message: '正在上传文件，请稍候...',
          duration: 0
        })

        // 3. 获取要上传的文件（压缩后或原始）
        const fileToUpload = compressedFilesRef?.value?.get(file.uid) || file

        // 4. 创建FormData
        const formData = new FormData()
        formData.append('file', fileToUpload)

        // 5. 添加额外数据
        if (data) {
          Object.keys(data).forEach(key => {
            formData.append(key, data[key])
          })
        }

        // 6. 发起请求
        const response = await fetch(action, {
          method: 'POST',
          headers: {
            Authorization: "Bearer " + getToken()
          },
          body: formData
        })

        const result = await response.json()

        // 7. 处理响应
        if (result.code === 200) {
          // 清理压缩缓存
          compressedFilesRef?.value?.delete(file.uid)
          uploadingFilesRef?.value?.delete(file.uid)

          // 调用成功回调
          if (onSuccess) {
            await onSuccess(result, file)
          }
          elOnSuccess(result, file)
        } else {
          throw new Error(result.msg || '上传失败')
        }
      } catch (error) {
        console.error('上传失败:', error)

        // 清理缓存
        compressedFilesRef?.value?.delete(uploadOptions.file.uid)
        uploadingFilesRef?.value?.delete(uploadOptions.file.uid)

        // 调用错误回调
        if (onError) {
          onError(error)
        } else {
          ElMessage.error('上传失败: ' + (error.message || '未知错误'))
        }
        elOnError(error)
      } finally {
        // 关闭loading
        ElMessage.close?.('loading')
      }
    }

    return handleHttpRequest
  }

  /**
   * 处理上传前压缩
   * @param {File} file - 文件对象
   * @param {Object} config - 压缩配置
   * @param {Ref} compressedFilesRef - 压缩文件缓存引用
   * @param {Function} showMessage - 显示消息的函数
   * @returns {boolean} 是否通过校验
   */
  const handleBeforeUpload = async (file, config, compressedFilesRef, showMessage = ElMessage) => {
    // 压缩处理
    if (compressConfig.compress && isImageFile(file)) {
      try {
        showMessage.loading?.({
          message: '正在压缩图片，请稍候...',
          duration: 0
        })

        const compressResult = await compressImage(file, compressConfig)

        if (compressResult.success) {
          // ✅ 关键修复：将Blob转换为File对象
          const compressedBlob = compressResult.file
          const newFile = new File([compressedBlob], file.name, {
            type: compressedBlob.type || file.type,
            lastModified: Date.now()
          })

          // 保存压缩后的文件到缓存
          compressedFilesRef.value.set(file.uid, newFile)

          // 显示成功消息
          showMessage.success?.(`图片压缩成功：${compressResult.compressionRatio}%`)
          return true
        } else {
          throw new Error(compressResult.error || '压缩失败')
        }
      } catch (error) {
        showMessage.close?.('loading')
        showMessage.error?.('图片压缩失败: ' + error.message)
        return false
      }
    }

    return true
  }

  /**
   * 创建上传前校验函数
   * @param {Object} validationConfig - 校验配置
   * @param {Array} validationConfig.fileType - 允许的文件类型
   * @param {Number} validationConfig.fileSize - 文件大小限制(MB)
   * @param {Function} validationConfig.onValidationError - 校验失败回调
   * @returns {Function} 校验函数
   */
  const createValidator = (validationConfig = {}) => {
    const { fileType = [], fileSize = 5, onValidationError } = validationConfig

    return (file) => {
      // 校验文件类型
      if (fileType.length) {
        const fileName = file.name.split('.')
        const fileExt = fileName[fileName.length - 1].toLowerCase()
        const isValidType = fileType.some(type => {
          if (typeof type === 'string') {
            return file.type.includes(type) || fileExt === type.toLowerCase()
          }
          return false
        })

        if (!isValidType) {
          const errorMsg = `文件格式不正确，请上传${fileType.join('/').toUpperCase()}格式文件!`
          onValidationError?.(errorMsg)
          return false
        }
      }

      // 校验文件大小
      if (fileSize) {
        const isValidSize = file.size / 1024 / 1024 < fileSize
        if (!isValidSize) {
          const errorMsg = `上传文件大小不能超过 ${fileSize} MB!`
          onValidationError?.(errorMsg)
          return false
        }
      }

      // 校验文件名（避免特殊字符）
      if (file.name.includes(',')) {
        const errorMsg = '文件名不正确，不能包含英文逗号!'
        onValidationError?.(errorMsg)
        return false
      }

      return true
    }
  }

  return {
    createUploadHandler,
    handleBeforeUpload,
    createValidator
  }
}
