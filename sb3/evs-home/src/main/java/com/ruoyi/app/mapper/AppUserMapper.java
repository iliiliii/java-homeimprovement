package com.ruoyi.app.mapper;

import com.ruoyi.common.core.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 小程序用户查询Mapper
 * 用于绕过若依的数据权限控制
 */
@Mapper
public interface AppUserMapper {
    
    /**
     * 根据手机号查询系统用户（员工）
     * 
     * @param phonenumber 手机号
     * @return 用户信息
     */
    @Select("SELECT user_id, dept_id, user_name, nick_name, email, phonenumber, sex, avatar, status " +
            "FROM sys_user WHERE phonenumber = #{phonenumber} AND del_flag = '0' AND status = '0' LIMIT 1")
    SysUser selectUserByPhone(@Param("phonenumber") String phonenumber);
    
    /**
     * 根据用户ID查询系统用户（员工）
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    @Select("SELECT user_id, dept_id, user_name, nick_name, email, phonenumber, sex, avatar, password, status " +
            "FROM sys_user WHERE user_id = #{userId} AND del_flag = '0' LIMIT 1")
    SysUser selectUserById(@Param("userId") Long userId);
}
