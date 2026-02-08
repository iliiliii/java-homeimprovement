package com.ruoyi.app.dto.response;

/**
 * 团队成员信息VO
 */
public class TeamMemberVO {
    
    /** 姓名 */
    private String name;
    
    /** 岗位 */
    private String post;
    
    /** 头像 */
    private String avatar;
    
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
}
