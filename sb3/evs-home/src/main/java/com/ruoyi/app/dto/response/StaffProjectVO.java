package com.ruoyi.app.dto.response;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 员工项目列表VO
 */
public class StaffProjectVO {

    /** 项目ID */
    private String id;

    /** 项目名称 */
    private String name;

    /** 客户名称 */
    private String customerName;

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

    /** 我的角色 */
    private String myRole;

    /** 角色显示文本 */
    private String myRoleText;

    /** 待处理问题数 */
    private int pendingIssueCount;

    /** 最近更新时间 */
    private Date lastUpdateTime;

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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getMyRole() {
        return myRole;
    }

    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    public String getMyRoleText() {
        return myRoleText;
    }

    public void setMyRoleText(String myRoleText) {
        this.myRoleText = myRoleText;
    }

    public int getPendingIssueCount() {
        return pendingIssueCount;
    }

    public void setPendingIssueCount(int pendingIssueCount) {
        this.pendingIssueCount = pendingIssueCount;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
