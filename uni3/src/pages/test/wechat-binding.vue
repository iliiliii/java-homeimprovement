<template>
  <view class="test-container">
    <view class="test-header">
      <text class="test-title">微信绑定测试</text>
    </view>
    
    <view class="test-section">
      <text class="section-title">当前状态</text>
      <view class="status-info">
        <text class="status-item">OpenID: {{ currentOpenid || '未获取' }}</text>
        <text class="status-item">绑定状态: {{ bindingStatus }}</text>
        <text class="status-item">用户信息: {{ userInfo }}</text>
      </view>
    </view>
    
    <view class="test-section">
      <text class="section-title">测试操作</text>
      <button class="test-btn" @click="getWechatCode">1. 获取微信Code</button>
      <button class="test-btn" @click="checkBinding" :disabled="!currentCode">2. 检查绑定状态</button>
      <button class="test-btn" @click="testBinding" :disabled="!currentOpenid">3. 测试绑定手机号</button>
      <button class="test-btn" @click="testLogin" :disabled="!currentOpenid">4. 测试直接登录</button>
      <button class="test-btn danger-btn" @click="testUnbind" :disabled="!currentOpenid">5. 解除绑定</button>
    </view>
    
    <view class="test-section">
      <text class="section-title">测试手机号</text>
      <input 
        class="test-input" 
        v-model="testPhone" 
        placeholder="输入测试手机号 (如: 13812345678)"
        type="tel"
      />
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
import { checkOpenidBinding, bindPhoneToOpenid, openidLogin, unbindWechat, sendCode } from '@/api/auth'
import { getDeviceId } from '@/utils/device'

const logs = ref([])
const currentCode = ref('')
const currentOpenid = ref('')
const bindingStatus = ref('未检查')
const userInfo = ref('')
const testPhone = ref('13812345678')

const addLog = (message) => {
  const timestamp = new Date().toLocaleTimeString()
  logs.value.unshift(`[${timestamp}] ${message}`)
  if (logs.value.length > 30) {
    logs.value.pop()
  }
}

const clearLogs = () => {
  logs.value = []
}

const getWechatCode = async () => {
  try {
    addLog('开始获取微信登录凭证...')
    
    const loginRes = await uni.login({ provider: 'weixin' })
    if (!loginRes.code) {
      addLog('❌ 获取微信code失败')
      return
    }
    
    currentCode.value = loginRes.code
    addLog(`✅ 获取微信code成功: ${loginRes.code}`)
    
  } catch (error) {
    addLog(`❌ 获取微信code失败: ${error.message}`)
  }
}

const checkBinding = async () => {
  if (!currentCode.value) {
    addLog('❌ 请先获取微信code')
    return
  }
  
  try {
    addLog('开始检查绑定状态...')
    
    const result = await checkOpenidBinding(currentCode.value)
    currentOpenid.value = result.openid
    
    if (result.isBound) {
      bindingStatus.value = '已绑定'
      userInfo.value = `用户ID: ${result.userId}, 类型: ${result.userType}, 手机: ${result.phone}`
      addLog(`✅ 检查成功 - 已绑定: ${userInfo.value}`)
    } else {
      bindingStatus.value = '未绑定'
      userInfo.value = ''
      addLog(`✅ 检查成功 - 未绑定，openid: ${result.openid}`)
    }
    
  } catch (error) {
    addLog(`❌ 检查绑定状态失败: ${error.message}`)
    bindingStatus.value = '检查失败'
  }
}

const testBinding = async () => {
  if (!currentOpenid.value) {
    addLog('❌ 请先检查绑定状态获取openid')
    return
  }
  
  if (!testPhone.value) {
    addLog('❌ 请输入测试手机号')
    return
  }
  
  try {
    addLog(`开始测试绑定手机号: ${testPhone.value}`)
    
    const result = await bindPhoneToOpenid({
      openid: currentOpenid.value,
      phone: testPhone.value,
      deviceId: getDeviceId()
    })
    
    addLog(`✅ 绑定成功! 用户: ${result.userInfo.name}`)
    addLog(`Token: ${result.accessToken.substring(0, 20)}...`)
    
    // 更新状态
    bindingStatus.value = '已绑定'
    userInfo.value = `用户: ${result.userInfo.name}, 手机: ${result.userInfo.phone}`
    
  } catch (error) {
    addLog(`❌ 绑定失败: ${error.message}`)
  }
}

const testLogin = async () => {
  if (!currentOpenid.value) {
    addLog('❌ 请先检查绑定状态获取openid')
    return
  }
  
  try {
    addLog('开始测试直接登录...')
    
    const result = await openidLogin({
      openid: currentOpenid.value,
      deviceId: getDeviceId()
    })
    
    addLog(`✅ 登录成功! 用户: ${result.userInfo.name}`)
    addLog(`Token: ${result.accessToken.substring(0, 20)}...`)
    addLog(`项目数量: ${result.projects.length}`)
    
  } catch (error) {
    addLog(`❌ 登录失败: ${error.message}`)
  }
}

const testUnbind = async () => {
  if (!currentOpenid.value) {
    addLog('❌ 请先检查绑定状态获取openid')
    return
  }
  
  try {
    // 显示解除绑定确认对话框
    const confirmResult = await new Promise((resolve) => {
      uni.showModal({
        title: '解除绑定确认',
        content: '确定要解除微信绑定吗？解除后需要重新绑定才能使用微信登录。',
        confirmText: '确定解除',
        cancelText: '取消',
        success: (res) => {
          resolve(res.confirm)
        }
      })
    })
    
    if (!confirmResult) {
      addLog('用户取消解除绑定')
      return
    }
    
    // 显示验证方式选择
    const verifyResult = await new Promise((resolve) => {
      uni.showActionSheet({
        itemList: ['手机号验证码验证', '密码验证（仅员工）'],
        success: (res) => {
          resolve(res.tapIndex === 0 ? 'phone' : 'password')
        },
        fail: () => {
          resolve(null)
        }
      })
    })
    
    if (!verifyResult) {
      addLog('用户取消验证方式选择')
      return
    }
    
    if (verifyResult === 'phone') {
      // 手机号验证码验证
      await testUnbindWithPhone()
    } else {
      // 密码验证
      await testUnbindWithPassword()
    }
    
  } catch (error) {
    addLog(`❌ 解除绑定失败: ${error.message}`)
  }
}

const testUnbindWithPhone = async () => {
  try {
    // 输入手机号
    const phoneResult = await new Promise((resolve) => {
      uni.showModal({
        title: '手机号验证',
        editable: true,
        placeholderText: '请输入绑定的手机号',
        success: (res) => {
          resolve(res.confirm ? res.content : null)
        }
      })
    })
    
    if (!phoneResult) {
      addLog('用户取消输入手机号')
      return
    }
    
    addLog(`发送验证码到: ${phoneResult}`)
    
    // 发送验证码
    await sendCode(phoneResult)
    addLog('✅ 验证码已发送')
    
    // 输入验证码
    const codeResult = await new Promise((resolve) => {
      uni.showModal({
        title: '验证码',
        editable: true,
        placeholderText: '请输入验证码',
        success: (res) => {
          resolve(res.confirm ? res.content : null)
        }
      })
    })
    
    if (!codeResult) {
      addLog('用户取消输入验证码')
      return
    }
    
    addLog('开始解除绑定...')
    
    // 调用解除绑定接口
    await unbindWechat({
      openid: currentOpenid.value,
      deviceId: getDeviceId(),
      verifyType: 'phone',
      phone: phoneResult,
      code: codeResult
    })
    
    addLog('✅ 解除绑定成功!')
    
    // 重置状态
    bindingStatus.value = '未绑定'
    userInfo.value = ''
    
  } catch (error) {
    addLog(`❌ 手机号验证解除绑定失败: ${error.message}`)
  }
}

const testUnbindWithPassword = async () => {
  try {
    // 输入密码
    const passwordResult = await new Promise((resolve) => {
      uni.showModal({
        title: '密码验证',
        editable: true,
        placeholderText: '请输入登录密码',
        success: (res) => {
          resolve(res.confirm ? res.content : null)
        }
      })
    })
    
    if (!passwordResult) {
      addLog('用户取消输入密码')
      return
    }
    
    addLog('开始解除绑定...')
    
    // 调用解除绑定接口
    await unbindWechat({
      openid: currentOpenid.value,
      deviceId: getDeviceId(),
      verifyType: 'password',
      password: passwordResult
    })
    
    addLog('✅ 解除绑定成功!')
    
    // 重置状态
    bindingStatus.value = '未绑定'
    userInfo.value = ''
    
  } catch (error) {
    addLog(`❌ 密码验证解除绑定失败: ${error.message}`)
  }
}
</script>

<style lang="scss" scoped>
.test-container {
  padding: 32rpx;
  min-height: 100vh;
  background: $u-bg-color;
}

.test-header {
  text-align: center;
  margin-bottom: 48rpx;
}

.test-title {
  font-size: $u-font-size-xl;
  font-weight: 600;
  color: $u-main-color;
}

.test-section {
  background: $color-white;
  border-radius: $u-border-radius-lg;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  display: block;
  font-size: $u-font-size-lg;
  font-weight: 600;
  color: $u-main-color;
  margin-bottom: 24rpx;
}

.status-info {
  background: $u-bg-color;
  border-radius: $u-border-radius;
  padding: 24rpx;
}

.status-item {
  display: block;
  font-size: $u-font-size-sm;
  color: $u-content-color;
  margin-bottom: 12rpx;
  font-family: monospace;
}

.test-btn {
  width: 100%;
  height: 80rpx;
  background: $color-brand;
  color: $color-white;
  border: none;
  border-radius: $u-border-radius-lg;
  font-size: $u-font-size-lg;
  margin-bottom: 16rpx;
  
  &:disabled {
    background: $u-disabled-color;
    color: $u-tips-color;
  }
  
  &.danger-btn {
    background: $color-error;
    
    &:disabled {
      background: $u-disabled-color;
      color: $u-tips-color;
    }
  }
}

.test-input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  background: $u-bg-color;
  border: 2rpx solid $u-border-color;
  border-radius: $u-border-radius-lg;
  font-size: $u-font-size-lg;
}

.clear-btn {
  width: 100%;
  height: 60rpx;
  background: $u-info;
  color: $color-white;
  border: none;
  border-radius: $u-border-radius;
  font-size: $u-font-size-sm;
  margin-top: 16rpx;
}

.log-box {
  background: $color-black;
  border-radius: $u-border-radius;
  padding: 24rpx;
  max-height: 600rpx;
  overflow-y: auto;
}

.log-item {
  display: block;
  font-size: $u-font-size-sm;
  color: $color-white;
  margin-bottom: 8rpx;
  font-family: monospace;
  word-break: break-all;
}
</style>