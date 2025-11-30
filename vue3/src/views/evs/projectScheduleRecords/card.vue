<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">进度跟踪</h2>
        <p class="page-subtitle">跟踪和管理项目施工进度</p>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch"  class="search-form">
      <el-form-item label="项目名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入项目名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 主要内容区域：左右布局 -->
    <el-row :gutter="20" v-loading="loading" class="main-content-row">
      <!-- 左侧：进行中的项目列表 -->
      <ProjectScheduleList
        :projects="inProgressProjects"
        :selected-project="selectedProject"
        :loading="loading"
        @select="handleProjectSelect"
      />

      <!-- 右侧：项目详情和进度 -->
      <ProjectScheduleDetail
        ref="scheduleDetailRef"
        :project="selectedProject"
        :schedule-items="scheduleItems"
        :loading="scheduleLoading"
        @acceptance-report="handleAcceptanceReport"
        @edit-acceptance="handleEditAcceptance"
        @delete-acceptance="handleDeleteAcceptance"
      />
    </el-row>

    <!-- 验收上报对话框 -->
    <AcceptanceReportDialog
      ref="acceptanceDialogRef"
      :visible="acceptanceDialogOpen"
      :project="selectedProject"
      :schedule-item="currentScheduleItem"
      :upload-url="uploadUrl"
      :upload-headers="uploadHeaders"
      @update:visible="acceptanceDialogOpen = $event"
      @submit="handleSubmitAcceptance"
      @success="handleAcceptanceSuccess"
      @error="handleAcceptanceError"
      @loading-change="handleLoadingChange"
    />

    <!-- 编辑验收记录对话框 -->
    <AcceptanceReportDialog
      ref="acceptanceEditDialogRef"
      :visible="acceptanceEditOpen"
      :project="selectedProject"
      :schedule-item="scheduleItems.find(s => s.id === currentEditRecord?.scheduleId)"
      :upload-url="uploadUrl"
      :upload-headers="uploadHeaders"
      :is-edit="true"
      :edit-record="currentEditRecord"
      @update:visible="acceptanceEditOpen = $event"
      @submit="handleSubmitEditAcceptance"
      @success="handleEditAcceptanceSuccess"
    />
  </div>
</template>

<script setup name="ProjectScheduleRecords">
import { listProjects } from "@/api/evs/projects"
import { listProjectSchedules } from "@/api/evs/projectSchedules"
import { listProjectScheduleRecords, addProjectScheduleRecords, delProjectScheduleRecords, updateProjectScheduleRecords } from "@/api/evs/projectScheduleRecords"
import ProjectScheduleList from "./components/ProjectScheduleList.vue"
import ProjectScheduleDetail from "./components/ProjectScheduleDetail.vue"
import AcceptanceReportDialog from "./components/AcceptanceReportDialog.vue"
import { Plus } from "@element-plus/icons-vue"
import { getToken } from "@/utils/auth"

const { proxy } = getCurrentInstance()

// 图片上传配置
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + '/common/upload')
const uploadHeaders = ref({ Authorization: "Bearer " + getToken() })

// 响应式数据
const loading = ref(true)
const showSearch = ref(true)
const selectedProject = ref(null)
const inProgressProjects = ref([])
const scheduleItems = ref([])
const scheduleLoading = ref(false)

// 验收上报相关
const acceptanceDialogOpen = ref(false)
const currentScheduleItem = ref(null)

// 编辑验收记录相关
const acceptanceEditOpen = ref(false)
const currentEditRecord = ref(null)

// 子组件引用
const scheduleDetailRef = ref(null)
const acceptanceDialogRef = ref(null)
const acceptanceEditDialogRef = ref(null)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 100,
    name: null,
    status: 'IN_PROGRESS' // 默认只显示进行中的项目
  }
})

const { queryParams } = toRefs(data)

/** 统一错误处理函数 */
function handleError(error, context = '', showToast = true) {
  const timestamp = new Date().toISOString()

  console.error(`${context}错误:`, {
    error: error,
    message: error.message,
    timestamp: timestamp
  })

  if (showToast) {
    let errorMsg = `${context}失败`

    if (error.response) {
      // 服务器返回的错误
      errorMsg = error.response.data?.msg || error.response.data?.message || `服务器错误(${error.response.status})`
    } else if (error.request) {
      // 网络错误
      errorMsg = '网络连接失败，请检查网络后重试'
    } else if (error.msg) {
      // 业务逻辑错误
      errorMsg = error.msg
    } else if (error.message) {
      // 其他错误
      errorMsg = error.message
    }

    proxy.$modal.msgError(errorMsg)
  }

  // 返回处理后的错误信息
  return {
    success: false,
    error: error,
    message: errorMsg || `${context}失败`,
    timestamp: timestamp
  }
}

/** 重置所有loading状态 */
function resetAllLoadingStates() {
  loading.value = false
  scheduleLoading.value = false
  // acceptanceDialogRef.value?.setSaving(false) // 不再需要，对话框自行管理
}

/** 查询项目列表 */
function getList() {
  loading.value = true

  listProjects({
    ...queryParams.value,
    includeScheduleInfo: true
  }).then(response => {
    inProgressProjects.value = (response.rows || []).filter(project => {
      return project.status === 'IN_PROGRESS' || project.status === 'PLANNED'
    })
    loading.value = false

    if (selectedProject.value && !inProgressProjects.value.find(p => p.id === selectedProject.value.id)) {
      selectedProject.value = null
      scheduleItems.value = []
    }

    if (inProgressProjects.value.length > 0) {
      const firstProject = inProgressProjects.value[0]
      selectedProject.value = firstProject
      loadProjectSchedules(firstProject.id)
    }
  }).catch(error => {
    handleError(error, '加载项目列表')
    resetAllLoadingStates()
  })
}

/** 选择项目 */
function handleProjectSelect(project) {
  selectedProject.value = project
  loadProjectSchedules(project.id)
}

/** 加载项目进度 */
function loadProjectSchedules(projectId) {
  if (!projectId) return

  scheduleLoading.value = true
  listProjectSchedules({ projectId, pageNum: 1, pageSize: 100 }).then(response => {
    scheduleItems.value = (response.rows || []).sort((a, b) => {
      return new Date(a.plannedStartDate) - new Date(b.plannedStartDate)
    })
    scheduleLoading.value = false
  }).catch(error => {
    handleError(error, '加载项目进度')
    scheduleLoading.value = false
  })
}

/** 验收上报 */
function handleAcceptanceReport(item) {
  currentScheduleItem.value = item
  acceptanceDialogOpen.value = true
}

/** 处理验收提交 - 简化为兼容性函数 */
function handleSubmitAcceptance(recordData) {
  // 对话框组件已自行处理API调用，这里主要用于兼容性
  console.log('handleSubmitAcceptance被调用，但API调用已由对话框组件处理')
  // 此函数保留是为了向后兼容，实际逻辑已在AcceptanceReportDialog内部处理
}

/** 成功回调处理 - 对话框组件已自动处理API调用 */
function handleAcceptanceSuccess(recordData) {
  console.log('收到验收成功回调，记录数据:', recordData)

  // 对话框已自动关闭，只需处理数据刷新
  if (selectedProject.value) {
    // 刷新项目进度数据
    loadProjectSchedules(selectedProject.value.id)

    // 延时刷新验收记录，确保后端数据已更新
    setTimeout(() => {
      console.log('开始刷新验收记录')
      scheduleDetailRef.value?.refreshAcceptanceRecords()
    }, 300) // 适当延时确保数据一致性
  }
}

/** 错误回调处理 - 对话框组件已自动处理错误显示 */
function handleAcceptanceError(error) {
  console.error('收到验收错误回调:', error)
  // 对话框已自动处理错误显示，这里可以添加额外逻辑
}

/** Loading状态变化处理 - 可选的全局loading管理 */
function handleLoadingChange(isLoading) {
  console.log('Loading状态变化:', isLoading)
  // 可以在这里添加全局loading状态管理
  // 例如：更新页面级loading状态
}

/** 编辑验收记录 */
function handleEditAcceptance(record) {
  // 保存当前编辑的记录
  currentEditRecord.value = record
  acceptanceEditOpen.value = true
}

/** 删除验收记录 */
function handleDeleteAcceptance(record) {
  // 删除逻辑已在ProjectScheduleDetail的handleDeleteAcceptance中处理
  // 这里不需要实现，直接传递给父组件
  // 此函数保留是为了兼容API
  console.log('删除验收记录:', record)
}

/** 处理编辑验收提交 */
function handleSubmitEditAcceptance(recordData) {
  // 添加ID用于更新
  recordData.id = currentEditRecord.value.id

  updateProjectScheduleRecords(recordData).then(() => {
    proxy.$modal.msgSuccess('验收记录更新成功')
    acceptanceEditOpen.value = false
    if (selectedProject.value) {
      loadProjectSchedules(selectedProject.value.id)
      // 刷新验收记录
      scheduleDetailRef.value?.refreshAcceptanceRecords()
    }
  }).catch(error => {
    handleError(error, '验收记录更新')
    acceptanceDialogRef.value?.setSaving(false)
  })
}

/** 编辑验收成功回调 */
function handleEditAcceptanceSuccess() {
  acceptanceEditOpen.value = false
  if (selectedProject.value) {
    loadProjectSchedules(selectedProject.value.id)
    // 刷新验收记录
    scheduleDetailRef.value?.refreshAcceptanceRecords()
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.status = 'IN_PROGRESS'
  handleQuery()
}

// 初始化
getList()
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
  height: calc(100vh - 84px); // 减去顶部导航栏和padding
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;

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

.main-content-row {
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;

  :deep(.el-row) {
    display: flex;
    flex-wrap: nowrap;
    flex: 1;
    min-height: 0;
    width: 100%;
  }

  :deep(.el-col) {
    display: flex;
    flex-direction: column;
    min-height: 0;
    height: 100%;
  }

  .left-col,
  .right-col {
    display: flex;
    flex-direction: column;
    min-height: 0;
    height: 100%;
    overflow: hidden;
  }
}
</style>
