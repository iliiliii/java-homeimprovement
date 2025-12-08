<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">客户管理</h2>
        <p class="page-subtitle">管理所有客户信息</p>
      </div>
      <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['evs:customers:add']">
        添加客户
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" class="search-form">
      <el-form-item label="客户姓名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入客户姓名"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入手机号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <!-- <el-form-item label="客户等级" prop="level">
        <el-select v-model="queryParams.level" placeholder="请选择客户等级" clearable>
          <el-option
            v-for="dict in decoration_customer_level"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item> -->
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 客户卡片展示 -->
    <div class="customer-container" v-loading="loading">
      <el-row :gutter="20">
        <el-col
          v-for="customer in customersList"
          :key="customer.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          :xl="4"
          style="margin-bottom: 20px;"
        >
        <el-card class="customer-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="customer-name-section">
                <div class="customer-name">{{ customer.name }}</div>
                <div class="customer-join-date" v-if="customer.createdAt">
                  加入于 {{ formatDate(customer.createdAt) }}
                </div>
              </div>
              <div class="customer-status" v-if="customer.isActive !== null && customer.isActive !== undefined">
                <span class="status-dot" :class="{ 'status-active': customer.isActive }"></span>
                <span class="status-text">{{ customer.isActive ? '活跃' : '非活跃' }}</span>
              </div>
            </div>
          </template>

          <div class="customer-details">
            <div class="detail-item">
              <el-icon class="detail-icon"><Phone /></el-icon>
              <span class="detail-value">{{ customer.phone }}</span>
            </div>

            <div class="detail-item" v-if="customer.address">
              <el-icon class="detail-icon"><Location /></el-icon>
              <span class="detail-value text-ellipsis" :title="customer.address">{{ customer.address }}</span>
            </div>

            <div class="detail-item projects-item">
              <el-icon class="detail-icon"><FolderOpened /></el-icon>
              <span class="detail-label">关联项目</span>
              <span class="project-count">{{ customer.projectCount || 0 }}</span>
             
            </div>
          </div>

          <template #footer>
            <div class="card-actions">
              <el-button link type="primary" icon="View" @click="handleView(customer)" v-hasPermi="['evs:customers:query']">
                查看
              </el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(customer)" v-hasPermi="['evs:customers:edit']">
                编辑
              </el-button>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="!loading && customersList.length === 0" description="暂无客户数据" />
    </div>

    <!-- 分页 -->
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 客户详情对话框 -->
    <el-dialog v-model="detailOpen" width="600px" append-to-body class="customer-detail-dialog">
      <template #header>
        <div class="detail-header">
          <div class="detail-title">
            <el-icon class="title-icon"><UserFilled /></el-icon>
            <span>{{ currentCustomer.name || '客户详情' }}</span>
          </div>
        </div>
      </template>

      <div class="detail-content">
        <!-- 客户操作部分 -->
        <el-card class="detail-card" shadow="never">
          <div class="section-header">
            <span class="section-subtitle">编辑或删除客户信息</span>
          </div>
          <div class="section-actions">
            <el-button 
              plain 
              icon="Edit" 
              @click="handleEditFromDetail"
              v-hasPermi="['evs:customers:edit']"
            >
              编辑客户
            </el-button>
            <el-button 
              plain 
              type="danger" 
              icon="Delete" 
              @click="handleDeleteFromDetail"
              v-hasPermi="['evs:customers:remove']"
            >
              删除客户
            </el-button>
          </div>
        </el-card>

        <!-- 客户基本信息部分 -->
        <el-card class="detail-card" shadow="never">
          <template #header>
            <div class="section-header">
              <el-icon class="info-icon"><InfoFilled /></el-icon>
              <span class="section-title">客户基本信息</span>
            </div>
          </template>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">客户姓名：</span>
              <span class="info-value">{{ currentCustomer.name || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">联系电话：</span>
              <span class="info-value-with-action">
                <span>{{ currentCustomer.phone || '-' }}</span>
                <el-icon 
                  v-if="currentCustomer.phone" 
                  class="copy-icon" 
                  @click="copyToClipboard(currentCustomer.phone)"
                >
                  <CopyDocument />
                </el-icon>
              </span>
            </div>
            <div class="info-item" v-if="currentCustomer.email">
              <span class="info-label">电子邮箱：</span>
              <span class="info-value-with-action">
                <span>{{ currentCustomer.email }}</span>
                <el-icon 
                  class="copy-icon" 
                  @click="copyToClipboard(currentCustomer.email)"
                >
                  <CopyDocument />
                </el-icon>
              </span>
            </div>
            <div class="info-item" v-if="currentCustomer.address">
              <span class="info-label">联系地址：</span>
              <span class="info-value">{{ currentCustomer.address }}</span>
            </div>
            <div class="info-item" v-if="currentCustomer.createdAt">
              <span class="info-label">创建日期：</span>
              <span class="info-value">{{ formatDate(currentCustomer.createdAt) }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </el-dialog>

    <!-- 添加或修改客户档案对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="customersRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="客户姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="客户等级" prop="level">
          <el-select v-model="form.level" placeholder="请选择客户等级">
            <el-option
              v-for="dict in decoration_customer_level"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input v-model="form.remarks" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Customers">
import { listCustomers, getCustomers, delCustomers, addCustomers, updateCustomers } from "@/api/evs/customers"
import { UserFilled, Phone, Message, Location, Calendar, FolderOpened, CopyDocument, InfoFilled } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const { proxy } = getCurrentInstance()
const { decoration_customer_level } = proxy.useDict('decoration_customer_level')

const customersList = ref([])
const open = ref(false)
const detailOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const currentCustomer = ref({})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    phone: null,
    address: null,
    level: null,
    source: null,
    remarks: null,
    avatar: null,
    isActive: null,
  },
  rules: {
    name: [
      { required: true, message: "客户姓名不能为空", trigger: "blur" }
    ],
    phone: [
      { required: true, message: "手机号不能为空", trigger: "blur" }
    ],
    isActive: [
      { required: true, message: "是否启用不能为空", trigger: "change" }
    ],
    createdAt: [
      { required: true, message: "创建时间不能为空", trigger: "blur" }
    ],
    updatedAt: [
      { required: true, message: "更新时间不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询客户档案列表 */
function getList() {
  loading.value = true
  const params = {
    ...queryParams.value,
    includeProjects: true
  }
  listCustomers(params).then(response => {
    customersList.value = response.rows
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
    name: null,
    phone: null,
    email: null,
    address: null,
    level: null,
    source: null,
    remarks: null,
    avatar: null,
    isActive: null,
    createdAt: null,
    updatedAt: null,
    deletedAt: null,
    createdBy: null,
    updatedBy: null,
    deletedBy: null
  }
  proxy.resetForm("customersRef")
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

/** 查看按钮操作 */
function handleView(row) {
  const _id = row.id
  getCustomers(_id).then(response => {
    currentCustomer.value = response.data
    detailOpen.value = true
  })
}

/** 复制到剪贴板 */
function copyToClipboard(text) {
  if (!text) return
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(text).then(() => {
      proxy.$modal.msgSuccess("已复制到剪贴板")
    }).catch(() => {
      // 降级方案
      fallbackCopy(text)
    })
  } else {
    // 降级方案
    fallbackCopy(text)
  }
}

/** 降级复制方案 */
function fallbackCopy(text) {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()
  try {
    document.execCommand('copy')
    proxy.$modal.msgSuccess("已复制到剪贴板")
  } catch (err) {
    proxy.$modal.msgError("复制失败")
  }
  document.body.removeChild(textarea)
}

/** 从详情对话框编辑 */
function handleEditFromDetail() {
  detailOpen.value = false
  handleUpdate({ id: currentCustomer.value.id })
}

/** 从详情对话框删除 */
function handleDeleteFromDetail() {
  detailOpen.value = false
  handleDelete({
    id: currentCustomer.value.id,
    name: currentCustomer.value.name,
    phone: currentCustomer.value.phone,
    projectCount: currentCustomer.value.projectCount || customersList.value.find(c => c.id === currentCustomer.value.id)?.projectCount || 0
  })
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加客户档案"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id
  getCustomers(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改客户档案"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["customersRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateCustomers(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addCustomers(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const projectCount = row.projectCount || 0

  // 检查是否有关联项目
  if (projectCount > 0) {
    proxy.$modal.msgWarning(`无法删除客户 ${row.name}(${row.phone})，该客户已关联 ${projectCount} 个项目`)
    return
  }

  // 关联项目小于等于0个，弹出确认框
  const _id = row.id
  proxy.$modal.confirm(`是否确认删除客户 ${row.name}(${row.phone}) 的数据项？`).then(function() {
    return delCustomers(_id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('evs/customers/export', {
    ...queryParams.value
  }, `customers_${new Date().getTime()}.xlsx`)
}

/** 格式化日期 */
function formatDate(date) {
  if (!date) return '-'
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/** 处理路由参数 */
function handleRouteQuery() {
  const { id } = route.query
  if (id) {
    // 如果有id参数，直接打开客户详情
    getCustomers(id).then(response => {
      if (response.data) {
        currentCustomer.value = response.data
        detailOpen.value = true
      }
    }).catch(() => {
      proxy.$modal.msgWarning('未找到该客户信息')
    })
  }
}

// 初始化
getList()
handleRouteQuery()

// 监听路由变化
watch(() => route.query, (newQuery) => {
  if (newQuery.id) {
    handleRouteQuery()
  }
}, { deep: true })
</script>

<style lang="scss" scoped>
.app-container {
  height: calc(100vh - 124px);
  display: flex;
  flex-direction: column;
  padding: 20px;
  overflow: hidden;
}

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

/* 客户容器 - 独立滚动 */
.customer-container {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 12px;
  padding: 2px;
}

/* 卡片样式 */
.customer-card {
  border-radius: 8px;
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.customer-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.customer-name-section {
  flex: 1;
  min-width: 0;
}

.customer-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.customer-join-date {
  font-size: 13px;
  color: #909399;
}

.customer-status {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #c0c4cc;
  display: inline-block;
}

.status-dot.status-active {
  background-color: #409eff;
}

.status-text {
  font-size: 13px;
  color: #606266;
}

/* 客户详细信息 */
.customer-details {
  flex: 1;
  padding: 16px 0;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-icon {
  color: #909399;
  margin-right: 8px;
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 2px;
}

.detail-label {
  color: #909399;
  white-space: nowrap;
  margin-right: 4px;
}

.detail-value {
  color: #606266;
  flex: 1;
  min-width: 0;
}

.text-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.projects-item {
  display: flex;
  align-items: center;
}

.project-count {
  color: #409eff;
  font-weight: 600;
  margin-left: 4px;
  margin-right: 8px;
}

.project-status {
  color: #909399;
  font-size: 13px;
}

/* 卡片底部操作区 */
.card-actions {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 8px 0 0;
  gap: 16px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .customer-card {
    margin-bottom: 16px;
  }

  .card-actions {
    flex-wrap: wrap;
    gap: 8px;
  }

  .card-actions .el-button {
    flex: 1;
    min-width: 0;
  }
}

/* 客户详情对话框样式 */
.customer-detail-dialog {
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .detail-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }

  .title-icon {
    color: #409eff;
    font-size: 20px;
  }

  .detail-content {
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .detail-card {
    border-radius: 8px;
    
    :deep(.el-card__body) {
      padding: 16px;
    }

    :deep(.el-card__header) {
      padding: 16px;
      border-bottom: 1px solid #ebeef5;
    }
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .section-subtitle {
    font-size: 14px;
    color: #909399;
    margin-bottom: 16px;
  }

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .info-icon {
    color: #409eff;
    font-size: 16px;
  }

  .section-actions {
    display: flex;
    gap: 12px;
  }

  .info-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .info-item {
    display: flex;
    align-items: flex-start;
    font-size: 14px;
    line-height: 1.6;
  }

  .info-label {
    color: #909399;
    min-width: 100px;
    flex-shrink: 0;
  }

  .info-value {
    color: #606266;
    flex: 1;
  }

  .info-value-with-action {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #606266;
    flex: 1;

    .copy-icon {
      color: #409eff;
      cursor: pointer;
      font-size: 16px;
      transition: color 0.3s;

      &:hover {
        color: #66b1ff;
      }
    }
  }
}
</style>
