package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.ProjectBudgets;

/**
 * 预算明细Mapper接口
 * 
 * @author evs
 * @date 2025-11-23
 */
public interface ProjectBudgetsMapper 
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
     * 删除预算明细
     * 
     * @param id 预算明细主键
     * @return 结果
     */
    public int deleteProjectBudgetsById(String id);

    /**
     * 批量删除预算明细
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProjectBudgetsByIds(String[] ids);
}
