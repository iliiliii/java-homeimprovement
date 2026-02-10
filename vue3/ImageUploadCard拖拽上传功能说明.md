# ImageUploadCard 拖拽上传功能说明

## 修改概述

为 `vue3/src/components/ImageUploadCard/index.vue` 组件添加了拖拽上传功能，同时保持向后兼容性，不影响现有使用场景。

## 新增功能

### 1. 拖拽上传支持
- 用户可以直接将图片文件拖拽到上传区域进行上传
- 拖拽时有视觉反馈（边框高亮、背景色变化）
- 支持单个或多个文件同时拖拽上传

### 2. 新增 Props

#### `enableDrag`
- **类型**: `Boolean`
- **默认值**: `true`
- **说明**: 是否启用拖拽上传功能
- **用法**: 
  ```vue
  <!-- 启用拖拽（默认） -->
  <ImageUploadCard v-model="fileList" />
  
  <!-- 禁用拖拽，使用传统点击上传 -->
  <ImageUploadCard v-model="fileList" :enable-drag="false" />
  ```

## 功能特性

### 拖拽交互体验
1. **默认状态**: 显示拖拽提示文字和图标
2. **悬停状态**: 边框和图标颜色变为主题色，背景色变浅
3. **拖拽进入**: 边框高亮，背景色变为浅蓝色，图标放大
4. **拖拽释放**: 自动开始上传流程

### 视觉设计
- **拖拽区域**: 120x120px 卡片式设计
- **提示文字**: 
  - 主文字: "拖拽图片到此处"
  - 副文字: "或点击上传"
- **图标**: Plus 图标，拖拽时会放大并变色
- **边框**: 虚线边框，拖拽时变为实线并高亮

### 样式状态
```scss
// 默认状态
border: 2px dashed #d9d9d9

// 悬停状态
border-color: #409eff
background-color: #f5f7fa

// 拖拽进入状态
border-color: #409eff
background-color: #ecf5ff
```

## 向后兼容性

### 完全兼容现有代码
所有现有使用 `ImageUploadCard` 的地方无需修改，拖拽功能默认启用：

```vue
<!-- 现有代码无需修改，自动支持拖拽 -->
<ImageUploadCard
  v-model="imageFileList"
  :max-count="10"
  :max-size="5"
/>
```

### 可选禁用拖拽
如果某些场景不需要拖拽功能，可以显式禁用：

```vue
<ImageUploadCard
  v-model="imageFileList"
  :enable-drag="false"
/>
```

## 使用场景

### 适合启用拖拽的场景
- ✅ 需要批量上传多张图片
- ✅ 用户体验要求较高的场景
- ✅ 桌面端应用
- ✅ 设计稿、现场照片等图片上传

### 可以禁用拖拽的场景
- 移动端应用（拖拽体验不佳）
- 单图上传场景
- 需要严格控制上传流程的场景

## 技术实现

### 核心改动
1. **Props 扩展**: 新增 `enableDrag` 属性
2. **模板更新**: 
   - 为 `el-upload` 添加 `:drag="enableDrag"` 属性
   - 添加拖拽专用的内容模板
   - 添加动态 class 绑定
3. **样式增强**: 
   - 新增拖拽状态样式
   - 添加过渡动画效果
   - 优化视觉反馈

### Element Plus 原生支持
利用 Element Plus `el-upload` 组件的原生 `drag` 属性，无需额外的拖拽事件处理逻辑。

## 测试建议

### 功能测试
1. ✅ 拖拽单个图片文件到上传区域
2. ✅ 拖拽多个图片文件到上传区域
3. ✅ 拖拽非图片文件（应显示错误提示）
4. ✅ 拖拽超大文件（应显示大小限制提示）
5. ✅ 拖拽超过数量限制的文件（应显示数量限制提示）
6. ✅ 点击上传按钮（传统方式仍然可用）
7. ✅ 禁用拖拽后的表现（`:enable-drag="false"`）

### 兼容性测试
1. ✅ 验证所有现有使用场景正常工作
2. ✅ 验证图片压缩功能正常
3. ✅ 验证上传进度显示正常
4. ✅ 验证图片预览功能正常
5. ✅ 验证图片删除功能正常

### 浏览器测试
- Chrome/Edge (推荐)
- Firefox
- Safari
- 移动端浏览器

## 现有使用位置

该组件在以下文件中被使用，所有位置都将自动获得拖拽上传功能：

1. `vue3/src/views/evs/qualityFixes/index.vue` - 质量修复图片上传
2. `vue3/src/views/evs/newsConsultation/card.vue` - 新闻资讯封面图片
3. `vue3/src/views/evs/projects/components/ProjectDesignDrafts.vue` - 项目设计稿
4. `vue3/src/views/evs/projectScheduleRecords/components/AcceptanceReportDialog.vue` - 验收报告照片
5. `vue3/src/views/evs/qualityIssues/index.vue` - 质量问题现场照片
6. `vue3/src/views/evs/qualityInspections/index.vue` - 质量检查现场照片
7. `vue3/src/views/evs/qualityInspections/components/FixSubmissionDialog.vue` - 修复提交照片
8. `vue3/src/views/evs/qualityInspections/card.vue` - 质量检查卡片

## 注意事项

1. **保持谨慎**: 该组件在多个关键业务场景中使用，任何修改都需要充分测试
2. **默认启用**: 拖拽功能默认启用，确保不影响现有用户体验
3. **可选禁用**: 提供 `enableDrag` 属性允许特定场景禁用拖拽
4. **样式一致**: 拖拽和点击上传的视觉效果保持一致
5. **错误处理**: 拖拽上传的错误处理与点击上传完全相同

## 后续优化建议

1. 可以考虑添加拖拽文件数量的实时提示
2. 可以添加拖拽文件预览功能
3. 可以优化移动端的拖拽体验
4. 可以添加拖拽排序功能（调整已上传图片的顺序）
