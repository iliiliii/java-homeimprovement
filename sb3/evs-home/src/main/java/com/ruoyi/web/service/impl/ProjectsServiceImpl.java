package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.ProjectsMapper;
import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.service.IProjectsService;
import com.ruoyi.common.utils.DateUtils;

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
}
