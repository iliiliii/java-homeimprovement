package com.ruoyi.app.dto.response;

/**
 * 项目成员信息VO
 */
public class ProjectMemberVO {
    
    /** 成员ID */
    private String id;
    
    /** 用户ID */
    private String userId;
    
    /** 姓名 */
    private String name;
    
    /** 岗位 */
    private String post;
    
    /** 头像 */
    private String avatar;
    
    /** 项目角色 */
    private String role;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPost() {
        return post;
    }
    
    public void setPost(String post) {
        this.post = post;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
}
