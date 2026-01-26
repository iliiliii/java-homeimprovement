# 微信 Code 重复使用问题修复说明

## 问题描述

### 错误信息
```
AppAuthServiceImpl - [checkOpenidBinding,122] - 检查openid绑定状态失败: 
微信登录失败: code been used, rid: 697788ee-4e97caaa-031328f4
com.ruoyi.common.exception.ServiceException: 微信登录失败: code been used
```

### 问题原因

**微信 code 的特性**:
1. **一次性使用**: 每个 code 只能调用一次微信 API
2. **有效期短**: code 有效期只有 5 分钟
3. **不可重复**: 使用后立即失效

**原有实现的问题**:
```javascript
// 登录时
const loginRes = await uni.login({ provider: 'weixin' })
const checkResult = await checkOpenidBinding(loginRes.code)  // ✅ 第一次使用 code
uni.setStorageSync('wechatCode', loginRes.code)              // ❌ 保存已使用的 code

// 导入数据时
const wechatCode = uni.getStorageSync('wechatCode')          // ❌ 获取已使用的 code
const checkResult = await checkOpenidBinding(wechatCode)     // ❌ 第二次使用 code，报错！
```

**问题流程**:
```
1. 用户点击"微信登录"
   ↓
2. 获取 code: "abc123"
   ↓
3. 调用 checkOpenidBinding(code) ✅ 成功，code 被使用
   ↓
4. 保存 code 到本地存储
   ↓
5. 进入游客模式
   ↓
6. 用户点击"导入历史数据"
   ↓
7. 读取保存的 code: "abc123"
   ↓
8. 再次调用 checkOpenidBinding(code) ❌ 失败：code been used
```

## 解决方案

### 核心思路

**保存 openid 而不是 code**，因为：
- ✅ openid 可以重复使用
- ✅ openid 不会过期（除非用户取消授权）
- ✅ openid 是用户的唯一标识
- ❌ code 只能使用一次
- ❌ code 有效期只有 5 分钟

### 修改内容

#### 1. 登录页面 (`uni3/src/pages/login/index-new.vue`)

**修改前**:
```javascript
if (checkResult.isBound) {
  await performOpenidLogin(checkResult.openid)
} else {
  // 保存 openid 和 code
  uni.setStorageSync('wechatOpenid', checkResult.openid)
  uni.setStorageSync('wechatCode', loginRes.code)  // ❌ 保存已使用的 code
  uni.setStorageSync('guestMode', true)
}
```

**修改后**:
```javascript
if (checkResult.isBound) {
  await performOpenidLogin(checkResult.openid)
} else {
  // 只保存 openid，不保存 code
  uni.setStorageSync('wechatOpenid', checkResult.openid)  // ✅ 保存 openid
  // 不再保存 code，因为 code 只能使用一次
  uni.setStorageSync('guestMode', true)
}
```

#### 2. 个人中心 (`uni3/src/pages/profile/index.vue`)

**修改前**:
```javascript
const performPhoneBinding = async (phone) => {
  // 获取保存的 code
  const wechatCode = uni.getStorageSync('wechatCode')  // ❌ 获取已使用的 code
  
  // 使用 code 检查绑定状态
  const checkResult = await checkOpenidBinding(wechatCode)  // ❌ 报错：code been used
  
  // 使用 openid 绑定
  const result = await bindPhoneToOpenid({
    openid: checkResult.openid,
    phone: phone,
    deviceId: getDeviceId()
  })
}
```

**修改后**:
```javascript
const performPhoneBinding = async (phone) => {
  // 直接获取保存的 openid
  const wechatOpenid = uni.getStorageSync('wechatOpenid')  // ✅ 获取 openid
  
  if (!wechatOpenid) {
    throw new Error('微信登录信息已过期，请重新登录')
  }
  
  // 直接使用 openid 绑定，不需要再次检查
  const result = await bindPhoneToOpenid({
    openid: wechatOpenid,  // ✅ 直接使用 openid
    phone: phone,
    deviceId: getDeviceId()
  })
}
```

#### 3. 退出登录清理

**修改前**:
```javascript
const handleLogout = () => {
  uni.removeStorageSync('guestMode')
  uni.removeStorageSync('wechatCode')  // ❌ 清除 code
  userStore.logout()
}
```

**修改后**:
```javascript
const handleLogout = () => {
  uni.removeStorageSync('guestMode')
  uni.removeStorageSync('wechatOpenid')  // ✅ 清除 openid
  userStore.logout()
}
```

## 优化后的流程

### 新的数据流转

```
1. 用户点击"微信登录"
   ↓
2. 获取 code: "abc123"
   ↓
3. 调用 checkOpenidBinding(code)
   ↓
4. 后端返回: { openid: "xyz789", isBound: false }
   ↓
5. 保存 openid 到本地存储 ✅
   uni.setStorageSync('wechatOpenid', 'xyz789')
   ↓
6. 进入游客模式
   ↓
7. 用户点击"导入历史数据"
   ↓
8. 读取保存的 openid: "xyz789" ✅
   ↓
9. 直接调用 bindPhoneToOpenid(openid, phone) ✅ 成功！
```

### 本地存储结构

**游客模式**:
```javascript
{
  guestMode: true,
  wechatOpenid: "xyz789",  // ✅ 保存 openid
  // wechatCode: "abc123"  // ❌ 不再保存 code
}
```

**正式用户**:
```javascript
{
  guestMode: false,
  token: "...",
  userType: "customer",
  userInfo: { ... }
  // wechatOpenid 可以保留或清除
}
```

## 技术细节

### 为什么 openid 可以重复使用？

1. **openid 的本质**:
   - openid 是微信用户在当前小程序下的唯一标识
   - 由微信服务器生成，不会改变（除非用户取消授权）
   - 不是临时凭证，而是永久标识

2. **openid 的获取**:
   - 通过 code 调用微信 API 获取
   - 一个 code 只能换取一次 openid
   - 但 openid 获取后可以永久使用

3. **openid 的使用场景**:
   - 用户身份识别
   - 账号绑定
   - 消息推送
   - 数据关联

### 为什么 code 不能重复使用？

1. **安全性考虑**:
   - code 是临时授权凭证
   - 防止 code 被截获后重复使用
   - 确保每次授权都是新的

2. **时效性要求**:
   - code 有效期只有 5 分钟
   - 使用后立即失效
   - 过期后需要重新获取

3. **微信的设计**:
   - code 用于换取 openid 和 session_key
   - 换取后 code 的使命就完成了
   - 后续操作应该使用 openid

## API 调用优化

### 优化前的 API 调用

```javascript
// 登录时
checkOpenidBinding(code)  // 使用 code

// 导入数据时
checkOpenidBinding(code)  // ❌ 再次使用 code，报错
bindPhoneToOpenid(openid, phone)
```

### 优化后的 API 调用

```javascript
// 登录时
checkOpenidBinding(code)  // 使用 code，获取 openid

// 导入数据时
// 不需要再次调用 checkOpenidBinding
bindPhoneToOpenid(openid, phone)  // ✅ 直接使用保存的 openid
```

**优势**:
1. 减少了一次 API 调用
2. 避免了 code 重复使用的问题
3. 提升了性能和用户体验

## 后端 API 说明

### checkOpenidBinding 接口

**用途**: 通过 code 获取 openid 并检查绑定状态

**调用时机**: 
- ✅ 微信登录时（第一次获取 openid）
- ❌ 导入数据时（不应该再次调用）

**参数**:
```javascript
{
  code: String,      // 微信登录凭证（一次性）
  deviceId: String
}
```

**返回**:
```javascript
{
  openid: String,    // 微信 openid（可重复使用）
  isBound: Boolean,  // 是否已绑定
  phone: String?     // 已绑定的手机号（脱敏）
}
```

### bindPhoneToOpenid 接口

**用途**: 绑定手机号到 openid

**调用时机**:
- ✅ 导入历史数据时
- ✅ 任何需要绑定的场景

**参数**:
```javascript
{
  openid: String,    // 微信 openid（不是 code）
  phone: String,     // 手机号
  deviceId: String
}
```

**返回**:
```javascript
{
  token: String,
  userInfo: Object,
  projects: Array,
  ...
}
```

## 错误处理

### 微信信息过期处理

```javascript
const wechatOpenid = uni.getStorageSync('wechatOpenid')

if (!wechatOpenid) {
  // openid 不存在，需要重新登录
  uni.showModal({
    title: '登录信息已过期',
    content: '请重新登录后再试',
    showCancel: false,
    confirmText: '重新登录',
    success: () => {
      uni.removeStorageSync('guestMode')
      uni.removeStorageSync('wechatOpenid')
      uni.reLaunch({ url: '/pages/login/index-new' })
    }
  })
  return
}
```

### 绑定失败处理

```javascript
try {
  await bindPhoneToOpenid({ openid, phone, deviceId })
} catch (error) {
  if (error.message.includes('登录信息已过期')) {
    // 引导用户重新登录
    showReloginDialog()
  } else if (error.message.includes('已绑定其他')) {
    // 账号已绑定其他微信
    showBindingConflictDialog()
  } else {
    // 其他错误
    uni.showToast({ title: error.message, icon: 'none' })
  }
}
```

## 测试验证

### 测试场景 1: 正常流程

```
1. 清除缓存
2. 点击"微信登录"
3. 进入游客模式 ✅
4. 点击"导入历史数据"
5. 输入账号
6. 绑定成功 ✅
```

### 测试场景 2: 重复导入

```
1. 游客模式下
2. 点击"导入历史数据"
3. 输入账号 A
4. 绑定失败（账号不存在）
5. 重新点击"导入历史数据"
6. 输入账号 B
7. 绑定成功 ✅（不会报 code been used）
```

### 测试场景 3: 关闭后重新打开

```
1. 游客模式下
2. 关闭小程序
3. 重新打开小程序
4. 自动进入游客模式 ✅
5. 点击"导入历史数据"
6. 输入账号
7. 绑定成功 ✅（openid 仍然有效）
```

## 注意事项

### 1. openid 的有效性

- openid 在用户取消授权前一直有效
- 如果用户删除小程序后重新添加，openid 不变
- 如果用户换了微信账号，openid 会改变

### 2. 安全性考虑

- openid 不是敏感信息，可以存储在本地
- 但不应该在日志中明文输出完整的 openid
- 建议在日志中只输出前 10 位

```javascript
// ❌ 不推荐
console.log('openid:', openid)

// ✅ 推荐
console.log('openid:', openid.substr(0, 10) + '...')
```

### 3. 清理策略

- 退出登录时清除 openid
- 重新登录时更新 openid
- 定期检查 openid 的有效性

## 相关文档

- [微信小程序登录文档](https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html)
- [微信登录交互优化完成说明](./微信登录交互优化完成说明.md)
- [微信登录优化测试指南](./微信登录优化测试指南.md)

## 更新日志

- 2025-01-26: 修复 code 重复使用问题，改为保存 openid

---

**维护**: 开发团队  
**更新**: 2025-01-26
