<template>
  <view class="test-container">
    <view class="test-header">
      <text class="test-title">微信登录测试</text>
    </view>
    
    <view class="test-section">
      <text class="section-title">1. 检查openid绑定状态</text>
      <button class="test-btn" @click="testCheckBinding">测试检查绑定</button>
      <view class="result-box" v-if="checkResult">
        <text>{{ JSON.stringify(checkResult, null, 2) }}</text>
      </view>
    </view>
    
    <view class="test-section">
      <text class="section-title">2. openid直接登录</text>
      <input class="test-input" v-model="testOpenid" placeholder="输入openid" />
      <button class="test-btn" @click="testOpenidLogin">测试openid登录</button>
      <view class="result-box" v-if="loginResult">
        <text>{{ JSON.stringify(loginResult, null, 2) }}</text>
      </view>
    </view>
    
    <view class="test-section">
      <text class="section-title">3. 绑定手机号</text>
      <input class="test-input" v-model="testPhone" placeholder="输入手机号" />
      <button class="test-btn" @click="testBindPhone">测试绑定手机号</button>
      <view class="result-box" v-if="bindResult">
        <text>{{ JSON.stringify(bindResult, null, 2) }}</text>
      </view>
    </view>
    
    <view class="test-section">
      <text class="section-title">日志</text>
      <view class="log-box">
        <text v-for="(log, index) in logs" :key="index" class="log-item">
          {{ log }}
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { checkOpenidBinding, openidLogin, bindPhoneToOpenid } from '@/api/auth'
import { getDeviceId } from '@/utils/device'

const checkResult = ref(null)
const loginResult = ref(null)
const bindResult = ref(null)
const testOpenid = ref('')
const testPhone = ref('')
const logs = ref([])

const addLog = (message) => {
  const timestamp = new Date().toLocaleTimeString()
  logs.value.unshift(`[${timestamp}] ${message}`)
  if (logs.value.length > 10) {
    logs.value.pop()
  }
}

const testCheckBinding = async () => {
  try {
    addLog('开始测试检查绑定状态...')
    
    // 获取微信code
    const loginRes = await uni.login({ provider: 'weixin' })
    if (!loginRes.code) {
      throw new Error('获取微信code失败')
    }
    
    addLog(`获取到微信code: ${loginRes.code}`)
    
    // 检查绑定状态
    const result = await checkOpenidBinding(loginRes.code)
    checkResult.value = result
    
    addLog(`检查结果: ${result.isBound ? '已绑定' : '未绑定'}`)
    if (result.isBound) {
      testOpenid.value = result.openid
    }
    
  } catch (error) {
    addLog(`检查绑定失败: ${error.message}`)
    uni.showToast({ title: error.message, icon: 'none' })
  }
}

const testOpenidLogin = async () => {
  if (!testOpenid.value) {
    uni.showToast({ title: '请先输入openid', icon: 'none' })
    return
  }
  
  try {
    addLog(`开始测试openid登录: ${testOpenid.value}`)
    
    const result = await openidLogin({
      openid: testOpenid.value,
      deviceId: getDeviceId()
    })
    
    loginResult.value = result
    addLog('openid登录成功')
    
  } catch (error) {
    addLog(`openid登录失败: ${error.message}`)
    uni.showToast({ title: error.message, icon: 'none' })
  }
}

const testBindPhone = async () => {
  if (!testOpenid.value) {
    uni.showToast({ title: '请先获取openid', icon: 'none' })
    return
  }
  
  if (!testPhone.value) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  
  try {
    addLog(`开始测试绑定手机号: ${testPhone.value}`)
    
    const result = await bindPhoneToOpenid({
      openid: testOpenid.value,
      phone: testPhone.value,
      deviceId: getDeviceId()
    })
    
    bindResult.value = result
    addLog('绑定手机号成功')
    
  } catch (error) {
    addLog(`绑定手机号失败: ${error.message}`)
    uni.showToast({ title: error.message, icon: 'none' })
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
  margin-bottom: 24rpx;
}

.test-input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  border: 2rpx solid $color-border;
  border-radius: $radius-l;
  font-size: 28rpx;
  margin-bottom: 24rpx;
  box-sizing: border-box;
}

.result-box {
  background: $color-gray-50;
  border-radius: $radius-m;
  padding: 24rpx;
  margin-top: 24rpx;
  
  text {
    font-size: 24rpx;
    color: $color-text-secondary;
    white-space: pre-wrap;
    word-break: break-all;
  }
}

.log-box {
  background: $color-black;
  border-radius: $radius-m;
  padding: 24rpx;
  max-height: 400rpx;
  overflow-y: auto;
}

.log-item {
  display: block;
  font-size: 24rpx;
  color: $color-white;
  margin-bottom: 8rpx;
  font-family: monospace;
}
</style>