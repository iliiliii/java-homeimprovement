# Projects 表 customer_id 字段深度分析报告

## 一、数据库层面分析

### 1.1 表结构定义
```sql
-- projects 表中的 customer_id 字段定义
`customer_id` VARCHAR(32) COMMENT '客户ID'

-- 索引定义
KEY `idx_customer_id` (`customer_id`)
```

**字段特性**:
- 数据类型: VARCHAR(32) - 支持 UUID 格式的客户ID
- 可为空: 允许 NULL（项目可以不关联客户，但实际业务中通常必填）
- 索引: 建立了普通索引 `idx_customer_id`，优化基于客户ID的查询性能
- 外键关系: 逻辑外键，关联 customers 表的 id 字段（无物理外键约束）

### 1.2 数据库查询模式

#### 关联查询（LEFT JOIN）
```sql
-- 项目关联客户信息
LEFT JOIN customers c ON p.customer_id = c.id

-- 客户统计项目数量
LEFT JOIN projects p ON c.id = p.customer_id AND p.deleted_at IS NULL
```

#### 条件过滤
```sql
-- 按客户ID精确查询
WHERE p.customer_id = #{customerId}

-- 按客户ID筛选（可选条件）
<if test="customerId != null and customerId != ''">
    AND p.customer_id = #{customerId}
</if>
```

## 二、后端业务层分析

### 2.1 实体类映射

**Projects.java**
```java
/** 客户ID */
private String customerId;

public void setCustomerId(String customerId) {
    this.customerId = customerId;
}

public String getCustomerId() {
    return customerId;
}
```

**关联对象**:
```java
/** 关联的客户信息 */
private Customers customer;
```

### 2.2 核心业务场景

#### 场景1: 用户登录 - 项目列表查询
**位置**: `AppAuthServiceImpl.java:770`

```java
// 客户登录时，根据 customer_id 查询其所有项目
if (userType == UserTypeEnum.CUSTOMER) {
    projects = appProjectMapper.selectProjectsByCustomerId(userId);
}
```

**SQL实现**:
```sql
SELECT id, project_type, name, customer_id, description, address, area, budget, 
       start_date, end_date, status, priority, progress
FROM projects 
WHERE customer_id = #{customerId} AND deleted_at IS NULL
ORDER BY created_at DESC
```

**业务意义**: 
- 客户登录后立即获取其名下所有项目
- 用于生成 Token 中的项目ID列表
- 决定客户可访问的数据范围

#### 场景2: 权限验证 - 客户访问控制
**位置**: 
- `AppQualityIssueServiceImpl.java:238`
- `AppProjectScheduleServiceImpl.java:269`
- `AppDashboardServiceImpl.java:463`

```java
// 验证客户是否有权访问该项目
if ("customer".equals(userType)) {
    Projects project = projectMapper.selectProjectById(projectId);
    if (project == null || !userId.equals(project.getCustomerId())) {
        throw new ServiceException("无权访问该项目");
    }
}
```

**权限验证流程**:
1. 获取项目信息
2. 比对 `project.customerId` 与当前用户ID
3. 不匹配则抛出权限异常

**应用范围**:
- 质检问题查询
- 项目进度查询
- 项目仪表盘访问
- 项目详情查看

#### 场景3: 数据关联 - 项目与客户信息联查
**位置**: `ProjectsMapper.xml`

```xml
<sql id="selectProjectsWithCustomerVo">
    SELECT
        p.id, p.project_type, p.name, p.customer_id, p.description, p.address,
        p.area, p.budget, p.actual_cost, p.start_date, p.end_date,
        p.actual_end_date, p.status, p.priority, p.progress,
        p.budgets_url, p.contracts_url, p.created_at, p.updated_at,
        p.deleted_at, p.created_by, p.updated_by,
        c.name as customer_name, c.phone as customer_phone, 
        c.email as customer_email, c.address as customer_address, 
        c.level as customer_level, c.source as customer_source, 
        c.is_active as customer_is_active
    FROM projects p
    LEFT JOIN customers c ON p.customer_id = c.id
</sql>
```

**ResultMap 映射**:
```xml
<resultMap type="Projects" id="ProjectsWithCustomerResult" extends="ProjectsResult">
    <association property="customer" javaType="Customers">
        <id property="id" column="customer_id"/>
        <result property="name" column="customer_name"/>
        <result property="phone" column="customer_phone"/>
        <!-- 更多客户字段 -->
    </association>
</resultMap>
```

#### 场景4: 统计分析 - 客户项目数量
**位置**: `CustomersMapper.xml`

```sql
SELECT
    c.id, c.name, c.phone, c.email, c.address, c.level, c.source,
    c.remarks, c.avatar, c.is_active, c.created_at, c.updated_at,
    c.deleted_at, c.created_by, c.updated_by,
    COUNT(p.id) as project_count
FROM customers c
LEFT JOIN projects p ON c.id = p.customer_id AND p.deleted_at IS NULL
GROUP BY c.id
```

**业务用途**:
- 客户列表显示项目数量
- 客户详情页展示项目统计
- 客户活跃度分析

#### 场景5: 数据筛选 - 按客户过滤项目
**位置**: `ProjectsMapper.xml`

```xml
<!-- 支持按客户ID筛选 -->
<if test="customerId != null and customerId != ''">
    AND p.customer_id = #{customerId}
</if>

<!-- 支持按客户姓名模糊搜索 -->
<if test="customerName != null and customerName != ''">
    AND c.name like concat('%', #{customerName}, '%')
</if>

<!-- 支持按客户电话模糊搜索 -->
<if test="customerPhone != null and customerPhone != ''">
    AND c.phone like concat('%', #{customerPhone}, '%')
</if>
```

### 2.3 Service 层处理

**ProjectsServiceImpl.java**
- `insertProjects()`: 创建项目时可设置 customerId
- `updateProjects()`: 修改项目时可更新 customerId
- `selectProjectsWithRelations()`: 支持关联查询客户信息

**CustomersServiceImpl.java**
- `selectCustomersWithRelations()`: 查询客户时可包含项目数量统计

## 三、前端应用层分析

### 3.1 Vue3 管理后台使用

#### 项目列表页面 (`vue3/src/views/evs/projects/index.vue`)
```javascript
// 查询参数包含 customerId
queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    customerId: null,  // 按客户筛选
    address: null,
    status: null,
}

// 表单验证规则
customerId: [
    { required: true, message: "请选择客户", trigger: "change" }
]
```

#### 项目编辑组件 (`vue3/src/views/evs/projects/components/ProjectEdit.vue`)
```vue
<el-form-item label="客户" prop="customerId">
    <el-select v-model="form.customerId" placeholder="请选择客户" clearable>
        <el-option
            v-for="customer in customerList"
            :key="customer.id"
            :label="customer.name"
            :value="customer.id"
        />
    </el-select>
</el-form-item>
```

#### 项目卡片展示 (`vue3/src/views/evs/projects/card.vue`)
```javascript
// 显示客户信息
<span>客户：</span>
<el-link
    v-if="project.customerId && getCustomerName(project) !== '未关联客户'"
    type="primary"
    @click="showCustomerInfo(project.customerId)"
>
    {{ getCustomerName(project) }}
</el-link>

// 获取客户名称
function getCustomerName(project) {
    if (project.customer && project.customer.name) {
        return project.customer.name
    }
    if (project.customerId) {
        return `客户ID: ${project.customerId}`
    }
    return '未关联客户'
}
```

#### 客户管理页面 (`vue3/src/views/evs/customers/card.vue`)
```javascript
// 获取客户的微信绑定信息
function getWechatBinding(customerId) {
    return wechatBindingsMap.value[customerId] || null
}
```

#### 首页统计 (`vue3/src/views/index.vue`)
```javascript
// 计算活跃客户（有进行中项目的客户）
const activeCustomerIds = new Set(
    projects.filter(p => p.status === 'in_progress')
           .map(p => p.customerId)
)
stats.value.activeCustomerCount = activeCustomerIds.size

// 为每个客户计算项目统计
const customerStats = customers.map(customer => {
    const customerProjects = projects.filter(
        p => p.customerId === customer.id
    )
    return {
        ...customer,
        projectCount: customerProjects.length
    }
})
```

### 3.2 前端业务逻辑

1. **项目创建/编辑**: customerId 为必填项，通过下拉选择客户
2. **项目列表筛选**: 支持按 customerId 过滤项目
3. **客户信息展示**: 点击客户名称可查看客户详情
4. **统计分析**: 基于 customerId 统计活跃客户、项目分布等

## 四、数据流转分析

### 4.1 完整数据流

```
1. 项目创建
   前端表单 → customerId 字段 → POST /evs/projects
   → ProjectsController.add()
   → ProjectsServiceImpl.insertProjects()
   → ProjectsMapper.insertProjects()
   → 数据库 projects 表

2. 客户登录查询项目
   客户登录 → AppAuthServiceImpl.wechatLogin()
   → findUserProjects(UserTypeEnum.CUSTOMER, userId)
   → appProjectMapper.selectProjectsByCustomerId(userId)
   → WHERE customer_id = #{customerId}
   → 返回项目列表 → 生成 Token

3. 权限验证
   API 请求 → Token 解析 → 获取 userType 和 userId
   → 查询项目: projectMapper.selectProjectById(projectId)
   → 验证: userId.equals(project.getCustomerId())
   → 通过/拒绝访问

4. 关联查询
   前端请求 → includeCustomer=true
   → ProjectsServiceImpl.selectProjectsWithRelations()
   → ProjectsMapper.selectProjectsWithCustomer()
   → LEFT JOIN customers c ON p.customer_id = c.id
   → 返回项目+客户信息
```

### 4.2 权限控制模型

```
用户类型判断:
├─ CUSTOMER (客户)
│  └─ 权限范围: customer_id = 当前用户ID 的项目
│     └─ 查询: WHERE customer_id = #{userId}
│     └─ 验证: project.customerId.equals(userId)
│
└─ STAFF (员工)
   └─ 权限范围: project_members 表中关联的项目
      └─ 查询: WHERE project_id IN (SELECT project_id FROM project_members WHERE user_id = #{userId})
      └─ 验证: dashboardMapper.checkStaffProjectAccess(userId, projectId)
```

## 五、业务意义总结

### 5.1 核心作用

1. **业务归属关系**
   - 明确项目所属客户
   - 建立客户与项目的一对多关系
   - 支持客户维度的数据管理

2. **权限控制基础**
   - 客户只能访问自己的项目（customer_id = userId）
   - 实现数据隔离和安全访问
   - 防止越权访问其他客户数据

3. **数据关联枢纽**
   - 连接 projects 和 customers 两张核心表
   - 支持双向关联查询
   - 便于统计分析和报表生成

4. **业务流程支撑**
   - 客户登录后自动加载其项目列表
   - 项目详情展示客户信息
   - 客户管理页面显示项目统计

5. **数据完整性保障**
   - 通过索引优化查询性能
   - 支持软删除（deleted_at）的关联过滤
   - 保证业务数据的可追溯性

### 5.2 与其他字段的关系

- **与 project_members 的区别**:
  - `customer_id`: 项目所有者（客户）
  - `project_members`: 项目参与者（员工团队）
  
- **权限模型差异**:
  - 客户: 通过 customer_id 直接关联
  - 员工: 通过 project_members 表间接关联

### 5.3 性能优化

1. **索引优化**: `idx_customer_id` 索引加速客户项目查询
2. **查询优化**: 使用 LEFT JOIN 减少多次查询
3. **缓存策略**: Token 中包含项目ID列表，减少重复查询

## 六、潜在问题与建议

### 6.1 当前设计的优点
- 简单清晰的一对多关系
- 高效的权限验证机制
- 灵活的关联查询支持

### 6.2 可能的改进方向
1. 考虑添加物理外键约束（如果数据一致性要求高）
2. 对于大量项目的客户，可考虑分页优化
3. 增加 customer_id 变更的审计日志

### 6.3 注意事项
- customer_id 允许为 NULL，但业务上通常必填
- 删除客户前需检查关联项目
- 修改 customer_id 需要严格的权限控制

---

**分析完成时间**: 2026-03-01
**分析范围**: sb3/evs-home 模块
**数据库**: MySQL
**框架**: Spring Boot + MyBatis
