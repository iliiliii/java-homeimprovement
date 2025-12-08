package com.ruoyi.app.security;

import com.ruoyi.app.enums.UserTypeEnum;
import com.ruoyi.common.utils.uuid.UUID;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小程序Token管理器
 */
@Component
public class AppTokenManager {
    
    private static final Logger log = LoggerFactory.getLogger(AppTokenManager.class);
    
    /** Token密钥（至少32字符） */
    @Value("${app.token.secret:ruoyi-app-secret-key-for-jwt-token-2025}")
    private String secret;
    
    /** Access Token有效期（小时） */
    @Value("${app.token.accessExpireHours:2}")
    private int accessExpireHours;
    
    /** Refresh Token有效期（天） */
    @Value("${app.token.refreshExpireDays:7}")
    private int refreshExpireDays;
    
    /**
     * 生成Access Token
     */
    public String generateAccessToken(UserTypeEnum userType, String userId, String phone, 
                                      String name, List<String> projectIds, String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", userType.getCode());
        claims.put("userId", userId);
        claims.put("phone", phone);
        claims.put("name", name);
        claims.put("projectIds", projectIds);
        claims.put("deviceId", deviceId);
        claims.put("tokenType", "access");
        
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessExpireHours * 60 * 60 * 1000L);
        
        return Jwts.builder()
                .setClaims(claims)
                .setId(UUID.fastUUID().toString())
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
    
    /**
     * 生成Refresh Token
     */
    public String generateRefreshToken(UserTypeEnum userType, String userId, String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", userType.getCode());
        claims.put("userId", userId);
        claims.put("deviceId", deviceId);
        claims.put("tokenType", "refresh");
        
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshExpireDays * 24 * 60 * 60 * 1000L);
        
        return Jwts.builder()
                .setClaims(claims)
                .setId(UUID.fastUUID().toString())
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
    
    /**
     * 验证Token
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 解析Token（不验证过期）
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 返回过期的claims
            return e.getClaims();
        } catch (JwtException e) {
            log.warn("Token解析失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            return true;
        }
    }
    
    /**
     * 从Token中获取用户类型
     */
    public UserTypeEnum getUserTypeFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            String userType = claims.get("userType", String.class);
            return UserTypeEnum.fromCode(userType);
        }
        return null;
    }
    
    /**
     * 从Token中获取用户ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("userId", String.class) : null;
    }
    
    /**
     * 从Token中获取项目ID列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getProjectIdsFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("projectIds", List.class) : null;
    }
    
    /**
     * 获取Access Token过期时间（秒）
     */
    public long getAccessTokenExpireSeconds() {
        return accessExpireHours * 60 * 60L;
    }
    
    /**
     * 获取Token剩余有效时间（秒）
     */
    public long getTokenRemainingSeconds(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims != null && claims.getExpiration() != null) {
                long remaining = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
                return Math.max(0, remaining);
            }
        } catch (Exception e) {
            log.warn("获取Token剩余时间失败: {}", e.getMessage());
        }
        return 0;
    }
}
