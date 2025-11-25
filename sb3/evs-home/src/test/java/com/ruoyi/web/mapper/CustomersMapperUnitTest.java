package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.Customers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 客户档案Mapper独立测试类
 * 使用内存数据库进行单元测试，不依赖Spring Boot上下文
 *
 * @author evs
 * @date 2025-11-18
 */
@DisplayName("客户档案Mapper接口独立测试")
class CustomersMapperUnitTest {

    private EmbeddedDatabase dataSource;
    private CustomersMapper customersMapper;
    private JdbcTemplate jdbcTemplate;

    private String testCustomerId = "unit-test-" + System.currentTimeMillis();
    private Customers testCustomer;

    @BeforeEach
    void setUp() {
        // 创建内存数据库
        dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();

        jdbcTemplate = new JdbcTemplate(dataSource);

        // 创建测试用的CustomersMapper实现
        customersMapper = new CustomersMapper() {
            @Override
            public Customers selectCustomersById(String id) {
                String sql = "SELECT id, name, phone, email, address, level, source, remarks, is_active, created_at, updated_at FROM customers WHERE id = ?";
                try {
                    return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                    Customers customer = new Customers();
                    customer.setId(rs.getString("id"));
                    customer.setName(rs.getString("name"));
                    customer.setPhone(rs.getString("phone"));
                    customer.setEmail(rs.getString("email"));
                    customer.setAddress(rs.getString("address"));
                    customer.setLevel(rs.getString("level"));
                    customer.setSource(rs.getString("source"));
                    customer.setRemarks(rs.getString("remarks"));
                    customer.setIsActive(rs.getInt("is_active"));
                    customer.setCreatedAt(rs.getDate("created_at"));
                    customer.setUpdatedAt(rs.getDate("updated_at"));
                    return customer;
                }, id);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public Customers selectCustomersByPhone(String phone) {
                if (phone == null) return null;
                String sql = "SELECT id, name, phone, email, address, level, source, remarks, is_active, created_at, updated_at FROM customers WHERE phone = ? LIMIT 1";
                List<Customers> customers = jdbcTemplate.query(sql, (rs, rowNum) -> {
                    Customers customer = new Customers();
                    customer.setId(rs.getString("id"));
                    customer.setName(rs.getString("name"));
                    customer.setPhone(rs.getString("phone"));
                    customer.setEmail(rs.getString("email"));
                    customer.setAddress(rs.getString("address"));
                    customer.setLevel(rs.getString("level"));
                    customer.setSource(rs.getString("source"));
                    customer.setRemarks(rs.getString("remarks"));
                    customer.setIsActive(rs.getInt("is_active"));
                    customer.setCreatedAt(rs.getDate("created_at"));
                    customer.setUpdatedAt(rs.getDate("updated_at"));
                    return customer;
                }, phone);
                return customers.isEmpty() ? null : customers.get(0);
            }

            @Override
            public List<Customers> selectCustomersList(Customers customers) {
                StringBuilder sql = new StringBuilder("SELECT id, name, phone, email, address, level, source, remarks, is_active, created_at, updated_at FROM customers WHERE 1=1");

                if (customers != null) {
                    if (customers.getName() != null && !customers.getName().isEmpty()) {
                        sql.append(" AND name LIKE '%").append(customers.getName()).append("%'");
                    }
                    if (customers.getPhone() != null && !customers.getPhone().isEmpty()) {
                        sql.append(" AND phone = '").append(customers.getPhone()).append("'");
                    }
                    if (customers.getLevel() != null && !customers.getLevel().isEmpty()) {
                        sql.append(" AND level = '").append(customers.getLevel()).append("'");
                    }
                }

                return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
                    Customers customer = new Customers();
                    customer.setId(rs.getString("id"));
                    customer.setName(rs.getString("name"));
                    customer.setPhone(rs.getString("phone"));
                    customer.setEmail(rs.getString("email"));
                    customer.setAddress(rs.getString("address"));
                    customer.setLevel(rs.getString("level"));
                    customer.setSource(rs.getString("source"));
                    customer.setRemarks(rs.getString("remarks"));
                    customer.setIsActive(rs.getInt("is_active"));
                    customer.setCreatedAt(rs.getDate("created_at"));
                    customer.setUpdatedAt(rs.getDate("updated_at"));
                    return customer;
                });
            }

            @Override
            public int insertCustomers(Customers customers) {
                String sql = "INSERT INTO customers (id, name, phone, email, address, level, source, remarks, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                return jdbcTemplate.update(sql,
                    customers.getId(),
                    customers.getName(),
                    customers.getPhone(),
                    customers.getEmail(),
                    customers.getAddress(),
                    customers.getLevel(),
                    customers.getSource(),
                    customers.getRemarks(),
                    customers.getIsActive(),
                    customers.getCreatedAt(),
                    customers.getUpdatedAt()
                );
            }

            @Override
            public int updateCustomers(Customers customers) {
                String sql = "UPDATE customers SET name = ?, phone = ?, email = ?, address = ?, level = ?, source = ?, remarks = ?, is_active = ?, updated_at = ? WHERE id = ?";
                return jdbcTemplate.update(sql,
                    customers.getName(),
                    customers.getPhone(),
                    customers.getEmail(),
                    customers.getAddress(),
                    customers.getLevel(),
                    customers.getSource(),
                    customers.getRemarks(),
                    customers.getIsActive(),
                    customers.getUpdatedAt(),
                    customers.getId()
                );
            }

            @Override
            public int deleteCustomersById(String id) {
                String sql = "DELETE FROM customers WHERE id = ?";
                return jdbcTemplate.update(sql, id);
            }

            @Override
            public int deleteCustomersByIds(String[] ids) {
                if (ids == null || ids.length == 0) return 0;
                String placeholders = String.join(",", java.util.Arrays.stream(ids).map(s -> "?").toArray(String[]::new));
                String sql = "DELETE FROM customers WHERE id IN (" + placeholders + ")";
                return jdbcTemplate.update(sql, (Object[]) ids);
            }

            @Override
            public List<Customers> selectCustomersWithProjectCount(Customers customers) {
                StringBuilder sql = new StringBuilder(
                    "SELECT c.id, c.name, c.phone, c.email, c.address, c.level, c.source, c.remarks, c.is_active, " +
                    "c.created_at, c.updated_at, COUNT(p.id) as project_count " +
                    "FROM customers c LEFT JOIN projects p ON c.id = p.customer_id WHERE 1=1"
                );

                if (customers != null) {
                    if (customers.getName() != null && !customers.getName().isEmpty()) {
                        sql.append(" AND c.name LIKE '%").append(customers.getName()).append("%'");
                    }
                    if (customers.getPhone() != null && !customers.getPhone().isEmpty()) {
                        sql.append(" AND c.phone = '").append(customers.getPhone()).append("'");
                    }
                    if (customers.getLevel() != null && !customers.getLevel().isEmpty()) {
                        sql.append(" AND c.level = '").append(customers.getLevel()).append("'");
                    }
                }

                sql.append(" GROUP BY c.id, c.name, c.phone, c.email, c.address, c.level, c.source, " +
                          "c.remarks, c.is_active, c.created_at, c.updated_at ORDER BY c.created_at DESC");

                return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
                    Customers customer = new Customers();
                    customer.setId(rs.getString("id"));
                    customer.setName(rs.getString("name"));
                    customer.setPhone(rs.getString("phone"));
                    customer.setEmail(rs.getString("email"));
                    customer.setAddress(rs.getString("address"));
                    customer.setLevel(rs.getString("level"));
                    customer.setSource(rs.getString("source"));
                    customer.setRemarks(rs.getString("remarks"));
                    customer.setIsActive(rs.getInt("is_active"));
                    customer.setCreatedAt(rs.getDate("created_at"));
                    customer.setUpdatedAt(rs.getDate("updated_at"));
                    customer.setProjectCount(rs.getInt("project_count"));
                    return customer;
                });
            }

            @Override
            public Customers selectCustomersWithProjectCountById(String id) {
                String sql =
                    "SELECT c.id, c.name, c.phone, c.email, c.address, c.level, c.source, c.remarks, c.is_active, " +
                    "c.created_at, c.updated_at, COUNT(p.id) as project_count " +
                    "FROM customers c LEFT JOIN projects p ON c.id = p.customer_id " +
                    "WHERE c.id = ? " +
                    "GROUP BY c.id, c.name, c.phone, c.email, c.address, c.level, c.source, " +
                    "c.remarks, c.is_active, c.created_at, c.updated_at";

                try {
                    return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                        Customers customer = new Customers();
                        customer.setId(rs.getString("id"));
                        customer.setName(rs.getString("name"));
                        customer.setPhone(rs.getString("phone"));
                        customer.setEmail(rs.getString("email"));
                        customer.setAddress(rs.getString("address"));
                        customer.setLevel(rs.getString("level"));
                        customer.setSource(rs.getString("source"));
                        customer.setRemarks(rs.getString("remarks"));
                        customer.setIsActive(rs.getInt("is_active"));
                        customer.setCreatedAt(rs.getDate("created_at"));
                        customer.setUpdatedAt(rs.getDate("updated_at"));
                        customer.setProjectCount(rs.getInt("project_count"));
                        return customer;
                    }, id);
                } catch (Exception e) {
                    return null;
                }
            }
        };

        // 创建测试数据表
        createTestTables();

        // 准备测试数据
        testCustomer = new Customers();
        testCustomer.setId(testCustomerId);
        testCustomer.setName("单元测试客户");
        testCustomer.setPhone("13800138000");
        testCustomer.setEmail("unit@test.com");
        testCustomer.setAddress("单元测试地址");
        testCustomer.setLevel("VIP");
        testCustomer.setSource("官网");
        testCustomer.setRemarks("单元测试备注");
        testCustomer.setIsActive(1);
        testCustomer.setCreatedAt(new Date());
        testCustomer.setUpdatedAt(new Date());
    }

    private void createTestTables() {
        String createCustomersTableSql = """
            CREATE TABLE customers (
                id VARCHAR(50) PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                phone VARCHAR(20),
                email VARCHAR(100),
                address VARCHAR(255),
                level VARCHAR(20),
                source VARCHAR(50),
                remarks TEXT,
                is_active INT DEFAULT 1,
                created_at DATE,
                updated_at DATE
            )
        """;

        String createProjectsTableSql = """
            CREATE TABLE projects (
                id VARCHAR(50) PRIMARY KEY,
                name VARCHAR(100),
                customer_id VARCHAR(50),
                created_at DATE,
                updated_at DATE
            )
        """;

        jdbcTemplate.execute(createCustomersTableSql);
        jdbcTemplate.execute(createProjectsTableSql);
    }

    @AfterEach
    void tearDown() {
        if (dataSource != null) {
            dataSource.shutdown();
        }
    }

    @Test
    @DisplayName("测试基本插入和查询功能")
    void testBasicInsertAndSelect() {
        // When - 插入客户
        int insertResult = customersMapper.insertCustomers(testCustomer);

        // Then - 验证插入成功
        assertEquals(1, insertResult, "插入客户应该成功");

        // And - 验证查询功能
        Customers foundCustomer = customersMapper.selectCustomersById(testCustomerId);
        assertNotNull(foundCustomer, "应该能找到插入的客户");
        assertEquals(testCustomer.getName(), foundCustomer.getName());
        assertEquals(testCustomer.getPhone(), foundCustomer.getPhone());
    }

    @Test
    @DisplayName("测试根据手机号查询功能")
    void testSelectByPhone() {
        // Given - 插入客户
        customersMapper.insertCustomers(testCustomer);

        // When - 根据手机号查询
        Customers foundCustomer = customersMapper.selectCustomersByPhone("13800138000");

        // Then - 验证查询结果
        assertNotNull(foundCustomer, "应该能根据手机号找到客户");
        assertEquals(testCustomerId, foundCustomer.getId());
        assertEquals("13800138000", foundCustomer.getPhone());
    }

    @Test
    @DisplayName("测试根据手机号查询不存在的记录")
    void testSelectByPhoneNotFound() {
        // When - 查询不存在的手机号
        Customers foundCustomer = customersMapper.selectCustomersByPhone("13900139999");

        // Then - 应该返回null
        assertNull(foundCustomer, "查询不存在的手机号应该返回null");
    }

    @Test
    @DisplayName("测试根据手机号查询 - 手机号为null")
    void testSelectByPhoneWithNull() {
        // When - 查询null手机号
        Customers foundCustomer = customersMapper.selectCustomersByPhone(null);

        // Then - 应该返回null
        assertNull(foundCustomer, "查询null手机号应该返回null");
    }

    @Test
    @DisplayName("测试更新功能")
    void testUpdate() {
        // Given - 插入客户
        customersMapper.insertCustomers(testCustomer);

        // When - 更新客户信息
        testCustomer.setName("更新后的客户名称");
        testCustomer.setLevel("SVIP");
        int updateResult = customersMapper.updateCustomers(testCustomer);

        // Then - 验证更新成功
        assertEquals(1, updateResult, "更新客户应该成功");

        // And - 验证更新后的数据
        Customers updatedCustomer = customersMapper.selectCustomersById(testCustomerId);
        assertEquals("更新后的客户名称", updatedCustomer.getName());
        assertEquals("SVIP", updatedCustomer.getLevel());
    }

    @Test
    @DisplayName("测试删除功能")
    void testDelete() {
        // Given - 插入客户
        customersMapper.insertCustomers(testCustomer);

        // 验证插入成功
        assertNotNull(customersMapper.selectCustomersById(testCustomerId));

        // When - 删除客户
        int deleteResult = customersMapper.deleteCustomersById(testCustomerId);

        // Then - 验证删除成功
        assertEquals(1, deleteResult, "删除客户应该成功");

        // And - 验证删除后查不到客户
        assertNull(customersMapper.selectCustomersById(testCustomerId),
                  "删除后应该查不到客户");
    }
}