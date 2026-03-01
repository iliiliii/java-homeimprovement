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
      <el-table-column label="客户" align="center" prop="customerId" width="200">
        <template #default="scope">
          <div v-if="scope.row.projectCustomers && scope.row.projectCustomers.length > 0">
            <!-- 显示主客户 -->
            <el-link
              type="primary"
              @click="goToCustomer(scope.row.customerId)"
              style="display: block; margin-bottom: 4px;"
            >
              {{ getCustomerName(scope.row.customerId) }}
              <el-tag size="small" type="success" style="margin-left: 4px;">主</el-tag>
            </el-link>
            <!-- 显示其他客户 -->
            <div v-if="scope.row.projectCustomers.length > 1" style="font-size: 12px; color: #909399;">
              +{{ scope.row.projectCustomers.length - 1 }} 个客户
              <el-button
                link
                type="primary"
                size="small"
                @click="showCustomersList(scope.row)"
              >
                查看
              </el-button>
            </div>
          </div>
          <div v-else-if="getCustomerName(scope.row.customerId)">
            <el-link
              type="primary"
              @click="goToCustomer(scope.row.customerId)"
            >
              {{ getCustomerName(scope.row.customerId) }}
            </el-link>
          </div>
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

    <!-- 使用 ProjectEdit 组件 -->
    <ProjectEdit ref="projectEditRef" @success="getList" />

    <!-- 项目客户列表对话框 -->
    <el-dialog title="项目客户列表" v-model="customersDialogVisible" width="600px" append-to-body>
      <el-empty v-if="!currentProjectCustomers || currentProjectCustomers.length === 0" description="暂无客户信息" />
      <el-table v-else :data="currentProjectCustomers" style="width: 100%">
        <el-table-column label="客户名称" prop="customerName" width="150">
          <template #default="scope">
            <el-link type="primary" @click="goToCustomer(scope.row.customerId)">
              {{ scope.row.customer?.name || scope.row.customerId }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="联系电话" prop="customerPhone" width="120">
          <template #default="scope">
            {{ scope.row.customer?.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" prop="role" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isPrimary" type="success" size="small">主客户</el-tag>
            <el-tag v-else type="info" size="small">{{ scope.row.role || '客户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="150">
          <template #default="scope">
            <el-button
              v-if="!scope.row.isPrimary"
              link
              type="primary"
              size="small"
              @click="handleSetPrimary(scope.row)"
            >
              设为主客户
            </el-button>
            <el-button
              v-if="!scope.row.isPrimary"
              link
              type="danger"
              size="small"
              @click="handleRemoveCustomer(scope.row)"
            >
              移除
            </el-button>
            <span v-if="scope.row.isPrimary" style="color: #909399; font-size: 12px;">
              主客户不可操作
            </span>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="customersDialogVisible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Projects">
import { listProjects, getProjects, delProjects, listProjectsWithCustomers } from "@/api/evs/projects"
import { listCustomers } from "@/api/evs/customers"
import { getProjectCustomersList, setPrimaryCustomer, removeCustomerFromProject } from "@/api/evs/projectCustomers"
import { useRouter } from 'vue-router'
import ProjectEdit from './components/ProjectEdit.vue'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { project_status } = proxy.useDict('project_status')

const projectsList = ref([])
const customersList = ref([])
const customerMap = ref(new Map())
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

// ProjectEdit 组件引用
const projectEditRef = ref(null)

// 客户列表对话框
const customersDialogVisible = ref(false)
const currentProjectCustomers = ref([])
const currentProject = ref(null)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    customerId: null,
    address: null,
    status: null,
  }
})

const { queryParams } = toRefs(data)

/** 查询项目信息列表 */
function getList() {
  loading.value = true
  // 查询项目列表时包含客户信息
  listProjectsWithCustomers(queryParams.value).then(response => {
    projectsList.value = response.rows
    // 为每个项目加载客户列表
    projectsList.value.forEach(project => {
      if (project.id) {
        getProjectCustomersList(project.id).then(res => {
          project.projectCustomers = res.data || []
        }).catch(() => {
          project.projectCustomers = []
        })
      }
    })
    total.value = response.total
    loading.value = false
  })
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
  projectEditRef.value.handleAdd()
}

/** 修改按钮操作 */
function handleUpdate(row) {
  const project = row.id ? row : projectsList.value.find(p => p.id === ids.value[0])
  if (project) {
    projectEditRef.value.handleEdit(project)
  }
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  const projectName = row.name || '选中的项目'
  
  proxy.$modal.confirm(`是否确认删除项目"${projectName}"？删除后将无法恢复，且所有关联的客户关系也会被删除。`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(function() {
    const loadingInstance = proxy.$loading({
      lock: true,
      text: '正在删除项目...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    return delProjects(_ids).then(() => {
      loadingInstance.close()
      getList()
      proxy.$modal.msgSuccess("删除成功")
    }).catch(err => {
      loadingInstance.close()
      throw err
    })
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.$modal.confirm('是否确认导出所有项目数据？').then(() => {
    const loadingInstance = proxy.$loading({
      lock: true,
      text: '正在导出数据，请稍候...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    proxy.download('evs/projects/export', {
      ...queryParams.value
    }, `projects_${new Date().getTime()}.xlsx`).then(() => {
      loadingInstance.close()
      proxy.$modal.msgSuccess("导出成功")
    }).catch(() => {
      loadingInstance.close()
      proxy.$modal.msgError("导出失败")
    })
  }).catch(() => {})
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

/** 显示项目客户列表 */
function showCustomersList(row) {
  currentProject.value = row
  const loadingInstance = proxy.$loading({
    lock: true,
    text: '正在加载客户列表...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  getProjectCustomersList(row.id).then(res => {
    loadingInstance.close()
    currentProjectCustomers.value = res.data || []
    customersDialogVisible.value = true
  }).catch(err => {
    loadingInstance.close()
    const errorMsg = err.message || err
    proxy.$modal.msgError("获取客户列表失败：" + errorMsg)
  })
}

/** 设置主客户 */
function handleSetPrimary(customer) {
  const customerName = customer.customer?.name || customer.customerId
  proxy.$modal.confirm(`确认将"${customerName}"设置为主客户吗？设置后该客户将作为项目的主要联系人。`).then(() => {
    const loadingInstance = proxy.$loading({
      lock: true,
      text: '正在设置主客户...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    setPrimaryCustomer({
      projectId: currentProject.value.id,
      customerId: customer.customerId
    }).then(() => {
      loadingInstance.close()
      proxy.$modal.msgSuccess("设置成功")
      // 刷新客户列表
      showCustomersList(currentProject.value)
      // 刷新项目列表
      getList()
    }).catch(err => {
      loadingInstance.close()
      const errorMsg = err.message || err
      proxy.$modal.msgError("设置失败：" + errorMsg)
    })
  }).catch(() => {})
}

/** 移除客户 */
function handleRemoveCustomer(customer) {
  const customerName = customer.customer?.name || customer.customerId
  proxy.$modal.confirm(`确认从项目中移除客户"${customerName}"吗？移除后该客户将无法访问此项目。`, '警告', {
    confirmButtonText: '确定移除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const loadingInstance = proxy.$loading({
      lock: true,
      text: '正在移除客户...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    removeCustomerFromProject({
      projectId: currentProject.value.id,
      customerId: customer.customerId
    }).then(() => {
      loadingInstance.close()
      proxy.$modal.msgSuccess("移除成功")
      // 刷新客户列表
      showCustomersList(currentProject.value)
      // 刷新项目列表
      getList()
    }).catch(err => {
      loadingInstance.close()
      const errorMsg = err.message || err
      proxy.$modal.msgError("移除失败：" + errorMsg)
    })
  }).catch(() => {})
}

// 初始化
getList()
getCustomersList()
</script>
