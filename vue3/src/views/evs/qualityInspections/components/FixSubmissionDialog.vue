<template>
  <!-- 整改提交对话框 -->
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    :title="dialogTitle"
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

      <el-form-item label="修复照片" prop="images">
        <ImageUploadCard
          ref="uploadRef"
          v-model="fixForm.images"
          :upload-url="uploadUrl"
          :upload-headers="uploadHeaders"
          :max-count="10"
          :max-size="5"
          tip-text="(最多10张，支持jpg、png格式，自动压缩)"
          :compress="true"
          :compress-quality="0.8"
          :compress-max-size="2"
          @success="handleUploadSuccess"
          @error="handleUploadError"
          @upload-status-change="handleUploadStatusChange"
        />
        <!-- 上传状态提示 -->
        <div v-if="uploadStatus.totalFiles > 0" class="upload-status-tip">
          <el-tag
              :type="uploadStatus.isAllUploaded ? 'success' : 'warning'"
              size="small"
          >
            <el-icon><Check v-if="uploadStatus.isAllUploaded" /><Loading v-else /></el-icon>
            {{ uploadStatus.isAllUploaded ? '图片上传完成' : `正在上传图片 (${uploadStatus.uploadedFiles}/${uploadStatus.totalFiles})` }}
          </el-tag>
          <span v-if="!uploadStatus.isAllUploaded" class="upload-hint">请等待图片上传完成后再提交</span>
        </div>
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
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="saving"
          :disabled="!uploadStatus.isAllUploaded"
        >提 交</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 图片预览对话框 -->
  <el-dialog v-model="dialogImageVisible" title="图片预览" width="800px" append-to-body>
    <img :src="dialogImageUrl" alt="预览图片" style="width: 100%;" />
  </el-dialog>
</template>

<script setup name="FixSubmissionDialog">
import { ref, watch, computed, getCurrentInstance } from 'vue'
import { getToken } from "@/utils/auth"
import { addQualityFixes, updateQualityFixes } from "@/api/evs/qualityFixes"
import { updateQualityIssues } from "@/api/evs/qualityIssues"
import ImageUploadCard from '@/components/ImageUploadCard/index.vue'
import { Plus, Check, Loading } from '@element-plus/icons-vue'

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
  },
  editMode: {
    type: Boolean,
    default: false
  },
  fixData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'success', 'error', 'refresh'])

const { proxy } = getCurrentInstance()

const saving = ref(false)
const uploadRef = ref(null)
const uploadStatus = ref({
  isAllUploaded: true,
  totalFiles: 0,
  uploadedFiles: 0
})
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
    { max: 500, message: '修复描述不能超过500字', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        // 检查是否包含特殊字符（XSS防护）
        const dangerousChars = /<script|javascript:|on\w+=/i
        if (dangerousChars.test(value)) {
          callback(new Error('修复描述不能包含特殊字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  status: [
    { required: true, message: '请选择修复状态', trigger: 'change' }
  ],
  images: [
    {
      validator: (rule, value, callback) => {
        // 检查图片数量限制
        if (value && Array.isArray(value) && value.length > 10) {
          callback(new Error('最多只能上传10张图片'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const uploadHeaders = ref({ Authorization: "Bearer " + getToken() })

// 计算对话框标题
const dialogTitle = computed(() => {
  return props.editMode ? '编辑整改' : '提交整改'
})

/** 监听对话框显示状态，初始化表单数据 */
watch(() => props.visible, (newVal) => {
  if (newVal && props.issue) {
    console.log('🔍 [FORM] 初始化表单', { editMode: props.editMode, fixData: props.fixData })

    // 生成默认时间
    const now = new Date()
    const defaultTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`

    if (props.editMode && props.fixData) {
      // 编辑模式：预填充现有数据
      console.log('🔍 [FORM] 编辑模式，预填充数据')

      // 解析图片数据 - 使用 ImageUploadCard 的格式
      let images = []
      try {
        if (props.fixData.images) {
          const parsedImages = typeof props.fixData.images === 'string'
            ? JSON.parse(props.fixData.images)
            : props.fixData.images
          if (Array.isArray(parsedImages)) {
            images = parsedImages.map((img, index) => {
              // 确保图片URL格式正确
              let imageUrl = ''
              const baseUrl = import.meta.env.VITE_APP_BASE_API

              if (img.startsWith('http')) {
                // 完整URL直接使用
                imageUrl = img
              } else if (img.startsWith(baseUrl)) {
                // 已包含baseUrl的路径直接使用
                imageUrl = img
              } else {
                // 纯粹的相对路径需要拼接baseUrl
                let path = img
                if (!path.startsWith('/')) {
                  path = '/' + path
                }
                const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
                imageUrl = cleanBaseUrl + path
              }

              return {
                uid: `edit-${index}`,
                name: `image-${index}.jpg`,
                url: imageUrl
              }
            })
          }
        }
      } catch (error) {
        console.warn('图片数据解析失败:', error)
      }

      fixForm.value = {
        id: props.fixData.id,
        qualityIssuesId: props.issue.id,
        fixDescription: props.fixData.fixDescription || '',
        images: images,
        status: props.fixData.status || 'IN_PROGRESS',
        fixedAt: props.fixData.fixedAt || ''
      }
    } else {
      // 新建模式：初始化空表单
      console.log('🔍 [FORM] 新建模式，初始化空表单')
      fixForm.value = {
        qualityIssuesId: props.issue.id,
        fixDescription: '',
        images: [],
        status: 'IN_PROGRESS',
        fixedAt: props.issue.status === 'RESOLVED' ? defaultTime : ''
      }
    }

    console.log('🔍 [FORM] 表单已初始化', fixForm.value)
  }
})

/** 监听修复状态变化 */
watch(() => fixForm.value.status, (newStatus, oldStatus) => {
  console.log('🔍 [FORM WATCH] 状态变化:', { oldStatus, newStatus })
  if (newStatus === 'RESOLVED' && !fixForm.value.fixedAt) {
    // 如果状态变为已解决，自动设置修复时间
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    const hours = String(now.getHours()).padStart(2, '0')
    const minutes = String(now.getMinutes()).padStart(2, '0')
    const seconds = String(now.getSeconds()).padStart(2, '0')
    const autoTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    console.log('🔍 [FORM WATCH] 自动设置修复时间:', autoTime)
    fixForm.value.fixedAt = autoTime
  }
})

/** 🔍 监听表单数据变化 - 简化版本 */
watch(() => fixForm.value, (newVal, oldVal) => {
  if (oldVal && newVal) {
    const changed = []
    if (oldVal.fixDescription !== newVal.fixDescription) changed.push('修复描述')
    if (oldVal.status !== newVal.status) changed.push('修复状态')
    if (JSON.stringify(oldVal.images) !== JSON.stringify(newVal.images)) changed.push('修复图片')
    if (oldVal.fixedAt !== newVal.fixedAt) changed.push('修复时间')

    if (changed.length > 0) {
      console.log('🔍 [FORM] 字段变化:', changed.join(', '))
    }
  }
}, { deep: true })

/** 上传状态变化回调 */
function handleUploadStatusChange(status) {
  uploadStatus.value = status
  console.log('上传状态变化:', status)
}

/** 上传成功回调 - 优化版本 */
function handleUploadSuccess({ file, response }) {
  try {
    console.log('整改记录图片上传成功:', { file, response })
  } catch (error) {
    console.error('整改记录图片上传回调处理失败:', error)
  }
}

/** 上传失败回调 - 优化版本 */
function handleUploadError({ file, message }) {
  try {
    console.error('整改记录图片上传失败:', { file, message })
  } catch (error) {
    console.error('整改记录图片上传错误回调处理失败:', error)
  }
}

/** 业务逻辑验证 */
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

  // 修复时间合理性检查
  if (form.fixedAt) {
    const fixedDate = new Date(form.fixedAt)
    const now = new Date()

    // 如果修复时间在未来，给出警告
    if (fixedDate > now) {
      const confirmResult = confirm('修复时间晚于当前时间，确定要提交吗？')
      if (!confirmResult) {
        return false
      }
    }
  }

  return true
}

/** 网络和服务器状态检查 */
function checkNetworkAndServerStatus() {
  // 检查网络连接
  if (!navigator.onLine) {
    proxy.$modal.msgError('网络连接已断开，请检查网络后重试')
    return false
  }

  // 检查用户认证状态
  const token = getToken()
  if (!token) {
    proxy.$modal.msgError('用户认证已失效，请重新登录')
    return false
  }

  return true
}


/** 提交整改 */
async function handleSubmit() {
  try {
    // 🔍 调试日志：输出表单当前状态
    console.log('🔍 [DEBUG] 表单提交前的完整数据:', JSON.stringify(fixForm.value, null, 2))
    console.log('🔍 [DEBUG] fixForm.fixDescription:', JSON.stringify(fixForm.value.fixDescription))
    console.log('🔍 [DEBUG] fixForm.status:', JSON.stringify(fixForm.value.status))
    console.log('🔍 [DEBUG] fixForm.images:', JSON.stringify(fixForm.value.images))

    // 表单验证
    const valid = await new Promise((resolve) => {
      proxy.$refs.fixFormRef.validate((valid, fields) => {
        console.log('🔍 [DEBUG] 表单验证结果:', { valid, fields })
        resolve(valid)
      })
    })
    if (!valid) {
      console.log('❌ [ERROR] 表单验证失败，提交终止')
      return
    }

    // 业务逻辑验证
    if (!validateBusinessLogic()) {
      console.log('❌ [ERROR] 业务逻辑验证失败，提交终止')
      return
    }

    if (!props.issue) {
      proxy.$modal.msgError('数据错误')
      return
    }

    // 检查图片上传状态
    if (!uploadStatus.value.isAllUploaded) {
      proxy.$modal.msgWarning('请等待图片上传完成后再提交')
      return
    }

    // 业务逻辑验证
    if (!validateBusinessLogic()) {
      console.log('❌ [ERROR] 业务逻辑验证失败，提交终止')
      return
    }

    // 网络和认证状态检查
    if (!checkNetworkAndServerStatus()) {
      return
    }

    saving.value = true

    // 🔧 使用 ImageUploadCard 的 extractImageUrls 方法处理图片
    const processedImages = uploadRef.value?.extractImageUrls(fixForm.value.images) || []

    console.log('🔍 [DEBUG] 处理后的图片:', processedImages)

    // 🔧 构造修复数据对象
    const fixData = {
      qualityIssuesId: props.issue.id,
      fixDescription: fixForm.value.fixDescription || '无',
      images: processedImages.length > 0 ? JSON.stringify(processedImages) : '[]',
      status: fixForm.value.status || 'IN_PROGRESS',
      fixedAt: fixForm.value.status === 'RESOLVED' ? fixForm.value.fixedAt : null,
      verifiedAt: null
    }

    console.log('🔍 [DEBUG] 最终提交数据:', JSON.stringify(fixData, null, 2))

    let apiResponse
    if (props.editMode) {
      // 编辑模式：调用更新API
      fixData.id = props.fixData.id
      apiResponse = await updateQualityFixes(fixData)
      console.log('🔍 [API RESPONSE] 编辑模式响应:', apiResponse)
    } else {
      // 新建模式：调用创建API
      apiResponse = await addQualityFixes(fixData)
      console.log('🔍 [API RESPONSE]:', apiResponse)
    }

    // 更新问题状态 - 修复：使用正确的 ref 访问方式
    const issueUpdateData = {
      id: props.issue.id,
      status: fixForm.value.status,  // 🔧 修复：正确访问 ref 的值
      resolvedAt: fixForm.value.status === 'RESOLVED' ? fixForm.value.fixedAt : null
    }

    console.log('🔍 [DEBUG] 问题更新数据:', JSON.stringify(issueUpdateData, null, 2))

    console.log('🔍 [API CALL] 准备调用 updateQualityIssues API')
    const updateResponse = await updateQualityIssues(issueUpdateData)
    console.log('🔍 [API RESPONSE] 问题状态更新响应:', updateResponse)

    const successMessage = props.editMode ? '整改编辑成功' : '整改提交成功'
    proxy.$modal.msgSuccess(successMessage)
    emit('success', fixData)
    emit('refresh') // 通知父组件刷新数据
    emit('update:visible', false)

  } catch (error) {
    console.error('提交整改失败:', error)

    // 统一错误处理
    if (error.response?.status === 401) {
      proxy.$modal.msgError('用户认证已失效，请重新登录')
    } else if (error.response?.status >= 500) {
      proxy.$modal.msgError('服务器错误，请稍后重试')
    } else {
      proxy.$modal.msgError(error.message || error.msg || '提交失败，请重试')
    }

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

.upload-status-tip {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;

  .el-tag {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .upload-hint {
    color: #e6a23c;
    font-size: 12px;
  }
}
</style>