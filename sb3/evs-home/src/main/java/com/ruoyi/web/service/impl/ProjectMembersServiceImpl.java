package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.ProjectMembersMapper;
import com.ruoyi.web.domain.ProjectMembers;
import com.ruoyi.web.service.IProjectMembersService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;

/**
 * 项目成员Service业务层处理
 * 
 * @author evs
 * @date 2025-11-24
 */
@Service
public class ProjectMembersServiceImpl implements IProjectMembersService 
{
    @Autowired
    private ProjectMembersMapper projectMembersMapper;

    /**
     * 查询项目成员
     * 
     * @param id 项目成员主键
     * @return 项目成员
     */
    @Override
    public ProjectMembers selectProjectMembersById(String id)
    {
        return projectMembersMapper.selectProjectMembersById(id);
    }

    /**
     * 查询项目成员列表
     * 
     * @param projectMembers 项目成员
     * @return 项目成员
     */
    @Override
    public List<ProjectMembers> selectProjectMembersList(ProjectMembers projectMembers)
    {
        return projectMembersMapper.selectProjectMembersList(projectMembers);
    }

    /**
     * 新增项目成员
     * 
     * @param projectMembers 项目成员
     * @return 结果
     */
    @Override
    public int insertProjectMembers(ProjectMembers projectMembers)
    {
        // 如果 id 为空，自动生成 UUID
        if (projectMembers.getId() == null || projectMembers.getId().isEmpty()) {
            projectMembers.setId(IdUtils.fastSimpleUUID());
        }
        projectMembers.setCreatedAt(DateUtils.getNowDate());
        projectMembers.setCreatedBy(SecurityUtils.getUsername());
        return projectMembersMapper.insertProjectMembers(projectMembers);
    }

    /**
     * 修改项目成员
     * 
     * @param projectMembers 项目成员
     * @return 结果
     */
    @Override
    public int updateProjectMembers(ProjectMembers projectMembers)
    {
        projectMembers.setUpdatedAt(DateUtils.getNowDate());
        projectMembers.setUpdatedBy(SecurityUtils.getUsername());
        return projectMembersMapper.updateProjectMembers(projectMembers);
    }

    /**
     * 批量删除项目成员
     * 
     * @param ids 需要删除的项目成员主键
     * @return 结果
     */
    @Override
    public int deleteProjectMembersByIds(String[] ids)
    {
        return projectMembersMapper.deleteProjectMembersByIds(ids);
    }

    /**
     * 删除项目成员信息
     * 
     * @param id 项目成员主键
     * @return 结果
     */
    @Override
    public int deleteProjectMembersById(String id)
    {
        return projectMembersMapper.deleteProjectMembersById(id);
    }
}
