package com.ruoyi.app.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * openid登录请求
 */
public class OpenidLoginRequest {
    
    /** 微信openid */
    @NotBlank(message = "微信openid不能为空")
    private String openid;
    
    /** 设备唯一标识 */
    private String deviceId;
    
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
}