package com.ruoyi.app.dto.response;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 进度VO
 */
public class ScheduleVO {

    /** 进度ID */
    private String id;

    /** 施工阶段 */
    private String stage;

    /** 阶段显示文本 */
    private String stageText;

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

    /** 描述 */
    private String description;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStageText() {
        return stageText;
    }

    public void setStageText(String stageText) {
        this.stageText = stageText;
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
}
