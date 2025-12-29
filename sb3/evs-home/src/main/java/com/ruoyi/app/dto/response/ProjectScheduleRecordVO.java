package com.ruoyi.app.dto.response;

import java.util.Date;
import java.util.List;

/**
 * 项目进度验收记录VO
 */
public class ProjectScheduleRecordVO {

    /** 记录ID */
    private String id;

    /** 进度ID */
    private String scheduleId;

    /** 项目ID */
    private String projectId;

    /** 施工阶段代码 */
    private String stage;

    /** 阶段显示名称 */
    private String stageName;

    /** 记录标题 */
    private String title;

    /** 记录内容描述 */
    private String description;

    /** 记录类型：INSPECTION-验收, PROGRESS-进度, MATERIAL-材料, ISSUE-问题 */
    private String type;

    /** 类型显示文本 */
    private String typeText;

    /** 创建时间 */
    private Date createTime;

    /** 创建人ID */
    private String createBy;

    /** 创建人姓名 */
    private String createByName;

    /** 创建人角色：STAFF-员工, CUSTOMER-客户 */
    private String createByRole;

    /** 验收状态：PASS-通过, FAIL-不通过, PENDING-待验收 */
    private String inspectionStatus;

    /** 验收状态显示文本 */
    private String inspectionStatusText;

    /** 附件图片列表 */
    private List<String> images;

    /** 附件文件列表 */
    private List<AttachmentVO> attachments;

    /** 备注 */
    private String remark;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeText() {
        return typeText;
    }

    public void setTypeText(String typeText) {
        this.typeText = typeText;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getCreateByName() {
        return createByName;
    }

    public void setCreateByName(String createByName) {
        this.createByName = createByName;
    }

    public String getCreateByRole() {
        return createByRole;
    }

    public void setCreateByRole(String createByRole) {
        this.createByRole = createByRole;
    }

    public String getInspectionStatus() {
        return inspectionStatus;
    }

    public void setInspectionStatus(String inspectionStatus) {
        this.inspectionStatus = inspectionStatus;
    }

    public String getInspectionStatusText() {
        return inspectionStatusText;
    }

    public void setInspectionStatusText(String inspectionStatusText) {
        this.inspectionStatusText = inspectionStatusText;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<AttachmentVO> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentVO> attachments) {
        this.attachments = attachments;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 附件VO
     */
    public static class AttachmentVO {
        /** 文件名 */
        private String fileName;
        /** 文件URL */
        private String fileUrl;
        /** 文件大小 */
        private Long fileSize;
        /** 文件类型 */
        private String fileType;

        // Getters and Setters
        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public void setFileSize(Long fileSize) {
            this.fileSize = fileSize;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }
    }
}