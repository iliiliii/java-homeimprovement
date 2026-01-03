package com.ruoyi.app.dto.response;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 项目进度VO
 */
public class ProjectScheduleVO {

    /** 进度ID */
    private String id;

    /** 项目ID */
    private String projectId;

    /** 阶段类型：DESIGN-设计阶段, CONSTRUCTION-施工阶段 */
    private String stageType;

    /** 施工阶段代码 */
    private String stage;

    /** 阶段显示名称 */
    private String stageName;

    /** 阶段顺序 */
    private Integer stageOrder;

    /** 计划开始日期 */
    private Date planStartDate;

    /** 计划结束日期 */
    private Date planEndDate;

    /** 实际开始日期 */
    private Date actualStartDate;

    /** 实际结束日期 */
    private Date actualEndDate;

    /** 状态：PENDING-待开始, IN_PROGRESS-进行中, COMPLETED-已完成 */
    private String status;

    /** 状态显示文本 */
    private String statusText;

    /** 完成度百分比 */
    private BigDecimal completionRate;

    /** 阶段描述 */
    private String description;

    /** 验收记录数量 */
    private Integer recordCount;

    /** 最新验收记录 */
    private List<ProjectScheduleRecordVO> latestRecords;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getStageType() {
        return stageType;
    }

    public void setStageType(String stageType) {
        this.stageType = stageType;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public Integer getStageOrder() {
        return stageOrder;
    }

    public void setStageOrder(Integer stageOrder) {
        this.stageOrder = stageOrder;
    }

    public Date getPlanStartDate() {
        return planStartDate;
    }

    public void setPlanStartDate(Date planStartDate) {
        this.planStartDate = planStartDate;
    }

    public Date getPlanEndDate() {
        return planEndDate;
    }

    public void setPlanEndDate(Date planEndDate) {
        this.planEndDate = planEndDate;
    }

    public Date getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(Date actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public Date getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(Date actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public BigDecimal getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(BigDecimal completionRate) {
        this.completionRate = completionRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public List<ProjectScheduleRecordVO> getLatestRecords() {
        return latestRecords;
    }

    public void setLatestRecords(List<ProjectScheduleRecordVO> latestRecords) {
        this.latestRecords = latestRecords;
    }
}