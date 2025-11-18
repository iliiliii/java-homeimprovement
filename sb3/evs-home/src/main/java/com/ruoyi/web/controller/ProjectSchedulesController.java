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
import com.ruoyi.web.domain.ProjectSchedules;
import com.ruoyi.web.service.IProjectSchedulesService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 项目进度Controller
 * 
 * @author evs
 * @date 2025-11-18
 */
@RestController
@RequestMapping("/evs/projectSchedules")
public class ProjectSchedulesController extends BaseController
{
    @Autowired
    private IProjectSchedulesService projectSchedulesService;

    /**
     * 查询项目进度列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectSchedules:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjectSchedules projectSchedules)
    {
        startPage();
        List<ProjectSchedules> list = projectSchedulesService.selectProjectSchedulesList(projectSchedules);
        return getDataTable(list);
    }

    /**
     * 导出项目进度列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectSchedules:export')")
    @Log(title = "项目进度", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProjectSchedules projectSchedules)
    {
        List<ProjectSchedules> list = projectSchedulesService.selectProjectSchedulesList(projectSchedules);
        ExcelUtil<ProjectSchedules> util = new ExcelUtil<ProjectSchedules>(ProjectSchedules.class);
        util.exportExcel(response, list, "项目进度数据");
    }

    /**
     * 获取项目进度详细信息
     */
    @PreAuthorize("@ss.hasPermi('evs:projectSchedules:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(projectSchedulesService.selectProjectSchedulesById(id));
    }

    /**
     * 新增项目进度
     */
    @PreAuthorize("@ss.hasPermi('evs:projectSchedules:add')")
    @Log(title = "项目进度", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProjectSchedules projectSchedules)
    {
        return toAjax(projectSchedulesService.insertProjectSchedules(projectSchedules));
    }

    /**
     * 修改项目进度
     */
    @PreAuthorize("@ss.hasPermi('evs:projectSchedules:edit')")
    @Log(title = "项目进度", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProjectSchedules projectSchedules)
    {
        return toAjax(projectSchedulesService.updateProjectSchedules(projectSchedules));
    }

    /**
     * 删除项目进度
     */
    @PreAuthorize("@ss.hasPermi('evs:projectSchedules:remove')")
    @Log(title = "项目进度", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(projectSchedulesService.deleteProjectSchedulesByIds(ids));
    }
}
