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

    /** 分页信息 */
    private PageInfo pageInfo;

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

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
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

    /**
     * 分页信息
     */
    public static class PageInfo {
        /** 当前页码 */
        private int pageNum;

        /** 每页数量 */
        private int pageSize;

        /** 总记录数 */
        private int total;

        /** 总页数 */
        private int totalPages;

        /** 是否有下一页 */
        private boolean hasMore;

        public int getPageNum() {
            return pageNum;
        }

        public void setPageNum(int pageNum) {
            this.pageNum = pageNum;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isHasMore() {
            return hasMore;
        }

        public void setHasMore(boolean hasMore) {
            this.hasMore = hasMore;
        }
    }
}
