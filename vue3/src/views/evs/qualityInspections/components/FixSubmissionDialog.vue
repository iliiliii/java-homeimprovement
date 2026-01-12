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
          v-model="imagesFileList"
          v-bind="getUploadProps()"
          @success="handleUploadSuccess"
          @error="handleUploadError"
          @upload-status-change="handleUploadStatusChange"
        />
        <!-- 上传状态提示 -->
        <div v-if="getStatusTip().show" class="upload-status-tip">
          <el-tag :type="getStatusTip().type" size="small">
            <el-icon><Loading v-if="!uploadStatus.isAllUploaded" /><Check v-else /></el-icon>
            {{ getStatusTip().message }}
          </el-tag>
          <span v-if="!uploadStatus.isAllUploaded && uploadStatus.totalFiles > 0" class="upload-hint">
            请等待图片上传完成后再提交
          </span>
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
          @click="handleFixSubmit"
          v-bind="getButtonProps('提 交')"
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
import { useUploadManager, uploadPresets } from '@/composables/useUploadManager'
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

// 修复照片上传状态
const imagesFileList = ref([])

// 初始化上传管理Hook - 使用整改预设配置
const {
  uploadStatus,
  submitting,
  uploadRef,
  isSubmitDisabled,
  handleSubmit,
  extractImageUrls,
  handleUploadStatusChange,
  handleUploadSuccess,
  handleUploadError,
  reset,
  getUploadProps,
  getButtonProps,
  getStatusTip
} = useUploadManager(uploadPresets.fix)
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
        images: parsedImages,
        status: props.fixData.status || 'IN_PROGRESS',
        fixedAt: props.fixData.fixedAt || ''
      }

      // 处理编辑模式下的图片回显
      if (images.length > 0) {
        // 优先使用组件的 parseFileIdsToList 方法（已处理URL拼接）
        // 如果组件未初始化，则手动拼接 VITE_APP_BASE_API
        if (uploadRef.value?.parseFileIdsToList) {
          imagesFileList.value = uploadRef.value.parseFileIdsToList(images)
        } else {
          const baseUrl = import.meta.env.VITE_APP_BASE_API
          imagesFileList.value = images.map((url, index) => {
            let fullUrl = url
            if (!url.startsWith('http') && !url.startsWith(baseUrl)) {
              const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
              const imagePath = url.startsWith('/') ? url : '/' + url
              fullUrl = cleanBaseUrl + imagePath
            }
            return {
              uid: `existing-${index}`,
              name: `image-${index}.jpg`,
              url: fullUrl,
              status: 'success'
            }
          })
        }
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
      // 重置图片上传状态
      imagesFileList.value = []
      // 重置上传管理器状态
      reset()
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



/** 格式化图片数据 */
function formatImagesData() {
  try {
    // 从图片上传组件提取URL数组
    const uploadedImages = extractImageUrls(imagesFileList.value)

    // 返回JSON字符串格式，保持与后端的兼容性
    return uploadedImages.length > 0 ? JSON.stringify(uploadedImages) : '[]'
  } catch (error) {
    console.error('格式化图片数据失败:', error)
    return '[]'
  }
}

/** 提交整改 - 集成Hook统一处理 */
async function handleFixSubmit() {
  // 使用统一的提交处理逻辑，但保留业务验证和调试日志
  handleSubmit(async () => {
    // 表单验证
    const valid = await new Promise((resolve) => {
      proxy.$refs.fixFormRef.validate((valid, fields) => {
        console.log('🔍 [DEBUG] 表单验证结果:', { valid, fields })
        resolve(valid)
      })
    })

    if (!valid) {
      throw new Error('表单验证失败')
    }

    // 业务逻辑验证
    if (!validateBusinessLogic()) {
      throw new Error('业务逻辑验证失败')
    }

    if (!props.issue) {
      throw new Error('数据错误')
    }

    // 设置loading状态
    saving.value = true

    // 🔍 调试日志：输出表单当前状态
    console.log('🔍 [DEBUG] 表单提交前的完整数据:', JSON.stringify(fixForm.value, null, 2))
    console.log('🔍 [DEBUG] fixForm.fixDescription:', JSON.stringify(fixForm.value.fixDescription))
    console.log('🔍 [DEBUG] fixForm.status:', JSON.stringify(fixForm.value.status))
    console.log('🔍 [DEBUG] fixForm.images:', JSON.stringify(fixForm.value.images))

    // 🔧 构造修复数据对象
    const fixData = {
      qualityIssuesId: props.issue.id,
      fixDescription: fixForm.value.fixDescription || '无',
      images: formatImagesData(),
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

    // 更新问题状态
    const issueUpdateData = {
      id: props.issue.id,
      status: fixForm.value.status,
      resolvedAt: fixForm.value.status === 'RESOLVED' ? fixForm.value.fixedAt : null
    }

    console.log('🔍 [DEBUG] 问题更新数据:', JSON.stringify(issueUpdateData, null, 2))
    console.log('🔍 [API CALL] 准备调用 updateQualityIssues API')
    const updateResponse = await updateQualityIssues(issueUpdateData)
    console.log('🔍 [API RESPONSE] 问题状态更新响应:', updateResponse)

    return { fixData, apiResponse, updateResponse }
  }).then(({ fixData, apiResponse, updateResponse }) => {
    // 成功处理
    const successMessage = props.editMode ? '整改编辑成功' : '整改提交成功'
    proxy.$modal.msgSuccess(successMessage)
    emit('success', fixData)
    emit('refresh') // 通知父组件刷新数据
    emit('update:visible', false)
  }).catch((error) => {
    // 统一错误处理（Hook已处理错误提示，这里只处理业务逻辑）
    console.error('提交整改失败:', error)
    emit('error', error)
  }).finally(() => {
    // 重置loading状态
    saving.value = false
  })
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