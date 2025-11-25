<template>
  <div class="upload-file">
    <el-upload
      multiple
      :action="uploadFileUrl"
      :before-upload="handleBeforeUpload"
      :file-list="fileList"
      :data="data"
      :limit="limit"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      :on-success="handleUploadSuccess"
      :show-file-list="false"
      :headers="headers"
      class="upload-file-uploader"
      ref="fileUpload"
      v-if="!disabled"
      :http-request="handleHttpRequest"
    >
      <!-- 上传按钮 -->
      <el-button type="primary">选取文件</el-button>
    </el-upload>
    <!-- 上传提示 -->
    <div class="el-upload__tip" v-if="showTip && !disabled">
      请上传
      <template v-if="fileSize"> 大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b> </template>
      <template v-if="fileType"> 格式为 <b style="color: #f56c6c">{{ fileType.join("/") }}</b> </template>
      <template v-if="compress"> 的文件</template>
      <template v-if="compress && compressEnabled">
        <span style="color: #52c41a; margin-left: 8px;">
          <el-icon><Picture /></el-icon>
          图片将自动压缩 (质量: {{ Math.round(compressQuality * 100) }}%, 最大: {{ compressMaxSize }}MB)
        </span>
      </template>
    </div>
    <!-- 文件列表 -->
    <transition-group ref="uploadFileList" class="upload-file-list el-upload-list el-upload-list--text" name="el-fade-in-linear" tag="ul">
      <li :key="file.uid" class="el-upload-list__item ele-upload-list__item-content" v-for="(file, index) in fileList">
        <el-link :href="`${baseUrl}${file.url}`" :underline="false" target="_blank">
          <span class="el-icon-document"> {{ getFileName(file.name) }} </span>
        </el-link>
        <div class="ele-upload-list__item-content-action">
          <el-link :underline="false" @click="handleDelete(index)" type="danger" v-if="!disabled">&nbsp;删除</el-link>
        </div>
      </li>
    </transition-group>
  </div>
</template>

<script setup>
import { getToken } from "@/utils/auth"
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
  // 数量限制
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
    default: () => ["doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "pdf"]
  },
  // 是否显示提示
  isShowTip: {
    type: Boolean,
    default: true
  },
  // 禁用组件（仅查看文件）
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

const { proxy } = getCurrentInstance()
const emit = defineEmits(['upload-success']) // 添加上传成功事件
const number = ref(0)
const uploadList = ref([])
const baseUrl = import.meta.env.VITE_APP_BASE_API
const uploadFileUrl = ref(import.meta.env.VITE_APP_BASE_API + props.action) // 上传文件服务器地址
const headers = ref({ Authorization: "Bearer " + getToken() })
const fileList = ref([])
const showTip = computed(
  () => props.isShowTip && (props.fileType || props.fileSize)
)

// 计算属性：是否启用压缩
const compressEnabled = computed(() => {
  return props.compress && (props.fileType.some(type => ['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp'].includes(type)) || true)
})
// 存储上传中的文件信息（用于获取文件大小）
const uploadingFiles = ref(new Map())
// 保存压缩后的文件
const compressedFiles = ref(new Map())

watch(() => props.modelValue, val => {
  if (val) {
    let temp = 1
    // 首先将值转为数组
    let list = []
    if (Array.isArray(val)) {
      list = val
    } else if (typeof val === 'string') {
      list = val.split(',')
    } else if (typeof val === 'object') {
      // 单个文件对象，转换为数组
      list = [val]
    }

    // 然后将数组转为对象数组
    fileList.value = list.map(item => {
      if (typeof item === "string") {
        item = { name: item, url: item }
      }
      item.uid = item.uid || new Date().getTime() + temp++
      return item
    })
  } else {
    fileList.value = []
    return []
  }
},{ deep: true, immediate: true })

// 上传前校检格式和大小
async function handleBeforeUpload(file) {
  // 校检文件类型
  if (props.fileType.length) {
    const fileName = file.name.split('.')
    const fileExt = fileName[fileName.length - 1]
    const isTypeOk = props.fileType.indexOf(fileExt) >= 0
    if (!isTypeOk) {
      proxy.$modal.msgError(`文件格式不正确，请上传${props.fileType.join("/")}格式文件!`)
      return false
    }
  }
  // 校检文件名是否包含特殊字符
  if (file.name.includes(',')) {
    proxy.$modal.msgError('文件名不正确，不能包含英文逗号!')
    return false
  }
  // 校检文件大小
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize
    if (!isLt) {
      proxy.$modal.msgError(`上传文件大小不能超过 ${props.fileSize} MB!`)
      return false
    }
  }

  // 🆕 图片压缩处理
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

  // 保存文件信息，用于上传成功后获取文件大小
  uploadingFiles.value.set(file.uid, {
    name: file.name,
    size: file.size
  })

  proxy.$modal.loading("正在上传文件，请稍候...")
  number.value++
  return true
}

// 文件个数超出
function handleExceed() {
  proxy.$modal.msgError(`上传文件数量不能超过 ${props.limit} 个!`)
}

// 上传失败
function handleUploadError(err) {
  proxy.$modal.msgError("上传文件失败")
  uploadedSuccessfully()
}

// 上传成功回调
function handleUploadSuccess(res, file) {
  if (res.code === 200) {
    // 获取上传前的文件信息
    const fileInfo = uploadingFiles.value.get(file.uid) || { name: file.name, size: 0 }
    const fileData = {
      name: res.fileName,
      url: res.fileName,
      originalName: fileInfo.name,
      size: fileInfo.size
    }
    uploadList.value.push(fileData)
    // 触发上传成功事件，传递文件信息
    emit('upload-success', fileData)
    uploadedSuccessfully()
  } else {
    number.value--
    uploadingFiles.value.delete(file.uid)
    proxy.$modal.msgError(res.msg)
    proxy.$refs.fileUpload.handleRemove(file)
    uploadedSuccessfully()
  }
}

// 删除文件
function handleDelete(index) {
  fileList.value.splice(index, 1)
  emit("update:modelValue", listToString(fileList.value))
}

// 上传结束处理
function uploadedSuccessfully() {
  if (number.value > 0 && uploadList.value.length === number.value) {
    fileList.value = fileList.value.filter(f => f.url !== undefined).concat(uploadList.value)
    uploadList.value = []
    number.value = 0
    uploadingFiles.value.clear()
    emit("update:modelValue", listToString(fileList.value))
  }
  // 兜底：确保一定关闭loading
  proxy.$modal.closeLoading()
}

// ✅ 核心修复：自定义上传函数
async function handleHttpRequest(options) {
  try {
    proxy.$modal.loading("正在上传文件，请稍候...")

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

    const data = await response.json()

    if (data.code === 200) {
      // 获取上传前的文件信息
      const fileInfo = uploadingFiles.value.get(options.file.uid) || { name: options.file.name, size: 0 }
      const fileData = {
        name: data.fileName,
        url: data.fileName,
        originalName: fileInfo.name,
        size: fileInfo.size
      }
      uploadList.value.push(fileData)
      // 清空该文件的压缩缓存和上传信息
      compressedFiles.value.delete(options.file.uid)
      uploadingFiles.value.delete(options.file.uid)
      // 触发上传成功事件
      emit('upload-success', fileData)
      // 调用el-upload的成功回调
      options.onSuccess(data, options.file)
      uploadedSuccessfully()
    } else {
      throw new Error(data.msg || '上传失败')
    }
  } catch (error) {
    proxy.$modal.msgError("上传文件失败")
    options.onError(error)
    number.value--
    compressedFiles.value.delete(options.file.uid)
    uploadingFiles.value.delete(options.file.uid)
    uploadedSuccessfully()
  }
}

// 获取文件名称
function getFileName(name) {
  // 如果name不存在或为空，返回空字符串
  if (!name) {
    return ''
  }
  // 如果是url那么取最后的名字 如果不是直接返回
  if (name.lastIndexOf("/") > -1) {
    return name.slice(name.lastIndexOf("/") + 1)
  } else {
    return name
  }
}

// 对象转成指定字符串分隔
function listToString(list, separator) {
  let strs = ""
  separator = separator || ","
  for (let i in list) {
    if (list[i].url) {
      strs += list[i].url + separator
    }
  }
  return strs != '' ? strs.substr(0, strs.length - 1) : ''
}

// 初始化拖拽排序
onMounted(() => {
  if (props.drag && !props.disabled) {
    nextTick(() => {
      const element = proxy.$refs.uploadFileList?.$el || proxy.$refs.uploadFileList
      Sortable.create(element, {
        ghostClass: 'file-upload-darg',
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
.file-upload-darg {
  opacity: 0.5;
  background: #c8ebfb;
}
.upload-file-uploader {
  margin-bottom: 5px;
}
.upload-file-list .el-upload-list__item {
  border: 1px solid #e4e7ed;
  line-height: 2;
  margin-bottom: 10px;
  position: relative;
  transition: none !important;
}
.upload-file-list .ele-upload-list__item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: inherit;
}
.ele-upload-list__item-content-action .el-link {
  margin-right: 10px;
}
</style>
