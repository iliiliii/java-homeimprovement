package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 微信绑定对象 app_wechat_bindings
 * 
 * @author evs
 * @date 2026-01-19
 */
public class AppWechatBindings extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private String id;

    /** 微信openId */
    @Excel(name = "微信openId")
    private String openId;

    /** 微信unionId */
    @Excel(name = "微信unionId")
    private String unionId;

    /** 用户类型：customer/staff */
    @Excel(name = "用户类型：customer/staff")
    private String userType;

    /** 用户ID */
    @Excel(name = "用户ID")
    private String userId;

    /** 绑定手机号 */
    @Excel(name = "绑定手机号")
    private String phone;

    /** 微信昵称 */
    @Excel(name = "微信昵称")
    private String nickname;

    /** 微信头像 */
    @Excel(name = "微信头像")
    private String avatar;

    /** 会话密钥 */
    @Excel(name = "会话密钥")
    private String sessionKey;

    /** 绑定时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "绑定时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date bindTime;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastLoginTime;

    public void setId(String id) 
    {
        this.id = id;
    }

    public String getId() 
    {
        return id;
    }

    public void setOpenId(String openId) 
    {
        this.openId = openId;
    }

    public String getOpenId() 
    {
        return openId;
    }

    public void setUnionId(String unionId) 
    {
        this.unionId = unionId;
    }

    public String getUnionId() 
    {
        return unionId;
    }

    public void setUserType(String userType) 
    {
        this.userType = userType;
    }

    public String getUserType() 
    {
        return userType;
    }

    public void setUserId(String userId) 
    {
        this.userId = userId;
    }

    public String getUserId() 
    {
        return userId;
    }

    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }

    public void setNickname(String nickname) 
    {
        this.nickname = nickname;
    }

    public String getNickname() 
    {
        return nickname;
    }

    public void setAvatar(String avatar) 
    {
        this.avatar = avatar;
    }

    public String getAvatar() 
    {
        return avatar;
    }

    public void setSessionKey(String sessionKey) 
    {
        this.sessionKey = sessionKey;
    }

    public String getSessionKey() 
    {
        return sessionKey;
    }

    public void setBindTime(Date bindTime) 
    {
        this.bindTime = bindTime;
    }

    public Date getBindTime() 
    {
        return bindTime;
    }

    public void setLastLoginTime(Date lastLoginTime) 
    {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getLastLoginTime() 
    {
        return lastLoginTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("openId", getOpenId())
            .append("unionId", getUnionId())
            .append("userType", getUserType())
            .append("userId", getUserId())
            .append("phone", getPhone())
            .append("nickname", getNickname())
            .append("avatar", getAvatar())
            .append("sessionKey", getSessionKey())
            .append("bindTime", getBindTime())
            .append("lastLoginTime", getLastLoginTime())
            .append("createTime", getCreateTime())
            .toString();
    }
}
