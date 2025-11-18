package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.ProjectBudgets;

/**
 * 项目预算Mapper接口
 * 
 * @author evs
 * @date 2025-11-18
 */
public interface ProjectBudgetsMapper 
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
     * 删除项目预算
     * 
     * @param id 项目预算主键
     * @return 结果
     */
    public int deleteProjectBudgetsById(String id);

    /**
     * 批量删除项目预算
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProjectBudgetsByIds(String[] ids);
}
