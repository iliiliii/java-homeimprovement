package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.QualityFixesMapper;
import com.ruoyi.web.domain.QualityFixes;
import com.ruoyi.web.service.IQualityFixesService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;

/**
 * 问题修复Service业务层处理
 * 
 * @author evs
 * @date 2025-12-02
 */
@Service
public class QualityFixesServiceImpl implements IQualityFixesService 
{
    @Autowired
    private QualityFixesMapper qualityFixesMapper;

    /**
     * 查询问题修复
     * 
     * @param id 问题修复主键
     * @return 问题修复
     */
    @Override
    public QualityFixes selectQualityFixesById(String id)
    {
        return qualityFixesMapper.selectQualityFixesById(id);
    }

    /**
     * 查询问题修复列表
     * 
     * @param qualityFixes 问题修复
     * @return 问题修复
     */
    @Override
    public List<QualityFixes> selectQualityFixesList(QualityFixes qualityFixes)
    {
        return qualityFixesMapper.selectQualityFixesList(qualityFixes);
    }

    /**
     * 新增问题修复
     * 
     * @param qualityFixes 问题修复
     * @return 结果
     */
    @Override
    public int insertQualityFixes(QualityFixes qualityFixes)
    {
        if (qualityFixes.getId() == null || qualityFixes.getId().isEmpty()) {
            qualityFixes.setId(IdUtils.fastSimpleUUID());
        }
        qualityFixes.setCreatedAt(DateUtils.getNowDate());
        qualityFixes.setCreatedBy(SecurityUtils.getUsername());
        return qualityFixesMapper.insertQualityFixes(qualityFixes);
    }

    /**
     * 修改问题修复
     * 
     * @param qualityFixes 问题修复
     * @return 结果
     */
    @Override
    public int updateQualityFixes(QualityFixes qualityFixes)
    {
        qualityFixes.setUpdatedAt(DateUtils.getNowDate());
        qualityFixes.setUpdatedBy(SecurityUtils.getUsername());
        return qualityFixesMapper.updateQualityFixes(qualityFixes);
    }

    /**
     * 批量删除问题修复
     * 
     * @param ids 需要删除的问题修复主键
     * @return 结果
     */
    @Override
    public int deleteQualityFixesByIds(String[] ids)
    {
        return qualityFixesMapper.deleteQualityFixesByIds(ids);
    }

    /**
     * 删除问题修复信息
     * 
     * @param id 问题修复主键
     * @return 结果
     */
    @Override
    public int deleteQualityFixesById(String id)
    {
        return qualityFixesMapper.deleteQualityFixesById(id);
    }
}
