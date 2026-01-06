# statusText 显示修复说明

## 问题分析
`statusText` 显示不正确的原因可能是：
1. 数据库中缺少对应的字典数据
2. 字典类型名称不匹配
3. 字典查询失败

## 解决方案

### 1. 添加字典数据
执行 `insert_dict_data.sql` 脚本，插入必要的字典数据：

#### 字典类型
- `decoration_construction_stage` - 装修施工阶段
- `decoration_construction_stage_status` - 施工阶段状态  
- `decoration_record_type` - 进度记录类型

#### 状态字典数据
- `PENDING` → `待开始`
- `IN_PROGRESS` → `进行中`
- `COMPLETED` → `已完成`

### 2. 添加兜底机制
为了确保即使字典查询失败也能正常显示，添加了硬编码的兜底方案：

```java
private String getStatusText(String status) {
    // 先尝试从字典表查询
    String dictLabel = getDictLabel(DICT_TYPE_STAGE_STATUS, status);
    
    // 如果字典查询返回原值（说明没找到），使用硬编码兜底
    if (dictLabel.equals(status)) {
        switch (status) {
            case "PENDING": return "待开始";
            case "IN_PROGRESS": return "进行中";
            case "COMPLETED": return "已完成";
            default: return status;
        }
    }
    return dictLabel;
}
```

### 3. 添加调试日志
在 `getDictLabel` 方法中添加了调试日志，方便排查问题：

```java
log.debug("字典查询 - 类型: {}, 值: {}, 标签: {}", dictType, dictValue, label);
```

## 执行步骤

### 1. 插入字典数据
在数据库中执行 `insert_dict_data.sql` 脚本：
```sql
-- 插入字典类型和字典数据
-- 包含施工阶段、状态、记录类型的完整字典
```

### 2. 重启服务
重新编译并重启后端服务，使修改生效。

### 3. 测试验证
调用API测试，检查返回的数据：
```json
{
  "stage": "WATER_ELECTRIC",
  "stageName": "水电改造",
  "status": "IN_PROGRESS", 
  "statusText": "进行中"
}
```

## 预期结果
- `statusText` 正确显示中文状态文本
- `stageName` 正确显示中文阶段名称
- `typeText` 正确显示中文记录类型
- 即使字典查询失败，也有兜底显示

## 调试方法
如果问题仍然存在：
1. 查看后端日志中的字典查询调试信息
2. 检查数据库中是否存在对应的字典数据
3. 确认字典类型名称是否正确
4. 验证 `sys_dict_type` 和 `sys_dict_data` 表的数据