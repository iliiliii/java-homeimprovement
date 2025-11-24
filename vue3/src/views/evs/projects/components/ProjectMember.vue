<template>
  <!-- 团队成员分配对话框 -->
  <el-dialog
    v-model="dialogVisible"
    :title="`${project?.name || ''} - 团队成员分配`"
    width="600px"
    append-to-body
    :close-on-click-modal="false"
  >
    <template #header>
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-icon style="font-size: 20px;"><User /></el-icon>
        <span style="font-size: 16px; font-weight: 600;">{{ project?.name || '' }} - 团队成员分配</span>
      </div>
    </template>

    <div style="padding: 8px 0;">
      <!-- 设计师 -->
      <div class="role-section role-section-blue">
        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 12px;">
          <el-tag type="primary" size="small" style="font-weight: 600;">设计师</el-tag>
          <span style="font-size: 13px; color: #999;">(可多选)</span>
        </div>
        <el-select
          v-model="teamForm.designers"
          multiple
          placeholder="请选择设计师"
          style="width: 100%;"
          filterable
          clearable
          :reserve-keyword="false"
        >
          <el-option
            v-for="user in designerOptions"
            :key="user.userId"
            :label="`${user.nickName || user.userName}${user.userName && user.nickName ? '（' + user.userName + '）' : ''}`"
            :value="user.userId"
          />
        </el-select>
      </div>

      <!-- 项目经理 -->
      <div class="role-section role-section-green">
        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 12px;">
          <el-tag type="success" size="small" style="font-weight: 600;">项目经理</el-tag>
          <span style="font-size: 13px; color: #999;">(可多选)</span>
        </div>
        <el-select
          v-model="teamForm.managers"
          multiple
          placeholder="请选择项目经理"
          style="width: 100%;"
          filterable
          clearable
          :reserve-keyword="false"
        >
          <el-option
            v-for="user in managerOptions"
            :key="user.userId"
            :label="`${user.nickName || user.userName}${user.userName && user.nickName ? '（' + user.userName + '）' : ''}`"
            :value="user.userId"
          />
        </el-select>
      </div>

      <!-- 工长 -->
      <div class="role-section role-section-orange">
        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 12px;">
          <el-tag type="warning" size="small" style="font-weight: 600;">工长</el-tag>
          <span style="font-size: 13px; color: #999;">(可多选)</span>
        </div>
        <el-select
          v-model="teamForm.foremen"
          multiple
          placeholder="请选择工长"
          style="width: 100%;"
          filterable
          clearable
          :reserve-keyword="false"
        >
          <el-option
            v-for="user in foremanOptions"
            :key="user.userId"
            :label="`${user.nickName || user.userName}${user.userName && user.nickName ? '（' + user.userName + '）' : ''}`"
            :value="user.userId"
          />
        </el-select>
      </div>

      <!-- 监理 -->
      <div class="role-section role-section-purple">
        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 12px;">
          <el-tag type="danger" size="small" style="font-weight: 600;">监理</el-tag>
          <span style="font-size: 13px; color: #999;">(可多选)</span>
        </div>
        <el-select
          v-model="teamForm.supervisors"
          multiple
          placeholder="请选择监理"
          style="width: 100%;"
          filterable
          clearable
          :reserve-keyword="false"
        >
          <el-option
            v-for="user in supervisorOptions"
            :key="user.userId"
            :label="`${user.nickName || user.userName}${user.userName && user.nickName ? '（' + user.userName + '）' : ''}`"
            :value="user.userId"
          />
        </el-select>
      </div>
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
import { listUser } from '@/api/system/user'
import { listProjectMembers, addProjectMembers, delProjectMembers } from '@/api/evs/projectMembers'

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
const saving = ref(false)
const teamForm = ref({
  designers: [],
  managers: [],
  foremen: [],
  supervisors: []
})

// 用户选项列表
const allUsers = ref([])
const designerOptions = ref([])
const managerOptions = ref([])
const foremanOptions = ref([])
const supervisorOptions = ref([])

// 计算属性：弹窗显示状态
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

/** 获取用户的主要岗位 */
function getUserPost(user) {
  // 优先从 posts 数组中获取
  if (user.posts && user.posts.length > 0) {
    for (const post of user.posts) {
      const postKey = (post.postKey || '').toLowerCase()
      const postName = (post.postName || '').toLowerCase()

      if (postKey.includes('designer') || postName.includes('设计师')) {
        return 'designer'
      } else if (postKey.includes('manager') || postKey.includes('pm') || postName.includes('经理') || postName.includes('项目经理')) {
        return 'manager'
      } else if (postKey.includes('foreman') || postKey.includes('worker') || postName.includes('工长')) {
        return 'foreman'
      } else if (postKey.includes('supervisor') || postName.includes('监理')) {
        return 'supervisor'
      }
    }
  }

  // 尝试从 postCode 和 postName 字段获取（来自后端关联查询）
  const postCode = (user.postCode || '').toLowerCase()
  const postName = (user.postName || '').toLowerCase()

  if (postCode.includes('designer') || postName.includes('设计师') || postCode === 'sjs') {
    return 'designer'
  } else if (postCode.includes('manager') || postCode.includes('pm') || postName.includes('经理') || postName.includes('项目经理')) {
    return 'manager'
  } else if (postCode.includes('foreman') || postCode.includes('worker') || postName.includes('工长')) {
    return 'foreman'
  } else if (postCode.includes('supervisor') || postName.includes('监理')) {
    return 'supervisor'
  }

  // 如果没有 posts，尝试从 roleKey 字段获取
  const roleKey = (user.roleKey || '').toLowerCase()
  if (roleKey.includes('designer') || roleKey.includes('设计师')) {
    return 'designer'
  } else if (roleKey.includes('manager') || roleKey.includes('pm') || roleKey.includes('经理')) {
    return 'manager'
  } else if (roleKey.includes('foreman') || roleKey.includes('worker') || roleKey.includes('工长')) {
    return 'foreman'
  } else if (roleKey.includes('supervisor') || roleKey.includes('监理')) {
    return 'supervisor'
  }

  return null
}

/** 加载用户选项 */
async function loadUserOptions() {
  try {
    const response = await listUser({ status: '0' }) // 只获取正常状态的用户
    allUsers.value = response.rows || []

    // 根据用户的岗位或角色分类
    designerOptions.value = allUsers.value.filter(user => {
      const post = getUserPost(user)
      return post === 'designer'
    })

    managerOptions.value = allUsers.value.filter(user => {
      const post = getUserPost(user)
      return post === 'manager'
    })

    foremanOptions.value = allUsers.value.filter(user => {
      const post = getUserPost(user)
      return post === 'foreman'
    })

    supervisorOptions.value = allUsers.value.filter(user => {
      const post = getUserPost(user)
      return post === 'supervisor'
    })

    // 如果没有匹配的用户，显示所有用户（允许灵活分配）
    if (designerOptions.value.length === 0) {
      designerOptions.value = allUsers.value
    }
    if (managerOptions.value.length === 0) {
      managerOptions.value = allUsers.value
    }
    if (foremanOptions.value.length === 0) {
      foremanOptions.value = allUsers.value
    }
    if (supervisorOptions.value.length === 0) {
      supervisorOptions.value = allUsers.value
    }
  } catch (error) {
    proxy.$modal.msgError('加载用户列表失败：' + (error.msg || error.message))
  }
}

/** 加载项目成员 */
async function loadProjectMembers(projectId) {
  try {
    const response = await listProjectMembers({ projectId, isActive: 1 })
    const members = response.rows || []

    // 重置表单
    teamForm.value = {
      designers: [],
      managers: [],
      foremen: [],
      supervisors: []
    }

    // 将项目成员的用户信息合并到 allUsers 中（去重）
    const membersWithUserInfo = members
      .filter(member => member.userName) // 只保留有用户信息的
      .map(member => ({
        userId: member.userId,
        userName: member.userName,
        nickName: member.nickName,
        postName: member.postName,
        postCode: member.postCode,
        roleKey: member.roleKey
      }))

    // 合并用户信息，去重
    membersWithUserInfo.forEach(memberUser => {
      const existsIndex = allUsers.value.findIndex(u => String(u.userId) === String(memberUser.userId))
      if (existsIndex >= 0) {
        // 更新已存在的用户信息
        Object.assign(allUsers.value[existsIndex], memberUser)
      } else {
        // 添加新用户
        allUsers.value.push(memberUser)
      }
    })

    // 按角色分类
    members.forEach(member => {
      const userId = String(member.userId) // 转换为字符串，确保与 el-select 的 value 类型一致
      switch (member.role) {
        case 'DESIGNER':
          if (!teamForm.value.designers.includes(userId)) {
            teamForm.value.designers.push(userId)
          }
          break
        case 'PM':
          if (!teamForm.value.managers.includes(userId)) {
            teamForm.value.managers.push(userId)
          }
          break
        case 'WORKER':
          if (!teamForm.value.foremen.includes(userId)) {
            teamForm.value.foremen.push(userId)
          }
          break
        case 'SUPERVISOR':
          if (!teamForm.value.supervisors.includes(userId)) {
            teamForm.value.supervisors.push(userId)
          }
          break
      }
    })

    // 重新分类用户选项
    designerOptions.value = allUsers.value.filter(user => {
      const post = getUserPost(user)
      return post === 'designer'
    })

    managerOptions.value = allUsers.value.filter(user => {
      const post = getUserPost(user)
      return post === 'manager'
    })

    foremanOptions.value = allUsers.value.filter(user => {
      const post = getUserPost(user)
      return post === 'foreman'
    })

    supervisorOptions.value = allUsers.value.filter(user => {
      const post = getUserPost(user)
      return post === 'supervisor'
    })

    // 如果没有匹配的用户，显示所有用户（允许灵活分配）
    if (designerOptions.value.length === 0) {
      designerOptions.value = allUsers.value
    }
    if (managerOptions.value.length === 0) {
      managerOptions.value = allUsers.value
    }
    if (foremanOptions.value.length === 0) {
      foremanOptions.value = allUsers.value
    }
    if (supervisorOptions.value.length === 0) {
      supervisorOptions.value = allUsers.value
    }
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
    // 先获取当前项目的所有成员
    const currentMembersResponse = await listProjectMembers({
      projectId: props.project.id
    })
    const currentMembers = currentMembersResponse.rows || []

    // 构建新的成员映射
    const newMembersMap = {
      DESIGNER: teamForm.value.designers || [],
      PM: teamForm.value.managers || [],
      WORKER: teamForm.value.foremen || [],
      SUPERVISOR: teamForm.value.supervisors || []
    }

    // 找出需要删除的成员（在旧列表中但不在新列表中）
    const toDelete = []
    currentMembers.forEach(member => {
      const memberUserIdStr = String(member.userId) // 转换为字符串
      const roleMembers = newMembersMap[member.role] || []
      if (!roleMembers.includes(memberUserIdStr) && member.isActive === 1) {
        toDelete.push(member.id)
      }
    })

    // 找出需要添加的成员（在新列表中但不在旧列表中）
    const toAdd = []
    Object.keys(newMembersMap).forEach(role => {
      const roleMembers = newMembersMap[role]
      roleMembers.forEach(userId => {
        const userIdStr = String(userId) // 转换为字符串进行比较
        const exists = currentMembers.some(
          m => String(m.userId) === userIdStr && m.role === role && m.isActive === 1
        )
        if (!exists) {
          toAdd.push({ projectId: props.project.id, userId: userId, role })
        }
      })
    })

    // 执行删除操作
    for (const id of toDelete) {
      await delProjectMembers(id)
    }

    // 执行添加操作
    for (const member of toAdd) {
      await addProjectMembers(member)
    }

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
      // 加载用户列表
      await loadUserOptions()

      // 加载当前项目的成员
      await loadProjectMembers(props.project.id)
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

.role-section-blue {
  background: #e6f7ff;
  border-color: #91d5ff;
}

.role-section-green {
  background: #f6ffed;
  border-color: #b7eb8f;
}

.role-section-orange {
  background: #fff7e6;
  border-color: #ffd591;
}

.role-section-purple {
  background: #f9f0ff;
  border-color: #d3adf7;
}
</style>
