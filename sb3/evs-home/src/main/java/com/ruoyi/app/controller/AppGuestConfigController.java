package com.ruoyi.app.controller;

import com.ruoyi.app.service.IGuestConfigService;
import com.ruoyi.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游客配置控制器
 * 提供游客演示项目配置相关接口
 */
@RestController
@RequestMapping("/app/guest")
public class AppGuestConfigController {
    
    private static final Logger log = LoggerFactory.getLogger(AppGuestConfigController.class);
    
    @Autowired
    private IGuestConfigService guestConfigService;
    
    /**
     * 获取游客演示项目配置
     * 无需认证，公开访问
     * 
     * @return 演示项目配置信息
     */
    @GetMapping("/demo-projects")
    public AjaxResult getDemoProjects() {
        try {
            log.info("[游客配置API] 获取演示项目配置");
            
            List<String> projectIds = guestConfigService.getGuestDemoProjectIds();
            String defaultProjectId = guestConfigService.getDefaultGuestProjectId();
            
            Map<String, Object> result = new HashMap<>();
            result.put("projectIds", projectIds);
            result.put("defaultProjectId", defaultProjectId);
            result.put("count", projectIds.size());
            
            log.info("[游客配置API] 返回 {} 个演示项目，默认项目: {}", projectIds.size(), defaultProjectId);
            
            return AjaxResult.success(result);
            
        } catch (Exception e) {
            log.error("[游客配置API] 获取演示项目配置失败", e);
            return AjaxResult.error("获取配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 健康检查接口
     * 用于验证Controller是否正常工作
     */
    @GetMapping("/health")
    public AjaxResult health() {
        log.info("[游客配置API] 健康检查");
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("timestamp", System.currentTimeMillis());
        result.put("message", "游客配置服务正常运行");
        return AjaxResult.success(result);
    }
    
    /**
     * 检查项目是否为演示项目
     * 无需认证，公开访问
     * 
     * @param projectId 项目ID
     * @return 是否为演示项目
     */
    @GetMapping("/check-demo-project/{projectId}")
    public AjaxResult checkDemoProject(@PathVariable String projectId) {
        try {
            log.info("[游客配置API] 检查项目是否为演示项目: {}", projectId);
            
            boolean isDemo = guestConfigService.isGuestDemoProject(projectId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("projectId", projectId);
            result.put("isDemo", isDemo);
            
            return AjaxResult.success(result);
            
        } catch (Exception e) {
            log.error("[游客配置API] 检查演示项目失败: projectId={}", projectId, e);
            return AjaxResult.error("检查失败: " + e.getMessage());
        }
    }
}
