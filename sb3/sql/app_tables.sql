-- =====================================================
-- 小程序认证相关表
-- 创建时间: 2025-12-08
-- 说明: 用于小程序登录认证、Token管理、审计日志等
-- =====================================================

-- ----------------------------
-- 1. 小程序登录日志表
-- ----------------------------
DROP TABLE IF EXISTS `app_login_logs`;
CREATE TABLE `app_login_logs` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `user_type` varchar(20) NOT NULL COMMENT '用户类型：customer/staff',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `login_type` varchar(20) NOT NULL COMMENT '登录类型：wechat/sms/password',
  `login_ip` varchar(50) DEFAULT NULL COMMENT '登录IP',
  `device_id` varchar(100) DEFAULT NULL COMMENT '设备唯一标识',
  `device_info` text COMMENT '设备信息（JSON）',
  `login_status` varchar(20) DEFAULT 'success' COMMENT '登录状态：success/failed',
  `fail_reason` varchar(200) DEFAULT NULL COMMENT '失败原因',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_type`, `user_id`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小程序登录日志表';

-- ----------------------------
-- 2. Token管理表
-- ----------------------------
DROP TABLE IF EXISTS `app_tokens`;
CREATE TABLE `app_tokens` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `user_type` varchar(20) NOT NULL COMMENT '用户类型：customer/staff',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `access_token` varchar(500) NOT NULL COMMENT 'Access Token',
  `refresh_token` varchar(500) NOT NULL COMMENT 'Refresh Token',
  `device_id` varchar(100) NOT NULL COMMENT '设备唯一标识',
  `access_token_expire` datetime NOT NULL COMMENT 'Access Token过期时间',
  `refresh_token_expire` datetime NOT NULL COMMENT 'Refresh Token过期时间',
  `is_revoked` tinyint(1) DEFAULT 0 COMMENT '是否已撤销',
  `revoke_time` datetime DEFAULT NULL COMMENT '撤销时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `last_use_time` datetime DEFAULT NULL COMMENT '最后使用时间',
  PRIMARY KEY (`id`),
  KEY `idx_access_token` (`access_token`(255)),
  KEY `idx_refresh_token` (`refresh_token`(255)),
  KEY `idx_user` (`user_type`, `user_id`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token管理表';

-- ----------------------------
-- 3. 短信验证码表
-- ----------------------------
DROP TABLE IF EXISTS `app_sms_codes`;
CREATE TABLE `app_sms_codes` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `code` varchar(10) NOT NULL COMMENT '验证码',
  `type` varchar(20) DEFAULT 'login' COMMENT '类型：login/bind/reset',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `is_used` tinyint(1) DEFAULT 0 COMMENT '是否已使用',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_phone_code` (`phone`, `code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码表';

-- ----------------------------
-- 4. 微信绑定表
-- ----------------------------
DROP TABLE IF EXISTS `app_wechat_bindings`;
CREATE TABLE `app_wechat_bindings` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `open_id` varchar(100) NOT NULL COMMENT '微信openId',
  `union_id` varchar(100) DEFAULT NULL COMMENT '微信unionId',
  `user_type` varchar(20) NOT NULL COMMENT '用户类型：customer/staff',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `phone` varchar(20) DEFAULT NULL COMMENT '绑定手机号',
  `nickname` varchar(100) DEFAULT NULL COMMENT '微信昵称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '微信头像',
  `session_key` varchar(100) DEFAULT NULL COMMENT '会话密钥',
  `bind_time` datetime NOT NULL COMMENT '绑定时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`),
  KEY `idx_user` (`user_type`, `user_id`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信绑定表';

-- ----------------------------
-- 5. 审计日志表
-- ----------------------------
DROP TABLE IF EXISTS `app_audit_logs`;
CREATE TABLE `app_audit_logs` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `user_type` varchar(20) NOT NULL COMMENT '用户类型：customer/staff',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `project_id` varchar(50) DEFAULT NULL COMMENT '项目ID',
  `action` varchar(100) NOT NULL COMMENT '操作类型',
  `resource_type` varchar(50) DEFAULT NULL COMMENT '资源类型',
  `resource_id` varchar(50) DEFAULT NULL COMMENT '资源ID',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `device_id` varchar(100) DEFAULT NULL COMMENT '设备ID',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `request_params` text COMMENT '请求参数',
  `response_code` int DEFAULT NULL COMMENT '响应码',
  `execute_time` int DEFAULT NULL COMMENT '执行时长（毫秒）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_type`, `user_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- =====================================================
-- 字典数据
-- =====================================================

-- 小程序配置字典类型
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (200, '小程序配置', 'app_config', '0', 'admin', NOW(), '', NULL, '小程序相关配置参数');

-- 小程序配置字典数据
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES 
(1, 'Token有效期（小时）', '2', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, 'Access Token有效期'),
(2, 'RefreshToken有效期（天）', '7', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, 'Refresh Token有效期'),
(3, '验证码有效期（分钟）', '5', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '短信验证码有效期'),
(4, '验证码长度', '6', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '短信验证码长度'),
(5, '单次上传图片数量', '9', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '单次最多上传图片数'),
(6, '图片大小限制（MB）', '5', 'app_config', '', '', 'N', '0', 'admin', NOW(), '', NULL, '单张图片大小限制');

-- 问题类型字典类型
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (201, '问题类型', 'issue_type', '0', 'admin', NOW(), '', NULL, '质检问题类型');

-- 问题类型字典数据
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES 
(1, '安全问题', 'safety', 'issue_type', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '安全相关问题'),
(2, '质量问题', 'quality', 'issue_type', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '质量相关问题'),
(3, '进度问题', 'progress', 'issue_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '进度相关问题'),
(4, '其他问题', 'other', 'issue_type', '', 'default', 'N', '0', 'admin', NOW(), '', NULL, '其他类型问题');

-- 严重程度字典类型
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (202, '严重程度', 'severity_level', '0', 'admin', NOW(), '', NULL, '问题严重程度');

-- 严重程度字典数据
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES 
(1, '低', 'low', 'severity_level', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '低严重程度'),
(2, '中', 'medium', 'severity_level', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '中等严重程度'),
(3, '高', 'high', 'severity_level', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '高严重程度'),
(4, '紧急', 'urgent', 'severity_level', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '紧急严重程度');
