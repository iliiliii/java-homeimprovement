package com.ruoyi.web.service;

import java.util.List;
import java.util.Set;
import com.ruoyi.web.domain.Projects;

/**
 * 项目信息Service接口
 * 
 * @author evs
 * @date 2025-11-23
 */
public interface IProjectsService
{
    /**
     * 查询项目信息（带权限控制）
     *
     * @param id 项目信息主键
     * @return 项目信息（仅返回当前用户有权限的项目）
     */
    public Projects selectProjectsById(String id);

    /**
     * 查询项目信息列表（带权限控制）
     *
     * @param projects 项目信息查询条件
     * @return 项目信息集合（仅返回当前用户有权限的项目）
     */
    public List<Projects> selectProjectsList(Projects projects);

    /**
     * 新增项目信息
     *
     * @param projects 项目信息
     * @return 结果
     */
    public int insertProjects(Projects projects);

    /**
     * 修改项目信息（带权限验证）
     *
     * @param projects 项目信息
     * @return 结果（无权限修改时抛出异常）
     */
    public int updateProjects(Projects projects);

    /**
     * 批量删除项目信息（带权限验证）
     *
     * @param ids 需要删除的项目信息主键集合
     * @return 结果（无权限删除时抛出异常）
     */
    public int deleteProjectsByIds(String[] ids);

    /**
     * 删除项目信息（带权限验证）
     *
     * @param id 项目信息主键
     * @return 结果（无权限删除时抛出异常）
     */
    public int deleteProjectsById(String id);

    /**
     * 软删除项目信息信息
     *
     * @param id 项目信息主键
     * @return 结果
     */
    public int softDeleteProjectsById(String id);

    /**
     * 查询项目信息列表（支持关联查询和权限过滤）
     *
     * @param projects 项目信息查询条件
     * @param includeRelations 需要包含的关联关系，用逗号分隔（如：customer,budgetItems,schedules）
     * @param memberUserId 团队成员用户ID（已废弃，由Service自动获取当前用户）
     * @param isAdmin 是否管理员（已废弃，由Service自动判断）
     * @return 项目信息集合（仅返回当前用户有权限的项目）
     */
    public List<Projects> selectProjectsWithRelations(Projects projects, String includeRelations, String memberUserId, boolean isAdmin);

    /**
     * 查询项目信息（支持关联查询，带权限控制）
     *
     * @param id 项��信息主键
     * @param includeRelations 需要包含的关联关系，用逗号分隔（如：customer,budgetItems,schedules）
     * @return 项目信息（仅返回当前用户有权限的项目）
     */
    public Projects selectProjectsWithRelationsById(String id, String includeRelations);

    /**
     * 查询项目列表及进度统计信息（带权限控制）
     *
     * @param projects 项目查询条件
     * @return 项目列表（包含进度统计字段，仅返回当前用户有权限的项目）
     */
    public List<Projects> selectProjectsListWithScheduleInfo(Projects projects);
}
