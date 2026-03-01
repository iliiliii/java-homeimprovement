# SQL语法错误修复说明

**修复时间**: 2026-03-01  
**问题**: 客户关联更新失败 - SQL语法错误

---

## ❌ 错误信息

```
### Error updating database. 
Cause: java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax; 
check the manual that corresponds to your MySQL server version for the right syntax 
to use near 'UPDATE project_customers SET is_primary = 1, updated_at = NOW() ' at line 5

### SQL: 
UPDATE project_customers SET is_primary = 0, updated_at = NOW() 
WHERE project_id = ? AND deleted_at IS NULL;

UPDATE project_customers SET is_primary = 1, updated_at = NOW() 
WHERE project_id = ? AND customer_id = ? AND deleted_at IS NULL;
```

---

## 🔍 问题分析

### 根本原因

MyBatis 默认不支持在单个 `<update>` 标签中执行多条 SQL 语句。

**原 Mapper XML 代码**:
```xml
<!-- 设置主客户 -->
<update id="setPrimaryCustomer">
    UPDATE project_customers 
    SET is_primary = 0, updated_at = NOW()
    WHERE project_id = #{projectId} AND deleted_at IS NULL;
    
    UPDATE project_customers
    SET is_primary = 1, updated_at = NOW()
    WHERE project_id = #{projectId} AND customer_id = #{customerId} AND deleted_at IS NULL;
</update>
```

### 为什么会出错？

1. MyBatis 的 `<update>` 标签只能执行单条 SQL 语句
2. 多条语句需要使用 JDBC 的 `allowMultiQueries=true` 参数，但这不是推荐做法
3. 更好的方式是拆分成多个独立的方法

---

## ✅ 修复方案

### 方案：拆分为两个独立的方法

将原来的一个方法拆分为两个：
1. `clearPrimaryCustomers` - 清除所有主客户标记
2. `setPrimaryCustomer` - 设置新的主客户

---

## 📝 修复内容

### 1. Mapper XML 修改

**文件**: `sb3/evs-home/src/main/resources/mapper/evs/ProjectCustomersMapper.xml`

**修改前**:
```xml
<!-- 设置主客户 -->
<update id="setPrimaryCustomer">
    UPDATE project_customers 
    SET is_primary = 0, updated_at = NOW()
    WHERE project_id = #{projectId} AND deleted_at IS NULL;
    
    UPDATE project_customers
    SET is_primary = 1, updated_at = NOW()
    WHERE project_id = #{projectId} AND customer_id = #{customerId} AND deleted_at IS NULL;
</update>
```

**修改后**:
```xml
<!-- 清除项目的所有主客户标记 -->
<update id="clearPrimaryCustomers">
    UPDATE project_customers 
    SET is_primary = 0, updated_at = NOW()
    WHERE project_id = #{projectId} AND deleted_at IS NULL
</update>

<!-- 设置主客户 -->
<update id="setPrimaryCustomer">
    UPDATE project_customers
    SET is_primary = 1, updated_at = NOW()
    WHERE project_id = #{projectId} AND customer_id = #{customerId} AND deleted_at IS NULL
</update>
```

---

### 2. Mapper 接口修改

**文件**: `sb3/evs-home/src/main/java/com/ruoyi/web/mapper/ProjectCustomersMapper.java`

**新增方法**:
```java
/**
 * 清除项目的所有主客户标记
 * 
 * @param projectId 项目ID
 * @return 影响行数
 */
public int clearPrimaryCustomers(@Param("projectId") String projectId);

/**
 * 设置主客户（同时取消其他主客户）
 * 
 * @param projectId 项目ID
 * @param customerId 客户ID
 * @return 影响行数
 */
public int setPrimaryCustomer(@Param("projectId") String projectId, 
                              @Param("customerId") String customerId);
```

---

### 3. Service 实现修改

**文件**: `sb3/evs-home/src/main/java/com/ruoyi/web/service/impl/ProjectCustomersServiceImpl.java`

**修改前**:
```java
// 设置主客户（会自动取消其他主客户）
int result = projectCustomersMapper.setPrimaryCustomer(projectId, customerId);
```

**修改后**:
```java
// 1. 先清除所有主客户标记
projectCustomersMapper.clearPrimaryCustomers(projectId);

// 2. 设置新的主客户
int result = projectCustomersMapper.setPrimaryCustomer(projectId, customerId);
```

---

### 4. ProjectsServiceImpl 修改

**文件**: `sb3/evs-home/src/main/java/com/ruoyi/web/service/impl/ProjectsServiceImpl.java`

**修改位置**: `syncProjectCustomer` 方法中的 "update" 操作

**修改前**:
```java
} else {
    // 如果已存在，设置为主客户
    projectCustomersMapper.setPrimaryCustomer(projectId, customerId);
    log.info("同步设置主客户：projectId={}, customerId={}", projectId, customerId);
}
```

**修改后**:
```java
} else {
    // 如果已存在，设置为主客户
    projectCustomersMapper.clearPrimaryCustomers(projectId);
    projectCustomersMapper.setPrimaryCustomer(projectId, customerId);
    log.info("同步设置主客户：projectId={}, customerId={}", projectId, customerId);
}
```

---

## 🔄 新的执行流程

### 设置主客户的流程

```
Service 层调用
  ↓
1. clearPrimaryCustomers(projectId)
   ↓
   UPDATE project_customers 
   SET is_primary = 0
   WHERE project_id = ? AND deleted_at IS NULL
  ↓
2. setPrimaryCustomer(projectId, customerId)
   ↓
   UPDATE project_customers 
   SET is_primary = 1
   WHERE project_id = ? AND customer_id = ? AND deleted_at IS NULL
  ↓
3. 同步更新 projects.customer_id
   ↓
完成
```

---

## ✅ 优点

### 1. 符合 MyBatis 规范
- 每个 `<update>` 标签只执行一条 SQL
- 不需要特殊的 JDBC 配置

### 2. 更好的事务控制
- 两个操作在同一个 `@Transactional` 方法中
- 任何一步失败都会回滚

### 3. 更清晰的职责分离
- `clearPrimaryCustomers` - 清除标记
- `setPrimaryCustomer` - 设置标记
- 每个方法职责单一

### 4. 更好的可测试性
- 可以单独测试每个方法
- 便于调试和排查问题

---

## 🧪 测试验证

### 编译测试

```bash
cd sb3/evs-home
mvn clean compile -DskipTests
```

**结果**: ✅ BUILD SUCCESS

---

### 功能测试

#### 测试 1: 编辑项目修改客户

1. 打开项目编辑对话框
2. 修改客户列表（添加或删除客户）
3. 修改主客户
4. 点击"确定"

**预期结果**:
- ✅ 提示"修改成功"
- ✅ 不再出现 SQL 语法错误
- ✅ 主客户正确更新

---

#### 测试 2: 查询数据库验证

```sql
-- 查询项目的所有客户
SELECT * FROM project_customers 
WHERE project_id = 'xxx' AND deleted_at IS NULL;
```

**预期结果**:
```
| id  | project_id | customer_id | is_primary |
|-----|------------|-------------|------------|
| 1   | xxx        | 客户1       | 0          |
| 2   | xxx        | 客户2       | 1          | ← 只有一个主客户
| 3   | xxx        | 客户3       | 0          |
```

---

## 📊 修复效果对比

### 修复前

```
编辑项目 → 修改客户 → 保存
  ↓
SQL 语法错误 ❌
客户关联更新失败 ❌
```

### 修复后

```
编辑项目 → 修改客户 → 保存
  ↓
清除所有主客户标记 ✅
设置新的主客户 ✅
同步更新 projects.customer_id ✅
保存成功 ✅
```

---

## 📋 修改文件清单

1. ✅ `sb3/evs-home/src/main/resources/mapper/evs/ProjectCustomersMapper.xml`
   - 拆分 `setPrimaryCustomer` 为两个方法

2. ✅ `sb3/evs-home/src/main/java/com/ruoyi/web/mapper/ProjectCustomersMapper.java`
   - 新增 `clearPrimaryCustomers` 方法声明

3. ✅ `sb3/evs-home/src/main/java/com/ruoyi/web/service/impl/ProjectCustomersServiceImpl.java`
   - 修改 `setPrimaryCustomer` 方法实现

4. ✅ `sb3/evs-home/src/main/java/com/ruoyi/web/service/impl/ProjectsServiceImpl.java`
   - 修改 `syncProjectCustomer` 方法

---

## ⚠️ 注意事项

### 1. 事务一致性

两个 UPDATE 操作必须在同一个事务中执行，确保：
- 要么都成功
- 要么都回滚

**已处理**: 使用 `@Transactional` 注解保证事务一致性

---

### 2. 并发控制

如果多个用户同时修改同一个项目的主客户，可能出现竞态条件。

**建议**:
- 使用乐观锁（version 字段）
- 或者在数据库层面添加唯一约束

**当前状态**: 暂未处理，后续可优化

---

### 3. 性能考虑

每次设置主客户需要执行两条 UPDATE 语句。

**影响**: 
- 对于单个项目操作，性能影响可忽略
- 如果需要批量设置主客户，可以考虑优化

**当前状态**: 性能可接受

---

## ✅ 总结

### 修复内容
1. ✅ 拆分 `setPrimaryCustomer` 为两个独立方法
2. ✅ 修改 Mapper 接口和 XML
3. ✅ 更新 Service 层调用逻辑
4. ✅ 编译通过

### 优点
- ✅ 符合 MyBatis 规范
- ✅ 事务控制更好
- ✅ 职责分离更清晰
- ✅ 便于测试和维护

### 测试
- ✅ 编译测试通过
- ⏳ 需要功能测试验证

---

**修复时间**: 2026-03-01  
**状态**: ✅ 代码已修复，编译通过，待功能测试
