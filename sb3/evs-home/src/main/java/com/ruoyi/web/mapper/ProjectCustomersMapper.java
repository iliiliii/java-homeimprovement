package com.ruoyi.web.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.web.domain.ProjectCustomers;

/**
 * 项目客户Mapper接口
 * 
 * @author evs
 * @date 2026-03-01
 */
public interface ProjectCustomersMapper 
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
     * 删除项目客户
     * 
     * @param id 项目客户主键
     * @return 结果
     */
    public int deleteProjectCustomersById(String id);

    /**
     * 批量删除项目客户
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProjectCustomersByIds(String[] ids);
    
    /**
     * 查询项目的所有客户
     * 
     * @param projectId 项目ID
     * @return 客户列表
     */
    public List<ProjectCustomers> selectByProjectId(@Param("projectId") String projectId);
    
    /**
     * 查询客户的所有项目
     * 
     * @param customerId 客户ID
     * @return 项目关联列表
     */
    public List<ProjectCustomers> selectByCustomerId(@Param("customerId") String customerId);
    
    /**
     * 查询项目的主客户
     * 
     * @param projectId 项目ID
     * @return 主客户信息
     */
    public ProjectCustomers selectPrimaryByProjectId(@Param("projectId") String projectId);
    
    /**
     * 检查客户是否关联到项目
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     * @return true=已关联，false=未关联
     */
    public boolean checkCustomerInProject(@Param("projectId") String projectId, 
                                         @Param("customerId") String customerId);
    
    /**
     * 查询所有记录（包括软删除的）
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     * @return 所有记录列表
     */
    public List<ProjectCustomers> selectAllRecords(@Param("projectId") String projectId, 
                                                   @Param("customerId") String customerId);
    
    /**
     * 批量添加项目客户关联
     * 
     * @param list 关联列表
     * @return 影响行数
     */
    public int batchInsert(@Param("list") List<ProjectCustomers> list);
    
    /**
     * 软删除项目客户关联
     * 
     * @param id 关联ID
     * @return 影响行数
     */
    public int softDelete(@Param("id") String id);
    
    /**
     * 清除项目的所有主客户标记
     * 
     * @param projectId 项目ID
     * @return 影响行数
     */
    public int clearPrimaryCustomers(@Param("projectId") String projectId);
    
    /**
     * 设置主客户（同时取消其他主客户）
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     * @return 影响行数
     */
    public int setPrimaryCustomer(@Param("projectId") String projectId, 
                                  @Param("customerId") String customerId);
    
    /**
     * 统计项目的客户数量
     * 
     * @param projectId 项目ID
     * @return 客户数量
     */
    public int countByProjectId(@Param("projectId") String projectId);
    
    /**
     * 批量统计项目的客户数量
     * 
     * @param projectIds 项目ID列表
     * @return Map<projectId, customerCount>
     */
    public List<Map<String, Object>> countByProjectIds(@Param("projectIds") List<String> projectIds);
}
