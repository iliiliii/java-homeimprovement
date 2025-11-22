package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.Customers;
import com.ruoyi.web.mapper.CustomersMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.HashSet;

/**
 * 客户档案Service层独立单元测试类
 * 使用Mockito进行依赖隔离测试
 *
 * @author evs
 * @date 2025-11-18
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("客户档案Service层单元测试")
class CustomersServiceUnitTest {

    @Mock
    private CustomersMapper customersMapper;

    @InjectMocks
    private CustomersServiceImpl customersService;

    private Customers testCustomer;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testCustomer = new Customers();
        testCustomer.setId("test-customer-123");
        testCustomer.setName("测试客户");
        testCustomer.setPhone("13800138000");
        testCustomer.setEmail("test@example.com");
        testCustomer.setAddress("测试地址");
        testCustomer.setLevel("VIP");
        testCustomer.setSource("官网");
        testCustomer.setRemarks("测试备注");
        testCustomer.setIsActive(1);
        testCustomer.setCreatedAt(new Date());
        testCustomer.setUpdatedAt(new Date());
        
        // 清理 SecurityContext，确保每个测试开始时都是干净的状态
        SecurityContextHolder.clearContext();
    }
    
    /**
     * 辅助方法：模拟登录用户
     * @param username 用户名
     */
    private void mockLoginUser(String username) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(1L);
        sysUser.setUserName(username);
        LoginUser loginUser = new LoginUser(sysUser, new HashSet<>());
        Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser, null, null);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("测试手机号存在检查 - 手机号存在")
    void testCheckPhoneExists_WhenPhoneExists_ShouldReturnTrue() {
        // Given - 模拟手机号存在
        when(customersMapper.selectCustomersByPhone(eq("13800138000"))).thenReturn(testCustomer);

        // When
        boolean result = customersService.checkPhoneExists("13800138000", null);

        // Then
        assertTrue(result, "手机号存在时应返回true");
        verify(customersMapper, times(1)).selectCustomersByPhone("13800138000");
    }

    @Test
    @DisplayName("测试手机号存在检查 - 手机号不存在")
    void testCheckPhoneExists_WhenPhoneNotExists_ShouldReturnFalse() {
        // Given - 模拟手机号不存在
        when(customersMapper.selectCustomersByPhone(eq("13900139999"))).thenReturn(null);

        // When
        boolean result = customersService.checkPhoneExists("13900139999", null);

        // Then
        assertFalse(result, "手机号不存在时应返回false");
        verify(customersMapper, times(1)).selectCustomersByPhone("13900139999");
    }

    @Test
    @DisplayName("测试手机号存在检查 - 排除ID与查询客户ID相同")
    void testCheckPhoneExists_WithExcludeId_WhenIdsMatch_ShouldReturnFalse() {
        // Given - 排除ID与查询客户ID相同
        String excludeId = "test-customer-123";
        testCustomer.setId(excludeId);
        when(customersMapper.selectCustomersByPhone(eq("13800138000"))).thenReturn(testCustomer);

        // When
        boolean result = customersService.checkPhoneExists("13800138000", excludeId);

        // Then
        assertFalse(result, "排除ID与查询客户ID相同时应返回false");
        verify(customersMapper, times(1)).selectCustomersByPhone("13800138000");
    }

    @Test
    @DisplayName("测试手机号存在检查 - 排除ID与查询客户ID不同")
    void testCheckPhoneExists_WithExcludeId_WhenIdsNotMatch_ShouldReturnTrue() {
        // Given - 排除ID与查询客户ID不同
        String excludeId = "different-customer-id";
        testCustomer.setId("test-customer-123");
        when(customersMapper.selectCustomersByPhone(eq("13800138000"))).thenReturn(testCustomer);

        // When
        boolean result = customersService.checkPhoneExists("13800138000", excludeId);

        // Then
        assertTrue(result, "排除ID与查询客户ID不同时应返回true");
        verify(customersMapper, times(1)).selectCustomersByPhone("13800138000");
    }

    @Test
    @DisplayName("测试手机号存在检查 - 手机号为null")
    void testCheckPhoneExists_WhenPhoneIsNull_ShouldReturnFalse() {
        // When
        boolean result = customersService.checkPhoneExists(null, null);

        // Then
        assertFalse(result, "手机号为null时应返回false");
        verify(customersMapper, never()).selectCustomersByPhone(any());
    }

    @Test
    @DisplayName("测试手机号存在检查 - 手机号为空字符串")
    void testCheckPhoneExists_WhenPhoneIsEmpty_ShouldReturnFalse() {
        // When
        boolean result = customersService.checkPhoneExists("", null);

        // Then
        assertFalse(result, "手机号为空字符串时应返回false");
        verify(customersMapper, never()).selectCustomersByPhone(any());
    }

    @Test
    @DisplayName("测试手机号存在检查 - 手机号为空白字符串")
    void testCheckPhoneExists_WhenPhoneIsBlank_ShouldReturnFalse() {
        // When
        boolean result = customersService.checkPhoneExists("   ", null);

        // Then
        assertFalse(result, "手机号为空白字符串时应返回false");
        verify(customersMapper, never()).selectCustomersByPhone(any());
    }

    @Test
    @DisplayName("测试客户查询 - 通过ID查询客户")
    void testSelectCustomersById_WithValidId_ShouldReturnCustomer() {
        // Given
        String customerId = "test-customer-123";
        when(customersMapper.selectCustomersById(customerId)).thenReturn(testCustomer);

        // When
        Customers result = customersService.selectCustomersById(customerId);

        // Then
        assertNotNull(result, "应该能找到客户");
        assertEquals(customerId, result.getId());
        assertEquals("测试客户", result.getName());
        verify(customersMapper, times(1)).selectCustomersById(customerId);
    }

    @Test
    @DisplayName("测试客户查询 - 通过ID查询不到客户")
    void testSelectCustomersById_WithInvalidId_ShouldReturnNull() {
        // Given
        String invalidId = "invalid-id";
        when(customersMapper.selectCustomersById(invalidId)).thenReturn(null);

        // When
        Customers result = customersService.selectCustomersById(invalidId);

        // Then
        assertNull(result, "查询不到客户时应返回null");
        verify(customersMapper, times(1)).selectCustomersById(invalidId);
    }

    @Test
    @DisplayName("测试客户列表查询")
    void testSelectCustomersList_ShouldReturnCustomersList() {
        // Given
        when(customersMapper.selectCustomersList(any(Customers.class))).thenReturn(java.util.Arrays.asList(testCustomer));

        // When
        java.util.List<Customers> result = customersService.selectCustomersList(new Customers());

        // Then
        assertNotNull(result, "查询结果不应为null");
        assertEquals(1, result.size(), "应该返回1个客户");
        verify(customersMapper, times(1)).selectCustomersList(any(Customers.class));
    }

    @Test
    @DisplayName("测试新增客户")
    void testInsertCustomers_WithValidCustomer_ShouldReturnSuccess() {
        // Given - 模拟登录用户
        mockLoginUser("testuser");
        when(customersMapper.insertCustomers(any(Customers.class))).thenReturn(1);

        // When
        int result = customersService.insertCustomers(testCustomer);

        // Then
        assertEquals(1, result, "新增客户应该成功");
        verify(customersMapper, times(1)).insertCustomers(testCustomer);
    }

    @Test
    @DisplayName("测试更新客户")
    void testUpdateCustomers_WithValidCustomer_ShouldReturnSuccess() {
        // Given - 模拟登录用户
        mockLoginUser("testuser");
        when(customersMapper.updateCustomers(any(Customers.class))).thenReturn(1);

        // When
        int result = customersService.updateCustomers(testCustomer);

        // Then
        assertEquals(1, result, "更新客户应该成功");
        verify(customersMapper, times(1)).updateCustomers(testCustomer);
    }

    @Test
    @DisplayName("测试删除客户")
    void testDeleteCustomersById_WithValidId_ShouldReturnSuccess() {
        // Given
        String customerId = "test-customer-123";
        when(customersMapper.deleteCustomersById(customerId)).thenReturn(1);

        // When
        int result = customersService.deleteCustomersById(customerId);

        // Then
        assertEquals(1, result, "删除客户应该成功");
        verify(customersMapper, times(1)).deleteCustomersById(customerId);
    }

    @Test
    @DisplayName("测试批量删除客户")
    void testDeleteCustomersByIds_WithValidIds_ShouldReturnSuccess() {
        // Given
        String[] customerIds = {"id1", "id2", "id3"};
        when(customersMapper.deleteCustomersByIds(customerIds)).thenReturn(3);

        // When
        int result = customersService.deleteCustomersByIds(customerIds);

        // Then
        assertEquals(3, result, "批量删除客户应该成功");
        verify(customersMapper, times(1)).deleteCustomersByIds(customerIds);
    }
}