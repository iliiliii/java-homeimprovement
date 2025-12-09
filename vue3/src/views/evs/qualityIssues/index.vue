<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="问题标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入问题标题"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="问题分类(GENERAL:一般问题、CRITICAL:红线问题、URGENT:紧急问题、OTHER:其他问题)" prop="category">
        <el-input
          v-model="queryParams.category"
          placeholder="请输入问题分类(GENERAL:一般问题、CRITICAL:红线问题、URGENT:紧急问题、OTHER:其他问题)"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="问题位置" prop="location">
        <el-input
          v-model="queryParams.location"
          placeholder="请输入问题位置"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="解决时间" prop="resolvedAt">
        <el-date-picker clearable
          v-model="queryParams.resolvedAt"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择解决时间">
        </el-date-picker>
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
          v-hasPermi="['evs:qualityIssues:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['evs:qualityIssues:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['evs:qualityIssues:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['evs:qualityIssues:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="qualityIssuesList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="问题ID" align="center" prop="id" />
      <el-table-column label="问题标题" align="center" prop="title" />
      <el-table-column label="问题分类(GENERAL:一般问题、CRITICAL:红线问题、URGENT:紧急问题、OTHER:其他问题)" align="center" prop="category" />
      <el-table-column label="问题位置" align="center" prop="location" />
      <el-table-column label="问题状态(OPEN:未解决、IN_PROGRESS:解决中、RESOLVED:已解决、CLOSED:已关闭)" align="center" prop="status" />
      <el-table-column label="解决时间" align="center" prop="resolvedAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.resolvedAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="整改期限" align="center" prop="dueDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.dueDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['evs:qualityIssues:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['evs:qualityIssues:remove']">删除</el-button>
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

    <!-- 添加或修改质量问题对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="qualityIssuesRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input v-model="form.projectId" placeholder="请输入项目ID" />
        </el-form-item>
        <el-form-item label="质检ID" prop="qualityInspectionId">
          <el-input v-model="form.qualityInspectionId" placeholder="请输入质检ID" />
        </el-form-item>
        <el-form-item label="问题标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入问题标题" />
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="问题分类(GENERAL:一般问题、CRITICAL:红线问题、URGENT:紧急问题、OTHER:其他问题)" prop="category">
          <el-input v-model="form.category" placeholder="请输入问题分类(GENERAL:一般问题、CRITICAL:红线问题、URGENT:紧急问题、OTHER:其他问题)" />
        </el-form-item>
        <el-form-item label="问题位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入问题位置" />
        </el-form-item>
        <el-form-item label="现场照片" prop="images">
          <ImageUploadCard
            v-model="imagesFileList"
            v-bind="getUploadProps()"
            @success="handleUploadSuccess"
            @error="handleUploadError"
            @upload-status-change="handleUploadStatusChange"
          />
          <!-- 上传状态提示 -->
          <div v-if="getStatusTip().show" class="upload-status-tip">
            <el-tag :type="getStatusTip().type" size="small">
              <el-icon><Loading v-if="!uploadStatus.isAllUploaded" /><Check v-else /></el-icon>
              {{ getStatusTip().message }}
            </el-tag>
            <span v-if="!uploadStatus.isAllUploaded && uploadStatus.totalFiles > 0" class="upload-hint">
              请等待图片上传完成后再提交
            </span>
          </div>
        </el-form-item>
        <el-form-item label="解决时间" prop="resolvedAt">
          <el-date-picker clearable
            v-model="form.resolvedAt"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择解决时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="整改期限" prop="dueDate">
          <el-date-picker clearable
            v-model="form.dueDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择整改期限">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" v-bind="getButtonProps()">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QualityIssues">
import { ref, reactive, toRefs, getCurrentInstance } from 'vue'
import { listQualityIssues, getQualityIssues, delQualityIssues, addQualityIssues, updateQualityIssues } from "@/api/evs/qualityIssues"
import { useUploadManager, uploadPresets } from '@/composables/useUploadManager'
import ImageUploadCard from '@/components/ImageUploadCard/index.vue'
import { Check, Loading } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const qualityIssuesList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

// 现场照片上传状态
const imagesFileList = ref([])

// 初始化上传管理Hook - 使用问题上报预设配置
const {
  uploadStatus,
  submitting,
  uploadRef,
  isSubmitDisabled,
  handleSubmit,
  extractImageUrls,
  handleUploadStatusChange,
  handleUploadSuccess,
  handleUploadError,
  reset: resetUpload,
  getUploadProps,
  getButtonProps,
  getStatusTip
} = useUploadManager(uploadPresets.issue)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: null,
    description: null,
    category: null,
    location: null,
    images: null,
    status: null,
    resolvedAt: null,
  },
  rules: {
    title: [
      { required: true, message: "问题标题不能为空", trigger: "blur" }
    ],
    description: [
      { required: true, message: "问题描述不能为空", trigger: "blur" }
    ],
    category: [
      { required: true, message: "问题分类(GENERAL:一般问题、CRITICAL:红线问题、URGENT:紧急问题、OTHER:其他问题)不能为空", trigger: "blur" }
    ],
    // 移除图片字段的必填验证，改为可选
    status: [
      { required: true, message: "问题状态(OPEN:未解决、IN_PROGRESS:解决中、RESOLVED:已解决、CLOSED:已关闭)不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询质量问题列表 */
function getList() {
  loading.value = true
  listQualityIssues(queryParams.value).then(response => {
    qualityIssuesList.value = response.rows
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
    projectId: null,
    qualityInspectionId: null,
    title: null,
    description: null,
    category: null,
    location: null,
    images: null,
    status: null,
    resolvedAt: null,
    dueDate: null,
    createdAt: null,
    updatedAt: null,
    createdBy: null,
    updatedBy: null
  }
  // 重置图片上传状态
  imagesFileList.value = []
  // 重置上传管理器状态
  resetUpload()
  proxy.resetForm("qualityIssuesRef")
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
  title.value = "添加质量问题"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getQualityIssues(_id).then(response => {
    form.value = response.data

    // 处理现有图片数据
    if (response.data.images) {
      try {
        // 解析后端返回的图片JSON数据
        const imageUrls = typeof response.data.images === 'string'
          ? JSON.parse(response.data.images)
          : response.data.images

        if (Array.isArray(imageUrls) && imageUrls.length > 0) {
          // 转换为ImageUploadCard组件可识别的格式
          imagesFileList.value = uploadRef.value?.parseFileIdsToList?.(imageUrls) ||
            imageUrls.map((url, index) => ({
              uid: `existing-${index}`,
              name: `image-${index}.jpg`,
              url: url.startsWith('http') ? url : (import.meta.env.VITE_APP_BASE_API + url),
              status: 'success'
            }))
        }
      } catch (error) {
        console.warn('解析现有图片数据失败:', error)
        imagesFileList.value = []
      }
    }

    open.value = true
    title.value = "修改质量问题"
  })
}

/** 格式化图片数据 */
function formatImagesData() {
  try {
    // 从图片上传组件提取URL数组
    const uploadedImages = extractImageUrls(imagesFileList.value)

    // 返回JSON字符串格式，保持与后端的兼容性
    return uploadedImages.length > 0 ? JSON.stringify(uploadedImages) : null
  } catch (error) {
    console.error('格式化图片数据失败:', error)
    return null
  }
}

/** 提交按钮 */
function submitForm() {
  // 使用统一的提交处理逻辑
  handleSubmit(async () => {
    // 表单验证
    const valid = await new Promise((resolve) => {
      proxy.$refs["qualityIssuesRef"].validate((valid) => {
        resolve(valid)
      })
    })

    if (!valid) {
      throw new Error('表单验证失败')
    }

    // 准备提交数据
    const submitData = { ...form.value }

    // 格式化图片数据
    submitData.images = formatImagesData()

    if (submitData.id != null) {
      return updateQualityIssues(submitData)
    } else {
      return addQualityIssues(submitData)
    }
  }).then(() => {
    // 提交成功处理
    open.value = false
    getList()
  }).catch(() => {
    // 错误已经在handleSubmit中统一处理，这里不需要额外处理
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除质量问题编号为"' + _ids + '"的数据项？').then(function() {
    return delQualityIssues(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('evs/qualityIssues/export', {
    ...queryParams.value
  }, `qualityIssues_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped lang="scss">
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
