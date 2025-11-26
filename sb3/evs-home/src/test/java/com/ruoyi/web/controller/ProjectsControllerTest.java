package com.ruoyi.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.service.IProjectsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.ruoyi.framework.config.SecurityConfig;
import com.ruoyi.web.controller.TestSecurityConfig;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 项目信息Controller测试类
 *
 * @author evs
 * @date 2025-11-23
 */
@WebMvcTest(ProjectsController.class)
@Import(TestSecurityConfig.class)
class ProjectsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProjectsService projectsService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper;

    private Projects testProject;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper();

        // 创建测试数据
        testProject = new Projects();
        testProject.setId("test-id-001");
        testProject.setName("测试项目");
        testProject.setProjectType("RESIDENTIAL");
        testProject.setCustomerId("customer-001");
        testProject.setAddress("测试地址");
        testProject.setArea(new BigDecimal("120.5"));
        testProject.setBudget(new BigDecimal("150000"));
        testProject.setActualCost(new BigDecimal("80000"));
        testProject.setStartDate(new Date());
        testProject.setEndDate(new Date());
        testProject.setStatus("PLANNING");
        testProject.setPriority("MEDIUM");
        testProject.setProgress(new BigDecimal("25"));
        testProject.setDescription("测试项目描述");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:list"})
    void testListProjectsWithCustomer() throws Exception {
        // 准备测试数据
        List<Projects> projectList = new ArrayList<>();
        projectList.add(testProject);

        TableDataInfo tableDataInfo = new TableDataInfo();
        tableDataInfo.setCode(200);
        tableDataInfo.setRows(projectList);
        tableDataInfo.setTotal(1);

        // 模拟service调用
        when(projectsService.selectProjectsWithRelations(any(Projects.class), eq("customer")))
                .thenReturn(projectList);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeCustomer", "true")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows[0].id").value("test-id-001"))
                .andExpect(jsonPath("$.rows[0].name").value("测试项目"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:list"})
    void testListProjectsWithBudgetItems() throws Exception {
        // 准备测试数据
        List<Projects> projectList = new ArrayList<>();
        projectList.add(testProject);

        // 模拟service调用
        when(projectsService.selectProjectsWithRelations(any(Projects.class), eq("budgetItems")))
                .thenReturn(projectList);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeBudgetItems", "true")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows[0].id").value("test-id-001"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:list"})
    void testListProjectsWithAllRelations() throws Exception {
        // 准备测试数据
        List<Projects> projectList = new ArrayList<>();
        projectList.add(testProject);

        // 模拟service调用
        when(projectsService.selectProjectsWithRelations(any(Projects.class), eq("customer,budgetItems,schedules")))
                .thenReturn(projectList);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeCustomer", "true")
                        .param("includeBudgetItems", "true")
                        .param("includeSchedules", "true")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows[0].id").value("test-id-001"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:list"})
    void testListProjectsWithoutRelations() throws Exception {
        // 准备测试数据
        List<Projects> projectList = new ArrayList<>();
        projectList.add(testProject);

        // 模拟service调用（不包含关联参数时应该调用原有的selectProjectsList方法）
        when(projectsService.selectProjectsWithRelations(any(Projects.class), eq("")))
                .thenReturn(projectList);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/list")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows[0].id").value("test-id-001"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:query"})
    void testGetProjectWithCustomer() throws Exception {
        // 模拟service调用
        when(projectsService.selectProjectsWithRelationsById(eq("test-id-001"), eq("customer")))
                .thenReturn(testProject);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/test-id-001")
                        .param("includeCustomer", "true")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("test-id-001"))
                .andExpect(jsonPath("$.data.name").value("测试项目"))
                .andExpect(jsonPath("$.data.customerId").value("customer-001"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:query"})
    void testGetProjectWithoutRelations() throws Exception {
        // 模拟service调用
        when(projectsService.selectProjectsWithRelationsById(eq("test-id-001"), eq("")))
                .thenReturn(testProject);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/test-id-001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("test-id-001"))
                .andExpect(jsonPath("$.data.name").value("测试项目"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:query"})
    void testGetProjectNotFound() throws Exception {
        // 模拟service返回null
        when(projectsService.selectProjectsWithRelationsById(eq("non-existent-id"), anyString()))
                .thenReturn(null);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/projects/non-existent-id")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:add"})
    void testAddProject() throws Exception {
        // 准备测试数据
        Projects newProject = new Projects();
        newProject.setName("新建项目");
        newProject.setProjectType("COMMERCIAL");
        newProject.setCustomerId("customer-002");

        // 模拟service调用
        when(projectsService.insertProjects(any(Projects.class)))
                .thenReturn(1);

        // 执行请求并验证结果
        mockMvc.perform(post("/evs/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("操作成功"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:edit"})
    void testUpdateProject() throws Exception {
        // 准备测试数据
        testProject.setName("更新后的项目");

        // 模拟service调用
        when(projectsService.updateProjects(any(Projects.class)))
                .thenReturn(1);

        // 执行请求并验证结果
        mockMvc.perform(put("/evs/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("操作成功"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:projects:remove"})
    void testDeleteProject() throws Exception {
        // 模拟service调用 - 匹配控制器的 deleteProjectsByIds 方法
        when(projectsService.deleteProjectsByIds(any(String[].class)))
                .thenReturn(1);

        // 执行请求并验证结果
        mockMvc.perform(delete("/evs/projects/test-id-001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("操作成功"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"evs:projects:query"})
    void testUnauthorizedAccess() throws Exception {
        // 测试没有list权限的用户无法访问列表接口
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeCustomer", "true")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUnauthenticatedAccess() throws Exception {
        // 测试未认证用户无法访问接口
        mockMvc.perform(get("/evs/projects/list")
                        .param("includeCustomer", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}