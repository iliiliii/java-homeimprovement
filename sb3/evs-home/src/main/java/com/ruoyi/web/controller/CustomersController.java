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
import com.ruoyi.web.domain.Customers;
import com.ruoyi.web.service.ICustomersService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 客户档案Controller
 * 
 * @author evs
 * @date 2025-11-23
 */
@RestController
@RequestMapping("/evs/customers")
public class CustomersController extends BaseController
{
    @Autowired
    private ICustomersService customersService;

    /**
     * 查询客户档案列表（支持关联查询）
     */
    @PreAuthorize("@ss.hasPermi('evs:customers:list')")
    @GetMapping("/list")
    public TableDataInfo list(Customers customers,
                             @RequestParam(required = false, defaultValue = "false") Boolean includeProjects)
    {
        startPage();
        List<Customers> list = customersService.selectCustomersWithRelations(customers, includeProjects);
        return getDataTable(list);
    }

    /**
     * 导出客户档案列表
     */
    @PreAuthorize("@ss.hasPermi('evs:customers:export')")
    @Log(title = "客户档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Customers customers)
    {
        List<Customers> list = customersService.selectCustomersList(customers);
        ExcelUtil<Customers> util = new ExcelUtil<Customers>(Customers.class);
        util.exportExcel(response, list, "客户档案数据");
    }

    /**
     * 获取客户档案详细信息（支持关联查询）
     */
    @PreAuthorize("@ss.hasPermi('evs:customers:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id,
                             @RequestParam(required = false, defaultValue = "false") Boolean includeProjects)
    {
        Customers customer = customersService.selectCustomersWithRelationsById(id, includeProjects);
        return success(customer);
    }

    /**
     * 新增客户档案
     */
    @PreAuthorize("@ss.hasPermi('evs:customers:add')")
    @Log(title = "客户档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Customers customers)
    {
        return toAjax(customersService.insertCustomers(customers));
    }

    /**
     * 修改客户档案
     */
    @PreAuthorize("@ss.hasPermi('evs:customers:edit')")
    @Log(title = "客户档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Customers customers)
    {
        return toAjax(customersService.updateCustomers(customers));
    }

    /**
     * 删除客户档案
     */
    @PreAuthorize("@ss.hasPermi('evs:customers:remove')")
    @Log(title = "客户档案", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        for (String id : ids) {
            customersService.softDeleteCustomersById(id);
        }
        return success();
        // return toAjax(customersService.softDeleteCustomersById(ids));
    }

        /**
     * 检查手机号是否存在
     */
    @PreAuthorize("@ss.hasPermi('evs:customers:list')")
    @GetMapping("/checkPhone/{phone}")
    public AjaxResult checkPhone(@PathVariable("phone") String phone, String excludeId)
    {
        boolean exists = customersService.checkPhoneExists(phone, excludeId);
        return success(exists);
    }
}
