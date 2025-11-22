package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.ProjectBudgets;

/**
 * 预算明细Service接口
 * 
 * @author evs
 * @date 2025-11-23
 */
public interface IProjectBudgetsService 
{
    /**
     * 查询预算明细
     * 
     * @param id 预算明细主键
     * @return 预算明细
     */
    public ProjectBudgets selectProjectBudgetsById(String id);

    /**
     * 查询预算明细列表
     * 
     * @param projectBudgets 预算明细
     * @return 预算明细集合
     */
    public List<ProjectBudgets> selectProjectBudgetsList(ProjectBudgets projectBudgets);

    /**
     * 新增预算明细
     * 
     * @param projectBudgets 预算明细
     * @return 结果
     */
    public int insertProjectBudgets(ProjectBudgets projectBudgets);

    /**
     * 修改预算明细
     * 
     * @param projectBudgets 预算明细
     * @return 结果
     */
    public int updateProjectBudgets(ProjectBudgets projectBudgets);

    /**
     * 批量删除预算明细
     * 
     * @param ids 需要删除的预算明细主键集合
     * @return 结果
     */
    public int deleteProjectBudgetsByIds(String[] ids);

    /**
     * 删除预算明细信息
     * 
     * @param id 预算明细主键
     * @return 结果
     */
    public int deleteProjectBudgetsById(String id);
}
