<template>
  <view class="phone-modal-overlay" v-if="visible" @click="handleOverlayClick">
    <view class="phone-modal" @click.stop>
      
      <!-- 头部 -->
      <view class="modal-header">
        <text class="modal-title">绑定手机号</text>
        <text class="modal-subtitle">请输入您在系统中注册的手机号</text>
      </view>
      
      <!-- 输入区域 -->
      <view class="input-section">
        <view class="input-wrapper" :class="{ 'input-error': hasError, 'input-focus': isFocused }">
          <view class="input-prefix">
            <text class="country-code">+86</text>
          </view>
          <input 
            class="phone-input"
            v-model="phoneNumber"
            type="tel"
            placeholder="请输入手机号"
            maxlength="11"
            @focus="handleFocus"
            @blur="handleBlur"
            @input="handleInput"
          />
          <view class="input-clear" v-if="phoneNumber" @click="clearInput">
            <text class="clear-text">×</text>
          </view>
        </view>
        
        <!-- 错误提示 -->
        <view class="error-message" v-if="errorMessage">
          <text class="error-text">{{ errorMessage }}</text>
        </view>
        
        <!-- 格式提示 -->
        <view class="format-tip" v-if="!hasError && phoneNumber.length > 0">
          <text class="tip-text">{{ formatTip }}</text>
        </view>
      </view>
      
      <!-- 安全提示 -->
      <view class="security-tip">
        <view class="tip-header">
          <text class="tip-title">安全提示</text>
        </view>
        <view class="tip-list">
          <text class="tip-item">• 仅限系统中已注册的手机号可以绑定</text>
          <text class="tip-item">• 如您的手机号未注册，请联系管理员添加</text>
          <text class="tip-item">• 绑定后可享受微信快速登录服务</text>
        </view>
      </view>
      
      <!-- 操作按钮 -->
      <view class="modal-actions">
        <button class="action-btn cancel-btn" @click="handleCancel">
          取消
        </button>
        <button 
          class="action-btn confirm-btn" 
          :class="{ 'btn-disabled': !isValidPhone }"
          :disabled="!isValidPhone"
          @click="handleConfirm"
        >
          <text>确认绑定</text>
        </button>
      </view>
      
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  retryCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['confirm', 'cancel', 'close'])

const phoneNumber = ref('')
const isFocused = ref(false)
const errorMessage = ref('')

// 计算属性
const hasError = computed(() => !!errorMessage.value)

const isValidPhone = computed(() => {
  return /^1[3-9]\d{9}$/.test(phoneNumber.value)
})

const formatTip = computed(() => {
  const len = phoneNumber.value.length
  if (len === 0) return ''
  if (len < 11) return `还需输入 ${11 - len} 位数字`
  if (isValidPhone.value) return '手机号格式正确'
  return '手机号格式不正确'
})

// 监听重试次数变化
watch(() => props.retryCount, (newCount) => {
  if (newCount > 0) {
    errorMessage.value = '手机号格式不正确，请重新输入'
    // 清空输入框并聚焦
    phoneNumber.value = ''
    setTimeout(() => {
      errorMessage.value = ''
    }, 2000)
  }
})

// 监听输入变化
watch(phoneNumber, (newValue) => {
  // 清除错误信息
  if (errorMessage.value) {
    errorMessage.value = ''
  }
  
  // 格式化输入（只保留数字）
  const cleaned = newValue.replace(/\D/g, '')
  if (cleaned !== newValue) {
    phoneNumber.value = cleaned
  }
})

const handleFocus = () => {
  isFocused.value = true
}

const handleBlur = () => {
  isFocused.value = false
}

const handleInput = (e) => {
  phoneNumber.value = e.detail.value
}

const clearInput = () => {
  phoneNumber.value = ''
  errorMessage.value = ''
}

const handleOverlayClick = () => {
  emit('close')
}

const handleCancel = () => {
  emit('cancel')
}

const handleConfirm = () => {
  if (!isValidPhone.value) {
    errorMessage.value = '请输入正确的手机号格式'
    return
  }
  
  emit('confirm', phoneNumber.value)
}

// 重置组件状态
const reset = () => {
  phoneNumber.value = ''
  errorMessage.value = ''
  isFocused.value = false
}

// 暴露重置方法
defineExpose({
  reset
})
</script>

<style lang="scss" scoped>
.phone-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8rpx);
  z-index: 9999;
  @include flex(column);
  align-items: center;
  justify-content: center;
  padding: 60rpx 40rpx;
}

.phone-modal {
  background: $color-white;
  border-radius: 24rpx;
  width: 100%;
  max-width: 600rpx;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
  animation: modalSlideUp 0.3s ease-out;
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(60rpx) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  text-align: center;
  padding: 48rpx 40rpx 32rpx;
  background: linear-gradient(135deg, #F8F9FA 0%, #F1F5F9 100%);
  border-bottom: 1rpx solid $u-border-color;
}

.header-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $u-primary 0%, $u-primary-dark 100%);
  @include flex(row);
  align-items: center;
  justify-content: center;
  margin: 0 auto 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(196, 0, 22, 0.3);
}

.phone-icon {
  font-size: 40rpx;
}

.modal-title {
  display: block;
  font-size: $u-font-size-xl;
  font-weight: 600;
  color: $u-main-color;
  margin-bottom: 12rpx;
}

.modal-subtitle {
  display: block;
  font-size: $u-font-size-sm;
  color: $u-content-color;
  line-height: 1.5;
}

.input-section {
  padding: 40rpx;
}

.input-wrapper {
  @include flex(row);
  align-items: center;
  background: #F9FAFB;
  border: 2rpx solid #E5E7EB;
  border-radius: $u-border-radius-lg;
  padding: 0 24rpx;
  height: 96rpx;
  transition: all 0.2s ease;
  
  &.input-focus {
    border-color: $u-primary;
    background: $color-white;
    box-shadow: 0 0 0 6rpx rgba(196, 0, 22, 0.1);
  }
  
  &.input-error {
    border-color: $u-error;
    background: #FEF2F2;
    box-shadow: 0 0 0 6rpx rgba(196, 0, 22, 0.1);
  }
}

.input-prefix {
  @include flex(row);
  align-items: center;
  margin-right: 16rpx;
}

.country-code {
  font-size: $u-font-size-lg;
  color: $u-content-color;
  font-weight: 500;
}

.phone-input {
  flex: 1;
  font-size: $u-font-size-lg;
  color: $u-main-color;
  background: transparent;
  border: none;
  outline: none;
  
  &::placeholder {
    color: #9CA3AF;
  }
}

.input-clear {
  width: 48rpx;
  height: 48rpx;
  @include flex(row);
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #E5E7EB;
  
  &:active {
    background: #D1D5DB;
  }
}

.clear-text {
  font-size: 32rpx;
  color: $u-content-color;
  font-weight: 300;
}

.error-message {
  margin-top: 16rpx;
  padding: 12rpx 16rpx;
  background: #FEF2F2;
  border-radius: $u-border-radius;
  border-left: 4rpx solid $u-error;
}

.error-text {
  font-size: $u-font-size-sm;
  color: $u-error;
}

.format-tip {
  margin-top: 16rpx;
  padding: 12rpx 16rpx;
  background: #F0F9FF;
  border-radius: $u-border-radius;
  border-left: 4rpx solid $u-primary;
}

.tip-text {
  font-size: $u-font-size-sm;
  color: $u-primary-dark;
}

.security-tip {
  padding: 32rpx 40rpx;
  background: #FAFAFA;
  border-top: 1rpx solid $u-border-color;
}

.tip-header {
  margin-bottom: 16rpx;
}

.tip-title {
  font-size: $u-font-size;
  font-weight: 600;
  color: $u-main-color;
}

.tip-list {
  @include flex(column);
  gap: 8rpx;
}

.tip-item {
  font-size: $u-font-size-sm;
  color: $u-content-color;
  line-height: 1.6;
}

.modal-actions {
  @include flex(row);
  padding: 32rpx 40rpx 40rpx;
  gap: 24rpx;
  background: #FAFAFA;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  border-radius: $u-border-radius-lg;
  font-size: $u-font-size-lg;
  font-weight: 500;
  @include flex(row);
  align-items: center;
  justify-content: center;
  border: none;
  transition: all 0.2s ease;
  
  &.cancel-btn {
    background: $color-white;
    color: $u-content-color;
    border: 2rpx solid $u-border-color;
    
    &:active {
      background: #F5F5F5;
      transform: scale(0.98);
    }
  }
  
  &.confirm-btn {
    background: linear-gradient(135deg, $u-primary 0%, $u-primary-dark 100%);
    color: $color-white;
    box-shadow: 0 8rpx 24rpx rgba(196, 0, 22, 0.3);
    
    &:active {
      transform: scale(0.98);
      box-shadow: 0 4rpx 12rpx rgba(196, 0, 22, 0.4);
    }
    
    &.btn-disabled {
      background: #D1D5DB;
      color: #9CA3AF;
      box-shadow: none;
      
      &:active {
        transform: none;
      }
    }
  }
}
</style>