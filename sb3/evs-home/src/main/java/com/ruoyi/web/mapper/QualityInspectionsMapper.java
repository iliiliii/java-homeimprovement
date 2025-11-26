package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.QualityInspections;

/**
 * 质量检测Mapper接口
 * 
 * @author evs
 * @date 2025-11-26
 */
public interface QualityInspectionsMapper 
{
    /**
     * 查询质量检测
     * 
     * @param id 质量检测主键
     * @return 质量检测
     */
    public QualityInspections selectQualityInspectionsById(String id);

    /**
     * 查询质量检测列表
     * 
     * @param qualityInspections 质量检测
     * @return 质量检测集合
     */
    public List<QualityInspections> selectQualityInspectionsList(QualityInspections qualityInspections);

    /**
     * 新增质量检测
     * 
     * @param qualityInspections 质量检测
     * @return 结果
     */
    public int insertQualityInspections(QualityInspections qualityInspections);

    /**
     * 修改质量检测
     * 
     * @param qualityInspections 质量检测
     * @return 结果
     */
    public int updateQualityInspections(QualityInspections qualityInspections);

    /**
     * 删除质量检测
     * 
     * @param id 质量检测主键
     * @return 结果
     */
    public int deleteQualityInspectionsById(String id);

    /**
     * 批量删除质量检测
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteQualityInspectionsByIds(String[] ids);
}
