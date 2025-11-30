package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.ProjectSchedules;

/**
 * 项目进度Mapper接口
 * 
 * @author evs
 * @date 2025-11-18
 */
public interface ProjectSchedulesMapper 
{
    /**
     * 查询项目进度
     *
     * @param projectSchedules 项目进度
     * @return 项目进度
     */
    public ProjectSchedules selectProjectSchedulesById(ProjectSchedules projectSchedules);

    /**
     * 查询项目进度列表
     * 
     * @param projectSchedules 项目进度
     * @return 项目进度集合
     */
    public List<ProjectSchedules> selectProjectSchedulesList(ProjectSchedules projectSchedules);

    /**
     * 新增项目进度
     * 
     * @param projectSchedules 项目进度
     * @return 结果
     */
    public int insertProjectSchedules(ProjectSchedules projectSchedules);

    /**
     * 修改项目进度
     * 
     * @param projectSchedules 项目进度
     * @return 结果
     */
    public int updateProjectSchedules(ProjectSchedules projectSchedules);

    /**
     * 删除项目进度
     * 
     * @param id 项目进度主键
     * @return 结果
     */
    public int deleteProjectSchedulesById(ProjectSchedules projectSchedules);

    /**
     * 批量删除项目进度
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProjectSchedulesByIds(ProjectSchedules projectSchedules);
}
