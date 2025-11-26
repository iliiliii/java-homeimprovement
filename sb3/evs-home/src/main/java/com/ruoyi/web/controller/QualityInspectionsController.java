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
import com.ruoyi.web.domain.QualityInspections;
import com.ruoyi.web.service.IQualityInspectionsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 质量检测Controller
 * 
 * @author evs
 * @date 2025-11-26
 */
@RestController
@RequestMapping("/evs/qualityInspections")
public class QualityInspectionsController extends BaseController
{
    @Autowired
    private IQualityInspectionsService qualityInspectionsService;

    /**
     * 查询质量检测列表
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityInspections:list')")
    @GetMapping("/list")
    public TableDataInfo list(QualityInspections qualityInspections)
    {
        startPage();
        List<QualityInspections> list = qualityInspectionsService.selectQualityInspectionsList(qualityInspections);
        return getDataTable(list);
    }

    /**
     * 导出质量检测列表
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityInspections:export')")
    @Log(title = "质量检测", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QualityInspections qualityInspections)
    {
        List<QualityInspections> list = qualityInspectionsService.selectQualityInspectionsList(qualityInspections);
        ExcelUtil<QualityInspections> util = new ExcelUtil<QualityInspections>(QualityInspections.class);
        util.exportExcel(response, list, "质量检测数据");
    }

    /**
     * 获取质量检测详细信息
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityInspections:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(qualityInspectionsService.selectQualityInspectionsById(id));
    }

    /**
     * 新增质量检测
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityInspections:add')")
    @Log(title = "质量检测", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QualityInspections qualityInspections)
    {
        return toAjax(qualityInspectionsService.insertQualityInspections(qualityInspections));
    }

    /**
     * 修改质量检测
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityInspections:edit')")
    @Log(title = "质量检测", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QualityInspections qualityInspections)
    {
        return toAjax(qualityInspectionsService.updateQualityInspections(qualityInspections));
    }

    /**
     * 删除质量检测
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityInspections:remove')")
    @Log(title = "质量检测", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(qualityInspectionsService.deleteQualityInspectionsByIds(ids));
    }
}
