package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.ProjectAttachmentMapper;
import com.ruoyi.web.domain.ProjectAttachment;
import com.ruoyi.web.service.IProjectAttachmentService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;


/**
 * 项目附件信息Service业务层处理
 * 
 * @author evs
 * @date 2025-12-29
 */
@Service
public class ProjectAttachmentServiceImpl implements IProjectAttachmentService 
{
    @Autowired
    private ProjectAttachmentMapper projectAttachmentMapper;

    /**
     * 查询项目附件信息
     * 
     * @param id 项目附件信息主键
     * @return 项目附件信息
     */
    @Override
    public ProjectAttachment selectProjectAttachmentById(String id)
    {
        return projectAttachmentMapper.selectProjectAttachmentById(id);
    }

    /**
     * 查询项目附件信息列表
     * 
     * @param projectAttachment 项目附件信息
     * @return 项目附件信息
     */
    @Override
    public List<ProjectAttachment> selectProjectAttachmentList(ProjectAttachment projectAttachment)
    {
        return projectAttachmentMapper.selectProjectAttachmentList(projectAttachment);
    }

    /**
     * 新增项目附件信息
     * 
     * @param projectAttachment 项目附件信息
     * @return 结果
     */
    @Override
    public int insertProjectAttachment(ProjectAttachment projectAttachment)
    {
        // 如果 id 为空，自动生成 UUID
        if (projectAttachment.getId() == null || projectAttachment.getId().isEmpty()) {
            projectAttachment.setId(IdUtils.fastSimpleUUID());
        }
        projectAttachment.setCreatedAt(DateUtils.getNowDate());
        projectAttachment.setCreatedBy(SecurityUtils.getUsername());
        return projectAttachmentMapper.insertProjectAttachment(projectAttachment);
    }

    /**
     * 修改项目附件信息
     * 
     * @param projectAttachment 项目附件信息
     * @return 结果
     */
    @Override
    public int updateProjectAttachment(ProjectAttachment projectAttachment)
    {
        projectAttachment.setUpdatedAt(DateUtils.getNowDate());
        projectAttachment.setUpdatedBy(SecurityUtils.getUsername());
        return projectAttachmentMapper.updateProjectAttachment(projectAttachment);
    }

    /**
     * 批量删除项目附件信息
     * 
     * @param ids 需要删除的项目附件信息主键
     * @return 结果
     */
    @Override
    public int deleteProjectAttachmentByIds(String[] ids)
    {
        return projectAttachmentMapper.deleteProjectAttachmentByIds(ids);
    }

    /**
     * 删除项目附件信息信息
     * 
     * @param id 项目附件信息主键
     * @return 结果
     */
    @Override
    public int deleteProjectAttachmentById(String id)
    {
        return projectAttachmentMapper.deleteProjectAttachmentById(id);
    }

    /**
     * 查询所有项目的合同总额
     * 
     * @return 合同总额
     */
    @Override
    public Double selectTotalContractAmount()
    {
        Double total = projectAttachmentMapper.selectTotalContractAmount();
        return total != null ? total : 0.0;
    }
}
