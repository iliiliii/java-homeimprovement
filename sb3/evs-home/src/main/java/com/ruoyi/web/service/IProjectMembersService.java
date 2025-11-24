package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.ProjectMembers;

/**
 * 项目成员Service接口
 * 
 * @author evs
 * @date 2025-11-24
 */
public interface IProjectMembersService 
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
     * 批量删除项目成员
     * 
     * @param ids 需要删除的项目成员主键集合
     * @return 结果
     */
    public int deleteProjectMembersByIds(String[] ids);

    /**
     * 删除项目成员信息
     * 
     * @param id 项目成员主键
     * @return 结果
     */
    public int deleteProjectMembersById(String id);
}
