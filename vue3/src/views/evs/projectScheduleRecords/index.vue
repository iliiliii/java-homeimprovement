<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="验收标题" prop="acceptanceTitle">
        <el-input
          v-model="queryParams.acceptanceTitle"
          placeholder="请输入验收标题"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="验收结果" prop="acceptanceResult">
        <el-input
          v-model="queryParams.acceptanceResult"
          placeholder="请输入验收结果"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="验收时间" prop="acceptanceTime">
        <el-date-picker clearable
          v-model="queryParams.acceptanceTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择验收时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="验收人" prop="acceptor">
        <el-input
          v-model="queryParams.acceptor"
          placeholder="请输入验收人"
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
          v-hasPermi="['evs:projectScheduleRecords:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['evs:projectScheduleRecords:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['evs:projectScheduleRecords:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['evs:projectScheduleRecords:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="projectScheduleRecordsList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="id" />
      <el-table-column label="记录类型" align="center" prop="recordType" />
      <el-table-column label="验收标题" align="center" prop="acceptanceTitle" />
      <el-table-column label="验收结果" align="center" prop="acceptanceResult" />
      <el-table-column label="验收时间" align="center" prop="acceptanceTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.acceptanceTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="验收人" align="center" prop="acceptor" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['evs:projectScheduleRecords:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['evs:projectScheduleRecords:remove']">删除</el-button>
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

    <!-- 添加或修改进度记录对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="projectScheduleRecordsRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input v-model="form.projectId" placeholder="请输入项目ID" />
        </el-form-item>
        <el-form-item label="进度ID" prop="scheduleId">
          <el-input v-model="form.scheduleId" placeholder="请输入进度ID" />
        </el-form-item>
        <el-form-item label="现场图片JSON数组格式" prop="images">
          <el-input v-model="form.images" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="验收标题" prop="acceptanceTitle">
          <el-input v-model="form.acceptanceTitle" placeholder="请输入验收标题" />
        </el-form-item>
        <el-form-item label="验收内容">
          <editor v-model="form.acceptanceContent" :min-height="192"/>
        </el-form-item>
        <el-form-item label="验收结果" prop="acceptanceResult">
          <el-input v-model="form.acceptanceResult" placeholder="请输入验收结果" />
        </el-form-item>
        <el-form-item label="验收时间" prop="acceptanceTime">
          <el-date-picker clearable
            v-model="form.acceptanceTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择验收时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="验收人" prop="acceptor">
          <el-input v-model="form.acceptor" placeholder="请输入验收人" />
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

<script setup name="ProjectScheduleRecords">
import { listProjectScheduleRecords, getProjectScheduleRecords, delProjectScheduleRecords, addProjectScheduleRecords, updateProjectScheduleRecords } from "@/api/evs/projectScheduleRecords"

const { proxy } = getCurrentInstance()

const projectScheduleRecordsList = ref([])
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
    recordType: null,
    acceptanceTitle: null,
    acceptanceResult: null,
    acceptanceTime: null,
    acceptor: null,
  },
  rules: {
    recordType: [
      { required: true, message: "记录类型不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询进度记录列表 */
function getList() {
  loading.value = true
  listProjectScheduleRecords(queryParams.value).then(response => {
    projectScheduleRecordsList.value = response.rows
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
    scheduleId: null,
    recordType: null,
    images: null,
    acceptanceTitle: null,
    acceptanceContent: null,
    acceptanceResult: null,
    acceptanceTime: null,
    acceptor: null,
    createdAt: null,
    updatedAt: null,
    createdBy: null,
    updatedBy: null
  }
  proxy.resetForm("projectScheduleRecordsRef")
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
  title.value = "添加进度记录"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getProjectScheduleRecords(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改进度记录"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["projectScheduleRecordsRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateProjectScheduleRecords(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addProjectScheduleRecords(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认删除进度记录编号为"' + _ids + '"的数据项？').then(function() {
    return delProjectScheduleRecords(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('evs/projectScheduleRecords/export', {
    ...queryParams.value
  }, `projectScheduleRecords_${new Date().getTime()}.xlsx`)
}

getList()
</script>
