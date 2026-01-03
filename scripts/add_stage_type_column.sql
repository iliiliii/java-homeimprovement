-- 添加阶段类型字段到 project_schedules 表
-- DESIGN: 设计阶段, CONSTRUCTION: 施工阶段

ALTER TABLE project_schedules 
ADD COLUMN stage_type VARCHAR(20) DEFAULT 'CONSTRUCTION' COMMENT '阶段类型（DESIGN:设计阶段、CONSTRUCTION:施工阶段）' 
AFTER project_id;

-- 更新现有数据，将所有现有记录标记为施工阶段
UPDATE project_schedules SET stage_type = 'CONSTRUCTION' WHERE stage_type IS NULL;
