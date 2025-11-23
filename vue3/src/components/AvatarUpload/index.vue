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
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const props = defineProps({
  // 头像URL或Base64
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
  }
})

const emit = defineEmits(['update:modelValue', 'upload-success'])

// 计算属性
const acceptType = computed(() => {
  return props.fileType.map(type => `image/${type}`).join(',')
})

const imageUrl = computed(() => props.modelValue)

// 上传前校检
function handleBeforeUpload(file) {
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

  // 返回 Promise 用于转换为 Base64
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      // 将 Base64 结果传给 on-success 处理器
      resolve({
        ...file,
        base64: e.target.result
      })
    }
    reader.onerror = () => {
      reject(new Error('文件读取失败'))
    }
    reader.readAsDataURL(file)
  })
}

// 上传成功回调
function handleUploadSuccess(response, file) {
  let base64Data
  if (file.base64) {
    // 使用转换后的 Base64 数据
    base64Data = file.base64
  } else {
    // 如果没有 base64，则重新读取文件
    const reader = new FileReader()
    reader.onload = (e) => {
      const result = e.target.result
      emit('update:modelValue', result)
      emit('upload-success', result)
      ElMessage.success('头像上传成功')
    }
    reader.readAsDataURL(file.raw || file)
    return
  }

  emit('update:modelValue', base64Data)
  emit('upload-success', base64Data)
  ElMessage.success('头像上传成功')
}

// 上传失败回调
function handleUploadError() {
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
