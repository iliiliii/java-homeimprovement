-- ===================================================================
-- 装修管理系统 - 核心表设计
-- Core Tables for Home Improvement Management System
-- ===================================================================
-- 设计原则:
-- 1. 三表架构: users(认证中心) + customers(客户) + team_members(员工)
-- 2. 无外键约束: 应用层保证数据一致性
-- 3. 软删除模式: 使用 deleted_at 字段
-- 4. 手机号关联: phone 作为用户绑定的核心字段
-- 5. 多态关联: related_type + related_id 支持灵活的角色绑定
-- ===================================================================

-- -------------------------------------------------------------------
-- 表1: users - 用户认证表 (Authentication Center)
-- -------------------------------------------------------------------
-- 职责: 统一管理所有用户的登录认证信息
-- 支持: 微信小程序登录(openid) + B端后台登录(username/password)
-- 关联: 通过 phone 字段与 customers/team_members 关联
-- -------------------------------------------------------------------

CREATE TABLE `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',

  -- ============ 小程序登录字段 ============
  `openid` VARCHAR(64) DEFAULT NULL COMMENT '微信OpenID (小程序登录唯一标识)',
  `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信UnionID (同一开放平台下的唯一标识)',
  `session_key` VARCHAR(128) DEFAULT NULL COMMENT '微信会话密钥 (用于数据解密)',

  -- ============ B端后台登录字段 ============
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名 (B端登录账号)',
  `password_hash` VARCHAR(255) DEFAULT NULL COMMENT '密码哈希 (bcrypt加密)',

  -- ============ 通用身份字段 ============
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号 (关键绑定字段, 唯一)',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称 (小程序自动获取或手动设置)',

  -- ============ 多态角色关联 ============
  `related_type` ENUM('customer', 'staff') DEFAULT NULL COMMENT '关联类型: customer=客户, staff=员工, NULL=未绑定',
  `related_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联ID: customer_id 或 team_member_id',

  -- ============ 权限与状态 ============
  `is_admin` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否管理员 (B端后台管理员权限)',
  `status` ENUM('active', 'inactive', 'blocked') NOT NULL DEFAULT 'active' COMMENT '账号状态: active=正常, inactive=未激活, blocked=已封禁',

  -- ============ 审计字段 ============
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '软删除时间 (NULL=未删除)',
  `last_login_at` TIMESTAMP NULL DEFAULT NULL COMMENT '最后登录时间',
  `bound_at` TIMESTAMP NULL DEFAULT NULL COMMENT '角色绑定时间 (从未绑定变为已绑定的时刻)',

  -- ============ 主键 ============
  PRIMARY KEY (`id`),

  -- ============ 唯一索引 ============
  UNIQUE KEY `uk_openid` (`openid`) COMMENT '微信OpenID唯一索引',
  UNIQUE KEY `uk_phone` (`phone`) COMMENT '手机号唯一索引',
  UNIQUE KEY `uk_username` (`username`) COMMENT '用户名唯一索引',

  -- ============ 普通索引 ============
  KEY `idx_related` (`related_type`, `related_id`) COMMENT '多态关联索引',
  KEY `idx_status` (`status`, `deleted_at`) COMMENT '状态查询索引',
  KEY `idx_unbound` (`related_type`, `deleted_at`) COMMENT '未绑定用户查询索引 (related_type IS NULL)',
  KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引',

  -- ============ 数据一致性约束 ============
  CONSTRAINT `chk_related_consistency` CHECK (
    (`related_type` IS NULL AND `related_id` IS NULL) OR
    (`related_type` IS NOT NULL AND `related_id` IS NOT NULL)
  ) COMMENT '确保 related_type 和 related_id 成对出现'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户认证表 - 统一管理小程序和B端登录';


-- -------------------------------------------------------------------
-- 表2: customers - 客户表 (Customer CRM)
-- -------------------------------------------------------------------
-- 职责: 管理所有客户的业务信息和项目数据
-- 来源: B端管理员录入 或 小程序用户注册后自动创建
-- 绑定: 通过 phone 字段与 users 表关联
-- -------------------------------------------------------------------

CREATE TABLE `customers` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '客户ID',

  -- ============ 客户基本信息 ============
  `name` VARCHAR(50) NOT NULL COMMENT '客户姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号 (关键绑定字段)',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `company` VARCHAR(100) DEFAULT NULL COMMENT '公司名称 (企业客户)',

  -- ============ 客户分类 ============
  `customer_type` ENUM('individual', 'enterprise') NOT NULL DEFAULT 'individual' COMMENT '客户类型: individual=个人, enterprise=企业',
  `source` VARCHAR(50) DEFAULT NULL COMMENT '客户来源: 线上广告/线下推广/老客介绍/其他',
  `level` ENUM('normal', 'vip', 'svip') NOT NULL DEFAULT 'normal' COMMENT '客户等级: normal=普通, vip=VIP, svip=超级VIP',
  `tags` JSON DEFAULT NULL COMMENT '客户标签 (JSON数组): ["高端客户","别墅装修"]',

  -- ============ 小程序绑定状态 ============
  `is_registered` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已在小程序注册: true=已注册, false=仅B端录入',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联的用户ID (users.id), NULL表示未注册',

  -- ============ 业务统计 (冗余字段, 提升查询性能) ============
  `project_count` INT NOT NULL DEFAULT 0 COMMENT '项目数量 (统计字段)',
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计合同金额 (单位:元)',

  -- ============ 备注与扩展 ============
  `notes` TEXT DEFAULT NULL COMMENT '客户备注信息',
  `extra_data` JSON DEFAULT NULL COMMENT '扩展数据 (JSON对象): 自定义字段',

  -- ============ 审计字段 ============
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID (users.id)',
  `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '软删除时间',

  -- ============ 主键 ============
  PRIMARY KEY (`id`),

  -- ============ 索引 ============
  KEY `idx_phone` (`phone`) COMMENT '手机号查���索引 (用于用户绑定匹配)',
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
  KEY `idx_is_registered` (`is_registered`, `deleted_at`) COMMENT '注册状态查询索引',
  KEY `idx_level` (`level`) COMMENT '客户等级索引',
  KEY `idx_customer_type` (`customer_type`) COMMENT '客户类型索引',
  KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表 - CRM客户关系管理';


-- -------------------------------------------------------------------
-- 表3: team_members - 团队成员表 (Staff Management)
-- -------------------------------------------------------------------
-- 职责: 管理所有员工的基本信息和项目分配
-- 角色: 设计师/项目经理/工长/监理
-- 绑定: 通过 phone 字段与 users 表关联
-- -------------------------------------------------------------------

CREATE TABLE `team_members` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '员工ID',

  -- ============ 员工基本信息 ============
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号 (关键绑定字段)',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',

  -- ============ 岗位信息 ============
  `role` ENUM('designer', 'manager', 'foreman', 'supervisor') NOT NULL COMMENT '角色: designer=设计师, manager=项目经理, foreman=工长, supervisor=监理',
  `department` VARCHAR(50) DEFAULT NULL COMMENT '所属部门',
  `job_title` VARCHAR(50) DEFAULT NULL COMMENT '职位名称',

  -- ============ 员工详细信息 ============
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号 (脱敏存储)',
  `hire_date` DATE DEFAULT NULL COMMENT '入职日期',
  `resign_date` DATE DEFAULT NULL COMMENT '离职日期 (NULL表示在职)',

  -- ============ 小程序绑定状态 ============
  `is_registered` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已在小程序注册',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联的用户ID (users.id)',

  -- ============ 员工状态 ============
  `status` ENUM('active', 'inactive', 'resigned') NOT NULL DEFAULT 'active' COMMENT '状态: active=在职, inactive=停职, resigned=离职',
  `project_count` INT NOT NULL DEFAULT 0 COMMENT '参与项目数量 (统计字段)',

  -- ============ 审计字段 ============
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID (users.id)',
  `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '软删除时间',

  -- ============ 主键 ============
  PRIMARY KEY (`id`),

  -- ============ 索引 ============
  KEY `idx_phone` (`phone`) COMMENT '手机号查询索引',
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
  KEY `idx_role` (`role`, `status`) COMMENT '角色与状态组合索引',
  KEY `idx_is_registered` (`is_registered`, `deleted_at`) COMMENT '注册状态查询索引',
  KEY `idx_status` (`status`) COMMENT '员工状态索引',
  KEY `idx_department` (`department`) COMMENT '部门查询索引',
  KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队成员表 - 员工信息管理';


-- ===================================================================
-- 表关系说明
-- ===================================================================
--
-- 1. users ↔ customers (一对一/一对零)
--    - 关联字段: users.phone = customers.phone
--    - 绑定标志: users.related_type='customer' AND users.related_id=customers.id
--    - 注册标志: customers.is_registered=true AND customers.user_id=users.id
--    - 双向维护: 小程序注册后自动匹配并更新双方字段
--
-- 2. users ↔ team_members (一对一/一对零)
--    - 关联字段: users.phone = team_members.phone
--    - 绑定标志: users.related_type='staff' AND users.related_id=team_members.id
--    - 注册标志: team_members.is_registered=true AND team_members.user_id=users.id
--    - 双向维护: 小程序注册后自动匹配并更新双方字段
--
-- 3. 数据一致性保证 (应用层逻辑)
--    - 当 customers.phone 存在对应 users 记录时, 自动更新双方绑定字段
--    - 当 team_members.phone 存在对应 users 记录时, 自动更新双方绑定字段
--    - 定期运行一致性检查脚本, 确保 phone 关联的数据同步
--
-- 4. 查询绑定状态
--    - 未绑定用户: SELECT * FROM users WHERE related_type IS NULL;
--    - 已绑定客户: SELECT * FROM users WHERE related_type='customer';
--    - 已绑定员工: SELECT * FROM users WHERE related_type='staff';
--    - 未注册客户: SELECT * FROM customers WHERE is_registered=FALSE;
--    - 未注册员工: SELECT * FROM team_members WHERE is_registered=FALSE;
--
-- ===================================================================
-- 业务场景SQL示例
-- ===================================================================

-- 场景1: B端管理员录入客户 (客户尚未在小程序注册)
-- INSERT INTO customers (name, phone, email, address, is_registered, created_by)
-- VALUES ('张先生', '13800138000', 'zhang@example.com', '北京市朝阳区...', FALSE, 1);

-- 场景2: 小程序用户注册 (自动匹配已存在的客户记录)
-- Step 1: 创建 users 记录
-- INSERT INTO users (openid, phone, nickname, avatar_url)
-- VALUES ('wx_openid_123', '13800138000', '张先生', 'https://...');
--
-- Step 2: 查找匹配的 customer 记录
-- SELECT id FROM customers WHERE phone='13800138000' AND deleted_at IS NULL;
--
-- Step 3: 双向绑定更新
-- UPDATE users SET related_type='customer', related_id=?, bound_at=NOW() WHERE id=?;
-- UPDATE customers SET is_registered=TRUE, user_id=? WHERE id=?;

-- 场景3: 查询某个客户的完整信息 (包含用户登录信息)
-- SELECT
--   c.*,
--   u.openid,
--   u.nickname,
--   u.avatar_url,
--   u.last_login_at,
--   u.status AS user_status
-- FROM customers c
-- LEFT JOIN users u ON c.user_id = u.id
-- WHERE c.id = ? AND c.deleted_at IS NULL;

-- 场景4: 查询所有未在小程序注册的客户 (用于B端管理员催促客户注册)
-- SELECT id, name, phone, email, created_at
-- FROM customers
-- WHERE is_registered = FALSE AND deleted_at IS NULL
-- ORDER BY created_at DESC;

-- ===================================================================
-- 数据初始化 (可选)
-- ===================================================================
-- 注意: 以下INSERT语句仅用于开发测试, 生产环境请根据实际需求调整

-- 初始化管理员账号
INSERT INTO `users` (`username`, `password_hash`, `phone`, `nickname`, `is_admin`, `status`)
VALUES
('admin', '$2b$10$dummy_hashed_password_here', '18800000000', '系统管理员', TRUE, 'active');

-- 初始化示例客户
INSERT INTO `customers` (`name`, `phone`, `email`, `address`, `customer_type`, `level`, `is_registered`, `created_by`)
VALUES
('张先生', '13800138001', 'zhang@example.com', '北京市朝阳区建国路88号', 'individual', 'vip', FALSE, 1),
('李女士', '13800138002', 'li@example.com', '上海市浦东新区世纪大道200号', 'individual', 'normal', FALSE, 1),
('王总', '13800138003', 'wang@example.com', '深圳市南山区科技园北区', 'enterprise', 'svip', FALSE, 1);

-- 初始化示例团队成员
INSERT INTO `team_members` (`name`, `phone`, `email`, `role`, `department`, `status`, `is_registered`, `created_by`)
VALUES
('张设计', '13800138011', 'designer.zhang@example.com', 'designer', '设计部', 'active', FALSE, 1),
('李经理', '13800138012', 'manager.li@example.com', 'manager', '项目部', 'active', FALSE, 1),
('王工长', '13800138013', 'foreman.wang@example.com', 'foreman', '施工部', 'active', FALSE, 1),
('赵监理', '13800138014', 'supervisor.zhao@example.com', 'supervisor', '质检部', 'active', FALSE, 1);

-- ===================================================================
-- 建表完成
-- ===================================================================
-- 下一步: 讨论后续业务表 (projects, quality_checks, etc.)
-- ===================================================================
