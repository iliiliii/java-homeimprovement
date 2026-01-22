# ProjectTimelineExport 组件功能扩展说明

## 功能概述

对 `vue3/src/views/evs/projects/components/ProjectTimelineExport.vue` 组件进行了功能扩展，新增了按阶段导出的功能，同时保持了原有的全量导出方式。

## 新增功能

### 1. 阶段选择器
- 添加了多选下拉框，用户可以选择要导出的特定阶段
- 下拉框显示所有项目阶段，包含阶段类型标签（设计/施工）
- 支持多选、标签折叠、清空等功能
- 占位符提示："选择导出阶段（不选则导出全部）"

### 2. 智能过滤逻辑
- **默认行为**：不选择任何阶段时，保持原有的导出方式（导出所有阶段）
- **选择阶段**：选择特定阶段后，只导出选中的阶段信息
- 使用 `filteredSchedules` 计算属性实现智能过滤

### 3. 图片显示优化
- **默认模式**（未选择阶段）：显示小图，最多4张，超出显示"+N"
- **阶段选择模式**：显示大图，所有图片完整展示
- 大图样式：宽度100%，最大300px，居中显示，保持比例

## 技术实现

### 数据结构
```javascript
// 新增数据
const selectedStages = ref([]) // 选中的阶段ID数组

// 计算属性
const availableStages = computed(() => {
  return schedules.value.map(schedule => ({
    id: schedule.id,
    name: getScheduleStageName(schedule),
    stageType: schedule.stageType
  }))
})

const filteredSchedules = computed(() => {
  if (selectedStages.value.length === 0) {
    return schedules.value // 保持原有导出方式
  }
  return schedules.value.filter(schedule => 
    selectedStages.value.includes(schedule.id)
  )
})
```

### 模板修改
1. **阶段选择器**：使用 `el-select` 组件，支持多选
2. **循环渲染**：从 `schedules` 改为 `filteredSchedules`
3. **图片显示**：根据 `selectedStages.length` 条件渲染不同样式

### 样式优化
- 调整了操作区域布局，改为垂直排列
- 添加了阶段选择器的样式
- 新增大图显示样式 `.record-image-large` 和 `.large-image`

## 使用说明

### 默认导出（保持原有功能）
1. 打开导出对话框
2. 不选择任何阶段
3. 点击"下载长图"或"预览"
4. 导出包含所有阶段的完整进度报告

### 按阶段导出（新功能）
1. 打开导出对话框
2. 在阶段选择器中选择一个或多个阶段
3. 点击"下载长图"或"预览"
4. 导出仅包含选中阶段的进度报告，图片以大图形式显示

## 兼容性保证

- ✅ 完全保持原有导出功能不变
- ✅ 新功能为可选扩展，不影响现有用户习惯
- ✅ 对话框打开时自动重置选择状态
- ✅ 所有原有样式和布局保持不变（在默认模式下）

## 文件修改清单

- `vue3/src/views/evs/projects/components/ProjectTimelineExport.vue`
  - 新增阶段选择器UI
  - 新增数据和计算属性
  - 修改图片显示逻辑
  - 新增相关CSS样式

## 测试建议

1. **默认功能测试**：不选择阶段，验证导出结果与原版本一致
2. **单阶段测试**：选择单个阶段，验证只导出该阶段信息
3. **多阶段测试**：选择多个阶段，验证导出包含所有选中阶段
4. **图片显示测试**：验证选择阶段时图片显示为大图
5. **清空测试**：选择阶段后清空，验证回到默认导出模式