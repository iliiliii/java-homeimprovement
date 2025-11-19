<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="项目名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入项目名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="客户" prop="customerId">
        <el-select
          v-model="queryParams.customerId"
          placeholder="请选择客户"
          clearable
          filterable
          style="width: 200px"
        >
          <el-option
            v-for="customer in customersList"
            :key="customer.id"
            :label="`${customer.name} (${customer.phone})`"
            :value="customer.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="项目状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择项目状态" clearable>
          <el-option
            v-for="dict in project_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 顶部操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['evs:projects:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['evs:projects:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 项目卡片网格 -->
    <el-row v-loading="loading" :gutter="16" style="margin-bottom: 16px;">
      <el-col
        v-for="project in projectsList"
        :key="project.id"
        :xs="24"
        :sm="12"
        :lg="8"
        style="margin-bottom: 16px;"
      >
        <el-card shadow="hover" style="height: 100%;">
          <!-- 卡片头部: 项目名称 + 状态标签 -->
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
              <div style="flex: 1; min-width: 0;">
                <div style="font-size: 16px; font-weight: 600; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                  {{ project.name }}
                </div>
                <div style="font-size: 13px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                  {{ project.address }}
                </div>
              </div>
              
              <el-tag  style="margin-left: 8px; flex-shrink: 0;">
                <dict-tag v-if="project.status" :options="project_status" :value="project.status" />
              </el-tag>
            </div>
          </template>

          <!-- 卡片主体 -->
          <div style="margin-bottom: 16px;">
            <!-- 客户信息 -->
            <div style="display: flex; align-items: center; margin-bottom: 8px; font-size: 13px; color: #606266;">
              <el-icon style="margin-right: 6px;"><User /></el-icon>
              <span>客户：</span>
              <el-link
                v-if="getCustomerName(project.customerId)"
                type="primary"
                :underline="false"
                @click="goToCustomer(project.customerId)"
                style="margin-left: 4px; font-size: 13px;"
              >
                {{ getCustomerName(project.customerId) }}
              </el-link>
              <span v-else style="margin-left: 4px;">{{ project.customerId }}</span>
            </div>

            <!-- 日期范围 -->
            <div style="display: flex; align-items: center; margin-bottom: 8px; font-size: 13px; color: #606266;">
              <el-icon style="margin-right: 6px;"><Calendar /></el-icon>
              <span>{{ parseTime(project.startDate, '{y}-{m}-{d}') }}</span>
              <span style="margin: 0 4px;">至</span>
              <span>{{ parseTime(project.endDate, '{y}-{m}-{d}') }}</span>
            </div>

            <!-- 房屋面积 -->
            <div v-if="project.area" style="display: flex; align-items: center; margin-bottom: 12px; font-size: 13px; color: #606266;">
              <el-icon style="margin-right: 6px;"><HomeFilled /></el-icon>
              <span>面积：{{ project.area }}㎡</span>
            </div>
          </div>

          <!-- 预算信息 -->
          <div style="margin-bottom: 12px;">
            <template v-if="project.budget && project.budget > 0">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                <span style="font-size: 14px; font-weight: 600; color: #303133;">总预算</span>
                <span style="font-size: 18px; font-weight: 600; color: #1677ff;">¥{{ formatBudget(project.budget) }}万</span>
              </div>
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; font-size: 13px; color: #909399;">
                <span>已支出</span>
                <span>¥{{ formatBudget(project.actualCost || 0) }}万</span>
              </div>
              <el-progress
                :percentage="calculateProgress(project.actualCost || 0, project.budget)"
                :stroke-width="6"
                :show-text="false"
              />
            </template>
            <div v-else style="padding: 12px; background: #fff7e6; border: 1px dashed #ffd591; border-radius: 4px; text-align: center;">
              <span style="font-size: 12px; color: #909399;">
                <el-icon style="vertical-align: middle; margin-right: 4px;"><Wallet /></el-icon>
                尚未设置预算
              </span>
            </div>
          </div>

          <!-- 卡片底部操作按钮 -->
          <template #footer>
            <div style="display: flex; justify-content: space-around; gap: 8px; margin-top: 4px;">
              <el-button
                type="primary"
                link
                size="small"
                @click="handleViewDetail(project)"
              >
                <el-icon><View /></el-icon>
                <span>查看</span>
              </el-button>
              <el-button
                type="primary"
                link
                size="small"
                @click="handleUpdate(project)"
                v-hasPermi="['evs:projects:edit']"
              >
                <el-icon><Edit /></el-icon>
                <span>编辑</span>
              </el-button>
              <el-button
                type="warning"
                link
                size="small"
                @click="handleBudgetManagement(project)"
              >
                <el-icon><Wallet /></el-icon>
                <span>预算</span>
              </el-button>
              <el-button
                type="success"
                link
                size="small"
                @click="handleProgressManagement(project)"
              >
                <el-icon><Clock /></el-icon>
                <span>进度</span>
              </el-button>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
      style="margin-top: 16px;"
    />

    <!-- 添加或修改项目信息对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body >
      <el-form ref="projectsRef" :model="form" :rules="rules" label-width="100px">
        <!-- 核心字段 - 新建和编辑都显示 -->
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="例如: 张先生家装修工程" />
        </el-form-item>

        <el-form-item label="客户" prop="customerId">
          <el-select
            v-model="form.customerId"
            placeholder="请选择客户"
            clearable
            filterable
            remote
            :remote-method="searchCustomers"
            :loading="loading"
            style="width: 100%"
          >
            <el-option
              v-for="customer in customersList"
              :key="customer.id"
              :label="`${customer.name} (${customer.phone})`"
              :value="customer.id"
            >
              <span style="float: left">{{ customer.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ customer.phone }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="工地地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>

        <el-form-item label="工地面积" prop="area">
          <el-input v-model="form.area" placeholder="请输入工地面积">
            <template #append>㎡</template>
          </el-input>
        </el-form-item>

        <el-form-item label="施工周期" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="项目描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请简要描述项目需求和特点" />
        </el-form-item>

        <!-- 编辑模式专属字段 -->
        <template v-if="isEdit">
          <el-form-item label="项目编号" prop="projectCode">
            <el-input v-model="form.projectCode" placeholder="系统自动生成" disabled />
          </el-form-item>

          <el-form-item label="预算金额" prop="budget">
            <el-input v-model="form.budget" placeholder="请输入预算金额(元)" />
          </el-form-item>

          <el-form-item label="实际费用" prop="actualCost">
            <el-input v-model="form.actualCost" placeholder="请输入实际费用(元)" />
          </el-form-item>

          <el-form-item label="实际完工日期" prop="actualEndDate">
            <el-date-picker
              clearable
              v-model="form.actualEndDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择实际完工日期"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="项目状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择项目状态" style="width: 100%">
              <el-option
                v-for="dict in project_status"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 项目详情对话框 -->
    <el-dialog
      v-model="detailOpen"
      width="1200px"
      :top="'20px'"
      append-to-body
      :show-close="true"
      class="project-detail-dialog"
    >
      <template #header>
        <div style="display: flex; align-items: center; gap: 12px;">
          <span style="font-size: 16px; font-weight: 600;">{{ currentProject.name }}</span>
          <dict-tag :options="project_status" :value="currentProject.status" />
        </div>
      </template>

      <div style="max-height: calc(90vh - 150px); overflow-y: auto; padding: 0 8px;">
        <el-space direction="vertical" :size="20" style="width: 100%;" class="project-detail-space">
          <!-- 项目设置操作 -->
          <el-card size="small" shadow="never" style="background: #fff7e6; border: 1px solid #ffd591;">
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div style="flex: 1;">
                <div style="font-weight: 600; margin-bottom: 8px;">
                  <el-icon style="vertical-align: middle;"><Setting /></el-icon>
                  <span style="margin-left: 10px; font-size: 16px;">项目设置</span>
                </div>
                <div style="font-size: 14px; color: #666;">管理项目预算、施工进度和项目信息</div>
              </div>
              <div style="flex: 2; display: flex; justify-content: flex-end; align-items: center; gap: 16px;">
                <el-button size="default" @click="handleBudgetManagement(currentProject)" style="padding: 10px 20px;">
                  <el-icon style="margin-right: 8px;"><Wallet /></el-icon>
                  管理预算
                </el-button>
                <el-button size="default" @click="handleProgressManagement(currentProject)" style="padding: 10px 20px;">
                  <el-icon style="margin-right: 8px;"><Clock /></el-icon>
                  管理进度
                </el-button>
                <el-button size="default" type="primary" @click="handleUpdate(currentProject)" style="padding: 10px 24px;">
                  <el-icon style="margin-right: 8px;"><Edit /></el-icon>
                  编辑项目
                </el-button>
              </div>
            </div>
          </el-card>

          <!-- 项目基本信息 -->
          <el-card size="small" shadow="never" style="padding: 8px;">
            <template #header>
              <div style="display: flex; align-items: center; gap: 10px;">
                <el-icon style="color: #1677ff; font-size: 18px;"><InfoFilled /></el-icon>
                <span style="font-weight: 600; font-size: 15px;">项目基本信息</span>
              </div>
            </template>
            <el-descriptions :column="3" size="default" border>
              <el-descriptions-item label="项目名称" :span="2">
                <span style="font-weight: 600;">{{ currentProject.name }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="项目状态">
                <dict-tag :options="project_status" :value="currentProject.status" />
              </el-descriptions-item>
              <el-descriptions-item label="项目编号">
                {{ currentProject.projectCode || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="工地面积">
                {{ currentProject.area ? currentProject.area + '㎡' : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="关联客户">
                <el-link
                  v-if="getCustomerName(currentProject.customerId)"
                  type="primary"
                  :underline="false"
                  @click="goToCustomer(currentProject.customerId)"
                >
                  {{ getCustomerName(currentProject.customerId) }}
                </el-link>
                <span v-else>{{ currentProject.customerId }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="工地地址" :span="3">
                {{ currentProject.address || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="开始日期">
                {{ parseTime(currentProject.startDate, '{y}-{m}-{d}') }}
              </el-descriptions-item>
              <el-descriptions-item label="预计完工">
                {{ parseTime(currentProject.endDate, '{y}-{m}-{d}') }}
              </el-descriptions-item>
              <el-descriptions-item label="实际完工">
                {{ currentProject.actualEndDate ? parseTime(currentProject.actualEndDate, '{y}-{m}-{d}') : '进行中' }}
              </el-descriptions-item>
              <el-descriptions-item label="项目描述" :span="3">
                {{ currentProject.description || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 项目预算 -->
          <el-card size="small" shadow="never" style="padding: 8px;">
            <template #header>
              <div style="display: flex; align-items: center; gap: 10px;">
                <el-icon style="color: #faad14; font-size: 18px;"><Coin /></el-icon>
                <span style="font-weight: 600; font-size: 15px;">项目预算</span>
              </div>
            </template>
            <div v-if="currentProject.budget && currentProject.budget > 0">
              <!-- 预算统计卡片 -->
              <el-row :gutter="16" style="margin-bottom: 16px;">
                <el-col :span="8">
                  <el-card size="small" shadow="never" style="background: #fafafa; height: 100%;">
                    <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 12px 0;">
                      <div style="font-size: 13px; color: #666; margin-bottom: 6px;">总预算</div>
                      <div style="font-weight: 600; color: #faad14; font-size: 18px;">
                        ¥{{ formatBudget(currentProject.budget) }}万
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card size="small" shadow="never" style="background: #fafafa; height: 100%;">
                    <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 12px 0;">
                      <div style="font-size: 13px; color: #666; margin-bottom: 6px;">已支出</div>
                      <div style="font-weight: 600; color: #faad14; font-size: 18px;">
                        ¥{{ formatBudget(currentProject.actualCost || 0) }}万
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card size="small" shadow="never" style="background: #f0f9ff; height: 100%;">
                    <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 12px 0;">
                      <div style="font-size: 13px; color: #666; margin-bottom: 6px;">使用率</div>
                      <div style="font-weight: 600; color: #1677ff; font-size: 18px;">
                        {{ calculateProgress(currentProject.actualCost || 0, currentProject.budget) }}%
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>

              <!-- 预算明细表格 -->
              <div v-if="currentProject.budgetItems && currentProject.budgetItems.length > 0">
                <el-table
                  :data="currentProject.budgetItems"
                  size="small"
                  style="margin-bottom: 16px;"
                  :show-header="true"
                >
                  <el-table-column prop="category" label="预算类别" width="30%" />
                  <el-table-column prop="amount" label="预算金额" width="30%">
                    <template #default="scope">
                      <span style="color: #faad14; font-weight: bold;">
                        ¥{{ scope.row.amount.toLocaleString() }}
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="description" label="说明" width="40%">
                    <template #default="scope">
                      <span v-if="scope.row.description">{{ scope.row.description }}</span>
                      <span v-else style="color: #999;">-</span>
                    </template>
                  </el-table-column>
                </el-table>

                <!-- 预算执行情况 -->
                <el-card size="small" shadow="never" style="background: #fff7e6; border-color: #faad14; padding: 16px;">
                  <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                    <span style="font-size: 16px; font-weight: 600;">预算执行情况</span>
                    <span style="color: #faad14; font-size: 20px; font-weight: 600;">
                      ¥{{ formatBudget(currentProject.budget) }}万
                    </span>
                  </div>
                  <el-progress
                    :percentage="calculateProgress(currentProject.actualCost || 0, currentProject.budget)"
                    :stroke-width="14"
                    :show-text="false"
                    style="margin-bottom: 12px;"
                  />
                  <div style="display: flex; justify-content: space-between; font-size: 14px;">
                    <span style="color: #666;">剩余预算</span>
                    <span :style="{ color: (currentProject.budget - (currentProject.actualCost || 0)) >= 0 ? '#52c41a' : '#ff4d4f', fontWeight: 600, fontSize: '15px' }">
                      ¥{{ formatBudget((currentProject.budget || 0) - (currentProject.actualCost || 0)) }}万
                    </span>
                  </div>
                </el-card>
              </div>
              <div v-else>
                <!-- 没有预算明细时的执行情况显示 -->
                <el-card size="small" shadow="never" style="background: #fff7e6; border-color: #faad14; padding: 16px;">
                  <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                    <span style="font-size: 16px; font-weight: 600;">预算执行情况</span>
                    <span style="color: #faad14; font-size: 20px; font-weight: 600;">
                      ¥{{ formatBudget(currentProject.budget) }}万
                    </span>
                  </div>
                  <el-progress
                    :percentage="calculateProgress(currentProject.actualCost || 0, currentProject.budget)"
                    :stroke-width="14"
                    :show-text="false"
                    style="margin-bottom: 12px;"
                  />
                  <div style="display: flex; justify-content: space-between; font-size: 14px;">
                    <span style="color: #666;">剩余预算</span>
                    <span :style="{ color: (currentProject.budget - (currentProject.actualCost || 0)) >= 0 ? '#52c41a' : '#ff4d4f', fontWeight: 600, fontSize: '15px' }">
                      ¥{{ formatBudget((currentProject.budget || 0) - (currentProject.actualCost || 0)) }}万
                    </span>
                  </div>
                </el-card>
              </div>
            </div>
            <div v-else style="text-align: center; padding: 20px 0;">
              <span style="color: #999;">暂无预算信息</span>
            </div>
          </el-card>

          <!-- 项目进度 -->
          <el-card size="small" shadow="never" v-if="currentProject.progress !== undefined" style="padding: 8px;">
            <template #header>
              <div style="display: flex; align-items: center; gap: 10px;">
                <el-icon style="color: #52c41a; font-size: 18px;"><TrendCharts /></el-icon>
                <span style="font-weight: 600; font-size: 15px;">项目进度</span>
              </div>
            </template>

            <!-- 进度统计卡片 -->
            <el-row :gutter="16" style="margin-bottom: 16px;">
              <el-col :span="6">
                <el-card size="small" shadow="never" style="text-align: center; background: #f0f5ff; height: 100%;">
                  <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 16px 8px;">
                    <div style="font-size: 28px; font-weight: bold; color: #1677ff; margin-bottom: 8px;">{{ currentProject.progress || 0 }}%</div>
                    <div style="font-size: 13px; color: #666;">完成进度</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card size="small" shadow="never" style="text-align: center; background: #f6ffed; height: 100%;">
                  <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 16px 8px;">
                    <div style="font-size: 20px; font-weight: bold; color: #52c41a; margin-bottom: 8px;">
                      <dict-tag :options="project_status" :value="currentProject.status" />
                    </div>
                    <div style="font-size: 13px; color: #666;">当前状态</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card size="small" shadow="never" style="text-align: center; background: #fff7e6; height: 100%;">
                  <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 16px 8px;">
                    <div style="font-size: 16px; font-weight: bold; color: #fa8c16; margin-bottom: 8px;">
                      {{ currentProject.actualEndDate ? parseTime(currentProject.actualEndDate, '{y}-{m}-{d}') : '进行中' }}
                    </div>
                    <div style="font-size: 13px; color: #666;">实际完工</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card size="small" shadow="never" style="text-align: center; background: #f9f0ff; height: 100%;">
                  <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 16px 8px;">
                    <div style="font-size: 16px; font-weight: bold; color: #722ed1; margin-bottom: 8px;">
                      {{ calculateDaysRemaining(currentProject.endDate, currentProject.actualEndDate) }}
                    </div>
                    <div style="font-size: 13px; color: #666;">剩余天数</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 施工进度时间轴 -->
            <div v-if="currentProject.timeline && currentProject.timeline.length > 0">
              <div style="margin-bottom: 12px;">
                <span style="font-weight: 600; font-size: 14px; color: #666;">施工进度时间轴</span>
              </div>
              <el-timeline style="max-height: 300px; overflow-y: auto;">
                <el-timeline-item
                  v-for="item in currentProject.timeline"
                  :key="item.id"
                  :color="item.status === 'completed' ? '#52c41a' : item.status === 'inProgress' ? '#1677ff' : '#d9d9d9'"
                  :icon="getTimelineIcon(item.status)"
                  size="small"
                >
                  <div style="margin-bottom: 8px;">
                    <span style="font-weight: 600; font-size: 14px; margin-right: 8px;">{{ item.title }}</span>
                    <el-tag :color="getTimelineStatusConfig(item.status).color" size="small">
                      {{ getTimelineStatusConfig(item.status).label }}
                    </el-tag>
                  </div>
                  <div style="color: #666; font-size: 13px; margin-bottom: 4px;">{{ item.description }}</div>
                  <div style="font-size: 12px; color: #999;">
                    <el-icon style="vertical-align: middle; margin-right: 4px;"><Calendar /></el-icon>
                    {{ item.date }}
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
            <div v-else style="text-align: center; padding: 20px 0; color: #999; font-size: 13px;">
              暂无施工进度详情，请点击"管理进度"添加施工阶段
            </div>
          </el-card>
        </el-space>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 预算管理对话框 -->
    <el-dialog
      v-model="budgetOpen"
      width="900px"
      append-to-body
      :show-close="true"
      :close-on-click-modal="false"
      class="project-budget-dialog"
    >
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon style="color: #faad14; font-size: 16px;"><Wallet /></el-icon>
          <span>{{ currentBudgetProject.name }} - 预算管理</span>
        </div>
      </template>

      <div style="max-height: calc(90vh - 150px); overflow-y: auto; padding: 0 8px;">
        <ProjectBudget
          v-if="currentBudgetProject"
          :project="currentBudgetProject"
          @save="handleSaveBudget"
        />
      </div>

      <!-- 预算管理对话框不需要footer，通过组件内部操作关闭 -->
    </el-dialog>

    <!-- 进度管理对话框 -->
    <el-dialog
      v-model="progressOpen"
      width="900px"
      append-to-body
      :show-close="true"
      :close-on-click-modal="false"
      class="project-progress-dialog"
    >
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon style="color: #1677ff; font-size: 16px;"><Clock /></el-icon>
          <span>{{ currentProgressProject.name }} - 施工进度管理</span>
        </div>
      </template>

      <div style="max-height: calc(90vh - 150px); overflow-y: auto; padding: 0 8px;">
        <ProjectProgress
          v-if="currentProgressProject"
          :project="currentProgressProject"
          @save="handleSaveProgress"
        />
      </div>

      <!-- 进度管理对话框不需要footer，通过组件内部操作关闭 -->
    </el-dialog>
  </div>
</template>

<script setup name="Projects">
import { listProjects, getProjects, delProjects, addProjects, updateProjects } from "@/api/evs/projects"
import { listCustomers } from "@/api/evs/customers"
import { useRouter } from 'vue-router'
import ProjectProgress from './components/ProjectProgress.vue'
import ProjectBudget from './components/ProjectBudget.vue'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { project_status } = proxy.useDict('project_status')

const projectsList = ref([])
const customersList = ref([])
const customerMap = ref(new Map())
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

// 详情弹窗
const detailOpen = ref(false)
const currentProject = ref({})

// 预算管理弹窗
const budgetOpen = ref(false)
const currentBudgetProject = ref({})

// 进度管理弹窗
const progressOpen = ref(false)
const currentProgressProject = ref({})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    customerId: null,
    address: null,
    status: null,
  },
  rules: {
    name: [
      { required: true, message: "项目名称不能为空", trigger: "blur" }
    ],
    customerId: [
      { required: true, message: "请选择客户", trigger: "change" }
    ],
    address: [
      { required: true, message: "项目地址不能为空", trigger: "blur" }
    ],
    area: [
      { required: true, message: "房屋面积不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

// 判断是否为编辑模式
const isEdit = computed(() => form.value.id != null)

// 时间轴状态配置（用于详情弹窗显示）
const timelineStatusConfig = {
  pending: { label: '待开始', color: '' },
  inProgress: { label: '进行中', color: 'primary' },
  completed: { label: '已完成', color: 'success' }
}

// 获取时间轴状态配置
function getTimelineStatusConfig(status) {
  return timelineStatusConfig[status] || timelineStatusConfig.pending
}

// 获取时间轴图标
function getTimelineIcon(status) {
  switch (status) {
    case 'completed':
      return 'Check'
    case 'inProgress':
      return 'Clock'
    default:
      return ''
  }
}

/** 查询项目信息列表 */
function getList() {
  loading.value = true
  listProjects(queryParams.value).then(response => {
    projectsList.value = response.rows
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
    name: null,
    customerId: null,
    projectCode: null,
    description: null,
    address: null,
    area: null,
    budget: null,
    actualCost: null,
    startDate: null,
    endDate: null,
    actualEndDate: null,
    status: null,
    priority: null,
    progress: null,
    isActive: null,
    createdAt: null,
    updatedAt: null,
    deletedAt: null,
    createdBy: null,
    updatedBy: null,
    deletedBy: null
  }
  proxy.resetForm("projectsRef")
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
  title.value = "添加项目信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getProjects(_id).then(response => {
    form.value = response.data
    // 处理日期范围字段
    if (form.value.startDate && form.value.endDate) {
      form.value.dateRange = [form.value.startDate, form.value.endDate]
    }
    open.value = true
    title.value = "修改项目信息"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["projectsRef"].validate(valid => {
    if (valid) {
      // 处理日期范围字段
      const submitData = { ...form.value }
      if (submitData.dateRange && submitData.dateRange.length === 2) {
        submitData.startDate = submitData.dateRange[0]
        submitData.endDate = submitData.dateRange[1]
        delete submitData.dateRange
      }

      if (form.value.id != null) {
        updateProjects(submitData).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addProjects(submitData).then(response => {
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
  proxy.$modal.confirm('是否确认删除项目信息编号为"' + _ids + '"的数据项？').then(function() {
    return delProjects(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('evs/projects/export', {
    ...queryParams.value
  }, `projects_${new Date().getTime()}.xlsx`)
}

/** 获取客户列表 */
function getCustomersList() {
  listCustomers({ pageNum: 1, pageSize: 1000 }).then(response => {
    customersList.value = response.rows || []
    // 构建客户映射，用于快速查找客户名称
    const map = new Map()
    customersList.value.forEach(customer => {
      map.set(customer.id, customer.name)
    })
    customerMap.value = map
  })
}

/** 搜索客户 */
function searchCustomers(query) {
  if (query) {
    listCustomers({ name: query, phone: query, pageNum: 1, pageSize: 50 }).then(response => {
      customersList.value = response.rows || []
    })
  } else {
    getCustomersList()
  }
}

/** 获取客户名称 */
function getCustomerName(customerId) {
  return customerMap.value.get(customerId) || ''
}

/** 跳转到客户详情页 */
function goToCustomer(customerId) {
  if (customerId) {
    // 使用Vue Router跳转到客户管理页面，并传递客户ID参数
    router.push({
      path: '/evs/customers',
      query: { id: customerId }
    })
  }
}

/** 格式化预算金额(转为万元) */
function formatBudget(amount) {
  if (!amount) return '0.00'
  return (amount / 10000).toFixed(2)
}

/** 计算预算进度百分比 */
function calculateProgress(actual, total) {
  if (!total || total === 0) return 0
  const percent = (actual / total) * 100
  return Math.min(Math.round(percent), 100)
}

/** 计算剩余天数 */
function calculateDaysRemaining(endDate, actualEndDate) {
  if (actualEndDate) {
    // 如果有实际完工日期，显示"已完成"
    return "已完成"
  }
  if (!endDate) {
    return "未设置"
  }
  const today = new Date()
  const end = new Date(endDate)
  const diffTime = end - today
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays < 0) {
    return `逾期${Math.abs(diffDays)}天`
  } else if (diffDays === 0) {
    return "今日到期"
  } else {
    return `${diffDays}天`
  }
}

/** 查看项目详情 */
function handleViewDetail(project) {
  currentProject.value = project
  detailOpen.value = true
}

/** 预算管理 */
function handleBudgetManagement(project) {
  currentBudgetProject.value = project
  budgetOpen.value = true
}

/** 处理预算保存事件 */
function handleSaveBudget(updateData) {
  updateProjects(updateData).then(response => {
    proxy.$modal.msgSuccess("预算已保存")
    budgetOpen.value = false
    getList() // 刷新项目列表
  }).catch(error => {
    proxy.$modal.msgError("保存失败：" + (error.message || "未知错误"))
  })
}

/** 进度管理 */
function handleProgressManagement(project) {
  currentProgressProject.value = project
  progressOpen.value = true
}

/** 处理进度保存事件 */
function handleSaveProgress(updateData) {
  updateProjects(updateData).then(response => {
    proxy.$modal.msgSuccess("进度已保存")
    progressOpen.value = false
    getList() // 刷新项目列表
  }).catch(error => {
    proxy.$modal.msgError("保存失败：" + (error.message || "未知错误"))
  })
}

// 初始化
getList()
getCustomersList()
</script>


<style lang="scss">
// 项目详情对话框和预算对话框中的 el-space__item 宽度设置为 100%
.project-budget-dialog .el-dialog__body .el-space.el-space--vertical > .el-space__item {
  width: 100% !important;
  flex-basis: 100% !important;
  max-width: 100% !important;
}

.project-detail-dialog .el-dialog__body .el-space.el-space--vertical > .el-space__item {
  width: 100% !important;
  flex-basis: 100% !important;
  max-width: 100% !important;
}

// 确保预算对话框中的 table 宽度为 100%
.project-budget-dialog .el-dialog__body .el-table {
  width: 100% !important;
}

// 确保 table 元素本身也有正确的宽度（Element Plus 会在 table 元素上设置内联样式）
// 需要覆盖 Element Plus 可能设置的 100px 默认宽度
.project-budget-dialog .el-dialog__body .el-table table.el-table__header,
.project-budget-dialog .el-dialog__body .el-table table.el-table__body,
.project-budget-dialog .el-dialog__body .el-table table {
  width: 100% !important;
  min-width: 100% !important;
}

// 确保 el-space__item 内的所有子元素都能正确继承宽度
.project-budget-dialog .el-dialog__body .el-space__item > * {
  width: 100%;
  box-sizing: border-box;
}

// 特别针对 el-table 的包装器
.project-budget-dialog .el-dialog__body .el-space__item .el-table {
  width: 100% !important;
  min-width: 100% !important;
}

// 进度对话框样式
.project-progress-dialog .el-dialog__body .el-space.el-space--vertical > .el-space__item {
  width: 100% !important;
  flex-basis: 100% !important;
  max-width: 100% !important;
}

// 确保进度对话框中的时间轴样式与 TSX 一致
.project-progress-dialog .el-timeline {
  padding-left: 0;
}

.project-progress-dialog .el-timeline-item__content {
  padding-left: 20px;
}

// 确保进度对话框中的所有内容区域宽度为 100%
.project-progress-dialog .el-dialog__body .el-space__item > * {
  width: 100%;
  box-sizing: border-box;
}

// Dialog 滚动容器优化
.project-budget-dialog .el-dialog__body > div[style*="overflow-y: auto"],
.project-progress-dialog .el-dialog__body > div[style*="overflow-y: auto"] {
  box-sizing: border-box;
}

// 确保内容在滚动容器中正确显示
.project-budget-dialog .project-budget-container,
.project-progress-dialog .project-progress-container {
  padding: 8px 0;
  width: 100%;
  box-sizing: border-box;
}
</style>
