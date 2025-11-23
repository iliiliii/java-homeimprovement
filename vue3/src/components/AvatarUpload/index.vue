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
import { getToken } from '@/utils/auth'
import { getCurrentInstance } from 'vue'

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

  // 显示加载中
  proxy.$modal.loading("正在上传头像，请稍候...")

  return true
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
      ElMessage.success('头像上传成功')
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
