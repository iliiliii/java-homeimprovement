<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>文件上传</span>
        </div>
      </template>

      <el-form :model="form" label-width="80px">
        <el-form-item label="文件上传" required>
          <el-row :gutter="20">
            <el-col :span="24">
              <FileUpload
                v-model="fileList"
                :limit="10"
                :file-size="10"
                :file-type="['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'pdf', 'zip', 'rar', 'jpg', 'jpeg', 'png']"
                :is-show-tip="true"
                @change="handleFileChange"
              />
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="文件分类">
          <el-select v-model="form.category" placeholder="请选择文件分类" clearable style="width: 200px">
            <el-option label="合同文档" value="contract" />
            <el-option label="设计图纸" value="design" />
            <el-option label="施工照片" value="photos" />
            <el-option label="验收报告" value="report" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            style="width: 400px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="uploading" @click="handleSubmit">提交</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="box-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>已上传文件</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="uploadedFilesList" style="width: 100%">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="文件名" align="center" prop="originalName" />
        <el-table-column label="文件类型" align="center" prop="mimeType" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.mimeType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="文件大小" align="center" prop="size" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column label="文件路径" align="center" prop="path" show-overflow-tooltip />
        <el-table-column label="分类" align="center" prop="category" width="120">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)">
              {{ getCategoryName(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" align="center" prop="createdAt" width="180" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="Download" @click="handleDownload(scope.row)">
              下载
            </el-button>
            <el-button link type="danger" icon="Delete" @click="handleDeleteFile(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>
  </div>
</template>

<script setup name="FileUploads">
import FileUpload from "@/components/FileUpload/index.vue"
import { listFileUploads, delFileUploads, addFileUploads } from "@/api/evs/fileUploads"

const { proxy } = getCurrentInstance()

const loading = ref(false)
const uploading = ref(false)
const fileList = ref([])
const uploadedFilesList = ref([])
const total = ref(0)

const form = reactive({
  category: null,
  remarks: ''
})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    fileName: null,
    originalName: null,
    path: null,
    type: null,
    category: null
  }
})

const { queryParams } = toRefs(data)

/** 查询文件上传列表 */
function getList() {
  loading.value = true
  listFileUploads(queryParams.value).then(response => {
    uploadedFilesList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 文件变化处理 */
function handleFileChange(value) {
  console.log('文件列表变化:', value)
  fileList.value = value
}

/** 提交 */
async function handleSubmit() {
  if (!fileList.value || fileList.value.length === 0) {
    proxy.$modal.msgError("请先选择要上传的文件")
    return
  }

  uploading.value = true
  try {
    // 由于FileUpload组件已经将文件上传到/common/upload，
    // 现在需要将文件元数据保存到数据库
    // FileUpload组件返回的文件格式: { name: fileName, url: fileName }

    const fileUploads = fileList.value.map(file => {
      // 从file.name或file.url中提取原始文件名（去掉路径）
      const originalName = file.name || file.url || ''
      const fileName = file.url || file.name || ''

      // 从原始文件名中提取扩展名作为类型
      const extension = originalName.split('.').pop()?.toLowerCase() || ''
      const mimeType = getMimeTypeByExtension(extension)

      return {
        fileName: fileName,
        originalName: originalName,
        mimeType: mimeType,
        size: null, // FileUpload组件不暴露文件大小，需要修改组件才能获取
        path: fileName,
        url: import.meta.env.VITE_APP_BASE_API + fileName,
        type: extension,
        category: form.category,
        remarks: form.remarks
      }
    })

    // 批量插入文件记录
    for (const fileData of fileUploads) {
      await addFileUploads(fileData)
    }

    proxy.$modal.msgSuccess(`成功上传 ${fileList.value.length} 个文件`)
    uploading.value = false

    // 重置表单
    handleReset()

    // 刷新列表
    getList()
  } catch (error) {
    console.error('保存文件信息失败:', error)
    proxy.$modal.msgError('保存文件信息失败')
    uploading.value = false
  }
}

/** 重置 */
function handleReset() {
  fileList.value = []
  form.category = null
  form.remarks = ''
}

/** 删除文件 */
function handleDeleteFile(row) {
  proxy.$modal.confirm(`是否确认删除文���"${row.originalName}"？`).then(function() {
    return delFileUploads(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 下载文件 */
function handleDownload(row) {
  // 构建下载URL
  const downloadUrl = import.meta.env.VITE_APP_BASE_API + '/common/download?fileName=' + encodeURIComponent(row.path)
  window.open(downloadUrl, '_blank')
}

/** 格式化文件大小 */
function formatFileSize(size) {
  if (!size && size !== 0) return '-'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let index = 0
  let fileSize = size
  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024
    index++
  }
  return fileSize.toFixed(2) + ' ' + units[index]
}

/** 获取分类名称 */
function getCategoryName(category) {
  const map = {
    contract: '合同文档',
    design: '设计图纸',
    photos: '施工照片',
    report: '验收报告',
    other: '其他'
  }
  return map[category] || category
}

/** 获取分类标签类型 */
function getCategoryType(category) {
  const map = {
    contract: 'success',
    design: 'warning',
    photos: 'info',
    report: 'danger',
    other: ''
  }
  return map[category] || ''
}

/** 根据文件扩展名获取MIME类型 */
function getMimeTypeByExtension(extension) {
  const mimeTypes = {
    'pdf': 'application/pdf',
    'doc': 'application/msword',
    'docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'xls': 'application/vnd.ms-excel',
    'xlsx': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'ppt': 'application/vnd.ms-powerpoint',
    'pptx': 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'txt': 'text/plain',
    'jpg': 'image/jpeg',
    'jpeg': 'image/jpeg',
    'png': 'image/png',
    'gif': 'image/gif',
    'zip': 'application/zip',
    'rar': 'application/x-rar-compressed',
    '7z': 'application/x-7z-compressed'
  }
  return mimeTypes[extension] || 'application/octet-stream'
}

getList()
</script>

<style scoped lang="scss">
.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>
