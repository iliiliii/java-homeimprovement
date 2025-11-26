package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.QualityInspectionsMapper;
import com.ruoyi.web.domain.QualityInspections;
import com.ruoyi.web.service.IQualityInspectionsService;

/**
 * 质量检测Service业务层处理
 * 
 * @author evs
 * @date 2025-11-26
 */
@Service
public class QualityInspectionsServiceImpl implements IQualityInspectionsService 
{
    @Autowired
    private QualityInspectionsMapper qualityInspectionsMapper;

    /**
     * 查询质量检测
     * 
     * @param id 质量检测主键
     * @return 质量检测
     */
    @Override
    public QualityInspections selectQualityInspectionsById(String id)
    {
        return qualityInspectionsMapper.selectQualityInspectionsById(id);
    }

    /**
     * 查询质量检测列表
     * 
     * @param qualityInspections 质量检测
     * @return 质量检测
     */
    @Override
    public List<QualityInspections> selectQualityInspectionsList(QualityInspections qualityInspections)
    {
        return qualityInspectionsMapper.selectQualityInspectionsList(qualityInspections);
    }

    /**
     * 新增质量检测
     * 
     * @param qualityInspections 质量检测
     * @return 结果
     */
    @Override
    public int insertQualityInspections(QualityInspections qualityInspections)
    {
        return qualityInspectionsMapper.insertQualityInspections(qualityInspections);
    }

    /**
     * 修改质量检测
     * 
     * @param qualityInspections 质量检测
     * @return 结果
     */
    @Override
    public int updateQualityInspections(QualityInspections qualityInspections)
    {
        return qualityInspectionsMapper.updateQualityInspections(qualityInspections);
    }

    /**
     * 批量删除质量检测
     * 
     * @param ids 需要删除的质量检测主键
     * @return 结果
     */
    @Override
    public int deleteQualityInspectionsByIds(String[] ids)
    {
        return qualityInspectionsMapper.deleteQualityInspectionsByIds(ids);
    }

    /**
     * 删除质量检测信息
     * 
     * @param id 质量检测主键
     * @return 结果
     */
    @Override
    public int deleteQualityInspectionsById(String id)
    {
        return qualityInspectionsMapper.deleteQualityInspectionsById(id);
    }
}
