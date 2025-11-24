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
 * @date 2025-11-24
 */
public class ProjectMembers extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 成员ID */
    private String id;

    /** 项目ID（逻辑关联） */
    @Excel(name = "项目ID", readConverterExp = "逻=辑关联")
    private String projectId;

    /** 用户ID（逻辑关联） */
    @Excel(name = "用户ID", readConverterExp = "逻=辑关联")
    private String userId;

    /** 项目角色（DESIGNER:设计师、PM:项目经理、WORKER:工长、SUPERVISOR:监理） */
    @Excel(name = "项目角色", readConverterExp = "D=ESIGNER:设计师、PM:项目经理、WORKER:工长、SUPERVISOR:监理")
    private String role;

    /** 是否启用（0:已移除，1:在职） */
    private Integer isActive;

    /** 创建时间 */
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

    /** 创建人（添加成员的人） */
    private String createdBy;

    /** 更新人（移除/修改的人） */
    private String updatedBy;

    // ==================== 用户关联字段 ====================
    /** 用户账号 */
    @Excel(name = "用户账号")
    private String userName;

    /** 用户昵称 */
    @Excel(name = "用户昵称")
    private String nickName;

    /** 岗位名称 */
    @Excel(name = "岗位名称")
    private String postName;

    /** 岗位编码 */
    @Excel(name = "岗位编码")
    private String postCode;

    /** 角色编码 */
    @Excel(name = "角色编码")
    private String roleKey;

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

    public void setUserId(String userId) 
    {
        this.userId = userId;
    }

    public String getUserId() 
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

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getPostName()
    {
        return postName;
    }

    public void setPostName(String postName)
    {
        this.postName = postName;
    }

    public String getPostCode()
    {
        return postCode;
    }

    public void setPostCode(String postCode)
    {
        this.postCode = postCode;
    }

    public String getRoleKey()
    {
        return roleKey;
    }

    public void setRoleKey(String roleKey)
    {
        this.roleKey = roleKey;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("projectId", getProjectId())
            .append("userId", getUserId())
            .append("role", getRole())
            .append("isActive", getIsActive())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .append("createdBy", getCreatedBy())
            .append("updatedBy", getUpdatedBy())
            .append("userName", getUserName())
            .append("nickName", getNickName())
            .append("postName", getPostName())
            .append("postCode", getPostCode())
            .append("roleKey", getRoleKey())
            .toString();
    }
}
