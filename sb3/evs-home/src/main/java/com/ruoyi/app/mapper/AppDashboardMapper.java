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
     * 根据字典标签查询字典值
     * 用于 decoration_staff_dashboard 等配置型字典
     * @param dictType 字典类型
     * @param dictLabel 字典标签（配置项名称）
     * @return 字典值（配置项值）
     */
    String selectDictValueByLabel(@Param("dictType") String dictType, @Param("dictLabel") String dictLabel);

    /**
     * 查询客户项目列表（带字典状态）
     */
    List<CustomerProjectVO> selectCustomerProjects(@Param("customerId") String customerId);

    /**
     * 查询员工项目列表（带字典状态）
     */
    List<StaffProjectVO> selectStaffProjectsWithDict(@Param("userId") String userId);

    /**
     * 查询员工项目列表（带分页和状态筛选）
     * 排序使用字典 decoration_status_project_order 的 dict_sort
     * @param userId 用户ID
     * @param allowedStatuses 允许的状态列表（为空时不筛选）
     * @param offset 偏移量
     * @param limit 限制数量
     */
    List<StaffProjectVO> selectStaffProjectsPaged(
            @Param("userId") String userId,
            @Param("allowedStatuses") List<String> allowedStatuses,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 统计员工项目总数（带状态筛选）
     * @param userId 用户ID
     * @param allowedStatuses 允许的状态列表（为空时不筛选）
     */
    int countStaffProjects(
            @Param("userId") String userId,
            @Param("allowedStatuses") List<String> allowedStatuses);

    /**
     * 查询允许显示的项目状态列表
     * 从字典 decoration_status_project_order 读取，status='0' 的状态才显示
     * @return 状态列表（包含 dictValue 和 dictSort）
     */
    List<Map<String, Object>> selectAllowedProjectStatuses();

    /**
     * 查询项目的房间列表
     */
    List<Map<String, Object>> selectProjectRooms(@Param("projectId") String projectId);

    /**
     * 查询项目的合同金额列表
     * @param projectId 项目ID
     * @return 合同金额列表（category为decoration_project_attachment_htje字典值）
     */
    List<Map<String, Object>> selectProjectContractAmounts(@Param("projectId") String projectId);

    /**
     * 查询合同金额字典数据
     * @return 字典数据列表
     */
    List<Map<String, Object>> selectContractAmountDictData();
}
