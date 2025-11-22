package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.ProjectBudgetsMapper;
import com.ruoyi.web.domain.ProjectBudgets;
import com.ruoyi.web.service.IProjectBudgetsService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;

/**
 * 预算明细Service业务层处理
 * 
 * @author evs
 * @date 2025-11-23
 */
@Service
public class ProjectBudgetsServiceImpl implements IProjectBudgetsService 
{
    @Autowired
    private ProjectBudgetsMapper projectBudgetsMapper;

    /**
     * 查询预算明细
     * 
     * @param id 预算明细主键
     * @return 预算明细
     */
    @Override
    public ProjectBudgets selectProjectBudgetsById(String id)
    {
        return projectBudgetsMapper.selectProjectBudgetsById(id);
    }

    /**
     * 查询预算明细列表
     * 
     * @param projectBudgets 预算明细
     * @return 预算明细
     */
    @Override
    public List<ProjectBudgets> selectProjectBudgetsList(ProjectBudgets projectBudgets)
    {
        return projectBudgetsMapper.selectProjectBudgetsList(projectBudgets);
    }

    /**
     * 新增预算明细
     * 
     * @param projectBudgets 预算明细
     * @return 结果
     */
    @Override
    public int insertProjectBudgets(ProjectBudgets projectBudgets)
    {
        // 如果 id 为空，自动生成 UUID
        if (projectBudgets.getId() == null || projectBudgets.getId().isEmpty()) {
            projectBudgets.setId(IdUtils.fastSimpleUUID());
        }
        projectBudgets.setCreatedAt(DateUtils.getNowDate());
        projectBudgets.setCreatedBy(SecurityUtils.getUsername());
        return projectBudgetsMapper.insertProjectBudgets(projectBudgets);
    }

    /**
     * 修改预算明细
     * 
     * @param projectBudgets 预算明细
     * @return 结果
     */
    @Override
    public int updateProjectBudgets(ProjectBudgets projectBudgets)
    {
        projectBudgets.setUpdatedAt(DateUtils.getNowDate());
        projectBudgets.setUpdatedBy(SecurityUtils.getUsername());
        return projectBudgetsMapper.updateProjectBudgets(projectBudgets);
    }

    /**
     * 批量删除预算明细
     * 
     * @param ids 需要删除的预算明细主键
     * @return 结果
     */
    @Override
    public int deleteProjectBudgetsByIds(String[] ids)
    {
        return projectBudgetsMapper.deleteProjectBudgetsByIds(ids);
    }

    /**
     * 删除预算明细信息
     * 
     * @param id 预算明细主键
     * @return 结果
     */
    @Override
    public int deleteProjectBudgetsById(String id)
    {
        return projectBudgetsMapper.deleteProjectBudgetsById(id);
    }
}
