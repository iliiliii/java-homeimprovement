<template>
  <view class="success-overlay" v-if="visible">
    <view class="success-modal">
      
      <!-- 成功动画 -->
      <view class="success-animation">
        <view class="success-circle">
          <view class="checkmark">
            <SvgIcon name="check" size="64rpx" color="#FFFFFF" />
          </view>
        </view>
        <view class="celebration-particles">
          <view class="particle" v-for="n in 8" :key="n" :style="getParticleStyle(n)"></view>
        </view>
      </view>
      
      <!-- 成功信息 -->
      <view class="success-content">
        <text class="success-title">绑定成功！</text>
        <text class="success-subtitle">您的微信账号已成功绑定</text>
        
        <!-- 用户信息 -->
        <view class="user-info" v-if="userInfo">
          <view class="user-avatar">
            <SvgIcon name="user" size="48rpx" color="#6B7280" />
          </view>
          <view class="user-details">
            <text class="user-name">{{ userInfo.name }}</text>
            <text class="user-phone">{{ userInfo.phone }}</text>
          </view>
        </view>
        
        <!-- 功能提示 -->
        <view class="feature-tips">
          <view class="tip-item">
            <SvgIcon name="zap" size="24rpx" color="#10B981" />
            <text>下次可直接微信登录</text>
          </view>
          <view class="tip-item">
            <SvgIcon name="shield-check" size="24rpx" color="#10B981" />
            <text>账户安全得到保障</text>
          </view>
        </view>
      </view>
      
      <!-- 倒计时提示 -->
      <view class="countdown-tip">
        <text>{{ countdownText }}</text>
      </view>
      
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  userInfo: {
    type: Object,
    default: null
  },
  autoCloseDelay: {
    type: Number,
    default: 3000
  }
})

const emit = defineEmits(['close'])

const countdown = ref(0)
let countdownTimer = null

const countdownText = computed(() => {
  if (countdown.value > 0) {
    return `${Math.ceil(countdown.value / 1000)} 秒后自动跳转到首页`
  }
  return '正在跳转到首页...'
})

// 监听显示状态
watch(() => props.visible, (newVisible) => {
  if (newVisible) {
    startCountdown()
  } else {
    stopCountdown()
  }
})

const startCountdown = () => {
  countdown.value = props.autoCloseDelay
  
  countdownTimer = setInterval(() => {
    countdown.value -= 100
    
    if (countdown.value <= 0) {
      stopCountdown()
      emit('close')
    }
  }, 100)
}

const stopCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

// 获取粒子样式
const getParticleStyle = (index) => {
  const angle = (index - 1) * 45 // 每个粒子间隔45度
  const distance = 120 + Math.random() * 60 // 随机距离
  const delay = Math.random() * 0.5 // 随机延迟
  
  return {
    '--angle': `${angle}deg`,
    '--distance': `${distance}rpx`,
    '--delay': `${delay}s`
  }
}

onUnmounted(() => {
  stopCountdown()
})
</script>

<style lang="scss" scoped>
.success-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(12rpx);
  z-index: 9999;
  @include flex(column);
  align-items: center;
  justify-content: center;
  padding: 60rpx 40rpx;
}

.success-modal {
  background: $color-white;
  border-radius: 32rpx;
  width: 100%;
  max-width: 560rpx;
  padding: 60rpx 40rpx 40rpx;
  text-align: center;
  position: relative;
  overflow: hidden;
  animation: successModalShow 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes successModalShow {
  from {
    opacity: 0;
    transform: translateY(100rpx) scale(0.8);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.success-animation {
  position: relative;
  margin-bottom: 48rpx;
}

.success-circle {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  margin: 0 auto;
  @include flex(row);
  align-items: center;
  justify-content: center;
  animation: successCircleScale 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) 0.2s both;
  box-shadow: 0 16rpx 40rpx rgba(16, 185, 129, 0.4);
}

@keyframes successCircleScale {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}

.checkmark {
  animation: checkmarkShow 0.4s ease-out 0.6s both;
}

@keyframes checkmarkShow {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.celebration-particles {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 12rpx;
  height: 12rpx;
  background: linear-gradient(45deg, #F59E0B, #EAB308);
  border-radius: 50%;
  animation: particleExplode 1.2s ease-out 0.8s both;
}

@keyframes particleExplode {
  0% {
    opacity: 1;
    transform: rotate(var(--angle)) translateX(0) scale(0);
  }
  50% {
    opacity: 1;
    transform: rotate(var(--angle)) translateX(var(--distance)) scale(1);
  }
  100% {
    opacity: 0;
    transform: rotate(var(--angle)) translateX(calc(var(--distance) * 1.5)) scale(0);
  }
}

.success-content {
  margin-bottom: 40rpx;
}

.success-title {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: $u-main-color;
  margin-bottom: 16rpx;
  animation: textSlideUp 0.6s ease-out 0.4s both;
}

.success-subtitle {
  display: block;
  font-size: $u-font-size-lg;
  color: $u-content-color;
  margin-bottom: 40rpx;
  animation: textSlideUp 0.6s ease-out 0.5s both;
}

@keyframes textSlideUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-info {
  @include flex(row);
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  background: #F8F9FA;
  border-radius: $u-border-radius-lg;
  margin-bottom: 32rpx;
  animation: userInfoShow 0.6s ease-out 0.6s both;
}

@keyframes userInfoShow {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #E5E7EB;
  @include flex(row);
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.user-details {
  text-align: left;
}

.user-name {
  display: block;
  font-size: $u-font-size-lg;
  font-weight: 600;
  color: $u-main-color;
  margin-bottom: 8rpx;
}

.user-phone {
  display: block;
  font-size: $u-font-size-sm;
  color: $u-content-color;
}

.feature-tips {
  @include flex(column);
  gap: 16rpx;
  animation: tipsShow 0.6s ease-out 0.7s both;
}

@keyframes tipsShow {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tip-item {
  @include flex(row);
  align-items: center;
  justify-content: center;
  
  text {
    font-size: $u-font-size-sm;
    color: $u-content-color;
    margin-left: 12rpx;
  }
}

.countdown-tip {
  padding: 24rpx;
  background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  border-radius: $u-border-radius-lg;
  animation: countdownShow 0.6s ease-out 0.8s both;
  
  text {
    font-size: $u-font-size-sm;
    color: #92400E;
    font-weight: 500;
  }
}

@keyframes countdownShow {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>