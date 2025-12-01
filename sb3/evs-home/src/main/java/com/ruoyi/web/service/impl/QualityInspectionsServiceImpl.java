package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.QualityInspectionsMapper;
import com.ruoyi.web.domain.QualityInspections;
import com.ruoyi.web.service.IQualityInspectionsService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;

/**
 * 质量检测Service业务层处理
 * 
 * @author evs
 * @date 2025-12-02
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
    public QualityInspections insertQualityInspections(QualityInspections qualityInspections)
    {
        if (qualityInspections.getId() == null || qualityInspections.getId().isEmpty()) {
            qualityInspections.setId(IdUtils.fastSimpleUUID());
        }
        qualityInspections.setCreatedAt(DateUtils.getNowDate());
        qualityInspections.setCreatedBy(SecurityUtils.getUsername());
        qualityInspectionsMapper.insertQualityInspections(qualityInspections);
        return qualityInspections;
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
        qualityInspections.setUpdatedAt(DateUtils.getNowDate());
        qualityInspections.setUpdatedBy(SecurityUtils.getUsername());
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
