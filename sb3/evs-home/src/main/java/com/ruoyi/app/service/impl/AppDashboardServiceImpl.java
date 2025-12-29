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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 小程序首页数据服务实现
 */
@Service
public class AppDashboardServiceImpl implements IAppDashboardService {

    private static final Logger log = LoggerFactory.getLogger(AppDashboardServiceImpl.class);

    @Value("${ruoyi.profile:}")
    private String uploadPath;

    @Autowired
    private AppTokenManager tokenManager;

    @Autowired
    private AppProjectMapper projectMapper;

    @Autowired
    private AppDashboardMapper dashboardMapper;

    // 字典类型常量
    private static final String DICT_TYPE_PROJECT_STATUS = "decoration_project_status";
    private static final String DICT_TYPE_CONSTRUCTION_STAGE = "decoration_construction_stage";
    private static final String DICT_TYPE_STAGE_STATUS = "decoration_construction_stage_status";
    private static final String DICT_TYPE_POST_ROLES = "decoration_post_roles";
    private static final String DICT_TYPE_ROOM_TYPE = "decoration_room_type";
    private static final String DICT_TYPE_ORIENTATION = "decoration_orientation";

    // 字典缓存（使用ConcurrentHashMap保证线程安全）
    // key: dictType:dictValue, value: dictLabel
    private final Map<String, String> dictCache = new ConcurrentHashMap<>();
    
    // 缓存过期时间（毫秒），默认5分钟
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000;
    private volatile long lastCacheRefreshTime = 0;

    /**
     * 从字典表获取标签（带缓存）
     * @param dictType 字典类型
     * @param dictValue 字典值
     * @return 字典标签，如果未找到则返回原值
     */
    private String getDictLabel(String dictType, String dictValue) {
        if (dictValue == null || dictValue.isEmpty()) {
            return dictValue;
        }
        
        // 检查缓存是否过期
        long now = System.currentTimeMillis();
        if (now - lastCacheRefreshTime > CACHE_EXPIRE_TIME) {
            dictCache.clear();
            lastCacheRefreshTime = now;
        }
        
        String cacheKey = dictType + ":" + dictValue;
        return dictCache.computeIfAbsent(cacheKey, k -> {
            String label = dashboardMapper.selectDictLabel(dictType, dictValue);
            return label != null ? label : dictValue;
        });
    }

    /**
     * 获取阶段名称文本
     */
    private String getStageText(String stage) {
        return getDictLabel(DICT_TYPE_CONSTRUCTION_STAGE, stage);
    }

    /**
     * 获取进度状态文本
     */
    private String getScheduleStatusText(String status) {
        return getDictLabel(DICT_TYPE_STAGE_STATUS, status);
    }

    /**
     * 获取角色名称文本
     */
    private String getRoleText(String role) {
        return getDictLabel(DICT_TYPE_POST_ROLES, role);
    }

    /**
     * 获取房间类型文本
     */
    private String getRoomTypeText(String roomType) {
        return getDictLabel(DICT_TYPE_ROOM_TYPE, roomType);
    }

    /**
     * 获取朝向文本
     */
    private String getOrientationText(String orientation) {
        return getDictLabel(DICT_TYPE_ORIENTATION, orientation);
    }

    /**
     * 清除字典缓存（可在字典更新时调用）
     */
    public void clearDictCache() {
        dictCache.clear();
        lastCacheRefreshTime = 0;
        log.info("字典缓存已清除");
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

        // 获取客户关联的项目列表
        List<CustomerProjectVO> projectVOs = dashboardMapper.selectCustomerProjects(userId);
        
        // 补充项目详细信息
        for (CustomerProjectVO vo : projectVOs) {
            // 从字典表获取状态文本
            vo.setStatusText(getProjectStatusText(vo.getStatus()));
            
            // 获取当前阶段
            ProjectSchedules currentSchedule = dashboardMapper.selectCurrentSchedule(vo.getId());
            if (currentSchedule != null) {
                vo.setCurrentStage(currentSchedule.getStage());
                vo.setCurrentStageText(getStageText(currentSchedule.getStage()));
            } else {
                vo.setCurrentStage("DESIGN");
                vo.setCurrentStageText(getStageText("设计阶段"));
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
        String userId = claims.get("userId").toString();

        if (!"staff".equals(userType)) {
            throw new ServiceException("非员工用户无法访问此接口");
        }

        StaffDashboardVO result = new StaffDashboardVO();
        // 不再返回用户信息，前端使用登录时缓存的数据

        // 获取员工关联的项目列表
        List<StaffProjectVO> projectVOs = dashboardMapper.selectStaffProjectsWithDict(userId);
        
        // 补充项目详细信息
        for (StaffProjectVO vo : projectVOs) {
            // 从字典表获取状态文本
            vo.setStatusText(getProjectStatusText(vo.getStatus()));
            
            // 获取当前阶段
            ProjectSchedules currentSchedule = dashboardMapper.selectCurrentSchedule(vo.getId());
            if (currentSchedule != null) {
                vo.setCurrentStage(currentSchedule.getStage());
                vo.setCurrentStageText(getStageText(currentSchedule.getStage()));
            }
            
            // 获取待处理问题数
            int issueCount = dashboardMapper.countPendingIssues(vo.getId());
            vo.setPendingIssueCount(issueCount);
            
            // 设置角色文本
            vo.setMyRoleText(getRoleText(vo.getMyRole()));
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
        result.setStatusText(getProjectStatusText(project.getStatus()));
        result.setStartDate(project.getStartDate());
        result.setEndDate(project.getEndDate());
        result.setBudget(project.getBudget());
        result.setActualCost(project.getActualCost());

        // 获取当前阶段
        ProjectSchedules currentSchedule = dashboardMapper.selectCurrentSchedule(projectId);
        if (currentSchedule != null) {
            result.setCurrentStage(currentSchedule.getStage());
            result.setCurrentStageText(getStageText(currentSchedule.getStage()));
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
            vo.setStageText(getStageText(s.getStage()));
            vo.setStageOrder(s.getStageOrder() != null ? s.getStageOrder().intValue() : 0);
            vo.setPlanStartDate(s.getPlanStartDate());
            vo.setPlanEndDate(s.getPlanEndDate());
            vo.setActualStartDate(s.getActualStartDate());
            vo.setActualEndDate(s.getActualEndDate());
            vo.setStatus(s.getStatus());
            vo.setStatusText(getScheduleStatusText(s.getStatus()));
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

    /**
     * 从字典表获取项目状态文本
     */
    private String getProjectStatusText(String status) {
        if (status == null || status.isEmpty()) {
            return status;
        }
        String label = dashboardMapper.selectDictLabel(DICT_TYPE_PROJECT_STATUS, status);
        return label != null ? label : status;
    }

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
            boolean hasAccess = dashboardMapper.checkStaffProjectAccess(userId, projectId);
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
        String status = project.getStatus();
        vo.setStatus(status);
        vo.setEndDate(project.getEndDate());

        // 从字典表获取状态文本
        vo.setStatusText(getProjectStatusText(status));
        
        // 设置卡片类型
        if ("DESIGN".equals(status)) {
            vo.setCardType("design");
        } else if ("COMPLETED".equals(status)) {
            vo.setCardType("completed");
        } else {
            vo.setCardType("construction");
        }

        // 获取当前阶段
        ProjectSchedules currentSchedule = dashboardMapper.selectCurrentSchedule(project.getId());
        if (currentSchedule != null) {
            vo.setCurrentStage(currentSchedule.getStage());
            vo.setCurrentStageText(getStageText(currentSchedule.getStage()));
        } else {
            vo.setCurrentStage("DESIGN");
            vo.setCurrentStageText(getStageText("DESIGN"));
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

    @Override
    public List<RoomVO> getProjectRooms(String token, String projectId) {
        validateTokenAndAccess(token, projectId);

        List<Map<String, Object>> rooms = dashboardMapper.selectProjectRooms(projectId);
        List<RoomVO> result = new ArrayList<>();

        for (Map<String, Object> room : rooms) {
            RoomVO vo = new RoomVO();
            vo.setId((String) room.get("id"));
            vo.setRoomName((String) room.get("roomName"));
            
            String roomType = (String) room.get("roomType");
            vo.setRoomType(roomType);
            vo.setRoomTypeText(getRoomTypeText(roomType));
            
            Object areaObj = room.get("area");
            if (areaObj != null) {
                vo.setArea(new BigDecimal(areaObj.toString()));
            }
            
            vo.setDescription((String) room.get("description"));
            vo.setFloor((String) room.get("floor"));
            
            // 设置朝向
            String orientation = (String) room.get("orientation");
            vo.setOrientation(orientation);
            vo.setOrientationText(getOrientationText(orientation));

            // 解析fileIds获取图片列表
            String fileIds = (String) room.get("fileIds");
            List<String> images = parseFileIdsToUrls(fileIds);
            vo.setImages(images);
            vo.setImageCount(images.size());

            result.add(vo);
        }

        return result;
    }

    /**
     * 解析fileIds JSON字符串为完整URL列表
     */
    private List<String> parseFileIdsToUrls(String fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> paths = parseJsonArray(fileIds);
        List<String> urls = new ArrayList<>();

        for (String path : paths) {
            if (path == null || path.isEmpty()) {
                continue;
            }
            // 如果已经是完整URL，直接使用
            if (path.startsWith("http://") || path.startsWith("https://")) {
                urls.add(path);
            } else {
                // 相对路径，需要拼接前缀
                // 路径格式通常是 profile/upload/xxx.jpg
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                urls.add(path);
            }
        }

        return urls;
    }

    @Override
    public List<ContractAmountVO> getProjectContractAmounts(String token, String projectId) {
        validateTokenAndAccess(token, projectId);

        // 获取字典数据（六个固定分类）
        List<Map<String, Object>> dictDataList = dashboardMapper.selectContractAmountDictData();
        
        // 获取项目的合同金额数据
        List<Map<String, Object>> contractAmounts = dashboardMapper.selectProjectContractAmounts(projectId);
        
        // 将合同金额数据转换为Map，方便查找
        Map<String, Map<String, Object>> amountMap = new HashMap<>();
        for (Map<String, Object> amount : contractAmounts) {
            String category = (String) amount.get("category");
            if (category != null) {
                amountMap.put(category, amount);
            }
        }
        
        // 构建结果列表，确保六个分类都有数据
        List<ContractAmountVO> result = new ArrayList<>();
        for (Map<String, Object> dict : dictDataList) {
            String dictValue = (String) dict.get("dictValue");
            String dictLabel = (String) dict.get("dictLabel");
            
            ContractAmountVO vo = new ContractAmountVO();
            vo.setCategory(dictValue);
            vo.setLabel(dictLabel);
            
            // 查找对应的金额数据
            Map<String, Object> amountData = amountMap.get(dictValue);
            if (amountData != null) {
                // 解析金额（contents字段存储金额）
                Object amountObj = amountData.get("amount");
                if (amountObj != null) {
                    try {
                        vo.setAmount(new BigDecimal(amountObj.toString()));
                    } catch (NumberFormatException e) {
                        vo.setAmount(BigDecimal.ZERO);
                    }
                } else {
                    vo.setAmount(BigDecimal.ZERO);
                }
                // 设置URL
                vo.setUrl((String) amountData.get("url"));
            } else {
                // 没有数据时，金额为0
                vo.setAmount(BigDecimal.ZERO);
                vo.setUrl(null);
            }
            
            result.add(vo);
        }
        
        return result;
    }
}
