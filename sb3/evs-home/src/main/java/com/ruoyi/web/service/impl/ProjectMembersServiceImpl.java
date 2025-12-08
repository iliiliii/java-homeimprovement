package com.ruoyi.web.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.app.mapper.AppProjectMapper;
import com.ruoyi.web.mapper.ProjectMembersMapper;
import com.ruoyi.web.mapper.ProjectsMapper;
import com.ruoyi.web.domain.ProjectMembers;
import com.ruoyi.web.domain.Projects;
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
    
    @Autowired
    private ProjectsMapper projectsMapper;
    
    @Autowired
    private AppProjectMapper appProjectMapper;

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
    
    /**
     * 根据用户ID查询关联的项目列表
     * 
     * @param userId 用户ID
     * @return 项目列表
     */
    @Override
    public List<Projects> selectProjectsByUserId(Long userId)
    {
        // 查询用户关联的项目成员记录
        ProjectMembers query = new ProjectMembers();
        query.setUserId(String.valueOf(userId));
        List<ProjectMembers> members = projectMembersMapper.selectProjectMembersList(query);
        
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取项目ID列表
        List<String> projectIds = members.stream()
                .map(ProjectMembers::getProjectId)
                .distinct()
                .collect(Collectors.toList());
        
        // 查询项目详情（使用AppProjectMapper绕过权限控制）
        List<Projects> projects = appProjectMapper.selectProjectsByIds(projectIds);
        
        return projects != null ? projects : new ArrayList<>();
    }
}
