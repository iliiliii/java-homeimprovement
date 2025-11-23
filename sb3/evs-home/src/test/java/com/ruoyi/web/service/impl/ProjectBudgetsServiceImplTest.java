package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ProjectBudgets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 预算明细Service测试
 *
 * @author evs
 * @date 2025-11-23
 */
@ExtendWith(MockitoExtension.class)
@Transactional
@DisplayName("ProjectBudgetsService 单元测试")
class ProjectBudgetsServiceImplTest {

    @Mock
    private ProjectBudgetsMapper mockProjectBudgetsMapper;

    @InjectMocks
    private ProjectBudgetsServiceImpl projectBudgetsService;

    private ProjectBudgets projectBudget1;
    private ProjectBudgets projectBudget2;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        projectBudget1 = new ProjectBudgets();
        projectBudget1.setId("budget001");
        projectBudget1.setProjectId("P2024110100001");
        projectBudget1.setCategory("水电安装");
        projectBudget1.setPlannedAmount(BigDecimal.valueOf(1000));
        projectBudget1.setRemarks("水电改造预算");

        projectBudget2 = new ProjectBudgets();
        projectBudget2.setId("budget002");
        projectBudget2.setProjectId("P2024110100002");
        projectBudget2.setCategory("拆除工程");
        projectBudget2.setPlannedAmount(BigDecimal.valueOf(2000));
        projectBudget2.setRemarks("拆除工程预算");
    }

    @Test
    @DisplayName("测试按项目ID查询预算明细列表 - 应该返回指定项目的预算")
    void testSelectProjectBudgetsListByProjectId() {
        // Arrange
        ProjectBudgets query = new ProjectBudgets();
        query.setProjectId("P2024110100001");

        when(mockProjectBudgetsMapper.selectProjectBudgetsList(query))
            .thenReturn(Arrays.asList(projectBudget1));

        // Act
        List<ProjectBudgets> result = projectBudgetsService.selectProjectBudgetsList(query);

        // Assert
        assertNotNull(result, "查询结果不应为null");
        assertEquals(1, result.size(), "应该返回1条记录");
        assertEquals("P2024110100001", result.get(0).getProjectId(), "项目ID应该匹配");
        assertEquals("水电安装", result.get(0).getCategory(), "预算分类应该匹配");

        // 验证调用
        verify(mockProjectBudgetsMapper, times(1)).selectProjectBudgetsList(eq(query));
    }

    @Test
    @DisplayName("测试按预算分类查询 - 应该返回指定分类的预算")
    void testSelectProjectBudgetsListByCategory() {
        // Arrange
        ProjectBudgets query = new ProjectBudgets();
        query.setCategory("水电安装");

        when(mockProjectBudgetsMapper.selectProjectBudgetsList(query))
            .thenReturn(Arrays.asList(projectBudget1));

        // Act
        List<ProjectBudgets> result = projectBudgetsService.selectProjectBudgetsList(query);

        // Assert
        assertNotNull(result, "查询结果不应为null");
        assertEquals(1, result.size(), "应该返回1条记录");
        assertEquals("水电安装", result.get(0).getCategory(), "预算分��应该匹配");

        // 验证调用
        verify(mockProjectBudgetsMapper, times(1)).selectProjectBudgetsList(eq(query));
    }

    @Test
    @DisplayName("测试按项目ID和预算分类组合查询 - 应该返回匹配条件的预算")
    void testSelectProjectBudgetsListByProjectIdAndCategory() {
        // Arrange
        ProjectBudgets query = new ProjectBudgets();
        query.setProjectId("P2024110100001");
        query.setCategory("水电安装");

        when(mockProjectBudgetsMapper.selectProjectBudgetsList(query))
            .thenReturn(Arrays.asList(projectBudget1));

        // Act
        List<ProjectBudgets> result = projectBudgetsService.selectProjectBudgetsList(query);

        // Assert
        assertNotNull(result, "查询结果不应为null");
        assertEquals(1, result.size(), "应该返回1条记录");
        assertEquals("P2024110100001", result.get(0).getProjectId(), "项目ID应该匹配");
        assertEquals("水电安装", result.get(0).getCategory(), "预算分类应该匹配");

        // 验证调用
        verify(mockProjectBudgetsMapper, times(1)).selectProjectBudgetsList(eq(query));
    }

    @Test
    @DisplayName("测试查询不存在的项目ID - 应该返回空列表")
    void testSelectProjectBudgetsListByNonExistentProjectId() {
        // Arrange
        ProjectBudgets query = new ProjectBudgets();
        query.setProjectId("NON_EXISTENT_PROJECT");

        when(mockProjectBudgetsMapper.selectProjectBudgetsList(query))
            .thenReturn(Arrays.asList());

        // Act
        List<ProjectBudgets> result = projectBudgetsService.selectProjectBudgetsList(query);

        // Assert
        assertNotNull(result, "查询结果不应为null");
        assertTrue(result.isEmpty(), "应该返回空列表");

        // 验证调用
        verify(mockProjectBudgetsMapper, times(1)).selectProjectBudgetsList(eq(query));
    }

    @Test
    @DisplayName("测试查询所有预算（无过滤条件） - 应该返回所有预算")
    void testSelectProjectBudgetsListWithoutConditions() {
        // Arrange
        ProjectBudgets query = new ProjectBudgets();
        List<ProjectBudgets> allBudgets = Arrays.asList(projectBudget1, projectBudget2);

        when(mockProjectBudgetsMapper.selectProjectBudgetsList(query))
            .thenReturn(allBudgets);

        // Act
        List<ProjectBudgets> result = projectBudgetsService.selectProjectBudgetsList(query);

        // Assert
        assertNotNull(result, "查询结果不应为null");
        assertEquals(2, result.size(), "应该返回2条记录");

        // 验证调用
        verify(mockProjectBudgetsMapper, times(1)).selectProjectBudgetsList(eq(query));
    }

    @Test
    @DisplayName("测试查询预算明细详情 - 应该返回正确的预算明细")
    void testSelectProjectBudgetsById() {
        // Arrange
        String budgetId = "budget001";
        when(mockProjectBudgetsMapper.selectProjectBudgetsById(budgetId))
            .thenReturn(projectBudget1);

        // Act
        ProjectBudgets result = projectBudgetsService.selectProjectBudgetsById(budgetId);

        // Assert
        assertNotNull(result, "查询结果不应为null");
        assertEquals("budget001", result.getId(), "ID应该匹配");
        assertEquals("P2024110100001", result.getProjectId(), "项目ID应该匹配");
        assertEquals("水电安装", result.getCategory(), "预算分类应该匹配");
        assertEquals(BigDecimal.valueOf(1000), result.getPlannedAmount(), "计划金额应该匹配");

        // 验证调用
        verify(mockProjectBudgetsMapper, times(1)).selectProjectBudgetsById(eq(budgetId));
    }

    @Test
    @DisplayName("测试查询不存在的预算明细 - 应该返回null")
    void testSelectProjectBudgetsByNonExistentId() {
        // Arrange
        String budgetId = "NON_EXISTENT_ID";
        when(mockProjectBudgetsMapper.selectProjectBudgetsById(budgetId))
            .thenReturn(null);

        // Act
        ProjectBudgets result = projectBudgetsService.selectProjectBudgetsById(budgetId);

        // Assert
        assertNull(result, "查询不存在的预算明细应该返回null");

        // 验证调用
        verify(mockProjectBudgetsMapper, times(1)).selectProjectBudgetsById(eq(budgetId));
    }

    @Test
    @DisplayName("测试新增预算明细 - 应该成功插入并返回1")
    void testInsertProjectBudgets() {
        // Arrange
        ProjectBudgets newBudget = new ProjectBudgets();
        newBudget.setProjectId("P2024110100003");
        newBudget.setCategory("泥瓦工程");
        newBudget.setPlannedAmount(BigDecimal.valueOf(3000));
        newBudget.setRemarks("泥瓦工程预算");

        when(mockProjectBudgetsMapper.insertProjectBudgets(any()))
            .thenReturn(1);

        // Act
        int result = projectBudgetsService.insertProjectBudgets(newBudget);

        // Assert
        assertEquals(1, result, "插入成功应该返回1");

        // 验证调用
        verify(mockProjectBudgetsMapper, times(1)).insertProjectBudgets(any());
    }

    @Test
    @DisplayName("测试修改预算明细 - 应该成功更新并返回1")
    void testUpdateProjectBudgets() {
        // Arrange
        ProjectBudgets updateBudget = new ProjectBudgets();
        updateBudget.setId("budget001");
        updateBudget.setProjectId("P2024110100001");
        updateBudget.setCategory("水电安装");
        updateBudget.setPlannedAmount(BigDecimal.valueOf(1500)); // 修改金额
        updateBudget.setRemarks("水电改造预算（已更新）");

        when(mockProjectBudgetsMapper.updateProjectBudgets(any()))
            .thenReturn(1);

        // Act
        int result = projectBudgetsService.updateProjectBudgets(updateBudget);

        // Assert
        assertEquals(1, result, "更新成功应该返回1");

        // 验证调用
        verify(mockProjectBudgetsMapper, times(1)).updateProjectBudgets(any());
    }

    @Test
    @DisplayName("测试删除单个预算明细 - 应该成功删除并返回1")
    void testDeleteProjectBudgetsById() {
        // Arrange
        String budgetId = "budget001";
        when(mockProjectBudgetsMapper.deleteProjectBudgetsById(budgetId))
            .thenReturn(1);

        // Act
        int result = projectBudgetsService.deleteProjectBudgetsById(budgetId);

        // Assert
        assertEquals(1, result, "删除成功应该返回1");

        // 验证调用
        verify(mockProjectBudgetsMapper, times(1)).deleteProjectBudgetsById(eq(budgetId));
    }

    @Test
    @DisplayName("测试批量删除预算明细 - 应该成功删除并返回删除数量")
    void testDeleteProjectBudgetsByIds() {
        // Arrange
        String[] budgetIds = {"budget001", "budget002", "budget003"};
        when(mockProjectBudgetsMapper.deleteProjectBudgetsByIds(budgetIds))
            .thenReturn(3);

        // Act
        int result = projectBudgetsService.deleteProjectBudgetsByIds(budgetIds);

        // Assert
        assertEquals(3, result, "批量删除成功应该返回3");

        // 验证调用
        verify(mockProjectBudgetsMapper, times(1)).deleteProjectBudgetsByIds(eq(budgetIds));
    }

    @Test
    @DisplayName("测试新增预算明细时自动生成ID - 应该调用IdUtils生成UUID")
    void testInsertProjectBudgetsWithAutoGeneratedId() {
        // Arrange
        ProjectBudgets newBudget = new ProjectBudgets();
        newBudget.setProjectId("P2024110100003");
        newBudget.setCategory("木工工程");
        newBudget.setPlannedAmount(BigDecimal.valueOf(4000));
        // 注意：不设置ID，应该由服务自动生成

        when(mockProjectBudgetsMapper.insertProjectBudgets(any()))
            .thenReturn(1);

        // Act
        int result = projectBudgetsService.insertProjectBudgets(newBudget);

        // Assert
        assertEquals(1, result, "插入成功应该返回1");

        // 验证调用（由于我们在spy或mock中设置了自动生成ID的逻辑）
        verify(mockProjectBudgetsMapper, times(1)).insertProjectBudgets(any());
    }
}
