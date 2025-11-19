package com.ruoyi.web.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.web.domain.ProjectSchedules;
import com.ruoyi.web.mapper.ProjectSchedulesMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 项目进度Service实现类测试
 * 测试重点：重复验证逻辑
 *
 * @author evs
 * @date 2025-11-20
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("项目进度Service实现类测试")
class ProjectSchedulesServiceImplTest {

    @Mock
    private ProjectSchedulesMapper projectSchedulesMapper;

    @InjectMocks
    private ProjectSchedulesServiceImpl projectSchedulesService;

    private ProjectSchedules testSchedule;
    private ProjectSchedules existingSchedule;

    // 测试常量
    private static final String PROJECT_ID_1 = "proj-001";
    private static final String PROJECT_ID_2 = "proj-002";
    private static final String SCHEDULE_ID_1 = "schedule-001";
    private static final String SCHEDULE_ID_2 = "schedule-002";
    private static final String DISMANTLING = "DISMANTLING";
    private static final String WATER_ELECTRIC = "WATER_ELECTRIC";
    private static final String TILES = "TILES";

    @BeforeEach
    void setUp() {
        // 创建基础测试数据
        testSchedule = createTestSchedule(SCHEDULE_ID_1, PROJECT_ID_1, DISMANTLING);
        existingSchedule = createTestSchedule(SCHEDULE_ID_2, PROJECT_ID_1, WATER_ELECTRIC);
    }

    private ProjectSchedules createTestSchedule(String id, String projectId, String stage) {
        ProjectSchedules schedule = new ProjectSchedules();
        schedule.setId(id);
        schedule.setProjectId(projectId);
        schedule.setStage(stage);
        schedule.setStageOrder(1L);
        schedule.setStatus("PLANNED");
        schedule.setCompletionRate(new BigDecimal("0"));
        schedule.setDescription("测试施工阶段");
        schedule.setCreatedAt(new Date());
        schedule.setUpdatedAt(new Date());
        schedule.setCreatedBy("test-user");
        schedule.setUpdatedBy("test-user");
        return schedule;
    }

    // ==================== 重复验证私有方法测试 ====================

    @Test
    @DisplayName("测试重复检查 - 存在重复记录")
    void testCheckDuplicate_WhenDuplicateExists_ShouldReturnDuplicate() {
        // Given - 模拟存在重复记录
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Arrays.asList(existingSchedule));

        // When - 使用反射调用私有方法
        ProjectSchedules result = invokeCheckDuplicate(PROJECT_ID_1, WATER_ELECTRIC);

        // Then
        assertNotNull(result, "存在重复记录时应返回重复的记录");
        assertEquals(WATER_ELECTRIC, result.getStage());
        assertEquals(PROJECT_ID_1, result.getProjectId());
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
    }

    @Test
    @DisplayName("测试重复检查 - 不存在重复记录")
    void testCheckDuplicate_WhenNoDuplicateExists_ShouldReturnNull() {
        // Given - 模拟不存在重复记录
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Collections.emptyList());

        // When
        ProjectSchedules result = invokeCheckDuplicate(PROJECT_ID_1, TILES);

        // Then
        assertNull(result, "不存在重复记录时应返回null");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
    }

    @Test
    @DisplayName("测试重复检查 - projectId为null")
    void testCheckDuplicate_WhenProjectIdIsNull_ShouldReturnNull() {
        // When
        ProjectSchedules result = invokeCheckDuplicate(null, DISMANTLING);

        // Then
        assertNull(result, "projectId为null时应返回null");
        verify(projectSchedulesMapper, never()).selectProjectSchedulesList(any());
    }

    @Test
    @DisplayName("测试重复检查 - projectId为空字符串")
    void testCheckDuplicate_WhenProjectIdIsEmpty_ShouldReturnNull() {
        // When
        ProjectSchedules result = invokeCheckDuplicate("", DISMANTLING);

        // Then
        assertNull(result, "projectId为空字符串时应返回null");
        verify(projectSchedulesMapper, never()).selectProjectSchedulesList(any());
    }

    @Test
    @DisplayName("测试重复检查 - stage为null")
    void testCheckDuplicate_WhenStageIsNull_ShouldReturnNull() {
        // When
        ProjectSchedules result = invokeCheckDuplicate(PROJECT_ID_1, null);

        // Then
        assertNull(result, "stage为null时应返回null");
        verify(projectSchedulesMapper, never()).selectProjectSchedulesList(any());
    }

    @Test
    @DisplayName("测试重复检查 - stage为空字符串")
    void testCheckDuplicate_WhenStageIsEmpty_ShouldReturnNull() {
        // When
        ProjectSchedules result = invokeCheckDuplicate(PROJECT_ID_1, "");

        // Then
        assertNull(result, "stage为空字符串时应返回null");
        verify(projectSchedulesMapper, never()).selectProjectSchedulesList(any());
    }

    @Test
    @DisplayName("测试排除重复检查 - 排除ID匹配")
    void testCheckDuplicateExclude_WhenExcludeIdMatches_ShouldReturnNull() {
        // Given - 排除ID与查询记录ID相同
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Arrays.asList(testSchedule));

        // When
        ProjectSchedules result = invokeCheckDuplicateExclude(PROJECT_ID_1, DISMANTLING, SCHEDULE_ID_1);

        // Then
        assertNull(result, "排除ID与查询记录ID相同时应返回null");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
    }

    @Test
    @DisplayName("测试排除重复检查 - 排除ID不匹配")
    void testCheckDuplicateExclude_WhenExcludeIdNotMatch_ShouldReturnDuplicate() {
        // Given - 排除ID与查询记录ID不同
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Arrays.asList(testSchedule));

        // When
        ProjectSchedules result = invokeCheckDuplicateExclude(PROJECT_ID_1, DISMANTLING, "different-id");

        // Then
        assertNotNull(result, "排除ID与查询记录ID不同时应返回重复记录");
        assertEquals(SCHEDULE_ID_1, result.getId());
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
    }

    @Test
    @DisplayName("测试排除重复检查 - 查询结果为空")
    void testCheckDuplicateExclude_WhenQueryResultEmpty_ShouldReturnNull() {
        // Given - 查询结果为空
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Collections.emptyList());

        // When
        ProjectSchedules result = invokeCheckDuplicateExclude(PROJECT_ID_1, TILES, SCHEDULE_ID_1);

        // Then
        assertNull(result, "查询结果为空时应返回null");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
    }

    @Test
    @DisplayName("测试排除重复检查 - 数据库查询异常")
    void testCheckDuplicateExclude_WhenMapperThrowsException_ShouldPropagateException() {
        // Given - 数据库查询异常
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenThrow(new RuntimeException("数据库连接异常"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            invokeCheckDuplicateExclude(PROJECT_ID_1, DISMANTLING, SCHEDULE_ID_1);
        });
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
    }

    // ==================== insertProjectSchedules方法测试 ====================

    @Test
    @DisplayName("测试新增项目进度 - 无重复记录，新增成功")
    void testInsertProjectSchedules_WhenNoDuplicate_ShouldReturnSuccess() {
        // Given - 无重复记录
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Collections.emptyList());
        when(projectSchedulesMapper.insertProjectSchedules(any(ProjectSchedules.class)))
                .thenReturn(1);

        // When
        int result = projectSchedulesService.insertProjectSchedules(testSchedule);

        // Then
        assertEquals(1, result, "新增成功应返回1");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, times(1)).insertProjectSchedules(testSchedule);
    }

    @Test
    @DisplayName("测试新增项目进度 - 存在重复记录，抛出异常")
    void testInsertProjectSchedules_WhenDuplicateExists_ShouldThrowServiceException() {
        // Given - 存在重复记录
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Arrays.asList(existingSchedule));

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            projectSchedulesService.insertProjectSchedules(testSchedule);
        });

        assertEquals("该施工阶段已存在，请选择其他阶段", exception.getMessage());
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, never()).insertProjectSchedules(any());
    }

    @Test
    @DisplayName("测试新增项目进度 - 不同项目ID，相同stage，新增成功")
    void testInsertProjectSchedules_WhenDifferentProjectId_ShouldReturnSuccess() {
        // Given - 不同项目的相同stage不算重复
        testSchedule.setProjectId(PROJECT_ID_2);
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Collections.emptyList());
        when(projectSchedulesMapper.insertProjectSchedules(any(ProjectSchedules.class)))
                .thenReturn(1);

        // When
        int result = projectSchedulesService.insertProjectSchedules(testSchedule);

        // Then
        assertEquals(1, result, "不同项目的相同stage应允许新增");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, times(1)).insertProjectSchedules(testSchedule);
    }

    // ==================== updateProjectSchedules方法测试 ====================

    @Test
    @DisplayName("测试更新项目进度 - 修改为不重复的stage，更新成功")
    void testUpdateProjectSchedules_WhenNoDuplicate_ShouldReturnSuccess() {
        // Given - 修改为不重复的stage
        testSchedule.setStage(TILES);
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Collections.emptyList());
        when(projectSchedulesMapper.updateProjectSchedules(any(ProjectSchedules.class)))
                .thenReturn(1);

        // When
        int result = projectSchedulesService.updateProjectSchedules(testSchedule);

        // Then
        assertEquals(1, result, "更新成功应返回1");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, times(1)).updateProjectSchedules(testSchedule);
    }

    @Test
    @DisplayName("测试更新项目进度 - 修改为重复的stage，抛出异常")
    void testUpdateProjectSchedules_WhenDuplicateExists_ShouldThrowServiceException() {
        // Given - 修改为重复的stage
        testSchedule.setStage(WATER_ELECTRIC);
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Arrays.asList(existingSchedule));

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            projectSchedulesService.updateProjectSchedules(testSchedule);
        });

        assertEquals("该施工阶段已存在，请选择其他阶段", exception.getMessage());
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, never()).updateProjectSchedules(any());
    }

    @Test
    @DisplayName("测试更新项目进度 - 更新当前记录的stage到相同值，更新成功")
    void testUpdateProjectSchedules_WhenStageUnchanged_ShouldReturnSuccess() {
        // Given - stage不变，只修改其他字段
        testSchedule.setDescription("更新后的描述");
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Arrays.asList(testSchedule)); // 返回自己
        when(projectSchedulesMapper.updateProjectSchedules(any(ProjectSchedules.class)))
                .thenReturn(1);

        // When
        int result = projectSchedulesService.updateProjectSchedules(testSchedule);

        // Then
        assertEquals(1, result, "stage不变的更新应成功");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, times(1)).updateProjectSchedules(testSchedule);
    }

    @Test
    @DisplayName("测试更新项目进度 - 不同项目ID，相同stage，更新成功")
    void testUpdateProjectSchedules_WhenDifferentProjectId_ShouldReturnSuccess() {
        // Given - 不同项目的相同stage不算重复
        testSchedule.setProjectId(PROJECT_ID_2);
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Collections.emptyList());
        when(projectSchedulesMapper.updateProjectSchedules(any(ProjectSchedules.class)))
                .thenReturn(1);

        // When
        int result = projectSchedulesService.updateProjectSchedules(testSchedule);

        // Then
        assertEquals(1, result, "不同项目的相同stage应允许更新");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, times(1)).updateProjectSchedules(testSchedule);
    }

    // ==================== 标准CRUD方法测试 ====================

    @Test
    @DisplayName("测试查询项目进度 - 通过ID查询")
    void testSelectProjectSchedulesById_ShouldReturnSchedule() {
        // Given
        when(projectSchedulesMapper.selectProjectSchedulesById(SCHEDULE_ID_1))
                .thenReturn(testSchedule);

        // When
        ProjectSchedules result = projectSchedulesService.selectProjectSchedulesById(SCHEDULE_ID_1);

        // Then
        assertNotNull(result);
        assertEquals(SCHEDULE_ID_1, result.getId());
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesById(SCHEDULE_ID_1);
    }

    @Test
    @DisplayName("测试查询项目进度列表")
    void testSelectProjectSchedulesList_ShouldReturnList() {
        // Given
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Arrays.asList(testSchedule, existingSchedule));

        // When
        List<ProjectSchedules> result = projectSchedulesService.selectProjectSchedulesList(new ProjectSchedules());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
    }

    @Test
    @DisplayName("测试删除项目进度 - 通过ID删除")
    void testDeleteProjectSchedulesById_ShouldReturnSuccess() {
        // Given
        when(projectSchedulesMapper.deleteProjectSchedulesById(SCHEDULE_ID_1))
                .thenReturn(1);

        // When
        int result = projectSchedulesService.deleteProjectSchedulesById(SCHEDULE_ID_1);

        // Then
        assertEquals(1, result);
        verify(projectSchedulesMapper, times(1)).deleteProjectSchedulesById(SCHEDULE_ID_1);
    }

    @Test
    @DisplayName("测试批量删除项目进度")
    void testDeleteProjectSchedulesByIds_ShouldReturnSuccess() {
        // Given
        String[] ids = {SCHEDULE_ID_1, SCHEDULE_ID_2};
        when(projectSchedulesMapper.deleteProjectSchedulesByIds(ids))
                .thenReturn(2);

        // When
        int result = projectSchedulesService.deleteProjectSchedulesByIds(ids);

        // Then
        assertEquals(2, result);
        verify(projectSchedulesMapper, times(1)).deleteProjectSchedulesByIds(ids);
    }

    // ==================== 反射调用私有方法辅助 ====================

    /**
     * 使用反射调用私有方法 checkDuplicate
     */
    private ProjectSchedules invokeCheckDuplicate(String projectId, String stage) {
        try {
            var method = ProjectSchedulesServiceImpl.class.getDeclaredMethod("checkDuplicate", String.class, String.class);
            method.setAccessible(true);
            return (ProjectSchedules) method.invoke(projectSchedulesService, projectId, stage);
        } catch (Exception e) {
            throw new RuntimeException("反射调用checkDuplicate方法失败", e);
        }
    }

    /**
     * 使用反射调用私有方法 checkDuplicateExclude
     */
    private ProjectSchedules invokeCheckDuplicateExclude(String projectId, String stage, String excludeId) {
        try {
            var method = ProjectSchedulesServiceImpl.class.getDeclaredMethod("checkDuplicateExclude", String.class, String.class, String.class);
            method.setAccessible(true);
            return (ProjectSchedules) method.invoke(projectSchedulesService, projectId, stage, excludeId);
        } catch (Exception e) {
            throw new RuntimeException("反射调用checkDuplicateExclude方法失败", e);
        }
    }
}