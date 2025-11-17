package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.Projects;

/**
 * 项目信息Service接口
 * 
 * @author evs
 * @date 2025-11-18
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
}
