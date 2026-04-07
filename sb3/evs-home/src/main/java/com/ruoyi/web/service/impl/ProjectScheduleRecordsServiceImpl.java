package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.ProjectScheduleRecordsMapper;
import com.ruoyi.web.mapper.ProjectMembersMapper;
import com.ruoyi.web.domain.ProjectScheduleRecords;
import com.ruoyi.web.service.IProjectScheduleRecordsService;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.common.exception.ServiceException;



/**
 * 进度记录Service业务层处理
 * 
 * @author evs
 * @date 2025-11-29
 */
@Service
public class ProjectScheduleRecordsServiceImpl implements IProjectScheduleRecordsService 
{
    @Autowired
    private ProjectScheduleRecordsMapper projectScheduleRecordsMapper;
    
    @Autowired
    private ProjectMembersMapper projectMembersMapper;

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
        projectScheduleRecords.setCreateTime(DateUtils.getNowDate());
        // ✅ 修改：存储用户ID而非用户名
        projectScheduleRecords.setCreateBy(SecurityUtils.getUserId().toString());
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
        // ✅ 先查询完整记录获取projectId
        ProjectScheduleRecords existingRecord = projectScheduleRecordsMapper
            .selectProjectScheduleRecordsById(projectScheduleRecords.getId());
        
        if (existingRecord == null) {
            throw new ServiceException("记录不存在");
        }
        
        // ✅ 权限验证：检查是否为项目成员
        String currentUserId = SecurityUtils.getUserId().toString();
        String projectId = existingRecord.getProjectId();
        
        if (!isProjectMember(currentUserId, projectId)) {
            throw new ServiceException("只有项目成员可以编辑验收记录");
        }
        
        projectScheduleRecords.setUpdateTime(DateUtils.getNowDate());
        // ✅ 修改：存储用户ID而非用户名
        projectScheduleRecords.setUpdateBy(currentUserId);
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
        // ✅ 批量删除时验证每条记录的权限
        String currentUserId = SecurityUtils.getUserId().toString();
        
        for (String id : ids) {
            ProjectScheduleRecords record = projectScheduleRecordsMapper.selectProjectScheduleRecordsById(id);
            if (record != null) {
                String projectId = record.getProjectId();
                if (!isProjectMember(currentUserId, projectId)) {
                    throw new ServiceException("只有项目成员可以删除验收记录");
                }
            }
        }
        
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
        // ✅ 单条删除时验证权限
        ProjectScheduleRecords record = projectScheduleRecordsMapper.selectProjectScheduleRecordsById(id);
        
        if (record == null) {
            throw new ServiceException("记录不存在");
        }
        
        String currentUserId = SecurityUtils.getUserId().toString();
        String projectId = record.getProjectId();
        
        if (!isProjectMember(currentUserId, projectId)) {
            throw new ServiceException("只有项目成员可以删除验收记录");
        }
        
        return projectScheduleRecordsMapper.deleteProjectScheduleRecordsById(id);
    }
    
    /**
     * 检查用户是否为项目成员
     */
    private boolean isProjectMember(String userId, String projectId) {
        return projectMembersMapper.checkUserIsProjectMember(userId, projectId);
    }
}
