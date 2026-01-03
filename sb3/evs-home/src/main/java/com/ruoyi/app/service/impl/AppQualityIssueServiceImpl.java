package com.ruoyi.app.service.impl;

import com.ruoyi.app.dto.request.QualityIssueReportRequest;
import com.ruoyi.app.mapper.AppDashboardMapper;
import com.ruoyi.app.mapper.AppProjectMapper;
import com.ruoyi.app.security.AppTokenManager;
import com.ruoyi.app.service.IAppQualityIssueService;
import com.ruoyi.app.util.InputSanitizer;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.domain.QualityFixes;
import com.ruoyi.web.domain.QualityInspections;
import com.ruoyi.web.domain.QualityIssues;
import com.ruoyi.web.mapper.QualityFixesMapper;
import com.ruoyi.web.mapper.QualityInspectionsMapper;
import com.ruoyi.web.mapper.QualityIssuesMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 小程序质量问题服务实现
 */
@Service
public class AppQualityIssueServiceImpl implements IAppQualityIssueService {

    private static final Logger log = LoggerFactory.getLogger(AppQualityIssueServiceImpl.class);

    @Autowired
    private AppTokenManager tokenManager;

    @Autowired
    private AppProjectMapper projectMapper;

    @Autowired
    private AppDashboardMapper dashboardMapper;

    @Autowired
    private QualityInspectionsMapper qualityInspectionsMapper;

    @Autowired
    private QualityIssuesMapper qualityIssuesMapper;

    @Autowired
    private QualityFixesMapper qualityFixesMapper;

    // 字典类型常量
    private static final String DICT_TYPE_CONSTRUCTION_STAGE = "decoration_construction_stage";
    private static final String DICT_TYPE_ISSUE_SEVERITY = "decoration_issue_severity";

    @Override
    public Map<String, Object> getQualityIssueList(String token, String projectId, Integer page, Integer pageSize) {
        validateTokenAndAccess(token, projectId);

        try {
            // 查询质量问题列表
            QualityIssues query = new QualityIssues();
            query.setProjectId(projectId);
            List<QualityIssues> issues = qualityIssuesMapper.selectQualityIssuesList(query);

            // 转换为前端需要的格式
            List<Map<String, Object>> list = new ArrayList<>();
            for (QualityIssues issue : issues) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", issue.getId());
                item.put("title", issue.getTitle());
                item.put("description", issue.getDescription());
                item.put("category", issue.getCategory());
                item.put("categoryText", getDictLabel(DICT_TYPE_ISSUE_SEVERITY, issue.getCategory()));
                item.put("location", issue.getLocation());
                item.put("status", issue.getStatus());
                item.put("statusText", getStatusText(issue.getStatus()));
                item.put("images", parseImagesJson(issue.getImages()));
                item.put("dueDate", issue.getDueDate());
                item.put("createdAt", issue.getCreatedAt());
                item.put("createdBy", issue.getCreatedBy());
                list.add(item);
            }

            // 分页处理
            int total = list.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<Map<String, Object>> pagedList = start < total ? list.subList(start, end) : new ArrayList<>();

            Map<String, Object> result = new HashMap<>();
            result.put("list", pagedList);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);

            return result;
        } catch (Exception e) {
            log.error("查询质量问题列表失败", e);
            throw new ServiceException("查询质量问题列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String reportQualityIssue(String token, String projectId, QualityIssueReportRequest request) {
        Map<String, Object> claims = validateTokenAndAccess(token, projectId);
        String userType = (String) claims.get("userType");
        String userName = (String) claims.get("name"); // token中存储的是name字段

        // 只有员工可以上报问题
        if (!"staff".equals(userType)) {
            throw new ServiceException("只有员工可以上报问题", 403);
        }

        try {
            // 对文本字段进行安全清理
            String sanitizedTitle = InputSanitizer.validateAndSanitizeText(request.getTitle(), "问题标题");
            String sanitizedDescription = InputSanitizer.validateAndSanitizeContent(request.getDescription(), "问题描述");
            String sanitizedLocation = InputSanitizer.validateAndSanitizeText(request.getLocation(), "问题位置");
            String sanitizedImages = InputSanitizer.sanitizeJson(request.getImages());
            
            // 验证图片JSON格式
            if (StringUtils.isNotEmpty(sanitizedImages) && !InputSanitizer.isValidJsonArray(sanitizedImages)) {
                throw new ServiceException("图片数据格式不正确");
            }

            // 1. 创建质检记录
            QualityInspections inspection = new QualityInspections();
            inspection.setId(IdUtils.fastSimpleUUID());
            inspection.setProjectId(projectId);
            inspection.setInspectionType(request.getInspectionType());
            inspection.setTitle(sanitizedTitle);
            inspection.setDescription(sanitizedDescription);
            inspection.setResult("UNQUALIFIED"); // 问题上报默认为不通过
            inspection.setImages(sanitizedImages);
            inspection.setRemarks("小程序问题上报");
            inspection.setCreatedAt(new Date());
            inspection.setCreatedBy(userName);

            // 解析检查日期
            if (StringUtils.isNotEmpty(request.getInspectionDate())) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    inspection.setInspectionDate(sdf.parse(request.getInspectionDate()));
                } catch (Exception e) {
                    inspection.setInspectionDate(new Date());
                }
            } else {
                inspection.setInspectionDate(new Date());
            }

            qualityInspectionsMapper.insertQualityInspections(inspection);

            // 2. 创建质量问题记录
            QualityIssues issue = new QualityIssues();
            issue.setId(IdUtils.fastSimpleUUID());
            issue.setProjectId(projectId);
            issue.setQualityInspectionId(inspection.getId());
            issue.setTitle(sanitizedTitle);
            issue.setDescription(sanitizedDescription);
            issue.setCategory(StringUtils.isNotEmpty(request.getCategory()) ? request.getCategory() : "GENERAL");
            issue.setLocation(sanitizedLocation);
            issue.setImages(sanitizedImages);
            issue.setStatus("OPEN");
            issue.setCreatedAt(new Date());
            issue.setCreatedBy(userName);

            // 解析整改期限
            if (StringUtils.isNotEmpty(request.getDueDate())) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    issue.setDueDate(sdf.parse(request.getDueDate()));
                } catch (Exception e) {
                    log.warn("解析整改期限失败: {}", request.getDueDate());
                }
            }

            qualityIssuesMapper.insertQualityIssues(issue);

            return issue.getId();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("问题上报失败", e);
            throw new ServiceException("问题上报失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getQualityIssueDetail(String token, String issueId) {
        try {
            QualityIssues issue = qualityIssuesMapper.selectQualityIssuesById(issueId);
            if (issue == null) {
                throw new ServiceException("质量问题不存在");
            }

            // 验证项目权限
            validateTokenAndAccess(token, issue.getProjectId());

            Map<String, Object> result = new HashMap<>();
            result.put("id", issue.getId());
            result.put("title", issue.getTitle());
            result.put("description", issue.getDescription());
            result.put("category", issue.getCategory());
            result.put("categoryText", getDictLabel(DICT_TYPE_ISSUE_SEVERITY, issue.getCategory()));
            result.put("location", issue.getLocation());
            result.put("status", issue.getStatus());
            result.put("statusText", getStatusText(issue.getStatus()));
            result.put("images", parseImagesJson(issue.getImages()));
            result.put("dueDate", issue.getDueDate());
            result.put("resolvedAt", issue.getResolvedAt());
            result.put("createdAt", issue.getCreatedAt());
            result.put("createdBy", issue.getCreatedBy());

            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询质量问题详情失败", e);
            throw new ServiceException("查询质量问题详情失败: " + e.getMessage());
        }
    }

    /**
     * 验证Token并检查项目访问权限
     */
    private Map<String, Object> validateTokenAndAccess(String token, String projectId) {
        Map<String, Object> claims = tokenManager.validateToken(extractToken(token));
        String userType = (String) claims.get("userType");
        String userId = claims.get("userId").toString();

        // 验证用户是否有权限访问该项目
        if ("customer".equals(userType)) {
            Projects project = projectMapper.selectProjectById(projectId);
            if (project == null || !userId.equals(project.getCustomerId())) {
                throw new ServiceException("无权访问该项目");
            }
        } else if ("staff".equals(userType)) {
            boolean hasAccess = dashboardMapper.checkStaffProjectAccess(userId, projectId);
            if (!hasAccess) {
                throw new ServiceException("无权访问该项目");
            }
        }

        return claims;
    }

    /**
     * 提取Token（移除Bearer前缀）
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    /**
     * 从字典表获取标签
     */
    private String getDictLabel(String dictType, String dictValue) {
        if (StringUtils.isEmpty(dictValue)) {
            return dictValue;
        }
        try {
            String label = dashboardMapper.selectDictLabel(dictType, dictValue);
            return label != null ? label : dictValue;
        } catch (Exception e) {
            return dictValue;
        }
    }

    /**
     * 获取问题状态文本
     */
    private String getStatusText(String status) {
        if (StringUtils.isEmpty(status)) {
            return "";
        }
        switch (status) {
            case "OPEN":
                return "待处理";
            case "IN_PROGRESS":
                return "整改中";
            case "RESOLVED":
                return "已解决";
            case "CLOSED":
                return "已关闭";
            default:
                return status;
        }
    }

    /**
     * 解析图片JSON字符串为URL列表
     */
    private List<String> parseImagesJson(String imagesJson) {
        List<String> images = new ArrayList<>();
        if (StringUtils.isEmpty(imagesJson)) {
            return images;
        }

        try {
            if (imagesJson.startsWith("[") && imagesJson.endsWith("]")) {
                String content = imagesJson.substring(1, imagesJson.length() - 1);
                if (!content.isEmpty()) {
                    String[] imageArray = content.split(",");
                    for (String img : imageArray) {
                        String cleanImg = img.trim().replace("\"", "");
                        if (!cleanImg.isEmpty()) {
                            images.add(cleanImg);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析图片JSON失败: {}", imagesJson, e);
        }

        return images;
    }

    @Override
    public List<Map<String, Object>> getDictData(String dictType) {
        try {
            List<Map<String, Object>> dictList = dashboardMapper.selectDictDataByType(dictType);
            // 转换为前端需要的格式
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> dict : dictList) {
                Map<String, Object> item = new HashMap<>();
                item.put("value", dict.get("dictValue"));
                item.put("label", dict.get("dictLabel"));
                result.add(item);
            }
            return result;
        } catch (Exception e) {
            log.error("获取字典数据失败: {}", dictType, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getFixesByIssueId(String token, String issueId) {
        try {
            // 先获取问题信息以验证权限
            QualityIssues issue = qualityIssuesMapper.selectQualityIssuesById(issueId);
            if (issue == null) {
                throw new ServiceException("质量问题不存在");
            }

            // 验证项目权限
            validateTokenAndAccess(token, issue.getProjectId());

            // 查询整改记录
            QualityFixes query = new QualityFixes();
            query.setQualityIssuesId(issueId);
            List<QualityFixes> fixes = qualityFixesMapper.selectQualityFixesList(query);

            // 转换为前端需要的格式
            List<Map<String, Object>> result = new ArrayList<>();
            for (QualityFixes fix : fixes) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", fix.getId());
                item.put("qualityIssuesId", fix.getQualityIssuesId());
                item.put("fixDescription", fix.getFixDescription());
                item.put("images", parseImagesJson(fix.getImages()));
                item.put("status", fix.getStatus());
                item.put("statusText", getFixStatusText(fix.getStatus()));
                item.put("fixedAt", fix.getFixedAt());
                item.put("verifiedAt", fix.getVerifiedAt());
                item.put("createdAt", fix.getCreatedAt());
                item.put("createdBy", fix.getCreatedBy());
                result.add(item);
            }

            // 按创建时间倒序排序
            result.sort((a, b) -> {
                Date dateA = (Date) a.get("createdAt");
                Date dateB = (Date) b.get("createdAt");
                if (dateA == null && dateB == null) return 0;
                if (dateA == null) return 1;
                if (dateB == null) return -1;
                return dateB.compareTo(dateA);
            });

            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询整改记录失败", e);
            throw new ServiceException("查询整改记录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitFix(String token, String issueId, String fixDescription, String images, String status) {
        try {
            // 先获取问题信息以验证权限
            QualityIssues issue = qualityIssuesMapper.selectQualityIssuesById(issueId);
            if (issue == null) {
                throw new ServiceException("质量问题不存在");
            }

            // 验证项目权限并获取用户信息
            Map<String, Object> claims = validateTokenAndAccess(token, issue.getProjectId());
            String userType = (String) claims.get("userType");
            String userName = (String) claims.get("name");

            // 只有员工可以提交整改
            if (!"staff".equals(userType)) {
                throw new ServiceException("只有员工可以提交整改", 403);
            }

            // 对文本字段进行安全清理
            String sanitizedDescription = InputSanitizer.validateAndSanitizeContent(fixDescription, "整改描述");
            String sanitizedImages = InputSanitizer.sanitizeJson(images);
            
            // 验证图片JSON格式
            if (StringUtils.isNotEmpty(sanitizedImages) && !InputSanitizer.isValidJsonArray(sanitizedImages)) {
                throw new ServiceException("图片数据格式不正确");
            }

            // 创建整改记录
            QualityFixes fix = new QualityFixes();
            fix.setId(IdUtils.fastSimpleUUID());
            fix.setQualityIssuesId(issueId);
            fix.setFixDescription(sanitizedDescription);
            fix.setImages(sanitizedImages);
            fix.setStatus(StringUtils.isNotEmpty(status) ? status : "IN_PROGRESS");
            fix.setCreatedAt(new Date());
            fix.setCreatedBy(userName);

            // 如果状态为已解决，设置修复时间
            if ("RESOLVED".equals(status)) {
                fix.setFixedAt(new Date());
            }

            qualityFixesMapper.insertQualityFixes(fix);

            // 更新问题状态
            issue.setStatus(status);
            issue.setUpdatedAt(new Date());
            if ("RESOLVED".equals(status)) {
                issue.setResolvedAt(new Date());
            }
            qualityIssuesMapper.updateQualityIssues(issue);

            return fix.getId();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("提交整改失败", e);
            throw new ServiceException("提交整改失败: " + e.getMessage());
        }
    }

    /**
     * 获取整改状态文本
     */
    private String getFixStatusText(String status) {
        if (StringUtils.isEmpty(status)) {
            return "";
        }
        switch (status) {
            case "OPEN":
                return "未解决";
            case "IN_PROGRESS":
                return "解决中";
            case "RESOLVED":
                return "已解决";
            default:
                return status;
        }
    }
}
