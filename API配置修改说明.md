# API配置修改说明

## 修改内容

### 1. 强制使用真实API
```javascript
// 修改前
const USE_MOCK = getEnvironment() === 'development'

// 修改后  
const USE_MOCK = false // 开发环境也使用真实API
```

### 2. 增强错误处理
- API调用失败时自动使用模拟数据作为兜底
- 提供详细的错误日志
- 用户友好的错误提示

### 3. 添加调试信息
- 记录API调用过程
- 显示返回数据结构
- 跟踪数据设置状态

## 当前API配置

### 请求地址
- **开发环境 (H5)**: `http://192.168.5.102:8080`
- **开发环境 (小程序)**: `http://172.31.102.128:8080`
- **生产环境**: `http://123.207.3.93:3000/dev-api`

### API端点
- **进度列表**: `GET /app/evs/projectSchedules/list`
- **验收记录**: `GET /app/evs/projectScheduleRecords/list`
- **记录详情**: `GET /app/evs/projectScheduleRecords/{recordId}`

### 请求头
- `Authorization: Bearer {token}` - 用户认证
- `X-Project-Id: {projectId}` - 当前项目ID
- `X-Device-Id: {deviceId}` - 设备标识

## 数据流程

### 1. 正常流程
```
页面加载 → 调用真实API → 返回数据 → 页面显示
```

### 2. 异常流程
```
页面加载 → 调用真实API → API失败 → 使用模拟数据 → 页面显示
```

## 预期行为

### 成功情况
1. 控制台显示: "开始调用真实API获取施工进度数据..."
2. 控制台显示: "API返回数据: [...]"
3. 控制台显示: "成功设置进度数据，共 X 项"
4. 页面显示真实的项目进度数据

### 失败情况
1. 控制台显示: "API调用失败，使用模拟数据作为兜底: [错误信息]"
2. 页面显示模拟数据
3. 用户看到错误提示

## 可能的问题

### 1. 网络连接问题
- **现象**: 请求超时或连接失败
- **解决**: 检查网络连接和服务器状态

### 2. 认证问题
- **现象**: 401 Unauthorized
- **解决**: 检查Token是否有效

### 3. 权限问题
- **现象**: 403 Forbidden
- **解决**: 检查用户是否有项目访问权限

### 4. 项目ID问题
- **现象**: 400 Bad Request 或空数据
- **解决**: 检查X-Project-Id请求头是否正确

### 5. 服务器问题
- **现象**: 500 Internal Server Error
- **解决**: 检查后端服务状态和数据库连接

## 调试方法

### 1. 检查控制台日志
```javascript
// 查看这些关键日志
"开始调用真实API获取施工进度数据..."
"API返回数据: [...]"
"成功设置进度数据，共 X 项"
```

### 2. 检查网络请求
- 打开浏览器开发者工具
- 查看Network标签
- 确认API请求状态和响应

### 3. 检查请求头
```
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
X-Project-Id: 5066c822136f45ec896d840cb725e110
X-Device-Id: xxx-xxx-xxx
```

### 4. 检查响应数据
```json
[
  {
    "id": "schedule_001",
    "stageName": "拆除工程",
    "status": "COMPLETED",
    "statusText": "已完成",
    // ... 更多字段
  }
]
```

## 回滚方案

如果API调用有问题，可以临时回滚到模拟数据：

```javascript
// 在 uni3/src/api/projectSchedule.js 中
const USE_MOCK = true // 临时使用模拟数据
```

## 后续优化

1. **缓存机制**: 添加数据缓存减少API调用
2. **重试机制**: API失败时自动重试
3. **离线支持**: 支持离线查看缓存数据
4. **性能监控**: 监控API响应时间和成功率