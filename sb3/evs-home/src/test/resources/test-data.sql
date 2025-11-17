-- 测试数据初始化脚本
-- 用于手机号验证功能的集成测试

-- 清理测试数据
DELETE FROM customers WHERE id LIKE 'test-%' OR id LIKE 'integration-test-%';

-- 插入测试客户数据
INSERT INTO customers (
    id, name, phone, email, address, level, source, remarks, avatar,
    is_active, created_at, updated_at, created_by, updated_by
) VALUES
-- 基础测试数据
(
    'test-customer-1',
    '测试客户1',
    '13800138001',
    'test1@example.com',
    '测试地址1',
    'VIP',
    '官网',
    '测试备注1',
    NULL,
    1,
    NOW(),
    NOW(),
    'admin',
    'admin'
),
(
    'test-customer-2',
    '测试客户2',
    '13800138002',
    'test2@example.com',
    '测试地址2',
    '普通',
    '推荐',
    '测试备注2',
    NULL,
    1,
    NOW(),
    NOW(),
    'admin',
    'admin'
),
(
    'test-customer-3',
    '测试客户3',
    '13800138003',
    'test3@example.com',
    '测试地址3',
    'SVIP',
    '合作',
    '测试备注3',
    NULL,
    1,
    NOW(),
    NOW(),
    'admin',
    'admin'
),

-- 相同手机号测试数据（用于测试重复手机号场景）
(
    'test-same-phone-1',
    '相同手机号客户1',
    '13900139999',
    'same1@example.com',
    '相同手机号地址1',
    'VIP',
    '官网',
    '相同手机号测试1',
    NULL,
    1,
    NOW(),
    NOW(),
    'admin',
    'admin'
),
(
    'test-same-phone-2',
    '相同手机号客户2',
    '13900139999',
    'same2@example.com',
    '相同手机号地址2',
    '普通',
    '推荐',
    '相同手机号测试2',
    NULL,
    1,
    NOW(),
    NOW(),
    'admin',
    'admin'
),

-- 不同客户等级测试数据
(
    'test-level-vip',
    'VIP客户',
    '13800138888',
    'vip@example.com',
    'VIP客户地址',
    'VIP',
    '官网',
    'VIP客户测试',
    NULL,
    1,
    NOW(),
    NOW(),
    'admin',
    'admin'
),
(
    'test-level-svip',
    'SVIP客户',
    '13800138777',
    'svip@example.com',
    'SVIP客户地址',
    'SVIP',
    '合作',
    'SVIP客户测试',
    NULL,
    1,
    NOW(),
    NOW(),
    'admin',
    'admin'
),
(
    'test-level-normal',
    '普通客户',
    '13800138666',
    'normal@example.com',
    '普通客户地址',
    '普通',
    '推荐',
    '普通客户测试',
    NULL,
    1,
    NOW(),
    NOW(),
    'admin',
    'admin'
),

-- 不同来源测试数据
(
    'test-source-web',
    '官网来源客户',
    '13800138555',
    'web@example.com',
    '官网来源客户地址',
    '普通',
    '官网',
    '官网来源测试',
    NULL,
    1,
    NOW(),
    NOW(),
    'admin',
    'admin'
),
(
    'test-source-referral',
    '推荐来源客户',
    '13800138444',
    'referral@example.com',
    '推荐来源客户地址',
    'VIP',
    '推荐',
    '推荐来源测试',
    NULL,
    1,
    NOW(),
    NOW(),
    'admin',
    'admin'
),
(
    'test-source-partner',
    '合作来源客户',
    '13800138333',
    'partner@example.com',
    '合作来源客户地址',
    'SVIP',
    '合作',
    '合作来源测试',
    NULL,
    1,
    NOW(),
    NOW(),
    'admin',
    'admin'
),

-- 非活跃客户测试数据
(
    'test-inactive',
    '非活跃客户',
    '13800138222',
    'inactive@example.com',
    '非活跃客户地址',
    '普通',
    '官网',
    '非活跃客户测试',
    NULL,
    0,
    NOW(),
    NOW(),
    'admin',
    'admin'
);

-- 创建测试索引（如果需要）
-- CREATE INDEX IF NOT EXISTS idx_customers_phone_test ON customers(phone);
-- CREATE INDEX IF NOT EXISTS idx_customers_name_test ON customers(name);

-- 验证测试数据插入结果
SELECT
    COUNT(*) as total_test_customers,
    COUNT(CASE WHEN phone = '13900139999' THEN 1 END) as same_phone_count,
    COUNT(CASE WHEN is_active = 1 THEN 1 END) as active_customers,
    COUNT(CASE WHEN level = 'VIP' THEN 1 END) as vip_customers
FROM customers
WHERE id LIKE 'test-%' OR id LIKE 'integration-test-%';