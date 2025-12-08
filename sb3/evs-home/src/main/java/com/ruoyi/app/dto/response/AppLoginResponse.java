package com.ruoyi.app.dto.response;

import java.util.List;

/**
 * 登录响应
 */
public class AppLoginResponse {
    
    /** Access Token */
    private String accessToken;
    
    /** Refresh Token */
    private String refreshToken;
    
    /** Token过期时间（秒） */
    private Long expiresIn;
    
    /** 用户类型：customer/staff */
    private String userType;
    
    /** 用户信息 */
    private AppUserInfo userInfo;
    
    /** 项目列表 */
    private List<AppProjectInfo> projects;
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    public AppUserInfo getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(AppUserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    public List<AppProjectInfo> getProjects() {
        return projects;
    }
    
    public void setProjects(List<AppProjectInfo> projects) {
        this.projects = projects;
    }
}
