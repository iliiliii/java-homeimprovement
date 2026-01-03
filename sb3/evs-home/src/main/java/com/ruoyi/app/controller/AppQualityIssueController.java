package com.ruoyi.app.controller;

import com.ruoyi.app.dto.request.QualityFixSubmitRequest;
import com.ruoyi.app.dto.request.QualityIssueReportRequest;
import com.ruoyi.app.service.IAppQualityIssueService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 小程序质量问题接口
 */
@RestController
@RequestMapping("/app")
@Validated
public class AppQualityIssueController {

    private static final Logger log = LoggerFactory.getLogger(AppQualityIssueController.class);

    @Autowired
    private IAppQualityIssueService qualityIssueService;

    /**
     * 获取项目质量问题列表
     */
    @GetMapping("/qualityIssues/list")
    public AjaxResult getQualityIssueList(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "X-Project-Id", required = false) String projectId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        try {
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            if (projectId == null || projectId.isEmpty()) {
                return AjaxResult.error(400, "未提供项目ID");
            }
            return AjaxResult.success(qualityIssueService.getQualityIssueList(token, projectId, page, pageSize));
        } catch (ServiceException e) {
            log.warn("获取质量问题列表失败: {}", e.getMessage());
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取质量问题列表异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 问题上报（仅员工可用）
     */
    @PostMapping("/qualityIssues/report")
    public AjaxResult reportQualityIssue(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "X-Project-Id", required = false) String projectId,
            @Valid @RequestBody QualityIssueReportRequest request) {
        try {
            log.info("接收到问题上报请求 - projectId: {}, title: {}", projectId, request.getTitle());
            
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            if (projectId == null || projectId.isEmpty()) {
                return AjaxResult.error(400, "未提供项目ID");
            }
            if (request.getTitle() == null || request.getTitle().isEmpty()) {
                return AjaxResult.error(400, "问题标题不能为空");
            }
            if (request.getDescription() == null || request.getDescription().isEmpty()) {
                return AjaxResult.error(400, "问题描述不能为空");
            }
            
            String issueId = qualityIssueService.reportQualityIssue(token, projectId, request);
            log.info("问题上报成功，ID: {}", issueId);
            return AjaxResult.success("问题上报成功", issueId);
        } catch (ServiceException e) {
            log.warn("问题上报失败: {}", e.getMessage());
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("问题上报异常", e);
            return AjaxResult.error(500, "上报失败: " + e.getMessage());
        }
    }

    /**
     * 获取质量问题详情
     */
    @GetMapping("/qualityIssues/{issueId}")
    public AjaxResult getQualityIssueDetail(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String issueId) {
        try {
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            return AjaxResult.success(qualityIssueService.getQualityIssueDetail(token, issueId));
        } catch (ServiceException e) {
            log.warn("获取质量问题详情失败: {}", e.getMessage());
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取质量问题详情异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取字典数据
     * @param dictType 字典类型（如：decoration_issue_severity, decoration_construction_stage）
     */
    @GetMapping("/dict/{dictType}")
    public AjaxResult getDictData(@PathVariable String dictType) {
        try {
            return AjaxResult.success(qualityIssueService.getDictData(dictType));
        } catch (Exception e) {
            log.error("获取字典数据异常: {}", dictType, e);
            return AjaxResult.error(500, "获取字典数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取问题的整改记录列表
     */
    @GetMapping("/qualityFixes/byIssue/{issueId}")
    public AjaxResult getFixesByIssueId(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String issueId) {
        try {
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            return AjaxResult.success(qualityIssueService.getFixesByIssueId(token, issueId));
        } catch (ServiceException e) {
            log.warn("获取整改记录失败: {}", e.getMessage());
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取整改记录异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 提交整改记录（仅员工可用）
     */
    @PostMapping("/qualityFixes/submit")
    public AjaxResult submitFix(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody QualityFixSubmitRequest request) {
        try {
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            
            String fixId = qualityIssueService.submitFix(token, request.getIssueId(), 
                    request.getFixDescription(), request.getImages(), request.getStatus());
            log.info("整改提交成功，ID: {}", fixId);
            return AjaxResult.success("整改提交成功", fixId);
        } catch (ServiceException e) {
            log.warn("整改提交失败: {}", e.getMessage());
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("整改提交异常", e);
            return AjaxResult.error(500, "提交失败: " + e.getMessage());
        }
    }
}
