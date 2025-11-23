package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ProjectBudgets;
import com.ruoyi.web.mapper.ProjectBudgetsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 预算明细Service单元测试
 *
 * 说明：
 * - 使用纯Mockito进行单元测试
 * - 只测试不涉及认证的方法（select* 和 delete*）
 * - 涉及SecurityUtils的方法（insert/update）需在集成测试或真实环境中验证
 *
 * @author evs
 * @date 2025-11-23
 */
@ExtendWith(MockitoExtension.class)
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
        assertNotNull(result, "���询结果不应为null");
        assertEquals(1, result.size(), "应该返回1条记录");
        assertEquals("水电安装", result.get(0).getCategory(), "预算分类应该匹配");

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

        // 验证调���
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

    /**
     * 注意：以下测试需要mock SecurityUtils.getUsername()，
     * 由于这是纯单元测试，推荐在集成测试或真实环境中验证
     *
     * 需要在集成测试中验证的测试场景：
     * 1. 新增预算明细 (insertProjectBudgets)
     *    - 验证ID自动生成
     *    - 验证createdAt设置
     *    - 验证createdBy设置
     *
     * 2. 修改预算明细 (updateProjectBudgets)
     *    - 验证updatedAt设置
     *    - 验证updatedBy设置
     *
     * 集成测��建议：
     * - 使用@WithMockUser("admin")或@WithMockUser(username="admin", roles={"user"})
     * - 或者在测试配置中mock SecurityUtils.getUsername()
     * - 或者在真实环境中进行端到端测试
     */
}
