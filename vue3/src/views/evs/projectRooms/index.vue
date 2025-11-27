<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="房间名称" prop="roomName">
        <el-input
          v-model="queryParams.roomName"
          placeholder="请输入房间名称"
          clearable
          @keyup.enter="handleQuery"
        />
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
          v-hasPermi="['evs:projectRooms:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['evs:projectRooms:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['evs:projectRooms:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['evs:projectRooms:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="projectRoomsList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="房间ID" align="center" prop="id" />
      <el-table-column label="项目ID" align="center" prop="projectId" />
      <el-table-column label="房间名称" align="center" prop="roomName" />
      <el-table-column label="房间类型" align="center" prop="roomType" />
      <el-table-column label="房间面积" align="center" prop="area" />
      <el-table-column label="房间描述" align="center" prop="description" />
      <el-table-column label="楼层信息" align="center" prop="floor" />
      <el-table-column label="朝向" align="center" prop="orientation" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['evs:projectRooms:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['evs:projectRooms:remove']">删除</el-button>
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

    <!-- 添加或修改项目房间对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="projectRoomsRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input v-model="form.projectId" placeholder="请输入项目ID" />
        </el-form-item>
        <el-form-item label="房间名称" prop="roomName">
          <el-input v-model="form.roomName" placeholder="请输入房间名称" />
        </el-form-item>
        <el-form-item label="房间面积" prop="area">
          <el-input v-model="form.area" placeholder="请输入房间面积" />
        </el-form-item>
        <el-form-item label="房间描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="楼层信息" prop="floor">
          <el-input v-model="form.floor" placeholder="请输入楼层信息" />
        </el-form-item>
        <el-form-item label="朝向" prop="orientation">
          <el-input v-model="form.orientation" placeholder="请输入朝向" />
        </el-form-item>
        <el-form-item label="关联文件ID数组" prop="fileIds">
          <el-input v-model="form.fileIds" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="设计稿URL数组" prop="designUrls">
          <el-input v-model="form.designUrls" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="施工图URL数组" prop="constructionUrls">
          <el-input v-model="form.constructionUrls" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="效果图URL数组" prop="effectUrls">
          <el-input v-model="form.effectUrls" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="其他文件URL数组" prop="otherUrls">
          <el-input v-model="form.otherUrls" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input v-model="form.sortOrder" placeholder="请输入排序" />
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

<script setup name="ProjectRooms">
import { listProjectRooms, getProjectRooms, delProjectRooms, addProjectRooms, updateProjectRooms } from "@/api/evs/projectRooms"

const { proxy } = getCurrentInstance()

const projectRoomsList = ref([])
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
    roomName: null,
    roomType: null,
  },
  rules: {
    projectId: [
      { required: true, message: "项目ID不能为空", trigger: "blur" }
    ],
    roomName: [
      { required: true, message: "房间名称不能为空", trigger: "blur" }
    ],
    roomType: [
      { required: true, message: "房间类型不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询项目房间列表 */
function getList() {
  loading.value = true
  listProjectRooms(queryParams.value).then(response => {
    projectRoomsList.value = response.rows
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
    projectId: null,
    roomName: null,
    roomType: null,
    area: null,
    description: null,
    floor: null,
    orientation: null,
    fileIds: null,
    designUrls: null,
    constructionUrls: null,
    effectUrls: null,
    otherUrls: null,
    sortOrder: null,
    createdAt: null,
    updatedAt: null,
    deletedAt: null,
    createdBy: null,
    updatedBy: null
  }
  proxy.resetForm("projectRoomsRef")
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
  title.value = "添加项目房间"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getProjectRooms(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改项目房间"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["projectRoomsRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateProjectRooms(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addProjectRooms(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认删除项目房间编号为"' + _ids + '"的数据项？').then(function() {
    return delProjectRooms(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('evs/projectRooms/export', {
    ...queryParams.value
  }, `projectRooms_${new Date().getTime()}.xlsx`)
}

getList()
</script>
