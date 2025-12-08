package com.ruoyi.app.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 微信登录请求
 */
public class WechatLoginRequest {
    
    /** 微信登录凭证 */
    @NotBlank(message = "微信登录凭证不能为空")
    private String code;
    
    /** 手机号动态令牌 */
    @NotBlank(message = "手机号动态令牌不能为空")
    private String phoneCode;
    
    /** 设备唯一标识 */
    private String deviceId;
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getPhoneCode() {
        return phoneCode;
    }
    
    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
