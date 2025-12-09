<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="问题ID" prop="qualityIssuesId">
        <el-input
          v-model="queryParams.qualityIssuesId"
          placeholder="请输入问题ID"
          clearable
          @keyup.enter="handleQuery"
        />
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
          v-hasPermi="['evs:qualityFixes:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['evs:qualityFixes:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['evs:qualityFixes:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['evs:qualityFixes:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="qualityFixesList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="修复ID" align="center" prop="id" />
      <el-table-column label="问题ID" align="center" prop="qualityIssuesId" />
      <el-table-column label="修复描述" align="center" prop="fixDescription" />
      <el-table-column label="修复图片" align="center" prop="images" width="120">
        <template #default="scope">
          <div v-if="scope.row.images" class="image-preview">
            <el-tag v-if="getImageCount(scope.row.images) > 0" type="success" size="small">
              {{ getImageCount(scope.row.images) }}张图片
            </el-tag>
            <span v-else class="no-images">无图片</span>
          </div>
          <span v-else class="no-images">无图片</span>
        </template>
      </el-table-column>
      <el-table-column label="修复状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="quality_fix_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="修复时间" align="center" prop="fixedAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.fixedAt, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="验收时间" align="center" prop="verifiedAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.verifiedAt, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['evs:qualityFixes:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['evs:qualityFixes:remove']">删除</el-button>
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

    <!-- 添加或修改问题修复对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="qualityFixesRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="问题ID" prop="qualityIssuesId">
          <el-input v-model="form.qualityIssuesId" placeholder="请输入问题ID" />
        </el-form-item>
        <el-form-item label="修复描述" prop="fixDescription">
          <el-input v-model="form.fixDescription" type="textarea" placeholder="请输入修复描述" :rows="3" />
        </el-form-item>
        <el-form-item label="修复图片" prop="images">
          <ImageUploadCard
            v-model="imageFileList"
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
              请等待图片上传完成后再提交表单
            </span>
          </div>
        </el-form-item>
        <el-form-item label="修复时间" prop="fixedAt">
          <el-date-picker clearable
            v-model="form.fixedAt"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择修复时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="验收时间" prop="verifiedAt">
          <el-date-picker clearable
            v-model="form.verifiedAt"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择验收时间">
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

<script setup name="QualityFixes">
import { listQualityFixes, getQualityFixes, delQualityFixes, addQualityFixes, updateQualityFixes } from "@/api/evs/qualityFixes"
import { useUploadManager, uploadPresets } from '@/composables/useUploadManager'
import ImageUploadCard from '@/components/ImageUploadCard/index.vue'
import { Loading, Check } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const qualityFixesList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

// 图片上传相关状态
const imageFileList = ref([])
const currentImages = ref([])

// 初始化上传管理Hook
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
  reset,
  getUploadProps,
  getButtonProps,
  getStatusTip
} = useUploadManager(uploadPresets.fix)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    qualityIssuesId: null,
  },
  rules: {
    qualityIssuesId: [
      { required: true, message: "问题ID不能为空", trigger: "blur" }
    ],
    fixDescription: [
      { required: true, message: "修复描述不能为空", trigger: "blur" }
    ],
    // 移除图片字段的必填验证，改为可选
    status: [
      { required: true, message: "修复状态不能为空", trigger: "change" }
    ],
    fixedAt: [
      { required: true, message: "修复时间不能为空", trigger: "blur" }
    ],
    verifiedAt: [
      { required: true, message: "验收时间不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询问题修复列表 */
function getList() {
  loading.value = true
  listQualityFixes(queryParams.value).then(response => {
    qualityFixesList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  resetForm()
}

// 表单重置
function resetForm() {
  form.value = {
    id: null,
    qualityIssuesId: null,
    fixDescription: null,
    images: null,
    status: null,
    fixedAt: null,
    verifiedAt: null,
    createdAt: null,
    updatedAt: null,
    createdBy: null,
    updatedBy: null
  }
  // 重置图片上传状态
  imageFileList.value = []
  currentImages.value = []
  // 重置上传管理器状态
  reset()
  proxy.resetForm("qualityFixesRef")
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
  resetForm()
  open.value = true
  title.value = "添加问题修复"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  resetForm()
  const _id = row.id || ids.value
  getQualityFixes(_id).then(response => {
    form.value = response.data

    // 处理现有图片数据
    if (form.value.images) {
      try {
        // 解析后端返回的图片JSON数据
        const imageUrls = typeof form.value.images === 'string'
          ? JSON.parse(form.value.images)
          : form.value.images

        if (Array.isArray(imageUrls) && imageUrls.length > 0) {
          // 转换为ImageUploadCard组件可识别的格式
          imageFileList.value = uploadRef.value?.parseFileIdsToList?.(imageUrls) ||
            imageUrls.map((url, index) => ({
              uid: `existing-${index}`,
              name: `image-${index}.jpg`,
              url: url.startsWith('http') ? url : (import.meta.env.VITE_APP_BASE_API + url),
              status: 'success'
            }))

          currentImages.value = imageUrls
        }
      } catch (error) {
        console.warn('解析现有图片数据失败:', error)
        // 如果解析失败，清空图片列表
        imageFileList.value = []
        currentImages.value = []
      }
    }

    open.value = true
    title.value = "修改问题修复"
  })
}

/** 格式化图片数据 */
function formatImageData() {
  try {
    // 从图片上传组件提取URL数组
    const uploadedImages = extractImageUrls(imageFileList.value)

    // 合并现有图片和新上传的图片
    const allImages = [...currentImages.value, ...uploadedImages]

    // 去重
    const uniqueImages = [...new Set(allImages)]

    // 返回JSON字符串格式，保持与后端的兼容性
    return uniqueImages.length > 0 ? JSON.stringify(uniqueImages) : null
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
      proxy.$refs["qualityFixesRef"].validate((valid) => {
        resolve(valid)
      })
    })

    if (!valid) {
      throw new Error('表单验证失败')
    }

    // 准备提交数据
    const submitData = { ...form.value }

    // 格式化图片数据
    submitData.images = formatImageData()

    if (submitData.id != null) {
      return updateQualityFixes(submitData)
    } else {
      return addQualityFixes(submitData)
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
  proxy.$modal.confirm('是否确认删除问题修复编号为"' + _ids + '"的数据项？').then(function() {
    return delQualityFixes(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('evs/qualityFixes/export', {
    ...queryParams.value
  }, `qualityFixes_${new Date().getTime()}.xlsx`)
}

/** 获取图片数量 */
function getImageCount(images) {
  if (!images) return 0
  try {
    const parsedImages = typeof images === 'string' ? JSON.parse(images) : images
    return Array.isArray(parsedImages) ? parsedImages.length : 0
  } catch (error) {
    return 0
  }
}

getList()
</script>

<style scoped lang="scss">
.image-preview {
  display: flex;
  align-items: center;
  justify-content: center;

  .no-images {
    color: #999;
    font-size: 12px;
  }
}

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