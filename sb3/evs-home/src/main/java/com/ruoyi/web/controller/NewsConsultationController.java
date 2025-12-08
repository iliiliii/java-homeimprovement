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
import com.ruoyi.web.domain.NewsConsultation;
import com.ruoyi.web.service.INewsConsultationService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 新闻咨询设置Controller
 * 
 * @author evs
 * @date 2025-12-08
 */
@RestController
@RequestMapping("/evs/newsConsultation")
public class NewsConsultationController extends BaseController
{
    @Autowired
    private INewsConsultationService newsConsultationService;

    /**
     * 查询新闻咨询设置列表
     */
    @PreAuthorize("@ss.hasPermi('evs:newsConsultation:list')")
    @GetMapping("/list")
    public TableDataInfo list(NewsConsultation newsConsultation)
    {
        startPage();
        List<NewsConsultation> list = newsConsultationService.selectNewsConsultationList(newsConsultation);
        return getDataTable(list);
    }

    /**
     * 导出新闻咨询设置列表
     */
    @PreAuthorize("@ss.hasPermi('evs:newsConsultation:export')")
    @Log(title = "新闻咨询设置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NewsConsultation newsConsultation)
    {
        List<NewsConsultation> list = newsConsultationService.selectNewsConsultationList(newsConsultation);
        ExcelUtil<NewsConsultation> util = new ExcelUtil<NewsConsultation>(NewsConsultation.class);
        util.exportExcel(response, list, "新闻咨询设置数据");
    }

    /**
     * 获取新闻咨询设置详细信息
     */
    @PreAuthorize("@ss.hasPermi('evs:newsConsultation:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(newsConsultationService.selectNewsConsultationById(id));
    }

    /**
     * 新增新闻咨询设置
     */
    @PreAuthorize("@ss.hasPermi('evs:newsConsultation:add')")
    @Log(title = "新闻咨询设置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody NewsConsultation newsConsultation)
    {
        return toAjax(newsConsultationService.insertNewsConsultation(newsConsultation));
    }

    /**
     * 修改新闻咨询设置
     */
    @PreAuthorize("@ss.hasPermi('evs:newsConsultation:edit')")
    @Log(title = "新闻咨询设置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody NewsConsultation newsConsultation)
    {
        return toAjax(newsConsultationService.updateNewsConsultation(newsConsultation));
    }

    /**
     * 删除新闻咨询设置
     */
    @PreAuthorize("@ss.hasPermi('evs:newsConsultation:remove')")
    @Log(title = "新闻咨询设置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(newsConsultationService.deleteNewsConsultationByIds(ids));
    }
}
