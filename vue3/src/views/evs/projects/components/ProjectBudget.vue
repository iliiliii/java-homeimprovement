<template>
  <div class="project-budget-container">
    <el-space direction="vertical" :size="20" :fill="true" style="width: 100%;">
      <!-- 预算总额显示 -->
      <div
        style="
          background: #fff7e6;
          border: 1px solid #ffd591;
          border-radius: 8px;
          padding: 16px;
          text-align: center;
        "
      >
        <div style="font-size: 14px; color: #999; margin-bottom: 8px;">预算总额</div>
        <div style="font-size: 32px; color: #faad14; font-weight: bold;">
          ¥{{ totalBudgetAmount.toLocaleString() }}
        </div>
        <div style="font-size: 12px; color: #999; margin-top: 8px;">
          {{ budgetItems.length }} 个预算项
        </div>
      </div>

      <!-- 预算列表表格 -->
      <el-table
        v-loading="loading"
        :data="budgetItems"
        size="small"
        :show-header="true"
        empty-text="暂无预算项，请点击下方按钮添加"
        style="width: 100%"
        table-layout="auto"
      >
        <el-table-column prop="category" label="预算类别" min-width="200">
          <template #default="scope">
            <span>{{ getCategoryLabel(scope.row.category) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="计划金额" min-width="150">
          <template #default="scope">
            <span style="color: #faad14; font-weight: bold;">
              ¥{{ scope.row.plannedAmount?.toLocaleString() || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.remarks">{{ scope.row.remarks }}</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-space size="small">
              <el-button
                type="primary"
                link
                size="small"
                @click="handleEditBudgetItem(scope.row)"
                :disabled="editingBudgetItem?.id === scope.row.id"
              >
                编辑
              </el-button>
              <el-popconfirm
                title="确认删除"
                description="确定要删除这项预算吗？"
                @confirm="handleDeleteBudgetItem(scope.row.id)"
                confirm-button-text="确定"
                cancel-button-text="取消"
              >
                <template #reference>
                  <el-button
                    type="danger"
                    link
                    size="small"
                    :disabled="editingBudgetItem?.id === scope.row.id"
                  >
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <!-- 添加/编辑预算表单 -->
      <div
        v-if="isAddingBudget || editingBudgetItem"
        style="
          background: #f5f5f5;
          padding: 16px;
          border-radius: 8px;
        "
      >
        <el-form :model="budgetItemForm" label-position="top">
          <el-space direction="vertical" :size="16" :fill="true" style="width: 100%;">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="预算类别">
                  <el-select
                    v-model="budgetItemForm.category"
                    placeholder="选择预算类别"
                    size="large"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="dict in decoration_project_budget"
                      :key="dict.value"
                      :label="dict.label"
                      :value="dict.value"
                    />
                    <el-option
                      v-if="decoration_project_budget.length === 0"
                      disabled
                      value=""
                      label="暂无可用预算类别"
                    />
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="计划金额">
                  <el-input-number
                    v-model="budgetItemForm.plannedAmount"
                    placeholder="输入计划金额"
                    size="large"
                    :min="0"
                    style="width: 100%"
                    :formatter="(value) => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                    :parser="(value) => value.replace(/¥\s?|(,*)/g, '')"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="备注（可选）">
              <el-input
                v-model="budgetItemForm.remarks"
                placeholder="输入备注说明"
                size="large"
              />
            </el-form-item>

            <div style="margin-top: 16px;">
              <el-space>
                <el-button
                  type="primary"
                  @click="editingBudgetItem ? handleUpdateBudgetItem() : handleAddBudgetItem()"
                >
                  {{ editingBudgetItem ? '确认修改' : '确认添加' }}
                </el-button>
                <el-button @click="handleCancelBudgetEdit">取消</el-button>
              </el-space>
            </div>
          </el-space>
        </el-form>
      </div>

      <!-- 添加预算项按钮 -->
      <el-button
        v-else
        type="dashed"
        style="width: 100%"
        @click="handleStartAddBudget"
      >
        <el-icon><Plus /></el-icon>
        添加预算项
      </el-button>
    </el-space>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useDict } from '@/utils/dict'
import { listProjectBudgets, addProjectBudgets, updateProjectBudgets, delProjectBudgets } from '@/api/evs/projectBudgets'

const { proxy } = getCurrentInstance()
const { decoration_project_budget } = useDict('decoration_project_budget')

// Props
const props = defineProps({
  project: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

// Emits
const emit = defineEmits(['save'])

// 响应式数据
const budgetItems = ref([]) // 预算条目数组
const isAddingBudget = ref(false)
const editingBudgetItem = ref(null)
const loading = ref(false) // 新增：加载状态
const budgetItemForm = ref({
  projectId: props.project.id,
  category: '',
  plannedAmount: null,
  remarks: ''
})

// 计算预算总额（使用计划金额）
const totalBudgetAmount = computed(() => {
  return budgetItems.value.reduce((sum, item) => sum + (item.plannedAmount || 0), 0)
})

// 根据字典值获取类别标签
function getCategoryLabel(categoryValue) {
  if (!categoryValue) return '-'
  const dict = decoration_project_budget.value?.find(item => item.value === categoryValue)
  return dict?.label || categoryValue
}

// 从API加载预算数据
function loadBudgetItems() {
  if (!props.project.id) {
    budgetItems.value = []
    return
  }

  loading.value = true
  listProjectBudgets({ projectId: props.project.id }).then(response => {
    budgetItems.value = response.rows || []
    loading.value = false
  }).catch(() => {
    loading.value = false
    budgetItems.value = []
  })
}

// 重置预算表单
function resetBudgetForm() {
  budgetItemForm.value = {
    projectId: props.project.id,
    category: '',
    plannedAmount: null,
    remarks: ''
  }
  isAddingBudget.value = false
  editingBudgetItem.value = null
}

// 开始添加预算项
function handleStartAddBudget() {
  resetBudgetForm()
  isAddingBudget.value = true
}

// 取消预算编辑
function handleCancelBudgetEdit() {
  resetBudgetForm()
}

// 添加预算项
async function handleAddBudgetItem() {
  // 验证表单
  if (!budgetItemForm.value.category) {
    proxy.$modal.msgError("请选择预算类别")
    return
  }
  if (!budgetItemForm.value.plannedAmount || budgetItemForm.value.plannedAmount <= 0) {
    proxy.$modal.msgError("请输入有效的计划金额")
    return
  }

  try {
    loading.value = true
    await addProjectBudgets(budgetItemForm.value)
    proxy.$modal.msgSuccess("预算项已添加")
    resetBudgetForm()
    loadBudgetItems() // 重新加载数据
  } catch (error) {
    proxy.$modal.msgError("添加失败，请重试")
  } finally {
    loading.value = false
  }
}

// 编辑预算项
function handleEditBudgetItem(item) {
  editingBudgetItem.value = item
  budgetItemForm.value = {
    projectId: item.projectId,
    category: item.category,
    plannedAmount: item.plannedAmount,
    remarks: item.remarks || ''
  }
  isAddingBudget.value = false
}

// 更新预算项
async function handleUpdateBudgetItem() {
  if (!editingBudgetItem.value) return

  // 验证表单
  if (!budgetItemForm.value.category) {
    proxy.$modal.msgError("请选择预算类别")
    return
  }
  if (!budgetItemForm.value.plannedAmount || budgetItemForm.value.plannedAmount <= 0) {
    proxy.$modal.msgError("请输入有效的计划金额")
    return
  }

  try {
    loading.value = true
    await updateProjectBudgets({
      id: editingBudgetItem.value.id,
      ...budgetItemForm.value
    })
    proxy.$modal.msgSuccess("预算项已更新")
    resetBudgetForm()
    loadBudgetItems() // 重新加载数据
  } catch (error) {
    proxy.$modal.msgError("更新失败，请重试")
  } finally {
    loading.value = false
  }
}

// 删除预算项
async function handleDeleteBudgetItem(itemId) {
  try {
    loading.value = true
    await delProjectBudgets(itemId)
    proxy.$modal.msgSuccess("预算项已删除")
    loadBudgetItems() // 重新加载数据

    // 如果删除的是正在编辑的项，清除编辑状态
    if (editingBudgetItem.value?.id === itemId) {
      resetBudgetForm()
    }
  } catch (error) {
    proxy.$modal.msgError("删除失败，请重试")
  } finally {
    loading.value = false
  }
}

// 保存预算条目
function handleSaveBudgetItems() {
  if (budgetItems.value.length === 0) {
    proxy.$modal.msgWarning("请至少添加一项预算")
    return
  }

  // 计算总预算
  const totalBudget = totalBudgetAmount.value

  // 构建更新数据
  const updateData = {
    id: props.project.id,
    name: props.project.name,
    budget: totalBudget,
    budgetItems: budgetItems.value
  }

  // 触发保存事件，通知父组件
  emit('save', updateData)
}

// 暴露方法给父组件
defineExpose({
  budgetItems,
  totalBudgetAmount,
  loadBudgetItems,
  resetBudgetForm,
  handleSaveBudgetItems,
  handleStartAddBudget
})

// 监听项目变化，初始化数据
watch(() => props.project, (newProject) => {
  if (newProject && newProject.id) {
    loadBudgetItems()
  }
}, { immediate: true, deep: true })
</script>

<style scoped>
.project-budget-container {
  width: 100%;
}

/* 确保 el-space 的子元素撑满宽度 */
.project-budget-container :deep(.el-space__item) {
  width: 100%;
}

/* 确保表格撑满容器宽度 */
.project-budget-container :deep(.el-table) {
  width: 100% !important;
}

.project-budget-container :deep(.el-table__body-wrapper) {
  width: 100%;
}

.project-budget-container :deep(.el-table__header-wrapper) {
  width: 100%;
}
</style>
