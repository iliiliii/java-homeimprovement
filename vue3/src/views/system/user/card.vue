<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">人员管理</h2>
        <p class="page-subtitle">管理团队成员信息</p>
      </div>
      <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['system:user:add']">
        添加成员
      </el-button>
    </div>

    <!-- 角色统计卡片 -->
    <el-row :gutter="20" class="role-stats">
      <el-col :span="6">
        <div class="stat-card stat-card-blue">
          <div class="stat-icon">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">设计师</div>
            <div class="stat-value">{{ roleStats.designer }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card-green">
          <div class="stat-icon">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">项目经理</div>
            <div class="stat-value">{{ roleStats.manager }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card-orange">
          <div class="stat-icon">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">工长</div>
            <div class="stat-value">{{ roleStats.foreman }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card-purple">
          <div class="stat-icon">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">监理</div>
            <div class="stat-value">{{ roleStats.supervisor }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 团队成员列表 -->
    <el-card class="team-list-card">
      <el-table v-loading="loading" :data="userList" style="width: 100%">
        <el-table-column label="头像" align="center" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :style="{ backgroundColor: getRoleColor(row) }">
              {{ getAvatarText(row.nickName || row.userName) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column label="姓名" align="center" prop="nickName" :show-overflow-tooltip="true">
          <template #default="{ row }">
            {{ row.nickName || row.userName }}
          </template>
        </el-table-column>
        <el-table-column label="角色" align="center" width="120">
          <template #default="{ row }">
            <el-tag :type="getPostTagType(row)" size="small">
              {{ getPostName(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="联系方式" align="center" width="150">
          <template #default="{ row }">
            <el-icon style="margin-right: 5px"><Phone /></el-icon>
            {{ row.phonenumber || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="参与项目" align="center" width="120">
          <template #default="{ row }">
            <el-icon style="margin-right: 5px"><Folder /></el-icon>
            {{ getProjectCount(row.userId) }}个项目
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:user:edit']">
                编辑
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:user:remove']">
                删除
              </el-button>
            </el-tooltip>
            <el-tooltip content="重置密码" placement="top">
              <el-button link type="warning" icon="Key" @click="handleResetPwd(scope.row)" v-hasPermi="['system:user:resetPwd']">
                重置密码
              </el-button>
            </el-tooltip>
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

    <!-- 添加或修改用户配置对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="userRef" label-width="80px">
        <el-row>
          <el-col :span="24" style="text-align: center; margin-bottom: 20px;">
            <AvatarUpload v-model="form.avatar" :size="100" />
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户昵称" prop="nickName">
              <el-input v-model="form.nickName" placeholder="请输入用户昵称" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归属部门" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="enabledDeptOptions"
                :props="{ value: 'id', label: 'label', children: 'children' }"
                value-key="id"
                placeholder="请选择归属部门"
                clearable
                check-strictly
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phonenumber">
              <el-input v-model="form.phonenumber" placeholder="请输入手机号码" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="用户名称" prop="userName">
              <el-input v-model="form.userName" placeholder="请输入用户名称" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="用户密码" prop="password">
              <el-input v-model="form.password" placeholder="请输入用户密码" type="password" maxlength="20" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户性别">
              <el-select v-model="form.sex" placeholder="请选择">
                <el-option v-for="dict in sys_user_sex" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="岗位">
              <el-select v-model="form.postIds" multiple placeholder="请选择岗位">
                <el-option v-for="item in postOptions" :key="item.postId" :label="item.postName" :value="item.postId" :disabled="item.status == 1"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
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

<script setup name="UserCard">
import { listUser, delUser, getUser, updateUser, addUser, deptTreeSelect, resetUserPwd } from "@/api/system/user"
import { listProjectMembers } from "@/api/evs/projectMembers"
import { User, Phone, Folder, Key } from "@element-plus/icons-vue"
import AvatarUpload from '@/components/AvatarUpload/index.vue'

const { proxy } = getCurrentInstance()
const { sys_normal_disable, sys_user_sex } = proxy.useDict("sys_normal_disable", "sys_user_sex")

const userList = ref([])
const open = ref(false)
const loading = ref(true)
const ids = ref([])
const total = ref(0)
const title = ref("")
const enabledDeptOptions = ref(undefined)
const initPassword = ref(undefined)
const postOptions = ref([])
const roleOptions = ref([])
const projectCountMap = ref({}) // 存储每个用户参与的项目数量

// 角色统计
const roleStats = computed(() => {
  const stats = {
    designer: 0,
    manager: 0,
    foreman: 0,
    supervisor: 0
  }

  userList.value.forEach(user => {
    const roleInfo = getUserPost(user)
    if (roleInfo === 'designer') {
      stats.designer++
    } else if (roleInfo === 'manager') {
      stats.manager++
    } else if (roleInfo === 'foreman') {
      stats.foreman++
    } else if (roleInfo === 'supervisor') {
      stats.supervisor++
    }
  })

  return stats
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

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phonenumber: undefined,
    status: undefined,
    deptId: undefined
  },
  rules: {
    userName: [
      { required: true, message: "用户名称不能为空", trigger: "blur" },
      { min: 2, max: 20, message: "用户名称长度必须介于 2 和 20 之间", trigger: "blur" }
    ],
    nickName: [{ required: true, message: "用户昵称不能为空", trigger: "blur" }],
    password: [
      { required: true, message: "用户密码不能为空", trigger: "blur" },
      { min: 5, max: 20, message: "用户密码长度必须介于 5 和 20 之间", trigger: "blur" },
      { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\\ |", trigger: "blur" }
    ],
    email: [{ type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] }],
    phonenumber: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询用户列表 */
function getList() {
  loading.value = true
  listUser(queryParams.value).then(res => {
    loading.value = false
    userList.value = res.rows
    total.value = res.total
    
    // 获取每个用户参与的项目数量
    getUserProjectCounts()
  })
}

/** 获取用户参与的项目数量 */
async function getUserProjectCounts() {
  const userIds = userList.value.map(user => user.userId)
  if (userIds.length === 0) return
  
  try {
    // 查询所有项目成员，统计每个用户参与的项目数
    const response = await listProjectMembers({ pageNum: 1, pageSize: 10000 })
    if (response.rows) {
      const countMap = {}
      userIds.forEach(userId => {
        countMap[userId] = response.rows.filter(member => member.userId === userId).length
      })
      projectCountMap.value = countMap
    }
  } catch (error) {
    console.error('获取项目数量失败:', error)
    // 如果失败，设置默认值
    userIds.forEach(userId => {
      projectCountMap.value[userId] = 0
    })
  }
}

/** 获取用户参与的项目数量 */
function getProjectCount(userId) {
  return projectCountMap.value[userId] || 0
}

/** 获取岗位名称 */
function getPostName(user) {
  const roleInfo = getUserPost(user)
  const roleMap = {
    designer: '设计师',
    manager: '项目经理',
    foreman: '工长',
    supervisor: '监理'
  }
  return roleMap[roleInfo] || '未分配'
}

/** 获取岗位标签类型 */
function getPostTagType(user) {
  const roleInfo = getUserPost(user)
  const typeMap = {
    designer: 'primary', // 蓝色
    manager: 'success', // 绿色
    foreman: 'warning', // 橙色
    supervisor: 'danger' // 紫色/红色
  }
  return typeMap[roleInfo] || ''
}

/** 获取岗位颜色 */
function getRoleColor(user) {
  const roleInfo = getUserPost(user)
  const colorMap = {
    designer: '#409EFF', // 蓝色
    manager: '#67C23A', // 绿色
    foreman: '#E6A23C', // 橙色
    supervisor: '#9C27B0' // 紫色
  }
  return colorMap[roleInfo] || '#909399'
}

/** 获取头像文字 */
function getAvatarText(name) {
  if (!name) return 'U'
  return name.length > 2 ? name.substring(name.length - 2) : name
}

/** 查询部门下拉树结构 */
function getDeptTree() {
  deptTreeSelect().then(response => {
    enabledDeptOptions.value = filterDisabledDept(JSON.parse(JSON.stringify(response.data)))
  })
}

/** 过滤禁用的部门 */
function filterDisabledDept(deptList) {
  return deptList.filter(dept => {
    if (dept.disabled) {
      return false
    }
    if (dept.children && dept.children.length) {
      dept.children = filterDisabledDept(dept.children)
    }
    return true
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const userIds = row.userId || ids.value
  proxy.$modal.confirm('是否确认删除用户编号为"' + userIds + '"的数据项？').then(function () {
    return delUser(userIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 重置操作表单 */
function reset() {
  form.value = {
    userId: undefined,
    deptId: undefined,
    userName: undefined,
    nickName: undefined,
    password: undefined,
    phonenumber: undefined,
    email: undefined,
    sex: undefined,
    status: "0",
    remark: undefined,
    postIds: [],
    avatar: undefined
  }
  proxy.resetForm("userRef")
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 重置密码按钮操作 */
function handleResetPwd(row) {
  proxy.$prompt('请输入"' + row.userName + '"的新密码', "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    closeOnClickModal: false,
    inputPattern: /^.{5,20}$/,
    inputErrorMessage: "用户密码长度必须介于 5 和 20 之间",
    inputValidator: (value) => {
      if (/<|>|"|'|\||\\/.test(value)) {
        return "不能包含非法字符：< > \" ' \\|"
      }
    },
  }).then(({ value }) => {
    resetUserPwd(row.userId, value).then(() => {
      proxy.$modal.msgSuccess("修改成功，新密码是：" + value)
    })
  }).catch(() => {})
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  getUser().then(response => {
    postOptions.value = response.posts
    roleOptions.value = response.roles
    open.value = true
    title.value = "添加成员"
    form.value.password = initPassword.value
  })
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const userId = row.userId || ids.value
  getUser(userId).then(response => {
    form.value = response.data
    postOptions.value = response.posts
    roleOptions.value = response.roles
    form.value.postIds = response.postIds
    form.value.roleIds = response.roleIds
    // 确保头像字段被正确设置
    if (response.data.avatar) {
      form.value.avatar = response.data.avatar
    }
    // 编辑模式下密码为空，不显示密码字段
    form.value.password = ""
    open.value = true
    title.value = "修改成员"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["userRef"].validate(valid => {
    if (valid) {
      if (form.value.userId != undefined) {
        updateUser(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addUser(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

onMounted(() => {
  getDeptTree()
  getList()
  proxy.getConfigKey("sys.user.initPassword").then(response => {
    initPassword.value = response.msg
  })
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
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

.role-stats {
  margin-bottom: 24px;
  
  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    transition: transform 0.3s;
    
    &:hover {
      transform: translateY(-2px);
    }
    
    .stat-icon {
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      margin-right: 16px;
    }
    
    .stat-content {
      flex: 1;
      
      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }
      
      .stat-value {
        font-size: 28px;
        font-weight: 600;
      }
    }
    
    &.stat-card-blue {
      .stat-icon {
        background-color: #ecf5ff;
        color: #409EFF;
      }
      .stat-value {
        color: #409EFF;
      }
    }
    
    &.stat-card-green {
      .stat-icon {
        background-color: #f0f9ff;
        color: #67C23A;
      }
      .stat-value {
        color: #67C23A;
      }
    }
    
    &.stat-card-orange {
      .stat-icon {
        background-color: #fdf6ec;
        color: #E6A23C;
      }
      .stat-value {
        color: #E6A23C;
      }
    }
    
    &.stat-card-purple {
      .stat-icon {
        background-color: #f4f1f8;
        color: #9C27B0;
      }
      .stat-value {
        color: #9C27B0;
      }
    }
  }
}

.team-list-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  
  :deep(.el-card__body) {
    padding: 20px;
  }
}

:deep(.el-table) {
  .el-table__cell {
    padding: 12px 0;
  }
}
</style>

