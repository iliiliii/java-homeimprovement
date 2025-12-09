package com.ruoyi.app.service.impl;

import com.ruoyi.app.dto.response.*;
import com.ruoyi.app.mapper.AppProjectMapper;
import com.ruoyi.app.mapper.AppDashboardMapper;
import com.ruoyi.app.security.AppTokenManager;
import com.ruoyi.app.service.IAppDashboardService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.web.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 小程序首页数据服务实现
 */
@Service
public class AppDashboardServiceImpl implements IAppDashboardService {

    private static final Logger log = LoggerFactory.getLogger(AppDashboardServiceImpl.class);

    @Autowired
    private AppTokenManager tokenManager;

    @Autowired
    private AppProjectMapper projectMapper;

    @Autowired
    private AppDashboardMapper dashboardMapper;

    // 阶段名称映射
    private static final Map<String, String> STAGE_TEXT_MAP = new HashMap<>();
    static {
        STAGE_TEXT_MAP.put("DESIGN", "设计阶段");
        STAGE_TEXT_MAP.put("DISMANTLING", "拆除阶段");
        STAGE_TEXT_MAP.put("WATER_ELECTRIC", "水电阶段");
        STAGE_TEXT_MAP.put("TILES", "泥瓦阶段");
        STAGE_TEXT_MAP.put("WOODWORK", "木工阶段");
        STAGE_TEXT_MAP.put("PAINTING", "油漆阶段");
        STAGE_TEXT_MAP.put("INSTALLATION", "安装阶段");
        STAGE_TEXT_MAP.put("SOFT_FURNISHING", "软装阶段");
        STAGE_TEXT_MAP.put("ACCEPTANCE", "验收阶段");
    }

    // 状态名称映射
    private static final Map<String, String> STATUS_TEXT_MAP = new HashMap<>();
    static {
        STATUS_TEXT_MAP.put("PENDING", "待开始");
        STATUS_TEXT_MAP.put("IN_PROGRESS", "进行中");
        STATUS_TEXT_MAP.put("COMPLETED", "已完成");
        STATUS_TEXT_MAP.put("DESIGN", "设计中");
        STATUS_TEXT_MAP.put("CONSTRUCTION", "施工中");
    }

    // 角色名称映射
    private static final Map<String, String> ROLE_TEXT_MAP = new HashMap<>();
    static {
        ROLE_TEXT_MAP.put("DESIGNER", "设计师");
        ROLE_TEXT_MAP.put("PM", "项目经理");
        ROLE_TEXT_MAP.put("WORKER", "工长");
        ROLE_TEXT_MAP.put("SUPERVISOR", "监理");
    }

    @Override
    public CustomerDashboardVO getCustomerDashboard(String token) {
        // 验证Token并获取用户信息
        Map<String, Object> claims = tokenManager.validateToken(extractToken(token));
        String userType = (String) claims.get("userType");
        String userId = (String) claims.get("userId");

        if (!"customer".equals(userType)) {
            throw new ServiceException("非客户用户无法访问此接口");
        }

        CustomerDashboardVO result = new CustomerDashboardVO();
        // 不再返回用户信息，前端使用登录时缓存的数据

        // 获取客户关联的项目列表（使用带字典的查询）
        List<CustomerProjectVO> projectVOs = dashboardMapper.selectCustomerProjects(userId);
        
        // 补充项目详细信息
        for (CustomerProjectVO vo : projectVOs) {
            // 获取当前阶段
            ProjectSchedules currentSchedule = dashboardMapper.selectCurrentSchedule(vo.getId());
            if (currentSchedule != null) {
                vo.setCurrentStage(currentSchedule.getStage());
                vo.setCurrentStageText(STAGE_TEXT_MAP.getOrDefault(currentSchedule.getStage(), currentSchedule.getStage()));
            } else {
                vo.setCurrentStage("DESIGN");
                vo.setCurrentStageText("设计阶段");
            }
            
            // 计算进度（如果数据库没有）
            if (vo.getProgressPercent() == null || vo.getProgressPercent() == 0) {
                vo.setProgressPercent(calculateProgress(vo.getId()));
            }
            
            // 设置卡片类型
            String status = vo.getStatus();
            if ("DESIGN".equals(status) || "design".equals(status)) {
                vo.setCardType("design");
            } else if ("COMPLETED".equals(status) || "completed".equals(status)) {
                vo.setCardType("completed");
            } else {
                vo.setCardType("construction");
            }
            
            // 设置下一个里程碑
            if (vo.getEndDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM.dd");
                vo.setNextMilestone(sdf.format(vo.getEndDate()));
            }
        }
        result.setProjects(projectVOs);

        // 设置默认选中的项目
        if (!projectVOs.isEmpty()) {
            result.setCurrentProjectId(projectVOs.get(0).getId());
        }

        return result;
    }

    @Override
    public StaffDashboardVO getStaffDashboard(String token) {
        // 验证Token并获取用户信息
        Map<String, Object> claims = tokenManager.validateToken(extractToken(token));
        String userType = (String) claims.get("userType");
        String odUserId = claims.get("userId").toString();
        Long userId = Long.parseLong(odUserId);

        if (!"staff".equals(userType)) {
            throw new ServiceException("非员工用户无法访问此接口");
        }

        StaffDashboardVO result = new StaffDashboardVO();
        // 不再返回用户信息，前端使用登录时缓存的数据

        // 获取员工关联的项目列表（使用带字典的查询）
        List<StaffProjectVO> projectVOs = dashboardMapper.selectStaffProjectsWithDict(userId);
        
        // 补充项目详细信息
        for (StaffProjectVO vo : projectVOs) {
            // 获取当前阶段
            ProjectSchedules currentSchedule = dashboardMapper.selectCurrentSchedule(vo.getId());
            if (currentSchedule != null) {
                vo.setCurrentStage(currentSchedule.getStage());
                vo.setCurrentStageText(STAGE_TEXT_MAP.getOrDefault(currentSchedule.getStage(), currentSchedule.getStage()));
            }
            
            // 获取待处理问题数
            int issueCount = dashboardMapper.countPendingIssues(vo.getId());
            vo.setPendingIssueCount(issueCount);
            
            // 设置状态文本
            vo.setStatusText(STATUS_TEXT_MAP.getOrDefault(vo.getStatus(), vo.getStatus()));
            vo.setMyRoleText(ROLE_TEXT_MAP.getOrDefault(vo.getMyRole(), vo.getMyRole()));
        }
        result.setProjects(projectVOs);

        // 统计待办事项
        StaffDashboardVO.TodoStats todoStats = new StaffDashboardVO.TodoStats();
        todoStats.setPendingInspections(dashboardMapper.countPendingInspections(userId));
        todoStats.setPendingIssues(dashboardMapper.countStaffPendingIssues(userId));
        todoStats.setTodayTasks(dashboardMapper.countTodayTasks(userId));
        result.setTodoStats(todoStats);

        return result;
    }

    @Override
    public ProjectDetailVO getProjectDetail(String token, String projectId) {
        // 验证Token
        validateTokenAndAccess(token, projectId);

        Projects project = projectMapper.selectProjectById(projectId);
        if (project == null) {
            throw new ServiceException("项目不存在");
        }

        ProjectDetailVO result = new ProjectDetailVO();
        result.setId(project.getId());
        result.setName(project.getName());
        result.setAddress(project.getAddress());
        result.setArea(project.getArea());
        result.setStatus(project.getStatus());
        result.setStatusText(STATUS_TEXT_MAP.getOrDefault(project.getStatus(), project.getStatus()));
        result.setStartDate(project.getStartDate());
        result.setEndDate(project.getEndDate());
        result.setBudget(project.getBudget());
        result.setActualCost(project.getActualCost());

        // 获取当前阶段
        ProjectSchedules currentSchedule = dashboardMapper.selectCurrentSchedule(projectId);
        if (currentSchedule != null) {
            result.setCurrentStage(currentSchedule.getStage());
            result.setCurrentStageText(STAGE_TEXT_MAP.getOrDefault(currentSchedule.getStage(), currentSchedule.getStage()));
        }

        // 计算总进度
        result.setProgressPercent(calculateProgress(projectId));

        // 获取进度列表
        result.setSchedules(getProjectSchedules(token, projectId));

        // 获取最近质检记录（最近3条）
        List<InspectionVO> inspections = getProjectInspections(token, projectId);
        result.setRecentInspections(inspections.size() > 3 ? inspections.subList(0, 3) : inspections);

        // 获取待处理问题数
        result.setPendingIssueCount(dashboardMapper.countPendingIssues(projectId));

        return result;
    }

    @Override
    public List<ScheduleVO> getProjectSchedules(String token, String projectId) {
        validateTokenAndAccess(token, projectId);

        List<ProjectSchedules> schedules = dashboardMapper.selectSchedulesByProjectId(projectId);
        return schedules.stream().map(s -> {
            ScheduleVO vo = new ScheduleVO();
            vo.setId(s.getId());
            vo.setStage(s.getStage());
            vo.setStageText(STAGE_TEXT_MAP.getOrDefault(s.getStage(), s.getStage()));
            vo.setStageOrder(s.getStageOrder() != null ? s.getStageOrder().intValue() : 0);
            vo.setPlanStartDate(s.getPlanStartDate());
            vo.setPlanEndDate(s.getPlanEndDate());
            vo.setActualStartDate(s.getActualStartDate());
            vo.setActualEndDate(s.getActualEndDate());
            vo.setStatus(s.getStatus());
            vo.setStatusText(STATUS_TEXT_MAP.getOrDefault(s.getStatus(), s.getStatus()));
            vo.setCompletionRate(s.getCompletionRate());
            vo.setDescription(s.getDescription());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<InspectionVO> getProjectInspections(String token, String projectId) {
        validateTokenAndAccess(token, projectId);

        List<QualityInspections> inspections = dashboardMapper.selectInspectionsByProjectId(projectId);
        return inspections.stream().map(this::convertToInspectionVO).collect(Collectors.toList());
    }

    @Override
    public List<DesignVO> getProjectDesigns(String token, String projectId) {
        validateTokenAndAccess(token, projectId);

        List<FileUploads> files = dashboardMapper.selectDesignFilesByProjectId(projectId);
        return files.stream().map(this::convertToDesignVO).collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    private void validateTokenAndAccess(String token, String projectId) {
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
            boolean hasAccess = dashboardMapper.checkStaffProjectAccess(Long.parseLong(userId), projectId);
            if (!hasAccess) {
                throw new ServiceException("无权访问该项目");
            }
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private CustomerProjectVO convertToCustomerProjectVO(Projects project) {
        CustomerProjectVO vo = new CustomerProjectVO();
        vo.setId(project.getId());
        vo.setName(project.getName());
        vo.setAddress(project.getAddress());
        vo.setArea(project.getArea());
        vo.setStatus(project.getStatus());
        vo.setEndDate(project.getEndDate());

        // 设置状态文本和卡片类型
        String status = project.getStatus();
        if ("DESIGN".equals(status)) {
            vo.setStatusText("设计中");
            vo.setCardType("design");
        } else if ("COMPLETED".equals(status)) {
            vo.setStatusText("已完工");
            vo.setCardType("completed");
        } else {
            vo.setStatusText("施工中");
            vo.setCardType("construction");
        }

        // 获取当前阶段
        ProjectSchedules currentSchedule = dashboardMapper.selectCurrentSchedule(project.getId());
        if (currentSchedule != null) {
            vo.setCurrentStage(currentSchedule.getStage());
            vo.setCurrentStageText(STAGE_TEXT_MAP.getOrDefault(currentSchedule.getStage(), currentSchedule.getStage()));
        } else {
            vo.setCurrentStage("DESIGN");
            vo.setCurrentStageText("设计阶段");
        }

        // 计算进度
        vo.setProgressPercent(calculateProgress(project.getId()));

        // 设置下一个里程碑
        if (project.getEndDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM.dd");
            vo.setNextMilestone(sdf.format(project.getEndDate()));
        }

        return vo;
    }

    private Integer calculateProgress(String projectId) {
        List<ProjectSchedules> schedules = dashboardMapper.selectSchedulesByProjectId(projectId);
        if (schedules == null || schedules.isEmpty()) {
            return 0;
        }

        int totalWeight = schedules.size();
        double totalProgress = 0;

        for (ProjectSchedules schedule : schedules) {
            if ("COMPLETED".equals(schedule.getStatus())) {
                totalProgress += 1;
            } else if ("IN_PROGRESS".equals(schedule.getStatus())) {
                BigDecimal rate = schedule.getCompletionRate();
                totalProgress += (rate != null ? rate.doubleValue() / 100 : 0.5);
            }
        }

        return (int) Math.round(totalProgress / totalWeight * 100);
    }

    private InspectionVO convertToInspectionVO(QualityInspections inspection) {
        InspectionVO vo = new InspectionVO();
        vo.setId(inspection.getId());
        vo.setInspectionType(inspection.getInspectionType());
        vo.setInspectionTypeText(getInspectionTypeText(inspection.getInspectionType()));
        vo.setTitle(inspection.getTitle());
        vo.setDescription(inspection.getDescription());
        vo.setResult(inspection.getResult());
        vo.setResultText("PASS".equals(inspection.getResult()) ? "合格" : "不合格");
        vo.setInspectionDate(inspection.getInspectionDate());
        vo.setRemarks(inspection.getRemarks());

        // 解析图片JSON
        if (inspection.getImages() != null && !inspection.getImages().isEmpty()) {
            vo.setImages(parseJsonArray(inspection.getImages()));
        }

        // 获取关联问题
        List<QualityIssues> issues = inspection.getIssues();
        if (issues != null) {
            vo.setIssueCount(issues.size());
            vo.setIssues(issues.stream().map(this::convertToIssueVO).collect(Collectors.toList()));
        }

        return vo;
    }

    private IssueVO convertToIssueVO(QualityIssues issue) {
        IssueVO vo = new IssueVO();
        vo.setId(issue.getId());
        vo.setTitle(issue.getTitle());
        vo.setDescription(issue.getDescription());
        vo.setCategory(issue.getCategory());
        vo.setCategoryText(getCategoryText(issue.getCategory()));
        vo.setLocation(issue.getLocation());
        vo.setStatus(issue.getStatus());
        vo.setStatusText(getIssueStatusText(issue.getStatus()));
        vo.setDueDate(issue.getDueDate());
        vo.setResolvedAt(issue.getResolvedAt());
        vo.setCreatedAt(issue.getCreatedAt());

        if (issue.getImages() != null && !issue.getImages().isEmpty()) {
            vo.setImages(parseJsonArray(issue.getImages()));
        }

        return vo;
    }

    private DesignVO convertToDesignVO(FileUploads file) {
        DesignVO vo = new DesignVO();
        vo.setId(file.getId());
        vo.setName(file.getOriginalName());
        vo.setImageUrl(file.getUrl() != null ? file.getUrl() : file.getPath());
        vo.setThumbnailUrl(file.getUrl() != null ? file.getUrl() : file.getPath());
        vo.setUpdateTime(file.getCreatedAt());

        // 根据文件名或标签判断空间类型
        String name = file.getOriginalName() != null ? file.getOriginalName().toLowerCase() : "";
        if (name.contains("客厅") || name.contains("living")) {
            vo.setSpaceType("living");
            vo.setSpaceTypeText("客厅");
        } else if (name.contains("卧室") || name.contains("bedroom")) {
            vo.setSpaceType("bedroom");
            vo.setSpaceTypeText("卧室");
        } else if (name.contains("厨房") || name.contains("kitchen")) {
            vo.setSpaceType("kitchen");
            vo.setSpaceTypeText("厨房");
        } else if (name.contains("卫生间") || name.contains("bathroom")) {
            vo.setSpaceType("bathroom");
            vo.setSpaceTypeText("卫生间");
        } else {
            vo.setSpaceType("other");
            vo.setSpaceTypeText("其他");
        }

        // 设置状态（默认已确认）
        vo.setStatus("confirmed");
        vo.setStatusText("已确认");

        // 格式化更新时间
        if (file.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM.dd HH:mm");
            vo.setUpdateTimeText(sdf.format(file.getCreatedAt()));
        }

        return vo;
    }

    private String getInspectionTypeText(String type) {
        Map<String, String> map = new HashMap<>();
        map.put("ROUTINE", "日常巡检");
        map.put("ACCEPTANCE", "节点验收");
        map.put("SPECIAL", "专项检查");
        return map.getOrDefault(type, type);
    }

    private String getCategoryText(String category) {
        Map<String, String> map = new HashMap<>();
        map.put("GENERAL", "一般问题");
        map.put("CRITICAL", "红线问题");
        map.put("URGENT", "紧急问题");
        map.put("OTHER", "其他问题");
        return map.getOrDefault(category, category);
    }

    private String getIssueStatusText(String status) {
        Map<String, String> map = new HashMap<>();
        map.put("OPEN", "未解决");
        map.put("IN_PROGRESS", "解决中");
        map.put("RESOLVED", "已解决");
        map.put("CLOSED", "已关闭");
        return map.getOrDefault(status, status);
    }

    private List<String> parseJsonArray(String json) {
        try {
            // 简单解析JSON数组
            if (json.startsWith("[") && json.endsWith("]")) {
                String content = json.substring(1, json.length() - 1);
                if (content.isEmpty()) {
                    return new ArrayList<>();
                }
                return Arrays.stream(content.split(","))
                        .map(s -> s.trim().replace("\"", ""))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("解析JSON数组失败: {}", json);
        }
        return new ArrayList<>();
    }
}
