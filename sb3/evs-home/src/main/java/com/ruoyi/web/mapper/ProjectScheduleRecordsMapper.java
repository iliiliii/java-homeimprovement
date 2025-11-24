package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.ProjectScheduleRecords;

/**
 * 进度记录Mapper接口
 * 
 * @author eve
 * @date 2025-11-24
 */
public interface ProjectScheduleRecordsMapper 
{
    /**
     * 查询进度记录
     * 
     * @param id 进度记录主键
     * @return 进度记录
     */
    public ProjectScheduleRecords selectProjectScheduleRecordsById(String id);

    /**
     * 查询进度记录列表
     * 
     * @param projectScheduleRecords 进度记录
     * @return 进度记录集合
     */
    public List<ProjectScheduleRecords> selectProjectScheduleRecordsList(ProjectScheduleRecords projectScheduleRecords);

    /**
     * 新增进度记录
     * 
     * @param projectScheduleRecords 进度记录
     * @return 结果
     */
    public int insertProjectScheduleRecords(ProjectScheduleRecords projectScheduleRecords);

    /**
     * 修改进度记录
     * 
     * @param projectScheduleRecords 进度记录
     * @return 结果
     */
    public int updateProjectScheduleRecords(ProjectScheduleRecords projectScheduleRecords);

    /**
     * 删除进度记录
     * 
     * @param id 进度记录主键
     * @return 结果
     */
    public int deleteProjectScheduleRecordsById(String id);

    /**
     * 批量删除进度记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProjectScheduleRecordsByIds(String[] ids);
}
