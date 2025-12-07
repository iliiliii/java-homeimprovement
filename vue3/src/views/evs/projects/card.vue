<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">项目管理</h2>
        <p class="page-subtitle">管理装修项目信息</p>
      </div>
      <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['evs:projects:add']">
        新建项目
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px" class="search-form">
      <el-form-item label="项目名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入项目名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
        <el-form-item label="项目状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择项目状态" clearable style="width: 150px">
          <el-option
            v-for="dict in decoration_project_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <!-- 管理员特有筛选条件 -->
      <el-form-item v-if="isAdmin" label="关联客户" prop="customerId">
        <el-input
          v-model="queryParams.customerId"
          placeholder="请输入客户ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item v-if="isAdmin" label="关联团队" prop="memberUserId">
        <el-input
          v-model="queryParams.memberUserId"
          placeholder="请输入团队成员用户ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 项目卡片网格 -->
    <div class="projects-container" v-loading="loading">
      <el-row :gutter="16">
        <el-col
          v-for="project in projectsList"
          :key="project.id"
          :xs="24"
          :sm="12"
          :lg="8"
          style="margin-bottom: 16px;"
      >
        <el-card shadow="hover" style="height: 100%;" :body-style="{ padding: '16px' }">
          <!-- 卡片头部: 项目名称 + 状态标签 -->
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
              <div style="flex: 1; min-width: 0;">
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
                v-if="project.customerId && getCustomerName(project) !== '未关联客户'"
                type="primary"
                :underline="false"
                @click="goToCustomer(project.customerId)"
                style="margin-left: 4px; font-size: 13px;"
              >
                {{ getCustomerName(project) }}
              </el-link>
              <span v-else style="margin-left: 4px;">{{ getCustomerName(project) }}</span>
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

    <!-- 空状态 -->
    <el-empty v-if="!loading && projectsList.length === 0" description="暂无项目数据" :image-size="200" />
    </div>

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
    <el-dialog v-model="budgetOpen" width="1200px" append-to-body :show-close="true" :close-on-click-modal="false" class="project-budget-dialog">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon style="color: #faad14; font-size: 16px;"><Wallet /></el-icon>
          <span>{{ currentBudgetProject.name }} - 预算管理</span>
        </div>
      </template>
      <div style="max-height: calc(90vh - 150px); overflow-y: auto; padding: 0 20px;">
        <ProjectBudget v-if="currentBudgetProject" :project="currentBudgetProject" @save="handleSaveBudget" />
      </div>
    </el-dialog>

    <!-- 进度管理对话框 -->
    <el-dialog v-model="progressOpen" width="1200px" append-to-body :show-close="true" :close-on-click-modal="false" class="project-progress-dialog">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon style="color: #1677ff; font-size: 16px;"><Clock /></el-icon>
          <span>{{ currentProgressProject.name }} - 施工进度管理</span>
        </div>
      </template>
      <div style="max-height: calc(90vh - 150px); overflow-y: auto; padding: 0 20px;">
        <ProjectProgress v-if="currentProgressProject" :project="currentProgressProject" @save="handleSaveProgress" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="Projects">
import { listProjects, updateProjects, delProjects, listProjectsWithCustomer, listProjectsWithMembers, getProjectWithCustomer } from "@/api/evs/projects"
import { useRouter, useRoute } from 'vue-router'
import userStore from '@/store/modules/user'
import ProjectProgress from './components/ProjectProgress.vue'
import ProjectBudget from './components/ProjectBudget.vue'
import ProjectDetail from './components/ProjectDetail.vue'
import ProjectEdit from './components/ProjectEdit.vue'

const router = useRouter()
const route = useRoute()
const { proxy } = getCurrentInstance()
const { decoration_project_status } = proxy.useDict('decoration_project_status')

// 判断是否为管理员
const isAdmin = computed(() => {
  return userStore.roles && userStore.roles.includes('admin')
})

// 组件引用
const projectDetailRef = ref()
const projectEditRef = ref()

const projectsList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

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
    address: null,
    status: null,
    // 管理员特有筛选条件
    customerId: null,      // 关联客户ID
    memberUserId: null,    // 关联团队成员用户ID
  }
})

const { queryParams } = toRefs(data)

/** 查询项目信息列表（使用关联查询支持权限过滤） */
function getList() {
  loading.value = true
  // 所有用户都使用关联查询，管理员查看所有，非管理员通过后端过滤
  listProjectsWithMembers(queryParams.value).then(response => {
    projectsList.value = response.rows
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

/** 新增按钮操作 */
function handleAdd() {
  projectEditRef.value?.handleAdd()
}

/** 修改按钮操作 */
function handleUpdate(row) {
  if (row) {
    projectEditRef.value?.handleEdit(row)
  }
}

/** 删除按钮操作 */
function handleDelete(row) {
  const projectIds = row.id
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

/** 获取客户名称 */
function getCustomerName(project) {
  // 直接从关联的客户信息中获取名称
  if (project.customer && project.customer.name) {
    return project.customer.name
  }
  // 如果没有关联的客户信息，返回客户ID或默认值
  return project.customerId || '未关联客户'
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
    
    // 如果项目详情对话框打开，刷新详情中的预算数据
    if (projectDetailRef.value && projectDetailRef.value.currentProject.id === updateData.id) {
      console.log('[项目卡片] 预算已更新，刷新详情数据')
      projectDetailRef.value.loadBudgetData()
    }
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
    
    // 如果项目详情对话框打开，刷新详情中的进度数据
    if (projectDetailRef.value && projectDetailRef.value.currentProject.id === updateData.id) {
      console.log('[项目卡片] 进度已更新，刷新详情数据')
      projectDetailRef.value.loadProgressData()
    }
  }).catch(error => {
    proxy.$modal.msgError("保存失败：" + (error.message || "未知错误"))
  })
}

/** 处理路由参数 */
function handleRouteQuery() {
  const { id, status } = route.query
  
  // 如果有状态参数，设置筛选条件
  if (status) {
    queryParams.value.status = status
    getList()
  }
  
  // 如果有id参数，直接打开项目详情
  if (id) {
    getProjectWithCustomer(id).then(response => {
      if (response.data) {
        projectDetailRef.value?.handleView(response.data)
      }
    }).catch(() => {
      proxy.$modal.msgWarning('未找到该项目信息')
    })
  }
}

// 初始化
getList()
// 延迟处理路由参数，确保组件已挂载
nextTick(() => {
  handleRouteQuery()
})

// 监听路由变化
watch(() => route.query, (newQuery) => {
  if (newQuery.id || newQuery.status) {
    handleRouteQuery()
  }
}, { deep: true })
</script>

<style scoped lang="scss">
.app-container {
  height: calc(100vh - 124px);
  display: flex;
  flex-direction: column;
  padding: 20px;
  overflow: hidden;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  .page-title {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }

  .page-subtitle {
    margin: 4px 0 0 0;
    font-size: 14px;
    color: #909399;
  }
}

.search-form {
  display: flex;
  justify-content: start;
  align-items: flex-start;
  margin-bottom: 12px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  :deep(.el-form-item) {
    margin-bottom: 0 !important;
    margin-right: 16px;
  }
}

/* 项目容器 - 独立滚动 */
.projects-container {
  flex: 1;
  overflow-y: auto;  // 只允许垂直滚动
  overflow-x: hidden; // 禁止水平滚动（取消底部滚动条）
  margin-bottom: 12px;
  padding: 2px;

  // 优化滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;

    &:hover {
      background: #a8a8a8;
    }
  }

  .el-col {
    margin-bottom: 16px;

    @media (min-width: 1200px) {
      margin-bottom: 20px;
    }

    @media (max-width: 768px) {
      margin-bottom: 12px;
    }
  }
}

// 预算对话框样式优化
.project-budget-dialog .el-dialog__body {
  padding: 20px 16px !important;
}

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

/* 确保所有表单和输入元素撑满 */
.project-budget-dialog .el-input,
.project-budget-dialog .el-select,
.project-budget-dialog .el-input-number,
.project-budget-dialog .el-form-item {
  width: 100% !important;
}

.project-budget-dialog .el-row {
  width: 100% !important;
}

.project-budget-dialog .el-col {
  width: 100% !important;
}

// 进度对话框样式优化
.project-progress-dialog .el-dialog__body {
  padding: 20px 16px !important;
}

.project-progress-dialog .el-dialog__body .el-space.el-space--vertical > .el-space__item {
  width: 100% !important;
  flex-basis: 100% !important;
  max-width: 100% !important;
}

.project-progress-dialog .el-timeline {
  padding-left: 0;
  width: 100% !important;
}

.project-progress-dialog .el-timeline-item__content {
  padding-left: 20px;
  width: 100% !important;
}

.project-progress-dialog .el-dialog__body .el-space__item > * {
  width: 100%;
  box-sizing: border-box;
}

/* 确保所有表单和输入元素撑满 */
.project-progress-dialog .el-input,
.project-progress-dialog .el-select,
.project-progress-dialog .el-date-editor,
.project-progress-dialog .el-form-item {
  width: 100% !important;
}

.project-progress-dialog .el-row {
  width: 100% !important;
}

.project-progress-dialog .el-col {
  width: 100% !important;
}

.project-progress-dialog .timeline-container {
  width: 100% !important;
}

/* 通用对话框样式强化 */
.el-dialog__body {
  padding: 20px 16px !important;
}

.el-dialog__body .el-space {
  width: 100% !important;
}

.el-dialog__body .el-space--vertical {
  width: 100% !important;
}

/* 关键修复：确保所有对话框中的 el-space__item 撑满宽度 */
.el-dialog__body .el-space__item {
  width: 100% !important;
  flex: 1 !important;
  min-width: 0 !important;
  box-sizing: border-box !important;
}

/* 更广泛的覆盖 - 确保所有场景下的 el-space__item */
.project-budget-dialog .el-space__item,
.project-progress-dialog .el-space__item {
  width: 100% !important;
  flex: 1 !important;
  min-width: 0 !important;
  box-sizing: border-box !important;
}
</style>
