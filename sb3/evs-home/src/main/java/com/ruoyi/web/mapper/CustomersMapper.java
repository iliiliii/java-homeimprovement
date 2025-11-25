package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.Customers;

/**
 * 客户档案Mapper接口
 * 
 * @author evs
 * @date 2025-11-23
 */
public interface CustomersMapper 
{
    /**
     * 查询客户档案
     * 
     * @param id 客户档案主键
     * @return 客户档案
     */
    public Customers selectCustomersById(String id);

    /**
     * 查询客户档案列表
     * 
     * @param customers 客户档案
     * @return 客户档案集合
     */
    public List<Customers> selectCustomersList(Customers customers);

    /**
     * 新增客户档案
     * 
     * @param customers 客户档案
     * @return 结果
     */
    public int insertCustomers(Customers customers);

    /**
     * 修改客户档案
     * 
     * @param customers 客户档案
     * @return 结果
     */
    public int updateCustomers(Customers customers);

    /**
     * 删除客户档案
     * 
     * @param id 客户档案主键
     * @return 结果
     */
    public int deleteCustomersById(String id);

    /**
     * 批量删除客户档案
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCustomersByIds(String[] ids);

    /**
     * 根据手机号查询客户档案
     *
     * @param phone 手机号
     * @return 客户档案
     */
    public Customers selectCustomersByPhone(String phone);

    /**
     * 查询客户档案列表（包含项目数量）
     *
     * @param customers 客户档案查询条件
     * @return 客户档案集合
     */
    public List<Customers> selectCustomersWithProjectCount(Customers customers);

    /**
     * 查询客户档案（包含项目数量）
     *
     * @param id 客户档案主键
     * @return 客户档案
     */
    public Customers selectCustomersWithProjectCountById(String id);
}
