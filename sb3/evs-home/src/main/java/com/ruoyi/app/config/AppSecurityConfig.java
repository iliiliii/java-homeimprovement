package com.ruoyi.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 小程序接口安全配置
 * 让/app/**接口绕过Spring Security认证，由AppTokenManager自行验证Token
 */
@Configuration
public class AppSecurityConfig {

    @Bean
    @Order(1) // 优先级高于默认配置
    public SecurityFilterChain appSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // 只匹配/app/**路径
            .securityMatcher("/app/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 所有/app/**请求都允许访问，由Controller自行验证Token
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}
