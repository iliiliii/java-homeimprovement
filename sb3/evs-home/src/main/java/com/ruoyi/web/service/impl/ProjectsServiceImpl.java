package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.ProjectsMapper;
import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.service.IProjectsService;
import com.ruoyi.common.utils.uuid.IdUtils;

/**
 * 项目信息Service业务层处理
 * 
 * @author evs
 * @date 2025-11-18
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
        // 如果 id 为空，自动生成 UUID
        if (projects.getId() == null || projects.getId().isEmpty()) {
            projects.setId(IdUtils.fastSimpleUUID());
        }
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
}
