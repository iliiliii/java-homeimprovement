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
import com.ruoyi.web.domain.ProjectCustomers;
import com.ruoyi.web.service.IProjectCustomersService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 项目客户Controller
 * 
 * @author evs
 * @date 2026-03-01
 */
@RestController
@RequestMapping("/evs/projectCustomers")
public class ProjectCustomersController extends BaseController
{
    @Autowired
    private IProjectCustomersService projectCustomersService;

    /**
     * 查询项目客户列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectCustomers:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjectCustomers projectCustomers)
    {
        startPage();
        List<ProjectCustomers> list = projectCustomersService.selectProjectCustomersList(projectCustomers);
        return getDataTable(list);
    }

    /**
     * 导出项目客户列表
     */
    @PreAuthorize("@ss.hasPermi('evs:projectCustomers:export')")
    @Log(title = "项目客户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProjectCustomers projectCustomers)
    {
        List<ProjectCustomers> list = projectCustomersService.selectProjectCustomersList(projectCustomers);
        ExcelUtil<ProjectCustomers> util = new ExcelUtil<ProjectCustomers>(ProjectCustomers.class);
        util.exportExcel(response, list, "项目客户数据");
    }

    /**
     * 获取项目客户详细信息
     */
    @PreAuthorize("@ss.hasPermi('evs:projectCustomers:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(projectCustomersService.selectProjectCustomersById(id));
    }

    /**
     * 新增项目客户
     */
    @PreAuthorize("@ss.hasPermi('evs:projectCustomers:add')")
    @Log(title = "项目客户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProjectCustomers projectCustomers)
    {
        return toAjax(projectCustomersService.insertProjectCustomers(projectCustomers));
    }

    /**
     * 修改项目客户
     */
    @PreAuthorize("@ss.hasPermi('evs:projectCustomers:edit')")
    @Log(title = "项目客户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProjectCustomers projectCustomers)
    {
        return toAjax(projectCustomersService.updateProjectCustomers(projectCustomers));
    }

    /**
     * 删除项目客户
     */
    @PreAuthorize("@ss.hasPermi('evs:projectCustomers:remove')")
    @Log(title = "项目客户", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(projectCustomersService.deleteProjectCustomersByIds(ids));
    }
    
    /**
     * 查询项目的所有客户
     */
    @PreAuthorize("@ss.hasPermi('evs:projectCustomers:query')")
    @GetMapping("/project/{projectId}")
    public AjaxResult getProjectCustomers(@PathVariable("projectId") String projectId)
    {
        List<ProjectCustomers> list = projectCustomersService.selectByProjectId(projectId);
        return success(list);
    }
    
    /**
     * 查询客户的所有项目
     */
    @PreAuthorize("@ss.hasPermi('evs:projectCustomers:query')")
    @GetMapping("/customer/{customerId}")
    public AjaxResult getCustomerProjects(@PathVariable("customerId") String customerId)
    {
        List<ProjectCustomers> list = projectCustomersService.selectByCustomerId(customerId);
        return success(list);
    }
    
    /**
     * 查询项目的主客户
     */
    @PreAuthorize("@ss.hasPermi('evs:projectCustomers:query')")
    @GetMapping("/primary/{projectId}")
    public AjaxResult getPrimaryCustomer(@PathVariable("projectId") String projectId)
    {
        ProjectCustomers primary = projectCustomersService.selectPrimaryByProjectId(projectId);
        return success(primary);
    }
    
    /**
     * 添加客户到项目
     * 
     * @param request 请求参数
     *   - projectId: 项目ID（必填）
     *   - customerIds: 客户ID列表（必填）
     *   - primaryCustomerId: 主客户ID（可选）
     */
    @PreAuthorize("@ss.hasPermi('evs:projectCustomers:add')")
    @Log(title = "添加客户到项目", businessType = BusinessType.INSERT)
    @PostMapping("/addCustomers")
    public AjaxResult addCustomersToProject(@RequestBody AddCustomersRequest request)
    {
        if (request.getProjectId() == null || request.getProjectId().isEmpty())
        {
            return error("项目ID不能为空");
        }
        
        if (request.getCustomerIds() == null || request.getCustomerIds().isEmpty())
        {
            return error("客户ID列表不能为空");
        }
        
        int result = projectCustomersService.addProjectCustomers(
            request.getProjectId(), 
            request.getCustomerIds(), 
            request.getPrimaryCustomerId()
        );
        
        return toAjax(result);
    }
    
    /**
     * 从项目移除客户
     * 
     * @param request 请求参数
     *   - projectId: 项目ID（必填）
     *   - customerId: 客户ID（必填）
     */
    @PreAuthorize("@ss.hasPermi('evs:projectCustomers:remove')")
    @Log(title = "移除项目客户", businessType = BusinessType.DELETE)
    @PostMapping("/removeCustomer")
    public AjaxResult removeCustomerFromProject(@RequestBody RemoveCustomerRequest request)
    {
        if (request.getProjectId() == null || request.getProjectId().isEmpty())
        {
            return error("项目ID不能为空");
        }
        
        if (request.getCustomerId() == null || request.getCustomerId().isEmpty())
        {
            return error("客户ID不能为空");
        }
        
        int result = projectCustomersService.removeProjectCustomer(
            request.getProjectId(), 
            request.getCustomerId()
        );
        
        return toAjax(result);
    }
    
    /**
     * 设置主客户
     * 
     * @param request 请求参数
     *   - projectId: 项目ID（必填）
     *   - customerId: 客户ID（必填）
     */
    @PreAuthorize("@ss.hasPermi('evs:projectCustomers:edit')")
    @Log(title = "设置主客户", businessType = BusinessType.UPDATE)
    @PostMapping("/setPrimary")
    public AjaxResult setPrimaryCustomer(@RequestBody SetPrimaryRequest request)
    {
        if (request.getProjectId() == null || request.getProjectId().isEmpty())
        {
            return error("项目ID不能为空");
        }
        
        if (request.getCustomerId() == null || request.getCustomerId().isEmpty())
        {
            return error("客户ID不能为空");
        }
        
        int result = projectCustomersService.setPrimaryCustomer(
            request.getProjectId(), 
            request.getCustomerId()
        );
        
        return toAjax(result);
    }
    
    /**
     * 检查客户是否有权访问项目
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     */
    @PreAuthorize("@ss.hasPermi('evs:projectCustomers:query')")
    @GetMapping("/checkAccess")
    public AjaxResult checkAccess(
        @RequestParam("projectId") String projectId,
        @RequestParam("customerId") String customerId)
    {
        boolean hasAccess = projectCustomersService.checkCustomerAccess(projectId, customerId);
        return success(hasAccess);
    }
    
    /**
     * 添加客户请求参数
     */
    public static class AddCustomersRequest
    {
        private String projectId;
        private List<String> customerIds;
        private String primaryCustomerId;
        
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        
        public List<String> getCustomerIds() { return customerIds; }
        public void setCustomerIds(List<String> customerIds) { this.customerIds = customerIds; }
        
        public String getPrimaryCustomerId() { return primaryCustomerId; }
        public void setPrimaryCustomerId(String primaryCustomerId) { this.primaryCustomerId = primaryCustomerId; }
    }
    
    /**
     * 移除客户请求参数
     */
    public static class RemoveCustomerRequest
    {
        private String projectId;
        private String customerId;
        
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
    }
    
    /**
     * 设置主客户请求参数
     */
    public static class SetPrimaryRequest
    {
        private String projectId;
        private String customerId;
        
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
    }
}
