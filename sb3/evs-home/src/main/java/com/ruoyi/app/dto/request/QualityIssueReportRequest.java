package com.ruoyi.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 小程序问题上报请求DTO
 */
public class QualityIssueReportRequest {

    /** 问题标题 */
    @NotBlank(message = "问题标题不能为空")
    @Size(max = 200, message = "问题标题长度不能超过200个字符")
    private String title;

    /** 问题描述 */
    @NotBlank(message = "问题描述不能为空")
    @Size(max = 2000, message = "问题描述长度不能超过2000个字符")
    private String description;

    /** 质检类型（施工阶段） */
    @Size(max = 64, message = "质检类型长度不能超过64个字符")
    private String inspectionType;

    /** 问题分类 */
    @Size(max = 64, message = "问题分类长度不能超过64个字符")
    private String category;

    /** 问题位置 */
    @Size(max = 200, message = "问题位置长度不能超过200个字符")
    private String location;

    /** 整改期限 */
    @Size(max = 32, message = "整改期限长度不能超过32个字符")
    private String dueDate;

    /** 现场照片（JSON数组） */
    @Size(max = 4000, message = "图片数据长度不能超过4000个字符")
    private String images;

    /** 检查日期 */
    @Size(max = 32, message = "检查日期长度不能超过32个字符")
    private String inspectionDate;

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

    public String getInspectionType() {
        return inspectionType;
    }

    public void setInspectionType(String inspectionType) {
        this.inspectionType = inspectionType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(String inspectionDate) {
        this.inspectionDate = inspectionDate;
    }
}
