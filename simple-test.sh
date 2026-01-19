#!/bin/bash

echo "=== 微信登录API简单测试 ==="

# 等待服务启动
echo "等待后端服务启动..."
for i in {1..20}; do
    if curl -s -X GET "http://192.168.5.102:8080/app/auth/health" --connect-timeout 2 > /dev/null 2>&1; then
        echo "✅ 后端服务已启动"
        break
    fi
    echo "等待中... ($i/20)"
    sleep 3
done

echo -e "\n1. 健康检查:"
curl -s -X GET "http://192.168.5.102:8080/app/auth/health"

echo -e "\n\n2. 检查绑定状态:"
curl -s -X POST "http://192.168.5.102:8080/app/auth/check-openid-binding" \
  -H "Content-Type: application/json" \
  -d '{"code":"test_code_12345","deviceId":"test_device_001"}'

echo -e "\n\n3. 绑定手机号:"
curl -s -X POST "http://192.168.5.102:8080/app/auth/bind-phone-to-openid" \
  -H "Content-Type: application/json" \
  -d '{"openid":"mock_openid_6610","phone":"13812345678","deviceId":"test_device_001"}'

echo -e "\n4. 直接登录:"
curl -s -X POST "http://192.168.5.102:8080/app/auth/openid-login" \
  -H "Content-Type: application/json" \
  -d '{"openid":"mock_openid_6610","deviceId":"test_device_001"}'

echo -e "\n\n5. 解除绑定（手机号验证）:"
curl -s -X POST "http://192.168.5.102:8080/app/auth/unbind-wechat" \
  -H "Content-Type: application/json" \
  -d '{"openid":"mock_openid_6610","deviceId":"test_device_001","verifyType":"phone","phone":"13812345678","code":"123456"}'

echo -e "\n\n6. 再次检查绑定状态（应该未绑定）:"
curl -s -X POST "http://192.168.5.102:8080/app/auth/check-openid-binding" \
  -H "Content-Type: application/json" \
  -d '{"code":"test_code_12345","deviceId":"test_device_001"}'

echo -e "\n\n=== 测试完成 ==="