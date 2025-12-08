package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.CustomersMapper;
import com.ruoyi.web.domain.Customers;
import com.ruoyi.web.service.ICustomersService;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;

/**
 * 客户档案Service业务层处理
 * 
 * @author evs
 * @date 2025-11-23
 */
@Service
public class CustomersServiceImpl implements ICustomersService 
{
    @Autowired
    private CustomersMapper customersMapper;

    /**
     * 查询客户档案
     * 
     * @param id 客户档案主键
     * @return 客户档案
     */
    @Override
    public Customers selectCustomersById(String id)
    {
        return customersMapper.selectCustomersById(id);
    }

    /**
     * 查询客户档案列表
     * 
     * @param customers 客户档案
     * @return 客户档案
     */
    @Override
    public List<Customers> selectCustomersList(Customers customers)
    {
        return customersMapper.selectCustomersList(customers);
    }

    /**
     * 新增客户档案
     *
     * @param customers 客户档案
     * @return 结果
     */
    @Override
    public int insertCustomers(Customers customers)
    {
        // 检查手机号是否已存在
        if (customers.getPhone() != null && !customers.getPhone().trim().isEmpty()) {
            if (checkPhoneExists(customers.getPhone(), null)) {
                throw new ServiceException("当前手机号已存在");
            }
        }

        // 如果 id 为空，自动生成 UUID
        if (customers.getId() == null || customers.getId().isEmpty()) {
            customers.setId(IdUtils.fastSimpleUUID());
        }
        customers.setCreatedAt(DateUtils.getNowDate());
        customers.setCreatedBy(SecurityUtils.getUsername());
        return customersMapper.insertCustomers(customers);
    }

    /**
     * 修改客户档案
     *
     * @param customers 客户档案
     * @return 结果
     */
    @Override
    public int updateCustomers(Customers customers)
    {
        // 检查手机号是否已存在（排除当前客户ID）
        if (customers.getPhone() != null && !customers.getPhone().trim().isEmpty()) {
            if (checkPhoneExists(customers.getPhone(), customers.getId())) {
                throw new ServiceException("当前手机号已存在");
            }
        }

        customers.setUpdatedAt(DateUtils.getNowDate());
        customers.setUpdatedBy(SecurityUtils.getUsername());
        return customersMapper.updateCustomers(customers);
    }

    /**
     * 批量删除客户档案
     * 
     * @param ids 需要删除的客户档案主键
     * @return 结果
     */
    @Override
    public int deleteCustomersByIds(String[] ids)
    {
        return customersMapper.deleteCustomersByIds(ids);
    }

    @Override
    public int softDeleteCustomersById(String id)
    {
        Customers customers = customersMapper.selectCustomersById(id);
        if (customers == null) {
            return 0;
        }
        customers.setDeletedAt(DateUtils.getNowDate());
        customers.setIsActive(0);
        return customersMapper.updateCustomers(customers);
    }

    /**
     * 删除客户档案信息
     * 
     * @param id 客户档案主键
     * @return 结果
     */
    @Override
    public int deleteCustomersById(String id)
    {
        return customersMapper.deleteCustomersById(id);
    }

    @Override
    public boolean checkPhoneExists(String phone, String excludeId)
    {
        if (phone == null || phone.trim().isEmpty())
        {
            return false;
        }

        Customers customer = customersMapper.selectCustomersByPhone(phone.trim());
        if (customer == null)
        {
            return false;
        }

        // 如果指定了排除ID，且查询到的客户ID与排除ID相同，则不算重复
        if (excludeId != null && !excludeId.trim().isEmpty() && excludeId.equals(customer.getId()))
        {
            return false;
        }

        return true;
    }

    @Override
    public List<Customers> selectCustomersWithRelations(Customers customers, boolean includeProjects)
    {
        if (includeProjects) {
            return customersMapper.selectCustomersWithProjectCount(customers);
        } else {
            return selectCustomersList(customers);
        }
    }

    @Override
    public Customers selectCustomersWithRelationsById(String id, boolean includeProjects)
    {
        if (includeProjects) {
            return customersMapper.selectCustomersWithProjectCountById(id);
        } else {
            return selectCustomersById(id);
        }
    }
}
