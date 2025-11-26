package com.ruoyi.web.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 项目信息对象测试类
 *
 * @author evs
 * @date 2025-11-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("项目信息实体测试")
class ProjectsTest {

    private Projects project;

    @BeforeEach
    void setUp() {
        project = new Projects();
        // 设置基础测试数据
        project.setId("test-project-001");
        project.setName("测试项目");
        project.setProjectType("RESIDENTIAL");
        project.setCustomerId("customer-123");
        project.setDescription("这是一个测试项目描述");
        project.setAddress("测试地址123号");
        project.setArea(new BigDecimal("120.5"));
        project.setBudget(new BigDecimal("150000"));
        project.setActualCost(new BigDecimal("80000"));
        project.setStartDate(new Date());
        project.setEndDate(new Date());
        project.setStatus("IN_PROGRESS");
        project.setPriority("MEDIUM");
        project.setProgress(new BigDecimal("35.5"));
        project.setBudgetsUrl("http://example.com/budget.pdf");
        project.setContractsUrl("http://example.com/contract.pdf");
        project.setCreatedAt(new Date());
        project.setUpdatedAt(new Date());
        project.setCreatedBy("admin");
        project.setUpdatedBy("admin");
    }

    // ========== 基础字段测试 ==========

    @Test
    @DisplayName("基础字段 Getter/Setter 测试")
    void testBasicFieldsGetterSetter() {
        // 测试ID
        assertEquals("test-project-001", project.getId());
        project.setId("new-id");
        assertEquals("new-id", project.getId());

        // 测试项目名称
        assertEquals("测试项目", project.getName());
        project.setName("新项目名称");
        assertEquals("新项目名称", project.getName());

        // 测试项目类型
        assertEquals("RESIDENTIAL", project.getProjectType());
        project.setProjectType("COMMERCIAL");
        assertEquals("COMMERCIAL", project.getProjectType());

        // 测试客户ID
        assertEquals("customer-123", project.getCustomerId());
        project.setCustomerId("new-customer-456");
        assertEquals("new-customer-456", project.getCustomerId());

        // 测试项目描述
        assertEquals("这是一个测试项目描述", project.getDescription());
        project.setDescription("新的描述");
        assertEquals("新的描述", project.getDescription());

        // 测试项目地址
        assertEquals("测试地址123号", project.getAddress());
        project.setAddress("新地址456号");
        assertEquals("新地址456号", project.getAddress());

        // 测试项目状态
        assertEquals("IN_PROGRESS", project.getStatus());
        project.setStatus("COMPLETED");
        assertEquals("COMPLETED", project.getStatus());

        // 测试优先级
        assertEquals("MEDIUM", project.getPriority());
        project.setPriority("HIGH");
        assertEquals("HIGH", project.getPriority());
    }

    @Test
    @DisplayName("数值字段 Getter/Setter 测试")
    void testNumericFieldsGetterSetter() {
        // 测试面积
        assertEquals(new BigDecimal("120.5"), project.getArea());
        BigDecimal newArea = new BigDecimal("200.8");
        project.setArea(newArea);
        assertEquals(newArea, project.getArea());

        // 测试预算
        assertEquals(new BigDecimal("150000"), project.getBudget());
        BigDecimal newBudget = new BigDecimal("200000");
        project.setBudget(newBudget);
        assertEquals(newBudget, project.getBudget());

        // 测试实际费用
        assertEquals(new BigDecimal("80000"), project.getActualCost());
        BigDecimal newActualCost = new BigDecimal("100000");
        project.setActualCost(newActualCost);
        assertEquals(newActualCost, project.getActualCost());

        // 测试进度
        assertEquals(new BigDecimal("35.5"), project.getProgress());
        BigDecimal newProgress = new BigDecimal("60.0");
        project.setProgress(newProgress);
        assertEquals(newProgress, project.getProgress());
    }

    @Test
    @DisplayName("日期字段 Getter/Setter 测试")
    void testDateFieldsGetterSetter() {
        Date now = new Date();

        // 测试开始日期
        assertNotNull(project.getStartDate());
        project.setStartDate(now);
        assertEquals(now, project.getStartDate());

        // 测试预计完工日期
        assertNotNull(project.getEndDate());
        project.setEndDate(now);
        assertEquals(now, project.getEndDate());

        // 测试实际完工日期
        project.setActualEndDate(now);
        assertEquals(now, project.getActualEndDate());

        // 测试创建时间
        assertNotNull(project.getCreatedAt());
        project.setCreatedAt(now);
        assertEquals(now, project.getCreatedAt());

        // 测试更新时间
        assertNotNull(project.getUpdatedAt());
        project.setUpdatedAt(now);
        assertEquals(now, project.getUpdatedAt());

        // 测试删除时间
        project.setDeletedAt(now);
        assertEquals(now, project.getDeletedAt());
    }

    @Test
    @DisplayName("用户字段 Getter/Setter 测试")
    void testUserFieldsGetterSetter() {
        // 测试创建人
        assertEquals("admin", project.getCreatedBy());
        project.setCreatedBy("user1");
        assertEquals("user1", project.getCreatedBy());

        // 测试更新人
        assertEquals("admin", project.getUpdatedBy());
        project.setUpdatedBy("user2");
        assertEquals("user2", project.getUpdatedBy());
    }

    @Test
    @DisplayName("URL字段 Getter/Setter 测试")
    void testUrlFieldsGetterSetter() {
        // 测试预算文件URL
        assertEquals("http://example.com/budget.pdf", project.getBudgetsUrl());
        String newBudgetsUrl = "http://example.com/new-budget.pdf";
        project.setBudgetsUrl(newBudgetsUrl);
        assertEquals(newBudgetsUrl, project.getBudgetsUrl());

        // 测试合同文件URL
        assertEquals("http://example.com/contract.pdf", project.getContractsUrl());
        String newContractsUrl = "http://example.com/new-contract.pdf";
        project.setContractsUrl(newContractsUrl);
        assertEquals(newContractsUrl, project.getContractsUrl());
    }

    // ========== 关联对象测试 ==========

    @Test
    @DisplayName("客户信息关联对象测试")
    void testCustomerField() {
        // 初始值应该为null
        assertNull(project.getCustomer());

        // 测试设置客户对象
        Customers customer = new Customers();
        customer.setId("customer-001");
        customer.setName("测试客户");

        project.setCustomer(customer);
        assertEquals(customer, project.getCustomer());
        assertEquals("customer-001", project.getCustomer().getId());
        assertEquals("测试客户", project.getCustomer().getName());

        // 测试设置为null
        project.setCustomer(null);
        assertNull(project.getCustomer());
    }

    @Test
    @DisplayName("预算明细列表字段测试")
    void testBudgetItemsField() {
        // 初始值应该为null
        assertNull(project.getBudgetItems());

        // 测试设置预算明细
        List<Object> budgetItems = Arrays.asList("预算项1", "预算项2", "预算项3");
        project.setBudgetItems(budgetItems);
        assertEquals(budgetItems, project.getBudgetItems());
        assertEquals(3, project.getBudgetItems().size());

        // 测试设置为空列表
        project.setBudgetItems(Collections.emptyList());
        assertEquals(Collections.emptyList(), project.getBudgetItems());

        // 测试设置为null
        project.setBudgetItems(null);
        assertNull(project.getBudgetItems());
    }

    @Test
    @DisplayName("进度计划列表字段测试")
    void testSchedulesField() {
        // 初始值应该为null
        assertNull(project.getSchedules());

        // 测试设置进度计划
        List<Object> schedules = Arrays.asList("进度计划1", "进度计划2");
        project.setSchedules(schedules);
        assertEquals(schedules, project.getSchedules());
        assertEquals(2, project.getSchedules().size());

        // 测试设置为空列表
        project.setSchedules(Collections.emptyList());
        assertEquals(Collections.emptyList(), project.getSchedules());

        // 测试设置为null
        project.setSchedules(null);
        assertNull(project.getSchedules());
    }

    // ========== toString 方法测试 ==========

    @Test
    @DisplayName("toString方法测试")
    void testToString() {
        String result = project.toString();

        assertNotNull(result);
        assertTrue(result.contains("test-project-001"));
        assertTrue(result.contains("测试项目"));
        assertTrue(result.contains("RESIDENTIAL"));
        assertTrue(result.contains("customer-123"));
        assertTrue(result.contains("IN_PROGRESS"));
        assertTrue(result.contains("MEDIUM"));
    }

    @Test
    @DisplayName("toString方法测试 - 空对象")
    void testToStringWithEmptyProject() {
        Projects emptyProject = new Projects();
        String result = emptyProject.toString();

        assertNotNull(result);
        // toString方法应该能够处理空值而不抛出异常
        assertDoesNotThrow(() -> emptyProject.toString());
    }

    // ========== 边界条件和异常情况测试 ==========

    @Test
    @DisplayName("设置空的项目名称")
    void testSetEmptyName() {
        project.setName("");
        assertEquals("", project.getName());

        project.setName(null);
        assertNull(project.getName());
    }

    @Test
    @DisplayName("设置空的客户ID")
    void testSetEmptyCustomerId() {
        project.setCustomerId("");
        assertEquals("", project.getCustomerId());

        project.setCustomerId(null);
        assertNull(project.getCustomerId());
    }

    @Test
    @DisplayName("设置空的数值字段")
    void testSetNullNumericFields() {
        project.setArea(null);
        assertNull(project.getArea());

        project.setBudget(null);
        assertNull(project.getBudget());

        project.setActualCost(null);
        assertNull(project.getActualCost());

        project.setProgress(null);
        assertNull(project.getProgress());
    }

    @Test
    @DisplayName("设置空的URL字段")
    void testSetEmptyUrlFields() {
        project.setBudgetsUrl("");
        assertEquals("", project.getBudgetsUrl());

        project.setBudgetsUrl(null);
        assertNull(project.getBudgetsUrl());

        project.setContractsUrl("");
        assertEquals("", project.getContractsUrl());

        project.setContractsUrl(null);
        assertNull(project.getContractsUrl());
    }

    @Test
    @DisplayName("关联对象空值处理")
    void testNullRelatedObjects() {
        // 关联对象为null时应该能正常处理
        project.setCustomer(null);
        assertNull(project.getCustomer());

        project.setBudgetItems(null);
        assertNull(project.getBudgetItems());

        project.setSchedules(null);
        assertNull(project.getSchedules());
    }
}