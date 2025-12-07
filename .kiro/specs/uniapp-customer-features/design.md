# 智享家Pro - 多角色小程序技术设计方案

## 1. 架构设计概述

### 1.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      uni3 小程序前端（多角色）                     │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  登录模块（统一入口）                                       │   │
│  │  - 微信登录（优先）                                         │   │
│  │  - 短信验证码登录                                           │   │
│  │  - 密码登录                                                │   │
│  └──────────────────────────────────────────────────────────┘   │
│                          ↓                                       │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  角色识别与路由                                             │   │
│  │  - 客户角色 → 客户功能页面                                  │   │
│  │  - 员工角色 → 员工功能页面（含工地巡视、问题上报等）         │   │
│  └──────────────────────────────────────────────────────────┘   │
│                          ↓                                       │
│  ┌────────────────────┬────────────────────────────────────┐   │
│  │  客户功能模块       │      员工功能模块                    │   │
│  │  - 首页概况        │      - 工地巡视                      │   │
│  │  - 设计方案        │      - 问题上报                      │   │
│  │  - 施工日志        │      - 整改修复                      │   │
│  │  - 施工排期        │      - 施工日志                      │   │
│  │  - 预算管理        │      - 质检管理                      │   │
│  │  - 物料清单        │      - 项目管理                      │   │
│  │  - 质检记录        │      - 个人中心                      │   │
│  │  - 个人中心        │                                      │   │
│  └────────────────────┴────────────────────────────────────┘   │
│                          ↓                                       │
│              ┌────────────────────────┐                          │
│              │   API Service Layer    │                          │
│              │  (request.js + APIs)   │                          │
│              └────────────────────────┘                          │
└─────────────────────────────────────────────────────────────────┘
                          ↓ HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                   sb3/evs-home 后端服务                           │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              小程序API层 (新增)                           │    │
│  │  com.ruoyi.app.controller                               │    │
│  │  - AppAuthController      (统一登录认证)                 │    │
│  │  - AppProjectController   (项目信息)                     │    │
│  │  - AppDesignController    (设计方案)                     │    │
│  │  - AppConstructController (施工日志)                     │    │
│  │  - AppScheduleController  (施工排期)                     │    │
│  │  - AppBudgetController    (预算管理)                     │    │
│  │  - AppMaterialController  (物料清单)                     │    │
│  │  - AppQualityController   (质检管理)                     │    │
│  │  - AppInspectionController (工地巡视 - 员工专用)         │    │
│  │  - AppIssueController     (问题上报 - 员工专用)          │    │
│  │  - AppRepairController    (整改修复 - 员工专用)          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                          ↓                                       │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              DTO转换层 (新增)                             │    │
│  │  com.ruoyi.app.dto                                      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                          ↓                                       │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │          业务服务层 (复用现有)                             │    │
│  │  com.ruoyi.web.service                                  │    │
│  │  - IProjectsService                                     │    │
│  │  - ICustomersService                                    │    │
│  │  - ISysUserService                                      │    │
│  │  - IProjectMembersService                               │    │
│  │  - ...                                                  │    │
│  └─────────────────────────────────────────────────────────┘    │
│                          ↓                                       │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │          数据访问层 (复用现有)                             │    │
│  │  com.ruoyi.web.mapper + domain                          │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                          ↓
                    ┌──────────┐
                    │  MySQL   │
                    └──────────┘
```


### 1.2 多角色小程序说明

**应用定位：**
- 不仅仅是"客户端"小程序，而是**多角色小程序**
- 支持客户和员工两种角色登录使用
- 根据用户角色展示不同的功能模块

**用户角色：**

| 角色类型 | 数据表 | 项目关联方式 | 功能范围 |
|---------|--------|-------------|---------|
| 客户 | `customers` | `projects.customer_id` | 查看项目信息、提交反馈 |
| 员工 | `sys_user` | `project_members` 表 | 工地巡视、问题上报、整改修复等 |

**关键特性：**
1. **统一登录入口**：微信登录、短信登录、密码登录
2. **角色自动识别**：登录后根据用户类型展示对应功能
3. **数据权限隔离**：客户只能看自己的项目，员工只能看关联的项目
4. **功能权限控制**：员工专属功能（工地巡视、问题上报等）客户无法访问

### 1.3 架构设计决策

**方案选择：在 evs-home 模块内扩展，采用包分层架构**

#### 优势分析：
1. **代码复用**：直接复用现有的 Domain、Mapper、Service 层
2. **维护成本低**：统一的代码库，减少重复代码
3. **部署简单**：单一服务部署，无需额外的服务治理
4. **开发效率高**：利用现有基础设施和工具类
5. **数据一致性**：共享同一数据库连接池和事务管理
6. **多角色支持**：通过Token中的用户类型字段区分角色

#### 包结构设计：
```
sb3/evs-home/src/main/java/com/ruoyi/
├── web/                          # 管理后台API（现有 - vue3使用）
│   ├── controller/               # 后台Controller
│   ├── service/                  # 业务服务层
│   ├── mapper/                   # 数据访问层
│   └── domain/                   # 领域模型
│
└── app/                          # 小程序API（新增 - uni3使用）
    ├── controller/               # 小程序Controller
    │   ├── AppAuthController.java          # 统一登录认证
    │   ├── AppProjectController.java       # 项目信息（通用）
    │   ├── AppDesignController.java        # 设计方案（通用）
    │   ├── AppConstructController.java     # 施工日志（通用）
    │   ├── AppScheduleController.java      # 施工排期（通用）
    │   ├── AppBudgetController.java        # 预算管理（通用）
    │   ├── AppMaterialController.java      # 物料清单（通用）
    │   ├── AppQualityController.java       # 质检管理（通用）
    │   ├── AppInspectionController.java    # 工地巡视（员工专用）
    │   ├── AppIssueController.java         # 问题上报（员工专用）
    │   └── AppRepairController.java        # 整改修复（员工专用）
    │
    ├── dto/                      # 数据传输对象
    │   ├── request/              # 请求DTO
    │   │   ├── AppLoginRequest.java
    │   │   ├── WechatLoginRequest.java
    │   │   ├── InspectionRequest.java
    │   │   └── IssueReportRequest.java
    │   └── response/             # 响应DTO
    │       ├── AppLoginResponse.java
    │       ├── AppProjectResponse.java
    │       ├── AppUserInfoResponse.java
    │       └── InspectionResponse.java
    │
    ├── service/                  # 小程序专用服务
    │   ├── IAppAuthService.java
    │   ├── IAppInspectionService.java
    │   └── IAppIssueService.java
    │
    ├── security/                 # 安全相关
    │   ├── AppAuthInterceptor.java         # 认证拦截器
    │   ├── AppRoleInterceptor.java         # 角色权限拦截器
    │   └── AppTokenManager.java            # Token管理器
    │
    └── enums/                    # 枚举类
        ├── UserTypeEnum.java               # 用户类型枚举
        └── InspectionTypeEnum.java         # 巡视类型枚举
```



## 2. 多角色认证与权限设计

### 2.1 三端对比

| 对比项 | 运营后台（vue3） | 小程序（uni3） |
|--------|-----------------|---------------|
| **使用端** | PC Web | 微信小程序 |
| **用户群体** | 内部运营管理人员 | 客户 + 现场员工 |
| **用户表** | `sys_user` | `customers` + `sys_user` |
| **URL前缀** | `/evs/*` | `/app/*` |
| **认证方式** | Session + 用户名密码 | JWT Token + 多种登录方式 |
| **权限模型** | RBAC（角色权限） | 角色 + 项目绑定 |
| **功能范围** | 完整的管理功能 | 查看 + 部分提交功能 |

### 2.2 小程序多角色设计

#### 2.2.1 角色类型定义

```java
public enum UserTypeEnum {
    CUSTOMER("customer", "客户"),
    STAFF("staff", "员工");
    
    private final String code;
    private final String desc;
}
```

#### 2.2.2 角色与数据表映射

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

#### 2.2.3 登录方式设计

**支持三种登录方式（按优先级）：**

1. **微信登录（优先推荐）**
   - 获取微信 openId
   - 首次登录需绑定手机号
   - 后续登录直接通过 openId 识别

2. **短信验证码登录**
   - 输入手机号 + 验证码
   - 适用于客户和员工

3. **密码登录**
   - 输入手机号 + 密码
   - 主要用于员工登录
   - 客户默认无密码，需要先设置

**登录流程：**
```
用户输入手机号
    ↓
查询 customers 表（通过 phone 字段）
    ↓ 找到
返回客户信息 → 生成Token（userType=customer）
    ↓ 未找到
查询 sys_user 表（通过 phonenumber 字段）
    ↓ 找到
返回员工信息 → 生成Token（userType=staff）
    ↓ 未找到
返回错误：用户不存在
```

### 2.3 Token设计

#### 2.3.1 Token Payload结构

**客户Token：**
```json
{
  "userType": "customer",
  "userId": "C001",                    // customers表的id
  "phone": "13800138000",
  "name": "张三",
  "projectIds": ["P001", "P002"],      // 客户关联的所有项目ID列表
  "currentProjectId": "P001",          // 当前选中的项目ID
  "deviceId": "设备唯一标识",
  "exp": 1704067200,
  "iat": 1703980800,
  "jti": "uuid"
}
```

**员工Token：**
```json
{
  "userType": "staff",
  "userId": "1001",                    // sys_user表的user_id
  "username": "zhangsan",
  "phone": "13900139000",
  "name": "张三",
  "deptId": "100",
  "projectIds": ["P001", "P003", "P005"], // 员工关联的所有项目ID列表
  "currentProjectId": "P001",          // 当前选中的项目ID
  "deviceId": "设备唯一标识",
  "exp": 1704067200,
  "iat": 1703980800,
  "jti": "uuid"
}
```

**关键字段说明：**
- `userType`：区分客户和员工
- `userId`：用户ID（客户为varchar，员工为bigint转string）
- `projectIds`：用户关联的所有项目ID列表
- `currentProjectId`：当前选中的项目ID（用于数据过滤）

#### 2.3.2 项目列表获取逻辑

**客户的项目列表：**
```sql
SELECT id, code, name, address, area, status
FROM projects
WHERE customer_id = #{customerId}
  AND deleted_at IS NULL
ORDER BY created_at DESC
```

**员工的项目列表：**
```sql
SELECT p.id, p.code, p.name, p.address, p.area, p.status
FROM projects p
INNER JOIN project_members pm ON p.id = pm.project_id
WHERE pm.user_id = #{userId}
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC
```

#### 2.3.3 项目列表与切换

**获取项目列表接口：**
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
      "isCurrent": true  // 是否为当前选中项目
    },
    {
      "id": "P002",
      "code": "P2025002",
      "name": "华润城B栋2002",
      "address": "深圳市福田区...",
      "area": 95.0,
      "status": "design",
      "stage": "方案设计",
      "progress": 60.0,
      "isCurrent": false
    }
  ]
}

后端逻辑：
- 客户：查询 projects 表 WHERE customer_id = #{userId}
- 员工：查询 projects 表 INNER JOIN project_members WHERE user_id = #{userId}
```

**前端项目切换：**
- 用户在小程序中选择项目后，前端更新本地存储的 `currentProjectId`
- 后续所有请求在Header中携带 `X-Project-Id: P002`
- 后端拦截器验证该项目ID是否在用户的 `projectIds` 列表中
- 无需刷新Token，只需验证权限

### 2.4 权限控制设计

#### 2.4.1 数据权限（项目级隔离）

**客户数据权限：**
- 只能查看 `projects.customer_id = 自己的ID` 的项目
- 自动过滤：WHERE project_id IN (客户的projectIds)

**员工数据权限：**
- 只能查看 `project_members` 表中关联的项目
- 自动过滤：WHERE project_id IN (员工的projectIds)

**实现方式：**
```java
@Component
public class AppAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        // 1. 解析Token
        String token = request.getHeader("Authorization");
        Claims claims = tokenManager.parseToken(token);
        
        // 2. 获取用户信息
        String userType = claims.get("userType", String.class);
        String userId = claims.get("userId", String.class);
        List<String> projectIds = claims.get("projectIds", List.class);
        String currentProjectId = claims.get("currentProjectId", String.class);
        
        // 3. 验证项目权限
        if (currentProjectId != null && !projectIds.contains(currentProjectId)) {
            throw new BusinessException("无权访问该项目");
        }
        
        // 4. 存入ThreadLocal
        AppContext.setUserType(userType);
        AppContext.setUserId(userId);
        AppContext.setProjectIds(projectIds);
        AppContext.setCurrentProjectId(currentProjectId);
        
        return true;
    }
}
```

#### 2.4.2 功能权限（角色级控制）

**通用功能（客户和员工都可访问）：**
- 项目信息查看
- 设计方案查看
- 施工日志查看
- 施工排期查看
- 预算管理查看
- 物料清单查看
- 质检记录查看

**员工专属功能（客户无法访问）：**
- 工地巡视
- 问题上报
- 整改修复

**实现方式：**
```java
// 自定义注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireStaff {
    String message() default "该功能仅员工可用";
}

// 拦截器
@Component
public class AppRoleInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            RequireStaff annotation = method.getMethodAnnotation(RequireStaff.class);
            
            if (annotation != null) {
                String userType = AppContext.getUserType();
                if (!"staff".equals(userType)) {
                    throw new BusinessException(annotation.message());
                }
            }
        }
        return true;
    }
}

// 使用示例
@RestController
@RequestMapping("/app/inspection")
public class AppInspectionController {
    
    @RequireStaff(message = "工地巡视功能仅员工可用")
    @PostMapping("/submit")
    public AjaxResult submitInspection(@RequestBody InspectionRequest request) {
        // 提交巡视记录
        return success();
    }
}
```

#### 2.4.3 数据过滤（Service层）

**自动注入项目ID过滤：**
```java
@Service
public class AppProjectServiceImpl implements IAppProjectService {
    
    @Override
    public List<ConstructionLog> getConstructionLogs(LogQueryRequest request) {
        // 从ThreadLocal获取当前项目ID
        String currentProjectId = AppContext.getCurrentProjectId();
        
        // 自动添加项目ID过滤
        request.setProjectId(currentProjectId);
        
        // 查询数据
        return constructionLogMapper.selectList(request);
    }
}
```

**Mapper层SQL示例：**
```xml
<select id="selectList" resultType="ConstructionLog">
    SELECT * FROM construction_logs
    WHERE project_id = #{projectId}  <!-- 自动注入 -->
      AND deleted_at IS NULL
    <if test="category != null">
        AND category = #{category}
    </if>
    ORDER BY log_date DESC
</select>
```

#### 2.2.6 权限控制与数据隔离

**客户端权限规则：**
1. 客户只能访问自己绑定的项目数据
2. 通过 Token 中的 `customerId` 和 `projectId` 进行数据过滤
3. 所有查询自动添加项目ID条件
4. 只读权限，不能修改核心数据

**运营端权限规则（对比）：**
1. 基于角色的权限控制（RBAC）
2. 可以访问多个项目数据
3. 根据角色权限决定可操作范围
4. 具有完整的增删改查权限

**实现方式：**

```java
// 1. 自定义注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AppAuth {
    boolean required() default true;
}

// 2. 客户端拦截器
public class AppAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        String token = request.getHeader("Authorization");
        
        // 1. 验证Token有效性
        if (!tokenManager.validateToken(token)) {
            throw new UnauthorizedException("Token无效或已过期");
        }
        
        // 2. 解析Token
        Claims claims = tokenManager.parseToken(token);
        String userType = claims.get("userType", String.class);
        
        // 3. 验证用户类型
        if (!"customer".equals(userType)) {
            throw new UnauthorizedException("用户类型不匹配");
        }
        
        // 4. 获取客户信息
        String customerId = claims.get("customerId", String.class);
        String projectId = claims.get("projectId", String.class);
        
        // 5. 验证客户与项目关联（二次验证）
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null || !customer.getIsActive()) {
            throw new UnauthorizedException("客户不存在或已禁用");
        }
        
        Project project = projectMapper.selectById(projectId);
        if (project == null || !project.getCustomerId().equals(customerId)) {
            throw new UnauthorizedException("客户与项目不匹配");
        }
        
        // 6. 将信息存入ThreadLocal
        AppContext.setCustomerId(customerId);
        AppContext.setProjectId(projectId);
        AppContext.setCustomer(customer);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler,
                               Exception ex) {
        // 清理ThreadLocal
        AppContext.clear();
    }
}

// 3. ThreadLocal上下文
public class AppContext {
    private static final ThreadLocal<String> customerIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> projectIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<Customer> customerHolder = new ThreadLocal<>();
    
    public static void setCustomerId(String customerId) {
        customerIdHolder.set(customerId);
    }
    
    public static String getCustomerId() {
        return customerIdHolder.get();
    }
    
    public static void setProjectId(String projectId) {
        projectIdHolder.set(projectId);
    }
    
    public static String getProjectId() {
        return projectIdHolder.get();
    }
    
    public static void setCustomer(Customer customer) {
        customerHolder.set(customer);
    }
    
    public static Customer getCustomer() {
        return customerHolder.get();
    }
    
    public static void clear() {
        customerIdHolder.remove();
        projectIdHolder.remove();
        customerHolder.remove();
    }
}

// 4. Service层自动注入项目ID
@Service
public class AppProjectServiceImpl implements IAppProjectService {
    
    @Override
    public ProjectDetail getProjectInfo() {
        // 从ThreadLocal获取项目ID
        String projectId = AppContext.getProjectId();
        String customerId = AppContext.getCustomerId();
        
        // 查询项目信息（自动带上项目ID过滤）
        Project project = projectMapper.selectById(projectId);
        
        // 二次验证（防御性编程）
        if (!project.getCustomerId().equals(customerId)) {
            throw new BusinessException("无权访问该项目");
        }
        
        return convertToDetail(project);
    }
}
```

**数据隔离层级：**
```
┌─────────────────────────────────────────┐
│  1. 拦截器层：验证Token，提取用户信息      │
│     - 验证userType = "customer"         │
│     - 验证客户与项目关联关系              │
│     - 将信息存入ThreadLocal              │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│  2. Service层：自动注入项目ID过滤         │
│     - 从ThreadLocal获取projectId        │
│     - 所有查询自动添加项目ID条件          │
│     - 二次验证数据归属                   │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│  3. Mapper层：SQL级别过滤                │
│     - WHERE project_id = #{projectId}   │
│     - 使用数据库视图限制访问范围          │
└─────────────────────────────────────────┘
```



### 2.5 核心API接口设计

#### 2.5.1 认证模块 (AppAuthController)

**1. 发送验证码**
```
POST /app/auth/send-code
Content-Type: application/json

Request:
{
  "phone": "13800138000",
  "projectCode": "P2025001"
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

**2. 验证码登录**
```
POST /app/auth/login
Content-Type: application/json

Request:
{
  "phone": "13800138000",
  "projectCode": "P2025001",
  "code": "123456",
  "deviceId": "设备唯一标识",
  "deviceInfo": {
    "model": "iPhone 13",
    "system": "iOS 15.0",
    "platform": "WeChat"
  }
}

Response:
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200,  // Access Token有效期（秒）
    "customer": {
      "id": "C001",                    // customers表的id
      "name": "张三",
      "phone": "138****8000",          // 脱敏显示
      "avatar": "https://...",
      "level": "VIP"                   // 客户等级
    },
    "project": {
      "id": "P001",                    // projects表的id
      "code": "P2025001",
      "customerId": "C001",            // 关联的客户ID
      "name": "万科城市花园A栋1001",
      "address": "深圳市南山区***",     // 部分脱敏
      "area": 120.5,
      "status": "construction",
      "stage": "水电阶段"
    }
  }
}

后端处理逻辑：
1. 验证验证码有效性
2. 从 customers 表查询客户：SELECT * FROM customers WHERE phone = ? AND is_active = 1
3. 从 projects 表查询项目：SELECT * FROM projects WHERE code = ?
4. 验证关联关系：project.customer_id == customer.id
5. 生成Token（包含 userType="customer", customerId, projectId）
6. 记录登录日志到 app_login_logs 表
7. 保存Token到 app_tokens 表
```

**3. 微信登录**
```
POST /app/auth/wechat-login
Content-Type: application/json

Request:
{
  "code": "微信授权code",
  "encryptedData": "加密数据",
  "iv": "加密算法初始向量",
  "projectCode": "P2025001",  // 首次登录必填
  "deviceId": "设备唯一标识",
  "deviceInfo": {
    "model": "iPhone 13",
    "system": "iOS 15.0",
    "platform": "WeChat"
  }
}

Response: 同验证码登录
```

**4. Token刷新**
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
  "msg": "刷新成功",
  "data": {
    "accessToken": "new_access_token...",
    "refreshToken": "new_refresh_token...",
    "expiresIn": 7200
  }
}
```

**5. 退出登录**
```
POST /app/auth/logout
Authorization: Bearer {accessToken}

Response:
{
  "code": 200,
  "msg": "退出成功"
}
```

**6. 验证Token有效性**
```
POST /app/auth/verify-token
Authorization: Bearer {accessToken}

Response:
{
  "code": 200,
  "data": {
    "valid": true,
    "expiresIn": 3600  // 剩余有效期（秒）
  }
}
```

#### 2.3.2 项目模块 (AppProjectController)

**1. 获取项目详情**
```
GET /app/project/info
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "P001",
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
      "phone": "13800138000"
    },
    "designer": {
      "name": "李设计师",
      "phone": "13900139000"
    },
    "projectManager": {
      "name": "王经理",
      "phone": "13700137000"
    }
  }
}
```

**2. 获取项目统计**
```
GET /app/project/statistics
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



#### 2.3.3 设计模块 (AppDesignController)

**1. 获取设计方案列表**
```
GET /app/design/list
Authorization: Bearer {token}
Query: ?page=1&pageSize=10

Response:
{
  "code": 200,
  "data": {
    "total": 3,
    "list": [
      {
        "id": "D001",
        "version": "V3.0",
        "status": "confirmed",
        "statusText": "已确认",
        "createTime": "2025-01-20 10:30:00",
        "updateTime": "2025-01-22 15:20:00",
        "designer": "李设计师",
        "thumbnail": "https://...",
        "roomCount": 8,
        "imageCount": 15
      }
    ]
  }
}
```

**2. 获取设计方案详情**
```
GET /app/design/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "D001",
    "version": "V3.0",
    "status": "confirmed",
    "description": "现代简约风格设计方案",
    "rooms": [
      {
        "id": "R001",
        "name": "客厅",
        "images": [
          {
            "id": "I001",
            "url": "https://...",
            "thumbnail": "https://...",
            "description": "客厅全景图"
          }
        ]
      }
    ],
    "feedbacks": [
      {
        "id": "F001",
        "content": "客厅沙发颜色建议调整",
        "createTime": "2025-01-21 14:30:00",
        "reply": "已调整为米色系",
        "replyTime": "2025-01-22 09:00:00"
      }
    ]
  }
}
```

**3. 提交设计反馈**
```
POST /app/design/feedback
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "designId": "D001",
  "roomId": "R001",
  "content": "建议调整电视背景墙颜色",
  "images": ["https://...", "https://..."]
}

Response:
{
  "code": 200,
  "msg": "反馈提交成功"
}
```

#### 2.3.4 施工日志模块 (AppConstructController)

**1. 获取施工日志列表**
```
GET /app/construct/logs
Authorization: Bearer {token}
Query: ?page=1&pageSize=10&category=水电&startDate=2025-01-01&endDate=2025-01-31

Response:
{
  "code": 200,
  "data": {
    "total": 25,
    "list": [
      {
        "id": "L001",
        "date": "2025-01-25",
        "category": "水电",
        "title": "客厅水电布线",
        "content": "完成客厅强弱电布线工作",
        "worker": "张师傅",
        "imageCount": 6,
        "thumbnail": "https://...",
        "weather": "晴",
        "temperature": "15-22℃"
      }
    ]
  }
}
```

**2. 获取施工日志详情**
```
GET /app/construct/logs/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "L001",
    "date": "2025-01-25",
    "category": "水电",
    "title": "客厅水电布线",
    "content": "完成客厅强弱电布线工作，包括...",
    "worker": "张师傅",
    "workerPhone": "13600136000",
    "images": [
      {
        "id": "I001",
        "url": "https://...",
        "thumbnail": "https://...",
        "description": "强电箱布线"
      }
    ],
    "materials": [
      {
        "name": "电线",
        "spec": "BV2.5平方",
        "quantity": "200米",
        "brand": "正泰"
      }
    ],
    "qualityCheck": {
      "result": "合格",
      "checker": "王监理",
      "checkTime": "2025-01-25 17:00:00"
    }
  }
}
```



#### 2.3.5 施工排期模块 (AppScheduleController)

**1. 获取排期列表**
```
GET /app/schedule/list
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": [
    {
      "id": "S001",
      "stage": "水电阶段",
      "startDate": "2025-01-15",
      "endDate": "2025-02-05",
      "status": "in_progress",
      "statusText": "进行中",
      "progress": 75,
      "manager": "张工",
      "tasks": [
        {
          "id": "T001",
          "name": "水电定位",
          "status": "completed",
          "completedDate": "2025-01-16"
        },
        {
          "id": "T002",
          "name": "水电布线",
          "status": "in_progress",
          "progress": 80
        }
      ]
    }
  ]
}
```

**2. 获取排期日历**
```
GET /app/schedule/calendar
Authorization: Bearer {token}
Query: ?year=2025&month=1

Response:
{
  "code": 200,
  "data": {
    "year": 2025,
    "month": 1,
    "days": [
      {
        "date": "2025-01-15",
        "events": [
          {
            "id": "E001",
            "title": "水电定位",
            "type": "task",
            "status": "completed"
          }
        ]
      }
    ]
  }
}
```

#### 2.3.6 预算模块 (AppBudgetController)

**1. 获取预算总览**
```
GET /app/budget/overview
Authorization: Bearer {token}

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
```

**2. 获取预算明细**
```
GET /app/budget/items
Authorization: Bearer {token}
Query: ?category=人工费

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

**3. 获取支付记录**
```
GET /app/budget/payments
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": [
    {
      "id": "P001",
      "amount": 105000.00,
      "paymentDate": "2025-01-10",
      "paymentMethod": "银行转账",
      "stage": "首期款（30%）",
      "receipt": "https://...",
      "status": "completed"
    }
  ]
}
```



#### 2.3.7 物料模块 (AppMaterialController)

**1. 获取物料清单**
```
GET /app/material/list
Authorization: Bearer {token}
Query: ?category=主材

Response:
{
  "code": 200,
  "data": [
    {
      "id": "M001",
      "category": "主材",
      "name": "地砖",
      "spec": "800x800mm",
      "brand": "马可波罗",
      "quantity": 120,
      "unit": "片",
      "status": "purchased",
      "statusText": "已采购",
      "image": "https://...",
      "purchaseDate": "2025-01-20",
      "arrivalDate": "2025-01-25"
    }
  ]
}
```

**2. 获取物料详情**
```
GET /app/material/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "M001",
    "name": "地砖",
    "spec": "800x800mm",
    "brand": "马可波罗",
    "model": "CH8003",
    "quantity": 120,
    "unit": "片",
    "unitPrice": 85.00,
    "totalPrice": 10200.00,
    "images": ["https://..."],
    "description": "全抛釉地砖，适用于客厅、餐厅",
    "purchaseInfo": {
      "supplier": "建材市场A店",
      "purchaseDate": "2025-01-20",
      "arrivalDate": "2025-01-25",
      "status": "arrived"
    }
  }
}
```

#### 2.3.8 质检模块 (AppQualityController)

**1. 获取质检记录列表**
```
GET /app/quality/inspections
Authorization: Bearer {token}

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
```

**2. 获取质检详情**
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
        "severityText": "中等",
        "images": ["https://..."],
        "status": "fixed",
        "fixDate": "2025-02-06",
        "fixImages": ["https://..."]
      }
    ]
  }
}
```

**3. 提交质量问题反馈**
```
POST /app/quality/feedback
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "description": "卧室墙面有裂缝",
  "location": "主卧室",
  "images": ["https://...", "https://..."]
}

Response:
{
  "code": 200,
  "msg": "反馈提交成功",
  "data": {
    "id": "F001",
    "trackingNumber": "QF20250125001"
  }
}
```

#### 2.3.9 工地巡视模块 (AppInspectionController) - 员工专属

**1. 提交巡视记录**
```
POST /app/inspection/submit
Authorization: Bearer {token}
X-Project-Id: P001
Content-Type: application/json

Request:
{
  "inspectionType": "daily",        // 巡视类型：daily-日常巡视, safety-安全检查, quality-质量检查
  "inspectionDate": "2025-01-25",
  "location": "3号楼2单元",
  "content": "检查施工进度和现场安全情况",
  "images": ["https://...", "https://..."],
  "issues": [                       // 发现的问题（可选）
    {
      "description": "脚手架未按规范搭建",
      "severity": "high",
      "images": ["https://..."]
    }
  ],
  "weather": "晴",
  "temperature": "15-22℃"
}

Response:
{
  "code": 200,
  "msg": "巡视记录提交成功",
  "data": {
    "id": "INS001",
    "inspectionNumber": "XS20250125001"
  }
}
```

**2. 获取巡视记录列表**
```
GET /app/inspection/list
Authorization: Bearer {token}
X-Project-Id: P001
Query: ?page=1&pageSize=10&type=daily&startDate=2025-01-01&endDate=2025-01-31

Response:
{
  "code": 200,
  "data": {
    "total": 25,
    "list": [
      {
        "id": "INS001",
        "inspectionNumber": "XS20250125001",
        "inspectionType": "daily",
        "inspectionTypeText": "日常巡视",
        "inspectionDate": "2025-01-25",
        "inspector": "张工",
        "location": "3号楼2单元",
        "issueCount": 2,
        "imageCount": 5,
        "createTime": "2025-01-25 14:30:00"
      }
    ]
  }
}
```

**3. 获取巡视记录详情**
```
GET /app/inspection/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "INS001",
    "inspectionNumber": "XS20250125001",
    "inspectionType": "daily",
    "inspectionDate": "2025-01-25",
    "inspector": "张工",
    "location": "3号楼2单元",
    "content": "检查施工进度和现场安全情况",
    "images": ["https://...", "https://..."],
    "issues": [
      {
        "id": "ISS001",
        "description": "脚手架未按规范搭建",
        "severity": "high",
        "images": ["https://..."],
        "status": "pending"
      }
    ],
    "weather": "晴",
    "temperature": "15-22℃",
    "createTime": "2025-01-25 14:30:00"
  }
}
```

#### 2.3.10 问题上报模块 (AppIssueController) - 员工专属

**1. 上报问题**
```
POST /app/issue/report
Authorization: Bearer {token}
X-Project-Id: P001
Content-Type: application/json

Request:
{
  "issueType": "safety",            // 问题类型：safety-安全, quality-质量, progress-进度, other-其他
  "title": "脚手架安全隐患",
  "description": "3号楼2单元脚手架未按规范搭建，存在安全隐患",
  "location": "3号楼2单元",
  "severity": "high",               // 严重程度：low-低, medium-中, high-高, urgent-紧急
  "images": ["https://...", "https://..."],
  "reportDate": "2025-01-25"
}

Response:
{
  "code": 200,
  "msg": "问题上报成功",
  "data": {
    "id": "ISSUE001",
    "issueNumber": "WT20250125001"
  }
}
```

**2. 获取问题列表**
```
GET /app/issue/list
Authorization: Bearer {token}
X-Project-Id: P001
Query: ?page=1&pageSize=10&type=safety&status=pending&severity=high

Response:
{
  "code": 200,
  "data": {
    "total": 15,
    "list": [
      {
        "id": "ISSUE001",
        "issueNumber": "WT20250125001",
        "issueType": "safety",
        "issueTypeText": "安全问题",
        "title": "脚手架安全隐患",
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
```

**3. 获取问题详情**
```
GET /app/issue/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "ISSUE001",
    "issueNumber": "WT20250125001",
    "issueType": "safety",
    "title": "脚手架安全隐患",
    "description": "3号楼2单元脚手架未按规范搭建，存在安全隐患",
    "location": "3号楼2单元",
    "severity": "high",
    "status": "pending",
    "reporter": "张工",
    "reportDate": "2025-01-25",
    "images": ["https://...", "https://..."],
    "repairs": []  // 整改记录列表
  }
}
```

#### 2.3.11 整改修复模块 (AppRepairController) - 员工专属

**1. 提交整改记录**
```
POST /app/repair/submit
Authorization: Bearer {token}
X-Project-Id: P001
Content-Type: application/json

Request:
{
  "issueId": "ISSUE001",            // 关联的问题ID
  "repairContent": "已按规范重新搭建脚手架，加固连接点",
  "repairDate": "2025-01-26",
  "repairer": "李工",
  "images": ["https://...", "https://..."],  // 整改后照片
  "remark": "已通过安全检查"
}

Response:
{
  "code": 200,
  "msg": "整改记录提交成功",
  "data": {
    "id": "REP001",
    "repairNumber": "ZG20250126001"
  }
}
```

**2. 获取整改记录列表**
```
GET /app/repair/list
Authorization: Bearer {token}
X-Project-Id: P001
Query: ?page=1&pageSize=10&issueId=ISSUE001

Response:
{
  "code": 200,
  "data": {
    "total": 5,
    "list": [
      {
        "id": "REP001",
        "repairNumber": "ZG20250126001",
        "issueNumber": "WT20250125001",
        "issueTitle": "脚手架安全隐患",
        "repairContent": "已按规范重新搭建脚手架",
        "repairer": "李工",
        "repairDate": "2025-01-26",
        "imageCount": 4,
        "createTime": "2025-01-26 10:30:00"
      }
    ]
  }
}
```

**3. 获取整改记录详情**
```
GET /app/repair/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "id": "REP001",
    "repairNumber": "ZG20250126001",
    "issue": {
      "id": "ISSUE001",
      "issueNumber": "WT20250125001",
      "title": "脚手架安全隐患",
      "severity": "high"
    },
    "repairContent": "已按规范重新搭建脚手架，加固连接点",
    "repairDate": "2025-01-26",
    "repairer": "李工",
    "images": ["https://...", "https://..."],
    "remark": "已通过安全检查",
    "createTime": "2025-01-26 10:30:00"
  }
}
```

### 2.4 通用接口设计

#### 2.4.1 文件上传
```
POST /app/upload/image
Authorization: Bearer {token}
Content-Type: multipart/form-data

Request:
- file: 图片文件
- type: 类型（feedback/avatar等）

Response:
{
  "code": 200,
  "data": {
    "url": "https://...",
    "thumbnail": "https://..."
  }
}
```

#### 2.4.2 消息通知
```
GET /app/notifications
Authorization: Bearer {token}
Query: ?page=1&pageSize=20&type=all

Response:
{
  "code": 200,
  "data": {
    "unreadCount": 5,
    "list": [
      {
        "id": "N001",
        "type": "schedule",
        "title": "施工进度更新",
        "content": "水电阶段已完成",
        "isRead": false,
        "createTime": "2025-02-05 18:00:00"
      }
    ]
  }
}
```

### 2.5 错误码规范

```
200  - 成功
400  - 请求参数错误
401  - 未授权（Token无效或过期）
403  - 无权限访问
404  - 资源不存在
500  - 服务器内部错误

业务错误码（10000+）：
10001 - 手机号格式错误
10002 - 项目编号不存在
10003 - 客户与项目不匹配
10004 - Token已过期
10005 - 文件上传失败
10006 - 数据不存在
```



## 2.3 双用户体系技术实现

### 2.3.1 认证服务设计

**客户认证服务（AppAuthService）：**
```java
@Service
public class AppAuthServiceImpl implements IAppAuthService {
    
    @Autowired
    private ICustomersService customersService;
    
    @Autowired
    private IProjectsService projectsService;
    
    @Autowired
    private AppTokenManager tokenManager;
    
    @Autowired
    private SmsService smsService;
    
    /**
     * 发送验证码
     */
    @Override
    public void sendVerificationCode(String phone, String projectCode) {
        // 1. 验证手机号格式
        if (!PhoneUtil.isValid(phone)) {
            throw new BusinessException("手机号格式错误");
        }
        
        // 2. 查询客户（从customers表）
        Customer customer = customersService.selectByPhone(phone);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        
        if (!customer.getIsActive()) {
            throw new BusinessException("客户已被禁用");
        }
        
        // 3. 查询项目（从projects表）
        Project project = projectsService.selectByCode(projectCode);
        if (project == null) {
            throw new BusinessException("项目编号不存在");
        }
        
        // 4. 验证客户与项目的关联关系
        if (!project.getCustomerId().equals(customer.getId())) {
            throw new BusinessException("客户与项目不匹配");
        }
        
        // 5. 检查发送频率限制
        checkSmsFrequency(phone);
        
        // 6. 生成验证码
        String code = RandomUtil.generateCode(6);
        
        // 7. 保存验证码到数据库
        saveSmsCode(phone, code, projectCode);
        
        // 8. 发送短信
        smsService.sendCode(phone, code);
    }
    
    /**
     * 验证码登录
     */
    @Override
    public AppLoginResponse login(AppLoginRequest request) {
        // 1. 验证验证码
        verifySmsCode(request.getPhone(), request.getCode());
        
        // 2. 查询客户信息（从customers表）
        Customer customer = customersService.selectByPhone(request.getPhone());
        if (customer == null || !customer.getIsActive()) {
            throw new BusinessException("客户不存在或已禁用");
        }
        
        // 3. 查询项目信息（从projects表）
        Project project = projectsService.selectByCode(request.getProjectCode());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        
        // 4. 再次验证关联关系（防御性编程）
        if (!project.getCustomerId().equals(customer.getId())) {
            throw new BusinessException("客户与项目不匹配");
        }
        
        // 5. 检查设备绑定（如果启用）
        if (appConfigUtil.isDeviceBindingEnabled()) {
            checkDeviceBinding(customer.getId(), request.getDeviceId());
        }
        
        // 6. 生成Token
        String accessToken = tokenManager.generateAccessToken(
            customer.getId(), 
            project.getId(), 
            request.getDeviceId()
        );
        String refreshToken = tokenManager.generateRefreshToken(
            customer.getId()
        );
        
        // 7. 保存Token到数据库
        saveToken(customer.getId(), project.getId(), accessToken, refreshToken, request.getDeviceId());
        
        // 8. 记录登录日志
        recordLoginLog(customer.getId(), project.getId(), request, "success");
        
        // 9. 标记验证码已使用
        markSmsCodeUsed(request.getPhone(), request.getCode());
        
        // 10. 构建响应
        return AppLoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(appConfigUtil.getTokenExpireHours() * 3600)
            .customer(convertToCustomerVO(customer))
            .project(convertToProjectVO(project))
            .build();
    }
}
```

**运营端认证服务（SysLoginService）对比：**
```java
@Service
public class SysLoginService {
    
    @Autowired
    private ISysUserService userService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    /**
     * 运营人员登录
     */
    public String login(String username, String password) {
        // 1. 使用Spring Security认证
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        
        // 2. 从sys_user表获取用户信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        SysUser user = loginUser.getUser();
        
        // 3. 查询用户角色和权限
        Set<String> roles = permissionService.getRolePermission(user);
        Set<String> permissions = permissionService.getMenuPermission(user);
        
        // 4. 生成Token（包含userId和roles）
        String token = tokenService.createToken(loginUser);
        
        // 5. 记录登录日志
        recordLoginInfo(user.getUserId());
        
        return token;
    }
}
```

### 2.3.2 Token管理器设计

```java
@Component
public class AppTokenManager {
    
    @Autowired
    private AppConfigUtil configUtil;
    
    @Autowired
    private RedisCache redisCache;
    
    private static final String TOKEN_PREFIX = "app:token:";
    
    /**
     * 生成Access Token
     */
    public String generateAccessToken(String customerId, String projectId, String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", "customer");      // 标识为客户
        claims.put("customerId", customerId);
        claims.put("projectId", projectId);
        claims.put("deviceId", deviceId);
        claims.put("jti", UUID.randomUUID().toString());
        
        int expireHours = configUtil.getTokenExpireHours();
        Date expireDate = DateUtils.addHours(new Date(), expireHours);
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(new Date())
            .setExpiration(expireDate)
            .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
            .compact();
    }
    
    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            // 1. 解析Token
            Claims claims = parseToken(token);
            
            // 2. 验证用户类型
            String userType = claims.get("userType", String.class);
            if (!"customer".equals(userType)) {
                return false;
            }
            
            // 3. 检查是否在黑名单中
            String jti = claims.get("jti", String.class);
            if (redisCache.hasKey(TOKEN_PREFIX + "blacklist:" + jti)) {
                return false;
            }
            
            // 4. 验证过期时间
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 撤销Token（加入黑名单）
     */
    public void revokeToken(String token) {
        Claims claims = parseToken(token);
        String jti = claims.get("jti", String.class);
        Date expiration = claims.getExpiration();
        
        // 计算剩余有效期
        long ttl = expiration.getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            // 加入黑名单，过期时间与Token一致
            redisCache.setCacheObject(
                TOKEN_PREFIX + "blacklist:" + jti, 
                true, 
                ttl, 
                TimeUnit.MILLISECONDS
            );
        }
    }
}
```

### 2.3.3 拦截器配置

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private AppAuthInterceptor appAuthInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 客户端接口拦截器
        registry.addInterceptor(appAuthInterceptor)
            .addPathPatterns("/app/**")
            .excludePathPatterns(
                "/app/auth/send-code",      // 发送验证码
                "/app/auth/login",          // 登录
                "/app/auth/wechat-login",   // 微信登录
                "/app/auth/refresh-token"   // 刷新Token
            )
            .order(1);
        
        // 运营端使用Spring Security，不需要额外拦截器
    }
}
```

### 2.3.4 数据库查询示例

**客户端查询（自动注入项目ID）：**
```xml
<!-- AppProjectMapper.xml -->
<select id="selectProjectInfo" resultType="Project">
    SELECT 
        p.*,
        c.name as customer_name,
        c.phone as customer_phone
    FROM projects p
    LEFT JOIN customers c ON p.customer_id = c.id
    WHERE p.id = #{projectId}
      AND p.customer_id = #{customerId}  <!-- 自动注入，双重验证 -->
      AND p.deleted_at IS NULL
</select>
```

**运营端查询（基于角色权限）：**
```xml
<!-- ProjectsMapper.xml -->
<select id="selectProjectsList" resultType="Projects">
    SELECT p.*, c.name as customer_name
    FROM projects p
    LEFT JOIN customers c ON p.customer_id = c.id
    WHERE p.deleted_at IS NULL
    <if test="userId != null and !isAdmin">
        <!-- 非管理员只能看自己负责的项目 -->
        AND EXISTS (
            SELECT 1 FROM project_members pm
            WHERE pm.project_id = p.id
              AND pm.user_id = #{userId}
        )
    </if>
    ORDER BY p.created_at DESC
</select>
```

### 2.3.5 用户信息获取工具类

```java
/**
 * 用户上下文工具类
 * 根据不同的用户类型返回不同的信息
 */
public class UserContextUtil {
    
    /**
     * 获取当前用户类型
     */
    public static String getUserType() {
        // 先尝试从客户端上下文获取
        String customerId = AppContext.getCustomerId();
        if (customerId != null) {
            return "customer";
        }
        
        // 再尝试从运营端上下文获取
        Long userId = SecurityUtils.getUserId();
        if (userId != null) {
            return "staff";
        }
        
        return null;
    }
    
    /**
     * 获取当前用户ID（统一接口）
     */
    public static String getCurrentUserId() {
        String userType = getUserType();
        if ("customer".equals(userType)) {
            return AppContext.getCustomerId();
        } else if ("staff".equals(userType)) {
            return String.valueOf(SecurityUtils.getUserId());
        }
        return null;
    }
    
    /**
     * 判断是否为客户
     */
    public static boolean isCustomer() {
        return "customer".equals(getUserType());
    }
    
    /**
     * 判断是否为运营人员
     */
    public static boolean isStaff() {
        return "staff".equals(getUserType());
    }
}
```

## 3. 前端架构设计

### 3.1 目录结构优化

```
uni3/src/
├── api/                          # API接口层（新增）
│   ├── auth.js                   # 认证相关
│   ├── project.js                # 项目相关
│   ├── design.js                 # 设计相关
│   ├── construct.js              # 施工相关
│   ├── schedule.js               # 排期相关
│   ├── budget.js                 # 预算相关
│   ├── material.js               # 物料相关
│   ├── quality.js                # 质检相关
│   ├── inspection.js             # 工地巡视（员工专属）
│   ├── issue.js                  # 问题上报（员工专属）
│   ├── repair.js                 # 整改修复（员工专属）
│   └── upload.js                 # 文件上传
│
├── stores/                       # 状态管理（现有，需完善）
│   ├── user.js                   # 用户状态（含角色信息）
│   ├── project.js                # 项目状态（含项目列表）
│   └── app.js                    # 应用状态
│
├── utils/                        # 工具函数
│   ├── request.js                # 请求封装（现有，需完善）
│   ├── auth.js                   # 认证工具（新增）
│   ├── permission.js             # 权限判断工具（新增）
│   ├── date.js                   # 日期处理（新增）
│   ├── image.js                  # 图片处理（新增）
│   └── validator.js              # 表单验证（新增）
│
├── components/                   # 公共组件
│   ├── ImagePreview/             # 图片预览组件（新增）
│   ├── ImageUpload/              # 图片上传组件（新增）
│   ├── ProjectSelector/          # 项目选择器（新增）
│   ├── EmptyState/               # 空状态组件（新增）
│   └── LoadingState/             # 加载状态组件（新增）
│
├── pages/                        # 页面
│   ├── login/                    # 登录页（现有）
│   ├── dashboard/                # 首页（现有，需完善）
│   │   ├── customer.vue          # 客户首页
│   │   └── staff.vue             # 员工首页
│   ├── design/                   # 设计方案（通用）
│   ├── construct/                # 施工日志（通用）
│   ├── schedule/                 # 施工排期（通用）
│   ├── budget/                   # 预算管理（通用）
│   ├── material/                 # 物料清单（通用）
│   ├── quality/                  # 质检管理（通用）
│   ├── inspection/               # 工地巡视（员工专属）
│   │   ├── list.vue              # 巡视记录列表
│   │   ├── detail.vue            # 巡视记录详情
│   │   └── submit.vue            # 提交巡视记录
│   ├── issue/                    # 问题上报（员工专属）
│   │   ├── list.vue              # 问题列表
│   │   ├── detail.vue            # 问题详情
│   │   └── report.vue            # 上报问题
│   ├── repair/                   # 整改修复（员工专属）
│   │   ├── list.vue              # 整改记录列表
│   │   ├── detail.vue            # 整改记录详情
│   │   └── submit.vue            # 提交整改记录
│   └── profile/                  # 个人中心（通用）
│
├── mixins/                       # 混入
│   └── roleCheck.js              # 角色检查混入（新增）
│
└── config/                       # 配置文件
    ├── app.js                    # 应用配置（现有）
    ├── api.js                    # API配置（新增）
    └── pages.js                  # 页面配置（角色权限）
```

### 3.2 前端角色权限控制

#### 3.2.1 权限判断工具 (utils/permission.js)

```javascript
import { useUserStore } from '@/stores/user.js'

/**
 * 判断是否为员工
 */
export const isStaff = () => {
  const userStore = useUserStore()
  return userStore.userType === 'staff'
}

/**
 * 判断是否为客户
 */
export const isCustomer = () => {
  const userStore = useUserStore()
  return userStore.userType === 'customer'
}

/**
 * 检查是否有权限访问页面
 */
export const hasPagePermission = (pagePath) => {
  const staffOnlyPages = [
    '/pages/inspection/',
    '/pages/issue/',
    '/pages/repair/'
  ]
  
  if (isCustomer()) {
    // 客户不能访问员工专属页面
    return !staffOnlyPages.some(path => pagePath.startsWith(path))
  }
  
  return true
}
```

#### 3.2.2 路由守卫 (main.js)

```javascript
import { hasPagePermission } from '@/utils/permission.js'

// 页面跳转拦截
uni.addInterceptor('navigateTo', {
  invoke(args) {
    const url = args.url
    if (!hasPagePermission(url)) {
      uni.showToast({
        title: '该功能仅员工可用',
        icon: 'none'
      })
      return false
    }
  }
})

uni.addInterceptor('redirectTo', {
  invoke(args) {
    const url = args.url
    if (!hasPagePermission(url)) {
      uni.showToast({
        title: '该功能仅员工可用',
        icon: 'none'
      })
      return false
    }
  }
})
```

#### 3.2.3 角色检查混入 (mixins/roleCheck.js)

```javascript
import { isStaff, isCustomer } from '@/utils/permission.js'

export default {
  data() {
    return {
      isStaff: false,
      isCustomer: false
    }
  },
  onLoad() {
    this.isStaff = isStaff()
    this.isCustomer = isCustomer()
    
    // 如果是员工专属页面，检查权限
    if (this.$options.requireStaff && !this.isStaff) {
      uni.showToast({
        title: '该功能仅员工可用',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  }
}

// 使用示例
// pages/inspection/submit.vue
export default {
  mixins: [roleCheck],
  requireStaff: true,  // 标记为员工专属页面
  // ...
}
```

#### 3.2.4 首页路由分发 (pages/dashboard/index.vue)

```vue
<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()

onMounted(() => {
  // 根据用户类型跳转到对应的首页
  if (userStore.userType === 'customer') {
    uni.redirectTo({
      url: '/pages/dashboard/customer'
    })
  } else if (userStore.userType === 'staff') {
    uni.redirectTo({
      url: '/pages/dashboard/staff'
    })
  }
})
</script>
```

### 3.3 API Service层设计

#### 3.3.1 API配置文件 (config/api.js)

```javascript
// API基础配置
export const API_CONFIG = {
  // 开发环境
  dev: {
    baseURL: 'http://localhost:8080',
    timeout: 30000
  },
  // 生产环境
  prod: {
    baseURL: 'https://api.example.com',
    timeout: 30000
  }
}

// 获取当前环境配置
export const getApiConfig = () => {
  // #ifdef H5
  return API_CONFIG.dev
  // #endif
  // #ifndef H5
  return API_CONFIG.prod
  // #endif
}
```

#### 3.2.2 请求封装完善 (utils/request.js)

```javascript
import { getApiConfig } from '@/config/api.js'

const { baseURL, timeout } = getApiConfig()

// Token管理
const getToken = () => uni.getStorageSync('token')
const setToken = (token) => uni.setStorageSync('token', token)
const removeToken = () => uni.removeStorageSync('token')

// 请求拦截器
const requestInterceptor = (config) => {
  const token = getToken()
  if (token) {
    config.header = {
      ...config.header,
      'Authorization': `Bearer ${token}`
    }
  }
  return config
}

// 响应拦截器
const responseInterceptor = (response) => {
  const { statusCode, data } = response
  
  if (statusCode === 200) {
    if (data.code === 200) {
      return data.data
    }
    // 业务错误
    uni.showToast({ title: data.msg || '请求失败', icon: 'none' })
    return Promise.reject(new Error(data.msg || '请求失败'))
  }
  
  // 401 未授权
  if (statusCode === 401) {
    removeToken()
    uni.reLaunch({ url: '/pages/login/index' })
    return Promise.reject(new Error('登录已过期'))
  }
  
  // 其他HTTP错误
  uni.showToast({ title: `请求失败(${statusCode})`, icon: 'none' })
  return Promise.reject(new Error(`HTTP Error: ${statusCode}`))
}

// 统一请求方法
const request = (options) => {
  const config = requestInterceptor({
    url: baseURL + options.url,
    method: options.method || 'GET',
    data: options.data,
    header: {
      'Content-Type': 'application/json',
      ...options.header
    },
    timeout: options.timeout || timeout
  })
  
  return new Promise((resolve, reject) => {
    if (options.loading !== false) {
      uni.showLoading({ title: '加载中...', mask: true })
    }
    
    uni.request({
      ...config,
      success: (response) => {
        responseInterceptor(response).then(resolve).catch(reject)
      },
      fail: (error) => {
        uni.showToast({ title: '网络请求失败', icon: 'none' })
        reject(new Error(error.errMsg || '网络请求失败'))
      },
      complete: () => {
        if (options.loading !== false) {
          uni.hideLoading()
        }
      }
    })
  })
}

export const get = (url, data, options = {}) => {
  return request({ url, method: 'GET', data, ...options })
}

export const post = (url, data, options = {}) => {
  return request({ url, method: 'POST', data, ...options })
}

export const put = (url, data, options = {}) => {
  return request({ url, method: 'PUT', data, ...options })
}

export const del = (url, data, options = {}) => {
  return request({ url, method: 'DELETE', data, ...options })
}

export default request
```



#### 3.2.3 API接口封装示例

**认证API (api/auth.js)**
```javascript
import { post } from '@/utils/request.js'

// 客户登录
export const login = (data) => {
  return post('/app/auth/login', data)
}

// 微信登录
export const wechatLogin = (data) => {
  return post('/app/auth/wechat-login', data)
}

// Token刷新
export const refreshToken = () => {
  return post('/app/auth/refresh')
}
```

**项目API (api/project.js)**
```javascript
import { get } from '@/utils/request.js'

// 获取项目详情
export const getProjectInfo = () => {
  return get('/app/project/info')
}

// 获取项目统计
export const getProjectStatistics = () => {
  return get('/app/project/statistics')
}
```

**设计API (api/design.js)**
```javascript
import { get, post } from '@/utils/request.js'

// 获取设计方案列表
export const getDesignList = (params) => {
  return get('/app/design/list', params)
}

// 获取设计方案详情
export const getDesignDetail = (id) => {
  return get(`/app/design/${id}`)
}

// 提交设计反馈
export const submitDesignFeedback = (data) => {
  return post('/app/design/feedback', data)
}
```

### 3.3 状态管理设计

#### 3.3.1 用户状态 (stores/user.js)

```javascript
import { defineStore } from 'pinia'
import { login, wechatLogin } from '@/api/auth.js'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    userInfo: uni.getStorageSync('userInfo') || null,
    projectInfo: uni.getStorageSync('projectInfo') || null
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    customerId: (state) => state.userInfo?.id,
    projectId: (state) => state.projectInfo?.id
  },
  
  actions: {
    // 登录
    async login(loginData) {
      const res = await login(loginData)
      this.setUserInfo(res)
      return res
    },
    
    // 微信登录
    async wechatLogin(loginData) {
      const res = await wechatLogin(loginData)
      this.setUserInfo(res)
      return res
    },
    
    // 设置用户信息
    setUserInfo(data) {
      this.token = data.token
      this.userInfo = data.customer
      this.projectInfo = data.project
      
      uni.setStorageSync('token', data.token)
      uni.setStorageSync('userInfo', data.customer)
      uni.setStorageSync('projectInfo', data.project)
    },
    
    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = null
      this.projectInfo = null
      
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('projectInfo')
      
      uni.reLaunch({ url: '/pages/login/index' })
    }
  }
})
```

#### 3.3.2 项目状态 (stores/project.js)

```javascript
import { defineStore } from 'pinia'
import { getProjectInfo, getProjectStatistics } from '@/api/project.js'

export const useProjectStore = defineStore('project', {
  state: () => ({
    projectDetail: null,
    statistics: null,
    lastUpdateTime: null
  }),
  
  getters: {
    projectStage: (state) => state.projectDetail?.stage,
    projectProgress: (state) => state.projectDetail?.progress || 0
  },
  
  actions: {
    // 获取项目详情
    async fetchProjectInfo(force = false) {
      // 缓存策略：5分钟内不重复请求
      const now = Date.now()
      if (!force && this.lastUpdateTime && (now - this.lastUpdateTime < 5 * 60 * 1000)) {
        return this.projectDetail
      }
      
      const data = await getProjectInfo()
      this.projectDetail = data
      this.lastUpdateTime = now
      return data
    },
    
    // 获取项目统计
    async fetchStatistics() {
      const data = await getProjectStatistics()
      this.statistics = data
      return data
    }
  }
})
```



### 3.4 公共组件设计

#### 3.4.1 图片预览组件 (components/ImagePreview/index.vue)

```vue
<template>
  <view class="image-preview" v-if="visible" @click="close">
    <swiper 
      class="swiper" 
      :current="currentIndex" 
      @change="onChange"
      @click.stop
    >
      <swiper-item v-for="(image, index) in images" :key="index">
        <image 
          :src="image" 
          mode="aspectFit" 
          class="preview-image"
          @click.stop
        />
      </swiper-item>
    </swiper>
    
    <view class="indicator">{{ currentIndex + 1 }} / {{ images.length }}</view>
    
    <view class="actions">
      <button @click.stop="saveImage">保存图片</button>
      <button @click.stop="close">关闭</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  images: {
    type: Array,
    default: () => []
  },
  current: {
    type: Number,
    default: 0
  }
})

const visible = ref(false)
const currentIndex = ref(props.current)

const open = (index = 0) => {
  currentIndex.value = index
  visible.value = true
}

const close = () => {
  visible.value = false
}

const onChange = (e) => {
  currentIndex.value = e.detail.current
}

const saveImage = () => {
  const imageUrl = props.images[currentIndex.value]
  uni.saveImageToPhotosAlbum({
    filePath: imageUrl,
    success: () => {
      uni.showToast({ title: '保存成功', icon: 'success' })
    },
    fail: () => {
      uni.showToast({ title: '保存失败', icon: 'none' })
    }
  })
}

defineExpose({ open, close })
</script>
```

#### 3.4.2 图片上传组件 (components/ImageUpload/index.vue)

```vue
<template>
  <view class="image-upload">
    <view class="image-list">
      <view 
        class="image-item" 
        v-for="(image, index) in imageList" 
        :key="index"
      >
        <image :src="image" mode="aspectFill" />
        <view class="delete-btn" @click="removeImage(index)">
          <u-icon name="close" color="#fff" size="20" />
        </view>
      </view>
      
      <view 
        class="upload-btn" 
        v-if="imageList.length < maxCount"
        @click="chooseImage"
      >
        <u-icon name="plus" color="#999" size="40" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { uploadImage } from '@/api/upload.js'

const props = defineProps({
  maxCount: {
    type: Number,
    default: 9
  },
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const imageList = ref([...props.modelValue])

const chooseImage = () => {
  const remainCount = props.maxCount - imageList.value.length
  
  uni.chooseImage({
    count: remainCount,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      uni.showLoading({ title: '上传中...' })
      
      try {
        for (const filePath of res.tempFilePaths) {
          const url = await uploadImage(filePath)
          imageList.value.push(url)
        }
        emit('update:modelValue', imageList.value)
      } catch (error) {
        uni.showToast({ title: '上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}

const removeImage = (index) => {
  imageList.value.splice(index, 1)
  emit('update:modelValue', imageList.value)
}
</script>
```



## 4. 数据库设计

### 4.1 现有表结构（复用）

基于现有的 evs-home 模块数据库表：

- `projects` - 项目表
- `customers` - 客户表
- `project_budgets` - 项目预算表
- `project_schedules` - 项目排期表
- `project_schedule_records` - 排期记录表
- `project_members` - 项目成员表
- `project_rooms` - 项目房间表
- `file_uploads` - 文件上传表
- `quality_inspections` - 质检表
- `quality_issues` - 质检问题表
- `quality_fixes` - 质检整改表

### 4.2 新增表设计

#### 4.2.1 客户登录记录表 (app_login_logs)

```sql
CREATE TABLE `app_login_logs` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `customer_id` varchar(50) NOT NULL COMMENT '客户ID',
  `project_id` varchar(50) NOT NULL COMMENT '项目ID',
  `login_type` varchar(20) NOT NULL COMMENT '登录类型：sms/wechat',
  `login_ip` varchar(50) DEFAULT NULL COMMENT '登录IP',
  `device_id` varchar(100) DEFAULT NULL COMMENT '设备唯一标识',
  `device_info` text COMMENT '设备信息（JSON）',
  `login_status` varchar(20) DEFAULT 'success' COMMENT '登录状态：success/failed',
  `fail_reason` varchar(200) DEFAULT NULL COMMENT '失败原因',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `location` varchar(200) DEFAULT NULL COMMENT '登录地点（根据IP解析）',
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户登录记录表';
```

#### 4.2.2 Token管理表 (app_tokens)

```sql
CREATE TABLE `app_tokens` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `customer_id` varchar(50) NOT NULL COMMENT '客户ID',
  `project_id` varchar(50) NOT NULL COMMENT '项目ID',
  `access_token` varchar(500) NOT NULL COMMENT 'Access Token',
  `refresh_token` varchar(500) NOT NULL COMMENT 'Refresh Token',
  `device_id` varchar(100) NOT NULL COMMENT '设备唯一标识',
  `access_token_expire` datetime NOT NULL COMMENT 'Access Token过期时间',
  `refresh_token_expire` datetime NOT NULL COMMENT 'Refresh Token过期时间',
  `is_revoked` tinyint(1) DEFAULT 0 COMMENT '是否已撤销',
  `revoke_time` datetime DEFAULT NULL COMMENT '撤销时间',
  `revoke_reason` varchar(200) DEFAULT NULL COMMENT '撤销原因',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `last_use_time` datetime DEFAULT NULL COMMENT '最后使用时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_access_token` (`access_token`(255)),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_expire` (`access_token_expire`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token管理表';
```

#### 4.2.3 短信验证码表 (app_sms_codes)

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

#### 4.2.4 审计日志表 (app_audit_logs)

```sql
CREATE TABLE `app_audit_logs` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `customer_id` varchar(50) NOT NULL COMMENT '客户ID',
  `project_id` varchar(50) NOT NULL COMMENT '项目ID',
  `action` varchar(100) NOT NULL COMMENT '操作类型',
  `resource_type` varchar(50) DEFAULT NULL COMMENT '资源类型',
  `resource_id` varchar(50) DEFAULT NULL COMMENT '资源ID',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `device_id` varchar(100) DEFAULT NULL COMMENT '设备ID',
  `device_info` text COMMENT '设备信息',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `request_data` text COMMENT '请求数据',
  `response_code` int DEFAULT NULL COMMENT '响应码',
  `response_msg` varchar(500) DEFAULT NULL COMMENT '响应消息',
  `execute_time` int DEFAULT NULL COMMENT '执行时长（毫秒）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户端审计日志表';
```

#### 4.2.5 设计方案表 (design_schemes)

```sql
CREATE TABLE `design_schemes` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `project_id` varchar(50) NOT NULL COMMENT '项目ID',
  `version` varchar(20) NOT NULL COMMENT '版本号',
  `status` varchar(20) NOT NULL COMMENT '状态：draft/pending/confirmed/rejected',
  `description` text COMMENT '方案描述',
  `designer_id` varchar(50) DEFAULT NULL COMMENT '设计师ID',
  `designer_name` varchar(50) DEFAULT NULL COMMENT '设计师姓名',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计方案表';
```

#### 4.2.6 设计方案图片表 (design_images)

```sql
CREATE TABLE `design_images` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `scheme_id` varchar(50) NOT NULL COMMENT '方案ID',
  `room_id` varchar(50) DEFAULT NULL COMMENT '房间ID',
  `room_name` varchar(50) DEFAULT NULL COMMENT '房间名称',
  `image_url` varchar(500) NOT NULL COMMENT '图片URL',
  `thumbnail_url` varchar(500) DEFAULT NULL COMMENT '缩略图URL',
  `description` varchar(200) DEFAULT NULL COMMENT '图片描述',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_scheme_id` (`scheme_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计方案图片表';
```

#### 4.2.7 设计反馈表 (design_feedbacks)

```sql
CREATE TABLE `design_feedbacks` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `scheme_id` varchar(50) NOT NULL COMMENT '方案ID',
  `room_id` varchar(50) DEFAULT NULL COMMENT '房间ID',
  `customer_id` varchar(50) NOT NULL COMMENT '客户ID',
  `content` text NOT NULL COMMENT '反馈内容',
  `images` text COMMENT '反馈图片（JSON数组）',
  `reply` text COMMENT '回复内容',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_scheme_id` (`scheme_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计反馈表';
```

#### 4.2.8 施工日志表 (construction_logs)

```sql
CREATE TABLE `construction_logs` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `project_id` varchar(50) NOT NULL COMMENT '项目ID',
  `log_date` date NOT NULL COMMENT '日志日期',
  `category` varchar(50) NOT NULL COMMENT '施工类别：水电/泥工/木工等',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `worker_name` varchar(50) DEFAULT NULL COMMENT '施工人员',
  `worker_phone` varchar(20) DEFAULT NULL COMMENT '施工人员电话',
  `weather` varchar(20) DEFAULT NULL COMMENT '天气',
  `temperature` varchar(20) DEFAULT NULL COMMENT '温度',
  `images` text COMMENT '图片列表（JSON数组）',
  `materials` text COMMENT '使用材料（JSON数组）',
  `quality_check` text COMMENT '质量检查（JSON对象）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_log_date` (`log_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='施工日志表';
```

#### 4.2.9 物料清单表 (project_materials)

```sql
CREATE TABLE `project_materials` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `project_id` varchar(50) NOT NULL COMMENT '项目ID',
  `category` varchar(50) NOT NULL COMMENT '类别：主材/辅材/家具/家电',
  `name` varchar(100) NOT NULL COMMENT '材料名称',
  `spec` varchar(100) DEFAULT NULL COMMENT '规格',
  `brand` varchar(100) DEFAULT NULL COMMENT '品牌',
  `model` varchar(100) DEFAULT NULL COMMENT '型号',
  `quantity` decimal(10,2) NOT NULL COMMENT '数量',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `unit_price` decimal(10,2) DEFAULT NULL COMMENT '单价',
  `total_price` decimal(10,2) DEFAULT NULL COMMENT '总价',
  `image_url` varchar(500) DEFAULT NULL COMMENT '图片URL',
  `description` text COMMENT '描述',
  `supplier` varchar(100) DEFAULT NULL COMMENT '供应商',
  `purchase_date` date DEFAULT NULL COMMENT '采购日期',
  `arrival_date` date DEFAULT NULL COMMENT '到货日期',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending/purchased/arrived/used',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目物料表';
```

#### 4.2.10 微信绑定表 (app_wechat_bindings)

```sql
CREATE TABLE `app_wechat_bindings` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `open_id` varchar(100) NOT NULL COMMENT '微信openId',
  `union_id` varchar(100) DEFAULT NULL COMMENT '微信unionId',
  `user_type` varchar(20) NOT NULL COMMENT '用户类型：customer/staff',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID（customers.id 或 sys_user.user_id）',
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

#### 4.2.11 工地巡视表 (app_site_inspections)

```sql
CREATE TABLE `app_site_inspections` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `inspection_number` varchar(50) NOT NULL COMMENT '巡视编号',
  `project_id` varchar(50) NOT NULL COMMENT '项目ID',
  `inspection_type` varchar(20) NOT NULL COMMENT '巡视类型：daily/safety/quality',
  `inspection_date` date NOT NULL COMMENT '巡视日期',
  `inspector_id` varchar(50) NOT NULL COMMENT '巡视人ID（sys_user.user_id）',
  `inspector_name` varchar(50) NOT NULL COMMENT '巡视人姓名',
  `location` varchar(200) DEFAULT NULL COMMENT '巡视位置',
  `content` text NOT NULL COMMENT '巡视内容',
  `images` text COMMENT '巡视照片（JSON数组）',
  `issue_count` int DEFAULT 0 COMMENT '发现问题数量',
  `weather` varchar(20) DEFAULT NULL COMMENT '天气',
  `temperature` varchar(20) DEFAULT NULL COMMENT '温度',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inspection_number` (`inspection_number`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_inspection_date` (`inspection_date`),
  KEY `idx_inspector_id` (`inspector_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工地巡视表';
```

#### 4.2.12 问题上报表 (app_issues)

```sql
CREATE TABLE `app_issues` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `issue_number` varchar(50) NOT NULL COMMENT '问题编号',
  `project_id` varchar(50) NOT NULL COMMENT '项目ID',
  `inspection_id` varchar(50) DEFAULT NULL COMMENT '关联的巡视记录ID',
  `issue_type` varchar(20) NOT NULL COMMENT '问题类型：safety/quality/progress/other',
  `title` varchar(200) NOT NULL COMMENT '问题标题',
  `description` text NOT NULL COMMENT '问题描述',
  `location` varchar(200) DEFAULT NULL COMMENT '问题位置',
  `severity` varchar(20) NOT NULL COMMENT '严重程度：low/medium/high/urgent',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending/processing/resolved/closed',
  `reporter_id` varchar(50) NOT NULL COMMENT '上报人ID',
  `reporter_name` varchar(50) NOT NULL COMMENT '上报人姓名',
  `report_date` date NOT NULL COMMENT '上报日期',
  `images` text COMMENT '问题照片（JSON数组）',
  `handler_id` varchar(50) DEFAULT NULL COMMENT '处理人ID',
  `handler_name` varchar(50) DEFAULT NULL COMMENT '处理人姓名',
  `resolve_date` date DEFAULT NULL COMMENT '解决日期',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_issue_number` (`issue_number`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_inspection_id` (`inspection_id`),
  KEY `idx_status` (`status`),
  KEY `idx_reporter_id` (`reporter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题上报表';
```

#### 4.2.13 整改修复表 (app_repairs)

```sql
CREATE TABLE `app_repairs` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `repair_number` varchar(50) NOT NULL COMMENT '整改编号',
  `issue_id` varchar(50) NOT NULL COMMENT '关联的问题ID',
  `project_id` varchar(50) NOT NULL COMMENT '项目ID',
  `repair_content` text NOT NULL COMMENT '整改内容',
  `repair_date` date NOT NULL COMMENT '整改日期',
  `repairer_id` varchar(50) NOT NULL COMMENT '整改人ID',
  `repairer_name` varchar(50) NOT NULL COMMENT '整改人姓名',
  `images` text COMMENT '整改照片（JSON数组）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `is_verified` tinyint(1) DEFAULT 0 COMMENT '是否已验收',
  `verifier_id` varchar(50) DEFAULT NULL COMMENT '验收人ID',
  `verifier_name` varchar(50) DEFAULT NULL COMMENT '验收人姓名',
  `verify_date` date DEFAULT NULL COMMENT '验收日期',
  `verify_result` varchar(20) DEFAULT NULL COMMENT '验收结果：passed/failed',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_repair_number` (`repair_number`),
  KEY `idx_issue_id` (`issue_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_repairer_id` (`repairer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='整改修复表';
```

#### 4.2.14 消息通知表 (app_notifications)

```sql
CREATE TABLE `app_notifications` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `customer_id` varchar(50) NOT NULL COMMENT '客户ID',
  `project_id` varchar(50) NOT NULL COMMENT '项目ID',
  `type` varchar(50) NOT NULL COMMENT '类型：schedule/quality/payment/system',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `link_type` varchar(50) DEFAULT NULL COMMENT '链接类型',
  `link_id` varchar(50) DEFAULT NULL COMMENT '链接ID',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '是否已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';
```



## 5. 安全设计（增强版）

### 5.1 认证安全

#### 5.1.1 JWT Token设计（增强）
- **算法**：使用 RS256 非对称加密算法（更安全）
- **Token有效期**：2小时（短期Token）
- **Refresh Token**：7天（用于刷新Access Token）
- **Token内容**：
  ```json
  {
    "customerId": "客户ID",
    "projectId": "项目ID",
    "phone": "手机号（加密）",
    "deviceId": "设备唯一标识",
    "exp": "过期时间",
    "iat": "签发时间",
    "jti": "Token唯一标识"
  }
  ```
- **Token黑名单**：Redis存储，用于强制退出和防止Token重放
- **设备绑定**：Token与设备ID绑定，防止Token被盗用

#### 5.1.2 登录安全（增强）
- **手机号验证码登录**：
  - 验证码6位数字，有效期5分钟
  - 同一手机号1分钟内只能发送1次
  - 验证码错误5次后锁定30分钟
  - 验证码使用后立即失效
- **项目编号验证**：
  - 验证项目编号与手机号的绑定关系
  - 记录登录失败次数，5次失败后锁定1小时
- **微信登录**：
  - 获取微信 openId 并绑定客户账号
  - 首次登录需要验证项目编号
  - 后续登录直接通过 openId 识别
- **设备指纹**：
  - 收集设备信息（型号、系统版本、网络类型）
  - 异常设备登录需要额外验证
- **登录日志**：
  - 记录每次登录的时间、IP、设备信息
  - 异常登录（异地、新设备）发送通知

#### 5.1.3 会话管理
- **单设备登录**：同一账号只允许一个设备在线（可配置）
- **自动续期**：Token过期前10分钟自动刷新
- **强制退出**：管理员可强制客户退出登录

### 5.2 数据安全（增强）

#### 5.2.1 数据隔离（多层防护）
- **拦截器层**：自动注入项目ID过滤条件
- **Service层**：二次验证项目ID归属
- **数据库层**：使用视图限制数据访问范围
- **审计日志**：记录所有数据访问操作

#### 5.2.2 敏感信息保护（增强）
- **手机号**：脱敏显示（138****8000），数据库加密存储
- **身份证号**：脱敏显示，数据库加密存储
- **地址信息**：部分脱敏（深圳市南山区***）
- **支付信息**：HTTPS传输 + AES加密
- **图片水印**：设计图和施工照片自动添加水印（项目编号+日期）

#### 5.2.3 数据权限控制
- **只读权限**：客户只能查看，不能修改核心数据
- **操作权限**：
  - 可提交反馈、问题报告
  - 不可修改预算、排期、质检结果
  - 不可删除任何数据
- **数据范围**：
  - 只能查看自己项目的数据
  - 不能查看其他客户的信息
  - 不能查看内部成本数据

### 5.3 接口安全（增强）

#### 5.3.1 请求签名（全面防护）
- **签名算法**：HMAC-SHA256
- **签名参数**：timestamp + nonce + body + appSecret
- **签名验证**：
  - 时间戳验证（5分钟内有效）
  - nonce防重放（Redis存储5分钟）
  - 签名匹配验证
- **关键接口**：反馈提交、文件上传、数据修改

#### 5.3.2 频率限制（细粒度控制）
- **登录接口**：
  - 同一手机号：5次/小时
  - 同一IP：20次/小时
  - 同一设备：10次/小时
- **验证码接口**：
  - 同一手机号：1次/分钟，10次/小时
  - 同一IP：30次/小时
- **上传接口**：
  - 10次/分钟，100次/天
  - 单次最多上传9张图片
- **反馈接口**：
  - 20次/小时，100次/天
- **查询接口**：
  - 100次/分钟（防止爬虫）

#### 5.3.3 接口加密
- **传输加密**：强制使用HTTPS（TLS 1.2+）
- **敏感数据加密**：手机号、身份证等字段AES加密传输
- **响应加密**：关键数据（预算金额）可选加密返回

#### 5.3.4 防护措施
- **SQL注入防护**：使用参数化查询，禁止拼接SQL
- **XSS防护**：输入内容HTML转义
- **CSRF防护**：Token验证
- **接口鉴权**：所有接口（除登录外）必须携带有效Token

### 5.4 文件安全（增强）

#### 5.4.1 上传限制（严格控制）
- **文件类型白名单**：
  - 图片：jpg、jpeg、png（禁止gif、svg）
  - 文档：pdf（禁止exe、zip等）
- **文件大小限制**：
  - 图片：单张5MB，总计50MB/天
  - 文档：单个10MB，总计30MB/天
- **文件内容检测**：
  - 图片：检测真实格式（防止伪装）
  - 病毒扫描：集成ClamAV扫描
  - 敏感内容检测：OCR识别违规内容
- **文件名处理**：
  - UUID重命名，防止路径遍历
  - 移除特殊字符
  - 限制文件名长度

#### 5.4.2 访问控制（多重验证）
- **URL签名**：
  - 签名参数：fileId + timestamp + token + secret
  - 签名有效期：1小时（可配置）
  - 一次性签名：使用后失效（可选）
- **访问权限**：
  - 验证Token有效性
  - 验证文件归属项目
  - 验证客户权限
- **防盗链**：
  - Referer验证
  - IP白名单（可选）
  - 访问日志记录
- **CDN防护**：
  - 使用CDN加速
  - 配置防盗链规则
  - 限制并发连接数

#### 5.4.3 存储安全
- **文件隔离**：按项目ID分目录存储
- **备份策略**：定期备份到OSS
- **定期清理**：临时文件7天后自动删除

### 5.5 业务安全（新增）

#### 5.5.1 操作审计
- **审计日志表**：记录所有关键操作
  ```sql
  CREATE TABLE `app_audit_logs` (
    `id` varchar(50) NOT NULL,
    `customer_id` varchar(50) NOT NULL,
    `project_id` varchar(50) NOT NULL,
    `action` varchar(100) NOT NULL COMMENT '操作类型',
    `resource_type` varchar(50) COMMENT '资源类型',
    `resource_id` varchar(50) COMMENT '资源ID',
    `ip_address` varchar(50) COMMENT 'IP地址',
    `device_info` text COMMENT '设备信息',
    `request_data` text COMMENT '请求数据',
    `response_code` int COMMENT '响应码',
    `create_time` datetime NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_create_time` (`create_time`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户端审计日志表';
  ```
- **记录内容**：
  - 登录/退出
  - 查看敏感数据（预算、合同）
  - 提交反馈/问题
  - 文件上传/下载
  - 异常操作（失败的请求）

#### 5.5.2 异常监控
- **异常行为检测**：
  - 短时间大量请求
  - 频繁登录失败
  - 异常IP访问
  - 非工作时间大量操作
- **告警机制**：
  - 实时告警（钉钉/企业微信）
  - 自动封禁（严重违规）
  - 人工审核（可疑行为）

#### 5.5.3 数据脱敏规则
- **自动脱敏**：API响应自动脱敏
- **脱敏规则**：
  - 手机号：保留前3后4位
  - 身份证：保留前6后4位
  - 姓名：保留姓氏
  - 地址：保留到区级
  - 金额：客户可见，但不可导出

### 5.6 配置安全（新增）

#### 5.6.1 小程序配置管理
- **独立配置表**：使用系统字典管理小程序配置
  ```sql
  -- 在现有 sys_dict_type 表中添加小程序配置类型
  INSERT INTO sys_dict_type VALUES 
  ('app_config', '小程序配置', '0', 'admin', NOW(), '', NULL, '小程序相关配置参数');
  
  -- 在 sys_dict_data 表中添加具体配置项
  INSERT INTO sys_dict_data VALUES 
  (NULL, 1, 'Token有效期（小时）', '2', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, 'Token有效期配置'),
  (NULL, 2, 'RefreshToken有效期（天）', '7', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, 'RefreshToken有效期'),
  (NULL, 3, '登录失败锁定次数', '5', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '登录失败锁定阈值'),
  (NULL, 4, '登录失败锁定时长（分钟）', '60', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '登录失败锁定时长'),
  (NULL, 5, '验证码有效期（分钟）', '5', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '短信验证码有效期'),
  (NULL, 6, '文件签名有效期（小时）', '1', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '文件访问签名有效期'),
  (NULL, 7, '单次上传图片数量', '9', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '单次最多上传图片数'),
  (NULL, 8, '图片大小限制（MB）', '5', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '单张图片大小限制'),
  (NULL, 9, '是否启用设备绑定', 'true', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '是否启用设备绑定'),
  (NULL, 10, '是否启用单设备登录', 'true', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '是否限制单设备登录'),
  (NULL, 11, '是否启用图片水印', 'true', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '是否为图片添加水印'),
  (NULL, 12, '接口请求频率限制（次/分钟）', '100', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '接口请求频率限制');
  ```

- **配置读取工具类**：
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
      
      // 是否启用设备绑定
      public boolean isDeviceBindingEnabled() {
          return getBooleanValue("是否启用设备绑定", true);
      }
      
      // 通用获取方法
      private int getIntValue(String label, int defaultValue) {
          String value = dictDataService.selectDictLabel(DICT_TYPE, label);
          return value != null ? Integer.parseInt(value) : defaultValue;
      }
      
      private boolean getBooleanValue(String label, boolean defaultValue) {
          String value = dictDataService.selectDictLabel(DICT_TYPE, label);
          return value != null ? Boolean.parseBoolean(value) : defaultValue;
      }
  }
  ```

#### 5.6.2 配置热更新
- **配置缓存**：Redis缓存配置，5分钟过期
- **配置变更通知**：配置修改后自动刷新缓存
- **配置版本控制**：记录配置变更历史

#### 5.6.3 敏感配置保护
- **加密存储**：JWT密钥、签名密钥等加密存储
- **权限控制**：只有超级管理员可修改安全配置
- **变更审计**：记录所有配置变更操作



## 6. 性能优化

### 6.1 前端性能优化

#### 6.1.1 图片优化
- 图片懒加载
- 缩略图预览，点击查看原图
- 图片压缩上传（质量80%）
- 使用CDN加速

#### 6.1.2 数据缓存
- 项目信息缓存（5分钟）
- 列表数据缓存（3分钟）
- 使用 Pinia 状态管理
- 本地存储策略

#### 6.1.3 列表优化
- 分页加载（每页20条）
- 虚拟滚动（长列表）
- 下拉刷新/上拉加载
- 骨架屏加载

#### 6.1.4 请求优化
- 请求防抖（搜索）
- 请求节流（滚动加载）
- 并发请求控制
- 请求取消机制

### 6.2 后端性能优化

#### 6.2.1 数据库优化
- 合理使用索引
- 避免N+1查询
- 使用连接查询代替多次查询
- 分页查询优化

#### 6.2.2 缓存策略
- Redis缓存热点数据
- 项目信息缓存（10分钟）
- 统计数据缓存（5分钟）
- 缓存预热机制

#### 6.2.3 接口优化
- 响应数据精简（只返回必要字段）
- 批量查询接口
- 异步处理耗时操作
- 接口响应时间监控



## 7. 开发计划

### 7.1 第一阶段：基础设施搭建（5-7天）

#### 后端开发
- [ ] 创建 `com.ruoyi.app` 包结构
- [ ] 配置小程序字典数据（app_config）
- [ ] 实现配置读取工具类（AppConfigUtil）
- [ ] 实现 JWT Token 工具类（RS256算法）
- [ ] 实现 Token 管理服务（生成、验证、刷新、撤销）
- [ ] 实现短信验证码服务（发送、验证）
- [ ] 实现 AppAuthInterceptor 拦截器（Token验证、设备绑定）
- [ ] 实现请求签名验证拦截器
- [ ] 实现频率限制拦截器（基于Redis）
- [ ] 配置 Spring Security（放行 /app/* 路径）
- [ ] 创建 AppAuthController（验证码、登录、刷新、退出）
- [ ] 创建数据库新增表（10张表）
- [ ] 编写 DTO 基础类
- [ ] 实现审计日志切面（AOP）
- [ ] 实现数据脱敏工具类

#### 前端开发
- [ ] 完善 request.js（Token管理、自动刷新、错误处理）
- [ ] 实现设备ID生成和存储
- [ ] 实现请求签名工具
- [ ] 创建 API 配置文件（支持环境切换）
- [ ] 完善用户状态管理（stores/user.js）
- [ ] 完善项目状态管理（stores/project.js）
- [ ] 实现验证码登录功能
- [ ] 实现微信登录功能
- [ ] 实现Token自动刷新机制
- [ ] 实现登录状态持久化

### 7.2 第二阶段：核心功能开发（7-10天）

#### 后端开发
- [ ] AppProjectController（项目信息、统计）
- [ ] AppDesignController（设计方案、反馈）
- [ ] AppConstructController（施工日志）
- [ ] AppScheduleController（施工排期）
- [ ] 文件上传接口

#### 前端开发
- [ ] 首页数据展示（dashboard）
- [ ] 设计方案列表和详情
- [ ] 施工日志列表和详情
- [ ] 施工排期展示
- [ ] 图片预览组件
- [ ] 图片上传组件

### 7.3 第三阶段：扩展功能开发（5-7天）

#### 后端开发
- [ ] AppBudgetController（预算管理）
- [ ] AppMaterialController（物料清单）
- [ ] AppQualityController（质检管理）
- [ ] 消息通知接口

#### 前端开发
- [ ] 预算管理页面
- [ ] 物料清单页面
- [ ] 质检管理页面
- [ ] 个人中心完善
- [ ] 消息通知功能

### 7.4 第四阶段：优化与测试（3-5天）

- [ ] 性能优化（缓存、图片、列表）
- [ ] 用户体验优化（加载状态、空状态、错误提示）
- [ ] 接口联调测试
- [ ] 功能测试
- [ ] 兼容性测试
- [ ] 安全测试
- [ ] 压力测试

### 7.5 第五阶段：部署上线（2-3天）

- [ ] 生产环境配置
- [ ] 数据库迁移
- [ ] 后端服务部署
- [ ] 小程序提审发布
- [ ] 监控告警配置
- [ ] 用户培训文档

**总计：20-30天**



## 8. 技术栈总结

### 8.1 前端技术栈
- **框架**：uni-app（Vue 3 + Composition API）
- **状态管理**：Pinia
- **UI组件库**：uview-plus
- **HTTP请求**：uni.request 封装
- **构建工具**：HBuilderX / Vite

### 8.2 后端技术栈
- **框架**：Spring Boot 3.x
- **安全**：Spring Security + JWT
- **ORM**：MyBatis
- **数据库**：MySQL 8.0
- **缓存**：Redis（可选）
- **文件存储**：本地存储 / OSS（可选）

### 8.3 开发工具
- **前端IDE**：HBuilderX / VS Code
- **后端IDE**：IntelliJ IDEA
- **API测试**：Postman / Apifox
- **版本控制**：Git
- **项目管理**：禅道 / Jira（可选）

## 9. 风险评估与应对

### 9.1 安全风险（高优先级）

#### 9.1.1 Token被盗用
- **风险描述**：Token被截获后可能被恶意使用
- **风险等级**：高
- **应对措施**：
  - 使用RS256非对称加密
  - Token与设备ID绑定
  - 短期Token + Refresh Token机制
  - 异常登录检测和通知
  - Token黑名单机制

#### 9.1.2 数据泄露
- **风险描述**：客户敏感数据被非法访问
- **风险等级**：高
- **应对措施**：
  - 严格的数据隔离（项目级）
  - 敏感信息加密存储
  - 数据脱敏显示
  - 完整的审计日志
  - 定期安全审计

#### 9.1.3 接口被恶意调用
- **风险描述**：接口被爬虫或恶意程序频繁调用
- **风险等级**：中
- **应对措施**：
  - 请求签名验证
  - 频率限制（多维度）
  - IP黑名单
  - 异常行为检测
  - 验证码保护

### 9.2 性能风险

#### 9.2.1 图片加载慢
- **风险描述**：大量高清图片影响用户体验
- **风险等级**：中
- **应对措施**：
  - 缩略图预览
  - CDN加速
  - 图片懒加载
  - 图片压缩
  - 渐进式加载

#### 9.2.2 并发访问压力
- **风险描述**：高峰期大量用户同时访问
- **风险等级**：中
- **应对措施**：
  - Redis缓存热点数据
  - 数据库连接池优化
  - 接口响应优化
  - 负载均衡（必要时）
  - 降级策略

### 9.3 兼容性风险

#### 9.3.1 小程序平台差异
- **风险描述**：微信、支付宝等平台API差异
- **风险等级**：低
- **应对措施**：
  - 使用uni-app条件编译
  - 充分的兼容性测试
  - 平台特性检测
  - 降级方案

#### 9.3.2 设备兼容性
- **风险描述**：不同设备显示效果差异
- **风险等级**：低
- **应对措施**：
  - 响应式设计
  - 多设备测试
  - 兼容性适配

### 9.4 业务风险

#### 9.4.1 配置错误
- **风险描述**：安全配置错误导致系统漏洞
- **风险等级**：高
- **应对措施**：
  - 配置字典化管理
  - 配置变更审计
  - 配置热更新
  - 默认安全配置
  - 配置验证机制

#### 9.4.2 用户误操作
- **风险描述**：用户误删除或误提交数据
- **风险等级**：低
- **应对措施**：
  - 二次确认机制
  - 操作可撤销（部分）
  - 友好的提示信息
  - 操作日志记录

## 10. 后续扩展

### 10.1 功能扩展
- 在线支付功能
- 视频直播（施工现场）
- 3D设计方案展示
- AI智能客服
- 电子合同签署

### 10.2 技术扩展
- 微服务架构改造
- 消息队列（异步处理）
- 大数据分析（用户行为）
- 推荐系统（材料推荐）

## 11. 总结

### 11.1 架构优势

本设计方案采用**在 evs-home 模块内扩展，使用包分层架构**的方式，具有以下优势：

1. **开发效率高**：复用现有代码，减少重复开发
2. **维护成本低**：统一代码库，便于维护
3. **部署简单**：单一服务，无需额外配置
4. **扩展性好**：清晰的包结构，便于后续扩展
5. **数据一致性**：共享数据库和事务管理

### 11.2 安全特性（增强）

针对小程序直接面向客户使用的特点，本方案特别加强了安全设计：

1. **多层认证防护**：
   - 短信验证码 + 项目编号双重验证
   - RS256非对称加密Token
   - Token与设备绑定
   - 短期Token + Refresh Token机制

2. **严格的数据隔离**：
   - 拦截器自动注入项目ID
   - Service层二次验证
   - 只读权限控制
   - 数据脱敏显示

3. **完善的安全机制**：
   - 请求签名验证
   - 多维度频率限制
   - 文件安全检测
   - 完整的审计日志
   - 异常行为监控

4. **配置化管理**：
   - 使用系统字典管理小程序配置
   - 支持配置热更新
   - 配置变更审计
   - 敏感配置加密

### 11.3 核心设计要点

- **前后端分离**：RESTful API设计，清晰的接口规范
- **安全优先**：多层安全防护，保护客户数据
- **配置灵活**：字典化配置管理，便于调整
- **性能优化**：缓存策略、图片优化、接口优化
- **用户体验**：友好的错误提示、流畅的交互
- **可维护性**：完整的日志、清晰的代码结构

### 11.4 实施建议

1. **分阶段实施**：按照开发计划分5个阶段，先完成基础设施和核心功能
2. **安全优先**：第一阶段重点完成安全机制，确保系统安全可靠
3. **充分测试**：每个阶段完成后进行充分测试，包括功能测试、安全测试、性能测试
4. **持续优化**：上线后持续监控，根据用户反馈和数据分析持续优化
5. **文档完善**：编写完整的接口文档、部署文档、运维文档

### 11.5 双用户体系对比总结

| 对比项 | 运营端（vue3） | 客户端（uni3） |
|--------|---------------|---------------|
| **用户群体** | 内部运营人员 | 外部客户 |
| **用户表** | `sys_user` | `customers` |
| **用户标识** | `user_id` (bigint) | `id` (varchar) |
| **认证方式** | 用户名+密码 | 手机号+验证码 / 微信登录 |
| **Token类型** | Session Token | JWT (RS256) |
| **Token标识** | `userType: "staff"` | `userType: "customer"` |
| **权限模型** | RBAC（角色权限） | 项目绑定 |
| **权限控制** | 基于角色和菜单权限 | 基于项目ID数据隔离 |
| **数据访问范围** | 根据角色可访问多个项目 | 仅能访问绑定的项目 |
| **操作权限** | 完整的增删改查 | 只读为主，部分提交 |
| **安全级别** | 标准（内网） | 增强（多层防护） |
| **接口路径** | `/evs/*` | `/app/*` |
| **拦截器** | Spring Security | AppAuthInterceptor |
| **配置管理** | 系统配置表 | 独立字典配置 |
| **审计日志** | `sys_oper_log` | `app_audit_logs` |
| **登录日志** | `sys_logininfor` | `app_login_logs` |
| **Token管理** | Session管理 | `app_tokens` 表 |
| **设备绑定** | 无 | 支持（可配置） |
| **频率限制** | 无 | 多维度限制 |
| **数据脱敏** | 无 | 自动脱敏 |

### 11.6 双用户体系架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                          前端层                                   │
├──────────────────────────┬──────────────────────────────────────┤
│   vue3 运营后台           │        uni3 客户小程序                │
│   - 内部运营人员使用       │        - 外部客户使用                 │
│   - 完整管理功能          │        - 查看和反馈功能               │
└──────────────────────────┴──────────────────────────────────────┘
              ↓                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       接口层（隔离）                              │
├──────────────────────────┬──────────────────────────────────────┤
│   /evs/* 运营端接口       │        /app/* 客户端接口              │
│   - Session认证           │        - JWT Token认证                │
│   - RBAC权限控制          │        - 项目绑定验证                 │
└──────────────────────────┴──────────────────────────────────────┘
              ↓                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    认证层（不同机制）                             │
├──────────────────────────┬──────────────────────────────────────┤
│   Spring Security         │        AppAuthInterceptor            │
│   - 用户名密码认证         │        - 验证码认证                   │
│   - Session管理           │        - Token验证                    │
│   - 角色权限验证          │        - 设备绑定验证                 │
└──────────────────────────┴──────────────────────────────────────┘
              ↓                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    业务层（共享复用）                             │
│   - IProjectsService                                            │
│   - ICustomersService                                           │
│   - IProjectBudgetsService                                      │
│   - ...（其他业务服务）                                          │
└─────────────────────────────────────────────────────────────────┘
              ↓                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    数据层（表隔离）                               │
├──────────────────────────┬──────────────────────────────────────┤
│   sys_user (运营人员表)    │        customers (客户表)            │
│   - user_id (主键)        │        - id (主键)                   │
│   - username              │        - phone (唯一)                │
│   - password              │        - name                        │
│   - dept_id               │        - level                       │
│   - 关联角色权限表         │        - 关联项目表                   │
└──────────────────────────┴──────────────────────────────────────┘
              ↓                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    共享数据表                                     │
│   - projects (项目表，包含customer_id字段)                        │
│   - project_budgets (预算表)                                     │
│   - project_schedules (排期表)                                   │
│   - construction_logs (施工日志表)                                │
│   - ...（其他业务表）                                             │
└─────────────────────────────────────────────────────────────────┘
```

### 11.7 关键设计要点

**1. 表隔离设计：**
- 运营人员和客户使用不同的用户表
- 通过 `projects.customer_id` 关联客户
- 避免了用户类型字段和复杂的联合查询

**2. 认证隔离设计：**
- 不同的认证机制和Token体系
- Token中包含 `userType` 字段区分用户类型
- 拦截器根据路径和用户类型执行不同逻辑

**3. 权限隔离设计：**
- 运营端：基于角色的权限控制（RBAC）
- 客户端：基于项目的数据隔离
- 互不干扰，各自独立

**4. 代码复用设计：**
- Service层和Mapper层共享
- 通过不同的Controller层调用
- 减少重复代码，便于维护

这种设计既保证了代码复用和维护便利，又确保了客户端的安全性和独立性，同时清晰地区分了两种用户类型的职责和权限。

