package com.ruoyi.web.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.ProjectBudgets;
import com.ruoyi.web.service.IProjectBudgetsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 预算明细Controller
 * 
 * @author evs
 * @date 2025-11-23
 */
@RestController
@RequestMapping("/evs/projectBudgets")
public class ProjectBudgetsController extends BaseController
{
    @Autowired
    private IProjectBudgetsService projectBudgetsService;

    /**
     * 查询预算明细列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectBudgets:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjectBudgets projectBudgets)
    {
        startPage();
        List<ProjectBudgets> list = projectBudgetsService.selectProjectBudgetsList(projectBudgets);
        return getDataTable(list);
    }

    /**
     * 导出预算明细列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectBudgets:export')")
    @Log(title = "预算明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProjectBudgets projectBudgets)
    {
        List<ProjectBudgets> list = projectBudgetsService.selectProjectBudgetsList(projectBudgets);
        ExcelUtil<ProjectBudgets> util = new ExcelUtil<ProjectBudgets>(ProjectBudgets.class);
        util.exportExcel(response, list, "预算明细数据");
    }

    /**
     * 获取预算明细详细信息
     */
    @PreAuthorize("@ss.hasPermi('evs:projectBudgets:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(projectBudgetsService.selectProjectBudgetsById(id));
    }

    /**
     * 新增预算明细
     */
    @PreAuthorize("@ss.hasPermi('evs:projectBudgets:add')")
    @Log(title = "预算明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProjectBudgets projectBudgets)
    {
        return toAjax(projectBudgetsService.insertProjectBudgets(projectBudgets));
    }

    /**
     * 修改预算明细
     */
    @PreAuthorize("@ss.hasPermi('evs:projectBudgets:edit')")
    @Log(title = "预算明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProjectBudgets projectBudgets)
    {
        return toAjax(projectBudgetsService.updateProjectBudgets(projectBudgets));
    }

    /**
     * 删除预算明细
     */
    @PreAuthorize("@ss.hasPermi('evs:projectBudgets:remove')")
    @Log(title = "预算明细", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(projectBudgetsService.deleteProjectBudgetsByIds(ids));
    }
}
