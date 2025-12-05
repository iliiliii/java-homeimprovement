<template>
  <!-- 使用公共的问题详情抽屉组件 -->
  <QualityIssueDetailDrawer
    v-model:visible="drawerVisible"
    :issue="issue"
    :title="drawerTitle"
    @submit-fix="handleAddFix"
  />

  <!-- 整改提交对话框 -->
  <FixSubmissionDialog
    v-model:visible="fixDialogVisible"
    :issue="issue"
    :fix-data="currentEditingFix"
    :upload-url="uploadUrl"
    :edit-mode="!!currentEditingFix"
    @success="handleFixSuccess"
    @error="handleFixError"
    @refresh="refreshDrawer"
  />
</template>

<script setup name="QualityFixesDrawer">
import { ref, computed, watch } from 'vue'
import { getCurrentInstance } from 'vue'
import QualityIssueDetailDrawer from '@/components/QualityIssueDetailDrawer/index.vue'
import FixSubmissionDialog from './FixSubmissionDialog.vue'

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
  viewMode: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'fix-success', 'fix-error'])

const { proxy } = getCurrentInstance()

// 计算抽屉标题
const drawerTitle = computed(() => {
  return props.viewMode ? '详情查看' : '整改历史记录'
})

// 响应式数据
const drawerVisible = ref(false)
const fixDialogVisible = ref(false)
const currentEditingFix = ref(null)
const drawerRef = ref(null)

// 监听属性变化
watch(() => props.visible, (newVal) => {
  drawerVisible.value = newVal
})

watch(drawerVisible, (newVal) => {
  emit('update:visible', newVal)
})

// 操作函数
function handleAddFix(issue) {
  if (!issue) return
  currentEditingFix.value = null
  fixDialogVisible.value = true
}

function handleFixSuccess(fixData) {
  console.log('🔍 [FIXES DRAWER] 整改成功:', fixData)
  proxy.$modal.msgSuccess('整改提交成功')
  fixDialogVisible.value = false
  refreshDrawer()
  emit('fix-success', fixData)
}

function handleFixError(error) {
  console.error('整改提交失败:', error)
  proxy.$modal.msgError('整改提交失败：' + (error.msg || error.message))
  emit('fix-error', error)
}

// 刷新抽屉数据
function refreshDrawer() {
  if (drawerRef.value && drawerRef.value.loadIssueFixes) {
    drawerRef.value.loadIssueFixes()
  }
}
</script>

<style scoped lang="scss">
// 样式已移至公共组件
</style>