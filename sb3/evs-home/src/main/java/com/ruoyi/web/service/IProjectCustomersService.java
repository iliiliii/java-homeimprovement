package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.ProjectCustomers;

/**
 * 项目客户Service接口
 * 
 * @author evs
 * @date 2026-03-01
 */
public interface IProjectCustomersService 
{
    /**
     * 查询项目客户
     * 
     * @param id 项目客户主键
     * @return 项目客户
     */
    public ProjectCustomers selectProjectCustomersById(String id);

    /**
     * 查询项目客户列表
     * 
     * @param projectCustomers 项目客户
     * @return 项目客户集合
     */
    public List<ProjectCustomers> selectProjectCustomersList(ProjectCustomers projectCustomers);

    /**
     * 新增项目客户
     * 
     * @param projectCustomers 项目客户
     * @return 结果
     */
    public int insertProjectCustomers(ProjectCustomers projectCustomers);

    /**
     * 修改项目客户
     * 
     * @param projectCustomers 项目客户
     * @return 结果
     */
    public int updateProjectCustomers(ProjectCustomers projectCustomers);

    /**
     * 批量删除项目客户
     * 
     * @param ids 需要删除的项目客户主键集合
     * @return 结果
     */
    public int deleteProjectCustomersByIds(String[] ids);

    /**
     * 删除项目客户信息
     * 
     * @param id 项目客户主键
     * @return 结果
     */
    public int deleteProjectCustomersById(String id);
    
    /**
     * 查询项目的所有客户
     * 
     * @param projectId 项目ID
     * @return 客户列表（包含客户详细信息）
     */
    public List<ProjectCustomers> selectByProjectId(String projectId);
    
    /**
     * 查询客户的所有项目
     * 
     * @param customerId 客户ID
     * @return 项目关联列表
     */
    public List<ProjectCustomers> selectByCustomerId(String customerId);
    
    /**
     * 查询项目的主客户
     * 
     * @param projectId 项目ID
     * @return 主客户信息
     */
    public ProjectCustomers selectPrimaryByProjectId(String projectId);
    
    /**
     * 添加客户到项目（支持多个客户）
     * 
     * @param projectId 项目ID
     * @param customerIds 客户ID列表
     * @param primaryCustomerId 主客户ID（可选）
     * @return 成功添加的数量
     */
    public int addProjectCustomers(String projectId, List<String> customerIds, String primaryCustomerId);
    
    /**
     * 移除客户（软删除）
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     * @return 结果
     */
    public int removeProjectCustomer(String projectId, String customerId);
    
    /**
     * 设置主客户
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     * @return 结果
     */
    public int setPrimaryCustomer(String projectId, String customerId);
    
    /**
     * 检查客户是否有权访问项目
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     * @return true=有权限，false=无权限
     */
    public boolean checkCustomerAccess(String projectId, String customerId);
}
