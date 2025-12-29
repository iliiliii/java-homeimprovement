package com.ruoyi.app.controller;

import com.ruoyi.app.service.IAppProjectScheduleService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序项目进度接口
 */
@RestController
@RequestMapping("/app")
public class AppProjectScheduleController {

    private static final Logger log = LoggerFactory.getLogger(AppProjectScheduleController.class);

    @Autowired
    private IAppProjectScheduleService projectScheduleService;

    /**
     * 获取项目进度列表
     * 返回项目的所有施工阶段进度信息
     */
    @GetMapping("/projectSchedules/list")
    public AjaxResult getProjectScheduleList(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "X-Project-Id", required = false) String projectId) {
        try {
            log.info("接收到获取项目进度列表请求 - projectId: {}, token存在: {}", 
                    projectId, token != null && !token.isEmpty());
            
            if (token == null || token.isEmpty()) {
                log.warn("未提供认证Token");
                return AjaxResult.error(401, "未提供认证Token");
            }
            if (projectId == null || projectId.isEmpty()) {
                log.warn("未提供项目ID");
                return AjaxResult.error(400, "未提供项目ID");
            }
            
            log.info("调用服务层获取项目进度列表");
            return AjaxResult.success(projectScheduleService.getProjectScheduleList(token, projectId));
        } catch (ServiceException e) {
            log.warn("获取项目进度列表失败: {}", e.getMessage());
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取项目进度列表异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目进度验收记录列表
     * 返回所有进度节点的验收记录，按时间排序
     */
    @GetMapping("/projectScheduleRecords/list")
    public AjaxResult getProjectScheduleRecordList(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "X-Project-Id", required = false) String projectId,
            @RequestParam(value = "scheduleId", required = false) String scheduleId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        try {
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            if (projectId == null || projectId.isEmpty()) {
                return AjaxResult.error(400, "未提供项目ID");
            }
            return AjaxResult.success(projectScheduleService.getProjectScheduleRecordList(
                    token, projectId, scheduleId, page, pageSize));
        } catch (ServiceException e) {
            log.warn("获取项目验收记录列表失败: {}", e.getMessage());
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取项目验收记录列表异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取进度验收记录详情
     */
    @GetMapping("/projectScheduleRecords/{recordId}")
    public AjaxResult getProjectScheduleRecordDetail(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String recordId) {
        try {
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            return AjaxResult.success(projectScheduleService.getProjectScheduleRecordDetail(token, recordId));
        } catch (ServiceException e) {
            log.warn("获取验收记录详情失败: {}", e.getMessage());
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取验收记录详情异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }
}