# acceptanceTime 字段缺失修复说明

## 问题描述

`/app/projectScheduleRecords/list` 接口没有返回 `acceptanceTime` 字段，导致前端无法获取验收时间信息。

## 问题分析

### 1. 数据库表结构正确

在 `project_schedule_records` 表中，`acceptance_time` 字段是存在的：

```sql
CREATE TABLE `project_schedule_records` (
  -- ... 其他字段
  `acceptance_time` datetime DEFAULT NULL COMMENT '验收时间（实际验收发生时间）',
  -- ... 其他字段
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目进度记录表（包含验收功能）';
```

### 2. VO 类缺少字段

`ProjectScheduleRecordVO` 类中没有定义 `acceptanceTime` 字段。

### 3. Mapper XML 查询缺少字段

在 `AppProjectScheduleMapper.xml` 中：
- `selectProjectScheduleRecordList` 查询没有选择 `acceptance_time` 字段
- `selectProjectScheduleRecordById` 查询没有选择 `acceptance_time` 字段
- `ProjectScheduleRecordVOResult` ResultMap 没有映射 `acceptance_time` 字段

## 修复方案

### 1. 修改 VO 类

在 `ProjectScheduleRecordVO.java` 中添加 `acceptanceTime` 字段：

```java
/** 验收时间（实际验收发生时间） */
private Date acceptanceTime;

public Date getAcceptanceTime() {
    return acceptanceTime;
}

public void setAcceptanceTime(Date acceptanceTime) {
    this.acceptanceTime = acceptanceTime;
}
```

### 2. 修改 Mapper XML 查询

在 `AppProjectScheduleMapper.xml` 中：

#### 修改列表查询
```xml
<select id="selectProjectScheduleRecordList" resultMap="ProjectScheduleRecordVOResult">
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
        psr.acceptance_time  -- 新增字段
    FROM project_schedule_records psr
    LEFT JOIN project_schedules ps ON psr.schedule_id = ps.id
    WHERE psr.project_id = #{projectId}
    <if test="scheduleId != null and scheduleId != ''">
        AND psr.schedule_id = #{scheduleId}
    </if>
    ORDER BY psr.created_at DESC
    LIMIT #{offset}, #{pageSize}
</select>
```

#### 修改详情查询
```xml
<select id="selectProjectScheduleRecordById" parameterType="String" resultMap="ProjectScheduleRecordVOResult">
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
        psr.acceptance_time  -- 新增字段
    FROM project_schedule_records psr
    LEFT JOIN project_schedules ps ON psr.schedule_id = ps.id
    WHERE psr.id = #{recordId}
</select>
```

#### 修改 ResultMap
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
    <result property="createTime" column="create_time"/>
    <result property="createBy" column="create_by"/>
    <result property="createByName" column="create_by_name"/>
    <result property="createByRole" column="create_by_role"/>
    <result property="inspectionStatus" column="inspection_status"/>
    <result property="acceptanceTime" column="acceptance_time"/>  <!-- 新增映射 -->
    <!-- 图片和附件通过单独查询获取 -->
</resultMap>
```

## 修复后的效果

修复后，`/app/projectScheduleRecords/list` 接口将返回包含 `acceptanceTime` 字段的数据：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "rows": [
      {
        "id": "REC2024112900001",
        "scheduleId": "SCH001",
        "projectId": "PRJ001",
        "stage": "FOUNDATION",
        "stageName": "基础施工",
        "title": "基础验收",
        "description": "基础施工完成，质量良好",
        "type": "ACCEPTANCE",
        "typeText": "验收",
        "createTime": "2024-11-29T10:00:00",
        "createBy": "USER001",
        "createByName": "张工程师",
        "createByRole": "staff",
        "inspectionStatus": "PASS",
        "inspectionStatusText": "通过",
        "acceptanceTime": "2024-11-29T14:30:00",  // 新增字段
        "images": ["image1.jpg", "image2.jpg"],
        "attachments": []
      }
    ],
    "total": 1
  }
}
```

## 字段说明

- **acceptanceTime**: 验收时间（实际验收发生的时间）
- **createTime**: 记录创建时间（系统自动生成）

这两个时间字段的区别：
- `createTime`: 记录在系统中创建的时间
- `acceptanceTime`: 实际进行验收的时间（可能与创建时间不同）

## 测试验证

修复后需要验证：

1. **接口返回数据**
   ```bash
   curl -X GET "http://localhost:8080/app/projectScheduleRecords/list" \
        -H "Authorization: Bearer <token>" \
        -H "X-Project-Id: <project_id>"
   ```

2. **检查返回字段**
   - 确认响应中包含 `acceptanceTime` 字段
   - 验证时间格式正确
   - 检查空值处理（如果数据库中为 NULL）

3. **前端显示**
   - 前端页面能正确显示验收时间
   - 时间格式化正确
   - 空值显示友好

## 文件修改清单

- ✅ `sb3/evs-home/src/main/java/com/ruoyi/app/dto/response/ProjectScheduleRecordVO.java`
  - 添加 `acceptanceTime` 字段
  - 添加对应的 getter/setter 方法

- ✅ `sb3/evs-home/src/main/resources/mapper/app/AppProjectScheduleMapper.xml`
  - 修改 `selectProjectScheduleRecordList` 查询，添加 `psr.acceptance_time`
  - 修改 `selectProjectScheduleRecordById` 查询，添加 `psr.acceptance_time`
  - 修改 `ProjectScheduleRecordVOResult` ResultMap，添加字段映射

## 注意事项

1. **数据库兼容性**
   - 确保数据库中 `acceptance_time` 字段存在
   - 如果是老数据，该字段可能为 NULL

2. **时间格式**
   - 数据库存储格式：`DATETIME`
   - Java 对象类型：`java.util.Date`
   - JSON 序列化格式：ISO 8601 格式

3. **业务逻辑**
   - 验收记录创建时，`acceptance_time` 可能为空
   - 实际验收时才设置 `acceptance_time`
   - 前端需要处理空值情况

## 相关接口

此修复影响以下接口：
- `GET /app/projectScheduleRecords/list` - 验收记录列表
- `GET /app/projectScheduleRecords/{recordId}` - 验收记录详情

其他相关接口（如果存在类似问题也需要检查）：
- `POST /app/projectScheduleRecords` - 创建验收记录
- `PUT /app/projectScheduleRecords/{recordId}` - 更新验收记录