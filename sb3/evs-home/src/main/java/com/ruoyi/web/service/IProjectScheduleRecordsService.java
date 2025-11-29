package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.ProjectScheduleRecords;

/**
 * 进度记录Service接口
 * 
 * @author evs
 * @date 2025-11-29
 */
public interface IProjectScheduleRecordsService 
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
     * 批量删除进度记录
     * 
     * @param ids 需要删除的进度记录主键集合
     * @return 结果
     */
    public int deleteProjectScheduleRecordsByIds(String[] ids);

    /**
     * 删除进度记录信息
     * 
     * @param id 进度记录主键
     * @return 结果
     */
    public int deleteProjectScheduleRecordsById(String id);
}
