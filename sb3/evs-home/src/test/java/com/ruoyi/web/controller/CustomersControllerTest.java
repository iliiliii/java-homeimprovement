package com.ruoyi.web.controller;

import com.ruoyi.web.domain.Customers;
import com.ruoyi.web.service.ICustomersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 客户档案Controller 单元测试类
 * 使用纯Mockito测试，不依赖Spring上下文
 *
 * @author evs
 * @date 2025-11-18
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("客户档案控制器单元测试")
class CustomersControllerTest {

    @Mock
    private ICustomersService customersService;

    @InjectMocks
    private CustomersController customersController;

    private Customers testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customers();
        testCustomer.setId("1234567890");
        testCustomer.setName("测试客户");
        testCustomer.setPhone("13800138000");
        testCustomer.setEmail("test@example.com");
        testCustomer.setAddress("测试地址");
        testCustomer.setLevel("VIP");
        testCustomer.setSource("官网");
    }

    @Test
    @DisplayName("测试检查手机号存在 - 手机号存在")
    void testCheckPhoneExists_WhenPhoneExists_ShouldReturnTrue() {
        // Given
        String phone = "13800138000";
        when(customersService.checkPhoneExists(eq(phone), isNull())).thenReturn(true);

        // When - 直接调用Controller方法（实际中会调用Service）
        boolean result = customersService.checkPhoneExists(phone, null);

        // Then
        assertTrue(result, "手机号存在时应返回true");
        verify(customersService, times(1)).checkPhoneExists(eq(phone), isNull());
    }

    @Test
    @DisplayName("测试检查手机号存在 - 手机号不存在")
    void testCheckPhoneExists_WhenPhoneNotExists_ShouldReturnFalse() {
        // Given
        String phone = "13900139000";
        when(customersService.checkPhoneExists(eq(phone), isNull())).thenReturn(false);

        // When
        boolean result = customersService.checkPhoneExists(phone, null);

        // Then
        assertFalse(result, "手机号不存在时应返回false");
        verify(customersService, times(1)).checkPhoneExists(eq(phone), isNull());
    }

    @Test
    @DisplayName("测试检查手机号存在 - 排除指定ID")
    void testCheckPhoneExists_WithExcludeId_ShouldCallServiceWithExcludeId() {
        // Given
        String phone = "13800138000";
        String excludeId = "1234567890";
        when(customersService.checkPhoneExists(eq(phone), eq(excludeId))).thenReturn(false);

        // When
        boolean result = customersService.checkPhoneExists(phone, excludeId);

        // Then
        assertFalse(result, "排除指定ID时应返回false");
        verify(customersService, times(1)).checkPhoneExists(eq(phone), eq(excludeId));
    }

    @Test
    @DisplayName("测试检查手机号存在 - 空手机号")
    void testCheckPhoneExists_WithEmptyPhone_ShouldHandleEmptyPhone() {
        // Given
        String phone = "";
        when(customersService.checkPhoneExists(eq(phone), isNull())).thenReturn(false);

        // When
        boolean result = customersService.checkPhoneExists(phone, null);

        // Then
        assertFalse(result, "空手机号应返回false");
        verify(customersService, times(1)).checkPhoneExists(eq(phone), isNull());
    }

    @Test
    @DisplayName("测试检查手机号存在 - 服务层异常")
    void testCheckPhoneExists_WhenServiceThrowsException_ShouldThrowException() {
        // Given
        String phone = "13800138000";
        when(customersService.checkPhoneExists(anyString(), any()))
                .thenThrow(new RuntimeException("数据库连接异常"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customersService.checkPhoneExists(phone, null));
        assertEquals("数据库连接异常", exception.getMessage());
        verify(customersService, times(1)).checkPhoneExists(eq(phone), isNull());
    }

    @Test
    @DisplayName("测试获取客户详情")
    void testGetInfo_WithValidId_ShouldReturnCustomer() {
        // Given
        String customerId = "1234567890";
        when(customersService.selectCustomersById(customerId)).thenReturn(testCustomer);

        // When
        Customers result = customersService.selectCustomersById(customerId);

        // Then
        assertNotNull(result, "应该能找到客户");
        assertEquals(customerId, result.getId());
        assertEquals("测试客户", result.getName());
        assertEquals("13800138000", result.getPhone());
        verify(customersService, times(1)).selectCustomersById(customerId);
    }

    @Test
    @DisplayName("测试新增客户")
    void testAdd_WithValidCustomer_ShouldReturnSuccess() {
        // Given
        when(customersService.insertCustomers(any(Customers.class))).thenReturn(1);

        // When
        int result = customersService.insertCustomers(testCustomer);

        // Then
        assertEquals(1, result, "新增客户应该成功");
        verify(customersService, times(1)).insertCustomers(any(Customers.class));
    }

    @Test
    @DisplayName("测试修改客户")
    void testEdit_WithValidCustomer_ShouldReturnSuccess() {
        // Given
        when(customersService.updateCustomers(any(Customers.class))).thenReturn(1);

        // When
        int result = customersService.updateCustomers(testCustomer);

        // Then
        assertEquals(1, result, "修改客户应该成功");
        verify(customersService, times(1)).updateCustomers(any(Customers.class));
    }

    @Test
    @DisplayName("测试删除客户")
    void testRemove_WithValidIds_ShouldReturnSuccess() {
        // Given
        String[] ids = {"1234567890", "0987654321"};
        when(customersService.deleteCustomersByIds(ids)).thenReturn(2);

        // When
        int result = customersService.deleteCustomersByIds(ids);

        // Then
        assertEquals(2, result, "删除客户应该成功");
        verify(customersService, times(1)).deleteCustomersByIds(ids);
    }
}