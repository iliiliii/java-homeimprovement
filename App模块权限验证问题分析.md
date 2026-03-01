# App 模块权限验证问题分析

**问题发现时间**: 2026-03-02  
**问题描述**: 增加关联客户后，用户访问项目时提示 `code: 500, msg: "无权访问该项目"`

---

## 🔍 问题根源分析

### 1. 问题定位

在 `sb3/evs-home/src/main/java/com/ruoyi/app` 模块下的多个 Service 实现类中，权限验证逻辑仍然使用旧的单客户验证方式：

**问题代码位置**:
1. `AppProjectScheduleServiceImpl.java` - 第 270 行
2. `AppDashboardServiceImpl.java` - 第 464 行
3. `AppQualityIssueServiceImpl.java` - 第 239 行

### 2. 问题代码示例

```java
// 当前的错误逻辑（旧版本）
if ("customer".equals(userType)) {
    Projects project = projectMapper.selectProjectById(projectId);
    if (project == null || !userId.equals(project.getCustomerId())) {
        throw new ServiceException("无权访问该项目");
    }
}
```

### 3. 问题原因

#### 3.1 旧的验证逻辑
- 只检查 `project.getCustomerId()` 是否等于当前用户ID
- 这是单客户模式的验证方式
- 只有主客户（customerId）才能通过验证

#### 3.2 新的业务需求
- 项目支持多个客户关联
- 所有关联的客户都应该有权访问项目
- 不仅仅是主客户（customerId）

#### 3.3 导致的问题
当你添加一个新客户到项目时：
1. 新客户被添加到 `project_customers` 表
2. 但 `projects.customer_id` 仍然是原来的主客户ID
3. 新客户访问项目时，验证逻辑检查 `userId != project.customerId`
4. 验证失败，抛出"无权访问该项目"异常

---

## 📋 受影响的文件清单

### Service 实现类（3个文件）

#### 1. AppProjectScheduleServiceImpl.java
**文件路径**: `sb3/evs-home/src/main/java/com/ruoyi/app/service/impl/AppProjectScheduleServiceImpl.java`

**问题方法**:
- `validateProjectAccess(String projectId, Map<String, Object> claims)` - 第 210-278 行

**问题代码行**: 第 269-271 行
```java
Projects project = projectMapper.selectProjectById(projectId);
if (project == null || !userId.equals(project.getCustomerId())) {
    throw new ServiceException("无权访问该项目");
}
```

#### 2. AppDashboardServiceImpl.java
**文件路径**: `sb3/evs-home/src/main/java/com/ruoyi/app/service/impl/AppDashboardServiceImpl.java`

**问题方法**:
- `validateProjectAccess(String projectId, Map<String, Object> claims)` - 第 404-472 行

**问题代码行**: 第 463-465 行
```java
Projects project = projectMapper.selectProjectById(projectId);
if (project == null || !userId.equals(project.getCustomerId())) {
    throw new ServiceException("无权访问该项目");
}
```

#### 3. AppQualityIssueServiceImpl.java
**文件路径**: `sb3/evs-home/src/main/java/com/ruoyi/app/service/impl/AppQualityIssueServiceImpl.java`

**问题方法**:
- `validateProjectAccess(String projectId, Map<String, Object> claims)` - 第 210-248 行

**问题代码行**: 第 238-240 行
```java
Projects project = projectMapper.selectProjectById(projectId);
if (project == null || !userId.equals(project.getCustomerId())) {
    throw new ServiceException("无权访问该项目");
}
```

---

## 🔧 正确的验证逻辑

### 应该使用的验证方式

```java
// 正确的多客户验证逻辑
if ("customer".equals(userType)) {
    // 检查客户是否关联到项目（支持多客户）
    boolean hasAccess = projectCustomersMapper.checkCustomerInProject(projectId, userId);
    if (!hasAccess) {
        throw new ServiceException("无权访问该项目");
    }
}
```

### 为什么这样是正确的？

1. **查询 project_customers 表**: 检查客户是否在项目的关联客户列表中
2. **支持多客户**: 所有关联的客户都能通过验证
3. **不依赖 customerId**: 不再依赖 `projects.customer_id` 字段
4. **向后兼容**: 主客户也在 `project_customers` 表中，所以主客户也能通过验证

---

## 📊 影响范围评估

### 1. 直接影响的功能

#### AppProjectScheduleServiceImpl
- `getProjectScheduleList()` - 获取项目进度列表
- `getProjectScheduleDetail()` - 获取进度详情
- `addProjectSchedule()` - 添加进度
- `updateProjectSchedule()` - 更新进度
- `deleteProjectSchedule()` - 删除进度

#### AppDashboardServiceImpl
- `getDashboardData()` - 获取仪表盘数据
- 所有需要项目权限验证的方法

#### AppQualityIssueServiceImpl
- `getQualityIssueList()` - 获取质量问题列表
- `getQualityIssueDetail()` - 获取问题详情
- `addQualityIssue()` - 添加问题
- `updateQualityIssue()` - 更新问题
- `deleteQualityIssue()` - 删除问题

### 2. 用户体验影响

**场景 1: 添加新客户到项目**
- 主客户添加了客户B到项目
- 客户B登录后访问项目
- ❌ 提示"无权访问该项目"（当前问题）
- ✅ 应该能正常访问（修复后）

**场景 2: 多客户协作**
- 项目有3个客户：A（主客户）、B、C
- 客户B和C登录后访问项目
- ❌ 提示"无权访问该项目"（当前问题）
- ✅ 应该能正常访问（修复后）

**场景 3: 切换主客户**
- 原主客户A，切换为客户B
- 客户A仍然在项目中（非主客户）
- ✅ 客户A应该仍能访问项目
- ✅ 客户B作为新主客户也能访问

---

## 🎯 修复方案

### 方案 1: 使用 ProjectCustomersMapper（推荐）

**优点**:
- 直接查询 `project_customers` 表
- 性能好（有索引）
- 逻辑清晰

**实现**:
```java
// 注入 Mapper
@Autowired
private ProjectCustomersMapper projectCustomersMapper;

// 验证逻辑
if ("customer".equals(userType)) {
    boolean hasAccess = projectCustomersMapper.checkCustomerInProject(projectId, userId);
    if (!hasAccess) {
        throw new ServiceException("无权访问该项目");
    }
}
```

### 方案 2: 使用 ProjectCustomersService（更规范）

**优点**:
- 通过 Service 层调用，更符合分层架构
- 可以复用业务逻辑
- 便于单元测试

**实现**:
```java
// 注入 Service
@Autowired
private IProjectCustomersService projectCustomersService;

// 验证逻辑
if ("customer".equals(userType)) {
    boolean hasAccess = projectCustomersService.checkCustomerAccess(projectId, userId);
    if (!hasAccess) {
        throw new ServiceException("无权访问该项目");
    }
}
```

### 方案 3: 使用 AppDashboardMapper（已有方法）

**优点**:
- 可能已经有类似的查询方法
- 不需要新增依赖

**实现**:
```java
// 检查是否已有类似方法
boolean hasAccess = dashboardMapper.checkCustomerProjectAccess(userId, projectId);
if (!hasAccess) {
    throw new ServiceException("无权访问该项目");
}
```

---

## 🔍 需要确认的问题

### 1. AppDashboardMapper 是否已有相关方法？

需要检查 `AppDashboardMapper.java` 和 `AppDashboardMapper.xml` 是否已经有：
- `checkCustomerProjectAccess(String customerId, String projectId)`
- 或类似的方法

### 2. 是否需要新增 Mapper 方法？

如果 `AppDashboardMapper` 没有相关方法，需要：
- 在 `AppDashboardMapper.java` 中添加方法声明
- 在 `AppDashboardMapper.xml` 中添加 SQL 实现

### 3. 依赖注入问题

需要确认：
- `app` 模块是否可以直接注入 `ProjectCustomersMapper`？
- 是否存在循环依赖问题？
- 是否需要通过 Service 层调用？

---

## 📝 修复步骤建议

### Step 1: 确认现有方法（5分钟）

检查 `AppDashboardMapper` 是否已有客户权限验证方法：

```bash
# 搜索相关方法
grep -r "checkCustomer" sb3/evs-home/src/main/java/com/ruoyi/app/mapper/
grep -r "checkCustomer" sb3/evs-home/src/main/resources/mapper/app/
```

### Step 2: 选择修复方案（5分钟）

根据 Step 1 的结果选择：
- 如果有现成方法 → 直接使用
- 如果没有 → 选择方案 1 或方案 2

### Step 3: 修改代码（30分钟）

修改 3 个 Service 实现类：
1. `AppProjectScheduleServiceImpl.java`
2. `AppDashboardServiceImpl.java`
3. `AppQualityIssueServiceImpl.java`

### Step 4: 测试验证（30分钟）

测试场景：
1. 主客户访问项目 ✓
2. 新增客户访问项目 ✓
3. 非关联客户访问项目 ✗（应该拒绝）
4. 切换主客户后访问 ✓

---

## ⚠️ 注意事项

### 1. 向后兼容

修复后需要确保：
- 旧的单客户项目仍然能正常访问
- 主客户的权限不受影响
- 数据迁移后的项目能正常访问

### 2. 性能考虑

- `checkCustomerInProject` 方法有索引支持，性能良好
- 避免在循环中调用权限验证
- 考虑添加缓存（如果需要）

### 3. 安全性

- 确保非关联客户无法访问项目
- 验证逻辑要严格
- 记录权限验证失败的日志

### 4. 测试覆盖

需要测试：
- 单客户项目（向后兼容）
- 多客户项目（新功能）
- 边界情况（无客户、已删除客户等）

---

## 📊 修复优先级

### P0 - 立即修复（影响核心功能）
- ✅ `AppProjectScheduleServiceImpl.java` - 进度管理
- ✅ `AppDashboardServiceImpl.java` - 仪表盘
- ✅ `AppQualityIssueServiceImpl.java` - 质量问题

### P1 - 尽快修复（可能存在类似问题）
- 检查其他 `app` 模块的 Service 类
- 检查是否有其他地方使用了 `project.getCustomerId()` 进行权限验证

---

## 🎯 总结

### 问题本质
- **旧逻辑**: 只验证 `userId == project.customerId`（单客户模式）
- **新需求**: 验证 `userId IN project_customers`（多客户模式）
- **冲突**: 新增的客户不在 `project.customerId` 中，导致验证失败

### 解决方案
- 将权限验证从 `projects.customer_id` 迁移到 `project_customers` 表
- 使用 `checkCustomerInProject()` 方法验证客户是否关联到项目
- 支持多客户同时访问同一项目

### 预期效果
- 所有关联的客户都能正常访问项目
- 主客户和非主客户权限一致
- 向后兼容旧的单客户项目

---

**文档版本**: v1.0  
**创建时间**: 2026-03-02  
**状态**: 待修复
