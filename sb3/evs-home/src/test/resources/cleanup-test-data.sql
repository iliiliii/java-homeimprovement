-- 测试数据清理脚本
-- 在测试完成后执行，清理所有测试相关的数据

-- 清理测试客户数据
DELETE FROM customers WHERE id LIKE 'test-%' OR id LIKE 'integration-test-%';

-- 清理测试过程中可能创建的其他相关数据
-- 例如：项目数据、质检记录等（根据实际业务需要添加）

-- 验证清理结果
SELECT
    'Customers cleanup complete',
    COUNT(*) as remaining_test_customers
FROM customers
WHERE id LIKE 'test-%' OR id LIKE 'integration-test-%';