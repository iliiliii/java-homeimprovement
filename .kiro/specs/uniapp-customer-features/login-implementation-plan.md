# 小程序登录功能实现计划

> 基于 design-final.md 的三种登录方式实现

## 目标

实现完整的多角色登录系统，支持：
- ✅ 微信登录（优先，客户和员工都可用）
- ✅ 短信验证码登录（客户和员工都可用）
- ✅ 密码登录（主要给员工用）
- ✅ 自动角色识别（客户/员工）
- ✅ Token管理和自动刷新
- ✅ 基于角色的权限控制

---

## 第一阶段：前端基础设施完善（1天）

### 任务 1.1：完善用户状态管理

**文件**：`uni3/src/store/user.js`

**需要添加的字段**：
```javascript
{
  token: '',              // Access Token
  refreshToken: '',       // Refresh Token
  userType: '',          // 'customer' 或 'staff'
  userId: '',            // 用户ID
  userInfo: {
    id: '',
    name: '',
    phone: '',
    avatar: ''
  },
  projects: [],          // 用户关联的项目列表
  currentProjectId: ''   // 当前选中的项目ID
}
```

**需要添加的方法**：
- `setLoginInfo(data)` - 设置登录信息
- `isCustomer()` - 是否客户
- `isStaff()` - 是否员工
- `refreshAccessToken()` - 刷新Token
- `logout()` - 退出登录

### 任务 1.2：创建认证API模块

**新建文件**：`uni3/src/api/auth.js`

```javascript
/**
 * 认证相关API
 */

// 1. 微信登录
export const wechatLogin = (data) => {
  return post('/app/auth/wechat-login', data)
}

// 2. 短信验证码登录
export const smsLogin = (data) => {
  return post('/app/auth/sms-login', data)
}

// 3. 密码登录
export const passwordLogin = (data) => {
  return post('/app/auth/password-login', data)
}

// 4. 发送验证码
export const sendCode = (phone) => {
  return post('/app/auth/send-code', { phone })
}

// 5. 刷新Token
export const refreshToken = (refreshToken) => {
  return post('/app/auth/refresh-token', { refreshToken })
}

// 6. 退出登录
export const logout = () => {
  return post('/app/auth/logout')
}
```

### 任务 1.3：完善请求封装

**文件**：`uni3/src/utils/request.js`

**需要添加**：
1. API基础地址配置（开发/生产环境）
2. Token自动刷新机制
3. 设备ID生成和携带
4. 项目ID自动添加到请求头

### 任务 1.4：创建权限工具

**新建文件**：`uni3/src/utils/permission.js`

```javascript
/**
 * 权限判断工具
 */
import { useUserStore } from '@/store/user'

// 是否员工
export const isStaff = () => {
  const userStore = useUserStore()
  return userStore.userType === 'staff'
}

// 是否客户
export const isCustomer = () => {
  const userStore = useUserStore()
  return userStore.userType === 'customer'
}

// 是否有页面权限
export const hasPagePermission = (pagePath) => {
  // 员工专属页面
  const staffOnlyPages = [
    '/pages/inspection/',
    '/pages/issue/',
    '/pages/repair/'
  ]
  
  if (isCustomer()) {
    return !staffOnlyPages.some(path => pagePath.startsWith(path))
  }
  
  return true
}
```

### 任务 1.5：创建设备ID工具

**新建文件**：`uni3/src/utils/device.js`

```javascript
/**
 * 设备ID生成和管理
 */

// 生成设备唯一标识
export const getDeviceId = () => {
  let deviceId = uni.getStorageSync('deviceId')
  
  if (!deviceId) {
    // 生成UUID
    deviceId = 'device_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    uni.setStorageSync('deviceId', deviceId)
  }
  
  return deviceId
}

// 获取设备信息
export const getDeviceInfo = () => {
  const systemInfo = uni.getSystemInfoSync()
  return {
    platform: systemInfo.platform,
    system: systemInfo.system,
    model: systemInfo.model,
    version: systemInfo.version
  }
}
```

### 任务 1.6：添加路由守卫

**文件**：`uni3/src/main.js`

```javascript
import { hasPagePermission } from '@/utils/permission'

// 路由拦截
uni.addInterceptor('navigateTo', {
  invoke(args) {
    // 检查权限
    if (!hasPagePermission(args.url)) {
      uni.showToast({
        title: '该功能仅员工可用',
        icon: 'none'
      })
      return false
    }
  }
})

uni.addInterceptor('switchTab', {
  invoke(args) {
    // 检查权限
    if (!hasPagePermission(args.url)) {
      uni.showToast({
        title: '该功能仅员工可用',
        icon: 'none'
      })
      return false
    }
  }
})
```

### 任务 1.7：更新登录页面（微信小程序特性）

**文件**：`uni3/src/pages/login/index.vue`

**需要修改**：
1. 移除项目编号输入框
2. 添加短信验证码登录表单
3. 添加密码登录表单
4. 添加登录方式切换
5. 对接真实API
6. 处理登录成功后的角色识别和跳转

**微信小程序特殊处理**：

#### 1. 微信登录流程（使用微信官方API）

```javascript
// 微信一键登录（获取手机号）
const handleWechatLogin = async (e) => {
  // 1. 用户点击按钮，触发 getPhoneNumber 回调
  if (e.detail.errMsg === 'getPhoneNumber:ok') {
    const code = e.detail.code  // 动态令牌
    
    // 2. 同时获取微信登录凭证
    const loginRes = await uni.login()
    const wxCode = loginRes.code
    
    // 3. 发送到后端
    const result = await wechatLogin({
      code: wxCode,           // 微信登录凭证
      phoneCode: code,        // 手机号动态令牌
      deviceId: getDeviceId()
    })
    
    // 4. 保存登录信息
    userStore.setLoginInfo(result)
    
    // 5. 跳转
    navigateAfterLogin(result.userType)
  }
}
```

#### 2. 按钮配置（微信小程序专用）

```vue
<!-- 微信一键登录按钮 -->
<button 
  class="glass-btn primary-btn" 
  open-type="getPhoneNumber"
  @getphonenumber="handleWechatLogin"
  v-if="isWechatMiniProgram"
>
  <SvgIcon name="brand-wechat" />
  微信一键登录
</button>
```

#### 3. 环境判断

```javascript
// 判断是否在微信小程序环境
const isWechatMiniProgram = ref(false)

onMounted(() => {
  // #ifdef MP-WEIXIN
  isWechatMiniProgram.value = true
  // #endif
})
```

#### 4. 短信验证码登录（倒计时）

```javascript
const smsForm = reactive({
  phone: '',
  code: ''
})

const countdown = ref(0)
const countdownTimer = ref(null)

// 发送验证码
const handleSendCode = async () => {
  if (!smsForm.phone || !/^1[3-9]\d{9}$/.test(smsForm.phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  
  if (countdown.value > 0) return
  
  try {
    await sendCode(smsForm.phone)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    
    // 开始倒计时
    countdown.value = 60
    countdownTimer.value = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer.value)
      }
    }, 1000)
  } catch (error) {
    uni.showToast({ title: error.message || '发送失败', icon: 'none' })
  }
}

// 短信登录
const handleSmsLogin = async () => {
  if (!smsForm.phone || !smsForm.code) {
    uni.showToast({ title: '请输入手机号和验证码', icon: 'none' })
    return
  }
  
  try {
    const result = await smsLogin({
      phone: smsForm.phone,
      code: smsForm.code,
      deviceId: getDeviceId()
    })
    
    userStore.setLoginInfo(result)
    navigateAfterLogin(result.userType)
  } catch (error) {
    uni.showToast({ title: error.message || '登录失败', icon: 'none' })
  }
}
```

#### 5. 密码登录

```javascript
const passwordForm = reactive({
  phone: '',
  password: ''
})

const handlePasswordLogin = async () => {
  if (!passwordForm.phone || !passwordForm.password) {
    uni.showToast({ title: '请输入手机号和密码', icon: 'none' })
    return
  }
  
  try {
    const result = await passwordLogin({
      phone: passwordForm.phone,
      password: passwordForm.password,
      deviceId: getDeviceId()
    })
    
    userStore.setLoginInfo(result)
    navigateAfterLogin(result.userType)
  } catch (error) {
    uni.showToast({ title: error.message || '登录失败', icon: 'none' })
  }
}
```

#### 6. 登录成功后跳转

```javascript
const navigateAfterLogin = (userType) => {
  // 根据角色跳转不同首页
  if (userType === 'customer') {
    uni.switchTab({ url: '/pages/dashboard/index' })
  } else if (userType === 'staff') {
    uni.switchTab({ url: '/pages/dashboard/index' })
  }
}
```

#### 7. 登录方式切换

```javascript
const loginMode = ref('wechat')  // 'wechat' | 'sms' | 'password'

const switchLoginMode = (mode) => {
  loginMode.value = mode
}
```

---

## 第二阶段：后端基础设施搭建（2天）

### 任务 2.1：数据库准备

**位置**：`sb3/sql/app_tables.sql`

创建5张新表：
- `app_login_logs` - 登录日志
- `app_tokens` - Token管理
- `app_sms_codes` - 短信验证码
- `app_wechat_bindings` - 微信绑定
- `app_audit_logs` - 审计日志

**位置**：`sb3/sql/app_dict_data.sql`

插入字典数据：
- `app_config` 字典类型和数据
- `issue_type` 字典类型和数据
- `severity_level` 字典类型和数据

### 任务 2.2：创建后端包结构

**位置**：`sb3/evs-home/src/main/java/com/ruoyi/app/`

```
com.ruoyi.app/
├── controller/
│   └── AppAuthController.java
├── dto/
│   ├── request/
│   │   ├── WechatLoginRequest.java
│   │   ├── SmsLoginRequest.java
│   │   ├── PasswordLoginRequest.java
│   │   └── SendCodeRequest.java
│   └── response/
│       ├── AppLoginResponse.java
│       └── AppUserInfo.java
├── service/
│   ├── IAppAuthService.java
│   ├── impl/
│   │   └── AppAuthServiceImpl.java
│   ├── ISmsService.java
│   ├── impl/
│   │   └── SmsServiceImpl.java
│   ├── IWechatService.java
│   └── impl/
│       └── WechatServiceImpl.java
├── mapper/
│   ├── AppLoginLogMapper.java
│   ├── AppTokenMapper.java
│   ├── AppSmsCodeMapper.java
│   ├── AppWechatBindingMapper.java
│   └── AppAuditLogMapper.java
├── domain/
│   ├── AppLoginLog.java
│   ├── AppToken.java
│   ├── AppSmsCode.java
│   ├── AppWechatBinding.java
│   └── AppAuditLog.java
├── security/
│   ├── AppTokenManager.java
│   ├── AppAuthInterceptor.java
│   ├── AppRoleInterceptor.java
│   └── AppContext.java
├── enums/
│   ├── UserTypeEnum.java
│   └── LoginTypeEnum.java
├── utils/
│   └── AppConfigUtil.java
└── annotation/
    └── RequireStaff.java
```

### 任务 2.3：实现核心工具类

#### 2.3.1 AppConfigUtil - 配置读取工具

```java
@Component
public class AppConfigUtil {
    @Autowired
    private ISysDictDataService dictDataService;
    
    // 获取Token有效期（小时）
    public int getTokenExpireHours() {
        return getIntValue("Token有效期（小时）", 2);
    }
    
    // 获取RefreshToken有效期（天）
    public int getRefreshTokenExpireDays() {
        return getIntValue("RefreshToken有效期（天）", 7);
    }
    
    // 获取验证码有效期（分钟）
    public int getSmsCodeExpireMinutes() {
        return getIntValue("验证码有效期（分钟）", 5);
    }
}
```

#### 2.3.2 AppTokenManager - JWT Token管理

```java
@Component
public class AppTokenManager {
    // 生成Access Token
    public String generateAccessToken(UserTypeEnum userType, String userId, 
                                     List<String> projectIds)
    
    // 生成Refresh Token
    public String generateRefreshToken(UserTypeEnum userType, String userId)
    
    // 验证Token
    public Claims validateToken(String token)
    
    // 刷新Token
    public TokenPair refreshToken(String refreshToken)
    
    // 撤销Token
    public void revokeToken(String token)
}
```

#### 2.3.3 SmsService - 短信服务

```java
@Service
public class SmsServiceImpl implements ISmsService {
    // 发送验证码
    public void sendCode(String phone)
    
    // 验证验证码
    public boolean verifyCode(String phone, String code)
}
```

#### 2.3.4 WechatService - 微信登录服务（对接微信官方API）

```java
@Service
public class WechatServiceImpl implements IWechatService {
    @Value("${wechat.miniapp.appid}")
    private String appId;
    
    @Value("${wechat.miniapp.secret}")
    private String appSecret;
    
    /**
     * 通过code换取openId和session_key
     * 调用微信接口：https://api.weixin.qq.com/sns/jscode2session
     */
    public WechatSession getSession(String code) {
        String url = String.format(
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            appId, appSecret, code
        );
        
        // 调用微信API
        String response = restTemplate.getForObject(url, String.class);
        WechatSession session = JSON.parseObject(response, WechatSession.class);
        
        if (session.getErrcode() != null && session.getErrcode() != 0) {
            throw new ServiceException("微信登录失败：" + session.getErrmsg());
        }
        
        return session;
    }
    
    /**
     * 通过phoneCode获取手机号
     * 调用微信接口：https://api.weixin.qq.com/wxa/business/getuserphonenumber
     */
    public String getPhoneNumber(String phoneCode) {
        // 1. 获取access_token
        String accessToken = getAccessToken();
        
        // 2. 调用获取手机号接口
        String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken;
        
        Map<String, String> params = new HashMap<>();
        params.put("code", phoneCode);
        
        String response = restTemplate.postForObject(url, params, String.class);
        WechatPhoneResponse phoneResponse = JSON.parseObject(response, WechatPhoneResponse.class);
        
        if (phoneResponse.getErrcode() != 0) {
            throw new ServiceException("获取手机号失败：" + phoneResponse.getErrmsg());
        }
        
        return phoneResponse.getPhone_info().getPurePhoneNumber();
    }
    
    /**
     * 获取access_token（需要缓存）
     */
    @Cacheable(value = "wechat:access_token", unless = "#result == null")
    public String getAccessToken() {
        String url = String.format(
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
            appId, appSecret
        );
        
        String response = restTemplate.getForObject(url, String.class);
        WechatAccessTokenResponse tokenResponse = JSON.parseObject(response, WechatAccessTokenResponse.class);
        
        if (tokenResponse.getErrcode() != null && tokenResponse.getErrcode() != 0) {
            throw new ServiceException("获取access_token失败：" + tokenResponse.getErrmsg());
        }
        
        return tokenResponse.getAccess_token();
    }
    
    /**
     * 查询或创建微信绑定
     */
    public AppWechatBinding getOrCreateBinding(String openId, String phone) {
        AppWechatBinding binding = appWechatBindingMapper.selectByOpenId(openId);
        
        if (binding == null) {
            // 创建新绑定
            binding = new AppWechatBinding();
            binding.setOpenId(openId);
            binding.setPhone(phone);
            binding.setBindTime(new Date());
            appWechatBindingMapper.insert(binding);
        }
        
        return binding;
    }
}

// 微信API响应对象
@Data
class WechatSession {
    private String openid;
    private String session_key;
    private String unionid;
    private Integer errcode;
    private String errmsg;
}

@Data
class WechatPhoneResponse {
    private Integer errcode;
    private String errmsg;
    private PhoneInfo phone_info;
    
    @Data
    static class PhoneInfo {
        private String phoneNumber;      // 带区号的手机号
        private String purePhoneNumber;  // 不带区号的手机号
        private String countryCode;      // 区号
    }
}

@Data
class WechatAccessTokenResponse {
    private String access_token;
    private Integer expires_in;
    private Integer errcode;
    private String errmsg;
}
```

**配置文件**：`application.yml`

```yaml
wechat:
  miniapp:
    appid: wx1234567890abcdef  # 小程序AppID
    secret: your_secret_here    # 小程序AppSecret
```

### 任务 2.4：实现认证拦截器

#### 2.4.1 AppAuthInterceptor - Token验证

```java
@Component
public class AppAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        // 1. 从Header获取Token
        // 2. 验证Token有效性
        // 3. 解析用户信息
        // 4. 存入AppContext
        // 5. 记录审计日志
    }
}
```

#### 2.4.2 AppRoleInterceptor - 角色权限拦截

```java
@Component
public class AppRoleInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        // 检查是否有@RequireStaff注解
        // 如果有，验证用户是否为员工
    }
}
```

#### 2.4.3 AppContext - 用户上下文

```java
public class AppContext {
    private static final ThreadLocal<AppUserContext> context = new ThreadLocal<>();
    
    public static void setContext(AppUserContext userContext)
    public static AppUserContext getContext()
    public static void clear()
    
    // 便捷方法
    public static String getUserId()
    public static UserTypeEnum getUserType()
    public static List<String> getProjectIds()
}
```

### 任务 2.5：实现AppAuthService

```java
@Service
public class AppAuthServiceImpl implements IAppAuthService {
    // 微信登录
    public AppLoginResponse wechatLogin(WechatLoginRequest request)
    
    // 短信验证码登录
    public AppLoginResponse smsLogin(SmsLoginRequest request)
    
    // 密码登录
    public AppLoginResponse passwordLogin(PasswordLoginRequest request)
    
    // 刷新Token
    public AppLoginResponse refreshToken(String refreshToken)
    
    // 退出登录
    public void logout()
    
    // 私有方法：查询用户（先查customers，再查sys_user）
    private UserInfo findUser(String phone)
    
    // 私有方法：查询用户的项目列表
    private List<ProjectInfo> findUserProjects(UserTypeEnum userType, String userId)
    
    // 私有方法：生成登录响应
    private AppLoginResponse buildLoginResponse(UserInfo user, List<ProjectInfo> projects)
}
```

### 任务 2.6：实现AppAuthController

```java
@RestController
@RequestMapping("/app/auth")
public class AppAuthController {
    @Autowired
    private IAppAuthService authService;
    
    @Autowired
    private ISmsService smsService;
    
    // 1. 微信登录
    @PostMapping("/wechat-login")
    public AjaxResult wechatLogin(@RequestBody WechatLoginRequest request)
    
    // 2. 短信验证码登录
    @PostMapping("/sms-login")
    public AjaxResult smsLogin(@RequestBody SmsLoginRequest request)
    
    // 3. 密码登录
    @PostMapping("/password-login")
    public AjaxResult passwordLogin(@RequestBody PasswordLoginRequest request)
    
    // 4. 发送验证码
    @PostMapping("/send-code")
    public AjaxResult sendCode(@RequestBody SendCodeRequest request)
    
    // 5. 刷新Token
    @PostMapping("/refresh-token")
    public AjaxResult refreshToken(@RequestBody RefreshTokenRequest request)
    
    // 6. 退出登录
    @PostMapping("/logout")
    public AjaxResult logout()
}
```

### 任务 2.7：配置Spring Security

**文件**：`SecurityConfig.java`

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/app/**").permitAll()  // 放行小程序接口
                .anyRequest().authenticated()
            )
            // ... 其他配置
    }
}
```

**文件**：`WebMvcConfig.java`

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private AppAuthInterceptor appAuthInterceptor;
    
    @Autowired
    private AppRoleInterceptor appRoleInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 小程序认证拦截器
        registry.addInterceptor(appAuthInterceptor)
                .addPathPatterns("/app/**")
                .excludePathPatterns("/app/auth/**");  // 排除登录接口
        
        // 角色权限拦截器
        registry.addInterceptor(appRoleInterceptor)
                .addPathPatterns("/app/**")
                .excludePathPatterns("/app/auth/**");
    }
}
```

---

## 第三阶段：前后端联调（1天）

### 任务 3.1：配置API地址

**文件**：`uni3/src/config/env.js`（新建）

```javascript
// 开发环境
const dev = {
  baseURL: 'http://localhost:8080'
}

// 生产环境
const prod = {
  baseURL: 'https://api.yourdomain.com'
}

export default process.env.NODE_ENV === 'development' ? dev : prod
```

### 任务 3.2：更新request.js

引入环境配置，使用真实的API地址。

### 任务 3.3：测试登录流程

1. **测试短信验证码登录**
   - 发送验证码
   - 输入验证码登录
   - 验证Token保存
   - 验证角色识别

2. **测试密码登录**
   - 输入手机号和密码
   - 验证登录成功
   - 验证角色识别

3. **测试微信登录**（需要微信小程序配置）
   - 获取微信授权
   - 获取手机号
   - 验证登录成功

4. **测试Token刷新**
   - Token过期后自动刷新
   - 验证刷新成功

5. **测试权限控制**
   - 客户访问员工专属页面被拦截
   - 员工可以访问所有页面

---

## 第四阶段：优化和完善（0.5天）

### 任务 4.1：用户体验优化

- 加载状态优化
- 错误提示优化
- 表单验证优化
- 登录成功动画

### 任务 4.2：安全性增强

- 密码加密传输
- 防止重复提交
- 验证码频率限制
- Token安全存储

---

## 微信小程序特殊注意事项

### 1. 微信登录流程说明

微信小程序的登录流程与普通Web应用不同，需要遵循微信官方的规范：

```
┌─────────────┐                    ┌─────────────┐                    ┌─────────────┐
│  小程序前端  │                    │  业务后端    │                    │  微信服务器  │
└─────────────┘                    └─────────────┘                    └─────────────┘
       │                                  │                                  │
       │ 1. wx.login()                    │                                  │
       │─────────────────────────────────>│                                  │
       │                                  │                                  │
       │ 2. 返回 code                     │                                  │
       │<─────────────────────────────────│                                  │
       │                                  │                                  │
       │ 3. 用户点击"获取手机号"按钮        │                                  │
       │    触发 getphonenumber 事件       │                                  │
       │    获得 phoneCode                │                                  │
       │                                  │                                  │
       │ 4. 发送 code + phoneCode         │                                  │
       │─────────────────────────────────>│                                  │
       │                                  │                                  │
       │                                  │ 5. code换取openid和session_key   │
       │                                  │─────────────────────────────────>│
       │                                  │                                  │
       │                                  │ 6. 返回openid和session_key       │
       │                                  │<─────────────────────────────────│
       │                                  │                                  │
       │                                  │ 7. phoneCode换取手机号            │
       │                                  │─────────────────────────────────>│
       │                                  │                                  │
       │                                  │ 8. 返回手机号                     │
       │                                  │<─────────────────────────────────│
       │                                  │                                  │
       │                                  │ 9. 查询用户（customers/sys_user）│
       │                                  │    生成Token                     │
       │                                  │                                  │
       │ 10. 返回Token和用户信息           │                                  │
       │<─────────────────────────────────│                                  │
       │                                  │                                  │
       │ 11. 保存Token，跳转首页           │                                  │
       │                                  │                                  │
```

### 2. 微信小程序API使用

#### 2.1 获取登录凭证

```javascript
// uni-app封装的API
uni.login({
  provider: 'weixin',
  success: (res) => {
    console.log('code:', res.code)
    // 将code发送到后端
  }
})
```

#### 2.2 获取手机号（需要用户授权）

```vue
<!-- 必须使用button组件，设置open-type="getPhoneNumber" -->
<button 
  open-type="getPhoneNumber" 
  @getphonenumber="handleGetPhoneNumber"
>
  获取手机号
</button>

<script>
const handleGetPhoneNumber = (e) => {
  if (e.detail.errMsg === 'getPhoneNumber:ok') {
    // e.detail.code 是动态令牌，需要发送到后端
    const phoneCode = e.detail.code
    // 发送到后端换取手机号
  } else {
    // 用户拒绝授权
    uni.showToast({ title: '需要授权手机号才能登录', icon: 'none' })
  }
}
</script>
```

#### 2.3 获取用户信息（头像、昵称）

```vue
<!-- 新版API：使用头像昵称填写组件 -->
<button open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
  选择头像
</button>

<script>
const onChooseAvatar = (e) => {
  const avatarUrl = e.detail.avatarUrl
  // 上传头像到服务器
}
</script>
```

### 3. 微信小程序权限说明

#### 3.1 需要在微信公众平台配置的权限

1. **服务器域名配置**
   - request合法域名：`https://your-api-domain.com`
   - uploadFile合法域名：`https://your-api-domain.com`
   - downloadFile合法域名：`https://your-api-domain.com`

2. **业务域名配置**（如果使用web-view）
   - 业务域名：`https://your-web-domain.com`

3. **获取手机号权限**
   - 需要小程序认证
   - 需要在"设置-接口设置"中开通"手机号快速验证组件"

#### 3.2 隐私协议配置

微信小程序要求必须配置隐私协议：

```json
// app.json 或 manifest.json
{
  "__usePrivacyCheck__": true,
  "permission": {
    "scope.userLocation": {
      "desc": "你的位置信息将用于小程序位置接口的效果展示"
    }
  }
}
```

### 4. 微信小程序登录最佳实践

#### 4.1 静默登录 + 手机号授权

```javascript
// 1. 页面加载时静默登录（获取openid）
onLoad() {
  this.silentLogin()
}

async silentLogin() {
  try {
    // 获取本地token
    const token = uni.getStorageSync('token')
    if (token) {
      // 验证token是否有效
      const valid = await this.validateToken(token)
      if (valid) {
        // token有效，直接进入
        return
      }
    }
    
    // token无效或不存在，获取微信code
    const loginRes = await uni.login()
    const wxCode = loginRes.code
    
    // 检查是否已绑定手机号
    const bindingRes = await this.checkBinding(wxCode)
    
    if (bindingRes.bound) {
      // 已绑定，直接登录
      this.autoLogin(bindingRes.token)
    } else {
      // 未绑定，需要用户授权手机号
      this.showPhoneAuth = true
    }
  } catch (error) {
    console.error('静默登录失败', error)
  }
}
```

#### 4.2 Token过期自动刷新

```javascript
// request.js 中的响应拦截器
const responseInterceptor = async (response) => {
  const { statusCode, data } = response
  
  if (statusCode === 401) {
    // Token过期，尝试刷新
    const refreshToken = uni.getStorageSync('refreshToken')
    
    if (refreshToken) {
      try {
        const newToken = await refreshAccessToken(refreshToken)
        uni.setStorageSync('token', newToken)
        
        // 重新发起原请求
        return request(response.config)
      } catch (error) {
        // 刷新失败，跳转登录
        uni.reLaunch({ url: '/pages/login/index' })
      }
    } else {
      // 没有refreshToken，跳转登录
      uni.reLaunch({ url: '/pages/login/index' })
    }
  }
  
  return response
}
```

### 5. 微信小程序调试技巧

#### 5.1 开发工具模拟

在微信开发者工具中：
- 可以模拟 `wx.login()` 返回测试code
- 可以模拟 `getPhoneNumber` 返回测试数据
- 需要在"详情-本地设置"中勾选"不校验合法域名"

#### 5.2 真机调试

- 使用"真机调试"功能测试真实的微信登录流程
- 测试手机号授权流程
- 测试网络请求

#### 5.3 体验版测试

- 上传体验版到微信服务器
- 添加体验成员
- 在真实环境中测试完整流程

### 6. 常见问题和解决方案

#### 6.1 获取手机号失败

**问题**：`getPhoneNumber` 返回 `fail`

**原因**：
- 小程序未认证
- 未开通"手机号快速验证组件"权限
- 用户拒绝授权

**解决**：
- 确保小程序已认证
- 在微信公众平台开通权限
- 提供友好的授权引导

#### 6.2 code换取openid失败

**问题**：调用 `jscode2session` 接口返回错误

**原因**：
- AppID或AppSecret配置错误
- code已使用过（code只能使用一次）
- code已过期（5分钟有效期）

**解决**：
- 检查配置
- 每次登录重新获取code
- 及时使用code

#### 6.3 request合法域名问题

**问题**：网络请求失败，提示"不在合法域名列表中"

**解决**：
- 在微信公众平台配置服务器域名
- 域名必须是https
- 开发时可以在开发者工具中关闭域名校验

---

## 开发顺序建议

### 第1天：前端基础（优先）

1. ✅ 完善 `store/user.js`
2. ✅ 创建 `api/auth.js`
3. ✅ 创建 `utils/permission.js`
4. ✅ 创建 `utils/device.js`
5. ✅ 更新 `utils/request.js`
6. ✅ 更新 `pages/login/index.vue`

### 第2-3天：后端实现

1. ✅ 创建数据库表和字典
2. ✅ 创建包结构和基础类
3. ✅ 实现工具类
4. ✅ 实现Service层
5. ✅ 实现Controller层
6. ✅ 配置拦截器

### 第4天：联调测试

1. ✅ 前后端联调
2. ✅ 功能测试
3. ✅ 优化完善

---

## 快速开始

我建议先从**前端基础设施**开始，因为：
1. 可以快速看到效果
2. 可以先用模拟数据测试
3. 后端可以并行开发

你想从哪个任务开始？我可以帮你生成具体的代码。
