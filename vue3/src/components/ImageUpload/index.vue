<template>
  <div class="component-upload-image">
    <el-upload
      multiple
      :disabled="disabled"
      :action="uploadImgUrl"
      list-type="picture-card"
      :on-success="handleUploadSuccess"
      :before-upload="handleBeforeUpload"
      :data="data"
      :limit="limit"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      ref="imageUpload"
      :before-remove="handleDelete"
      :show-file-list="true"
      :headers="headers"
      :file-list="fileList"
      :on-preview="handlePictureCardPreview"
      :class="{ hide: fileList.length >= limit }"
      :http-request="handleHttpRequest"
    >
      <el-icon class="avatar-uploader-icon"><plus /></el-icon>
    </el-upload>
    <!-- 上传提示 -->
    <div class="el-upload__tip" v-if="showTip && !disabled">
      请上传
      <template v-if="fileSize">
        大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b>
      </template>
      <template v-if="fileType">
        格式为 <b style="color: #f56c6c">{{ fileType.join("/") }}</b>
      </template>
      <template v-if="compress">
        的文件
        <span style="color: #52c41a; margin-left: 8px;">
          <el-icon><Picture /></el-icon>
          图片将自动压缩 (质量: {{ Math.round(compressQuality * 100) }}%, 最大: {{ compressMaxSize }}MB)
        </span>
      </template>
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="预览"
      width="800px"
      append-to-body
    >
      <img
        :src="dialogImageUrl"
        style="display: block; max-width: 100%; margin: 0 auto"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { getToken } from "@/utils/auth"
import { isExternal } from "@/utils/validate"
import Sortable from 'sortablejs'
import { Picture } from '@element-plus/icons-vue'
import { useImageCompress } from '@/composables/useImageCompress'

const { compressImage, isImageFile } = useImageCompress()

const props = defineProps({
  modelValue: [String, Object, Array],
  // 上传接口地址
  action: {
    type: String,
    default: "/common/upload"
  },
  // 上传携带的参数
  data: {
    type: Object
  },
  // 图片数量限制
  limit: {
    type: Number,
    default: 5
  },
  // 大小限制(MB)
  fileSize: {
    type: Number,
    default: 5
  },
  // 文件类型, 例如['png', 'jpg', 'jpeg']
  fileType: {
    type: Array,
    default: () => ["png", "jpg", "jpeg"]
  },
  // 是否显示提示
  isShowTip: {
    type: Boolean,
    default: true
  },
  // 禁用组件（仅查看图片）
  disabled: {
    type: Boolean,
    default: false
  },
  // 拖动排序
  drag: {
    type: Boolean,
    default: true
  },
  // 是否启用图片压缩
  compress: {
    type: Boolean,
    default: true
  },
  // 压缩质量 (0.0-1.0, 越小文件越小)
  compressQuality: {
    type: Number,
    default: 0.7
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

const { proxy } = getCurrentInstance()
const emit = defineEmits()
const number = ref(0)
const uploadList = ref([])
const dialogImageUrl = ref("")
const dialogVisible = ref(false)
const baseUrl = import.meta.env.VITE_APP_BASE_API
const uploadImgUrl = ref(import.meta.env.VITE_APP_BASE_API + props.action) // 上传的图片服务器地址
const headers = ref({ Authorization: "Bearer " + getToken() })
const fileList = ref([])
const showTip = computed(
  () => props.isShowTip && (props.fileType || props.fileSize)
)
// 保存压缩后的文件
const compressedFiles = ref(new Map())

watch(() => props.modelValue, val => {
  if (val) {
    // 首先将值转为数组
    let list = []
    if (Array.isArray(val)) {
      list = val
    } else if (typeof val === 'string') {
      list = val.split(",")
    } else if (typeof val === 'object') {
      // 单个文件对象，转换为数组
      list = [val]
    }

    // 然后将数组转为对象数组
    fileList.value = list.map(item => {
      if (typeof item === "string") {
        if (item.indexOf(baseUrl) === -1 && !isExternal(item)) {
          item = { name: baseUrl + item, url: baseUrl + item }
        } else {
          item = { name: item, url: item }
        }
      }
      return item
    })
  } else {
    fileList.value = []
    return []
  }
},{ deep: true, immediate: true })

// 上传前loading加载
async function handleBeforeUpload(file) {
  let isImg = false
  if (props.fileType.length) {
    let fileExtension = ""
    if (file.name.lastIndexOf(".") > -1) {
      fileExtension = file.name.slice(file.name.lastIndexOf(".") + 1)
    }
    isImg = props.fileType.some(type => {
      if (file.type.indexOf(type) > -1) return true
      if (fileExtension && fileExtension.indexOf(type) > -1) return true
      return false
    })
  } else {
    isImg = file.type.indexOf("image") > -1
  }
  if (!isImg) {
    proxy.$modal.msgError(`文件格式不正确，请上传${props.fileType.join("/")}图片格式文件!`)
    return false
  }
  if (file.name.includes(',')) {
    proxy.$modal.msgError('文件名不正确，不能包含英文逗号!')
    return false
  }
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize
    if (!isLt) {
      proxy.$modal.msgError(`上传头像图片大小不能超过 ${props.fileSize} MB!`)
      return false
    }
  }

  // 🆕 图片压缩处理 (平衡压缩)
  if (props.compress && isImageFile(file)) {
    try {
      proxy.$modal.loading("正在压缩图片，请稍候...")

      const compressResult = await compressImage(file, {
        maxSizeMB: props.compressMaxSize,
        maxWidthOrHeight: props.compressMaxWidthOrHeight,
        initialQuality: props.compressQuality,
        enableSmartCompression: true
      })

      if (compressResult.success) {
        // ✅ 修复：浏览器image-compression返回Blob，需转换为File
        const compressedBlob = compressResult.file
        const newFile = new File([compressedBlob], file.name, {
          type: compressedBlob.type || file.type,
          lastModified: Date.now()
        })
        // 保存压缩后的文件到Map中，http-request中会使用
        compressedFiles.value.set(file.uid, newFile)
        proxy.$modal.msgSuccess(`图片压缩成功：${compressResult.compressionRatio}%`)
      }
    } catch (error) {
      proxy.$modal.closeLoading()
      proxy.$modal.msgError('图片压缩失败: ' + error.message)
      return false
    }
  }

  proxy.$modal.loading("正在上传图片，请稍候...")
  number.value++
}

// 文件个数超出
function handleExceed() {
  proxy.$modal.msgError(`上传文件数量不能超过 ${props.limit} 个!`)
}

// 上传成功回调
function handleUploadSuccess(res, file) {
  // 添加空值检查，防止 res 为 undefined
  if (!res) {
    number.value--
    proxy.$modal.closeLoading()
    proxy.$modal.msgError("上传失败：服务器响应异常")
    proxy.$refs.imageUpload?.handleRemove(file)
    uploadedSuccessfully()
    return
  }
  
  if (res.code === 200) {
    uploadList.value.push({ name: res.fileName, url: res.fileName })
    uploadedSuccessfully()
  } else {
    number.value--
    proxy.$modal.closeLoading()
    proxy.$modal.msgError(res.msg || "上传失败")
    proxy.$refs.imageUpload?.handleRemove(file)
    uploadedSuccessfully()
  }
}

// 删除图片
function handleDelete(file) {
  const findex = fileList.value.map(f => f.name).indexOf(file.name)
  if (findex > -1 && uploadList.value.length === number.value) {
    fileList.value.splice(findex, 1)
    emit("update:modelValue", listToString(fileList.value))
    return false
  }
}

// 上传结束处理
function uploadedSuccessfully() {
  if (number.value > 0 && uploadList.value.length === number.value) {
    fileList.value = fileList.value.filter(f => f.url !== undefined).concat(uploadList.value)
    uploadList.value = []
    number.value = 0
    emit("update:modelValue", listToString(fileList.value))
    proxy.$modal.closeLoading()
  }
}

// ✅ 核心修复：自定义上传函数
async function handleHttpRequest(options) {
  try {
    proxy.$modal.loading("正在上传图片，请稍候...")

    // 创建FormData
    const formData = new FormData()
    // 使用压缩后的文件或原始文件
    formData.append('file', compressedFiles.value.get(options.file.uid) || options.file)

    // 添加其他数据
    Object.keys(props.data || {}).forEach(key => {
      formData.append(key, props.data[key])
    })

    // 发起请求
    const response = await fetch(options.action, {
      method: 'POST',
      headers: {
        Authorization: headers.value.Authorization
      },
      body: formData
    })

    // 检查响应状态
    if (!response.ok) {
      throw new Error(`上传失败: HTTP ${response.status} ${response.statusText}`)
    }

    // 解析响应
    let data
    try {
      const text = await response.text()
      if (!text) {
        throw new Error('服务器响应为空')
      }
      data = JSON.parse(text)
    } catch (parseError) {
      throw new Error('服务器响应格式错误: ' + parseError.message)
    }

    // 验证响应数据结构
    if (!data || typeof data !== 'object') {
      throw new Error('服务器响应数据格式异常')
    }

    if (data.code === 200) {
      uploadList.value.push({ name: data.fileName, url: data.fileName })
      // 清空该文件的压缩缓存
      compressedFiles.value.delete(options.file.uid)
      // 调用el-upload的成功回调，确保传递正确的数据格式
      if (options.onSuccess) {
        options.onSuccess(data, options.file)
      }
      uploadedSuccessfully()
    } else {
      throw new Error(data.msg || '上传失败')
    }
  } catch (error) {
    proxy.$modal.closeLoading()
    proxy.$modal.msgError('上传失败: ' + error.message)
    if (options.onError) {
      options.onError(error)
    }
    number.value--
    compressedFiles.value.delete(options.file.uid)
    // 确保调用上传成功回调以清理状态，但传递错误信息
    if (options.onSuccess) {
      options.onSuccess(null, options.file)
    }
  }
}

// 上传失败
function handleUploadError() {
  proxy.$modal.msgError("上传图片失败")
  proxy.$modal.closeLoading()
}

// 预览
function handlePictureCardPreview(file) {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}

// 对象转成指定字符串分隔
function listToString(list, separator) {
  let strs = ""
  separator = separator || ","
  for (let i in list) {
    if (undefined !== list[i].url && list[i].url.indexOf("blob:") !== 0) {
      strs += list[i].url.replace(baseUrl, "") + separator
    }
  }
  return strs != "" ? strs.substr(0, strs.length - 1) : ""
}

// 初始化拖拽排序
onMounted(() => {
  if (props.drag && !props.disabled) {
    nextTick(() => {
      const element = proxy.$refs.imageUpload?.$el?.querySelector('.el-upload-list')
      Sortable.create(element, {
        onEnd: (evt) => {
          const movedItem = fileList.value.splice(evt.oldIndex, 1)[0]
          fileList.value.splice(evt.newIndex, 0, movedItem)
          emit('update:modelValue', listToString(fileList.value))
        }
      })
    })
  }
})
</script>

<style scoped lang="scss">
// .el-upload--picture-card 控制加号部分
:deep(.hide .el-upload--picture-card) {
    display: none;
}

:deep(.el-upload.el-upload--picture-card.is-disabled) {
  display: none !important;
} 
</style>