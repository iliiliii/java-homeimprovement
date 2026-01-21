<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">新闻资讯</h2>
        <p class="page-subtitle">管理系统资讯和公告信息,支持多位置发布</p>
      </div>
      <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['evs:newsConsultation:add']">
        新建资讯
      </el-button>
    </div>

    <!-- 搜索筛选区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" class="search-form">
      <el-form-item label="发布位置" prop="publishPosition">
        <el-select 
          v-model="queryParams.publishPosition" 
          placeholder="全部位置" 
          clearable 
          style="width: 200px;"
          @change="handleQuery"
        >
          <el-option
            v-for="dict in decoration_news_position"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发布状态" prop="publishStatus">
        <el-select 
          v-model="queryParams.publishStatus" 
          placeholder="全部状态" 
          clearable 
          style="width: 150px;"
          @change="handleQuery"
        >
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="草稿" value="DRAFT" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 资讯列表表格 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="newsConsultationList" style="width: 100%">
        
        
        <el-table-column label="标题" min-width="100">
          <template #default="scope">
            <div style="display: flex; align-items: center;">
              <el-icon style="margin-right: 8px; color: #409EFF;"><Document /></el-icon>
              <span>{{ scope.row.title }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="内容" min-width="100" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ scope.row.subtitle || '暂无内容' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="发布位置" width="120" align="center">
          <template #default="scope">
            <dict-tag :options="decoration_news_position" :value="scope.row.publishPosition" />
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
        <el-table-column label="排序" width="100" align="center">
          <template #default="scope">
            <el-button
              type="primary"
              link
              size="small"
              :icon="Top"
              :disabled="scope.$index === 0 || isMoving"
              @click="handleMoveUp(scope.$index)"
              title="上移"
            />
            <el-button
              type="primary"
              link
              size="small"
              :icon="Bottom"
              :disabled="scope.$index === newsConsultationList.length - 1 || isMoving"
              @click="handleMoveDown(scope.$index)"
              title="下移"
            />
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
        
        <el-table-column label="操作" width="300" align="center" fixed="right">
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
            <el-option
              v-for="dict in decoration_news_position"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
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
        <el-form-item v-if="form.id" label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" placeholder="请输入排序" style="width: 100%" />
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">数值越大排序越靠前</div>
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
import { listNewsConsultation, getNewsConsultation, delNewsConsultation, addNewsConsultation, updateNewsConsultation, updateNewsConsultationOrder } from "@/api/evs/newsConsultation"
import { Document, Calendar, VideoPlay, View, Loading, Check, Top, Bottom } from '@element-plus/icons-vue'
import { useUploadManager, uploadPresets } from '@/composables/useUploadManager'
import ImageUploadCard from '@/components/ImageUploadCard/index.vue'

const { proxy } = getCurrentInstance()
const { decoration_news_position } = proxy.useDict('decoration_news_position')

const newsConsultationList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const isMoving = ref(false) // 防抖状态，防止重复操作

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

/** 上移操作 */
async function handleMoveUp(index) {
  // 防抖/节流：如果正在执行移动操作，直接返回
  if (isMoving.value) {
    console.warn('移动操作正在进行中，请稍候...')
    return
  }
  
  if (index <= 0) return
  
  // 获取当前记录和上一条记录
  const currentItem = newsConsultationList.value[index]
  const prevItem = newsConsultationList.value[index - 1]
  
  // 获取当前记录的sortOrder
  const currentSortOrder = currentItem.sortOrder
  // 获取上一条记录的sortOrder
  const prevSortOrder = prevItem.sortOrder
  
  // 验证sortOrder值有效性
  if (currentSortOrder == null || prevSortOrder == null) {
    proxy.$modal.msgError("排序值无效，请刷新后重试")
    return
  }
  
  // 交换两个sortOrder值
  const newCurrentSortOrder = prevSortOrder
  const newPrevSortOrder = currentSortOrder
  
  console.log('上移操作 - 交换sortOrder:', {
    当前记录: { id: currentItem.id, 原sortOrder: currentSortOrder, 新sortOrder: newCurrentSortOrder },
    上一条记录: { id: prevItem.id, 原sortOrder: prevSortOrder, 新sortOrder: newPrevSortOrder }
  })
  
  isMoving.value = true
  
  try {
    await updateNewsConsultationOrder(currentItem.id, newCurrentSortOrder)
    await updateNewsConsultationOrder(prevItem.id, newPrevSortOrder)
    
    proxy.$modal.msgSuccess("排序已更新")
    await getList()
  } catch (error) {
    console.error('更新排序失败:', error)
    proxy.$modal.msgError("更新排序失败")
  } finally {
    setTimeout(() => {
      isMoving.value = false
    }, 100)
  }
}

/** 下移操作 */
async function handleMoveDown(index) {
  // 防抖/节流：如果正在执行移动操作，直接返回
  if (isMoving.value) {
    console.warn('移动操作正在进行中，请稍候...')
    return
  }
  
  if (index >= newsConsultationList.value.length - 1) return
  
  // 获取当前记录和下一条记录
  const currentItem = newsConsultationList.value[index]
  const nextItem = newsConsultationList.value[index + 1]
  
  // 获取当前记录的sortOrder
  const currentSortOrder = currentItem.sortOrder
  // 获取下一条记录的sortOrder
  const nextSortOrder = nextItem.sortOrder
  
  // 验证sortOrder值有效性
  if (currentSortOrder == null || nextSortOrder == null) {
    proxy.$modal.msgError("排序值无效，请刷新后重试")
    return
  }
  
  // 交换两个sortOrder值
  const newCurrentSortOrder = nextSortOrder
  const newNextSortOrder = currentSortOrder
  
  console.log('下移操作 - 交换sortOrder:', {
    当前记录: { id: currentItem.id, 原sortOrder: currentSortOrder, 新sortOrder: newCurrentSortOrder },
    下一条记录: { id: nextItem.id, 原sortOrder: nextSortOrder, 新sortOrder: newNextSortOrder }
  })
  
  isMoving.value = true
  
  try {
    await updateNewsConsultationOrder(currentItem.id, newCurrentSortOrder)
    await updateNewsConsultationOrder(nextItem.id, newNextSortOrder)
    
    proxy.$modal.msgSuccess("排序已更新")
    await getList()
  } catch (error) {
    console.error('更新排序失败:', error)
    proxy.$modal.msgError("更新排序失败")
  } finally {
    setTimeout(() => {
      isMoving.value = false
    }, 100)
  }
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
  // 计算新增资讯的排序值：使用当前最大值 + 1，确保新增的排在最前面（降序排列）
  if (newsConsultationList.value.length > 0) {
    const validOrders = newsConsultationList.value
      .map(item => item.sortOrder)
      .filter(order => order != null)
    
    if (validOrders.length > 0) {
      const maxOrder = Math.max(...validOrders)
      form.value.sortOrder = maxOrder + 1
    } else {
      form.value.sortOrder = 100
    }
  } else {
    form.value.sortOrder = 100
  }
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
          // 需要拼接 VITE_APP_BASE_API 前缀，否则图片无法正确显示
          const baseUrl = import.meta.env.VITE_APP_BASE_API
          let fullUrl = coverImageUrl
          
          // 如果不是完整URL且不以baseUrl开头，则拼接baseUrl
          if (!coverImageUrl.startsWith('http') && !coverImageUrl.startsWith(baseUrl)) {
            const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
            const imagePath = coverImageUrl.startsWith('/') ? coverImageUrl : '/' + coverImageUrl
            fullUrl = cleanBaseUrl + imagePath
          }
          
          coverImageFileList.value = [{
            uid: 'existing-cover',
            name: 'cover-image.jpg',
            url: fullUrl,
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
  align-items: center;
  margin-bottom: 12px;

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