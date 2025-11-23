<template>
  <div class="app-container">
    <!-- 搜索表单 -->
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
            v-for="dict in decoration_project_status"
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

    <!-- 顶部操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['evs:projects:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['evs:projects:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['evs:projects:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['evs:projects:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <!-- 项目卡片网格 -->
    <el-row v-loading="loading" :gutter="16" style="margin-bottom: 16px;">
      <el-col
        v-for="project in projectsList"
        :key="project.id"
        :xs="24"
        :sm="12"
        :lg="8"
        style="margin-bottom: 16px;"
      >
        <el-card shadow="hover" style="height: 100%;" :body-style="{ padding: '16px' }">
          <!-- 卡片选择框 -->
          <div style="position: absolute; top: 12px; right: 12px; z-index: 1;">
            <el-checkbox v-model="project.checked" @change="handleCardSelectChange(project)" />
          </div>

          <!-- 卡片头部: 项目名称 + 状态标签 -->
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
              <div style="flex: 1; min-width: 0; padding-right: 40px;">
                <div style="font-size: 16px; font-weight: 600; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                  {{ project.name }}
                </div>
                <div style="font-size: 13px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                  {{ project.address }}
                </div>
              </div>
              <dict-tag v-if="project.status" :options="decoration_project_status" :value="project.status" />
            </div>
          </template>

          <!-- 卡片主体 -->
          <div style="margin-bottom: 16px;">
            <!-- 客户信息 -->
            <div style="display: flex; align-items: center; margin-bottom: 8px; font-size: 13px; color: #606266;">
              <el-icon style="margin-right: 6px;"><User /></el-icon>
              <span>客户：</span>
              <el-link
                v-if="getCustomerName(project.customerId)"
                type="primary"
                :underline="false"
                @click="goToCustomer(project.customerId)"
                style="margin-left: 4px; font-size: 13px;"
              >
                {{ getCustomerName(project.customerId) }}
              </el-link>
              <span v-else style="margin-left: 4px;">{{ project.customerId }}</span>
            </div>

            <!-- 日期范围 -->
            <div style="display: flex; align-items: center; margin-bottom: 8px; font-size: 13px; color: #606266;">
              <el-icon style="margin-right: 6px;"><Calendar /></el-icon>
              <span>{{ parseTime(project.startDate, '{y}-{m}-{d}') }}</span>
              <span style="margin: 0 4px;">至</span>
              <span>{{ parseTime(project.endDate, '{y}-{m}-{d}') }}</span>
            </div>

            <!-- 房屋面积 -->
            <div v-if="project.area" style="display: flex; align-items: center; margin-bottom: 12px; font-size: 13px; color: #606266;">
              <el-icon style="margin-right: 6px;"><HomeFilled /></el-icon>
              <span>面积：{{ project.area }}㎡</span>
            </div>
          </div>

          <!-- 预算信息 -->
          <div style="margin-bottom: 12px;">
            <template v-if="project.budget && project.budget > 0">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                <span style="font-size: 14px; font-weight: 600; color: #303133;">总预算</span>
                <span style="font-size: 18px; font-weight: 600; color: #1677ff;">¥{{ formatBudget(project.budget) }}万</span>
              </div>
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; font-size: 13px; color: #909399;">
                <span>已支出</span>
                <span>¥{{ formatBudget(project.actualCost || 0) }}万</span>
              </div>
              <el-progress
                :percentage="calculateProgress(project.actualCost || 0, project.budget)"
                :stroke-width="6"
                :show-text="false"
              />
            </template>
            <div v-else style="padding: 12px; background: #fff7e6; border: 1px dashed #ffd591; border-radius: 4px; text-align: center;">
              <span style="font-size: 12px; color: #909399;">
                <el-icon style="vertical-align: middle; margin-right: 4px;"><Wallet /></el-icon>
                尚未设置预算
              </span>
            </div>
          </div>

          <!-- 卡片底部操作按钮 -->
          <template #footer>
            <div style="display: flex; justify-content: space-around; gap: 8px; margin-top: 4px;">
              <el-button
                type="primary"
                link
                size="small"
                @click="handleViewDetail(project)"
              >
                <el-icon><View /></el-icon>
                <span>查看</span>
              </el-button>
              <el-button
                type="primary"
                link
                size="small"
                @click="handleUpdate(project)"
                v-hasPermi="['evs:projects:edit']"
              >
                <el-icon><Edit /></el-icon>
                <span>编辑</span>
              </el-button>
              <el-button
                type="warning"
                link
                size="small"
                @click="handleBudgetManagement(project)"
              >
                <el-icon><Wallet /></el-icon>
                <span>预算</span>
              </el-button>
              <el-button
                type="success"
                link
                size="small"
                @click="handleProgressManagement(project)"
              >
                <el-icon><Clock /></el-icon>
                <span>进度</span>
              </el-button>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
      style="margin-top: 16px;"
    />

    <!-- 项目详情组件 -->
    <ProjectDetail
      ref="projectDetailRef"
      @update="handleUpdate"
      @budget="handleBudgetManagement"
      @progress="handleProgressManagement"
    />

    <!-- 编辑项目组件 -->
    <ProjectEdit
      ref="projectEditRef"
      @success="getList"
    />

    <!-- 预算管理对话框 -->
    <el-dialog v-model="budgetOpen" width="900px" append-to-body :show-close="true" :close-on-click-modal="false" class="project-budget-dialog">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon style="color: #faad14; font-size: 16px;"><Wallet /></el-icon>
          <span>{{ currentBudgetProject.name }} - 预算管理</span>
        </div>
      </template>
      <div style="max-height: calc(90vh - 150px); overflow-y: auto; padding: 0 8px;">
        <ProjectBudget v-if="currentBudgetProject" :project="currentBudgetProject" @save="handleSaveBudget" />
      </div>
    </el-dialog>

    <!-- 进度管理对话框 -->
    <el-dialog v-model="progressOpen" width="900px" append-to-body :show-close="true" :close-on-click-modal="false" class="project-progress-dialog">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon style="color: #1677ff; font-size: 16px;"><Clock /></el-icon>
          <span>{{ currentProgressProject.name }} - 施工进度管理</span>
        </div>
      </template>
      <div style="max-height: calc(90vh - 150px); overflow-y: auto; padding: 0 8px;">
        <ProjectProgress v-if="currentProgressProject" :project="currentProgressProject" @save="handleSaveProgress" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="Projects">
import { listProjects, updateProjects, delProjects } from "@/api/evs/projects"
import { listCustomers } from "@/api/evs/customers"
import { useRouter } from 'vue-router'
import ProjectProgress from './components/ProjectProgress.vue'
import ProjectBudget from './components/ProjectBudget.vue'
import ProjectDetail from './components/ProjectDetail.vue'
import ProjectEdit from './components/ProjectEdit.vue'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { decoration_project_status } = proxy.useDict('decoration_project_status')

// 组件引用
const projectDetailRef = ref()
const projectEditRef = ref()

const projectsList = ref([])
const customersList = ref([])
const customerMap = ref(new Map())
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

// 选择状态管理
const ids = ref([])
const single = ref(true)
const multiple = ref(true)

// 预算管理弹窗
const budgetOpen = ref(false)
const currentBudgetProject = ref({})

// 进度管理弹窗
const progressOpen = ref(false)
const currentProgressProject = ref({})

// 列显隐信息
const columns = ref({
  id: { label: '项目ID', visible: true },
  name: { label: '项目名称', visible: true },
  customer: { label: '客户', visible: true },
  address: { label: '地址', visible: true },
  status: { label: '状态', visible: true },
  budget: { label: '预算', visible: true },
  createTime: { label: '创建时间', visible: true }
})

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
  listProjects(queryParams.value).then(response => {
    projectsList.value = response.rows.map(project => ({
      ...project,
      checked: false // 确保每个项目都有 checked 属性
    }))
    total.value = response.total
    loading.value = false
    // 重置选择状态
    ids.value = []
    single.value = true
    multiple.value = true
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

/** 新增按钮操作 */
function handleAdd() {
  projectEditRef.value?.handleAdd()
}

/** 修改按钮操作 */
function handleUpdate(row) {
  // 如果没有传入 row，则从选中的记录中获取
  const project = row || projectsList.value.find(item => ids.value.includes(item.id))
  if (project) {
    projectEditRef.value?.handleEdit(project)
  }
}

/** 删除按钮操作 */
function handleDelete(row) {
  const projectIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除项目编号为"' + projectIds + '"的数据项？').then(function () {
    return delProjects(projectIds)
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

/** 卡片选择处理 */
function handleCardSelectChange(project) {
  ids.value = projectsList.value.filter(item => item.checked).map(item => item.id)
  single.value = ids.value.length !== 1
  multiple.value = !ids.value.length
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

/** 格式化预算金额(转为万元) */
function formatBudget(amount) {
  if (!amount) return '0.00'
  return (amount / 10000).toFixed(2)
}

/** 计算预算进度百分比 */
function calculateProgress(actual, total) {
  if (!total || total === 0) return 0
  const percent = (actual / total) * 100
  return Math.min(Math.round(percent), 100)
}

/** 查看项目详情 */
function handleViewDetail(project) {
  projectDetailRef.value?.handleView(project)
}

/** 预算管理 */
function handleBudgetManagement(project) {
  currentBudgetProject.value = project
  budgetOpen.value = true
}

/** 处理预算保存事件 */
function handleSaveBudget(updateData) {
  updateProjects(updateData).then(() => {
    proxy.$modal.msgSuccess("预算已保存")
    budgetOpen.value = false
    getList() // 刷新项目列表
  }).catch(error => {
    proxy.$modal.msgError("保存失败：" + (error.message || "未知错误"))
  })
}

/** 进度管理 */
function handleProgressManagement(project) {
  currentProgressProject.value = project
  progressOpen.value = true
}

/** 处理进度保存事件 */
function handleSaveProgress(updateData) {
  updateProjects(updateData).then(() => {
    proxy.$modal.msgSuccess("进度已保存")
    progressOpen.value = false
    getList() // 刷新项目列表
  }).catch(error => {
    proxy.$modal.msgError("保存失败：" + (error.message || "未知错误"))
  })
}

// 初始化
getList()
getCustomersList()
</script>

<style lang="scss">
// 预算对话框样式优化
.project-budget-dialog .el-dialog__body .el-space.el-space--vertical > .el-space__item {
  width: 100% !important;
  flex-basis: 100% !important;
  max-width: 100% !important;
}

.project-budget-dialog .el-dialog__body .el-table {
  width: 100% !important;
}

.project-budget-dialog .el-dialog__body .el-table table.el-table__header,
.project-budget-dialog .el-dialog__body .el-table table.el-table__body,
.project-budget-dialog .el-dialog__body .el-table table {
  width: 100% !important;
  min-width: 100% !important;
}

.project-budget-dialog .el-dialog__body .el-space__item > * {
  width: 100%;
  box-sizing: border-box;
}

// 进度对话框样式优化
.project-progress-dialog .el-dialog__body .el-space.el-space--vertical > .el-space__item {
  width: 100% !important;
  flex-basis: 100% !important;
  max-width: 100% !important;
}

.project-progress-dialog .el-timeline {
  padding-left: 0;
}

.project-progress-dialog .el-timeline-item__content {
  padding-left: 20px;
}

.project-progress-dialog .el-dialog__body .el-space__item > * {
  width: 100%;
  box-sizing: border-box;
}
</style>
