package com.ruoyi.mapper.evs;

import com.ruoyi.projectScheduleStats.ProjectScheduleStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 项目进度统计Mapper接口
 */
@Mapper
public interface ProjectScheduleStatsMapper {

    /**
     * 批量查询项目进度统计
     * @param projectIds 项目ID列表（String类型）
     * @return 统计信息Map，key为项目ID（String），value为统计信息
     */
    @MapKey("projectId")
    Map<String, ProjectScheduleStats> selectScheduleStatsMap(@Param("projectIds") List<String> projectIds);
}
