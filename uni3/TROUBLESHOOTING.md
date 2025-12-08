# 问题排查指南

## 已修复的问题

### ✅ 问题1: module '@/store/user.js' is not defined

**错误信息**：
```
Error: module '@/store/user.js' is not defined, require args is
```

**原因**：
在 `main.js` 中使用了 `require` 动态导入，但 uni-app 在某些环境下不支持这种方式。

**解决方案**：
改用 ES6 的 `import` 语法：

```javascript
// ❌ 错误的方式
const { useUserStore } = require('@/store/user')

// ✅ 正确的方式
import { useUserStore } from '@/store/user'
```

**修改的文件**：
- `uni3/src/main.js`

---

## 常见问题

### Q1: 页面显示空白

**可能原因**：
1. 路由守卫拦截了页面访问
2. 组件导入路径错误
3. 样式文件未正确加载

**排查步骤**：
1. 打开控制台查看错误信息
2. 检查是否已登录（使用开发者模式快速登录）
3. 检查组件导入路径是否正确

**解决方案**：
```javascript
// 使用开发者模式快速登录
// 在登录页面点击"🔧 开发者模式"
// 选择"🚀 直接进入首页"
```

### Q2: 路由守卫一直拦截

**可能原因**：
1. 未登录
2. Token未正确保存
3. `isLoggedIn()` 函数返回错误

**排查步骤**：
```javascript
// 在控制台检查登录状态
import { useUserStore } from '@/store/user'
const userStore = useUserStore()

console.log('Token:', userStore.token)
console.log('是否已登录:', !!userStore.token)
```

**解决方案**：
使用开发者模式登录，或检查 Token 是否正确保存。

### Q3: 开发者模式按钮不显示

**可能原因**：
`showDevMode` 设置为 `false`

**解决方案**：
在 `pages/login/index.vue` 中：
```javascript
const showDevMode = ref(true)  // 确保设为 true
```

### Q4: 点击开发者模式按钮没反应

**可能原因**：
1. `useUserStore` 未正确导入
2. 控制台有错误

**排查步骤**：
1. 打开控制台查看错误
2. 检查 `store/user.js` 是否存在
3. 检查导入路径是否正确

**解决方案**：
```javascript
// 确保正确导入
import { useUserStore } from '@/store/user'
```

### Q5: 提示"该功能仅员工可用"

**可能原因**：
使用客户身份访问员工专属页面

**解决方案**：
使用"👨‍💼 模拟员工登录"重新登录

### Q6: API请求失败

**可能原因**：
1. 后端服务未启动
2. API地址配置错误
3. Token无效（开发者模式的Token是模拟的）

**排查步骤**：
1. 检查后端服务是否运行
2. 检查 `utils/request.js` 中的 `BASE_URL`
3. 查看控制台的网络请求

**解决方案**：
```javascript
// 在 utils/request.js 中配置正确的API地址
const BASE_URL = 'http://localhost:8080'  // 改成你的后端地址
```

### Q7: 微信登录失败

**可能原因**：
1. 不在真机环境
2. 小程序未认证
3. 未开通手机号权限

**解决方案**：
1. 使用真机调试
2. 确保小程序已认证
3. 在微信公众平台开通"手机号快速验证组件"

### Q8: Token自动刷新失败

**可能原因**：
1. RefreshToken过期
2. 后端刷新接口未实现
3. 网络请求失败

**排查步骤**：
```javascript
// 检查RefreshToken
import { useUserStore } from '@/store/user'
const userStore = useUserStore()

console.log('RefreshToken:', userStore.refreshToken)
```

**解决方案**：
重新登录获取新的Token

---

## 调试技巧

### 1. 查看完整的用户状态

```javascript
import { useUserStore } from '@/store/user'
const userStore = useUserStore()

console.log('=== 用户状态 ===')
console.log('Token:', userStore.token)
console.log('RefreshToken:', userStore.refreshToken)
console.log('用户类型:', userStore.userType)
console.log('用户ID:', userStore.userId)
console.log('用户信息:', userStore.userInfo)
console.log('项目列表:', userStore.projects)
console.log('当前项目ID:', userStore.currentProjectId)
console.log('当前项目:', userStore.currentProject)
console.log('是否已登录:', !!userStore.token)
console.log('是否客户:', userStore.isCustomer)
console.log('是否员工:', userStore.isStaff)
```

### 2. 测试路由守卫

```javascript
import { hasPagePermission, isLoggedIn } from '@/utils/permission'

console.log('=== 路由守卫测试 ===')
console.log('是否已登录:', isLoggedIn())
console.log('能访问工地巡视吗:', hasPagePermission('/pages/inspection/list'))
console.log('能访问首页吗:', hasPagePermission('/pages/dashboard/index'))
```

### 3. 清除所有缓存

```javascript
// 清除登录状态
import { useUserStore } from '@/store/user'
const userStore = useUserStore()
userStore.logout()

// 或者手动清除
uni.clearStorageSync()
```

### 4. 模拟不同场景

```javascript
// 场景1: 模拟Token过期
import { useUserStore } from '@/store/user'
const userStore = useUserStore()
userStore.setToken('expired_token')

// 场景2: 模拟客户访问员工页面
userStore.userType = 'customer'
uni.navigateTo({ url: '/pages/inspection/list' })

// 场景3: 模拟员工访问
userStore.userType = 'staff'
uni.navigateTo({ url: '/pages/inspection/list' })
```

---

## 开发环境配置

### 1. 微信开发者工具设置

- ✅ 勾选"不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书"
- ✅ 勾选"启用调试"
- ✅ 打开"调试器"查看控制台

### 2. 本地存储查看

在微信开发者工具中：
1. 点击"调试器"
2. 选择"Storage"标签
3. 查看"Storage" → "wx.storage"
4. 可以看到所有保存的数据

### 3. 网络请求查看

在微信开发者工具中：
1. 点击"调试器"
2. 选择"Network"标签
3. 查看所有网络请求
4. 检查请求头、响应等

---

## 生产环境检查清单

上线前请确保：

- [ ] 关闭开发者模式（`showDevMode = false`）
- [ ] 配置正确的生产API地址
- [ ] 配置微信小程序AppID和AppSecret
- [ ] 在微信公众平台配置服务器域名
- [ ] 开通手机号快速验证组件权限
- [ ] 测试所有登录方式
- [ ] 测试Token刷新机制
- [ ] 测试权限控制
- [ ] 测试真机环境

---

## 获取帮助

如果以上方法都无法解决问题：

1. **查看文档**
   - [QUICK_START.md](./QUICK_START.md)
   - [DEV_MODE_GUIDE.md](./DEV_MODE_GUIDE.md)

2. **查看控制台**
   - 打开微信开发者工具的调试器
   - 查看Console中的错误信息
   - 查看Network中的网络请求

3. **检查代码**
   - 确保所有文件都已正确创建
   - 确保导入路径正确
   - 确保没有语法错误

4. **重新编译**
   ```bash
   # 停止当前编译
   # 重新运行
   npm run dev:mp-weixin
   ```

5. **清除缓存**
   - 在微信开发者工具中点击"清缓存" → "清除全部缓存"
   - 重新编译项目

---

**祝调试顺利！** 🐛
