package com.ruoyi.web.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.web.domain.Projects;

/**
 * 项目信息Mapper接口
 * 
 * @author evs
 * @date 2025-11-23
 */
public interface ProjectsMapper 
{
    /**
     * 查询项目信息
     *
     * @param projects 项目信息（包含权限参数）
     * @return 项目信息
     */
    public Projects selectProjectsById(Projects projects);

    /**
     * 查询项目信息列表
     *
     * @param projects 项目信息（包含权限参数）
     * @return 项目信息集合
     */
    public List<Projects> selectProjectsList(Projects projects);

    /**
     * 新增项目信息
     *
     * @param projects 项目信息
     * @return 结果
     */
    public int insertProjects(Projects projects);

    /**
     * 修改项目信息
     *
     * @param projects 项目信息
     * @return 结果
     */
    public int updateProjects(Projects projects);

    /**
     * 删除项��信息
     *
     * @param projects 项目信息（包含权限参数）
     * @return 结果
     */
    public int deleteProjectsById(Projects projects);

    /**
     * 批量删除项目��息
     *
     * @param projects 项目信息（包含权限参数和ID数组）
     * @return 结果
     */
    public int deleteProjectsByIds(Projects projects);

    /**
     * 查询项目信息列表（包含客户信息）
     *
     * @param projects 项目信息查询条件
     * @return 项目信息集合
     */
    public List<Projects> selectProjectsWithCustomer(Projects projects);

    /**
     * 查询项目信息（包含客户信息）
     *
     * @param projects 项目信息（包含权限参数）
     * @return 项目信息
     */
    public Projects selectProjectsWithCustomerById(Projects projects);

    /**
     * 通过项目成员关联查询项目列表（支持权限过滤）
     *
     * @param projects 项目信息
     * @param memberUserId 团队成员用户ID（用于筛选）
     * @param isAdmin 是否管理员
     * @return 项目信息集合
     */
    public List<Projects> selectProjectsWithMembers(@Param("projects") Projects projects,
                                                    @Param("memberUserId") String memberUserId,
                                                    @Param("isAdmin") boolean isAdmin);

    /**
     * 批量查询项目进度统计信息
     *
     * @param projectIds 项目ID列表
     * @return 统计信息Map，key为项目ID，value为统计数据
     */
    @MapKey("project_id")
    Map<String, Map<String, Object>> selectScheduleStatsMap(@Param("projectIds") List<String> projectIds);
}
