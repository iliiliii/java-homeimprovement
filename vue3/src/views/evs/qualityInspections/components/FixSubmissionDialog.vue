<template>
  <!-- 整改提交对话框 -->
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    title="提交整改"
    width="600px"
    append-to-body
    :close-on-click-modal="false"
  >
    <el-form ref="fixFormRef" :model="fixForm" :rules="fixRules" label-width="100px">
      <el-form-item label="修复描述" prop="fixDescription" required>
        <el-input
          v-model="fixForm.fixDescription"
          type="textarea"
          :rows="4"
          placeholder="请详细描述修复措施和方法"
          :maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="修复状态" prop="status" required>
        <el-radio-group v-model="fixForm.status">
          <el-radio label="OPEN">未解决</el-radio>
          <el-radio label="IN_PROGRESS">解决中</el-radio>
          <el-radio label="RESOLVED">已解决</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="修复照片">
        <el-upload
          v-model:file-list="fixForm.images"
          :action="uploadUrl"
          list-type="picture-card"
          :auto-upload="true"
          :limit="10"
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
              (最多10张，支持jpg、png格式)
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item label="修复时间" prop="fixedAt" v-if="fixForm.status === 'RESOLVED'">
        <el-date-picker
          v-model="fixForm.fixedAt"
          type="datetime"
          placeholder="请选择修复时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%;"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">取 消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">提 交</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 图片预览对话框 -->
  <el-dialog v-model="dialogImageVisible" title="图片预览" width="800px" append-to-body>
    <img :src="dialogImageUrl" alt="预览图片" style="width: 100%;" />
  </el-dialog>
</template>

<script setup name="FixSubmissionDialog">
import { onMounted, watch } from 'vue'
import { getToken } from "@/utils/auth"
import { addQualityFixes } from "@/api/evs/qualityFixes"
import { updateQualityIssues } from "@/api/evs/qualityIssues"
import { Plus } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  issue: {
    type: Object,
    default: null
  },
  uploadUrl: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible', 'success', 'error', 'refresh'])

const { proxy } = getCurrentInstance()

const saving = ref(false)
const uploadRef = ref(null)
const dialogImageVisible = ref(false)
const dialogImageUrl = ref('')

const fixForm = ref({
  qualityIssuesId: '',
  fixDescription: '',
  images: [],
  status: 'IN_PROGRESS',
  fixedAt: ''
})

const fixRules = {
  fixDescription: [
    { required: true, message: '修复描述不能为空', trigger: 'blur' },
    { max: 500, message: '修复描述不能超过500字', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择修复状态', trigger: 'change' }
  ]
}

const uploadHeaders = ref({ Authorization: "Bearer " + getToken() })

/** 监听对话框显示状态，初始化表单数据 */
watch(() => props.visible, (newVal) => {
  if (newVal && props.issue) {
    // 初始化表单数据
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    const hours = String(now.getHours()).padStart(2, '0')
    const minutes = String(now.getMinutes()).padStart(2, '0')
    const seconds = String(now.getSeconds()).padStart(2, '0')
    const defaultTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`

    fixForm.value = {
      qualityIssuesId: props.issue.id,
      fixDescription: '',
      images: [],
      status: 'IN_PROGRESS',
      fixedAt: props.issue.status === 'RESOLVED' ? defaultTime : ''
    }
  }
})

/** 监听修复状态变化 */
watch(() => fixForm.value.status, (newStatus) => {
  if (newStatus === 'RESOLVED' && !fixForm.value.fixedAt) {
    // 如果状态变为已解决，自动设置修复时间
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    const hours = String(now.getHours()).padStart(2, '0')
    const minutes = String(now.getMinutes()).padStart(2, '0')
    const seconds = String(now.getSeconds()).padStart(2, '0')
    fixForm.value.fixedAt = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  }
})

/** 处理图片上传超出限制 */
function handleExceed() {
  proxy.$modal.msgWarning('最多只能上传10张图片')
}

/** 预览图片 */
function handlePictureCardPreview(file) {
  if (file.url) {
    dialogImageUrl.value = file.url.startsWith('http') ? file.url : props.uploadUrl + file.url
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
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    proxy.$modal.msgError('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    proxy.$modal.msgError('上传图片大小不能超过 5MB!')
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
    const index = fixForm.value.images.findIndex(img => img.uid === file.uid)
    if (index > -1) {
      fixForm.value.images.splice(index, 1)
    }
  }
}

/** 上传失败回调 */
function handleUploadError(err, file) {
  proxy.$modal.msgError('图片上传失败')
  const index = fixForm.value.images.findIndex(img => img.uid === file.uid)
  if (index > -1) {
    fixForm.value.images.splice(index, 1)
  }
}

/** 验证业务逻辑 */
function validateBusinessLogic() {
  const form = fixForm.value

  // 如果状态为已解决但没有修复时间，自动设置
  if (form.status === 'RESOLVED' && !form.fixedAt) {
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    const hours = String(now.getHours()).padStart(2, '0')
    const minutes = String(now.getMinutes()).padStart(2, '0')
    const seconds = String(now.getSeconds()).padStart(2, '0')
    form.fixedAt = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  }

  return true
}

/** 提交整改 */
async function handleSubmit() {
  try {
    // 表单验证
    const valid = await new Promise((resolve) => {
      proxy.$refs.fixFormRef.validate(resolve)
    })
    if (!valid) return

    // 业务逻辑验证
    if (!validateBusinessLogic()) {
      return
    }

    if (!props.issue) {
      proxy.$modal.msgError('数据错误')
      return
    }

    // 检查是否有未上传完成的图片
    const hasUnuploadedImages = fixForm.value.images.some(img => img.raw && !img.url && !img.response)
    if (hasUnuploadedImages) {
      proxy.$modal.msgWarning('请等待图片上传完成后再提交')
      return
    }

    saving.value = true

    // 提取所有图片URL
    const imageUrls = fixForm.value.images
      .map(img => {
        if (img.response && img.response.fileName) {
          return img.response.fileName
        }
        if (img.url) {
          if (img.url.startsWith('http')) {
            const baseUrl = props.uploadUrl
            return img.url.replace(baseUrl, '').replace(/^\/+/, '')
          }
          return img.url
        }
        return null
      })
      .filter(url => url !== null)

    // 创建修复记录
    const fixData = {
      qualityIssuesId: props.issue.id,
      fixDescription: fixForm.fixDescription,
      images: JSON.stringify(imageUrls),
      status: fixForm.status,
      fixedAt: fixForm.status === 'RESOLVED' ? fixForm.fixedAt : null,
      verifiedAt: fixForm.status === 'RESOLVED' ? fixForm.fixedAt : null
    }

    await addQualityFixes(fixData)

    // 更新问题状态
    const issueUpdateData = {
      id: props.issue.id,
      status: fixForm.status === 'RESOLVED' ? 'RESOLVED' : 'IN_PROGRESS',
      resolvedAt: fixForm.status === 'RESOLVED' ? fixForm.fixedAt : null
    }

    await updateQualityIssues(issueUpdateData)

    proxy.$modal.msgSuccess('整改提交成功')
    emit('success', fixData)
    emit('refresh') // 通知父组件刷新数据
    emit('update:visible', false)

  } catch (error) {
    console.error('提交整改失败:', error)
    proxy.$modal.msgError('整改提交失败：' + (error.msg || error.message))
    emit('error', error)
  } finally {
    saving.value = false
  }
}

// 暴露给父组件使用
defineExpose({
  closeDialog: () => emit('update:visible', false)
})
</script>

<style scoped lang="scss">
.dialog-footer {
  text-align: right;
}
</style>