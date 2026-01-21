<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-subtitle">管理团队成员信息</p>
      </div>
      <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['system:user:add']">
        添加成员
      </el-button>
    </div>

    <!-- 筛选表单区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" class="search-form">
      <el-form-item label="用户账号" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户账号" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号码" prop="phonenumber">
        <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号码" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 团队成员列表 -->
    <el-card class="team-list-card">
      <el-table v-loading="loading" :data="userList" style="width: 100%">
        <el-table-column label="头像" align="center" width="80">
          <template #default="{ row }">
            <el-avatar v-if="row.avatar" :size="40" :src="getAvatarUrl(row.avatar)">
              {{ getAvatarText(row.nickName || row.userName) }}
            </el-avatar>
            <el-avatar v-else :size="40" :style="{ backgroundColor: '#909399' }">
              {{ getAvatarText(row.nickName || row.userName) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column label="账号(名称)" align="center" width="180">
          <template #default="{ row }">
            {{ row.userName }}（{{ row.nickName }}）
          </template>
        </el-table-column>
        <el-table-column label="手机号码" align="center" min-width="120">
          <template #default="{ row }">
            <el-icon style="margin-right: 5px"><Phone /></el-icon>
            {{ row.phonenumber || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="岗位" align="center" min-width="120">
          <template #default="{ row }">
            <template v-if="userPostNamesMap[row.userId]">
              <el-tag 
                v-for="(postName, idx) in userPostNamesMap[row.userId]" 
                :key="idx"
                :type="getPostTagType(idx)"
                size="small"
                style="margin: 2px;"
              >
                {{ postName }}
              </el-tag>
            </template>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="参与项目" align="center" min-width="100">
          <template #default="{ row }">
            <el-icon style="margin-right: 5px"><Folder /></el-icon>
            {{ getProjectCount(row.userId) }}个项目
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" min-width="180">
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
            <AvatarUpload v-model="form.avatar" :size="100" :userId="form.userId" />
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户名称" prop="nickName">
              <el-input v-model="form.nickName" placeholder="请输入用户名称" maxlength="30" />
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
            <el-form-item v-if="false" label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户账号" prop="userName">
              <el-input v-model="form.userName" placeholder="请输入用户账号" maxlength="30" />
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
import { listPost } from "@/api/system/post"
import { listProjectMembers } from "@/api/evs/projectMembers"
import { getAllUserPost } from "@/api/evs/userPost"
import { User, Phone, Folder, Key } from "@element-plus/icons-vue"
import AvatarUpload from '@/components/AvatarUpload/index.vue'

const { proxy } = getCurrentInstance()
const { sys_normal_disable, sys_user_sex } = proxy.useDict("sys_normal_disable", "sys_user_sex")

const userList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const total = ref(0)
const title = ref("")
const enabledDeptOptions = ref(undefined)
const initPassword = ref(undefined)
const postOptions = ref([])
const roleOptions = ref([])
const projectCountMap = ref({})      // 存储每个用户参与的项目数量
const userPostNamesMap = ref({})     // 存储每个用户的岗位名称列表
const allPostsMap = ref({})          // 岗位ID -> 岗位信息 的映射

// 标签类型循环
const tagTypes = ['primary', 'success', 'warning', 'danger', 'info']

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
      { required: true, message: "用户账号不能为空", trigger: "blur" },
      { min: 2, max: 20, message: "用户账号长度必须介于 2 和 20 之间", trigger: "blur" }
    ],
    nickName: [{ required: true, message: "用户名称不能为空", trigger: "blur" }],
    password: [
      {
        required: () => !form.value.userId,
        message: "用户密码不能为空",
        trigger: "blur"
      },
      {
        validator: (rule, value, callback) => {
          if (!form.value.userId && !value) {
            callback(new Error("用户密码不能为空"))
          } else if (value && (value.length < 5 || value.length > 20)) {
            callback(new Error("用户密码长度必须介于 5 和 20 之间"))
          } else if (value && /[<>"'|\\]/.test(value)) {
            callback(new Error("不能包含非法字符：< > \" ' \\|"))
          } else {
            callback()
          }
        },
        trigger: "blur"
      }
    ],
    email: [{ type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] }],
    phonenumber: [
      { required: true, message: "手机号码不能为空", trigger: "blur" },
      { pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

// 获取查询表单引用
const queryRef = ref()

/** 查询用户列表 */
function getList() {
  loading.value = true
  listUser(queryParams.value).then(res => {
    loading.value = false
    userList.value = res.rows
    total.value = res.total

    // 获取每个用户参与的项目数量
    getUserProjectCounts()
    // 获取每个用户的岗位名称
    getUserPostNames()
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

/** 加载岗位列表 */
async function loadAllPosts() {
  try {
    const response = await listPost({ status: '0' })
    const posts = response.rows || []
    const postsMap = {}
    posts.forEach(post => {
      postsMap[post.postId] = post
    })
    allPostsMap.value = postsMap
  } catch (error) {
    console.error('加载岗位列表失败:', error)
  }
}

/** 获取用户的岗位名称 */
async function getUserPostNames() {
  try {
    const response = await getAllUserPost()
    const userPostList = response.data || []
    
    // 构建用户ID -> 岗位名称列表 的映射
    const namesMap = {}
    userPostList.forEach(item => {
      const userId = item.userId
      const postId = item.postId
      const post = allPostsMap.value[postId]
      
      if (post) {
        if (!namesMap[userId]) {
          namesMap[userId] = []
        }
        if (!namesMap[userId].includes(post.postName)) {
          namesMap[userId].push(post.postName)
        }
      }
    })
    
    userPostNamesMap.value = namesMap
  } catch (error) {
    console.error('获取用户岗位失败:', error)
  }
}

/** 获取用户参与的项目数量 */
async function getUserProjectCounts() {
  const userIds = userList.value.map(user => user.userId)
  if (userIds.length === 0) return
  
  try {
    // 查询所有项目成员，统计每个用户参与的项目数（按项目ID去重）
    const response = await listProjectMembers({ pageNum: 1, pageSize: 10000, isActive: 1 })
    if (response.rows) {
      const countMap = {}
      userIds.forEach(userId => {
        // 获取该用户参与的所有项目ID，然后去重统计
        const userProjects = response.rows.filter(member => 
          String(member.userId) === String(userId)
        )
        // 按项目ID去重
        const uniqueProjectIds = [...new Set(userProjects.map(m => m.projectId))]
        countMap[userId] = uniqueProjectIds.length
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

/** 获取岗位标签类型 */
function getPostTagType(index) {
  return tagTypes[index % tagTypes.length]
}

/** 获取头像完整URL */
function getAvatarUrl(avatarPath) {
  if (!avatarPath) return ''

  // 如果已经是完整URL（以http开头），直接返回
  if (avatarPath.startsWith('http')) {
    return avatarPath
  }

  // 如果是相对路径，添加基础URL前缀
  return import.meta.env.VITE_APP_BASE_API + avatarPath
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
    form.value.roleIds = [2]
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

onMounted(async () => {
  getDeptTree()
  // 先加载岗位列表
  await loadAllPosts()
  // 再加载用户列表
  getList()
  proxy.getConfigKey("sys.user.initPassword").then(response => {
    initPassword.value = response.msg
  })
})
</script>

<style scoped lang="scss">
.app-container {
  height: calc(100vh - 90px);
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

.team-list-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex: 1;
  min-height: 200px;

  :deep(.el-card__body) {
    padding: 20px;
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  :deep(.el-table-wrapper) {
    flex: 1;
    overflow-y: auto;
  }

  :deep(.el-table) {
    .el-table__cell {
      padding: 12px 0;
    }

    .el-table__header-wrapper {
      position: sticky;
      top: 0;
      z-index: 10;
      background: #fff;
    }
  }

  :deep(.el-pagination) {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
    flex-shrink: 0;
  }
}
</style>
