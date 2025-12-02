package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.QualityIssuesMapper;
import com.ruoyi.web.domain.QualityIssues;
import com.ruoyi.web.service.IQualityIssuesService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;

/**
 * 质量问题Service业务层处理
 * 
 * @author evs
 * @date 2025-12-02
 */
@Service
public class QualityIssuesServiceImpl implements IQualityIssuesService 
{
    @Autowired
    private QualityIssuesMapper qualityIssuesMapper;

    /**
     * 查询质量问题
     * 
     * @param id 质量问题主键
     * @return 质量问题
     */
    @Override
    public QualityIssues selectQualityIssuesById(String id)
    {
        return qualityIssuesMapper.selectQualityIssuesById(id);
    }

    /**
     * 查询质量问题列表
     * 
     * @param qualityIssues 质量问题
     * @return 质量问题
     */
    @Override
    public List<QualityIssues> selectQualityIssuesList(QualityIssues qualityIssues)
    {
        return qualityIssuesMapper.selectQualityIssuesList(qualityIssues);
    }

    /**
     * 新增质量问题
     * 
     * @param qualityIssues 质量问题
     * @return 结果
     */
    @Override
    public int insertQualityIssues(QualityIssues qualityIssues)
    {
        if (qualityIssues.getId() == null || qualityIssues.getId().isEmpty()) {
            qualityIssues.setId(IdUtils.fastSimpleUUID());
        }
        qualityIssues.setCreatedAt(DateUtils.getNowDate());
        qualityIssues.setCreatedBy(SecurityUtils.getUsername());
        return qualityIssuesMapper.insertQualityIssues(qualityIssues);
    }

    /**
     * 修改质量问题
     *
     * @param qualityIssues 质量问题
     * @return 结果
     */
    @Override
    public int updateQualityIssues(QualityIssues qualityIssues)
    {
        // 🔍 后端调试：记录问题状态更新数据
        System.out.println("🔍 [BACKEND] 接收到的问题状态更新数据:");
        System.out.println("  - id: " + qualityIssues.getId());
        System.out.println("  - status: " + qualityIssues.getStatus());
        System.out.println("  - resolvedAt: " + qualityIssues.getResolvedAt());

        qualityIssues.setUpdatedAt(DateUtils.getNowDate());
        qualityIssues.setUpdatedBy(SecurityUtils.getUsername());

        System.out.println("🔍 [BACKEND] 准备更新的完整问题对象: " + qualityIssues.toString());

        int result = qualityIssuesMapper.updateQualityIssues(qualityIssues);

        System.out.println("🔍 [BACKEND] 问题状态更新结果: " + result);

        return result;
    }

    /**
     * 批量删除质量问题
     * 
     * @param ids 需要删除的质量问题主键
     * @return 结果
     */
    @Override
    public int deleteQualityIssuesByIds(String[] ids)
    {
        return qualityIssuesMapper.deleteQualityIssuesByIds(ids);
    }

    /**
     * 删除质量问题信息
     * 
     * @param id 质量问题主键
     * @return 结果
     */
    @Override
    public int deleteQualityIssuesById(String id)
    {
        return qualityIssuesMapper.deleteQualityIssuesById(id);
    }
}
