<template>
  <el-dialog
    v-model="dialogVisible"
    width="500px"
    append-to-body
    :show-close="true"
    :close-on-click-modal="false"
    class="customer-info-dialog"
  >
    <template #header>
      <span style="font-size: 16px; font-weight: 600;">客户信息</span>
    </template>

    <div v-if="customerInfo" class="customer-info-content">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="客户姓名">
          <span>{{ customerInfo.name || '-' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="联系电话">
          <span>{{ customerInfo.phone || '-' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="客户地址">
          <span>{{ customerInfo.address || '-' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="客户等级">
          <dict-tag :options="decoration_customer_level" :value="customerInfo.level" />
        </el-descriptions-item>

        <el-descriptions-item label="客户状态">
          <el-tag :type="customerInfo.isActive ? 'success' : 'info'" size="small">
            {{ customerInfo.isActive ? '活跃' : '非活跃' }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="关联项目">
          <span>{{ customerInfo.projectCount || 0 }} 个项目</span>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <div v-else class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <template #footer>
      <div style="text-align: right;">
        <el-button @click="handleClose">关闭</el-button>
        <el-button
          v-if="isAdmin"
          type="primary"
          @click="handleViewDetail"
        >
          查看详情
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, getCurrentInstance, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCustomers } from '@/api/evs/customers'
import { parseTime } from '@/utils/ruoyi'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { decoration_customer_level } = proxy.useDict('decoration_customer_level')

// 使用 Pinia store
const userStore = useUserStore()

// 判断是否为管理员 - 使用响应式计算属性
const isAdmin = computed(() => {
  console.log('当前用户角色:', userStore.roles)  // 添加调试日志
  return userStore.roles && userStore.roles.includes('admin')
})

// 是否显示对话框
const dialogVisible = ref(false)

// 当前客户信息
const customerInfo = ref(null)

// 客户ID
const customerId = ref(null)

// 加载状态
const loading = ref(false)

/** 显示客户信息对话框 */
function show(id) {
  customerId.value = id
  dialogVisible.value = true
  loadCustomerInfo()
}

/** 加载客户信息 */
function loadCustomerInfo() {
  if (!customerId.value) return

  loading.value = true
  getCustomers(customerId.value)
    .then(response => {
      customerInfo.value = response.data
    })
    .catch(error => {
      console.error('加载客户信息失败:', error)
      ElMessage.error('加载客户信息失败')
      customerInfo.value = null
    })
    .finally(() => {
      loading.value = false
    })
}

/** 关闭对话框 */
function handleClose() {
  dialogVisible.value = false
  customerInfo.value = null
  customerId.value = null
}

/** 查看客户详情页 */
function handleViewDetail() {
  if (customerInfo.value?.id) {
    // 跳转到客户详情页
    router.push({
      path: '/customers',
      query: { id: customerInfo.value.id }
    })
    handleClose()
  }
}

// 暴露方法给父组件
defineExpose({
  show,
  handleClose
})
</script>

<style scoped lang="scss">
.customer-info-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 20px 10px 20px;
    border-bottom: 1px solid #ebeef5;
  }

  :deep(.el-dialog__body) {
    padding: 20px;
  }

  :deep(.el-descriptions__label) {
    font-weight: 600;
    color: #606266;
    background-color: #fafafa;
    width: 100px;
  }

  :deep(.el-descriptions__content) {
    color: #303133;
  }
}

.customer-info-content {
  .el-descriptions {
    :deep(.el-descriptions__body) {
      .el-descriptions__table {
        border: 1px solid #ebeef5;
        border-radius: 4px;
      }
    }
  }
}

.loading-container {
  padding: 20px 0;
}

.el-tag {
  font-weight: 500;
}
</style>
