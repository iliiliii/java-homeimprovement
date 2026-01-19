package com.ruoyi.app.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 检查openid绑定状态请求
 */
public class CheckOpenidBindingRequest {
    
    /** 微信登录凭证 */
    @NotBlank(message = "微信登录凭证不能为空")
    private String code;
    
    /** 设备唯一标识 */
    private String deviceId;
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}