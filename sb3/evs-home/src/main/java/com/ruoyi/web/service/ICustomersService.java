package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.Customers;

/**
 * 客户档案Service接口
 * 
 * @author evs
 * @date 2025-11-18
 */
public interface ICustomersService 
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
     * 批量删除客户档案
     * 
     * @param ids 需要删除的客户档案主键集合
     * @return 结果
     */
    public int deleteCustomersByIds(String[] ids);

    /**
     * 删除客户档案信息
     *
     * @param id 客户档案主键
     * @return 结果
     */
    public int deleteCustomersById(String id);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @param excludeId 排除的客户ID（编辑时使用）
     * @return 是否存在
     */
    public boolean checkPhoneExists(String phone, String excludeId);
}
