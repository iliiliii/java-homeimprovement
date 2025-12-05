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
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;

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

    /**
     * 设置当前用户权限信息
     */
    private Projects setCurrentUser(Projects projects) {
        Long currentUserId = SecurityUtils.getUserId();
        Boolean isAdmin = SecurityUtils.hasRole("admin");

        System.out.println("=== 权限设置调试 ===");
        System.out.println("SecurityUtils.getUserId(): " + currentUserId);
        System.out.println("SecurityUtils.hasRole('admin'): " + isAdmin);

        if (currentUserId != null) {
            projects.setCurrentUserId(String.valueOf(currentUserId));
            projects.setIsAdmin(isAdmin);
            System.out.println("设置权限参数 - currentUserId: " + currentUserId + ", isAdmin: " + isAdmin);
        } else {
            System.out.println("⚠️ 警告：未获取到当前用户ID");
        }
        return projects;
    }

    /**
     * 查询项目信息
     *
     * @param id 项目信息主键
     * @return 项目信息
     */
    @Override
    public Projects selectProjectsById(String id)
    {
        Projects query = new Projects();
        query.setId(id);
        setCurrentUser(query);
        return projectsMapper.selectProjectsById(query);
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
        System.out.println("\n=== 项目列表查询调试 ===");
        System.out.println("原始查询参数: " + projects);

        Projects query = setCurrentUser(projects);
        System.out.println("设置权限后参数: currentUserId=" + query.getCurrentUserId() + ", isAdmin=" + query.getIsAdmin());

        List<Projects> result = projectsMapper.selectProjectsList(query);
        System.out.println("查询结果数量: " + result.size());

        if (result.size() > 0) {
            System.out.println("返回的第一个项目: " + result.get(0));
        }

        return result;
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
        // 验证权限：检查用户是否有权限修改该项目
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("用户未登录");
        }

        // 验证操作的项目是否存在且用户有权限
        Projects existing = selectProjectsById(projects.getId());
        if (existing == null) {
            throw new ServiceException("项目不存在或无权限操作");
        }

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
        // 验证权限：检查用户是否有权限删除这些项目
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("用户未登录");
        }

        // 逐个验证每个ID的删除权限
        for (String id : ids) {
            Projects existing = selectProjectsById(id);
            if (existing == null) {
                throw new ServiceException("项目不存在或无权限删除: " + id);
            }
        }

        // 创建删除对象
        Projects query = new Projects();
        query.setIds(ids);
        query.setCurrentUserId(String.valueOf(currentUserId));

        return projectsMapper.deleteProjectsByIds(query);
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
        // 验证权限：检查用户是否有权限删除该项目
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("用户未登录");
        }

        // 验证要删除的项目是否存在且用户有权限
        Projects existing = selectProjectsById(id);
        if (existing == null) {
            throw new ServiceException("项目不存在或无权限删除");
        }

        // 创建删除对象
        Projects query = new Projects();
        query.setId(id);
        query.setCurrentUserId(String.valueOf(currentUserId));

        return projectsMapper.deleteProjectsById(query);
    }

    @Override
    public int softDeleteProjectsById(String id)
    {
        // 使用带权限控制的查询方法
        Projects projects = selectProjectsById(id);
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

        // 设置权限信息
        Projects query = setCurrentUser(projects);

        List<Projects> projectList;

        // 优先处理 customer 关联查询
        if (includeRelations.contains("customer")) {
            projectList = projectsMapper.selectProjectsWithCustomer(query);
        } else if (includeRelations.contains("projectMembers")) {
            // 通过 projectMembers 关联查询
            projectList = projectsMapper.selectProjectsWithMembers(query, memberUserId, isAdmin);
        } else {
            projectList = selectProjectsList(query);
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
            Projects query = new Projects();
            query.setId(id);
            setCurrentUser(query);
            project = projectsMapper.selectProjectsWithCustomerById(query);
        } else {
            project = selectProjectsById(id);
        }

        return project;
    }

    @Override
    public List<Projects> selectProjectsListWithScheduleInfo(Projects projects)
    {
        // 设置权限信息（自动应用权限控制）
        Projects query = setCurrentUser(projects);

        // 1. 查询项目列表（带权限过滤）
        List<Projects> projectsList = projectsMapper.selectProjectsList(query);

        // 2. 如果无项目，直接返回
        if (projectsList.isEmpty()) {
            return projectsList;
        }

        // 3. 获取所有项目ID（保持String类型）
        List<String> projectIds = projectsList.stream()
                .map(Projects::getId)
                .collect(Collectors.toList());

        // 4. 批量查询进度统计
        Map<String, Map<String, Object>> statsMap = projectsMapper.selectScheduleStatsMap(projectIds);

        // 5. 为每��项目设置统计信息
        for (Projects project : projectsList) {
            Map<String, Object> stats = statsMap.get(project.getId());
            if (stats != null) {
                project.setTotalSchedules(((Number) stats.get("total_count")).longValue());
                project.setCompletedSchedules(((Number) stats.get("completed_count")).longValue());
                project.setInProgressSchedules(((Number) stats.get("in_progress_count")).longValue());
                project.setProgressRate(((Number) stats.get("progress_rate")).intValue());
            } else {
                // 如果没有进度数据，初始化为0
                project.setTotalSchedules(0L);
                project.setCompletedSchedules(0L);
                project.setInProgressSchedules(0L);
                project.setProgressRate(0);
            }
        }

        return projectsList;
    }
}
