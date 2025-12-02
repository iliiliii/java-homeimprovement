<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="质检标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入质检标题"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="质检结果" prop="result">
        <el-input
          v-model="queryParams.result"
          placeholder="请输入质检结果"
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
          v-hasPermi="['evs:qualityInspections:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['evs:qualityInspections:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['evs:qualityInspections:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['evs:qualityInspections:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="qualityInspectionsList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="质检ID" align="center" prop="id" />
      <el-table-column label="质检类型" align="center" prop="inspectionType" />
      <el-table-column label="质检标题" align="center" prop="title" />
      <el-table-column label="质检描述" align="center" prop="description" />
      <el-table-column label="质检结果" align="center" prop="result" />
      <el-table-column label="检查日期" align="center" prop="inspectionDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.inspectionDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['evs:qualityInspections:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['evs:qualityInspections:remove']">删除</el-button>
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

    <!-- 添加或修改质量检测对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="qualityInspectionsRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input v-model="form.projectId" placeholder="请输入项目ID" />
        </el-form-item>
        <el-form-item label="进度ID" prop="scheduleId">
          <el-input v-model="form.scheduleId" placeholder="请输入进度ID" />
        </el-form-item>
        <el-form-item label="质检标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入质检标题" />
        </el-form-item>
        <el-form-item label="质检描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="质检结果" prop="result">
          <el-input v-model="form.result" placeholder="请输入质检结果" />
        </el-form-item>
        <el-form-item label="检查日期" prop="inspectionDate">
          <el-date-picker clearable
            v-model="form.inspectionDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择检查日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="现场照片" prop="images">
          <ImageUploadCard
            ref="uploadRef"
            v-model="form.images"
            :upload-url="uploadUrl"
            :upload-headers="uploadHeaders"
            :max-count="20"
            :max-size="10"
            tip-text="(最多20张，支持jpg、png格式，自动压缩)"
            :compress="true"
            :compress-quality="0.8"
            :compress-max-size="5"
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
        <el-form-item label="备注" prop="remarks">
          <el-input v-model="form.remarks" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button
            type="primary"
            @click="submitForm"
            :loading="submitting"
            :disabled="!uploadStatus.isAllUploaded"
          >确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QualityInspections">
import { ref, reactive, toRefs, getCurrentInstance } from 'vue'
import { getToken } from "@/utils/auth"
import { listQualityInspections, getQualityInspections, delQualityInspections, addQualityInspections, updateQualityInspections } from "@/api/evs/qualityInspections"
import ImageUploadCard from '@/components/ImageUploadCard/index.vue'
import { Check, Loading } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const qualityInspectionsList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const submitting = ref(false)
const uploadRef = ref(null)

// 上传配置
const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/common/upload'
const uploadHeaders = ref({ Authorization: "Bearer " + getToken() })

// 上传状态管理
const uploadStatus = ref({
  isAllUploaded: true,
  totalFiles: 0,
  uploadedFiles: 0
})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    inspectionType: null,
    title: null,
    result: null,
  },
  rules: {
    inspectionType: [
      { required: true, message: "质检类型不能为空", trigger: "change" }
    ],
    title: [
      { required: true, message: "质检标题不能为空", trigger: "blur" }
    ],
    result: [
      { required: true, message: "质检结果不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询质量检测列表 */
function getList() {
  loading.value = true
  listQualityInspections(queryParams.value).then(response => {
    qualityInspectionsList.value = response.rows
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
    scheduleId: null,
    inspectionType: null,
    title: null,
    description: null,
    result: null,
    inspectionDate: null,
    images: null,
    remarks: null,
    createdAt: null,
    updatedAt: null,
    createdBy: null,
    updatedBy: null
  }
  proxy.resetForm("qualityInspectionsRef")
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
  title.value = "添加质量检测"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getQualityInspections(_id).then(response => {
    form.value = response.data

    // 处理图片数据
    if (response.data.images) {
      try {
        const parsedImages = typeof response.data.images === 'string'
          ? JSON.parse(response.data.images)
          : response.data.images

        if (Array.isArray(parsedImages)) {
          form.value.images = parsedImages.map((img, index) => {
            // 确保图片URL格式正确
            let imageUrl = ''
            const baseUrl = import.meta.env.VITE_APP_BASE_API

            if (img.startsWith('http')) {
              imageUrl = img
            } else if (img.startsWith(baseUrl)) {
              imageUrl = img
            } else {
              let path = img
              if (!path.startsWith('/')) {
                path = '/' + path
              }
              const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
              imageUrl = cleanBaseUrl + path
            }

            return {
              uid: `edit-${index}`,
              name: `image-${index}.jpg`,
              url: imageUrl
            }
          })
        }
      } catch (error) {
        console.warn('图片数据解析失败:', error)
        form.value.images = []
      }
    } else {
      form.value.images = []
    }

    open.value = true
    title.value = "修改质量检测"
  })
}

// 上传状态变化回调
function handleUploadStatusChange(status) {
  uploadStatus.value = status
}

// 上传成功回调
function handleUploadSuccess({ file, response }) {
  try {
    console.log('质检记录图片上传成功:', { file, response })
  } catch (error) {
    console.error('质检记录图片上传回调处理失败:', error)
  }
}

// 上传失败回调
function handleUploadError({ file, message }) {
  try {
    console.error('质检记录图片上传失败:', { file, message })
  } catch (error) {
    console.error('质检记录图片上传错误回调处理失败:', error)
  }
}

/** 提交按钮 */
function submitForm() {
  // 检查图片上传状态
  if (!uploadStatus.value.isAllUploaded) {
    proxy.$modal.msgWarning('请等待图片上传完成后再提交')
    return
  }

  // 检查网络和认证状态
  if (!navigator.onLine) {
    proxy.$modal.msgError('网络连接已断开，请检查网络后重试')
    return
  }

  const token = getToken()
  if (!token) {
    proxy.$modal.msgError('用户认证已失效，请重新登录')
    return
  }

  proxy.$refs["qualityInspectionsRef"].validate(valid => {
    if (valid) {
      submitting.value = true

      // 使用 ImageUploadCard 的 extractImageUrls 方法处理图片
      const processedImages = uploadRef.value?.extractImageUrls(form.value.images) || []

      // 处理提交数据
      const submitData = {
        ...form.value,
        images: processedImages.length > 0 ? JSON.stringify(processedImages) : '[]'
      }

      if (form.value.id != null) {
        updateQualityInspections(submitData).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        }).catch(error => {
          console.error('修改质量检测失败:', error)
          if (error.response?.status === 401) {
            proxy.$modal.msgError('用户认证已失效，请重新登录')
          } else if (error.response?.status >= 500) {
            proxy.$modal.msgError('服务器错误，请稍后重试')
          } else {
            proxy.$modal.msgError(error.message || error.msg || '修改失败，请重试')
          }
        }).finally(() => {
          submitting.value = false
        })
      } else {
        addQualityInspections(submitData).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        }).catch(error => {
          console.error('新增质量检测失败:', error)
          if (error.response?.status === 401) {
            proxy.$modal.msgError('用户认证已失效，请重新登录')
          } else if (error.response?.status >= 500) {
            proxy.$modal.msgError('服务器错误，请稍后重试')
          } else {
            proxy.$modal.msgError(error.message || error.msg || '新增失败，请重试')
          }
        }).finally(() => {
          submitting.value = false
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除质量检测编号为"' + _ids + '"的数据项？').then(function() {
    return delQualityInspections(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('evs/qualityInspections/export', {
    ...queryParams.value
  }, `qualityInspections_${new Date().getTime()}.xlsx`)
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
