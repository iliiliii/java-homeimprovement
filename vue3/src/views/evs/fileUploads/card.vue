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
                @upload-success="handleFileUploadSuccess"
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
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交</el-button>
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
import { listFileUploads, delFileUploads, addFileUploads, getFileUploads, updateFileUploads } from "@/api/evs/fileUploads"
import { useRoute } from "vue-router"

const { proxy } = getCurrentInstance()
const route = useRoute()

const loading = ref(false)
const fileList = ref('') // FileUpload组件返回的是字符串（逗号分隔的文件URL）
const fileIdList = ref([]) // 存储已上传文件的ID数组
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

/** 文件上传成功回调 - 立即保存到数据库并获得ID */
async function handleFileUploadSuccess(fileData) {
  try {
    // fileData: { name: fileName, url: fileName, originalName: string, size: number }
    const fileName = fileData.url || fileData.name
    const fileNameWithoutPrefix = fileName.startsWith('/') ? fileName.substring(1) : fileName
    const urlParts = fileNameWithoutPrefix.split('/')
    const fullFileName = urlParts[urlParts.length - 1]
    
    // 从文件名中提取扩展名作为类型
    const nameParts = fullFileName.split('.')
    const extension = nameParts.length > 1 ? nameParts[nameParts.length - 1].toLowerCase() : ''
    const mimeType = getMimeTypeByExtension(extension)
    
    // 根据当前路由或功能自动设置分类
    const autoCategory = getAutoCategory()
    
    const fileUploadData = {
      fileName: fileNameWithoutPrefix,
      originalName: fileData.originalName || fullFileName,
      mimeType: mimeType,
      size: fileData.size || 0,
      path: fileNameWithoutPrefix,
      url: fileName,  // 直接使用后端返回的路径
      type: extension,
      category: form.category || autoCategory,
      remarks: form.remarks || ''
    }
    
    // 保存文件到数据库
    const response = await addFileUploads(fileUploadData)
    if (response.code === 200 && response.data) {
      // 获取文件ID并添加到列表
      const fileId = response.data.id
      fileIdList.value.push(fileId)
      proxy.$modal.msgSuccess(`文件 "${fileData.originalName}" 上传成功`)
      
      // 刷新列表
      getList()
    } else {
      proxy.$modal.msgError('保存文件信息失败')
    }
  } catch (error) {
    console.error('保存文件信息失败:', error)
    proxy.$modal.msgError('保存文件信息失败: ' + (error.message || '未知错误'))
  }
}

/** 根据当前功能自动获取分类 */
function getAutoCategory() {
  // 可以根据路由、页面功能等自动设置分类
  // 这里可以根据实际业务需求调整
  const path = route.path
  
  // 根据路径判断分类
  if (path.includes('project')) {
    return 'project'
  } else if (path.includes('contract')) {
    return 'contract'
  } else if (path.includes('design')) {
    return 'design'
  } else if (path.includes('photo')) {
    return 'photos'
  } else if (path.includes('report')) {
    return 'report'
  }
  
  // 默认返回 'other'
  return 'other'
}

/** 解析文件列表字符串为数组 */
function parseFileList(fileListStr) {
  if (!fileListStr) return []
  if (typeof fileListStr === 'string') {
    return fileListStr.split(',').filter(url => url.trim())
  }
  if (Array.isArray(fileListStr)) {
    return fileListStr.map(item => {
      if (typeof item === 'string') {
        return item
      }
      if (typeof item === 'object' && item.url) {
        return item.url
      }
      return item
    }).filter(url => url)
  }
  return []
}

/** 提交 - 现在主要用于更新已上传文件的分类和备注 */
async function handleSubmit() {
  if (fileIdList.value.length === 0) {
    proxy.$modal.msgError("请先上传文件")
    return
  }

  loading.value = true
  try {
    // 更新已上传文件的分类和备注
    for (const fileId of fileIdList.value) {
      const fileInfo = await getFileUploads(fileId)
      if (fileInfo.code === 200 && fileInfo.data) {
        const fileData = fileInfo.data
        // 只更新分类和备注
        if (form.category || form.remarks) {
          await updateFileUploads({
            id: fileId,
            category: form.category || fileData.category,
            remarks: form.remarks || fileData.remarks
          })
        }
      }
    }

    proxy.$modal.msgSuccess(`成功更新 ${fileIdList.value.length} 个文件的分类和备注`)
    loading.value = false

    // 重置表单
    handleReset()

    // 刷新列表
    getList()
  } catch (error) {
    console.error('更新文件信息失败:', error)
    proxy.$modal.msgError('更新文件信息失败: ' + (error.message || '未知错误'))
    loading.value = false
  }
}

/** 重置 */
function handleReset() {
  fileList.value = ''
  fileIdList.value = []
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
  // 使用 $download 插件下载文件，自动携带认证 token
  // 通过文件ID获取文件信息，然后下载
  if (row.path) {
    proxy.$download.name(row.path, false)
  } else {
    // 如果没有path，通过ID获取文件信息
    getFileUploads(row.id).then(response => {
      if (response.code === 200 && response.data && response.data.path) {
        proxy.$download.name(response.data.path, false)
      } else {
        proxy.$modal.msgError('获取文件信息失败')
      }
    })
  }
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
