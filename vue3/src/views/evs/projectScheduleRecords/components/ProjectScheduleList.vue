<template>
  <el-col :span="8" class="left-col">
    <el-card shadow="never" class="project-list-card">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon style="color: #52c41a;"><CircleCheck /></el-icon>
          <span style="font-weight: 600;">进行中的项目</span>
          <el-badge :value="projects.length" class="item" />
        </div>
      </template>
      <div class="project-list">
        <div
          v-for="project in projects"
          :key="project.id"
          class="project-item"
          :class="{ active: selectedProject?.id === project.id }"
          @click="$emit('select', project)"
        >
          <div class="project-item-header">
            <div class="project-name">{{ project.name }}</div>
            <dict-tag :options="decoration_project_status" :value="project.status" size="small" />
          </div>
          <div class="project-address">
            <el-icon><Location /></el-icon>
            <span>{{ project.address || '未设置地址' }}</span>
          </div>
          <div class="project-progress-info">
            <el-progress
              :percentage="project.progressRate || 0"
              :stroke-width="8"
              :show-text="true"
              :format="(percentage) => `${percentage}%`"
            >
              <div class="progress-summary">
                已完成 {{ project.completedSchedules || 0 }}/{{ project.totalSchedules || 0 }} · 进行中 {{ project.inProgressSchedules || 0 }}
              </div>
            </el-progress>
          </div>
        </div>
        <el-empty v-if="projects.length === 0" description="暂无进行中的项目" :image-size="100" />
      </div>
    </el-card>
  </el-col>
</template>

<script setup name="ProjectScheduleList">
import { CircleCheck, Location } from "@element-plus/icons-vue"

const { proxy } = getCurrentInstance()
const { decoration_project_status } = proxy.useDict('decoration_project_status')

defineProps({
  projects: {
    type: Array,
    default: () => []
  },
  selectedProject: {
    type: Object,
    default: null
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['select'])
</script>

<style scoped lang="scss">
.left-col {
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  overflow: hidden;
}

.project-list-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  overflow: hidden;

  :deep(.el-card__header) {
    flex-shrink: 0;
    padding: 16px;
  }

  :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    padding: 0;
    overflow-y: auto;
    overflow-x: hidden;
    position: relative;
    height: 0;

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

  .project-list {
    flex: 1;
    min-height: 0;
    position: relative;
  }

  .project-item {
    padding: 16px;
    margin-bottom: 12px;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    background: #fff;

    &:hover {
      border-color: #1677ff;
      box-shadow: 0 2px 8px rgba(22, 119, 255, 0.1);
    }

    &.active {
      border-color: #1677ff;
      background: #f0f5ff;
      box-shadow: 0 2px 8px rgba(22, 119, 255, 0.2);
    }

    .project-item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .project-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .project-address {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #666;
      margin-bottom: 12px;
    }

    .project-progress-info {
      .progress-summary {
        margin-top: 8px;
        font-size: 12px;
        color: #999;
      }
    }
  }
}
</style>
