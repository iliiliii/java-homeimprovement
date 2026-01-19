<template>
  <view class="test-container">
    <view class="test-header">
      <text class="test-title">网络连接测试</text>
    </view>
    
    <view class="test-section">
      <text class="section-title">1. 基础网络测试</text>
      <button class="test-btn" @click="testBasicNetwork">测试基础网络</button>
    </view>
    
    <view class="test-section">
      <text class="section-title">2. API健康检查</text>
      <button class="test-btn" @click="testHealthCheck">测试健康检查</button>
    </view>
    
    <view class="test-section">
      <text class="section-title">3. 检查绑定接口测试</text>
      <button class="test-btn" @click="testCheckBinding">测试检查绑定</button>
    </view>
    
    <view class="test-section">
      <text class="section-title">日志</text>
      <view class="log-box">
        <text v-for="(log, index) in logs" :key="index" class="log-item">
          {{ log }}
        </text>
      </view>
      <button class="clear-btn" @click="clearLogs">清空日志</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { getDeviceId } from '@/utils/device'

const logs = ref([])

const addLog = (message) => {
  const timestamp = new Date().toLocaleTimeString()
  logs.value.unshift(`[${timestamp}] ${message}`)
  if (logs.value.length > 20) {
    logs.value.pop()
  }
}

const clearLogs = () => {
  logs.value = []
}

const testBasicNetwork = async () => {
  try {
    addLog('开始基础网络测试...')
    
    const result = await uni.request({
      url: 'https://www.baidu.com',
      method: 'GET',
      timeout: 5000
    })
    
    addLog(`基础网络测试成功: ${result.statusCode}`)
    
  } catch (error) {
    addLog(`基础网络测试失败: ${error.errMsg || error.message}`)
  }
}

const testHealthCheck = async () => {
  try {
    addLog('开始API健康检查...')
    
    const result = await uni.request({
      url: 'http://192.168.5.102:8080/app/auth/health',
      method: 'GET',
      timeout: 10000
    })
    
    addLog(`健康检查成功: ${result.statusCode}, 数据: ${JSON.stringify(result.data)}`)
    
  } catch (error) {
    addLog(`健康检查失败: ${error.errMsg || error.message}`)
  }
}

const testCheckBinding = async () => {
  try {
    addLog('开始测试检查绑定接口...')
    
    // 检查微信环境
    const accountInfo = uni.getAccountInfoSync()
    addLog(`微信环境检查: ${JSON.stringify(accountInfo)}`)
    
    // 先获取微信code
    addLog('正在获取微信登录凭证...')
    const loginRes = await uni.login({ provider: 'weixin' })
    if (!loginRes.code) {
      addLog('获取微信code失败')
      return
    }
    
    addLog(`获取到微信code: ${loginRes.code}`)
    
    const deviceId = getDeviceId()
    addLog(`设备ID: ${deviceId}`)
    
    const requestData = {
      code: loginRes.code,
      deviceId: deviceId
    }
    
    addLog(`请求数据: ${JSON.stringify(requestData)}`)
    addLog('发送请求到后端...')
    
    const result = await uni.request({
      url: 'http://192.168.5.102:8080/app/auth/check-openid-binding',
      method: 'POST',
      data: requestData,
      header: {
        'Content-Type': 'application/json'
      },
      timeout: 15000
    })
    
    addLog(`检查绑定接口响应: HTTP ${result.statusCode}`)
    addLog(`响应头: ${JSON.stringify(result.header)}`)
    addLog(`响应数据: ${JSON.stringify(result.data)}`)
    
    if (result.statusCode === 200) {
      if (result.data.code === 200) {
        addLog('✅ 接口调用成功!')
        const bindingData = result.data.data
        if (bindingData.isBound) {
          addLog(`🔗 openid已绑定: 用户ID=${bindingData.userId}, 类型=${bindingData.userType}`)
        } else {
          addLog(`🆕 openid未绑定，需要绑定手机号`)
        }
      } else {
        addLog(`❌ 业务错误: ${result.data.msg}`)
      }
    } else {
      addLog(`❌ HTTP错误: ${result.statusCode}`)
    }
    
  } catch (error) {
    addLog(`❌ 检查绑定接口失败: ${error.errMsg || error.message}`)
    if (error.data) {
      addLog(`错误详情: ${JSON.stringify(error.data)}`)
    }
    if (error.statusCode) {
      addLog(`HTTP状态码: ${error.statusCode}`)
    }
  }
}
</script>

<style lang="scss" scoped>
.test-container {
  padding: 32rpx;
  min-height: 100vh;
  background: $color-gray-50;
}

.test-header {
  text-align: center;
  margin-bottom: 48rpx;
}

.test-title {
  font-size: 36rpx;
  font-weight: 600;
  color: $color-text-primary;
}

.test-section {
  background: $color-white;
  border-radius: $radius-l;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 24rpx;
}

.test-btn {
  width: 100%;
  height: 80rpx;
  background: $color-brand;
  color: $color-white;
  border: none;
  border-radius: $radius-l;
  font-size: 28rpx;
  margin-bottom: 16rpx;
}

.clear-btn {
  width: 100%;
  height: 60rpx;
  background: $color-gray-500;
  color: $color-white;
  border: none;
  border-radius: $radius-m;
  font-size: 24rpx;
  margin-top: 16rpx;
}

.log-box {
  background: $color-black;
  border-radius: $radius-m;
  padding: 24rpx;
  max-height: 600rpx;
  overflow-y: auto;
}

.log-item {
  display: block;
  font-size: 24rpx;
  color: $color-white;
  margin-bottom: 8rpx;
  font-family: monospace;
  word-break: break-all;
}
</style>