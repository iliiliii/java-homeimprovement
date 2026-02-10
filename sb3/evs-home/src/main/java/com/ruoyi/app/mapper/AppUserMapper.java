package com.ruoyi.app.mapper;

import com.ruoyi.common.core.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 小程序用户查询Mapper
 * 用于绕过若依的数据权限控制
 */
@Mapper
public interface AppUserMapper {
    
    /**
     * 根据手机号查询系统用户（员工）
     * 使用别名映射到SysUser实体类字段
     * 
     * @param phonenumber 手机号
     * @return 用户信息
     */
    @Select("SELECT user_id AS userId, dept_id AS deptId, user_name AS userName, nick_name AS nickName, " +
            "email, phonenumber, sex, avatar, status " +
            "FROM sys_user WHERE phonenumber = #{phonenumber} AND del_flag = '0' AND status = '0' LIMIT 1")
    SysUser selectUserByPhone(@Param("phonenumber") String phonenumber);
    
    /**
     * 根据用户ID查询系统用户（员工）
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    @Select("SELECT user_id AS userId, dept_id AS deptId, user_name AS userName, nick_name AS nickName, " +
            "email, phonenumber, sex, avatar, password, status " +
            "FROM sys_user WHERE user_id = #{userId} AND del_flag = '0' LIMIT 1")
    SysUser selectUserById(@Param("userId") Long userId);
    
    /**
     * 查询团队成员列表（包含岗位信息）
     * 过滤条件：email不为空、未删除、未停用
     * 排序规则：按照用户的最小岗位排序值（post_sort）进行排序
     * 
     * @return 团队成员列表（Map格式，包含用户信息和岗位名称）
     */
    @Select("SELECT u.user_id AS userId, u.user_name AS userName, u.nick_name AS nickName, " +
            "u.email, u.phonenumber, u.sex, u.avatar, u.status, " +
            "GROUP_CONCAT(p.post_name ORDER BY p.post_sort SEPARATOR '、') AS postNames, " +
            "MIN(p.post_sort) AS minPostSort " +
            "FROM sys_user u " +
            "LEFT JOIN sys_user_post up ON u.user_id = up.user_id " +
            "LEFT JOIN sys_post p ON up.post_id = p.post_id AND p.status = '0' " +
            "WHERE u.email IS NOT NULL AND u.email != '' AND u.del_flag = '0' AND u.status = '0' " +
            "GROUP BY u.user_id " +
            "ORDER BY minPostSort ASC, u.create_time ASC")
    List<java.util.Map<String, Object>> selectTeamMembersWithPost();
    
    /**
     * 查询项目成员列表（包含用户信息和岗位）
     * 
     * @param projectId 项目ID
     * @return 项目成员列表
     */
    @Select("SELECT pm.id, pm.user_id AS userId, pm.role, " +
            "u.user_name AS userName, u.nick_name AS nickName, u.avatar, " +
            "GROUP_CONCAT(DISTINCT p.post_name ORDER BY p.post_sort SEPARATOR '、') AS postNames " +
            "FROM project_members pm " +
            "INNER JOIN sys_user u ON pm.user_id = u.user_id AND u.del_flag = '0' AND u.status = '0' " +
            "LEFT JOIN sys_user_post up ON u.user_id = up.user_id " +
            "LEFT JOIN sys_post p ON up.post_id = p.post_id AND p.status = '0' " +
            "WHERE pm.project_id = #{projectId} AND pm.is_active = 1 " +
            "GROUP BY pm.id, pm.user_id, pm.role, u.user_name, u.nick_name, u.avatar " +
            "ORDER BY pm.created_at ASC")
    List<Map<String, Object>> selectProjectMembers(@Param("projectId") String projectId);
}
