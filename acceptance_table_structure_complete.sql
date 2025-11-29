-- ==========================================
-- 项目进度记录表 - 完整结构定义（验收功能版）
-- ==========================================

-- 删除表（如果存在）
DROP TABLE IF EXISTS project_schedule_records;

-- 创建项目进度记录表
CREATE TABLE project_schedule_records (
    -- 主键和关联
    id VARCHAR(64) NOT NULL COMMENT '主键ID（格式：REC + 年月日 + 6位序列号）',
    project_id VARCHAR(64) NOT NULL COMMENT '项目ID（关联projects表）',
    schedule_id VARCHAR(64) NOT NULL COMMENT '进度ID（关联project_schedules表）',

    -- 进度记录基础信息
    record_type VARCHAR(20) NOT NULL DEFAULT 'PROGRESS' COMMENT '记录类型（START:开始、PROGRESS:进度更新、COMPLETE:完成、ISSUE:问题、ACCEPTANCE:验收）',
    images JSON DEFAULT NULL COMMENT '现场图片JSON数组格式（如：["img1.jpg","img2.jpg"]）',

    -- 验收专用字段（仅当record_type=ACCEPTANCE时有效）
    acceptance_content TEXT DEFAULT NULL COMMENT '验收内容',
    acceptance_result VARCHAR(20) DEFAULT NULL COMMENT '验收结果（QUALIFIED:合格、UNQUALIFIED:不合格）',
    acceptance_time DATETIME DEFAULT NULL COMMENT '验收时间（实际验收发生时间）',
    acceptor VARCHAR(50) DEFAULT NULL COMMENT '验收人（实际验收人员姓名）',

    -- 系统审计字段（自动维护）
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（系统自动）',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间（系统自动）',
    created_by VARCHAR(64) NOT NULL COMMENT '记录创建人（系统自动，存储用户ID）',
    updated_by VARCHAR(64) NOT NULL COMMENT '记录更新人（系统自动，存储用户ID）',

    -- 索引
    PRIMARY KEY (id),
    KEY idx_project (project_id),
    KEY idx_schedule (schedule_id),
    KEY idx_record_type (record_type),
    KEY idx_acceptance_time (acceptance_time),
    KEY idx_acceptor (acceptor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='项目进度记录表（包含验收功能）';

-- ==========================================
-- 示例数据
-- ==========================================

-- 1. 验收记录示例
INSERT INTO project_schedule_records (
    id, project_id, schedule_id, record_type, images,
    acceptance_content, acceptance_result, acceptance_time, acceptor,
    created_by, updated_by
) VALUES
(
    'REC2024112900001',
    'PRJ2024001',
    'SCH2024001001',
    'ACCEPTANCE',
    '["upload/2024/11/waterproof_test.jpg","upload/2024/11/wiring_layout.jpg","upload/2024/11/switch_position.jpg"]',
    '水电工程验收完成：
1. 电路布线规范，强弱电分离合理
2. 开关插座位置符合设计图纸
3. 水管打压测试30分钟无渗漏
4. 配电箱接线规范，标识清晰',
    'QUALIFIED',
    '2024-11-29 10:30:00',
    '张三（监理工程师）',
    'admin',
    'admin'
),
(
    'REC2024112900002',
    'PRJ2024001',
    'SCH2024001002',
    'ACCEPTANCE',
    '["upload/2024/11/tile_issue1.jpg","upload/2024/11/waterproof_bubble.jpg"]',
    '泥瓦工程验收发现问题：
1. 卫生间防水层局部有气泡
2. 地砖平整度偏差超过2mm
3. 墙���瓷砖空鼓率超过5%',
    'UNQUALIFIED',
    '2024-11-29 15:20:00',
    '李四（质检员）',
    'admin',
    'admin'
);

-- 2. 普通进度记录示例
INSERT INTO project_schedule_records (
    id, project_id, schedule_id, record_type, images, acceptance_content, acceptance_result, acceptance_time, acceptor,
    created_by, updated_by
) VALUES
(
    'REC2024112800001',
    'PRJ2024001',
    'SCH2024001001',
    'START',
    NULL,
    NULL, NULL, NULL, NULL,
    'project_manager_zhang',
    'project_manager_zhang'
),
(
    'REC2024112800002',
    'PRJ2024001',
    'SCH2024001001',
    'PROGRESS',
    NULL,
    '水电进度更新：
- 强弱电布线完成80%
- 给排水管道铺设完成70%',
    NULL, NULL, NULL,
    'project_manager_zhang',
    'project_manager_zhang'
);

-- ==========================================
-- 常用查询示例
-- ==========================================

-- 1. 查询项目的所有验收记录
SELECT
    id,
    schedule_id,
    acceptance_content,
    acceptance_result,
    DATE_FORMAT(acceptance_time, '%Y-%m-%d %H:%i') AS acceptance_time,
    acceptor,
    JSON_LENGTH(images) AS image_count,
    created_at AS submitted_at
FROM project_schedule_records
WHERE project_id = 'PRJ2024001'
  AND record_type = 'ACCEPTANCE'
ORDER BY acceptance_time DESC;

-- 2. 验收统计报表（按项目）
SELECT
    p.name AS project_name,
    COUNT(*) AS total_acceptance_records,
    SUM(CASE WHEN psr.acceptance_result = 'QUALIFIED' THEN 1 ELSE 0 END) AS qualified_count,
    SUM(CASE WHEN psr.acceptance_result = 'UNQUALIFIED' THEN 1 ELSE 0 END) AS unqualified_count,
    ROUND(
        SUM(CASE WHEN psr.acceptance_result = 'QUALIFIED' THEN 1 ELSE 0 END) * 100.0 / COUNT(*),
        2
    ) AS pass_rate,
    MIN(psr.acceptance_time) AS first_acceptance_time,
    MAX(psr.acceptance_time) AS last_acceptance_time
FROM project_schedule_records psr
LEFT JOIN projects p ON psr.project_id = p.id
WHERE psr.record_type = 'ACCEPTANCE'
GROUP BY psr.project_id, p.name
ORDER BY pass_rate DESC;

-- 3. 查询未合格的验收记录（需要整改）
SELECT
    psr.id,
    psr.project_id,
    p.name AS project_name,
    psr.schedule_id,
    ps.stage_name,
    psr.acceptance_content,
    psr.acceptance_result,
    psr.acceptance_time,
    psr.acceptor,
    DATEDIFF(NOW(), psr.acceptance_time) AS days_since_acceptance
FROM project_schedule_records psr
LEFT JOIN projects p ON psr.project_id = p.id
LEFT JOIN project_schedules ps ON psr.schedule_id = ps.id
WHERE psr.record_type = 'ACCEPTANCE'
  AND psr.acceptance_result = 'UNQUALIFIED'
  AND psr.acceptance_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
ORDER BY psr.acceptance_time ASC;

-- 4. 验收员工作量统计
SELECT
    acceptor,
    COUNT(*) AS total_acceptance,
    SUM(CASE WHEN acceptance_result = 'QUALIFIED' THEN 1 ELSE 0 END) AS qualified_count,
    SUM(CASE WHEN acceptance_result = 'UNQUALIFIED' THEN 1 ELSE 0 END) AS unqualified_count,
    ROUND(AVG(CASE WHEN acceptance_result = 'QUALIFIED' THEN 100 ELSE 0 END), 2) AS avg_pass_rate
FROM project_schedule_records
WHERE record_type = 'ACCEPTANCE'
  AND acceptance_time >= DATE_SUB(NOW(), INTERVAL 1 MONTH)
GROUP BY acceptor
ORDER BY total_acceptance DESC;

-- 5. 查询项目各阶段的验收情况
SELECT
    ps.stage_name,
    psr.acceptance_result,
    COUNT(*) AS count,
    MIN(psr.acceptance_time) AS first_time,
    MAX(psr.acceptance_time) AS last_time
FROM project_schedule_records psr
LEFT JOIN project_schedules ps ON psr.schedule_id = ps.id
WHERE psr.project_id = 'PRJ2024001'
  AND psr.record_type = 'ACCEPTANCE'
GROUP BY ps.stage_name, psr.acceptance_result
ORDER BY ps.stage_name, psr.acceptance_result;

-- ==========================================
-- 数据维护示例
-- ==========================================

-- 更���验收记录（修改验收结果）
UPDATE project_schedule_records
SET
    acceptance_result = 'QUALIFIED',
    acceptance_time = '2024-11-29 16:00:00',
    acceptor = '王五（重新验收）',
    updated_at = NOW(),
    updated_by = 'admin'
WHERE id = 'REC2024112900002';

-- 删除验收记录（物理删除）
-- DELETE FROM project_schedule_records WHERE id = 'REC2024112900002';

-- ==========================================
-- 表结构说明
-- ==========================================
/*
字段说明：

1. 核心业务字段：
   - project_id, schedule_id: 关联项目和时间轴
   - record_type: 区分记录类型，ACCEPTANCE为验收类型
   - images: 现场图片JSON数组
   - acceptance_content: 验收内容/进度说明/问题描述（核心信息）

2. 验收专用字段：
   - acceptance_content: 验收内容（与核心字段复用同一字段）
   - acceptance_result: QUALIFIED/UNQUALIFIED
   - acceptance_time: 实际验收时间（区别于created_at系统时间）
   - acceptor: 实际验收人（区别于created_by系统操作者）

3. 系统字段：
   - created_at/created_by: 记录创建的时间和人员（系统自动）
   - updated_at/updated_by: 记录更新的时间和人员（系统自动）

使用场景：
- 验收时：record_type='ACCEPTANCE'，填写验收专用字段
- 进度记录时：record_type='PROGRESS'，在acceptance_content中记录进度
- 问题时：record_type='ISSUE'，在acceptance_content��描述问题
*/
