package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.QualityFixes;

/**
 * 问题修复Service接口
 * 
 * @author evs
 * @date 2025-12-02
 */
public interface IQualityFixesService 
{
    /**
     * 查询问题修复
     * 
     * @param id 问题修复主键
     * @return 问题修复
     */
    public QualityFixes selectQualityFixesById(String id);

    /**
     * 查询问题修复列表
     * 
     * @param qualityFixes 问题修复
     * @return 问题修复集合
     */
    public List<QualityFixes> selectQualityFixesList(QualityFixes qualityFixes);

    /**
     * 新增问题修复
     * 
     * @param qualityFixes 问题修复
     * @return 结果
     */
    public int insertQualityFixes(QualityFixes qualityFixes);

    /**
     * 修改问题修复
     * 
     * @param qualityFixes 问题修复
     * @return 结果
     */
    public int updateQualityFixes(QualityFixes qualityFixes);

    /**
     * 批量删除问题修复
     * 
     * @param ids 需要删除的问题修复主键集合
     * @return 结果
     */
    public int deleteQualityFixesByIds(String[] ids);

    /**
     * 删除问题修复信息
     * 
     * @param id 问题修复主键
     * @return 结果
     */
    public int deleteQualityFixesById(String id);
}
