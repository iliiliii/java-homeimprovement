package com.ruoyi.app.service.impl;

import com.ruoyi.app.dto.request.*;
import com.ruoyi.app.dto.response.*;
import com.ruoyi.app.enums.LoginTypeEnum;
import com.ruoyi.app.enums.UserTypeEnum;
import com.ruoyi.app.security.AppTokenManager;
import com.ruoyi.app.service.IAppAuthService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.web.domain.Customers;
import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.service.ICustomersService;
import com.ruoyi.web.service.IProjectsService;
import com.ruoyi.web.service.IProjectMembersService;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private IProjectsService projectsService;
    
    @Autowired
    private IProjectMembersService projectMembersService;
    
    @Autowired
    private ISysUserService sysUserService;
    
    // 简单的验证码存储（生产环境应使用Redis）
    private static final Map<String, SmsCodeInfo> smsCodeCache = new ConcurrentHashMap<>();
    
    // 验证码有效期（分钟）
    private static final int SMS_CODE_EXPIRE_MINUTES = 5;
    
    @Override
    public AppLoginResponse wechatLogin(WechatLoginRequest request, String ipAddress) {
        // TODO: 实现微信登录
        // 1. 通过code换取openId
        // 2. 通过phoneCode获取手机号
        // 3. 查询用户
        // 4. 生成Token
        
        // 暂时返回模拟数据，等微信配置完成后再实现
        throw new ServiceException("微信登录功能正在开发中，请使用短信验证码登录");
    }
    
    @Override
    public AppLoginResponse smsLogin(SmsLoginRequest request, String ipAddress) {
        String phone = request.getPhone();
        String code = request.getCode();
        String deviceId = request.getDeviceId();
        
        // 验证验证码
        if (!verifySmsCode(phone, code)) {
            throw new ServiceException("验证码错误或已过期");
        }
        
        // 查询用户
        UserInfo userInfo = findUserByPhone(phone);
        if (userInfo == null) {
            throw new ServiceException("用户不存在，请联系管理员");
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
            throw new ServiceException("客户请使用短信验证码登录");
        }
        
        // 验证员工密码
        SysUser sysUser = sysUserService.selectUserById(Long.parseLong(userInfo.getUserId()));
        if (sysUser == null) {
            throw new ServiceException("用户不存在");
        }
        
        // TODO: 验证密码（需要使用若依的密码加密方式）
        // 暂时跳过密码验证，生产环境需要实现
        // if (!SecurityUtils.matchesPassword(password, sysUser.getPassword())) {
        //     throw new ServiceException("密码错误");
        // }
        
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
        // 检查发送频率（1分钟内只能发送一次）
        SmsCodeInfo existingCode = smsCodeCache.get(phone);
        if (existingCode != null) {
            long elapsed = System.currentTimeMillis() - existingCode.getCreateTime();
            if (elapsed < 60 * 1000) {
                throw new ServiceException("发送太频繁，请稍后再试");
            }
        }
        
        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 保存验证码
        SmsCodeInfo codeInfo = new SmsCodeInfo();
        codeInfo.setCode(code);
        codeInfo.setCreateTime(System.currentTimeMillis());
        codeInfo.setExpireTime(System.currentTimeMillis() + SMS_CODE_EXPIRE_MINUTES * 60 * 1000);
        smsCodeCache.put(phone, codeInfo);
        
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
        // TODO: 将Token加入黑名单（需要Redis支持）
        log.info("用户退出登录，Token: {}", token.substring(0, Math.min(20, token.length())) + "...");
    }
    
    @Override
    public boolean validateToken(String token) {
        try {
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
        
        // 2. 再查询员工表
        SysUser userQuery = new SysUser();
        userQuery.setPhonenumber(phone);
        List<SysUser> users = sysUserService.selectUserList(userQuery);
        
        if (users != null && !users.isEmpty()) {
            SysUser user = users.get(0);
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
            SysUser user = sysUserService.selectUserById(Long.parseLong(userId));
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
     * 查询用户的项目列表
     */
    private List<AppProjectInfo> findUserProjects(UserTypeEnum userType, String userId) {
        List<Projects> projects;
        
        if (userType == UserTypeEnum.CUSTOMER) {
            // 客户：查询customer_id = userId的项目
            Projects query = new Projects();
            query.setCustomerId(userId);
            projects = projectsService.selectProjectsList(query);
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
     * 验证短信验证码
     */
    private boolean verifySmsCode(String phone, String code) {
        SmsCodeInfo codeInfo = smsCodeCache.get(phone);
        if (codeInfo == null) {
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > codeInfo.getExpireTime()) {
            smsCodeCache.remove(phone);
            return false;
        }
        
        // 检查验证码是否正确
        if (!codeInfo.getCode().equals(code)) {
            return false;
        }
        
        // 验证成功，删除验证码
        smsCodeCache.remove(phone);
        return true;
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
    
    /**
     * 验证码信息内部类
     */
    private static class SmsCodeInfo {
        private String code;
        private long createTime;
        private long expireTime;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public long getCreateTime() { return createTime; }
        public void setCreateTime(long createTime) { this.createTime = createTime; }
        public long getExpireTime() { return expireTime; }
        public void setExpireTime(long expireTime) { this.expireTime = expireTime; }
    }
}
