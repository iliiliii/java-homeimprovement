package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.ProjectAttachment;

/**
 * 项目附件信息Mapper接口
 * 
 * @author evs
 * @date 2025-12-29
 */
public interface ProjectAttachmentMapper 
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
     * 删除项目附件信息
     * 
     * @param id 项目附件信息主键
     * @return 结果
     */
    public int deleteProjectAttachmentById(String id);

    /**
     * 批量删除项目附件信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProjectAttachmentByIds(String[] ids);

    /**
     * 查询所有项目的合同总额
     * 
     * @return 合同总额
     */
    public Double selectTotalContractAmount();
}
