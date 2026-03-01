# project_customers 表结构 - 精简实用版

## 一、你的观点分析 ✅

### 1. `is_active` vs `deleted_at`
**你的观点**: 软删除字段可以替代 is_active

**分析**:
- ✅ 对于简单场景，确实可以只用 `deleted_at`
- ⚠️ 但性能上有差异

**性能对比**:
```sql
-- 使用 deleted_at（你的方案）
SELECT * FROM project_customers 
WHERE project_id = 'P001' AND deleted_at IS NULL;
-- 性能：需要判断 NULL，索引效率较低

-- 使用 is_active
SELECT * FROM project_customers 
WHERE project_id = 'P001' AND is_active = 1;
-- 性能：等值查询，索引效率高
```

**结论**: 
- 如果数据量不大（< 10万条），用 `deleted_at` 完全够用 ✅
- 如果追求极致性能，建议两者都保留

### 2. `share_ratio` 份额比例
**你的观点**: 没有这个需求

**分析**: ✅ 完全同意！如果业务不需要，不要添加无用字段

### 3. `join_date` vs `created_at`
**你的观点**: join_date 就是创建时间

**分析**:
- ✅ 对于大多数场景，确实一样
- ⚠️ 但有细微差别：
  - `created_at`: 记录创建时间（数据库层面）
  - `join_date`: 业务加入时间（业务层面）
  - 通常情况下两者相同

**结论**: 可以只用 `created_at` ✅

### 4. `leave_date` 退出时间
**你的观点**: 没有必要

**分析**:
- ✅ 如果不需要记录退出时间，确实不需要
- ⚠️ 但如果需要审计（谁在什么时候退出），可能有用

**结论**: 根据业务需求决定，不强求

## 二、精简版表结构（推荐）⭐

根据你的需求，这是最精简实用的版本：

```sql
CREATE TABLE `project_customers` (
  -- 主键
  `id` VARCHAR(32) NOT NULL COMMENT '关联ID',
  
  -- 核心关联字段（必填）
  `project_id` VARCHAR(32) NOT NULL COMMENT '项目ID',
  `customer_id` VARCHAR(32) NOT NULL COMMENT '客户ID',
  
  -- 核心业务字段
  `role` VARCHAR(20) NOT NULL DEFAULT 'OWNER' COMMENT '客户角色（OWNER:业主、CO_OWNER:共同业主、INVESTOR:投资方、FAMILY:家庭成员）',
  `is_primary` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主客户（1:是 0:否）',
  
  -- 软删除字段
  `deleted_at` DATETIME NULL COMMENT '删除时间',
  
  -- 审计字段
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  
  -- 主键和索引
  PRIMARY KEY (`id`) USING BTREE,
  
  -- 唯一约束：防止重复添加
  UNIQUE KEY `uk_project_customer` (`project_id`, `customer_id`) USING BTREE,
  
  -- 单列索引
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_customer_id` (`customer_id`) USING BTREE,
  KEY `idx_is_primary` (`is_primary`) USING BTREE,
  
  -- 组合索引（优化常用查询）
  KEY `idx_project_deleted` (`project_id`, `deleted_at`) USING BTREE,
  KEY `idx_customer_deleted` (`customer_id`, `deleted_at`) USING BTREE
  
) ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci 
ROW_FORMAT=DYNAMIC 
COMMENT='项目客户关联表';
```

## 三、与你原始设计的对比

| 字段 | 你的原始设计 | 精简版设计 | 说明 |
|------|-------------|-----------|------|
| id | ✅ varchar(32) | ✅ varchar(32) | 保持不变 |
| project_id | ⚠️ NULL | ✅ NOT NULL | 必须改为 NOT NULL |
| user_id | ❌ | - | 删除 |
| customer_id | - | ✅ NOT NULL | 新增（替代 user_id） |
| role | ⚠️ varchar(50) | ✅ varchar(20) | 缩短长度 |
| is_primary | ❌ 缺失 | ✅ TINYINT(1) | 🔴 必须添加 |
| is_active | - | ❌ 不添加 | 按你的建议 |
| share_ratio | - | ❌ 不添加 | 按你的建议 |
| join_date | - | ❌ 不添加 | 按你的建议 |
| leave_date | - | ❌ 不添加 | 按你的建议 |
| deleted_at | ✅ | ✅ | 保留 |
| created_at | ✅ | ✅ | 保持不变 |
| updated_at | ✅ | ✅ | 保持不变 |
| created_by | ✅ | ✅ | 保持不变 |
| updated_by | ✅ | ✅ | 保持不变 |

## 四、必须修改的地方（最小改动）

基于你的原始设计，只需要做这些修改：

```sql
-- 1. 将 user_id 改为 customer_id
ALTER TABLE `project_customers` 
  CHANGE COLUMN `user_id` `customer_id` VARCHAR(32) NOT NULL COMMENT '客户ID';

-- 2. 修改 project_id 为 NOT NULL
ALTER TABLE `project_customers` 
  MODIFY COLUMN `project_id` VARCHAR(32) NOT NULL COMMENT '项目ID';

-- 3. 添加 is_primary 字段（最重要！）
ALTER TABLE `project_customers` 
  ADD COLUMN `is_primary` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主客户（1:是 0:否）' AFTER `role`;

-- 4. 修改 role 字段长度
ALTER TABLE `project_customers` 
  MODIFY COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'OWNER' COMMENT '客户角色';

-- 5. 添加唯一约束
ALTER TABLE `project_customers` 
  ADD UNIQUE KEY `uk_project_customer` (`project_id`, `customer_id`);

-- 6. 添加 is_primary 索引
ALTER TABLE `project_customers` 
  ADD KEY `idx_is_primary` (`is_primary`);

-- 7. 优化组合索引
ALTER TABLE `project_customers` 
  ADD KEY `idx_project_deleted` (`project_id`, `deleted_at`),
  ADD KEY `idx_customer_deleted` (`customer_id`, `deleted_at`);

-- 8. 删除不必要的索引
ALTER TABLE `project_customers` 
  DROP KEY `idx_role`,
  DROP KEY `idx_user_id`;
```

## 五、查询示例（使用 deleted_at）

### 5.1 查询项目的所有有效客户
```sql
SELECT * FROM project_customers 
WHERE project_id = 'P001' 
  AND deleted_at IS NULL
ORDER BY is_primary DESC, created_at ASC;
```

### 5.2 查询项目的主客户
```sql
SELECT * FROM project_customers 
WHERE project_id = 'P001' 
  AND is_primary = 1 
  AND deleted_at IS NULL
LIMIT 1;
```

### 5.3 检查客户是否有权访问项目
```sql
SELECT COUNT(*) > 0 FROM project_customers 
WHERE project_id = 'P001' 
  AND customer_id = 'C001' 
  AND deleted_at IS NULL;
```

### 5.4 查询客户的所有项目
```sql
SELECT * FROM project_customers 
WHERE customer_id = 'C001' 
  AND deleted_at IS NULL
ORDER BY is_primary DESC, created_at DESC;
```

## 六、Java 实体类（精简版）

```java
package com.ruoyi.web.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目客户关联对象 project_customers
 */
public class ProjectCustomers extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 关联ID */
    private String id;

    /** 项目ID */
    private String projectId;

    /** 客户ID */
    private String customerId;

    /** 客户角色 */
    private String role;

    /** 是否主客户 */
    private Boolean isPrimary;

    /** 删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deletedAt;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    /** 创建人 */
    private String createdBy;

    /** 更新人 */
    private String updatedBy;

    /** 关联的客户信息 */
    private Customers customer;

    // Getters and Setters
    // ... 省略
}
```

## 七、Mapper 查询（使用 deleted_at）

```xml
<!-- 查询项目的所有客户 -->
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

<!-- 查询项目的主客户 -->
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

<!-- 检查客户是否关联到项目 -->
<select id="checkCustomerInProject" resultType="boolean">
    SELECT COUNT(*) > 0
    FROM project_customers
    WHERE project_id = #{projectId} 
      AND customer_id = #{customerId}
      AND deleted_at IS NULL
</select>
```

## 八、性能优化建议

### 8.1 索引使用
```sql
-- 查询项目的客户（使用组合索引）
EXPLAIN SELECT * FROM project_customers 
WHERE project_id = 'P001' AND deleted_at IS NULL;
-- 使用索引: idx_project_deleted

-- 查询客户的项目（使用组合索引）
EXPLAIN SELECT * FROM project_customers 
WHERE customer_id = 'C001' AND deleted_at IS NULL;
-- 使用索引: idx_customer_deleted
```

### 8.2 如果数据量很大（> 100万条）
可以考虑添加 `is_active` 字段提升性能：

```sql
-- 添加 is_active 字段
ALTER TABLE `project_customers` 
  ADD COLUMN `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否有效' AFTER `is_primary`,
  ADD KEY `idx_is_active` (`is_active`);

-- 同步现有数据
UPDATE project_customers 
SET is_active = 0 
WHERE deleted_at IS NOT NULL;

-- 创建触发器自动同步
DELIMITER $$
CREATE TRIGGER sync_is_active_on_delete
BEFORE UPDATE ON project_customers
FOR EACH ROW
BEGIN
    IF NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL THEN
        SET NEW.is_active = 0;
    END IF;
END$$
DELIMITER ;
```

## 九、最终建议

### ✅ 推荐使用精简版（第二部分的表结构）

**核心改动**（相比你的原始设计）：
1. 🔴 `user_id` → `customer_id`（必须）
2. 🔴 添加 `is_primary` 字段（必须）
3. 🔴 添加唯一约束 `uk_project_customer`（必须）
4. 🔴 `project_id` 和 `customer_id` 改为 NOT NULL（必须）
5. 🟡 优化索引（建议）

**不添加的字段**（按你的建议）：
- ❌ `is_active`（用 deleted_at 替代）
- ❌ `share_ratio`（业务不需要）
- ❌ `join_date`（用 created_at 替代）
- ❌ `leave_date`（业务不需要）

### 📊 字段统计

| 类别 | 数量 | 字段 |
|------|------|------|
| 必须字段 | 5 | id, project_id, customer_id, role, is_primary |
| 软删除 | 1 | deleted_at |
| 审计字段 | 4 | created_at, updated_at, created_by, updated_by |
| **总计** | **10** | 精简实用 |

### 🎯 总结

你的观点很务实！精简版表结构：
- ✅ 保留核心功能
- ✅ 去除冗余字段
- ✅ 简化维护成本
- ✅ 满足业务需求

**最终建议**: 使用第二部分的精简版表结构 ⭐

---

**版本**: v2.0 精简版  
**适用场景**: 数据量 < 100万，追求简洁实用  
**核心改动**: 4个必须修改项
