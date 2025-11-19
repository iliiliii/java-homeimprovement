<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="项目ID" prop="projectId">
        <el-input
          v-model="queryParams.projectId"
          placeholder="请输入项目ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="施工阶段" prop="stage">
        <el-select
          v-model="queryParams.stage"
          placeholder="请选择施工阶段"
          clearable
        >
          <el-option
            v-for="dict in project_schedule"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="阶段顺序" prop="stageOrder">
        <el-input
          v-model="queryParams.stageOrder"
          placeholder="请输入阶段顺序"
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
          v-hasPermi="['evs:projectSchedules:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['evs:projectSchedules:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['evs:projectSchedules:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['evs:projectSchedules:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="projectSchedulesList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="进度ID" align="center" prop="id" />
      <el-table-column label="项目ID" align="center" prop="projectId" />
      <el-table-column label="施工阶段" align="center" prop="stage">
        <template #default="scope">
          {{ project_schedule.find(item => item.value === scope.row.stage)?.label || scope.row.stage }}
        </template>
      </el-table-column>
      <el-table-column label="阶段顺序" align="center" prop="stageOrder" />
      <el-table-column label="阶段状态" align="center" prop="status" />
      <el-table-column label="完成度百分比" align="center" prop="completionRate" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['evs:projectSchedules:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['evs:projectSchedules:remove']">删除</el-button>
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

    <!-- 添加或修改项目进度对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="projectSchedulesRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input v-model="form.projectId" placeholder="请输入项目ID" />
        </el-form-item>
        <el-form-item label="施工阶段" prop="stage">
          <el-select v-model="form.stage" placeholder="请选择施工阶段">
            <el-option
              v-for="dict in project_schedule"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="阶段顺序" prop="stageOrder">
          <el-input v-model="form.stageOrder" placeholder="请输入阶段顺序" />
        </el-form-item>
        <el-form-item label="计划开始日期" prop="planStartDate">
          <el-date-picker clearable
            v-model="form.planStartDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择计划开始日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="计划结束日期" prop="planEndDate">
          <el-date-picker clearable
            v-model="form.planEndDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择计划结束日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="实际开始日期" prop="actualStartDate">
          <el-date-picker clearable
            v-model="form.actualStartDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择实际开始日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="实际结束日期" prop="actualEndDate">
          <el-date-picker clearable
            v-model="form.actualEndDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择实际结束日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="完成度百分比" prop="completionRate">
          <el-input v-model="form.completionRate" placeholder="请输入完成度百分比" />
        </el-form-item>
        <el-form-item label="阶段描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="创建时间" prop="createdAt">
          <el-date-picker clearable
            v-model="form.createdAt"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择创建时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="更新时间" prop="updatedAt">
          <el-date-picker clearable
            v-model="form.updatedAt"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择更新时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="创建人" prop="createdBy">
          <el-input v-model="form.createdBy" placeholder="请输入创建人" />
        </el-form-item>
        <el-form-item label="更新人" prop="updatedBy">
          <el-input v-model="form.updatedBy" placeholder="请输入更新人" />
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

<script setup name="ProjectSchedules">
import { listProjectSchedules, getProjectSchedules, delProjectSchedules, addProjectSchedules, updateProjectSchedules } from "@/api/evs/projectSchedules"
import { useDict } from '@/utils/dict'

const { proxy } = getCurrentInstance()
const { project_schedule } = useDict('project_schedule')

const projectSchedulesList = ref([])
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
    projectId: null,
    stage: null,
    stageOrder: null,
    status: null,
  },
  rules: {
    projectId: [
      { required: true, message: "项目ID不能为空", trigger: "blur" }
    ],
    stage: [
      { required: true, message: "施工阶段不能为空", trigger: "blur" }
    ],
    stageOrder: [
      { required: true, message: "阶段顺序不能为空", trigger: "blur" }
    ],
    status: [
      { required: true, message: "阶段状态不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询项目进度列表 */
function getList() {
  loading.value = true
  listProjectSchedules(queryParams.value).then(response => {
    projectSchedulesList.value = response.rows
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
    stage: null,
    stageOrder: null,
    planStartDate: null,
    planEndDate: null,
    actualStartDate: null,
    actualEndDate: null,
    status: null,
    completionRate: null,
    description: null,
    createdAt: null,
    updatedAt: null,
    createdBy: null,
    updatedBy: null
  }
  proxy.resetForm("projectSchedulesRef")
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
  title.value = "添加项目进度"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getProjectSchedules(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改项目进度"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["projectSchedulesRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateProjectSchedules(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addProjectSchedules(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认删除项目进度编号为"' + _ids + '"的数据项？').then(function() {
    return delProjectSchedules(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('evs/projectSchedules/export', {
    ...queryParams.value
  }, `projectSchedules_${new Date().getTime()}.xlsx`)
}

getList()
</script>
