<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="项目名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入项目名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="客户" prop="customerId">
        <el-select
          v-model="queryParams.customerId"
          placeholder="请选择客户"
          clearable
          filterable
          style="width: 200px"
        >
          <el-option
            v-for="customer in customersList"
            :key="customer.id"
            :label="`${customer.name} (${customer.phone})`"
            :value="customer.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="项目状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择项目状态" clearable>
          <el-option
            v-for="dict in project_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['evs:projects:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['evs:projects:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['evs:projects:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['evs:projects:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="projectsList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="项目ID" align="center" prop="id" />
      <el-table-column label="项目名称" align="center" prop="name" />
      <el-table-column label="客户" align="center" prop="customerId" width="150">
        <template #default="scope">
          <el-link
            v-if="getCustomerName(scope.row.customerId)"
            type="primary"
            @click="goToCustomer(scope.row.customerId)"
          >
            {{ getCustomerName(scope.row.customerId) }}
          </el-link>
          <span v-else>{{ scope.row.customerId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="项目编号" align="center" prop="projectCode" />
      <el-table-column label="项目描述" align="center" prop="description" />
      <el-table-column label="项目地址" align="center" prop="address" />
      <el-table-column label="房屋面积" align="center" prop="area" />
      <el-table-column label="预算金额" align="center" prop="budget" />
      <el-table-column label="实际费用" align="center" prop="actualCost" />
      <el-table-column label="开始日期" align="center" prop="startDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="预计完工日期" align="center" prop="endDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="实际完工日期" align="center" prop="actualEndDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.actualEndDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="项目状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="project_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="优先级" align="center" prop="priority" />
      <el-table-column label="进度百分比" align="center" prop="progress" />
      <el-table-column label="创建时间" align="center" prop="createdAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createdAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['evs:projects:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['evs:projects:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改项目信息对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="projectsRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
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
        <el-form-item label="项目编号" prop="projectCode">
          <el-input v-model="form.projectCode" placeholder="请输入项目编号" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="项目地址" prop="address">
          <el-input v-model="form.address" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="房屋面积" prop="area">
          <el-input v-model="form.area" placeholder="请输入房屋面积" />
        </el-form-item>
        <el-form-item label="预算金额" prop="budget">
          <el-input v-model="form.budget" placeholder="请输入预算金额" />
        </el-form-item>
        <el-form-item label="实际费用" prop="actualCost">
          <el-input v-model="form.actualCost" placeholder="请输入实际费用" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker clearable
            v-model="form.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择开始日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="预计完工日期" prop="endDate">
          <el-date-picker clearable
            v-model="form.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择预计完工日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="实际完工日期" prop="actualEndDate">
          <el-date-picker clearable
            v-model="form.actualEndDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择实际完工日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择项目状态">
            <el-option
              v-for="dict in project_status"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间" prop="createdAt">
          <el-date-picker clearable
            v-model="form.createdAt"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择创建时间">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Projects">
import { listProjects, getProjects, delProjects, addProjects, updateProjects } from "@/api/evs/projects"
import { listCustomers } from "@/api/evs/customers"
import { useRouter } from 'vue-router'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { project_status } = proxy.useDict('project_status')

const projectsList = ref([])
const customersList = ref([])
const customerMap = ref(new Map())
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    customerId: null,
    address: null,
    status: null,
  },
  rules: {
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
})

const { queryParams, form, rules } = toRefs(data)

/** 查询项目信息列表 */
function getList() {
  loading.value = true
  listProjects(queryParams.value).then(response => {
    projectsList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
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
    deletedBy: null
  }
  proxy.resetForm("projectsRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加项目信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getProjects(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改项目信息"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["projectsRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateProjects(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addProjects(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除项目信息编号为"' + _ids + '"的数据项？').then(function() {
    return delProjects(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('evs/projects/export', {
    ...queryParams.value
  }, `projects_${new Date().getTime()}.xlsx`)
}

/** 获取客户列表 */
function getCustomersList() {
  listCustomers({ pageNum: 1, pageSize: 1000 }).then(response => {
    customersList.value = response.rows || []
    // 构建客户映射，用于快速查找客户名称
    const map = new Map()
    customersList.value.forEach(customer => {
      map.set(customer.id, customer.name)
    })
    customerMap.value = map
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

/** 获取客户名称 */
function getCustomerName(customerId) {
  return customerMap.value.get(customerId) || ''
}

/** 跳转到客户详情页 */
function goToCustomer(customerId) {
  if (customerId) {
    // 使用Vue Router跳转到客户管理页面，并传递客户ID参数
    router.push({
      path: '/evs/customers',
      query: { id: customerId }
    })
  }
}

// 初始化
getList()
getCustomersList()
</script>
