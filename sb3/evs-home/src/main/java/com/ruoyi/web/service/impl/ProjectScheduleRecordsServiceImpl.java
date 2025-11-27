package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.ProjectScheduleRecordsMapper;
import com.ruoyi.web.domain.ProjectScheduleRecords;
import com.ruoyi.web.service.IProjectScheduleRecordsService;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
/**
 * 进度记录Service业务层处理
 * 
 * @author evs
 * @date 2025-11-27
 */
@Service
public class ProjectScheduleRecordsServiceImpl implements IProjectScheduleRecordsService 
{
    @Autowired
    private ProjectScheduleRecordsMapper projectScheduleRecordsMapper;

    /**
     * 查询进度记录
     * 
     * @param id 进度记录主键
     * @return 进度记录
     */
    @Override
    public ProjectScheduleRecords selectProjectScheduleRecordsById(String id)
    {
        return projectScheduleRecordsMapper.selectProjectScheduleRecordsById(id);
    }

    /**
     * 查询进度记录列表
     * 
     * @param projectScheduleRecords 进度记录
     * @return 进度记录
     */
    @Override
    public List<ProjectScheduleRecords> selectProjectScheduleRecordsList(ProjectScheduleRecords projectScheduleRecords)
    {
        return projectScheduleRecordsMapper.selectProjectScheduleRecordsList(projectScheduleRecords);
    }

    /**
     * 新增进度记录
     * 
     * @param projectScheduleRecords 进度记录
     * @return 结果
     */
    @Override
    public int insertProjectScheduleRecords(ProjectScheduleRecords projectScheduleRecords)
    {
        // 如果 id 为空，自动生成 UUID
        if (projectScheduleRecords.getId() == null || projectScheduleRecords.getId().isEmpty()) {
            projectScheduleRecords.setId(IdUtils.fastSimpleUUID());
        }
        projectScheduleRecords.setCreatedAt(DateUtils.getNowDate());
        projectScheduleRecords.setCreatedBy(SecurityUtils.getUsername());
        return projectScheduleRecordsMapper.insertProjectScheduleRecords(projectScheduleRecords);
    }

    /**
     * 修改进度记录
     * 
     * @param projectScheduleRecords 进度记录
     * @return 结果
     */
    @Override
    public int updateProjectScheduleRecords(ProjectScheduleRecords projectScheduleRecords)
    {
        projectScheduleRecords.setUpdatedAt(DateUtils.getNowDate());
        projectScheduleRecords.setUpdatedBy(SecurityUtils.getUsername());
        return projectScheduleRecordsMapper.updateProjectScheduleRecords(projectScheduleRecords);
    }

    /**
     * 批量删除进度记录
     * 
     * @param ids 需要删除的进度记录主键
     * @return 结果
     */
    @Override
    public int deleteProjectScheduleRecordsByIds(String[] ids)
    {
        return projectScheduleRecordsMapper.deleteProjectScheduleRecordsByIds(ids);
    }

    /**
     * 删除进度记录信息
     * 
     * @param id 进度记录主键
     * @return 结果
     */
    @Override
    public int deleteProjectScheduleRecordsById(String id)
    {
        return projectScheduleRecordsMapper.deleteProjectScheduleRecordsById(id);
    }
}
