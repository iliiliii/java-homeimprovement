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
}
