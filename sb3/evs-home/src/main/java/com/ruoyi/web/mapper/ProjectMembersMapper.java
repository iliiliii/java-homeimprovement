package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.ProjectMembers;

/**
 * 项目成员Mapper接口
 * 
 * @author evs
 * @date 2025-11-24
 */
public interface ProjectMembersMapper 
{
    /**
     * 查询项目成员
     * 
     * @param id 项目成员主键
     * @return 项目成员
     */
    public ProjectMembers selectProjectMembersById(String id);

    /**
     * 查询项目成员列表
     * 
     * @param projectMembers 项目成员
     * @return 项目成员集合
     */
    public List<ProjectMembers> selectProjectMembersList(ProjectMembers projectMembers);

    /**
     * 新增项目成员
     * 
     * @param projectMembers 项目成员
     * @return 结果
     */
    public int insertProjectMembers(ProjectMembers projectMembers);

    /**
     * 修改项目成员
     * 
     * @param projectMembers 项目成员
     * @return 结果
     */
    public int updateProjectMembers(ProjectMembers projectMembers);

    /**
     * 删除项目成员
     * 
     * @param id 项目成员主键
     * @return 结果
     */
    public int deleteProjectMembersById(String id);

    /**
     * 批量删除项目成员
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProjectMembersByIds(String[] ids);

    /**
     * 根据项目ID删除所有成员
     * 
     * @param projectId 项目ID
     * @return 结果
     */
    public int deleteProjectMembersByProjectId(String projectId);
}
