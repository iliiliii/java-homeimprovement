package com.ruoyi.app.dto.response;

/**
 * openid绑定状态检查响应
 */
public class OpenidBindingCheckResponse {
    
    /** 是否已绑定 */
    private Boolean isBound;
    
    /** 微信openid */
    private String openid;
    
    /** 用户ID（已绑定时返回） */
    private String userId;
    
    /** 用户类型（已绑定时返回） */
    private String userType;
    
    /** 绑定的手机号（已绑定时返回，脱敏显示） */
    private String phone;
    
    public Boolean getIsBound() {
        return isBound;
    }
    
    public void setIsBound(Boolean isBound) {
        this.isBound = isBound;
    }
    
    public String getOpenid() {
        return openid;
    }
    
    public void setOpenid(String openid) {
        this.openid = openid;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
}