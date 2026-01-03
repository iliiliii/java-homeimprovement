package com.ruoyi.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户岗位关联 数据层
 */
@Mapper
public interface UserPostMapper {

    /**
     * 查询所有用户-岗位关联
     * @return 用户岗位关联列表 [{userId, postId}, ...]
     */
    List<Map<String, Object>> selectUserPostAll();

    /**
     * 根据岗位ID查询用户列表
     * @param postId 岗位ID
     * @return 用户列表
     */
    List<Map<String, Object>> selectUsersByPostId(@Param("postId") Long postId);
}
