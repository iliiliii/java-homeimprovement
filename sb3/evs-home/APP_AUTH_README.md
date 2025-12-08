# 小程序认证模块说明

## 模块概述

本模块实现了小程序的多角色认证功能，支持客户和员工两种角色登录。

## 目录结构

```
com.ruoyi.app/
├── controller/
│   └── AppAuthController.java      # 认证控制器
├── dto/
│   ├── request/
│   │   ├── WechatLoginRequest.java     # 微信登录请求
│   │   ├── SmsLoginRequest.java        # 短信登录请求
│   │   ├── PasswordLoginRequest.java   # 密码登录请求
│   │   ├── SendCodeRequest.java        # 发送验证码请求
│   │   └── RefreshTokenRequest.java    # 刷新Token请求
│   └── response/
│       ├── AppLoginResponse.java       # 登录响应
│       ├── AppUserInfo.java            # 用户信息
│       └── AppProjectInfo.java         # 项目信息
├── service/
│   ├── IAppAuthService.java            # 认证服务接口
│   └── impl/
│       └── AppAuthServiceImpl.java     # 认证服务实现
├── security/
│   ├── AppTokenManager.java            # Token管理器
│   └── AppContext.java                 # 用户上下文
└── enums/
    ├── UserTypeEnum.java               # 用户类型枚举
    └── LoginTypeEnum.java              # 登录类型枚举
```

## API接口

### 1. 微信登录

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
    "accessToken": "...",
    "refreshToken": "...",
    "expiresIn": 7200,
    "userType": "customer",
    "userInfo": { ... },
    "projects": [ ... ]
  }
}
```

### 2. 短信验证码登录

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

### 3. 密码登录

```
POST /app/auth/password-login

Request:
{
  "phone": "13800138000",
  "password": "密码",
  "deviceId": "设备唯一标识"
}

Response: 同微信登录
```

### 4. 发送验证码

```
POST /app/auth/send-code

Request:
{
  "phone": "13800138000"
}

Response:
{
  "code": 200,
  "msg": "验证码已发送"
}
```

### 5. 刷新Token

```
POST /app/auth/refresh-token

Request:
{
  "refreshToken": "..."
}

Response:
{
  "code": 200,
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "expiresIn": 7200,
    ...
  }
}
```

### 6. 验证Token

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

### 7. 退出登录

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

## 数据库表

需要执行 `sql/app_tables.sql` 创建以下表：

1. `app_login_logs` - 登录日志表
2. `app_tokens` - Token管理表
3. `app_sms_codes` - 短信验证码表
4. `app_wechat_bindings` - 微信绑定表
5. `app_audit_logs` - 审计日志表

## 配置说明

在 `application.yml` 中添加以下配置：

```yaml
# 小程序配置
app:
  token:
    # Token密钥（至少32字符）
    secret: ruoyi-app-secret-key-for-jwt-token-2025
    # Access Token有效期（小时）
    accessExpireHours: 2
    # Refresh Token有效期（天）
    refreshExpireDays: 7
  wechat:
    # 小程序AppID
    appid: wx1234567890abcdef
    # 小程序AppSecret
    secret: your_app_secret_here
```

## 使用说明

### 1. 执行数据库脚本

```bash
mysql -u root -p your_database < sql/app_tables.sql
```

### 2. 启动后端服务

```bash
mvn spring-boot:run
```

### 3. 测试接口

#### 发送验证码
```bash
curl -X POST http://localhost:8080/app/auth/send-code \
  -H "Content-Type: application/json" \
  -d '{"phone": "13800138000"}'
```

#### 短信登录
```bash
curl -X POST http://localhost:8080/app/auth/sms-login \
  -H "Content-Type: application/json" \
  -d '{"phone": "13800138000", "code": "123456", "deviceId": "test_device"}'
```

## 开发模式

在开发模式下，验证码会打印到控制台日志中：

```
【开发模式】手机号: 13800138000, 验证码: 123456
```

## 注意事项

1. **微信登录**：需要配置真实的微信小程序AppID和AppSecret
2. **短信服务**：生产环境需要对接真实的短信服务商
3. **Token安全**：生产环境需要使用更复杂的密钥
4. **验证码存储**：生产环境建议使用Redis存储验证码
5. **密码验证**：密码登录功能需要完善密码验证逻辑

## 后续优化

- [ ] 实现微信登录（对接微信API）
- [ ] 对接短信服务商
- [ ] 使用Redis存储验证码和Token
- [ ] 实现Token黑名单
- [ ] 添加登录日志持久化
- [ ] 添加审计日志
- [ ] 实现密码加密验证
