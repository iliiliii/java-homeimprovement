/**
 * 统一的图片上传管理 Hook
 * 封装图片上传的所有重复逻辑，包括状态管理、进度跟踪、错误处理、表单提交等
 */

import { ref, reactive, computed } from 'vue'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

/**
 * 图片上传配置选项
 * @typedef {Object} UploadOptions
 * @property {number} maxCount - 最大上传数量，默认20
 * @property {number} maxSize - 最大文件大小(MB)，默认10
 * @property {boolean} compress - 是否启用压缩，默认true
 * @property {number} compressQuality - 压缩质量(0-1)，默认0.8
 * @property {number} compressMaxSize - 压缩后最大大小(MB)，默认3
 * @property {number} compressMaxWidthOrHeight - 压缩最大宽高，默认1920
 * @property {string} uploadUrl - 上传地址，默认 /common/upload
 * @property {Object} uploadHeaders - 请求头，默认包含Authorization
 * @property {boolean} autoCheckNetwork - 是否自动检查网络，默认true
 * @property {boolean} autoCheckAuth - 是否自动检查认证，默认true
 * @property {Function} onSuccess - 成功回调
 * @property {Function} onError - 失败回调
 * @property {Function} onStatusChange - 状态变化回调
 */

/**
 * @param {UploadOptions} options - 上传配置选项
 * @returns {Object} 图片上传管理对象
 */
export function useUploadManager(options = {}) {
  // ==================== 默认配置 ====================
  const defaultOptions = {
    maxCount: 20,
    maxSize: 10,
    compress: true,
    compressQuality: 0.8,
    compressMaxSize: 3,
    compressMaxWidthOrHeight: 1920,
    uploadUrl: import.meta.env.VITE_APP_BASE_API + '/common/upload',
    uploadHeaders: { Authorization: "Bearer " + getToken() },
    autoCheckNetwork: true,
    autoCheckAuth: true,
    onSuccess: null,
    onError: null,
    onStatusChange: null
  }

  const config = { ...defaultOptions, ...options }

  // ==================== 响应式状态 ====================
  // 上传状态 - 增强版本，支持详细进度信息
  const uploadStatus = ref({
    isAllUploaded: true,
    totalFiles: 0,
    uploadedFiles: 0,
    uploadingFiles: [], // 正在上传的文件列表 {uid, name, progress}
    failedFiles: [],    // 上传失败的文件列表 {uid, name, error}
    overallProgress: 0, // 总体进度百分比 (0-100)
    maxFailedFiles: 50, // 失败文件最大保存数量，防止内存泄漏
    fileLocks: new Set() // 文件状态锁，防止并发操作冲突
  })

  // 提交状态
  const submitting = ref(false)

  // 错误状态
  const error = ref(null)

  // 上传组件引用
  const uploadRef = ref(null)

  // 自动隐藏定时器
  const autoHideTimer = ref(null)

  // 进度显示控制
  const forceShowProgress = ref(false)

  // ==================== 计算属性 ====================
  // 按钮是否应该禁用
  const isSubmitDisabled = computed(() => {
    return submitting.value || !uploadStatus.value.isAllUploaded
  })

  // 上传进度百分比 - 使用状态中的总体进度
  const uploadProgress = computed(() => {
    return uploadStatus.value.overallProgress
  })

  // 当前正在上传的文件数量
  const uploadingCount = computed(() => {
    return uploadStatus.value.uploadingFiles.length
  })

  // 上传失败文件数量
  const failedCount = computed(() => {
    return uploadStatus.value.failedFiles.length
  })

  // 当前正在上传的第一个文件名（用于显示）
  const currentUploadingFileName = computed(() => {
    const files = uploadStatus.value.uploadingFiles
    return files.length > 0 ? files[0].name : ''
  })

  // 是否有错误
  const hasError = computed(() => {
    return error.value !== null
  })

  // 是否应该显示进度条 - 核心逻辑：只在必要时显示
  const shouldShowProgress = computed(() => {
    const { uploadingFiles, failedFiles } = uploadStatus.value

    // 强制显示（如手动触发）
    if (forceShowProgress.value) {
      return true
    }

    // 有正在上传的文件
    if (uploadingFiles.length > 0) {
      return true
    }

    // 有失败的文件需要用户处理
    if (failedFiles.length > 0) {
      return true
    }

    return false
  })

  // 进度显示状态优先级
  const progressDisplayState = computed(() => {
    const { uploadingFiles, failedFiles, overallProgress, isAllUploaded } = uploadStatus.value

    // 优先级1: 失败状态
    if (failedFiles.length > 0) {
      return {
        state: 'failed',
        message: `上传失败 ${failedFiles.length} 个文件`,
        showRetry: true,
        progress: overallProgress
      }
    }

    // 优先级2: 上传中状态
    const activeUploading = uploadingFiles.filter(f => f.progress > 0 && f.progress < 100)
    if (activeUploading.length > 0) {
      return {
        state: 'uploading',
        message: `正在上传 ${activeUploading.length} 个文件`,
        showRetry: false,
        progress: overallProgress
      }
    }

    // 优先级3: 刚完成状态（短暂显示后隐藏）
    if (isAllUploaded && uploadingFiles.length === 0 && failedFiles.length === 0) {
      return {
        state: 'completed',
        message: '上传完成',
        showRetry: false,
        progress: 100
      }
    }

    // 默认: 不显示
    return {
      state: 'hidden',
      message: '',
      showRetry: false,
      progress: 0
    }
  })

  // ==================== 核心功能函数 ====================

  /**
   * 检查网络状态
   */
  function checkNetwork() {
    if (!config.autoCheckNetwork) return true

    if (!navigator.onLine) {
      ElMessage.error('网络连接已断开，请检查网络后重试')
      return false
    }
    return true
  }

  /**
   * 检查认证状态
   */
  function checkAuth() {
    if (!config.autoCheckAuth) return true

    const token = getToken()
    if (!token) {
      ElMessage.error('用户认证已失效，请重新登录')
      return false
    }
    return true
  }

  /**
   * 检查是否可以提交
   */
  function canSubmit() {
    // 检查图片上传状态
    if (!uploadStatus.value.isAllUploaded) {
      ElMessage.warning('请等待图片上传完成后再提交')
      return false
    }

    // 检查网络状态
    if (!checkNetwork()) {
      return false
    }

    // 检查认证状态
    if (!checkAuth()) {
      return false
    }

    return true
  }

  /**
   * 上传状态变化回调 - 增强版本，支持详细进度跟踪
   * @param {Object} status - 状态对象
   */
  function handleUploadStatusChange(status) {
    // 更新基础状态
    uploadStatus.value = {
      ...uploadStatus.value,
      ...status
    }

    // 计算总体进度
    updateOverallProgress()

    // 调用外部回调
    if (config.onStatusChange) {
      config.onStatusChange(uploadStatus.value)
    }
  }

  /**
   * 更新总体进度 - 修复版本，确保进度计算准确
   */
  function updateOverallProgress() {
    const { totalFiles, uploadedFiles, uploadingFiles } = uploadStatus.value

    // 如果没有文件，进度为100%
    if (totalFiles === 0) {
      uploadStatus.value.overallProgress = 100
      uploadStatus.value.isAllUploaded = true
      return
    }

    // 计算已完成的文件数（包括已上传和正在上传完成的）
    const completedFiles = uploadedFiles + uploadingFiles.filter(f => f.progress >= 100).length
    const activeUploadingFiles = uploadingFiles.filter(f => f.progress < 100 && f.progress > 0)

    // 基础进度：已完成文件的占比
    let progress = (completedFiles / totalFiles) * 100

    // 计算正在上传文件的平均进度
    if (activeUploadingFiles.length > 0) {
      const activeProgressSum = activeUploadingFiles.reduce((sum, file) => sum + file.progress, 0)
      const activeProgressAvg = activeProgressSum / activeUploadingFiles.length
      const activeWeight = (activeUploadingFiles.length / totalFiles)

      // 将正在上传的文件进度按权重加到总进度中
      progress += activeProgressAvg * activeWeight
    }

    // 确保进度在合理范围内
    progress = Math.max(0, Math.min(100, progress))

    // 更新状态
    uploadStatus.value.overallProgress = Math.round(progress)

    // 判断是否全部上传完成
    const allCompleted = completedFiles >= totalFiles
    uploadStatus.value.isAllUploaded = allCompleted
  }

  /**
   * 获取文件状态锁
   * @param {string} fileUid - 文件UID
   * @returns {boolean} 是否成功获取锁
   */
  function acquireFileLock(fileUid) {
    if (uploadStatus.value.fileLocks.has(fileUid)) {
      return false // 文件已被锁定
    }
    uploadStatus.value.fileLocks.add(fileUid)
    return true
  }

  /**
   * 释放文件状态锁
   * @param {string} fileUid - 文件UID
   */
  function releaseFileLock(fileUid) {
    uploadStatus.value.fileLocks.delete(fileUid)
  }

  /**
   * 添加文件到上传队列 - 增强版本，支持并发控制
   * @param {Object} file - 文件对象 {uid, name}
   */
  function addToUploadQueue(file) {
    // 尝试获取文件锁，防止重复操作
    if (!acquireFileLock(file.uid)) {
      console.warn(`文件 ${file.name} 正在处理中，跳过重复添加`)
      return false
    }

    try {
      // 检查文件是否已在队列中
      const existingFile = uploadStatus.value.uploadingFiles.find(f => f.uid === file.uid)
      if (!existingFile) {
        uploadStatus.value.uploadingFiles.push({
          uid: file.uid,
          name: file.name,
          progress: 0,
          addedAt: Date.now() // 添加时间戳
        })
        updateOverallProgress()
        return true
      } else {
        // 文件已存在，释放锁
        releaseFileLock(file.uid)
        return false
      }
    } catch (error) {
      // 出现错误时释放锁
      releaseFileLock(file.uid)
      throw error
    }
  }

  /**
   * 更新文件上传进度
   * @param {string} fileUid - 文件UID
   * @param {number} progress - 进度百分比 (0-100)
   */
  function updateFileProgress(fileUid, progress) {
    const file = uploadStatus.value.uploadingFiles.find(f => f.uid === fileUid)
    if (file) {
      // 确保进度值在合理范围内
      file.progress = Math.max(0, Math.min(100, progress))

      // 如果进度达到100%，标记为已完成
      if (file.progress >= 100) {
        // 使用 nextTick 确保状态更新后再标记完成
        setTimeout(() => {
          markFileUploaded(fileUid)
        }, 0)
      } else {
        updateOverallProgress()
      }
    }
  }

  /**
   * 标记文件上传完成 - 增强版本，支持锁管理
   * @param {string} fileUid - 文件UID
   */
  function markFileUploaded(fileUid) {
    const index = uploadStatus.value.uploadingFiles.findIndex(f => f.uid === fileUid)
    if (index > -1) {
      uploadStatus.value.uploadingFiles.splice(index, 1)
      uploadStatus.value.uploadedFiles++

      // 释放文件锁
      releaseFileLock(fileUid)

      updateOverallProgress()
    }
  }

  /**
   * 清理过多的失败文件记录，防止内存泄漏
   */
  function cleanupFailedFiles() {
    if (uploadStatus.value.failedFiles.length > uploadStatus.value.maxFailedFiles) {
      // 保留最近的失败记录，删除最旧的
      const excess = uploadStatus.value.failedFiles.length - uploadStatus.value.maxFailedFiles
      uploadStatus.value.failedFiles.splice(0, excess)
    }
  }

  /**
   * 标记文件上传失败 - 增强版本，支持锁管理
   * @param {string} fileUid - 文件UID
   * @param {string} error - 错误信息
   */
  function markFileFailed(fileUid, error) {
    const index = uploadStatus.value.uploadingFiles.findIndex(f => f.uid === fileUid)
    if (index > -1) {
      const file = uploadStatus.value.uploadingFiles[index]
      uploadStatus.value.uploadingFiles.splice(index, 1)

      // 释放文件锁
      releaseFileLock(fileUid)

      // 检查是否已存在相同uid的失败记录，避免重复
      const existingFailedIndex = uploadStatus.value.failedFiles.findIndex(f => f.uid === fileUid)
      if (existingFailedIndex > -1) {
        // 更新现有记录的错误信息
        uploadStatus.value.failedFiles[existingFailedIndex].error = error
      } else {
        // 添加新的失败记录
        uploadStatus.value.failedFiles.push({
          uid: fileUid,
          name: file.name,
          error,
          timestamp: Date.now() // 添加时间戳用于清理
        })
      }

      // 清理过多的失败记录
      cleanupFailedFiles()
      updateOverallProgress()
    }
  }

  /**
   * 上传成功回调 - 增强版本，管理上传队列
   * @param {Object} data - 成功数据
   */
  function handleUploadSuccess({ response, file, imageUrl }) {
    console.log('图片上传成功:', { response, file, imageUrl })

    // 从上传队列中移除已完成的文件
    markFileUploaded(file.uid)

    // 清除错误状态
    error.value = null

    // 检查是否需要自动隐藏进度显示
    const { uploadingFiles, failedFiles } = uploadStatus.value
    if (uploadingFiles.length === 0 && failedFiles.length === 0) {
      // 所有文件上传完成，延迟隐藏进度显示
      hideProgressAfterDelay()
    }

    // 调用外部回调
    if (config.onSuccess) {
      config.onSuccess({ response, file, imageUrl })
    }
  }

  /**
   * 上传失败回调 - 增强版本，管理上传队列
   * @param {Object} data - 失败数据
   */
  function handleUploadError({ response, file, message }) {
    const errorMessage = message || response?.msg || '图片上传失败'
    console.error('图片上传失败:', errorMessage)

    // 从上传队列中移除失败的文件
    markFileFailed(file.uid, errorMessage)

    // 设置错误状态
    error.value = errorMessage

    // 显示错误提示
    ElMessage.error(errorMessage)

    // 调用外部回调
    if (config.onError) {
      config.onError({ response, file, message: errorMessage })
    }
  }

  /**
   * 提取图片URL数组
   * @param {Array} fileList - 文件列表
   * @returns {Array} 图片URL数组
   */
  function extractImageUrls(fileList = []) {
    if (!uploadRef.value || !uploadRef.value.extractImageUrls) {
      console.warn('uploadRef或extractImageUrls方法未就绪')
      return []
    }

    return uploadRef.value.extractImageUrls(fileList)
  }

  /**
   * 处理表单提交
   * @param {Function} submitCallback - 提交回调函数
   * @param {Function} validateCallback - 验证回调函数（可选）
   * @returns {Promise} 提交Promise
   */
  async function handleSubmit(submitCallback, validateCallback = null) {
    try {
      // 检查是否可以提交
      if (!canSubmit()) {
        return Promise.reject(new Error('提交条件不满足'))
      }

      // 执行验证回调（如果提供）
      if (validateCallback) {
        const valid = await validateCallback()
        if (!valid) {
          return Promise.reject(new Error('表单验证失败'))
        }
      }

      // 设置提交状态
      submitting.value = true
      error.value = null

      // 调用提交回调
      const result = await submitCallback()

      // 提交成功
      ElMessage.success('操作成功')

      return result

    } catch (err) {
      console.error('提交失败:', err)

      // 设置错误状态
      const errorMessage = err.response?.status === 401
        ? '用户认证已失效，请重新登录'
        : err.response?.status >= 500
        ? '服务器错误，请稍后重试'
        : err.message || err.msg || '操作失败，请重试'

      error.value = errorMessage
      ElMessage.error(errorMessage)

      throw err

    } finally {
      // 重置提交状态
      submitting.value = false
    }
  }

  /**
   * 清理已完成的文件记录
   */
  function clearCompletedFiles() {
    uploadStatus.value.failedFiles = []
    // 清理上传完成或失败的文件
    uploadStatus.value.uploadingFiles = uploadStatus.value.uploadingFiles.filter(file =>
      file.progress === 0 || file.progress < 100
    )
    updateOverallProgress()
  }

  /**
   * 重置所有状态 - 增强版本
   */
  function reset() {
    // 清理自动隐藏定时器
    clearAutoHideTimer()
    forceShowProgress.value = false

    // 清理所有文件锁
    uploadStatus.value.fileLocks.clear()

    uploadStatus.value = {
      isAllUploaded: true,
      totalFiles: 0,
      uploadedFiles: 0,
      uploadingFiles: [],
      failedFiles: [],
      overallProgress: 0,
      maxFailedFiles: 50,
      fileLocks: new Set()
    }
    submitting.value = false
    error.value = null
  }

  /**
   * 获取上传配置（用于传递给子组件）
   */
  function getUploadProps() {
    return {
      ref: uploadRef,
      'onUpload-status-change': handleUploadStatusChange,
      onSuccess: handleUploadSuccess,
      onError: handleUploadError,
      onUploadStart: handleUploadStart,
      onUploadProgress: handleUploadProgress,
      onRetryAll: retryFailedUpload,
      onHideProgress: hideProgressManually,
      // 进度相关属性
      showOverallProgress: true,
      shouldShowProgress: shouldShowProgress.value,
      progressDisplayState: progressDisplayState.value,
      overallProgress: uploadStatus.value.overallProgress,
      uploadingCount: uploadStatus.value.uploadingFiles.length,
      currentUploadingFileName: currentUploadingFileName.value,
      // ImageUploadCard 支持的属性
      ...(config.maxCount !== undefined && { 'max-count': config.maxCount }),
      ...(config.maxSize !== undefined && { 'max-size': config.maxSize }),
      ...(config.compress !== undefined && { compress: config.compress }),
      ...(config.compressQuality !== undefined && { 'compress-quality': config.compressQuality }),
      ...(config.compressMaxSize !== undefined && { 'compress-max-size': config.compressMaxSize }),
      ...(config.compressMaxWidthOrHeight !== undefined && { 'compress-max-width-or-height': config.compressMaxWidthOrHeight }),
      ...(config.uploadUrl !== undefined && { 'upload-url': config.uploadUrl }),
      ...(config.uploadHeaders !== undefined && { 'upload-headers': config.uploadHeaders })
    }
  }

  /**
   * 处理上传开始事件
   */
  function handleUploadStart({ file }) {
    addToUploadQueue({
      uid: file.uid,
      name: file.name
    })
  }

  /**
   * 处理上传进度事件
   */
  function handleUploadProgress({ file, progress }) {
    updateFileProgress(file.uid, progress)
  }

  /**
   * 获取按钮配置（用于控制按钮状态）
   */
  function getButtonProps(buttonText = '提交') {
    return {
      disabled: isSubmitDisabled.value,
      loading: submitting.value
    }
  }

  /**
   * 清除自动隐藏定时器
   */
  function clearAutoHideTimer() {
    if (autoHideTimer.value) {
      clearTimeout(autoHideTimer.value)
      autoHideTimer.value = null
    }
  }

  /**
   * 延迟隐藏成功状态
   * @param {number} delay - 延迟时间，默认3000ms
   */
  function hideProgressAfterDelay(delay = 3000) {
    clearAutoHideTimer()

    const { uploadingFiles, failedFiles } = uploadStatus.value

    // 只有在全部完成且没有失败文件时才自动隐藏
    if (uploadingFiles.length === 0 && failedFiles.length === 0) {
      autoHideTimer.value = setTimeout(() => {
        forceShowProgress.value = false
        autoHideTimer.value = null
      }, delay)
    }
  }

  /**
   * 手动隐藏进度显示
   */
  function hideProgressManually() {
    clearAutoHideTimer()
    forceShowProgress.value = false
  }

  /**
   * 强制显示进度（用于手动触发）
   */
  function showProgressManually() {
    clearAutoHideTimer()
    forceShowProgress.value = true
  }

  /**
   * 重试失败的上传
   * @param {string} fileUid - 文件UID，可选。如果不提供，则重试所有失败的文件
   */
  function retryFailedUpload(fileUid = null) {
    clearAutoHideTimer()
    forceShowProgress.value = true // 重试时强制显示进度

    if (fileUid) {
      // 重试指定文件
      const failedIndex = uploadStatus.value.failedFiles.findIndex(f => f.uid === fileUid)
      if (failedIndex > -1) {
        const failedFile = uploadStatus.value.failedFiles[failedIndex]
        uploadStatus.value.failedFiles.splice(failedIndex, 1)

        // 重新添加到上传队列
        addToUploadQueue({
          uid: failedFile.uid,
          name: failedFile.name
        })

        console.log(`重试上传文件: ${failedFile.name}`)
      }
    } else {
      // 重试所有失败的文件
      const failedFiles = [...uploadStatus.value.failedFiles]
      uploadStatus.value.failedFiles = []

      failedFiles.forEach(failedFile => {
        addToUploadQueue({
          uid: failedFile.uid,
          name: failedFile.name
        })
        console.log(`重试上传文件: ${failedFile.name}`)
      })
    }
  }

  /**
   * 获取状态提示信息 - 增强版本
   */
  function getStatusTip() {
    if (hasError.value && uploadStatus.value.failedFiles.length === 0) {
      return {
        type: 'error',
        message: error.value,
        show: true
      }
    }

    const { totalFiles, uploadedFiles, failedFiles, uploadingFiles } = uploadStatus.value

    if (totalFiles > 0) {
      const activeUploading = uploadingFiles.filter(f => f.progress > 0 && f.progress < 100).length

      if (failedFiles.length > 0) {
        return {
          type: 'warning',
          message: `上传完成: ${uploadedFiles}/${totalFiles}，失败: ${failedFiles.length}个文件`,
          show: true,
          canRetry: true,
          failedCount: failedFiles.length
        }
      }

      if (uploadStatus.value.isAllUploaded) {
        return {
          type: 'success',
          message: `所有图片上传完成 (${uploadedFiles}/${totalFiles})`,
          show: true
        }
      }

      if (activeUploading > 0) {
        return {
          type: 'info',
          message: `正在上传 ${activeUploading} 个文件 (${uploadedFiles}/${totalFiles})`,
          show: true
        }
      }
    }

    return {
      type: 'info',
      message: '',
      show: false
    }
  }

  // ==================== 返回值 ====================
  return {
    // 状态
    uploadStatus,
    submitting,
    error,
    uploadRef,

    // 计算属性
    isSubmitDisabled,
    uploadProgress,
    hasError,
    uploadingCount,
    failedCount,
    currentUploadingFileName,
    shouldShowProgress,
    progressDisplayState,

    // 核心功能
    handleSubmit,
    canSubmit,
    extractImageUrls,
    reset,
    clearCompletedFiles,
    retryFailedUpload,

    // 进度显示控制
    hideProgressAfterDelay,
    hideProgressManually,
    showProgressManually,
    clearAutoHideTimer,

    // 回调
    handleUploadStatusChange,
    handleUploadSuccess,
    handleUploadError,

    // 进度管理方法
    addToUploadQueue,
    updateFileProgress,
    markFileUploaded,
    markFileFailed,
    cleanupFailedFiles,

    // 并发控制方法
    acquireFileLock,
    releaseFileLock,

    // 工具函数
    getUploadProps,
    getButtonProps,
    getStatusTip,

    // 配置（只读）
    config: Object.freeze(config)
  }
}

/**
 * 预设的上传配置
 */
export const uploadPresets = {
  // 资讯配置 - 基础配置
  news: {
    maxCount: 1,
    maxSize: 5,
    compress: true,
    compressQuality: 0.8,
    compressMaxSize: 2
  },

  // 问题上报配置 - 较多图片
  issue: {
    maxCount: 15,
    maxSize: 8,
    compress: true,
    compressQuality: 0.8,
    compressMaxSize: 3
  },

  // 整改配置 - 中等数量
  fix: {
    maxCount: 10,
    maxSize: 5,
    compress: true,
    compressQuality: 0.8,
    compressMaxSize: 2
  },

  // 验收配置 - 最多图片
  acceptance: {
    maxCount: 20,
    maxSize: 10,
    compress: true,
    compressQuality: 0.8,
    compressMaxSize: 3
  },

  // 质检配置 - 较多图片
  inspection: {
    maxCount: 20,
    maxSize: 10,
    compress: true,
    compressQuality: 0.8,
    compressMaxSize: 5
  },

  // 设计稿配置 - 灵活配置
  design: {
    maxCount: 20,
    maxSize: 10,
    compress: false // 设计稿通常需要保持高质量
  }
}
