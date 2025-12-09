package com.ruoyi.app.dto.response;

import java.util.List;

/**
 * 员工首页数据VO
 * 注：用户信息在登录时已返回并缓存，此处不再重复返回
 */
public class StaffDashboardVO {

    /** 负责的项目列表 */
    private List<StaffProjectVO> projects;

    /** 待办事项统计 */
    private TodoStats todoStats;

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
