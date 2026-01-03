package com.ruoyi.app.service;

import com.ruoyi.app.dto.request.AcceptanceRecordRequest;
import com.ruoyi.app.dto.response.ProjectScheduleVO;
import com.ruoyi.app.dto.response.ProjectScheduleRecordVO;
import com.ruoyi.common.core.page.TableDataInfo;

import java.util.List;

/**
 * 小程序项目进度服务接口
 */
public interface IAppProjectScheduleService {

    /**
     * 获取项目进度列表
     * @param token 认证Token
     * @param projectId 项目ID
     * @return 进度列表
     */
    List<ProjectScheduleVO> getProjectScheduleList(String token, String projectId);

    /**
     * 获取项目进度验收记录列表
     * @param token 认证Token
     * @param projectId 项目ID
     * @param scheduleId 进度ID（可选，用于筛选特定进度的记录）
     * @param page 页码
     * @param pageSize 页大小
     * @return 验收记录列表
     */
    TableDataInfo getProjectScheduleRecordList(String token, String projectId, String scheduleId, Integer page, Integer pageSize);

    /**
     * 获取进度验收记录详情
     * @param token 认证Token
     * @param recordId 记录ID
     * @return 记录详情
     */
    ProjectScheduleRecordVO getProjectScheduleRecordDetail(String token, String recordId);

    /**
     * 新增验收记录（仅员工可用）
     * @param token 认证Token
     * @param projectId 项目ID
     * @param request 验收记录请求
     * @return 新增记录ID
     */
    String addAcceptanceRecord(String token, String projectId, AcceptanceRecordRequest request);

    /**
     * 编辑验收记录（仅员工可用，且只能编辑自己创建的记录）
     * @param token 认证Token
     * @param projectId 项目ID
     * @param recordId 记录ID
     * @param request 验收记录请求
     */
    void updateAcceptanceRecord(String token, String projectId, String recordId, AcceptanceRecordRequest request);

    /**
     * 删除验收记录（仅员工可用，且只能删除自己创建的记录）
     * @param token 认证Token
     * @param projectId 项目ID
     * @param recordId 记录ID
     */
    void deleteAcceptanceRecord(String token, String projectId, String recordId);
}