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
      <el-form-item label="现场照片">
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

const emit = defineEmits(['update:visible', 'submit', 'success'])

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
    { max: 50, message: '验收标题不能超过50字', trigger: 'blur' }
  ],
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

      // 2. 生成验收标题（检查验收-流程标题-次数次）
      // stage是编码（如DEMOLITION），需要转换为显示名称（如拆除工程）
      const stageDict = decoration_construction_stage.value.find(dict => dict.value === props.scheduleItem.stage)
      const stageName = stageDict?.label || props.scheduleItem.stage || '验收'
      const autoGeneratedTitle = `检查验收-${stageName}-${acceptanceCount + 1}次`

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

/** 提交验收 */
function handleSubmit() {
  proxy.$refs.acceptanceFormRef.validate(valid => {
    if (!valid) return

    if (!props.project || !props.scheduleItem) {
      proxy.$modal.msgError('数据错误')
      saving.value = false  // 重置loading状态
      return
    }

    // 使用组件提供的精确上传状态检查
    if (!uploadStatus.value.isAllUploaded) {
      proxy.$modal.msgWarning('请等待图片上传完成后再提交')
      saving.value = false  // 重置loading状态
      return
    }

    saving.value = true

    try {
      // 使用新组件的工具函数提取图片URL
      const imageUrls = uploadRef.value?.extractImageUrls(acceptanceForm.value.images) || []

      // 构造验收记录数据对象（与后端 ProjectScheduleRecords 实体类字段完全对应）
      const recordData = {
        projectId: props.project.id,                    // 项目ID
        scheduleId: props.scheduleItem.id,              // 进度ID
        recordType: 'ACCEPTANCE',                       // 记录类型：验收
        images: JSON.stringify(imageUrls),              // 现场照片（JSON格式）
        acceptanceTitle: acceptanceForm.value.title,    // 验收标题 新增字段
        acceptanceContent: acceptanceForm.value.content, // 验收内容
        acceptanceResult: acceptanceForm.value.result,   // 验收结果（QUALIFIED/UNQUALIFIED）
        acceptanceTime: acceptanceForm.value.acceptanceTime, // 验收时间
        acceptor: acceptanceForm.value.acceptor         // 验收人
      }

      emit('submit', recordData)
      // 注意：saving状态将在父组件成功/失败回调中重置
      // 如果父组件调用了 setSaving(false)，则可以注释掉下一行
      // saving.value = false  // 不要在这里重置，交给父组件处理
    } catch (error) {
      console.error('提交验收数据时出错:', error)
      proxy.$modal.msgError('数据处理失败，请重试')
      saving.value = false  // 异常情况下重置loading状态
    }
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
