package com.ruoyi.web.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 客户档案对象 customers
 * 
 * @author evs
 * @date 2025-11-23
 */
public class Customers extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 客户ID */
    private String id;

    /** 客户姓名 */
    @Excel(name = "客户姓名")
    private String name;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    /** 邮箱 */
    private String email;

    /** 地址 */
    @Excel(name = "地址")
    private String address;

    /** 客户等级（NORMAL:普通、VIP:重要、KEY:关键） */
    @Excel(name = "客户等级", readConverterExp = "N=ORMAL:普通、VIP:重要、KEY:关键")
    private String level;

    /** 客户来源 */
    @Excel(name = "客户来源")
    private String source;

    /** 备注 */
    private String remarks;

    /** 头像 */
    @Excel(name = "头像")
    private String avatar;

    /** 是否启用 */
    @Excel(name = "是否启用")
    private Integer isActive;

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

    /** 关联的项目列表 */
    private List<Projects> projects;

    /** 项目数量统计 */
    private Integer projectCount;

    public void setId(String id)
    {
        this.id = id;
    }

    public String getId() 
    {
        return id;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }

    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }

    public void setEmail(String email) 
    {
        this.email = email;
    }

    public String getEmail() 
    {
        return email;
    }

    public void setAddress(String address) 
    {
        this.address = address;
    }

    public String getAddress() 
    {
        return address;
    }

    public void setLevel(String level) 
    {
        this.level = level;
    }

    public String getLevel() 
    {
        return level;
    }

    public void setSource(String source) 
    {
        this.source = source;
    }

    public String getSource() 
    {
        return source;
    }

    public void setRemarks(String remarks) 
    {
        this.remarks = remarks;
    }

    public String getRemarks() 
    {
        return remarks;
    }

    public void setAvatar(String avatar) 
    {
        this.avatar = avatar;
    }

    public String getAvatar() 
    {
        return avatar;
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

    public void setProjects(List<Projects> projects)
    {
        this.projects = projects;
    }

    public List<Projects> getProjects()
    {
        return projects;
    }

    public void setProjectCount(Integer projectCount)
    {
        this.projectCount = projectCount;
    }

    public Integer getProjectCount()
    {
        return projectCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("phone", getPhone())
            .append("email", getEmail())
            .append("address", getAddress())
            .append("level", getLevel())
            .append("source", getSource())
            .append("remarks", getRemarks())
            .append("avatar", getAvatar())
            .append("isActive", getIsActive())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .append("deletedAt", getDeletedAt())
            .append("createdBy", getCreatedBy())
            .append("updatedBy", getUpdatedBy())
            .toString();
    }
}
