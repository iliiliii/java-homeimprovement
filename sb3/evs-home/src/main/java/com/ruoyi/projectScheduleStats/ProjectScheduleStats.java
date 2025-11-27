package com.ruoyi.projectScheduleStats;

import java.io.Serializable;

/**
 * 项目进度统计实体类
 */
public class ProjectScheduleStats implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private String projectId;

    /** 进度总数 */
    private Long totalCount;

    /** 已完成数量 */
    private Long completedCount;

    /** 进行中数量 */
    private Long inProgressCount;

    /** 完成率 */
    private Integer progressRate;

    // getter/setter 方法
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Long completedCount) {
        this.completedCount = completedCount;
    }

    public Long getInProgressCount() {
        return inProgressCount;
    }

    public void setInProgressCount(Long inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public Integer getProgressRate() {
        return progressRate;
    }

    public void setProgressRate(Integer progressRate) {
        this.progressRate = progressRate;
    }
}
