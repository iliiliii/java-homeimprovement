# acceptanceTime 字段返回 null 问题深度分析

## 问题现状

- **数据库中有值**：确认 `project_schedule_records.acceptance_time` 字段在数据库中存在且有值
- **接口返回 null**：`/app/projectScheduleRecords/list` 接口返回的 `acceptanceTime` 字段为 null

## 已完成的修复

1. ✅ **VO 类添加字段**：在 `ProjectScheduleRecordVO` 中添加了 `acceptanceTime` 字段
2. ✅ **SQL 查询添加字段**：在所有相关查询中添加了 `psr.acceptance_time`
3. ✅ **ResultMap 添加映射**：添加了 `<result property="acceptanceTime" column="acceptance_time"/>`
4. ✅ **添加调试日志**：在服务层添加了调试日志
5. ✅ **启用 SQL 日志**：启用了 MyBatis 的 SQL 执行日志

## 可能的原因分析

### 1. MyBatis 缓存问题

**可能性**：MyBatis 一级缓存或二级缓存导致旧数据被缓存

**解决方案**：
```xml
<!-- 在 Mapper XML 中添加 -->
<select id="selectProjectScheduleRecordList" resultMap="ProjectScheduleRecordVOResult" useCache="false">
```

### 2. 数据库连接时区问题

**可能性**：MySQL 时区设置导致 DATETIME 字段读取异常

**检查方法**：
```sql
-- 检查数据库时区
SELECT @@global.time_zone, @@session.time_zone;

-- 检查具体数据
SELECT 
    id,
    acceptance_time,
    UNIX_TIMESTAMP(acceptance_time) as timestamp,
    DATE_FORMAT(acceptance_time, '%Y-%m-%d %H:%i:%s') as formatted_time
FROM project_schedule_records 
WHERE acceptance_time IS NOT NULL;
```

**解决方案**：在数据库连接 URL 中添加时区参数
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/evs_home?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=Asia/Shanghai
```

### 3. MyBatis 类型处理器问题

**可能性**：DATETIME 类型到 Java Date 类型的转换问题

**解决方案**：在 ResultMap 中指定类型处理器
```xml
<result property="acceptanceTime" column="acceptance_time" javaType="java.util.Date" jdbcType="TIMESTAMP"/>
```

### 4. 字段别名冲突

**可能性**：SQL 查询中的字段别名与其他字段冲突

**检查方法**：确保 SQL 中没有重复的列别名

### 5. 数据库字段实际为空

**可能性**：虽然说数据库中有值，但实际查询的记录中该字段为空

**验证方法**：
```sql
-- 使用与 Mapper 完全相同的查询
SELECT 
    psr.id,
    psr.schedule_id,
    psr.project_id,
    ps.stage,
    ps.stage as stage_name,
    psr.acceptance_title as title,
    psr.acceptance_content as description,
    psr.record_type as type,
    psr.created_at as create_time,
    psr.created_by as create_by,
    psr.acceptor as create_by_name,
    'staff' as create_by_role,
    psr.acceptance_result as inspection_status,
    psr.acceptance_time,
    -- 额外的调试字段
    psr.acceptance_time IS NULL as is_null,
    LENGTH(psr.acceptance_time) as time_length
FROM project_schedule_records psr
LEFT JOIN project_schedules ps ON psr.schedule_id = ps.id
WHERE psr.project_id = 'YOUR_PROJECT_ID'  -- 替换为实际项目ID
ORDER BY psr.created_at DESC
LIMIT 10;
```

## 调试步骤

### 步骤 1：启用详细日志

在 `application.yml` 中添加：
```yaml
logging:
  level:
    com.ruoyi.app.mapper: debug
    org.apache.ibatis: debug
    java.sql: debug
```

### 步骤 2：添加调试代码

在 `AppProjectScheduleServiceImpl.java` 中：
```java
// 查询验收记录列表
List<ProjectScheduleRecordVO> records = projectScheduleMapper.selectProjectScheduleRecordList(
        projectId, scheduleId, offset, pageSize);

log.info("查询到 {} 条验收记录", records.size());

// 为每条记录设置类型文本和验收状态文本
for (ProjectScheduleRecordVO record : records) {
    // 调试日志：检查 acceptanceTime 字段
    log.info("记录 {} 的 acceptanceTime: {}", record.getId(), record.getAcceptanceTime());
    log.info("记录 {} 的 createTime: {}", record.getId(), record.getCreateTime());
    
    // ... 其他处理
}
```

### 步骤 3：直接数据库验证

执行以下 SQL 确认数据：
```sql
-- 检查表结构
SHOW CREATE TABLE project_schedule_records;

-- 检查数据
SELECT * FROM project_schedule_records WHERE acceptance_time IS NOT NULL LIMIT 5;
```

### 步骤 4：MyBatis 测试

创建一个简单的测试查询：
```xml
<select id="testAcceptanceTime" resultType="java.util.Date">
    SELECT acceptance_time FROM project_schedule_records WHERE id = #{recordId}
</select>
```

## 立即修复方案

### 方案 1：修改 ResultMap（推荐）

```xml
<resultMap id="ProjectScheduleRecordVOResult" type="com.ruoyi.app.dto.response.ProjectScheduleRecordVO">
    <id property="id" column="id"/>
    <result property="scheduleId" column="schedule_id"/>
    <result property="projectId" column="project_id"/>
    <result property="stage" column="stage"/>
    <result property="stageName" column="stage_name"/>
    <result property="title" column="title"/>
    <result property="description" column="description"/>
    <result property="type" column="type"/>
    <result property="createTime" column="create_time" javaType="java.util.Date" jdbcType="TIMESTAMP"/>
    <result property="createBy" column="create_by"/>
    <result property="createByName" column="create_by_name"/>
    <result property="createByRole" column="create_by_role"/>
    <result property="inspectionStatus" column="inspection_status"/>
    <result property="acceptanceTime" column="acceptance_time" javaType="java.util.Date" jdbcType="TIMESTAMP"/>
    <!-- 图片和附件通过单独查询获取 -->
</resultMap>
```

### 方案 2：启用驼峰命名转换

在 `mybatis-config.xml` 中：
```xml
<settings>
    <!-- 使用驼峰命名法转换字段 -->
    <setting name="mapUnderscoreToCamelCase" value="true"/>
</settings>
```

### 方案 3：添加字段别名

在 SQL 查询中：
```sql
SELECT 
    -- ... 其他字段
    psr.acceptance_time as acceptanceTime  -- 使用驼峰命名别名
FROM project_schedule_records psr
-- ... 其他部分
```

## 验证修复

修复后，通过以下方式验证：

1. **查看日志**：检查 SQL 执行日志和调试日志
2. **API 测试**：调用接口查看返回结果
3. **数据对比**：对比数据库数据和接口返回数据

## 预期结果

修复后，接口应该返回：
```json
{
  "code": 200,
  "data": {
    "rows": [
      {
        "id": "REC001",
        "acceptanceTime": "2024-11-29T14:30:00",  // 不再是 null
        "createTime": "2024-11-29T10:00:00",
        // ... 其他字段
      }
    ]
  }
}
```

## 文件修改清单

- ✅ `sb3/evs-home/src/main/java/com/ruoyi/app/dto/response/ProjectScheduleRecordVO.java`
- ✅ `sb3/evs-home/src/main/resources/mapper/app/AppProjectScheduleMapper.xml`
- ✅ `sb3/evs-home/src/main/java/com/ruoyi/app/service/impl/AppProjectScheduleServiceImpl.java`
- ✅ `sb3/ruoyi-admin/src/main/resources/application.yml`
- 📝 `sb3/debug_acceptance_time.sql` - 调试 SQL
- 📝 `sb3/acceptanceTime字段返回null问题深度分析.md` - 本文档

## 下一步行动

1. **重启应用**：重启 Spring Boot 应用以应用配置更改
2. **执行调试 SQL**：运行 `debug_acceptance_time.sql` 中的查询
3. **查看日志**：观察应用启动和 API 调用的日志
4. **测试接口**：调用 `/app/projectScheduleRecords/list` 接口
5. **分析结果**：根据日志和返回结果确定具体原因

如果以上方案都不能解决问题，可能需要：
- 检查 Spring Boot 版本和 MyBatis 版本兼容性
- 检查数据库驱动版本
- 考虑使用 MyBatis-Plus 或其他 ORM 框架