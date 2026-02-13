# Dashboard 页面添加更新按钮示例

如果需要在 dashboard 页面添加手动检查更新的功能，可以参考以下代码：

## 方案一：在个人中心添加检查更新按钮

由于 dashboard 是首页，不适合添加"检查更新"按钮。建议在 `pages/profile/index.vue`（个人中心）添加。

### 示例代码

```vue
<template>
  <view class="profile-page">
    <!-- 其他内容 -->
    
    <!-- 设置区域 -->
    <view class="settings-section">
      <view class="setting-item" @click="handleCheckUpdate">
        <text class="setting-label">检查更新</text>
        <text class="setting-value">当前版本 v1.0.0</text>
        <text class="setting-arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import updateManager from '@/utils/updateManager.js'

// 检查更新
const handleCheckUpdate = () => {
  // 显示加载提示
  uni.showLoading({
    title: '检查中...',
    mask: true
  })
  
  // 延迟关闭加载提示（因为微信会自动检查）
  setTimeout(() => {
    uni.hideLoading()
    
    // 如果没有弹出更新提示，说明已是最新版本
    uni.showToast({
      title: '当前已是最新版本',
      icon: 'success',
      duration: 2000
    })
  }, 1500)
  
  // 触发检查（实际上微信会自动检查）
  updateManager.checkForUpdate()
}
</script>

<style lang="scss" scoped>
.settings-section {
  margin-top: 20rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 0 32rpx;
}

.setting-item {
  display: flex;
  align-items: center;
  height: 100rpx;
  border-bottom: 1rpx solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
}

.setting-label {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.setting-value {
  font-size: 26rpx;
  color: #999;
  margin-right: 16rpx;
}

.setting-arrow {
  font-size: 32rpx;
  color: #ccc;
}
</style>
```

## 方案二：在 Dashboard 页面添加隐藏入口

如果确实需要在 dashboard 页面添加，可以使用长按或多次点击触发：

```vue
<template>
  <view class="dashboard-page">
    <!-- 在页面某个位置添加隐藏触发区域 -->
    <view 
      class="version-trigger" 
      @longpress="handleCheckUpdate"
      @click="handleVersionClick"
    >
      <!-- 可以是logo或其他元素 -->
    </view>
    
    <!-- 其他内容 -->
  </view>
</template>

<script setup>
import { ref } from 'vue'
import updateManager from '@/utils/updateManager.js'

// 版本点击计数（连续点击5次触发）
const versionClickCount = ref(0)
let versionClickTimer = null

// 长按检查更新
const handleCheckUpdate = () => {
  console.log('[Dashboard] 触发更新检查')
  
  uni.showModal({
    title: '检查更新',
    content: '是否检查小程序更新？',
    success: (res) => {
      if (res.confirm) {
        updateManager.checkForUpdate()
      }
    }
  })
}

// 连续点击检查更新
const handleVersionClick = () => {
  versionClickCount.value++
  
  // 清除之前的定时器
  if (versionClickTimer) {
    clearTimeout(versionClickTimer)
  }
  
  // 2秒内连续点击5次触发
  if (versionClickCount.value >= 5) {
    versionClickCount.value = 0
    handleCheckUpdate()
    return
  }
  
  // 2秒后重置计数
  versionClickTimer = setTimeout(() => {
    versionClickCount.value = 0
  }, 2000)
}
</script>

<style lang="scss" scoped>
.version-trigger {
  // 隐藏的触发区域样式
  position: fixed;
  bottom: 120rpx;
  right: 32rpx;
  width: 80rpx;
  height: 80rpx;
  opacity: 0; // 完全透明
  z-index: 999;
}
</style>
```

## 方案三：在开发模式下显示更新按钮

只在开发/测试环境显示更新按钮：

```vue
<template>
  <view class="dashboard-page">
    <!-- 开发模式下的更新按钮 -->
    <view v-if="isDev" class="dev-tools">
      <button @click="handleCheckUpdate" size="mini" type="warn">
        检查更新
      </button>
    </view>
    
    <!-- 其他内容 -->
  </view>
</template>

<script setup>
import { ref } from 'vue'
import updateManager from '@/utils/updateManager.js'

// 判断是否为开发模式
const isDev = ref(false)

// #ifdef MP-WEIXIN
// 可以通过账号信息判断是否为开发者
const checkDevMode = async () => {
  try {
    const accountInfo = uni.getAccountInfoSync()
    // 体验版或开发版显示
    isDev.value = accountInfo.miniProgram.envVersion !== 'release'
  } catch (error) {
    console.error('获取账号信息失败:', error)
  }
}

checkDevMode()
// #endif

const handleCheckUpdate = () => {
  updateManager.checkForUpdate()
}
</script>

<style lang="scss" scoped>
.dev-tools {
  position: fixed;
  top: 100rpx;
  right: 20rpx;
  z-index: 9999;
  background: rgba(255, 0, 0, 0.1);
  padding: 10rpx;
  border-radius: 8rpx;
}
</style>
```

## 推荐方案

**最佳实践**：
1. 保持 App.vue 中的全局自动更新（已实现）
2. 在个人中心页面添加"检查更新"选项（方案一）
3. 不在 dashboard 首页添加更新按钮，避免干扰用户

**原因**：
- 微信小程序会自动检查更新，无需频繁手动触发
- 首页应该专注于核心功能展示
- 个人中心是放置设置类功能的最佳位置
- 全局自动更新已经能满足99%的场景

## 注意事项

1. **不要过度使用手动检查**
   - 微信会自动检查，手动检查只是触发监听器
   - 频繁检查不会加快更新速度

2. **用户体验优先**
   - 不要在首页显眼位置放置更新按钮
   - 不要频繁打扰用户检查更新
   - 让更新过程尽可能自然流畅

3. **测试环境限制**
   - 开发工具无法测试更新功能
   - 需要发布体验版或正式版测试
   - 建议使用方案三在测试时显示更新按钮

## 总结

当前实现的全局自动更新方案已经非常完善，能够满足绝大多数场景。如果确实需要手动检查更新的入口，建议：

1. ✅ 优先在个人中心添加（方案一）
2. ⚠️ 谨慎在首页添加隐藏入口（方案二）
3. 🔧 开发测试时使用开发模式按钮（方案三）

**当前状态**：全局自动更新已在 `App.vue` 中完美实现，无需额外操作。
