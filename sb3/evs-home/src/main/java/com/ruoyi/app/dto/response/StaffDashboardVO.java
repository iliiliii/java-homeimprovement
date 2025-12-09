package com.ruoyi.app.dto.response;

import java.util.List;

/**
 * 员工首页数据VO
 */
public class StaffDashboardVO {

    /** 用户信息 */
    private AppUserInfo userInfo;

    /** 负责的项目列表 */
    private List<StaffProjectVO> projects;

    /** 待办事项统计 */
    private TodoStats todoStats;

    public AppUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(AppUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<StaffProjectVO> getProjects() {
        return projects;
    }

    public void setProjects(List<StaffProjectVO> projects) {
        this.projects = projects;
    }

    public TodoStats getTodoStats() {
        return todoStats;
    }

    public void setTodoStats(TodoStats todoStats) {
        this.todoStats = todoStats;
    }

    /**
     * 待办事项统计
     */
    public static class TodoStats {
        /** 待巡查数量 */
        private int pendingInspections;

        /** 待整改问题数量 */
        private int pendingIssues;

        /** 今日待办数量 */
        private int todayTasks;

        public int getPendingInspections() {
            return pendingInspections;
        }

        public void setPendingInspections(int pendingInspections) {
            this.pendingInspections = pendingInspections;
        }

        public int getPendingIssues() {
            return pendingIssues;
        }

        public void setPendingIssues(int pendingIssues) {
            this.pendingIssues = pendingIssues;
        }

        public int getTodayTasks() {
            return todayTasks;
        }

        public void setTodayTasks(int todayTasks) {
            this.todayTasks = todayTasks;
        }
    }
}
