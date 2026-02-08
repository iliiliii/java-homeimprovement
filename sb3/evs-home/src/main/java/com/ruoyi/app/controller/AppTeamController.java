package com.ruoyi.app.controller;

import com.ruoyi.app.service.IAppTeamService;
import com.ruoyi.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序团队成员接口
 */
@RestController
@RequestMapping("/app/team")
public class AppTeamController {
    
    private static final Logger log = LoggerFactory.getLogger(AppTeamController.class);
    
    @Autowired
    private IAppTeamService teamService;
    
    /**
     * 获取团队成员列表
     * 过滤条件：email不为空、未删除、未停用
     * 
     * @return 团队成员列表
     */
    @GetMapping("/members")
    public AjaxResult getTeamMembers() {
        try {
            return AjaxResult.success(teamService.getTeamMembers());
        } catch (Exception e) {
            log.error("获取团队成员列表异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }
}
