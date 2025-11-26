package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.mapper.ProjectsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
 * 项目成员关联查询测试类
 * 专门测试通过 projectMembers 表关联查询和权限过滤功能
 *
 * @author evs
 * @date 2025-11-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("项目成员关联查询测试")
class ProjectsServiceImplMembersTest {

    @Mock
    private ProjectsMapper projectsMapper;

    @InjectMocks
    private ProjectsServiceImpl projectsService;

    @Captor
    private ArgumentCaptor<Projects> projectsCaptor;

    private Projects testProject1;
    private Projects testProject2;
    private Projects testProject3;
    private List<Projects> allProjects;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testProject1 = createTestProject("1", "测试项目1", "IN_PROGRESS");
        testProject2 = createTestProject("2", "测试项目2", "PLANNED");
        testProject3 = createTestProject("3", "测试项目3", "COMPLETED");

        allProjects = Arrays.asList(testProject1, testProject2, testProject3);
    }

    @Test
    @DisplayName("通过项目成员关联查询 - 管理员查看所有项目")
    void testSelectProjectsWithMembersAsAdmin() {
        // Given
        Projects queryProject = new Projects();
        String memberUserId = "user123";
        boolean isAdmin = true;

        when(projectsMapper.selectProjectsWithMembers(queryProject, memberUserId, isAdmin))
            .thenReturn(allProjects);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", memberUserId, isAdmin);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size(), "管理员应该能看到所有项目");
        verify(projectsMapper, times(1))
            .selectProjectsWithMembers(queryProject, memberUserId, isAdmin);
        verify(projectsMapper, never()).selectProjectsList(any());
    }

    @Test
    @DisplayName("通过项目成员关联查询 - 非管理员只能看自己参与的项目")
    void testSelectProjectsWithMembersAsNormalUser() {
        // Given
        Projects queryProject = new Projects();
        String memberUserId = "user123";
        boolean isAdmin = false;

        List<Projects> userProjects = Arrays.asList(
            createTestProject("1", "用户参与的项目1", "IN_PROGRESS"),
            createTestProject("3", "用户参与的项目3", "COMPLETED")
        );

        when(projectsMapper.selectProjectsWithMembers(queryProject, memberUserId, isAdmin))
            .thenReturn(userProjects);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", memberUserId, isAdmin);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size(), "非管理员应该只能看到自己参与的项目");
        assertEquals("1", result.get(0).getId());
        assertEquals("3", result.get(1).getId());
        verify(projectsMapper, times(1))
            .selectProjectsWithMembers(queryProject, memberUserId, isAdmin);
    }

    @Test
    @DisplayName("通过项目成员关联查询 - 支持筛选条件（项目名称）")
    void testSelectProjectsWithMembersWithNameFilter() {
        // Given
        Projects queryProject = new Projects();
        queryProject.setName("筛选测试");
        queryProject.setStatus("IN_PROGRESS");

        String memberUserId = "user123";
        boolean isAdmin = true;

        List<Projects> filteredProjects = Arrays.asList(
            createTestProject("1", "筛选测试项目", "IN_PROGRESS")
        );

        when(projectsMapper.selectProjectsWithMembers(queryProject, memberUserId, isAdmin))
            .thenReturn(filteredProjects);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", memberUserId, isAdmin);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("筛选测试项目", result.get(0).getName());

        // 验证查询参数正确传递
        verify(projectsMapper).selectProjectsWithMembers(
            argThat(project -> "筛选测试".equals(project.getName())),
            eq(memberUserId),
            eq(isAdmin)
        );
    }

    @Test
    @DisplayName("通过项目成员关联查询 - 管理员支持关联客户筛选")
    void testSelectProjectsWithMembersAsAdminWithCustomerFilter() {
        // Given
        Projects queryProject = new Projects();
        queryProject.setCustomerId("customer456");

        String memberUserId = "user123";
        boolean isAdmin = true;

        when(projectsMapper.selectProjectsWithMembers(queryProject, memberUserId, isAdmin))
            .thenReturn(allProjects);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", memberUserId, isAdmin);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        // 验证查询参数中包含客户ID
        verify(projectsMapper).selectProjectsWithMembers(
            argThat(project -> "customer456".equals(project.getCustomerId())),
            eq(memberUserId),
            eq(isAdmin)
        );
    }

    @Test
    @DisplayName("通过项目成员关联查询 - 管理员支持团队成员筛选")
    void testSelectProjectsWithMembersAsAdminWithMemberUserIdFilter() {
        // Given
        Projects queryProject = new Projects();
        // memberUserId不是Projects类的字段，作为查询参数传递
        String memberUserId = "user123";
        String filterMemberUserId = "member789";
        boolean isAdmin = true;

        when(projectsMapper.selectProjectsWithMembers(queryProject, filterMemberUserId, isAdmin))
            .thenReturn(allProjects);

        // When - 通过查询参数传递成员筛选条件
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", filterMemberUserId, isAdmin);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        // 验证成员用户ID参数正确传递（作为查询参数，不是Projects字段）
        verify(projectsMapper).selectProjectsWithMembers(
            eq(queryProject),
            eq("member789"),  // 查询条件中的成员用户ID
            eq(isAdmin)
        );
    }

    @Test
    @DisplayName("通过项目成员关联查询 - 空项目列表")
    void testSelectProjectsWithMembersEmptyList() {
        // Given
        Projects queryProject = new Projects();
        String memberUserId = "nonexistent_user";
        boolean isAdmin = false;

        when(projectsMapper.selectProjectsWithMembers(queryProject, memberUserId, isAdmin))
            .thenReturn(Collections.emptyList());

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", memberUserId, isAdmin);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty(), "空结果应该返回空列表");
        verify(projectsMapper, times(1))
            .selectProjectsWithMembers(queryProject, memberUserId, isAdmin);
    }

    @Test
    @DisplayName("关联查询组合 - projectMembers + customer")
    void testSelectProjectsWithMembersAndCustomer() {
        // Given
        Projects queryProject = new Projects();
        String memberUserId = "user123";
        boolean isAdmin = true;

        // Service层会优先匹配"customer"，所以应该Mock selectProjectsWithCustomer 而不是 selectProjectsWithMembers
        when(projectsMapper.selectProjectsWithCustomer(queryProject))
            .thenReturn(allProjects);

        // When - 包含多个关联关系，Service会优先选择"customer"
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers,customer", memberUserId, isAdmin);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        // 应该调用 selectProjectsWithCustomer 方法（优先匹配"customer"）
        verify(projectsMapper, times(1))
            .selectProjectsWithCustomer(queryProject);
    }

    @Test
    @DisplayName("权限验证 - 管理员角色检查")
    void testAdminRolePermissionCheck() {
        // Given
        Projects queryProject = new Projects();
        String adminUserId = "admin";
        boolean isAdmin = true;

        when(projectsMapper.selectProjectsWithMembers(queryProject, adminUserId, isAdmin))
            .thenReturn(allProjects);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", adminUserId, isAdmin);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size(), "管理员应该能看到所有项目");

        // 验证 isAdmin 参数为 true
        verify(projectsMapper).selectProjectsWithMembers(
            any(Projects.class),
            eq(adminUserId),
            eq(true)
        );
    }

    @Test
    @DisplayName("权限验证 - 普通用户角色检查")
    void testNormalUserRolePermissionCheck() {
        // Given
        Projects queryProject = new Projects();
        String normalUserId = "normal_user";
        boolean isAdmin = false;

        List<Projects> userSpecificProjects = Arrays.asList(
            createTestProject("1", "用户项目1", "IN_PROGRESS")
        );

        when(projectsMapper.selectProjectsWithMembers(queryProject, normalUserId, isAdmin))
            .thenReturn(userSpecificProjects);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", normalUserId, isAdmin);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size(), "普通用户应该只能看到自己的项目");

        // 验证 isAdmin 参数为 false
        verify(projectsMapper).selectProjectsWithMembers(
            any(Projects.class),
            eq(normalUserId),
            eq(false)
        );
    }

    @Test
    @DisplayName("边界条件 - 空的memberUserId参数")
    void testSelectProjectsWithMembersWithEmptyUserId() {
        // Given
        Projects queryProject = new Projects();
        String memberUserId = "";
        boolean isAdmin = false;

        when(projectsMapper.selectProjectsWithMembers(queryProject, memberUserId, isAdmin))
            .thenReturn(Collections.emptyList());

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", memberUserId, isAdmin);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证空字符串参数被正确传递
        verify(projectsMapper).selectProjectsWithMembers(
            any(Projects.class),
            eq(""),
            eq(false)
        );
    }

    @Test
    @DisplayName("SQL构建验证 - 管理员查询SQL应不包含用户过滤")
    void testAdminQuerySqlShouldNotIncludeUserFilter() {
        // Given
        Projects queryProject = new Projects();
        queryProject.setStatus("IN_PROGRESS");
        String memberUserId = "user123";
        boolean isAdmin = true;

        when(projectsMapper.selectProjectsWithMembers(queryProject, memberUserId, isAdmin))
            .thenReturn(Arrays.asList(testProject1));

        // When
        projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", memberUserId, isAdmin);

        // Then - 验证参数传递正确，SQL层面的过滤由Mapper处理
        verify(projectsMapper).selectProjectsWithMembers(
            argThat(project -> "IN_PROGRESS".equals(project.getStatus())),
            eq(memberUserId),
            eq(true)  // 管理员标志
        );
    }

    /**
     * 创建测试用的项目对象
     */
    private Projects createTestProject(String id, String name, String status) {
        Projects project = new Projects();
        project.setId(id);
        project.setName(name);
        project.setProjectType("RESIDENTIAL");
        project.setCustomerId("customer-" + id);
        project.setAddress("测试地址" + id);
        project.setArea(new BigDecimal("120.5"));
        project.setBudget(new BigDecimal("150000"));
        project.setActualCost(new BigDecimal("80000"));
        project.setStartDate(new Date());
        project.setEndDate(new Date());
        project.setStatus(status);
        project.setPriority("MEDIUM");
        project.setProgress(new BigDecimal("25"));
        project.setDescription("测试项目描述" + id);
        return project;
    }
}
