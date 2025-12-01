package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.QualityFixes;

/**
 * 问题修复Mapper接口
 * 
 * @author evs
 * @date 2025-12-02
 */
public interface QualityFixesMapper 
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
     * 删除问题修复
     * 
     * @param id 问题修复主键
     * @return 结果
     */
    public int deleteQualityFixesById(String id);

    /**
     * 批量删除问题修复
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteQualityFixesByIds(String[] ids);
}
