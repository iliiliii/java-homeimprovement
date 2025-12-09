package com.ruoyi.app.mapper;

import com.ruoyi.app.dto.response.AppUserInfo;
import com.ruoyi.app.dto.response.CustomerProjectVO;
import com.ruoyi.app.dto.response.StaffProjectVO;
import com.ruoyi.web.domain.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 小程序首页数据Mapper
 */
@Mapper
public interface AppDashboardMapper {

    /**
     * 根据ID查询客户信息
     */
    Customers selectCustomerById(@Param("id") String id);

    /**
     * 根据ID查询员工信息
     */
    AppUserInfo selectStaffById(@Param("userId") String userId);

    /**
     * 查询员工关联的项目列表
     */
    List<StaffProjectVO> selectStaffProjects(@Param("userId") String userId);

    /**
     * 查询项目当前进行中的阶段
     */
    ProjectSchedules selectCurrentSchedule(@Param("projectId") String projectId);

    /**
     * 查询项目的所有进度
     */
    List<ProjectSchedules> selectSchedulesByProjectId(@Param("projectId") String projectId);

    /**
     * 查询项目的质检记录（包含问题）
     */
    List<QualityInspections> selectInspectionsByProjectId(@Param("projectId") String projectId);

    /**
     * 查询项目的设计文件
     */
    List<FileUploads> selectDesignFilesByProjectId(@Param("projectId") String projectId);

    /**
     * 统计项目待处理问题数
     */
    int countPendingIssues(@Param("projectId") String projectId);

    /**
     * 统计员工待巡检数量
     */
    int countPendingInspections(@Param("userId") String userId);

    /**
     * 统计员工待处理问题数
     */
    int countStaffPendingIssues(@Param("userId") String userId);

    /**
     * 统计员工今日待办数
     */
    int countTodayTasks(@Param("userId") String userId);

    /**
     * 检查员工是否有权限访问项目
     */
    boolean checkStaffProjectAccess(@Param("userId") String userId, @Param("projectId") String projectId);

    /**
     * 查询字典标签
     */
    String selectDictLabel(@Param("dictType") String dictType, @Param("dictValue") String dictValue);

    /**
     * 查询客户项目列表（带字典状态）
     */
    List<CustomerProjectVO> selectCustomerProjects(@Param("customerId") String customerId);

    /**
     * 查询员工项目列表（带字典状态）
     */
    List<StaffProjectVO> selectStaffProjectsWithDict(@Param("userId") String userId);

    /**
     * 查询项目的房间列表
     */
    List<Map<String, Object>> selectProjectRooms(@Param("projectId") String projectId);
}
