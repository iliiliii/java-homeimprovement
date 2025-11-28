<template>
  <!-- 验收上报对话框 -->
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    title="验收上报"
    width="600px"
    append-to-body
    :close-on-click-modal="false"
  >
    <el-form ref="acceptanceFormRef" :model="acceptanceForm" :rules="acceptanceRules" label-width="100px">
      <el-form-item label="验收内容" prop="content" required>
        <el-input
          v-model="acceptanceForm.content"
          type="textarea"
          :rows="6"
          placeholder="请描述验收情况 (不超过200字)"
          :maxlength="200"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="现场照片">
        <el-upload
          v-model:file-list="acceptanceForm.images"
          :action="uploadUrl"
          list-type="picture-card"
          :auto-upload="true"
          :limit="20"
          :headers="uploadHeaders"
          :on-exceed="handleExceed"
          :on-preview="handlePictureCardPreview"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="handleBeforeUpload"
        >
          <el-icon><Plus /></el-icon>
          <template #tip>
            <div class="el-upload__tip" style="color: #999; font-size: 12px; margin-top: 8px;">
              (最多20张)
            </div>
          </template>
        </el-upload>
      </el-form-item>
      <el-form-item label="验收结果" prop="result" required>
        <el-radio-group v-model="acceptanceForm.result">
          <el-radio label="QUALIFIED">合格</el-radio>
          <el-radio label="UNQUALIFIED">不合格</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="验收时间" prop="acceptanceTime" required>
        <el-date-picker
          v-model="acceptanceForm.acceptanceTime"
          type="datetime"
          placeholder="请选择验收时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%;"
        />
      </el-form-item>
      <el-form-item label="验收人" prop="acceptor" required>
        <el-input
          v-model="acceptanceForm.acceptor"
          placeholder="请输入验收人姓名"
          clearable
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">取 消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">提交验收</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 图片预览对话框 -->
  <el-dialog v-model="dialogImageVisible" title="图片预览" width="800px" append-to-body>
    <img :src="dialogImageUrl" alt="预览图片" style="width: 100%;" />
  </el-dialog>
</template>

<script setup name="AcceptanceReportDialog">
import { Plus } from "@element-plus/icons-vue"

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  project: {
    type: Object,
    default: null
  },
  scheduleItem: {
    type: Object,
    default: null
  },
  uploadUrl: {
    type: String,
    default: ''
  },
  uploadHeaders: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:visible', 'submit', 'success'])

const { proxy } = getCurrentInstance()

const saving = ref(false)
const dialogImageVisible = ref(false)
const dialogImageUrl = ref('')
const acceptanceForm = ref({
  content: '',
  images: [],
  result: 'QUALIFIED',
  acceptanceTime: '',
  acceptor: ''
})
const acceptanceRules = {
  content: [
    { required: true, message: '验收内容不能为空', trigger: 'blur' },
    { max: 200, message: '验收内容不能超过200字', trigger: 'blur' }
  ],
  result: [
    { required: true, message: '请选择验收结果', trigger: 'change' }
  ],
  acceptanceTime: [
    { required: true, message: '请选择验收时间', trigger: 'change' }
  ],
  acceptor: [
    { required: true, message: '验收人不能为空', trigger: 'blur' }
  ]
}

/** 初始化表单 */
watch(() => props.visible, (newVal) => {
  if (newVal && props.scheduleItem) {
    // 获取当前时间
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    const hours = String(now.getHours()).padStart(2, '0')
    const minutes = String(now.getMinutes()).padStart(2, '0')
    const seconds = String(now.getSeconds()).padStart(2, '0')
    const defaultTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`

    acceptanceForm.value = {
      content: '',
      images: [],
      result: 'QUALIFIED',
      acceptanceTime: defaultTime,
      acceptor: ''
    }
  }
})

/** 处理图片上传超出限制 */
function handleExceed() {
  proxy.$modal.msgWarning('最多只能上传20张图片')
}

/** 预览图片 */
function handlePictureCardPreview(file) {
  if (file.url) {
    dialogImageUrl.value = file.url.startsWith('http') ? file.url : import.meta.env.VITE_APP_BASE_API + file.url
  } else if (file.raw) {
    dialogImageUrl.value = URL.createObjectURL(file.raw)
  } else {
    dialogImageUrl.value = ''
  }
  dialogImageVisible.value = true
}

/** 上传前验证 */
function handleBeforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    proxy.$modal.msgError('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    proxy.$modal.msgError('上传图片大小不能超过 10MB!')
    return false
  }
  return true
}

/** 上传成功回调 */
function handleUploadSuccess(response, file) {
  if (response.code === 200) {
    file.url = response.fileName
    file.response = response
    proxy.$modal.msgSuccess('图片上传成功')
  } else {
    proxy.$modal.msgError(response.msg || '图片上传失败')
    const index = acceptanceForm.value.images.findIndex(img => img.uid === file.uid)
    if (index > -1) {
      acceptanceForm.value.images.splice(index, 1)
    }
  }
}

/** 上传失败回调 */
function handleUploadError(err, file) {
  proxy.$modal.msgError('图片上传失败')
  const index = acceptanceForm.value.images.findIndex(img => img.uid === file.uid)
  if (index > -1) {
    acceptanceForm.value.images.splice(index, 1)
  }
}

/** 提交验收 */
function handleSubmit() {
  proxy.$refs.acceptanceFormRef.validate(valid => {
    if (!valid) return

    if (!props.project || !props.scheduleItem) {
      proxy.$modal.msgError('数据错误')
      return
    }

    const hasUnuploadedImages = acceptanceForm.value.images.some(img => img.raw && !img.url && !img.response)
    if (hasUnuploadedImages) {
      proxy.$modal.msgWarning('请等待图片上传完成后再提交')
      return
    }

    saving.value = true

    // 根据验收结果设置完成度
    const completionRate = acceptanceForm.value.result === 'QUALIFIED' ? 100 : 0
    const description = acceptanceForm.value.content

    // 提取所有图片URL
    const imageUrls = acceptanceForm.value.images
      .map(img => {
        if (img.response && img.response.fileName) {
          return img.response.fileName
        }
        if (img.url) {
          if (img.url.startsWith('http')) {
            const baseUrl = import.meta.env.VITE_APP_BASE_API
            return img.url.replace(baseUrl, '').replace(/^\/+/, '')
          }
          return img.url
        }
        return null
      })
      .filter(url => url !== null)

    const recordData = {
      projectId: props.project.id,
      scheduleId: props.scheduleItem.id,
      recordType: 'ACCEPTANCE',
      completionRate: completionRate,
      description: description,
      images: JSON.stringify(imageUrls),
      acceptanceTitle: '',
      acceptanceContent: acceptanceForm.value.content,
      acceptanceResult: acceptanceForm.value.result,
      acceptanceTime: acceptanceForm.value.acceptanceTime,
      acceptor: acceptanceForm.value.acceptor
    }

    emit('submit', recordData)
  })
}

// 暴露给父组件使用
defineExpose({
  closeDialog: () => emit('update:visible', false),
  setSaving: (status) => saving.value = status
})
</script>

<style scoped lang="scss">
.dialog-footer {
  text-align: right;
}
</style>
