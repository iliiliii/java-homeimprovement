# 项目成员关联查询 API 测试指南

## 测试概述

本文档描述了针对项目成员关联查询功能的完整测试方案，包括单元测试和集成测试。

## 测试文件结构

### 后端测试文件

1. **Service 层测试**
   - `sb3/evs-home/src/test/java/com/ruoyi/web/service/impl/ProjectsServiceImplMembersTest.java`
   - 专门测试通过 projectMembers 表关联查询和权限过滤逻辑

2. **Controller 层测试**
   - `sb3/evs-home/src/test/java/com/ruoyi/web/controller/ProjectsControllerMembersTest.java`
   - 专门测试 API 接口的权限控制和参数传递

## 运行测试

### 运行所有测试
```bash
# 在 sb3 目录下
mvn test
```

### 运行特定测试类
```bash
# 运行 Service 层测试
mvn test -Dtest=ProjectsServiceImplMembersTest

# 运行 Controller 层测试
mvn test -Dtest=ProjectsControllerMembersTest
```

### 运行特定测试方法
```bash
# 运行单个测试方法
mvn test -Dtest=ProjectsServiceImplMembersTest#testSelectProjectsWithMembersAsAdmin
```

## 测试用例详情

### Service 层测试 (ProjectsServiceImplMembersTest)

#### 1. 权限控制测试

**测试用例：`testSelectProjectsWithMembersAsAdmin`**
- 场景：管理员查询项目
- 期望结果：能看到所有项目
- 验证：isAdmin=true，调用 selectProjectsWithMembers 方法

**测试用例：`testSelectProjectsWithMembersAsNormalUser`**
- 场景：普通用户查询项目
- 期望结果：只能看到自己参与的项目
- 验证：isAdmin=false，仅返回用户关联的项目

#### 2. 筛选功能测试

**测试用例：`testSelectProjectsWithMembersWithNameFilter`**
- 测试项目名称筛选功能
- 验证筛选参数正确传递到 Mapper

**测试用例：`testSelectProjectsWithMembersAsAdminWithCustomerFilter`**
- 测试管理员特有的客户筛选功能
- 验证 customerId 参数正确传递

#### 3. 边界条件测试

**测试用例：`testSelectProjectsWithMembersWithEmptyUserId`**
- 测试空用户ID参数的处理
- 验证空字符串参数被正确传递

**测试用例：`testSelectProjectsWithMembersEmptyList`**
- 测试空结果集的处理
- 验证返回空列表

### Controller 层测试 (ProjectsControllerMembersTest)

#### 1. API 权限验证测试

**测试用例：`testListProjectsWithMembersAsAdmin`**
- HTTP GET /evs/projects/list
- 参数：includeProjectMembers=true, memberUserId=admin
- 期望：返回所有项目 (3个)

**测试用例：`testListProjectsWithMembersAsNormalUser`**
- HTTP GET /evs/projects/list
- 参数：includeProjectMembers=true, memberUserId=user123
- 期望：返回用户参与的项目 (2个)

#### 2. 筛选功能测试

**测试用例：`testListProjectsWithMembersAndNameFilter`**
- 测试项目名称筛选
- 参数：name=测试项目1
- 期望：返回匹配的项目

**测试用例：`testListProjectsWithMembersAndStatusFilter`**
- 测试项目状态筛选
- 参数：status=PLANNED
- 期望：返回指定状态的项目

**测试用例：`testListProjectsWithMembersAsAdminWithCustomerFilter`**
- 测试管理员客户筛选
- 参数：customerId=customer-123
- 期望：返回指定客户的项目

#### 3. 组合筛选测试

**测试用例：`testListProjectsWithMembersWithMultipleFilters`**
- 测试多个筛选条件组合
- 参数：name=测试, status=IN_PROGRESS
- 期望：返回同时匹配的项目

## 测试数据

### 测试项目数据
```java
// 项目1
ID: "1"
名称: "测试项目1"
状态: "IN_PROGRESS"
客户ID: "customer-1"

// 项目2
ID: "2"
名称: "测试项目2"
状态: "PLANNED"
客户ID: "customer-2"

// 项目3
ID: "3"
名称: "测试项目3"
状态: "COMPLETED"
客户ID: "customer-3"
```

## 预期测试结果

### 管理员查询结果
- 所有项目：3个
- 支持筛选：项目名称、项目状态、关联客户、关联团队成员

### 普通用户查询结果
- 参与项目：2个 (项目1, 项目3)
- 支持筛选：项目名称、项目状态

## 关键验证点

### 1. 权限验证
- ✅ 管理员可以查看所有项目
- ✅ 普通用户只能查看自己参与的项目
- ✅ 权限参数 isAdmin 正确传递

### 2. 参数传递
- ✅ includeProjectMembers 参数正确传递
- ✅ memberUserId 参数正确传递
- ✅ 筛选参数 (name, status, customerId) 正确传递

### 3. 响应格式
- ✅ HTTP 状态码 200
- ✅ JSON 格式正确
- ✅ 包含 rows 数组和 total 总数

## 故障排除

### 常见问题

1. **测试失败：方法签名不匹配**
   - 检查 Service 层方法参数：`(Projects projects, String includeRelations, String memberUserId, boolean isAdmin)`
   - 确保 Controller 层调用时传递所有参数

2. **权限验证失败**
   - 检查 `@WithMockUser` 注解中的 authorities
   - 确保用户有 `evs:projects:list` 权限

3. **Mock 验证失败**
   - 检查 when().thenReturn() 的参数匹配
   - 使用 `any(Projects.class)` 匹配任意 Projects 对象

## 扩展测试

### 添加新的测试用例

1. **在 Service 层添加测试**
   - 在 `ProjectsServiceImplMembersTest.java` 中添加新的 `@Test` 方法
   - 使用清晰的 `@DisplayName` 描述测试场景

2. **在 Controller 层添加测试**
   - 在 `ProjectsControllerMembersTest.java` 中添加新的 `@Test` 方法
   - 使用 `mockMvc.perform()` 构建 HTTP 请求

## 最佳实践

1. **测试命名**
   - 使用描述性的测试方法名
   - 添加 `@DisplayName` 注解详细说明测试场景

2. **测试数据**
   - 使用独立的数据创建方法
   - 确保测试数据的一致性

3. **Mock 使用**
   - 只 Mock 必要的依赖
   - 验证 Mock 方法的调用

4. **断言验证**
   - 验证返回值
   - 验证 Mock 方法的调用次数和参数

## 总结

本测试套件覆盖了以下关键场景：
- ✅ 管理员和普通用户的权限控制
- ✅ 各种筛选条件的正确传递和过滤
- ✅ 边界条件和异常情况的处理
- ✅ API 接口的正确响应格式

通过这些测试，可以确保项目成员关联查询功能的正确性和稳定性。
