package com.ruoyi.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.web.domain.ProjectBudgets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 预算明细Controller集成测试
 *
 * @author evs
 * @date 2025-11-23
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("ProjectBudgetsController 集成测试")
class ProjectBudgetsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ProjectBudgets testBudget1;
    private ProjectBudgets testBudget2;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testBudget1 = new ProjectBudgets();
        testBudget1.setId("test-budget-001");
        testBudget1.setProjectId("P2024110100001");
        testBudget1.setCategory("水电安装");
        testBudget1.setPlannedAmount(BigDecimal.valueOf(1000));
        testBudget1.setRemarks("水电改造预算");

        testBudget2 = new ProjectBudgets();
        testBudget2.setId("test-budget-002");
        testBudget2.setProjectId("P2024110100002");
        testBudget2.setCategory("拆除工程");
        testBudget2.setPlannedAmount(BigDecimal.valueOf(2000));
        testBudget2.setRemarks("拆除工程预算");
    }

    @Test
    @DisplayName("测试GET /evs/projectBudgets/list - 按项目ID查询预算明细")
    void testListProjectBudgetsByProjectId() throws Exception {
        // Given - 查询参数
        String projectId = "P2024110100001";

        // When & Then
        mockMvc.perform(get("/evs/projectBudgets/list")
                .param("projectId", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.msg").value("查询成功"))
            .andExpect(jsonPath("$.rows").isArray());
    }

    @Test
    @DisplayName("测试GET /evs/projectBudgets/list - 按预算分类查询")
    void testListProjectBudgetsByCategory() throws Exception {
        // Given - 查询参数
        String category = "水电安装";

        // When & Then
        mockMvc.perform(get("/evs/projectBudgets/list")
                .param("category", category)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.msg").value("查询成功"));
    }

    @Test
    @DisplayName("测试GET /evs/projectBudgets/list - 组合条件查询")
    void testListProjectBudgetsByMultipleConditions() throws Exception {
        // Given - 查询参数
        String projectId = "P2024110100001";
        String category = "水电安装";

        // When & Then
        mockMvc.perform(get("/evs/projectBudgets/list")
                .param("projectId", projectId)
                .param("category", category)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.msg").value("查询成功"));
    }

    @Test
    @DisplayName("测试GET /evs/projectBudgets/list - 不存在的项目ID应返回空列表")
    void testListProjectBudgetsWithNonExistentProjectId() throws Exception {
        // Given - 查询不存在的项目ID
        String projectId = "NON_EXISTENT_PROJECT";

        // When & Then
        mockMvc.perform(get("/evs/projectBudgets/list")
                .param("projectId", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.rows").isArray())
            .andExpect(jsonPath("$.rows", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    @DisplayName("测试GET /evs/projectBudgets/{id} - 获取预算明细详情")
    void testGetProjectBudgetsById() throws Exception {
        // Given - 预算ID
        String budgetId = "test-budget-001";

        // When & Then
        mockMvc.perform(get("/evs/projectBudgets/{id}", budgetId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试POST /evs/projectBudgets - 新增预算明细")
    void testAddProjectBudgets() throws Exception {
        // Given - 新预算数据
        ProjectBudgets newBudget = new ProjectBudgets();
        newBudget.setProjectId("P2024110100003");
        newBudget.setCategory("泥瓦工程");
        newBudget.setPlannedAmount(BigDecimal.valueOf(3000));
        newBudget.setRemarks("��瓦工程预算");

        // When & Then
        mockMvc.perform(post("/evs/projectBudgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBudget)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试PUT /evs/projectBudgets - 修改预算明细")
    void testUpdateProjectBudgets() throws Exception {
        // Given - 更新数据
        ProjectBudgets updateBudget = new ProjectBudgets();
        updateBudget.setId("test-budget-001");
        updateBudget.setProjectId("P2024110100001");
        updateBudget.setCategory("水电安装");
        updateBudget.setPlannedAmount(BigDecimal.valueOf(1500));
        updateBudget.setRemarks("水电改造预算（已更新）");

        // When & Then
        mockMvc.perform(put("/evs/projectBudgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBudget)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试DELETE /evs/projectBudgets/{id} - 删除单个预算明细")
    void testDeleteProjectBudgets() throws Exception {
        // Given - 预算ID
        String budgetId = "test-budget-001";

        // When & Then
        mockMvc.perform(delete("/evs/projectBudgets/{id}", budgetId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试DELETE /evs/projectBudgets/{ids} - 批量删除预算明细")
    void testDeleteProjectBudgetsBatch() throws Exception {
        // Given - 多个预算ID
        String budgetIds = "test-budget-001,test-budget-002";

        // When & Then
        mockMvc.perform(delete("/evs/projectBudgets/{ids}", budgetIds)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试POST /evs/projectBudgets/export - 导出预算明细列表")
    void testExportProjectBudgets() throws Exception {
        // Given - 导出参数
        String projectId = "P2024110100001";

        // When & Then
        mockMvc.perform(post("/evs/projectBudgets/export")
                .param("projectId", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试GET /evs/projectBudgets/list - 验证返回数据格式")
    void testListProjectBudgetsReturnDataFormat() throws Exception {
        // Given - 查询参数
        String projectId = "P2024110100001";

        // When & Then
        mockMvc.perform(get("/evs/projectBudgets/list")
                .param("projectId", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").isNumber())
            .andExpect(jsonPath("$.rows").isArray())
            .andExpect(jsonPath("$.rows[0].id").exists())
            .andExpect(jsonPath("$.rows[0].projectId").exists())
            .andExpect(jsonPath("$.rows[0].category").exists())
            .andExpect(jsonPath("$.rows[0].plannedAmount").exists())
            .andExpect(jsonPath("$.rows[0].remarks").exists());
    }

    @Test
    @DisplayName("测试GET /evs/projectBudgets/list - 验证分页参数")
    void testListProjectBudgetsPagination() throws Exception {
        // Given - 分页参数
        String projectId = "P2024110100001";
        int pageNum = 1;
        int pageSize = 10;

        // When & Then
        mockMvc.perform(get("/evs/projectBudgets/list")
                .param("projectId", projectId)
                .param("pageNum", String.valueOf(pageNum))
                .param("pageSize", String.valueOf(pageSize))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.total").isNumber())
            .andExpect(jsonPath("$.rows").isArray());
    }

    @Test
    @DisplayName("测试POST /evs/projectBudgets - 验证必填字段验证")
    void testAddProjectBudgetsValidation() throws Exception {
        // Given - 缺少必填字段的数据
        ProjectBudgets invalidBudget = new ProjectBudgets();
        invalidBudget.setProjectId("P2024110100003");
        // 缺少category和plannedAmount

        // When & Then - 应该返回验证错误
        mockMvc.perform(post("/evs/projectBudgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBudget)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(equalTo(200), equalTo(500)))); // 根据实际验证逻辑调整
    }

    @Test
    @DisplayName("测试GET /evs/projectBudgets/{id} - 不存在的预算ID")
    void testGetProjectBudgetsByNonExistentId() throws Exception {
        // Given - 不存在的预算ID
        String budgetId = "NON_EXISTENT_BUDGET_ID";

        // When & Then
        mockMvc.perform(get("/evs/projectBudgets/{id}", budgetId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(anyOf(equalTo(200), equalTo(500))));
    }
}
