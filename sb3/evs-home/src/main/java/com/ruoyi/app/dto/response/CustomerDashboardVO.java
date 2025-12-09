package com.ruoyi.app.dto.response;

import java.util.List;

/**
 * 客户首页数据VO
 * 注：用户信息在登录时已返回并缓存，此处不再重复返回
 */
public class CustomerDashboardVO {

    /** 项目列表 */
    private List<CustomerProjectVO> projects;

    /** 当前选中的项目ID */
    private String currentProjectId;

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
