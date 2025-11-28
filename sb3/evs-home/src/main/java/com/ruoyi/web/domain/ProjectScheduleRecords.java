package com.ruoyi.web.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 进度记录对象 project_schedule_records
 * 
 * @author evs
 * @date 2025-11-27
 */
public class ProjectScheduleRecords extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private String id;

    /** 项目ID */
    @Excel(name = "项目ID")
    private String projectId;

    /** 进度ID（逻辑关联） */
    @Excel(name = "进度ID", readConverterExp = "逻=辑关联")
    private String scheduleId;

    /** 记录类型（START:开始、PROGRESS:进度更新、COMPLETE:完成、ISSUE:问题） */
    @Excel(name = "记录类型", readConverterExp = "S=TART:开始、PROGRESS:进度更新、COMPLETE:完成、ISSUE:问题")
    private String recordType;

    /** 完成度百分比 */
    @Excel(name = "完成度百分比")
    private BigDecimal completionRate;

    /** 记录描述 */
    @Excel(name = "记录描述")
    private String description;

    /** 现场图片JSON */
    @Excel(name = "现场图片JSON")
    private String images;

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

    public void setScheduleId(String scheduleId) 
    {
        this.scheduleId = scheduleId;
    }

    public String getScheduleId() 
    {
        return scheduleId;
    }

    public void setRecordType(String recordType) 
    {
        this.recordType = recordType;
    }

    public String getRecordType() 
    {
        return recordType;
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

    public void setImages(String images)
    {
        this.images = images;
    }

    public String getImages()
    {
        return images;
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
            .append("scheduleId", getScheduleId())
            .append("recordType", getRecordType())
            .append("completionRate", getCompletionRate())
            .append("description", getDescription())
            .append("images", getImages())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .append("createdBy", getCreatedBy())
            .append("updatedBy", getUpdatedBy())
            .toString();
    }
}
