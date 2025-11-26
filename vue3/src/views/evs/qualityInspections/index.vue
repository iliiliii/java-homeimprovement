<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="质检标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入质检标题"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="质检结果" prop="result">
        <el-input
          v-model="queryParams.result"
          placeholder="请输入质检结果"
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
          v-hasPermi="['evs:qualityInspections:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['evs:qualityInspections:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['evs:qualityInspections:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['evs:qualityInspections:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="qualityInspectionsList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="质检ID" align="center" prop="id" />
      <el-table-column label="质检类型" align="center" prop="inspectionType" />
      <el-table-column label="质检标题" align="center" prop="title" />
      <el-table-column label="质检描述" align="center" prop="description" />
      <el-table-column label="质检结果" align="center" prop="result" />
      <el-table-column label="检查日期" align="center" prop="inspectionDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.inspectionDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['evs:qualityInspections:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['evs:qualityInspections:remove']">删除</el-button>
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

    <!-- 添加或修改质量检测对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="qualityInspectionsRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input v-model="form.projectId" placeholder="请输入项目ID" />
        </el-form-item>
        <el-form-item label="进度ID" prop="scheduleId">
          <el-input v-model="form.scheduleId" placeholder="请输入进度ID" />
        </el-form-item>
        <el-form-item label="质检标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入质检标题" />
        </el-form-item>
        <el-form-item label="质检描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="质检结果" prop="result">
          <el-input v-model="form.result" placeholder="请输入质检结果" />
        </el-form-item>
        <el-form-item label="检查日期" prop="inspectionDate">
          <el-date-picker clearable
            v-model="form.inspectionDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择检查日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="质检图片JSON" prop="images">
          <el-input v-model="form.images" type="textarea" placeholder="请输入内容" />
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

<script setup name="QualityInspections">
import { listQualityInspections, getQualityInspections, delQualityInspections, addQualityInspections, updateQualityInspections } from "@/api/evs/qualityInspections"

const { proxy } = getCurrentInstance()

const qualityInspectionsList = ref([])
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
    inspectionType: null,
    title: null,
    result: null,
  },
  rules: {
    inspectionType: [
      { required: true, message: "质检类型不能为空", trigger: "change" }
    ],
    title: [
      { required: true, message: "质检标题不能为空", trigger: "blur" }
    ],
    result: [
      { required: true, message: "质检结果不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询质量检测列表 */
function getList() {
  loading.value = true
  listQualityInspections(queryParams.value).then(response => {
    qualityInspectionsList.value = response.rows
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
    inspectionType: null,
    title: null,
    description: null,
    result: null,
    inspectionDate: null,
    images: null,
    remarks: null,
    createdAt: null,
    updatedAt: null,
    createdBy: null,
    updatedBy: null
  }
  proxy.resetForm("qualityInspectionsRef")
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
  title.value = "添加质量检测"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getQualityInspections(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改质量检测"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["qualityInspectionsRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateQualityInspections(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addQualityInspections(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认删除质量检测编号为"' + _ids + '"的数据项？').then(function() {
    return delQualityInspections(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('evs/qualityInspections/export', {
    ...queryParams.value
  }, `qualityInspections_${new Date().getTime()}.xlsx`)
}

getList()
</script>
