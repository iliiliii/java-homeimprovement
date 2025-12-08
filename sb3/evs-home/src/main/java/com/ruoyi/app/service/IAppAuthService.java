package com.ruoyi.app.service;

import com.ruoyi.app.dto.request.*;
import com.ruoyi.app.dto.response.AppLoginResponse;

/**
 * 小程序认证服务接口
 */
public interface IAppAuthService {
    
    /**
     * 微信登录
     */
    AppLoginResponse wechatLogin(WechatLoginRequest request, String ipAddress);
    
    /**
     * 短信验证码登录
     */
    AppLoginResponse smsLogin(SmsLoginRequest request, String ipAddress);
    
    /**
     * 密码登录
     */
    AppLoginResponse passwordLogin(PasswordLoginRequest request, String ipAddress);
    
    /**
     * 发送验证码
     */
    void sendCode(String phone, String ipAddress);
    
    /**
     * 刷新Token
     */
    AppLoginResponse refreshToken(String refreshToken);
    
    /**
     * 退出登录
     */
    void logout(String token);
    
    /**
     * 验证Token
     */
    boolean validateToken(String token);
}
