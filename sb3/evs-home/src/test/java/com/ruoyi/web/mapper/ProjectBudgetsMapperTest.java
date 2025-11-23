package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ProjectBudgets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 预算明细Mapper测试
 *
 * @author evs
 * @date 2025-11-23
 */
// 注意：这里使用@DataJpaTest来测试JPA，如果使用MyBatis，需要调整为MyBatis的测试方式
// 由于我们使用的是MyBatis，建议使用@SpringBootTest + @MybatisTest
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProjectBudgetsMapper 数据访问层测试")
class ProjectBudgetsMapperTest {

    @Autowired
    private ProjectBudgetsMapper projectBudgetsMapper;

    @Test
    @DisplayName("���试按项目ID查询预算明细")
    void testSelectProjectBudgetsListByProjectId() {
        // 创建测试数据
        ProjectBudgets budget1 = new ProjectBudgets();
        budget1.setId("test001");
        budget1.setProjectId("P2024110100001");
        budget1.setCategory("水电安装");
        budget1.setPlannedAmount(BigDecimal.valueOf(1000));
        budget1.setRemarks("水电改造");
        projectBudgetsMapper.insertProjectBudgets(budget1);

        ProjectBudgets budget2 = new ProjectBudgets();
        budget2.setId("test002");
        budget2.setProjectId("P2024110100002");
        budget2.setCategory("拆除工程");
        budget2.setPlannedAmount(BigDecimal.valueOf(2000));
        budget2.setRemarks("拆除工作");
        projectBudgetsMapper.insertProjectBudgets(budget2);

        // 执行查询
        ProjectBudgets query = new ProjectBudgets();
        query.setProjectId("P2024110100001");

        List<ProjectBudgets> result = projectBudgetsMapper.selectProjectBudgetsList(query);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");
        assertEquals(1, result.size(), "应该返回1条记录");
        assertEquals("P2024110100001", result.get(0).getProjectId(), "项目ID应该匹配");
    }

    @Test
    @DisplayName("测试按预算分类查询")
    void testSelectProjectBudgetsListByCategory() {
        // 创建测试数据
        ProjectBudgets budget1 = new ProjectBudgets();
        budget1.setId("test003");
        budget1.setProjectId("P2024110100003");
        budget1.setCategory("水电安装");
        budget1.setPlannedAmount(BigDecimal.valueOf(3000));
        budget1.setRemarks("水电预算1");
        projectBudgetsMapper.insertProjectBudgets(budget1);

        ProjectBudgets budget2 = new ProjectBudgets();
        budget2.setId("test004");
        budget2.setProjectId("P2024110100004");
        budget2.setCategory("水电安装");
        budget2.setPlannedAmount(BigDecimal.valueOf(4000));
        budget2.setRemarks("水电预算2");
        projectBudgetsMapper.insertProjectBudgets(budget2);

        ProjectBudgets budget3 = new ProjectBudgets();
        budget3.setId("test005");
        budget3.setProjectId("P2024110100005");
        budget3.setCategory("泥瓦工程");
        budget3.setPlannedAmount(BigDecimal.valueOf(5000));
        budget3.setRemarks("泥瓦预算");
        projectBudgetsMapper.insertProjectBudgets(budget3);

        // 执行查询
        ProjectBudgets query = new ProjectBudgets();
        query.setCategory("水电安装");

        List<ProjectBudgets> result = projectBudgetsMapper.selectProjectBudgetsList(query);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");
        assertEquals(2, result.size(), "应该返回2条记录");
        for (ProjectBudgets budget : result) {
            assertEquals("水电安装", budget.getCategory(), "预算分类应该匹配");
        }
    }

    @Test
    @DisplayName("测试按项目ID和预算分类组合查询")
    void testSelectProjectBudgetsListByProjectIdAndCategory() {
        // 创建测试数据
        ProjectBudgets budget1 = new ProjectBudgets();
        budget1.setId("test006");
        budget1.setProjectId("P2024110100006");
        budget1.setCategory("水电安装");
        budget1.setPlannedAmount(BigDecimal.valueOf(6000));
        budget1.setRemarks("水电预算");
        projectBudgetsMapper.insertProjectBudgets(budget1);

        ProjectBudgets budget2 = new ProjectBudgets();
        budget2.setId("test007");
        budget2.setProjectId("P2024110100006");
        budget2.setCategory("泥瓦工程");
        budget2.setPlannedAmount(BigDecimal.valueOf(7000));
        budget2.setRemarks("泥瓦预算");
        projectBudgetsMapper.insertProjectBudgets(budget2);

        ProjectBudgets budget3 = new ProjectBudgets();
        budget3.setId("test008");
        budget3.setProjectId("P2024110100007");
        budget3.setCategory("水电安装");
        budget3.setPlannedAmount(BigDecimal.valueOf(8000));
        budget3.setRemarks("另一个水电预算");
        projectBudgetsMapper.insertProjectBudgets(budget3);

        // 执行查询 - 只查询特定项目的特定分类
        ProjectBudgets query = new ProjectBudgets();
        query.setProjectId("P2024110100006");
        query.setCategory("水电安装");

        List<ProjectBudgets> result = projectBudgetsMapper.selectProjectBudgetsList(query);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");
        assertEquals(1, result.size(), "应该返回1条记录");
        assertEquals("P2024110100006", result.get(0).getProjectId(), "项目ID应该匹配");
        assertEquals("水电安装", result.get(0).getCategory(), "预算分类应该匹配");
    }

    @Test
    @DisplayName("测试查询不存在的项目ID")
    void testSelectProjectBudgetsListByNonExistentProjectId() {
        // 执行查询
        ProjectBudgets query = new ProjectBudgets();
        query.setProjectId("NON_EXISTENT_PROJECT");

        List<ProjectBudgets> result = projectBudgetsMapper.selectProjectBudgetsList(query);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");
        assertTrue(result.isEmpty(), "应该返回空列表");
    }

    @Test
    @DisplayName("测试查询所有预算（无过滤条件）")
    void testSelectProjectBudgetsListWithoutConditions() {
        // 创建测试数据
        ProjectBudgets budget1 = new ProjectBudgets();
        budget1.setId("test009");
        budget1.setProjectId("P2024110100009");
        budget1.setCategory("水电安装");
        budget1.setPlannedAmount(BigDecimal.valueOf(9000));
        budget1.setRemarks("水电预算");
        projectBudgetsMapper.insertProjectBudgets(budget1);

        ProjectBudgets budget2 = new ProjectBudgets();
        budget2.setId("test010");
        budget2.setProjectId("P2024110100010");
        budget2.setCategory("泥瓦工程");
        budget2.setPlannedAmount(BigDecimal.valueOf(10000));
        budget2.setRemarks("泥瓦预算");
        projectBudgetsMapper.insertProjectBudgets(budget2);

        // 执行查询 - 无查询条件
        ProjectBudgets query = new ProjectBudgets();

        List<ProjectBudgets> result = projectBudgetsMapper.selectProjectBudgetsList(query);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");
        assertTrue(result.size() >= 2, "应该返回至少2条记录（包含我们刚创建的）");
    }

    @Test
    @DisplayName("测试根据ID查询预算明细")
    void testSelectProjectBudgetsById() {
        // 创建测试数据
        ProjectBudgets budget = new ProjectBudgets();
        budget.setId("test011");
        budget.setProjectId("P2024110100011");
        budget.setCategory("木工工程");
        budget.setPlannedAmount(BigDecimal.valueOf(11000));
        budget.setRemarks("木工预算");
        projectBudgetsMapper.insertProjectBudgets(budget);

        // 执行查询
        ProjectBudgets result = projectBudgetsMapper.selectProjectBudgetsById("test011");

        // 验证结果
        assertNotNull(result, "查询结果不应为null");
        assertEquals("test011", result.getId(), "ID应该匹配");
        assertEquals("P2024110100011", result.getProjectId(), "项目ID应该匹配");
        assertEquals("木工工程", result.getCategory(), "预算分类应该匹配");
        assertEquals(BigDecimal.valueOf(11000), result.getPlannedAmount(), "计划金额应该匹配");
        assertEquals("木工预算", result.getRemarks(), "备注应该匹配");
    }

    @Test
    @DisplayName("测试根据不存在的ID查询预算明细")
    void testSelectProjectBudgetsByNonExistentId() {
        // 执行查询
        ProjectBudgets result = projectBudgetsMapper.selectProjectBudgetsById("NON_EXISTENT_ID");

        // 验证结果
        assertNull(result, "查询不存在的ID应该返回null");
    }

    @Test
    @DisplayName("测试插入预算明细")
    void testInsertProjectBudgets() {
        // 创建测试数据
        ProjectBudgets budget = new ProjectBudgets();
        budget.setId("test012");
        budget.setProjectId("P2024110100012");
        budget.setCategory("油漆工程");
        budget.setPlannedAmount(BigDecimal.valueOf(12000));
        budget.setRemarks("油漆预算");

        // 执行插入
        int result = projectBudgetsMapper.insertProjectBudgets(budget);

        // 验证结果
        assertEquals(1, result, "插入成功应该返回1");

        // 验证数据是否真正插入
        ProjectBudgets insertedBudget = projectBudgetsMapper.selectProjectBudgetsById("test012");
        assertNotNull(insertedBudget, "插入的数据应该能被查询到");
        assertEquals("P2024110100012", insertedBudget.getProjectId(), "项目ID应该匹配");
    }

    @Test
    @DisplayName("测试更新预算明细")
    void testUpdateProjectBudgets() {
        // 先插入一条数据
        ProjectBudgets budget = new ProjectBudgets();
        budget.setId("test013");
        budget.setProjectId("P2024110100013");
        budget.setCategory("水电安装");
        budget.setPlannedAmount(BigDecimal.valueOf(13000));
        budget.setRemarks("原始备注");
        projectBudgetsMapper.insertProjectBudgets(budget);

        // 更新数据
        ProjectBudgets updateBudget = new ProjectBudgets();
        updateBudget.setId("test013");
        updateBudget.setProjectId("P2024110100013");
        updateBudget.setCategory("水电安装");
        updateBudget.setPlannedAmount(BigDecimal.valueOf(15000)); // 修改金额
        updateBudget.setRemarks("更新后的备注");

        // 执行更新
        int result = projectBudgetsMapper.updateProjectBudgets(updateBudget);

        // 验证结果
        assertEquals(1, result, "更新成功应该返回1");

        // 验证数据是否真正更新
        ProjectBudgets updatedBudget = projectBudgetsMapper.selectProjectBudgetsById("test013");
        assertNotNull(updatedBudget, "更新后的数据应该能被查询到");
        assertEquals(BigDecimal.valueOf(15000), updatedBudget.getPlannedAmount(), "计划金额应该已更新");
        assertEquals("更新后的备注", updatedBudget.getRemarks(), "备注应该已更新");
    }

    @Test
    @DisplayName("测试删除预算明细")
    void testDeleteProjectBudgetsById() {
        // 先插入一条数据
        ProjectBudgets budget = new ProjectBudgets();
        budget.setId("test014");
        budget.setProjectId("P2024110100014");
        budget.setCategory("材料费");
        budget.setPlannedAmount(BigDecimal.valueOf(14000));
        budget.setRemarks("材料费用");
        projectBudgetsMapper.insertProjectBudgets(budget);

        // 验证数据存在
        ProjectBudgets beforeDelete = projectBudgetsMapper.selectProjectBudgetsById("test014");
        assertNotNull(beforeDelete, "插入的数据应该存在");

        // 执行删除
        int result = projectBudgetsMapper.deleteProjectBudgetsById("test014");

        // 验证结果
        assertEquals(1, result, "删除成功应该返回1");

        // 验证数据已被删除
        ProjectBudgets afterDelete = projectBudgetsMapper.selectProjectBudgetsById("test014");
        assertNull(afterDelete, "删除后的数据应该不存在");
    }

    @Test
    @DisplayName("测试批量删除预算明细")
    void testDeleteProjectBudgetsByIds() {
        // 先插入多条数据
        ProjectBudgets budget1 = new ProjectBudgets();
        budget1.setId("test015");
        budget1.setProjectId("P2024110100015");
        budget1.setCategory("人工费");
        budget1.setPlannedAmount(BigDecimal.valueOf(15000));
        budget1.setRemarks("人工费用1");
        projectBudgetsMapper.insertProjectBudgets(budget1);

        ProjectBudgets budget2 = new ProjectBudgets();
        budget2.setId("test016");
        budget2.setProjectId("P2024110100016");
        budget2.setCategory("人工费");
        budget2.setPlannedAmount(BigDecimal.valueOf(16000));
        budget2.setRemarks("人工费用2");
        projectBudgetsMapper.insertProjectBudgets(budget2);

        ProjectBudgets budget3 = new ProjectBudgets();
        budget3.setId("test017");
        budget3.setProjectId("P2024110100017");
        budget3.setCategory("管理费");
        budget3.setPlannedAmount(BigDecimal.valueOf(17000));
        budget3.setRemarks("管理费用");
        projectBudgetsMapper.insertProjectBudgets(budget3);

        // 执行批量删除
        String[] ids = {"test015", "test016"};
        int result = projectBudgetsMapper.deleteProjectBudgetsByIds(ids);

        // 验证结果
        assertEquals(2, result, "批量删除成功应该返回2");

        // 验证指定数据已被删除
        ProjectBudgets deleted1 = projectBudgetsMapper.selectProjectBudgetsById("test015");
        assertNull(deleted1, "test015应该已被删除");

        ProjectBudgets deleted2 = projectBudgetsMapper.selectProjectBudgetsById("test016");
        assertNull(deleted2, "test016应该已被删除");

        // 验证未被删除的数据仍然存在
        ProjectBudgets notDeleted = projectBudgetsMapper.selectProjectBudgetsById("test017");
        assertNotNull(notDeleted, "test017应该仍然存在");
        assertEquals("管理费", notDeleted.getCategory(), "未被删除的数据应该保持完整");
    }
}
