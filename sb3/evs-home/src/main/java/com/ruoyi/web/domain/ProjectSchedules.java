package com.ruoyi.web.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目进度对象 project_schedules
 * 
 * @author evs
 * @date 2025-11-18
 */
public class ProjectSchedules extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 进度ID */
    private String id;

    /** 项目ID */
    @Excel(name = "项目ID")
    private String projectId;

    /** 施工阶段（DISMANTLING:拆除、WATER_ELECTRIC:水电、TILES:泥瓦、WOODWORK:木工、PAINTING:油漆、INSTALLATION:安装、SOFT_FURNISHING:软装、ACCEPTANCE:验收） */
    @Excel(name = "施工阶段", readConverterExp = "D=ISMANTLING:拆除、WATER_ELECTRIC:水电、TILES:泥瓦、WOODWORK:木工、PAINTING:油漆、INSTALLATION:安装、SOFT_FURNISHING:软装、ACCEPTANCE:验收")
    private String stage;

    /** 阶段顺序（1-8） */
    @Excel(name = "阶段顺序", readConverterExp = "1=-8")
    private Long stageOrder;

    /** 计划开始日期 */
    private Date planStartDate;

    /** 计划结束日期 */
    private Date planEndDate;

    /** 实际开始日期 */
    private Date actualStartDate;

    /** 实际结束日期 */
    private Date actualEndDate;

    /** 阶段状态 */
    @Excel(name = "阶段状态")
    private String status;

    /** 完成度百分比 */
    @Excel(name = "完成度百分比")
    private BigDecimal completionRate;

    /** 阶段描述 */
    private String description;

    /** 创建时间 */
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

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

    public void setProjectId(String projectId) 
    {
        this.projectId = projectId;
    }

    public String getProjectId() 
    {
        return projectId;
    }

    public void setStage(String stage) 
    {
        this.stage = stage;
    }

    public String getStage() 
    {
        return stage;
    }

    public void setStageOrder(Long stageOrder) 
    {
        this.stageOrder = stageOrder;
    }

    public Long getStageOrder() 
    {
        return stageOrder;
    }

    public void setPlanStartDate(Date planStartDate) 
    {
        this.planStartDate = planStartDate;
    }

    public Date getPlanStartDate() 
    {
        return planStartDate;
    }

    public void setPlanEndDate(Date planEndDate) 
    {
        this.planEndDate = planEndDate;
    }

    public Date getPlanEndDate() 
    {
        return planEndDate;
    }

    public void setActualStartDate(Date actualStartDate) 
    {
        this.actualStartDate = actualStartDate;
    }

    public Date getActualStartDate() 
    {
        return actualStartDate;
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

    public void setCompletionRate(BigDecimal completionRate) 
    {
        this.completionRate = completionRate;
    }

    public BigDecimal getCompletionRate() 
    {
        return completionRate;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
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
            .append("projectId", getProjectId())
            .append("stage", getStage())
            .append("stageOrder", getStageOrder())
            .append("planStartDate", getPlanStartDate())
            .append("planEndDate", getPlanEndDate())
            .append("actualStartDate", getActualStartDate())
            .append("actualEndDate", getActualEndDate())
            .append("status", getStatus())
            .append("completionRate", getCompletionRate())
            .append("description", getDescription())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .append("createdBy", getCreatedBy())
            .append("updatedBy", getUpdatedBy())
            .toString();
    }
}
