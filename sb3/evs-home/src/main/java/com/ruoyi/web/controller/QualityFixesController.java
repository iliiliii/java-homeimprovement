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
import com.ruoyi.web.domain.QualityFixes;
import com.ruoyi.web.service.IQualityFixesService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 问题修复Controller
 * 
 * @author evs
 * @date 2025-12-02
 */
@RestController
@RequestMapping("/evs/qualityFixes")
public class QualityFixesController extends BaseController
{
    @Autowired
    private IQualityFixesService qualityFixesService;

    /**
     * 查询问题修复列表
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityFixes:list')")
    @GetMapping("/list")
    public TableDataInfo list(QualityFixes qualityFixes)
    {
        startPage();
        List<QualityFixes> list = qualityFixesService.selectQualityFixesList(qualityFixes);
        return getDataTable(list);
    }

    /**
     * 导出问题修复列表
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityFixes:export')")
    @Log(title = "问题修复", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QualityFixes qualityFixes)
    {
        List<QualityFixes> list = qualityFixesService.selectQualityFixesList(qualityFixes);
        ExcelUtil<QualityFixes> util = new ExcelUtil<QualityFixes>(QualityFixes.class);
        util.exportExcel(response, list, "问题修复数据");
    }

    /**
     * 获取问题修复详细信息
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityFixes:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(qualityFixesService.selectQualityFixesById(id));
    }

    /**
     * 根据问题ID获取整改记录列表
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityFixes:query')")
    @GetMapping(value = "/byIssue/{issueId}")
    public AjaxResult getFixesByIssueId(@PathVariable("issueId") String issueId)
    {
        QualityFixes query = new QualityFixes();
        query.setQualityIssuesId(issueId);
        List<QualityFixes> list = qualityFixesService.selectQualityFixesList(query);
        return success(list);
    }

    /**
     * 新增问题修复
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityFixes:add')")
    @Log(title = "问题修复", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QualityFixes qualityFixes)
    {
        return toAjax(qualityFixesService.insertQualityFixes(qualityFixes));
    }

    /**
     * 修改问题修复
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityFixes:edit')")
    @Log(title = "问题修复", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QualityFixes qualityFixes)
    {
        return toAjax(qualityFixesService.updateQualityFixes(qualityFixes));
    }

    /**
     * 删除问题修复
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityFixes:remove')")
    @Log(title = "问题修复", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(qualityFixesService.deleteQualityFixesByIds(ids));
    }
}
