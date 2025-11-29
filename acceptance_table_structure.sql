-- 项目进度记录表（验收功能扩展）
-- 基于现有 ProjectScheduleRecords 表，新增 3 个验收相关字段

-- 方式一：ALTER TABLE 新增字段（推荐，用于现有项目）
ALTER TABLE project_schedule_records
ADD COLUMN acceptance_result VARCHAR(20) NULL COMMENT '验收结果（QUALIFIED:合格 UNQUALIFIED:不合格）',
ADD COLUMN acceptance_time DATETIME NULL COMMENT '验收时间（实际验收发生时间，非系统创建时间）',
ADD COLUMN acceptor VARCHAR(50) NULL COMMENT '验收人（实际验收人员姓名，非系统操作者）';

-- 创建索引（可选）
CREATE INDEX idx_project_schedule_records_acceptance_result ON project_schedule_records(acceptance_result);
CREATE INDEX idx_project_schedule_records_acceptance_time ON project_schedule_records(acceptance_time);
CREATE INDEX idx_project_schedule_records_acceptor ON project_schedule_records(acceptor);

-- 方式二：完整表结构定义（新建项目参考）
CREATE TABLE project_schedule_records (
    id VARCHAR(64) NOT NULL COMMENT '主键ID',
    project_id VARCHAR(64) NOT NULL COMMENT '项目ID',
    schedule_id VARCHAR(64) NOT NULL COMMENT '进度ID（逻辑关联）',
    record_type VARCHAR(20) NOT NULL COMMENT '记录类型（START:开始、PROGRESS:进度更新、COMPLETE:完成、ISSUE:问题、ACCEPTANCE:验收）',
    completion_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '完成度百分比',
    description TEXT NOT NULL COMMENT '记录描述（验收内容/问题描述等）',
    images JSON DEFAULT NULL COMMENT '现场图片JSON数组',
    acceptance_result VARCHAR(20) DEFAULT NULL COMMENT '验收结果（QUALIFIED:合格 UNQUALIFIED:不合格，仅record_type=ACCEPTANCE时有效）',
    acceptance_time DATETIME DEFAULT NULL COMMENT '验收时间（仅record_type=ACCEPTANCE时有效）',
    acceptor VARCHAR(50) DEFAULT NULL COMMENT '验收人（仅record_type=ACCEPTANCE时有效）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（系统自动）',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（系统自动）',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人（系统自动）',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人（系统自动）',
    PRIMARY KEY (id),
    KEY idx_project (project_id),
    KEY idx_schedule (schedule_id),
    KEY idx_record_type (record_type),
    KEY idx_acceptance_result (acceptance_result),
    KEY idx_acceptance_time (acceptance_time),
    KEY idx_acceptor (acceptor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目进度记录表';

-- 示例数据
INSERT INTO project_schedule_records (
    id, project_id, schedule_id, record_type, description,
    images, acceptance_result, acceptance_time, acceptor,
    created_by, updated_by
) VALUES (
    'REC2024112900001', 'PRJ2024001', 'SCH2024001001', 'ACCEPTANCE',
    '水电工程验收：线路铺设规范，开关插座位置正确，水管打压测试合格',
    '["upload/images/2024/11/waterproof1.jpg", "upload/images/2024/11/wiring2.jpg"]',
    'QUALIFIED', '2024-11-29 10:30:00', '张三',
    'admin', 'admin'
);

-- 查询示例
-- 1. 查询所有验收记录
SELECT
    id,
    project_id,
    schedule_id,
    record_type,
    description,
    acceptance_result,
    acceptance_time,
    acceptor,
    images,
    created_at AS submitted_at
FROM project_schedule_records
WHERE record_type = 'ACCEPTANCE'
ORDER BY acceptance_time DESC;

-- 2. 查询项目验收统计
SELECT
    project_id,
    COUNT(*) AS total_acceptance,
    SUM(CASE WHEN acceptance_result = 'QUALIFIED' THEN 1 ELSE 0 END) AS qualified_count,
    SUM(CASE WHEN acceptance_result = 'UNQUALIFIED' THEN 1 ELSE 0 END) AS unqualified_count,
    ROUND(SUM(CASE WHEN acceptance_result = 'QUALIFIED' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS pass_rate
FROM project_schedule_records
WHERE record_type = 'ACCEPTANCE'
GROUP BY project_id;

-- 3. 查询验收历史记录
SELECT
    psr.*,
    p.name AS project_name,
    ps.stage_name
FROM project_schedule_records psr
LEFT JOIN projects p ON psr.project_id = p.id
LEFT JOIN project_schedules ps ON psr.schedule_id = ps.id
WHERE psr.record_type = 'ACCEPTANCE'
ORDER BY psr.acceptance_time DESC;
