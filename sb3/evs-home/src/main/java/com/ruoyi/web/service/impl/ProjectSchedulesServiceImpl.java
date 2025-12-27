package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.ProjectSchedulesMapper;
import com.ruoyi.web.domain.ProjectSchedules;
import com.ruoyi.web.service.IProjectSchedulesService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.common.utils.SecurityUtils;
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
     * 检查项目施工阶段是否重复
     *
     * @param projectId 项目ID
     * @param stage 施工阶段
     * @return 存在重复的记录， null表示不存在重复
     */
    private ProjectSchedules checkDuplicate(String projectId, String stage)
    {
        if (projectId == null || projectId.isEmpty() || stage == null || stage.isEmpty())
        {
            return null;
        }

        ProjectSchedules query = new ProjectSchedules();
        query.setProjectId(projectId);
        query.setStage(stage);

        // 设置当前用户ID，用于数据权限过滤
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId != null) {
            query.setCurrentUserId(String.valueOf(currentUserId));
            query.setIsAdmin(SecurityUtils.isAdmin(currentUserId));
        }

        List<ProjectSchedules> existing = projectSchedulesMapper.selectProjectSchedulesList(query);
        return existing.isEmpty() ? null : existing.get(0);
    }

    /**
     * 检查项目施工阶段是否重复（排除指定记录）
     *
     * @param projectId 项目ID
     * @param stage 施工阶段
     * @param excludeId 排除的记录ID
     * @return 存在重复的记录， null表示不存在重复
     */
    private ProjectSchedules checkDuplicateExclude(String projectId, String stage, String excludeId)
    {
        if (projectId == null || projectId.isEmpty() || stage == null || stage.isEmpty())
        {
            return null;
        }

        ProjectSchedules query = new ProjectSchedules();
        query.setProjectId(projectId);
        query.setStage(stage);

        // 设置当前用户ID，用于数据权限过滤
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId != null) {
            query.setCurrentUserId(String.valueOf(currentUserId));
            query.setIsAdmin(SecurityUtils.isAdmin(currentUserId));
        }

        List<ProjectSchedules> existing = projectSchedulesMapper.selectProjectSchedulesList(query);

        for (ProjectSchedules schedule : existing)
        {
            if (!excludeId.equals(schedule.getId()))
            {
                return schedule;
            }
        }
        return null;
    }

    /**
     * 查询项目进度
     *
     * @param id 项目进度主键
     * @return 项目进度
     */
    @Override
    public ProjectSchedules selectProjectSchedulesById(String id)
    {
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return null;
        }

        ProjectSchedules query = new ProjectSchedules();
        query.setId(id);
        query.setCurrentUserId(String.valueOf(currentUserId));
        query.setIsAdmin(SecurityUtils.isAdmin(currentUserId));

        return projectSchedulesMapper.selectProjectSchedulesById(query);
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
        // 设置当前用户ID，用于数据权限过滤
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId != null) {
            projectSchedules.setCurrentUserId(String.valueOf(currentUserId));
            projectSchedules.setIsAdmin(SecurityUtils.isAdmin(currentUserId));
        }

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
        // 检查重复：同一项目不能有相同的施工阶段
        ProjectSchedules duplicate = checkDuplicate(projectSchedules.getProjectId(), projectSchedules.getStage());
        if (duplicate != null)
        {
            throw new ServiceException("该施工阶段已存在，请选择其他阶段");
        }

        // 如果 id 为空，自动生成 UUID
        if (projectSchedules.getId() == null || projectSchedules.getId().isEmpty()) {
            projectSchedules.setId(IdUtils.fastSimpleUUID());
        }
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
        // 验证权限：检查用户是否有权限修改该项目进度
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("用户未登录");
        }

        // 验证操作的项目进度是否存在且用户有权限
        ProjectSchedules existing = selectProjectSchedulesById(projectSchedules.getId());
        if (existing == null) {
            throw new ServiceException("项目进度不存在或无权限操作");
        }

        // 检查重复：同一项目不能有相同的施工阶段（排除当前记录）
        ProjectSchedules duplicate = checkDuplicateExclude(
            projectSchedules.getProjectId(),
            projectSchedules.getStage(),
            projectSchedules.getId()
        );
        if (duplicate != null)
        {
            throw new ServiceException("该施工阶段已存在，请选择其他阶段");
        }

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
        // 验证权限：检查用户是否有权限删除这些项目进度
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("用户未登录");
        }

        boolean isAdmin = SecurityUtils.isAdmin(currentUserId);

        // 非管理员需要逐个验证每个ID的删除权限
        if (!isAdmin) {
            for (String id : ids) {
                ProjectSchedules existing = selectProjectSchedulesById(id);
                if (existing == null) {
                    throw new ServiceException("项目进度不存在或无权限删除: " + id);
                }
            }
        }

        // 创建查询对象传递参数
        ProjectSchedules query = new ProjectSchedules();
        query.setIds(ids);
        query.setCurrentUserId(String.valueOf(currentUserId));
        query.setIsAdmin(isAdmin);

        return projectSchedulesMapper.deleteProjectSchedulesByIds(query);
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
        // 验证权限：检查用户是否有权限删除该项目进度
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("用户未登录");
        }

        boolean isAdmin = SecurityUtils.isAdmin(currentUserId);

        // 非管理员需要验证要删除的项目进度是否存在且用户有权限
        if (!isAdmin) {
            ProjectSchedules existing = selectProjectSchedulesById(id);
            if (existing == null) {
                throw new ServiceException("项目进度不存在或无权限删除");
            }
        }

        // 创建查询对象传递参数
        ProjectSchedules query = new ProjectSchedules();
        query.setId(id);
        query.setCurrentUserId(String.valueOf(currentUserId));
        query.setIsAdmin(isAdmin);

        return projectSchedulesMapper.deleteProjectSchedulesById(query);
    }
}
