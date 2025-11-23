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
import com.ruoyi.web.domain.FileUploads;
import com.ruoyi.web.service.IFileUploadsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 文件上传Controller
 * 
 * @author evs
 * @date 2025-11-23
 */
@RestController
@RequestMapping("/evs/fileUploads")
public class FileUploadsController extends BaseController
{
    @Autowired
    private IFileUploadsService fileUploadsService;

    /**
     * 查询文件上传列表
     */
    @PreAuthorize("@ss.hasPermi('evs:fileUploads:list')")
    @GetMapping("/list")
    public TableDataInfo list(FileUploads fileUploads)
    {
        startPage();
        List<FileUploads> list = fileUploadsService.selectFileUploadsList(fileUploads);
        return getDataTable(list);
    }

    /**
     * 导出文件上传列表
     */
    @PreAuthorize("@ss.hasPermi('evs:fileUploads:export')")
    @Log(title = "文件上传", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FileUploads fileUploads)
    {
        List<FileUploads> list = fileUploadsService.selectFileUploadsList(fileUploads);
        ExcelUtil<FileUploads> util = new ExcelUtil<FileUploads>(FileUploads.class);
        util.exportExcel(response, list, "文件上传数据");
    }

    /**
     * 获取文件上传详细信息
     */
    @PreAuthorize("@ss.hasPermi('evs:fileUploads:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(fileUploadsService.selectFileUploadsById(id));
    }

    /**
     * 新增文件上传
     */
    @PreAuthorize("@ss.hasPermi('evs:fileUploads:add')")
    @Log(title = "文件上传", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FileUploads fileUploads)
    {
        int result = fileUploadsService.insertFileUploads(fileUploads);
        if (result > 0) {
            // 返回文件对象，包含生成的ID
            return success(fileUploads);
        }
        return error("新增文件上传失败");
    }

    /**
     * 修改文件上传
     */
    @PreAuthorize("@ss.hasPermi('evs:fileUploads:edit')")
    @Log(title = "文件上传", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FileUploads fileUploads)
    {
        return toAjax(fileUploadsService.updateFileUploads(fileUploads));
    }

    /**
     * 删除文件上传
     */
    @PreAuthorize("@ss.hasPermi('evs:fileUploads:remove')")
    @Log(title = "文件上传", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(fileUploadsService.deleteFileUploadsByIds(ids));
    }
}
