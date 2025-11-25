<template>
  <div class="avatar-upload">
    <el-upload
      class="avatar-uploader"
      :show-file-list="false"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :accept="acceptType"
      :disabled="disabled"
      :action="uploadUrl"
      :headers="headers"
      :data="uploadData"
      name="avatarfile"
      :http-request="handleHttpRequest"
    >
      <img
        v-if="imageUrl"
        :src="imageUrl"
        class="avatar"
        :style="{ width: size + 'px', height: size + 'px' }"
      />
      <el-icon
        v-else
        class="avatar-uploader-icon"
        :style="{ width: size + 'px', height: size + 'px', lineHeight: size + 'px' }"
      >
        <Plus />
      </el-icon>
    </el-upload>
    <div v-if="showTip && !disabled" class="el-upload__tip">
      <span v-if="fileSize">图片大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b></span>
      <span v-if="fileType"> 格式为 <b style="color: #f56c6c">{{ fileType.join("/").toUpperCase() }}</b></span>
      <span v-if="compress" style="color: #52c41a; margin-left: 8px;">
      </span>
    </div>
  </div>
</template>

<script setup>
/**
 * 头像上传组件
 *
 * 重要修复记录：
 * 问题：压缩显示成功但服务器接收到的文件大小未改变
 * 原因：el-upload的before-upload钩子中修改file参数只是局部变量修改
 *
 * ✅ 解决方案：
 * 1. 使用:http-request自定义上传逻辑，完全接管上传过程
 * 2. 在before-upload中压缩图片并保存到compressedFile.value
 * 3. 在http-request中手动创建FormData并使用压缩后的文件
 * 4. 确保Blob转换为File对象后再添加到FormData
 *
 * 工作流程：
 * before-upload → 校验文件 → 压缩图片 → 保存到ref → http-request → 使用压缩后文件 → 上传
 */

import { ElMessage } from 'element-plus'
import { Plus, Picture } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import { getCurrentInstance } from 'vue'
import { useImageCompress } from '@/composables/useImageCompress'

const { compressImage, isImageFile } = useImageCompress()

const { proxy } = getCurrentInstance()
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + '/system/user/profile/avatar')

const props = defineProps({
  // 头像URL（相对路径或完整URL）
  modelValue: {
    type: String,
    default: ''
  },
  // 头像大小(px)
  size: {
    type: Number,
    default: 120
  },
  // 文件大小限制(MB)
  fileSize: {
    type: Number,
    default: 2
  },
  // 支持的文件类型
  fileType: {
    type: Array,
    default: () => ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp']
  },
  // 是否显示提示
  showTip: {
    type: Boolean,
    default: true
  },
  // 禁用组件
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否启用图片压缩
  compress: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'upload-success'])

// 计算属性 - 获取文件类型限制字符串
const acceptType = computed(() => {
  return props.fileType.map(type => `image/${type}`).join(',')
})

// 计算属性 - 获取完整的头像URL用于显示
const imageUrl = computed(() => {
  if (!props.modelValue) return ''

  // 如果���经是完整URL（以http开头），直接返回
  if (props.modelValue.startsWith('http')) {
    return props.modelValue
  }

  // 如果是相对路径，添加基础URL前缀用于显示
  return import.meta.env.VITE_APP_BASE_API + props.modelValue
})

// 请求头（包含认证 token）
const headers = computed(() => {
  return {
    Authorization: "Bearer " + getToken()
  }
})

// 上传时的数据
const uploadData = ref({
  // 这里可以添加其他上传参数
})

// 保存压缩后的文件
// 🔑 关键：使用ref保存压缩后的文件，而不是尝试修改函数参数
const compressedFile = ref(null)

/**
 * 上传前校验和压缩处理
 * 🔑 关键修复：这里只负责压缩和保存到ref，不修改file参数
 *
 * 错误示例：
 * async function handleBeforeUpload(file) {
 *   const result = await compressImage(file)
 *   if (result.success) {
 *     file = result.file  // ❌ 无效：只是修改局部变量
 *   }
 *   return true
 * }
 *
 * 正确示例：
 * async function handleBeforeUpload(file) {
 *   const result = await compressImage(file)
 *   if (result.success) {
 *     compressedFile.value = new File([result.file], file.name)  // ✅ 保存到ref
 *   }
 *   return true
 * }
 */

// 上传前校检
async function handleBeforeUpload(file) {
  // 校检文件类型
  const fileName = file.name.split('.')
  const fileExt = fileName[fileName.length - 1].toLowerCase()
  const isTypeOk = props.fileType.includes(fileExt)
  if (!isTypeOk) {
    ElMessage.error(`头像格式不正确，请上传 ${props.fileType.join('/').toUpperCase()} 格式的图片!`)
    return false
  }

  // 校检文件大小
  const isLt = file.size / 1024 / 1024 < props.fileSize
  if (!isLt) {
    ElMessage.error(`头像大小不能超过 ${props.fileSize} MB!`)
    return false
  }

  // 🆕 头像压缩处理 (高质量压缩)
  if (props.compress && isImageFile(file)) {
    try {
      proxy.$modal.loading("正在压缩头像，请稍候...")

      const compressResult = await compressImage(file, {
        maxSizeMB: 0.5,              // 头像压缩后最大 0.5MB
        maxWidthOrHeight: 500,        // 头像最大边长 500px
        initialQuality: 0.9,          // 90% 质量（高质量）
        enableSmartCompression: true
      })

      if (compressResult.success) {
        // ✅ 修复：浏览器image-compression返回Blob，需转换为File
        const compressedBlob = compressResult.file
        const newFile = new File([compressedBlob], file.name, {
          type: compressedBlob.type || file.type,
          lastModified: Date.now()
        })
        // 保存压缩后的文件到全局变量，http-request中会使用
        compressedFile.value = newFile
        // proxy.$modal.msgSuccess(`头像压缩成功：${compressResult.compressionRatio}%`)
      }
    } catch (error) {
      proxy.$modal.closeLoading()
      ElMessage.error('头像压缩失败: ' + error.message)
      return false
    }
  }

  return true
}

// ✅ 核心修复：自定义上传函数
async function handleHttpRequest(options) {
  try {
    proxy.$modal.loading("正在上传头像，请稍候...")

    // 创建FormData
    const formData = new FormData()
    // 使用压缩后的文件或原始文件
    formData.append('avatarfile', compressedFile.value || options.file)

    // 添加其他数据
    Object.keys(props.uploadData || {}).forEach(key => {
      formData.append(key, props.uploadData[key])
    })

    // 发起请求
    const response = await fetch(options.action, {
      method: 'POST',
      headers: {
        Authorization: headers.value.Authorization
      },
      body: formData
    })

    const data = await response.json()

    if (data.code === 200) {
      // 上传成功
      const avatarUrl = data.imgUrl || data.data?.imgUrl
      if (avatarUrl) {
        emit('update:modelValue', avatarUrl)
        emit('upload-success', avatarUrl)
        ElMessage.success('头像上传成功')
      }
      // 调用el-upload的成功回调
      options.onSuccess(data, options.file)
    } else {
      throw new Error(data.msg || '上传失败')
    }
  } catch (error) {
    ElMessage.error('上传失败: ' + error.message)
    options.onError(error)
  } finally {
    proxy.$modal.closeLoading()
    compressedFile.value = null // 清空压缩文件缓存
  }
}

// 上传成功回调
function handleUploadSuccess(response) {
  proxy.$modal.closeLoading()

  if (response.code === 200) {
    // 上传成功，使用返回的头像URL（相对路径）
    const avatarUrl = response.imgUrl || response.data?.imgUrl
    if (avatarUrl) {
      // 注意：这里直接返回相对路径，不拼接完整URL
      // imageUrl computed 会自动将相对路径转换为完整URL用于显示
      emit('update:modelValue', avatarUrl)
      emit('upload-success', avatarUrl)
      // ElMessage.success('头像上传成功')
    } else {
      ElMessage.error('头像上传成功但未返回URL')
    }
  } else {
    ElMessage.error(response.msg || '头像上传失败')
  }
}

// 上传失败回调
function handleUploadError() {
  proxy.$modal.closeLoading()
  ElMessage.error('头像上传失败')
}
</script>

<style scoped lang="scss">
.avatar-upload {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
}

.avatar-uploader {
  .el-upload {
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
    border: 1px dashed #d9d9d9;
    border-radius: 6px;

    &:hover {
      border-color: #409eff;

      .avatar-uploader-icon {
        color: #409eff;
      }
    }
  }

  .avatar {
    display: block;
    object-fit: cover;
    border-radius: 6px;
  }

  .avatar-uploader-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #8c939d;
    font-size: 28px;
    text-align: center;
  }
}

.el-upload__tip {
  text-align: center;
  color: #606266;
  font-size: 12px;
  margin-top: 8px;

  b {
    font-weight: normal;
  }
}
</style>
