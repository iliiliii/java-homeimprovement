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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 客户档案Service实现类测试
 *
 * @author evs
 * @date 2025-11-18
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("客户档案Service实现类测试")
class CustomersServiceImplTest {

    @Mock
    private CustomersMapper customersMapper;

    @InjectMocks
    private CustomersServiceImpl customersService;

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
        when(customersMapper.selectCustomersByPhone(eq(phone))).thenReturn(testCustomer);

        // When
        boolean result = customersService.checkPhoneExists(phone, null);

        // Then
        assertTrue(result, "手机号存在时应返回true");
        verify(customersMapper, times(1)).selectCustomersByPhone(eq(phone));
    }

    @Test
    @DisplayName("测试检查手机号存在 - 手机号不存在")
    void testCheckPhoneExists_WhenPhoneNotExists_ShouldReturnFalse() {
        // Given
        String phone = "13900139000";
        when(customersMapper.selectCustomersByPhone(eq(phone))).thenReturn(null);

        // When
        boolean result = customersService.checkPhoneExists(phone, null);

        // Then
        assertFalse(result, "手机号不存在时应返回false");
        verify(customersMapper, times(1)).selectCustomersByPhone(eq(phone));
    }

    @Test
    @DisplayName("测试检查手机号存在 - 排除指定ID且ID匹配")
    void testCheckPhoneExists_WithExcludeId_WhenIdsMatch_ShouldReturnFalse() {
        // Given
        String phone = "13800138000";
        String excludeId = "1234567890";
        testCustomer.setId(excludeId);
        when(customersMapper.selectCustomersByPhone(eq(phone))).thenReturn(testCustomer);

        // When
        boolean result = customersService.checkPhoneExists(phone, excludeId);

        // Then
        assertFalse(result, "排除ID与查询客户ID相同时应返回false");
        verify(customersMapper, times(1)).selectCustomersByPhone(eq(phone));
    }

    @Test
    @DisplayName("测试检查手机号存在 - 排除指定ID但ID不匹配")
    void testCheckPhoneExists_WithExcludeId_WhenIdsNotMatch_ShouldReturnTrue() {
        // Given
        String phone = "13800138000";
        String excludeId = "0987654321";
        testCustomer.setId("1234567890");
        when(customersMapper.selectCustomersByPhone(eq(phone))).thenReturn(testCustomer);

        // When
        boolean result = customersService.checkPhoneExists(phone, excludeId);

        // Then
        assertTrue(result, "排除ID与查询客户ID不同时应返回true");
        verify(customersMapper, times(1)).selectCustomersByPhone(eq(phone));
    }

    @Test
    @DisplayName("测试检查手机号存在 - 手机号为null")
    void testCheckPhoneExists_WhenPhoneIsNull_ShouldReturnFalse() {
        // Given
        String phone = null;

        // When
        boolean result = customersService.checkPhoneExists(phone, null);

        // Then
        assertFalse(result, "手机号为null时应返回false");
        verify(customersMapper, never()).selectCustomersByPhone(anyString());
    }

    @Test
    @DisplayName("测试检查手机号存在 - 手机号为空字符串")
    void testCheckPhoneExists_WhenPhoneIsEmpty_ShouldReturnFalse() {
        // Given
        String phone = "";

        // When
        boolean result = customersService.checkPhoneExists(phone, null);

        // Then
        assertFalse(result, "手机号为空字符串时应返回false");
        verify(customersMapper, never()).selectCustomersByPhone(anyString());
    }

    @Test
    @DisplayName("测试检查手机号存在 - 手机号为空白字符串")
    void testCheckPhoneExists_WhenPhoneIsBlank_ShouldReturnFalse() {
        // Given
        String phone = "   ";

        // When
        boolean result = customersService.checkPhoneExists(phone, null);

        // Then
        assertFalse(result, "手机号为空白字符串时应返回false");
        verify(customersMapper, never()).selectCustomersByPhone(anyString());
    }

    @Test
    @DisplayName("测试检查手机号存在 - 排除ID为null")
    void testCheckPhoneExists_WhenExcludeIdIsNull_ShouldNotExclude() {
        // Given
        String phone = "13800138000";
        String excludeId = null;
        when(customersMapper.selectCustomersByPhone(eq(phone))).thenReturn(testCustomer);

        // When
        boolean result = customersService.checkPhoneExists(phone, excludeId);

        // Then
        assertTrue(result, "排除ID为null时不应排除任何客户");
        verify(customersMapper, times(1)).selectCustomersByPhone(eq(phone));
    }

    @Test
    @DisplayName("测试检查手机号存在 - 排除ID为空字符串")
    void testCheckPhoneExists_WhenExcludeIdIsEmpty_ShouldNotExclude() {
        // Given
        String phone = "13800138000";
        String excludeId = "";
        when(customersMapper.selectCustomersByPhone(eq(phone))).thenReturn(testCustomer);

        // When
        boolean result = customersService.checkPhoneExists(phone, excludeId);

        // Then
        assertTrue(result, "排除ID为空字符串时不应排除任何客户");
        verify(customersMapper, times(1)).selectCustomersByPhone(eq(phone));
    }

    @Test
    @DisplayName("测试检查手机号存在 - 排除ID为空白字符串")
    void testCheckPhoneExists_WhenExcludeIdIsBlank_ShouldNotExclude() {
        // Given
        String phone = "13800138000";
        String excludeId = "   ";
        when(customersMapper.selectCustomersByPhone(eq(phone))).thenReturn(testCustomer);

        // When
        boolean result = customersService.checkPhoneExists(phone, excludeId);

        // Then
        assertTrue(result, "排除ID为空白字符串时不应排除任何客户");
        verify(customersMapper, times(1)).selectCustomersByPhone(eq(phone));
    }

    @Test
    @DisplayName("测试检查手机号存在 - 数据库查询异常")
    void testCheckPhoneExists_WhenMapperThrowsException_ShouldPropagateException() {
        // Given
        String phone = "13800138000";
        when(customersMapper.selectCustomersByPhone(eq(phone)))
                .thenThrow(new RuntimeException("数据库连接异常"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customersService.checkPhoneExists(phone, null);
        });

        assertEquals("数据库连接异常", exception.getMessage());
        verify(customersMapper, times(1)).selectCustomersByPhone(eq(phone));
    }

    @Test
    @DisplayName("测试检查手机号存在 - 手机号带空格")
    void testCheckPhoneExists_WhenPhoneHasSpaces_ShouldTrimAndCheck() {
        // Given
        String phone = " 13800138000 ";
        when(customersMapper.selectCustomersByPhone(eq("13800138000"))).thenReturn(testCustomer);

        // When
        boolean result = customersService.checkPhoneExists(phone, null);

        // Then
        assertTrue(result, "手机号带空格时应去除空格后检查");
        verify(customersMapper, times(1)).selectCustomersByPhone(eq("13800138000"));
    }

    @Test
    @DisplayName("测试检查手机号存在 - 排除ID带空格")
    void testCheckPhoneExists_WhenExcludeIdHasSpaces_ShouldNotTrimExcludeId() {
        // Given
        String phone = "13800138000";
        String excludeId = " 1234567890 ";
        testCustomer.setId("1234567890");
        when(customersMapper.selectCustomersByPhone(eq(phone))).thenReturn(testCustomer);

        // When
        boolean result = customersService.checkPhoneExists(phone, excludeId);

        // Then
        assertTrue(result, "排除ID带空格时不应该去除空格后比较");
        verify(customersMapper, times(1)).selectCustomersByPhone(eq(phone));
    }
}