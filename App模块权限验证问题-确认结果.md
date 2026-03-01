# App 模块权限验证问题 - 确认结果

**确认时间**: 2026-03-02  
**问题**: 增加关联客户后，用户访问项目提示"无权访问该项目"

---

## ✅ 问题确认

### 1. 问题根源已确认

在 `app` 模块的 3 个 Service 实现类中，客户权限验证仍使用旧的单客户模式：

```java
// ❌ 错误的验证逻辑（旧版本）
if ("customer".equals(userType)) {
    Projects project = projectMapper.selectProjectById(projectId);
    if (project == null || !userId.equals(project.getCustomerId())) {
        throw new ServiceException("无权访问该项目");
    }
}
```

**问题**: 只检查 `project.customerId`，不检查 `project_customers` 表

### 2. 受影响的文件（3个）

| 文件 | 方法 | 问题代码行 | 影响功能 |
|------|------|-----------|---------|
| `AppProjectScheduleServiceImpl.java` | `validateProjectAccess()` | 269-271 | 项目进度管理 |
| `AppDashboardServiceImpl.java` | `validateProjectAccess()` | 463-465 | 仪表盘数据 |
| `AppQualityIssueServiceImpl.java` | `validateProjectAccess()` | 238-240 | 质量问题管理 |

### 3. 现有资源确认

#### ✅ AppDashboardMapper 已有部分支持

**已有方法**:
- `checkStaffProjectAccess()` - 员工权限验证 ✓
- `selectCustomerProjects()` - 客户项目列表（已使用 `project_customers` 表）✓

**缺少方法**:
- ❌ `checkCustomerProjectAccess()` - 客户权限验证（需要新增）

#### ✅ ProjectCustomersMapper 已完整实现

**可用方法**:
- `checkCustomerInProject(projectId, customerId)` - 检查客户是否关联到项目 ✓
- 有索引支持，性能良好 ✓

---

## 🎯 修复方案确认

### 方案选择: 使用 ProjectCustomersMapper（推荐）

**理由**:
1. ✅ `ProjectCustomersMapper` 已完整实现
2. ✅ `checkCustomerInProject()` 方法已存在
3. ✅ 有索引支持，性能好
4. ✅ 逻辑清晰，易于维护
5. ✅ 不需要修改 `AppDashboardMapper`

### 正确的验证逻辑

```java
// ✅ 正确的验证逻辑（新版本）
if ("customer".equals(userType)) {
    // 检查客户是否关联到项目（支持多客户）
    boolean hasAccess = projectCustomersMapper.checkCustomerInProject(projectId, userId);
    if (!hasAccess) {
        throw new ServiceException("无权访问该项目");
    }
}
```

---

## 📋 详细修复步骤

### Step 1: 注入 ProjectCustomersMapper

在 3 个 Service 实现类中添加依赖注入：

```java
@Autowired
private ProjectCustomersMapper projectCustomersMapper;
```

**需要修改的文件**:
1. `AppProjectScheduleServiceImpl.java`
2. `AppDashboardServiceImpl.java`
3. `AppQualityIssueServiceImpl.java`

### Step 2: 修改权限验证逻辑

#### 2.1 AppProjectScheduleServiceImpl.java

**位置**: 第 269-271 行

**修改前**:
```java
if ("customer".equals(userType)) {
    Projects project = projectMapper.selectProjectById(projectId);
    if (project == null || !userId.equals(project.getCustomerId())) {
        throw new ServiceException("无权访问该项目");
    }
}
```

**修改后**:
```java
if ("customer".equals(userType)) {
    // 检查客户是否关联到项目（支持多客户）
    boolean hasAccess = projectCustomersMapper.checkCustomerInProject(projectId, userId);
    if (!hasAccess) {
        log.warn("[权限验证] 客户 {} 无权访问项目: {}", userId, projectId);
        throw new ServiceException("无权访问该项目");
    }
    log.info("[权限验证] 客户 {} 访问项目: {}", userId, projectId);
}
```

#### 2.2 AppDashboardServiceImpl.java

**位置**: 第 463-465 行

**修改前**:
```java
if ("customer".equals(userType)) {
    Projects project = projectMapper.selectProjectById(projectId);
    if (project == null || !userId.equals(project.getCustomerId())) {
        throw new ServiceException("无权访问该项目");
    }
}
```

**修改后**:
```java
if ("customer".equals(userType)) {
    // 检查客户是否关联到项目（支持多客户）
    boolean hasAccess = projectCustomersMapper.checkCustomerInProject(projectId, userId);
    if (!hasAccess) {
        log.warn("[权限验证] 客户 {} 无权访问项目: {}", userId, projectId);
        throw new ServiceException("无权访问该项目");
    }
    log.info("[权限验证] 客户 {} 访问项目: {}", userId, projectId);
}
```

#### 2.3 AppQualityIssueServiceImpl.java

**位置**: 第 238-240 行

**修改前**:
```java
if ("customer".equals(userType)) {
    Projects project = projectMapper.selectProjectById(projectId);
    if (project == null || !userId.equals(project.getCustomerId())) {
        throw new ServiceException("无权访问该项目");
    }
}
```

**修改后**:
```java
if ("customer".equals(userType)) {
    // 检查客户是否关联到项目（支持多客户）
    boolean hasAccess = projectCustomersMapper.checkCustomerInProject(projectId, userId);
    if (!hasAccess) {
        log.warn("[权限验证] 客户 {} 无权访问项目: {}", userId, projectId);
        throw new ServiceException("无权访问该项目");
    }
    log.info("[权限验证] 客户 {} 访问项目: {}", userId, projectId);
}
```

---

## 🔍 验证 checkCustomerInProject 方法

### 方法签名

**ProjectCustomersMapper.java**:
```java
/**
 * 检查客户是否关联到项目
 */
boolean checkCustomerInProject(@Param("projectId") String projectId, 
                               @Param("customerId") String customerId);
```

### SQL 实现

**ProjectCustomersMapper.xml**:
```xml
<!-- 检查客户是否关联到项目 -->
<select id="checkCustomerInProject" resultType="boolean">
    SELECT COUNT(*) > 0
    FROM project_customers
    WHERE project_id = #{projectId} 
      AND customer_id = #{customerId}
      AND deleted_at IS NULL
</select>
```

### 索引支持

```sql
-- 复合索引（project_id, customer_id）
KEY `idx_project_customer` (`project_id`, `customer_id`)
```

**性能**: 查询速度快，有索引支持 ✓

---

## 📊 修复效果预期

### 修复前（当前问题）

| 场景 | 主客户 | 新增客户 | 结果 |
|------|--------|---------|------|
| 访问项目 | ✅ 成功 | ❌ 失败（无权访问） | 不符合预期 |
| 查看进度 | ✅ 成功 | ❌ 失败（无权访问） | 不符合预期 |
| 查看质检 | ✅ 成功 | ❌ 失败（无权访问） | 不符合预期 |

### 修复后（预期效果）

| 场景 | 主客户 | 新增客户 | 非关联客户 | 结果 |
|------|--------|---------|-----------|------|
| 访问项目 | ✅ 成功 | ✅ 成功 | ❌ 失败 | 符合预期 |
| 查看进度 | ✅ 成功 | ✅ 成功 | ❌ 失败 | 符合预期 |
| 查看质检 | ✅ 成功 | ✅ 成功 | ❌ 失败 | 符合预期 |

---

## 🧪 测试计划

### 测试场景

#### 场景 1: 主客户访问
1. 使用主客户账号登录
2. 访问项目仪表盘
3. 查看项目进度
4. 查看质量问题
5. **预期**: 全部成功 ✓

#### 场景 2: 新增客户访问
1. 主客户添加客户B到项目
2. 使用客户B账号登录
3. 访问项目仪表盘
4. 查看项目进度
5. 查看质量问题
6. **预期**: 全部成功 ✓

#### 场景 3: 非关联客户访问
1. 使用客户C账号登录（未关联到项目）
2. 尝试访问项目
3. **预期**: 提示"无权访问该项目" ✓

#### 场景 4: 切换主客户
1. 项目有客户A（主客户）和客户B
2. 切换主客户为客户B
3. 使用客户A账号登录
4. 访问项目
5. **预期**: 客户A仍能访问（因为仍在 project_customers 中）✓

#### 场景 5: 移除客户
1. 项目有客户A（主客户）和客户B
2. 移除客户B
3. 使用客户B账号登录
4. 尝试访问项目
5. **预期**: 提示"无权访问该项目" ✓

### 测试数据准备

```sql
-- 1. 创建测试项目
INSERT INTO projects (id, name, customer_id, ...) 
VALUES ('test-project-001', '测试项目', 'customer-a', ...);

-- 2. 添加主客户到 project_customers
INSERT INTO project_customers (id, project_id, customer_id, is_primary, ...)
VALUES (UUID(), 'test-project-001', 'customer-a', 1, ...);

-- 3. 添加第二个客户
INSERT INTO project_customers (id, project_id, customer_id, is_primary, ...)
VALUES (UUID(), 'test-project-001', 'customer-b', 0, ...);
```

---

## ⚠️ 注意事项

### 1. 依赖注入

确保 `ProjectCustomersMapper` 能被正确注入到 `app` 模块的 Service 中：
- 检查包扫描配置
- 检查是否存在循环依赖

### 2. 日志记录

修改后添加详细的日志：
```java
log.info("[权限验证] 客户 {} 访问项目: {}", userId, projectId);
log.warn("[权限验证] 客户 {} 无权访问项目: {}", userId, projectId);
```

### 3. 向后兼容

修复后需要确保：
- 旧的单客户项目仍能正常访问
- 数据迁移后的项目能正常访问
- 主客户权限不受影响

### 4. 性能监控

- 监控 `checkCustomerInProject` 方法的执行时间
- 确保索引生效
- 必要时添加缓存

---

## 📝 修复检查清单

### 代码修改
- [ ] `AppProjectScheduleServiceImpl.java` - 注入 Mapper
- [ ] `AppProjectScheduleServiceImpl.java` - 修改验证逻辑
- [ ] `AppDashboardServiceImpl.java` - 注入 Mapper
- [ ] `AppDashboardServiceImpl.java` - 修改验证逻辑
- [ ] `AppQualityIssueServiceImpl.java` - 注入 Mapper
- [ ] `AppQualityIssueServiceImpl.java` - 修改验证逻辑

### 编译测试
- [ ] 代码编译通过
- [ ] 无语法错误
- [ ] 无依赖注入错误

### 功能测试
- [ ] 主客户访问项目 ✓
- [ ] 新增客户访问项目 ✓
- [ ] 非关联客户访问项目 ✗
- [ ] 切换主客户后访问 ✓
- [ ] 移除客户后访问 ✗

### 性能测试
- [ ] 查询性能正常
- [ ] 索引生效
- [ ] 无性能下降

### 日志检查
- [ ] 权限验证成功有日志
- [ ] 权限验证失败有日志
- [ ] 日志信息完整

---

## 🎯 总结

### 问题确认
✅ 问题根源已明确：3 个 Service 类使用旧的单客户验证逻辑  
✅ 受影响范围已确定：项目进度、仪表盘、质量问题  
✅ 修复方案已确定：使用 `ProjectCustomersMapper.checkCustomerInProject()`

### 修复方案
✅ 技术可行：`ProjectCustomersMapper` 已完整实现  
✅ 性能良好：有索引支持，查询速度快  
✅ 逻辑清晰：代码简洁，易于维护  
✅ 向后兼容：不影响现有功能

### 下一步
1. 按照修复步骤修改 3 个 Service 类
2. 编译测试
3. 功能测试
4. 部署上线

---

**文档版本**: v1.0  
**确认时间**: 2026-03-02  
**状态**: 已确认，待修复
