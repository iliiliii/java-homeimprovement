package com.ruoyi.app.service;

import com.ruoyi.app.dto.request.*;
import com.ruoyi.app.dto.response.AppLoginResponse;
import com.ruoyi.app.dto.response.OpenidBindingCheckResponse;

/**
 * 小程序认证服务接口
 */
public interface IAppAuthService {
    
    /**
     * 检查openid绑定状态
     */
    OpenidBindingCheckResponse checkOpenidBinding(CheckOpenidBindingRequest request, String ipAddress);
    
    /**
     * openid直接登录（已绑定用户）
     */
    AppLoginResponse openidLogin(OpenidLoginRequest request, String ipAddress);
    
    /**
     * 绑定手机号到openid
     */
    AppLoginResponse bindPhoneToOpenid(BindPhoneToOpenidRequest request, String ipAddress);
    
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
    
    /**
     * 解除微信绑定
     */
    void unbindWechat(UnbindWechatRequest request, String ipAddress);
}
