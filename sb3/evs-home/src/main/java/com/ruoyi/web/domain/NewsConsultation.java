package com.ruoyi.web.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 新闻咨询设置对象 news_consultation
 * 
 * @author evs
 * @date 2025-12-08
 */
public class NewsConsultation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 咨询ID */
    private String id;

    /** 标题 */
    @Excel(name = "标题")
    private String title;

    /** 副标题 */
    @Excel(name = "副标题")
    private String subtitle;

    /** 发布位置 */
    @Excel(name = "发布位置")
    private String publishPosition;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发布时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date publishTime;

    /** 发布状态（DRAFT:草稿, PUBLISHED:已发布） */
    @Excel(name = "发布状态", readConverterExp = "D=RAFT:草稿,,P=UBLISHED:已发布")
    private String publishStatus;

    /** 封面图片URL */
    @Excel(name = "封面图片URL")
    private String coverImage;

    /** 跳转地址 */
    @Excel(name = "跳转地址")
    private String jumpUrl;

    /** 排序（数值越大越靠前） */
    @Excel(name = "排序", readConverterExp = "数=值越大越靠前")
    private Long sortOrder;

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

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setSubtitle(String subtitle) 
    {
        this.subtitle = subtitle;
    }

    public String getSubtitle() 
    {
        return subtitle;
    }

    public void setPublishPosition(String publishPosition) 
    {
        this.publishPosition = publishPosition;
    }

    public String getPublishPosition() 
    {
        return publishPosition;
    }

    public void setPublishTime(Date publishTime) 
    {
        this.publishTime = publishTime;
    }

    public Date getPublishTime() 
    {
        return publishTime;
    }

    public void setPublishStatus(String publishStatus) 
    {
        this.publishStatus = publishStatus;
    }

    public String getPublishStatus() 
    {
        return publishStatus;
    }

    public void setCoverImage(String coverImage) 
    {
        this.coverImage = coverImage;
    }

    public String getCoverImage() 
    {
        return coverImage;
    }

    public void setJumpUrl(String jumpUrl) 
    {
        this.jumpUrl = jumpUrl;
    }

    public String getJumpUrl() 
    {
        return jumpUrl;
    }

    public void setSortOrder(Long sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Long getSortOrder() 
    {
        return sortOrder;
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
            .append("title", getTitle())
            .append("subtitle", getSubtitle())
            .append("publishPosition", getPublishPosition())
            .append("publishTime", getPublishTime())
            .append("publishStatus", getPublishStatus())
            .append("coverImage", getCoverImage())
            .append("jumpUrl", getJumpUrl())
            .append("sortOrder", getSortOrder())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .append("deletedAt", getDeletedAt())
            .append("createdBy", getCreatedBy())
            .append("updatedBy", getUpdatedBy())
            .toString();
    }
}
