package com.ruoyi.web.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * 测试用安全配置
 * 仅包含 @PreAuthorize 注解所需的最���配置
 *
 * @author evs
 * @date 2025-11-26
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(new HttpStatusEntryPoint())
            )
            .authorizeHttpRequests((requests) -> requests
                .anyRequest().authenticated()
            )
            .build();
    }

    /**
     * 提供 'ss' bean 用于 @PreAuthorize("@ss.hasPermi('xxx')") 表达式
     */
    @Bean
    @Primary
    public TestSecurityService ss() {
        return new TestSecurityService();
    }

    /**
     * 简化的安全服务实现，用于测试
     */
    public static class TestSecurityService {
        public boolean hasPermi(String permission) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return false;
            }

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(permission));
        }
    }

    /**
     * 自定义认证入口点，返回401状态码
     */
    private static class HttpStatusEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                           org.springframework.security.core.AuthenticationException authException)
                throws IOException, ServletException {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
