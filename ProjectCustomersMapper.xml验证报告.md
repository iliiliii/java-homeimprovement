# ProjectCustomersMapper.xml 验证报告

**验证时间**: 2026-03-01  
**文件路径**: `sb3/evs-home/src/main/resources/mapper/evs/ProjectCustomersMapper.xml`

---

## ✅ 已完成部分

### 1. 基础结构 ✅
- ✅ XML 声明和 DOCTYPE 正确
- ✅ namespace 配置正确：`com.ruoyi.web.mapper.ProjectCustomersMapper`
- ✅ 基础 resultMap 配置完整

### 2. 已实现的 SQL 方法（6个）

| 方法 | 类型 | 状态 | 说明 |
|------|------|------|------|
| selectProjectCustomersList | SELECT | ✅ | 条件查询列表 |
| selectProjectCustomersById | SELECT | ✅ | 根据ID查询 |
| insertProjectCustomers | INSERT | ✅ | 插入单条记录 |
| updateProjectCustomers | UPDATE | ✅ | 更新记录 |
| deleteProjectCustomersById | DELETE | ⚠️ | 硬删除，应改为软删除 |
| deleteProjectCustomersByIds | DELETE | ⚠️ | 批量硬删除，应改为软删除 |

---

## ❌ 缺失部分

### 1. 缺少关键结果映射

```xml
<!-- ❌ 缺少：包含客户信息的结果映射 -->
<resultMap type="ProjectCustomers" id="ProjectCustomersWithCustomerResult" 
           extends="ProjectCustomersResult">
    <association property="customer" javaType="Customers">
        <id property="id" column="customer_id"/>
        <result property="name" column="customer_name"/>
        <result property="phone" column="customer_phone"/>
        <result property="email" column="customer_email"/>
        <result property="avatar" column="customer_avatar"/>
    </association>
</resultMap>
```

**用途**: 用于关联查询时返回客户详细信息

---

### 2. 缺少核心业务 SQL（9个）

#### ❌ 2.1 查询项目的所有客户（带客户信息）

```xml
<select id="selectByProjectId" resultMap="ProjectCustomersWithCustomerResult">
    SELECT 
        pc.id, pc.project_id, pc.customer_id, pc.role, pc.is_primary,
        pc.deleted_at, pc.created_at, pc.updated_at, pc.created_by, pc.updated_by,
        c.name as customer_name, c.phone as customer_phone,
        c.email as customer_email, c.avatar as customer_avatar
    FROM project_customers pc
    LEFT JOIN customers c ON pc.customer_id = c.id
    WHERE pc.project_id = #{projectId} AND pc.deleted_at IS NULL
    ORDER BY pc.is_primary DESC, pc.created_at ASC
</select>
```

**重要性**: 🔴 核心功能  
**用途**: 查询项目的所有关联客户，按主客户优先排序

---

#### ❌ 2.2 查询客户的所有项目

```xml
<select id="selectByCustomerId" resultMap="ProjectCustomersResult">
    SELECT *
    FROM project_customers
    WHERE customer_id = #{customerId} AND deleted_at IS NULL
    ORDER BY is_primary DESC, created_at DESC
</select>
```

**重要性**: 🟡 常用功能  
**用途**: 查询客户参与的所有项目

---

#### ❌ 2.3 查询项目的主客户

```xml
<select id="selectPrimaryByProjectId" resultMap="ProjectCustomersWithCustomerResult">
    SELECT 
        pc.id, pc.project_id, pc.customer_id, pc.role, pc.is_primary,
        pc.deleted_at, pc.created_at, pc.updated_at,
        c.name as customer_name, c.phone as customer_phone,
        c.email as customer_email, c.avatar as customer_avatar
    FROM project_customers pc
    LEFT JOIN customers c ON pc.customer_id = c.id
    WHERE pc.project_id = #{projectId} 
      AND pc.is_primary = 1 
      AND pc.deleted_at IS NULL
    LIMIT 1
</select>
```

**重要性**: 🔴 核心功能  
**用途**: 获取项目的主客户信息（用于权限验证、数据展示等）

---

#### ❌ 2.4 检查客户是否关联到项目

```xml
<select id="checkCustomerInProject" resultType="boolean">
    SELECT COUNT(*) > 0
    FROM project_customers
    WHERE project_id = #{projectId} 
      AND customer_id = #{customerId}
      AND deleted_at IS NULL
</select>
```

**重要性**: 🔴 核心功能  
**用途**: 权限验证的基础方法，检查客户是否有权访问项目

---

#### ❌ 2.5 批量插入

```xml
<insert id="batchInsert">
    INSERT INTO project_customers (
        id, project_id, customer_id, role, is_primary,
        created_at, created_by
    ) VALUES
    <foreach collection="list" item="item" separator=",">
        (
            #{item.id}, #{item.projectId}, #{item.customerId}, 
            #{item.role}, #{item.isPrimary},
            #{item.createdAt}, #{item.createdBy}
        )
    </foreach>
</insert>
```

**重要性**: 🟡 性能优化  
**用途**: 批量添加多个客户到项目，提高性能

---

#### ❌ 2.6 软删除

```xml
<update id="softDelete">
    UPDATE project_customers
    SET deleted_at = NOW(), updated_at = NOW()
    WHERE id = #{id}
</update>
```

**重要性**: 🔴 核心功能  
**用途**: 软删除客户关联（保留历史记录）

---

#### ❌ 2.7 设置主客户

```xml
<update id="setPrimaryCustomer">
    <!-- 先取消该项目的所有主客户标记 -->
    UPDATE project_customers 
    SET is_primary = 0, updated_at = NOW()
    WHERE project_id = #{projectId};
    
    <!-- 再设置新的主客户 -->
    UPDATE project_customers
    SET is_primary = 1, updated_at = NOW()
    WHERE project_id = #{projectId} AND customer_id = #{customerId};
</update>
```

**重要性**: 🔴 核心功能  
**用途**: 设置项目的主客户（确保只有一个主客户）

---

#### ❌ 2.8 统计项目客户数量

```xml
<select id="countByProjectId" resultType="int">
    SELECT COUNT(*)
    FROM project_customers
    WHERE project_id = #{projectId} AND deleted_at IS NULL
</select>
```

**重要性**: 🟡 常用功能  
**用途**: 统计项目有多少个客户

---

#### ❌ 2.9 批量统计项目客户数量

```xml
<select id="countByProjectIds" resultType="java.util.Map">
    SELECT 
        project_id as projectId,
        COUNT(*) as customerCount
    FROM project_customers
    WHERE project_id IN
    <foreach collection="projectIds" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
    AND deleted_at IS NULL
    GROUP BY project_id
</select>
```

**重要性**: 🟡 性能优化  
**用途**: 批量统计多个项目的客户数量（用于列表展示）

---

## ⚠️ 需要修改的部分

### 1. selectProjectCustomersList - 缺少软删除过滤

**当前代码**:
```xml
<select id="selectProjectCustomersList" parameterType="ProjectCustomers" resultMap="ProjectCustomersResult">
    <include refid="selectProjectCustomersVo"/>
    <where>  
        <if test="projectId != null  and projectId != ''"> and project_id = #{projectId}</if>
        <!-- ... 其他条件 ... -->
    </where>
</select>
```

**问题**: 没有过滤 `deleted_at IS NULL`，会查询到已删除的记录

**建议修改**:
```xml
<select id="selectProjectCustomersList" parameterType="ProjectCustomers" resultMap="ProjectCustomersResult">
    <include refid="selectProjectCustomersVo"/>
    <where>
        deleted_at IS NULL  <!-- 添加这一行 -->
        <if test="projectId != null  and projectId != ''"> and project_id = #{projectId}</if>
        <!-- ... 其他条件 ... -->
    </where>
</select>
```

---

### 2. deleteProjectCustomersById - 应改为软删除

**当前代码**:
```xml
<delete id="deleteProjectCustomersById" parameterType="String">
    delete from project_customers where id = #{id}
</delete>
```

**问题**: 硬删除会丢失历史数据

**建议修改**:
```xml
<update id="deleteProjectCustomersById" parameterType="String">
    UPDATE project_customers
    SET deleted_at = NOW(), updated_at = NOW()
    WHERE id = #{id}
</update>
```

---

### 3. deleteProjectCustomersByIds - 应改为批量软删除

**当前代码**:
```xml
<delete id="deleteProjectCustomersByIds" parameterType="String">
    delete from project_customers where id in 
    <foreach item="id" collection="array" open="(" separator="," close=")">
        #{id}
    </foreach>
</delete>
```

**建议修改**:
```xml
<update id="deleteProjectCustomersByIds" parameterType="String">
    UPDATE project_customers
    SET deleted_at = NOW(), updated_at = NOW()
    WHERE id IN
    <foreach item="id" collection="array" open="(" separator="," close=")">
        #{id}
    </foreach>
</update>
```

---

## 📊 完成度统计

### SQL 方法统计

| 类型 | 应有数量 | 已完成 | 缺失 | 完成率 |
|------|---------|--------|------|--------|
| SELECT | 7 | 2 | 5 | 28.6% |
| INSERT | 2 | 1 | 1 | 50% |
| UPDATE | 4 | 1 | 3 | 25% |
| DELETE | 2 | 2 | 0 | 100%（但需改为软删除） |
| **总计** | **15** | **6** | **9** | **40%** |

### 功能完成度

| 功能模块 | 完成度 | 说明 |
|---------|--------|------|
| 基础 CRUD | 80% | 已完成，但删除应改为软删除 |
| 项目客户查询 | 0% | 缺少 selectByProjectId |
| 客户项目查询 | 0% | 缺少 selectByCustomerId |
| 主客户管理 | 0% | 缺少 selectPrimaryByProjectId, setPrimaryCustomer |
| 权限检查 | 0% | 缺少 checkCustomerInProject |
| 批量操作 | 0% | 缺少 batchInsert |
| 统计功能 | 0% | 缺少 countByProjectId, countByProjectIds |
| **总体** | **35%** | 只有基础 CRUD，核心业务功能缺失 |

---

## 🎯 优先级建议

### P0 - 必须立即补充（阻塞核心功能）

1. ✅ **checkCustomerInProject** - 权限验证的基础
2. ✅ **selectByProjectId** - 查询项目客户
3. ✅ **selectPrimaryByProjectId** - 查询主客户
4. ✅ **setPrimaryCustomer** - 设置主客户
5. ✅ **softDelete** - 软删除

### P1 - 重要功能（影响用户体验）

6. ✅ **selectByCustomerId** - 查询客户项目
7. ✅ **batchInsert** - 批量添加客户

### P2 - 优化功能（提升性能）

8. ✅ **countByProjectId** - 统计客户数量
9. ✅ **countByProjectIds** - 批量统计

### P3 - 修复现有问题

10. ⚠️ 修改 selectProjectCustomersList 添加软删除过滤
11. ⚠️ 修改 deleteProjectCustomersById 改为软删除
12. ⚠️ 修改 deleteProjectCustomersByIds 改为批量软删除

---

## 📝 下一步行动

### 立即执行

1. **补充 ProjectCustomersWithCustomerResult 结果映射**（5分钟）
2. **补充 9 个核心业务 SQL**（1小时）
3. **修改 3 个现有方法**（15分钟）
4. **同步更新 Mapper 接口**（30分钟）

### 预计总时间

- XML 补充和修改：1.5小时
- Mapper 接口补充：0.5小时
- **总计：2小时**

---

## ✅ 验证结论

**当前状态**: ⚠️ 文件已创建，但严重不完整

**完成度**: 35%（只有基础 CRUD）

**主要问题**:
1. 缺少 9 个核心业务 SQL
2. 缺少关联查询的结果映射
3. 删除方法应改为软删除
4. 查询列表缺少软删除过滤

**影响**:
- 核心业务功能无法实现
- Mapper 接口方法调用会报错
- 权限验证无法进行
- 多客户功能无法使用

**建议**: 按照优先级立即补充缺失的 SQL 配置

---

**验证完成时间**: 2026-03-01  
**验证人**: AI Assistant
