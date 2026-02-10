# projectSchedules 权限判断方式修改说明

## 修改概述

将 `evs/projectSchedules/list` 接口的管理员权限判断方式从**硬编码 userId** 改为**基于角色判断**，与 `evs/projects/list` 保持一致。

---

## 修改内容

### 修改文件

`sb3/evs-home/src/main/java/com/ruoyi/web/service/impl/ProjectSchedulesServiceImpl.java`

### 修改位置

共修改了 **6 处**，涉及以下方法：

1. `checkDuplicate()` - 检查重复方法
2. `checkDuplicateExclude()` - 检查重复（排除指定记录）方法
3. `selectProjectSchedulesById()` - 查询单个进度方法
4. `selectProjectSchedulesList()` - 查询进度列表方法
5. `deleteProjectSchedulesByIds()` - 批量删除方法
6. `deleteProjectSchedulesById()` - 删除单个进度方法

---

## 修改对比

### 修改前（旧代码）

```java
// 使用硬编码判断：userId == 1 就是管理员
projectSchedules.setIsAdmin(SecurityUtils.isAdmin(currentUserId));
```

**问题**：
- 只有 userId = 1 的用户才被认为是管理员
- 无法灵活配置管理员角色
- 与 projects/list 的权限判断方式不一致

---

### 修改后（新代码）

```java
// 使用角色判断：拥有 admin 或 gly 角色的用户是管理员
projectSchedules.setIsAdmin(SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("gly"));
```

**优点**：
- 基于角色判断，更灵活
- 可以通过配置角色来管理权限
- 与 projects/list 保持一致
- 支持多个管理员角色（admin、gly）

---

## 详细修改示例

### 1. selectProjectSchedulesList() 方法

**修改前**：
```java
@Override
public List<ProjectSchedules> selectProjectSchedulesList(ProjectSchedules projectSchedules) {
    Long currentUserId = SecurityUtils.getUserId();
    if (currentUserId != null) {
        projectSchedules.setCurrentUserId(String.valueOf(currentUserId));
        projectSchedules.setIsAdmin(SecurityUtils.isAdmin(currentUserId));  // ❌ 旧方式
    }
    return projectSchedulesMapper.selectProjectSchedulesList(projectSchedules);
}
```

**修改后**：
```java
@Override
public List<ProjectSchedules> selectProjectSchedulesList(ProjectSchedules projectSchedules) {
    Long currentUserId = SecurityUtils.getUserId();
    if (currentUserId != null) {
        projectSchedules.setCurrentUserId(String.valueOf(currentUserId));
        projectSchedules.setIsAdmin(SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("gly"));  // ✅ 新方式
    }
    return projectSchedulesMapper.selectProjectSchedulesList(projectSchedules);
}
```

---

### 2. deleteProjectSchedulesByIds() 方法

**修改前**：
```java
@Override
public int deleteProjectSchedulesByIds(String[] ids) {
    Long currentUserId = SecurityUtils.getUserId();
    if (currentUserId == null) {
        throw new ServiceException("用户未登录");
    }

    boolean isAdmin = SecurityUtils.isAdmin(currentUserId);  // ❌ 旧方式

    if (!isAdmin) {
        // 验证权限...
    }
    
    // ...
}
```

**修改后**：
```java
@Override
public int deleteProjectSchedulesByIds(String[] ids) {
    Long currentUserId = SecurityUtils.getUserId();
    if (currentUserId == null) {
        throw new ServiceException("用户未登录");
    }

    boolean isAdmin = SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("gly");  // ✅ 新方式

    if (!isAdmin) {
        // 验证权限...
    }
    
    // ...
}
```

---

## 权限判断逻辑对比

### 旧方式：SecurityUtils.isAdmin(userId)

```java
public static boolean isAdmin(Long userId) {
    return userId != null && 1L == userId;
}
```

**判断规则**：
- userId == 1 → 管理员
- userId != 1 → 普通用户

**限制**：
- 只能有一个管理员（userId = 1）
- 无法灵活配置

---

### 新方式：SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("gly")

```java
public static boolean hasRole(String role) {
    List<SysRole> roleList = getLoginUser().getUser().getRoles();
    Collection<String> roles = roleList.stream()
        .map(SysRole::getRoleKey)
        .collect(Collectors.toSet());
    return hasRole(roles, role);
}
```

**判断规则**：
- 拥有 "admin" 角色 → 管理员
- 拥有 "gly" 角色 → 管理员
- 其他角色 → 普通用户

**优点**：
- 可以有多个管理员
- 通过数据库配置角色
- 灵活可扩展

---

## 影响范围

### 受影响的接口

1. **GET /evs/projectSchedules/list** - 查询项目进度列表
2. **GET /evs/projectSchedules/{id}** - 查询单个项目进度
3. **DELETE /evs/projectSchedules/{ids}** - 批量删除项目进度
4. **DELETE /evs/projectSchedules/{id}** - 删除单个项目进度

### 权限行为变化

#### 修改前

| 用户 | userId | 权限 |
|-----|--------|------|
| admin | 1 | ✅ 管理员（可查看所有） |
| user1 | 2 | ❌ 普通用户（只看参与的） |
| user2 | 103 | ❌ 普通用户（只看参与的） |

**问题**：即使 user1 拥有 admin 角色，但因为 userId != 1，仍被当作普通用户

---

#### 修改后

| 用户 | userId | 角色 | 权限 |
|-----|--------|------|------|
| admin | 1 | admin | ✅ 管理员（可查看所有） |
| user1 | 2 | admin | ✅ 管理员（可查看所有） |
| user2 | 103 | gly | ✅ 管理员（可查看所有） |
| user3 | 104 | employee | ❌ 普通用户（只看参与的） |

**改进**：只要用户拥有 admin 或 gly 角色，就被认为是管理员

---

## 与 projects/list 的一致性

### projects/list 的权限判断

**文件**：`ProjectsServiceImpl.java`

```java
private Projects setCurrentUser(Projects projects) {
    Long currentUserId = SecurityUtils.getUserId();
    Boolean isAdmin = SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("gly");
    
    if (currentUserId != null) {
        projects.setCurrentUserId(String.valueOf(currentUserId));
        projects.setIsAdmin(isAdmin != null ? isAdmin : false);
    }
    return projects;
}
```

### projectSchedules/list 的权限判断（修改后）

**文件**：`ProjectSchedulesServiceImpl.java`

```java
@Override
public List<ProjectSchedules> selectProjectSchedulesList(ProjectSchedules projectSchedules) {
    Long currentUserId = SecurityUtils.getUserId();
    if (currentUserId != null) {
        projectSchedules.setCurrentUserId(String.valueOf(currentUserId));
        projectSchedules.setIsAdmin(SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("gly"));
    }
    return projectSchedulesMapper.selectProjectSchedulesList(projectSchedules);
}
```

✅ **现在两者的权限判断逻辑完全一致**

---

## projectScheduleRecords/list 的情况

### 检查结果

经过检查，`ProjectScheduleRecordsServiceImpl.java` 中**没有使用 isAdmin 字段**，因此：

❌ **不需要修改**

**原因**：
- 该接口目前没有实现数据级权限控制
- 没有使用 isAdmin 进行管理员判断
- 如果未来需要添加权限控制，应该参照 projects/list 和 projectSchedules/list 的实现

---

## 测试建议

### 测试场景

1. **管理员用户（拥有 admin 角色）**
   - 应该能查看所有项目的进度
   - 应该能删除任何项目的进度

2. **管理员用户（拥有 gly 角色）**
   - 应该能查看所有项目的进度
   - 应该能删除任何项目的进度

3. **普通用户（无 admin/gly 角色）**
   - 只能查看自己参与项目的进度
   - 只能删除自己参与项目的进度

### 测试步骤

1. 创建测试用户并分配不同角色
2. 使用不同用户登录并调用接口
3. 验证返回的数据是否符合权限规则

---

## 总结

### 修改内容

- ✅ 修改了 `ProjectSchedulesServiceImpl.java` 中的 6 处权限判断
- ✅ 从硬编码 userId 判断改为基于角色判断
- ✅ 与 projects/list 保持一致
- ✅ projectScheduleRecords/list 无需修改（未使用 isAdmin）

### 优势

1. **灵活性**：可以通过配置角色来管理权限
2. **一致性**：与 projects/list 的权限模型一致
3. **可扩展性**：支持多个管理员角色
4. **可维护性**：统一的权限判断逻辑

### 注意事项

- 确保数据库中的角色配置正确（admin、gly）
- 测试不同角色用户的权限是否正常
- 如果需要添加新的管理员角色，只需修改判断条件即可
