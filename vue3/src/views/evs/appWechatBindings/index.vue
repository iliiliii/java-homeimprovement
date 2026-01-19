<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="微信openId" prop="openId">
        <el-input
          v-model="queryParams.openId"
          placeholder="请输入微信openId"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="微信unionId" prop="unionId">
        <el-input
          v-model="queryParams.unionId"
          placeholder="请输入微信unionId"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入用户ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="绑定手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入绑定手机号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="微信昵称" prop="nickname">
        <el-input
          v-model="queryParams.nickname"
          placeholder="请输入微信昵称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="会话密钥" prop="sessionKey">
        <el-input
          v-model="queryParams.sessionKey"
          placeholder="请输入会话密钥"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="绑定时间" prop="bindTime">
        <el-date-picker clearable
          v-model="queryParams.bindTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择绑定时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="最后登录时间" prop="lastLoginTime">
        <el-date-picker clearable
          v-model="queryParams.lastLoginTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择最后登录时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['evs:appWechatBindings:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['evs:appWechatBindings:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['evs:appWechatBindings:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['evs:appWechatBindings:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="appWechatBindingsList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="id" />
      <el-table-column label="微信openId" align="center" prop="openId" />
      <el-table-column label="微信unionId" align="center" prop="unionId" />
      <el-table-column label="用户类型：customer/staff" align="center" prop="userType" />
      <el-table-column label="用户ID" align="center" prop="userId" />
      <el-table-column label="绑定手机号" align="center" prop="phone" />
      <el-table-column label="微信昵称" align="center" prop="nickname" />
      <el-table-column label="微信头像" align="center" prop="avatar" />
      <el-table-column label="会话密钥" align="center" prop="sessionKey" />
      <el-table-column label="绑定时间" align="center" prop="bindTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.bindTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最后登录时间" align="center" prop="lastLoginTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.lastLoginTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['evs:appWechatBindings:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['evs:appWechatBindings:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改微信绑定对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="appWechatBindingsRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="微信openId" prop="openId">
          <el-input v-model="form.openId" placeholder="请输入微信openId" />
        </el-form-item>
        <el-form-item label="微信unionId" prop="unionId">
          <el-input v-model="form.unionId" placeholder="请输入微信unionId" />
        </el-form-item>
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="绑定手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入绑定手机号" />
        </el-form-item>
        <el-form-item label="微信昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入微信昵称" />
        </el-form-item>
        <el-form-item label="微信头像" prop="avatar">
          <el-input v-model="form.avatar" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="会话密钥" prop="sessionKey">
          <el-input v-model="form.sessionKey" placeholder="请输入会话密钥" />
        </el-form-item>
        <el-form-item label="绑定时间" prop="bindTime">
          <el-date-picker clearable
            v-model="form.bindTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择绑定时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="最后登录时间" prop="lastLoginTime">
          <el-date-picker clearable
            v-model="form.lastLoginTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择最后登录时间">
          </el-date-picker>
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

<script setup name="AppWechatBindings">
import { listAppWechatBindings, getAppWechatBindings, delAppWechatBindings, addAppWechatBindings, updateAppWechatBindings } from "@/api/evs/appWechatBindings"

const { proxy } = getCurrentInstance()

const appWechatBindingsList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    openId: null,
    unionId: null,
    userType: null,
    userId: null,
    phone: null,
    nickname: null,
    avatar: null,
    sessionKey: null,
    bindTime: null,
    lastLoginTime: null,
  },
  rules: {
    openId: [
      { required: true, message: "微信openId不能为空", trigger: "blur" }
    ],
    userType: [
      { required: true, message: "用户类型：customer/staff不能为空", trigger: "change" }
    ],
    userId: [
      { required: true, message: "用户ID不能为空", trigger: "blur" }
    ],
    bindTime: [
      { required: true, message: "绑定时间不能为空", trigger: "blur" }
    ],
    createTime: [
      { required: true, message: "创建时间不能为空", trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询微信绑定列表 */
function getList() {
  loading.value = true
  listAppWechatBindings(queryParams.value).then(response => {
    appWechatBindingsList.value = response.rows
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
    openId: null,
    unionId: null,
    userType: null,
    userId: null,
    phone: null,
    nickname: null,
    avatar: null,
    sessionKey: null,
    bindTime: null,
    lastLoginTime: null,
    createTime: null
  }
  proxy.resetForm("appWechatBindingsRef")
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

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加微信绑定"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getAppWechatBindings(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改微信绑定"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["appWechatBindingsRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateAppWechatBindings(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addAppWechatBindings(form.value).then(response => {
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
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除微信绑定编号为"' + _ids + '"的数据项？').then(function() {
    return delAppWechatBindings(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('evs/appWechatBindings/export', {
    ...queryParams.value
  }, `appWechatBindings_${new Date().getTime()}.xlsx`)
}

getList()
</script>
