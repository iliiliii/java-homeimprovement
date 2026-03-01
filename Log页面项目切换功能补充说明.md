# Log 页面项目切换功能补充说明

## 问题分析

### 发现的问题
`pages/log/index` 页面在员工端使用时，无法响应在 Dashboard 页面进行的项目切换操作，导致切换项目后仍显示旧项目的数据。

### 根本原因
通过对比 `pages/design/index` 和 `pages/log/index` 两个页面的代码，发现关键差异：

**design/index.vue 有完整的项目切换响应机制：**
```javascript
// 1. 监听 store 中的项目 ID 变化
watch(() => userStore.currentProjectId, async (newId, oldId) => {
  if (newId !== oldId) {
    searchKeyword.value = '' // 清空筛选
    await initProjectId()
    loadRooms()  // 重新加载数据
  }
})

// 2. 页面显示时重新加载
onShow(async () => {
  await initProjectId()
  loadRooms()
})
```

**log/index.vue 缺少项目切换监听：**
- ❌ 没有 `watch(() => userStore.currentProjectId)` 监听
- ❌ 没有 `onShow` 生命周期处理
- ✅ 只有 `onLoad` 处理 URL 参数（仅支持直接跳转）

## 项目切换的完整流程

### 员工端项目切换流程

```
1. Dashboard 页面（StaffDashboard 组件）
   ↓
2. 员工点击项目卡片
   ↓
3. 触发 handleProjectClick → emit('select-project', project.id)
   ↓
4. Dashboard 接收事件 → handleSelectStaffProject(projectId)
   ↓
5. 调用 userStore.switchProject(projectId)
   ↓
6. 更新 userStore.currentProjectId
   ↓
7. 触发所有监听该值的 watch
   ↓
8. 各页面响应：
   - design/index: ✅ 有 watch → 自动重新加载数据
   - log/index: ❌ 没有 watch → 不会自动刷新（修复前）
```

## 实施的修复

### 修改内容

**1. 添加必要的导入**
```javascript
// 修改前
import { ref, onMounted, computed } from 'vue'
import { onPullDownRefresh, onLoad } from '@dcloudio/uni-app'

// 修改后
import { ref, onMounted, computed, watch } from 'vue'
import { onPullDownRefresh, onLoad, onShow } from '@dcloudio/uni-app'
```

**2. 添加项目切换监听**
```javascript
// 监听项目切换
watch(() => userStore.currentProjectId, async (newId, oldId) => {
  if (newId !== oldId) {
    console.log('[Log] 检测到项目切换:', oldId, '->', newId)
    await initProjectId()
    loadSchedules()
  }
})
```

**3. 添加页面显示时的数据刷新**
```javascript
// 页面显示时加载数据
onShow(async () => {
  // 确保项目ID已初始化
  await initProjectId()
  loadSchedules()
})
```

## 使用场景对比

### 场景 1：通过 URL 跳转（修复前后都支持）
```
Dashboard → 点击"查看设计" → design/index?projectId=xxx ✅
Dashboard → 点击"查看日志" → log/index?projectId=xxx ✅
```

### 场景 2：在 Dashboard 切换项目后访问

**修复前：**
```
Dashboard → 切换项目 A → 进入 log 页面 → 显示项目 A 数据 ✅
Dashboard → 切换项目 B → log 页面不刷新 → 仍显示项目 A 数据 ❌
```

**修复后：**
```
Dashboard → 切换项目 A → 进入 log 页面 → 显示项目 A 数据 ✅
Dashboard → 切换项目 B → log 页面自动刷新 → 显示项目 B 数据 ✅
```

### 场景 3：在页面内切换项目（员工端）

**修复前：**
```
在 log 页面 → 返回 Dashboard 切换项目 → 返回 log → 仍显示旧项目 ❌
```

**修复后：**
```
在 log 页面 → 返回 Dashboard 切换项目 → 返回 log → 自动显示新项目 ✅
```

## 技术细节

### watch 监听的工作原理

```javascript
watch(() => userStore.currentProjectId, async (newId, oldId) => {
  // newId: 新的项目 ID
  // oldId: 旧的项目 ID
  
  if (newId !== oldId) {
    // 1. 防止重复触发
    // 2. 只在真正变化时执行
    
    console.log('[Log] 检测到项目切换:', oldId, '->', newId)
    
    // 3. 重新初始化项目 ID（处理游客模式等特殊情况）
    await initProjectId()
    
    // 4. 重新加载数据
    loadSchedules()
  }
})
```

### onShow 的作用

```javascript
onShow(async () => {
  // 页面显示时触发（包括从其他页面返回）
  // 确保数据是最新的
  await initProjectId()
  loadSchedules()
})
```

**为什么需要 onShow：**
1. 用户可能在其他页面切换了项目
2. 返回当前页面时需要刷新数据
3. 配合 watch 形成完整的响应机制

## 其他页面检查

### 已确认支持项目切换的页面
- ✅ `pages/design/index.vue` - 已有完整的项目切换支持
- ✅ `pages/log/index.vue` - 本次修复已添加支持
- ✅ `pages/dashboard/index.vue` - 项目切换的发起页面

### 不需要项目切换的页面
- ⭕ `pages/brand/index.vue` - 展示公司信息，与项目无关
- ⭕ `pages/login/index-new.vue` - 登录页面
- ⭕ `pages/profile/index.vue` - 个人中心

## 测试建议

### 员工端测试流程

1. **基础切换测试**
   - 登录员工账号
   - 在 Dashboard 选择项目 A
   - 进入日志页面，确认显示项目 A 的数据
   - 返回 Dashboard，切换到项目 B
   - 再次进入日志页面，确认自动显示项目 B 的数据

2. **快速切换测试**
   - 在 Dashboard 快速切换多个项目
   - 进入日志页面，确认显示最后选择的项目数据

3. **页面返回测试**
   - 进入日志页面（项目 A）
   - 返回 Dashboard，切换到项目 B
   - 返回日志页面（通过导航栏返回按钮）
   - 确认自动刷新为项目 B 的数据

4. **下拉刷新测试**
   - 在日志页面下拉刷新
   - 确认数据正确刷新

### 客户端测试流程

1. **单项目客户**
   - 登录客户账号（只有一个项目）
   - 进入日志页面
   - 确认显示正确的项目数据

2. **多项目客户**
   - 登录客户账号（有多个项目）
   - 在 Dashboard 切换项目
   - 进入日志页面
   - 确认显示当前选中项目的数据

### 游客模式测试

1. **未登录游客**
   - 不登录直接访问
   - 进入日志页面
   - 确认显示演示项目数据

2. **已登录游客**
   - 使用游客账号登录
   - 进入日志页面
   - 确认显示演示项目数据

## 代码规范

### 项目切换功能的标准实现模式

对于需要响应项目切换的页面，应遵循以下模式：

```javascript
// 1. 导入必要的依赖
import { ref, computed, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getCurrentProjectId, isGuestUser } from '@/config/guest'

// 2. 初始化 store
const userStore = useUserStore()

// 3. 定义项目 ID 相关状态
const urlProjectId = ref('')
const currentProjectId = ref('')

// 4. 实现项目 ID 初始化函数
const initProjectId = async () => {
  const projectId = await getCurrentProjectId(urlProjectId.value, userStore.currentProjectId)
  
  // 验证项目ID格式（防止注入）
  if (projectId && !/^[a-fA-F0-9]{32}$/.test(projectId)) {
    console.error('[安全] 无效的项目ID格式:', projectId)
    currentProjectId.value = ''
    return
  }
  
  currentProjectId.value = projectId
  
  // 保存到 storage
  if (projectId) {
    uni.setStorageSync('currentProjectId', projectId)
  }
}

// 5. 处理 URL 参数
onLoad((options) => {
  if (options.projectId) {
    urlProjectId.value = options.projectId
    if (userStore.isStaff) {
      userStore.switchProject(options.projectId)
    }
  }
  initProjectId()
})

// 6. 页面显示时刷新
onShow(async () => {
  await initProjectId()
  loadData() // 加载页面数据
})

// 7. 监听项目切换
watch(() => userStore.currentProjectId, async (newId, oldId) => {
  if (newId !== oldId) {
    await initProjectId()
    loadData() // 重新加载数据
  }
})
```

## 总结

### 修复内容
- ✅ 添加了 `watch` 监听 `userStore.currentProjectId` 的变化
- ✅ 添加了 `onShow` 生命周期处理页面显示时的数据刷新
- ✅ 添加了必要的日志输出，便于调试

### 修复效果
- ✅ 员工端可以在 Dashboard 切换项目后，日志页面自动显示新项目的数据
- ✅ 从其他页面返回日志页面时，数据保持最新
- ✅ 与 design 页面保持一致的用户体验

### 后续建议
1. 对所有需要响应项目切换的页面进行统一检查
2. 建立项目切换功能的开发规范文档
3. 在代码审查时重点关注项目切换相关的实现

---

**修复时间：** 2026-03-02  
**修复人员：** Kiro AI Assistant  
**影响范围：** uni3/src/pages/log/index.vue  
**测试状态：** 待测试
