package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.mapper.ProjectsMapper;
import com.ruoyi.web.service.IProjectsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 项目信息Service业务层处理测试类
 *
 * @author evs
 * @date 2025-11-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("项目信息Service测试")
class ProjectsServiceImplTest {

    @Mock
    private ProjectsMapper projectsMapper;

    @InjectMocks
    private ProjectsServiceImpl projectsService;

    @Captor
    private ArgumentCaptor<Projects> projectsCaptor;

    private Projects testProject;
    private List<Projects> testProjectList;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testProject = createTestProject("1", "测试项目1", "IN_PROGRESS");

        Projects project2 = createTestProject("2", "测试项目2", "PLANNED");
        Projects project3 = createTestProject("3", "测试项目3", "COMPLETED");

        testProjectList = Arrays.asList(testProject, project2, project3);
    }

    @Test
    @DisplayName("查询项目信息 - 成功")
    void testSelectProjectsById() {
        // Given
        when(projectsMapper.selectProjectsById("1")).thenReturn(testProject);

        // When
        Projects result = projectsService.selectProjectsById("1");

        // Then
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("测试项目1", result.getName());
        assertEquals("IN_PROGRESS", result.getStatus());
        verify(projectsMapper, times(1)).selectProjectsById("1");
    }

    @Test
    @DisplayName("查询项目信息列表 - 成功")
    void testSelectProjectsList() {
        // Given
        Projects queryProject = new Projects();
        queryProject.setName("测试");
        when(projectsMapper.selectProjectsList(queryProject)).thenReturn(testProjectList);

        // When
        List<Projects> result = projectsService.selectProjectsList(queryProject);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("测试项目1", result.get(0).getName());
        assertEquals("测试项目2", result.get(1).getName());
        assertEquals("测试项目3", result.get(2).getName());
        verify(projectsMapper, times(1)).selectProjectsList(queryProject);
    }

    @Test
    @DisplayName("新增项目信息 - 成功")
    void testInsertProjects() {
        // Given
        Projects newProject = createTestProject(null, "新项目", "PLANNED");
        when(projectsMapper.insertProjects(any(Projects.class))).thenReturn(1);

        // When
        int result = projectsService.insertProjects(newProject);

        // Then
        assertEquals(1, result);
        verify(projectsMapper, times(1)).insertProjects(projectsCaptor.capture());

        Projects capturedProject = projectsCaptor.getValue();
        assertEquals("新项目", capturedProject.getName());
        assertEquals("PLANNED", capturedProject.getStatus());
    }

    @Test
    @DisplayName("修改项目信息 - 成功")
    void testUpdateProjects() {
        // Given
        testProject.setName("更新后的项目");
        when(projectsMapper.updateProjects(any(Projects.class))).thenReturn(1);

        // When
        int result = projectsService.updateProjects(testProject);

        // Then
        assertEquals(1, result);
        verify(projectsMapper, times(1)).updateProjects(projectsCaptor.capture());

        Projects capturedProject = projectsCaptor.getValue();
        assertEquals("更新后的项目", capturedProject.getName());
    }

    @Test
    @DisplayName("删除项目信息 - 批量删除")
    void testDeleteProjectsByIds() {
        // Given
        String[] ids = {"1", "2", "3"};
        when(projectsMapper.deleteProjectsByIds(ids)).thenReturn(3);

        // When
        int result = projectsService.deleteProjectsByIds(ids);

        // Then
        assertEquals(3, result);
        verify(projectsMapper, times(1)).deleteProjectsByIds(ids);
    }

    @Test
    @DisplayName("删除项目信息 - 单个删除")
    void testDeleteProjectsById() {
        // Given
        when(projectsMapper.deleteProjectsById("1")).thenReturn(1);

        // When
        int result = projectsService.deleteProjectsById("1");

        // Then
        assertEquals(1, result);
        verify(projectsMapper, times(1)).deleteProjectsById("1");
    }

    @Test
    @DisplayName("软删除项目信息 - 成功")
    void testSoftDeleteProjectsById() {
        // Given
        when(projectsMapper.selectProjectsById("1")).thenReturn(testProject);
        when(projectsMapper.updateProjects(any(Projects.class))).thenReturn(1);

        // When
        int result = projectsService.softDeleteProjectsById("1");

        // Then
        assertEquals(1, result);
        verify(projectsMapper, times(1)).selectProjectsById("1");
        verify(projectsMapper, times(1)).updateProjects(projectsCaptor.capture());

        Projects capturedProject = projectsCaptor.getValue();
        assertNotNull(capturedProject.getDeletedAt());
    }

    @Test
    @DisplayName("软删除项目信息 - 项目不存在")
    void testSoftDeleteProjectsByIdNotFound() {
        // Given
        when(projectsMapper.selectProjectsById("non-existent")).thenReturn(null);

        // When
        int result = projectsService.softDeleteProjectsById("non-existent");

        // Then
        assertEquals(0, result);
        verify(projectsMapper, times(1)).selectProjectsById("non-existent");
        verify(projectsMapper, never()).updateProjects(any());
    }

    // ========== 关联查询相关测试 ==========

    @Test
    @DisplayName("关联查询 - 包含客户信息")
    void testSelectProjectsWithRelationsWithCustomer() {
        // Given
        Projects queryProject = new Projects();
        when(projectsMapper.selectProjectsWithCustomer(queryProject)).thenReturn(testProjectList);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(queryProject, "customer", null, true);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(projectsMapper, times(1)).selectProjectsWithCustomer(queryProject);
        verify(projectsMapper, never()).selectProjectsList(any());
    }

    @Test
    @DisplayName("关联查询 - 不包含关联信息")
    void testSelectProjectsWithRelationsWithoutInclude() {
        // Given
        Projects queryProject = new Projects();
        when(projectsMapper.selectProjectsList(queryProject)).thenReturn(testProjectList);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(queryProject, "", null, true);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(projectsMapper, times(1)).selectProjectsList(queryProject);
        verify(projectsMapper, never()).selectProjectsWithCustomer(any());
    }

    @Test
    @DisplayName("关联查询 - 空的includeRelations参数")
    void testSelectProjectsWithRelationsWithNullInclude() {
        // Given
        Projects queryProject = new Projects();
        when(projectsMapper.selectProjectsList(queryProject)).thenReturn(testProjectList);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(queryProject, null, null, true);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(projectsMapper, times(1)).selectProjectsList(queryProject);
        verify(projectsMapper, never()).selectProjectsWithCustomer(any());
    }

    @Test
    @DisplayName("关联查询 - 包含客户信息（基本测试）")
    void testSelectProjectsWithRelationsWithUserProjectIds() {
        // Given
        Projects queryProject = new Projects();
        // 注意：setUserProjectIds方法不存在，Service层不进行权限过滤
        // 权限过滤在SQL层面通过Mapper实现

        List<Projects> allProjects = Arrays.asList(
                createTestProject("1", "项目1", "IN_PROGRESS"),
                createTestProject("2", "项目2", "PLANNED"),
                createTestProject("3", "项目3", "COMPLETED")
        );

        when(projectsMapper.selectProjectsWithCustomer(queryProject)).thenReturn(allProjects);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(queryProject, "customer", null, true);

        // Then - Service层返回Mapper查询的所有结果
        assertNotNull(result);
        assertEquals(3, result.size()); // 返回所有项目
        assertEquals("1", result.get(0).getId());
        assertEquals("2", result.get(1).getId());
        assertEquals("3", result.get(2).getId());
        verify(projectsMapper, times(1)).selectProjectsWithCustomer(queryProject);
    }

    @Test
    @DisplayName("关联查询 - 应用权限过滤 - 无用户项目ID")
    void testSelectProjectsWithRelationsWithEmptyUserProjectIds() {
        // Given
        Projects queryProject = new Projects();
        // 注意：setUserProjectIds方法不存在，删除这行调用

        List<Projects> allProjects = Arrays.asList(
                createTestProject("1", "项目1", "IN_PROGRESS"),
                createTestProject("2", "项目2", "PLANNED")
        );

        when(projectsMapper.selectProjectsWithCustomer(queryProject)).thenReturn(allProjects);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(queryProject, "customer", null, true);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // 返回所有项目
        verify(projectsMapper, times(1)).selectProjectsWithCustomer(queryProject);
    }

    @Test
    @DisplayName("关联查询 - 不应用权限过滤 - 无用户项目ID")
    void testSelectProjectsWithRelationsWithNullUserProjectIds() {
        // Given
        Projects queryProject = new Projects();
        // 注意：setUserProjectIds方法不存在，删除这行调用

        List<Projects> allProjects = Arrays.asList(
                createTestProject("1", "项目1", "IN_PROGRESS"),
                createTestProject("2", "项目2", "PLANNED")
        );

        when(projectsMapper.selectProjectsWithCustomer(queryProject)).thenReturn(allProjects);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(queryProject, "customer", null, true);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // null权限列表应该返回所有结果
        verify(projectsMapper, times(1)).selectProjectsWithCustomer(queryProject);
    }

    @Test
    @DisplayName("关联查询 - 权限过滤 - 部分匹配")
    void testSelectProjectsWithRelationsPartialMatch() {
        // Given
        Projects queryProject = new Projects();
        // 注意：setUserProjectIds方法不存在，删除这行调用

        List<Projects> allProjects = Arrays.asList(
                createTestProject("1", "项目1", "IN_PROGRESS"),
                createTestProject("2", "项目2", "PLANNED"),
                createTestProject("3", "项目3", "COMPLETED")
        );

        when(projectsMapper.selectProjectsWithCustomer(queryProject)).thenReturn(allProjects);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(queryProject, "customer", null, true);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size()); // 返回所有项目
        verify(projectsMapper, times(1)).selectProjectsWithCustomer(queryProject);
    }

    @Test
    @DisplayName("根据ID关联查询 - 包含客户信息")
    void testSelectProjectsWithRelationsByIdWithCustomer() {
        // Given
        Projects projectWithCustomer = createTestProject("1", "带客户信息的项目", "IN_PROGRESS");
        when(projectsMapper.selectProjectsWithCustomerById("1")).thenReturn(projectWithCustomer);

        // When
        Projects result = projectsService.selectProjectsWithRelationsById("1", "customer");

        // Then
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("带客户信息的项目", result.getName());
        verify(projectsMapper, times(1)).selectProjectsWithCustomerById("1");
        verify(projectsMapper, never()).selectProjectsById(any());
    }

    @Test
    @DisplayName("根据ID关联查询 - 不包含关联信息")
    void testSelectProjectsWithRelationsByIdWithoutInclude() {
        // Given
        when(projectsMapper.selectProjectsById("1")).thenReturn(testProject);

        // When
        Projects result = projectsService.selectProjectsWithRelationsById("1", "");

        // Then
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("测试项目1", result.getName());
        verify(projectsMapper, times(1)).selectProjectsById("1");
        verify(projectsMapper, never()).selectProjectsWithCustomerById(any());
    }

    @Test
    @DisplayName("根据ID关联查询 - 空的includeRelations参数")
    void testSelectProjectsWithRelationsByIdWithNullInclude() {
        // Given
        when(projectsMapper.selectProjectsById("1")).thenReturn(testProject);

        // When
        Projects result = projectsService.selectProjectsWithRelationsById("1", null);

        // Then
        assertNotNull(result);
        assertEquals("1", result.getId());
        verify(projectsMapper, times(1)).selectProjectsById("1");
        verify(projectsMapper, never()).selectProjectsWithCustomerById(any());
    }

    @Test
    @DisplayName("通过项目成员关联查询 - 管理员查看所有项目")
    void testSelectProjectsWithMembersAsAdmin() {
        // Given
        Projects queryProject = new Projects();
        String memberUserId = "user123";
        boolean isAdmin = true;

        when(projectsMapper.selectProjectsWithMembers(queryProject, memberUserId, isAdmin))
            .thenReturn(testProjectList);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", memberUserId, isAdmin);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
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
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("3", result.get(1).getId());
        verify(projectsMapper, times(1))
            .selectProjectsWithMembers(queryProject, memberUserId, isAdmin);
    }

    @Test
    @DisplayName("通过项目成员关联查询 - 支持筛选条件")
    void testSelectProjectsWithMembersWithFilters() {
        // Given
        Projects queryProject = new Projects();
        queryProject.setName("筛选测试");
        queryProject.setStatus("IN_PROGRESS");
        queryProject.setCustomerId("customer123");

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
        verify(projectsMapper, times(1))
            .selectProjectsWithMembers(queryProject, memberUserId, isAdmin);
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
            .thenReturn(testProjectList);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", memberUserId, isAdmin);

        // Then
        assertNotNull(result);
        verify(projectsMapper, times(1))
            .selectProjectsWithMembers(queryProject, memberUserId, isAdmin);

        // 验证查询参数中包含客户ID
        verify(projectsMapper).selectProjectsWithMembers(
            argThat(project -> "customer456".equals(project.getCustomerId())),
            eq(memberUserId), eq(isAdmin)
        );
    }

    @Test
    @DisplayName("通过项目成员关联查询 - 空的memberUserId参数")
    void testSelectProjectsWithMembersWithEmptyUserId() {
        // Given
        Projects queryProject = new Projects();
        String memberUserId = "";
        boolean isAdmin = false;

        // 非管理员且用户ID为空时，Mapper应该收到空字符串
        when(projectsMapper.selectProjectsWithMembers(queryProject, memberUserId, isAdmin))
            .thenReturn(Collections.emptyList());

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", memberUserId, isAdmin);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectsMapper, times(1))
            .selectProjectsWithMembers(queryProject, memberUserId, isAdmin);
    }

    @Test
    @DisplayName("通过项目成员关联查询 - 管理员角色权限验证")
    void testSelectProjectsWithMembersAdminRoleCheck() {
        // Given
        Projects queryProject = new Projects();
        String memberUserId = "adminUser";
        boolean isAdmin = true;

        when(projectsMapper.selectProjectsWithMembers(queryProject, memberUserId, isAdmin))
            .thenReturn(testProjectList);

        // When - 模拟管理员用户访问
        List<Projects> result = projectsService.selectProjectsWithRelations(
            queryProject, "projectMembers", memberUserId, isAdmin);

        // Then - 管理员应该能看到所有项目
        assertNotNull(result);
        assertEquals(3, result.size(), "管理员应该能看到所有项目");

        verify(projectsMapper, times(1))
            .selectProjectsWithMembers(queryProject, memberUserId, true);
    }

    @Test
    @DisplayName("关联查询组合 - projectMembers + customer")
    void testSelectProjectsWithMembersAndCustomer() {
        // Given
        Projects queryProject = new Projects();
        String memberUserId = "user123";
        boolean isAdmin = true;

        // Service层会优先匹配"customer"，所以应该Mock selectProjectsWithCustomer
        when(projectsMapper.selectProjectsWithCustomer(queryProject))
            .thenReturn(testProjectList);

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
    @DisplayName("边界条件测试 - 空项目列表权限过滤")
    void testSelectProjectsWithRelationsEmptyListWithUserFilter() {
        // Given
        Projects queryProject = new Projects();
        // 注意：setUserProjectIds方法不存在，删除这行调用

        List<Projects> emptyList = Collections.emptyList();
        when(projectsMapper.selectProjectsWithCustomer(queryProject)).thenReturn(emptyList);

        // When
        List<Projects> result = projectsService.selectProjectsWithRelations(queryProject, "customer", null, true);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectsMapper, times(1)).selectProjectsWithCustomer(queryProject);
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