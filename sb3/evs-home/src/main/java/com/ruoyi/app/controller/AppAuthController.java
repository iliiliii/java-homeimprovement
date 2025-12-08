package com.ruoyi.app.controller;

import com.ruoyi.app.dto.request.*;
import com.ruoyi.app.dto.response.AppLoginResponse;
import com.ruoyi.app.service.IAppAuthService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 小程序认证控制器
 */
@RestController
@RequestMapping("/app/auth")
public class AppAuthController {
    
    @Autowired
    private IAppAuthService authService;
    
    /**
     * 微信登录
     */
    @PostMapping("/wechat-login")
    public AjaxResult wechatLogin(@Validated @RequestBody WechatLoginRequest request) {
        String ipAddress = IpUtils.getIpAddr(ServletUtils.getRequest());
        AppLoginResponse response = authService.wechatLogin(request, ipAddress);
        return AjaxResult.success(response);
    }
    
    /**
     * 短信验证码登录
     */
    @PostMapping("/sms-login")
    public AjaxResult smsLogin(@Validated @RequestBody SmsLoginRequest request) {
        String ipAddress = IpUtils.getIpAddr(ServletUtils.getRequest());
        AppLoginResponse response = authService.smsLogin(request, ipAddress);
        return AjaxResult.success(response);
    }
    
    /**
     * 密码登录
     */
    @PostMapping("/password-login")
    public AjaxResult passwordLogin(@Validated @RequestBody PasswordLoginRequest request) {
        String ipAddress = IpUtils.getIpAddr(ServletUtils.getRequest());
        AppLoginResponse response = authService.passwordLogin(request, ipAddress);
        return AjaxResult.success(response);
    }
    
    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public AjaxResult sendCode(@Validated @RequestBody SendCodeRequest request) {
        String ipAddress = IpUtils.getIpAddr(ServletUtils.getRequest());
        authService.sendCode(request.getPhone(), ipAddress);
        return AjaxResult.success("验证码已发送");
    }
    
    /**
     * 刷新Token
     */
    @PostMapping("/refresh-token")
    public AjaxResult refreshToken(@Validated @RequestBody RefreshTokenRequest request) {
        AppLoginResponse response = authService.refreshToken(request.getRefreshToken());
        return AjaxResult.success(response);
    }
    
    /**
     * 验证Token
     */
    @PostMapping("/validate-token")
    public AjaxResult validateToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            return AjaxResult.error("Token不存在");
        }
        
        boolean valid = authService.validateToken(token);
        if (valid) {
            return AjaxResult.success("Token有效");
        } else {
            return AjaxResult.error("Token无效或已过期");
        }
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public AjaxResult logout(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null) {
            authService.logout(token);
        }
        return AjaxResult.success("退出成功");
    }
    
    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
