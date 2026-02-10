# isAdmin 字段控制流程详解

## 概述

`isAdmin` 字段用于判断当前用户是否为管理员，从而决定是否需要进行数据权限过滤。

**核心判断逻辑**：`userId == 1` 的用户被认为是管理员（超级管理员）

---

## 完整流程图

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. 用户登录                                                      │
│    POST /login                                                   │
│    { username: "admin", password: "xxx" }                       │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. 认证成功，Spring Security 存储用户信息                        │
│    SecurityContextHolder.getContext().setAuthentication(...)    │
│                                                                  │
│    存储的用户信息（LoginUser）：                                  │
│    - userId: 1                                                   │
│    - username: "admin"                                           │
│    - roles: [admin]                                              │
│    - permissions: [...]                                          │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. 用户请求 API                                                  │
│    GET /evs/projectSchedules/list                               │
│    Authorization: Bearer <token>                                │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. Controller 层                                                 │
│    ProjectSchedulesController.list()                            │
│    ↓                                                             │
│    调用 Service 层                                               │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 5. Service 层：ProjectSchedulesServiceImpl                       │
│                                                                  │
│    selectProjectSchedulesList(projectSchedules) {               │
│        // 🔑 步骤1：获取当前用户ID                               │
│        Long currentUserId = SecurityUtils.getUserId();          │
│        // → 从 Spring Security 上下文获取                        │
│        // → 返回：1                                              │
│                                                                  │
│        if (currentUserId != null) {                             │
│            // 🔑 步骤2：设置用户ID                               │
│            projectSchedules.setCurrentUserId("1");              │
│                                                                  │
│            // 🔑 步骤3：判断是否为管理员                         │
│            projectSchedules.setIsAdmin(                         │
│                SecurityUtils.isAdmin(currentUserId)             │
│            );                                                    │
│            // → SecurityUtils.isAdmin(1) 返回 true              │
│        }                                                         │
│                                                                  │
│        return projectSchedulesMapper.selectProjectSchedulesList(│
│            projectSchedules                                      │
│        );                                                        │
│    }                                                             │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 6. SecurityUtils.isAdmin(userId) 方法                            │
│                                                                  │
│    public static boolean isAdmin(Long userId) {                 │
│        return userId != null && 1L == userId;                   │
│    }                                                             │
│                                                                  │
│    🔑 核心逻辑：userId == 1 → 管理员                             │
│                                                                  │
│    示例：                                                         │
│    - isAdmin(1)   → true  ✅ 管理员                              │
│    - isAdmin(2)   → false ❌ 普通用户                            │
│    - isAdmin(103) → false ❌ 普通用户                            │
│    - isAdmin(null)→ false ❌ 未登录                              │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 7. Mapper 层：ProjectSchedulesMapper.xml                         │
│                                                                  │
│    <select id="selectProjectSchedulesList">                     │
│        SELECT ps.* FROM project_schedules ps                    │
│        <where>                                                   │
│            <!-- 🔑 使用 isAdmin 字段判断 -->                     │
│            <if test="isAdmin != null and isAdmin == true">      │
│                <!-- 管理员：查看所有数据 -->                      │
│                1=1                                               │
│            </if>                                                 │
│            <if test="isAdmin == null or isAdmin == false">      │
│                <!-- 普通用户：只看参与的项目 -->                  │
│                AND ps.project_id IN (                           │
│                    SELECT pm.project_id                         │
│                    FROM project_members pm                      │
│                    WHERE pm.user_id = #{currentUserId}          │
│                      AND pm.is_active = 1                       │
│                )                                                 │
│            </if>                                                 │
│        </where>                                                  │
│    </select>                                                     │
│                                                                  │
│    当 isAdmin = true 时：                                        │
│    → WHERE 1=1 (无限制，查询所有数据)                            │
│                                                                  │
│    当 isAdmin = false 时：                                       │
│    → WHERE ps.project_id IN (...) (只查询参与的项目)            │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 8. 返回结果                                                      │
│    - 管理员：返回所有项目的进度                                   │
│    - 普通用户：只返回参与项目的进度                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## 关键代码位置

### 1. Service 层设置 isAdmin

**文件**: `ProjectSchedulesServiceImpl.java` (第 123-126 行)

```java
@Override
public List<ProjectSchedules> selectProjectSchedulesList(ProjectSchedules projectSchedules) {
    // 获取当前用户ID
    Long currentUserId = SecurityUtils.getUserId();
    
    if (currentUserId != null) {
        // 设置用户ID
        projectSchedules.setCurrentUserId(String.valueOf(currentUserId));
        
        // 🔑 关键：设置 isAdmin 字段
        projectSchedules.setIsAdmin(SecurityUtils.isAdmin(currentUserId));
        //                          ↑
        //                          调用 SecurityUtils.isAdmin() 判断
    }
    
    return projectSchedulesMapper.selectProjectSchedulesList(projectSchedules);
}
```

---

### 2. SecurityUtils.isAdmin() 判断逻辑

**文件**: `SecurityUtils.java` (第 120-123 行)

```java
/**
 * 是否为管理员
 * 
 * @param userId 用户ID
 * @return 结果
 */
public static boolean isAdmin(Long userId) {
    // 🔑 核心判断：userId == 1 就是管理员
    return userId != null && 1L == userId;
}
```

**判断规则**：
- `userId == 1` → 返回 `true` (管理员)
- `userId != 1` → 返回 `false` (普通用户)
- `userId == null` → 返回 `false` (未登录)

---

### 3. SecurityUtils.getUserId() 获取用户ID

**文件**: `SecurityUtils.java` (第 26-35 行)

```java
/**
 * 用户ID
 **/
public static Long getUserId() {
    try {
        // 从 Spring Security 上下文获取登录用户信息
        return getLoginUser().getUserId();
    } catch (Exception e) {
        throw new ServiceException("获取用户ID异常", HttpStatus.UNAUTHORIZED);
    }
}

/**
 * 获取用户
 **/
public static LoginUser getLoginUser() {
    try {
        // 从 Spring Security 上下文获取认证信息
        return (LoginUser) getAuthentication().getPrincipal();
    } catch (Exception e) {
        throw new ServiceException("获取用户信息异常", HttpStatus.UNAUTHORIZED);
    }
}

/**
 * 获取Authentication
 */
public static Authentication getAuthentication() {
    // 从 Spring Security 的 SecurityContextHolder 获取
    return SecurityContextHolder.getContext().getAuthentication();
}
```

**数据来源**：
- Spring Security 在用户登录成功后，会将用户信息存储在 `SecurityContextHolder` 中
- 每次请求都会从 HTTP Header 的 Token 中解析出用户信息并加载到上下文

---

### 4. Mapper 层使用 isAdmin

**文件**: `ProjectSchedulesMapper.xml` (第 40-56 行)

```xml
<select id="selectProjectSchedulesList" parameterType="ProjectSchedules" resultMap="ProjectSchedulesResult">
    <include refid="selectProjectSchedulesVo"/>
    <where>
        <!-- 🔑 使用 isAdmin 字段进行权限判断 -->
        <if test="isAdmin != null and isAdmin == true">
            <!-- 管理员：查看所有项目的进度 -->
            1=1
        </if>
        <if test="isAdmin == null or isAdmin == false">
            <!-- 非管理员：只能查看参与项目的进度 -->
            <if test="currentUserId != null and currentUserId != ''">
                AND ps.project_id IN (
                    SELECT pm.project_id
                    FROM project_members pm
                    WHERE pm.user_id = #{currentUserId} AND pm.is_active = 1
                )
            </if>
            <if test="currentUserId == null or currentUserId == ''">
                <!-- 未登录用户无法查看任何进度 -->
                1=0
            </if>
        </if>
    </where>
</select>
```

---

## 数据库中的管理员用户

### sys_user 表数据

```sql
-- user_id = 1 的用户是超级管理员
INSERT INTO sys_user (user_id, user_name, nick_name, ...) VALUES 
(1, 'admin', '若依', ...);  -- ✅ 管理员（userId = 1）

-- 其他用户都是普通用户
INSERT INTO sys_user (user_id, user_name, nick_name, ...) VALUES 
(2,   'ry',    '若依', ...),  -- ❌ 普通用户
(100, '234',   '123',  ...),  -- ❌ 普通用户
(103, 'user1', '张三', ...),  -- ❌ 普通用户
(104, 'user2', '李四', ...);  -- ❌ 普通用户
```

---

## 实际案例演示

### 案例 1：管理员登录

```
1. 用户登录
   - username: admin
   - userId: 1

2. Service 层处理
   Long currentUserId = SecurityUtils.getUserId();
   → currentUserId = 1
   
   projectSchedules.setIsAdmin(SecurityUtils.isAdmin(1));
   → SecurityUtils.isAdmin(1) 返回 true
   → projectSchedules.isAdmin = true

3. Mapper 层 SQL
   <if test="isAdmin != null and isAdmin == true">
       1=1  ✅ 条件成立
   </if>
   
   生成的 SQL：
   SELECT ps.* FROM project_schedules ps WHERE 1=1
   
4. 结果
   ✅ 查询所有项目的进度，无限制
```

---

### 案例 2：普通用户登录

```
1. 用户登录
   - username: user1
   - userId: 103

2. Service 层处理
   Long currentUserId = SecurityUtils.getUserId();
   → currentUserId = 103
   
   projectSchedules.setIsAdmin(SecurityUtils.isAdmin(103));
   → SecurityUtils.isAdmin(103) 返回 false (103 != 1)
   → projectSchedules.isAdmin = false

3. Mapper 层 SQL
   <if test="isAdmin == null or isAdmin == false">
       AND ps.project_id IN (
           SELECT pm.project_id
           FROM project_members pm
           WHERE pm.user_id = '103' AND pm.is_active = 1
       )  ✅ 条件成立
   </if>
   
   生成的 SQL：
   SELECT ps.* FROM project_schedules ps 
   WHERE ps.project_id IN (
       SELECT pm.project_id 
       FROM project_members pm 
       WHERE pm.user_id = '103' AND pm.is_active = 1
   )
   
4. 结果
   ✅ 只查询用户 103 参与项目的进度
```

---

## isAdmin 的三种状态

| isAdmin 值 | 含义 | SQL 条件 | 查询范围 |
|-----------|------|---------|---------|
| `true` | 管理员 (userId=1) | `WHERE 1=1` | 所有数据 |
| `false` | 普通用户 (userId≠1) | `WHERE project_id IN (...)` | 参与的项目 |
| `null` | 未登录或异常 | `WHERE 1=0` | 无数据 |

---

## 为什么硬编码 userId = 1？

### 设计原因

1. **超级管理员唯一性**
   - 系统中只有一个超级管理员账号
   - userId = 1 是系统初始化时创建的第一个用户
   - 具有最高权限，不受任何限制

2. **简单高效**
   - 不需要查询数据库判断角色
   - 直接通过 userId 判断，性能最优

3. **若依框架约定**
   - 这是若依（RuoYi）框架的设计约定
   - 所有基于若依的系统都遵循这个规则

### 其他角色判断

如果需要判断其他角色（如：项目经理、设计师），使用 `SecurityUtils.hasRole()` 方法：

```java
// 判断是否有 admin 角色
Boolean isAdmin = SecurityUtils.hasRole("admin");

// 判断是否有 gly（管理员）角色
Boolean isGly = SecurityUtils.hasRole("gly");

// 判断是否有项目经理角色
Boolean isPM = SecurityUtils.hasRole("project_manager");
```

---

## 总结

### isAdmin 的控制流程

```
用户登录
  ↓
Spring Security 存储用户信息（包含 userId）
  ↓
Service 层调用 SecurityUtils.getUserId() 获取 userId
  ↓
Service 层调用 SecurityUtils.isAdmin(userId) 判断
  ↓
判断逻辑：userId == 1 ? true : false
  ↓
将结果设置到查询对象：projectSchedules.setIsAdmin(result)
  ↓
Mapper 层根据 isAdmin 值生成不同的 SQL
  ↓
- isAdmin = true  → WHERE 1=1 (查询所有)
- isAdmin = false → WHERE project_id IN (...) (查询参与的)
```

### 关键点

1. **判断位置**：`SecurityUtils.isAdmin(userId)` 方法
2. **判断逻辑**：`userId == 1` 就是管理员
3. **设置位置**：Service 层的 `selectProjectSchedulesList()` 方法
4. **使用位置**：Mapper XML 的 `<if test="isAdmin == true">` 条件
5. **数据来源**：Spring Security 的 `SecurityContextHolder`

### 核心代码

```java
// 判断是否为管理员的核心代码
public static boolean isAdmin(Long userId) {
    return userId != null && 1L == userId;
}
```

这就是 `isAdmin` 字段的完整控制流程！
