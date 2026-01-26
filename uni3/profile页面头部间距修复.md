# Profile 页面头部间距修复

## 问题

优化布局后，page-content 内容被固定的 PageHeader 遮挡，需要为头部留出空间。

## 解决方案

为 `.page-content` 添加 `padding-top`，留出头部空间。

### 头部高度计算

PageHeader 组件的总高度 = 状态栏高度 + 导航栏高度

- **状态栏高度**：动态，约 20-44px（不同设备不同）
- **导航栏高度**：固定 56px
- **总高度**：约 76-100px

### 样式修改

```scss
.page-content {
  padding: 16rpx 32rpx 32rpx;
  // 为固定头部留出空间：状态栏高度(约44px) + 导航栏高度(56px) = 100px
  padding-top: 100px;
}
```

使用 `100px` 作为安全值：
- ✅ 适配大部分设备（iPhone X 及以上的刘海屏）
- ✅ 确保内容不被遮挡
- ✅ 简单可靠，不需要动态计算

## 其他页面参考

其他使用 PageHeader 的页面也采用类似方案：

### schedule/index.vue
```javascript
navHeight.value = statusBarHeight.value + 56
```

### design/index.vue
```javascript
// PageHeader (56) + StatusBar + Filter (80+24=104rpx approx)
let filterHeight = rooms.value.length > 0 ? uni.upx2px(124) : 0
```

## 测试验证

- [x] 内容不被头部遮挡
- [x] 下拉刷新正常
- [x] 滚动流畅
- [x] 不同设备显示正常

## 文件修改

- ✅ `uni3/src/pages/profile/index.vue` - 添加 padding-top: 100px
