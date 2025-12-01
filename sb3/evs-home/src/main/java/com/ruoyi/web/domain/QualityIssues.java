package com.ruoyi.web.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 质量问题对象 quality_issues
 * 
 * @author evs
 * @date 2025-12-02
 */
public class QualityIssues extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 问题ID */
    private String id;

    /** 项目ID */
    private String projectId;

    /** 质检ID */
    private String qualityInspectionId;

    /** 问题标题 */
    @Excel(name = "问题标题")
    private String title;

    /** 问题描述 */
    private String description;

    /** 问题分类(GENERAL:一般问题、CRITICAL:红线问题、URGENT:紧急问题、OTHER:其他问题) */
    @Excel(name = "问题分类(GENERAL:一般问题、CRITICAL:红线问题、URGENT:紧急问题、OTHER:其他问题)")
    private String category;

    /** 问题位置 */
    @Excel(name = "问题位置")
    private String location;

    /** 问题图片JSON */
    private String images;

    /** 问题状态(OPEN:未解决、IN_PROGRESS:解决中、RESOLVED:已解决、CLOSED:已关闭) */
    @Excel(name = "问题状态(OPEN:未解决、IN_PROGRESS:解决中、RESOLVED:已解决、CLOSED:已关闭)")
    private String status;

    /** 解决时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "解决时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date resolvedAt;

    /** 整改期限 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "整改期限", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dueDate;

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

    public void setQualityInspectionId(String qualityInspectionId) 
    {
        this.qualityInspectionId = qualityInspectionId;
    }

    public String getQualityInspectionId() 
    {
        return qualityInspectionId;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setCategory(String category) 
    {
        this.category = category;
    }

    public String getCategory() 
    {
        return category;
    }

    public void setLocation(String location) 
    {
        this.location = location;
    }

    public String getLocation() 
    {
        return location;
    }

    public void setImages(String images) 
    {
        this.images = images;
    }

    public String getImages() 
    {
        return images;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setResolvedAt(Date resolvedAt) 
    {
        this.resolvedAt = resolvedAt;
    }

    public Date getResolvedAt() 
    {
        return resolvedAt;
    }

    public void setDueDate(Date dueDate) 
    {
        this.dueDate = dueDate;
    }

    public Date getDueDate() 
    {
        return dueDate;
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
            .append("qualityInspectionId", getQualityInspectionId())
            .append("title", getTitle())
            .append("description", getDescription())
            .append("category", getCategory())
            .append("location", getLocation())
            .append("images", getImages())
            .append("status", getStatus())
            .append("resolvedAt", getResolvedAt())
            .append("dueDate", getDueDate())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .append("createdBy", getCreatedBy())
            .append("updatedBy", getUpdatedBy())
            .toString();
    }
}
