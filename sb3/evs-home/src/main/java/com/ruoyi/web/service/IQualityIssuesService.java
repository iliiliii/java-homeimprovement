package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.QualityIssues;

/**
 * 质量问题Service接口
 * 
 * @author evs
 * @date 2025-12-02
 */
public interface IQualityIssuesService 
{
    /**
     * 查询质量问题
     * 
     * @param id 质量问题主键
     * @return 质量问题
     */
    public QualityIssues selectQualityIssuesById(String id);

    /**
     * 查询质量问题列表
     * 
     * @param qualityIssues 质量问题
     * @return 质量问题集合
     */
    public List<QualityIssues> selectQualityIssuesList(QualityIssues qualityIssues);

    /**
     * 新增质量问题
     * 
     * @param qualityIssues 质量问题
     * @return 结果
     */
    public int insertQualityIssues(QualityIssues qualityIssues);

    /**
     * 修改质量问题
     * 
     * @param qualityIssues 质量问题
     * @return 结果
     */
    public int updateQualityIssues(QualityIssues qualityIssues);

    /**
     * 批量删除质量问题
     * 
     * @param ids 需要删除的质量问题主键集合
     * @return 结果
     */
    public int deleteQualityIssuesByIds(String[] ids);

    /**
     * 删除质量问题信息
     * 
     * @param id 质量问题主键
     * @return 结果
     */
    public int deleteQualityIssuesById(String id);
}
