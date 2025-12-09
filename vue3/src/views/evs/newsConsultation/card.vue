<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">资讯设置</h2>
        <p class="page-subtitle">管理系统资讯和公告信息,支持多位置发布</p>
      </div>
      <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['evs:newsConsultation:add']">
        新建资讯
      </el-button>
    </div>

    <!-- 资讯列表表格 -->
    <el-card shadow="never" style="margin-top: 20px;">
      <el-table v-loading="loading" :data="newsConsultationList" style="width: 100%">
        <el-table-column label="标题" min-width="200">
          <template #default="scope">
            <div style="display: flex; align-items: center;">
              <el-icon style="margin-right: 8px; color: #409EFF;"><Document /></el-icon>
              <span>{{ scope.row.title }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="内容" min-width="300" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ scope.row.subtitle || '暂无内容' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="发布位置" width="120" align="center">
          <template #default="scope">
            <el-tag 
              :type="scope.row.publishPosition === 'Banner区域' ? 'danger' : 'primary'"
              effect="plain"
              size="small"
            >
              {{ scope.row.publishPosition || '未设置' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="发布日期" width="140" align="center">
          <template #default="scope">
            <div style="display: flex; align-items: center; justify-content: center;">
              <el-icon style="margin-right: 4px; color: #909399;"><Calendar /></el-icon>
              <span>{{ parseTime(scope.row.publishTime, '{y}-{m}-{d}') || '未设置' }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag 
              :type="scope.row.publishStatus === 'PUBLISHED' ? 'success' : 'info'"
              effect="plain"
              size="small"
            >
              {{ scope.row.publishStatus === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              link 
              type="primary" 
              @click="handleTogglePublish(scope.row)"
              v-hasPermi="['evs:newsConsultation:edit']"
            >
              <el-icon style="margin-right: 4px;">
                <View v-if="scope.row.publishStatus === 'PUBLISHED'" />
                <VideoPlay v-else />
              </el-icon>
              {{ scope.row.publishStatus === 'PUBLISHED' ? '取消发布' : '发布' }}
            </el-button>
            <el-button 
              link 
              type="primary" 
              icon="Edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['evs:newsConsultation:edit']"
            >
              编辑
            </el-button>
            <el-button 
              link 
              type="primary" 
              icon="Delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['evs:newsConsultation:remove']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 添加或修改新闻咨询设置对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="newsConsultationRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input 
            v-model="form.title" 
            placeholder="请输入标题" 
            maxlength="100"
            show-word-limit
            clearable
          />
        </el-form-item>
        <el-form-item label="副标题" prop="subtitle">
          <el-input 
            v-model="form.subtitle" 
            type="textarea" 
            :rows="3"
            placeholder="请输入副标题/内容"
            maxlength="500"
            show-word-limit
            clearable
          />
        </el-form-item>
        <el-form-item label="发布位置" prop="publishPosition">
          <el-select v-model="form.publishPosition" placeholder="请选择发布位置" style="width: 100%">
            <el-option label="Banner区域" value="Banner区域" />
            <el-option label="资讯区域" value="资讯区域" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布时间" prop="publishTime">
          <el-date-picker 
            clearable
            v-model="form.publishTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择发布时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="发布状态" prop="publishStatus">
          <el-radio-group v-model="form.publishStatus">
            <el-radio label="DRAFT">草稿</el-radio>
            <el-radio label="PUBLISHED">已发布</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="封面图片" prop="coverImage">
          <ImageUploadCard
            v-model="coverImageFileList"
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
        <el-form-item label="跳转地址" prop="jumpUrl">
          <el-input 
            v-model="form.jumpUrl" 
            placeholder="请输入跳转地址"
            maxlength="500"
            show-word-limit
            clearable
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" placeholder="请输入排序" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button
            type="primary"
            @click="submitForm"
            :loading="submitting"
            :disabled="isSubmitDisabled"
          >
            确定
          </el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="NewsConsultationCard">
import { listNewsConsultation, getNewsConsultation, delNewsConsultation, addNewsConsultation, updateNewsConsultation } from "@/api/evs/newsConsultation"
import { Document, Calendar, VideoPlay, View, Loading, Check } from '@element-plus/icons-vue'
import { useUploadManager, uploadPresets } from '@/composables/useUploadManager'
import ImageUploadCard from '@/components/ImageUploadCard/index.vue'

const { proxy } = getCurrentInstance()

const newsConsultationList = ref([])
const open = ref(false)
const loading = ref(true)
const total = ref(0)
const title = ref("")

// 封面图片上传状态
const coverImageFileList = ref([])

// 初始化上传管理Hook - 使用资讯预设配置
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
  getStatusTip
} = useUploadManager(uploadPresets.news)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: null,
    publishPosition: null,
    publishTime: null,
    publishStatus: null,
    sortOrder: null,
  },
  rules: {
    title: [
      { required: true, message: "标题不能为空", trigger: "blur" },
      { min: 1, max: 100, message: "标题长度在1到100个字符之间", trigger: "blur" },
      { 
        validator: (_rule, value, callback) => {
          if (value && /[<>\"'&]/.test(value)) {
            callback(new Error("标题不能包含特殊字符：< > \" ' &"))
          } else {
            callback()
          }
        }, 
        trigger: "blur" 
      }
    ],
    subtitle: [
      { max: 500, message: "副标题长度不能超过500个字符", trigger: "blur" },
      { 
        validator: (_rule, value, callback) => {
          if (value && /[<>\"'&]/.test(value)) {
            callback(new Error("副标题不能包含特殊字符：< > \" ' &"))
          } else {
            callback()
          }
        }, 
        trigger: "blur" 
      }
    ],
    publishPosition: [
      { required: true, message: "发布位置不能为空", trigger: "change" }
    ],
    publishStatus: [
      { required: true, message: "发布状态不能为空", trigger: "change" }
    ],
    jumpUrl: [
      { max: 500, message: "跳转地址长度不能超过500个字符", trigger: "blur" },
      {
        validator: (_rule, value, callback) => {
          if (!value || value.trim() === '') {
            callback()
          } else {
            // 简单的URL格式验证
            const urlPattern = /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/
            if (!urlPattern.test(value) && !value.startsWith('/') && !value.startsWith('#')) {
              callback(new Error("请输入正确的URL地址格式"))
            } else {
              callback()
            }
          }
        },
        trigger: "blur"
      }
    ],
    sortOrder: [
      { type: "number", min: 0, max: 999999, message: "排序值必须在0到999999之间", trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询新闻咨询设置列表 */
function getList() {
  loading.value = true
  listNewsConsultation(queryParams.value).then(response => {
    newsConsultationList.value = response.rows
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
    title: null,
    subtitle: null,
    publishPosition: null,
    publishTime: null,
    publishStatus: 'DRAFT',
    coverImage: null,
    jumpUrl: null,
    sortOrder: null,
    createdAt: null,
    updatedAt: null,
    deletedAt: null,
    createdBy: null,
    updatedBy: null
  }
  // 重置图片上传状态
  coverImageFileList.value = []
  // 重置上传管理器状态
  reset()
  proxy.resetForm("newsConsultationRef")
}

/** 新增按钮操作 */
function handleAdd() {
  resetForm()
  open.value = true
  title.value = "新建资讯"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  resetForm()
  const _id = row.id
  getNewsConsultation(_id).then(response => {
    form.value = response.data

    // 处理现有封面图片数据
    if (form.value.coverImage) {
      try {
        // 将封面图片转换为ImageUploadCard可识别的格式
        const coverImageUrl = form.value.coverImage
        if (coverImageUrl) {
          coverImageFileList.value = uploadRef.value?.parseFileIdsToList?.([coverImageUrl]) || [{
            uid: 'existing-cover',
            name: 'cover-image.jpg',
            url: coverImageUrl.startsWith('http') ? coverImageUrl : (import.meta.env.VITE_APP_BASE_API + coverImageUrl),
            status: 'success'
          }]
        }
      } catch (error) {
        console.warn('解析现有封面图片失败:', error)
        coverImageFileList.value = []
      }
    }

    open.value = true
    title.value = "编辑资讯"
  })
}

/** 格式化封面图片数据 */
function formatCoverImageData() {
  try {
    // 从图片上传组件提取URL数组
    const uploadedImages = extractImageUrls(coverImageFileList.value)

    // 资讯封面只取第一张图片
    return uploadedImages.length > 0 ? uploadedImages[0] : null
  } catch (error) {
    console.error('格式化封面图片数据失败:', error)
    return null
  }
}

/** 提交按钮 */
function submitForm() {
  // 使用统一的提交处理逻辑
  handleSubmit(async () => {
    // 表单验证
    const valid = await new Promise((resolve) => {
      proxy.$refs["newsConsultationRef"].validate((valid) => {
        resolve(valid)
      })
    })

    if (!valid) {
      throw new Error('表单验证失败')
    }

    // 准备提交数据
    const submitData = { ...form.value }

    // 格式化封面图片数据
    submitData.coverImage = formatCoverImageData()

    if (submitData.id != null) {
      return updateNewsConsultation(submitData)
    } else {
      return addNewsConsultation(submitData)
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
  const _id = row.id
  proxy.$modal.confirm('是否确认删除该资讯？').then(function() {
    return delNewsConsultation(_id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 发布/取消发布操作 */
function handleTogglePublish(row) {
  const newStatus = row.publishStatus === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED'
  const action = newStatus === 'PUBLISHED' ? '发布' : '取消发布'
  
  proxy.$modal.confirm(`是否确认${action}该资讯？`).then(() => {
    const updateData = {
      ...row,
      publishStatus: newStatus
    }
    return updateNewsConsultation(updateData)
  }).then(() => {
    proxy.$modal.msgSuccess(`${action}成功`)
    getList()
  }).catch(() => {})
}

getList()
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px 0;
    color: #303133;
  }

  .page-subtitle {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

:deep(.el-table) {
  .el-table__cell {
    padding: 16px 0;
  }
}

:deep(.el-card__body) {
  padding: 20px;
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