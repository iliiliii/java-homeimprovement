<template>
  <view class="privacy-container">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <u-icon name="arrow-left" color="#1A2B3C" size="24" />
      </view>
      <text class="nav-title">隐私政策</text>
    </view>

    <!-- 隐私政策内容 -->
    <view class="privacy-content">
      <view class="privacy-header">
        <text class="privacy-title">{{ APP_CONFIG.name }} 隐私政策</text>
        <text class="update-time">更新日期：2025年12月3日</text>
      </view>

      <view class="section">
        <text class="section-title">引言</text>
        <text class="section-text">{{ APP_CONFIG.name }}（以下简称“我们”）深知个人信息对您的重要性，并会尽全力保护您的个人信息安全可靠。我们致力于维持您对我们的信任，恪守以下原则，保护您的个人信息：权责一致原则、目的明确原则、选择同意原则、最少够用原则、确保安全原则、主体参与原则、公开透明原则等。</text>
      </view>

      <view class="section">
        <text class="section-title">一、我们如何收集和使用您的个人信息</text>
        <text class="section-text">个人信息是指以电子或者其他方式记录的能够单独或者与其他信息结合识别特定自然人身份或者反映特定自然人活动情况的各种信息。</text>
        <text class="section-text">我们会出于以下目的，收集和使用您的个人信息：</text>
        <text class="section-text">1.1 帮助您成为我们的用户</text>
        <text class="section-text">为完成创建账号，您需提供手机号码。我们将通过发送短信验证码的方式来验证您的身份是否有效。</text>
        <text class="section-text">1.2 为您提供工程管理服务</text>
        <text class="section-text">我们需要收集您的项目编号等信息，以便为您提供项目相关的施工进度、设计方案、物料清单等服务。</text>
      </view>

      <view class="section">
        <text class="section-title">二、我们如何使用 Cookie 和同类技术</text>
        <text class="section-text">2.1 为确保产品正常运转，我们会在您的计算机或移动设备上存储名为 Cookie 的小数据文件。</text>
        <text class="section-text">2.2 Cookie 通常包含标识符、站点名称以及一些号码和字符。借助于 Cookie，网站能够存储您的偏好等数据。</text>
        <text class="section-text">2.3 我们不会将 Cookie 用于本政策所述目的之外的任何用途。您可根据自己的偏好管理或删除 Cookie。</text>
      </view>

      <view class="section">
        <text class="section-title">三、我们如何共享、转让、公开披露您的个人信息</text>
        <text class="section-text">3.1 共享</text>
        <text class="section-text">我们不会与{{ APP_CONFIG.name }}以外的任何公司、组织和个人分享您的个人信息，但以下情况除外：</text>
        <text class="section-text">• 在获取明确同意的情况下共享</text>
        <text class="section-text">• 根据法律法规或政府主管部门的要求进行提供</text>
        <text class="section-text">3.2 转让</text>
        <text class="section-text">我们不会将您的个人信息转让给任何公司、组织和个人，但以下情况除外：在获得您的明确同意后，我们会向其他方转让您的个人信息。</text>
        <text class="section-text">3.3 公开披露</text>
        <text class="section-text">我们仅会在以下情况下，公开披露您的个人信息：</text>
        <text class="section-text">• 获得您明确同意后</text>
        <text class="section-text">• 基于法律的披露</text>
      </view>

      <view class="section">
        <text class="section-title">四、我们如何保护您的个人信息</text>
        <text class="section-text">4.1 我们已使用符合业界标准的安全防护措施保护您提供的个人信息，防止数据遭到未经授权访问、公开披露、使用、修改、损坏或丢失。</text>
        <text class="section-text">4.2 我们会采取一切合理可行的措施，保护您的个人信息。例如，在您的浏览器与"服务"之间交换数据时受 SSL 加密保护。</text>
        <text class="section-text">4.3 我们会采取一切合理可行的措施，确保未收集无关的个人信息。</text>
      </view>

      <view class="section">
        <text class="section-title">五、您的权利</text>
        <text class="section-text">按照中国相关的法律、法规、标准，以及其他国家、地区的通行做法，我们保障您对自己的个人信息行使以下权利：</text>
        <text class="section-text">5.1 访问您的个人信息</text>
        <text class="section-text">您有权访问您的个人信息，法律法规规定的例外情况除外。</text>
        <text class="section-text">5.2 更正您的个人信息</text>
        <text class="section-text">当您发现我们处理的关于您的个人信息有错误时，您有权要求我们做出更正。</text>
        <text class="section-text">5.3 删除您的个人信息</text>
        <text class="section-text">在以下情形中，您可以向我们提出删除个人信息的请求：</text>
        <text class="section-text">• 如果我们处理个人信息的行为违反法律法规</text>
        <text class="section-text">• 如果我们收集、使用您的个人信息，却未征得您的同意</text>
      </view>

      <view class="section">
        <text class="section-title">六、本政策如何更新</text>
        <text class="section-text">我们可能适时修订本政策的条款，该等修订构成本政策的一部分。如该等修订造成您在本政策下权利的实质减少，我们将在修订生效前通过在主页上显著位置提示或向您发送电子邮件或以其他方式通知您。</text>
      </view>

      <view class="section">
        <text class="section-title">七、如何联系我们</text>
        <text class="section-text">如果您对本隐私政策有任何疑问、意见或建议，可通过以下方式与我们联系：</text>
        <text class="section-text">客服电话：400-XXX-XXXX</text>
        <text class="section-text">电子邮箱：privacy@zhixiangjia.com</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { APP_CONFIG } from '@/config/app.js'

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.privacy-container {
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

.privacy-content {
  padding: 48rpx 32rpx;
}

.privacy-header {
  margin-bottom: 64rpx;
  text-align: center;
}

.privacy-title {
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
