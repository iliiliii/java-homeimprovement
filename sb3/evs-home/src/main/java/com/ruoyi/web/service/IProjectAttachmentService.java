package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.ProjectAttachment;

/**
 * 项目附件信息Service接口
 * 
 * @author evs
 * @date 2025-12-29
 */
public interface IProjectAttachmentService 
{
    /**
     * 查询项目附件信息
     * 
     * @param id 项目附件信息主键
     * @return 项目附件信息
     */
    public ProjectAttachment selectProjectAttachmentById(String id);

    /**
     * 查询项目附件信息列表
     * 
     * @param projectAttachment 项目附件信息
     * @return 项目附件信息集合
     */
    public List<ProjectAttachment> selectProjectAttachmentList(ProjectAttachment projectAttachment);

    /**
     * 新增项目附件信息
     * 
     * @param projectAttachment 项目附件信息
     * @return 结果
     */
    public int insertProjectAttachment(ProjectAttachment projectAttachment);

    /**
     * 修改项目附件信息
     * 
     * @param projectAttachment 项目附件信息
     * @return 结果
     */
    public int updateProjectAttachment(ProjectAttachment projectAttachment);

    /**
     * 批量删除项目附件信息
     * 
     * @param ids 需要删除的项目附件信息主键集合
     * @return 结果
     */
    public int deleteProjectAttachmentByIds(String[] ids);

    /**
     * 删除项目附件信息信息
     * 
     * @param id 项目附件信息主键
     * @return 结果
     */
    public int deleteProjectAttachmentById(String id);
}
