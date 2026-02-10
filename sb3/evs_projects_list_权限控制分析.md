# evs/projects/list API 权限控制分析

## 概述

`/evs/projects/list` API 采用**多层权限控制**机制，从 Controller 层到 Service 层再到 Mapper 层，确保用户只能访问自己有权限的项目数据。

---

## 权限控制架构

### 1. Controller 层 - 接口权限验证

**文件**: `ProjectsController.java`

```java
@PreAuthorize("@ss.hasPermi('evs:projects:list')")
@GetMapping("/list")
public TableDataInfo list(Projects projects, ...) {
    // ...
}
```

**权限点**:
- 使用 Spring Security 的 `@PreAuthorize` 注解
- 要求用户必须拥有 `evs:projects:list` 权限
- 如果用户没有该权限，请求会被直接拒绝（返回 403）

---

### 2. Service 层 - 用户身份识别

**文件**: `ProjectsServiceImpl.java`

#### 2.1 核心方法：`setCurrentUser()`

```java
private Projects setCurrentUser(Projects projects) {
    Long currentUserId = SecurityUtils.getUserId();
    Boolean isAdmin = SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("gly");
    
    if (currentUserId != null) {
        projects.setCurrentUserId(String.valueOf(currentUserId));
        projects.setIsAdmin(isAdmin != null ? isAdmin : false);
    } else {
        projects.setIsAdmin(false);
    }
    return projects;
}
```

**功能**:
1. 从 Spring Security 上下文获取当前登录用户 ID
2. 判断用户是否为管理员（admin 或 gly 角色）
3. 将用户信息注入到查询对象中，传递给 Mapper 层

#### 2.2 查询方法调用链

```java
@Override
public List<Projects> selectProjectsList(Projects projects) {
    // 自动注入权限信息
    Projects query = setCurrentUser(projects);
    
    // 调用 Mapper 查询（带权限过滤）
    return projectsMapper.selectProjectsList(query);
}
```

**关键点**:
- 所有查询方法都会先调用 `setCurrentUser()` 注入权限信息
- 确保每次查询都携带当前用户的身份标识

---

### 3. Mapper 层 - 数据过滤（核心）

**文件**: `ProjectsMapper.xml`

#### 3.1 权限过滤 SQL

```xml
<sql id="selectProjectsListVo">
    <include refid="selectProjectsVo"/>
    <where>
        <!-- 统一的权限控制逻辑 -->
        <if test="isAdmin != null and isAdmin == true">
            <!-- 管理员查看所有项目 -->
            1=1
        </if>
        <if test="isAdmin == null or isAdmin == false">
            <!-- 非管理员只能查看参与的项目 -->
            <if test="currentUserId != null and currentUserId != ''">
                AND p.id IN (
                    SELECT pm.project_id
                    FROM project_members pm
                    WHERE pm.user_id = #{currentUserId} AND pm.is_active = 1
                )
            </if>
            <if test="currentUserId == null or currentUserId == ''">
                <!-- 未登录用户无法查看任何项目 -->
                1=0
            </if>
        </if>
        <!-- 其他查询条件 -->
        <if test="status != null and status != ''"> AND p.status = #{status}</if>
        <if test="name != null and name != ''"> AND p.name like concat('%', #{name}, '%')</if>
    </where>
    ORDER BY p.created_at DESC
</sql>
```

#### 3.2 权限规则详解

| 用户类型 | 权限规则 | SQL 条件 |
|---------|---------|---------|
| **管理员** (admin/gly) | 可查看所有项目 | `1=1` (无限制) |
| **普通员工** | 只能查看自己参与的项目 | `p.id IN (SELECT project_id FROM project_members WHERE user_id = #{currentUserId} AND is_active = 1)` |
| **未登录用户** | 无法查看任何项目 | `1=0` (永远为假) |

**关键表**: `project_members`
- 存储项目成员关系
- `user_id`: 用户 ID
- `project_id`: 项目 ID
- `is_active`: 成员是否激活（1=激活，0=停用）

---

## 权限判断工具类

**文件**: `SecurityUtils.java`

### 核心方法

```java
// 获取当前登录用户 ID
public static Long getUserId() {
    return getLoginUser().getUserId();
}

// 判断用户是否拥有指定角色
public static boolean hasRole(String role) {
    List<SysRole> roleList = getLoginUser().getUser().getRoles();
    Collection<String> roles = roleList.stream()
        .map(SysRole::getRoleKey)
        .collect(Collectors.toSet());
    return hasRole(roles, role);
}

// 判断用户是否拥有指定权限
public static boolean hasPermi(String permission) {
    return hasPermi(getLoginUser().getPermissions(), permission);
}
```

**数据来源**:
- 从 Spring Security 的 `SecurityContextHolder` 获取当前认证信息
- 用户登录后，用户信息（ID、角色、权限）会存储在 Security Context 中

---

## 完整权限控制流程

```
1. 用户请求 GET /evs/projects/list
   ↓
2. Controller 层：@PreAuthorize 验证权限
   - 检查用户是否有 'evs:projects:list' 权限
   - 无权限 → 返回 403 Forbidden
   ↓
3. Service 层：setCurrentUser() 注入身份信息
   - 获取 currentUserId (用户 ID)
   - 判断 isAdmin (是否管理员)
   - 注入到查询对象
   ↓
4. Mapper 层：SQL 动态过滤
   - 管理员：查询所有项目 (1=1)
   - 普通员工：只查询参与的项目 (子查询 project_members)
   - 未登录：返回空结果 (1=0)
   ↓
5. 返回过滤后的项目列表
```

---

## 权限控制特点

### ✅ 优点

1. **多层防护**: Controller + Service + Mapper 三层权限控制
2. **自动化**: Service 层自动注入权限信息，无需手动传递
3. **数据隔离**: 通过 SQL 子查询实现数据级权限控制
4. **角色分离**: 管理员和普通员工有明确的权限边界
5. **统一管理**: 所有查询方法复用相同的权限逻辑

### 🔒 安全机制

1. **接口级权限**: `@PreAuthorize` 确保只有授权用户能调用接口
2. **数据级权限**: SQL 子查询确保用户只能看到自己的数据
3. **身份验证**: 通过 Spring Security 验证用户身份
4. **角色控制**: 基于角色（admin/gly）区分管理员和普通用户

---

## 相关数据表

### project_members (项目成员表)

| 字段 | 类型 | 说明 |
|-----|------|------|
| id | varchar | 主键 |
| project_id | varchar | 项目 ID |
| user_id | bigint | 用户 ID |
| is_active | tinyint | 是否激活 (1=是, 0=否) |
| role | varchar | 成员角色 |
| created_at | datetime | 创建时间 |

**作用**: 
- 存储用户与项目的关联关系
- 用于权限过滤的核心依据
- 只有 `is_active = 1` 的成员才能访问项目

---

## 示例场景

### 场景 1: 管理员查询

```
用户: admin (角色: admin)
请求: GET /evs/projects/list

权限判断:
- isAdmin = true
- SQL: WHERE 1=1

结果: 返回所有项目
```

### 场景 2: 普通员工查询

```
用户: 张三 (user_id: 100, 角色: employee)
请求: GET /evs/projects/list

权限判断:
- isAdmin = false
- currentUserId = 100
- SQL: WHERE p.id IN (
    SELECT project_id FROM project_members 
    WHERE user_id = 100 AND is_active = 1
  )

结果: 只返回张三参与的项目
```

### 场景 3: 未登录用户

```
用户: 未登录
请求: GET /evs/projects/list

权限判断:
- currentUserId = null
- SQL: WHERE 1=0

结果: 返回空列表
```

---

## 总结

`/evs/projects/list` API 的权限控制采用**基于角色的访问控制（RBAC）+ 数据级权限过滤**的混合模式：

1. **接口权限**: 通过 Spring Security 注解控制接口访问
2. **角色权限**: 区分管理员和普通用户的数据访问范围
3. **数据权限**: 通过 SQL 子查询实现细粒度的数据过滤
4. **自动化**: Service 层自动处理权限信息注入

这种设计确保了数据安全性，同时保持了代码的可维护性和扩展性。
