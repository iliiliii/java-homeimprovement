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
              <div class="project-progress-info">
                <el-progress
                  :percentage="getPassRate(project)"
                  :stroke-width="8"
                  :show-text="true"
                  :format="(percentage) => `${percentage}%`"
                >
                  <div class="progress-summary">
                    已检查 {{ getTotalInspections(project) }} · 不通过 {{ getFailedInspections(project) }} · 问题 {{ getPendingIssuesCount(project) }}
                  </div>
                </el-progress>
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

            <!-- 质检概览 -->
            <div class="inspection-overview-section">
              <div class="overview-header">
                <span class="overview-label">质检概览</span>
                <span class="overview-percentage">{{ getPassRate(selectedProject) }}%</span>
              </div>
              <el-progress
                :percentage="getPassRate(selectedProject)"
                :stroke-width="12"
                :show-text="false"
                style="margin-bottom: 8px;"
              />
              <div class="overview-footer">
                <span>{{ getPassRate(selectedProject) }}% 通过率 · 已检查 {{ getTotalInspections(selectedProject) }} · 不通过 {{ getFailedInspections(selectedProject) }} · 问题 {{ getPendingIssuesCount(selectedProject) }}</span>
              </div>
            </div>
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
                                  <el-tag :type="getIssueStatusType(issue.status)" size="small" style="margin-left: 8px;">
                                    {{ getIssueStatusText(issue.status) }}
                                  </el-tag>
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
                                <div class="issue-detail-item" v-if="issue.dueDate">
                                  <span class="detail-label">整改期限：</span>
                                  <span class="detail-value">{{ proxy.parseTime(issue.dueDate, '{y}-{m}-{d}') }}</span>
                                </div>
                                <div class="issue-detail-item" v-if="issue.location">
                                  <span class="detail-label">问题位置：</span>
                                  <span class="detail-value">{{ issue.location }}</span>
                                </div>
                                <div class="issue-detail-item">
                                  <span class="detail-label">问题分类：</span>
                                  <el-tag :type="getIssueCategoryType(issue.category)" size="small">
                                    {{ getIssueCategoryText(issue.category) }}
                                  </el-tag>
                                </div>
                                <div class="issue-actions" style="margin-top: 12px;">
                                  <el-button
                                    v-if="issue.status === 'OPEN' || issue.status === 'IN_PROGRESS'"
                                    type="primary"
                                    size="small"
                                    @click="handleSubmitFix(issue)"
                                  >
                                    提交整改
                                  </el-button>
                                  <el-button
                                    v-if="issue.status === 'RESOLVED'"
                                    type="success"
                                    size="small"
                                    disabled
                                  >
                                    已完成整改
                                  </el-button>
                                  <el-button
                                    v-if="issue.images && JSON.parse(issue.images || '[]').length > 0"
                                    type="default"
                                    size="small"
                                    @click="previewIssueImages(issue)"
                                  >
                                    查看图片
                                  </el-button>
                                </div>
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
  </div>
</template>

<script setup name="QualityInspections">
import { listQualityInspections } from "@/api/evs/qualityInspections"
import { listProjectsWithCustomer } from "@/api/evs/projects"
import { listProjectSchedules } from "@/api/evs/projectSchedules"
import { addQualityInspections } from "@/api/evs/qualityInspections"
import { addQualityIssues, listQualityIssues } from "@/api/evs/qualityIssues"
import { addQualityFixes } from "@/api/evs/qualityFixes"
import { Calendar, Location, CircleCheck, Warning, WarningFilled, Plus, User, Check, Loading } from "@element-plus/icons-vue"
import { getToken } from "@/utils/auth"
import { useProjectAuth } from '@/utils/projectAuth'
import { onMounted } from 'vue'
import FixSubmissionDialog from './components/FixSubmissionDialog.vue'
import ImageUploadCard from '@/components/ImageUploadCard/index.vue'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const { decoration_project_status } = proxy.useDict('decoration_project_status')

// 权限控制
const { isAdmin, getUserProjectIds } = useProjectAuth()

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

// 问题上报相关
const issueDialogOpen = ref(false)
const issueSaving = ref(false)
const dialogImageVisible = ref(false)
const dialogImageUrl = ref('')

// 整改相关
const fixDialogOpen = ref(false)
const currentIssue = ref(null)

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
async function loadProjectInspections(projectId) {
  if (!projectId) return

  inspectionLoading.value = true
  try {
    // 加载质检记录
    const response = await listQualityInspections({ projectId, pageNum: 1, pageSize: 100 })
    const inspections = response.rows || []

    // 为每个质检记录加载对应的问题
    const inspectionsWithIssues = await Promise.all(
      inspections.map(async (inspection) => {
        try {
          const issuesResponse = await listQualityIssues({
            qualityInspectionId: inspection.id,
            pageNum: 1,
            pageSize: 100
          })
          const issues = issuesResponse.rows || []
          inspectionIssuesMap.value.set(inspection.id, issues)
          return inspection
        } catch (error) {
          console.error(`加载质检记录 ${inspection.id} 的问题失败:`, error)
          inspectionIssuesMap.value.set(inspection.id, [])
          return inspection
        }
      })
    )

    inspectionItems.value = inspectionsWithIssues.sort((a, b) => {
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
      // 更新列表数据
      loadProjectInspectionsForList(selectedProject.value.id)
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
}

/** 整改失败回调 */
function handleFixError(error) {
  console.error('整改提交失败:', error)
  proxy.$modal.msgError('整改提交失败：' + (error.msg || error.message))
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
