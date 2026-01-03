package com.ruoyi.app.service;

import com.ruoyi.app.dto.response.*;
import java.util.List;
import java.util.Map;

/**
 * 小程序首页数据服务接口
 */
public interface IAppDashboardService {

    /**
     * 获取客户首页数据
     */
    CustomerDashboardVO getCustomerDashboard(String token);

    /**
     * 获取员工首页数据
     */
    StaffDashboardVO getStaffDashboard(String token);

    /**
     * 获取员工首页数据（带分页）
     * @param token 认证Token
     * @param pageNum 页码
     */
    StaffDashboardVO getStaffDashboard(String token, Integer pageNum);

    /**
     * 获取项目详情
     */
    ProjectDetailVO getProjectDetail(String token, String projectId);

    /**
     * 获取项目进度列表
     */
    List<ScheduleVO> getProjectSchedules(String token, String projectId);

    /**
     * 获取项目质检记录
     */
    List<InspectionVO> getProjectInspections(String token, String projectId);

    /**
     * 获取项目设计图列表
     */
    List<DesignVO> getProjectDesigns(String token, String projectId);

    /**
     * 获取项目房间列表（设计稿）
     */
    List<RoomVO> getProjectRooms(String token, String projectId);

    /**
     * 获取项目合同金额列表
     * @param token 认证Token
     * @param projectId 项目ID
     * @return 合同金额列表（包含六个固定分类，无数据的显示金额为0）
     */
    List<ContractAmountVO> getProjectContractAmounts(String token, String projectId);
}
