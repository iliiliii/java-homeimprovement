# API路径调整说明

## 修改内容

### 后端Controller路径调整

**修改前:**
```java
@RestController
@RequestMapping("/app/evs")
public class AppProjectScheduleController {
```

**修改后:**
```java
@RestController
@RequestMapping("/app")
public class AppProjectScheduleController {
```

### 前端API调用路径调整

**修改前:**
```javascript
get('/app/evs/projectSchedules/list')
get('/app/evs/projectScheduleRecords/list', params)
get(`/app/evs/projectScheduleRecords/${recordId}`)
```

**修改后:**
```javascript
get('/app/projectSchedules/list')
get('/app/projectScheduleRecords/list', params)
get(`/app/projectScheduleRecords/${recordId}`)
```

## 最终API端点

### 1. 获取项目进度列表
- **路径**: `GET /app/projectSchedules/list`
- **完整URL**: `http://192.168.5.102:8080/app/projectSchedules/list`
- **描述**: 获取当前项目的所有施工阶段进度

### 2. 获取验收记录列表
- **路径**: `GET /app/projectScheduleRecords/list`
- **完整URL**: `http://192.168.5.102:8080/app/projectScheduleRecords/list`
- **参数**: 
  - `scheduleId` (可选): 进度ID
  - `page`: 页码
  - `pageSize`: 页大小

### 3. 获取验收记录详情
- **路径**: `GET /app/projectScheduleRecords/{recordId}`
- **完整URL**: `http://192.168.5.102:8080/app/projectScheduleRecords/{recordId}`
- **描述**: 获取指定验收记录的详细信息

## 请求头要求

所有接口都需要以下请求头：

```
Authorization: Bearer {token}
X-Project-Id: {projectId}
X-Device-Id: {deviceId}
```

## 路径简化的好处

1. **更简洁**: 去掉了中间的 `evs/` 层级
2. **更直观**: 路径直接反映资源结构
3. **更统一**: 与其他小程序接口保持一致的命名规范
4. **更易维护**: 减少路径层级，降低维护复杂度

## 影响范围

### 后端
- ✅ `AppProjectScheduleController.java` - 已修改

### 前端
- ✅ `uni3/src/api/projectSchedule.js` - 已修改

### 文档
- ✅ API文档需要更新路径信息
- ✅ 接口测试工具需要更新端点

## 测试验证

修改完成后，需要验证：

1. **后端编译**: 确保Controller路径修改后编译正常
2. **前端调用**: 确保API调用使用新路径
3. **功能测试**: 验证数据加载功能正常
4. **错误处理**: 确认错误处理机制正常工作

## 兼容性说明

这是一个破坏性变更，需要确保：
- 前后端同时部署
- 旧版本客户端需要更新
- API文档同步更新

## 回滚方案

如果需要回滚，可以：

1. **后端回滚**:
```java
@RequestMapping("/app/evs")
```

2. **前端回滚**:
```javascript
get('/app/evs/projectSchedules/list')
```

## 部署注意事项

1. 确保后端服务重启后新路径生效
2. 前端需要重新编译部署
3. 清除可能的API缓存
4. 更新相关文档和测试用例