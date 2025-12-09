package com.ruoyi.app.dto.response;

import java.util.Date;
import java.util.List;

/**
 * 质量问题VO
 */
public class IssueVO {

    /** 问题ID */
    private String id;

    /** 问题标题 */
    private String title;

    /** 问题描述 */
    private String description;

    /** 问题分类：GENERAL-一般问题, CRITICAL-红线问题, URGENT-紧急问题 */
    private String category;

    /** 分类显示文本 */
    private String categoryText;

    /** 问题位置 */
    private String location;

    /** 问题图片列表 */
    private List<String> images;

    /** 问题状态：OPEN-未解决, IN_PROGRESS-解决中, RESOLVED-已解决, CLOSED-已关闭 */
    private String status;

    /** 状态显示文本 */
    private String statusText;

    /** 整改期限 */
    private Date dueDate;

    /** 解决时间 */
    private Date resolvedAt;

    /** 创建时间 */
    private Date createdAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryText() {
        return categoryText;
    }

    public void setCategoryText(String categoryText) {
        this.categoryText = categoryText;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
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

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
