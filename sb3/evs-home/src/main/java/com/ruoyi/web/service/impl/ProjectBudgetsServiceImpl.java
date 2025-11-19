package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.ProjectBudgetsMapper;
import com.ruoyi.web.domain.ProjectBudgets;
import com.ruoyi.web.service.IProjectBudgetsService;
import com.ruoyi.common.utils.uuid.IdUtils;

/**
 * 项目预算Service业务层处理
 * 
 * @author evs
 * @date 2025-11-18
 */
@Service
public class ProjectBudgetsServiceImpl implements IProjectBudgetsService 
{
    @Autowired
    private ProjectBudgetsMapper projectBudgetsMapper;

    /**
     * 查询项目预算
     * 
     * @param id 项目预算主键
     * @return 项目预算
     */
    @Override
    public ProjectBudgets selectProjectBudgetsById(String id)
    {
        return projectBudgetsMapper.selectProjectBudgetsById(id);
    }

    /**
     * 查询项目预算列表
     * 
     * @param projectBudgets 项目预算
     * @return 项目预算
     */
    @Override
    public List<ProjectBudgets> selectProjectBudgetsList(ProjectBudgets projectBudgets)
    {
        return projectBudgetsMapper.selectProjectBudgetsList(projectBudgets);
    }

    /**
     * 新增项目预算
     * 
     * @param projectBudgets 项目预算
     * @return 结果
     */
    @Override
    public int insertProjectBudgets(ProjectBudgets projectBudgets)
    {
        // 如果 id 为空，自动生成 UUID
        if (projectBudgets.getId() == null || projectBudgets.getId().isEmpty()) {
            projectBudgets.setId(IdUtils.fastSimpleUUID());
        }
        return projectBudgetsMapper.insertProjectBudgets(projectBudgets);
    }

    /**
     * 修改项目预算
     * 
     * @param projectBudgets 项目预算
     * @return 结果
     */
    @Override
    public int updateProjectBudgets(ProjectBudgets projectBudgets)
    {
        return projectBudgetsMapper.updateProjectBudgets(projectBudgets);
    }

    /**
     * 批量删除项目预算
     * 
     * @param ids 需要删除的项目预算主键
     * @return 结果
     */
    @Override
    public int deleteProjectBudgetsByIds(String[] ids)
    {
        return projectBudgetsMapper.deleteProjectBudgetsByIds(ids);
    }

    /**
     * 删除项目预算信息
     * 
     * @param id 项目预算主键
     * @return 结果
     */
    @Override
    public int deleteProjectBudgetsById(String id)
    {
        return projectBudgetsMapper.deleteProjectBudgetsById(id);
    }
}
