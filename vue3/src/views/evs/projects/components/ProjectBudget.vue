<template>
  <div class="project-budget-container">
    <el-space direction="vertical" :size="20" style="width: 100%;">
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
        :data="budgetItems"
        size="small"
        :show-header="true"
        empty-text="暂无预算项，请点击下方按钮添加"
        style="width: 100%"
      >
        <el-table-column prop="category" label="预算类别" width="25%" />
        <el-table-column prop="amount" label="预算金额" width="25%">
          <template #default="scope">
            <span style="color: #faad14; font-weight: bold;">
              ¥{{ scope.row.amount.toLocaleString() }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" width="35%">
          <template #default="scope">
            <span v-if="scope.row.description">{{ scope.row.description }}</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="15%">
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
                description="确定要���除这项预算吗？"
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
          <el-space direction="vertical" :size="16" style="width: 100%;">
            <el-space :size="16" style="width: 100%;">
              <el-form-item label="预算类别" style="flex: 1; margin-bottom: 0;">
                <el-select
                  v-model="budgetItemForm.category"
                  placeholder="选择预算类别"
                  size="large"
                  style="width: 100%"
                >
                  <el-option value="拆除工程" label="拆除工程" />
                  <el-option value="水电安装" label="水电安装" />
                  <el-option value="泥瓦工程" label="泥瓦工程" />
                  <el-option value="木工工程" label="木工工程" />
                  <el-option value="油漆工程" label="油漆工程" />
                  <el-option value="材料费" label="材料费" />
                  <el-option value="人工费" label="人工费" />
                  <el-option value="管理费" label="管理费" />
                  <el-option value="其他" label="其他" />
                </el-select>
              </el-form-item>

              <el-form-item label="预算金额" style="flex: 1; margin-bottom: 0;">
                <el-input-number
                  v-model="budgetItemForm.amount"
                  placeholder="输入预算金额"
                  size="large"
                  :min="0"
                  style="width: 100%"
                  :formatter="(value) => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                  :parser="(value) => value.replace(/¥\s?|(,*)/g, '')"
                />
              </el-form-item>
            </el-space>

            <el-form-item label="说明（可选）" style="margin-bottom: 0;">
              <el-input
                v-model="budgetItemForm.description"
                placeholder="输入预算说明"
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
import { ref, computed } from 'vue'

const { proxy } = getCurrentInstance()

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
const budgetItemForm = ref({
  category: '',
  amount: null,
  description: ''
})

// 计算预算总额
const totalBudgetAmount = computed(() => {
  return budgetItems.value.reduce((sum, item) => sum + (item.amount || 0), 0)
})

// 初始化数据
const initData = () => {
  budgetItems.value = props.project.budgetItems || []
  resetBudgetForm()
}

// 重置预算表单
function resetBudgetForm() {
  budgetItemForm.value = {
    category: '',
    amount: null,
    description: ''
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
function handleAddBudgetItem() {
  // 验证表单
  if (!budgetItemForm.value.category) {
    proxy.$modal.msgError("请选择预算类别")
    return
  }
  if (!budgetItemForm.value.amount || budgetItemForm.value.amount <= 0) {
    proxy.$modal.msgError("请输入有效的预算金��")
    return
  }

  const newItem = {
    id: Date.now().toString(), // 临时ID，后端会生成真实ID
    category: budgetItemForm.value.category,
    amount: budgetItemForm.value.amount,
    description: budgetItemForm.value.description
  }

  budgetItems.value.push(newItem)
  proxy.$modal.msgSuccess("预算项已添加")
  resetBudgetForm()
}

// 编辑预算项
function handleEditBudgetItem(item) {
  editingBudgetItem.value = item
  budgetItemForm.value = {
    category: item.category,
    amount: item.amount,
    description: item.description || ''
  }
  isAddingBudget.value = false
}

// 更新预算项
function handleUpdateBudgetItem() {
  if (!editingBudgetItem.value) return

  // 验证表单
  if (!budgetItemForm.value.category) {
    proxy.$modal.msgError("请选择预算类别")
    return
  }
  if (!budgetItemForm.value.amount || budgetItemForm.value.amount <= 0) {
    proxy.$modal.msgError("请输入有效的预算金额")
    return
  }

  const index = budgetItems.value.findIndex(item => item.id === editingBudgetItem.value.id)
  if (index !== -1) {
    budgetItems.value[index] = {
      ...editingBudgetItem.value,
      category: budgetItemForm.value.category,
      amount: budgetItemForm.value.amount,
      description: budgetItemForm.value.description
    }
    proxy.$modal.msgSuccess("预算项已更新")
    resetBudgetForm()
  }
}

// 删除预算项
function handleDeleteBudgetItem(itemId) {
  budgetItems.value = budgetItems.value.filter(item => item.id !== itemId)
  proxy.$modal.msgSuccess("预算项已删除")

  // 如果删除的是正在编辑的项，清除编辑状态
  if (editingBudgetItem.value?.id === itemId) {
    resetBudgetForm()
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
  resetBudgetForm,
  handleSaveBudgetItems
})

// 监听项目变化，初始化数据
watch(() => props.project, (newProject) => {
  if (newProject) {
    initData()
  }
}, { immediate: true, deep: true })
</script>

<style scoped>
.project-budget-container {
  width: 100%;
}
</style>
