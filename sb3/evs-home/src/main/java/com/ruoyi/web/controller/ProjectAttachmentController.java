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
import com.ruoyi.web.domain.ProjectAttachment;
import com.ruoyi.web.service.IProjectAttachmentService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 项目附件信息Controller
 * 
 * @author evs
 * @date 2025-12-29
 */
@RestController
@RequestMapping("/evs/projectAttachment")
public class ProjectAttachmentController extends BaseController
{
    @Autowired
    private IProjectAttachmentService projectAttachmentService;

    /**
     * 查询项目附件信息列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectAttachment:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjectAttachment projectAttachment)
    {
        startPage();
        List<ProjectAttachment> list = projectAttachmentService.selectProjectAttachmentList(projectAttachment);
        return getDataTable(list);
    }

    /**
     * 导出项目附件信息列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectAttachment:export')")
    @Log(title = "项目附件信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProjectAttachment projectAttachment)
    {
        List<ProjectAttachment> list = projectAttachmentService.selectProjectAttachmentList(projectAttachment);
        ExcelUtil<ProjectAttachment> util = new ExcelUtil<ProjectAttachment>(ProjectAttachment.class);
        util.exportExcel(response, list, "项目附件信息数据");
    }

    /**
     * 获取项目附件信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('evs:projectAttachment:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(projectAttachmentService.selectProjectAttachmentById(id));
    }

    /**
     * 新增项目附件信息
     */
    @PreAuthorize("@ss.hasPermi('evs:projectAttachment:add')")
    @Log(title = "项目附件信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProjectAttachment projectAttachment)
    {
        return toAjax(projectAttachmentService.insertProjectAttachment(projectAttachment));
    }

    /**
     * 修改项目附件信息
     */
    @PreAuthorize("@ss.hasPermi('evs:projectAttachment:edit')")
    @Log(title = "项目附件信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProjectAttachment projectAttachment)
    {
        return toAjax(projectAttachmentService.updateProjectAttachment(projectAttachment));
    }

    /**
     * 删除项目附件信息
     */
    @PreAuthorize("@ss.hasPermi('evs:projectAttachment:remove')")
    @Log(title = "项目附件信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(projectAttachmentService.deleteProjectAttachmentByIds(ids));
    }

    /**
     * 获取所有项目的合同总额
     */
    @PreAuthorize("@ss.hasPermi('evs:projectAttachment:list')")
    @GetMapping("/totalAmount")
    public AjaxResult getTotalContractAmount()
    {
        return success(projectAttachmentService.selectTotalContractAmount());
    }
}
