package com.ruoyi.web.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.ruoyi.web.mapper.ProjectsMapper;
import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.service.IProjectsService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.projectScheduleStats.ProjectScheduleStats;
import com.ruoyi.mapper.evs.ProjectScheduleStatsMapper;

/**
 * 项目信息Service业务层处理
 * 
 * @author evs
 * @date 2025-11-23
 */
@Service
public class ProjectsServiceImpl implements IProjectsService
{
    @Autowired
    private ProjectsMapper projectsMapper;

    @Autowired
    private ProjectScheduleStatsMapper projectScheduleStatsMapper;

    /**
     * 查询项目信息
     * 
     * @param id 项目信息主键
     * @return 项目信息
     */
    @Override
    public Projects selectProjectsById(String id)
    {
        return projectsMapper.selectProjectsById(id);
    }

    /**
     * 查询项目信息列表
     * 
     * @param projects 项目信息
     * @return 项目信息
     */
    @Override
    public List<Projects> selectProjectsList(Projects projects)
    {
        return projectsMapper.selectProjectsList(projects);
    }

    /**
     * 新增项目信息
     * 
     * @param projects 项目信息
     * @return 结果
     */
    @Override
    public int insertProjects(Projects projects)
    {
        return projectsMapper.insertProjects(projects);
    }

    /**
     * 修改项目信息
     * 
     * @param projects 项目信息
     * @return 结果
     */
    @Override
    public int updateProjects(Projects projects)
    {
        return projectsMapper.updateProjects(projects);
    }

    /**
     * 批量删除项目信息
     * 
     * @param ids 需要删除的项目信息主键
     * @return 结果
     */
    @Override
    public int deleteProjectsByIds(String[] ids)
    {
        return projectsMapper.deleteProjectsByIds(ids);
    }

    /**
     * 删除项目信息信息
     * 
     * @param id 项目信息主键
     * @return 结果
     */
    @Override
    public int deleteProjectsById(String id)
    {
        return projectsMapper.deleteProjectsById(id);
    }

    @Override
    public int softDeleteProjectsById(String id)
    {
        Projects projects = projectsMapper.selectProjectsById(id);
        if (projects == null) {
            return 0;
        }
        projects.setDeletedAt(DateUtils.getNowDate());
        return projectsMapper.updateProjects(projects);
    }

    @Override
    public List<Projects> selectProjectsWithRelations(Projects projects, String includeRelations, String memberUserId, boolean isAdmin)
    {
        if (!StringUtils.hasText(includeRelations)) {
            return selectProjectsList(projects);
        }

        List<Projects> projectList;

        if (includeRelations.contains("customer")) {
            projectList = projectsMapper.selectProjectsWithCustomer(projects);
        } else if (includeRelations.contains("projectMembers")) {
            // 通过 projectMembers 关联查询
            projectList = projectsMapper.selectProjectsWithMembers(projects, memberUserId, isAdmin);
        } else {
            projectList = selectProjectsList(projects);
        }

        return projectList;
    }

    @Override
    public Projects selectProjectsWithRelationsById(String id, String includeRelations)
    {
        if (!StringUtils.hasText(includeRelations)) {
            return selectProjectsById(id);
        }

        Projects project;

        if (includeRelations.contains("customer")) {
            project = projectsMapper.selectProjectsWithCustomerById(id);
        } else {
            project = selectProjectsById(id);
        }

        return project;
    }

    @Override
    public List<Projects> selectProjectsListWithScheduleInfo(Projects projects)
    {
        // 1. 查询项目列表
        List<Projects> projectsList = selectProjectsList(projects);

        // 2. 如果无项目，直接返回
        if (projectsList.isEmpty()) {
            return projectsList;
        }

        // 3. 获取所有项目ID（保持String类型）
        List<String> projectIds = projectsList.stream()
                .map(Projects::getId)
                .collect(Collectors.toList());

        // 4. 批量查询进度统计
        Map<String, ProjectScheduleStats> statsMap =
            projectScheduleStatsMapper.selectScheduleStatsMap(projectIds);

        // 5. 为每个项目设置统计信息
        for (Projects p : projectsList) {
            ProjectScheduleStats stats = statsMap.get(p.getId());
            if (stats != null) {
                p.setTotalSchedules(stats.getTotalCount());
                p.setCompletedSchedules(stats.getCompletedCount());
                p.setInProgressSchedules(stats.getInProgressCount());
                p.setProgressRate(stats.getProgressRate());
            } else {
                // 如果没有进度数据，初始化为0
                p.setTotalSchedules(0L);
                p.setCompletedSchedules(0L);
                p.setInProgressSchedules(0L);
                p.setProgressRate(0);
            }
        }

        return projectsList;
    }
}
