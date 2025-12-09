package com.ruoyi.app.controller;

import com.ruoyi.app.dto.response.AppProjectInfo;
import com.ruoyi.app.service.IAppDashboardService;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序首页数据接口
 */
@RestController
@RequestMapping("/app/dashboard")
public class AppDashboardController {

    @Autowired
    private IAppDashboardService dashboardService;

    /**
     * 获取客户首页数据
     * 包含：项目列表、当前项目进度、最新动态等
     */
    @GetMapping("/customer")
    public AjaxResult getCustomerDashboard(@RequestHeader("Authorization") String token) {
        return AjaxResult.success(dashboardService.getCustomerDashboard(token));
    }

    /**
     * 获取员工首页数据
     * 包含：负责的项目列表、待办事项、质检任务等
     */
    @GetMapping("/staff")
    public AjaxResult getStaffDashboard(@RequestHeader("Authorization") String token) {
        return AjaxResult.success(dashboardService.getStaffDashboard(token));
    }

    /**
     * 获取项目详情（包含进度、质检等信息）
     */
    @GetMapping("/project/{projectId}")
    public AjaxResult getProjectDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable String projectId) {
        return AjaxResult.success(dashboardService.getProjectDetail(token, projectId));
    }

    /**
     * 获取项目进度列表
     */
    @GetMapping("/project/{projectId}/schedules")
    public AjaxResult getProjectSchedules(
            @RequestHeader("Authorization") String token,
            @PathVariable String projectId) {
        return AjaxResult.success(dashboardService.getProjectSchedules(token, projectId));
    }

    /**
     * 获取项目质检记录
     */
    @GetMapping("/project/{projectId}/inspections")
    public AjaxResult getProjectInspections(
            @RequestHeader("Authorization") String token,
            @PathVariable String projectId) {
        return AjaxResult.success(dashboardService.getProjectInspections(token, projectId));
    }

    /**
     * 获取项目设计图列表
     */
    @GetMapping("/project/{projectId}/designs")
    public AjaxResult getProjectDesigns(
            @RequestHeader("Authorization") String token,
            @PathVariable String projectId) {
        return AjaxResult.success(dashboardService.getProjectDesigns(token, projectId));
    }
}
