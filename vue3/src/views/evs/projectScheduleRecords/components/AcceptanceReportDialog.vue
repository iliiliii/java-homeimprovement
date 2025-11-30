<template>
  <!-- 验收上报对话框 -->
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    :title="isEdit ? '编辑验收记录' : '验收上报'"
    width="600px"
    append-to-body
    :close-on-click-modal="false"
  >
    <el-form ref="acceptanceFormRef" :model="acceptanceForm" :rules="acceptanceRules" label-width="100px">
      <el-form-item label="验收标题" prop="title">
        <el-input
          v-model="acceptanceForm.title"
          placeholder="请输入验收标题（可选）"
          clearable
          :maxlength="50"
          show-word-limit
        />
      </el-form-item>
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
      <el-form-item label="现场照片" prop="images">
        <ImageUploadCard
          ref="uploadRef"
          v-model="acceptanceForm.images"
          :upload-url="props.uploadUrl"
          :upload-headers="{
            Authorization: 'Bearer ' + userStore.token
          }"
          tip-text="(最多20张，支持多选)"
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
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="saving"
          :disabled="!uploadStatus.isAllUploaded"
        >
          {{ isEdit ? '更新验收' : '提交验收' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="AcceptanceReportDialog">
import { onMounted, watch } from 'vue'
import useUserStore from '@/store/modules/user'
import { listProjectScheduleRecords } from "@/api/evs/projectScheduleRecords"
import ImageUploadCard from '@/components/ImageUploadCard/index.vue'
import { Check, Loading } from '@element-plus/icons-vue'

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
  isEdit: {
    type: Boolean,
    default: false
  },
  editRecord: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'submit', 'success', 'error', 'loading-change'])

const { proxy } = getCurrentInstance()

const userStore = useUserStore()
const { decoration_construction_stage } = proxy.useDict('decoration_construction_stage')

const saving = ref(false)
const uploadRef = ref(null)
const uploadStatus = ref({
  isAllUploaded: true,
  totalFiles: 0,
  uploadedFiles: 0
})
const acceptanceForm = ref({
  title: '',
  content: '',
  images: [],
  result: 'QUALIFIED',
  acceptanceTime: '',
  acceptor: ''
})
const acceptanceRules = {
  title: [
    { required: true, message: '验收标题不能为空', trigger: 'blur' },
    { max: 50, message: '验收标题不能超过50字', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        // 检查是否包含特殊字符（XSS防护）
        const dangerousChars = /<script|javascript:|on\w+=/i
        if (dangerousChars.test(value)) {
          callback(new Error('标题不能包含特殊字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  content: [
    { required: true, message: '验收内容不能为空', trigger: 'blur' },
    { max: 200, message: '验收内容不能超过200字', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        // 检查内容长度是否过短（无效输入）
        if (value && value.trim().length < 5) {
          callback(new Error('验收内容至少需要5个字符'))
        } else {
          // 检查是否包含危险脚本
          const dangerousChars = /<script|javascript:|on\w+=/i
          if (dangerousChars.test(value)) {
            callback(new Error('内容不能包含特殊字符'))
          } else {
            callback()
          }
        }
      },
      trigger: 'blur'
    }
  ],
  images: [
    {
      validator: (rule, value, callback) => {
        // 检查图片数量限制
        if (value && Array.isArray(value) && value.length > 20) {
          callback(new Error('最多只能上传20张图片'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  result: [
    { required: true, message: '请选择验收结果', trigger: 'change' }
  ],
  acceptanceTime: [
    { required: true, message: '请选择验收时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value) {
          const selectedTime = new Date(value)
          const now = new Date()
          const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
          const oneWeekLater = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)

          // 验收时间不能早于一周前
          if (selectedTime < oneWeekAgo) {
            callback(new Error('验收时间不能早于一周前'))
          }
          // 验收时间不能晚于一周后
          else if (selectedTime > oneWeekLater) {
            callback(new Error('验收时间不能晚于一周后'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  acceptor: [
    { required: true, message: '验收人不能为空', trigger: 'blur' },
    { min: 2, max: 20, message: '验收人姓名长度应在2-20个字符之间', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        // 检查是否包含特殊字符
        const validNamePattern = /^[\u4e00-\u9fa5a-zA-Z\s]+$/
        if (value && !validNamePattern.test(value)) {
          callback(new Error('验收人姓名只能包含中文、英文和空格'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

/** 监听对话框显示状态，��始化表单数据 */
watch(() => props.visible, async (newVal) => {
  // 确保只在新曾模式下初始化，编辑模式下由编辑监听器处理
  if (newVal && props.scheduleItem && !props.isEdit) {
    try {
      // 1. 查询当前阶段已验收次数
      const response = await listProjectScheduleRecords({
        scheduleId: props.scheduleItem.id,
        recordType: 'ACCEPTANCE'
      })
      const acceptanceCount = response.rows?.length || 0

      // 2. 生成验收标题（检查验收-流程标题-#序号）
      // stage是编码（如DEMOLITION），需要转换为显示名称（如拆除工程）
      const stageDict = decoration_construction_stage.value.find(dict => dict.value === props.scheduleItem.stage)
      const stageName = stageDict?.label || props.scheduleItem.stage || '验收'
      const autoGeneratedTitle = `检查验收-${stageName}-#${acceptanceCount + 1}`

      // 3. 生成默认验收时间（当前时间）
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      const seconds = String(now.getSeconds()).padStart(2, '0')
      const defaultTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`

      // 4. 重置表单为初始状态
      acceptanceForm.value = {
        title: autoGeneratedTitle,               // 自动生成的验收标题
        content: '',                              // 验收内容（需手动填写）
        images: [],                               // 现场照片
        result: 'QUALIFIED',                      // 验收结果（默认合格）
        acceptanceTime: defaultTime,              // 验收时间
        acceptor: userStore.nickName || userStore.name || ''  // 默认验收人为当前登录用户昵称
      }
    } catch (error) {
      console.error('初始化验收表单失败:', error)
      proxy.$modal.msgError('初始化表单失败，请重试')

      // 失败时仍初始化基本表单
      acceptanceForm.value = {
        title: '',
        content: '',
        images: [],
        result: 'QUALIFIED',
        acceptanceTime: '',
        acceptor: userStore.nickName || userStore.name || ''
      }
    }
  }
})

/** 监听编辑模式变化，预填充表单 */
watch(() => [props.visible, props.isEdit, props.editRecord], async ([visible, isEdit, editRecord]) => {
  // 当对话框显示、处于编辑模式且有编辑记录时，填充表单数据
  if (visible && isEdit && editRecord) {
    try {
      console.log('编辑模式：预填充表单数据', editRecord)

      // 解析图片数组
      let images = []
      if (editRecord.images) {
        try {
          images = JSON.parse(editRecord.images)
        } catch (e) {
          console.warn('解析图片JSON失败，使用空数组', e)
          images = []
        }
      }

      acceptanceForm.value = {
        title: editRecord.acceptanceTitle || '',
        content: editRecord.acceptanceContent || '',
        images: images.map((img, index) => {
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
        }),
        result: editRecord.acceptanceResult || 'QUALIFIED',
        acceptanceTime: editRecord.acceptanceTime || '',
        acceptor: editRecord.acceptor || ''
      }
      console.log('表单数据已预填充', acceptanceForm.value)
    } catch (error) {
      console.error('初始化编辑表单失败:', error)
      proxy.$modal.msgError('初始化表单失败，请重试')
    }
  }
}, { immediate: true })

/** 上传成功回调 - 优化版本 */
function handleUploadSuccess({ file, response }) {
  try {
    // 新组件已经处理了URL设置和成功提示
    console.log('验收记录图片上传成功:', { file, response })

    // 可以在这里添加特殊的业务逻辑，如记录日志等
    // 例如：统计上传成功的图片数量、验证图片格式等

  } catch (error) {
    console.error('验收记录图片上传回调处理失败:', error)
    // 不需要显示错误提示，因为组件已经处理了
  }
}

/** 上传失败回调 - 优化版本 */
function handleUploadError({ file, message }) {
  try {
    // 新组件已经处理了错误提示和文件移除
    console.error('验收记录图片上传失败:', { file, message })

    // 可以在这里添加特殊的业务逻辑，如记录错误日志等
    // 例如：统计失败次数、分析失败原因等

  } catch (error) {
    console.error('验收记录图片上传错误回调处理失败:', error)
  }
}

/** 上传状态变化回调 */
function handleUploadStatusChange(status) {
  uploadStatus.value = status
  console.log('上传状态变化:', status)
}

/** 业务逻辑验证 */
function validateBusinessLogic() {
  const form = acceptanceForm.value

  // 验收内容不能只有标题或重复内容
  if (form.content && form.content.trim() === form.title?.trim()) {
    proxy.$modal.msgError('验收内容不能与标题相同')
    return false
  }

  // 验收人不能与当前用户不符（如果是首次提交）
  if (!props.isEdit) {
    const currentUser = userStore.nickName || userStore.name || ''
    if (form.acceptor && form.acceptor !== currentUser) {
      // 这里只是警告，不是错误，允许用户选择其他验收人
      console.warn('验收人与当前登录用户不一致', {
        current_user: currentUser,
        selected_acceptor: form.acceptor
      })
    }
  }

  // 验收时间合理性检查
  if (form.acceptanceTime) {
    const acceptanceDate = new Date(form.acceptanceTime)
    const now = new Date()

    // 如果验收时间在未来，给出警告
    if (acceptanceDate > now) {
      const confirmResult = confirm('验收时间晚于当前时间，确定要提交吗？')
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
  const token = localStorage.getItem('token') || userStore.token
  if (!token) {
    proxy.$modal.msgError('用户认证已失效，请重新登录')
    return false
  }

  return true
}

/** 提交验收 - 重构为自管理API调用 */
async function handleSubmit() {
  // 表单验证
  try {
    const valid = await new Promise((resolve) => {
      proxy.$refs.acceptanceFormRef.validate(resolve)
    })
    if (!valid) return

    // 业务逻辑验证
    if (!validateBusinessLogic()) {
      return
    }

    if (!props.project || !props.scheduleItem) {
      proxy.$modal.msgError('数据错误')
      return
    }

    // 检查图片上传状态
    if (!uploadStatus.value.isAllUploaded) {
      proxy.$modal.msgWarning('请等待图片上传完成后再提交')
      return
    }

    // 网络和认证状态检查
    if (!checkNetworkAndServerStatus()) {
      return
    }

    // 设置loading状态
    saving.value = true
    emit('loading-change', true)

    // API调用监控
    const apiStartTime = Date.now()
    console.log('开始API调用，时间戳:', apiStartTime)

    // 构造验收记录数据对象
    const recordData = {
      projectId: props.project.id,
      scheduleId: props.scheduleItem.id,
      recordType: 'ACCEPTANCE',
      images: JSON.stringify(uploadRef.value?.extractImageUrls(acceptanceForm.value.images) || []),
      acceptanceTitle: acceptanceForm.value.title,
      acceptanceContent: acceptanceForm.value.content,
      acceptanceResult: acceptanceForm.value.result,
      acceptanceTime: acceptanceForm.value.acceptanceTime,
      acceptor: acceptanceForm.value.acceptor
    }

    console.log('提交验收记录数据:', recordData)

    // 直接调用API，不依赖父组件
    const { addProjectScheduleRecords } = await import('@/api/evs/projectScheduleRecords')
    await addProjectScheduleRecords(recordData)

    // API调用成功监控
    const apiEndTime = Date.now()
    const apiDuration = apiEndTime - apiStartTime
    console.log(`API调用成功，耗时: ${apiDuration}ms`)

    // 成功处理
    proxy.$modal.msgSuccess('验收上报成功')
    emit('success', recordData)

    // 重置loading状态
    saving.value = false
    emit('loading-change', false)

    // 关闭对话框
    emit('update:visible', false)

    console.log('验收提交成功，loading状态已重置，对话框已关闭')

  } catch (error) {
    // 统一错误处理
    console.error('提交验收数据时出错:', error)

    // 重置loading状态
    saving.value = false
    emit('loading-change', false)
    emit('error', error)

    // 显示错误信息
    if (error.response?.status === 401) {
      proxy.$modal.msgError('用户认证已失效，请重新登录')
    } else if (error.response?.status >= 500) {
      proxy.$modal.msgError('服务器错误，请稍后重试')
    } else {
      proxy.$modal.msgError(error.message || error.msg || '提交失败，请重试')
    }
  }
}

// 组件初始化
onMounted(() => {
  console.log('AcceptanceReportDialog组件已挂载')
  console.log('初始uploadStatus:', uploadStatus.value)
  console.log('初始saving状态:', saving.value)
})

// 监听uploadStatus变化
watch(() => uploadStatus.value, (newVal, oldVal) => {
  console.log('uploadStatus变化:', { old: oldVal, new: newVal })
}, { deep: true })

// 监听saving状态变化
watch(() => saving.value, (newVal, oldVal) => {
  console.log('saving状态变化:', { old: oldVal, new: newVal })
})

// 暴露给父组件使用
defineExpose({
  closeDialog: () => emit('update:visible', false),
  setSaving: (status) => {
    console.log('外部设置loading状态为:', status)
    saving.value = status
    emit('loading-change', status)
  }
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
