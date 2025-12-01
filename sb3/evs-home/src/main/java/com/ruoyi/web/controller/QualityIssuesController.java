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
import com.ruoyi.web.domain.QualityIssues;
import com.ruoyi.web.service.IQualityIssuesService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 质量问题Controller
 * 
 * @author evs
 * @date 2025-12-02
 */
@RestController
@RequestMapping("/evs/qualityIssues")
public class QualityIssuesController extends BaseController
{
    @Autowired
    private IQualityIssuesService qualityIssuesService;

    /**
     * 查询质量问题列表
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityIssues:list')")
    @GetMapping("/list")
    public TableDataInfo list(QualityIssues qualityIssues)
    {
        startPage();
        List<QualityIssues> list = qualityIssuesService.selectQualityIssuesList(qualityIssues);
        return getDataTable(list);
    }

    /**
     * 导出质量问题列表
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityIssues:export')")
    @Log(title = "质量问题", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QualityIssues qualityIssues)
    {
        List<QualityIssues> list = qualityIssuesService.selectQualityIssuesList(qualityIssues);
        ExcelUtil<QualityIssues> util = new ExcelUtil<QualityIssues>(QualityIssues.class);
        util.exportExcel(response, list, "质量问题数据");
    }

    /**
     * 获取质量问题详细信息
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityIssues:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(qualityIssuesService.selectQualityIssuesById(id));
    }

    /**
     * 新增质量问题
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityIssues:add')")
    @Log(title = "质量问题", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QualityIssues qualityIssues)
    {
        return toAjax(qualityIssuesService.insertQualityIssues(qualityIssues));
    }

    /**
     * 修改质量问题
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityIssues:edit')")
    @Log(title = "质量问题", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QualityIssues qualityIssues)
    {
        return toAjax(qualityIssuesService.updateQualityIssues(qualityIssues));
    }

    /**
     * 删除质量问题
     */
    @PreAuthorize("@ss.hasPermi('evs:qualityIssues:remove')")
    @Log(title = "质量问题", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(qualityIssuesService.deleteQualityIssuesByIds(ids));
    }
}
