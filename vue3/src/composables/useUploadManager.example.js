/**
 * useUploadManager Hook 使用示例
 * 展示如何在实际组件中使用统一的图片上传管理Hook
 */

// ==================== 示例1：整改提交对话框 ====================
/**
<script setup>
// 1. 导入Hook和预设配置
import { useUploadManager, uploadPresets } from '@/composables/useUploadManager'
import { addQualityFixes } from '@/api/evs/qualityFixes'

// 2. 使用Hook，传入预设配置
const uploadManager = useUploadManager(uploadPresets.fix)

// 3. 解构需要的状态和方法
const {
  uploadStatus,
  submitting,
  error,
  uploadRef,
  isSubmitDisabled,
  handleSubmit,
  extractImageUrls,
  getUploadProps,
  getButtonProps,
  getStatusTip
} = uploadManager

// 4. 表单数据
const form = ref({
  fixDescription: '',
  images: [],
  status: 'IN_PROGRESS',
  fixedAt: ''
})

// 5. 表单验证函数
const validateForm = () => {
  if (!form.value.fixDescription) {
    ElMessage.error('请输入修复描述')
    return false
  }
  return true
}

// 6. 提交函数
async function handleSubmitFix() {
  await handleSubmit(async () => {
    // 提取图片URL
    const imageUrls = extractImageUrls(form.value.images)

    // 构造提交数据
    const submitData = {
      qualityIssuesId: props.issue.id,
      fixDescription: form.value.fixDescription,
      images: imageUrls.length > 0 ? JSON.stringify(imageUrls) : '[]',
      status: form.value.status,
      fixedAt: form.value.status === 'RESOLVED' ? form.value.fixedAt : null
    }

    // 调用API
    return await addQualityFixes(submitData)
  }, validateForm)
}
</script>

<template>
  <el-dialog v-model="visible" title="提交整改" width="600px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="修复描述" prop="fixDescription" required>
        <el-input
          v-model="form.fixDescription"
          type="textarea"
          :rows="4"
          placeholder="请详细描述修复措施和方法"
        />
      </el-form-item>

      <el-form-item label="修复照片" prop="images">
        <!-- 使用Hook提供的配置 -->
        <ImageUploadCard v-bind="getUploadProps()" v-model="form.images" />

        <!-- 使用Hook提供的状态提示 -->
        <div v-if="getStatusTip().show" class="upload-status-tip">
          <el-tag :type="getStatusTip().type">
            {{ getStatusTip().message }}
          </el-tag>
        </div>
      </el-form-item>

      <el-form-item label="修复状态" prop="status" required>
        <el-radio-group v-model="form.status">
          <el-radio label="IN_PROGRESS">整改中</el-radio>
          <el-radio label="RESOLVED">已完成</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">取消</el-button>
        <!-- 使用Hook提供的按钮配置 -->
        <el-button
          type="primary"
          v-bind="getButtonProps('提交整改')"
          @click="handleSubmitFix"
        >
          提交整改
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.upload-status-tip {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
</style>
*/

// ==================== 示例2：问题上报页面 ====================
/**
<script setup>
// 1. 导入Hook和预设配置
import { useUploadManager, uploadPresets } from '@/composables/useUploadManager'
import { addQualityIssues } from '@/api/evs/qualityIssues'

// 2. 使用Hook（自定义配置）
const uploadManager = useUploadManager({
  ...uploadPresets.issue,
  onSuccess: ({ response, file, imageUrl }) => {
    console.log('问题图片上传成功:', imageUrl)
  },
  onError: ({ message }) => {
    console.error('问题图片上传失败:', message)
  }
})

const {
  uploadStatus,
  submitting,
  handleSubmit,
  extractImageUrls,
  getUploadProps,
  getButtonProps
} = uploadManager

// 3. 表单数据和验证
const form = ref({
  title: '',
  description: '',
  category: 'GENERAL',
  images: []
})

const validateForm = () => {
  if (!form.value.title) return false
  if (!form.value.description) return false
  return true
}

// 4. 提交
async function handleSubmitIssue() {
  await handleSubmit(async () => {
    const imageUrls = extractImageUrls(form.value.images)
    const submitData = {
      ...form.value,
      images: imageUrls.length > 0 ? JSON.stringify(imageUrls) : '[]'
    }
    return await addQualityIssues(submitData)
  }, validateForm)
}
</script>

<template>
  <el-form :model="form" label-width="100px">
    <el-form-item label="问题标题" prop="title" required>
      <el-input v-model="form.title" placeholder="请输入问题标题" />
    </el-form-item>

    <el-form-item label="问题描述" prop="description" required>
      <el-input
        v-model="form.description"
        type="textarea"
        :rows="4"
        placeholder="请详细描述问题"
      />
    </el-form-item>

    <el-form-item label="现场照片" prop="images">
      <ImageUploadCard v-bind="getUploadProps()" v-model="form.images" />
    </el-form-item>

    <el-form-item>
      <el-button
        type="primary"
        v-bind="getButtonProps('提交问题')"
        @click="handleSubmitIssue"
      >
        提交问题
      </el-button>
    </el-form-item>
  </el-form>
</template>
*/

// ==================== 示例3：验收上报对话框 ====================
/**
<script setup>
// 1. 导入Hook和预设配置
import { useUploadManager, uploadPresets } from '@/composables/useUploadManager'
import { addProjectScheduleRecords } from '@/api/evs/projectScheduleRecords'

// 2. 使用Hook（使用验收预设配置）
const uploadManager = useUploadManager(uploadPresets.acceptance)

const {
  uploadStatus,
  handleSubmit,
  extractImageUrls,
  getUploadProps,
  getButtonProps,
  getStatusTip
} = uploadManager

// 3. 表单数据
const acceptanceForm = ref({
  title: '',
  content: '',
  images: [],
  result: 'QUALIFIED',
  acceptanceTime: '',
  acceptor: ''
})

// 4. 提交
async function handleSubmitAcceptance() {
  await handleSubmit(async () => {
    const imageUrls = extractImageUrls(acceptanceForm.value.images)
    const submitData = {
      projectId: props.project.id,
      scheduleId: props.scheduleItem.id,
      recordType: 'ACCEPTANCE',
      acceptanceTitle: acceptanceForm.value.title,
      acceptanceContent: acceptanceForm.value.content,
      images: JSON.stringify(imageUrls),
      acceptanceResult: acceptanceForm.value.result,
      acceptanceTime: acceptanceForm.value.acceptanceTime,
      acceptor: acceptanceForm.value.acceptor
    }
    return await addProjectScheduleRecords(submitData)
  })
}
</script>

<template>
  <el-dialog v-model="visible" title="验收上报" width="600px">
    <el-form :model="acceptanceForm" label-width="100px">
      <el-form-item label="验收内容" prop="content" required>
        <el-input
          v-model="acceptanceForm.content"
          type="textarea"
          :rows="6"
          placeholder="请描述验收情况"
        />
      </el-form-item>

      <el-form-item label="现场照片" prop="images">
        <ImageUploadCard v-bind="getUploadProps()" v-model="acceptanceForm.images" />

        <!-- 状态提示 -->
        <div v-if="getStatusTip().show" class="upload-status-tip">
          <el-tag :type="getStatusTip().type">
            {{ getStatusTip().message }}
          </el-tag>
          <span v-if="!uploadStatus.isAllUploaded" class="upload-hint">
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
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button
        type="primary"
        v-bind="getButtonProps('提交验收')"
        @click="handleSubmitAcceptance"
      >
        提交验收
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.upload-status-tip {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;

  .upload-hint {
    color: #e6a23c;
    font-size: 12px;
  }
}
</style>
*/

// ==================== 示例4：完全自定义配置 ====================
/**
<script setup>
// 如果需要完全自定义配置
const uploadManager = useUploadManager({
  maxCount: 5,           // 最多5张
  maxSize: 3,            // 每张最大3MB
  compress: true,        // 启用压缩
  compressQuality: 0.9,  // 压缩质量90%
  compressMaxSize: 1.5,  // 压缩后最大1.5MB
  onSuccess: (data) => {
    console.log('自定义成功处理:', data)
  },
  onError: (data) => {
    console.log('自定义错误处理:', data)
  }
})

const {
  uploadStatus,
  submitting,
  handleSubmit,
  extractImageUrls,
  getUploadProps,
  getButtonProps
} = uploadManager
</script>

<template>
  <div>
    <!-- 使用自定义配置的上传组件 -->
    <ImageUploadCard v-bind="getUploadProps()" v-model="images" />

    <!-- 按钮会自动根据上传状态禁用 -->
    <el-button
      type="primary"
      v-bind="getButtonProps('自定义提交')"
      @click="handleSubmit(customSubmitFunction)"
    >
      自定义提交
    </el-button>
  </div>
</template>
*/

// ==================== 关键优势对比 ====================

/**
 * 使用前（各组件重复的代码）：
 *
 * // 每个组件都需要重复定义：
 * const uploadStatus = ref({ isAllUploaded: true, totalFiles: 0, uploadedFiles: 0 })
 * const submitting = ref(false)
 *
 * // 每个组件都需要重复实现：
 * function handleUploadStatusChange(status) {
 *   uploadStatus.value = status
 * }
 *
 * function handleUploadSuccess({ response, file }) {
 *   console.log('图片上传成功:', file)
 * }
 *
 * function handleUploadError({ message }) {
 *   ElMessage.error('图片上传失败: ' + message)
 * }
 *
 * async function submitForm() {
 *   if (!uploadStatus.value.isAllUploaded) {
 *     ElMessage.warning('请等待图片上传完成后再提交')
 *     return
 *   }
 *   submitting.value = true
 *   try {
 *     const imageUrls = uploadRef.value?.extractImageUrls(form.value.images) || []
 *     await submitApi({ ...form.value, images: JSON.stringify(imageUrls) })
 *     ElMessage.success('提交成功')
 *   } catch (error) {
 *     ElMessage.error('提交失败')
 *   } finally {
 *     submitting.value = false
 *   }
 * }
 *
 * <el-button
 *   type="primary"
 *   :disabled="!uploadStatus.isAllUploaded || submitting"
 *   :loading="submitting"
 *   @click="submitForm"
 * >
 *   提交
 * </el-button>
 */

/**
 * 使用后（统一Hook）：
 *
 * // 一行代码初始化所有状态和逻辑
 * const uploadManager = useUploadManager(uploadPresets.fix)
 *
 * // 直接使用Hook提供的方法
 * const { handleSubmit, extractImageUrls, getButtonProps, getUploadProps } = uploadManager
 *
 * async function submitForm() {
 *   await handleSubmit(async () => {
 *     const imageUrls = extractImageUrls(form.value.images)
 *     return await submitApi({ ...form.value, images: JSON.stringify(imageUrls) })
 *   })
 * }
 *
 * // 模板中直接使用配置好的属性
 * <el-button type="primary" v-bind="getButtonProps('提交')" @click="submitForm">
 *   提交
 * </el-button>
 */

/**
 * 优势总结：
 * 1. 消除重复代码 - 所有组件共享相同的逻辑
 * 2. 统一交互体验 - 所有模块使用相同的状态管理和UI反馈
 * 3. 易于维护 - 修改Hook即可影响所有使用的地方
 * 4. 配置灵活 - 支持预设配置和自定义配置
 * 5. 类型安全 - 完整的TypeScript类型定义
 * 6. 错误处理 - 统一的错误处理和用户提示
 * 7. 状态管理 - 自动管理上传状态和提交状态
 */
