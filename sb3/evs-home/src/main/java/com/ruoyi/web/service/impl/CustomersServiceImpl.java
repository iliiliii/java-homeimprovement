package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.CustomersMapper;
import com.ruoyi.web.domain.Customers;
import com.ruoyi.web.service.ICustomersService;
import com.ruoyi.common.utils.uuid.IdUtils;

/**
 * 客户档案Service业务层处理
 * 
 * @author evs
 * @date 2025-11-16
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
        // 如果 id 为空，自动生成 UUID
        if (customers.getId() == null || customers.getId().isEmpty()) {
            customers.setId(IdUtils.fastSimpleUUID());
        }
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
}
