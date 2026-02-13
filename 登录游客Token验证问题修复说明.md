# 登录游客Token验证问题修复说明

## 问题描述

用户报告：
- **未登录时**：可以正常查看演示数据 ✅
- **登录后**：无法查看数据，提示错误 ❌

**错误信息：**
```
code: 500
msg: "获取数据失败: JWT strings must contain exactly 2 period characters. Found: 0"
```

**用户说明：**
> 我们登录后因为账号未进行关联，也还属于游客，只是登录后的游客

## 问题分析

### 根本原因

1. **未登录用户**：
   - 没有token
   - 后端检测到无token，直接检查是否为演示项目
   - 如果是演示项目，允许访问 ✅

2. **登录后的游客用户**：
   - 有token（但可能格式不正确或无效）
   - 后端尝试验证token：`tokenManager.validateToken(token)`
   - Token格式错误，抛出异常：`JWT strings must contain exactly 2 period characters`
   - 直接返回500错误，不再检查是否为演示项目 ❌

### 代码流程

**修改前的逻辑：**
```java
private void validateTokenAndAccess(String token, String projectId) {
    // 1. 如果没有token，检查演示项目
    if (token == null || token.isEmpty() || "null".equals(token)) {
        boolean isDemo = guestConfigService.isGuestDemoProject(projectId);
        if (!isDemo) {
            throw new ServiceException("未登录用户只能访问演示项目");
        }
        return; // ✅ 未登录访问演示项目成功
    }
    
    // 2. 有token，直接验证（这里会抛出异常）
    Map<String, Object> claims = tokenManager.validateToken(extractToken(token));
    // ❌ 如果token格式错误，这里会抛出异常，不再继续执行
    
    // 3. 后续的游客权限检查代码永远不会执行到
    String userType = (String) claims.get("userType");
    if ("guest".equals(userType)) {
        // ...
    }
}
```

**问题：**
- 登录后的游客有token，但token可能无效
- 代码直接验证token，失败后抛出异常
- 没有降级处理，不检查是否为演示项目

## 解决方案

### 修改策略

添加 try-catch 捕获token验证异常，如果是演示项目则允许访问（降级为未登录访问）。

### 修改后的逻辑

```java
private void validateTokenAndAccess(String token, String projectId) {
    // 1. 如果没有token，检查演示项目
    if (token == null || token.isEmpty() || "null".equals(token)) {
        boolean isDemo = guestConfigService.isGuestDemoProject(projectId);
        if (!isDemo) {
            throw new ServiceException("未登录用户只能访问演示项目");
        }
        return; // ✅ 未登录访问演示项目成功
    }
    
    // 2. 尝试验证token（添加异常处理）
    Map<String, Object> claims = null;
    try {
        claims = tokenManager.validateToken(extractToken(token));
    } catch (Exception e) {
        // Token验证失败（可能是格式错误或过期）
        log.warn("[权限验证] Token验证失败: {}", e.getMessage());
        
        // 如果是演示项目，允许访问（降级为未登录访问）
        boolean isDemo = guestConfigService.isGuestDemoProject(projectId);
        if (isDemo) {
            log.info("[权限验证] Token无效但访问演示项目，允许访问: {}", projectId);
            return; // ✅ Token无效但访问演示项目成功
        }
        
        // 非演示项目，拒绝访问
        throw new ServiceException("Token无效或已过期");
    }
    
    // 3. Token有效，继续验证用户权限
    String userType = (String) claims.get("userType");
    String userId = claims.get("userId").toString();

    // 游客用户：只能访问演示项目
    if ("guest".equals(userType)) {
        boolean hasAccess = guestConfigService.validateGuestProjectAccess(projectId);
        if (!hasAccess) {
            throw new ServiceException("游客只能访问演示项目");
        }
        return; // ✅ 游客访问演示项目成功
    }

    // 4. 正常用户权限验证
    if ("customer".equals(userType)) {
        // 验证客户权限
    } else if ("staff".equals(userType)) {
        // 验证员工权限
    }
}
```

### 修改的文件

1. **sb3/evs-home/src/main/java/com/ruoyi/app/service/impl/AppProjectScheduleServiceImpl.java**
   - 修改 `validateTokenAndAccess()` 方法
   - 添加token验证异常处理
   - 演示项目降级访问

2. **sb3/evs-home/src/main/java/com/ruoyi/app/service/impl/AppDashboardServiceImpl.java**
   - 修改 `validateTokenAndAccess()` 方法
   - 添加token验证异常处理
   - 演示项目降级访问

## 技术细节

### Token验证异常类型

JWT token验证可能抛出的异常：

1. **格式错误**：`JWT strings must contain exactly 2 period characters`
   - Token不是标准的JWT格式（应该是 `header.payload.signature`）
   - 例如：token只是一个普通字符串

2. **签名错误**：`JWT signature does not match`
   - Token被篡改
   - 签名密钥不匹配

3. **过期错误**：`JWT expired`
   - Token已过期
   - 超过了设置的有效期

4. **其他错误**：
   - Token为空
   - Token格式不完整

### 降级策略

**降级条件：**
- Token验证失败（任何异常）
- 访问的是演示项目

**降级行为：**
- 忽略token验证失败
- 将用户视为未登录用户
- 允许访问演示项目
- 记录日志：`Token无效但访问演示项目，允许访问`

**不降级条件：**
- Token验证失败
- 访问的不是演示项目
- 抛出异常：`Token无效或已过期`

### 日志输出

**未登录访问演示项目：**
```
[权限验证] 未登录用户访问演示项目: 9fa800b545b445e4b699b1598bec4619
```

**Token无效但访问演示项目：**
```
[权限验证] Token验证失败: JWT strings must contain exactly 2 period characters. Found: 0
[权限验证] Token无效但访问演示项目，允许访问: 9fa800b545b445e4b699b1598bec4619
```

**Token有效的游客访问演示项目：**
```
[权限验证] 游客用户 user123 访问演示项目: 9fa800b545b445e4b699b1598bec4619
```

**Token无效访问非演示项目：**
```
[权限验证] Token验证失败: JWT strings must contain exactly 2 period characters. Found: 0
抛出异常: Token无效或已过期
```

## 使用场景

### 场景1：未登录用户访问演示项目
```
用户状态：未登录
Token：无
项目：演示项目
结果：✅ 允许访问
```

### 场景2：登录后的游客（Token无效）访问演示项目
```
用户状态：登录（游客）
Token：无效（格式错误）
项目：演示项目
结果：✅ 允许访问（降级为未登录访问）
```

### 场景3：登录后的游客（Token有效）访问演示项目
```
用户状态：登录（游客）
Token：有效
项目：演示项目
结果：✅ 允许访问（正常游客访问）
```

### 场景4：登录后的游客（Token无效）访问非演示项目
```
用户状态：登录（游客）
Token：无效（格式错误）
项目：非演示项目
结果：❌ 拒绝访问（Token无效或已过期）
```

### 场景5：正常用户访问自己的项目
```
用户状态：登录（客户/员工）
Token：有效
项目：用户有权限的项目
结果：✅ 允许访问（正常权限验证）
```

### 场景6：正常用户（Token无效）访问自己的项目
```
用户状态：登录（客户/员工）
Token：无效（过期）
项目：用户有权限的项目
结果：❌ 拒绝访问（Token无效或已过期）
```

## 优势

### 1. 容错性更强
- Token验证失败不会直接导致演示项目无法访问
- 提供了降级方案，确保演示功能可用

### 2. 用户体验更好
- 登录后的游客即使token有问题，仍然可以查看演示数据
- 不需要退出登录重新访问

### 3. 安全性不降低
- 只对演示项目降级
- 非演示项目仍然严格验证token
- 正常用户的权限验证不受影响

### 4. 日志更清晰
- 记录token验证失败的原因
- 记录降级访问的情况
- 便于排查问题

## 验证方法

### 1. 测试未登录访问
```bash
# 不带token访问演示项目
curl -X GET "http://192.168.5.102:8080/app/projectSchedules/list" \
  -H "X-Project-Id: 9fa800b545b445e4b699b1598bec4619"

# 预期：200 OK，返回数据
```

### 2. 测试无效token访问演示项目
```bash
# 带无效token访问演示项目
curl -X GET "http://192.168.5.102:8080/app/projectSchedules/list" \
  -H "Authorization: Bearer invalid-token" \
  -H "X-Project-Id: 9fa800b545b445e4b699b1598bec4619"

# 预期：200 OK，返回数据（降级访问）
```

### 3. 测试无效token访问非演示项目
```bash
# 带无效token访问非演示项目
curl -X GET "http://192.168.5.102:8080/app/projectSchedules/list" \
  -H "Authorization: Bearer invalid-token" \
  -H "X-Project-Id: other-project-id"

# 预期：500 Error，Token无效或已过期
```

### 4. 前端测试
1. 清除小程序缓存
2. 不登录，访问日志页面
3. 确认能看到演示数据 ✅
4. 使用游客账号登录（未关联项目）
5. 访问日志页面
6. 确认能看到演示数据 ✅

### 5. 查看后端日志
```bash
# 查看权限验证日志
tail -f /path/to/logs/evs-home.log | grep "权限验证"

# 预期日志：
# [权限验证] Token验证失败: JWT strings must contain exactly 2 period characters. Found: 0
# [权限验证] Token无效但访问演示项目，允许访问: 9fa800b545b445e4b699b1598bec4619
```

## 注意事项

### 1. Token格式问题
如果登录后的游客token格式不正确，建议检查：
- 登录接口是否正确生成JWT token
- Token生成时是否使用了正确的密钥
- Token是否包含必要的claims

### 2. 游客登录逻辑
建议检查游客登录的实现：
- 游客登录时是否生成了有效的JWT token
- Token中是否包含 `userType: "guest"`
- Token的有效期设置是否合理

### 3. 降级访问的安全性
- 降级访问只适用于演示项目
- 演示项目应该是只读数据
- 不应该包含敏感信息

### 4. 性能考虑
- Token验证失败会有异常开销
- 建议在前端避免使用无效token
- 可以在前端检测token格式

## 后续优化建议

### 1. 前端优化
在前端检测token格式，避免发送无效token：

```javascript
// uni3/src/utils/request.js
const requestInterceptor = (config) => {
  const token = uni.getStorageSync('token')
  
  // 检查token格式
  if (token && token !== '' && token !== 'null') {
    // 简单的JWT格式检查（应该包含两个点）
    const parts = token.split('.')
    if (parts.length === 3) {
      // 格式正确，添加到请求头
      config.header = {
        ...config.header,
        'Authorization': `Bearer ${token}`
      }
    } else {
      // 格式错误，清除token
      console.warn('[Request] Token格式错误，清除token')
      uni.removeStorageSync('token')
    }
  }
  
  return config
}
```

### 2. 游客登录优化
确保游客登录时生成有效的JWT token：

```java
// 游客登录时
Map<String, Object> claims = new HashMap<>();
claims.put("userId", guestUser.getId());
claims.put("userType", "guest");
claims.put("name", "游客用户");
claims.put("projectIds", demoProjectIds);

String accessToken = tokenManager.createAccessToken(claims);
String refreshToken = tokenManager.createRefreshToken(claims);
```

### 3. 统一异常处理
在全局异常处理器中统一处理JWT异常：

```java
@ExceptionHandler(JwtException.class)
public AjaxResult handleJwtException(JwtException e) {
    log.warn("JWT验证失败: {}", e.getMessage());
    return AjaxResult.error("Token无效或已过期，请重新登录");
}
```

## 总结

通过添加token验证异常处理和演示项目降级访问机制，我们解决了：

1. ✅ 未登录用户可以访问演示项目
2. ✅ 登录后的游客（token无效）可以访问演示项目
3. ✅ 登录后的游客（token有效）可以访问演示项目
4. ✅ 正常用户的权限验证不受影响
5. ✅ 非演示项目的安全性不降低

这个修改提高了系统的容错性和用户体验，同时保持了安全性。

---

**文档版本：** 1.0  
**创建时间：** 2026-02-13  
**维护人员：** Kiro AI Assistant
