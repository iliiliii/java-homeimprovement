-- ===================================================================
-- 装修管理系统 - 核心业务表设计 (Batch 1 - P0)
-- Core Business Tables for Home Improvement Management System
-- ===================================================================
-- 设计原则:
-- 1. 八表架构: projects(核心枢纽) + budgets + timeline + team_assignments + designs + images + quality_checks + issues
-- 2. 无外键约束: 应用层保证数据一致性
-- 3. 软删除模式: 所有表使用 deleted_at 字段
-- 4. 冗余优化: 适当冗余提升查询性能 (如 customer_name, total_budget)
-- 5. JSON扩展: 使用JSON字段支持灵活的数据扩展
-- 6. 索引覆盖: 覆盖所有常用查询场景
-- ===================================================================
--
-- 表关系架构:
--                    ┌─────────────┐
--                    │  customers  │ (已完成)
--                    └──────┬──────┘
--                           │ 1:N
--                           ↓
-- ┌─────────────────────────────────────────────────────────────┐
-- │                     projects                                │
-- │                 (项目核心表 - 枢纽)                           │
-- └──────┬──────────────┬──────────────┬────────────────────────┘
--        │              │              │
--        │ 1:N          │ 1:N          │ M:N
--        ↓              ↓              ↓
-- ┌──────────────┐ ┌──────────────┐ ┌─────────────────────┐
-- │project_      │ │project_      │ │project_team_        │
-- │budgets       │ │timeline      │ │assignments          │
-- │(预算明细)    │ │(进度阶段)    │ │(团队分配中间表)      │
-- └──────────────┘ └──────┬───────┘ └──────┬──────────────┘
--                        │                 │
--                        │ 1:N             │ N:1
--                        ↓                 ↓
--                 ┌──────────────┐  ┌─────────────┐
--                 │quality_      │  │team_members │
--                 │checks        │  │(已完成)      │
--                 │(质检记录)    │  └─────────────┘
--                 └──────┬───────┘
--                        │ 1:N
--                        ↓
--                 ┌──────────────┐
--                 │quality_      │
--                 │issues        │
--                 │(质量问题)    │
--                 └──────────────┘
--
--        ┌──────────────┐
--        │project_      │
--        │designs       │
--        │(设计稿分类)  │
--        └──────┬───────┘
--               │ 1:N
--               ↓
--        ┌──────────────┐
--        │design_       │
--        │images        │
--        │(设计图片)    │
--        └──────────────┘
--
-- ===================================================================

-- -------------------------------------------------------------------
-- 表1: projects - 项目基础信息表 (Project Core)
-- -------------------------------------------------------------------
-- 职责: 装修项目全生命周期管理的核心枢纽表
-- 管理: 项目基础信息 + 状态流转 + 时间规划 + 财务统计 + 进度统计
-- 关联: 客户(customers) + 预算(project_budgets) + 进度(project_timeline) + 团队(project_team_assignments) + 设计稿(project_designs) + 质检(quality_checks)
-- 状态流: planning → in_progress → completed/suspended
-- -------------------------------------------------------------------

CREATE TABLE `projects` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '项目ID',

  -- ============ 项目基本信息 ============
  `project_name` VARCHAR(100) NOT NULL COMMENT '项目名称 (如:现代简约三居室装修)',
  `site_name` VARCHAR(100) NOT NULL COMMENT '工地名称 (如:阳光花园1201室)',
  `site_address` VARCHAR(255) NOT NULL COMMENT '工地详细地址',

  -- ============ 客户关联 ============
  `customer_id` BIGINT UNSIGNED NOT NULL COMMENT '关联客户ID (customers.id)',
  `customer_name` VARCHAR(50) NOT NULL COMMENT '客户姓名 (冗余字段,避免频繁JOIN customers表)',

  -- ============ 项目状态 ============
  `status` ENUM('planning', 'in_progress', 'completed', 'suspended')
    NOT NULL DEFAULT 'planning'
    COMMENT '项目状态: planning=规划中, in_progress=进行中, completed=已完成, suspended=已暂停',

  -- ============ 时间信息 ============
  `start_date` DATE NOT NULL COMMENT '计划开工日期',
  `end_date` DATE NOT NULL COMMENT '计划完工日期',
  `actual_start_date` DATE DEFAULT NULL COMMENT '实际开工日期',
  `actual_end_date` DATE DEFAULT NULL COMMENT '实际完工日期',

  -- ============ 财务统计 (冗余字段,应用层维护) ============
  `total_budget` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '项目总预算(元) - 从project_budgets表自动聚合',
  `total_expense` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '实际总支出(元) - 从project_budgets表自动聚合',

  -- ============ 进度统计 (冗余字段,应用层维护) ============
  `progress_percentage` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '总进度百分比 (0-100) - 从project_timeline表自动计算',

  -- ============ 项目描述 ============
  `description` TEXT DEFAULT NULL COMMENT '项目详细描述/需求说明',
  `notes` TEXT DEFAULT NULL COMMENT '项目备注信息',

  -- ============ 扩展字段 ============
  `extra_data` JSON DEFAULT NULL COMMENT '扩展数据 (JSON对象): 自定义字段',

  -- ============ 审计字段 ============
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID (users.id)',
  `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '软删除时间 (NULL=未删除)',

  -- ============ 主键 ============
  PRIMARY KEY (`id`),

  -- ============ 索引 ============
  KEY `idx_customer_id` (`customer_id`) COMMENT '客户查询索引',
  KEY `idx_status` (`status`, `deleted_at`) COMMENT '状态查询索引 (包含软删除过滤)',
  KEY `idx_dates` (`start_date`, `end_date`) COMMENT '日期范围查询索引',
  KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引',

  -- ============ 全文索引 ============
  FULLTEXT KEY `ft_search` (`project_name`, `site_name`, `site_address`)
    COMMENT '项目名称/工地全文搜索索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='项目表 - 装修项目全生命周期管理核心表';


-- -------------------------------------------------------------------
-- 表2: project_budgets - 项目预算明细表 (Project Budget Details)
-- -------------------------------------------------------------------
-- 职责: 项目预算明细管理 - 支持多项预算分类
-- 覆盖: 水电/泥瓦/木工/油漆/主材/软装/设计费等全品类预算
-- 聚合: 自动更新到 projects.total_budget 和 projects.total_expense
-- -------------------------------------------------------------------

CREATE TABLE `project_budgets` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '预算记录ID',

  -- ============ 项目关联 ============
  `project_id` BIGINT UNSIGNED NOT NULL COMMENT '关联项目ID (projects.id)',

  -- ============ 预算分类 ============
  `budget_category` VARCHAR(50) NOT NULL COMMENT '预算类别 (如:水电改造/泥瓦工程/木工工程/主材采购/软装配饰)',
  `category_code` VARCHAR(20) DEFAULT NULL COMMENT '类别编码 (如:ELEC/TILE/WOOD/MAT/SOFT) - 系统标准编码',

  -- ============ 预算金额 ============
  `budget_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '预算金额(元)',
  `actual_expense` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '实际支出金额(元)',

  -- ============ 预算说明 ============
  `description` TEXT DEFAULT NULL COMMENT '预算详细说明',
  `notes` TEXT DEFAULT NULL COMMENT '备注信息',

  -- ============ 审计字段 ============
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '软删除时间',

  -- ============ 主键 ============
  PRIMARY KEY (`id`),

  -- ============ 索引 ============
  KEY `idx_project_id` (`project_id`) COMMENT '项目查询索引',
  KEY `idx_category` (`budget_category`) COMMENT '类别查询索引',
  KEY `idx_category_code` (`category_code`) COMMENT '类别编码查询索引',

  -- ============ 唯一约束 ============
  UNIQUE KEY `uk_project_category` (`project_id`, `budget_category`, `deleted_at`)
    COMMENT '同一项目同一类别唯一约束'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='项目预算表 - 预算明细与支出管理';


-- -------------------------------------------------------------------
-- 表3: project_timeline - 项目进度时间轴表 (Project Timeline)
-- -------------------------------------------------------------------
-- 职责: 项目施工进度跟踪 - 8大标准施工阶段管理
-- 标准: 拆除→水电→泥瓦→木工→油漆→安装→软装→验收
-- 计算: 自动更新到 projects.progress_percentage
-- -------------------------------------------------------------------

CREATE TABLE `project_timeline` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '时间轴记录ID',

  -- ============ 项目关联 ============
  `project_id` BIGINT UNSIGNED NOT NULL COMMENT '关联项目ID (projects.id)',

  -- ============ 阶段信息 ============
  `stage_order` TINYINT UNSIGNED NOT NULL COMMENT '阶段顺序 (1-8, 对应8大标准施工阶段)',
  `stage_title` VARCHAR(50) NOT NULL COMMENT '阶段标题 (如:拆除工程/水电改造/泥瓦工程)',
  `stage_description` TEXT DEFAULT NULL COMMENT '阶段详细说明',

  -- ============ 时间计划 ============
  `planned_date` DATE NOT NULL COMMENT '计划开始日期',
  `planned_duration` TINYINT UNSIGNED DEFAULT NULL COMMENT '计划工期(天)',
  `actual_start_date` DATE DEFAULT NULL COMMENT '实际开始日期',
  `actual_end_date` DATE DEFAULT NULL COMMENT '实际完成日期',

  -- ============ 阶段状态 ============
  `status` ENUM('pending', 'in_progress', 'completed')
    NOT NULL DEFAULT 'pending'
    COMMENT '阶段状态: pending=待开始, in_progress=进行中, completed=已完成',

  -- ============ 进度信息 ============
  `progress_percentage` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '阶段进度百分比 (0-100)',

  -- ============ 备注 ============
  `notes` TEXT DEFAULT NULL COMMENT '阶段备注',

  -- ============ 审计字段 ============
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '软删除时间',

  -- ============ 主键 ============
  PRIMARY KEY (`id`),

  -- ============ 唯一索引 ============
  UNIQUE KEY `uk_project_stage` (`project_id`, `stage_order`, `deleted_at`)
    COMMENT '同一项目同一阶段唯一约束',

  -- ============ 普通索引 ============
  KEY `idx_project_id` (`project_id`) COMMENT '项目查询索引',
  KEY `idx_status` (`status`) COMMENT '状态查询索引',
  KEY `idx_dates` (`planned_date`, `actual_end_date`) COMMENT '日期查询索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='项目进度时间轴表 - 8大标准施工阶段管理';


-- -------------------------------------------------------------------
-- 表4: project_team_assignments - 项目团队分配表 (Project Team Assignments)
-- -------------------------------------------------------------------
-- 职责: 项目与团队成员的多对多关系管理
-- 支持: 一个项目多个岗位, 每个岗位多个人员, 一个人多个岗位
-- 岗位: designer(设计师) / manager(项目经理) / foreman(工长) / supervisor(监理)
-- -------------------------------------------------------------------

CREATE TABLE `project_team_assignments` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分配记录ID',

  -- ============ 关联关系 ============
  `project_id` BIGINT UNSIGNED NOT NULL COMMENT '项目ID (projects.id)',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '团队成员ID (team_members.id)',

  -- ============ 岗位信息 ============
  `role` ENUM('designer', 'manager', 'foreman', 'supervisor') NOT NULL
    COMMENT '岗位角色: designer=设计师, manager=项目经理, foreman=工长, supervisor=监理',

  -- ============ 分配信息 ============
  `assigned_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `assigned_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '分配人ID (users.id)',

  -- ============ 在岗状态 ============
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否在岗: true=在岗, false=已离岗',
  `leave_date` DATE DEFAULT NULL COMMENT '离岗日期',
  `leave_reason` VARCHAR(255) DEFAULT NULL COMMENT '离岗原因',

  -- ============ 审计字段 ============
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '软删除时间',

  -- ============ 主键 ============
  PRIMARY KEY (`id`),

  -- ============ 唯一索引 ============
  UNIQUE KEY `uk_project_member_role` (`project_id`, `member_id`, `role`, `deleted_at`)
    COMMENT '同一项目同一人同一岗位唯一约束 (支持一人多岗位,但同岗位不重复)',

  -- ============ 普通索引 ============
  KEY `idx_project_id` (`project_id`) COMMENT '项目查询索引',
  KEY `idx_member_id` (`member_id`) COMMENT '成员查询索引',
  KEY `idx_role` (`role`) COMMENT '岗位查询索引',
  KEY `idx_active` (`is_active`) COMMENT '在岗状态索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='项目团队分配表 - 项目与成员多对多关系中间表';


-- -------------------------------------------------------------------
-- 表5: project_designs - 设计稿分类表 (Project Design Categories)
-- -------------------------------------------------------------------
-- 职责: 按房间/楼层/视图维度管理设计稿分类
-- 维度: room(房间) / floor(楼层) / view(视图角度) / other(其他)
-- 应用: 客厅/主卧/次卧/厨房/卫生间 + 1层/2层/3层 + 平面图/立面图/效果图/细节图
-- -------------------------------------------------------------------

CREATE TABLE `project_designs` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '设计稿分类ID',

  -- ============ 项目关联 ============
  `project_id` BIGINT UNSIGNED NOT NULL COMMENT '项目ID (projects.id)',

  -- ============ 分类维度 ============
  `category_type` ENUM('room', 'floor', 'view', 'other') NOT NULL
    COMMENT '分类类型: room=房间, floor=楼层, view=视图角度, other=其他',

  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称 (如:客厅/1层/平面图)',

  -- ============ 排序与描述 ============
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '显示排序序号',
  `description` TEXT DEFAULT NULL COMMENT '分类说明',

  -- ============ 审计字段 ============
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '软删除时间',

  -- ============ 主键 ============
  PRIMARY KEY (`id`),

  -- ============ 索引 ============
  KEY `idx_project_id` (`project_id`) COMMENT '项目查询索引',
  KEY `idx_category` (`category_type`, `category_name`) COMMENT '分类查询索引',
  KEY `idx_sort` (`project_id`, `sort_order`) COMMENT '排序索引',

  -- ============ 唯一约束 ============
  UNIQUE KEY `uk_project_category` (`project_id`, `category_type`, `category_name`, `deleted_at`)
    COMMENT '同一项目同一分类类型下分类名称唯一'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='设计稿分类表 - 按房间/楼层/视图维度管理设计稿';


-- -------------------------------------------------------------------
-- 表6: design_images - 设计图片表 (Design Images)
-- -------------------------------------------------------------------
-- 职责: 存储设计稿图片, 支持版本历史管理
-- 版本: 支持多版本跟踪 (version字段 + is_current标记)
-- 格式: 支持多种图片格式 (jpg/png/pdf/dwg等)
-- 关联: 通过 design_id 关联到 project_designs
-- -------------------------------------------------------------------

CREATE TABLE `design_images` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片ID',

  -- ============ 关联分类 ============
  `design_id` BIGINT UNSIGNED NOT NULL COMMENT '设计稿分类ID (project_designs.id)',
  `project_id` BIGINT UNSIGNED NOT NULL COMMENT '项目ID (冗余字段,查询优化)',

  -- ============ 图片信息 ============
  `image_name` VARCHAR(255) NOT NULL COMMENT '图片名称',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL/存储路径',
  `image_size` BIGINT UNSIGNED DEFAULT NULL COMMENT '图片大小(字节)',
  `image_width` INT UNSIGNED DEFAULT NULL COMMENT '图片宽度(像素)',
  `image_height` INT UNSIGNED DEFAULT NULL COMMENT '图片高度(像素)',
  `image_format` VARCHAR(20) DEFAULT NULL COMMENT '图片格式 (jpg/png/pdf/dwg)',

  -- ============ 版本管理 ============
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  `is_current` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否当前版本: true=当前版本, false=历史版本',

  -- ============ 上传信息 ============
  `upload_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `uploader` VARCHAR(50) DEFAULT NULL COMMENT '上传人姓名',
  `uploader_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '上传人ID (users.id)',

  -- ============ 图片说明 ============
  `description` TEXT DEFAULT NULL COMMENT '图片说明/标注',
  `notes` TEXT DEFAULT NULL COMMENT '备注信息',

  -- ============ 审计字段 ============
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '软删除时间',

  -- ============ 主键 ============
  PRIMARY KEY (`id`),

  -- ============ 索引 ============
  KEY `idx_design_id` (`design_id`) COMMENT '设计稿分类索引',
  KEY `idx_project_id` (`project_id`) COMMENT '项目查询索引',
  KEY `idx_version` (`design_id`, `version`) COMMENT '版本查询索引',
  KEY `idx_current` (`design_id`, `is_current`) COMMENT '当前版本查询索引',
  KEY `idx_uploader` (`uploader_id`) COMMENT '上传人查询索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='设计图片表 - 设计稿图片存储与版本历史管理';


-- -------------------------------------------------------------------
-- 表7: quality_checks - 质检记录表 (Quality Check Records)
-- -------------------------------------------------------------------
-- 职责: 质量检查记录与验收管理
-- 关联: 项目(projects) + 项目进度(project_timeline) + 质量问题(quality_issues)
-- 闭环: 检查 → 发现问题 → 整改 → 复查通过 (自动状态更新)
-- -------------------------------------------------------------------

CREATE TABLE `quality_checks` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '质检记录ID',

  -- ============ 项目关联 ============
  `project_id` BIGINT UNSIGNED NOT NULL COMMENT '项目ID (projects.id)',
  `project_name` VARCHAR(100) NOT NULL COMMENT '项目名称 (冗余字段)',
  `timeline_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联进度阶段ID (project_timeline.id) - 可选',

  -- ============ 质检基本信息 ============
  `check_date` DATE NOT NULL COMMENT '质检日期',
  `check_category` VARCHAR(50) NOT NULL COMMENT '质检类别 (如:水电工程/泥瓦工程/木工工程)',
  `check_title` VARCHAR(100) NOT NULL COMMENT '质检标题',
  `check_content` TEXT DEFAULT NULL COMMENT '质检内容详细描述',

  -- ============ 质检人员 ============
  `inspector` VARCHAR(50) NOT NULL COMMENT '质检员姓名',
  `inspector_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '质检员ID (team_members.id)',

  -- ============ 质检结果 ============
  `status` ENUM('pending', 'passed', 'failed') NOT NULL DEFAULT 'pending'
    COMMENT '质检状态: pending=待检查, passed=通过, failed=不通过',
  `is_passed` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否通过 (冗余字段,便于统计)',

  -- ============ 质检统计 (冗余字段,应用层维护) ============
  `total_issues` INT NOT NULL DEFAULT 0 COMMENT '发现问题总数',
  `resolved_issues` INT NOT NULL DEFAULT 0 COMMENT '已解决问题数',

  -- ============ 质检图片 ============
  `images` JSON DEFAULT NULL COMMENT '质检现场照片 (JSON数组): ["url1", "url2", ...]',

  -- ============ 备注 ============
  `notes` TEXT DEFAULT NULL COMMENT '质检备注',

  -- ============ 审计字段 ============
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '软删除时间',

  -- ============ 主键 ============
  PRIMARY KEY (`id`),

  -- ============ 索引 ============
  KEY `idx_project_id` (`project_id`) COMMENT '项目查询索引',
  KEY `idx_timeline_id` (`timeline_id`) COMMENT '进度阶段索引',
  KEY `idx_check_date` (`check_date`) COMMENT '日期查询索引',
  KEY `idx_status` (`status`, `deleted_at`) COMMENT '状态查询索引',
  KEY `idx_inspector` (`inspector_id`) COMMENT '质检员查询索引',
  KEY `idx_category` (`check_category`) COMMENT '类别查询索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='质检记录表 - 质量检查与验收记录管理';


-- -------------------------------------------------------------------
-- 表8: quality_issues - 质量问题表 (Quality Issues)
-- -------------------------------------------------------------------
-- 职责: 质量问题上报与整改闭环管理
-- 流程: 上报 → 待整改 → 已整改 → 已解决
-- 级别: low(轻微) / medium(一般) / high(严重) / critical(致命)
-- 自动化: 问题解决后自动更新质检状态
-- -------------------------------------------------------------------

CREATE TABLE `quality_issues` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '问题ID',

  -- ============ 质检关联 ============
  `check_id` BIGINT UNSIGNED NOT NULL COMMENT '关联质检记录ID (quality_checks.id)',
  `project_id` BIGINT UNSIGNED NOT NULL COMMENT '关联项目ID (冗余字段,查询优化)',

  -- ============ 问题基本信息 ============
  `issue_title` VARCHAR(100) NOT NULL COMMENT '问题标题',
  `issue_description` TEXT NOT NULL COMMENT '问题详细描述',
  `issue_level` ENUM('low', 'medium', 'high', 'critical') NOT NULL DEFAULT 'medium'
    COMMENT '问题级别: low=轻微, medium=一般, high=严重, critical=致命',

  -- ============ 问题上报 ============
  `report_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
  `reporter` VARCHAR(50) NOT NULL COMMENT '上报人姓名',
  `reporter_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '上报人ID (team_members.id/users.id)',
  `report_images` JSON DEFAULT NULL COMMENT '问题照片 (JSON数组): ["url1", "url2", ...]',

  -- ============ 问题状态 ============
  `status` ENUM('pending', 'in_progress', 'resolved') NOT NULL DEFAULT 'pending'
    COMMENT '问题状态: pending=待整改, in_progress=整改中, resolved=已解决',

  -- ============ 整改信息 ============
  `resolution_description` TEXT DEFAULT NULL COMMENT '整改方案/措施描述',
  `resolution_images` JSON DEFAULT NULL COMMENT '整改后照片 (JSON数组)',
  `resolve_time` TIMESTAMP NULL DEFAULT NULL COMMENT '整改完成时间',
  `resolver` VARCHAR(50) DEFAULT NULL COMMENT '整改人姓名',
  `resolver_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '整改人ID (team_members.id)',

  -- ============ 审计字段 ============
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '软删除时间',

  -- ============ 主键 ============
  PRIMARY KEY (`id`),

  -- ============ 索引 ============
  KEY `idx_check_id` (`check_id`) COMMENT '质检记录查询索引',
  KEY `idx_project_id` (`project_id`) COMMENT '项目查询索引',
  KEY `idx_status` (`status`, `deleted_at`) COMMENT '状态查询索引',
  KEY `idx_level` (`issue_level`) COMMENT '问题级别索引',
  KEY `idx_reporter` (`reporter_id`) COMMENT '上报人查询索引',
  KEY `idx_resolver` (`resolver_id`) COMMENT '整改人查询索引',
  KEY `idx_report_time` (`report_time`) COMMENT '上报时间索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='质量问题表 - 问题上报、整改、闭环管理';


-- ===================================================================
-- 表关系说明
-- ===================================================================
--
-- 1. projects 与 customers (多对一)
--    - 关联字段: projects.customer_id = customers.id
--    - 冗余优化: projects.customer_name 避免频繁 JOIN customers 表
--    - 级联逻辑: 删除客户前检查是否有关联项目
--
-- 2. projects 与 project_budgets (一对多)
--    - 关联字段: project_budgets.project_id = projects.id
--    - 自动聚合: 定期更新 projects.total_budget = SUM(project_budgets.budget_amount)
--    - 自动聚合: 定期更新 projects.total_expense = SUM(project_budgets.actual_expense)
--
-- 3. projects 与 project_timeline (一对多)
--    - 关联字段: project_timeline.project_id = projects.id
--    - 标准8阶段: stage_order 1-8 对应标准施工阶段
--    - 自动计算: projects.progress_percentage = AVG(project_timeline.progress_percentage)
--
-- 4. projects 与 project_team_assignments (多对多)
--    - 中间表: project_team_assignments
--    - 关联关系: projects.id → project_team_assignments.project_id → team_members.id
--    - 支持复杂: 一个项目多个岗位, 每个岗位多个人员, 一个人多个项目
--
-- 5. projects 与 project_designs (一对多)
--    - 关联字段: project_designs.project_id = projects.id
--    - 多维分类: 按房间/楼层/视图角度灵活分类
--
-- 6. project_designs 与 design_images (一对多)
--    - 关联字段: design_images.design_id = project_designs.id
--    - 版本控制: 通过 version + is_current 字段实现
--    - 冗余设计: design_images.project_id 提升查询效率
--
-- 7. projects 与 quality_checks (一对多)
--    - 关联字段: quality_checks.project_id = projects.id
--    - 阶段关联: quality_checks.timeline_id → project_timeline.id (可选)
--
-- 8. quality_checks 与 quality_issues (一对多)
--    - 关联字段: quality_issues.check_id = quality_checks.id
--    - 自动更新: 所有问题解决后自动更新质检状态
--
-- 9. 数据一致性保证 (应用层逻辑)
--    - 定期任务: 每10分钟更新项目的统计字段 (预算总额/进度百分比)
--    - 触发逻辑: 质量问题状态变更时检查并更新质检状态
--    - 约束检查: 删除前置检查 (删除项目前检查是否有关联数据)
--
-- ===================================================================
-- 业务场景SQL示例
-- ===================================================================

-- 场景1: 创建新项目并初始化标准进度阶段
-- Step 1: 创建项目
-- INSERT INTO projects (project_name, site_name, site_address, customer_id, customer_name,
--                       start_date, end_date, total_budget, created_by)
-- VALUES ('现代简约三居室装修', '阳光花园1201室', '北京市朝阳区建国路88号2单元1201',
--         1, '张先生', '2024-03-01', '2024-06-30', 280000.00, 1);

-- Step 2: 初始化8大标准施工阶段
-- INSERT INTO project_timeline (project_id, stage_order, stage_title, stage_description, planned_date)
-- VALUES
--   (1, 1, '拆除工程', '拆除旧墙体、拆除旧地板、垃圾清运', '2024-03-01'),
--   (1, 2, '水电改造', '水电管道铺设、开关插座定位', '2024-03-10'),
--   (1, 3, '泥瓦工程', '墙面找平、地面铺贴', '2024-03-20'),
--   (1, 4, '木工工程', '吊顶、柜体制作', '2024-04-15'),
--   (1, 5, '油漆工程', '墙面乳胶漆、木器漆', '2024-05-10'),
--   (1, 6, '安装工程', '灯具、洁具安装', '2024-06-01'),
--   (1, 7, '软装配饰', '家具进场、窗帘布艺', '2024-06-15'),
--   (1, 8, '竣工验收', '全面质检、业主验收', '2024-06-25');

-- Step 3: 初始化预算明细
-- INSERT INTO project_budgets (project_id, budget_category, category_code, budget_amount, description)
-- VALUES
--   (1, '拆除工程', 'DEMO', 8000.00, '拆除旧墙体、垃圾清运费用'),
--   (1, '水电改造', 'ELEC', 35000.00, '水电材料费+人工费'),
--   (1, '泥瓦工程', 'TILE', 45000.00, '瓷砖、水泥、沙子等'),
--   (1, '木工工程', 'WOOD', 42000.00, '吊顶、柜体等木作'),
--   (1, '油漆工程', 'PAINT', 25000.00, '墙面乳胶漆、木器漆'),
--   (1, '主材采购', 'MAT', 80000.00, '卫浴、橱柜、地板等主材'),
--   (1, '软装配饰', 'SOFT', 35000.00, '窗帘、灯具、家具等'),
--   (1, '设计费用', 'DESIGN', 10000.00, '设计服务费');

-- 场景2: 分配项目团队成员 (支持一人多岗位,多岗位多人)
-- INSERT INTO project_team_assignments (project_id, member_id, role, assigned_by)
-- VALUES
--   -- 设计师团队 (2名)
--   (1, 101, 'designer', 1),    -- 张设计师
--   (1, 102, 'designer', 1),    -- 李设计师
--   -- 项目经理 (1名)
--   (1, 201, 'manager', 1),     -- 王项目经理
--   -- 工长团队 (2名)
--   (1, 301, 'foreman', 1),     -- 刘工长
--   (1, 302, 'foreman', 1),     -- 陈工长
--   -- 监理 (1名)
--   (1, 401, 'supervisor', 1);  -- 赵监理

-- 场景3: 创建设计稿分类并上传图片
-- Step 1: 创建设计稿分类
-- INSERT INTO project_designs (project_id, category_type, category_name, sort_order, description)
-- VALUES
--   -- 按房间分类
--   (1, 'room', '客厅', 1, '客厅空间设计'),
--   (1, 'room', '主卧', 2, '主卧室空间设计'),
--   (1, 'room', '次卧', 3, '次卧室空间设计'),
--   (1, 'room', '厨房', 4, '厨房空间设计'),
--   (1, 'room', '卫生间', 5, '卫生间空间设计'),
--   -- 按楼层分类
--   (1, 'floor', '1层', 1, '一层空间设计'),
--   (1, 'floor', '2层', 2, '二层空间设计'),
--   -- 按视图分类
--   (1, 'view', '平面图', 1, '平面布置图'),
--   (1, 'view', '立面图', 2, '立面效果图'),
--   (1, 'view', '效果图', 3, '3D效果图');

-- Step 2: 上传设计图片 (支持版本历史)
-- INSERT INTO design_images (design_id, project_id, image_name, image_url, version, is_current, uploader, uploader_id)
-- VALUES
--   -- 客厅平面图版本历史
--   (1, 1, '客厅平面图-v1', '/uploads/project1/living-room-v1.jpg', 1, FALSE, '张设计师', 101),
--   (1, 1, '客厅平面图-v2', '/uploads/project1/living-room-v2.jpg', 2, FALSE, '张设计师', 101),
--   (1, 1, '客厅平面图-v3', '/uploads/project1/living-room-v3.jpg', 3, TRUE, '张设计师', 101),
--   -- 客厅效果图
--   (3, 1, '客厅3D效果图', '/uploads/project1/living-room-3d.jpg', 1, TRUE, '李设计师', 102);

-- 场景4: 质量检查与问题管理
-- Step 1: 创建质检记录
-- INSERT INTO quality_checks (project_id, project_name, check_date, check_category,
--                            check_title, check_content, inspector, inspector_id)
-- VALUES (1, '现代简约三居室装修', '2024-04-25', '水电工程',
--         '水电改造节点验收', '检查水电管道铺设、开关插座定位、防水处理等',
--         '赵监理', 401);

-- Step 2: 发现质量问题
-- INSERT INTO quality_issues (check_id, project_id, issue_title, issue_description,
--                            issue_level, reporter, reporter_id, report_images)
-- VALUES
--   (1, 1, '卫生间防水高度不足', '主卧卫生间淋浴区防水层高度仅1.5米，不符合规范要求的1.8米',
--    'high', '赵监理', 401,
--    '["/uploads/project1/issue1-1.jpg", "/uploads/project1/issue1-2.jpg"]'),
--   (1, 1, '插座位置不合理', '客厅沙发背景墙插座位置与沙发冲突，需要调整',
--    'medium', '赵监理', 401,
--    '["/uploads/project1/issue2-1.jpg"]');

-- Step 3: 整改并记录
-- UPDATE quality_issues
-- SET status='resolved', resolution_description='已按要求重新做防水到1.8米高度',
--     resolution_images='["/uploads/project1/resolved1-1.jpg"]', resolve_time=NOW(),
--     resolver='陈工长', resolver_id=302
-- WHERE id=1;

-- 场景5: 查询项目的完整信息 (包含所有关联数据)
-- SELECT
--   p.*,
--   c.phone AS customer_phone,
--   c.email AS customer_email,
--   -- 预算统计
--   (SELECT COUNT(*) FROM project_budgets pb WHERE pb.project_id=p.id AND pb.deleted_at IS NULL) AS budget_count,
--   (SELECT SUM(pb.budget_amount) FROM project_budgets pb WHERE pb.project_id=p.id AND pb.deleted_at IS NULL) AS total_budget_check,
--   -- 进度统计
--   (SELECT COUNT(*) FROM project_timeline pt WHERE pt.project_id=p.id AND pt.status='completed' AND pt.deleted_at IS NULL) AS completed_stages,
--   (SELECT COUNT(*) FROM project_timeline pt WHERE pt.project_id=p.id AND pt.deleted_at IS NULL) AS total_stages,
--   -- 团队统计
--   (SELECT COUNT(*) FROM project_team_assignments pta WHERE pta.project_id=p.id AND pta.is_active=TRUE AND pta.deleted_at IS NULL) AS team_count,
--   -- 质检统计
--   (SELECT COUNT(*) FROM quality_checks qc WHERE qc.project_id=p.id AND qc.deleted_at IS NULL) AS quality_checks_count,
--   (SELECT COUNT(*) FROM quality_issues qi WHERE qi.project_id=p.id AND qi.status='resolved' AND qi.deleted_at IS NULL) AS resolved_issues_count,
--   -- 设计稿统计
--   (SELECT COUNT(*) FROM project_designs pd WHERE pd.project_id=p.id AND pd.deleted_at IS NULL) AS design_categories_count,
--   (SELECT COUNT(*) FROM design_images di WHERE di.project_id=p.id AND di.is_current=TRUE AND di.deleted_at IS NULL) AS current_designs_count
-- FROM projects p
-- LEFT JOIN customers c ON p.customer_id=c.id
-- WHERE p.id=1 AND p.deleted_at IS NULL;

-- 场景6: 查询所有进行中的项目 (包含进度和预算信息)
-- SELECT
--   p.id,
--   p.project_name,
--   p.site_name,
--   p.customer_name,
--   p.progress_percentage,
--   p.total_budget,
--   p.total_expense,
--   DATEDIFF(p.end_date, CURDATE()) AS remaining_days,
--   -- 预算执行率
--   CASE WHEN p.total_budget > 0
--        THEN ROUND(p.total_expense / p.total_budget * 100, 2)
--        ELSE 0
--   END AS budget_usage_rate,
--   -- 进度阶段信息
--   pt.stage_title AS current_stage,
--   pt.status AS stage_status,
--   -- 团队项目经理
--   tm.name AS project_manager_name,
--   tm.phone AS project_manager_phone
-- FROM projects p
-- LEFT JOIN project_timeline pt ON p.id = pt.project_id
--                              AND pt.status IN ('in_progress', 'pending')
--                              AND pt.deleted_at IS NULL
-- LEFT JOIN project_team_assignments pta ON p.id = pta.project_id
--                                        AND pta.role='manager'
--                                        AND pta.is_active=TRUE
--                                        AND pta.deleted_at IS NULL
-- LEFT JOIN team_members tm ON pta.member_id = tm.id
-- WHERE p.status='in_progress' AND p.deleted_at IS NULL
-- ORDER BY p.end_date ASC;

-- 场景7: 查询所有未解决的质量问题 (按严重程度排序)
-- SELECT
--   qi.id,
--   qi.issue_title,
--   qi.issue_level,
--   qi.status,
--   qi.report_time,
--   qi.reporter AS reporter_name,
--   p.project_name,
--   p.site_name,
--   qc.check_title,
--   qc.check_date,
--   -- 问题处理时效
--   DATEDIFF(NOW(), qi.report_time) AS days_pending,
--   CASE qi.issue_level
--     WHEN 'critical' THEN 4
--     WHEN 'high' THEN 3
--     WHEN 'medium' THEN 2
--     WHEN 'low' THEN 1
--   END AS priority_score
-- FROM quality_issues qi
-- JOIN quality_checks qc ON qi.check_id = qc.id
-- JOIN projects p ON qi.project_id = p.id
-- WHERE qi.status IN ('pending', 'in_progress') AND qi.deleted_at IS NULL
-- ORDER BY priority_score DESC, qi.report_time ASC;

-- 场景8: 查询某个团队成员参与的所有项目
-- SELECT
--   p.id AS project_id,
--   p.project_name,
--   p.site_name,
--   p.customer_name,
--   p.status AS project_status,
--   p.start_date,
--   p.end_date,
--   pta.role AS member_role,
--   pta.assigned_at,
--   pta.is_active,
--   -- 项目进度
--   p.progress_percentage,
--   -- 项目预算
--   p.total_budget,
--   p.total_expense,
--   -- 项目时效
--   CASE
--     WHEN p.status='completed' THEN DATEDIFF(p.actual_end_date, p.actual_start_date)
--     WHEN p.status='in_progress' THEN DATEDIFF(CURDATE(), p.actual_start_date)
--     ELSE NULL
--   END AS project_duration
-- FROM projects p
-- JOIN project_team_assignments pta ON p.id = pta.project_id
-- WHERE pta.member_id = 101 -- 张设计师
--   AND p.deleted_at IS NULL
--   AND pta.deleted_at IS NULL
-- ORDER BY pta.assigned_at DESC;

-- 场景9: 质检状态自动化更新 (应用层逻辑)
-- 当所有质量问题解决后，自动更新质检状态
-- UPDATE quality_checks qc
-- SET status='passed',
--     is_passed=TRUE,
--     resolved_issues = (
--       SELECT COUNT(*) FROM quality_issues qi
--       WHERE qi.check_id=qc.id AND qi.status='resolved' AND qi.deleted_at IS NULL
--     )
-- WHERE qc.id IN (
--   SELECT check_id FROM quality_issues qi
--   WHERE qi.check_id=qc.id
--     AND qi.deleted_at IS NULL
--   GROUP BY check_id
--   HAVING SUM(CASE WHEN qi.status='resolved' THEN 1 ELSE 0 END) = COUNT(*)
-- ) AND qc.deleted_at IS NULL;

-- 场景10: 项目进度自动计算 (应用层逻辑)
-- 定期更新项目进度百分比
-- UPDATE projects p
-- SET progress_percentage = (
--   SELECT COALESCE(
--     ROUND(AVG(pt.progress_percentage), 0), 0
--   )
--   FROM project_timeline pt
--   WHERE pt.project_id = p.id AND pt.deleted_at IS NULL
-- )
-- WHERE p.deleted_at IS NULL;

-- ===================================================================
-- 数据初始化 (开发测试用)
-- ===================================================================
-- 注意: 以下INSERT语句仅用于开发测试, 生产环境请根据实际需求调整

-- 初始化示例项目
INSERT INTO `projects` (
  `project_name`, `site_name`, `site_address`,
  `customer_id`, `customer_name`,
  `status`, `start_date`, `end_date`,
  `total_budget`, `description`, `created_by`
) VALUES
('现代简约三居室装修', '阳光花园1201室', '北京市朝阳区建国路88号2单元1201',
 1, '张先生', 'in_progress', '2024-03-01', '2024-06-30', 280000.00,
 '现代简约风格三居室装修，包含全套水电、泥瓦、木工、油漆工程', 1),
('北欧风格复式楼装修', '翡翠城别墅区B12栋', '上海市浦东新区世纪大道1688号',
 2, '李女士', 'planning', '2024-04-01', '2024-09-30', 450000.00,
 '北欧风格复式楼，两层空间改造装修', 1),
('新中式四居室装修', '江南水岸8号楼301', '深圳市南山区深南大道9999号',
 3, '王总', 'completed', '2023-10-01', '2024-02-28', 380000.00,
 '新中式风格大平层，全套装修工程', 1);

-- 初始化8大标准施工阶段 (为每个项目)
INSERT INTO `project_timeline` (
  `project_id`, `stage_order`, `stage_title`, `stage_description`,
  `planned_date`, `planned_duration`, `status`
) SELECT
  p.id,
  s.stage_order,
  s.stage_title,
  s.stage_description,
  DATE_ADD(p.start_date, INTERVAL s.day_offset DAY) AS planned_date,
  s.duration,
  CASE
    WHEN p.status='completed' THEN 'completed'
    WHEN p.status='in_progress' AND s.stage_order=3 THEN 'in_progress'
    WHEN p.status='in_progress' AND s.stage_order<3 THEN 'completed'
    WHEN p.status='planning' THEN 'pending'
    ELSE 'pending'
  END AS status
FROM projects p
CROSS JOIN (
  SELECT 1 AS stage_order, '拆除工程' AS stage_title, '拆除旧墙体、拆除旧地板、垃圾清运' AS stage_description, 0 AS day_offset, 5 AS duration
  UNION SELECT 2, '水电改造', '水电管道铺设、开关插座定位', 5, 10
  UNION SELECT 3, '泥瓦工程', '墙面找平、地面铺贴', 15, 15
  UNION SELECT 4, '木工工程', '吊顶、柜体制作', 30, 15
  UNION SELECT 5, '油漆工程', '墙面乳胶漆、木器漆', 45, 10
  UNION SELECT 6, '安装工程', '灯具、洁具安装', 55, 7
  UNION SELECT 7, '软装配饰', '家具进场、窗帘布艺', 62, 5
  UNION SELECT 8, '竣工验收', '全面质检、业主验收', 67, 3
) s;

-- 初始化预算类别 (为每个项目)
INSERT INTO `project_budgets` (
  `project_id`, `budget_category`, `category_code`, `budget_amount`, `description`
) SELECT
  p.id,
  s.budget_category,
  s.category_code,
  ROUND(p.total_budget * s.ratio, 2) AS budget_amount,
  s.description
FROM projects p
CROSS JOIN (
  SELECT '拆除工程' AS budget_category, 'DEMO' AS category_code, 0.03 AS ratio, '拆除旧墙体、垃圾清运费用' AS description
  UNION SELECT '水电改造', 'ELEC', 0.15, '水电材料费+人工费'
  UNION SELECT '泥瓦工程', 'TILE', 0.20, '瓷砖、水泥、沙子等'
  UNION SELECT '木工工程', 'WOOD', 0.18, '吊顶、柜体等木作'
  UNION SELECT '油漆工程', 'PAINT', 0.10, '墙面乳胶漆、木器漆'
  UNION SELECT '主材采购', 'MAT', 0.25, '卫浴、橱柜、地板等主材'
  UNION SELECT '软装配饰', 'SOFT', 0.07, '窗帘、灯具、家具等'
  UNION SELECT '设计费用', 'DESIGN', 0.02, '设计服务费'
) s;

-- ===================================================================
-- 建表完成
-- ===================================================================
-- 下一步: 讨论P1批次业务表 (inspection_records, notifications, contracts, etc.)
-- ===================================================================