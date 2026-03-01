<template>
  <!-- 添加或修改项目信息对话框 -->
  <el-dialog :title="title" v-model="open" width="600px" append-to-body>
    <el-form ref="projectsRef" :model="form" :rules="rules" label-width="100px">
      <!-- 核心字段 - 新建和编辑都显示 -->
      <el-form-item label="项目名称" prop="name">
        <el-input v-model="form.name" placeholder="例如: 张先生家装修工程" />
      </el-form-item>

      <el-form-item label="客户" prop="customerIds">
        <el-select
          v-model="form.customerIds"
          placeholder="请选择客户（可多选，最多10个）"
          clearable
          filterable
          multiple
          remote
          :remote-method="searchCustomers"
          :loading="loading"
          :multiple-limit="10"
          style="width: 100%"
        >
          <el-option
            v-for="customer in customersList"
            :key="customer.id"
            :label="`${customer.name} (${customer.phone})`"
            :value="customer.id"
            :disabled="isEdit && customer.id === form.primaryCustomerId"
          >
            <span style="float: left">{{ customer.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">
              {{ customer.id === form.primaryCustomerId ? '主客户' : customer.phone }}
            </span>
          </el-option>
        </el-select>
        <div style="margin-top: 5px; font-size: 12px; color: #909399">
          <span v-if="!isEdit">提示：第一个选择的客户将作为主客户</span>
          <span v-else style="color: #E6A23C">
            <i class="el-icon-warning"></i> 主客户不能直接删除，请先切换主客户后再删除
          </span>
        </div>
      </el-form-item>
      
      <!-- 编辑模式下显示主客户选择 -->
      <el-form-item v-if="isEdit && form.customerIds && form.customerIds.length > 1" label="主客户" prop="primaryCustomerId">
        <el-select
          v-model="form.primaryCustomerId"
          placeholder="请选择主客户"
          style="width: 100%"
        >
          <el-option
            v-for="customerId in form.customerIds"
            :key="customerId"
            :label="getCustomerName(customerId)"
            :value="customerId"
          />
        </el-select>
        <div style="margin-top: 5px; font-size: 12px; color: #909399">
          主客户将显示在项目列表中
        </div>
      </el-form-item>

      <el-form-item label="工地地址" prop="address">
        <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入详细地址" />
      </el-form-item>

      <el-form-item label="工地面积" prop="area">
        <el-input v-model="form.area" placeholder="请输入工地面积">
          <template #append>㎡</template>
        </el-input>
      </el-form-item>

      <el-form-item label="施工周期" prop="dateRange">
        <el-date-picker
          v-model="form.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="项目描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请简要描述项目需求和特点" />
      </el-form-item>

      <!-- 编辑模式专属字段 -->
      <template v-if="isEdit">

        <el-form-item label="入住日期" prop="actualEndDate">
          <el-date-picker
            clearable
            v-model="form.actualEndDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择入住日期"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="项目状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择项目状态" style="width: 100%">
            <el-option
              v-for="dict in decoration_project_status"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="submitForm" :loading="submitLoading" :disabled="submitLoading">
          {{ submitLoading ? '提交中...' : '确 定' }}
        </el-button>
        <el-button @click="cancel" :disabled="submitLoading">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { listCustomers } from '@/api/evs/customers'
import { getProjects, addProjects, updateProjects } from '@/api/evs/projects'
import { getProjectCustomersList, addCustomersToProject, removeCustomerFromProject, setPrimaryCustomer } from '@/api/evs/projectCustomers'

const { proxy } = getCurrentInstance()
const { decoration_project_status } = proxy.useDict('decoration_project_status')

const props = defineProps({
  // 无需接收 props，对外暴露方法
})

const emit = defineEmits(['success'])

// 响应式数据
const open = ref(false)
const loading = ref(false)
const submitLoading = ref(false)  // 新增：提交按钮加载状态
const title = ref("")
const isEdit = computed(() => form.value.id != null)
const originalPrimaryCustomerId = ref(null)  // 新增：保存原始主客户ID

const form = ref({
  id: null,
  name: null,
  customerId: null,  // 保留用于兼容性
  customerIds: [],   // 新增：多客户选择
  primaryCustomerId: null,  // 新增：主客户ID
  projectCode: null,
  description: null,
  address: null,
  area: null,
  budget: null,
  actualCost: null,
  startDate: null,
  endDate: null,
  actualEndDate: null,
  status: null,
  priority: null,
  progress: null,
  isActive: null,
  createdAt: null,
  updatedAt: null,
  deletedAt: null,
  createdBy: null,
  updatedBy: null,
  deletedBy: null,
  dateRange: null
})

const customersList = ref([])
const customerMap = ref(new Map())
let searchTimer = null  // 搜索防抖定时器

const rules = {
  name: [
    { required: true, message: "项目名称不能为空", trigger: "blur" }
  ],
  customerIds: [
    { required: true, message: "请至少选择一个客户", trigger: "change", type: 'array', min: 1 }
  ],
  address: [
    { required: true, message: "项目地址不能为空", trigger: "blur" }
  ],
  area: [
    { required: true, message: "房屋面积不能为空", trigger: "blur" }
  ],
}

/** 取消按钮 */
function cancel() {
  open.value = false
  submitLoading.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: null,
    name: null,
    customerId: null,
    customerIds: [],
    primaryCustomerId: null,
    projectCode: null,
    description: null,
    address: null,
    area: null,
    budget: null,
    actualCost: null,
    startDate: null,
    endDate: null,
    actualEndDate: null,
    status: null,
    priority: null,
    progress: null,
    isActive: null,
    createdAt: null,
    updatedAt: null,
    deletedAt: null,
    createdBy: null,
    updatedBy: null,
    deletedBy: null,
    dateRange: null
  }
  submitLoading.value = false
  originalPrimaryCustomerId.value = null
  proxy.resetForm("projectsRef")
}

/** 打开新增对话框 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加项目信息"
}

/** 打开编辑对话框 */
function handleEdit(row) {
  reset()
  const _id = row.id
  // 获取最新数据
  getProjects(_id).then(response => {
    // 不要直接赋值，而是逐个字段赋值，保留 customerIds 和 primaryCustomerId
    const projectData = response.data
    form.value.id = projectData.id
    form.value.name = projectData.name
    form.value.customerId = projectData.customerId
    form.value.projectCode = projectData.projectCode
    form.value.description = projectData.description
    form.value.address = projectData.address
    form.value.area = projectData.area
    form.value.budget = projectData.budget
    form.value.actualCost = projectData.actualCost
    form.value.startDate = projectData.startDate
    form.value.endDate = projectData.endDate
    form.value.actualEndDate = projectData.actualEndDate
    form.value.status = projectData.status
    form.value.priority = projectData.priority
    form.value.progress = projectData.progress
    form.value.isActive = projectData.isActive
    form.value.createdAt = projectData.createdAt
    form.value.updatedAt = projectData.updatedAt
    form.value.createdBy = projectData.createdBy
    form.value.updatedBy = projectData.updatedBy
    
    // 处理日期范围字段
    if (form.value.startDate && form.value.endDate) {
      form.value.dateRange = [form.value.startDate, form.value.endDate]
    }
    
    // 获取项目的所有客户
    getProjectCustomersList(_id).then(res => {
      const projectCustomers = res.data || []
      console.log('获取到的项目客户列表:', projectCustomers)
      
      // 提取客户ID列表
      form.value.customerIds = projectCustomers.map(pc => pc.customerId)
      console.log('提取的客户ID列表:', form.value.customerIds)
      
      // 找到主客户
      const primary = projectCustomers.find(pc => pc.isPrimary)
      if (primary) {
        form.value.primaryCustomerId = primary.customerId
        originalPrimaryCustomerId.value = primary.customerId  // 保存原始主客户ID
        console.log('主客户ID:', form.value.primaryCustomerId)
      } else if (form.value.customerId) {
        // 兼容旧数据：如果没有主客户但有 customerId，使用它
        form.value.primaryCustomerId = form.value.customerId
        originalPrimaryCustomerId.value = form.value.customerId  // 保存原始主客户ID
        if (!form.value.customerIds.includes(form.value.customerId)) {
          form.value.customerIds.unshift(form.value.customerId)
        }
        console.log('使用兼容模式，主客户ID:', form.value.primaryCustomerId)
      }
      
      console.log('最终 customerIds:', form.value.customerIds)
      console.log('最终 primaryCustomerId:', form.value.primaryCustomerId)
      console.log('原始 primaryCustomerId:', originalPrimaryCustomerId.value)
    }).catch(err => {
      console.error('获取项目客户列表失败:', err)
      // 如果获取失败，使用旧的 customerId 字段
      if (form.value.customerId) {
        form.value.customerIds = [form.value.customerId]
        form.value.primaryCustomerId = form.value.customerId
        originalPrimaryCustomerId.value = form.value.customerId
      }
    })
    
    open.value = true
    title.value = "修改项目信息"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["projectsRef"].validate(valid => {
    if (valid) {
      // 编辑模式下，提交前验证客户关联
      if (form.value.id != null) {
        const newCustomerIds = form.value.customerIds || []
        const newPrimaryId = form.value.primaryCustomerId
        
        // 检查是否至少有一个客户
        if (newCustomerIds.length === 0) {
          proxy.$modal.msgError("项目至少需要一个客户")
          return
        }
        
        // 检查是否尝试删除主客户
        if (originalPrimaryCustomerId.value && 
            !newCustomerIds.includes(originalPrimaryCustomerId.value)) {
          proxy.$modal.msgError("不能删除主客户，请先选择其他客户作为主客户")
          return
        }
        
        // 检查新的主客户是否在客户列表中
        if (newPrimaryId && !newCustomerIds.includes(newPrimaryId)) {
          proxy.$modal.msgError("主客户必须在客户列表中")
          return
        }
      }
      
      // 设置提交加载状态
      submitLoading.value = true
      
      // 处理日期范围字段
      const submitData = { ...form.value }
      if (submitData.dateRange && submitData.dateRange.length === 2) {
        submitData.startDate = submitData.dateRange[0]
        submitData.endDate = submitData.dateRange[1]
        delete submitData.dateRange
      }
      
      // 设置主客户ID（第一个客户或用户选择的主客户）
      if (submitData.customerIds && submitData.customerIds.length > 0) {
        if (!submitData.primaryCustomerId) {
          submitData.primaryCustomerId = submitData.customerIds[0]
        }
        // 设置 customerId 为主客户（保持向后兼容）
        submitData.customerId = submitData.primaryCustomerId
      }

      if (form.value.id != null) {
        // 编辑模式
        updateProjects(submitData).then(response => {
          // 更新成功后，处理客户关联
          handleCustomerRelations(form.value.id, submitData.customerIds, submitData.primaryCustomerId)
            .then(() => {
              proxy.$modal.msgSuccess("修改成功")
              open.value = false
              emit('success')
            })
            .catch(err => {
              const errorMsg = err.message || err
              proxy.$modal.msgError("客户关联更新失败：" + errorMsg)
            })
            .finally(() => {
              submitLoading.value = false
            })
        }).catch(err => {
          submitLoading.value = false
          const errorMsg = err.message || err
          proxy.$modal.msgError("项目更新失败：" + errorMsg)
        })
      } else {
        // 新增模式
        console.log('=== 开始新增项目流程 ===')
        console.log('提交数据:', submitData)
        
        addProjects(submitData).then(response => {
          console.log('=== 项目创建成功 ===')
          console.log('完整响应:', response)
          console.log('response.data:', response.data)
          console.log('response.data?.id:', response.data?.id)
          
          // 项目创建成功后，添加客户关联
          const projectId = response.data?.id || response.data
          console.log('=== 提取项目ID ===')
          console.log('projectId:', projectId)
          console.log('projectId 类型:', typeof projectId)
          console.log('customerIds:', submitData.customerIds)
          console.log('customerIds 长度:', submitData.customerIds?.length)
          console.log('primaryCustomerId:', submitData.primaryCustomerId)
          
          // 检查条件
          console.log('=== 检查添加客户条件 ===')
          console.log('projectId 存在?', !!projectId)
          console.log('customerIds 存在?', !!submitData.customerIds)
          console.log('customerIds 长度 > 0?', submitData.customerIds && submitData.customerIds.length > 0)
          
          if (projectId && submitData.customerIds && submitData.customerIds.length > 0) {
            console.log('=== 条件满足，准备添加客户 ===')
            
            // 添加客户到项目
            const customerData = {
              projectId: projectId,
              customerIds: submitData.customerIds,
              primaryCustomerId: submitData.primaryCustomerId
            }
            console.log('请求数据:', JSON.stringify(customerData, null, 2))
            
            console.log('=== 调用 addCustomersToProject ===')
            addCustomersToProject(customerData).then(addResponse => {
              console.log('=== 客户关联添加成功 ===')
              console.log('添加响应:', addResponse)
              proxy.$modal.msgSuccess("新增成功")
              open.value = false
              emit('success')
            }).catch(err => {
              console.error('=== 客户关联添加失败 ===')
              console.error('错误对象:', err)
              console.error('错误消息:', err.message)
              console.error('错误堆栈:', err.stack)
              const errorMsg = err.message || err
              proxy.$modal.msgWarning("项目创建成功，但客户关联失败：" + errorMsg)
              open.value = false
              emit('success')
            }).finally(() => {
              submitLoading.value = false
            })
          } else {
            console.warn('=== 跳过客户关联 ===')
            console.warn('原因分析:')
            console.warn('- projectId:', projectId, '(', typeof projectId, ')')
            console.warn('- customerIds:', submitData.customerIds)
            console.warn('- customerIds.length:', submitData.customerIds?.length)
            proxy.$modal.msgSuccess("新增成功")
            submitLoading.value = false
            open.value = false
            emit('success')
          }
        }).catch(err => {
          console.error('=== 项目创建失败 ===')
          console.error('错误:', err)
          submitLoading.value = false
          const errorMsg = err.message || err
          proxy.$modal.msgError("项目创建失败：" + errorMsg)
        })
      }
    }
  })
}

/** 处理客户关联（编辑模式） */
async function handleCustomerRelations(projectId, newCustomerIds, newPrimaryCustomerId) {
  try {
    console.log('=== 开始处理客户关联（编辑模式）===')
    console.log('projectId:', projectId)
    console.log('newCustomerIds:', newCustomerIds)
    console.log('newPrimaryCustomerId:', newPrimaryCustomerId)
    
    // 获取当前的客户关联
    const currentRes = await getProjectCustomersList(projectId)
    const currentCustomers = currentRes.data || []
    const currentCustomerIds = currentCustomers.map(pc => pc.customerId)
    
    console.log('当前客户列表:', currentCustomers)
    console.log('当前客户ID列表:', currentCustomerIds)
    
    // 找出需要添加的客户（新选择的但不在当前列表中的）
    const toAdd = newCustomerIds.filter(id => !currentCustomerIds.includes(id))
    console.log('需要添加的客户:', toAdd)
    
    // 找出需要移除的客户（当前列表中但不在新选择中的）
    const toRemove = currentCustomerIds.filter(id => !newCustomerIds.includes(id))
    console.log('需要移除的客户:', toRemove)
    
    // 检查是否要移除主客户
    const currentPrimary = currentCustomers.find(pc => pc.isPrimary)
    if (currentPrimary && toRemove.includes(currentPrimary.customerId)) {
      proxy.$modal.msgError("不能移除主客户，请先设置其他客户为主客户")
      return Promise.reject(new Error("不能移除主客户"))
    }
    
    // 1. 先移除客户（物理删除）
    if (toRemove.length > 0) {
      console.log('=== 准备移除客户 ===')
      for (const customerId of toRemove) {
        console.log('移除客户:', customerId)
        try {
          await removeCustomerFromProject({
            projectId: projectId,
            customerId: customerId
          })
          console.log('客户移除成功:', customerId)
        } catch (err) {
          console.error('客户移除失败:', customerId, err)
          // 继续处理其他客户
        }
      }
      console.log('=== 客户移除完成 ===')
    } else {
      console.log('=== 无需移除客户 ===')
    }
    
    // 2. 添加新客户
    if (toAdd.length > 0) {
      console.log('=== 准备添加新客户 ===')
      console.log('添加请求数据:', {
        projectId: projectId,
        customerIds: toAdd,
        primaryCustomerId: newPrimaryCustomerId
      })
      
      await addCustomersToProject({
        projectId: projectId,
        customerIds: toAdd,
        primaryCustomerId: newPrimaryCustomerId
      })
      
      console.log('=== 新客户添加成功 ===')
    } else {
      console.log('=== 无需添加新客户 ===')
    }
    
    // 3. 更新主客户（如果主客户发生变化）
    console.log('当前主客户:', currentPrimary)
    console.log('新主客户ID:', newPrimaryCustomerId)
    console.log('需要更新主客户?', !currentPrimary || currentPrimary.customerId !== newPrimaryCustomerId)
    
    if (!currentPrimary || currentPrimary.customerId !== newPrimaryCustomerId) {
      console.log('=== 准备更新主客户 ===')
      console.log('更新请求数据:', {
        projectId: projectId,
        customerId: newPrimaryCustomerId
      })
      
      await setPrimaryCustomer({
        projectId: projectId,
        customerId: newPrimaryCustomerId
      })
      
      console.log('=== 主客户更新成功 ===')
    } else {
      console.log('=== 主客户无需更新 ===')
    }
    
    console.log('=== 客户关联处理完成 ===')
    return Promise.resolve()
  } catch (error) {
    console.error('=== 客户关联处理失败 ===')
    console.error('错误:', error)
    return Promise.reject(error)
  }
}

/** 获取客户列表 */
function getCustomersList() {
  listCustomers({ pageNum: 1, pageSize: 1000 }).then(response => {
    customersList.value = response.rows || []
    // 构建客户映射
    const map = new Map()
    customersList.value.forEach(customer => {
      map.set(customer.id, customer.name)
    })
    customerMap.value = map
  })
}

/** 搜索客户（带防抖） */
function searchCustomers(query) {
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 如果查询为空，立即加载默认列表
  if (!query) {
    getCustomersList()
    return
  }
  
  // 设置加载状态
  loading.value = true
  
  // 设置防抖定时器（500ms）
  searchTimer = setTimeout(() => {
    listCustomers({ name: query, pageNum: 1, pageSize: 50 }).then(response => {
      customersList.value = response.rows || []
      loading.value = false
    }).catch(() => {
      loading.value = false
    })
  }, 500)
}

/** 获取客户名称 */
function getCustomerName(customerId) {
  return customerMap.value.get(customerId) || customerId
}

// 组件挂载时获取客户列表
onMounted(() => {
  getCustomersList()
})

// 暴露方法给父组件
defineExpose({
  handleAdd,
  handleEdit
})
</script>
