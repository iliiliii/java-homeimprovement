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
import com.ruoyi.web.domain.ProjectMembers;
import com.ruoyi.web.service.IProjectMembersService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 项目成员Controller
 * 
 * @author evs
 * @date 2025-11-24
 */
@RestController
@RequestMapping("/evs/projectMembers")
public class ProjectMembersController extends BaseController
{
    @Autowired
    private IProjectMembersService projectMembersService;

    /**
     * 查询项目成员列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectMembers:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjectMembers projectMembers)
    {
        startPage();
        List<ProjectMembers> list = projectMembersService.selectProjectMembersList(projectMembers);
        return getDataTable(list);
    }

    /**
     * 导出项目成员列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectMembers:export')")
    @Log(title = "项目成员", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProjectMembers projectMembers)
    {
        List<ProjectMembers> list = projectMembersService.selectProjectMembersList(projectMembers);
        ExcelUtil<ProjectMembers> util = new ExcelUtil<ProjectMembers>(ProjectMembers.class);
        util.exportExcel(response, list, "项目成员数据");
    }

    /**
     * 获取项目成员详细信息
     */
    @PreAuthorize("@ss.hasPermi('evs:projectMembers:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(projectMembersService.selectProjectMembersById(id));
    }

    /**
     * 新增项目成员
     */
    @PreAuthorize("@ss.hasPermi('evs:projectMembers:add')")
    @Log(title = "项目成员", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProjectMembers projectMembers)
    {
        return toAjax(projectMembersService.insertProjectMembers(projectMembers));
    }

    /**
     * 修改项目成员
     */
    @PreAuthorize("@ss.hasPermi('evs:projectMembers:edit')")
    @Log(title = "项目成员", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProjectMembers projectMembers)
    {
        return toAjax(projectMembersService.updateProjectMembers(projectMembers));
    }

    /**
     * 删除项目成员
     */
    @PreAuthorize("@ss.hasPermi('evs:projectMembers:remove')")
    @Log(title = "项目成员", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(projectMembersService.deleteProjectMembersByIds(ids));
    }
}
