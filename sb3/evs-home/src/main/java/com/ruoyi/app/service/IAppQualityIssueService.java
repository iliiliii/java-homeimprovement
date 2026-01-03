package com.ruoyi.app.service;

import com.ruoyi.app.dto.request.QualityIssueReportRequest;

import java.util.List;
import java.util.Map;

/**
 * 小程序质量问题Service接口
 */
public interface IAppQualityIssueService {

    /**
     * 获取项目质量问题列表
     */
    Map<String, Object> getQualityIssueList(String token, String projectId, Integer page, Integer pageSize);

    /**
     * 问题上报
     */
    String reportQualityIssue(String token, String projectId, QualityIssueReportRequest request);

    /**
     * 获取质量问题详情
     */
    Map<String, Object> getQualityIssueDetail(String token, String issueId);

    /**
     * 获取字典数据列表
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<Map<String, Object>> getDictData(String dictType);

    /**
     * 获取问题的整改记录列表
     * @param token 认证token
     * @param issueId 问题ID
     * @return 整改记录列表
     */
    List<Map<String, Object>> getFixesByIssueId(String token, String issueId);

    /**
     * 提交整改记录
     * @param token 认证token
     * @param issueId 问题ID
     * @param fixDescription 整改描述
     * @param images 整改图片
     * @param status 整改状态
     * @return 整改记录ID
     */
    String submitFix(String token, String issueId, String fixDescription, String images, String status);
}
