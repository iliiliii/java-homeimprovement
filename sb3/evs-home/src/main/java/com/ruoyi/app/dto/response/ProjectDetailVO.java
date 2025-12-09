package com.ruoyi.app.dto.response;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 项目详情VO
 */
public class ProjectDetailVO {

    /** 项目ID */
    private String id;

    /** 项目名称 */
    private String name;

    /** 项目地址 */
    private String address;

    /** 面积 */
    private BigDecimal area;

    /** 项目状态 */
    private String status;

    /** 状态显示文本 */
    private String statusText;

    /** 当前阶段 */
    private String currentStage;

    /** 当前阶段显示文本 */
    private String currentStageText;

    /** 总进度百分比 */
    private Integer progressPercent;

    /** 开始日期 */
    private Date startDate;

    /** 预计完工日期 */
    private Date endDate;

    /** 预算金额 */
    private BigDecimal budget;

    /** 实际费用 */
    private BigDecimal actualCost;

    /** 进度列表 */
    private List<ScheduleVO> schedules;

    /** 最近质检记录 */
    private List<InspectionVO> recentInspections;

    /** 待处理问题数 */
    private int pendingIssueCount;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
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

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public String getCurrentStageText() {
        return currentStageText;
    }

    public void setCurrentStageText(String currentStageText) {
        this.currentStageText = currentStageText;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
    }

    public List<ScheduleVO> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleVO> schedules) {
        this.schedules = schedules;
    }

    public List<InspectionVO> getRecentInspections() {
        return recentInspections;
    }

    public void setRecentInspections(List<InspectionVO> recentInspections) {
        this.recentInspections = recentInspections;
    }

    public int getPendingIssueCount() {
        return pendingIssueCount;
    }

    public void setPendingIssueCount(int pendingIssueCount) {
        this.pendingIssueCount = pendingIssueCount;
    }
}
