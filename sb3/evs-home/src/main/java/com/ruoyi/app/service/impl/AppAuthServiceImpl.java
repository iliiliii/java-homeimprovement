package com.ruoyi.app.service.impl;

import com.ruoyi.app.dto.request.*;
import com.ruoyi.app.dto.response.*;
import com.ruoyi.app.enums.LoginTypeEnum;
import com.ruoyi.app.enums.UserTypeEnum;
import com.ruoyi.app.mapper.AppProjectMapper;
import com.ruoyi.app.mapper.AppUserMapper;
import com.ruoyi.app.security.AppTokenManager;
import com.ruoyi.app.service.IAppAuthService;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.web.domain.Customers;
import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.service.ICustomersService;
import com.ruoyi.web.service.IProjectMembersService;
import com.ruoyi.app.service.IWechatService;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 小程序认证服务实现
 */
@Service
public class AppAuthServiceImpl implements IAppAuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AppAuthServiceImpl.class);
    
    @Autowired
    private AppTokenManager tokenManager;
    
    @Autowired
    private ICustomersService customersService;
    
    @Autowired
    private IProjectMembersService projectMembersService;
    
    @Autowired
    private AppUserMapper appUserMapper;
    
    @Autowired
    private AppProjectMapper appProjectMapper;
    
    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private IWechatService wechatService;
    
    // 验证码有效期（分钟）
    private static final int SMS_CODE_EXPIRE_MINUTES = 5;
    
    // 验证码错误次数限制
    private static final int MAX_CODE_ERROR_COUNT = 5;
    
    // 账号锁定时间（分钟）
    private static final int ACCOUNT_LOCK_MINUTES = 30;
    
    // Redis Key前缀
    private static final String SMS_CODE_KEY = "app:sms:code:";
    private static final String CODE_ERROR_KEY = "app:sms:error:";
    private static final String ACCOUNT_LOCK_KEY = "app:account:lock:";
    private static final String TOKEN_BLACKLIST_KEY = "app:token:blacklist:";
    
    // 密码加密器
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public AppLoginResponse wechatLogin(WechatLoginRequest request, String ipAddress) {
        String code = request.getCode();
        String phoneCode = request.getPhoneCode();
        String deviceId = request.getDeviceId();
        
        // 1. 通过code换取openId和sessionKey
        IWechatService.WxSession wxSession = wechatService.code2Session(code);
        String openId = wxSession.getOpenId();
        
        // 2. 通过phoneCode获取手机号
        IWechatService.WxPhoneInfo phoneInfo = wechatService.getPhoneNumber(phoneCode);
        String phone = phoneInfo.getPurePhoneNumber();
        
        if (StringUtils.isEmpty(phone)) {
            throw new ServiceException("获取手机号失败，请重试");
        }
        
        // 3. 检查账号是否被锁定
        if (isAccountLocked(phone)) {
            throw new ServiceException("账号已被锁定，请" + ACCOUNT_LOCK_MINUTES + "分钟后再试");
        }
        
        // 4. 查询用户
        UserInfo userInfo = findUserByPhone(phone);
        if (userInfo == null) {
            throw new ServiceException("用户不存在，请联系管理员");
        }
        
        // 5. 保存/更新微信绑定信息（可选，用于后续直接通过openId登录）
        saveWechatBinding(openId, wxSession.getUnionId(), userInfo, phone);
        
        // 6. 查询用户的项目列表
        List<AppProjectInfo> projects = findUserProjects(userInfo.getUserType(), userInfo.getUserId());
        
        // 7. 生成Token
        List<String> projectIds = projects.stream()
                .map(AppProjectInfo::getId)
                .collect(Collectors.toList());
        
        String accessToken = tokenManager.generateAccessToken(
                userInfo.getUserType(),
                userInfo.getUserId(),
                phone,
                userInfo.getName(),
                projectIds,
                deviceId
        );
        
        String refreshToken = tokenManager.generateRefreshToken(
                userInfo.getUserType(),
                userInfo.getUserId(),
                deviceId
        );
        
        // 8. 记录登录日志
        logLogin(userInfo.getUserType(), userInfo.getUserId(), LoginTypeEnum.WECHAT, 
                ipAddress, deviceId, true, null);
        
        // 9. 构建响应
        return buildLoginResponse(accessToken, refreshToken, userInfo, projects);
    }
    
    /**
     * 保存微信绑定信息
     */
    private void saveWechatBinding(String openId, String unionId, UserInfo userInfo, String phone) {
        try {
            // 将绑定信息存储到Redis（简化实现，生产环境应存储到数据库）
            String bindingKey = "app:wechat:binding:" + openId;
            Map<String, String> binding = new HashMap<>();
            binding.put("openId", openId);
            binding.put("unionId", unionId != null ? unionId : "");
            binding.put("userType", userInfo.getUserType().getCode());
            binding.put("userId", userInfo.getUserId());
            binding.put("phone", phone);
            binding.put("bindTime", String.valueOf(System.currentTimeMillis()));
            
            redisCache.setCacheMap(bindingKey, binding);
            log.info("微信绑定信息已保存: openId={}, userId={}", openId, userInfo.getUserId());
        } catch (Exception e) {
            log.warn("保存微信绑定信息失败: {}", e.getMessage());
            // 不影响登录流程
        }
    }
    
    @Override
    public AppLoginResponse smsLogin(SmsLoginRequest request, String ipAddress) {
        String phone = request.getPhone();
        String code = request.getCode();
        String deviceId = request.getDeviceId();
        
        // 检查账号是否被锁定
        if (isAccountLocked(phone)) {
            throw new ServiceException("账号已被锁定，请" + ACCOUNT_LOCK_MINUTES + "分钟后再试");
        }
        
        // 查询用户（先查询，用于记录失败日志）
        UserInfo userInfo = findUserByPhone(phone);
        if (userInfo == null) {
            throw new ServiceException("用户不存在，请联系管理员");
        }
        
        // 验证验证码
        if (!verifySmsCode(phone, code)) {
            // 记录错误次数
            incrementCodeErrorCount(phone);
            logLogin(userInfo.getUserType(), userInfo.getUserId(), LoginTypeEnum.SMS, 
                    ipAddress, deviceId, false, "验证码错误");
            throw new ServiceException("验证码错误或已过期");
        }
        
        // 验证成功，清除错误计数
        clearCodeErrorCount(phone);
        
        // 查询用户的项目列表
        List<AppProjectInfo> projects = findUserProjects(userInfo.getUserType(), userInfo.getUserId());
        
        // 生成Token
        List<String> projectIds = projects.stream()
                .map(AppProjectInfo::getId)
                .collect(Collectors.toList());
        
        String accessToken = tokenManager.generateAccessToken(
                userInfo.getUserType(),
                userInfo.getUserId(),
                phone,
                userInfo.getName(),
                projectIds,
                deviceId
        );
        
        String refreshToken = tokenManager.generateRefreshToken(
                userInfo.getUserType(),
                userInfo.getUserId(),
                deviceId
        );
        
        // 记录登录日志
        logLogin(userInfo.getUserType(), userInfo.getUserId(), LoginTypeEnum.SMS, 
                ipAddress, deviceId, true, null);
        
        // 构建响应
        return buildLoginResponse(accessToken, refreshToken, userInfo, projects);
    }
    
    @Override
    public AppLoginResponse passwordLogin(PasswordLoginRequest request, String ipAddress) {
        String phone = request.getPhone();
        String password = request.getPassword();
        String deviceId = request.getDeviceId();
        
        // 查询用户
        UserInfo userInfo = findUserByPhone(phone);
        if (userInfo == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 验证密码（只有员工支持密码登录）
        if (userInfo.getUserType() != UserTypeEnum.STAFF) {
            throw new ServiceException("当前账户不支持密码登录");
        }
        
        // 验证员工密码（使用自定义Mapper绕过数据权限）
        SysUser sysUser = appUserMapper.selectUserById(Long.parseLong(userInfo.getUserId()));
        if (sysUser == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 验证密码（使用BCrypt加密方式）
        if (!matchesPassword(password, sysUser.getPassword())) {
            // 记录登录失败
            logLogin(userInfo.getUserType(), userInfo.getUserId(), LoginTypeEnum.PASSWORD, 
                    ipAddress, deviceId, false, "密码错误");
            throw new ServiceException("密码错误");
        }
        
        // 查询用户的项目列表
        List<AppProjectInfo> projects = findUserProjects(userInfo.getUserType(), userInfo.getUserId());
        
        // 生成Token
        List<String> projectIds = projects.stream()
                .map(AppProjectInfo::getId)
                .collect(Collectors.toList());
        
        String accessToken = tokenManager.generateAccessToken(
                userInfo.getUserType(),
                userInfo.getUserId(),
                phone,
                userInfo.getName(),
                projectIds,
                deviceId
        );
        
        String refreshToken = tokenManager.generateRefreshToken(
                userInfo.getUserType(),
                userInfo.getUserId(),
                deviceId
        );
        
        // 记录登录日志
        logLogin(userInfo.getUserType(), userInfo.getUserId(), LoginTypeEnum.PASSWORD, 
                ipAddress, deviceId, true, null);
        
        // 构建响应
        return buildLoginResponse(accessToken, refreshToken, userInfo, projects);
    }
    
    @Override
    public void sendCode(String phone, String ipAddress) {
        // 检查账号是否被锁定
        if (isAccountLocked(phone)) {
            throw new ServiceException("账号已被锁定，请" + ACCOUNT_LOCK_MINUTES + "分钟后再试");
        }
        
        // 检查发送频率（1分钟内只能发送一次）
        String codeKey = SMS_CODE_KEY + phone;
        String existingCode = redisCache.getCacheObject(codeKey);
        if (StringUtils.isNotEmpty(existingCode)) {
            Long ttl = redisCache.getExpire(codeKey);
            if (ttl != null && ttl > (SMS_CODE_EXPIRE_MINUTES - 1) * 60) {
                throw new ServiceException("发送太频繁，请稍后再试");
            }
        }
        
        // 验证手机号是否存在（防止短信轰炸）
        UserInfo userInfo = findUserByPhone(phone);
        if (userInfo == null) {
            throw new ServiceException("该手机号未注册，请联系管理员");
        }
        
        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 保存验证码到Redis
        redisCache.setCacheObject(codeKey, code, SMS_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // TODO: 调用短信服务发送验证码
        // 开发阶段直接打印验证码
        log.info("【开发模式】手机号: {}, 验证码: {}", phone, code);
        
        // 生产环境需要调用短信服务
        // smsService.send(phone, "您的验证码是：" + code + "，有效期5分钟。");
    }
    
    @Override
    public AppLoginResponse refreshToken(String refreshToken) {
        // 验证RefreshToken
        try {
            tokenManager.validateToken(refreshToken);
        } catch (Exception e) {
            throw new ServiceException("RefreshToken无效或已过期");
        }
        
        // 解析Token获取用户信息
        UserTypeEnum userType = tokenManager.getUserTypeFromToken(refreshToken);
        String userId = tokenManager.getUserIdFromToken(refreshToken);
        
        if (userType == null || userId == null) {
            throw new ServiceException("Token解析失败");
        }
        
        // 查询用户信息
        UserInfo userInfo = findUserById(userType, userId);
        if (userInfo == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 查询用户的项目列表
        List<AppProjectInfo> projects = findUserProjects(userType, userId);
        
        // 生成新Token
        List<String> projectIds = projects.stream()
                .map(AppProjectInfo::getId)
                .collect(Collectors.toList());
        
        String newAccessToken = tokenManager.generateAccessToken(
                userType,
                userId,
                userInfo.getPhone(),
                userInfo.getName(),
                projectIds,
                null
        );
        
        String newRefreshToken = tokenManager.generateRefreshToken(
                userType,
                userId,
                null
        );
        
        // 构建响应
        return buildLoginResponse(newAccessToken, newRefreshToken, userInfo, projects);
    }
    
    @Override
    public void logout(String token) {
        try {
            // 获取Token的过期时间
            long expireSeconds = tokenManager.getTokenRemainingSeconds(token);
            if (expireSeconds > 0) {
                // 将Token加入黑名单，过期时间与Token剩余有效期一致
                String blacklistKey = TOKEN_BLACKLIST_KEY + token;
                redisCache.setCacheObject(blacklistKey, "revoked", (int) expireSeconds, TimeUnit.SECONDS);
                log.info("Token已加入黑名单，剩余有效期: {}秒", expireSeconds);
            }
        } catch (Exception e) {
            log.warn("退出登录处理异常: {}", e.getMessage());
        }
    }
    
    @Override
    public boolean validateToken(String token) {
        try {
            // 检查Token是否在黑名单中
            String blacklistKey = TOKEN_BLACKLIST_KEY + token;
            if (redisCache.hasKey(blacklistKey)) {
                return false;
            }
            
            tokenManager.validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 根据手机号查询用户（先查客户，再查员工）
     */
    private UserInfo findUserByPhone(String phone) {
        // 1. 先查询客户表
        Customers customerQuery = new Customers();
        customerQuery.setPhone(phone);
        List<Customers> customers = customersService.selectCustomersList(customerQuery);
        
        if (customers != null && !customers.isEmpty()) {
            Customers customer = customers.get(0);
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(customer.getId());
            userInfo.setUserType(UserTypeEnum.CUSTOMER);
            userInfo.setName(customer.getName());
            userInfo.setPhone(customer.getPhone());
            userInfo.setAvatar(customer.getAvatar());
            return userInfo;
        }
        
        // 2. 再查询员工表（使用自定义Mapper绕过数据权限）
        SysUser user = appUserMapper.selectUserByPhone(phone);
        
        if (user != null) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(String.valueOf(user.getUserId()));
            userInfo.setUserType(UserTypeEnum.STAFF);
            userInfo.setName(user.getNickName());
            userInfo.setPhone(user.getPhonenumber());
            userInfo.setAvatar(user.getAvatar());
            return userInfo;
        }
        
        return null;
    }
    
    /**
     * 根据ID查询用户
     */
    private UserInfo findUserById(UserTypeEnum userType, String userId) {
        if (userType == UserTypeEnum.CUSTOMER) {
            Customers customer = customersService.selectCustomersById(userId);
            if (customer != null) {
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId(customer.getId());
                userInfo.setUserType(UserTypeEnum.CUSTOMER);
                userInfo.setName(customer.getName());
                userInfo.setPhone(customer.getPhone());
                userInfo.setAvatar(customer.getAvatar());
                return userInfo;
            }
        } else if (userType == UserTypeEnum.STAFF) {
            SysUser user = appUserMapper.selectUserById(Long.parseLong(userId));
            if (user != null) {
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId(String.valueOf(user.getUserId()));
                userInfo.setUserType(UserTypeEnum.STAFF);
                userInfo.setName(user.getNickName());
                userInfo.setPhone(user.getPhonenumber());
                userInfo.setAvatar(user.getAvatar());
                return userInfo;
            }
        }
        return null;
    }
    
    /**
     * 查询用户的项目列表（使用自定义Mapper绕过数据权限）
     */
    private List<AppProjectInfo> findUserProjects(UserTypeEnum userType, String userId) {
        List<Projects> projects;
        
        if (userType == UserTypeEnum.CUSTOMER) {
            // 客户：直接查询customer_id = userId的项目
            projects = appProjectMapper.selectProjectsByCustomerId(userId);
        } else {
            // 员工：查询project_members中user_id = userId的项目
            projects = projectMembersService.selectProjectsByUserId(Long.parseLong(userId));
        }
        
        if (projects == null || projects.isEmpty()) {
            return new ArrayList<>();
        }
        
        return projects.stream().map(p -> {
            AppProjectInfo info = new AppProjectInfo();
            info.setId(p.getId());
            info.setCode(p.getId()); // 使用ID作为code
            info.setName(p.getName());
            info.setStatus(p.getStatus());
            info.setPhase(p.getStatus()); // 使用status作为phase
            info.setAddress(p.getAddress());
            info.setArea(p.getArea() != null ? p.getArea().doubleValue() : null);
            return info;
        }).collect(Collectors.toList());
    }
    
    /**
     * 验证短信验证码（使用Redis）
     */
    private boolean verifySmsCode(String phone, String code) {
        String codeKey = SMS_CODE_KEY + phone;
        String cachedCode = redisCache.getCacheObject(codeKey);
        
        if (StringUtils.isEmpty(cachedCode)) {
            return false;
        }
        
        // 检查验证码是否正确
        if (!cachedCode.equals(code)) {
            return false;
        }
        
        // 验证成功，删除验证码
        redisCache.deleteObject(codeKey);
        return true;
    }
    
    /**
     * 检查账号是否被锁定
     */
    private boolean isAccountLocked(String phone) {
        String lockKey = ACCOUNT_LOCK_KEY + phone;
        return redisCache.hasKey(lockKey);
    }
    
    /**
     * 增加验证码错误次数
     */
    private void incrementCodeErrorCount(String phone) {
        String errorKey = CODE_ERROR_KEY + phone;
        Integer count = redisCache.getCacheObject(errorKey);
        if (count == null) {
            count = 0;
        }
        count++;
        
        if (count >= MAX_CODE_ERROR_COUNT) {
            // 锁定账号
            String lockKey = ACCOUNT_LOCK_KEY + phone;
            redisCache.setCacheObject(lockKey, "locked", ACCOUNT_LOCK_MINUTES, TimeUnit.MINUTES);
            redisCache.deleteObject(errorKey);
            log.warn("账号 {} 因验证码错误次数过多被锁定", phone);
        } else {
            // 更新错误次数，30分钟后自动清除
            redisCache.setCacheObject(errorKey, count, ACCOUNT_LOCK_MINUTES, TimeUnit.MINUTES);
        }
    }
    
    /**
     * 清除验证码错误计数
     */
    private void clearCodeErrorCount(String phone) {
        String errorKey = CODE_ERROR_KEY + phone;
        redisCache.deleteObject(errorKey);
    }
    
    /**
     * 验证密码
     */
    private boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (StringUtils.isEmpty(rawPassword) || StringUtils.isEmpty(encodedPassword)) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * 构建登录响应
     */
    private AppLoginResponse buildLoginResponse(String accessToken, String refreshToken, 
                                                UserInfo userInfo, List<AppProjectInfo> projects) {
        AppLoginResponse response = new AppLoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(tokenManager.getAccessTokenExpireSeconds());
        response.setUserType(userInfo.getUserType().getCode());
        
        AppUserInfo appUserInfo = new AppUserInfo();
        appUserInfo.setId(userInfo.getUserId());
        appUserInfo.setName(userInfo.getName());
        appUserInfo.setPhone(AppUserInfo.maskPhone(userInfo.getPhone()));
        appUserInfo.setAvatar(userInfo.getAvatar());
        response.setUserInfo(appUserInfo);
        
        response.setProjects(projects);
        
        return response;
    }
    
    /**
     * 记录登录日志
     */
    private void logLogin(UserTypeEnum userType, String userId, LoginTypeEnum loginType,
                         String ipAddress, String deviceId, boolean success, String failReason) {
        // TODO: 保存到数据库
        log.info("登录日志 - 用户类型: {}, 用户ID: {}, 登录方式: {}, IP: {}, 设备: {}, 成功: {}", 
                userType.getCode(), userId, loginType.getCode(), ipAddress, deviceId, success);
    }
    
    /**
     * 用户信息内部类
     */
    private static class UserInfo {
        private String userId;
        private UserTypeEnum userType;
        private String name;
        private String phone;
        private String avatar;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public UserTypeEnum getUserType() { return userType; }
        public void setUserType(UserTypeEnum userType) { this.userType = userType; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }
}
