-- ============================================
-- 项目多客户关联 - 数据迁移与同步 SQL
-- 创建时间: 2026-03-01
-- 说明: 将 projects.customer_id 迁移到 project_customers 表
--       并创建触发器保持数据同步
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
-- 步骤 2: 创建触发器
-- ============================================

-- 2.1 删除旧触发器（如果存在）
DROP TRIGGER IF EXISTS trg_projects_customer_insert;
DROP TRIGGER IF EXISTS trg_projects_customer_update;
DROP TRIGGER IF EXISTS trg_projects_customer_delete;

-- 2.2 创建 INSERT 触发器
DELIMITER $$

CREATE TRIGGER trg_projects_customer_insert
AFTER INSERT ON projects
FOR EACH ROW
BEGIN
    IF NEW.customer_id IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1 
            FROM project_customers 
            WHERE project_id = NEW.id 
              AND customer_id = NEW.customer_id
              AND deleted_at IS NULL
        ) THEN
            INSERT INTO project_customers (
                id,
                project_id,
                customer_id,
                role,
                is_primary,
                created_at,
                created_by
            ) VALUES (
                REPLACE(UUID(), '-', ''),
                NEW.id,
                NEW.customer_id,
                'OWNER',
                1,
                NEW.created_at,
                NEW.created_by
            );
        END IF;
    END IF;
END$$

DELIMITER ;

-- 2.3 创建 UPDATE 触发器
DELIMITER $$

CREATE TRIGGER trg_projects_customer_update
AFTER UPDATE ON projects
FOR EACH ROW
BEGIN
    IF OLD.customer_id != NEW.customer_id OR (OLD.customer_id IS NULL AND NEW.customer_id IS NOT NULL) THEN
        
        IF OLD.customer_id IS NOT NULL THEN
            UPDATE project_customers
            SET deleted_at = NOW(),
                updated_at = NOW(),
                updated_by = NEW.updated_by
            WHERE project_id = NEW.id
              AND customer_id = OLD.customer_id
              AND deleted_at IS NULL;
        END IF;
        
        IF NEW.customer_id IS NOT NULL THEN
            IF NOT EXISTS (
                SELECT 1 
                FROM project_customers 
                WHERE project_id = NEW.id 
                  AND customer_id = NEW.customer_id
                  AND deleted_at IS NULL
            ) THEN
                INSERT INTO project_customers (
                    id,
                    project_id,
                    customer_id,
                    role,
                    is_primary,
                    created_at,
                    created_by
                ) VALUES (
                    REPLACE(UUID(), '-', ''),
                    NEW.id,
                    NEW.customer_id,
                    'OWNER',
                    1,
                    NOW(),
                    NEW.updated_by
                );
            ELSE
                UPDATE project_customers
                SET deleted_at = NULL,
                    updated_at = NOW(),
                    updated_by = NEW.updated_by,
                    is_primary = 1
                WHERE project_id = NEW.id
                  AND customer_id = NEW.customer_id
                  AND deleted_at IS NOT NULL;
            END IF;
        END IF;
    END IF;
END$$

DELIMITER ;

-- 2.4 创建 DELETE 触发器（软删除）
DELIMITER $$

CREATE TRIGGER trg_projects_customer_delete
AFTER UPDATE ON projects
FOR EACH ROW
BEGIN
    IF OLD.deleted_at IS NULL AND NEW.deleted_at IS NOT NULL THEN
        UPDATE project_customers
        SET deleted_at = NEW.deleted_at,
            updated_at = NOW(),
            updated_by = NEW.updated_by
        WHERE project_id = NEW.id
          AND deleted_at IS NULL;
    END IF;
    
    IF OLD.deleted_at IS NOT NULL AND NEW.deleted_at IS NULL THEN
        UPDATE project_customers
        SET deleted_at = NULL,
            updated_at = NOW(),
            updated_by = NEW.updated_by
        WHERE project_id = NEW.id
          AND deleted_at IS NOT NULL;
    END IF;
END$$

DELIMITER ;

-- ============================================
-- 步骤 3: 验证触发器
-- ============================================

-- 3.1 查看已创建的触发器
SHOW TRIGGERS WHERE `Table` = 'projects';

-- ============================================
-- 步骤 4: 数据一致性检查
-- ============================================

-- 4.1 检查缺少 project_customers 记录的项目
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

-- 4.2 检查主客户是否一致
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

-- 4.3 检查是否有项目没有主客户
SELECT 
    p.id AS project_id,
    p.name AS project_name,
    '没有主客户' AS issue
FROM projects p
WHERE 
    p.deleted_at IS NULL
    AND NOT EXISTS (
        SELECT 1 
        FROM project_customers pc 
        WHERE pc.project_id = p.id 
          AND pc.is_primary = 1
          AND pc.deleted_at IS NULL
    );

-- 4.4 检查是否有项目有多个主客户
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
-- 2. 查看触发器列表（步骤 3.1）
-- 3. 运行数据一致性检查（步骤 4）
-- 4. 如果发现问题，请参考《数据迁移与同步方案.md》
-- ============================================
