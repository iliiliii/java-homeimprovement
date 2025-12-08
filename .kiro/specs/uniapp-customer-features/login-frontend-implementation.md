# 小程序登录功能前端实现说明

## 已完成的文件

### 1. 新建文件

| 文件路径 | 说明 | 状态 |
|---------|------|------|
| `uni3/src/pages/login/index-new.vue` | 新版登录页面（支持三种登录方式） | ✅ 已创建 |
| `uni3/src/api/auth.js` | 认证相关API接口 | ✅ 已创建 |
| `uni3/src/utils/device.js` | 设备ID生成和管理 | ✅ 已创建 |
| `uni3/src/utils/permission.js` | 权限判断工具 | ✅ 已创建 |

### 2. 更新文件

| 文件路径 | 更新内容 | 状态 |
|---------|---------|------|
| `uni3/src/store/user.js` | 完善用户状态管理，添加角色、Token刷新等 | ✅ 已更新 |
| `uni3/src/utils/request.js` | 添加Token自动刷新、设备ID、项目ID等 | ✅ 已更新 |
| `uni3/src/main.js` | 添加路由守卫，拦截未登录和无权限访问 | ✅ 已更新 |

---

## 使用说明

### 步骤1：替换登录页面

将新版登录页面替换旧版：

```bash
# 备份旧版
mv uni3/src/pages/login/index.vue uni3/src/pages/login/index-old.vue

# 使用新版
mv uni3/src/pages/login/index-new.vue uni3/src/pages/login/index.vue
```

或者直接复制 `index-new.vue` 的内容到 `index.vue`。

### 步骤2：配置API地址

在 `uni3/src/utils/request.js` 中修改API地址：

```javascript
// 开发环境
const BASE_URL = 'http://localhost:8080'  // 改成你的后端地址

// 生产环境
const BASE_URL = 'https://api.yourdomain.com'  // 改成你的生产域名
```

### 步骤3：配置微信小程序

在微信公众平台配置：

1. **服务器域名**
   - request合法域名：`https://api.yourdomain.com`
   - uploadFile合法域名：`https://api.yourdomain.com`

2. **开通权限**
   - 小程序认证
   - 开通"手机号快速验证组件"

3. **配置AppID和AppSecret**
   - 在后端 `application.yml` 中配置

---

## 功能说明

### 1. 三种登录方式

#### 微信一键登录（推荐）

```vue
<button 
  open-type="getPhoneNumber" 
  @getphonenumber="handleWechatLogin"
>
  微信一键登录
</button>
```

**流程**：
1. 用户点击按钮
2. 微信弹出授权框
3. 用户同意后，获取 `phoneCode`
4. 同时调用 `uni.login()` 获取 `wxCode`
5. 发送到后端换取手机号和openId
6. 后端查询用户（先查customers，再查sys_user）
7. 返回Token和用户信息
8. 前端保存并跳转

#### 短信验证码登录

```javascript
// 发送验证码
await sendCode(phone)

// 登录
await smsLogin({ phone, code, deviceId })
```

**流程**：
1. 用户输入手机号
2. 点击"获取验证码"
3. 后端发送短信
4. 用户输入验证码
5. 提交登录
6. 后端验证验证码
7. 返回Token和用户信息

#### 密码登录

```javascript
await passwordLogin({ phone, password, deviceId })
```

**流程**：
1. 用户输入手机号和密码
2. 提交登录
3. 后端验证密码
4. 返回Token和用户信息

### 2. Token管理

#### 自动刷新

当Token过期（401）时，自动使用RefreshToken刷新：

```javascript
// request.js 中的逻辑
if (statusCode === 401) {
  // 尝试刷新Token
  const newToken = await refreshTokenRequest(refreshToken)
  // 保存新Token
  // 重试原请求
}
```

#### 手动刷新

```javascript
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
await userStore.refreshAccessToken()
```

### 3. 权限控制

#### 页面级权限

```javascript
// 员工专属页面
const staffOnlyPages = [
  '/pages/inspection/',  // 工地巡视
  '/pages/issue/',       // 问题上报
  '/pages/repair/'       // 整改记录
]

// 客户访问时会被拦截
if (isCustomer() && staffOnlyPages.includes(url)) {
  uni.showToast({ title: '该功能仅员工可用', icon: 'none' })
  return false
}
```

#### 组件级权限

```vue
<template>
  <!-- 只有员工可见 -->
  <view v-if="isStaff">
    <button>上报问题</button>
  </view>
  
  <!-- 只有客户可见 -->
  <view v-if="isCustomer">
    <text>客户专属内容</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isStaff = computed(() => userStore.isStaff)
const isCustomer = computed(() => userStore.isCustomer)
</script>
```

### 4. 用户状态管理

#### 获取用户信息

```javascript
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 用户类型
console.log(userStore.userType)  // 'customer' 或 'staff'

// 用户信息
console.log(userStore.userInfo)  // { id, name, phone, avatar }

// 项目列表
console.log(userStore.projects)  // [{ id, name, ... }]

// 当前项目
console.log(userStore.currentProject)

// 是否已登录
console.log(userStore.isLoggedIn)

// 是否员工
console.log(userStore.isStaff)

// 是否客户
console.log(userStore.isCustomer)
```

#### 切换项目

```javascript
userStore.switchProject('P001')
```

#### 退出登录

```javascript
userStore.logout()
// 会自动清除所有状态并跳转登录页
```

---

## API接口说明

### 后端需要实现的接口

#### 1. 微信登录

```
POST /app/auth/wechat-login

Request:
{
  "code": "微信登录凭证",
  "phoneCode": "手机号动态令牌",
  "deviceId": "设备唯一标识"
}

Response:
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "expiresIn": 7200,
    "userType": "customer",  // 或 "staff"
    "userInfo": {
      "id": "C001",
      "name": "张三",
      "phone": "138****8000",
      "avatar": "https://..."
    },
    "projects": [
      {
        "id": "P001",
        "code": "P2025001",
        "name": "万科城市花园A栋1001",
        "status": "construction"
      }
    ]
  }
}
```

#### 2. 短信验证码登录

```
POST /app/auth/sms-login

Request:
{
  "phone": "13800138000",
  "code": "123456",
  "deviceId": "设备唯一标识"
}

Response: 同微信登录
```

#### 3. 密码登录

```
POST /app/auth/password-login

Request:
{
  "phone": "13800138000",
  "password": "加密后的密码",
  "deviceId": "设备唯一标识"
}

Response: 同微信登录
```

#### 4. 发送验证码

```
POST /app/auth/send-code

Request:
{
  "phone": "13800138000"
}

Response:
{
  "code": 200,
  "msg": "验证码已发送",
  "data": {
    "expireTime": 300
  }
}
```

#### 5. 刷新Token

```
POST /app/auth/refresh-token

Request:
{
  "refreshToken": "eyJhbGc..."
}

Response:
{
  "code": 200,
  "data": {
    "accessToken": "new_token...",
    "refreshToken": "new_refresh_token...",
    "expiresIn": 7200
  }
}
```

#### 6. 验证Token

```
POST /app/auth/validate-token

Headers:
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "msg": "Token有效"
}
```

#### 7. 退出登录

```
POST /app/auth/logout

Headers:
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "msg": "退出成功"
}
```

---

## 测试步骤

### 1. 开发环境测试

1. **启动后端服务**
   ```bash
   cd sb3
   mvn spring-boot:run
   ```

2. **配置开发者工具**
   - 打开微信开发者工具
   - 导入uni3项目
   - 在"详情-本地设置"中勾选"不校验合法域名"

3. **测试短信登录**
   - 输入手机号
   - 点击"获取验证码"
   - 输入验证码
   - 点击"登录"
   - 验证是否跳转首页

4. **测试密码登录**
   - 切换到密码登录
   - 输入手机号和密码
   - 点击"登录"
   - 验证是否跳转首页

5. **测试微信登录**（需要真机）
   - 点击"微信一键登录"
   - 授权手机号
   - 验证是否登录成功

### 2. 真机测试

1. **上传体验版**
   - 在微信开发者工具中点击"上传"
   - 填写版本号和备注
   - 上传成功

2. **添加体验成员**
   - 在微信公众平台添加体验成员
   - 体验成员扫码体验

3. **测试完整流程**
   - 测试三种登录方式
   - 测试Token刷新
   - 测试权限控制
   - 测试退出登录

---

## 常见问题

### 1. 微信登录失败

**问题**：点击"微信一键登录"没有反应

**解决**：
- 检查是否在真机上测试（开发者工具无法测试）
- 检查小程序是否已认证
- 检查是否开通"手机号快速验证组件"权限

### 2. 网络请求失败

**问题**：提示"不在合法域名列表中"

**解决**：
- 开发时：在开发者工具中关闭域名校验
- 生产时：在微信公众平台配置服务器域名

### 3. Token刷新失败

**问题**：Token过期后无法自动刷新

**解决**：
- 检查RefreshToken是否存在
- 检查RefreshToken是否过期
- 检查后端刷新接口是否正常

### 4. 权限拦截不生效

**问题**：客户可以访问员工专属页面

**解决**：
- 检查路由守卫是否正确配置
- 检查userType是否正确保存
- 检查页面路径是否在staffOnlyPages列表中

---

## 下一步

前端登录功能已完成，接下来需要：

1. **后端实现**
   - 创建数据库表
   - 实现认证接口
   - 实现Token管理
   - 实现微信登录服务

2. **联调测试**
   - 前后端联调
   - 功能测试
   - 性能测试

3. **上线准备**
   - 配置生产环境
   - 提交小程序审核
   - 发布上线

参考文档：
- [login-implementation-plan.md](./login-implementation-plan.md) - 完整实现计划
- [design-final.md](./design-final.md) - 技术设计方案
