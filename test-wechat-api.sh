#!/bin/bash

echo "=== 微信登录API测试（开发模式）==="

# 等待服务启动
echo "等待后端服务启动..."
for i in {1..30}; do
    if curl -s -X GET "http://192.168.5.102:8080/app/auth/health" --connect-timeout 2 > /dev/null 2>&1; then
        echo "✅ 后端服务已启动"
        break
    fi
    echo "等待中... ($i/30)"
    sleep 2
done

# 测试健康检查
echo -e "\n1. 测试健康检查..."
curl -s -X GET "http://192.168.5.102:8080/app/auth/health" | jq .

echo -e "\n2. 测试检查绑定状态（首次，应该未绑定）..."
RESPONSE=$(curl -s -X POST "http://192.168.5.102:8080/app/auth/check-openid-binding" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "test_code_12345",
    "deviceId": "test_device_001"
  }')
echo "$RESPONSE" | jq .

# 提取openid
OPENID=$(echo "$RESPONSE" | jq -r '.data.openid')
echo "提取到的openid: $OPENID"

echo -e "\n3. 测试绑定手机号..."
BIND_RESPONSE=$(curl -s -X POST "http://192.168.5.102:8080/app/auth/bind-phone-to-openid" \
  -H "Content-Type: application/json" \
  -d "{
    \"openid\": \"$OPENID\",
    \"phone\": \"13812345678\",
    \"deviceId\": \"test_device_001\"
  }")
echo "$BIND_RESPONSE" | jq .

echo -e "\n4. 再次检查绑定状态（应该已绑定）..."
curl -s -X POST "http://192.168.5.102:8080/app/auth/check-openid-binding" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "test_code_12345",
    "deviceId": "test_device_001"
  }' | jq .

echo -e "\n5. 测试直接登录..."
curl -s -X POST "http://192.168.5.102:8080/app/auth/openid-login" \
  -H "Content-Type: application/json" \
  -d "{
    \"openid\": \"$OPENID\",
    \"deviceId\": \"test_device_001\"
  }" | jq .

echo -e "\n=== 测试完成 ==="
echo "如果所有测试都成功，说明微信登录功能已正常工作！"
echo "现在可以在小程序中测试前端页面了。"