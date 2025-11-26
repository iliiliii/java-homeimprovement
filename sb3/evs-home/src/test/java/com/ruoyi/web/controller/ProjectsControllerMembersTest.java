package com.ruoyi.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.service.IProjectsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 项目成员关联查询Controller测试类
 * 专门测试通过 projectMembers 表关联查询和权限过滤的API接口
 *
 * @author evs
 * @date 2025-11-27
 */
@WebMvcTest(ProjectsController.class)
@DisplayName("项目成员关联查询Controller测试")
class ProjectsControllerMembersTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProjectsService projectsService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper;
    private Projects testProject1;
    private Projects testProject2;
    private Projects testProject3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper();

        // 创建测试数据
        testProject1 = createTestProject("1", "测试项目1", "IN_PROGRESS");
        testProject2 = createTestProject("2", "测试项目2", "PLANNED");
        testProject3 = createTestProject("3", "测试项目3", "COMPLETED");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:list"})
    @DisplayName("API测试 - 管理员通过项目成员关联查询查看所有项目")
    void testListProjectsWithMembersAsAdmin() throws Exception {
        // 准备测试数据
        List<Projects> projectList = new ArrayList<>();
        projectList.add(testProject1);
        projectList.add(testProject2);
        projectList.add(testProject3);

        // 模拟service调用 - 管理员
        when(projectsService.selectProjectsWithRelations(
            any(Projects.class), eq("projectMembers"), eq("admin"), eq(true)))
            .thenReturn(projectList);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeProjectMembers", "true")
                        .param("memberUserId", "admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows.length()").value(3))
                .andExpect(jsonPath("$.rows[0].id").value("1"))
                .andExpect(jsonPath("$.rows[1].id").value("2"))
                .andExpect(jsonPath("$.rows[2].id").value("3"))
                .andExpect(jsonPath("$.total").value(3));
    }

    @Test
    @WithMockUser(username = "user123", authorities = {"evs:projects:list"})
    @DisplayName("API测试 - 普通用户通过项目成员关联查询查看自己参与的项目")
    void testListProjectsWithMembersAsNormalUser() throws Exception {
        // 准备测试数据 - 用户只能看到自己参与的项目
        List<Projects> projectList = new ArrayList<>();
        projectList.add(testProject1);
        projectList.add(testProject3);

        // 模���service调用 - 普通用户
        when(projectsService.selectProjectsWithRelations(
            any(Projects.class), eq("projectMembers"), eq("user123"), eq(false)))
            .thenReturn(projectList);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeProjectMembers", "true")
                        .param("memberUserId", "user123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows.length()").value(2))
                .andExpect(jsonPath("$.rows[0].id").value("1"))
                .andExpect(jsonPath("$.rows[1].id").value("3"))
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:list"})
    @DisplayName("API测试 - 管理员使用项目名称筛选")
    void testListProjectsWithMembersAndNameFilter() throws Exception {
        // 准备测试数据
        List<Projects> projectList = new ArrayList<>();
        projectList.add(testProject1);

        // 模拟service调用
        when(projectsService.selectProjectsWithRelations(
            any(Projects.class), eq("projectMembers"), eq("admin"), eq(true)))
            .thenReturn(projectList);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeProjectMembers", "true")
                        .param("name", "测试项目1")
                        .param("memberUserId", "admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows.length()").value(1))
                .andExpect(jsonPath("$.rows[0].name").value("测试项目1"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:list"})
    @DisplayName("API测试 - 管理员使用项目状态筛选")
    void testListProjectsWithMembersAndStatusFilter() throws Exception {
        // 准备测���数据
        List<Projects> projectList = new ArrayList<>();
        projectList.add(testProject2);

        // 模拟service调用
        when(projectsService.selectProjectsWithRelations(
            any(Projects.class), eq("projectMembers"), eq("admin"), eq(true)))
            .thenReturn(projectList);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeProjectMembers", "true")
                        .param("status", "PLANNED")
                        .param("memberUserId", "admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows.length()").value(1))
                .andExpect(jsonPath("$.rows[0].status").value("PLANNED"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:list"})
    @DisplayName("API测试 - 管理���使用关联客户筛选（管理员特有功能）")
    void testListProjectsWithMembersAsAdminWithCustomerFilter() throws Exception {
        // 准备测试数据
        Projects customerProject = createTestProject("1", "客户专属项目", "IN_PROGRESS");
        customerProject.setCustomerId("customer-123");

        List<Projects> projectList = new ArrayList<>();
        projectList.add(customerProject);

        // 模拟service调用
        when(projectsService.selectProjectsWithRelations(
            any(Projects.class), eq("projectMembers"), eq("admin"), eq(true)))
            .thenReturn(projectList);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeProjectMembers", "true")
                        .param("customerId", "customer-123")
                        .param("memberUserId", "admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows.length()").value(1))
                .andExpect(jsonPath("$.rows[0].customerId").value("customer-123"));
    }

    @Test
    @WithMockUser(username = "user123", authorities = {"evs:projects:list"})
    @DisplayName("API测试 - 普通用户权限验证 - 只能看自己的项目")
    void testListProjectsWithMembersPermissionCheckForNormalUser() throws Exception {
        // 准备测试数据 - 只有用户参与的项目
        List<Projects> userProjects = new ArrayList<>();
        userProjects.add(testProject1);

        // 模拟service调用 - isAdmin为false
        when(projectsService.selectProjectsWithRelations(
            any(Projects.class), eq("projectMembers"), eq("user123"), eq(false)))
            .thenReturn(userProjects);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeProjectMembers", "true")
                        .param("memberUserId", "user123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows.length()").value(1))
                .andExpect(jsonPath("$.rows[0].id").value("1"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:list"})
    @DisplayName("API测试 - 管理员权限验证 - 能看到所有项目")
    void testListProjectsWithMembersPermissionCheckForAdmin() throws Exception {
        // 准备测试数据 - 包含所有项目
        List<Projects> allProjects = new ArrayList<>();
        allProjects.add(testProject1);
        allProjects.add(testProject2);
        allProjects.add(testProject3);

        // 模拟service调用 - isAdmin为true
        when(projectsService.selectProjectsWithRelations(
            any(Projects.class), eq("projectMembers"), eq("admin"), eq(true)))
            .thenReturn(allProjects);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeProjectMembers", "true")
                        .param("memberUserId", "admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows.length()").value(3));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"evs:projects:list"})
    @DisplayName("API测试 - 空结果查询")
    void testListProjectsWithMembersEmptyResult() throws Exception {
        // 准备测试数据 - 空结果
        List<Projects> emptyList = new ArrayList<>();

        // 模拟service调用
        when(projectsService.selectProjectsWithRelations(
            any(Projects.class), eq("projectMembers"), eq("user"), eq(false)))
            .thenReturn(emptyList);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeProjectMembers", "true")
                        .param("memberUserId", "user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows.length()").value(0))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"evs:projects:list"})
    @DisplayName("API测试 - 组合筛选条件（项目名称 + 状态）")
    void testListProjectsWithMembersWithMultipleFilters() throws Exception {
        // 准备测试数据
        List<Projects> filteredProjects = new ArrayList<>();
        filteredProjects.add(testProject1);

        // 模拟service调用
        when(projectsService.selectProjectsWithRelations(
            any(Projects.class), eq("projectMembers"), eq("user"), eq(false)))
            .thenReturn(filteredProjects);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeProjectMembers", "true")
                        .param("name", "测试")
                        .param("status", "IN_PROGRESS")
                        .param("memberUserId", "user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows.length()").value(1));
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
