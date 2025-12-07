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
          </div>

          <!-- 质量问题面板 -->
          <div class="quality-issues-section">
            <QualityIssuesPanel
              :issues="allIssues"
              :issue-fixes-map="issueFixesMap"
              :loading="inspectionLoading"
              @submit-fix="handleSubmitFix"
              @refresh="loadProjectInspections(selectedProject?.id)"
            />
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
        <el-form-item label="问题分类" prop="category" required>
          <el-select v-model="issueForm.category" placeholder="请选择问题分类" style="width: 100%;">
            <el-option label="一般问题" value="GENERAL" />
            <el-option label="红线问题" value="CRITICAL" />
            <el-option label="紧急问题" value="URGENT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题位置" prop="location">
          <el-input
            v-model="issueForm.location"
            placeholder="请输入问题具体位置（如：主卧墙面、厨房水管等）"
            clearable
          />
        </el-form-item>
        <el-form-item label="整改期限" prop="dueDate">
          <el-date-picker
            v-model="issueForm.dueDate"
            type="date"
            placeholder="请选择整改期限"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="现场照片" prop="images">
          <ImageUploadCard
            ref="uploadRef"
            v-model="issueForm.images"
            :upload-url="uploadUrl"
            :upload-headers="uploadHeaders"
            :max-count="20"
            :max-size="10"
            tip-text="(最多20张，支持多选)"
            @success="handleUploadSuccess"
            @error="handleUploadError"
            @upload-status-change="handleUploadStatusChange"
          />
          <!-- 上传状态提示 -->
          <div v-if="uploadStatus.totalFiles > 0" class="upload-status-tip">
            <el-tag
                :type="uploadStatus.isAllUploaded ? 'success' : 'warning'"
                size="small"
            >
              <el-icon><Check v-if="uploadStatus.isAllUploaded" /><Loading v-else /></el-icon>
              {{ uploadStatus.isAllUploaded ? '图片上传完成' : `正在上传图片 (${uploadStatus.uploadedFiles}/${uploadStatus.totalFiles})` }}
            </el-tag>
            <span v-if="!uploadStatus.isAllUploaded" class="upload-hint">请等待图片上传完成后再提交</span>
          </div>
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
          <el-button
          type="primary"
          @click="submitIssue"
          :loading="issueSaving"
          :disabled="!uploadStatus.isAllUploaded"
        >
          提交上报
        </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 整改提交对话框 -->
    <FixSubmissionDialog
      v-model:visible="fixDialogOpen"
      :issue="currentIssue"
      :upload-url="uploadUrl"
      @success="handleFixSuccess"
      @error="handleFixError"
      @refresh="loadProjectInspections(selectedProject?.id)"
    />

    <!-- 图片预览对话框 -->
    <el-dialog v-model="dialogImageVisible" title="图片预览" width="800px" append-to-body>
      <img :src="dialogImageUrl" alt="预览图片" style="width: 100%;" />
    </el-dialog>

    <!-- 整改历史抽屉 -->
    <QualityFixesDrawer
      v-model:visible="fixesDrawerOpen"
      :issue="currentIssueForDrawer"
      :upload-url="uploadUrl"
      :view-mode="true"
      @fix-success="handleDrawerFixSuccess"
      @fix-error="handleDrawerFixError"
    />
  </div>
</template>

<script setup name="QualityInspections">
import { listQualityInspections } from "@/api/evs/qualityInspections"
import { getQualityInspectionsWithIssues } from "@/api/evs/qualityInspections"
import { listProjectsWithMembers } from "@/api/evs/projects"
import { addQualityInspections } from "@/api/evs/qualityInspections"
import { addQualityIssues, listQualityIssues } from "@/api/evs/qualityIssues"
import { addQualityFixes, getQualityFixesByIssueId } from "@/api/evs/qualityFixes"
import { Calendar, Location, CircleCheck, Plus, Check, Loading } from "@element-plus/icons-vue"
import { getToken } from "@/utils/auth"
import FixSubmissionDialog from './components/FixSubmissionDialog.vue'
import QualityFixesDrawer from './components/QualityFixesDrawer.vue'
import ImageUploadCard from '@/components/ImageUploadCard/index.vue'
import QualityIssuesPanel from './components/QualityIssuesPanel.vue'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const { decoration_project_status } = proxy.useDict('decoration_project_status')

// 用户存储
const userStore = useUserStore()

// 上传状态管理
const uploadStatus = ref({
  isAllUploaded: true,
  totalFiles: 0,
  uploadedFiles: 0
})

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
const inspectionIssuesMap = ref(new Map()) // 存储质检对应的问题列表
const issueFixesMap = ref(new Map()) // 存储问题对应的整改记录列表

// 计算所有问题列表（从所有质检记录中提取）
const allIssues = computed(() => {
  const issues = []
  inspectionIssuesMap.value.forEach((issueList) => {
    issues.push(...issueList)
  })
  // 按创建时间倒序排序
  return issues.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
})

// 问题上报相关
const issueDialogOpen = ref(false)
const issueSaving = ref(false)
const dialogImageVisible = ref(false)
const dialogImageUrl = ref('')

// 整改相关
const fixDialogOpen = ref(false)
const currentIssue = ref(null)
const fixesDrawerOpen = ref(false)
const currentIssueForDrawer = ref(null)

// 上传组件引用
const uploadRef = ref(null)

// 上传状态变化回调
function handleUploadStatusChange(status) {
  uploadStatus.value = status
  console.log('上传状态变化:', status)
}

const issueForm = ref({
  title: '',
  description: '',
  inspectionType: '',
  category: '',
  location: '',
  dueDate: '',
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
  category: [
    { required: true, message: '请选择问题分类', trigger: 'change' }
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
    status: null // 不在后端过滤状态，由前端过滤（确保普通用户能获取到自己参与的项目）
  }
})

const { queryParams } = toRefs(data)

/** 查询项目列表（采用项目管理架构） */
function getList() {
  loading.value = true

  // 调用带成员权限过滤的项目API，后端自动处理权限过滤
  listProjectsWithMembers(queryParams.value).then(response => {
    // 兼容不同的返回格式（rows 或 data）
    const rows = response.rows || response.data || []
    console.log('[质量检测] 获取到项目列表:', rows.length, '条记录')
    if (rows.length > 0) {
      console.log('[质量检测] 项目状态分布:', rows.map(p => ({ name: p.name, status: p.status })))
    }
    
    // 筛选进行中的项目（忽略大小写）
    inProgressProjects.value = rows.filter(project => {
      const status = (project.status || '').toUpperCase()
      return status === 'IN_PROGRESS' || status === 'PLANNED'
    })
    console.log('[质量检测] 过滤后进行中的项目:', inProgressProjects.value.length, '条')

    // 如果当前选中的项目不在列表中，清空选择
    if (selectedProject.value && !inProgressProjects.value.find(p => p.id === selectedProject.value.id)) {
      selectedProject.value = null
      inspectionItems.value = []
    }

    loading.value = false
  }).catch(error => {
    console.error('获取项目列表失败:', error)
    proxy.$modal.msgError('获取项目列表失败')
    inProgressProjects.value = []
    selectedProject.value = null
    inspectionItems.value = []
    loading.value = false
  })
}

/** 为列表加载项目质检数据（用于显示在左侧）- 当前未使用，保留以备将来扩展 */
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
  
  // 清空之前项目的数据
  inspectionItems.value = []
  inspectionIssuesMap.value.clear()
  issueFixesMap.value.clear()
  
  // 加载新项目的数据
  loadProjectInspections(project.id)
}

/** 加载项目质检记录 */
async function loadProjectInspections(projectId) {
  if (!projectId) return

  inspectionLoading.value = true
  try {
    // 使用JOIN查询一次性获取质检记录和问题（解决N+1问题）
    const response = await getQualityInspectionsWithIssues(projectId)
    const inspections = response.data || response.rows || []

    // 将问题数据存储到缓存中
    inspections.forEach(inspection => {
      if (inspection.issues) {
        inspectionIssuesMap.value.set(inspection.id, inspection.issues)

        // 为每个问题加载整改记录
        inspection.issues.forEach(issue => {
          if (issue.id) {
            loadIssueFixes(issue.id)
          }
        })
      } else {
        inspectionIssuesMap.value.set(inspection.id, [])
      }
    })

    // 将数据存储到缓存
    projectInspectionsMap.value.set(projectId, inspections)

    inspectionItems.value = inspections.sort((a, b) => {
      // 按检查日期倒序排序
      const dateA = new Date(a.inspectionDate || a.createdAt)
      const dateB = new Date(b.inspectionDate || b.createdAt)
      return dateB - dateA
    })

  } catch (error) {
    console.error('加载项目质检记录失败:', error)
    proxy.$modal.msgError('加载项目质检记录失败')
  } finally {
    inspectionLoading.value = false
  }
}

/** 加载问题的整改记录 */
async function loadIssueFixes(issueId) {
  if (!issueId) return

  try {
    console.log('🔍 [FIXES] 正在加载问题的整改记录:', issueId)
    const response = await getQualityFixesByIssueId(issueId)
    const fixes = response.data || response.rows || []

    // 按创建时间倒序排序
    fixes.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))

    issueFixesMap.value.set(issueId, fixes)
    console.log('🔍 [FIXES] 已加载整改记录:', { issueId, count: fixes.length, fixes })
  } catch (error) {
    console.error('加载整改记录失败:', error)
    issueFixesMap.value.set(issueId, [])
  }
}

/** 获取问题的整改记录列表 */
function getIssueFixes(issueId) {
  if (!issueId) return []
  return issueFixesMap.value.get(issueId) || []
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

/** 获取问题数量（从缓存中获取） */
function getIssuesCount(inspection) {
  if (!inspection.id) return 0

  const issues = inspectionIssuesMap.value.get(inspection.id) || []
  return issues.filter(issue => issue.status !== 'RESOLVED' && issue.status !== 'CLOSED').length
}

/** 获取问题列表（从缓存中获取） */
function getIssuesList(inspection) {
  if (!inspection.id) return []

  return inspectionIssuesMap.value.get(inspection.id) || []
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

  // 计算默认整改期限（7天后）
  const defaultDueDate = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
  const dueDateYear = defaultDueDate.getFullYear()
  const dueDateMonth = String(defaultDueDate.getMonth() + 1).padStart(2, '0')
  const dueDateDay = String(defaultDueDate.getDate()).padStart(2, '0')
  const defaultDueDateStr = `${dueDateYear}-${dueDateMonth}-${dueDateDay}`

  issueForm.value = {
    title: '',
    description: '',
    inspectionType: '',
    category: 'GENERAL',
    location: '',
    dueDate: defaultDueDateStr,
    images: [],
    inspectionDate: defaultTime
  }
  issueDialogOpen.value = true
}

/** 上传成功回调 - 简化版本 */
function handleUploadSuccess({ response, file }) {
  try {
    console.log('问题上报图片上传成功:', { response, file })
    // ImageUploadCard组件已处理大部分逻辑，这里只记录日志
  } catch (error) {
    console.error('问题上报图片上传回调处理失败:', error)
  }
}

/** 上传失败回调 - 简化版本 */
function handleUploadError({ message }) {
  try {
    console.error('问题上报图片上传失败:', message)
    // ImageUploadCard组件已处理错误提示和文件移除
  } catch (error) {
    console.error('问题上报图片上传错误回调处理失败:', error)
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

    // 检查图片上传状态
    if (!uploadStatus.value.isAllUploaded) {
      proxy.$modal.msgWarning('请等待图片上传完成后再提交')
      return
    }

    issueSaving.value = true

    // 使用ImageUploadCard组件提供的工具函数提取图片URL
    const imageUrls = uploadRef.value?.extractImageUrls(issueForm.value.images) || []

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

    // 先创建质检记录，然后创建问题记录
    addQualityInspections(inspectionData).then(async (inspectionResponse) => {
      // 获取创建的质检记录ID
      const inspectionId = inspectionResponse.data?.id || inspectionResponse.id

      if (!inspectionId) {
        throw new Error('质检记录创建失败，无法获取ID')
      }

      // 创建质量问题记录
      const issueData = {
        projectId: selectedProject.value.id,
        qualityInspectionId: inspectionId,
        title: issueForm.value.title,
        description: issueForm.value.description,
        category: issueForm.value.category || 'GENERAL',
        location: issueForm.value.location || '',
        images: JSON.stringify(imageUrls),
        status: 'OPEN',
        dueDate: issueForm.value.dueDate || null
      }

      return addQualityIssues(issueData)

    }).then(() => {
      proxy.$modal.msgSuccess('问题上报成功')
      issueDialogOpen.value = false
      issueSaving.value = false
      // 重新加载质检记录
      loadProjectInspections(selectedProject.value.id)
    }).catch(error => {
      console.error('问题上报失败:', error)
      proxy.$modal.msgError('问题上报失败：' + (error.msg || error.message))
      issueSaving.value = false
    })
  })
}

/** 提交整改 */
function handleSubmitFix(issue) {
  if (!issue) {
    proxy.$modal.msgError('数据错误')
    return
  }
  currentIssue.value = issue
  fixDialogOpen.value = true
}

/** 整改成功回调 */
function handleFixSuccess(fixData) {
  proxy.$modal.msgSuccess('整改提交成功')
  fixDialogOpen.value = false
  currentIssue.value = null
  // 主动刷新数据，确保问题状态及时更新
  if (selectedProject.value) {
    loadProjectInspections(selectedProject.value.id)
  }
}

/** 整改失败回调 */
function handleFixError(error) {
  console.error('整改提交失败:', error)
  proxy.$modal.msgError('整改提交失败：' + (error.msg || error.message))
}

/** 查看整改历史 */
function handleViewFixes(issue) {
  if (!issue) {
    proxy.$modal.msgError('数据错误')
    return
  }
  currentIssueForDrawer.value = issue
  fixesDrawerOpen.value = true
}

/** 查看问题详情 */
function handleViewDetails(issue) {
  if (!issue) {
    proxy.$modal.msgError('数据错误')
    return
  }
  currentIssueForDrawer.value = issue
  fixesDrawerOpen.value = true
}

/** 抽屉整改成功回调 */
function handleDrawerFixSuccess(fixData) {
  console.log('🔍 [MAIN] 抽屉整改成功:', fixData)
  // 主动刷新数据，确保问题状态及时更新
  if (selectedProject.value) {
    loadProjectInspections(selectedProject.value.id)
  }
}

/** 抽屉整改失败回调 */
function handleDrawerFixError(error) {
  console.error('抽屉整改提交失败:', error)
  // 错误信息已在抽屉组件中处理
}

/** 获取问题状态标签类型 */
function getIssueStatusType(status) {
  const typeMap = {
    'OPEN': 'danger',
    'IN_PROGRESS': 'warning',
    'RESOLVED': 'success',
    'CLOSED': 'info'
  }
  return typeMap[status] || 'info'
}

/** 获取问题状态文本 */
function getIssueStatusText(status) {
  const textMap = {
    'OPEN': '待处理',
    'IN_PROGRESS': '整改中',
    'RESOLVED': '已解决',
    'CLOSED': '已关闭'
  }
  return textMap[status] || '未知'
}

/** 获取问题图标颜色 */
function getIssueIconColor(status) {
  const colorMap = {
    'OPEN': '#ff4d4f',      // 红色 - 待处理需要关注
    'IN_PROGRESS': '#e6a23c', // 橙色 - 整改中
    'RESOLVED': '#52c41a',   // 绿色 - 已解决
    'CLOSED': '#909399'     // 灰色 - 已关闭
  }
  return colorMap[status] || '#ff4d4f'
}

/** 获取问题分类标签类型 */
function getIssueCategoryType(category) {
  const typeMap = {
    'GENERAL': 'info',
    'CRITICAL': 'danger',
    'URGENT': 'warning',
    'OTHER': ''
  }
  return typeMap[category] || ''
}

/** 获取问题分类文本 */
function getIssueCategoryText(category) {
  const textMap = {
    'GENERAL': '一般问题',
    'CRITICAL': '红线问题',
    'URGENT': '紧急问题',
    'OTHER': '其他'
  }
  return textMap[category] || '未分类'
}

/** 获取整改状态标签类型 */
function getFixStatusType(status) {
  const typeMap = {
    'OPEN': 'info',
    'IN_PROGRESS': 'warning',
    'RESOLVED': 'success',
    'CLOSED': 'info'
  }
  return typeMap[status] || 'info'
}

/** 获取整改状态文本 */
function getFixStatusText(status) {
  const textMap = {
    'OPEN': '未解决',
    'IN_PROGRESS': '解决中',
    'RESOLVED': '已解决',
    'CLOSED': '已关闭'
  }
  return textMap[status] || '未知'
}

/** 预览问题图片 */
function previewIssueImages(issue) {
  try {
    const images = JSON.parse(issue.images || '[]')
    if (images && images.length > 0) {
      // 显示第一张图片
      const firstImage = images[0]
      dialogImageUrl.value = firstImage.startsWith('http') ? firstImage : import.meta.env.VITE_APP_BASE_API + firstImage
      dialogImageVisible.value = true
    } else {
      proxy.$modal.msgWarning('该问题暂无图片')
    }
  } catch (error) {
    console.error('解析图片数据失败:', error)
    proxy.$modal.msgError('图片数据格式错误')
  }
}

/** 预览整改图片 */
function previewFixImages(fix) {
  try {
    const images = JSON.parse(fix.images || '[]')
    if (images && images.length > 0) {
      // 显示第一张图片
      const firstImage = images[0]
      dialogImageUrl.value = firstImage.startsWith('http') ? firstImage : import.meta.env.VITE_APP_BASE_API + firstImage
      dialogImageVisible.value = true
    } else {
      proxy.$modal.msgWarning('该整改记录暂无图片')
    }
  } catch (error) {
    console.error('解析整改图片数据失败:', error)
    proxy.$modal.msgError('图片数据格式错误')
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

    .project-status-hint {
      display: flex;
      align-items: center;
      justify-content: flex-end;
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

  .inspection-overview-section {
    margin-bottom: 0;
    padding: 12px 16px;
    background: #fafafa;
    border-radius: 8px;

    .overview-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .overview-label {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
      }

      .overview-percentage {
        font-size: 14px;
        font-weight: 600;
        color: #1677ff;
      }
    }

    .overview-footer {
      text-align: center;
      font-size: 12px;
      color: #666;
      margin-top: 4px;
    }
  }

  .quality-issues-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;
    position: relative;
    height: 0;
  }
}

// 上传状态提示样式
.upload-status-tip {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;

  .el-tag {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .upload-hint {
    color: #e6a23c;
    font-size: 12px;
  }
}
</style>
