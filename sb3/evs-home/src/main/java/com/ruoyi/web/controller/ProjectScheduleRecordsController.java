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
import com.ruoyi.web.domain.ProjectScheduleRecords;
import com.ruoyi.web.service.IProjectScheduleRecordsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 进度记录Controller
 * 
 * @author eve
 * @date 2025-11-24
 */
@RestController
@RequestMapping("/evs/projectScheduleRecords")
public class ProjectScheduleRecordsController extends BaseController
{
    @Autowired
    private IProjectScheduleRecordsService projectScheduleRecordsService;

    /**
     * 查询进度记录列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectScheduleRecords:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjectScheduleRecords projectScheduleRecords)
    {
        startPage();
        List<ProjectScheduleRecords> list = projectScheduleRecordsService.selectProjectScheduleRecordsList(projectScheduleRecords);
        return getDataTable(list);
    }

    /**
     * 导出进度记录列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectScheduleRecords:export')")
    @Log(title = "进度记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProjectScheduleRecords projectScheduleRecords)
    {
        List<ProjectScheduleRecords> list = projectScheduleRecordsService.selectProjectScheduleRecordsList(projectScheduleRecords);
        ExcelUtil<ProjectScheduleRecords> util = new ExcelUtil<ProjectScheduleRecords>(ProjectScheduleRecords.class);
        util.exportExcel(response, list, "进度记录数据");
    }

    /**
     * 获取进度记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('evs:projectScheduleRecords:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(projectScheduleRecordsService.selectProjectScheduleRecordsById(id));
    }

    /**
     * 新增进度记录
     */
    @PreAuthorize("@ss.hasPermi('evs:projectScheduleRecords:add')")
    @Log(title = "进度记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProjectScheduleRecords projectScheduleRecords)
    {
        return toAjax(projectScheduleRecordsService.insertProjectScheduleRecords(projectScheduleRecords));
    }

    /**
     * 修改进度记录
     */
    @PreAuthorize("@ss.hasPermi('evs:projectScheduleRecords:edit')")
    @Log(title = "进度记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProjectScheduleRecords projectScheduleRecords)
    {
        return toAjax(projectScheduleRecordsService.updateProjectScheduleRecords(projectScheduleRecords));
    }

    /**
     * 删除进度记录
     */
    @PreAuthorize("@ss.hasPermi('evs:projectScheduleRecords:remove')")
    @Log(title = "进度记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(projectScheduleRecordsService.deleteProjectScheduleRecordsByIds(ids));
    }
}
