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
      <!-- <el-form-item label="项目状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择项目状态" clearable>
          <el-option
            v-for="dict in decoration_project_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item> -->
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 主要内容区域：左右布局 -->
    <el-row :gutter="20" v-loading="loading" class="main-content-row">
      <!-- 左侧：进行中的项目列表 -->
      <el-col :span="8" class="left-col">
        <el-card shadow="never" class="project-list-card">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon style="color: #52c41a;"><CircleCheck /></el-icon>
              <span style="font-weight: 600;">进行中的项目</span>
              <el-badge :value="inProgressProjects.length" class="item" />
            </div>
          </template>
          <div class="project-list">
            <div
              v-for="project in inProgressProjects"
              :key="project.id"
              class="project-item"
              :class="{ active: selectedProject?.id === project.id }"
              @click="selectProject(project)"
            >
              <div class="project-item-header">
                <div class="project-name">{{ project.name }}</div>
                <dict-tag :options="decoration_project_status" :value="project.status" size="small" />
              </div>
              <div class="project-address">
                <el-icon><Location /></el-icon>
                <span>{{ project.address || '未设置地址' }}</span>
              </div>
              <div class="project-progress-info">
                <el-progress
                  :percentage="project.progressRate || 0"  <!-- 直接使用后端数据 -->
                  :stroke-width="8"
                  :show-text="true"
                  :format="(percentage) => `${percentage}%`"
                >
                <div class="progress-summary">
                  已完成 {{ project.completedSchedules || 0 }}/{{ project.totalSchedules || 0 }} · 进行中 {{ project.inProgressSchedules || 0 }}
                </div>
                </el-progress>
              </div>
            </div>
            <el-empty v-if="inProgressProjects.length === 0" description="暂无进行中的项目" :image-size="100" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：项目详情和进度 -->
      <el-col :span="16" class="right-col">
        <el-card shadow="never" v-if="selectedProject" class="project-detail-card">
          <!-- 固定区域：项目头部、统计卡片、整体进度条 -->
          <div class="project-detail-fixed">
            <!-- 项目头部信息 -->
            <div class="project-detail-header">
              <div class="project-title-section">
                <h3 class="project-title">{{ selectedProject.name }}</h3>
                <dict-tag :options="decoration_project_status" :value="selectedProject.status" />
              </div>
              <div class="project-info">
                <div class="info-item">
                  <span class="info-label">工地地址：</span>
                  <span class="info-value">{{ selectedProject.address || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">开工日期：</span>
                  <span class="info-value">{{ proxy.parseTime(selectedProject.startDate, '{y}-{m}-{d}') }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">预计完工：</span>
                  <span class="info-value">{{ proxy.parseTime(selectedProject.endDate, '{y}-{m}-{d}') }}</span>
                </div>
              </div>
            </div>

            <!-- 进度统计卡片 -->
            <el-row :gutter="16" class="stat-cards-row">
              <el-col :span="8">
                <el-card shadow="never" class="stat-card stat-card-blue">
                  <div class="stat-content">
                    <div class="stat-label">总进度</div>
                    <div class="stat-value">{{ getProjectProgress(selectedProject) }}%</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="never" class="stat-card stat-card-green">
                  <div class="stat-content">
                    <div class="stat-label">已完成</div>
                    <div class="stat-value">{{ getCompletedCount(selectedProject) }}/{{ getTotalCount(selectedProject) }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="never" class="stat-card stat-card-orange">
                  <div class="stat-content">
                    <div class="stat-label">进行中</div>
                    <div class="stat-value">{{ getInProgressCount(selectedProject) }}</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 整体进度条 -->
            <div class="overall-progress-section">
              <div class="progress-header">
                <span class="progress-label">整体进度</span>
                <span class="progress-percentage">{{ getProjectProgress(selectedProject) }}%</span>
              </div>
              <el-progress
                :percentage="getProjectProgress(selectedProject)"
                :stroke-width="12"
                :show-text="false"
                style="margin-bottom: 8px;"
              />
              <div class="progress-footer">
                <span>{{ getProjectProgress(selectedProject) }}%</span>
              </div>
            </div>
          </div>

          <!-- 滚动区域：施工进度时间轴 -->
          <div class="timeline-section timeline-scrollable">
            <div class="timeline-title">施工进度时间轴</div>
            <div v-loading="scheduleLoading" class="timeline-content">
              <el-timeline v-if="scheduleItems.length > 0">
                <el-timeline-item
                  v-for="item in scheduleItems"
                  :key="item.id"
                  :color="getTimelineColor(item.status)"
                  :icon="getTimelineIcon(item.status)"
                  size="large"
                >
                  <div class="timeline-item-content">
                    <div class="timeline-item-header">
                      <span class="timeline-item-title">
                        {{ getScheduleStageName(item.stage) }}
                      </span>
                      <el-tag :type="getTimelineTagType(item.status)" size="small">
                        {{ getTimelineStatusLabel(item.status) }}
                      </el-tag>
                    </div>
                    <div class="timeline-item-description">{{ item.description || '暂无描述' }}</div>
                    <div class="timeline-item-date">
                      <el-icon><Calendar /></el-icon>
                      <span>{{ proxy.parseTime(item.plannedStartDate, '{y}-{m}-{d}') }}</span>
                    </div>
                    <el-button
                      type="primary"
                      size="small"
                      style="margin-top: 8px;"
                      @click="handleAcceptanceReport(item)"
                    >
                      <el-icon><Plus /></el-icon>
                      验收上报
                    </el-button>
                  </div>
                </el-timeline-item>
              </el-timeline>
              <el-empty v-else description="暂无施工进度" :image-size="100" />
            </div>
          </div>
        </el-card>

        <!-- 未选择项目时的提示 -->
        <el-card shadow="never" v-else class="project-detail-card project-detail-card-empty">
          <el-empty description="请从左侧选择一个项目查看详情" :image-size="120" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 验收上报对话框 -->
    <el-dialog
      v-model="acceptanceDialogOpen"
      title="开工准备 - 验收上报"
      width="600px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form ref="acceptanceFormRef" :model="acceptanceForm" :rules="acceptanceRules" label-width="100px">
        <el-form-item label="验收标题" prop="title" required>
          <el-input
            v-model="acceptanceForm.title"
            placeholder="例如: 水电管路验收"
            clearable
          />
        </el-form-item>
        <el-form-item label="验收内容" prop="content" required>
          <el-input
            v-model="acceptanceForm.content"
            type="textarea"
            :rows="4"
            placeholder="请描述验收情况 (不超过100字)"
            :maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="现场照片">
          <el-upload
            v-model:file-list="acceptanceForm.images"
            :action="uploadUrl"
            list-type="picture-card"
            :auto-upload="true"
            :limit="20"
            :headers="uploadHeaders"
            :on-exceed="handleExceed"
            :on-preview="handlePictureCardPreview"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="handleBeforeUpload"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip" style="color: #999; font-size: 12px; margin-top: 8px;">
                (最多20张)
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="验收结果" prop="result" required>
          <el-radio-group v-model="acceptanceForm.result">
            <el-radio label="QUALIFIED">合格</el-radio>
            <el-radio label="UNQUALIFIED">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="验收时间" prop="acceptanceTime" required>
          <el-date-picker
            v-model="acceptanceForm.acceptanceTime"
            type="datetime"
            placeholder="请选择验收时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="验收人" prop="acceptor" required>
          <el-input
            v-model="acceptanceForm.acceptor"
            placeholder="请输入验收人姓名"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="acceptanceDialogOpen = false">取 消</el-button>
          <el-button type="primary" @click="submitAcceptance" :loading="acceptanceSaving">提交验收</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="dialogImageVisible" title="图片预览" width="800px" append-to-body>
      <img :src="dialogImageUrl" alt="预览图片" style="width: 100%;" />
    </el-dialog>
  </div>
</template>

<script setup name="ProjectScheduleRecords">
import { listProjects } from "@/api/evs/projects"
import { listProjectSchedules } from "@/api/evs/projectSchedules"
import { listProjectScheduleRecords, addProjectScheduleRecords } from "@/api/evs/projectScheduleRecords"
import { Calendar, Location, CircleCheck, Plus } from "@element-plus/icons-vue"
import { getToken } from "@/utils/auth"

const { proxy } = getCurrentInstance()
const { decoration_project_status, project_schedule } = proxy.useDict('decoration_project_status', 'project_schedule')

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
const projectProgressMap = ref(new Map()) // 存储每个项目的进度数据

// 验收上报相关
const acceptanceDialogOpen = ref(false)
const acceptanceSaving = ref(false)
const currentScheduleItem = ref(null)
const dialogImageVisible = ref(false)
const dialogImageUrl = ref('')
const acceptanceForm = ref({
  title: '',
  content: '',
  images: [],
  result: 'QUALIFIED',
  acceptanceTime: '',
  acceptor: ''
})
const acceptanceRules = {
  title: [
    { required: true, message: '验收标题不能为空', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '验收内容不能为空', trigger: 'blur' },
    { max: 100, message: '验收内容不能超过100字', trigger: 'blur' }
  ],
  result: [
    { required: true, message: '请选择验收结果', trigger: 'change' }
  ],
  acceptanceTime: [
    { required: true, message: '请选择验收时间', trigger: 'change' }
  ],
  acceptor: [
    { required: true, message: '验收人不能为空', trigger: 'blur' }
  ]
}

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 100,
    name: null,
    status: 'IN_PROGRESS' // 默认只显示进行中的项目
  }
})

const { queryParams } = toRefs(data)

/** 查询项目列表 */
function getList() {
  loading.value = true

  // ✅ 传递 includeScheduleInfo=true 参数
  listProjects({
    ...queryParams.value,
    includeScheduleInfo: true  // 新增：要求返回进度统计信息
  }).then(response => {
    // 筛选进行中的项目
    inProgressProjects.value = (response.rows || []).filter(project => {
      return project.status === 'IN_PROGRESS' || project.status === 'PLANNED'
    })
    loading.value = false

    // 如果当前选中的项目不在列表中，清空选择
    if (selectedProject.value && !inProgressProjects.value.find(p => p.id === selectedProject.value.id)) {
      selectedProject.value = null
      scheduleItems.value = []
    }

    // 只为第一个项目加载进度
    if (inProgressProjects.value.length > 0) {
      const firstProject = inProgressProjects.value[0]
      selectedProject.value = firstProject
      loadProjectSchedules(firstProject.id)
    }
  })
}

/** 为列表加载项目进度（用于显示在左侧） */
function loadProjectProgressForList(projectId) {
  if (!projectId) return
  
  listProjectSchedules({ projectId, pageNum: 1, pageSize: 100 }).then(response => {
    const schedules = response.rows || []
    projectProgressMap.value.set(projectId, schedules)
  }).catch(error => {
    console.error('加载项目进度失败:', error)
  })
}

/** 选择项目 */
function selectProject(project) {
  selectedProject.value = project
  loadProjectSchedules(project.id)
}

/** 加载项目进度 */
function loadProjectSchedules(projectId) {
  if (!projectId) return
  
  scheduleLoading.value = true
  listProjectSchedules({ projectId, pageNum: 1, pageSize: 100 }).then(response => {
    scheduleItems.value = (response.rows || []).sort((a, b) => {
      // 按计划开始日期排序
      return new Date(a.plannedStartDate) - new Date(b.plannedStartDate)
    })
    scheduleLoading.value = false
  }).catch(error => {
    console.error('加载项目进度失败:', error)
    proxy.$modal.msgError('加载项目进度失败')
    scheduleLoading.value = false
  })
}

/** 获取项目进度数据（用于计算） */
function getProjectSchedules(project) {
  if (!project) return []
  // 如果当前选中的项目，使用scheduleItems
  if (selectedProject.value && project.id === selectedProject.value.id) {
    return scheduleItems.value
  }
  // 否则从map中获取
  return projectProgressMap.value.get(project.id) || []
}

/** 获取项目进度百分比 */
function getProjectProgress(project) {
  if (!project) return 0
  const schedules = getProjectSchedules(project)
  if (!schedules.length) return 0
  const total = schedules.length
  const completed = schedules.filter(item => item.status === 'COMPLETED').length
  return total > 0 ? Math.round((completed / total) * 100) : 0
}

/** 获取已完成数量 */
function getCompletedCount(project) {
  if (!project) return 0
  const schedules = getProjectSchedules(project)
  return schedules.filter(item => item.status === 'COMPLETED').length
}

/** 获取进行中数量 */
function getInProgressCount(project) {
  if (!project) return 0
  const schedules = getProjectSchedules(project)
  return schedules.filter(item => item.status === 'IN_PROGRESS').length
}

/** 获取总数量 */
function getTotalCount(project) {
  if (!project) return 0
  const schedules = getProjectSchedules(project)
  return schedules.length
}

/** 获取时间轴颜色 */
function getTimelineColor(status) {
  const colorMap = {
    'COMPLETED': '#52c41a',
    'IN_PROGRESS': '#1677ff',
    'PLANNED': '#d9d9d9'
  }
  return colorMap[status] || '#d9d9d9'
}

/** 获取时间轴图标 */
function getTimelineIcon(status) {
  if (status === 'COMPLETED') {
    return 'Check'
  } else if (status === 'IN_PROGRESS') {
    return 'Clock'
  }
  return ''
}

/** 获取时间轴标签类型 */
function getTimelineTagType(status) {
  const typeMap = {
    'COMPLETED': 'success',
    'IN_PROGRESS': 'primary',
    'PLANNED': 'info'
  }
  return typeMap[status] || 'info'
}

/** 获取时间轴状态标签 */
function getTimelineStatusLabel(status) {
  const labelMap = {
    'COMPLETED': '已完成',
    'IN_PROGRESS': '进行中',
    'PLANNED': '待开始'
  }
  return labelMap[status] || '待开始'
}

/** 获取施工阶段名称 */
function getScheduleStageName(stage) {
  const stageDict = project_schedule.value.find(dict => dict.value === stage)
  return stageDict?.label || stage
}

/** 验收上报 */
function handleAcceptanceReport(item) {
  currentScheduleItem.value = item
  // 获取当前阶段名称作为默认标题
  const stageName = getScheduleStageName(item.stage)
  // 获取当前时间，格式化为 YYYY-MM-DD HH:mm:ss
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  const defaultTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  
  acceptanceForm.value = {
    title: `${stageName}验收`,
    content: '',
    images: [],
    result: 'QUALIFIED',
    acceptanceTime: defaultTime,
    acceptor: ''
  }
  acceptanceDialogOpen.value = true
}

/** 处理图片上传超出限制 */
function handleExceed() {
  proxy.$modal.msgWarning('最多只能上传20张图片')
}

/** 预览图片 */
function handlePictureCardPreview(file) {
  if (file.url) {
    dialogImageUrl.value = file.url.startsWith('http') ? file.url : import.meta.env.VITE_APP_BASE_API + file.url
  } else if (file.raw) {
    dialogImageUrl.value = URL.createObjectURL(file.raw)
  } else {
    dialogImageUrl.value = ''
  }
  dialogImageVisible.value = true
}

/** 上传前验证 */
function handleBeforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isImage) {
    proxy.$modal.msgError('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    proxy.$modal.msgError('上传图片大小不能超过 10MB!')
    return false
  }
  return true
}

/** 上传成功回调 */
function handleUploadSuccess(response, file) {
  if (response.code === 200) {
    // 将服务器返回的文件名保存到file对象
    file.url = response.fileName
    file.response = response
    proxy.$modal.msgSuccess('图片上传成功')
  } else {
    proxy.$modal.msgError(response.msg || '图片上传失败')
    // 从列表中移除失败的文件
    const index = acceptanceForm.value.images.findIndex(img => img.uid === file.uid)
    if (index > -1) {
      acceptanceForm.value.images.splice(index, 1)
    }
  }
}

/** 上传失败回调 */
function handleUploadError(err, file) {
  proxy.$modal.msgError('图片上传失败')
  // 从列表中移除失败的文件
  const index = acceptanceForm.value.images.findIndex(img => img.uid === file.uid)
  if (index > -1) {
    acceptanceForm.value.images.splice(index, 1)
  }
}

/** 提交验收 */
function submitAcceptance() {
  proxy.$refs.acceptanceFormRef.validate(valid => {
    if (!valid) return
    
    if (!currentScheduleItem.value || !selectedProject.value) {
      proxy.$modal.msgError('数据错误')
      return
    }
    
    // 检查是否有未上传完成的图片
    const hasUnuploadedImages = acceptanceForm.value.images.some(img => img.raw && !img.url && !img.response)
    if (hasUnuploadedImages) {
      proxy.$modal.msgWarning('请等待图片上传完成后再提交')
      return
    }
    
    acceptanceSaving.value = true
    
    // 根据验收结果设置完成度：合格=100%，不合格=0%
    const completionRate = acceptanceForm.value.result === 'QUALIFIED' ? 100 : 0
    
    // 组合描述信息：标题 + 内容
    const description = `${acceptanceForm.value.title}\n${acceptanceForm.value.content}`
    
    // 提取所有图片URL
    const imageUrls = acceptanceForm.value.images
      .map(img => {
        // 优先使用response中的fileName，其次使用url
        if (img.response && img.response.fileName) {
          return img.response.fileName
        }
        if (img.url) {
          // 如果是完整URL，提取路径部分
          if (img.url.startsWith('http')) {
            const baseUrl = import.meta.env.VITE_APP_BASE_API
            return img.url.replace(baseUrl, '').replace(/^\/+/, '')
          }
          return img.url
        }
        return null
      })
      .filter(url => url !== null)
    
    const recordData = {
      projectId: selectedProject.value.id,
      scheduleId: currentScheduleItem.value.id,
      recordType: 'ACCEPTANCE',
      completionRate: completionRate,
      description: description,
      images: JSON.stringify(imageUrls),
      // 扩展字段，如果后端支持的话
      acceptanceTitle: acceptanceForm.value.title,
      acceptanceContent: acceptanceForm.value.content,
      acceptanceResult: acceptanceForm.value.result,
      acceptanceTime: acceptanceForm.value.acceptanceTime,
      acceptor: acceptanceForm.value.acceptor
    }
    
    addProjectScheduleRecords(recordData).then(() => {
      proxy.$modal.msgSuccess('验收上报成功')
      acceptanceDialogOpen.value = false
      acceptanceSaving.value = false
      // 重新加载进度
      loadProjectSchedules(selectedProject.value.id)
    }).catch(error => {
      proxy.$modal.msgError('验收上报失败：' + (error.msg || error.message))
      acceptanceSaving.value = false
    })
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
  display: flex;              // ✅ 新增：Flexbox布局
  justify-content: start;     // ✅ 新增：水平左对齐
  align-items: flex-start;    // ✅ 新增：垂直顶部对齐（与用户页面一致）
  margin-bottom: 12px;        // ✅ 修改：统一为12px
  padding: 20px;             // ✅ 修改：统一为20px
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  :deep(.el-form-item) {      // ✅ 新增：表单项样式控制
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
    // height: 100%;
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

// 左侧项目列表
.project-list-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  overflow: hidden;

  :deep(.el-card__header) {
    flex-shrink: 0;
    padding: 16px;
  }

  :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    padding: 0;
    overflow-y: auto;
    overflow-x: hidden;
    position: relative;
    height: 0; // 关键：配合 flex: 1 让内容区域正确计算高度
    
    // 自定义滚动条样式 - 滚动条显示在外部
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
  }

  .project-list {
    flex: 1;
    min-height: 0;
    position: relative;
  }

  .project-item {
    padding: 16px;
    margin-bottom: 12px;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    background: #fff;

    &:hover {
      border-color: #1677ff;
      box-shadow: 0 2px 8px rgba(22, 119, 255, 0.1);
    }

    &.active {
      border-color: #1677ff;
      background: #f0f5ff;
      box-shadow: 0 2px 8px rgba(22, 119, 255, 0.2);
    }

    .project-item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .project-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .project-address {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #666;
      margin-bottom: 12px;
    }

    .project-progress-info {
      .progress-summary {
        margin-top: 8px;
        font-size: 12px;
        color: #999;
      }
    }
  }
}

// 右侧项目详情
.project-detail-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  overflow: hidden;

  :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    padding: 20px;
    box-sizing: border-box;
    overflow: hidden;
    position: relative;
    height: 0; // 关键：配合 flex: 1 让内容区域正确计算高度
  }

  &.project-detail-card-empty {
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  // 固定区域
  .project-detail-fixed {
    flex-shrink: 0;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e8e8e8;
    overflow: visible;
  }

  .project-detail-header {
    margin-bottom: 16px;
    padding-bottom: 0;
    border-bottom: none;

    .project-title-section {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;

      .project-title {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #303133;
      }
    }

    .project-info {
      display: flex;
      flex-wrap: wrap;
      gap: 24px;

      .info-item {
        font-size: 14px;

        .info-label {
          color: #666;
        }

        .info-value {
          color: #303133;
          font-weight: 500;
        }
      }
    }
  }

  .stat-cards-row {
    margin-bottom: 16px;
    
    :deep(.el-col) {
      display: flex;
      height: auto;
    }
  }

  .stat-card {
    flex: 1;
    text-align: center;
    padding: 12px 8px;
    height: auto;
    min-height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;

    :deep(.el-card__body) {
      padding: 0;
      width: 100%;
    }

    &.stat-card-blue {
      background: #e6f7ff;
      border-color: #91d5ff;
    }

    &.stat-card-green {
      background: #f6ffed;
      border-color: #b7eb8f;
    }

    &.stat-card-orange {
      background: #fff7e6;
      border-color: #ffd591;
    }

    .stat-content {
      width: 100%;
      .stat-label {
        font-size: 12px;
        color: #666;
        margin-bottom: 4px;
      }

      .stat-value {
        font-size: 18px;
        font-weight: 600;
        line-height: 1.2;
      }
    }

    &.stat-card-blue .stat-content .stat-value {
      color: #1677ff;
    }

    &.stat-card-green .stat-content .stat-value {
      color: #52c41a;
    }

    &.stat-card-orange .stat-content .stat-value {
      color: #fa8c16;
    }
  }

  .overall-progress-section {
    margin-bottom: 0;
    padding: 12px 16px;
    background: #fafafa;
    border-radius: 8px;

    .progress-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .progress-label {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
      }

      .progress-percentage {
        font-size: 14px;
        font-weight: 600;
        color: #1677ff;
      }
    }

    .progress-footer {
      text-align: center;
      font-size: 12px;
      color: #666;
      margin-top: 4px;
    }
  }

  .timeline-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;
    position: relative;
    height: 0; // 关键：配合 flex: 1 让时间轴区域正确计算高度

    &.timeline-scrollable {
      .timeline-title {
        flex-shrink: 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 12px;
        padding-bottom: 8px;
        border-bottom: 1px solid #e8e8e8;
      }

      .timeline-content {
        flex: 1;
        overflow-y: auto;
        overflow-x: hidden;
        min-height: 0;
        padding-right: 8px;
        margin-top: 0;
        height: 100%;
        
        // 自定义滚动条样式
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
      }
    }

    .timeline-item-content {
      background: #fafafa;
      padding: 16px;
      border-radius: 8px;
      margin-bottom: 12px;

      .timeline-item-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .timeline-item-title {
          font-size: 15px;
          font-weight: 600;
          color: #303133;
        }
      }

      .timeline-item-description {
        color: #666;
        font-size: 14px;
        margin-bottom: 8px;
      }

      .timeline-item-date {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #999;
      }
    }
  }
}
</style>
