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
     * 根据客户ID查询项目列表
     * 使用别名映射到Projects实体类字段
     * 
     * @param customerId 客户ID
     * @return 项目列表
     */
    @Select("SELECT id, project_type AS projectType, name, customer_id AS customerId, description, " +
            "address, area, budget, start_date AS startDate, end_date AS endDate, status, priority, progress " +
            "FROM projects WHERE customer_id = #{customerId} AND deleted_at IS NULL " +
            "ORDER BY created_at DESC")
    List<Projects> selectProjectsByCustomerId(@Param("customerId") String customerId);
    
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
