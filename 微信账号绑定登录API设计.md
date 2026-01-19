# 优化版微信登录API设计

## 业务流程

### 1. 静默检查阶段
```
小程序启动 → 获取微信code → 检查openid是否已绑定 → 已绑定直接登录 / 未绑定进入绑定流程
```

### 2. 绑定流程策略
```
未绑定用户 → 选择绑定方式：
1. 微信一键获取手机号（推荐）
2. 手动输入手机号 + 短信验证码
```

### 3. 已绑定用户登录
```
openid已绑定 → 直接使用openid登录 → 返回用户信息
```

## API接口设计

### 1. 检查openid绑定状态

**接口地址：** `POST /app/auth/check-openid`

**请求参数：**
```json
{
  "code": "微信登录凭证",
  "deviceId": "设备唯一标识"
}
```

**业务逻辑：**
1. 使用code向微信服务器获取openid
2. 在系统中查找该openid是否已绑定用户
3. 返回绑定状态和基本信息

**成功响应：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "isBound": true,
    "openid": "微信openid",
    "userId": "用户ID（已绑定时返回）",
    "userType": "customer|staff（已绑定时返回）"
  }
}
```

### 2. openid直接登录（已绑定用户）

**接口地址：** `POST /app/auth/openid-login`

**请求参数：**
```json
{
  "openid": "微信openid",
  "deviceId": "设备唯一标识"
}
```

**业务逻辑：**
1. 验证openid是否存在且已绑定
2. 检查用户状态是否正常
3. 生成JWT token
4. 返回完整用户信息

**成功响应：**
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "accessToken": "JWT访问令牌",
    "refreshToken": "刷新令牌",
    "expiresIn": 7200,
    "userType": "customer|staff",
    "userInfo": {
      "id": "用户ID",
      "name": "用户姓名",
      "phone": "138****8888",
      "avatar": "头像URL"
    },
    "projects": [...]
  }
}
```

### 3. 微信绑定登录（一键获取手机号）

**接口地址：** `POST /app/auth/wechat-bind-login`

**请求参数：**
```json
{
  "code": "微信登录凭证",
  "phoneCode": "手机号授权码",
  "deviceId": "设备唯一标识"
}
```

**业务逻辑：**
1. 获取openid和手机号
2. 验证手机号是否在系统中存在
3. 检查该手机号是否已绑定其他openid
4. 绑定openid到用户记录
5. 激活账号并返回登录信息

### 4. 手机号绑定登录（验证码方式）

**接口地址：** `POST /app/auth/phone-bind-login`

**请求参数：**
```json
{
  "code": "微信登录凭证",
  "phone": "手机号",
  "smsCode": "短信验证码",
  "deviceId": "设备唯一标识"
}
```

**业务逻辑：**
1. 验证短信验证码
2. 获取微信openid
3. 验证手机号是否在系统中存在
4. 绑定openid到用户记录
5. 返回登录信息

## 数据库设计优化

### 用户表字段
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  phone VARCHAR(11) UNIQUE NOT NULL COMMENT '手机号',
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  wechat_openid VARCHAR(100) UNIQUE COMMENT '微信openid',
  user_type ENUM('customer', 'staff') NOT NULL COMMENT '用户类型',
  status ENUM('inactive', 'active', 'disabled') DEFAULT 'inactive' COMMENT '账号状态',
  first_login_at TIMESTAMP NULL COMMENT '首次登录时间',
  last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 登录日志表
```sql
CREATE TABLE login_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户ID',
  openid VARCHAR(100) COMMENT '微信openid',
  phone VARCHAR(11) COMMENT '手机号',
  login_type ENUM('openid', 'wechat_bind', 'phone_bind', 'sms', 'password') NOT NULL,
  device_id VARCHAR(100) COMMENT '设备ID',
  ip_address VARCHAR(45) COMMENT 'IP地址',
  status ENUM('success', 'failed') NOT NULL,
  error_message TEXT COMMENT '错误信息',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 前端流程图

```
小程序启动
    ↓
获取微信code
    ↓
调用 /check-openid
    ↓
已绑定？ ——— 是 ——→ 调用 /openid-login ——→ 登录成功
    ↓ 否
显示绑定选择页面
    ↓
用户选择绑定方式
    ↓
微信一键绑定 ——→ 调用 /wechat-bind-login
    ↓
手动输入绑定 ——→ 输入手机号 ——→ 发送验证码 ——→ 调用 /phone-bind-login
    ↓
绑定成功 ——→ 登录成功
```

## 错误处理策略

### 前端错误分类
1. **网络错误** - 重试机制
2. **openid未绑定** - 进入绑定流程
3. **手机号不存在** - 联系管理员
4. **账号状态异常** - 联系管理员
5. **验证码错误** - 重新输入
6. **重复绑定** - 提示已绑定其他账号

### 后端错误码设计
```json
{
  "USER_NOT_FOUND": "手机号未在系统中注册",
  "ACCOUNT_INACTIVE": "账号尚未激活",
  "ACCOUNT_DISABLED": "账号已被禁用",
  "OPENID_ALREADY_BOUND": "该微信已绑定其他账号",
  "PHONE_ALREADY_BOUND": "该手机号已绑定其他微信",
  "SMS_CODE_INVALID": "验证码错误或已过期",
  "WECHAT_AUTH_FAILED": "微信授权失败"
}
```

## 安全考虑

1. **防重复绑定** - 一个openid只能绑定一个账号
2. **防恶意绑定** - 验证码限制频率
3. **会话安全** - JWT token过期机制
4. **数据加密** - 敏感信息加密存储
5. **操作日志** - 记录所有绑定和登录操作

## 用户体验优化

1. **静默检查** - 已绑定用户无感知登录
2. **多种绑定方式** - 适应不同用户习惯
3. **清晰的状态提示** - 每个步骤都有明确说明
4. **错误引导** - 出错时提供解决方案
5. **联系管理员** - 一键拨号功能