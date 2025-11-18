package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.ProjectBudgets;

/**
 * 项目预算Service接口
 * 
 * @author evs
 * @date 2025-11-18
 */
public interface IProjectBudgetsService 
{
    /**
     * 查询项目预算
     * 
     * @param id 项目预算主键
     * @return 项目预算
     */
    public ProjectBudgets selectProjectBudgetsById(String id);

    /**
     * 查询项目预算列表
     * 
     * @param projectBudgets 项目预算
     * @return 项目预算集合
     */
    public List<ProjectBudgets> selectProjectBudgetsList(ProjectBudgets projectBudgets);

    /**
     * 新增项目预算
     * 
     * @param projectBudgets 项目预算
     * @return 结果
     */
    public int insertProjectBudgets(ProjectBudgets projectBudgets);

    /**
     * 修改项目预算
     * 
     * @param projectBudgets 项目预算
     * @return 结果
     */
    public int updateProjectBudgets(ProjectBudgets projectBudgets);

    /**
     * 批量删除项目预算
     * 
     * @param ids 需要删除的项目预算主键集合
     * @return 结果
     */
    public int deleteProjectBudgetsByIds(String[] ids);

    /**
     * 删除项目预算信息
     * 
     * @param id 项目预算主键
     * @return 结果
     */
    public int deleteProjectBudgetsById(String id);
}
