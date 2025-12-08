package com.ruoyi.app.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新Token请求
 */
public class RefreshTokenRequest {
    
    /** RefreshToken */
    @NotBlank(message = "RefreshToken不能为空")
    private String refreshToken;
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
