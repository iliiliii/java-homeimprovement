package com.ruoyi.app.dto.response;

import java.util.List;

/**
 * 客户首页数据VO
 */
public class CustomerDashboardVO {

    /** 用户信息 */
    private AppUserInfo userInfo;

    /** 项目列表 */
    private List<CustomerProjectVO> projects;

    /** 当前选中的项目ID */
    private String currentProjectId;

    public AppUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(AppUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<CustomerProjectVO> getProjects() {
        return projects;
    }

    public void setProjects(List<CustomerProjectVO> projects) {
        this.projects = projects;
    }

    public String getCurrentProjectId() {
        return currentProjectId;
    }

    public void setCurrentProjectId(String currentProjectId) {
        this.currentProjectId = currentProjectId;
    }
}
