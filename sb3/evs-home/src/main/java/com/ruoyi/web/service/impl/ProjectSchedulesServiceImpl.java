package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.ProjectSchedulesMapper;
import com.ruoyi.web.domain.ProjectSchedules;
import com.ruoyi.web.service.IProjectSchedulesService;

/**
 * 项目进度Service业务层处理
 * 
 * @author evs
 * @date 2025-11-18
 */
@Service
public class ProjectSchedulesServiceImpl implements IProjectSchedulesService 
{
    @Autowired
    private ProjectSchedulesMapper projectSchedulesMapper;

    /**
     * 查询项目进度
     * 
     * @param id 项目进度主键
     * @return 项目进度
     */
    @Override
    public ProjectSchedules selectProjectSchedulesById(String id)
    {
        return projectSchedulesMapper.selectProjectSchedulesById(id);
    }

    /**
     * 查询项目进度列表
     * 
     * @param projectSchedules 项目进度
     * @return 项目进度
     */
    @Override
    public List<ProjectSchedules> selectProjectSchedulesList(ProjectSchedules projectSchedules)
    {
        return projectSchedulesMapper.selectProjectSchedulesList(projectSchedules);
    }

    /**
     * 新增项目进度
     * 
     * @param projectSchedules 项目进度
     * @return 结果
     */
    @Override
    public int insertProjectSchedules(ProjectSchedules projectSchedules)
    {
        return projectSchedulesMapper.insertProjectSchedules(projectSchedules);
    }

    /**
     * 修改项目进度
     * 
     * @param projectSchedules 项目进度
     * @return 结果
     */
    @Override
    public int updateProjectSchedules(ProjectSchedules projectSchedules)
    {
        return projectSchedulesMapper.updateProjectSchedules(projectSchedules);
    }

    /**
     * 批量删除项目进度
     * 
     * @param ids 需要删除的项目进度主键
     * @return 结果
     */
    @Override
    public int deleteProjectSchedulesByIds(String[] ids)
    {
        return projectSchedulesMapper.deleteProjectSchedulesByIds(ids);
    }

    /**
     * 删除项目进度信息
     * 
     * @param id 项目进度主键
     * @return 结果
     */
    @Override
    public int deleteProjectSchedulesById(String id)
    {
        return projectSchedulesMapper.deleteProjectSchedulesById(id);
    }
}
