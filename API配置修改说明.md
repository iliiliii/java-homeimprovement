# API配置修改说明

## 修改内容

### 问题描述
用户报告API返回500错误：
```
code: 500
msg: "No static resource app/guest/demo-projects."
```

原因是 `guest.js` 中硬编码了API地址，没有使用 `request.js` 中的 `getBaseUrl()` 函数。

### 解决方案

#### 1. 修改 `uni3/src/config/guest.js`

**修改前：**
```javascript
// 硬编码的API地址
const response = await uni.request({
  url: 'https://hsdlp.gzcelestial.com/prod-api/app/guest/demo-projects',
  method: 'GET',
  timeout: 10000
})
```

**修改后：**
```javascript
import { getBaseUrl } from '@/utils/request'

// 使用 request.js 中的 getBaseUrl() 获取正确的API地址
const baseUrl = getBaseUrl()
const apiUrl = `${baseUrl}/app/guest/demo-projects`

const response = await uni.request({
  url: apiUrl,
  method: 'GET',
  timeout: 10000
})
```

#### 2. 优势

- ✅ 自动适配开发/生产环境
- ✅ 统一API地址管理
- ✅ 避免硬编码导致的环境问题
- ✅ 与项目其他API请求保持一致

---

## 当前状态

### 前端代码
- ✅ `guest.js` 已正确导入 `getBaseUrl`
- ✅ API地址使用动态获取
- ✅ 保留了临时降级方案（API失败时使用硬编码项目ID）
- ✅ 添加了详细的日志输出

### 后端代码
- ✅ `AppGuestConfigController` 已创建
- ✅ `GuestConfigServiceImpl` 已创建
- ✅ 健康检查接口 `/app/guest/health` 已添加
- ⚠️ 需要确认是否已部署到服务器

### 配置
- ⚠️ 需要在后台管理系统中配置字典
  - 字典类型：`guest_demo`
  - 字典数据：`projects_01` = 实际的演示项目ID

---

## 验证步骤

### 步骤1：测试健康检查接口

**开发环境：**
```bash
curl http://192.168.5.102:8080/app/guest/health
```

**生产环境：**
```bash
curl https://hsdlp.gzcelestial.com/prod-api/app/guest/health
```

**预期响应：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "status": "ok",
    "timestamp": 1234567890,
    "message": "游客配置服务正常运行"
  }
}
```

### 步骤2：测试演示项目配置接口

**开发环境：**
```bash
curl http://192.168.5.102:8080/app/guest/demo-projects
```

**生产环境：**
```bash
curl https://hsdlp.gzcelestial.com/prod-api/app/guest/demo-projects
```

**预期响应：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "projectIds": ["项目ID1", "项目ID2"],
    "defaultProjectId": "项目ID1",
    "count": 2
  }
}
```

### 步骤3：配置字典

1. 登录后台管理系统
2. 进入 **系统管理 > 字典管理**
3. 添加字典类型：
   - 字典名称：`游客演示项目`
   - 字典类型：`guest_demo`
   - 状态：正常
4. 添加字典数据：
   - 字典标签：`projects_01`
   - 字典键值：`实际的演示项目ID`（例如：`9fa800b545b445e4b699b1598bec4619`）
   - 字典排序：1
   - 状态：正常

### 步骤4：测试前端

1. 清除小程序缓存
2. 重新启动应用
3. 查看控制台日志：
   ```
   [Guest Config] 预加载配置开始
   [Guest Config] API地址: http://192.168.5.102:8080/app/guest/demo-projects
   [Guest Config] 配置加载成功: { projectIds: [...], defaultProjectId: '...', count: 1 }
   ```
4. 访问设计页面和日志页面，确认能看到数据

---

## 降级方案

如果API调用失败，系统会自动使用硬编码的项目ID作为降级方案：

```javascript
// 临时方案：使用硬编码的项目ID
console.warn('[Guest Config] API调用失败，使用硬编码的演示项目ID（临时方案）')
GUEST_DEMO_PROJECT_IDS = ['9fa800b545b445e4b699b1598bec4619']
DEFAULT_GUEST_PROJECT_ID = '9fa800b545b445e4b699b1598bec4619'
```

**注意：** 这只是临时方案，正式环境应该使用后端API。

### 删除降级代码

当后端API正常工作后，可以删除 `guest.js` 中的降级代码：

```javascript
// 删除这段代码
} catch (error) {
  console.error('[Guest Config] 配置加载异常:', error)
  
  // ===== 临时方案：使用硬编码的项目ID =====
  // TODO: 后端部署后删除这段代码
  console.warn('[Guest Config] API调用失败，使用硬编码的演示项目ID（临时方案）')
  GUEST_DEMO_PROJECT_IDS = ['9fa800b545b445e4b699b1598bec4619']
  DEFAULT_GUEST_PROJECT_ID = '9fa800b545b445e4b699b1598bec4619'
  CONFIG_LOADED = true
  // ===== 临时方案结束 =====
  
  // 通知所有等待的回调
  LOAD_CALLBACKS.forEach(callback => callback())
  LOAD_CALLBACKS = []
}
```

改为：

```javascript
} catch (error) {
  console.error('[Guest Config] 配置加载异常:', error)
  throw error // 直接抛出错误，不使用降级方案
} finally {
  CONFIG_LOADING = false
}
```

---

## 环境配置

### 开发环境
- API地址：`http://192.168.5.102:8080`
- 自动使用开发环境配置

### 生产环境
- API地址：`https://hsdlp.gzcelestial.com/prod-api`
- 自动使用生产环境配置

### 配置逻辑

```javascript
const getBaseUrl = () => {
  // 开发环境使用本地地址
  if (isDev) {
    return 'http://192.168.5.102:8080'
  }
  
  // 生产环境使用正式域名
  return 'https://hsdlp.gzcelestial.com/prod-api'
}
```

---

## 常见问题

### Q1: API返回404

**原因：** 后端代码没有部署

**解决：**
1. 确认后端代码已编译
2. 确认后端服务已重启
3. 测试健康检查接口

### Q2: API返回500

**原因：** 字典配置不存在或Service层异常

**解决：**
1. 检查字典配置是否正确
2. 查看后端日志
3. 确认Service有 `@Service` 注解

### Q3: 前端仍然使用硬编码的项目ID

**原因：** API调用失败，触发了降级方案

**解决：**
1. 检查网络连接
2. 确认API地址正确
3. 查看控制台日志

### Q4: 开发环境和生产环境使用不同的API地址

**原因：** `getBaseUrl()` 根据环境自动切换

**解决：**
- 这是正常行为，无需修改
- 开发环境：`http://192.168.5.102:8080`
- 生产环境：`https://hsdlp.gzcelestial.com/prod-api`

---

## 相关文件

### 前端
- `uni3/src/config/guest.js` - 游客配置管理
- `uni3/src/utils/request.js` - 统一请求封装
- `uni3/src/App.vue` - 应用启动时预加载配置
- `uni3/src/pages/dashboard/index.vue` - Dashboard预加载配置
- `uni3/src/pages/design/index.vue` - 设计页面
- `uni3/src/pages/log/index.vue` - 日志页面

### 后端
- `sb3/evs-home/src/main/java/com/ruoyi/app/controller/AppGuestConfigController.java`
- `sb3/evs-home/src/main/java/com/ruoyi/app/service/IGuestConfigService.java`
- `sb3/evs-home/src/main/java/com/ruoyi/app/service/impl/GuestConfigServiceImpl.java`

### 文档
- `API_500错误排查指南.md` - 详细的排查步骤
- `游客演示项目完整实施方案.md` - 完整实施方案
- `未登录访问演示项目实施说明.md` - 未登录访问说明
- `预加载演示项目配置优化说明.md` - 预加载优化说明

---

## 下一步

1. ✅ 确认后端代码已部署
2. ✅ 配置字典 `guest_demo`
3. ✅ 测试健康检查接口
4. ✅ 测试演示项目配置接口
5. ✅ 测试前端功能
6. ⚠️ 删除降级代码（API正常后）

---

**文档版本：** 1.0  
**创建时间：** 2026-02-13  
**最后更新：** 2026-02-13  
**维护人员：** Kiro AI Assistant
