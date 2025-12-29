<template>
  <div class="project-attachment-container">
    <el-space direction="vertical" :size="20" :fill="true" style="width: 100%;">
      <!-- 合同统计显示 -->
      <div style="background: #e6f7ff; border: 1px solid #91d5ff; border-radius: 8px; padding: 16px; text-align: center;">
        <div style="font-size: 14px; color: #999; margin-bottom: 8px;">合同总数</div>
        <div style="font-size: 32px; color: #1890ff; font-weight: bold;">{{ attachmentItems.length }}</div>
      </div>

      <!-- 合同列表表格 -->
      <el-table v-loading="loading" :data="attachmentItems" size="small" :show-header="true" empty-text="暂无合同，请点击下方按钮添加" style="width: 100%" table-layout="auto">
        <el-table-column prop="category" label="合同分类" min-width="120">
          <template #default="scope">
            <el-tag :type="getCategoryTagType(scope.row.category)" size="small">
              {{ getCategoryLabel(scope.row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contents" label="金额" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.contents">{{ formatAmount(scope.row.contents) }}</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="链接" min-width="150">
          <template #default="scope">
            <template v-if="getAttaUrlsArray(scope.row.attaUrls).length > 0">
              <el-space wrap>
                <el-link v-for="(url, idx) in getAttaUrlsArray(scope.row.attaUrls)" :key="idx" type="primary" :href="url" target="_blank" :underline="false">
                  <el-icon><Link /></el-icon> {{url}}
                </el-link>
              </el-space>
            </template>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.remarks">{{ scope.row.remarks }}</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-space size="small">
              <el-button type="primary" link size="small" @click="handleEditItem(scope.row)" :disabled="editingItem?.id === scope.row.id">编辑</el-button>
              <el-popconfirm title="确认删除" description="确定要删除这条合同吗？" @confirm="handleDeleteItem(scope.row.id)" confirm-button-text="确定" cancel-button-text="取消">
                <template #reference>
                  <el-button type="danger" link size="small" :disabled="editingItem?.id === scope.row.id">删除</el-button>
                </template>
              </el-popconfirm>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <!-- 添加/编辑表单 -->
      <div v-if="isAdding || editingItem" style="background: #f5f5f5; padding: 16px; border-radius: 8px;">
        <el-form :model="itemForm" label-position="top">
          <el-space direction="vertical" :size="16" :fill="true" style="width: 100%;">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="合同分类">
                  <el-select v-model="itemForm.category" placeholder="选择合同分类" size="large" style="width: 100%" :disabled="editingItem !== null">
                    <el-option v-for="dict in availableCategoryOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
                  </el-select>
                  <div v-if="editingItem" style="font-size: 12px; color: #909399; margin-top: 4px;">编辑模式下不可修改分类</div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="金额">
                  <el-input v-model="itemForm.contents" placeholder="输入金额" size="large" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="链接">
              <el-input v-model="attaUrlsText" type="textarea" :rows="3" placeholder="合同链接" size="large" />
            </el-form-item>

            <el-form-item label="备注（可选）">
              <el-input v-model="itemForm.remarks" placeholder="输入备注说明" size="large" />
            </el-form-item>

            <div style="margin-top: 16px;">
              <el-space>
                <el-button type="primary" @click="editingItem ? handleUpdateItem() : handleAddItem()">
                  {{ editingItem ? '确认修改' : '确认添加' }}
                </el-button>
                <el-button @click="handleCancelEdit">取消</el-button>
              </el-space>
            </div>
          </el-space>
        </el-form>
      </div>

      <!-- 添加按钮 -->
      <el-button v-else-if="!allCategoriesUsed" type="dashed" style="width: 100%" @click="handleStartAdd">
        <el-icon><Plus /></el-icon>
        添加合同
      </el-button>
      <div v-else style="text-align: center; color: #909399; font-size: 14px; padding: 12px;">
        所有合同分类已添加完成，如需修改请点击对应行的「编辑」按钮
      </div>
    </el-space>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useDict } from '@/utils/dict'
import { listProjectAttachment, addProjectAttachment, updateProjectAttachment, delProjectAttachment } from '@/api/evs/projectAttachment'

const { proxy } = getCurrentInstance()
const { decoration_project_attachment_htje } = useDict('decoration_project_attachment_htje')

// Props
const props = defineProps({
  project: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

// 响应式数据
const attachmentItems = ref([])
const isAdding = ref(false)
const editingItem = ref(null)
const loading = ref(false)
const attaUrlsText = ref('')

const itemForm = ref({
  projectId: props.project.id,
  category: '',
  contents: '',
  attaUrls: '',
  remarks: ''
})

// 分类统计
const categoryStats = computed(() => {
  if (attachmentItems.value.length === 0) return '暂无合同'
  const stats = {}
  attachmentItems.value.forEach(item => {
    const label = getCategoryLabel(item.category)
    stats[label] = (stats[label] || 0) + 1
  })
  return Object.entries(stats).map(([k, v]) => `${k}: ${v}`).join(' | ')
})

// 已使用的分类列表
const usedCategories = computed(() => {
  return attachmentItems.value.map(item => item.category)
})

// 可用的分类选项（新增时过滤已使用的，编辑时保留当前分类）
const availableCategoryOptions = computed(() => {
  if (!decoration_project_attachment_htje.value) return []
  
  // 编辑模式：保留当前编辑项的分类
  if (editingItem.value) {
    return decoration_project_attachment_htje.value.filter(dict => 
      dict.value === editingItem.value.category || !usedCategories.value.includes(dict.value)
    )
  }
  
  // 新增模式：过滤掉已使用的分类
  return decoration_project_attachment_htje.value.filter(dict => 
    !usedCategories.value.includes(dict.value)
  )
})

// 是否所有分类都已使用
const allCategoriesUsed = computed(() => {
  if (!decoration_project_attachment_htje.value) return false
  return decoration_project_attachment_htje.value.every(dict => 
    usedCategories.value.includes(dict.value)
  )
})

// 格式化金额（千分位）
function formatAmount(value) {
  if (!value) return '-'
  // 尝试提取数字部分
  const numStr = String(value).replace(/[^\d.-]/g, '')
  const num = parseFloat(numStr)
  if (isNaN(num)) return value
  // 格式化为千分位
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

// 获取分类标签
function getCategoryLabel(value) {
  if (!value) return '-'
  const dict = decoration_project_attachment_htje.value?.find(item => item.value === value)
  return dict?.label || value
}

// 获取分类标签类型
function getCategoryTagType(value) {
  if (!value) return ''
  const dict = decoration_project_attachment_htje.value?.find(item => item.value === value)
  return dict?.listClass || dict?.cssClass || ''
}

// 解析attaUrls为数组
function getAttaUrlsArray(attaUrls) {
  if (!attaUrls) return []
  try {
    const parsed = JSON.parse(attaUrls)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

// 加载合同数据
function loadAttachmentItems() {
  if (!props.project.id) {
    attachmentItems.value = []
    return
  }
  loading.value = true
  listProjectAttachment({ projectId: props.project.id }).then(response => {
    attachmentItems.value = response.rows || []
    loading.value = false
  }).catch(() => {
    loading.value = false
    attachmentItems.value = []
  })
}

// 重置表单
function resetForm() {
  itemForm.value = {
    projectId: props.project.id,
    category: '',
    contents: '',
    attaUrls: '',
    remarks: ''
  }
  attaUrlsText.value = ''
  isAdding.value = false
  editingItem.value = null
}

// 开始添加
function handleStartAdd() {
  resetForm()
  isAdding.value = true
}

// 取消编辑
function handleCancelEdit() {
  resetForm()
}

// 添加合同
async function handleAddItem() {
  if (!itemForm.value.category) {
    proxy.$modal.msgError("请选择合同分类")
    return
  }

  // 检查分类是否已存在
  if (usedCategories.value.includes(itemForm.value.category)) {
    proxy.$modal.msgError("该分类已存在，请选择其他分类或编辑已有记录")
    return
  }

  try {
    loading.value = true
    // 将文本转为JSON数组
    const urls = attaUrlsText.value.split('\n').map(s => s.trim()).filter(s => s)
    itemForm.value.attaUrls = urls.length > 0 ? JSON.stringify(urls) : ''
    
    await addProjectAttachment(itemForm.value)
    proxy.$modal.msgSuccess("合同已添加")
    resetForm()
    loadAttachmentItems()
  } catch (error) {
    proxy.$modal.msgError("添加失败，请重试")
  } finally {
    loading.value = false
  }
}

// 编辑合同
function handleEditItem(item) {
  editingItem.value = item
  itemForm.value = {
    projectId: item.projectId,
    category: item.category,
    contents: item.contents || '',
    attaUrls: item.attaUrls || '',
    remarks: item.remarks || ''
  }
  // 将JSON数组转为文本
  const urls = getAttaUrlsArray(item.attaUrls)
  attaUrlsText.value = urls.join('\n')
  isAdding.value = false
}

// 更新合同
async function handleUpdateItem() {
  if (!editingItem.value) return
  if (!itemForm.value.category) {
    proxy.$modal.msgError("请选择合同分类")
    return
  }

  try {
    loading.value = true
    const urls = attaUrlsText.value.split('\n').map(s => s.trim()).filter(s => s)
    itemForm.value.attaUrls = urls.length > 0 ? JSON.stringify(urls) : ''
    
    await updateProjectAttachment({
      id: editingItem.value.id,
      ...itemForm.value
    })
    proxy.$modal.msgSuccess("合同已更新")
    resetForm()
    loadAttachmentItems()
  } catch (error) {
    proxy.$modal.msgError("更新失败，请重试")
  } finally {
    loading.value = false
  }
}

// 删除合同
async function handleDeleteItem(itemId) {
  try {
    loading.value = true
    await delProjectAttachment(itemId)
    proxy.$modal.msgSuccess("合同已删除")
    loadAttachmentItems()
    if (editingItem.value?.id === itemId) {
      resetForm()
    }
  } catch (error) {
    proxy.$modal.msgError("删除失败，请重试")
  } finally {
    loading.value = false
  }
}

// 暴露方法给父组件
defineExpose({
  attachmentItems,
  loadAttachmentItems,
  resetForm,
  handleStartAdd
})

// 监听项目变化
watch(() => props.project, (newProject) => {
  if (newProject && newProject.id) {
    loadAttachmentItems()
  }
}, { immediate: true, deep: true })
</script>

<style scoped>
.project-attachment-container {
  width: 100%;
}

.project-attachment-container :deep(.el-space__item) {
  width: 100%;
}

.project-attachment-container :deep(.el-table) {
  width: 100% !important;
}
</style>
