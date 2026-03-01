# project_customers 表结构分析与优化建议

## 一、当前表结构分析

### 你的表结构
```sql
CREATE TABLE `project_customers` (
  `id` varchar(32) NOT NULL COMMENT 'ID',
  `project_id` varchar(32) DEFAULT NULL COMMENT '项目ID（逻辑关联）',
  `user_id` varchar(32) DEFAULT NULL COMMENT '客户ID（逻辑关联）',
  `role` varchar(50) NOT NULL COMMENT '项目角色-默认都是CUS 全是客户',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人（添加成员的人）',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人（移除/修改的人）',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目客户关联表';
```

## 二、存在的问题 ⚠️

### 问题1: 缺少关键字段 🔴 严重

#### 1.1 缺少 `is_primary` 字段
**问题**: 无法区分主客户和次客户

**影响**:
- 无法标识哪个客户是主客户
- 无法实现主客户优先显示
- 无法限制"只能有一个主客户"的业务规则
- 与 `projects.customer_id` 无法对应

**必须添加**:
```sql
`is_primary` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主客户（1:是 0:否）',
```

#### 1.2 缺少 `is_active` 字段
**问题**: 使用 `deleted_at` 软删除，但缺少状态标识

**影响**:
- 查询时需要判断 `deleted_at IS NULL`，性能较差
- 无法快速过滤有效记录
- 索引效率低

**建议添加**:
```sql
`is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否有效（1:有效 0:无效）',
```

#### 1.3 缺少业务字段
**问题**: 缺少份额比例、加入时间等业务字段

**建议添加**:
```sql
`share_ratio` DECIMAL(5,2) NULL COMMENT '份额比例（%）',
`join_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
`leave_date` DATETIME NULL COMMENT '退出时间',
`remarks` TEXT COMMENT '备注',
```

### 问题2: 字段命名不一致 🟡 中等

#### 2.1 `user_id` vs `customer_id`
**问题**: 
- 你使用 `user_id`，但实际应该是 `customer_id`
- 与 `customers` 表的 `id` 字段对应
- 与 `projects.customer_id` 命名不一致

**建议**: 改为 `customer_id`
```sql
`customer_id` varchar(32) DEFAULT NULL COMMENT '客户ID（逻辑关联）',
```

#### 2.2 `role` 字段设计不合理
**问题**:
- 注释说"默认都是CUS 全是客户"
- 但实际应该区分不同的客户角色（业主、共同业主、投资方等）
- `varchar(50)` 太长，浪费空间

**建议**: 改为 `varchar(20)` 并明确角色类型
```sql
`role` VARCHAR(20) NOT NULL DEFAULT 'OWNER' COMMENT '客户角色（OWNER:业主、CO_OWNER:共同业主、INVESTOR:投资方、FAMILY:家庭成员）',
```

### 问题3: 缺少唯一约束 🔴 严重

**问题**: 没有防止同一客户被重复添加到同一项目

**影响**:
- 可能出现重复数据
- 数据完整性无法保证

**必须添加**:
```sql
UNIQUE KEY `uk_project_customer` (`project_id`, `customer_id`)
```

### 问题4: 索引设计不合理 🟡 中等

**问题**: 
- `idx_role` 索引意义不大（role 字段区分度低）
- 缺少 `is_primary` 和 `is_active` 的索引

**建议**:
```sql
-- 删除 role 索引
-- DROP KEY `idx_role`;

-- 添加有用的索引
KEY `idx_is_primary` (`is_primary`),
KEY `idx_is_active` (`is_active`),
KEY `idx_project_active` (`project_id`, `is_active`),  -- 组合索引，查询项目的有效客户
KEY `idx_customer_active` (`customer_id`, `is_active`) -- 组合索引，查询客户的有效项目
```

### 问题5: 字段可空性设计不当 🟡 中等

**问题**: `project_id` 和 `customer_id` 允许为 NULL

**影响**:
- 关联表的核心字段不应该为空
- 可能导致脏数据

**建议**: 改为 NOT NULL
```sql
`project_id` varchar(32) NOT NULL COMMENT '项目ID',
`customer_id` varchar(32) NOT NULL COMMENT '客户ID',
```

## 三、优化后的完整表结构 ✅

```sql
CREATE TABLE `project_customers` (
  -- 主键
  `id` VARCHAR(32) NOT NULL COMMENT '关联ID',
  
  -- 核心关联字段（必填）
  `project_id` VARCHAR(32) NOT NULL COMMENT '项目ID',
  `customer_id` VARCHAR(32) NOT NULL COMMENT '客户ID',
  
  -- 业务字段
  `role` VARCHAR(20) NOT NULL DEFAULT 'OWNER' COMMENT '客户角色（OWNER:业主、CO_OWNER:共同业主、INVESTOR:投资方、FAMILY:家庭成员）',
  `is_primary` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主客户（1:是 0:否）',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否有效（1:有效 0:无效）',
  `share_ratio` DECIMAL(5,2) NULL COMMENT '份额比例（%）',
  `join_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `leave_date` DATETIME NULL COMMENT '退出时间',
  `remarks` TEXT COMMENT '备注',
  
  -- 软删除字段（可选，如果使用 is_active 可以不要）
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
  KEY `idx_is_active` (`is_active`) USING BTREE,
  
  -- 组合索引（优化常用查询）
  KEY `idx_project_active` (`project_id`, `is_active`) USING BTREE,
  KEY `idx_customer_active` (`customer_id`, `is_active`) USING BTREE
  
) ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci 
ROW_FORMAT=DYNAMIC 
COMMENT='项目客户关联表';
```

## 四、字段对比表

| 字段 | 你的设计 | 推荐设计 | 重要性 | 说明 |
|------|---------|---------|--------|------|
| id | ✅ varchar(32) | ✅ varchar(32) | 必须 | 主键 |
| project_id | ⚠️ NULL | ✅ NOT NULL | 必须 | 不应为空 |
| user_id | ❌ | - | - | 应改为 customer_id |
| customer_id | - | ✅ NOT NULL | 必须 | 客户ID |
| role | ⚠️ varchar(50) | ✅ varchar(20) | 必须 | 缩短长度 |
| is_primary | ❌ 缺失 | ✅ TINYINT(1) | 🔴 必须 | 标识主客户 |
| is_active | ❌ 缺失 | ✅ TINYINT(1) | 🔴 必须 | 状态标识 |
| share_ratio | ❌ 缺失 | ✅ DECIMAL(5,2) | 建议 | 份额比例 |
| join_date | ❌ 缺失 | ✅ DATETIME | 建议 | 加入时间 |
| leave_date | ❌ 缺失 | ✅ DATETIME | 建议 | 退出时间 |
| remarks | ❌ 缺失 | ✅ TEXT | 可选 | 备注 |
| deleted_at | ✅ | ⚠️ 可选 | 可选 | 有 is_active 可不要 |
| created_at | ✅ | ✅ | 必须 | 审计字段 |
| updated_at | ✅ | ✅ | 必须 | 审计字段 |
| created_by | ✅ | ✅ | 必须 | 审计字段 |
| updated_by | ✅ | ✅ | 必须 | 审计字段 |

## 五、索引对比

| 索引 | 你的设计 | 推荐设计 | 说明 |
|------|---------|---------|------|
| PRIMARY KEY | ✅ id | ✅ id | 主键 |
| UNIQUE KEY | ❌ 缺失 | ✅ (project_id, customer_id) | 🔴 防止重复 |
| idx_project_id | ✅ | ✅ | 查询项目的客户 |
| idx_user_id | ⚠️ | - | 应改为 idx_customer_id |
| idx_customer_id | - | ✅ | 查询客户的项目 |
| idx_role | ⚠️ 意义不大 | ❌ 删除 | 区分度低 |
| idx_is_primary | ❌ 缺失 | ✅ | 查询主客户 |
| idx_is_active | ❌ 缺失 | ✅ | 过滤有效记录 |
| idx_project_active | ❌ 缺失 | ✅ | 组合索引优化 |
| idx_customer_active | ❌ 缺失 | ✅ | 组合索引优化 |

## 六、关键业务逻辑验证

### 6.1 查询项目的所有客户
```sql
-- ✅ 优化后的查询（使用 is_active）
SELECT * FROM project_customers 
WHERE project_id = 'P001' AND is_active = 1
ORDER BY is_primary DESC, join_date ASC;

-- ⚠️ 你的设计需要这样查询（性能较差）
SELECT * FROM project_customers 
WHERE project_id = 'P001' AND deleted_at IS NULL
ORDER BY created_at ASC;
```

### 6.2 查询项目的主客户
```sql
-- ✅ 优化后的查询
SELECT * FROM project_customers 
WHERE project_id = 'P001' AND is_primary = 1 AND is_active = 1
LIMIT 1;

-- ❌ 你的设计无法实现（缺少 is_primary 字段）
```

### 6.3 检查客户是否有权访问项目
```sql
-- ✅ 优化后的查询
SELECT COUNT(*) > 0 FROM project_customers 
WHERE project_id = 'P001' 
  AND customer_id = 'C001' 
  AND is_active = 1;

-- ⚠️ 你的设计
SELECT COUNT(*) > 0 FROM project_customers 
WHERE project_id = 'P001' 
  AND user_id = 'C001' 
  AND deleted_at IS NULL;
```

### 6.4 防止重复添加
```sql
-- ✅ 优化后：唯一约束自动防止
INSERT INTO project_customers (id, project_id, customer_id, ...) 
VALUES ('xxx', 'P001', 'C001', ...);
-- 如果已存在，会报错：Duplicate entry

-- ❌ 你的设计：需要手动检查
SELECT COUNT(*) FROM project_customers 
WHERE project_id = 'P001' AND user_id = 'C001' AND deleted_at IS NULL;
-- 如果 > 0，则不插入
```

## 七、数据迁移脚本

如果你已经有数据，需要添加新字段：

```sql
-- 1. 添加缺失的字段
ALTER TABLE `project_customers` 
  ADD COLUMN `is_primary` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主客户' AFTER `role`,
  ADD COLUMN `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否有效' AFTER `is_primary`,
  ADD COLUMN `share_ratio` DECIMAL(5,2) NULL COMMENT '份额比例（%）' AFTER `is_active`,
  ADD COLUMN `join_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间' AFTER `share_ratio`,
  ADD COLUMN `leave_date` DATETIME NULL COMMENT '退出时间' AFTER `join_date`,
  ADD COLUMN `remarks` TEXT COMMENT '备注' AFTER `leave_date`;

-- 2. 修改字段名称
ALTER TABLE `project_customers` 
  CHANGE COLUMN `user_id` `customer_id` VARCHAR(32) NOT NULL COMMENT '客户ID';

-- 3. 修改字段属性
ALTER TABLE `project_customers` 
  MODIFY COLUMN `project_id` VARCHAR(32) NOT NULL COMMENT '项目ID',
  MODIFY COLUMN `customer_id` VARCHAR(32) NOT NULL COMMENT '客户ID',
  MODIFY COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'OWNER' COMMENT '客户角色';

-- 4. 添加唯一约束
ALTER TABLE `project_customers` 
  ADD UNIQUE KEY `uk_project_customer` (`project_id`, `customer_id`);

-- 5. 添加索引
ALTER TABLE `project_customers` 
  ADD KEY `idx_is_primary` (`is_primary`),
  ADD KEY `idx_is_active` (`is_active`),
  ADD KEY `idx_project_active` (`project_id`, `is_active`),
  ADD KEY `idx_customer_active` (`customer_id`, `is_active`);

-- 6. 删除不必要的索引
ALTER TABLE `project_customers` 
  DROP KEY `idx_role`,
  DROP KEY `idx_user_id`;

-- 7. 同步现有数据的 is_active 状态
UPDATE project_customers 
SET is_active = 0 
WHERE deleted_at IS NOT NULL;

-- 8. 设置主客户标记（根据 projects 表的 customer_id）
UPDATE project_customers pc
INNER JOIN projects p ON pc.project_id = p.id
SET pc.is_primary = 1
WHERE pc.customer_id = p.customer_id;
```

## 八、建议的实施步骤

### 方案A: 直接使用优化后的表结构（推荐）⭐
如果还没有创建表或数据很少：
```sql
-- 直接使用完整的优化后表结构
DROP TABLE IF EXISTS `project_customers`;
-- 然后执行第三部分的完整建表语句
```

### 方案B: 渐进式修改
如果已经有数据或正在使用：
1. 先添加新字段（不影响现有功能）
2. 修改应用代码适配新字段
3. 数据迁移和验证
4. 添加约束和索引
5. 删除旧字段或索引

## 九、总结与建议

### 🔴 必须修改的问题（P0）
1. ✅ 添加 `is_primary` 字段 - 区分主客户
2. ✅ 添加 `is_active` 字段 - 状态标识
3. ✅ 将 `user_id` 改为 `customer_id` - 命名一致性
4. ✅ 添加唯一约束 `uk_project_customer` - 防止重复
5. ✅ `project_id` 和 `customer_id` 改为 NOT NULL - 数据完整性

### 🟡 建议修改的问题（P1）
6. ✅ 添加 `share_ratio`、`join_date`、`leave_date` 等业务字段
7. ✅ 优化索引设计
8. ✅ 缩短 `role` 字段长度

### 🟢 可选优化（P2）
9. 考虑是否保留 `deleted_at`（有 `is_active` 可能不需要）
10. 添加更多业务字段（根据实际需求）

### 最终建议
**不建议使用你当前的表结构**，建议使用第三部分提供的优化后的完整表结构。

主要原因：
1. 缺少关键字段（is_primary、is_active）会导致功能无法实现
2. 缺少唯一约束会导致数据重复
3. 字段命名不一致会增加维护成本
4. 索引设计不合理会影响查询性能

---

**评估结果**: ❌ 不可行，需要重大修改  
**建议**: 使用优化后的表结构  
**优先级**: 🔴 高优先级
