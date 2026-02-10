# evs/projectSchedules/list 和 evs/projectScheduleRecords/list 权限控制分析

## 概述

这两个 API 的权限控制机制存在**显著差异**：

- **projectSchedules/list**: 实现了完整的三层权限控制（与 projects/list 类似）
- **projectScheduleRecords/list**: ⚠️ **缺少权限控制**，存在安全隐患

---

## 一、evs/projectSchedules/list 权限控制

### 1.1 Controller 层 - 接口权限验证

**文件**: `ProjectSchedulesController.java`

```java
@PreAuthorize("@ss.hasPermi('evs:projectSchedules:list')")
@GetMapping("/list")
public TableDataInfo list(ProjectSchedules projectSchedules) {
    startPage();
    List<ProjectSchedules> list = projectSchedulesService.selectProjectSchedulesList(projectSchedules);
    return getDataTable(list);
}
```

**权限点**:
- 要求用户拥有 `evs:projectSchedules:list` 权限
- 无权限用户请求会被拒绝（403）

---

### 1.2 Service 层 - 用户身份识别

**文件**: `ProjectSchedulesServiceImpl.java`

```java
@Override
public List<ProjectSchedules> selectProjectSchedulesList(ProjectSchedules projectSchedules) {
    // 设置当前用户ID，用于数据权限过滤
    Long currentUserId = SecurityUtils.getUserId();
    if (currentUserId != null) {
        projectSchedules.setCurrentUserId(String.valueOf(currentUserId));
        projectSchedules.setIsAdmin(SecurityUtils.isAdmin(currentUserId));
    }
    
    return projectSchedulesMapper.selectProjectSchedulesList(projectSchedules);
}
```

**功能**:
1. 获取当前登录用户 ID
2. 判断用户是否为管理员（通过 `SecurityUtils.isAdmin(userId)`）
3. 将权限信息注入到查询对象

**注意**: 与 `projects/list` 不同，这里使用 `SecurityUtils.isAdmin(userId)` 而不是 `hasRole("admin")`

---

### 1.3 Mapper 层 - 数据过滤（核心）

**文件**: `ProjectSchedulesMapper.xml`

```xml
<select id="selectProjectSchedulesList" parameterType="ProjectSchedules" resultMap="ProjectSchedulesResult">
    <include refid="selectProjectSchedulesVo"/>
    <where>
        <!-- 数据权限控制：区分管理员和非管理员 -->
        <if test="isAdmin != null and isAdmin == true">
            <!-- 管理员查看所有项目的进度 -->
            1=1
        </if>
        <if test="isAdmin == null or isAdmin == false">
            <!-- 非管理员只能查看参与项目的进度 -->
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
        <!-- 其他查询条件 -->
        <if test="projectId != null and projectId != ''"> and ps.project_id = #{projectId}</if>
        <if test="status != null and status != ''"> and ps.status = #{status}</if>
    </where>
    order by ps.project_id, ps.stage_order asc
</select>
```

#### 权限规则

| 用户类型 | 权限规则 | SQL 条件 |
|---------|---------|---------|
| **管理员** | 可查看所有项目的进度 | `1=1` (无限制) |
| **普通员工** | 只能查看参与项目的进度 | `ps.project_id IN (SELECT project_id FROM project_members WHERE user_id = #{currentUserId})` |
| **未登录用户** | 无法查看任何进度 | `1=0` (永远为假) |

**权限依据**: 通过 `project_members` 表判断用户是否为项目成员

---

### 1.4 删除操作的权限控制

**特别注意**: 删除操作也实现了权限控制

```xml
<delete id="deleteProjectSchedulesById" parameterType="ProjectSchedules">
    delete from project_schedules
    where id = #{id}
    <if test="isAdmin == null or isAdmin == false">
      and project_id in (
          select pm.project_id from project_members pm
          where pm.user_id = #{currentUserId} and pm.is_active = 1
      )
    </if>
</delete>
```

**规则**:
- 管理员可删除任何进度
- 普通员工只能删除自己参与项目的进度

---

## 二、evs/projectScheduleRecords/list 权限控制

### 2.1 Controller 层 - 接口权限验证

**文件**: `ProjectScheduleRecordsController.java`

```java
@PreAuthorize("@ss.hasPermi('evs:projectScheduleRecords:list')")
@GetMapping("/list")
public TableDataInfo list(ProjectScheduleRecords projectScheduleRecords) {
    startPage();
    List<ProjectScheduleRecords> list = projectScheduleRecordsService.selectProjectScheduleRecordsList(projectScheduleRecords);
    return getDataTable(list);
}
```

**权限点**:
- 要求用户拥有 `evs:projectScheduleRecords:list` 权限

---

### 2.2 Service 层 - ⚠️ 无权限控制

**文件**: `ProjectScheduleRecordsServiceImpl.java`

```java
@Override
public List<ProjectScheduleRecords> selectProjectScheduleRecordsList(ProjectScheduleRecords projectScheduleRecords) {
    // ⚠️ 直接查询，没有注入用户身份信息
    return projectScheduleRecordsMapper.selectProjectScheduleRecordsList(projectScheduleRecords);
}
```

**问题**: 
- ❌ 没有获取当前用户 ID
- ❌ 没有判断用户角色
- ❌ 没有注入权限信息到查询对象

---

### 2.3 Mapper 层 - ⚠️ 无数据过滤

**文件**: `ProjectScheduleRecordsMapper.xml`

```xml
<select id="selectProjectScheduleRecordsList" parameterType="ProjectScheduleRecords" resultMap="ProjectScheduleRecordsResult">
    <include refid="selectProjectScheduleRecordsVo"/>
    <where>
        <if test="projectId != null and projectId != ''"> and project_id = #{projectId}</if>
        <if test="scheduleId != null and scheduleId != ''"> and schedule_id = #{scheduleId}</if>
        <if test="recordType != null and recordType != ''"> and record_type = #{recordType}</if>
        <!-- ⚠️ 没有任何权限过滤条件 -->
    </where>
</select>
```

**问题**:
- ❌ 没有基于用户身份的数据过滤
- ❌ 所有拥有接口权限的用户都能看到所有记录
- ❌ 无法区分管理员和普通员工

---

## 三、权限控制对比

### 3.1 完整对比表

| 层级 | projectSchedules/list | projectScheduleRecords/list |
|-----|----------------------|----------------------------|
| **Controller 层** | ✅ `@PreAuthorize` 接口权限 | ✅ `@PreAuthorize` 接口权限 |
| **Service 层** | ✅ 注入 `currentUserId` 和 `isAdmin` | ❌ 无权限处理 |
| **Mapper 层** | ✅ SQL 数据过滤（基于 project_members） | ❌ 无数据过滤 |
| **管理员权限** | ✅ 查看所有进度 | ⚠️ 查看所有记录（无区分） |
| **普通员工权限** | ✅ 只看参与项目的进度 | ⚠️ 查看所有记录（安全隐患） |
| **删除权限** | ✅ 有权限控制 | ❌ 无权限控制 |

---

### 3.2 安全风险分析

#### projectScheduleRecords/list 的安全问题

1. **数据泄露风险**
   - 任何拥有 `evs:projectScheduleRecords:list` 权限的用户都能查看所有项目的进度记录
   - 员工可以看到不属于自己项目的验收记录、问题记录等敏感信息

2. **越权访问**
   - 普通员工可以通过指定 `projectId` 参数查询任意项目的记录
   - 无法实现数据隔离

3. **业务逻辑漏洞**
   - 违反了"员工只能访问自己参与项目"的业务规则
   - 与 `projects` 和 `projectSchedules` 的权限模型不一致

---

## 四、权限控制流程对比

### 4.1 projectSchedules/list 流程（正确）

```
1. 用户请求 GET /evs/projectSchedules/list
   ↓
2. Controller: @PreAuthorize 验证接口权限
   - 检查 'evs:projectSchedules:list' 权限
   ↓
3. Service: 注入用户身份信息
   - currentUserId = SecurityUtils.getUserId()
   - isAdmin = SecurityUtils.isAdmin(currentUserId)
   ↓
4. Mapper: SQL 动态过滤
   - 管理员: 查询所有进度 (1=1)
   - 普通员工: 只查询参与项目的进度 (子查询 project_members)
   - 未登录: 返回空 (1=0)
   ↓
5. 返回过滤后的进度列表
```

---

### 4.2 projectScheduleRecords/list 流程（有缺陷）

```
1. 用户请求 GET /evs/projectScheduleRecords/list
   ↓
2. Controller: @PreAuthorize 验证接口权限
   - 检查 'evs:projectScheduleRecords:list' 权限
   ↓
3. Service: ⚠️ 直接查询，无权限处理
   ↓
4. Mapper: ⚠️ 无权限过滤
   - 返回所有符合条件的记录（无用户隔离）
   ↓
5. ⚠️ 返回所有记录（安全隐患）
```

---

## 五、修复建议

### 5.1 修复 projectScheduleRecords 权限控制

#### 步骤 1: 修改 Service 层

```java
@Override
public List<ProjectScheduleRecords> selectProjectScheduleRecordsList(ProjectScheduleRecords projectScheduleRecords) {
    // 设置当前用户ID，用于数据权限过滤
    Long currentUserId = SecurityUtils.getUserId();
    if (currentUserId != null) {
        projectScheduleRecords.setCurrentUserId(String.valueOf(currentUserId));
        projectScheduleRecords.setIsAdmin(SecurityUtils.isAdmin(currentUserId));
    }
    
    return projectScheduleRecordsMapper.selectProjectScheduleRecordsList(projectScheduleRecords);
}
```

#### 步骤 2: 修改实体类

在 `ProjectScheduleRecords.java` 中添加权限字段：

```java
/** 当前用户ID（用于数据权限过滤） */
private String currentUserId;

/** 是否为管理员（用于数据权限控制） */
private Boolean isAdmin;

// getter 和 setter 方法
```

#### 步骤 3: 修改 Mapper XML

```xml
<select id="selectProjectScheduleRecordsList" parameterType="ProjectScheduleRecords" resultMap="ProjectScheduleRecordsResult">
    <include refid="selectProjectScheduleRecordsVo"/>
    <where>
        <!-- 数据权限控制：区分管理员和非管理员 -->
        <if test="isAdmin != null and isAdmin == true">
            <!-- 管理员查看所有记录 -->
            1=1
        </if>
        <if test="isAdmin == null or isAdmin == false">
            <!-- 非管理员只能查看参与项目的记录 -->
            <if test="currentUserId != null and currentUserId != ''">
                AND project_id IN (
                    SELECT pm.project_id
                    FROM project_members pm
                    WHERE pm.user_id = #{currentUserId} AND pm.is_active = 1
                )
            </if>
            <if test="currentUserId == null or currentUserId == ''">
                <!-- 未登录用户无法查看任何记录 -->
                1=0
            </if>
        </if>
        <!-- 原有查询条件 -->
        <if test="projectId != null and projectId != ''"> and project_id = #{projectId}</if>
        <if test="scheduleId != null and scheduleId != ''"> and schedule_id = #{scheduleId}</if>
        <if test="recordType != null and recordType != ''"> and record_type = #{recordType}</if>
    </where>
</select>
```

---

## 六、权限判断方法对比

### 6.1 两种管理员判断方式

| 方法 | 使用位置 | 判断逻辑 |
|-----|---------|---------|
| `SecurityUtils.hasRole("admin")` | projects/list | 检查用户是否拥有 "admin" 或 "gly" 角色 |
| `SecurityUtils.isAdmin(userId)` | projectSchedules/list | 检查 userId 是否等于 1（超级管理员） |

#### SecurityUtils.isAdmin() 实现

```java
public static boolean isAdmin(Long userId) {
    return userId != null && 1L == userId;
}
```

**区别**:
- `hasRole("admin")`: 基于角色判断，更灵活
- `isAdmin(userId)`: 硬编码判断 userId=1，更严格

**建议**: 统一使用 `hasRole("admin")` 方式，保持一致性

---

## 七、总结

### 7.1 projectSchedules/list

✅ **权限控制完善**
- 三层权限控制（Controller + Service + Mapper）
- 基于 `project_members` 表实现数据隔离
- 管理员和普通员工有明确的权限边界
- 删除操作也有权限控制

### 7.2 projectScheduleRecords/list

⚠️ **存在安全隐患**
- 只有接口级权限控制
- 缺少数据级权限过滤
- 所有用户都能看到所有记录
- **需要立即修复**

### 7.3 修复优先级

🔴 **高优先级**: 修复 `projectScheduleRecords/list` 的权限控制
- 存在数据泄露风险
- 违反业务规则
- 与其他 API 权限模型不一致

### 7.4 最佳实践

1. **统一权限模型**: 所有业务 API 应采用相同的权限控制机制
2. **三层防护**: Controller（接口权限）+ Service（身份识别）+ Mapper（数据过滤）
3. **基于关系表**: 通过 `project_members` 等关系表实现细粒度权限控制
4. **一致性**: 使用统一的管理员判断方法（建议 `hasRole()`）
