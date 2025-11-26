<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">质量检查</h2>
        <p class="page-subtitle">跟踪进行中项目的质量验收</p>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" class="search-form">
      <el-form-item label="项目名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入项目名称"
          clearable
          @keyup.enter="handleQuery"
          style="width: 180px;"
        />
      </el-form-item>

      <el-form-item label="项目状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择项目状态" clearable style="width: 130px">
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
              <div class="project-stats">
                <div class="stat-item">
                  <span class="stat-label">总检查</span>
                  <span class="stat-value stat-value-blue">{{ getTotalInspections(project) }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">通过</span>
                  <span class="stat-value stat-value-green">{{ getPassedInspections(project) }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">问题</span>
                  <span class="stat-value stat-value-red">{{ getFailedInspections(project) }}</span>
                </div>
              </div>
              <div v-if="getPendingIssuesCount(project) > 0" class="pending-issues-alert">
                <el-icon><Warning /></el-icon>
                <span>{{ getPendingIssuesCount(project) }}个问题待处理</span>
              </div>
            </div>
            <el-empty v-if="inProgressProjects.length === 0" description="暂无进行中的项目" :image-size="100" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：项目详情和质检记录 -->
      <el-col :span="16" class="right-col">
        <el-card shadow="never" v-if="selectedProject" class="project-detail-card">
          <!-- 固定区域：项目头部、统计卡片 -->
          <div class="project-detail-fixed">
            <!-- 项目头部信息 -->
            <div class="project-detail-header">
              <div class="project-title-section">
                <h3 class="project-title">{{ selectedProject.name }}-质检记录</h3>
                <el-button
                  type="primary"
                  icon="Plus"
                  @click="handleReportIssue"
                  v-hasPermi="['evs:qualityInspections:add']"
                >
                  问题上报
                </el-button>
              </div>
              <div class="project-info">
                <div class="info-item">
                  <span class="info-label">工地地址：</span>
                  <span class="info-value">{{ selectedProject.address || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">施工周期：</span>
                  <span class="info-value">
                    {{ proxy.parseTime(selectedProject.startDate, '{y}-{m}-{d}') }}至
                    {{ proxy.parseTime(selectedProject.endDate, '{y}-{m}-{d}') }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 统计卡片 -->
            <el-row :gutter="16" class="stat-cards-row">
              <el-col :span="6">
                <el-card shadow="never" class="stat-card stat-card-blue">
                  <div class="stat-content">
                    <div class="stat-label">总检查</div>
                    <div class="stat-value">{{ getTotalInspections(selectedProject) }}次</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never" class="stat-card stat-card-green">
                  <div class="stat-content">
                    <div class="stat-label">通过率</div>
                    <div class="stat-value">{{ getPassRate(selectedProject) }}%</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never" class="stat-card stat-card-red">
                  <div class="stat-content">
                    <div class="stat-label">不通过</div>
                    <div class="stat-value">{{ getFailedInspections(selectedProject) }}次</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="never" class="stat-card stat-card-orange">
                  <div class="stat-content">
                    <div class="stat-label">待检查</div>
                    <div class="stat-value">{{ getPendingInspections(selectedProject) }}项</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- 滚动区域：质检记录时间轴 -->
          <div class="timeline-section timeline-scrollable">
            <div class="timeline-title">质检记录时间轴</div>
            <div v-loading="inspectionLoading" class="timeline-content">
              <el-timeline v-if="inspectionItems.length > 0">
                <el-timeline-item
                  v-for="item in inspectionItems"
                  :key="item.id"
                  :color="getTimelineColor(item.result)"
                  :icon="getTimelineIcon(item.result)"
                  size="large"
                >
                  <div class="timeline-item-content">
                    <div class="timeline-item-header">
                      <span class="timeline-item-title">{{ item.inspectionType || item.title }}</span>
                      <el-tag :type="getTimelineTagType(item.result)" size="small">
                        {{ getTimelineStatusLabel(item.result) }}
                      </el-tag>
                    </div>
                    <div class="timeline-item-date">
                      <el-icon><Calendar /></el-icon>
                      <span>{{ proxy.parseTime(item.inspectionDate, '{y}-{m}-{d}') }}</span>
                      <span v-if="item.createdBy" style="margin-left: 16px;">
                        <el-icon><User /></el-icon>
                        <span>{{ item.createdBy }}</span>
                      </span>
                    </div>
                    <div v-if="item.result === 'QUALIFIED'" class="inspection-result-box inspection-result-pass">
                      <div class="result-text">质量检查通过</div>
                      <div class="result-description">{{ item.description || '施工质量符合标准,可进入下一阶段' }}</div>
                    </div>
                    <div v-else-if="item.result === 'UNQUALIFIED'" class="inspection-result-box inspection-result-fail">
                      <div class="result-text">发现{{ getIssuesCount(item) }}个质量问题</div>
                      <!-- 问题列表 -->
                      <div v-if="getIssuesList(item).length > 0" class="issues-list">
                        <div
                          v-for="(issue, index) in getIssuesList(item)"
                          :key="issue.id || index"
                          class="issue-item"
                        >
                          <el-collapse v-model="expandedIssues" accordion>
                            <el-collapse-item :name="issue.id || index">
                              <template #title>
                                <div class="issue-title">
                                  <el-icon style="color: #ff4d4f;"><WarningFilled /></el-icon>
                                  <span>{{ issue.title || '质量问题' }}</span>
                                  <el-tag type="danger" size="small" style="margin-left: 8px;">待整改</el-tag>
                                </div>
                              </template>
                              <div class="issue-details">
                                <div class="issue-detail-item">
                                  <span class="detail-label">问题描述：</span>
                                  <span class="detail-value">{{ issue.description || '暂无描述' }}</span>
                                </div>
                                <div class="issue-detail-item">
                                  <span class="detail-label">上报时间：</span>
                                  <span class="detail-value">{{ proxy.parseTime(issue.createdAt || item.inspectionDate, '{y}-{m}-{d} {h}:{i}') }}</span>
                                </div>
                                <div class="issue-detail-item">
                                  <span class="detail-label">上报人：</span>
                                  <span class="detail-value">{{ issue.reportedBy || issue.createdBy || item.createdBy || '未知' }}</span>
                                </div>
                                <el-button
                                  type="primary"
                                  size="small"
                                  style="margin-top: 12px;"
                                  @click="handleSubmitFix(issue)"
                                >
                                  提交整改
                                </el-button>
                              </div>
                            </el-collapse-item>
                          </el-collapse>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-timeline-item>
              </el-timeline>
              <el-empty v-else description="暂无质检记录" :image-size="100" />
            </div>
          </div>
        </el-card>

        <!-- 未选择项目时的提示 -->
        <el-card shadow="never" v-else class="project-detail-card project-detail-card-empty">
          <el-empty description="请从左侧选择一个项目查看详情" :image-size="120" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 问题上报对话框 -->
    <el-dialog
      v-model="issueDialogOpen"
      title="问题上报"
      width="600px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form ref="issueFormRef" :model="issueForm" :rules="issueRules" label-width="100px">
        <el-form-item label="问题标题" prop="title" required>
          <el-input
            v-model="issueForm.title"
            placeholder="例如: 墙面平整度问题"
            clearable
          />
        </el-form-item>
        <el-form-item label="问题描述" prop="description" required>
          <el-input
            v-model="issueForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述质量问题"
            :maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="质检类型" prop="inspectionType" required>
          <el-select v-model="issueForm.inspectionType" placeholder="请选择质检类型" style="width: 100%;">
            <el-option label="水电工程" value="水电工程" />
            <el-option label="泥瓦工程" value="泥瓦工程" />
            <el-option label="木工工程" value="木工工程" />
            <el-option label="油漆工程" value="油漆工程" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="现场照片">
          <el-upload
            v-model:file-list="issueForm.images"
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
        <el-form-item label="检查日期" prop="inspectionDate" required>
          <el-date-picker
            v-model="issueForm.inspectionDate"
            type="datetime"
            placeholder="请选择检查日期"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="issueDialogOpen = false">取 消</el-button>
          <el-button type="primary" @click="submitIssue" :loading="issueSaving">提交上报</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="dialogImageVisible" title="图片预览" width="800px" append-to-body>
      <img :src="dialogImageUrl" alt="预览图片" style="width: 100%;" />
    </el-dialog>
  </div>
</template>

<script setup name="QualityInspections">
import { listQualityInspections } from "@/api/evs/qualityInspections"
import { listProjectsWithCustomer } from "@/api/evs/projects"
import { listProjectSchedules } from "@/api/evs/projectSchedules"
import { addQualityInspections } from "@/api/evs/qualityInspections"
import { Calendar, Location, CircleCheck, Warning, WarningFilled, Plus, User } from "@element-plus/icons-vue"
import { getToken } from "@/utils/auth"
import { useProjectAuth } from '@/utils/projectAuth'
import { onMounted } from 'vue'

const { proxy } = getCurrentInstance()
const { decoration_project_status } = proxy.useDict('decoration_project_status')

// 权限控制
const { isAdmin, getUserProjectIds } = useProjectAuth()

// 图片上传配置
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + '/common/upload')
const uploadHeaders = ref({ Authorization: "Bearer " + getToken() })

// 响应式数据
const loading = ref(true)
const showSearch = ref(true)
const selectedProject = ref(null)
const inProgressProjects = ref([])
const inspectionItems = ref([])
const inspectionLoading = ref(false)
const projectInspectionsMap = ref(new Map()) // 存储每个项目的质检数据
const expandedIssues = ref([])

// 问题上报相关
const issueDialogOpen = ref(false)
const issueSaving = ref(false)
const dialogImageVisible = ref(false)
const dialogImageUrl = ref('')
const issueForm = ref({
  title: '',
  description: '',
  inspectionType: '',
  images: [],
  inspectionDate: ''
})
const issueRules = {
  title: [
    { required: true, message: '问题标题不能为空', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '问题描述不能为空', trigger: 'blur' },
    { max: 500, message: '问题描述不能超过500字', trigger: 'blur' }
  ],
  inspectionType: [
    { required: true, message: '请选择质检类型', trigger: 'change' }
  ],
  inspectionDate: [
    { required: true, message: '请选择检查日期', trigger: 'change' }
  ]
}

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 100,
    name: null,
    status: 'IN_PROGRESS', // 默认只显示进行中的项目
    includeCustomer: true
  }
})

const { queryParams } = toRefs(data)

/** 查询项目列表（带权限控制） */
async function getList() {
  loading.value = true

  try {
    // 根据用户权限构建查询参数
    const query_params = { ...queryParams.value }

    if (!isAdmin.value) {
      // 非管理员：只能查看自己关联的项目
      const userProjectIds = await getUserProjectIds()
      query_params.userProjectIds = userProjectIds
    }

    // 使用带客户信息的API
    const response = await listProjectsWithCustomer(query_params)

    // 筛选进行中的项目
    inProgressProjects.value = (response.rows || []).filter(project => {
      return project.status === 'IN_PROGRESS' || project.status === 'PLANNED'
    })

    // 如果当前选中的项目不在列表中，清空选择
    if (selectedProject.value && !inProgressProjects.value.find(p => p.id === selectedProject.value.id)) {
      selectedProject.value = null
      inspectionItems.value = []
    }

    // 为每个项目加载质检数据
    inProgressProjects.value.forEach(project => {
      loadProjectInspectionsForList(project.id)
    })

  } catch (error) {
    console.error('获取项目列表失败:', error)
    proxy.$modal.msgError('获取项目列表失败')
    inProgressProjects.value = []
    selectedProject.value = null
    inspectionItems.value = []
  } finally {
    loading.value = false
  }
}

/** 为列表加载项目质检数据（用于显示在左侧） */
function loadProjectInspectionsForList(projectId) {
  if (!projectId) return
  
  listQualityInspections({ projectId, pageNum: 1, pageSize: 100 }).then(response => {
    const inspections = response.rows || []
    projectInspectionsMap.value.set(projectId, inspections)
  }).catch(error => {
    console.error('加载项目质检数据失败:', error)
  })
}

/** 选择项目 */
function selectProject(project) {
  selectedProject.value = project
  loadProjectInspections(project.id)
}

/** 加载项目质检记录 */
function loadProjectInspections(projectId) {
  if (!projectId) return
  
  inspectionLoading.value = true
  listQualityInspections({ projectId, pageNum: 1, pageSize: 100 }).then(response => {
    inspectionItems.value = (response.rows || []).sort((a, b) => {
      // 按检查日期倒序排序
      const dateA = new Date(a.inspectionDate || a.createdAt)
      const dateB = new Date(b.inspectionDate || b.createdAt)
      return dateB - dateA
    })
    inspectionLoading.value = false
  }).catch(error => {
    console.error('加载项目质检记录失败:', error)
    proxy.$modal.msgError('加载项目质检记录失败')
    inspectionLoading.value = false
  })
}

/** 获取项目质检数据（用于计算） */
function getProjectInspections(project) {
  if (!project) return []
  // 如果当前选中的项目，使用inspectionItems
  if (selectedProject.value && project.id === selectedProject.value.id) {
    return inspectionItems.value
  }
  // 否则从map中获取
  return projectInspectionsMap.value.get(project.id) || []
}

/** 获取总检查数 */
function getTotalInspections(project) {
  if (!project) return 0
  const inspections = getProjectInspections(project)
  return inspections.filter(item => item.result !== 'PENDING').length
}

/** 获取通过数 */
function getPassedInspections(project) {
  if (!project) return 0
  const inspections = getProjectInspections(project)
  return inspections.filter(item => item.result === 'QUALIFIED').length
}

/** 获取不通过数 */
function getFailedInspections(project) {
  if (!project) return 0
  const inspections = getProjectInspections(project)
  return inspections.filter(item => item.result === 'UNQUALIFIED').length
}

/** 获取待检查数 */
function getPendingInspections(project) {
  if (!project) return 0
  const inspections = getProjectInspections(project)
  return inspections.filter(item => item.result === 'PENDING').length
}

/** 获取通过率 */
function getPassRate(project) {
  const total = getTotalInspections(project)
  if (total === 0) return 0
  const passed = getPassedInspections(project)
  return Math.round((passed / total) * 100)
}

/** 获取待处理问题数 */
function getPendingIssuesCount(project) {
  if (!project) return 0
  const inspections = getProjectInspections(project)
  let count = 0
  inspections.forEach(item => {
    if (item.result === 'UNQUALIFIED') {
      // 假设每个不通过的检查都有问题，实际应该从问题表获取
      count += getIssuesCount(item)
    }
  })
  return count
}

/** 获取问题数量 */
function getIssuesCount(inspection) {
  if (inspection.issues && Array.isArray(inspection.issues)) {
    return inspection.issues.filter(issue => issue.status !== 'RESOLVED' && issue.status !== 'CLOSED').length
  }
  // 如果没有issues字段，根据描述判断（简单处理）
  return inspection.result === 'UNQUALIFIED' ? 1 : 0
}

/** 获取问题列表 */
function getIssuesList(inspection) {
  if (inspection.issues && Array.isArray(inspection.issues)) {
    return inspection.issues
  }
  // 如果没有issues字段，从描述中创建一个虚拟问题（用于显示）
  if (inspection.result === 'UNQUALIFIED' && inspection.description) {
    return [{
      id: inspection.id + '_issue',
      title: inspection.title || '质量问题',
      description: inspection.description,
      createdAt: inspection.inspectionDate || inspection.createdAt,
      reportedBy: inspection.createdBy,
      status: 'OPEN'
    }]
  }
  return []
}

/** 获取时间轴颜色 */
function getTimelineColor(result) {
  const colorMap = {
    'QUALIFIED': '#52c41a',
    'UNQUALIFIED': '#ff4d4f',
    'PENDING': '#d9d9d9'
  }
  return colorMap[result] || '#d9d9d9'
}

/** 获取时间轴图标 */
function getTimelineIcon(result) {
  if (result === 'QUALIFIED') {
    return 'Check'
  } else if (result === 'UNQUALIFIED') {
    return 'Close'
  }
  return ''
}

/** 获取时间轴标签类型 */
function getTimelineTagType(result) {
  const typeMap = {
    'QUALIFIED': 'success',
    'UNQUALIFIED': 'danger',
    'PENDING': 'info'
  }
  return typeMap[result] || 'info'
}

/** 获取时间轴状态标签 */
function getTimelineStatusLabel(result) {
  const labelMap = {
    'QUALIFIED': '√ 通过',
    'UNQUALIFIED': '× 不通过',
    'PENDING': '待检查'
  }
  return labelMap[result] || '待检查'
}

/** 问题上报 */
function handleReportIssue() {
  if (!selectedProject.value) {
    proxy.$modal.msgWarning('请先选择项目')
    return
  }
  
  // 获取当前时间，格式化为 YYYY-MM-DD HH:mm:ss
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  const defaultTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  
  issueForm.value = {
    title: '',
    description: '',
    inspectionType: '',
    images: [],
    inspectionDate: defaultTime
  }
  issueDialogOpen.value = true
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
    file.url = response.fileName
    file.response = response
    proxy.$modal.msgSuccess('图片上传成功')
  } else {
    proxy.$modal.msgError(response.msg || '图片上传失败')
    const index = issueForm.value.images.findIndex(img => img.uid === file.uid)
    if (index > -1) {
      issueForm.value.images.splice(index, 1)
    }
  }
}

/** 上传失败回调 */
function handleUploadError(err, file) {
  proxy.$modal.msgError('图片上传失败')
  const index = issueForm.value.images.findIndex(img => img.uid === file.uid)
  if (index > -1) {
    issueForm.value.images.splice(index, 1)
  }
}

/** 提交问题上报 */
function submitIssue() {
  proxy.$refs.issueFormRef.validate(valid => {
    if (!valid) return
    
    if (!selectedProject.value) {
      proxy.$modal.msgError('数据错误')
      return
    }
    
    // 检查是否有未上传完成的图片
    const hasUnuploadedImages = issueForm.value.images.some(img => img.raw && !img.url && !img.response)
    if (hasUnuploadedImages) {
      proxy.$modal.msgWarning('请等待图片上传完成后再提交')
      return
    }
    
    issueSaving.value = true
    
    // 提取所有图片URL
    const imageUrls = issueForm.value.images
      .map(img => {
        if (img.response && img.response.fileName) {
          return img.response.fileName
        }
        if (img.url) {
          if (img.url.startsWith('http')) {
            const baseUrl = import.meta.env.VITE_APP_BASE_API
            return img.url.replace(baseUrl, '').replace(/^\/+/, '')
          }
          return img.url
        }
        return null
      })
      .filter(url => url !== null)
    
    const inspectionData = {
      projectId: selectedProject.value.id,
      inspectionType: issueForm.value.inspectionType,
      title: issueForm.value.title,
      description: issueForm.value.description,
      result: 'UNQUALIFIED', // 问题上报默认为不通过
      inspectionDate: issueForm.value.inspectionDate,
      images: JSON.stringify(imageUrls),
      remarks: '问题上报'
    }
    
    addQualityInspections(inspectionData).then(() => {
      proxy.$modal.msgSuccess('问题上报成功')
      issueDialogOpen.value = false
      issueSaving.value = false
      // 重新加载质检记录
      loadProjectInspections(selectedProject.value.id)
      // 更新列表数据
      loadProjectInspectionsForList(selectedProject.value.id)
    }).catch(error => {
      proxy.$modal.msgError('问题上报失败：' + (error.msg || error.message))
      issueSaving.value = false
    })
  })
}

/** 提交整改 */
function handleSubmitFix(issue) {
  proxy.$modal.msgInfo('整改功能开发中...')
  // TODO: 实现整改功能
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
onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
  height: calc(100vh - 84px);
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
    height: 0;
    
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
    padding: 16px;
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

    .project-stats {
      display: flex;
      gap: 16px;
      margin-bottom: 12px;

      .stat-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;

        .stat-label {
          font-size: 12px;
          color: #999;
        }

        .stat-value {
          font-size: 16px;
          font-weight: 600;

          &.stat-value-blue {
            color: #1677ff;
          }

          &.stat-value-green {
            color: #52c41a;
          }

          &.stat-value-red {
            color: #ff4d4f;
          }
        }
      }
    }

    .pending-issues-alert {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 12px;
      background: #fff1f0;
      border: 1px solid #ffccc7;
      border-radius: 4px;
      font-size: 13px;
      color: #ff4d4f;

      .el-icon {
        font-size: 16px;
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
    height: 0;
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
      justify-content: space-between;
      align-items: center;
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

    &.stat-card-red {
      background: #fff1f0;
      border-color: #ffccc7;
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

    &.stat-card-red .stat-content .stat-value {
      color: #ff4d4f;
    }

    &.stat-card-orange .stat-content .stat-value {
      color: #fa8c16;
    }
  }

  .timeline-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;
    position: relative;
    height: 0;

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

      .timeline-item-date {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #999;
        margin-bottom: 12px;
      }

      .inspection-result-box {
        padding: 12px;
        border-radius: 6px;
        margin-top: 8px;

        &.inspection-result-pass {
          background: #f6ffed;
          border: 1px solid #b7eb8f;

          .result-text {
            font-size: 14px;
            font-weight: 600;
            color: #52c41a;
            margin-bottom: 4px;
          }

          .result-description {
            font-size: 13px;
            color: #666;
          }
        }

        &.inspection-result-fail {
          background: #fff1f0;
          border: 1px solid #ffccc7;

          .result-text {
            font-size: 14px;
            font-weight: 600;
            color: #ff4d4f;
            margin-bottom: 8px;
          }
        }
      }

      .issues-list {
        margin-top: 12px;

        .issue-item {
          margin-bottom: 8px;

          .issue-title {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 14px;
            font-weight: 500;
            color: #303133;
          }

          .issue-details {
            padding: 12px;
            background: #fff;
            border-radius: 4px;
            margin-top: 8px;

            .issue-detail-item {
              margin-bottom: 8px;
              font-size: 13px;

              .detail-label {
                color: #666;
                margin-right: 8px;
              }

              .detail-value {
                color: #303133;
              }
            }
          }
        }
      }
    }
  }
}
</style>
