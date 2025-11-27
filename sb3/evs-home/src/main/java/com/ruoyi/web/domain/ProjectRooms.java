package com.ruoyi.web.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目房间对象 project_rooms
 * 
 * @author evs
 * @date 2025-11-27
 */
public class ProjectRooms extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 房间ID */
    private String id;

    /** 项目ID */
    @Excel(name = "项目ID")
    private String projectId;

    /** 房间名称 */
    @Excel(name = "房间名称")
    private String roomName;

    /** 房间类型（客厅、卧室、厨房、卫生间、书房、餐厅、阳台、儿童房、老人房、衣帽间、储物间、其他） */
    @Excel(name = "房间类型", readConverterExp = "客=厅、卧室、厨房、卫生间、书房、餐厅、阳台、儿童房、老人房、衣帽间、储物间、其他")
    private String roomType;

    /** 房间面积（平米） */
    @Excel(name = "房间面积", readConverterExp = "平=米")
    private BigDecimal area;

    /** 房间描述 */
    @Excel(name = "房间描述")
    private String description;

    /** 楼层信息 */
    @Excel(name = "楼层信息")
    private String floor;

    /** 朝向（N:北、S:南、E:东、W:西、NE:东北、NW:西北、SE:东南、SW:西南） */
    @Excel(name = "朝向", readConverterExp = "N=:北、S:南、E:东、W:西、NE:东北、NW:西北、SE:东南、SW:西南")
    private String orientation;

    /** 关联文件ID数组（JSON格式：["f1", "f2", "f3"]） */
    private String fileIds;

    /** 设计稿URL数组（JSON格式：["url1", "url2", "url3"]） */
    private String designUrls;

    /** 施工图URL数组（JSON格式） */
    private String constructionUrls;

    /** 效果图URL数组（JSON格式） */
    private String effectUrls;

    /** 其他文件URL数组（JSON格式） */
    private String otherUrls;

    /** 排序（数字越小排序越靠前） */
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

    public void setProjectId(String projectId) 
    {
        this.projectId = projectId;
    }

    public String getProjectId() 
    {
        return projectId;
    }

    public void setRoomName(String roomName) 
    {
        this.roomName = roomName;
    }

    public String getRoomName() 
    {
        return roomName;
    }

    public void setRoomType(String roomType) 
    {
        this.roomType = roomType;
    }

    public String getRoomType() 
    {
        return roomType;
    }

    public void setArea(BigDecimal area) 
    {
        this.area = area;
    }

    public BigDecimal getArea() 
    {
        return area;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setFloor(String floor) 
    {
        this.floor = floor;
    }

    public String getFloor() 
    {
        return floor;
    }

    public void setOrientation(String orientation) 
    {
        this.orientation = orientation;
    }

    public String getOrientation() 
    {
        return orientation;
    }

    public void setFileIds(String fileIds) 
    {
        this.fileIds = fileIds;
    }

    public String getFileIds() 
    {
        return fileIds;
    }

    public void setDesignUrls(String designUrls) 
    {
        this.designUrls = designUrls;
    }

    public String getDesignUrls() 
    {
        return designUrls;
    }

    public void setConstructionUrls(String constructionUrls) 
    {
        this.constructionUrls = constructionUrls;
    }

    public String getConstructionUrls() 
    {
        return constructionUrls;
    }

    public void setEffectUrls(String effectUrls) 
    {
        this.effectUrls = effectUrls;
    }

    public String getEffectUrls() 
    {
        return effectUrls;
    }

    public void setOtherUrls(String otherUrls) 
    {
        this.otherUrls = otherUrls;
    }

    public String getOtherUrls() 
    {
        return otherUrls;
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
            .append("projectId", getProjectId())
            .append("roomName", getRoomName())
            .append("roomType", getRoomType())
            .append("area", getArea())
            .append("description", getDescription())
            .append("floor", getFloor())
            .append("orientation", getOrientation())
            .append("fileIds", getFileIds())
            .append("designUrls", getDesignUrls())
            .append("constructionUrls", getConstructionUrls())
            .append("effectUrls", getEffectUrls())
            .append("otherUrls", getOtherUrls())
            .append("sortOrder", getSortOrder())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .append("deletedAt", getDeletedAt())
            .append("createdBy", getCreatedBy())
            .append("updatedBy", getUpdatedBy())
            .toString();
    }
}
