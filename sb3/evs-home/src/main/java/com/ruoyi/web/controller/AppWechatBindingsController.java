package com.ruoyi.system.controller;

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
import com.ruoyi.system.domain.AppWechatBindings;
import com.ruoyi.system.service.IAppWechatBindingsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 微信绑定Controller
 * 
 * @author evs
 * @date 2026-01-19
 */
@RestController
@RequestMapping("/evs/appWechatBindings")
public class AppWechatBindingsController extends BaseController
{
    @Autowired
    private IAppWechatBindingsService appWechatBindingsService;

    /**
     * 查询微信绑定列表
     */
    @PreAuthorize("@ss.hasPermi('evs:appWechatBindings:list')")
    @GetMapping("/list")
    public TableDataInfo list(AppWechatBindings appWechatBindings)
    {
        startPage();
        List<AppWechatBindings> list = appWechatBindingsService.selectAppWechatBindingsList(appWechatBindings);
        return getDataTable(list);
    }

    /**
     * 导出微信绑定列表
     */
    @PreAuthorize("@ss.hasPermi('evs:appWechatBindings:export')")
    @Log(title = "微信绑定", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AppWechatBindings appWechatBindings)
    {
        List<AppWechatBindings> list = appWechatBindingsService.selectAppWechatBindingsList(appWechatBindings);
        ExcelUtil<AppWechatBindings> util = new ExcelUtil<AppWechatBindings>(AppWechatBindings.class);
        util.exportExcel(response, list, "微信绑定数据");
    }

    /**
     * 获取微信绑定详细信息
     */
    @PreAuthorize("@ss.hasPermi('evs:appWechatBindings:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(appWechatBindingsService.selectAppWechatBindingsById(id));
    }

    /**
     * 新增微信绑定
     */
    @PreAuthorize("@ss.hasPermi('evs:appWechatBindings:add')")
    @Log(title = "微信绑定", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AppWechatBindings appWechatBindings)
    {
        return toAjax(appWechatBindingsService.insertAppWechatBindings(appWechatBindings));
    }

    /**
     * 修改微信绑定
     */
    @PreAuthorize("@ss.hasPermi('evs:appWechatBindings:edit')")
    @Log(title = "微信绑定", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AppWechatBindings appWechatBindings)
    {
        return toAjax(appWechatBindingsService.updateAppWechatBindings(appWechatBindings));
    }

    /**
     * 删除微信绑定
     */
    @PreAuthorize("@ss.hasPermi('evs:appWechatBindings:remove')")
    @Log(title = "微信绑定", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(appWechatBindingsService.deleteAppWechatBindingsByIds(ids));
    }
}
