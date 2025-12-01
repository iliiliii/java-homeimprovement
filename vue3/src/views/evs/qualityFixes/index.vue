<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="问题ID" prop="qualityIssuesId">
        <el-input
          v-model="queryParams.qualityIssuesId"
          placeholder="请输入问题ID"
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
          v-hasPermi="['evs:qualityFixes:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['evs:qualityFixes:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['evs:qualityFixes:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['evs:qualityFixes:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="qualityFixesList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="修复ID" align="center" prop="id" />
      <el-table-column label="问题ID" align="center" prop="qualityIssuesId" />
      <el-table-column label="修复描述" align="center" prop="fixDescription" />
      <el-table-column label="修复图片JSON" align="center" prop="images" />
      <el-table-column label="修复状态(OPEN:未解决、IN_PROGRESS:解决中、RESOLVED:已解决)" align="center" prop="status" />
      <el-table-column label="修复时间" align="center" prop="fixedAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.fixedAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="验收时间" align="center" prop="verifiedAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.verifiedAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['evs:qualityFixes:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['evs:qualityFixes:remove']">删除</el-button>
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

    <!-- 添加或修改问题修复对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="qualityFixesRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="问题ID" prop="qualityIssuesId">
          <el-input v-model="form.qualityIssuesId" placeholder="请输入问题ID" />
        </el-form-item>
        <el-form-item label="修复描述" prop="fixDescription">
          <el-input v-model="form.fixDescription" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="修复图片JSON" prop="images">
          <el-input v-model="form.images" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="修复时间" prop="fixedAt">
          <el-date-picker clearable
            v-model="form.fixedAt"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择修复时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="验收时间" prop="verifiedAt">
          <el-date-picker clearable
            v-model="form.verifiedAt"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择验收时间">
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

<script setup name="QualityFixes">
import { listQualityFixes, getQualityFixes, delQualityFixes, addQualityFixes, updateQualityFixes } from "@/api/evs/qualityFixes"

const { proxy } = getCurrentInstance()

const qualityFixesList = ref([])
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
    qualityIssuesId: null,
  },
  rules: {
    qualityIssuesId: [
      { required: true, message: "问题ID不能为空", trigger: "blur" }
    ],
    fixDescription: [
      { required: true, message: "修复描述不能为空", trigger: "blur" }
    ],
    images: [
      { required: true, message: "修复图片JSON不能为空", trigger: "blur" }
    ],
    status: [
      { required: true, message: "修复状态(OPEN:未解决、IN_PROGRESS:解决中、RESOLVED:已解决)不能为空", trigger: "change" }
    ],
    fixedAt: [
      { required: true, message: "修复时间不能为空", trigger: "blur" }
    ],
    verifiedAt: [
      { required: true, message: "验收时间不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询问题修复列表 */
function getList() {
  loading.value = true
  listQualityFixes(queryParams.value).then(response => {
    qualityFixesList.value = response.rows
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
    qualityIssuesId: null,
    fixDescription: null,
    images: null,
    status: null,
    fixedAt: null,
    verifiedAt: null,
    createdAt: null,
    updatedAt: null,
    createdBy: null,
    updatedBy: null
  }
  proxy.resetForm("qualityFixesRef")
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
  title.value = "添加问题修复"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getQualityFixes(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改问题修复"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["qualityFixesRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateQualityFixes(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addQualityFixes(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认删除问题修复编号为"' + _ids + '"的数据项？').then(function() {
    return delQualityFixes(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('evs/qualityFixes/export', {
    ...queryParams.value
  }, `qualityFixes_${new Date().getTime()}.xlsx`)
}

getList()
</script>
