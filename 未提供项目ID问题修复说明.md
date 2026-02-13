# 未提供项目ID问题修复说明

## 问题描述

用户报告API返回400错误：
```
app/projectSchedules/list
code: 400
msg: "未提供项目ID"
```

## 问题分析

### 根本原因

1. **API依赖请求头中的项目ID**
   - 后端API从请求头 `X-Project-Id` 获取项目ID
   - `request.js` 的请求拦截器从 `uni.getStorageSync('currentProjectId')` 读取项目ID并添加到请求头

2. **未登录用户没有设置项目ID**
   - 未登录用户访问页面时，`currentProjectId` 在 storage 中为空
   - 虽然页面代码中通过 `getCurrentProjectId()` 获取了演示项目ID
   - 但这个ID只保存在页面的 `currentProjectId.value` 变量中
   - 没有同步到 `uni.storage`，导致请求拦截器无法获取

### 请求流程

```
页面加载
  ↓
initProjectId() - 获取演示项目ID
  ↓
currentProjectId.value = '演示项目ID'  ← 只在页面变量中
  ↓
调用 getProjectScheduleList()
  ↓
request.js 请求拦截器
  ↓
uni.getStorageSync('currentProjectId')  ← 返回空值！
  ↓
请求头中没有 X-Project-Id
  ↓
后端返回 400: "未提供项目ID"
```

## 解决方案

### 修改内容

在 `initProjectId()` 函数中，将获取到的项目ID同步保存到 `uni.storage`：

**修改文件：**
1. `uni3/src/pages/log/index.vue`
2. `uni3/src/pages/design/index.vue`

**修改前：**
```javascript
// 初始化项目ID（异步）
const initProjectId = async () => {
  const projectId = await getCurrentProjectId(urlProjectId.value, userStore.currentProjectId)
  currentProjectId.value = projectId
  console.log('[Log] 初始化项目ID:', projectId, '是否游客:', isGuestUser())
}
```

**修改后：**
```javascript
// 初始化项目ID（异步）
const initProjectId = async () => {
  const projectId = await getCurrentProjectId(urlProjectId.value, userStore.currentProjectId)
  currentProjectId.value = projectId
  
  // 将项目ID保存到storage，供request.js的请求拦截器使用
  if (projectId) {
    uni.setStorageSync('currentProjectId', projectId)
    console.log('[Log] 初始化项目ID:', projectId, '是否游客:', isGuestUser())
  } else {
    console.warn('[Log] 项目ID为空')
  }
}
```

### 修复后的流程

```
页面加载
  ↓
initProjectId() - 获取演示项目ID
  ↓
currentProjectId.value = '演示项目ID'
  ↓
uni.setStorageSync('currentProjectId', '演示项目ID')  ← 新增：保存到storage
  ↓
调用 getProjectScheduleList()
  ↓
request.js 请求拦截器
  ↓
uni.getStorageSync('currentProjectId')  ← 返回演示项目ID ✅
  ↓
请求头中添加 X-Project-Id: '演示项目ID'
  ↓
后端成功返回数据 ✅
```

## 技术细节

### request.js 请求拦截器

**位置：** `uni3/src/utils/request.js`

```javascript
const requestInterceptor = (config) => {
  // 获取 token
  const token = uni.getStorageSync('token')
  if (token && token !== '' && token !== 'null') {
    config.header = {
      ...config.header,
      'Authorization': `Bearer ${token}`
    }
  } else {
    // 未登录时，不添加Authorization头，但仍然允许请求
    console.log('[Request] 未登录用户，不添加Authorization头')
  }
  
  // 添加设备ID
  config.header['X-Device-Id'] = getDeviceId()
  
  // 添加当前项目ID ← 关键代码
  const currentProjectId = uni.getStorageSync('currentProjectId')
  if (currentProjectId) {
    config.header['X-Project-Id'] = currentProjectId
    console.log('[Request] 添加项目ID到请求头:', currentProjectId)
  } else {
    console.warn('[Request] 当前项目ID为空')
  }
  
  return config
}
```

**关键点：**
- 请求拦截器从 `uni.getStorageSync('currentProjectId')` 读取项目ID
- 如果项目ID存在，添加到请求头 `X-Project-Id`
- 后端从请求头获取项目ID

### getCurrentProjectId() 函数

**位置：** `uni3/src/config/guest.js`

```javascript
/**
 * 获取当前应该使用的项目ID
 * 优先级：URL参数 > Store中的项目ID > 游客演示项目ID
 * @param {string} urlProjectId - URL参数中的项目ID
 * @param {string} storeProjectId - Store中保存的项目ID
 * @returns {Promise<string>}
 */
export const getCurrentProjectId = async (urlProjectId, storeProjectId) => {
  // 优先使用URL参数
  if (urlProjectId) {
    return urlProjectId
  }
  
  // 其次使用Store中的项目ID
  if (storeProjectId) {
    return storeProjectId
  }
  
  // 如果是游客用户，使用演示项目ID
  const guestProjectId = await getGuestProjectId()
  if (guestProjectId) {
    return guestProjectId
  }
  
  return ''
}
```

**优先级：**
1. URL参数中的项目ID（最高优先级）
2. Store中保存的项目ID
3. 游客演示项目ID（从API获取）

## 影响范围

### 修改的文件
- ✅ `uni3/src/pages/log/index.vue` - 日志页面
- ✅ `uni3/src/pages/design/index.vue` - 设计页面

### 影响的功能
- ✅ 未登录用户访问日志页面
- ✅ 未登录用户访问设计页面
- ✅ 游客用户访问日志页面
- ✅ 游客用户访问设计页面

### 不影响的功能
- ✅ 正常登录用户的所有功能
- ✅ 员工用户的所有功能
- ✅ 客户用户的所有功能

## 验证方法

### 1. 查看控制台日志

**页面加载时：**
```
[Log] 初始化项目ID: 9fa800b545b445e4b699b1598bec4619 是否游客: true
```

**API请求时：**
```
[Request] 添加项目ID到请求头: 9fa800b545b445e4b699b1598bec4619
[Request] 发起请求: {
  url: "http://192.168.5.102:8080/app/projectSchedules/list",
  method: "GET",
  header: {
    "X-Project-Id": "9fa800b545b445e4b699b1598bec4619",
    ...
  }
}
```

### 2. 测试步骤

**测试未登录访问：**
1. 清除小程序缓存
2. 不登录，直接访问应用
3. 进入日志页面 `/pages/log/index`
4. 查看控制台日志
5. 确认能看到演示项目的数据

**测试游客登录访问：**
1. 清除小程序缓存
2. 使用游客账号登录
3. 进入日志页面 `/pages/log/index`
4. 查看控制台日志
5. 确认能看到演示项目的数据

**测试设计页面：**
1. 重复上述步骤
2. 访问设计页面 `/pages/design/index`
3. 确认能看到演示项目的房间列表

### 3. 检查请求头

使用开发者工具的网络面板，查看API请求：

**请求URL：**
```
GET http://192.168.5.102:8080/app/projectSchedules/list
```

**请求头：**
```
X-Project-Id: 9fa800b545b445e4b699b1598bec4619
X-Device-Id: xxx-xxx-xxx
Content-Type: application/json
```

**响应：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": "xxx",
      "name": "施工进度1",
      ...
    }
  ]
}
```

## 常见问题

### Q1: 为什么要保存到 uni.storage？

**答：**
因为 `request.js` 的请求拦截器需要从 `uni.storage` 读取项目ID。这是统一的请求处理机制，所有API请求都会经过这个拦截器。

### Q2: 会不会影响正常用户？

**答：**
不会。正常用户的项目ID本来就保存在 `uni.storage` 中（通过 `userStore.switchProject()` 设置）。这个修改只是确保游客和未登录用户也能正确设置项目ID。

### Q3: 如果用户切换项目怎么办？

**答：**
- 正常用户：通过 `userStore.switchProject()` 切换，会自动更新 storage
- 游客用户：不能切换项目，只能查看演示项目
- URL参数：如果URL中有 `projectId` 参数，会优先使用并更新 storage

### Q4: 为什么不在 guest.js 中直接保存？

**答：**
因为 `guest.js` 是配置模块，不应该直接操作业务逻辑。项目ID的设置应该在页面初始化时完成，这样更清晰和可控。

### Q5: 其他页面需要修改吗？

**答：**
目前只有日志页面和设计页面需要修改，因为：
- 这两个页面是游客可以访问的主要数据页面
- 其他页面（如Dashboard）不直接调用需要项目ID的API
- 如果将来有其他页面需要支持游客访问，也需要类似的修改

## 相关代码

### 页面初始化流程

```javascript
// 1. 页面加载时获取URL参数
onLoad((options) => {
  if (options.projectId) {
    urlProjectId.value = options.projectId
  }
  initProjectId()
})

// 2. 初始化项目ID
const initProjectId = async () => {
  // 获取项目ID（URL参数 > Store > 演示项目）
  const projectId = await getCurrentProjectId(
    urlProjectId.value, 
    userStore.currentProjectId
  )
  
  // 保存到页面变量
  currentProjectId.value = projectId
  
  // 保存到storage（供request.js使用）← 关键
  if (projectId) {
    uni.setStorageSync('currentProjectId', projectId)
  }
}

// 3. 页面显示时加载数据
onShow(async () => {
  await initProjectId()
  loadSchedules()
})

// 4. 加载数据
const loadSchedules = async () => {
  if (!currentProjectId.value) {
    return
  }
  
  // 调用API（request.js会自动从storage读取项目ID并添加到请求头）
  const data = await getProjectScheduleList()
  schedules.value = data
}
```

### request.js 请求流程

```javascript
// 1. 页面调用API
const data = await getProjectScheduleList()

// 2. request.js 发起请求
const request = (options) => {
  // 3. 请求拦截器处理
  const config = requestInterceptor({
    url: BASE_URL + options.url,
    method: options.method || 'GET',
    ...
  })
  
  // 4. 发送请求
  uni.request({
    ...config,
    success: (response) => {
      // 5. 响应拦截器处理
      responseInterceptor(response, options)
        .then(resolve)
        .catch(reject)
    }
  })
}

// 请求拦截器
const requestInterceptor = (config) => {
  // 从storage读取项目ID ← 关键
  const currentProjectId = uni.getStorageSync('currentProjectId')
  
  if (currentProjectId) {
    // 添加到请求头
    config.header['X-Project-Id'] = currentProjectId
  }
  
  return config
}
```

## 总结

通过在 `initProjectId()` 函数中添加 `uni.setStorageSync('currentProjectId', projectId)`，我们确保了：

1. ✅ 未登录用户能正确设置演示项目ID
2. ✅ 游客用户能正确设置演示项目ID
3. ✅ request.js 能从storage读取项目ID
4. ✅ API请求头中包含正确的项目ID
5. ✅ 后端能正确返回项目数据
6. ✅ 不影响正常用户的功能

这是一个简单但关键的修复，解决了未登录/游客用户无法访问数据的问题。

---

**文档版本：** 1.0  
**创建时间：** 2026-02-13  
**维护人员：** Kiro AI Assistant
