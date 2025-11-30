<template>
  <div class="image-upload-card">
    <!-- 图片卡片式上传 -->
    <el-upload
      v-model:file-list="fileList"
      :action="uploadUrl"
      list-type="picture-card"
      :auto-upload="autoUpload"
      :multiple="multiple"
      :limit="maxCount"
      :headers="uploadHeaders"
      :on-exceed="handleExceed"
      :on-preview="handlePictureCardPreview"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :on-remove="handleRemove"
      :before-upload="handleBeforeUpload"
      :before-remove="beforeRemove"
      :http-request="handleHttpRequest"
      :disabled="disabled"
      class="upload-component"
    >
      <el-icon><Plus /></el-icon>
      <template #tip v-if="showTip">
        <div class="el-upload__tip">
          {{ computedTipText }}
          <!-- <span v-if="compress" class="compress-tip">
            <el-icon><Picture /></el-icon>
            图片将自动压缩 (质量: {{ Math.round(compressQuality * 100) }}%, 最大: {{ compressMaxSize }}MB)
          </span> -->
        </div>
      </template>
    </el-upload>

    <!-- 图片预览组件 - 直接使用 v-viewer 全屏预览 -->
    <div
      v-if="showPreview"
      class="preview-wrapper"
      @click.self="closePreview"
    >
      <ImagePreview
        :images="previewImages"
        :options="previewOptions"
        width="100%"
        height="100%"
        :z-index="3000"
      />
    </div>
  </div>
</template>

<script setup>
import { Plus, Picture } from '@element-plus/icons-vue'
import { onMounted, onUnmounted } from 'vue'
import useUserStore from '@/store/modules/user'
import { useImageCompress } from '@/composables/useImageCompress'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const { compressImage, isImageFile } = useImageCompress()

// Props定义
const props = defineProps({
  // v-model绑定
  modelValue: {
    type: Array,
    default: () => []
  },
  // 上传地址
  uploadUrl: {
    type: String,
    default: () => import.meta.env.VITE_APP_BASE_API + '/common/upload'
  },
  // 最大上传数量
  maxCount: {
    type: Number,
    default: 20
  },
  // 最大文件大小(MB)
  maxSize: {
    type: Number,
    default: 10
  },
  // 是否自动上传
  autoUpload: {
    type: Boolean,
    default: true
  },
  // 是否支持多选
  multiple: {
    type: Boolean,
    default: true
  },
  // 上传请求头
  uploadHeaders: {
    type: Object,
    default: () => ({})
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否显示提示文字
  showTip: {
    type: Boolean,
    default: true
  },
  // 自定义提示文字
  tipText: {
    type: String,
    default: ''
  },
  // 是否启用图片压缩
  compress: {
    type: Boolean,
    default: true
  },
  // 压缩质量 (0.0-1.0, 越小文件越小)
  compressQuality: {
    type: Number,
    default: 0.8
  },
  // 压缩后最大文件大小 (MB)
  compressMaxSize: {
    type: Number,
    default: 1
  },
  // 压缩最大宽度或高度
  compressMaxWidthOrHeight: {
    type: Number,
    default: 1920
  }
})

// Emits定义
const emit = defineEmits([
  'update:modelValue',
  'success',
  'error',
  'remove',
  'change',
  'exceed',
  'preview',
  'upload-status-change'
])

// 响应式数据
const fileList = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 预览控制状态 - 替换原来的对话框模式
const showPreview = ref(false)
const currentPreviewIndex = ref(0)

// 计算预览图片列表
const previewImages = computed(() => {
  if (!fileList.value || fileList.value.length === 0) return []

  return fileList.value
    .filter(file => {
      // 过滤掉无效的图片
      if (file.status === 'fail' || file.status === 'error' || file.status === 'uploading') {
        return false
      }
      return file.url || file.response
    })
    .map(file => {
      // 提取图片URL
      if (file.url) {
        return file.url
      }
      if (file.response?.code === 200) {
        const imageUrl = file.response.fileName || file.response.url || file.response.imgUrl || file.response.data
        if (imageUrl) {
          if (imageUrl.startsWith('http')) {
            return imageUrl
          } else {
            const baseUrl = import.meta.env.VITE_APP_BASE_API
            const baseUrlWithSlash = baseUrl.endsWith('/') ? baseUrl : baseUrl + '/'
            const imagePath = imageUrl.startsWith('/') ? imageUrl.substring(1) : imageUrl
            return baseUrlWithSlash + imagePath
          }
        }
      }
      return ''
    })
    .filter(url => url)
})

// 预览选项配置 - 优化版本，直接使用 v-viewer 功能
const previewOptions = computed(() => ({
  // 工具栏配置
  toolbar: true,
  zoomOn: true,
  zoomOff: true,
  rotateOn: true,
  flipHOn: true,
  fullScreen: true,
  prev: previewImages.value.length > 1,
  next: previewImages.value.length > 1,
  reset: true,
  download: true,

  // 导航和交互
  navbar: true,
  title: false,
  tooltip: true,
  movable: true,
  zoomable: true,
  rotatable: true,
  flip: true,
  transition: true,
  keyboard: true,
  loop: true,

  // 缩放限制
  minZoomRatio: 0.1,
  maxZoomRatio: 5,

  // 设置初始查看的图片索引
  initialViewIndex: currentPreviewIndex.value
}))

// 删除操作状态管理
const deletingFiles = ref(new Set()) // 正在删除的文件UID集合

// 保存压缩后的文件缓存
const compressedFiles = ref(new Map()) // 压缩后的文件缓存 (fileUid -> File)

// 计算所有图片是否都已上传完成
const isAllUploaded = computed(() => {
  if (!fileList.value || fileList.value.length === 0) return true

  return fileList.value.every(file => {
    // 1. 检查文件状态：上传中和失败状态都不算完成
    if (file.status === 'uploading' || file.status === 'fail') return false

    // 2. 检查是否有有效的上传结果
    if (file.response?.code === 200) return true

    // 3. 检查是否是成功状态且有URL（编辑模式下的已存在文件）
    if (file.status === 'success' && file.url) return true

    // 4. 检查是编辑文件（没有raw属性但有URL）
    if (!file.raw && file.url && file.status !== 'error') return true

    return false
  })
})

// 监听上传状态变化，发送通知事件
watch(isAllUploaded, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    emit('upload-status-change', {
      isAllUploaded: newVal,
      totalFiles: fileList.value.length,
      uploadedFiles: fileList.value.filter(file =>
        file.status === 'success' || file.response?.code === 200
      ).length
    })
  }
}, { immediate: true })

// 计算提示文字
const computedTipText = computed(() => {
  if (props.tipText) return props.tipText
  return `支持拖拽上传，最多${props.maxCount}张图片，单张不超过${props.maxSize}MB`
})

// 处理图片超出限制
function handleExceed() {
  proxy.$modal.msgWarning(`最多只能上传${props.maxCount}张图片`)
  emit('exceed', { maxCount: props.maxCount })
}

// 预览图片 - 直接触发 v-viewer 全屏预览
function handlePictureCardPreview(file) {
  // 计算当前图片在预览列表中的索引
  const fileIndex = fileList.value.findIndex(f => f.uid === file.uid)
  currentPreviewIndex.value = Math.max(0, fileIndex)

  // 显示预览组件
  showPreview.value = true

  // 获取当前图片URL用于事件发送
  let previewUrl = ''
  if (file.url) {
    previewUrl = file.url
  } else if (file.response) {
    const imageUrl = file.response.fileName || file.response.url || file.response.imgUrl || file.response.data
    if (imageUrl) {
      if (imageUrl.startsWith('http')) {
        previewUrl = imageUrl
      } else {
        const baseUrl = import.meta.env.VITE_APP_BASE_API
        const baseUrlWithSlash = baseUrl.endsWith('/') ? baseUrl : baseUrl + '/'
        const imagePath = imageUrl.startsWith('/') ? imageUrl.substring(1) : imageUrl
        previewUrl = baseUrlWithSlash + imagePath
      }
    }
  } else if (file.raw) {
    previewUrl = URL.createObjectURL(file.raw)
  }

  // 发送预览事件，包含更多上下文信息
  emit('preview', {
    file,
    url: previewUrl,
    index: currentPreviewIndex.value,
    totalImages: previewImages.value.length,
    allImages: previewImages.value
  })
}

// 上传前验证和压缩处理
async function handleBeforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLtMaxSize = file.size / 1024 / 1024 < props.maxSize

  if (!isImage) {
    proxy.$modal.msgError('只能上传图片文件!')
    return false
  }
  if (!isLtMaxSize) {
    proxy.$modal.msgError(`上传图片大小不能超过 ${props.maxSize}MB!`)
    return false
  }

  // 图片压缩处理
  if (props.compress && isImageFile(file)) {
    try {
      console.log('开始压缩图片:', file.name, `原始大小: ${(file.size / 1024 / 1024).toFixed(2)}MB`)

      const compressResult = await compressImage(file, {
        maxSizeMB: props.compressMaxSize,
        maxWidthOrHeight: props.compressMaxWidthOrHeight,
        initialQuality: props.compressQuality,
        enableSmartCompression: true
      })

      if (compressResult.success) {
        // 将压缩后的 Blob 转换为 File
        const compressedBlob = compressResult.file
        const newFile = new File([compressedBlob], file.name, {
          type: compressedBlob.type || file.type,
          lastModified: Date.now()
        })

        // 保存压缩后的文件到 Map 中，http-request 中会使用
        compressedFiles.value.set(file.uid, newFile)

        console.log('图片压缩成功:', {
          原始大小: `${compressResult.originalSizeMB}MB`,
          压缩后大小: `${compressResult.compressedSizeMB}MB`,
          压缩比例: `${compressResult.compressionRatio}%`,
          质量设置: `${Math.round(compressResult.quality * 100)}%`
        })
      }
    } catch (error) {
      console.error('图片压缩失败:', error)
      // 压缩失败不影响上传，继续使用原始文件
      proxy.$modal.msgWarning('图片压缩失败，将使用原始文件上传')
    }
  }

  return true
}

// 上传成功回调
function handleUploadSuccess(response, file) {
  if (response.code === 200) {
    // 优化图片URL设置，支持多种返回格式
    const imageUrl = response.fileName || response.url || response.imgUrl || response.data
    if (imageUrl) {
      // 如果返回的是完整URL（包含http），直接使用
      if (imageUrl.startsWith('http')) {
        file.url = imageUrl
      } else {
        // 如果返回的是相对路径，拼接Base API
        const baseUrl = import.meta.env.VITE_APP_BASE_API
        const baseUrlWithSlash = baseUrl.endsWith('/') ? baseUrl : baseUrl + '/'
        const imagePath = imageUrl.startsWith('/') ? imageUrl.substring(1) : imageUrl
        file.url = baseUrlWithSlash + imagePath
      }
    }
    file.response = response

    proxy.$modal.msgSuccess('图片上传成功')
    emit('success', { response, file, imageUrl })
  } else {
    proxy.$modal.msgError(response.msg || '图片上传失败')

    // 从文件列表中移除失败的文件
    const index = fileList.value.findIndex(img => img.uid === file.uid)
    if (index > -1) {
      fileList.value.splice(index, 1)
    }

    emit('error', { response, file, message: response.msg || '图片上传失败' })
  }

  emit('change', { fileList: fileList.value, file, type: 'upload' })
}

// 上传失败回调
function handleUploadError(error, file) {
  console.error('图片上传失败:', error)

  // 尝试从后端响应中提取具体错误信息
  let errorMessage = '图片上传失败'

  // 优先从 response 中提取错误消息
  if (error.response && error.response.data) {
    const responseData = error.response.data
    if (responseData.msg) {
      errorMessage = responseData.msg
    } else if (responseData.message) {
      errorMessage = responseData.message
    } else if (typeof responseData === 'string') {
      errorMessage = responseData
    }
  }
  // 其次从 error 对象本身提取
  else if (error.message) {
    errorMessage = error.message
  }
  // 最后从文件响应用中提取（如果存在）
  else if (file.response) {
    if (file.response.msg) {
      errorMessage = file.response.msg
    } else if (file.response.message) {
      errorMessage = file.response.message
    }
  }

  proxy.$modal.msgError('图片上传失败: '+errorMessage)

  // 从文件列表中移除失败的文件
  const index = fileList.value.findIndex(img => img.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }

  // 清理压缩缓存
  compressedFiles.value.delete(file.uid)

  emit('error', { error, file, message: errorMessage })
  emit('change', { fileList: fileList.value, file, type: 'error' })
}

// 自定义上传函数 - 支持压缩文件上传
async function handleHttpRequest(options) {
  try {
    // 创建FormData
    const formData = new FormData()

    // 使用压缩后的文件或原始文件
    const fileToUpload = compressedFiles.value.get(options.file.uid) || options.file
    formData.append('file', fileToUpload)

    // 发起请求
    const response = await fetch(options.action, {
      method: 'POST',
      headers: options.headers || props.uploadHeaders,
      body: formData
    })

    const data = await response.json()

    if (data.code === 200) {
      // 清空该文件的压缩缓存
      compressedFiles.value.delete(options.file.uid)

      // 调用el-upload的成功回调
      options.onSuccess(data, options.file)
    } else {
      throw new Error(data.msg || '上传失败')
    }
  } catch (error) {
    console.error('自定义上传失败:', error)
    // 清理压缩缓存
    compressedFiles.value.delete(options.file.uid)
    options.onError(error)
  }
}

// 关闭预览
function closePreview() {
  showPreview.value = false
  currentPreviewIndex.value = 0
}

// 预览图片错误处理 - ImagePreview 组件会自动处理，这里保留日志
function handlePreviewError(event) {
  console.warn('预览图片加载失败:', event.target?.src)
  // ImagePreview 组件会自动处理错误图片，这里不需要额外处理
}

// 监听 ESC 键关闭预览
onMounted(() => {
  const handleKeydown = (e) => {
    if (e.key === 'Escape' && showPreview.value) {
      closePreview()
    }
  }
  document.addEventListener('keydown', handleKeydown)

  onUnmounted(() => {
    document.removeEventListener('keydown', handleKeydown)
  })
})

// 删除前确认钩子 - 防重复点击版本
function beforeRemove(file) {
  // 防止重复删除同一个文件
  if (deletingFiles.value.has(file.uid)) {
    return Promise.reject(new Error('正在删除中，请稍候'))
  }

  return proxy.$modal.confirm(
    '确定要删除这张图片吗？',
    '删除确认',
    {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    }
  ).then(() => {
    // 标记文件正在删除状态
    deletingFiles.value.add(file.uid)
    return Promise.resolve()
  }).catch((error) => {
    // 如果是用户取消，不添加到删除队列
    if (error === 'cancel') {
      return Promise.reject()
    }
    return Promise.reject(error)
  })
}

// 移除图片回调（删除完成后触发）- 优化版本
function handleRemove(file, fileList) {
  try {
    // 移除删除状态标记
    deletingFiles.value.delete(file.uid)

    // 验证删除操作是否成功
    const isFileRemoved = !fileList.some(f => f.uid === file.uid)

    if (isFileRemoved) {
      // 删除成功，发送事件通知
      emit('remove', { file, fileList })
      emit('change', { fileList, file, type: 'remove' })
      proxy.$modal.msgSuccess('图片已删除')
    } else {
      // 删除失败，回滚状态
      console.warn('删除操作可能未成功，文件仍存在于列表中')
      proxy.$modal.msgWarning('删除操作异常，请检查图片列表')
    }
  } catch (error) {
    console.error('删除回调处理失败:', error)
    // 确保移除删除状态标记
    deletingFiles.value.delete(file.uid)
    proxy.$modal.msgError('删除操作异常')
  }
}

// 工具函数：解析fileIds为fileList格式
function parseFileIdsToList(fileIds) {
  if (!fileIds) return []

  try {
    let fileIdsArray = []
    if (typeof fileIds === 'string') {
      try {
        fileIdsArray = JSON.parse(fileIds)
      } catch {
        fileIdsArray = fileIds.split(',').filter(Boolean)
      }
    } else if (Array.isArray(fileIds)) {
      fileIdsArray = fileIds
    }

    const baseUrl = import.meta.env.VITE_APP_BASE_API
    return fileIdsArray.map((fileId, index) => {
      let fullUrl = fileId
      if (!fileId.startsWith('http')) {
        if (fileId.startsWith(baseUrl)) {
          fullUrl = fileId
        } else {
          let path = fileId
          if (!path.startsWith('/')) {
            path = '/' + path
          }
          const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
          fullUrl = cleanBaseUrl + path
        }
      }

      return {
        uid: `existing-${index}`,
        name: `image-${index}.jpg`,
        url: fullUrl,
        status: 'success'
      }
    })
  } catch (error) {
    console.error('解析fileIds失败:', error)
    return []
  }
}

// 工具函数：从fileList提取图片URL数组 - 防抖优化版本
function extractImageUrls(fileList) {
  if (!fileList || !Array.isArray(fileList)) return []

  return fileList
    .filter(file => {
      // 过滤掉正在删除的文件
      if (deletingFiles.value.has(file.uid)) {
        return false
      }

      // 过滤掉上传失败的文件
      if (file.status === 'fail' || file.status === 'error') {
        return false
      }

      // 过滤掉上传中的文件
      if (file.status === 'uploading') {
        return false
      }

      // 确保有有效的URL或response
      return file.url || file.response
    })
    .map(file => {
      try {
        // 优先使用后端返回的文件名
        if (file.response?.code === 200 && file.response.fileName) {
          // 如果返回的是相对路径，确保以/开头
          let path = file.response.fileName
          if (!path.startsWith('/')) {
            path = '/' + path
          }
          return path
        }

        // 处理完整URL
        if (file.url) {
          if (file.url.startsWith('http')) {
            // 移除完整URL前缀，仅保留相对路径
            const baseUrl = import.meta.env.VITE_APP_BASE_API
            if (file.url.startsWith(baseUrl)) {
              let path = file.url.substring(baseUrl.length)
              // 确保以/开头
              if (!path.startsWith('/')) {
                path = '/' + path
              }
              return path
            }
            // 如果不以baseUrl开头，可能是完整CDN URL，直接返回原路径
            return file.url
          }

          // 相对路径，确保以/开头
          let path = file.url
          if (!path.startsWith('/')) {
            path = '/' + path
          }
          return path
        }

        return null
      } catch (error) {
        console.error('处理图片URL时出错:', error, file)
        return null
      }
    })
    .filter(url => url !== null)
}

// 组件卸载时清理资源
onUnmounted(() => {
  // 清理删除状态
  deletingFiles.value.clear()

  // 清理压缩文件缓存
  compressedFiles.value.clear()
})

// 暴露给父组件的工具函数
defineExpose({
  parseFileIdsToList,
  extractImageUrls,
  deletingFiles: () => deletingFiles.value,
  clearDeletingState: () => deletingFiles.value.clear()
})
</script>

<style scoped lang="scss">
.image-upload-card {
  .upload-component {
    // 上传按钮尺寸
    :deep(.el-upload--picture-card) {
      --el-upload-picture-card-size: 120px;
      width: 120px;
      height: 120px;
      line-height: 120px;
    }

    // 预览图片列表尺寸 - 与上传按钮保持一致
    :deep(.el-upload-list--picture-card) {
      .el-upload-list__item {
        width: 120px;
        height: 120px;
        position: relative;
        overflow: hidden;

        // 确保图片在卡片内完整显示且不变形
        .el-upload-list__item-thumbnail {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        // 删除按钮位置调整
        .el-upload-list__item-actions {
          width: 120px;
          height: 120px;
          z-index: 10;

          span {
            font-size: 16px;
          }

          // 在图片加载过程中也显示删除按钮
          &::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0);
            transition: background-color 0.3s ease;
          }

          &:hover::before {
            background: rgba(0, 0, 0, 0.3);
          }
        }

        // 删除中的状态
        &.deleting {
          opacity: 0.5;
          pointer-events: none;

          .el-upload-list__item-actions {
            display: none;
          }

          .el-upload-list__item-thumbnail::after {
            content: '删除中...';
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            color: #1677ff;
            font-size: 12px;
            font-weight: 500;
            z-index: 2;
            background: rgba(255, 255, 255, 0.9);
            padding: 4px 8px;
            border-radius: 4px;
          }
        }
      }
    }
  }

  .el-upload__tip {
    color: #999;
    font-size: 12px;
    margin-top: 8px;
    text-align: center;

    .compress-tip {
      color: #52c41a;
      margin-left: 8px;
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
  }
}

// 图片预览包装器样式
.preview-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  z-index: 2999;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}
</style>