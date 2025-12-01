package com.ruoyi.web.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 问题修复对象 quality_fixes
 * 
 * @author evs
 * @date 2025-12-02
 */
public class QualityFixes extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 修复ID */
    private String id;

    /** 问题ID */
    @Excel(name = "问题ID")
    private String qualityIssuesId;

    /** 修复描述 */
    @Excel(name = "修复描述")
    private String fixDescription;

    /** 修复图片JSON */
    @Excel(name = "修复图片JSON")
    private String images;

    /** 修复状态(OPEN:未解决、IN_PROGRESS:解决中、RESOLVED:已解决) */
    @Excel(name = "修复状态(OPEN:未解决、IN_PROGRESS:解决中、RESOLVED:已解决)")
    private String status;

    /** 修复时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "修复时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date fixedAt;

    /** 验收时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "验收时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date verifiedAt;

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

    public void setQualityIssuesId(String qualityIssuesId) 
    {
        this.qualityIssuesId = qualityIssuesId;
    }

    public String getQualityIssuesId() 
    {
        return qualityIssuesId;
    }

    public void setFixDescription(String fixDescription) 
    {
        this.fixDescription = fixDescription;
    }

    public String getFixDescription() 
    {
        return fixDescription;
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

    public void setFixedAt(Date fixedAt) 
    {
        this.fixedAt = fixedAt;
    }

    public Date getFixedAt() 
    {
        return fixedAt;
    }

    public void setVerifiedAt(Date verifiedAt) 
    {
        this.verifiedAt = verifiedAt;
    }

    public Date getVerifiedAt() 
    {
        return verifiedAt;
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
            .append("qualityIssuesId", getQualityIssuesId())
            .append("fixDescription", getFixDescription())
            .append("images", getImages())
            .append("status", getStatus())
            .append("fixedAt", getFixedAt())
            .append("verifiedAt", getVerifiedAt())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .append("createdBy", getCreatedBy())
            .append("updatedBy", getUpdatedBy())
            .toString();
    }
}
