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
  `id` VARCHAR(32) NOT NULL COMMENT '客户ID',
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_level` (`level`),
  KEY `idx_active_deleted` (`deleted_at`),
  KEY `idx_name` (`name`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户档案表';

-- ----------------------------
-- 2、项目信息表
-- ----------------------------
DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects` (
  `id` VARCHAR(32) NOT NULL COMMENT '项目ID',
  `project_type` VARCHAR(20) NOT NULL DEFAULT 'RESIDENTIAL' COMMENT '项目类型（RESIDENTIAL:家装、COMMERCIAL:工装）',
  `name` VARCHAR(200) NOT NULL COMMENT '项目名称',
  `customer_id` VARCHAR(32) COMMENT '客户ID',
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
  `budgets_url` VARCHAR(500) COMMENT '预算文件URL',
  `contracts_url` VARCHAR(500) COMMENT '合同文件URL',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME COMMENT '删除时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_project_type` (`project_type`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_active_deleted` (`deleted_at`),
  KEY `idx_name` (`name`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目信息表';

-- ----------------------------
-- 3、项目成员表
-- ----------------------------
DROP TABLE IF EXISTS `project_members`;
CREATE TABLE `project_members` (
  `id` VARCHAR(32) NOT NULL COMMENT '成员ID',
  `project_id` VARCHAR(32) COMMENT '项目ID（逻辑关联）',
  `user_id` VARCHAR(32) COMMENT '用户ID（逻辑关联）',
  `role` VARCHAR(20) NOT NULL COMMENT '项目角色（DESIGNER:设计师、PM:项目经理、WORKER:工长、SUPERVISOR:监理）',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用（0:已移除，1:在职）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) COMMENT '创建人（添加成员的人）',
  `updated_by` VARCHAR(64) COMMENT '更新人（移除/修改的人）',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role` (`role`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';

-- ----------------------------
-- 4、预算明细表
-- ----------------------------
-- 说明：删除冗余的 project_budgets 主表，budget_items 直接关联项目
-- 优势：消除数据冗余、简化表关系、避免主从表同步问题
-- 总预算通过 SUM(planned_amount) 聚合计算，无需单独存储
DROP TABLE IF EXISTS `project_budgets`;
CREATE TABLE `project_budgets` (
  `id` VARCHAR(32) NOT NULL COMMENT '明细ID',
  `project_id` VARCHAR(32) COMMENT '项目ID',
  `category` VARCHAR(50) NOT NULL COMMENT '预算分类（拆除工程、水电安装、泥瓦工程、木工工程、油漆工程、材料费、人工费、管理费、其他）',
  `planned_amount` DECIMAL(15,2) NOT NULL COMMENT '计划金额',
  `remarks` TEXT COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算明细表';

-- ----------------------------
-- 5、项目进度表（8大施工阶段）
-- ----------------------------
DROP TABLE IF EXISTS `project_schedules`;
CREATE TABLE `project_schedules` (
  `id` VARCHAR(32) NOT NULL COMMENT '进度ID',
  `project_id` VARCHAR(32) COMMENT '项目ID',
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
  KEY `idx_project_id` (`project_id`),
  KEY `idx_stage` (`stage`),
  KEY `idx_status` (`status`),
  KEY `idx_stage_order` (`stage_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目进度表';

-- ----------------------------
-- 6、进度记录表
-- ----------------------------
DROP TABLE IF EXISTS `project_schedule_records`;
CREATE TABLE `project_schedule_records` (
  `id` VARCHAR(32) NOT NULL COMMENT '记录ID',
  `project_id` VARCHAR(32) COMMENT '项目ID',
  `schedule_id` VARCHAR(32) COMMENT '进度ID（逻辑关联）',
  `record_type` VARCHAR(20) NOT NULL COMMENT '记录类型（START:开始、PROGRESS:进度更新、COMPLETE:完成、ISSUE:问题）',
  `completion_rate` DECIMAL(5,2) COMMENT '完成度百分比',
  `description` TEXT COMMENT '记录描述',
  `images` TEXT COMMENT '现场图片JSON',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_record_type` (`record_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='进度记录表';

-- ----------------------------
-- 7、质检表
-- ----------------------------
DROP TABLE IF EXISTS `quality_inspections`;
CREATE TABLE `quality_inspections` (
  `id` VARCHAR(32) NOT NULL COMMENT '质检ID',
  `project_id` VARCHAR(32) COMMENT '项目ID（逻辑关联）',
  `schedule_id` VARCHAR(32) COMMENT '进度ID（逻辑关联）',
  `inspection_type` VARCHAR(50) NOT NULL COMMENT '质检类型',
  `title` VARCHAR(200) NOT NULL COMMENT '质检标题',
  `description` TEXT COMMENT '质检描述',
  `result` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '质检结果',
  `inspection_date` DATETIME COMMENT '检查日期',
  `images` TEXT COMMENT '质检图片JSON',
  `remarks` TEXT COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_result` (`result`),
  KEY `idx_inspection_date` (`inspection_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='质检表';

-- ----------------------------
-- 8、质量问题表
-- ----------------------------
DROP TABLE IF EXISTS `quality_issues`;
CREATE TABLE `quality_issues` (
  `id` VARCHAR(32) NOT NULL COMMENT '问题ID',
  `project_id` VARCHAR(32) COMMENT '项目ID',
  `quality_inspection_id` VARCHAR(32) NOT NULL COMMENT '质检ID',
  `title` VARCHAR(200) NOT NULL COMMENT '问题标题',
  `description` TEXT NOT NULL COMMENT '问题描述',
  `category` VARCHAR(50) NOT NULL DEFAULT 'GENERAL' COMMENT '问题分类(GENERAL:一般问题、CRITICAL:红线问题、URGENT:紧急问题、OTHER:其他问题)',
  `location` VARCHAR(200) COMMENT '问题位置',
  `images` TEXT NOT NULL COMMENT '问题图片JSON',
  `status` VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '问题状态(OPEN:未解决、IN_PROGRESS:解决中、RESOLVED:已解决、CLOSED:已关闭)',
  `resolved_at` DATETIME COMMENT '解决时间',
  `due_date` DATETIME COMMENT '整改期限',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_quality_inspection_id` (`quality_inspection_id`),
  KEY `idx_status` (`status`),
  KEY `idx_due_date` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='质量问题表';

-- ----------------------------
-- 9、问题修复表
-- ----------------------------
DROP TABLE IF EXISTS `quality_fixes`;
CREATE TABLE `project_quality_fixes` (
  `id` VARCHAR(32) NOT NULL COMMENT '修复ID',
  `quality_issues_id` VARCHAR(32) NOT NULL COMMENT '问题ID',
  `fix_description` TEXT NOT NULL COMMENT '修复描述',
  `images` TEXT NOT NULL COMMENT '修复图片JSON',
  `status` VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '修复状态(OPEN:未解决、IN_PROGRESS:解决中、RESOLVED:已解决)' ,
  `fixed_at` DATETIME COMMENT '修复时间',
  `verified_at` DATETIME COMMENT '验收时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_quality_issues_id` (`quality_issues_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题修复表';

-- ----------------------------
-- 10、团队成员表
-- ----------------------------
DROP TABLE IF EXISTS `team_members`;
CREATE TABLE `team_members` (
  `id` VARCHAR(32) NOT NULL COMMENT '成员ID',
  `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `member_name` VARCHAR(100) NOT NULL COMMENT '成员姓名',
  `phone` VARCHAR(20) COMMENT '联系电话',
  `role` VARCHAR(20) NOT NULL COMMENT '团队角色（DESIGNER:设计师、PM:项目经理、WORKER:工长、SUPERVISOR:监理）',
  `skills` TEXT COMMENT '技能特长',
  `avatar` VARCHAR(500) COMMENT '头像',
  `experience_years` INT COMMENT '工作经验（年）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME COMMENT '删除时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_role` (`role`),
  KEY `idx_deleted_at` (`deleted_at`),
  KEY `idx_member_name` (`member_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队成员表';

-- ----------------------------
-- 11、项目文件表
-- ----------------------------
DROP TABLE IF EXISTS `file_uploads`;
-- 文件上传表
CREATE TABLE IF NOT EXISTS `file_uploads` (
  `id` VARCHAR(32) NOT NULL COMMENT '文件ID (CUID)',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `mime_type` VARCHAR(100) NOT NULL COMMENT 'MIME类型',
  `size` INT NOT NULL COMMENT '文件大小(字节)',
  `path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `url` VARCHAR(500) NOT NULL COMMENT '访问URL',
  `type` VARCHAR(20) NOT NULL DEFAULT 'OTHER' COMMENT '文件类型',
  `category` VARCHAR(50) COMMENT '分类',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME COMMENT '删除时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_category` (`category`),
  KEY `idx_active_deleted` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件上传表';

-- ----------------------------
-- 12、项目房间表
-- 说明：使用JSON字段存储文件ID和URL，支持灵活的文件管理
-- ----------------------------
DROP TABLE IF EXISTS `project_rooms`;
CREATE TABLE `project_rooms` (
  `id` VARCHAR(32) NOT NULL COMMENT '房间ID',
  `project_id` VARCHAR(32) NOT NULL COMMENT '项目ID',
  `room_name` VARCHAR(100) NOT NULL COMMENT '房间名称',
  `room_type` VARCHAR(50) COMMENT '房间类型（客厅、卧室、厨房、卫生间、书房、餐厅、阳台、儿童房、老人房、衣帽间、储物间、其他）',
  `area` DECIMAL(10,2) COMMENT '房间面积（平米）',
  `description` TEXT COMMENT '房间描述',
  `floor` VARCHAR(50) COMMENT '楼层信息',
  `orientation` VARCHAR(20) COMMENT '朝向（N:北、S:南、E:东、W:西、NE:东北、NW:西北、SE:东南、SW:西南）',
  `file_ids` TEXT COMMENT '关联文件ID数组（JSON格式：["f1", "f2", "f3"]）',
  `design_urls` TEXT COMMENT '设计稿URL数组（JSON格式：["url1", "url2", "url3"]）',
  `construction_urls` TEXT COMMENT '施工图URL数组（JSON格式）',
  `effect_urls` TEXT COMMENT '效果图URL数组（JSON格式）',
  `other_urls` TEXT COMMENT '其他文件URL数组（JSON格式）',
  `sort_order` INT DEFAULT 0 COMMENT '排序（数字越小排序越靠前）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME COMMENT '删除时间',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `updated_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_room_type` (`room_type`),
  KEY `idx_active_deleted` (`deleted_at`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_room_name` (`room_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目房间表';


-- ==========================================
-- 装修业务字典数据
-- ==========================================

-- ----------------------------
-- 客户等级字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES(200, '客户等级', 'decoration_customer_level', '0', 'admin', NOW(), '', NULL, '装修客户等级');
INSERT INTO `sys_dict_data` VALUES(2001, 1, '普通客户', 'NORMAL', 'decoration_customer_level', '', 'info', 'Y', '0', 'admin', NOW(), '', NULL, '普通客户');
INSERT INTO `sys_dict_data` VALUES(2002, 2, '重要客户', 'VIP', 'decoration_customer_level', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '重要客户');
INSERT INTO `sys_dict_data` VALUES(2003, 3, '关键客户', 'KEY', 'decoration_customer_level', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '关键客户');


-- ----------------------------
-- 项目状态字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES(202, '项目状态', 'decoration_project_status', '0', 'admin', NOW(), '', NULL, '装修项目状态');
INSERT INTO `sys_dict_data` VALUES(2021, 1, '设计中', 'PLANNING', 'decoration_project_status', '', 'info', 'Y', '0', 'admin', NOW(), '', NULL, '项目设计阶段');
INSERT INTO `sys_dict_data` VALUES(2022, 2, '施工中', 'IN_PROGRESS', 'decoration_project_status', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '项目施工中');
INSERT INTO `sys_dict_data` VALUES(2023, 3, '暂停', 'SUSPENDED', 'decoration_project_status', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '项目暂停');
INSERT INTO `sys_dict_data` VALUES(2024, 4, '已完成', 'COMPLETED', 'decoration_project_status', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '项目已完成');
INSERT INTO `sys_dict_data` VALUES(2025, 5, '已取消', 'CANCELLED', 'decoration_project_status', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '项目已取消');

-- ----------------------------
-- 施工阶段字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES(204, '施工阶段', 'decoration_construction_stage', '0', 'admin', NOW(), '', NULL, '装修施工阶段');
INSERT INTO `sys_dict_data` VALUES(2041, 1, '拆除工程', 'DISMANTLING', 'decoration_construction_stage', '', 'info', 'Y', '0', 'admin', NOW(), '', NULL, '拆除旧装修');
INSERT INTO `sys_dict_data` VALUES(2042, 2, '水电改造', 'WATER_ELECTRIC', 'decoration_construction_stage', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '水电管道改造');
INSERT INTO `sys_dict_data` VALUES(2043, 3, '泥瓦工程', 'TILES', 'decoration_construction_stage', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '泥瓦施工');
INSERT INTO `sys_dict_data` VALUES(2044, 4, '木工工程', 'WOODWORK', 'decoration_construction_stage', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '木工制作');
INSERT INTO `sys_dict_data` VALUES(2045, 5, '油漆工程', 'PAINTING', 'decoration_construction_stage', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '油漆施工');
INSERT INTO `sys_dict_data` VALUES(2046, 6, '安装工程', 'INSTALLATION', 'decoration_construction_stage', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '设备安装');
INSERT INTO `sys_dict_data` VALUES(2047, 7, '软装配饰', 'SOFT_FURNISHING', 'decoration_construction_stage', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '软装搭配');
INSERT INTO `sys_dict_data` VALUES(2048, 8, '竣工验收', 'ACCEPTANCE', 'decoration_construction_stage', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '最终验收');

-- ----------------------------
-- 质检结果字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES(206, '质检结果', 'decoration_quality_result', '0', 'admin', NOW(), '', NULL, '装修质检结果');
INSERT INTO `sys_dict_data` VALUES(2061, 1, '待检查', 'PENDING', 'decoration_quality_result', '', 'warning', 'Y', '0', 'admin', NOW(), '', NULL, '等待质检');
INSERT INTO `sys_dict_data` VALUES(2062, 2, '合格', 'PASSED', 'decoration_quality_result', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '质检合格');
INSERT INTO `sys_dict_data` VALUES(2063, 3, '不合格', 'FAILED', 'decoration_quality_result', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '质检不合格');
INSERT INTO `sys_dict_data` VALUES(2064, 4, '需整改', 'NEEDS_FIX', 'decoration_quality_result', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '需要整改');

-- ----------------------------
-- 问题严重程度字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES(208, '问题严重程度', 'decoration_issue_severity', '0', 'admin', NOW(), '', NULL, '装修问题严重程度');
INSERT INTO `sys_dict_data` VALUES(2081, 1, '轻微', 'LOW', 'decoration_issue_severity', '', 'info', 'Y', '0', 'admin', NOW(), '', NULL, '轻微问题');
INSERT INTO `sys_dict_data` VALUES(2082, 2, '一般', 'MEDIUM', 'decoration_issue_severity', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '一般问题');
INSERT INTO `sys_dict_data` VALUES(2083, 3, '严重', 'HIGH', 'decoration_issue_severity', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '严重问题');
INSERT INTO `sys_dict_data` VALUES(2084, 4, '紧急', 'URGENT', 'decoration_issue_severity', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '紧急问题');

-- ----------------------------
-- 房间类型字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES(210, '房间类型', 'decoration_room_type', '0', 'admin', NOW(), '', NULL, '装修房间类型');
INSERT INTO `sys_dict_data` VALUES(2101, 1, '客厅', 'LIVING_ROOM', 'decoration_room_type', '', 'info', 'Y', '0', 'admin', NOW(), '', NULL, '客厅');
INSERT INTO `sys_dict_data` VALUES(2102, 2, '卧室', 'BEDROOM', 'decoration_room_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '卧室');
INSERT INTO `sys_dict_data` VALUES(2103, 3, '厨房', 'KITCHEN', 'decoration_room_type', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '厨房');
INSERT INTO `sys_dict_data` VALUES(2104, 4, '卫生间', 'BATHROOM', 'decoration_room_type', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '卫生间');
INSERT INTO `sys_dict_data` VALUES(2105, 5, '书房', 'STUDY', 'decoration_room_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '书房');
INSERT INTO `sys_dict_data` VALUES(2106, 6, '餐厅', 'DINING_ROOM', 'decoration_room_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '餐厅');
INSERT INTO `sys_dict_data` VALUES(2107, 7, '阳台', 'BALCONY', 'decoration_room_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '阳台');
INSERT INTO `sys_dict_data` VALUES(2108, 8, '儿童房', 'CHILDREN_ROOM', 'decoration_room_type', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '儿童房');
INSERT INTO `sys_dict_data` VALUES(2109, 9, '老人房', 'ELDER_ROOM', 'decoration_room_type', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '老人房');
INSERT INTO `sys_dict_data` VALUES(2110, 10, '衣帽间', 'CLOAKROOM', 'decoration_room_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '衣帽间');
INSERT INTO `sys_dict_data` VALUES(2111, 11, '储物间', 'STORAGE_ROOM', 'decoration_room_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '储物间');
INSERT INTO `sys_dict_data` VALUES(2112, 12, '前台接待区', 'RECEPTION', 'decoration_room_type', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '前台接待区');
INSERT INTO `sys_dict_data` VALUES(2113, 13, '开放办公区', 'OPEN_OFFICE', 'decoration_room_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '开放办公区');
INSERT INTO `sys_dict_data` VALUES(2114, 14, '会议室', 'MEETING_ROOM', 'decoration_room_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '会议室');
INSERT INTO `sys_dict_data` VALUES(2115, 15, '经理办公室', 'MANAGER_OFFICE', 'decoration_room_type', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '经理办公室');
INSERT INTO `sys_dict_data` VALUES(2116, 16, '休息区', 'LOUNGE', 'decoration_room_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '休息区');
INSERT INTO `sys_dict_data` VALUES(2117, 17, '茶水间', 'PANTRY', 'decoration_room_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '茶水间');
INSERT INTO `sys_dict_data` VALUES(2118, 18, '公共卫生间', 'PUBLIC_RESTROOM', 'decoration_room_type', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '公共卫生间');
INSERT INTO `sys_dict_data` VALUES(2119, 19, '机房', 'SERVER_ROOM', 'decoration_room_type', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '机房');
INSERT INTO `sys_dict_data` VALUES(2120, 20, '展厅', 'EXHIBITION_HALL', 'decoration_room_type', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '展厅');
INSERT INTO `sys_dict_data` VALUES(2121, 21, '员工餐厅', 'CANTEEN', 'decoration_room_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '员工餐厅');
INSERT INTO `sys_dict_data` VALUES(2122, 22, '其他', 'OTHER', 'decoration_room_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '其他房间');

-- ----------------------------
-- 项目类型字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES(220, '项目类型', 'decoration_project_type', '0', 'admin', NOW(), '', NULL, '装修项目类型');
INSERT INTO `sys_dict_data` VALUES(2201, 1, '家装', 'RESIDENTIAL', 'decoration_project_type', '', 'primary', 'Y', '0', 'admin', NOW(), '', NULL, '家庭装修');
INSERT INTO `sys_dict_data` VALUES(2202, 2, '工装', 'COMMERCIAL', 'decoration_project_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '工装办公');
-- ----------------------------
-- 客户来源字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES(222, '客户来源', 'decoration_customer_source', '0', 'admin', NOW(), '', NULL, '装修客户来源');
INSERT INTO `sys_dict_data` VALUES(2221, 1, '网络推广', 'ONLINE_PROMOTION', 'decoration_customer_source', '', 'info', 'Y', '0', 'admin', NOW(), '', NULL, '网络推广');
INSERT INTO `sys_dict_data` VALUES(2222, 2, '抖音推广', 'DOUYIN', 'decoration_customer_source', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '抖音推广');
INSERT INTO `sys_dict_data` VALUES(2223, 3, '小红书推广', 'XIAOHONGSHU', 'decoration_customer_source', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '小红书推广');
INSERT INTO `sys_dict_data` VALUES(2224, 4, '朋友介绍', 'FRIEND_REFERRAL', 'decoration_customer_source', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '朋友推荐');
INSERT INTO `sys_dict_data` VALUES(2225, 5, '电话咨询', 'PHONE_INQUIRY', 'decoration_customer_source', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '电话咨询');
INSERT INTO `sys_dict_data` VALUES(2226, 6, '微博推广', 'WEIBO', 'decoration_customer_source', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '微博推广');




-- ==========================================
-- 示例测试数据
-- ==========================================

-- 插入示例客户
INSERT INTO `customers` (`id`, `name`, `phone`, `email`, `address`, `level`, `source`, `remarks`, `created_by`, `updated_by`) VALUES
('C2024110100001', '张先生', '13812345678', 'zhang@example.com', '北京市朝阳区建国路88号', 'VIP', '网络推广', '三居室装修', 'admin', 'admin'),
('C2024110100002', '李女士', '13987654321', 'li@example.com', '上海市浦东新区陆家嘴金融区', 'KEY', '朋友介绍', '办公室装修', 'admin', 'admin'),
('C2024110100003', '王总', '13611112222', 'wang@example.com', '广州市天河区珠江新城', 'NORMAL', '电话咨询', '别墅装修', 'admin', 'admin');

-- 插入示例项目
INSERT INTO `projects` (`id`, `project_type`, `name`, `customer_id`, `description`, `address`, `area`, `budget`, `start_date`, `end_date`, `status`, `progress`, `created_by`, `updated_by`) VALUES
('P2024110100001', 'RESIDENTIAL', '现代简约三居室装修', 'C2024110100001', '120平米现代简约风格三居室装修', '北京市朝阳区建国路88号阳光花园2号楼1201室', 120.00, 300000.00, '2024-03-01 08:00:00', '2024-06-30 18:00:00', 'IN_PROGRESS', 35.50, 'admin', 'admin'),
('P2024110100002', 'COMMERCIAL', '办公室装修设计', 'C2024110100002', '300平米开放式办公室装修设计', '上海市浦东新区陆家嘴环路1000号恒生银行大厦15楼', 300.00, 500000.00, '2024-04-01 08:00:00', '2024-07-31 18:00:00', 'PLANNING', 0.00, 'admin', 'admin'),
('P2024110100003', 'RESIDENTIAL', '欧式别墅装修', 'C2024110100003', '500平米欧式风格别墅整体装修', '广州市天河区珠江新城花城大道85号高德��地春广场', 500.00, 800000.00, '2024-02-15 08:00:00', '2024-08-15 18:00:00', 'IN_PROGRESS', 62.30, 'admin', 'admin');

-- 插入示例项目进度
INSERT INTO `project_schedules` (`id`, `stage`, `stage_order`, `plan_start_date`, `plan_end_date`, `actual_start_date`, `actual_end_date`, `status`, `completion_rate`, `description`, `created_by`, `updated_by`) VALUES
('S2024110100001', 'DISMANTLING', 1, '2024-03-01', '2024-03-05', '2024-03-01', '2024-03-04', 'COMPLETED', 100.00, '拆除旧装修', 'admin', 'admin'),
('S2024110100002', 'WATER_ELECTRIC', 2, '2024-03-06', '2024-03-15', '2024-03-06', '2024-03-16', 'COMPLETED', 100.00, '水电改造', 'admin', 'admin'),
('S2024110100003', 'TILES', 3, '2024-03-17', '2024-04-05', '2024-03-17', '2024-04-08', 'COMPLETED', 100.00, '泥瓦施工', 'admin', 'admin'),
('S2024110100004', 'WOODWORK', 4, '2024-04-09', '2024-04-25', '2024-04-09', '2024-04-27', 'IN_PROGRESS', 75.00, '木工制作', 'admin', 'admin'),
('S2024110100005', 'PAINTING', 5, '2024-04-28', '2024-05-10', NULL, NULL, 'PENDING', 0.00, '油漆施工', 'admin', 'admin');

-- 插入示例房间数据
INSERT INTO `project_rooms` (`id`, `project_id`, `room_name`, `room_type`, `area`, `description`, `floor`, `orientation`, `design_urls`, `construction_urls`, `effect_urls`, `sort_order`, `created_by`, `updated_by`) VALUES
('R2024110100001', 'P2024110100001', '客厅', 'LIVING_ROOM', 35.50, '客厅采用现代简约风格，大面积落地窗', '1楼', '南', '["https://example.com/design/living_1.jpg", "https://example.com/design/living_2.jpg"]', '["https://example.com/construction/living_1.jpg"]', '["https://example.com/effect/living.jpg"]', 1, 'admin', 'admin'),
('R2024110100002', 'P2024110100001', '主卧', 'BEDROOM', 20.00, '主卧室配独立卫生间和衣帽间', '1楼', '南', '["https://example.com/design/bedroom_1.jpg"]', '["https://example.com/construction/bedroom_1.jpg"]', '["https://example.com/effect/bedroom.jpg"]', 2, 'admin', 'admin'),
('R2024110100003', 'P2024110100001', '厨房', 'KITCHEN', 12.00, '开放式厨房设计，整体橱柜', '1楼', '北', '["https://example.com/design/kitchen_1.jpg"]', '["https://example.com/construction/kitchen_1.jpg"]', '["https://example.com/effect/kitchen.jpg"]', 3, 'admin', 'admin'),
('R2024110100004', 'P2024110100001', '卫生间', 'BATHROOM', 8.00, '干湿分离设计，智能马桶', '1楼', '北', '["https://example.com/design/bathroom_1.jpg"]', '["https://example.com/construction/bathroom_1.jpg"]', '["https://example.com/effect/bathroom.jpg"]', 4, 'admin', 'admin'),
('R2024110100005', 'P2024110100001', '儿童房', 'CHILDREN_ROOM', 15.00, '儿童房采用环保材料，颜色明亮', '2楼', '南', '["https://example.com/design/children_1.jpg"]', '["https://example.com/construction/children_1.jpg"]', '["https://example.com/effect/children.jpg"]', 5, 'admin', 'admin'),
('R2024110100006', 'P2024110100002', '前台接待区', 'RECEPTION', 25.00, '现代简约风格前台接待区，体现企业形象', '1楼', '南', '["https://example.com/design/reception_1.jpg"]', '["https://example.com/construction/reception_1.jpg"]', '["https://example.com/effect/reception.jpg"]', 1, 'admin', 'admin'),
('R2024110100007', 'P2024110100002', '开放办公区', 'OPEN_OFFICE', 150.00, '开放式办公区，灵活办公工位设计', '1楼', '南', '["https://example.com/design/open_office_1.jpg"]', '["https://example.com/construction/open_office_1.jpg"]', '["https://example.com/effect/open_office.jpg"]', 2, 'admin', 'admin'),
('R2024110100008', 'P2024110100002', '会议室', 'MEETING_ROOM', 30.00, '中型会议室，可容纳12人会议', '1楼', '北', '["https://example.com/design/meeting_1.jpg"]', '["https://example.com/construction/meeting_1.jpg"]', '["https://example.com/effect/meeting.jpg"]', 3, 'admin', 'admin'),
('R2024110100009', 'P2024110100002', '经理办公室', 'MANAGER_OFFICE', 25.00, '独立经理办公室，商务风格', '1楼', '北', '["https://example.com/design/manager_1.jpg"]', '["https://example.com/construction/manager_1.jpg"]', '["https://example.com/effect/manager.jpg"]', 4, 'admin', 'admin'),
('R2024110100010', 'P2024110100002', '休息区', 'LOUNGE', 40.00, '员工休息区，配备沙发和咖啡机', '1楼', '东', '["https://example.com/design/lounge_1.jpg"]', '["https://example.com/construction/lounge_1.jpg"]', '["https://example.com/effect/lounge.jpg"]', 5, 'admin', 'admin'),
('R2024110100011', 'P2024110100002', '茶水间', 'PANTRY', 12.00, '员工茶水间，配备饮水机和微波炉', '1楼', '北', '["https://example.com/design/pantry_1.jpg"]', '["https://example.com/construction/pantry_1.jpg"]', '["https://example.com/effect/pantry.jpg"]', 6, 'admin', 'admin');

-- ==========================================
-- 说明和注意事项
-- ==========================================

-- 1. 本脚本创建装修管理系统的业务表，依赖若依框架的基础表
-- 2. 时间字段使用DATETIME，支持自动时间戳
-- 3. 金额字段使用DECIMAL(15,2)，确保精度
-- 4. JSON字段存储为TEXT，应用层处理JSON解析
-- 5. 支持软删除（deleted_at字段）
-- 6. 包含完整的索引优化
-- 7. 字典数据已初始化，可直接使用
-- 8. 项目房间表（project_rooms）使用JSON字段存储文件，支持设计稿、施工图、效果图等多种文件类型
-- 9. 项目表增加project_type字段，区分家装(RESIDENTIAL)和工装(COMMERCIAL)
-- 10. 项目状态分为：设计中、施工中、暂停、已完成、已取消
-- 11. 房间类型字典包含22种类型：11种家装类型 + 10种工装类型 + 1种其他
-- 12. 客户来源包括：网络推广、抖音推广、小红书推广、朋友推荐、电话咨询、微博推广
-- 13. 示例数据仅供测试使用，生产环境请修改或删除
-- 14. 执行顺序：先执行若依基础表脚本，再执行本脚本

-- ==========================================
-- 项目房间表常用查询示例
-- ==========================================

-- 1. 查询项目下的所有房间
-- SELECT * FROM project_rooms WHERE project_id = 'P2024110100001' AND deleted_at IS NULL ORDER BY sort_order;

-- 2. 查询房间的文件信息
-- SELECT id, room_name, room_type, design_urls, construction_urls, effect_urls FROM project_rooms WHERE id = 'R2024110100001';

-- 3. 按房间类型统计
-- SELECT room_type, COUNT(*) as room_count FROM project_rooms WHERE deleted_at IS NULL GROUP BY room_type;

-- 4. 解析JSON字段查询（MySQL 8.0+）
-- SELECT room_name, JSON_UNQUOTE(design_urls->'$[0]') as first_design_url FROM project_rooms WHERE id = 'R2024110100001';

-- 5. 更新房间文件
-- UPDATE project_rooms SET design_urls = JSON_ARRAY('https://example.com/new_design.jpg') WHERE id = 'R2024110100001';

-- 6. 添加文件到JSON数组
-- UPDATE project_rooms SET design_urls = JSON_ARRAY_APPX(design_urls, 'https://example.com/new_design.jpg') WHERE id = 'R2024110100001';

-- ==========================================
-- 项目类型查询示例
-- ==========================================

-- 1. 查询所有家装项目
-- SELECT * FROM projects WHERE project_type = 'RESIDENTIAL';

-- 2. 查询所有工装项目
-- SELECT * FROM projects WHERE project_type = 'COMMERCIAL';

-- 3. 统计家装和工装项目数量
-- SELECT project_type, COUNT(*) as project_count FROM projects GROUP BY project_type;

-- 4. 查询特定项目的所有房间（自动区分家装/工装房间类型）
-- SELECT pr.*, p.project_type FROM project_rooms pr JOIN projects p ON pr.project_id = p.id WHERE pr.project_id = 'P2024110100001';

-- 5. 查询工装项目的会议室
-- SELECT pr.* FROM project_rooms pr JOIN projects p ON pr.project_id = p.id WHERE p.project_type = 'COMMERCIAL' AND pr.room_type = 'MEETING_ROOM';

-- 6. 按项目类型和房间类型组合查询
-- SELECT p.project_type, pr.room_type, COUNT(*) as count FROM projects p JOIN project_rooms pr ON p.id = pr.project_id GROUP BY p.project_type, pr.room_type;

-- ==========================================
-- 客户来源查询示例
-- ==========================================

-- 1. 查询所有客户来源渠道及数量
-- SELECT source, COUNT(*) as customer_count FROM customers GROUP BY source;

-- 2. 查询抖音推广来源的客户
-- SELECT * FROM customers WHERE source = 'DOUYIN';

-- 3. 查询朋友推荐来源的客户（需要联表查询项目信息）
-- SELECT c.*, p.name as project_name FROM customers c JOIN projects p ON c.id = p.customer_id WHERE c.source = 'FRIEND_REFERRAL';

-- 4. 统计各来源渠道的客户转化率（带项目）
-- SELECT c.source, COUNT(c.id) as total_customers, COUNT(p.id) as project_count FROM customers c LEFT JOIN projects p ON c.id = p.customer_id GROUP BY c.source;

-- 5. 查询网络推广（线上渠道）的客户
-- SELECT * FROM customers WHERE source IN ('ONLINE_PROMOTION', 'DOUYIN', 'XIAOHONGSHU', 'WEIBO');

-- ==========================================
-- JSON字段使用说明
-- ==========================================

-- 1. file_ids: 存储 file_uploads 表的ID数组，格式 ["id1", "id2", "id3"]
-- 2. design_urls: 设计稿URL数组，格式 ["url1", "url2"]
-- 3. construction_urls: 施工图URL数组
-- 4. effect_urls: 效果图URL数组
-- 5. other_urls: 其他文件URL数组

-- JSON字段操作函数：
-- - JSON_ARRAY(): 创建JSON数组
-- - JSON_ARRAY_APPX(): 追加元素到JSON数组
-- - JSON_UNQUOTE(): 提取JSON数组中的值
-- - JSON_EXTRACT(): 提取JSON数据
