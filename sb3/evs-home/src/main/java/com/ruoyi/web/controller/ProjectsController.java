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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.service.IProjectsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 项目信息Controller
 * 
 * @author evs
 * @date 2025-11-23
 */
@RestController
@RequestMapping("/evs/projects")
public class ProjectsController extends BaseController
{
    @Autowired
    private IProjectsService projectsService;

    /**
     * 查询项目信息列表（支持关联查询和权限过滤）
     */
    @PreAuthorize("@ss.hasPermi('evs:projects:list')")
    @GetMapping("/list")
    public TableDataInfo list(Projects projects,
                             @RequestParam(required = false) String includeCustomer,
                             @RequestParam(required = false) String includeBudgetItems,
                             @RequestParam(required = false) String includeSchedules,
                             @RequestParam(required = false) String includeProjectMembers)
    {
        startPage();

        // 构建关联查询参数
        StringBuilder includeRelations = new StringBuilder();
        if ("true".equals(includeCustomer)) {
            includeRelations.append("customer");
        }
        if ("true".equals(includeBudgetItems)) {
            if (includeRelations.length() > 0) includeRelations.append(",");
            includeRelations.append("budgetItems");
        }
        if ("true".equals(includeSchedules)) {
            if (includeRelations.length() > 0) includeRelations.append(",");
            includeRelations.append("schedules");
        }
        if ("true".equals(includeProjectMembers)) {
            if (includeRelations.length() > 0) includeRelations.append(",");
            includeRelations.append("projectMembers");
        }

        // 获取当前用户ID和权限信息（自动获取，不依赖前端传递）
        Long currentUserId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.hasRole("admin");

        // 传递查询参数和权限信息到Service层
        List<Projects> list = projectsService.selectProjectsWithRelations(
            projects, includeRelations.toString(), currentUserId.toString(), isAdmin);

        return getDataTable(list);
    }

    /**
     * 导出项目信息列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projects:export')")
    @Log(title = "项目信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Projects projects)
    {
        List<Projects> list = projectsService.selectProjectsList(projects);
        ExcelUtil<Projects> util = new ExcelUtil<Projects>(Projects.class);
        util.exportExcel(response, list, "项目信息数据");
    }

    /**
     * 获取项目信息详细信息（支持关联查询）
     */
    @PreAuthorize("@ss.hasPermi('evs:projects:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id,
                             @RequestParam(required = false) String includeCustomer,
                             @RequestParam(required = false) String includeBudgetItems,
                             @RequestParam(required = false) String includeSchedules)
    {
        // 构建关联查询参数
        StringBuilder includeRelations = new StringBuilder();
        if (includeCustomer != null) includeRelations.append("customer");
        if (includeBudgetItems != null) {
            if (includeRelations.length() > 0) includeRelations.append(",");
            includeRelations.append("budgetItems");
        }
        if (includeSchedules != null) {
            if (includeRelations.length() > 0) includeRelations.append(",");
            includeRelations.append("schedules");
        }

        Projects project = projectsService.selectProjectsWithRelationsById(
            id, includeRelations.toString());

        // 即使项目不存在也返回data字段，确保响应结构一致
        AjaxResult result = new AjaxResult();
        result.put("code", 200);
        result.put("msg", "操作成功");
        result.put("data", project);
        return result;
    }

    /**
     * 新增项目信息
     */
    @PreAuthorize("@ss.hasPermi('evs:projects:add')")
    @Log(title = "项目信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Projects projects)
    {
        return toAjax(projectsService.insertProjects(projects));
    }

    /**
     * 修改项目信息
     */
    @PreAuthorize("@ss.hasPermi('evs:projects:edit')")
    @Log(title = "项目信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Projects projects)
    {
        return toAjax(projectsService.updateProjects(projects));
    }

    /**
     * 删除项目信息
     */
    @PreAuthorize("@ss.hasPermi('evs:projects:remove')")
    @Log(title = "项目信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(projectsService.deleteProjectsByIds(ids));
    }
}
