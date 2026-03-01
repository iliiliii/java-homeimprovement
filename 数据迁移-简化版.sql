-- ============================================
-- 项目多客户关联 - 数据迁移 SQL（简化版）
-- 创建时间: 2026-03-01
-- 说明: 仅迁移历史数据，不创建触发器
--       数据同步由应用代码处理
-- ============================================

-- ============================================
-- 步骤 1: 历史数据迁移
-- ============================================

-- 1.1 迁移数据
INSERT INTO project_customers (
    id, 
    project_id, 
    customer_id, 
    role, 
    is_primary, 
    created_at, 
    created_by
)
SELECT 
    REPLACE(UUID(), '-', ''),
    p.id AS project_id,
    p.customer_id,
    'OWNER' AS role,
    1 AS is_primary,
    p.created_at,
    p.created_by
FROM projects p
WHERE 
    p.customer_id IS NOT NULL 
    AND p.deleted_at IS NULL
    AND NOT EXISTS (
        SELECT 1 
        FROM project_customers pc 
        WHERE pc.project_id = p.id 
          AND pc.customer_id = p.customer_id
          AND pc.deleted_at IS NULL
    );

-- 1.2 验证迁移结果
SELECT 
    '迁移前 projects 表中有 customer_id 的记录数' AS description,
    COUNT(*) AS count
FROM projects 
WHERE customer_id IS NOT NULL AND deleted_at IS NULL

UNION ALL

SELECT 
    '迁移后 project_customers 表中的记录数' AS description,
    COUNT(*) AS count
FROM project_customers 
WHERE deleted_at IS NULL

UNION ALL

SELECT 
    '未迁移的记录数（应该为0）' AS description,
    COUNT(*) AS count
FROM projects p
WHERE 
    p.customer_id IS NOT NULL 
    AND p.deleted_at IS NULL
    AND NOT EXISTS (
        SELECT 1 
        FROM project_customers pc 
        WHERE pc.project_id = p.id 
          AND pc.customer_id = p.customer_id
          AND pc.deleted_at IS NULL
    );

-- ============================================
-- 步骤 2: 数据一致性检查
-- ============================================

-- 2.1 检查缺少 project_customers 记录的项目
SELECT 
    p.id AS project_id,
    p.name AS project_name,
    p.customer_id,
    '缺少 project_customers 记录' AS issue
FROM projects p
WHERE 
    p.customer_id IS NOT NULL 
    AND p.deleted_at IS NULL
    AND NOT EXISTS (
        SELECT 1 
        FROM project_customers pc 
        WHERE pc.project_id = p.id 
          AND pc.customer_id = p.customer_id
          AND pc.deleted_at IS NULL
    );

-- 2.2 检查主客户是否一致
SELECT 
    p.id AS project_id,
    p.name AS project_name,
    p.customer_id AS projects_customer_id,
    pc.customer_id AS pc_customer_id,
    '主客户不一致' AS issue
FROM projects p
INNER JOIN project_customers pc ON p.id = pc.project_id
WHERE 
    p.deleted_at IS NULL
    AND pc.deleted_at IS NULL
    AND pc.is_primary = 1
    AND p.customer_id != pc.customer_id;

-- 2.3 检查是否有项目没有主客户
SELECT 
    p.id AS project_id,
    p.name AS project_name,
    '没有主客户' AS issue
FROM projects p
WHERE 
    p.deleted_at IS NULL
    AND p.customer_id IS NOT NULL
    AND NOT EXISTS (
        SELECT 1 
        FROM project_customers pc 
        WHERE pc.project_id = p.id 
          AND pc.is_primary = 1
          AND pc.deleted_at IS NULL
    );

-- 2.4 检查是否有项目有多个主客户
SELECT 
    pc.project_id,
    COUNT(*) AS primary_customer_count,
    '有多个主客户' AS issue
FROM project_customers pc
WHERE 
    pc.is_primary = 1
    AND pc.deleted_at IS NULL
GROUP BY pc.project_id
HAVING COUNT(*) > 1;

-- ============================================
-- 执行完成
-- ============================================
-- 请按照以下步骤验证：
-- 1. 检查迁移结果统计（步骤 1.2）
-- 2. 运行数据一致性检查（步骤 2）
-- 3. 如果发现问题，请手动修复
-- 4. 后续数据同步由应用代码自动处理
-- ============================================
