# MyBatis Mapper XML修复报告

## 🐛 错误信息

```
Error updating database. Cause: org.apache.ibatis.reflection.ReflectionException:
There is no getter for property named 'createdAt' in 'class com.ruoyi.web.domain.ProjectScheduleRecords'
```

## 🔍 问题根本原因

Mapper XML文件中使用了**错误的方法名**：

| XML中错误的方法名 | BaseEntity中的正确方法名 |
|-----------------|------------------------|
| `createdAt` ❌ | `createTime` ✅ |
| `updatedAt` ❌ | `updateTime` ✅ |
| `createdBy` ❌ | `createBy` ✅ |
| `updatedBy` ❌ | `updateBy` ✅ |

## ✅ 修复方案

### 修复位置1：resultMap (第18-22行)

```xml
<!-- 修改前 -->
<result property="createdAt"    column="created_at"    />
<result property="updatedAt"    column="updated_at"    />
<result property="createdBy"    column="created_by"    />
<result property="updatedBy"    column="updated_by"    />

<!-- 修改后 ✅ -->
<!-- ✅ 审计字段：使用BaseEntity中的属性名 -->
<result property="createTime"    column="created_at"    />
<result property="updateTime"    column="updated_at"    />
<result property="createBy"    column="created_by"    />
<result property="updateBy"    column="updated_by"    />
```

### 修复位置2：INSERT语句 (第59-62行，76-79行)

```xml
<!-- INSERT字段定义：修改前 -->
<if test="createdAt != null">created_at,</if>
<if test="updatedAt != null">updated_at,</if>
<if test="createdBy != null">created_by,</if>
<if test="updatedBy != null">updated_by,</if>

<!-- INSERT字段定义：修改后 ✅ -->
<!-- ✅ 审计字段：使用BaseEntity中的属性名 -->
<if test="createTime != null">created_at,</if>
<if test="updateTime != null">updated_at,</if>
<if test="createBy != null">created_by,</if>
<if test="updateBy != null">updated_by,</if>

<!-- INSERT值定义：修改前 -->
<if test="createdAt != null">#{createdAt},</if>
<if test="updatedAt != null">#{updatedAt},</if>
<if test="createdBy != null">#{createdBy},</if>
<if test="updatedBy != null">#{updatedBy},</if>

<!-- INSERT值定义：修改后 ✅ -->
<!-- ✅ 审计字段：使用BaseEntity中的属性名 -->
<if test="createTime != null">#{createTime},</if>
<if test="updateTime != null">#{updateTime},</if>
<if test="createBy != null">#{createBy},</if>
<if test="updateBy != null">#{updateBy},</if>
```

### 修复位置3：UPDATE语句 (第96-99行)

```xml
<!-- 修改前 -->
<if test="createdAt != null">created_at = #{createdAt},</if>
<if test="updatedAt != null">updated_at = #{updatedAt},</if>
<if test="createdBy != null">created_by = #{createdBy},</if>
<if test="updatedBy != null">updated_by = #{updatedBy},</if>

<!-- 修改后 ✅ -->
<!-- ✅ 审计字段：使用BaseEntity中的属性名 -->
<if test="createTime != null">created_at = #{createTime},</if>
<if test="updateTime != null">updated_at = #{updateTime},</if>
<if test="createBy != null">created_by = #{createBy},</if>
<if test="updateBy != null">updated_by = #{updateBy},</if>
```

## 📊 MyBatis属性名规范

### BaseEntity字段与数据库字段映射

| Java属性名 | 数据库字段名 | 说明 |
|------------|--------------|------|
| `createTime` | `created_at` | 创建时间 |
| `updateTime` | `updated_at` | 更新时间 |
| `createBy` | `created_by` | 创建者 |
| `updateBy` | `updated_by` | 更新者 |

### MyBatis中正确的使用方式

```xml
<!-- 结果映射：property是Java属性名，column是数据库字段名 -->
<result property="createTime" column="created_at" />

<!-- SQL查询：使用Java属性名 -->
WHERE create_time > #{createTime}

<!-- INSERT：使用Java属性名 -->
<if test="createTime != null">created_at = #{createTime},</if>

<!-- UPDATE：使用Java属性名 -->
<if test="updateTime != null">updated_at = #{updateTime},</if>
```

## 🎯 修复验证

### 修复后的数据流

```
实体类 (ProjectScheduleRecords)
    ↓ extends BaseEntity
属性: createTime, updateTime, createBy, updateBy
    ↓
Service层 (ProjectScheduleRecordsServiceImpl)
调用: setCreateTime(), setUpdateTime()
    ↓
Mapper XML (ProjectScheduleRecordsMapper.xml)
映射: property="createTime" → column="created_at"
    ↓
数据库 (project_schedule_records)
字段: created_at, updated_at, created_by, updated_by
```

### 测试场景

1. **插入验收记录**
   - Service调用 `setCreateTime()` 设置时间
   - Mapper XML检测 `createTime != null`
   - 执行SQL: `INSERT INTO ... created_at = #{createTime}`
   - **预期**：数据成功插入数据库

2. **更新验收记录**
   - Service调用 `setUpdateTime()` 设置时间
   - Mapper XML检测 `updateTime != null`
   - 执行SQL: `UPDATE ... updated_at = #{updateTime}`
   - **预期**：数据成功更新

3. **查询验收记录**
   - Mapper XML将 `created_at` 映射到 `createTime` 属性
   - 查询结果正确封装到实体类
   - **预期**：实体类中的createTime属性有值

## 📝 经验教训

### 1. 属性名命名规范
- **Java实体类**：使用驼峰命名法，如 `createTime`
- **数据库字段**：使用下划线命名法，如 `created_at`
- **MyBatis映射**：`property="createTime"` → `column="created_at"`

### 2. BaseEntity字段继承
- 继承BaseEntity的类会自动获得审计字段
- 必须使用BaseEntity中定义的属性名
- 不要自定义同名属性

### 3. XML配置检查
- 修改实体类后需要同步修改：
  - Mapper XML文件
  - ServiceImpl中的setter/getter调用
- 确保三者（实体类、Service、XML）属性名完全一致

---

**修复时间**：2025-11-29
**修复文件**：`sb3/evs-home/src/main/resources/mapper/evs/ProjectScheduleRecordsMapper.xml`
**修复状态**：✅ 完成
**修复点数量**：4个（resultMap、INSERT字段、INSERT值、UPDATE）
**下一步**：重新编译并测试验收上报功能
