# 智享家Pro - 多角色小程序技术设计方案（最终版）

> 基于现有 evs-home 模块表结构设计，复用现有业务表，最小化新增表

## 目录

1. [架构设计概述](#1-架构设计概述)
2. [多角色认证与权限设计](#2-多角色认证与权限设计)
3. [功能模块与表映射](#3-功能模块与表映射)
4. [API接口设计](#4-api接口设计)
5. [数据库设计](#5-数据库设计)
6. [前端架构设计](#6-前端架构设计)
7. [字典配置设计](#7-字典配置设计)
8. [开发计划](#8-开发计划)

---

## 1. 架构设计概述

### 1.1 项目定位

**智享家Pro小程序** 是一个多角色应用，支持客户和员工两种角色：

- **客户角色**：查看项目信息、设计方案、施工进度、预算等
- **员工角色**：除客户功能外，还包括工地巡视、问题上报、整改记录等

### 1.2 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      uni3 小程序前端（多角色）                     │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  统一登录入口                                             │   │
│  │  - 微信登录（优先）                                        │   │
│  │  - 短信验证码登录                                          │   │
│  │  - 密码登录                                               │   │
│  └──────────────────────────────────────────────────────────┘   │
│                          ↓                                       │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  角色识别与路由分发                                        │   │
│  │  - 客户 → 客户功能页面                                     │   │
│  │  - 员工 → 员工功能页面                                     │   │
│  └──────────────────────────────────────────────────────────┘   │
│                          ↓                                       │
│  ┌────────────────────┬────────────────────────────────────┐   │
│  │  客户功能模块       │      员工功能模块                    │   │
│  │  - 项目概况        │      - 项目概况                      │   │
│  │  - 设计方案        │      - 设计方案                      │   │
│  │  - 施工排期        │      - 施工排期                      │   │
│  │  - 预算管理        │      - 预算管理                      │   │
│  │  - 质检记录        │      - 质检记录                      │   │
│  │  - 个人中心        │      - 工地巡视（专属）              │   │
│  │                    │      - 问题上报（专属）              │   │
│  │                    │      - 整改记录（专属）              │   │
│  │                    │      - 个人中心                      │   │
│  └────────────────────┴────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                          ↓ HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                   sb3/evs-home 后端服务                           │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              小程序API层 (com.ruoyi.app)                 │    │
│  │  - AppAuthController      (统一登录认证)                 │    │
│  │  - AppProjectController   (项目信息)                     │    │
│  │  - AppDesignController    (设计方案 - project_rooms)     │    │
│  │  - AppScheduleController  (施工排期 - project_schedules) │    │
│  │  - AppBudgetController    (预算管理 - project_budgets)   │    │
│  │  - AppQualityController   (质检管理 - quality_*)         │    │
│  │  - AppInspectionController (工地巡视 - schedule_records) │    │
│  │  - AppIssueController     (问题上报 - quality_issues)    │    │
│  │  - AppRepairController    (整改记录 - quality_fixes)     │    │
│  └─────────────────────────────────────────────────────────┘    │
│                          ↓                                       │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │          业务服务层 (复用现有 com.ruoyi.web.service)      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                          ↓                                       │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │          数据访问层 (复用现有 com.ruoyi.web.mapper)       │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                          ↓
                    ┌──────────┐
                    │  MySQL   │
                    └──────────┘
```



### 1.3 设计原则

1. **最大化复用现有表结构**：不新增业务表，直接使用现有表
2. **最小化新增表**：只新增认证和审计相关的5张表
3. **清晰的角色隔离**：客户和员工功能明确区分
4. **统一的认证体系**：三种登录方式，统一Token管理
5. **严格的数据权限**：基于项目ID的数据隔离

### 1.4 三端对比

| 对比项 | 运营后台（vue3） | 小程序（uni3） |
|--------|-----------------|---------------|
| **使用端** | PC Web | 微信小程序 |
| **用户群体** | 内部运营管理人员 | 客户 + 现场员工 |
| **用户表** | `sys_user` | `customers` + `sys_user` |
| **URL前缀** | `/evs/*` | `/app/*` |
| **认证方式** | Session + 用户名密码 | JWT Token + 多种登录 |
| **权限模型** | RBAC（角色权限） | 角色 + 项目绑定 |
| **功能范围** | 完整的管理功能 | 查看 + 部分提交功能 |

---

## 2. 多角色认证与权限设计

### 2.1 角色定义

```java
public enum UserTypeEnum {
    CUSTOMER("customer", "客户"),
    STAFF("staff", "员工");
    
    private final String code;
    private final String desc;
}
```

### 2.2 角色与数据表映射

| 角色 | 数据表 | 主键字段 | 手机号字段 | 项目关联方式 |
|------|--------|---------|-----------|-------------|
| 客户 | `customers` | `id` (varchar) | `phone` | `projects.customer_id = customers.id` |
| 员工 | `sys_user` | `user_id` (bigint) | `phonenumber` | `project_members.user_id = sys_user.user_id` |

**关联关系：**
```sql
-- 客户与项目（1:N）
customers.id → projects.customer_id

-- 员工与项目（N:N，通过中间表）
sys_user.user_id → project_members.user_id
project_members.project_id → projects.id
```

### 2.3 登录方式设计

支持三种登录方式（按优先级）：

#### 2.3.1 微信登录（优先推荐）

**流程：**
```
1. 用户点击"微信登录"
2. 调用 wx.login() 获取 code
3. 如果是首次登录，需要获取手机号（wx.getPhoneNumber）
4. 后端通过 code 换取 openId
5. 查询 app_wechat_bindings 表
   - 已绑定：直接登录
   - 未绑定：需要绑定手机号
6. 根据手机号查询用户（先查customers，再查sys_user）
7. 生成Token并返回
```

#### 2.3.2 短信验证码登录

**流程：**
```
1. 用户输入手机号
2. 点击"获取验证码"
3. 后端发送短信验证码
4. 用户输入验证码
5. 后端验证验证码
6. 根据手机号查询用户（先查customers，再查sys_user）
7. 生成Token并返回
```

#### 2.3.3 密码登录

**流程：**
```
1. 用户输入手机号和密码
2. 后端验证密码
3. 主要用于员工登录（sys_user表有password字段）
4. 客户默认无密码，需要先设置
5. 生成Token并返回
```

### 2.4 Token设计

**Token Payload结构：**

```json
{
  "userType": "customer",              // 或 "staff"
  "userId": "C001",                    // customers.id 或 sys_user.user_id
  "phone": "13800138000",
  "name": "张三",
  "projectIds": ["P001", "P002"],      // 用户关联的所有项目ID列表
  "deviceId": "设备唯一标识",
  "exp": 1704067200,
  "iat": 1703980800,
  "jti": "uuid"
}
```

**Token有效期：**
- Access Token：2小时（从字典配置读取）
- Refresh Token：7天（从字典配置读取）

### 2.5 权限控制

#### 2.5.1 数据权限（项目级隔离）

**客户数据权限：**
```sql
-- 客户只能查看自己的项目
SELECT * FROM projects
WHERE customer_id = #{userId}
  AND deleted_at IS NULL
```

**员工数据权限：**
```sql
-- 员工只能查看关联的项目
SELECT p.* FROM projects p
INNER JOIN project_members pm ON p.id = pm.project_id
WHERE pm.user_id = #{userId}
  AND p.deleted_at IS NULL
```

#### 2.5.2 功能权限（角色级控制）

**通用功能（客户和员工都可访问）：**
- 项目信息查看
- 设计方案查看（project_rooms）
- 施工排期查看（project_schedules）
- 预算管理查看（project_budgets）
- 质检记录查看（quality_inspections）

**员工专属功能（客户无法访问）：**
- 工地巡视（project_schedule_records）
- 问题上报（quality_issues）
- 整改记录（quality_fixes）

---

## 3. 功能模块与表映射

### 3.1 功能模块总览

| 功能模块 | 小程序功能 | 对应的现有表 | 客户可用 | 员工可用 |
|---------|-----------|-------------|---------|---------|
| 项目信息 | 项目概况、统计 | `projects` | ✅ | ✅ |
| 设计方案 | 设计图查看 | `project_rooms` | ✅ | ✅ |
| 施工排期 | 排期查看 | `project_schedules` | ✅ | ✅ |
| 预算管理 | 预算查看 | `project_budgets` | ✅ | ✅ |
| 质检管理 | 质检记录查看 | `quality_inspections`, `quality_issues`, `quality_fixes` | ✅ | ✅ |
| 工地巡视 | 巡视记录 | `project_schedule_records` | ❌ | ✅ |
| 问题上报 | 问题上报 | `quality_issues` | ❌ | ✅ |
| 整改记录 | 整改记录 | `quality_fixes` | ❌ | ✅ |

### 3.2 表字段说明

#### 3.2.1 project_rooms（设计方案）

用于存储设计方案的房间信息和图片：

```sql
-- 关键字段
id              -- 房间ID
project_id      -- 项目ID
room_name       -- 房间名称（客厅、卧室等）
room_type       -- 房间类型
images          -- 设计图片（JSON数组）
description     -- 房间描述
```

#### 3.2.2 project_schedules（施工排期）

用于存储施工排期信息：

```sql
-- 关键字段
id              -- 排期ID
project_id      -- 项目ID
stage_name      -- 阶段名称（水电、泥工等）
start_date      -- 开始日期
end_date        -- 结束日期
status          -- 状态
progress        -- 进度百分比
```

#### 3.2.3 project_schedule_records（工地巡视）

**复用为工地巡视记录表**：

```sql
-- 关键字段
id              -- 记录ID
schedule_id     -- 关联的排期ID
project_id      -- 项目ID
record_date     -- 记录日期
content         -- 巡视内容
images          -- 巡视照片（JSON数组）
recorder_id     -- 记录人ID（员工ID）
recorder_name   -- 记录人姓名
```

#### 3.2.4 quality_issues（问题上报）

**直接使用质检问题表**：

```sql
-- 关键字段
id              -- 问题ID
inspection_id   -- 关联的质检ID
project_id      -- 项目ID
issue_type      -- 问题类型
description     -- 问题描述
severity        -- 严重程度
status          -- 状态
reporter_id     -- 上报人ID
images          -- 问题照片（JSON数组）
```

#### 3.2.5 quality_fixes（整改记录）

**直接使用质检整改表**：

```sql
-- 关键字段
id              -- 整改ID
issue_id        -- 关联的问题ID
project_id      -- 项目ID
fix_content     -- 整改内容
fix_date        -- 整改日期
fixer_id        -- 整改人ID
images          -- 整改照片（JSON数组）
```



---

## 4. API接口设计

### 4.1 认证模块 (AppAuthController)

#### 4.1.1 微信登录

```
POST /app/auth/wechat-login
Content-Type: application/json

Request:
{
  "code": "微信授权code",
  "encryptedData": "加密数据（首次登录获取手机号）",
  "iv": "加密算法初始向量",
  "deviceId": "设备唯一标识"
}

Response:
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200,
    "userType": "customer",  // 或 "staff"
    "userInfo": {
      "id": "C001",
      "name": "张三",
      "phone": "138****8000",
      "avatar": "https://..."
    },
    "projects": [
      {
        "id": "P001",
        "code": "P2025001",
        "name": "万科城市花园A栋1001",
        "status": "construction"
      }
    ]
  }
}

后端逻辑：
1. 通过code换取openId
2. 查询app_wechat_bindings表
3. 如果已绑定，获取用户信息
4. 如果未绑定，解密手机号，查询用户（先customers，再sys_user）
5. 创建绑定记录
6. 查询用户的项目列表
7. 生成Token
```

#### 4.1.2 短信验证码登录

```
POST /app/auth/sms-login
Content-Type: application/json

Request:
{
  "phone": "13800138000",
  "code": "123456",
  "deviceId": "设备唯一标识"
}

Response: 同微信登录
```

#### 4.1.3 密码登录

```
POST /app/auth/password-login
Content-Type: application/json

Request:
{
  "phone": "13800138000",
  "password": "加密后的密码",
  "deviceId": "设备唯一标识"
}

Response: 同微信登录
```

#### 4.1.4 发送验证码

```
POST /app/auth/send-code
Content-Type: application/json

Request:
{
  "phone": "13800138000"
}

Response:
{
  "code": 200,
  "msg": "验证码已发送",
  "data": {
    "expireTime": 300  // 有效期（秒）
  }
}
```

#### 4.1.5 刷新Token

```
POST /app/auth/refresh-token
Content-Type: application/json

Request:
{
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response:
{
  "code": 200,
  "data": {
    "accessToken": "new_access_token...",
    "refreshToken": "new_refresh_token...",
    "expiresIn": 7200
  }
}
```

#### 4.1.6 退出登录

```
POST /app/auth/logout
Authorization: Bearer {accessToken}

Response:
{
  "code": 200,
  "msg": "退出成功"
}
```

### 4.2 项目模块 (AppProjectController)

#### 4.2.1 获取项目列表

```
GET /app/project/list
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": [
    {
      "id": "P001",
      "code": "P2025001",
      "name": "万科城市花园A栋1001",
      "address": "深圳市南山区...",
      "area": 120.5,
      "status": "construction",
      "stage": "水电阶段",
      "progress": 35.5,
      "startDate": "2025-01-15",
      "expectedEndDate": "2025-06-30"
    }
  ]
}

后端逻辑：
- 客户：WHERE customer_id = #{userId}
- 员工：INNER JOIN project_members WHERE user_id = #{userId}
```

#### 4.2.2 获取项目详情

```
GET /app/project/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "P001",
    "code": "P2025001",
    "name": "万科城市花园A栋1001",
    "address": "深圳市南山区...",
    "area": 120.5,
    "status": "construction",
    "stage": "水电阶段",
    "progress": 35.5,
    "startDate": "2025-01-15",
    "expectedEndDate": "2025-06-30",
    "customer": {
      "name": "张三",
      "phone": "138****8000"
    },
    "designer": {
      "name": "李设计师",
      "phone": "139****9000"
    },
    "projectManager": {
      "name": "王经理",
      "phone": "137****7000"
    }
  }
}
```

#### 4.2.3 获取项目统计

```
GET /app/project/{id}/statistics
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "totalBudget": 350000.00,
    "paidAmount": 175000.00,
    "remainAmount": 175000.00,
    "scheduleCount": 12,
    "completedSchedule": 4,
    "qualityIssueCount": 3,
    "fixedIssueCount": 2,
    "constructionDays": 45,
    "remainDays": 120
  }
}
```

### 4.3 设计方案模块 (AppDesignController)

**基于 project_rooms 表**

#### 4.3.1 获取设计方案列表

```
GET /app/design/rooms
Authorization: Bearer {token}
Query: ?projectId=P001

Response:
{
  "code": 200,
  "data": [
    {
      "id": "R001",
      "roomName": "客厅",
      "roomType": "living_room",
      "area": 35.5,
      "imageCount": 5,
      "thumbnail": "https://...",
      "description": "现代简约风格"
    },
    {
      "id": "R002",
      "roomName": "主卧",
      "roomType": "bedroom",
      "area": 18.0,
      "imageCount": 3,
      "thumbnail": "https://..."
    }
  ]
}

后端逻辑：
SELECT * FROM project_rooms
WHERE project_id = #{projectId}
  AND deleted_at IS NULL
ORDER BY created_at DESC
```

#### 4.3.2 获取房间设计详情

```
GET /app/design/rooms/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "R001",
    "roomName": "客厅",
    "roomType": "living_room",
    "area": 35.5,
    "description": "现代简约风格，采用灰白色调",
    "images": [
      {
        "url": "https://...",
        "thumbnail": "https://...",
        "description": "客厅全景图"
      },
      {
        "url": "https://...",
        "thumbnail": "https://...",
        "description": "电视背景墙"
      }
    ]
  }
}
```

### 4.4 施工排期模块 (AppScheduleController)

**基于 project_schedules 表**

#### 4.4.1 获取排期列表

```
GET /app/schedule/list
Authorization: Bearer {token}
Query: ?projectId=P001

Response:
{
  "code": 200,
  "data": [
    {
      "id": "S001",
      "stageName": "水电阶段",
      "startDate": "2025-01-15",
      "endDate": "2025-02-05",
      "status": "in_progress",
      "statusText": "进行中",
      "progress": 75,
      "manager": "张工",
      "description": "水电定位、布线、验收"
    }
  ]
}

后端逻辑：
SELECT * FROM project_schedules
WHERE project_id = #{projectId}
  AND deleted_at IS NULL
ORDER BY start_date ASC
```

#### 4.4.2 获取排期详情

```
GET /app/schedule/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "S001",
    "stageName": "水电阶段",
    "startDate": "2025-01-15",
    "endDate": "2025-02-05",
    "status": "in_progress",
    "progress": 75,
    "manager": "张工",
    "description": "水电定位、布线、验收",
    "tasks": [
      {
        "name": "水电定位",
        "status": "completed",
        "completedDate": "2025-01-16"
      },
      {
        "name": "水电布线",
        "status": "in_progress",
        "progress": 80
      }
    ]
  }
}
```

### 4.5 预算管理模块 (AppBudgetController)

**基于 project_budgets 表**

#### 4.5.1 获取预算总览

```
GET /app/budget/overview
Authorization: Bearer {token}
Query: ?projectId=P001

Response:
{
  "code": 200,
  "data": {
    "totalBudget": 350000.00,
    "paidAmount": 175000.00,
    "remainAmount": 175000.00,
    "categories": [
      {
        "name": "设计费",
        "budget": 35000.00,
        "actual": 35000.00,
        "percent": 10
      },
      {
        "name": "人工费",
        "budget": 105000.00,
        "actual": 52500.00,
        "percent": 30
      }
    ]
  }
}

后端逻辑：
SELECT * FROM project_budgets
WHERE project_id = #{projectId}
  AND deleted_at IS NULL
```

#### 4.5.2 获取预算明细

```
GET /app/budget/items
Authorization: Bearer {token}
Query: ?projectId=P001&category=人工费

Response:
{
  "code": 200,
  "data": [
    {
      "id": "B001",
      "category": "人工费",
      "name": "水电工",
      "unit": "天",
      "quantity": 20,
      "unitPrice": 350.00,
      "amount": 7000.00,
      "remark": "包含材料搬运"
    }
  ]
}
```

### 4.6 质检管理模块 (AppQualityController)

**基于 quality_inspections, quality_issues, quality_fixes 表**

#### 4.6.1 获取质检记录列表

```
GET /app/quality/inspections
Authorization: Bearer {token}
Query: ?projectId=P001

Response:
{
  "code": 200,
  "data": [
    {
      "id": "Q001",
      "stage": "水电验收",
      "inspectionDate": "2025-02-05",
      "inspector": "王监理",
      "result": "qualified",
      "resultText": "合格",
      "issueCount": 2,
      "fixedCount": 2,
      "score": 95
    }
  ]
}

后端逻辑：
SELECT * FROM quality_inspections
WHERE project_id = #{projectId}
  AND deleted_at IS NULL
ORDER BY inspection_date DESC
```

#### 4.6.2 获取质检详情

```
GET /app/quality/inspections/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "Q001",
    "stage": "水电验收",
    "inspectionDate": "2025-02-05",
    "inspector": "王监理",
    "result": "qualified",
    "score": 95,
    "summary": "整体施工质量良好，个别细节需要整改",
    "issues": [
      {
        "id": "I001",
        "description": "厨房插座高度不符合规范",
        "severity": "medium",
        "images": ["https://..."],
        "status": "fixed",
        "fixDate": "2025-02-06"
      }
    ]
  }
}
```

### 4.7 工地巡视模块 (AppInspectionController) - 员工专属

**基于 project_schedule_records 表**

#### 4.7.1 提交巡视记录

```
POST /app/inspection/submit
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "projectId": "P001",
  "scheduleId": "S001",           // 关联的排期ID
  "recordDate": "2025-01-25",
  "content": "检查施工进度和现场安全情况",
  "images": ["https://...", "https://..."],
  "weather": "晴",
  "temperature": "15-22℃"
}

Response:
{
  "code": 200,
  "msg": "巡视记录提交成功",
  "data": {
    "id": "REC001"
  }
}

后端逻辑：
INSERT INTO project_schedule_records
(schedule_id, project_id, record_date, content, images, recorder_id, recorder_name, ...)
VALUES (...)
```

#### 4.7.2 获取巡视记录列表

```
GET /app/inspection/list
Authorization: Bearer {token}
Query: ?projectId=P001&startDate=2025-01-01&endDate=2025-01-31

Response:
{
  "code": 200,
  "data": {
    "total": 25,
    "list": [
      {
        "id": "REC001",
        "recordDate": "2025-01-25",
        "stageName": "水电阶段",
        "recorder": "张工",
        "content": "检查施工进度和现场安全情况",
        "imageCount": 5,
        "createTime": "2025-01-25 14:30:00"
      }
    ]
  }
}

后端逻辑：
SELECT r.*, s.stage_name
FROM project_schedule_records r
LEFT JOIN project_schedules s ON r.schedule_id = s.id
WHERE r.project_id = #{projectId}
  AND r.deleted_at IS NULL
ORDER BY r.record_date DESC
```

#### 4.7.3 获取巡视记录详情

```
GET /app/inspection/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "REC001",
    "recordDate": "2025-01-25",
    "stageName": "水电阶段",
    "recorder": "张工",
    "content": "检查施工进度和现场安全情况",
    "images": ["https://...", "https://..."],
    "weather": "晴",
    "temperature": "15-22℃",
    "createTime": "2025-01-25 14:30:00"
  }
}
```

### 4.8 问题上报模块 (AppIssueController) - 员工专属

**基于 quality_issues 表**

#### 4.8.1 上报问题

```
POST /app/issue/report
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "projectId": "P001",
  "inspectionId": "Q001",         // 关联的质检ID（可选）
  "issueType": "safety",          // 问题类型
  "description": "脚手架未按规范搭建",
  "location": "3号楼2单元",
  "severity": "high",
  "images": ["https://...", "https://..."]
}

Response:
{
  "code": 200,
  "msg": "问题上报成功",
  "data": {
    "id": "ISSUE001"
  }
}

后端逻辑：
INSERT INTO quality_issues
(inspection_id, project_id, issue_type, description, severity, reporter_id, images, ...)
VALUES (...)
```

#### 4.8.2 获取问题列表

```
GET /app/issue/list
Authorization: Bearer {token}
Query: ?projectId=P001&status=pending&severity=high

Response:
{
  "code": 200,
  "data": {
    "total": 15,
    "list": [
      {
        "id": "ISSUE001",
        "issueType": "safety",
        "issueTypeText": "安全问题",
        "description": "脚手架未按规范搭建",
        "location": "3号楼2单元",
        "severity": "high",
        "severityText": "高",
        "status": "pending",
        "statusText": "待处理",
        "reporter": "张工",
        "reportDate": "2025-01-25",
        "imageCount": 3
      }
    ]
  }
}

后端逻辑：
SELECT * FROM quality_issues
WHERE project_id = #{projectId}
  AND deleted_at IS NULL
ORDER BY created_at DESC
```

#### 4.8.3 获取问题详情

```
GET /app/issue/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "ISSUE001",
    "issueType": "safety",
    "description": "脚手架未按规范搭建",
    "location": "3号楼2单元",
    "severity": "high",
    "status": "pending",
    "reporter": "张工",
    "reportDate": "2025-01-25",
    "images": ["https://...", "https://..."],
    "fixes": []  // 整改记录列表
  }
}
```

### 4.9 整改记录模块 (AppRepairController) - 员工专属

**基于 quality_fixes 表**

#### 4.9.1 提交整改记录

```
POST /app/repair/submit
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "issueId": "ISSUE001",
  "projectId": "P001",
  "fixContent": "已按规范重新搭建脚手架",
  "fixDate": "2025-01-26",
  "images": ["https://...", "https://..."],
  "remark": "已通过安全检查"
}

Response:
{
  "code": 200,
  "msg": "整改记录提交成功",
  "data": {
    "id": "FIX001"
  }
}

后端逻辑：
INSERT INTO quality_fixes
(issue_id, project_id, fix_content, fix_date, fixer_id, images, ...)
VALUES (...)
```

#### 4.9.2 获取整改记录列表

```
GET /app/repair/list
Authorization: Bearer {token}
Query: ?projectId=P001&issueId=ISSUE001

Response:
{
  "code": 200,
  "data": {
    "total": 5,
    "list": [
      {
        "id": "FIX001",
        "issueDescription": "脚手架未按规范搭建",
        "fixContent": "已按规范重新搭建脚手架",
        "fixer": "李工",
        "fixDate": "2025-01-26",
        "imageCount": 4,
        "createTime": "2025-01-26 10:30:00"
      }
    ]
  }
}

后端逻辑：
SELECT f.*, i.description as issue_description
FROM quality_fixes f
LEFT JOIN quality_issues i ON f.issue_id = i.id
WHERE f.project_id = #{projectId}
  AND f.deleted_at IS NULL
ORDER BY f.fix_date DESC
```

#### 4.9.3 获取整改记录详情

```
GET /app/repair/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "FIX001",
    "issue": {
      "id": "ISSUE001",
      "description": "脚手架未按规范搭建",
      "severity": "high"
    },
    "fixContent": "已按规范重新搭建脚手架",
    "fixDate": "2025-01-26",
    "fixer": "李工",
    "images": ["https://...", "https://..."],
    "remark": "已通过安全检查",
    "createTime": "2025-01-26 10:30:00"
  }
}
```



---

## 5. 数据库设计

### 5.1 现有表（复用）

以下表直接复用，无需修改：

| 表名 | 用途 | 说明 |
|------|------|------|
| `customers` | 客户信息 | 客户基本信息 |
| `sys_user` | 员工信息 | 员工基本信息 |
| `projects` | 项目信息 | 项目基本信息 |
| `project_members` | 项目成员 | 员工与项目关联 |
| `project_rooms` | 设计方案 | 房间设计图片 |
| `project_schedules` | 施工排期 | 施工阶段排期 |
| `project_schedule_records` | 工地巡视 | 巡视记录（复用） |
| `project_budgets` | 预算管理 | 项目预算明细 |
| `quality_inspections` | 质检记录 | 质检主记录 |
| `quality_issues` | 问题上报 | 质检问题（复用） |
| `quality_fixes` | 整改记录 | 质检整改（复用） |

### 5.2 新增表（最小化）

只新增5张认证和审计相关的表：

#### 5.2.1 小程序登录日志表 (app_login_logs)

```sql
CREATE TABLE `app_login_logs` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `user_type` varchar(20) NOT NULL COMMENT '用户类型：customer/staff',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `login_type` varchar(20) NOT NULL COMMENT '登录类型：wechat/sms/password',
  `login_ip` varchar(50) DEFAULT NULL COMMENT '登录IP',
  `device_id` varchar(100) DEFAULT NULL COMMENT '设备唯一标识',
  `device_info` text COMMENT '设备信息（JSON）',
  `login_status` varchar(20) DEFAULT 'success' COMMENT '登录状态：success/failed',
  `fail_reason` varchar(200) DEFAULT NULL COMMENT '失败原因',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_type`, `user_id`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小程序登录日志表';
```

#### 5.2.2 Token管理表 (app_tokens)

```sql
CREATE TABLE `app_tokens` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `user_type` varchar(20) NOT NULL COMMENT '用户类型：customer/staff',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `access_token` varchar(500) NOT NULL COMMENT 'Access Token',
  `refresh_token` varchar(500) NOT NULL COMMENT 'Refresh Token',
  `device_id` varchar(100) NOT NULL COMMENT '设备唯一标识',
  `access_token_expire` datetime NOT NULL COMMENT 'Access Token过期时间',
  `refresh_token_expire` datetime NOT NULL COMMENT 'Refresh Token过期时间',
  `is_revoked` tinyint(1) DEFAULT 0 COMMENT '是否已撤销',
  `revoke_time` datetime DEFAULT NULL COMMENT '撤销时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `last_use_time` datetime DEFAULT NULL COMMENT '最后使用时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_access_token` (`access_token`(255)),
  KEY `idx_user` (`user_type`, `user_id`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token管理表';
```

#### 5.2.3 短信验证码表 (app_sms_codes)

```sql
CREATE TABLE `app_sms_codes` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `code` varchar(10) NOT NULL COMMENT '验证码',
  `type` varchar(20) DEFAULT 'login' COMMENT '类型：login/bind',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `is_used` tinyint(1) DEFAULT 0 COMMENT '是否已使用',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码表';
```

#### 5.2.4 微信绑定表 (app_wechat_bindings)

```sql
CREATE TABLE `app_wechat_bindings` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `open_id` varchar(100) NOT NULL COMMENT '微信openId',
  `union_id` varchar(100) DEFAULT NULL COMMENT '微信unionId',
  `user_type` varchar(20) NOT NULL COMMENT '用户类型：customer/staff',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `phone` varchar(20) DEFAULT NULL COMMENT '绑定手机号',
  `nickname` varchar(100) DEFAULT NULL COMMENT '微信昵称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '微信头像',
  `bind_time` datetime NOT NULL COMMENT '绑定时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`),
  KEY `idx_user` (`user_type`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信绑定表';
```

#### 5.2.5 审计日志表 (app_audit_logs)

```sql
CREATE TABLE `app_audit_logs` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `user_type` varchar(20) NOT NULL COMMENT '用户类型：customer/staff',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `project_id` varchar(50) DEFAULT NULL COMMENT '项目ID',
  `action` varchar(100) NOT NULL COMMENT '操作类型',
  `resource_type` varchar(50) DEFAULT NULL COMMENT '资源类型',
  `resource_id` varchar(50) DEFAULT NULL COMMENT '资源ID',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `device_id` varchar(100) DEFAULT NULL COMMENT '设备ID',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `response_code` int DEFAULT NULL COMMENT '响应码',
  `execute_time` int DEFAULT NULL COMMENT '执行时长（毫秒）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_type`, `user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
```

---

## 6. 前端架构设计

### 6.1 目录结构

```
uni3/src/
├── api/                          # API接口层
│   ├── auth.js                   # 认证相关
│   ├── project.js                # 项目相关
│   ├── design.js                 # 设计方案（project_rooms）
│   ├── schedule.js               # 施工排期（project_schedules）
│   ├── budget.js                 # 预算管理（project_budgets）
│   ├── quality.js                # 质检管理（quality_*）
│   ├── inspection.js             # 工地巡视（schedule_records）- 员工专属
│   ├── issue.js                  # 问题上报（quality_issues）- 员工专属
│   ├── repair.js                 # 整改记录（quality_fixes）- 员工专属
│   └── upload.js                 # 文件上传
│
├── stores/                       # 状态管理
│   ├── user.js                   # 用户状态（含角色、项目列表）
│   ├── project.js                # 当前项目状态
│   └── app.js                    # 应用状态
│
├── utils/                        # 工具函数
│   ├── request.js                # 请求封装
│   ├── auth.js                   # 认证工具
│   ├── permission.js             # 权限判断
│   ├── date.js                   # 日期处理
│   ├── image.js                  # 图片处理
│   └── validator.js              # 表单验证
│
├── components/                   # 公共组件
│   ├── ImagePreview/             # 图片预览
│   ├── ImageUpload/              # 图片上传
│   ├── ProjectSelector/          # 项目选择器
│   ├── EmptyState/               # 空状态
│   └── LoadingState/             # 加载状态
│
├── pages/                        # 页面
│   ├── login/                    # 登录页
│   │   └── index.vue
│   ├── dashboard/                # 首页
│   │   ├── customer.vue          # 客户首页
│   │   └── staff.vue             # 员工首页
│   ├── design/                   # 设计方案（通用）
│   │   ├── list.vue              # 房间列表
│   │   └── detail.vue            # 房间详情
│   ├── schedule/                 # 施工排期（通用）
│   │   ├── list.vue              # 排期列表
│   │   └── detail.vue            # 排期详情
│   ├── budget/                   # 预算管理（通用）
│   │   ├── overview.vue          # 预算总览
│   │   └── detail.vue            # 预算明细
│   ├── quality/                  # 质检管理（通用）
│   │   ├── list.vue              # 质检列表
│   │   └── detail.vue            # 质检详情
│   ├── inspection/               # 工地巡视（员工专属）
│   │   ├── list.vue              # 巡视列表
│   │   ├── detail.vue            # 巡视详情
│   │   └── submit.vue            # 提交巡视
│   ├── issue/                    # 问题上报（员工专属）
│   │   ├── list.vue              # 问题列表
│   │   ├── detail.vue            # 问题详情
│   │   └── report.vue            # 上报问题
│   ├── repair/                   # 整改记录（员工专属）
│   │   ├── list.vue              # 整改列表
│   │   ├── detail.vue            # 整改详情
│   │   └── submit.vue            # 提交整改
│   └── profile/                  # 个人中心（通用）
│       └── index.vue
│
├── mixins/                       # 混入
│   └── roleCheck.js              # 角色检查
│
└── config/                       # 配置文件
    ├── app.js                    # 应用配置
    └── api.js                    # API配置
```

### 6.2 状态管理设计

#### 6.2.1 用户状态 (stores/user.js)

```javascript
import { defineStore } from 'pinia'
import { login, wechatLogin, smsLogin } from '@/api/auth.js'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    userType: uni.getStorageSync('userType') || '',  // customer/staff
    userInfo: uni.getStorageSync('userInfo') || null,
    projects: uni.getStorageSync('projects') || []    // 用户的项目列表
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    isCustomer: (state) => state.userType === 'customer',
    isStaff: (state) => state.userType === 'staff',
    userId: (state) => state.userInfo?.id
  },
  
  actions: {
    // 微信登录
    async wechatLogin(loginData) {
      const res = await wechatLogin(loginData)
      this.setUserInfo(res)
      return res
    },
    
    // 短信登录
    async smsLogin(loginData) {
      const res = await smsLogin(loginData)
      this.setUserInfo(res)
      return res
    },
    
    // 设置用户信息
    setUserInfo(data) {
      this.token = data.accessToken
      this.userType = data.userType
      this.userInfo = data.userInfo
      this.projects = data.projects
      
      uni.setStorageSync('token', data.accessToken)
      uni.setStorageSync('userType', data.userType)
      uni.setStorageSync('userInfo', data.userInfo)
      uni.setStorageSync('projects', data.projects)
    },
    
    // 退出登录
    logout() {
      this.token = ''
      this.userType = ''
      this.userInfo = null
      this.projects = []
      
      uni.removeStorageSync('token')
      uni.removeStorageSync('userType')
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('projects')
      uni.removeStorageSync('currentProjectId')
      
      uni.reLaunch({ url: '/pages/login/index' })
    }
  }
})
```

#### 6.2.2 项目状态 (stores/project.js)

```javascript
import { defineStore } from 'pinia'
import { getProjectDetail } from '@/api/project.js'

export const useProjectStore = defineStore('project', {
  state: () => ({
    currentProjectId: uni.getStorageSync('currentProjectId') || '',
    currentProject: null
  }),
  
  actions: {
    // 切换项目
    switchProject(projectId) {
      this.currentProjectId = projectId
      uni.setStorageSync('currentProjectId', projectId)
      this.currentProject = null  // 清空缓存，重新加载
    },
    
    // 获取当前项目详情
    async fetchCurrentProject() {
      if (!this.currentProjectId) return null
      
      const data = await getProjectDetail(this.currentProjectId)
      this.currentProject = data
      return data
    }
  }
})
```

### 6.3 权限控制

#### 6.3.1 权限判断工具 (utils/permission.js)

```javascript
import { useUserStore } from '@/stores/user.js'

export const isStaff = () => {
  const userStore = useUserStore()
  return userStore.userType === 'staff'
}

export const isCustomer = () => {
  const userStore = useUserStore()
  return userStore.userType === 'customer'
}

export const hasPagePermission = (pagePath) => {
  const staffOnlyPages = [
    '/pages/inspection/',
    '/pages/issue/',
    '/pages/repair/'
  ]
  
  if (isCustomer()) {
    return !staffOnlyPages.some(path => pagePath.startsWith(path))
  }
  
  return true
}
```

#### 6.3.2 路由守卫 (main.js)

```javascript
import { hasPagePermission } from '@/utils/permission.js'

uni.addInterceptor('navigateTo', {
  invoke(args) {
    if (!hasPagePermission(args.url)) {
      uni.showToast({
        title: '该功能仅员工可用',
        icon: 'none'
      })
      return false
    }
  }
})
```



---

## 7. 字典配置设计

### 7.1 小程序配置字典 (app_config)

```sql
-- 字典类型
INSERT INTO sys_dict_type VALUES 
('app_config', '小程序配置', '0', 'admin', NOW(), '', NULL, '小程序相关配置参数');

-- 字典数据
INSERT INTO sys_dict_data VALUES 
(NULL, 1, 'Token有效期（小时）', '2', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, 'Token有效期配置'),
(NULL, 2, 'RefreshToken有效期（天）', '7', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, 'RefreshToken有效期'),
(NULL, 3, '验证码有效期（分钟）', '5', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '短信验证码有效期'),
(NULL, 4, '单次上传图片数量', '9', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '单次最多上传图片数'),
(NULL, 5, '图片大小限制（MB）', '5', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '单张图片大小限制');
```

### 7.2 问题类型字典 (issue_type)

```sql
-- 字典类型
INSERT INTO sys_dict_type VALUES 
('issue_type', '问题类型', '0', 'admin', NOW(), '', NULL, '质检问题类型');

-- 字典数据
INSERT INTO sys_dict_data VALUES 
(NULL, 1, '安全问题', 'safety', 'issue_type', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '安全相关问题'),
(NULL, 2, '质量问题', 'quality', 'issue_type', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '质量相关问题'),
(NULL, 3, '进度问题', 'progress', 'issue_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '进度相关问题'),
(NULL, 4, '其他问题', 'other', 'issue_type', '', 'default', 'N', '0', 'admin', NOW(), '', NULL, '其他类型问题');
```

### 7.3 严重程度字典 (severity_level)

```sql
-- 字典类型
INSERT INTO sys_dict_type VALUES 
('severity_level', '严重程度', '0', 'admin', NOW(), '', NULL, '问题严重程度');

-- 字典数据
INSERT INTO sys_dict_data VALUES 
(NULL, 1, '低', 'low', 'severity_level', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '低严重程度'),
(NULL, 2, '中', 'medium', 'severity_level', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '中等严重程度'),
(NULL, 3, '高', 'high', 'severity_level', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '高严重程度'),
(NULL, 4, '紧急', 'urgent', 'severity_level', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '紧急严重程度');
```

### 7.4 配置读取工具类

```java
@Component
public class AppConfigUtil {
    @Autowired
    private ISysDictDataService dictDataService;
    
    private static final String DICT_TYPE = "app_config";
    
    // 获取Token有效期（小时）
    public int getTokenExpireHours() {
        return getIntValue("Token有效期（小时）", 2);
    }
    
    // 获取RefreshToken有效期（天）
    public int getRefreshTokenExpireDays() {
        return getIntValue("RefreshToken有效期（天）", 7);
    }
    
    // 获取验证码有效期（分钟）
    public int getSmsCodeExpireMinutes() {
        return getIntValue("验证码有效期（分钟）", 5);
    }
    
    // 获取单次上传图片数量
    public int getMaxUploadImageCount() {
        return getIntValue("单次上传图片数量", 9);
    }
    
    // 获取图片大小限制（MB）
    public int getMaxImageSizeMB() {
        return getIntValue("图片大小限制（MB）", 5);
    }
    
    // 通用获取方法
    private int getIntValue(String label, int defaultValue) {
        String value = dictDataService.selectDictLabel(DICT_TYPE, label);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }
}
```

---

## 8. 开发计划

### 8.1 第一阶段：基础设施搭建（5-7天）

#### 后端开发
- [ ] 创建 `com.ruoyi.app` 包结构
- [ ] 配置小程序字典数据（app_config、issue_type、severity_level）
- [ ] 实现配置读取工具类（AppConfigUtil）
- [ ] 实现 JWT Token 工具类（RS256算法）
- [ ] 实现 Token 管理服务（生成、验证、刷新、撤销）
- [ ] 实现短信验证码服务（发送、验证）
- [ ] 实现微信登录服务（openId换取、绑定）
- [ ] 实现 AppAuthInterceptor 拦截器（Token验证、角色识别）
- [ ] 实现 AppRoleInterceptor 拦截器（员工专属功能拦截）
- [ ] 配置 Spring Security（放行 /app/* 路径）
- [ ] 创建 AppAuthController（三种登录方式）
- [ ] 创建数据库新增表（5张表）
- [ ] 编写 DTO 基础类
- [ ] 实现审计日志切面（AOP）

#### 前端开发
- [ ] 完善 request.js（Token管理、自动刷新、错误处理）
- [ ] 实现设备ID生成和存储
- [ ] 创建 API 配置文件（支持环境切换）
- [ ] 完善用户状态管理（stores/user.js）
- [ ] 完善项目状态管理（stores/project.js）
- [ ] 实现三种登录功能（微信/短信/密码）
- [ ] 实现Token自动刷新机制
- [ ] 实现权限判断工具（permission.js）
- [ ] 实现路由守卫（角色权限控制）
- [ ] 实现登录状态持久化

### 8.2 第二阶段：通用功能开发（7-10天）

#### 后端开发
- [ ] AppProjectController（项目列表、详情、统计）
- [ ] AppDesignController（基于project_rooms）
- [ ] AppScheduleController（基于project_schedules）
- [ ] AppBudgetController（基于project_budgets）
- [ ] AppQualityController（基于quality_*）
- [ ] 文件上传接口

#### 前端开发
- [ ] 客户首页（dashboard/customer.vue）
- [ ] 员工首页（dashboard/staff.vue）
- [ ] 项目选择器组件
- [ ] 设计方案页面（基于project_rooms）
- [ ] 施工排期页面（基于project_schedules）
- [ ] 预算管理页面（基于project_budgets）
- [ ] 质检管理页面（基于quality_*）
- [ ] 图片预览组件
- [ ] 图片上传组件

### 8.3 第三阶段：员工专属功能开发（5-7天）

#### 后端开发
- [ ] AppInspectionController（基于project_schedule_records）
- [ ] AppIssueController（基于quality_issues）
- [ ] AppRepairController（基于quality_fixes）

#### 前端开发
- [ ] 工地巡视页面（inspection/）
  - [ ] 巡视列表
  - [ ] 巡视详情
  - [ ] 提交巡视
- [ ] 问题上报页面（issue/）
  - [ ] 问题列表
  - [ ] 问题详情
  - [ ] 上报问题
- [ ] 整改记录页面（repair/）
  - [ ] 整改列表
  - [ ] 整改详情
  - [ ] 提交整改
- [ ] 个人中心完善

### 8.4 第四阶段：优化与测试（3-5天）

- [ ] 性能优化（缓存、图片、列表）
- [ ] 用户体验优化（加载状态、空状态、错误提示）
- [ ] 接口联调测试
- [ ] 功能测试
- [ ] 兼容性测试
- [ ] 数据权限测试（客户/员工隔离）
- [ ] 角色权限测试（员工专属功能）

### 8.5 第五阶段：部署上线（2-3天）

- [ ] 生产环境配置
- [ ] 数据库迁移
- [ ] 后端服务部署
- [ ] 小程序提审发布
- [ ] 用户培训文档

**总计：22-32天**

---

## 9. 总结

### 9.1 设计亮点

1. **最大化复用现有表结构**
   - 设计方案使用 `project_rooms`
   - 施工排期使用 `project_schedules`
   - 工地巡视使用 `project_schedule_records`
   - 问题上报使用 `quality_issues`
   - 整改记录使用 `quality_fixes`
   - 只新增5张认证和审计表

2. **清晰的多角色设计**
   - 客户和员工统一登录入口
   - 基于Token的角色识别
   - 前后端双重权限控制
   - 员工专属功能明确隔离

3. **严格的数据权限**
   - 客户只能看自己的项目
   - 员工只能看关联的项目
   - 基于项目ID的数据过滤
   - 多层权限验证

4. **灵活的配置管理**
   - 使用若依字典功能
   - 支持运营后台可视化配置
   - 配置热更新

### 9.2 技术栈

**前端：**
- uni-app（Vue 3 + Composition API）
- Pinia（状态管理）
- uview-plus（UI组件库）

**后端：**
- Spring Boot 3.x
- Spring Security + JWT
- MyBatis
- MySQL 8.0

### 9.3 关键指标

- **新增表数量**：5张（最小化）
- **复用表数量**：11张（最大化）
- **API接口数量**：约30个
- **前端页面数量**：约20个
- **开发周期**：22-32天

---

**设计完成！** 🎉

