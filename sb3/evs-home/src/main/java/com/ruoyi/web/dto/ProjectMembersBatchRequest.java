package com.ruoyi.web.dto;

import java.util.List;

/**
 * 项目成员批量保存请求DTO
 */
public class ProjectMembersBatchRequest {

    /** 项目ID */
    private String projectId;

    /** 成员列表 */
    private List<MemberItem> members;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public List<MemberItem> getMembers() {
        return members;
    }

    public void setMembers(List<MemberItem> members) {
        this.members = members;
    }

    /**
     * 成员项
     */
    public static class MemberItem {
        /** 用户ID */
        private String userId;
        
        /** 角色/岗位编码 */
        private String role;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
