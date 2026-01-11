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
          @click="handleAcceptanceSubmit"
          v-bind="getButtonProps(isEdit ? '更新验收' : '提交验收')"
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
import { useUploadManager, uploadPresets } from '@/composables/useUploadManager'
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
const { decoration_construction_stage, decoration_design_stage } = proxy.useDict('decoration_construction_stage', 'decoration_design_stage')

const saving = ref(false)

// 现场照片上传状态
const imagesFileList = ref([])

// 初始化上传管理Hook - 使用验收预设配置
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
} = useUploadManager(uploadPresets.acceptance)
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
          callback()
          /* 验收时间不能早于一周前
          if (selectedTime < oneWeekAgo) {
            callback(new Error('验收时间不能早于一周前'))
          }
          // 验收时间不能晚于一周后
          else if (selectedTime > oneWeekLater) {
            callback(new Error('验收时间不能晚于一周后'))
          } else {
            callback()
          }
          */
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
      // 根据阶段类型选择对应的字典
      const dictData = props.scheduleItem.stageType === 'DESIGN' 
        ? decoration_design_stage.value 
        : decoration_construction_stage.value
      const stageDict = dictData.find(dict => dict.value === props.scheduleItem.stage)
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
      // 重置图片上传状态
      imagesFileList.value = []
      // 重置上传管理器状态
      reset()
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
        images: images,
        result: editRecord.acceptanceResult || 'QUALIFIED',
        acceptanceTime: editRecord.acceptanceTime || '',
        acceptor: editRecord.acceptor || ''
      }

      // 处理编辑模式下的图片回显
      if (images.length > 0) {
        // 直接使用后端返回的路径，不拼接任何前缀
        // 后端返回格式：/profile/upload/xxx.jpg 或 http://...
        imagesFileList.value = images.map((url, index) => ({
          uid: `existing-${index}`,
          name: `image-${index}.jpg`,
          url: url,  // 直接使用，不拼接 VITE_APP_BASE_API
          status: 'success'
        }))
      }
      console.log('表单数据已预填充', acceptanceForm.value)
    } catch (error) {
      console.error('初始化编辑表单失败:', error)
      proxy.$modal.msgError('初始化表单失败，请重试')
    }
  }
}, { immediate: true })

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


/** 提交验收 - 集成Hook统一处理 */
async function handleAcceptanceSubmit() {
  // 使用统一的提交处理逻辑，但保留业务验证
  handleSubmit(async () => {
    // 表单验证
    const valid = await new Promise((resolve) => {
      proxy.$refs.acceptanceFormRef.validate((valid) => {
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

    if (!props.project || !props.scheduleItem) {
      throw new Error('数据错误')
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
      images: formatImagesData(),
      acceptanceTitle: acceptanceForm.value.title,
      acceptanceContent: acceptanceForm.value.content,
      acceptanceResult: acceptanceForm.value.result,
      acceptanceTime: acceptanceForm.value.acceptanceTime,
      acceptor: acceptanceForm.value.acceptor
    }

    // 编辑模式下添加ID字段
    if (props.isEdit && props.editRecord?.id) {
      recordData.id = props.editRecord.id
    }

    console.log('提交验收记录数据:', recordData)

    // 根据编辑模式调用不同的API
    const { addProjectScheduleRecords, updateProjectScheduleRecords } = await import('@/api/evs/projectScheduleRecords')

    let result
    if (props.isEdit) {
      // 编辑模式：调用更新API
      result = await updateProjectScheduleRecords(recordData)
      console.log('编辑模式：调用更新API')
    } else {
      // 新增模式：调用新增API
      result = await addProjectScheduleRecords(recordData)
      console.log('新增模式：调用新增API')
    }

    // API调用成功监控
    const apiEndTime = Date.now()
    const apiDuration = apiEndTime - apiStartTime
    console.log(`API调用成功，耗时: ${apiDuration}ms`)

    return result
  }).then((result) => {
    // 成功处理
    if (props.isEdit) {
      proxy.$modal.msgSuccess('验收记录更新成功')
    } else {
      proxy.$modal.msgSuccess('验收上报成功')
    }

    emit('success', result)

    // 重置loading状态
    saving.value = false
    emit('loading-change', false)

    // 关闭对话框
    emit('update:visible', false)

    console.log('验收提交成功，loading状态已重置，对话框已关闭')
  }).catch((error) => {
    // 统一错误处理（Hook已处理错误提示，这里只处理业务逻辑）
    console.error('提交验收数据时出错:', error)

    // 重置loading状态
    saving.value = false
    emit('loading-change', false)
    emit('error', error)
  })
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
