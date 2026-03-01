package com.ruoyi.app.mapper;

import com.ruoyi.web.domain.Projects;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 小程序项目查询Mapper
 * 用于绕过若依的数据权限控制
 */
@Mapper
public interface AppProjectMapper {
    
    /**
     * 根据客户ID查询项目列表（旧方法，已废弃）
     * 使用别名映射到Projects实体类字段
     * 
     * @param customerId 客户ID
     * @return 项目列表
     * @deprecated 使用 selectProjectsByCustomerIdFromRelation 替代
     */
    @Deprecated
    @Select("SELECT id, project_type AS projectType, name, customer_id AS customerId, description, " +
            "address, area, budget, start_date AS startDate, end_date AS endDate, status, priority, progress " +
            "FROM projects WHERE customer_id = #{customerId} AND deleted_at IS NULL " +
            "ORDER BY created_at DESC")
    List<Projects> selectProjectsByCustomerId(@Param("customerId") String customerId);
    
    /**
     * 根据客户ID从关联表查询项目列表（新方法）
     * 通过 project_customers 表查询客户关联的所有项目
     * 
     * @param customerId 客户ID
     * @return 项目列表
     */
    @Select("SELECT p.id, p.project_type AS projectType, p.name, p.customer_id AS customerId, p.description, " +
            "p.address, p.area, p.budget, p.start_date AS startDate, p.end_date AS endDate, " +
            "p.status, p.priority, p.progress " +
            "FROM projects p " +
            "INNER JOIN project_customers pc ON p.id = pc.project_id " +
            "WHERE pc.customer_id = #{customerId} AND pc.deleted_at IS NULL AND p.deleted_at IS NULL " +
            "ORDER BY pc.is_primary DESC, p.created_at DESC")
    List<Projects> selectProjectsByCustomerIdFromRelation(@Param("customerId") String customerId);
    
    /**
     * 根据项目ID查询项目
     * 
     * @param id 项目ID
     * @return 项目信息
     */
    @Select("SELECT id, project_type AS projectType, name, customer_id AS customerId, description, " +
            "address, area, budget, start_date AS startDate, end_date AS endDate, status, priority, progress " +
            "FROM projects WHERE id = #{id} AND deleted_at IS NULL")
    Projects selectProjectById(@Param("id") String id);
    
    /**
     * 根据项目ID列表查询项目
     * 
     * @param ids 项目ID列表
     * @return 项目列表
     */
    List<Projects> selectProjectsByIds(@Param("ids") List<String> ids);
}
