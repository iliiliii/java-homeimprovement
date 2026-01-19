package com.ruoyi.app.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 解除微信绑定请求
 */
public class UnbindWechatRequest {
    
    /** 微信openid */
    @NotBlank(message = "openid不能为空")
    private String openid;
    
    /** 设备ID */
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;
    
    /** 验证方式：phone(手机号验证) 或 password(密码验证) */
    private String verifyType;
    
    /** 手机号（手机号验证时必填） */
    private String phone;
    
    /** 验证码（手机号验证时必填） */
    private String code;
    
    /** 密码（密码验证时必填） */
    private String password;
    
    public String getOpenid() {
        return openid;
    }
    
    public void setOpenid(String openid) {
        this.openid = openid;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getVerifyType() {
        return verifyType;
    }
    
    public void setVerifyType(String verifyType) {
        this.verifyType = verifyType;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}