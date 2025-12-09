package com.ruoyi.app.dto.response;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 客户项目卡片VO
 */
public class CustomerProjectVO {

    /** 项目ID */
    private String id;

    /** 项目名称 */
    private String name;

    /** 项目地址 */
    private String address;

    /** 面积 */
    private BigDecimal area;

    /** 项目状态：DESIGN-设计中, CONSTRUCTION-施工中, COMPLETED-已完工 */
    private String status;

    /** 状态显示文本 */
    private String statusText;

    /** 当前阶段 */
    private String currentStage;

    /** 当前阶段显示文本 */
    private String currentStageText;

    /** 总进度百分比 */
    private Integer progressPercent;

    /** 预计完工日期 */
    private Date endDate;

    /** 下一个里程碑 */
    private String nextMilestone;

    /** 卡片颜色类型：design-设计阶段(紫色), construction-施工阶段(蓝色), completed-已完工(绿色) */
    private String cardType;

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

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getNextMilestone() {
        return nextMilestone;
    }

    public void setNextMilestone(String nextMilestone) {
        this.nextMilestone = nextMilestone;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
}
