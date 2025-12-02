package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.QualityInspections;

/**
 * 质量检测Service接口
 * 
 * @author evs
 * @date 2025-12-02
 */
public interface IQualityInspectionsService 
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
     * @return 创建的质量检测记录
     */
    public QualityInspections insertQualityInspections(QualityInspections qualityInspections);

    /**
     * 修改质量检测
     * 
     * @param qualityInspections 质量检测
     * @return 结果
     */
    public int updateQualityInspections(QualityInspections qualityInspections);

    /**
     * 批量删除质量检测
     * 
     * @param ids 需要删除的质量检测主键集合
     * @return 结果
     */
    public int deleteQualityInspectionsByIds(String[] ids);

    /**
     * 删除质量检测信息
     *
     * @param id 质量检测主键
     * @return 结果
     */
    public int deleteQualityInspectionsById(String id);

    /**
     * 按项目ID查询质检记录及对应问题（使用JOIN查询，解决N+1问题）
     *
     * @param projectId 项目ID
     * @return 质检记录集合（包含问题列表）
     */
    public List<QualityInspections> selectQualityInspectionsWithIssuesByProjectId(String projectId);
}
