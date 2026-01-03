package com.ruoyi.app.service.impl;

import com.ruoyi.app.dto.request.AcceptanceRecordRequest;
import com.ruoyi.app.dto.response.ProjectScheduleVO;
import com.ruoyi.app.dto.response.ProjectScheduleRecordVO;
import com.ruoyi.app.mapper.AppProjectScheduleMapper;
import com.ruoyi.app.mapper.AppProjectMapper;
import com.ruoyi.app.mapper.AppDashboardMapper;
import com.ruoyi.app.security.AppTokenManager;
import com.ruoyi.app.service.IAppProjectScheduleService;
import com.ruoyi.app.util.InputSanitizer;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.domain.Projects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 小程序项目进度服务实现
 */
@Service
public class AppProjectScheduleServiceImpl implements IAppProjectScheduleService {

    private static final Logger log = LoggerFactory.getLogger(AppProjectScheduleServiceImpl.class);

    @Autowired
    private AppProjectScheduleMapper projectScheduleMapper;
    
    @Autowired
    private AppTokenManager tokenManager;

    @Autowired
    private AppProjectMapper projectMapper;

    @Autowired
    private AppDashboardMapper dashboardMapper;

    // 字典类型常量
    private static final String DICT_TYPE_CONSTRUCTION_STAGE = "decoration_construction_stage";
    private static final String DICT_TYPE_DESIGN_STAGE = "decoration_design_stage";
    private static final String DICT_TYPE_STAGE_STATUS = "decoration_construction_stage_status";
    private static final String DICT_TYPE_RECORD_TYPE = "decoration_record_type";

    /**
     * 从字典表获取标签
     */
    private String getDictLabel(String dictType, String dictValue) {
        if (StringUtils.isEmpty(dictValue)) {
            return dictValue;
        }
        try {
            String label = dashboardMapper.selectDictLabel(dictType, dictValue);
            log.debug("字典查询 - 类型: {}, 值: {}, 标签: {}", dictType, dictValue, label);
            return label != null ? label : dictValue;
        } catch (Exception e) {
            log.warn("字典查询失败 - 类型: {}, 值: {}, 错误: {}", dictType, dictValue, e.getMessage());
            return dictValue;
        }
    }

    @Override
    public List<ProjectScheduleVO> getProjectScheduleList(String token, String projectId) {
        // 使用与AppDashboardService相同的权限验证方式
        validateTokenAndAccess(token, projectId);

        try {
            // 查询项目进度列表
            List<ProjectScheduleVO> schedules = projectScheduleMapper.selectProjectScheduleList(projectId);
            
            // 为每个进度设置状态文本和阶段名称
            for (ProjectScheduleVO schedule : schedules) {
                // 设置阶段名称文本（根据阶段类型从对应字典表查询）
                schedule.setStageName(getStageText(schedule.getStage(), schedule.getStageType()));
                
                // 设置状态文本（从字典表查询）
                schedule.setStatusText(getStatusText(schedule.getStatus()));
                
                // 查询最新的验收记录（最多3条）
                List<ProjectScheduleRecordVO> latestRecords = projectScheduleMapper.selectLatestRecordsByScheduleId(
                        schedule.getId(), 3);
                
                // 为每条记录查询图片和设置文本
                for (ProjectScheduleRecordVO record : latestRecords) {
                    // 设置阶段名称
                    record.setStageName(getStageText(record.getStage()));
                    
                    // 设置记录类型文本
                    record.setTypeText(getTypeText(record.getType()));
                    
                    // 设置验收状态文本
                    record.setInspectionStatusText(getInspectionStatusText(record.getInspectionStatus()));
                    
                    // 查询图片列表
                    String imagesJson = projectScheduleMapper.selectRecordImages(record.getId());
                    List<String> images = parseImagesJson(imagesJson);
                    record.setImages(images);
                }
                
                schedule.setLatestRecords(latestRecords);
                
                // 设置验收记录数量
                Integer recordCount = projectScheduleMapper.countRecordsByScheduleId(schedule.getId());
                schedule.setRecordCount(recordCount != null ? recordCount : 0);
            }
            
            return schedules;
        } catch (Exception e) {
            log.error("查询项目进度列表失败", e);
            throw new ServiceException("查询项目进度列表失败: " + e.getMessage());
        }
    }

    @Override
    public TableDataInfo getProjectScheduleRecordList(String token, String projectId, String scheduleId, Integer page, Integer pageSize) {
        // 验证用户权限
        validateTokenAndAccess(token, projectId);

        try {
            // 设置分页参数
            int offset = (page - 1) * pageSize;
            
            // 查询验收记录列表
            List<ProjectScheduleRecordVO> records = projectScheduleMapper.selectProjectScheduleRecordList(
                    projectId, scheduleId, offset, pageSize);
            
            // 为每条记录设置类型文本和验收状态文本
            for (ProjectScheduleRecordVO record : records) {
                // 设置阶段名称文本
                record.setStageName(getStageText(record.getStage()));
                
                // 设置记录类型文本
                record.setTypeText(getTypeText(record.getType()));
                
                // 设置验收状态文本
                record.setInspectionStatusText(getInspectionStatusText(record.getInspectionStatus()));
                
                // 查询图片列表
                String imagesJson = projectScheduleMapper.selectRecordImages(record.getId());
                List<String> images = parseImagesJson(imagesJson);
                record.setImages(images);
            }
            
            // 查询总数
            Long total = projectScheduleMapper.countProjectScheduleRecords(projectId, scheduleId);
            
            // 构建分页结果
            TableDataInfo dataTable = new TableDataInfo();
            dataTable.setRows(records);
            dataTable.setTotal(total);
            
            return dataTable;
        } catch (Exception e) {
            log.error("查询项目验收记录列表失败", e);
            throw new ServiceException("查询项目验收记录列表失败: " + e.getMessage());
        }
    }

    @Override
    public ProjectScheduleRecordVO getProjectScheduleRecordDetail(String token, String recordId) {
        try {
            // 查询记录详情
            ProjectScheduleRecordVO record = projectScheduleMapper.selectProjectScheduleRecordById(recordId);
            if (record == null) {
                throw new ServiceException("验收记录不存在");
            }
            
            // 验证项目权限
            validateTokenAndAccess(token, record.getProjectId());
            
            // 设置文本显示
            // 设置阶段名称文本
            record.setStageName(getStageText(record.getStage()));
            
            // 设置记录类型文本
            record.setTypeText(getTypeText(record.getType()));
            
            // 设置验收状态文本
            record.setInspectionStatusText(getInspectionStatusText(record.getInspectionStatus()));
            
            // 查询图片列表
            String imagesJson = projectScheduleMapper.selectRecordImages(recordId);
            List<String> images = parseImagesJson(imagesJson);
            record.setImages(images);
            
            // 查询附件列表（当前版本暂无附件表，返回空列表）
            record.setAttachments(new ArrayList<>());
            
            return record;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询验收记录详情失败", e);
            throw new ServiceException("查询验收记录详情失败: " + e.getMessage());
        }
    }

    /**
     * 验证Token并检查项目访问权限
     */
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

    /**
     * 解析图片JSON字符串为URL列表
     */
    private List<String> parseImagesJson(String imagesJson) {
        List<String> images = new ArrayList<>();
        if (StringUtils.isEmpty(imagesJson)) {
            return images;
        }
        
        try {
            // 简单解析JSON数组格式：["img1.jpg","img2.jpg"]
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
     * 获取阶段显示文本（根据阶段类型从对应字典表查询，带兜底）
     */
    private String getStageText(String stage, String stageType) {
        if (StringUtils.isEmpty(stage)) {
            return "";
        }
        
        // 根据阶段类型选择字典
        String dictType = "DESIGN".equals(stageType) ? DICT_TYPE_DESIGN_STAGE : DICT_TYPE_CONSTRUCTION_STAGE;
        
        // 先尝试从字典表查询
        String dictLabel = getDictLabel(dictType, stage);
        
        // 如果字典查询返回原值（说明没找到），使用硬编码兜底
        if (dictLabel.equals(stage)) {
            // 设计阶段兜底
            if ("DESIGN".equals(stageType)) {
                switch (stage) {
                    case "0":
                        return "前案设计";
                    case "1":
                        return "平面图";
                    case "2":
                        return "效果图";
                    case "3":
                        return "施工图";
                    default:
                        return stage;
                }
            }
            // 施工阶段兜底
            switch (stage) {
                case "DISMANTLING":
                    return "拆除工程";
                case "WATER_ELECTRIC":
                    return "水电改造";
                case "TILES":
                    return "泥瓦工程";
                case "WOODWORK":
                    return "木工工程";
                case "PAINTING":
                    return "油漆工程";
                case "INSTALLATION":
                    return "安装工程";
                case "SOFT_FURNISHING":
                    return "软装工程";
                case "ACCEPTANCE":
                    return "验收工程";
                default:
                    return stage;
            }
        }
        
        return dictLabel;
    }

    /**
     * 获取阶段显示文本（从字典表查询，带兜底）- 兼容旧方法
     */
    private String getStageText(String stage) {
        return getStageText(stage, "CONSTRUCTION");
    }

    /**
     * 获取状态显示文本（从字典表查询，带兜底）
     */
    private String getStatusText(String status) {
        if (StringUtils.isEmpty(status)) {
            return "";
        }
        
        // 先尝试从字典表查询
        String dictLabel = getDictLabel(DICT_TYPE_STAGE_STATUS, status);
        
        // 如果字典查询返回原值（说明没找到），使用硬编码兜底
        if (dictLabel.equals(status)) {
            switch (status) {
                case "PENDING":
                    return "待开始";
                case "IN_PROGRESS":
                    return "进行中";
                case "COMPLETED":
                    return "已完成";
                default:
                    return status;
            }
        }
        
        return dictLabel;
    }

    /**
     * 获取记录类型显示文本（从字典表查询，带兜底）
     */
    private String getTypeText(String type) {
        if (StringUtils.isEmpty(type)) {
            return "";
        }
        
        // 先尝试从字典表查询
        String dictLabel = getDictLabel(DICT_TYPE_RECORD_TYPE, type);
        
        // 如果字典查询返回原值（说明没找到），使用硬编码兜底
        if (dictLabel.equals(type)) {
            switch (type) {
                case "START":
                    return "开始";
                case "PROGRESS":
                    return "进度更新";
                case "COMPLETE":
                    return "完成";
                case "ISSUE":
                    return "问题";
                case "ACCEPTANCE":
                    return "验收";
                default:
                    return type;
            }
        }
        
        return dictLabel;
    }

    /**
     * 获取验收状态显示文本
     */
    private String getInspectionStatusText(String status) {
        if (StringUtils.isEmpty(status)) {
            return "";
        }
        switch (status) {
            case "QUALIFIED":
                return "合格";
            case "UNQUALIFIED":
                return "不合格";
            default:
                return status;
        }
    }

    @Override
    public String addAcceptanceRecord(String token, String projectId, AcceptanceRecordRequest request) {
        // 验证Token并检查是否为员工用户
        Map<String, Object> claims = tokenManager.validateToken(extractToken(token));
        String userType = (String) claims.get("userType");
        String userId = claims.get("userId").toString();

        // 仅员工可以新增验收记录
        if (!"staff".equals(userType)) {
            throw new ServiceException("仅员工可以新增验收记录", 403);
        }

        // 验证员工是否有权限访问该项目
        boolean hasAccess = dashboardMapper.checkStaffProjectAccess(userId, projectId);
        if (!hasAccess) {
            throw new ServiceException("无权访问该项目", 403);
        }

        try {
            // 生成记录ID（格式：REC + 年月日 + 6位序列号）
            String recordId = generateRecordId();

            // 对文本字段进行安全清理
            String sanitizedTitle = InputSanitizer.validateAndSanitizeText(request.getAcceptanceTitle(), "验收标题");
            String sanitizedContent = InputSanitizer.validateAndSanitizeContent(request.getAcceptanceContent(), "验收内容");
            String sanitizedAcceptor = InputSanitizer.validateAndSanitizeText(request.getAcceptor(), "验收人");
            String sanitizedImages = InputSanitizer.sanitizeJson(request.getImages());
            
            // 验证图片JSON格式
            if (StringUtils.isNotEmpty(sanitizedImages) && !InputSanitizer.isValidJsonArray(sanitizedImages)) {
                throw new ServiceException("图片数据格式不正确");
            }

            // 解析验收时间
            Date acceptanceTime = null;
            if (StringUtils.isNotEmpty(request.getAcceptanceTime())) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    acceptanceTime = sdf.parse(request.getAcceptanceTime());
                } catch (Exception e) {
                    log.warn("解析验收时间失败: {}", request.getAcceptanceTime());
                    acceptanceTime = new Date();
                }
            } else {
                acceptanceTime = new Date();
            }

            // 插入验收记录
            int rows = projectScheduleMapper.insertAcceptanceRecord(
                    recordId,
                    projectId,
                    request.getScheduleId(),
                    StringUtils.isNotEmpty(request.getRecordType()) ? request.getRecordType() : "ACCEPTANCE",
                    sanitizedImages,
                    sanitizedTitle,
                    sanitizedContent,
                    request.getAcceptanceResult(),
                    acceptanceTime,
                    sanitizedAcceptor,
                    userId
            );

            if (rows <= 0) {
                throw new ServiceException("新增验收记录失败");
            }

            log.info("验收记录新增成功 - recordId: {}, projectId: {}, scheduleId: {}, userId: {}",
                    recordId, projectId, request.getScheduleId(), userId);

            return recordId;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("新增验收记录异常", e);
            throw new ServiceException("新增验收记录失败: " + e.getMessage());
        }
    }

    /**
     * 生成记录ID（格式：REC + 年月日 + 6位序列号）
     */
    private String generateRecordId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        // 生成6位随机序列号
        int seq = (int) (Math.random() * 900000) + 100000;
        return "REC" + dateStr + seq;
    }

    @Override
    public void updateAcceptanceRecord(String token, String projectId, String recordId, AcceptanceRecordRequest request) {
        // 验证Token并检查是否为员工用户
        Map<String, Object> claims = tokenManager.validateToken(extractToken(token));
        String userType = (String) claims.get("userType");
        String userId = claims.get("userId").toString();

        // 仅员工可以编辑验收记录
        if (!"staff".equals(userType)) {
            throw new ServiceException("仅员工可以编辑验收记录", 403);
        }

        // 查询记录是否存在
        ProjectScheduleRecordVO existingRecord = projectScheduleMapper.selectProjectScheduleRecordById(recordId);
        if (existingRecord == null) {
            throw new ServiceException("验收记录不存在", 404);
        }

        // 验证是否为记录创建者
        String createBy = projectScheduleMapper.selectRecordCreateBy(recordId);
        if (!userId.equals(createBy)) {
            throw new ServiceException("只能编辑自己创建的记录", 403);
        }

        // 验证员工是否有权限访问该项目
        boolean hasAccess = dashboardMapper.checkStaffProjectAccess(userId, projectId);
        if (!hasAccess) {
            throw new ServiceException("无权访问该项目", 403);
        }

        try {
            // 对文本字段进行安全清理
            String sanitizedTitle = InputSanitizer.validateAndSanitizeText(request.getAcceptanceTitle(), "验收标题");
            String sanitizedContent = InputSanitizer.validateAndSanitizeContent(request.getAcceptanceContent(), "验收内容");
            String sanitizedAcceptor = InputSanitizer.validateAndSanitizeText(request.getAcceptor(), "验收人");
            String sanitizedImages = InputSanitizer.sanitizeJson(request.getImages());
            
            // 验证图片JSON格式
            if (StringUtils.isNotEmpty(sanitizedImages) && !InputSanitizer.isValidJsonArray(sanitizedImages)) {
                throw new ServiceException("图片数据格式不正确");
            }

            // 解析验收时间
            Date acceptanceTime = null;
            if (StringUtils.isNotEmpty(request.getAcceptanceTime())) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    acceptanceTime = sdf.parse(request.getAcceptanceTime());
                } catch (Exception e) {
                    log.warn("解析验收时间失败: {}", request.getAcceptanceTime());
                    acceptanceTime = new Date();
                }
            }

            // 更新验收记录
            int rows = projectScheduleMapper.updateAcceptanceRecord(
                    recordId,
                    sanitizedImages,
                    sanitizedTitle,
                    sanitizedContent,
                    request.getAcceptanceResult(),
                    acceptanceTime,
                    sanitizedAcceptor,
                    userId
            );

            if (rows <= 0) {
                throw new ServiceException("编辑验收记录失败");
            }

            log.info("验收记录编辑成功 - recordId: {}, userId: {}", recordId, userId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("编辑验收记录异常", e);
            throw new ServiceException("编辑验收记录失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteAcceptanceRecord(String token, String projectId, String recordId) {
        // 验证Token并检查是否为员工用户
        Map<String, Object> claims = tokenManager.validateToken(extractToken(token));
        String userType = (String) claims.get("userType");
        String userId = claims.get("userId").toString();

        // 仅员工可以删除验收记录
        if (!"staff".equals(userType)) {
            throw new ServiceException("仅员工可以删除验收记录", 403);
        }

        // 查询记录是否存在
        ProjectScheduleRecordVO existingRecord = projectScheduleMapper.selectProjectScheduleRecordById(recordId);
        if (existingRecord == null) {
            throw new ServiceException("验收记录不存在", 404);
        }

        // 验证是否为记录创建者
        String createBy = projectScheduleMapper.selectRecordCreateBy(recordId);
        if (!userId.equals(createBy)) {
            throw new ServiceException("只能删除自己创建的记录", 403);
        }

        // 验证员工是否有权限访问该项目
        boolean hasAccess = dashboardMapper.checkStaffProjectAccess(userId, projectId);
        if (!hasAccess) {
            throw new ServiceException("无权访问该项目", 403);
        }

        try {
            // 删除验收记录
            int rows = projectScheduleMapper.deleteAcceptanceRecord(recordId);

            if (rows <= 0) {
                throw new ServiceException("删除验收记录失败");
            }

            log.info("验收记录删除成功 - recordId: {}, userId: {}", recordId, userId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除验收记录异常", e);
            throw new ServiceException("删除验收记录失败: " + e.getMessage());
        }
    }
}