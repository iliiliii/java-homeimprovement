package com.ruoyi.app.mapper;

import com.ruoyi.app.dto.response.ProjectScheduleVO;
import com.ruoyi.app.dto.response.ProjectScheduleRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 小程序项目进度数据访问层
 */
@Mapper
public interface AppProjectScheduleMapper {

    /**
     * 查询项目进度列表
     * @param projectId 项目ID
     * @return 进度列表
     */
    List<ProjectScheduleVO> selectProjectScheduleList(@Param("projectId") String projectId);

    /**
     * 查询进度的最新验收记录
     * @param scheduleId 进度ID
     * @param limit 限制数量
     * @return 最新记录列表
     */
    List<ProjectScheduleRecordVO> selectLatestRecordsByScheduleId(@Param("scheduleId") String scheduleId, @Param("limit") Integer limit);

    /**
     * 统计进度的验收记录数量
     * @param scheduleId 进度ID
     * @return 记录数量
     */
    Integer countRecordsByScheduleId(@Param("scheduleId") String scheduleId);

    /**
     * 查询项目验收记录列表（分页）
     * @param projectId 项目ID
     * @param scheduleId 进度ID（可选）
     * @param offset 偏移量
     * @param pageSize 页大小
     * @return 记录列表
     */
    List<ProjectScheduleRecordVO> selectProjectScheduleRecordList(
            @Param("projectId") String projectId,
            @Param("scheduleId") String scheduleId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize);

    /**
     * 统计项目验收记录总数
     * @param projectId 项目ID
     * @param scheduleId 进度ID（可选）
     * @return 记录总数
     */
    Long countProjectScheduleRecords(@Param("projectId") String projectId, @Param("scheduleId") String scheduleId);

    /**
     * 根据ID查询验收记录详情
     * @param recordId 记录ID
     * @return 记录详情
     */
    ProjectScheduleRecordVO selectProjectScheduleRecordById(@Param("recordId") String recordId);

    /**
     * 查询记录的图片JSON字符串
     * @param recordId 记录ID
     * @return 图片JSON字符串
     */
    String selectRecordImages(@Param("recordId") String recordId);

    /**
     * 查询记录的附件列表
     * @param recordId 记录ID
     * @return 附件列表
     */
    List<ProjectScheduleRecordVO.AttachmentVO> selectRecordAttachments(@Param("recordId") String recordId);

    /**
     * 插入验收记录
     * @param id 记录ID
     * @param projectId 项目ID
     * @param scheduleId 进度ID
     * @param recordType 记录类型
     * @param images 图片JSON
     * @param acceptanceTitle 验收标题
     * @param acceptanceContent 验收内容
     * @param acceptanceResult 验收结果
     * @param acceptanceTime 验收时间
     * @param acceptor 验收人
     * @param createBy 创建人
     * @return 影响行数
     */
    int insertAcceptanceRecord(
            @Param("id") String id,
            @Param("projectId") String projectId,
            @Param("scheduleId") String scheduleId,
            @Param("recordType") String recordType,
            @Param("images") String images,
            @Param("acceptanceTitle") String acceptanceTitle,
            @Param("acceptanceContent") String acceptanceContent,
            @Param("acceptanceResult") String acceptanceResult,
            @Param("acceptanceTime") java.util.Date acceptanceTime,
            @Param("acceptor") String acceptor,
            @Param("createBy") String createBy);

    /**
     * 更新验收记录
     * @param id 记录ID
     * @param images 图片JSON
     * @param acceptanceTitle 验收标题
     * @param acceptanceContent 验收内容
     * @param acceptanceResult 验收结果
     * @param acceptanceTime 验收时间
     * @param acceptor 验收人
     * @param updateBy 更新人
     * @return 影响行数
     */
    int updateAcceptanceRecord(
            @Param("id") String id,
            @Param("images") String images,
            @Param("acceptanceTitle") String acceptanceTitle,
            @Param("acceptanceContent") String acceptanceContent,
            @Param("acceptanceResult") String acceptanceResult,
            @Param("acceptanceTime") java.util.Date acceptanceTime,
            @Param("acceptor") String acceptor,
            @Param("updateBy") String updateBy);

    /**
     * 删除验收记录
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteAcceptanceRecord(@Param("id") String id);

    /**
     * 查询记录创建人
     * @param recordId 记录ID
     * @return 创建人ID
     */
    String selectRecordCreateBy(@Param("recordId") String recordId);
}