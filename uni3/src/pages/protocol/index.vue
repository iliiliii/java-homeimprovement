<template>
  <view class="protocol-container">
    <!-- 自定义导航栏 -->
    <NavBar title="用户协议" />

    <!-- 协议内容 -->
    <view class="protocol-content">
      <view class="protocol-header">
        <text class="protocol-title">{{ APP_CONFIG.name }} 用户协议</text>
        <text class="update-time">更新日期：2025年12月3日</text>
      </view>

      <view class="section">
        <text class="section-title">一、协议的范围</text>
        <text class="section-text">欢迎您使用{{ APP_CONFIG.name }}服务。本协议是您与{{ APP_CONFIG.name }}之间关于您使用{{ APP_CONFIG.name }}服务所订立的协议。</text>
      </view>

      <view class="section">
        <text class="section-title">二、账号注册与使用</text>
        <text class="section-text">2.1 您在使用{{ APP_CONFIG.name }} 服务前需要注册一个账号。账号应当使用手机号码绑定注册，请您使用尚未与{{ APP_CONFIG.name }} 账号绑定的手机号码，以及未被{{ APP_CONFIG.name }} 根据本协议封禁的手机号码注册{{ APP_CONFIG.name }} 账号。</text>
        <text class="section-text">2.2 您在注册账号时不得以虚假信息或他人信息注册账号，不得未经他人许可而使用他人手机号码进行注册。</text>
        <text class="section-text">2.3 您注册成功后，{{ APP_CONFIG.name }} 将给予您一个用户账号及相应的密码，该用户账号和密码由用户负责保管。</text>
      </view>

      <view class="section">
        <text class="section-title">三、用户个人信息保护</text>
        <text class="section-text">3.1 保护用户个人信息是{{ APP_CONFIG.name }} 的一项基本原则。</text>
        <text class="section-text">3.2 您在注册账号或使用{{ APP_CONFIG.name }} 服务的过程中，需要提供一些必要的信息，例如：为向您提供账号注册服务或进行用户身份识别，需要您填写手机号码。</text>
        <text class="section-text">3.3 {{ APP_CONFIG.name }} 将运用各种安全技术和程序建立完善的管理制度来保护您的个人信息，以免遭受未经授权的访问、使用或披露。</text>
      </view>

      <view class="section">
        <text class="section-title">四、使用规则</text>
        <text class="section-text">4.1 您在使用{{ APP_CONFIG.name }} 服务时，必须遵守中华人民共和国相关法律法规的规定，不得利用{{ APP_CONFIG.name }} 服务从事违法活动。</text>
        <text class="section-text">4.2 您在使用{{ APP_CONFIG.name }} 服务时，不得从事以下行为：</text>
        <text class="section-text">• 发布或分享计算机病毒、木马或其他恶意程序</text>
        <text class="section-text">• 未经授权访问他人账号或数据</text>
        <text class="section-text">• 干扰或破坏{{ APP_CONFIG.name }} 服务的正常运行</text>
      </view>

      <view class="section">
        <text class="section-title">五、服务的变更、中断或终止</text>
        <text class="section-text">5.1 {{ APP_CONFIG.name }} 可能会对服务内容进行变更，也可能会中断、中止或终止服务。</text>
        <text class="section-text">5.2 如发生下列任何一种情形，{{ APP_CONFIG.name }} 有权不经通知而中断或终止向您提供服务：</text>
        <text class="section-text">• 根据法律规定您应提交真实信息，而您提供的个人资料不真实、或与注册时信息不一致又未能提供合理证明</text>
        <text class="section-text">• 您违反相关法律法规或本协议的约定</text>
      </view>

      <view class="section">
        <text class="section-title">六、其他</text>
        <text class="section-text">6.1 本协议的订立、执行和解释及争议的解决均应适用中华人民共和国法律。</text>
        <text class="section-text">6.2 如双方就本协议内容或其执行发生任何争议，双方应尽力友好协商解决；协商不成时，任何一方均可向{{ APP_CONFIG.name }} 所在地的人民法院提起诉讼。</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { APP_CONFIG } from '@/config/app.js'
import { getTotalNavHeight } from '@/utils/system.js'
import NavBar from '@/components/NavBar.vue'

const navHeight = ref(0)

onMounted(() => {
  navHeight.value = getTotalNavHeight()
})
</script>

<style lang="scss" scoped>
.protocol-container {
  min-height: 100vh;
  background: $glass-bg;
}

.custom-nav {
  position: sticky;
  top: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $glass-text-main;
}

.protocol-content {
  padding: 48rpx 32rpx;
}

.protocol-header {
  margin-bottom: 64rpx;
  text-align: center;
}

.protocol-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: $glass-text-main;
  margin-bottom: 16rpx;
}

.update-time {
  display: block;
  font-size: 24rpx;
  color: $glass-text-muted;
}

.section {
  margin-bottom: 48rpx;
}

.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: $glass-text-main;
  margin-bottom: 24rpx;
}

.section-text {
  display: block;
  font-size: 28rpx;
  line-height: 1.8;
  color: $glass-text-main;
  margin-bottom: 16rpx;
  text-align: justify;
}
</style>
