<template>
  <!-- 添加或修改项目信息对话框 -->
  <el-dialog :title="title" v-model="open" width="600px" append-to-body>
    <el-form ref="projectsRef" :model="form" :rules="rules" label-width="100px">
      <!-- 核心字段 - 新建和编辑都显示 -->
      <el-form-item label="项目名称" prop="name">
        <el-input v-model="form.name" placeholder="例如: 张先生家装修工程" />
      </el-form-item>

      <el-form-item label="客户" prop="customerId">
        <el-select
          v-model="form.customerId"
          placeholder="请选择客户"
          clearable
          filterable
          remote
          :remote-method="searchCustomers"
          :loading="loading"
          style="width: 100%"
        >
          <el-option
            v-for="customer in customersList"
            :key="customer.id"
            :label="`${customer.name} (${customer.phone})`"
            :value="customer.id"
          >
            <span style="float: left">{{ customer.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">{{ customer.phone }}</span>
          </el-option>
        </el-select>
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
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="���简要描述项目需求和特点" />
      </el-form-item>

      <!-- 编辑模式专属字段 -->
      <template v-if="isEdit">
        <el-form-item label="项目编号" prop="projectCode">
          <el-input v-model="form.projectCode" placeholder="系统自动生成" disabled />
        </el-form-item>

        <el-form-item label="预算金额" prop="budget">
          <el-input v-model="form.budget" placeholder="请输入预算金额(元)" />
        </el-form-item>

        <el-form-item label="实际费用" prop="actualCost">
          <el-input v-model="form.actualCost" placeholder="请输入实际费用(元)" />
        </el-form-item>

        <el-form-item label="实际完工日期" prop="actualEndDate">
          <el-date-picker
            clearable
            v-model="form.actualEndDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择实际完工日期"
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
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { listCustomers } from '@/api/evs/customers'
import { getProjects, addProjects, updateProjects } from '@/api/evs/projects'

const { proxy } = getCurrentInstance()
const { decoration_project_status } = proxy.useDict('decoration_project_status')

const props = defineProps({
  // 无需接收 props，对外暴露方法
})

const emit = defineEmits(['success'])

// 响应式数据
const open = ref(false)
const loading = ref(false)
const title = ref("")
const isEdit = computed(() => form.value.id != null)

const form = ref({
  id: null,
  name: null,
  customerId: null,
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

const rules = {
  name: [
    { required: true, message: "项目名称不能为空", trigger: "blur" }
  ],
  customerId: [
    { required: true, message: "请选择客户", trigger: "change" }
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
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: null,
    name: null,
    customerId: null,
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
    form.value = response.data
    // 处理日期范围字段
    if (form.value.startDate && form.value.endDate) {
      form.value.dateRange = [form.value.startDate, form.value.endDate]
    }
    open.value = true
    title.value = "修改项目信息"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["projectsRef"].validate(valid => {
    if (valid) {
      // 处理日期范围字段
      const submitData = { ...form.value }
      if (submitData.dateRange && submitData.dateRange.length === 2) {
        submitData.startDate = submitData.dateRange[0]
        submitData.endDate = submitData.dateRange[1]
        delete submitData.dateRange
      }

      if (form.value.id != null) {
        updateProjects(submitData).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          emit('success')
        })
      } else {
        addProjects(submitData).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          emit('success')
        })
      }
    }
  })
}

/** 获取客户列表 */
function getCustomersList() {
  listCustomers({ pageNum: 1, pageSize: 1000 }).then(response => {
    customersList.value = response.rows || []
  })
}

/** 搜索客户 */
function searchCustomers(query) {
  if (query) {
    listCustomers({ name: query, phone: query, pageNum: 1, pageSize: 50 }).then(response => {
      customersList.value = response.rows || []
    })
  } else {
    getCustomersList()
  }
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
