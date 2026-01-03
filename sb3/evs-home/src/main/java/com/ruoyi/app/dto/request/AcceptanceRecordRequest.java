package com.ruoyi.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 验收记录新增请求DTO
 */
public class AcceptanceRecordRequest {

    /** 进度ID（关联project_schedules表） */
    @NotBlank(message = "进度ID不能为空")
    @Size(max = 64, message = "进度ID长度不能超过64个字符")
    private String scheduleId;

    /** 记录类型（默认ACCEPTANCE） */
    @Size(max = 32, message = "记录类型长度不能超过32个字符")
    @Pattern(regexp = "^[A-Z_]*$", message = "记录类型格式不正确")
    private String recordType;

    /** 验收标题 */
    @Size(max = 200, message = "验收标题长度不能超过200个字符")
    private String acceptanceTitle;

    /** 验收内容 */
    @Size(max = 2000, message = "验收内容长度不能超过2000个字符")
    private String acceptanceContent;

    /** 现场图片JSON数组格式 */
    @Size(max = 4000, message = "图片数据长度不能超过4000个字符")
    private String images;

    /** 验收结果（QUALIFIED:合格、UNQUALIFIED:不合格） */
    @Pattern(regexp = "^(QUALIFIED|UNQUALIFIED)?$", message = "验收结果格式不正确")
    private String acceptanceResult;

    /** 验收时间 */
    @Size(max = 32, message = "验收时间长度不能超过32个字符")
    private String acceptanceTime;

    /** 验收人 */
    @Size(max = 64, message = "验收人长度不能超过64个字符")
    private String acceptor;

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getAcceptanceTitle() {
        return acceptanceTitle;
    }

    public void setAcceptanceTitle(String acceptanceTitle) {
        this.acceptanceTitle = acceptanceTitle;
    }

    public String getAcceptanceContent() {
        return acceptanceContent;
    }

    public void setAcceptanceContent(String acceptanceContent) {
        this.acceptanceContent = acceptanceContent;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getAcceptanceResult() {
        return acceptanceResult;
    }

    public void setAcceptanceResult(String acceptanceResult) {
        this.acceptanceResult = acceptanceResult;
    }

    public String getAcceptanceTime() {
        return acceptanceTime;
    }

    public void setAcceptanceTime(String acceptanceTime) {
        this.acceptanceTime = acceptanceTime;
    }

    public String getAcceptor() {
        return acceptor;
    }

    public void setAcceptor(String acceptor) {
        this.acceptor = acceptor;
    }
}
