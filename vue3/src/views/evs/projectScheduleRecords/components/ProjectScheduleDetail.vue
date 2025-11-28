<template>
  <el-col :span="16" class="right-col">
    <el-card shadow="never" v-if="project" class="project-detail-card">
      <!-- 固定区域：项目头部、统计卡片、整体进度条 -->
      <div class="project-detail-fixed">
        <!-- 项目头部信息 -->
        <div class="project-detail-header">
          <div class="project-title-section">
            <h3 class="project-title">{{ project.name }}</h3>
            <dict-tag :options="decoration_project_status" :value="project.status" />
          </div>
          <div class="project-info">
            <div class="info-item">
              <span class="info-label">工地地址：</span>
              <span class="info-value">{{ project.address || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">开工日期：</span>
              <span class="info-value">{{ parseTime(project.startDate, '{y}-{m}-{d}') }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">预计完工：</span>
              <span class="info-value">{{ parseTime(project.endDate, '{y}-{m}-{d}') }}</span>
            </div>
          </div>
        </div>

        <!-- 进度统计卡片 -->
        <el-row :gutter="16" class="stat-cards-row">
          <el-col :span="8">
            <el-card shadow="never" class="stat-card stat-card-blue">
              <div class="stat-content">
                <div class="stat-label">总进度</div>
                <div class="stat-value">{{ getProjectProgress(project) }}%</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="stat-card stat-card-green">
              <div class="stat-content">
                <div class="stat-label">已完成</div>
                <div class="stat-value">{{ getCompletedCount(project) }}/{{ getTotalCount(project) }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="stat-card stat-card-orange">
              <div class="stat-content">
                <div class="stat-label">进行中</div>
                <div class="stat-value">{{ getInProgressCount(project) }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 整体进度条 -->
        <div class="overall-progress-section">
          <div class="progress-header">
            <span class="progress-label">整体进度</span>
            <span class="progress-percentage">{{ getProjectProgress(project) }}%</span>
          </div>
          <el-progress
            :percentage="getProjectProgress(project)"
            :stroke-width="12"
            :show-text="false"
            style="margin-bottom: 8px;"
          />
          <div class="progress-footer">
            <span>{{ getProjectProgress(project) }}%</span>
          </div>
        </div>
      </div>

      <!-- 滚动区域：施工进度时间轴 -->
      <div class="timeline-section timeline-scrollable">
        <div class="timeline-title">施工进度时间轴</div>
        <div v-loading="loading" class="timeline-content">
          <el-timeline v-if="scheduleItems.length > 0">
            <el-timeline-item
              v-for="item in scheduleItems"
              :key="item.id"
              :color="getTimelineColor(item.status)"
              :icon="getTimelineIcon(item.status)"
              size="large"
            >
              <div class="timeline-item-content">
                <div class="timeline-item-header">
                  <span class="timeline-item-title">
                    {{ getScheduleStageName(item.stage) }}
                  </span>
                  <el-tag :type="getTimelineTagType(item.status)" size="small">
                    {{ getTimelineStatusLabel(item.status) }}
                  </el-tag>
                </div>
                <div class="timeline-item-description">{{ item.description || '暂无描述' }}</div>
                <div class="timeline-item-date">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ parseTime(item.plannedStartDate, '{y}-{m}-{d}') }}</span>
                </div>

                <!-- 验收记录展示 -->
                <div v-if="getAcceptanceRecords(item.id).length > 0" class="acceptance-records-section">
                  <div class="acceptance-records-header">
                    <span class="acceptance-label">验收记录</span>
                    <el-tag size="small" type="success">{{ getAcceptanceRecords(item.id).length }} 次验收</el-tag>
                  </div>
                  <div class="acceptance-records-list">
                    <div
                      v-for="(record, index) in getAcceptanceRecords(item.id)"
                      :key="record.id"
                      class="acceptance-record-item"
                      :class="{ 'unqualified': record.acceptanceResult === 'UNQUALIFIED' }"
                    >
                      <div class="record-header">
                        <div class="record-info">
                          <el-icon class="record-icon" :class="record.acceptanceResult">
                            <Check v-if="record.acceptanceResult === 'QUALIFIED'" />
                            <Close v-else />
                          </el-icon>
                          <span class="record-title">
                            验收 #{{ getAcceptanceRecords(item.id).length - index }}
                          </span>
                          <el-tag
                            :type="record.acceptanceResult === 'QUALIFIED' ? 'success' : 'danger'"
                            size="small"
                          >
                            {{ record.acceptanceResult === 'QUALIFIED' ? '合格' : '不合格' }}
                          </el-tag>
                        </div>
                        <span class="record-time">{{ parseTime(record.acceptanceTime, '{y}-{m}-{d} {h}:{i}') }}</span>
                      </div>
                      <div class="record-content">{{ record.acceptanceContent }}</div>
                      <div v-if="record.images && JSON.parse(record.images).length > 0" class="record-images">
                        <el-image
                          v-for="(img, imgIndex) in JSON.parse(record.images)"
                          :key="imgIndex"
                          :src="getImageUrl(img)"
                          :preview-src-list="JSON.parse(record.images).map(getImageUrl)"
                          :initial-index="imgIndex"
                          :z-index="3000"
                          fit="cover"
                          class="record-image"
                        />
                      </div>
                    </div>
                  </div>
                </div>

                <el-button
                  type="primary"
                  size="small"
                  style="margin-top: 8px;"
                  @click="$emit('acceptance-report', item)"
                >
                  <el-icon><Plus /></el-icon>
                  验收上报
                </el-button>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无施工进度" :image-size="100" />
        </div>
      </div>
    </el-card>

    <!-- 未选择项目时的提示 -->
    <el-card shadow="never" v-else class="project-detail-card project-detail-card-empty">
      <el-empty description="请从左侧选择一个项目查看详情" :image-size="120" />
    </el-card>
  </el-col>
</template>

<script setup name="ProjectScheduleDetail">
import { Calendar, Plus, Check, Close } from "@element-plus/icons-vue"
import { parseTime } from "@/utils/ruoyi"
import { listProjectScheduleRecords } from "@/api/evs/projectScheduleRecords"

const { proxy } = getCurrentInstance()
const { decoration_project_status, decoration_construction_stage } = proxy.useDict('decoration_project_status', 'decoration_construction_stage')

const props = defineProps({
  project: {
    type: Object,
    default: null
  },
  scheduleItems: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['acceptance-report'])

// 验收记录数据
const acceptanceRecords = ref([])
const recordsLoading = ref(false)

// 工具函数
function getProjectSchedules(project) {
  if (!project) return []
  return props.scheduleItems
}

function getProjectProgress(project) {
  if (!project) return 0
  const schedules = getProjectSchedules(project)
  if (!schedules.length) return 0
  const total = schedules.length
  const completed = schedules.filter(item => item.status === 'COMPLETED').length
  return total > 0 ? Math.round((completed / total) * 100) : 0
}

function getCompletedCount(project) {
  if (!project) return 0
  const schedules = getProjectSchedules(project)
  return schedules.filter(item => item.status === 'COMPLETED').length
}

function getInProgressCount(project) {
  if (!project) return 0
  const schedules = getProjectSchedules(project)
  return schedules.filter(item => item.status === 'IN_PROGRESS').length
}

function getTotalCount(project) {
  if (!project) return 0
  const schedules = getProjectSchedules(project)
  return schedules.length
}

function getTimelineColor(status) {
  const colorMap = {
    'COMPLETED': '#52c41a',
    'IN_PROGRESS': '#1677ff',
    'PLANNED': '#d9d9d9'
  }
  return colorMap[status] || '#d9d9d9'
}

function getTimelineIcon(status) {
  if (status === 'COMPLETED') {
    return 'Check'
  } else if (status === 'IN_PROGRESS') {
    return 'Clock'
  }
  return ''
}

function getTimelineTagType(status) {
  const typeMap = {
    'COMPLETED': 'success',
    'IN_PROGRESS': 'primary',
    'PLANNED': 'info'
  }
  return typeMap[status] || 'info'
}

function getTimelineStatusLabel(status) {
  const labelMap = {
    'COMPLETED': '已完成',
    'IN_PROGRESS': '进行中',
    'PLANNED': '待开始'
  }
  return labelMap[status] || '待开始'
}

function getScheduleStageName(stage) {
  const stageDict = decoration_construction_stage.value.find(dict => dict.value === stage)
  return stageDict?.label || stage
}

/** 加载验收记录 */
async function loadAcceptanceRecords(projectId) {
  if (!projectId) return

  recordsLoading.value = true
  try {
    const response = await listProjectScheduleRecords({
      projectId: projectId,
      recordType: 'ACCEPTANCE',
      pageNum: 1,
      pageSize: 100
    })
    acceptanceRecords.value = response.rows || []
  } catch (error) {
    console.error('加载验收记录失败:', error)
    proxy.$modal.msgError('加载验收记录失败')
  } finally {
    recordsLoading.value = false
  }
}

/** 获取指定进度的验收记录 */
function getAcceptanceRecords(scheduleId) {
  return acceptanceRecords.value
    .filter(record => record.scheduleId === scheduleId)
    .sort((a, b) => new Date(b.acceptanceTime) - new Date(a.acceptanceTime))
}

/** 获取���片URL */
function getImageUrl(imgPath) {
  if (!imgPath) return ''
  if (imgPath.startsWith('http')) {
    return imgPath
  }
  const baseUrl = import.meta.env.VITE_APP_BASE_API
  return baseUrl + imgPath
}

// 监听项目变化，自动加载验收记录
watch(() => props.project?.id, (newVal) => {
  if (newVal) {
    loadAcceptanceRecords(newVal)
  } else {
    acceptanceRecords.value = []
  }
}, { immediate: true })

// 暴露给父组件使用
defineExpose({
  refreshAcceptanceRecords: () => {
    if (props.project?.id) {
      loadAcceptanceRecords(props.project.id)
    }
  }
})
</script>

<style scoped lang="scss">
.right-col {
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  overflow: hidden;
}

.project-detail-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  overflow: hidden;

  :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    padding: 20px;
    box-sizing: border-box;
    overflow: hidden;
    position: relative;
    height: 0;
  }

  &.project-detail-card-empty {
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .project-detail-fixed {
    flex-shrink: 0;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e8e8e8;
    overflow: visible;
  }

  .project-detail-header {
    margin-bottom: 16px;
    padding-bottom: 0;
    border-bottom: none;

    .project-title-section {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;

      .project-title {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #303133;
      }
    }

    .project-info {
      display: flex;
      flex-wrap: wrap;
      gap: 24px;

      .info-item {
        font-size: 14px;

        .info-label {
          color: #666;
        }

        .info-value {
          color: #303133;
          font-weight: 500;
        }
      }
    }
  }

  .stat-cards-row {
    margin-bottom: 16px;

    :deep(.el-col) {
      display: flex;
      height: auto;
    }
  }

  .stat-card {
    flex: 1;
    text-align: center;
    padding: 12px 8px;
    height: auto;
    min-height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;

    :deep(.el-card__body) {
      padding: 0;
      width: 100%;
    }

    &.stat-card-blue {
      background: #e6f7ff;
      border-color: #91d5ff;
    }

    &.stat-card-green {
      background: #f6ffed;
      border-color: #b7eb8f;
    }

    &.stat-card-orange {
      background: #fff7e6;
      border-color: #ffd591;
    }

    .stat-content {
      width: 100%;
      .stat-label {
        font-size: 12px;
        color: #666;
        margin-bottom: 4px;
      }

      .stat-value {
        font-size: 18px;
        font-weight: 600;
        line-height: 1.2;
      }
    }

    &.stat-card-blue .stat-content .stat-value {
      color: #1677ff;
    }

    &.stat-card-green .stat-content .stat-value {
      color: #52c41a;
    }

    &.stat-card-orange .stat-content .stat-value {
      color: #fa8c16;
    }
  }

  .overall-progress-section {
    margin-bottom: 0;
    padding: 12px 16px;
    background: #fafafa;
    border-radius: 8px;

    .progress-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .progress-label {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
      }

      .progress-percentage {
        font-size: 14px;
        font-weight: 600;
        color: #1677ff;
      }
    }

    .progress-footer {
      text-align: center;
      font-size: 12px;
      color: #666;
      margin-top: 4px;
    }
  }

  .timeline-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;
    position: relative;
    height: 0;

    &.timeline-scrollable {
      .timeline-title {
        flex-shrink: 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 12px;
        padding-bottom: 8px;
        border-bottom: 1px solid #e8e8e8;
      }

      .timeline-content {
        flex: 1;
        overflow-y: auto;
        overflow-x: hidden;
        min-height: 0;
        padding-right: 8px;
        margin-top: 0;
        height: 100%;

        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-track {
          background: #f1f1f1;
          border-radius: 3px;
        }

        &::-webkit-scrollbar-thumb {
          background: #c1c1c1;
          border-radius: 3px;

          &:hover {
            background: #a8a8a8;
          }
        }
      }
    }

    .timeline-item-content {
      background: #fafafa;
      padding: 16px;
      border-radius: 8px;
      margin-bottom: 12px;

      .timeline-item-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .timeline-item-title {
          font-size: 15px;
          font-weight: 600;
          color: #303133;
        }
      }

      .timeline-item-description {
        color: #666;
        font-size: 14px;
        margin-bottom: 8px;
      }

      .timeline-item-date {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #999;
        margin-bottom: 8px;
      }

      // 验收记录样式
      .acceptance-records-section {
        margin-top: 12px;
        padding-top: 12px;
        border-top: 1px dashed #e8e8e8;

        .acceptance-records-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 12px;

          .acceptance-label {
            font-size: 14px;
            font-weight: 600;
            color: #303133;
          }
        }

        .acceptance-records-list {
          display: flex;
          flex-direction: column;
          gap: 12px;

          .acceptance-record-item {
            background: #fff;
            border: 1px solid #e8e8e8;
            border-radius: 6px;
            padding: 12px;
            transition: all 0.3s;

            &:hover {
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
            }

            &.unqualified {
              border-color: #ff7875;
              background: #fff2f0;

              .record-icon.QUALIFIED {
                color: #52c41a;
              }

              .record-icon.UNQUALIFIED {
                color: #ff4d4f;
              }
            }

            .record-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 8px;

              .record-info {
                display: flex;
                align-items: center;
                gap: 8px;

                .record-icon {
                  font-size: 16px;
                  color: #52c41a;

                  &.UNQUALIFIED {
                    color: #ff4d4f;
                  }
                }

                .record-title {
                  font-size: 13px;
                  font-weight: 500;
                  color: #303133;
                }
              }

              .record-time {
                font-size: 12px;
                color: #999;
              }
            }

            .record-content {
              font-size: 13px;
              color: #666;
              line-height: 1.6;
              margin-bottom: 8px;
              white-space: pre-wrap;
            }

            .record-images {
              display: flex;
              gap: 8px;
              flex-wrap: wrap;
              margin-top: 8px;

              .record-image {
                width: 60px;
                height: 60px;
                border-radius: 4px;
                cursor: pointer;
                overflow: hidden;

                :deep(.el-image__inner) {
                  transition: transform 0.3s;
                }

                &:hover {
                  :deep(.el-image__inner) {
                    transform: scale(1.05);
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
