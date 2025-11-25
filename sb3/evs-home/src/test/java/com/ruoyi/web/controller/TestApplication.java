package com.ruoyi.web.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Controller测试专用Spring Boot配置类
 * 避免与ruoyi-admin模块的循环依赖
 *
 * @author evs
 * @date 2025-11-26
 */
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}