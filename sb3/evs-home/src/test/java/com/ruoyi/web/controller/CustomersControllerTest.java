package com.ruoyi.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.web.domain.Customers;
import com.ruoyi.web.service.ICustomersService;
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
 * 客户档案Controller测试类
 *
 * @author evs
 * @date 2025-11-23
 */
@WebMvcTest(CustomersController.class)
@Import(TestSecurityConfig.class)
class CustomersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ICustomersService customersService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper;

    private Customers testCustomer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper();

        // 创建测试数据
        testCustomer = new Customers();
        testCustomer.setId("customer-001");
        testCustomer.setName("测试客户");
        testCustomer.setPhone("13800138000");
        testCustomer.setEmail("test@example.com");
        testCustomer.setAddress("测试地址");
        testCustomer.setLevel("VIP");
        testCustomer.setSource("REFERRAL");
        testCustomer.setRemarks("测试备注");
        testCustomer.setIsActive(1);
        testCustomer.setCreatedAt(new Date());
        testCustomer.setCreatedBy("admin");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:customers:list"})
    void testListCustomersWithProjects() throws Exception {
        // 准备测试数据
        List<Customers> customerList = new ArrayList<>();
        customerList.add(testCustomer);
        testCustomer.setProjectCount(3); // 设置项目数量

        // 模拟service调用
        when(customersService.selectCustomersWithRelations(any(Customers.class), eq(true)))
                .thenReturn(customerList);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/customers/list")
                        .param("includeProjects", "true")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows[0].id").value("customer-001"))
                .andExpect(jsonPath("$.rows[0].name").value("测试客户"))
                .andExpect(jsonPath("$.rows[0].phone").value("13800138000"))
                .andExpect(jsonPath("$.rows[0].projectCount").value(3))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:customers:list"})
    void testListCustomersWithoutProjects() throws Exception {
        // 准备测试数据
        List<Customers> customerList = new ArrayList<>();
        customerList.add(testCustomer);

        // 模拟service调用
        when(customersService.selectCustomersWithRelations(any(Customers.class), eq(false)))
                .thenReturn(customerList);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/customers/list")
                        .param("includeProjects", "false")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows[0].id").value("customer-001"))
                .andExpect(jsonPath("$.rows[0].name").value("测试客户"))
                .andExpect(jsonPath("$.rows[0].projectCount").doesNotExist());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:customers:query"})
    void testGetCustomerWithProjects() throws Exception {
        // 设置项目数量
        testCustomer.setProjectCount(5);

        // 模拟service调用
        when(customersService.selectCustomersWithRelationsById(eq("customer-001"), eq(true)))
                .thenReturn(testCustomer);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/customers/customer-001")
                        .param("includeProjects", "true")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("customer-001"))
                .andExpect(jsonPath("$.data.name").value("测试客户"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.projectCount").value(5));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:customers:list"})
    void testCheckPhoneExists() throws Exception {
        // 模拟service调用 - 手机号存在
        when(customersService.checkPhoneExists(eq("13800138000"), eq(null)))
                .thenReturn(true);

        // 执行请求并验证结果
        mockMvc.perform(get("/evs/customers/checkPhone/13800138000")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"evs:customers:add"})
    void testAddCustomer() throws Exception {
        // 准备测试数据
        Customers newCustomer = new Customers();
        newCustomer.setName("新客户");
        newCustomer.setPhone("13900139000");
        newCustomer.setEmail("new@example.com");

        // 模拟service调用
        when(customersService.insertCustomers(any(Customers.class)))
                .thenReturn(1);

        // 执行请求并验证结果
        mockMvc.perform(post("/evs/customers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("操作成功"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"evs:customers:query"})
    void testUnauthorizedAccess() throws Exception {
        // 测试没有list权限的用户无法访问列表接口
        mockMvc.perform(get("/evs/customers/list")
                        .param("includeProjects", "true")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUnauthenticatedAccess() throws Exception {
        // 测试未认证用户无法访问接口
        mockMvc.perform(get("/evs/customers/list")
                        .param("includeProjects", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}