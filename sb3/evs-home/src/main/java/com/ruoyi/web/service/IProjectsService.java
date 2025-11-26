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
     * 查询项目信息
     * 
     * @param id 项目信息主键
     * @return 项目信息
     */
    public Projects selectProjectsById(String id);

    /**
     * 查询项目信息列表
     * 
     * @param projects 项目信息
     * @return 项目信息集合
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
     * 修改项目信息
     * 
     * @param projects 项目信息
     * @return 结果
     */
    public int updateProjects(Projects projects);

    /**
     * 批量删除项目信息
     * 
     * @param ids 需要删除的项目信息主键集合
     * @return 结果
     */
    public int deleteProjectsByIds(String[] ids);

    /**
     * 删除项目信息信息
     * 
     * @param id 项目信息主键
     * @return 结果
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
     * 查询项目信息列表（支持关联查询）
     *
     * @param projects 项目信息查询条件
     * @param includeRelations 需要包含的关联关系，用逗号分隔（如：customer,budgetItems,schedules）
     * @param memberUserId 团队成员用户ID（用于筛选）
     * @param isAdmin 是否管理员
     * @return 项目信息集合
     */
    public List<Projects> selectProjectsWithRelations(Projects projects, String includeRelations, String memberUserId, boolean isAdmin);

    /**
     * 查询项目信息（支持关联查询）
     *
     * @param id 项目信息主键
     * @param includeRelations 需要包含的关联关系，用逗号分隔（如：customer,budgetItems,schedules）
     * @return 项目信息
     */
    public Projects selectProjectsWithRelationsById(String id, String includeRelations);
}
