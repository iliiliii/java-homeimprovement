# 后端API客户信息缺失问题分析

## 问题描述

前端请求携带了 `includeProjectMembers=true` 参数：
```
GET /evs/projects/list?pageNum=1&pageSize=10&includeProjectMembers=true
```

但是返回的数据中没有客户信息（`customer` 字段为空）。

---

## 根本原因分析

### 1. Controller 层问题

**文件**: `sb3/evs-home/src/main/java/com/ruoyi/web/controller/ProjectsController.java`

**代码分析**:
```java
@GetMapping("/list")
public TableDataInfo list(Projects projects,
                         @RequestParam(required = false) String includeCustomer,
                         @RequestParam(required = false) String includeBudgetItems,
                         @RequestParam(required = false) String includeSchedules,
                         @RequestParam(required = false) String includeProjectMembers,
                         @RequestParam(required = false) String includeScheduleInfo)
{
    startPage();

    // ✅ 构建关联查询参数
    StringBuilder includeRelations = new StringBuilder();
    if ("true".equals(includeCustomer)) {
        includeRelations.append("customer");
    }
    if ("true".equals(includeBudgetItems)) {
        if (includeRelations.length() > 0) includeRelations.append(",");
        includeRelations.append("budgetItems");
    }
    if ("true".equals(includeSchedules)) {
        if (includeRelations.length() > 0) includeRelations.append(",");
        includeRelations.append("schedules");
    }
    if ("true".equals(includeProjectMembers)) {
        if (includeRelations.length() > 0) includeRelations.append(",");
        includeRelations.append("projectMembers");
    }

    // ❌ 问题：构建了 includeRelations 但没有使用！
    List<Projects> list;
    if ("true".equals(includeScheduleInfo)) {
        list = projectsService.selectProjectsListWithScheduleInfo(projects);
    } else {
        // ❌ 直接调用 selectProjectsList，没有传递 includeRelations
        list = projectsService.selectProjectsList(projects);
    }

    return getDataTable(list);
}
```

**问题所在**:
1. ✅ Controller 正确接收了 `includeProjectMembers` 参数
2. ✅ Controller 正确构建了 `includeRelations` 字符串（值为 "projectMembers"）
3. ❌ **但是调用 Service 时，没有传递 `includeRelations` 参数**
4. ❌ 调用的是 `selectProjectsList(projects)`，而不是 `selectProjectsWithRelations(projects, includeRelations, ...)`

### 2. Service 层分析

**文件**: `sb3/evs-home/src/main/java/com/ruoyi/web/service/impl/ProjectsServiceImpl.java`

**selectProjectsList 方法**:
```java
@Override
public List<Projects> selectProjectsList(Projects projects)
{
    Projects query = setCurrentUser(projects);
    List<Projects> result = projectsMapper.selectProjectsList(query);
    return result;
}
```

**问题**: 
- 这个方法只查询项目基本信息
- **不会加载任何关联数据**（customer、budgetItems、schedules、projectMembers）

**selectProjectsWithRelations 方法**:
```java
@Override
public List<Projects> selectProjectsWithRelations(Projects projects, String includeRelations, String memberUserId, boolean isAdmin)
{
    if (!StringUtils.hasText(includeRelations)) {
        return selectProjectsList(projects);
    }

    List<Projects> projectList;

    if (includeRelations.contains("customer")) {
        // ✅ 如果包含 customer，会调用 selectProjectsWithCustomer
        projectList = projectsMapper.selectProjectsWithCustomer(projects);
    } else if (includeRelations.contains("projectMembers")) {
        // ✅ 如果包含 projectMembers，会调用 selectProjectsWithMembers
        projectList = projectsMapper.selectProjectsWithMembers(projects, memberUserId, isAdmin);
    } else {
        projectList = selectProjectsList(projects);
    }

    return projectList;
}
```

**分析**:
- 这个方法可以根据 `includeRelations` 参数加载关联数据
- 但是 **Controller 没有调用这个方法**

---

## 问题总结

### 数据流分析

```
前端请求
  ↓
GET /evs/projects/list?includeProjectMembers=true
  ↓
Controller.list() 接收参数
  ↓
构建 includeRelations = "projectMembers"  ✅
  ↓
❌ 调用 projectsService.selectProjectsList(projects)
   （没有传递 includeRelations）
  ↓
Service.selectProjectsList()
  ↓
Mapper.selectProjectsList()
  ↓
❌ 只查询项目基本信息，不包含 customer
  ↓
返回数据：{ id, name, customerId, customer: null }
```

### 正确的数据流应该是

```
前端请求
  ↓
GET /evs/projects/list?includeProjectMembers=true&includeCustomer=true
  ↓
Controller.list() 接收参数
  ↓
构建 includeRelations = "projectMembers,customer"  ✅
  ↓
✅ 调用 projectsService.selectProjectsWithRelations(
     projects, includeRelations, memberUserId, isAdmin)
  ↓
Service.selectProjectsWithRelations()
  ↓
判断 includeRelations.contains("customer")  ✅
  ↓
Mapper.selectProjectsWithCustomer()
  ↓
✅ 查询项目信息并关联查询客户信息
  ↓
返回数据：{ id, name, customerId, customer: { id, name, ... } }
```

---

## 解决方案

### 方案1: 修改 Controller（推荐）

**修改文件**: `ProjectsController.java`

```java
@GetMapping("/list")
public TableDataInfo list(Projects projects,
                         @RequestParam(required = false) String includeCustomer,
                         @RequestParam(required = false) String includeBudgetItems,
                         @RequestParam(required = false) String includeSchedules,
                         @RequestParam(required = false) String includeProjectMembers,
                         @RequestParam(required = false) String includeScheduleInfo)
{
    startPage();

    // 构建关联查询参数
    StringBuilder includeRelations = new StringBuilder();
    if ("true".equals(includeCustomer)) {
        includeRelations.append("customer");
    }
    if ("true".equals(includeBudgetItems)) {
        if (includeRelations.length() > 0) includeRelations.append(",");
        includeRelations.append("budgetItems");
    }
    if ("true".equals(includeSchedules)) {
        if (includeRelations.length() > 0) includeRelations.append(",");
        includeRelations.append("schedules");
    }
    if ("true".equals(includeProjectMembers)) {
        if (includeRelations.length() > 0) includeRelations.append(",");
        includeRelations.append("projectMembers");
    }

    List<Projects> list;
    if ("true".equals(includeScheduleInfo)) {
        list = projectsService.selectProjectsListWithScheduleInfo(projects);
    } else if (includeRelations.length() > 0) {
        // ✅ 修改：如果有关联查询参数，使用 selectProjectsWithRelations
        Long currentUserId = SecurityUtils.getUserId();
        Boolean isAdmin = SecurityUtils.hasRole("admin");
        list = projectsService.selectProjectsWithRelations(
            projects, 
            includeRelations.toString(), 
            currentUserId != null ? String.valueOf(currentUserId) : null, 
            isAdmin != null ? isAdmin : false
        );
    } else {
        // 没有关联查询参数，使用普通查询
        list = projectsService.selectProjectsList(projects);
    }

    return getDataTable(list);
}
```

### 方案2: 前端同时传递 includeCustomer 参数

**修改文件**: `vue3/src/api/evs/projects.js`

```javascript
// 当前
export function listProjectsWithMembers(query) {
  return request({
    url: '/evs/projects/list',
    method: 'get',
    params: {
      ...query,
      includeProjectMembers: true
    }
  })
}

// 修改为
export function listProjectsWithMembers(query) {
  return request({
    url: '/evs/projects/list',
    method: 'get',
    params: {
      ...query,
      includeProjectMembers: true,
      includeCustomer: true  // ✅ 添加这一行
    }
  })
}
```

### 方案3: 修改 Service 层逻辑

**修改文件**: `ProjectsServiceImpl.java`

```java
@Override
public List<Projects> selectProjectsWithRelations(Projects projects, String includeRelations, String memberUserId, boolean isAdmin)
{
    if (!StringUtils.hasText(includeRelations)) {
        return selectProjectsList(projects);
    }

    List<Projects> projectList;

    // ✅ 修改：同时支持 customer 和 projectMembers
    if (includeRelations.contains("customer") && includeRelations.contains("projectMembers")) {
        // 需要同时查询客户和项目成员
        projectList = projectsMapper.selectProjectsWithCustomerAndMembers(projects, memberUserId, isAdmin);
    } else if (includeRelations.contains("customer")) {
        projectList = projectsMapper.selectProjectsWithCustomer(projects);
    } else if (includeRelations.contains("projectMembers")) {
        projectList = projectsMapper.selectProjectsWithMembers(projects, memberUserId, isAdmin);
    } else {
        projectList = selectProjectsList(projects);
    }

    return projectList;
}
```

---

## 推荐实施方案

### 短期方案（最快）: 方案2

**优点**:
- ✅ 只需修改前端一行代码
- ✅ 不需要修改后端
- ✅ 立即生效

**实施步骤**:
1. 修改 `vue3/src/api/evs/projects.js`
2. 添加 `includeCustomer: true` 参数
3. 测试验证

### 长期方案（最佳）: 方案1

**优点**:
- ✅ 修复了后端逻辑缺陷
- ✅ 使 `includeRelations` 参数真正生效
- ✅ 提升代码质量和可维护性

**实施步骤**:
1. 修改 `ProjectsController.java` 的 `list` 方法
2. 添加条件判断，使用 `selectProjectsWithRelations`
3. 测试所有关联查询场景
4. 前端可以灵活控制需要加载的关联数据

---

## 测试验证

### 测试场景1: 只查询客户信息
```
GET /evs/projects/list?includeCustomer=true
```

**预期结果**:
```json
{
  "rows": [
    {
      "id": "xxx",
      "name": "项目名称",
      "customerId": "customer_1",
      "customer": {
        "id": "customer_1",
        "name": "张三",
        "phone": "13800138000"
      }
    }
  ]
}
```

### 测试场景2: 同时查询客户和项目成员
```
GET /evs/projects/list?includeCustomer=true&includeProjectMembers=true
```

**预期结果**:
```json
{
  "rows": [
    {
      "id": "xxx",
      "name": "项目名称",
      "customerId": "customer_1",
      "customer": {
        "id": "customer_1",
        "name": "张三"
      },
      "projectMembers": [...]
    }
  ]
}
```

### 测试场景3: 不传任何参数
```
GET /evs/projects/list
```

**预期结果**:
```json
{
  "rows": [
    {
      "id": "xxx",
      "name": "项目名称",
      "customerId": "customer_1"
      // 没有 customer 对象
    }
  ]
}
```

---

## 总结

### 问题根源

**Controller 层的实现缺陷**:
- 接收了关联查询参数
- 构建了 `includeRelations` 字符串
- **但没有使用这个字符串**
- 直接调用了不支持关联查询的方法

### 影响范围

- ✅ 项目列表查询
- ✅ 项目详情查询
- ⚠️ 所有需要关联查询客户信息的场景

### 修复优先级

**P0 - 立即修复**

**原因**:
- 影响用户体验（客户名称显示为"-"）
- 代码逻辑不一致（参数接收了但没用）
- 修复成本低（只需修改几行代码）

---

**分析日期**: 2025年12月5日  
**分析人员**: Kiro AI Assistant  
**建议**: 优先实施方案2（前端快速修复），然后实施方案1（后端彻底修复）
