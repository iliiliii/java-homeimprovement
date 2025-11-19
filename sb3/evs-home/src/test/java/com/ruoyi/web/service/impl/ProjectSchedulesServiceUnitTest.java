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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 项目进度Service层独立单元测试类
 * 使用Mockito进行依赖隔离测试，专注于业务逻辑验证
 *
 * @author evs
 * @date 2025-11-20
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("项目进度Service层单元测试")
class ProjectSchedulesServiceUnitTest {

    @Mock
    private ProjectSchedulesMapper projectSchedulesMapper;

    @InjectMocks
    private ProjectSchedulesServiceImpl projectSchedulesService;

    // 测试数据
    private ProjectSchedules dismantlingSchedule;
    private ProjectSchedules waterElectricSchedule;
    private ProjectSchedules tilesSchedule;

    // 测试常量
    private static final String PROJECT_ID = "project-001";
    private static final String DEMOLITION_ID = "demolition-001";
    private static final String WATER_ELECTRIC_ID = "water-electric-001";
    private static final String TILES_ID = "tiles-001";
    private static final String DISMANTLING = "DISMANTLING";
    private static final String WATER_ELECTRIC = "WATER_ELECTRIC";
    private static final String TILES = "TILES";
    private static final String WOODWORK = "WOODWORK";

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        dismantlingSchedule = createTestSchedule(DEMOLITION_ID, PROJECT_ID, DISMANTLING, 1L);
        waterElectricSchedule = createTestSchedule(WATER_ELECTRIC_ID, PROJECT_ID, WATER_ELECTRIC, 2L);
        tilesSchedule = createTestSchedule(TILES_ID, PROJECT_ID, TILES, 3L);
    }

    /**
     * 创建测试用的ProjectSchedules对象
     */
    private ProjectSchedules createTestSchedule(String id, String projectId, String stage, Long stageOrder) {
        ProjectSchedules schedule = new ProjectSchedules();
        schedule.setId(id);
        schedule.setProjectId(projectId);
        schedule.setStage(stage);
        schedule.setStageOrder(stageOrder);
        schedule.setStatus("PLANNED");
        schedule.setCompletionRate(new BigDecimal("0"));
        schedule.setDescription(stage + "阶段施工");
        schedule.setCreatedAt(new Date());
        schedule.setUpdatedAt(new Date());
        schedule.setCreatedBy("test-user");
        schedule.setUpdatedBy("test-user");
        return schedule;
    }

    // ==================== 新增操作测试 ====================

    @Test
    @DisplayName("测试新增项目进度 - 首次添加某阶段，应成功")
    void testInsertProjectSchedules_FirstTimeAddStage_ShouldReturnSuccess() {
        // Given - 首次添加拆除阶段
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Collections.emptyList());
        when(projectSchedulesMapper.insertProjectSchedules(any(ProjectSchedules.class)))
                .thenReturn(1);

        // When
        int result = projectSchedulesService.insertProjectSchedules(dismantlingSchedule);

        // Then
        assertEquals(1, result, "首次添加施工阶段应该成功");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, times(1)).insertProjectSchedules(dismantlingSchedule);
    }

    @Test
    @DisplayName("测试新增项目进度 - 添加已存在的施工阶段，应抛出异常")
    void testInsertProjectSchedules_AddExistingStage_ShouldThrowServiceException() {
        // Given - 拆除阶段已存在
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Arrays.asList(dismantlingSchedule));

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            projectSchedulesService.insertProjectSchedules(dismantlingSchedule);
        });

        assertEquals("该施工阶段已存在，请选择其他阶段", exception.getMessage());
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, never()).insertProjectSchedules(any());
    }

    @Test
    @DisplayName("测试新增项目进度 - 不同项目相同阶段，应成功")
    void testInsertProjectSchedules_DifferentProjectSameStage_ShouldReturnSuccess() {
        // Given - 不同项目的相同阶段
        ProjectSchedules differentProjectSchedule = createTestSchedule("new-id", "project-002", DISMANTLING, 1L);
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Collections.emptyList());
        when(projectSchedulesMapper.insertProjectSchedules(any(ProjectSchedules.class)))
                .thenReturn(1);

        // When
        int result = projectSchedulesService.insertProjectSchedules(differentProjectSchedule);

        // Then
        assertEquals(1, result, "不同项目的相同阶段应该允许添加");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, times(1)).insertProjectSchedules(differentProjectSchedule);
    }

    // ==================== 更新操作测试 ====================

    @Test
    @DisplayName("测试更新项目进度 - 修改为新的施工阶段，应成功")
    void testUpdateProjectSchedules_ChangeToNewStage_ShouldReturnSuccess() {
        // Given - 将拆除阶段改为木工阶段
        dismantlingSchedule.setStage(WOODWORK);
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Collections.emptyList());
        when(projectSchedulesMapper.updateProjectSchedules(any(ProjectSchedules.class)))
                .thenReturn(1);

        // When
        int result = projectSchedulesService.updateProjectSchedules(dismantlingSchedule);

        // Then
        assertEquals(1, result, "修改为新的施工阶段应该成功");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, times(1)).updateProjectSchedules(dismantlingSchedule);
    }

    @Test
    @DisplayName("测试更新项目进度 - 修改为已存在的施工阶段，应抛出异常")
    void testUpdateProjectSchedules_ChangeToExistingStage_ShouldThrowServiceException() {
        // Given - 将拆除阶段改为水电阶段（水电阶段已存在）
        dismantlingSchedule.setStage(WATER_ELECTRIC);
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Arrays.asList(waterElectricSchedule));

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            projectSchedulesService.updateProjectSchedules(dismantlingSchedule);
        });

        assertEquals("该施工阶段已存在，请选择其他阶段", exception.getMessage());
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, never()).updateProjectSchedules(any());
    }

    @Test
    @DisplayName("测试更新项目进度 - 施工阶段不变，修改其他字段，应成功")
    void testUpdateProjectSchedules_UpdateOtherFields_ShouldReturnSuccess() {
        // Given - 只修改描述，施工阶段不变
        dismantlingSchedule.setDescription("更新后的拆除阶段描述");
        dismantlingSchedule.setCompletionRate(new BigDecimal("50"));
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Arrays.asList(dismantlingSchedule)); // 返回自己
        when(projectSchedulesMapper.updateProjectSchedules(any(ProjectSchedules.class)))
                .thenReturn(1);

        // When
        int result = projectSchedulesService.updateProjectSchedules(dismantlingSchedule);

        // Then
        assertEquals(1, result, "施工阶段不变的更新应该成功");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, times(1)).updateProjectSchedules(dismantlingSchedule);
    }

    // ==================== 查询操作测试 ====================

    @Test
    @DisplayName("测试查询项目进度 - 通过ID查询存在记录")
    void testSelectProjectSchedulesById_ExistingRecord_ShouldReturnSchedule() {
        // Given
        when(projectSchedulesMapper.selectProjectSchedulesById(DEMOLITION_ID))
                .thenReturn(dismantlingSchedule);

        // When
        ProjectSchedules result = projectSchedulesService.selectProjectSchedulesById(DEMOLITION_ID);

        // Then
        assertNotNull(result, "应该能查询到记录");
        assertEquals(DEMOLITION_ID, result.getId());
        assertEquals(DISMANTLING, result.getStage());
        assertEquals(PROJECT_ID, result.getProjectId());
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesById(DEMOLITION_ID);
    }

    @Test
    @DisplayName("测试查询项目进度 - 通过ID查询不存在记录")
    void testSelectProjectSchedulesById_NonExistingRecord_ShouldReturnNull() {
        // Given
        when(projectSchedulesMapper.selectProjectSchedulesById("non-existing-id"))
                .thenReturn(null);

        // When
        ProjectSchedules result = projectSchedulesService.selectProjectSchedulesById("non-existing-id");

        // Then
        assertNull(result, "查询不存在的记录应返回null");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesById("non-existing-id");
    }

    @Test
    @DisplayName("测试查询项目进度列表 - 返回多条记录")
    void testSelectProjectSchedulesList_MultipleRecords_ShouldReturnList() {
        // Given
        List<ProjectSchedules> expectedList = Arrays.asList(
                dismantlingSchedule, waterElectricSchedule, tilesSchedule
        );
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(expectedList);

        // When
        List<ProjectSchedules> result = projectSchedulesService.selectProjectSchedulesList(new ProjectSchedules());

        // Then
        assertNotNull(result, "查询结果不应为null");
        assertEquals(3, result.size(), "应该返回3条记录");
        assertEquals(DISMANTLING, result.get(0).getStage());
        assertEquals(WATER_ELECTRIC, result.get(1).getStage());
        assertEquals(TILES, result.get(2).getStage());
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
    }

    @Test
    @DisplayName("测试查询项目进度列表 - 无记录")
    void testSelectProjectSchedulesList_NoRecords_ShouldReturnEmptyList() {
        // Given
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenReturn(Collections.emptyList());

        // When
        List<ProjectSchedules> result = projectSchedulesService.selectProjectSchedulesList(new ProjectSchedules());

        // Then
        assertNotNull(result, "查询结果不应为null");
        assertTrue(result.isEmpty(), "无记录时应返回空列表");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
    }

    // ==================== 删除操作测试 ====================

    @Test
    @DisplayName("测试删除项目进度 - 通过ID删除成功")
    void testDeleteProjectSchedulesById_ExistingId_ShouldReturnSuccess() {
        // Given
        when(projectSchedulesMapper.deleteProjectSchedulesById(DEMOLITION_ID))
                .thenReturn(1);

        // When
        int result = projectSchedulesService.deleteProjectSchedulesById(DEMOLITION_ID);

        // Then
        assertEquals(1, result, "删除成功应返回1");
        verify(projectSchedulesMapper, times(1)).deleteProjectSchedulesById(DEMOLITION_ID);
    }

    @Test
    @DisplayName("测试删除项目进度 - 删除不存在的ID")
    void testDeleteProjectSchedulesById_NonExistingId_ShouldReturnZero() {
        // Given
        when(projectSchedulesMapper.deleteProjectSchedulesById("non-existing-id"))
                .thenReturn(0);

        // When
        int result = projectSchedulesService.deleteProjectSchedulesById("non-existing-id");

        // Then
        assertEquals(0, result, "删除不存在的记录应返回0");
        verify(projectSchedulesMapper, times(1)).deleteProjectSchedulesById("non-existing-id");
    }

    @Test
    @DisplayName("测试批量删除项目进度 - 成功删除多条")
    void testDeleteProjectSchedulesByIds_MultipleIds_ShouldReturnCount() {
        // Given
        String[] ids = {DEMOLITION_ID, WATER_ELECTRIC_ID, TILES_ID};
        when(projectSchedulesMapper.deleteProjectSchedulesByIds(ids))
                .thenReturn(3);

        // When
        int result = projectSchedulesService.deleteProjectSchedulesByIds(ids);

        // Then
        assertEquals(3, result, "批量删除成功应返回删除的记录数");
        verify(projectSchedulesMapper, times(1)).deleteProjectSchedulesByIds(ids);
    }

    @Test
    @DisplayName("测试批量删除项目进度 - 空ID数组")
    void testDeleteProjectSchedulesByIds_EmptyArray_ShouldReturnZero() {
        // Given
        String[] emptyIds = {};
        when(projectSchedulesMapper.deleteProjectSchedulesByIds(emptyIds))
                .thenReturn(0);

        // When
        int result = projectSchedulesService.deleteProjectSchedulesByIds(emptyIds);

        // Then
        assertEquals(0, result, "删除空ID数组应返回0");
        verify(projectSchedulesMapper, times(1)).deleteProjectSchedulesByIds(emptyIds);
    }

    // ==================== 异常处理测试 ====================

    @Test
    @DisplayName("测试新增项目进度 - 数据库异常")
    void testInsertProjectSchedules_DatabaseException_ShouldPropagateException() {
        // Given - 数据库操作异常
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            projectSchedulesService.insertProjectSchedules(dismantlingSchedule);
        });
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, never()).insertProjectSchedules(any());
    }

    @Test
    @DisplayName("测试更新项目进度 - 数据库异常")
    void testUpdateProjectSchedules_DatabaseException_ShouldPropagateException() {
        // Given - 数据库操作异常
        when(projectSchedulesMapper.selectProjectSchedulesList(any(ProjectSchedules.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            projectSchedulesService.updateProjectSchedules(dismantlingSchedule);
        });
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesList(any(ProjectSchedules.class));
        verify(projectSchedulesMapper, never()).updateProjectSchedules(any());
    }

    @Test
    @DisplayName("测试查询项目进度 - 数据库异常")
    void testSelectProjectSchedulesById_DatabaseException_ShouldPropagateException() {
        // Given - 数据库查询异常
        when(projectSchedulesMapper.selectProjectSchedulesById(anyString()))
                .thenThrow(new RuntimeException("数据库查询失败"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            projectSchedulesService.selectProjectSchedulesById(DEMOLITION_ID);
        });
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesById(DEMOLITION_ID);
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试新增项目进度 - null对象应抛出异常")
    void testInsertProjectSchedules_NullObject_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            projectSchedulesService.insertProjectSchedules(null);
        });
        verify(projectSchedulesMapper, never()).insertProjectSchedules(any());
    }

    @Test
    @DisplayName("测试更新项目进度 - null对象应抛出异常")
    void testUpdateProjectSchedules_NullObject_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            projectSchedulesService.updateProjectSchedules(null);
        });
        verify(projectSchedulesMapper, never()).updateProjectSchedules(any());
    }

    @Test
    @DisplayName("测试查询项目进度 - null ID")
    void testSelectProjectSchedulesById_NullId_ShouldReturnNull() {
        // Given
        when(projectSchedulesMapper.selectProjectSchedulesById(isNull()))
                .thenReturn(null);

        // When
        ProjectSchedules result = projectSchedulesService.selectProjectSchedulesById(null);

        // Then
        assertNull(result, "查询null ID应返回null");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesById(isNull());
    }

    @Test
    @DisplayName("测试查询项目进度 - 空字符串ID")
    void testSelectProjectSchedulesById_EmptyId_ShouldReturnNull() {
        // Given
        when(projectSchedulesMapper.selectProjectSchedulesById(anyString()))
                .thenReturn(null);

        // When
        ProjectSchedules result = projectSchedulesService.selectProjectSchedulesById("");

        // Then
        assertNull(result, "查询空字符串ID应返回null");
        verify(projectSchedulesMapper, times(1)).selectProjectSchedulesById(eq(""));
    }
}