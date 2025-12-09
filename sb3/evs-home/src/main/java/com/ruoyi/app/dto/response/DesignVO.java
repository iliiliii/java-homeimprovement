package com.ruoyi.app.dto.response;

import java.util.Date;

/**
 * 设计图VO
 */
public class DesignVO {

    /** 文件ID */
    private String id;

    /** 文件名称 */
    private String name;

    /** 空间类型：living-客厅, bedroom-卧室, kitchen-厨房, bathroom-卫生间, balcony-阳台 */
    private String spaceType;

    /** 空间类型显示文本 */
    private String spaceTypeText;

    /** 版本号 */
    private String version;

    /** 图片URL */
    private String imageUrl;

    /** 缩略图URL */
    private String thumbnailUrl;

    /** 状态：draft-草稿, reviewing-待确认, confirmed-已确认 */
    private String status;

    /** 状态显示文本 */
    private String statusText;

    /** 更新时间 */
    private Date updateTime;

    /** 更新时间显示文本 */
    private String updateTimeText;

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

    public String getSpaceType() {
        return spaceType;
    }

    public void setSpaceType(String spaceType) {
        this.spaceType = spaceType;
    }

    public String getSpaceTypeText() {
        return spaceTypeText;
    }

    public void setSpaceTypeText(String spaceTypeText) {
        this.spaceTypeText = spaceTypeText;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateTimeText() {
        return updateTimeText;
    }

    public void setUpdateTimeText(String updateTimeText) {
        this.updateTimeText = updateTimeText;
    }
}
