-- ==========================================
-- 装修管理系统业务表
-- 精简版 - 仅业务表，依赖若依基础表
-- MySQL 8.0+
-- ==========================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 使用若依框架的数据库（通常名为 ruoyi_vue）
-- CREATE DATABASE IF NOT EXISTS `ruoyi_vue` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE `ruoyi_vue`;

-- ==========================================
-- 装修管理业务表
-- ==========================================

-- ----------------------------
-- 1、客户档案表
-- ----------------------------
DROP TABLE IF EXISTS `customers`;
CREATE TABLE `customers` (
  `id` VARCHAR(30) NOT NULL COMMENT '客户ID',
  `name` VARCHAR(100) NOT NULL COMMENT '客户姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(255) COMMENT '邮箱',
  `address` VARCHAR(500) COMMENT '地址',
  `level` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '客户等级（NORMAL:普通、VIP:重要、KEY:关键）',
  `source` VARCHAR(50) COMMENT '客户来源',
  `remarks` TEXT COMMENT '备注',
  `avatar` VARCHAR(500) COMMENT '头像',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME COMMENT '删除时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  `deleted_by` VARCHAR(64) COMMENT '删除人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_level` (`level`),
  KEY `idx_active_deleted` (`is_active`, `deleted_at`),
  KEY `idx_name` (`name`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户档案表';

-- ----------------------------
-- 2、项目信息表
-- ----------------------------
DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects` (
  `id` VARCHAR(30) NOT NULL COMMENT '项目ID',
  `name` VARCHAR(200) NOT NULL COMMENT '项目名称',
  `customer_id` VARCHAR(30) NOT NULL COMMENT '客户ID',
  `project_code` VARCHAR(50) COMMENT '项目编号',
  `description` TEXT COMMENT '项目描述',
  `address` VARCHAR(500) NOT NULL COMMENT '项目地址',
  `area` DECIMAL(10,2) COMMENT '房屋面积（平米）',
  `budget` DECIMAL(15,2) COMMENT '预算金额',
  `actual_cost` DECIMAL(15,2) DEFAULT 0.00 COMMENT '实际费用',
  `start_date` DATETIME COMMENT '开始日期',
  `end_date` DATETIME COMMENT '预计完工日期',
  `actual_end_date` DATETIME COMMENT '实际完工日期',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PLANNING' COMMENT '项目状态',
  `priority` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级',
  `progress` DECIMAL(5,2) DEFAULT 0.00 COMMENT '进度百分比',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME COMMENT '删除时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  `deleted_by` VARCHAR(64) COMMENT '删除人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_code` (`project_code`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_active_deleted` (`is_active`, `deleted_at`),
  KEY `idx_name` (`name`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_project_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目信息表';

-- ----------------------------
-- 3、项目成员表
-- ----------------------------
DROP TABLE IF EXISTS `project_members`;
CREATE TABLE `project_members` (
  `id` VARCHAR(30) NOT NULL COMMENT '成员ID',
  `project_id` VARCHAR(30) NOT NULL COMMENT '项目ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID（关联sys_user）',
  `role` VARCHAR(20) NOT NULL COMMENT '项目角色（DESIGNER:设计师、PM:项目经理、WORKER:工长、SUPERVISOR:监理）',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `added_by` VARCHAR(64) COMMENT '添加人',
  `added_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `removed_by` VARCHAR(64) COMMENT '移除人',
  `removed_at` DATETIME COMMENT '移除时间',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`, `user_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role` (`role`),
  KEY `idx_is_active` (`is_active`),
  CONSTRAINT `fk_pm_project` FOREIGN KEY (`project_id`) REFERENCES `projects`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pm_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';

-- ----------------------------
-- 4、项目合同表
-- ----------------------------
DROP TABLE IF EXISTS `project_contracts`;
CREATE TABLE `project_contracts` (
  `id` VARCHAR(30) NOT NULL COMMENT '合同ID',
  `project_id` VARCHAR(30) NOT NULL COMMENT '项目ID',
  `contract_no` VARCHAR(50) NOT NULL COMMENT '合同编号',
  `contract_amount` DECIMAL(15,2) NOT NULL COMMENT '合同金额',
  `payment_terms` TEXT COMMENT '付款条款',
  `start_date` DATETIME COMMENT '合同开始日期',
  `end_date` DATETIME COMMENT '合同结束日期',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '合同状态',
  `contract_file` VARCHAR(500) COMMENT '合同文件路径',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contract_no` (`contract_no`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_contract_project` FOREIGN KEY (`project_id`) REFERENCES `projects`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目合同表';

-- ----------------------------
-- 5、项目预算表
-- ----------------------------
DROP TABLE IF EXISTS `project_budgets`;
CREATE TABLE `project_budgets` (
  `id` VARCHAR(30) NOT NULL COMMENT '预算ID',
  `project_id` VARCHAR(30) NOT NULL COMMENT '项目ID',
  `total_budget` DECIMAL(15,2) NOT NULL COMMENT '总预算',
  `actual_cost` DECIMAL(15,2) DEFAULT 0.00 COMMENT '实际成本',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '预算状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_budget_project` FOREIGN KEY (`project_id`) REFERENCES `projects`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目预算表';

-- ----------------------------
-- 6、预算明细表
-- ----------------------------
DROP TABLE IF EXISTS `budget_items`;
CREATE TABLE `budget_items` (
  `id` VARCHAR(30) NOT NULL COMMENT '明细ID',
  `budget_id` VARCHAR(30) NOT NULL COMMENT '预算ID',
  `category` VARCHAR(50) NOT NULL COMMENT '预算分类',
  `item_name` VARCHAR(200) NOT NULL COMMENT '项目名称',
  `planned_amount` DECIMAL(15,2) NOT NULL COMMENT '计划金额',
  `actual_amount` DECIMAL(15,2) DEFAULT 0.00 COMMENT '实际金额',
  `quantity` DECIMAL(10,2) COMMENT '数量',
  `unit` VARCHAR(20) COMMENT '单位',
  `remarks` TEXT COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_budget_id` (`budget_id`),
  KEY `idx_category` (`category`),
  CONSTRAINT `fk_budget_item_budget` FOREIGN KEY (`budget_id`) REFERENCES `project_budgets`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算明细表';

-- ----------------------------
-- 7、项目进度表（8大施工阶段）
-- ----------------------------
DROP TABLE IF EXISTS `project_schedules`;
CREATE TABLE `project_schedules` (
  `id` VARCHAR(30) NOT NULL COMMENT '进度ID',
  `project_id` VARCHAR(30) NOT NULL COMMENT '项目ID',
  `stage` VARCHAR(20) NOT NULL COMMENT '施工阶段（DISMANTLING:拆除、WATER_ELECTRIC:水电、TILES:泥瓦、WOODWORK:木工、PAINTING:油漆、INSTALLATION:安装、SOFT_FURNISHING:软装、ACCEPTANCE:验收）',
  `stage_order` INT NOT NULL COMMENT '阶段顺序（1-8）',
  `plan_start_date` DATETIME COMMENT '计划开始日期',
  `plan_end_date` DATETIME COMMENT '计划结束日期',
  `actual_start_date` DATETIME COMMENT '实际开始日期',
  `actual_end_date` DATETIME COMMENT '实际结束日期',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '阶段状态',
  `completion_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '完成度百分比',
  `description` TEXT COMMENT '阶段描述',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_stage` (`project_id`, `stage`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_stage` (`stage`),
  KEY `idx_status` (`status`),
  KEY `idx_stage_order` (`stage_order`),
  CONSTRAINT `fk_schedule_project` FOREIGN KEY (`project_id`) REFERENCES `projects`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目进度表';

-- ----------------------------
-- 8、进度记录表
-- ----------------------------
DROP TABLE IF EXISTS `schedule_records`;
CREATE TABLE `schedule_records` (
  `id` VARCHAR(30) NOT NULL COMMENT '记录ID',
  `schedule_id` VARCHAR(30) NOT NULL COMMENT '进度ID',
  `record_type` VARCHAR(20) NOT NULL COMMENT '记录类型（START:开始、PROGRESS:进度更新、COMPLETE:完成、ISSUE:问题）',
  `completion_rate` DECIMAL(5,2) COMMENT '完成度百分比',
  `description` TEXT COMMENT '记录描述',
  `images` TEXT COMMENT '现场图片JSON',
  `recorded_by` BIGINT(20) NOT NULL COMMENT '记录人（sys_user.user_id）',
  `recorded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_record_type` (`record_type`),
  KEY `idx_recorded_by` (`recorded_by`),
  KEY `idx_recorded_at` (`recorded_at`),
  CONSTRAINT `fk_record_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `project_schedules`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_record_user` FOREIGN KEY (`recorded_by`) REFERENCES `sys_user`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='进度记录表';

-- ----------------------------
-- 9、质检表
-- ----------------------------
DROP TABLE IF EXISTS `quality_inspections`;
CREATE TABLE `quality_inspections` (
  `id` VARCHAR(30) NOT NULL COMMENT '质检ID',
  `project_id` VARCHAR(30) NOT NULL COMMENT '项目ID',
  `schedule_id` VARCHAR(30) COMMENT '关联进度ID',
  `inspection_type` VARCHAR(50) NOT NULL COMMENT '质检类型',
  `title` VARCHAR(200) NOT NULL COMMENT '质检标题',
  `description` TEXT COMMENT '质检描述',
  `result` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '质检结果',
  `inspector_id` BIGINT(20) NOT NULL COMMENT '检查员ID（sys_user.user_id）',
  `inspection_date` DATETIME COMMENT '检查日期',
  `images` TEXT COMMENT '质检图片JSON',
  `notes` TEXT COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_result` (`result`),
  KEY `idx_inspector_id` (`inspector_id`),
  KEY `idx_inspection_date` (`inspection_date`),
  CONSTRAINT `fk_qi_project` FOREIGN KEY (`project_id`) REFERENCES `projects`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_qi_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `project_schedules`(`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_qi_inspector` FOREIGN KEY (`inspector_id`) REFERENCES `sys_user`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='质检表';

-- ----------------------------
-- 10、质量问题表
-- ----------------------------
DROP TABLE IF EXISTS `quality_issues`;
CREATE TABLE `quality_issues` (
  `id` VARCHAR(30) NOT NULL COMMENT '问题ID',
  `inspection_id` VARCHAR(30) NOT NULL COMMENT '质检ID',
  `title` VARCHAR(200) NOT NULL COMMENT '问题标题',
  `description` TEXT NOT NULL COMMENT '问题描述',
  `severity` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '严重程度',
  `category` VARCHAR(50) NOT NULL COMMENT '问题分类',
  `location` VARCHAR(200) COMMENT '问题位置',
  `images` TEXT NOT NULL COMMENT '问题图片JSON',
  `status` VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '问题状态',
  `reported_by` BIGINT(20) NOT NULL COMMENT '上报人ID（sys_user.user_id）',
  `reported_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
  `resolved_by` BIGINT(20) COMMENT '解决人ID（sys_user.user_id）',
  `resolved_at` DATETIME COMMENT '解决时间',
  `due_date` DATETIME COMMENT '整改期限',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_inspection_id` (`inspection_id`),
  KEY `idx_severity` (`severity`),
  KEY `idx_status` (`status`),
  KEY `idx_reported_by` (`reported_by`),
  KEY `idx_due_date` (`due_date`),
  CONSTRAINT `fk_issue_inspection` FOREIGN KEY (`inspection_id`) REFERENCES `quality_inspections`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_issue_reporter` FOREIGN KEY (`reported_by`) REFERENCES `sys_user`(`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_issue_resolver` FOREIGN KEY (`resolved_by`) REFERENCES `sys_user`(`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='质量问题表';

-- ----------------------------
-- 11、问题修复表
-- ----------------------------
DROP TABLE IF EXISTS `quality_fixes`;
CREATE TABLE `quality_fixes` (
  `id` VARCHAR(30) NOT NULL COMMENT '修复ID',
  `issue_id` VARCHAR(30) NOT NULL COMMENT '问题ID',
  `fix_description` TEXT NOT NULL COMMENT '修复描述',
  `images` TEXT NOT NULL COMMENT '修复图片JSON',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '修复状态',
  `fixed_by` BIGINT(20) NOT NULL COMMENT '修复人ID（sys_user.user_id）',
  `fixed_at` DATETIME COMMENT '修复时间',
  `verified_by` BIGINT(20) COMMENT '验收人ID（sys_user.user_id）',
  `verified_at` DATETIME COMMENT '验收时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_issue_id` (`issue_id`),
  KEY `idx_status` (`status`),
  KEY `idx_fixed_by` (`fixed_by`),
  KEY `idx_fixed_at` (`fixed_at`),
  CONSTRAINT `fk_fix_issue` FOREIGN KEY (`issue_id`) REFERENCES `quality_issues`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fix_user` FOREIGN KEY (`fixed_by`) REFERENCES `sys_user`(`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fix_verifier` FOREIGN KEY (`verified_by`) REFERENCES `sys_user`(`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题修复表';

-- ----------------------------
-- 12、团队成员表
-- ----------------------------
DROP TABLE IF EXISTS `team_members`;
CREATE TABLE `team_members` (
  `id` VARCHAR(30) NOT NULL COMMENT '成员ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID（关联sys_user）',
  `member_name` VARCHAR(100) NOT COMMENT '成员姓名',
  `phone` VARCHAR(20) COMMENT '联系电话',
  `role` VARCHAR(20) NOT NULL COMMENT '团队角色（DESIGNER:设计师、PM:项目经理、WORKER:工长、SUPERVISOR:监理）',
  `skills` TEXT COMMENT '技能特长',
  `avatar` VARCHAR(500) COMMENT '头像',
  `experience_years` INT COMMENT '工作经验（年）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`),
  KEY `idx_member_name` (`member_name`),
  CONSTRAINT `fk_team_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队成员表';

-- ----------------------------
-- 13、项目文件表
-- ----------------------------
DROP TABLE IF EXISTS `file_uploads`;
-- 文件上传表
CREATE TABLE IF NOT EXISTS `file_uploads` (
  `id` VARCHAR(30) NOT NULL COMMENT '文件ID (CUID)',
  `uploader_id` VARCHAR(30) NOT NULL COMMENT '上传人ID',
  `uploader_name` VARCHAR(100) NOT NULL COMMENT '上传人姓名',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `mime_type` VARCHAR(100) NOT NULL COMMENT 'MIME类型',
  `size` INT NOT NULL COMMENT '文件大小(字节)',
  `path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `url` VARCHAR(500) NOT NULL COMMENT '访问URL',
  `type` VARCHAR(20) NOT NULL DEFAULT 'OTHER' COMMENT '文件类型',
  `tags` TEXT NOT NULL DEFAULT '[]' COMMENT '标签JSON数组',
  `category` VARCHAR(50) COMMENT '分类',
  `description` TEXT COMMENT '描述',
  `project_id` VARCHAR(30) COMMENT '项目ID',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME COMMENT '删除时间',
  `updated_by` VARCHAR(30) COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_type` (`type`),
  KEY `idx_category` (`category`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_active_deleted` (`is_active`, `deleted_at`),
  KEY `idx_project_active` (`project_id`, `is_active`),
  CONSTRAINT `fk_file_project` FOREIGN KEY (`project_id`)
    REFERENCES `projects`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件上传表';


-- ==========================================
-- 装修业务字典数据
-- ==========================================

-- ----------------------------
-- 客户等级字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES(100, '客户等级', 'decoration_customer_level', '0', 'admin', NOW(), '', NULL, '装修客户等级');
INSERT INTO `sys_dict_data` VALUES(1001, 1, '普通客户', 'NORMAL', 'decoration_customer_level', '', 'info', 'Y', '0', 'admin', NOW(), '', NULL, '普通客户');
INSERT INTO `sys_dict_data` VALUES(1002, 2, '重要客户', 'VIP', 'decoration_customer_level', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '重要客户');
INSERT INTO `sys_dict_data` VALUES(1003, 3, '关键客户', 'KEY', 'decoration_customer_level', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '关键客户');

-- ----------------------------
-- 项目状态字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES(101, '项目状态', 'decoration_project_status', '0', 'admin', NOW(), '', NULL, '装修项目状态');
INSERT INTO `sys_dict_data` VALUES(1011, 1, '规划中', 'PLANNING', 'decoration_project_status', '', 'info', 'Y', '0', 'admin', NOW(), '', NULL, '项目规划阶段');
INSERT INTO `sys_dict_data` VALUES(1012, 2, '进行中', 'IN_PROGRESS', 'decoration_project_status', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '项目施工中');
INSERT INTO `sys_dict_data` VALUES(1013, 3, '暂停', 'SUSPENDED', 'decoration_project_status', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '项目暂停');
INSERT INTO `sys_dict_data` VALUES(1014, 4, '已完成', 'COMPLETED', 'decoration_project_status', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '项目已完成');
INSERT INTO `sys_dict_data` VALUES(1015, 5, '已取消', 'CANCELLED', 'decoration_project_status', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '项目已取消');

-- ----------------------------
-- 施工阶段字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES(102, '施工阶段', 'decoration_construction_stage', '0', 'admin', NOW(), '', NULL, '装修施工阶段');
INSERT INTO `sys_dict_data` VALUES(1021, 1, '拆除工程', 'DISMANTLING', 'decoration_construction_stage', '', 'info', 'Y', '0', 'admin', NOW(), '', NULL, '拆除旧装修');
INSERT INTO `sys_dict_data` VALUES(1022, 2, '水电改造', 'WATER_ELECTRIC', 'decoration_construction_stage', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '水电管道改造');
INSERT INTO `sys_dict_data` VALUES(1023, 3, '泥瓦工程', 'TILES', 'decoration_construction_stage', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '泥瓦施工');
INSERT INTO `sys_dict_data` VALUES(1024, 4, '木工工程', 'WOODWORK', 'decoration_construction_stage', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '木工制作');
INSERT INTO `sys_dict_data` VALUES(1025, 5, '油漆工程', 'PAINTING', 'decoration_construction_stage', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '油漆施工');
INSERT INTO `sys_dict_data` VALUES(1026, 6, '安装工程', 'INSTALLATION', 'decoration_construction_stage', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '设备安装');
INSERT INTO `sys_dict_data` VALUES(1027, 7, '软装配饰', 'SOFT_FURNISHING', 'decoration_construction_stage', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '软装搭配');
INSERT INTO `sys_dict_data` VALUES(1028, 8, '竣工验收', 'ACCEPTANCE', 'decoration_construction_stage', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '最终验收');

-- ----------------------------
-- 质检结果字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES(103, '质检结果', 'decoration_quality_result', '0', 'admin', NOW(), '', NULL, '装修质检结果');
INSERT INTO `sys_dict_data` VALUES(1031, 1, '待检查', 'PENDING', 'decoration_quality_result', '', 'warning', 'Y', '0', 'admin', NOW(), '', NULL, '等待质检');
INSERT INTO `sys_dict_data` VALUES(1032, 2, '合格', 'PASSED', 'decoration_quality_result', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '质检合格');
INSERT INTO `sys_dict_data` VALUES(1033, 3, '不合格', 'FAILED', 'decoration_quality_result', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '质检不合格');
INSERT INTO `sys_dict_data` VALUES(1034, 4, '需整改', 'NEEDS_FIX', 'decoration_quality_result', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '需要整改');

-- ----------------------------
-- 问题严重程度字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES(104, '问题严重程度', 'decoration_issue_severity', '0', 'admin', NOW(), '', NULL, '装修问题严重程度');
INSERT INTO `sys_dict_data` VALUES(1041, 1, '轻微', 'LOW', 'decoration_issue_severity', '', 'info', 'Y', '0', 'admin', NOW(), '', NULL, '轻微问题');
INSERT INTO `sys_dict_data` VALUES(1042, 2, '一般', 'MEDIUM', 'decoration_issue_severity', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '一般问题');
INSERT INTO `sys_dict_data` VALUES(1043, 3, '严重', 'HIGH', 'decoration_issue_severity', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '严重问题');
INSERT INTO `sys_dict_data` VALUES(1044, 4, '紧急', 'URGENT', 'decoration_issue_severity', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '紧急问题');

-- ==========================================
-- 示例测试数据
-- ==========================================

-- 插入示例客户
INSERT INTO `customers` (`id`, `name`, `phone`, `email`, `address`, `level`, `source`, `remarks`, `created_by`, `updated_by`) VALUES
('C2024110100001', '张先生', '13812345678', 'zhang@example.com', '北京市朝阳区建国路88号', 'VIP', '网络推广', '三居室装修', 'admin', 'admin'),
('C2024110100002', '李女士', '13987654321', 'li@example.com', '上海市浦东新区陆家嘴金融区', 'KEY', '朋友介绍', '办公室装修', 'admin', 'admin'),
('C2024110100003', '王总', '13611112222', 'wang@example.com', '广州市天河区珠江新城', 'NORMAL', '电话咨询', '别墅装修', 'admin', 'admin');

-- 插入示例项目
INSERT INTO `projects` (`id`, `name`, `customer_id`, `project_code`, `description`, `address`, `area`, `budget`, `start_date`, `end_date`, `status`, `progress`, `created_by`, `updated_by`) VALUES
('P2024110100001', '现代简约三居室装修', 'C2024110100001', 'PRJ20241100001', '120平米现代简约风格三居室装修', '北京市朝阳区建国路88号阳光花园2号楼1201室', 120.00, 300000.00, '2024-03-01 08:00:00', '2024-06-30 18:00:00', 'IN_PROGRESS', 35.50, 'admin', 'admin'),
('P2024110100002', '办公室装修设计', 'C2024110100002', 'PRJ20241100002', '300平米开放式办公室装修设计', '上海市浦东新区陆家嘴环路1000号恒生银行大厦15楼', 300.00, 500000.00, '2024-04-01 08:00:00', '2024-07-31 18:00:00', 'PLANNING', 0.00, 'admin', 'admin'),
('P2024110100003', '欧式别墅装修', 'C2024110100003', 'PRJ20241100003', '500平米欧式风格别墅整体装修', '广州市天河区珠江新城花城大道85号高德置地春广场', 500.00, 800000.00, '2024-02-15 08:00:00', '2024-08-15 18:00:00', 'IN_PROGRESS', 62.30, 'admin', 'admin');

-- 插入示例项目进度
INSERT INTO `project_schedules` (`id`, `project_id`, `stage`, `stage_order`, `plan_start_date`, `plan_end_date`, `actual_start_date`, `actual_end_date`, `status`, `completion_rate`, `description`, `created_by`, `updated_by`) VALUES
('S2024110100001', 'P2024110100001', 'DISMANTLING', 1, '2024-03-01', '2024-03-05', '2024-03-01', '2024-03-04', 'COMPLETED', 100.00, '拆除旧装修', 'admin', 'admin'),
('S2024110100002', 'P2024110100001', 'WATER_ELECTRIC', 2, '2024-03-06', '2024-03-15', '2024-03-06', '2024-03-16', 'COMPLETED', 100.00, '水电改造', 'admin', 'admin'),
('S2024110100003', 'P2024110100001', 'TILES', 3, '2024-03-17', '2024-04-05', '2024-03-17', '2024-04-08', 'COMPLETED', 100.00, '泥瓦施工', 'admin', 'admin'),
('S2024110100004', 'P2024110100001', 'WOODWORK', 4, '2024-04-09', '2024-04-25', '2024-04-09', '2024-04-27', 'IN_PROGRESS', 75.00, '木工制作', 'admin', 'admin'),
('S2024110100005', 'P2024110100001', 'PAINTING', 5, '2024-04-28', '2024-05-10', NULL, NULL, 'PENDING', 0.00, '油漆施工', 'admin', 'admin');

-- ==========================================
-- 说明和注意事项
-- ==========================================

-- 1. 本脚本创建装修管理系统的业务表，依赖若依框架的基础表
-- 2. 所有外键关联到sys_user、sys_role、sys_dict_type等若依基础表
-- 3. 时间字段使用DATETIME，支持自动时间戳
-- 4. 金额字段使用DECIMAL(15,2)，确保精度
-- 5. JSON字段存储为TEXT，应用层处理JSON解析
-- 6. 支持软删除（deleted_at字段）
-- 7. 包含完整的索引优化
-- 8. 字典数据已初始化，可直接使用
-- 9. 示例数据仅供测试使用，生产环境请修改或删除
-- 10. 执行顺序：先执行若依基础表脚本，再执行本脚本
