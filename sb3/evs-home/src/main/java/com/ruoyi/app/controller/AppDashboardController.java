package com.ruoyi.app.controller;

import com.ruoyi.app.service.IAppDashboardService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序首页数据接口
 */
@RestController
@RequestMapping("/app/dashboard")
public class AppDashboardController {

    private static final Logger log = LoggerFactory.getLogger(AppDashboardController.class);

    @Autowired
    private IAppDashboardService dashboardService;

    /**
     * 获取客户首页数据
     * 包含：项目列表、当前项目进度、最新动态等
     */
    @GetMapping("/customer")
    public AjaxResult getCustomerDashboard(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            return AjaxResult.success(dashboardService.getCustomerDashboard(token));
        } catch (ServiceException e) {
            log.warn("获取客户首页数据失败: {}", e.getMessage());
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取客户首页数据异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取员工首页数据
     * 包含：负责的项目列表、待办事项、质检任务等
     */
    @GetMapping("/staff")
    public AjaxResult getStaffDashboard(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            return AjaxResult.success(dashboardService.getStaffDashboard(token));
        } catch (ServiceException e) {
            log.warn("获取员工首页数据失败: {}", e.getMessage());
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取员工首页数据异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目详情（包含进度、质检等信息）
     */
    @GetMapping("/project/{projectId}")
    public AjaxResult getProjectDetail(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String projectId) {
        try {
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            return AjaxResult.success(dashboardService.getProjectDetail(token, projectId));
        } catch (ServiceException e) {
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取项目详情异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目进度列表
     */
    @GetMapping("/project/{projectId}/schedules")
    public AjaxResult getProjectSchedules(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String projectId) {
        try {
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            return AjaxResult.success(dashboardService.getProjectSchedules(token, projectId));
        } catch (ServiceException e) {
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取项目进度异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目质检记录
     */
    @GetMapping("/project/{projectId}/inspections")
    public AjaxResult getProjectInspections(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String projectId) {
        try {
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            return AjaxResult.success(dashboardService.getProjectInspections(token, projectId));
        } catch (ServiceException e) {
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取项目质检记录异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目设计图列表
     */
    @GetMapping("/project/{projectId}/designs")
    public AjaxResult getProjectDesigns(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String projectId) {
        try {
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            return AjaxResult.success(dashboardService.getProjectDesigns(token, projectId));
        } catch (ServiceException e) {
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取项目设计图异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目房间列表（设计稿管理）
     */
    @GetMapping("/project/{projectId}/rooms")
    public AjaxResult getProjectRooms(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String projectId) {
        try {
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            return AjaxResult.success(dashboardService.getProjectRooms(token, projectId));
        } catch (ServiceException e) {
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取项目房间列表异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }
}
