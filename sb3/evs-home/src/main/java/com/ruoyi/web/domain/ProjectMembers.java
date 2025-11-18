package com.ruoyi.web.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目成员对象 project_members
 * 
 * @author evs
 * @date 2025-11-18
 */
public class ProjectMembers extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 成员ID */
    private String id;

    /** 项目ID */
    @Excel(name = "项目ID")
    private String projectId;

    /** 用户ID（关联sys_user） */
    @Excel(name = "用户ID", readConverterExp = "关=联sys_user")
    private Long userId;

    /** 项目角色（DESIGNER:设计师、PM:项目经理、WORKER:工长、SUPERVISOR:监理） */
    @Excel(name = "项目角色", readConverterExp = "D=ESIGNER:设计师、PM:项目经理、WORKER:工长、SUPERVISOR:监理")
    private String role;

    /** 是否启用 */
    @Excel(name = "是否启用")
    private Integer isActive;

    /** 添加人 */
    @Excel(name = "添加人")
    private String addedBy;

    /** 添加时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "添加时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date addedAt;

    /** 移除人 */
    private String removedBy;

    /** 移除时间 */
    private Date removedAt;

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

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setRole(String role) 
    {
        this.role = role;
    }

    public String getRole() 
    {
        return role;
    }

    public void setIsActive(Integer isActive) 
    {
        this.isActive = isActive;
    }

    public Integer getIsActive() 
    {
        return isActive;
    }

    public void setAddedBy(String addedBy) 
    {
        this.addedBy = addedBy;
    }

    public String getAddedBy() 
    {
        return addedBy;
    }

    public void setAddedAt(Date addedAt) 
    {
        this.addedAt = addedAt;
    }

    public Date getAddedAt() 
    {
        return addedAt;
    }

    public void setRemovedBy(String removedBy) 
    {
        this.removedBy = removedBy;
    }

    public String getRemovedBy() 
    {
        return removedBy;
    }

    public void setRemovedAt(Date removedAt) 
    {
        this.removedAt = removedAt;
    }

    public Date getRemovedAt() 
    {
        return removedAt;
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
            .append("userId", getUserId())
            .append("role", getRole())
            .append("isActive", getIsActive())
            .append("addedBy", getAddedBy())
            .append("addedAt", getAddedAt())
            .append("removedBy", getRemovedBy())
            .append("removedAt", getRemovedAt())
            .append("updatedBy", getUpdatedBy())
            .toString();
    }
}
