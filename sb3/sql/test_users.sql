-- =====================================================
-- 测试用户数据
-- 用于小程序登录测试
-- =====================================================

-- 插入测试客户（用于客户登录测试）
INSERT INTO `customers` (`id`, `name`, `phone`, `email`, `address`, `avatar`, `status`, `source`, `created_at`, `created_by`) 
VALUES 
('test_customer_001', '测试客户', '13800138000', 'test@example.com', '测试地址', NULL, 'active', 'manual', NOW(), 'admin'),
('test_customer_002', '张三', '13800138001', 'zhangsan@example.com', '北京市朝阳区', NULL, 'active', 'manual', NOW(), 'admin');

-- 查看已有的系统用户（员工）
-- SELECT user_id, user_name, nick_name, phonenumber FROM sys_user;

-- 如果需要更新员工手机号用于测试，可以执行：
-- UPDATE sys_user SET phonenumber = '13900139000' WHERE user_name = 'admin';

-- =====================================================
-- 验证数据
-- =====================================================

-- 查看客户数据
SELECT id, name, phone, status FROM customers WHERE phone IN ('13800138000', '13800138001');

-- 查看员工数据
SELECT user_id, user_name, nick_name, phonenumber FROM sys_user WHERE phonenumber IS NOT NULL AND phonenumber != '';
