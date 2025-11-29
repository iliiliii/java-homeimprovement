package com.ruoyi.web.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 进度记录对象 project_schedule_records
 * 
 * @author evs
 * @date 2025-11-29
 */
public class ProjectScheduleRecords extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID（格式：REC + 年月日 + 6位序列号） */
    private String id;

    /** 项目ID（关联projects表） */
    private String projectId;

    /** 进度ID（关联project_schedules表） */
    private String scheduleId;

    /** 记录类型（START:开始、PROGRESS:进度更新、COMPLETE:完成、ISSUE:问题、ACCEPTANCE:验收） */
    @Excel(name = "记录类型", readConverterExp = "S=TART:开始、PROGRESS:进度更新、COMPLETE:完成、ISSUE:问题、ACCEPTANCE:验收")
    private String recordType;

    /** 现场图片JSON数组格式（如：["img1.jpg","img2.jpg"]） */
    private String images;

    /** 验收标题 */
    @Excel(name = "验收标题")
    private String acceptanceTitle;

    /** 验收内容 */
    private String acceptanceContent;

    /** 验收结果（QUALIFIED:合格、UNQUALIFIED:不合格） */
    @Excel(name = "验收结果", readConverterExp = "Q=UALIFIED:合格、UNQUALIFIED:不合格")
    private String acceptanceResult;

    /** 验收时间（实际验收发生时间） */
    @Excel(name = "验收时间", readConverterExp = "实=际验收发生时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 与BaseEntity保持一致，不设置timezone
    private Date acceptanceTime;

    /** 验收人（实际验收人员姓名） */
    @Excel(name = "验收人", readConverterExp = "实=际验收人员姓名")
    private String acceptor;

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

    public void setScheduleId(String scheduleId) 
    {
        this.scheduleId = scheduleId;
    }

    public String getScheduleId() 
    {
        return scheduleId;
    }

    public void setRecordType(String recordType) 
    {
        this.recordType = recordType;
    }

    public String getRecordType() 
    {
        return recordType;
    }

    public void setImages(String images) 
    {
        this.images = images;
    }

    public String getImages() 
    {
        return images;
    }

    public void setAcceptanceTitle(String acceptanceTitle) 
    {
        this.acceptanceTitle = acceptanceTitle;
    }

    public String getAcceptanceTitle() 
    {
        return acceptanceTitle;
    }

    public void setAcceptanceContent(String acceptanceContent) 
    {
        this.acceptanceContent = acceptanceContent;
    }

    public String getAcceptanceContent() 
    {
        return acceptanceContent;
    }

    public void setAcceptanceResult(String acceptanceResult) 
    {
        this.acceptanceResult = acceptanceResult;
    }

    public String getAcceptanceResult() 
    {
        return acceptanceResult;
    }

    public void setAcceptanceTime(Date acceptanceTime) 
    {
        this.acceptanceTime = acceptanceTime;
    }

    public Date getAcceptanceTime() 
    {
        return acceptanceTime;
    }

    public void setAcceptor(String acceptor) 
    {
        this.acceptor = acceptor;
    }

    public String getAcceptor()
    {
        return acceptor;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("projectId", getProjectId())
            .append("scheduleId", getScheduleId())
            .append("recordType", getRecordType())
            .append("images", getImages())
            .append("acceptanceTitle", getAcceptanceTitle())
            .append("acceptanceContent", getAcceptanceContent())
            .append("acceptanceResult", getAcceptanceResult())
            .append("acceptanceTime", getAcceptanceTime())
            .append("acceptor", getAcceptor())
            .toString();
    }
}
