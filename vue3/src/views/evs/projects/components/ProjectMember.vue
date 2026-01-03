<template>
  <!-- 团队成员分配对话框 -->
  <el-dialog
    v-model="dialogVisible"
    :title="`${project?.name || ''} - 团队成员分配`"
    width="700px"
    append-to-body
    :close-on-click-modal="false"
  >
    <template #header>
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-icon style="font-size: 20px;"><User /></el-icon>
        <span style="font-size: 16px; font-weight: 600;">{{ project?.name || '' }} - 团队成员分配</span>
      </div>
    </template>

    <div v-loading="loading" style="padding: 8px 0;">
      <!-- 动态岗位列表 -->
      <div 
        v-for="(post, index) in postList" 
        :key="post.postId"
        class="role-section"
        :style="{ background: getPostColor(index).bg, borderColor: getPostColor(index).border }"
      >
        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 12px;">
          <el-tag :type="getPostColor(index).type" size="small" style="font-weight: 600;">
            {{ post.postName }}
          </el-tag>
          <span style="font-size: 13px; color: #999;">(可多选)</span>
        </div>
        <el-select
          v-model="teamForm[post.postId]"
          multiple
          :placeholder="`请选择${post.postName}`"
          style="width: 100%;"
          filterable
          clearable
          :reserve-keyword="false"
        >
          <el-option
            v-for="user in postUsersMap[post.postId] || []"
            :key="user.userId"
            :label="getUserLabel(user)"
            :value="String(user.userId)"
          />
        </el-select>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="postList.length === 0 && !loading" description="暂无可用岗位" />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取 消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存分配</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { User } from '@element-plus/icons-vue'
import { listPost } from '@/api/system/post'
import { getUsersByPostId } from '@/api/evs/userPost'
import { listProjectMembers, batchSaveProjectMembers } from '@/api/evs/projectMembers'

const { proxy } = getCurrentInstance()

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  project: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const postList = ref([])           // 岗位列表
const postUsersMap = ref({})       // 岗位ID -> 用户列表 的映射
const teamForm = ref({})           // 表单数据：{ postId: [userId1, userId2, ...] }

// 颜色配置
const colorConfigs = [
  { bg: '#e6f7ff', border: '#91d5ff', type: 'primary' },
  { bg: '#f6ffed', border: '#b7eb8f', type: 'success' },
  { bg: '#fff7e6', border: '#ffd591', type: 'warning' },
  { bg: '#f9f0ff', border: '#d3adf7', type: 'danger' },
  { bg: '#e6fffb', border: '#87e8de', type: 'info' },
  { bg: '#fff1f0', border: '#ffa39e', type: 'danger' },
  { bg: '#f4ffb8', border: '#d3f261', type: 'success' },
  { bg: '#f0f5ff', border: '#adc6ff', type: 'primary' }
]

// 计算属性：弹窗显示状态
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

/** 获取岗位颜色配置 */
function getPostColor(index) {
  return colorConfigs[index % colorConfigs.length]
}

/** 获取用户显示标签 */
function getUserLabel(user) {
  const name = user.nickName || user.userName
  const account = user.userName
  if (name && account && name !== account) {
    return `${name}（${account}）`
  }
  return name || account
}

/** 加载岗位列表 */
async function loadPostList() {
  try {
    const response = await listPost({ status: '0' }) // 只获取正常状态的岗位
    postList.value = response.rows || []
    
    // 初始化表单数据结构
    const formData = {}
    postList.value.forEach(post => {
      formData[post.postId] = []
    })
    teamForm.value = formData
  } catch (error) {
    proxy.$modal.msgError('加载岗位列表失败：' + (error.msg || error.message))
  }
}

/** 加载每个岗位下的用户 */
async function loadPostUsers() {
  const usersMap = {}
  
  // 并行加载所有岗位的用户
  await Promise.all(postList.value.map(async (post) => {
    try {
      const response = await getUsersByPostId(post.postId)
      usersMap[post.postId] = response.data || []
    } catch (error) {
      console.error(`加载岗位 ${post.postName} 的用户失败:`, error)
      usersMap[post.postId] = []
    }
  }))
  
  postUsersMap.value = usersMap
}

/** 加载项目成员 */
async function loadProjectMembers(projectId) {
  try {
    // 不分页，获取全部成员
    const response = await listProjectMembers({ projectId, isActive: 1, pageSize: 999 })
    const members = response.rows || []

    // 重置表单
    const formData = {}
    postList.value.forEach(post => {
      formData[post.postId] = []
    })

    // 按岗位分类成员
    // role字段存储的是岗位编码(postCode)，需要转换为postId
    members.forEach(member => {
      const userId = String(member.userId)
      const role = member.role // 岗位编码，如 DESIGNER, PM 等
      
      // 通过岗位编码找到对应的岗位ID
      const post = postList.value.find(p => p.postCode === role)
      if (post) {
        if (!formData[post.postId]) {
          formData[post.postId] = []
        }
        if (!formData[post.postId].includes(userId)) {
          formData[post.postId].push(userId)
        }
      }
    })

    teamForm.value = formData
  } catch (error) {
    proxy.$modal.msgError('加载项目成员失败：' + (error.msg || error.message))
  }
}

/** 保存团队分配 */
async function handleSave() {
  if (!props.project?.id) {
    proxy.$modal.msgError('项目ID不存在')
    return
  }

  saving.value = true
  try {
    // 构建成员列表：[{ userId, role }, ...]
    const members = []
    postList.value.forEach(post => {
      const userIds = teamForm.value[post.postId] || []
      userIds.forEach(userId => {
        members.push({
          userId: String(userId),
          role: post.postCode  // 使用岗位编码作为role
        })
      })
    })

    // 一次性批量保存
    await batchSaveProjectMembers({
      projectId: props.project.id,
      members: members
    })

    proxy.$modal.msgSuccess('团队分配保存成功')
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    proxy.$modal.msgError('保存团队分配失败：' + (error.msg || error.message))
  } finally {
    saving.value = false
  }
}

/** 取消操作 */
function handleCancel() {
  dialogVisible.value = false
}

/** 监听弹窗显示，加载数据 */
watch(
  () => dialogVisible.value,
  async (newVal) => {
    if (newVal && props.project?.id) {
      loading.value = true
      try {
        // 1. 加载岗位列表
        await loadPostList()
        
        // 2. 加载每个岗位下的用户
        await loadPostUsers()
        
        // 3. 加载当前项目的成员
        await loadProjectMembers(props.project.id)
      } finally {
        loading.value = false
      }
    }
  },
  { immediate: false }
)
</script>

<style scoped>
/* 角色分配区域样式 */
.role-section {
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #e8e8e8;
}
</style>
