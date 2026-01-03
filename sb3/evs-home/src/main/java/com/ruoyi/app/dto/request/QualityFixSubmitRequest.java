package com.ruoyi.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 整改提交请求DTO
 */
public class QualityFixSubmitRequest {

    /** 问题ID */
    @NotBlank(message = "问题ID不能为空")
    @Size(max = 64, message = "问题ID长度不能超过64个字符")
    private String issueId;

    /** 整改描述 */
    @NotBlank(message = "整改描述不能为空")
    @Size(max = 2000, message = "整改描述长度不能超过2000个字符")
    private String fixDescription;

    /** 整改图片（JSON数组） */
    @Size(max = 4000, message = "图片数据长度不能超过4000个字符")
    private String images;

    /** 状态 */
    @Size(max = 32, message = "状态长度不能超过32个字符")
    private String status;

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getFixDescription() {
        return fixDescription;
    }

    public void setFixDescription(String fixDescription) {
        this.fixDescription = fixDescription;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
