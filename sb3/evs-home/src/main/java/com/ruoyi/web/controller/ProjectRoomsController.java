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
import com.ruoyi.web.domain.ProjectRooms;
import com.ruoyi.web.service.IProjectRoomsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 项目房间Controller
 * 
 * @author evs
 * @date 2025-11-27
 */
@RestController
@RequestMapping("/evs/projectRooms")
public class ProjectRoomsController extends BaseController
{
    @Autowired
    private IProjectRoomsService projectRoomsService;

    /**
     * 查询项目房间列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectRooms:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjectRooms projectRooms)
    {
        startPage();
        List<ProjectRooms> list = projectRoomsService.selectProjectRoomsList(projectRooms);
        return getDataTable(list);
    }

    /**
     * 导出项目房间列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectRooms:export')")
    @Log(title = "项目房间", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProjectRooms projectRooms)
    {
        List<ProjectRooms> list = projectRoomsService.selectProjectRoomsList(projectRooms);
        ExcelUtil<ProjectRooms> util = new ExcelUtil<ProjectRooms>(ProjectRooms.class);
        util.exportExcel(response, list, "项目房间数据");
    }

    /**
     * 获取项目房间详细信息
     */
    @PreAuthorize("@ss.hasPermi('evs:projectRooms:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(projectRoomsService.selectProjectRoomsById(id));
    }

    /**
     * 新增项目房间
     */
    @PreAuthorize("@ss.hasPermi('evs:projectRooms:add')")
    @Log(title = "项目房间", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProjectRooms projectRooms)
    {
        return toAjax(projectRoomsService.insertProjectRooms(projectRooms));
    }

    /**
     * 修改项目房间
     */
    @PreAuthorize("@ss.hasPermi('evs:projectRooms:edit')")
    @Log(title = "项目房间", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProjectRooms projectRooms)
    {
        return toAjax(projectRoomsService.updateProjectRooms(projectRooms));
    }

    /**
     * 删除项目房间
     */
    @PreAuthorize("@ss.hasPermi('evs:projectRooms:remove')")
    @Log(title = "项目房间", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(projectRoomsService.deleteProjectRoomsByIds(ids));
    }
}
