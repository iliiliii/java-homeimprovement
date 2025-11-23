package com.ruoyi.web.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目信息对象 projects
 * 
 * @author evs
 * @date 2025-11-23
 */
public class Projects extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private String id;

    /** 项目类型（RESIDENTIAL:家装、COMMERCIAL:工装） */
    @Excel(name = "项目类型", readConverterExp = "R=ESIDENTIAL:家装、COMMERCIAL:工装")
    private String projectType;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String name;

    /** 客户ID */
    private String customerId;

    /** 项目描述 */
    private String description;

    /** 项目地址 */
    @Excel(name = "项目地址")
    private String address;

    /** 房屋面积（平米） */
    @Excel(name = "房屋面积", readConverterExp = "平=米")
    private BigDecimal area;

    /** 预算金额 */
    @Excel(name = "预算金额")
    private BigDecimal budget;

    /** 实际费用 */
    private BigDecimal actualCost;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 预计完工日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "预计完工日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 实际完工日期 */
    private Date actualEndDate;

    /** 项目状态 */
    @Excel(name = "项目状态")
    private String status;

    /** 优先级 */
    @Excel(name = "优先级")
    private String priority;

    /** 进度百分比 */
    private BigDecimal progress;

    /** 预算文件URL */
    private String budgetsUrl;

    /** 合同文件URL */
    private String contractsUrl;

    /** 创建时间 */
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

    /** 删除时间 */
    private Date deletedAt;

    /** 创建人 */
    private String createdBy;

    /** 更新人 */
    private String updatedBy;

    public void setId(String id) 
    {
        this.id = id;
    }

    public String getId() 
    {
        return id;
    }

    public void setProjectType(String projectType) 
    {
        this.projectType = projectType;
    }

    public String getProjectType() 
    {
        return projectType;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }

    public void setCustomerId(String customerId) 
    {
        this.customerId = customerId;
    }

    public String getCustomerId() 
    {
        return customerId;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setAddress(String address) 
    {
        this.address = address;
    }

    public String getAddress() 
    {
        return address;
    }

    public void setArea(BigDecimal area) 
    {
        this.area = area;
    }

    public BigDecimal getArea() 
    {
        return area;
    }

    public void setBudget(BigDecimal budget) 
    {
        this.budget = budget;
    }

    public BigDecimal getBudget() 
    {
        return budget;
    }

    public void setActualCost(BigDecimal actualCost) 
    {
        this.actualCost = actualCost;
    }

    public BigDecimal getActualCost() 
    {
        return actualCost;
    }

    public void setStartDate(Date startDate) 
    {
        this.startDate = startDate;
    }

    public Date getStartDate() 
    {
        return startDate;
    }

    public void setEndDate(Date endDate) 
    {
        this.endDate = endDate;
    }

    public Date getEndDate() 
    {
        return endDate;
    }

    public void setActualEndDate(Date actualEndDate) 
    {
        this.actualEndDate = actualEndDate;
    }

    public Date getActualEndDate() 
    {
        return actualEndDate;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setPriority(String priority) 
    {
        this.priority = priority;
    }

    public String getPriority() 
    {
        return priority;
    }

    public void setProgress(BigDecimal progress) 
    {
        this.progress = progress;
    }

    public BigDecimal getProgress() 
    {
        return progress;
    }

    public void setBudgetsUrl(String budgetsUrl) 
    {
        this.budgetsUrl = budgetsUrl;
    }

    public String getBudgetsUrl() 
    {
        return budgetsUrl;
    }

    public void setContractsUrl(String contractsUrl) 
    {
        this.contractsUrl = contractsUrl;
    }

    public String getContractsUrl() 
    {
        return contractsUrl;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    public void setUpdatedAt(Date updatedAt) 
    {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() 
    {
        return updatedAt;
    }

    public void setDeletedAt(Date deletedAt) 
    {
        this.deletedAt = deletedAt;
    }

    public Date getDeletedAt() 
    {
        return deletedAt;
    }

    public void setCreatedBy(String createdBy) 
    {
        this.createdBy = createdBy;
    }

    public String getCreatedBy() 
    {
        return createdBy;
    }

    public void setUpdatedBy(String updatedBy) 
    {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedBy() 
    {
        return updatedBy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("projectType", getProjectType())
            .append("name", getName())
            .append("customerId", getCustomerId())
            .append("description", getDescription())
            .append("address", getAddress())
            .append("area", getArea())
            .append("budget", getBudget())
            .append("actualCost", getActualCost())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("actualEndDate", getActualEndDate())
            .append("status", getStatus())
            .append("priority", getPriority())
            .append("progress", getProgress())
            .append("budgetsUrl", getBudgetsUrl())
            .append("contractsUrl", getContractsUrl())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .append("deletedAt", getDeletedAt())
            .append("createdBy", getCreatedBy())
            .append("updatedBy", getUpdatedBy())
            .toString();
    }
}
