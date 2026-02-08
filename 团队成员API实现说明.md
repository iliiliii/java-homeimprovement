# 团队成员API实现说明

## 概述
为小程序品牌页面（`uni3/src/pages/brand/index.vue`）创建了团队成员数据API，用于从后端获取团队成员信息。

## 后端实现

### 1. 数据传输对象（DTO）
**文件**: `sb3/evs-home/src/main/java/com/ruoyi/app/dto/response/TeamMemberVO.java`
- 包含字段：name（姓名）、post（岗位）、avatar（头像）

### 2. Mapper层
**文件**: `sb3/evs-home/src/main/java/com/ruoyi/app/mapper/AppUserMapper.java`
- 新增方法：`selectTeamMembersWithPost()`
- 查询逻辑：
  - 关联查询 `sys_user`、`sys_user_post`、`sys_post` 三张表
  - 使用 `LEFT JOIN` 关联用户岗位信息
  - 使用 `GROUP_CONCAT` 合并多个岗位名称（用"、"分隔）
- 查询条件：
  - email IS NOT NULL AND email != '' （邮箱不为空）
  - del_flag = '0' （用户未删除）
  - status = '0' （用户未停用）
  - 岗位表 status = '0' （岗位未停用）
- 排序：按用户创建时间升序，岗位按 post_sort 排序

### 3. Service层
**接口**: `sb3/evs-home/src/main/java/com/ruoyi/app/service/IAppTeamService.java`
**实现**: `sb3/evs-home/src/main/java/com/ruoyi/app/service/impl/AppTeamServiceImpl.java`
- 方法：`getTeamMembers()`
- 功能：查询符合条件的用户（包含岗位信息）并转换为VO对象
- 数据映射：
  - name: 优先使用nickName（昵称），否则使用userName（用户名）
  - post: 使用关联查询得到的岗位名称（postNames），如果用户没有岗位则显示"团队成员"
  - avatar: 直接使用avatar字段

### 4. Controller层
**文件**: `sb3/evs-home/src/main/java/com/ruoyi/app/controller/AppTeamController.java`
- 路径：`/app/team`
- 接口：`GET /app/team/members`
- 返回：团队成员列表（无需认证）

## 前端实现

### 1. API封装
**文件**: `uni3/src/api/team.js`
- 方法：`getTeamMembers()`
- 调用：`GET /app/team/members`

### 2. 页面集成
**文件**: `uni3/src/pages/brand/index.vue`
- 在 `onMounted` 生命周期中调用API
- 成功时使用后端数据
- 失败或无数据时使用默认数据（保证页面正常显示）
- 显示字段：name（姓名）、post（岗位）、avatar（头像）

## API使用示例

### 请求
```
GET /app/team/members
```

### 响应
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "name": "张三",
      "post": "首席设计师",
      "avatar": "/profile/avatar/2024/12/03/xxx.jpg"
    },
    {
      "name": "李四",
      "post": "项目经理、质检员",
      "avatar": "/profile/avatar/2024/12/03/yyy.jpg"
    }
  ]
}
```

## 数据来源
- 主表：`sys_user` （用户表）
- 关联表：`sys_user_post` （用户岗位关联表）
- 关联表：`sys_post` （岗位表）
- 过滤条件确保只显示有效的团队成员
- 如果用户关联了多个岗位，会用"、"连接显示（如："项目经理、质检员"）

## 注意事项
1. 该接口无需认证，可公开访问
2. 前端有降级处理，确保在API失败时页面仍能正常显示
3. 建议在后台用户管理中为团队成员：
   - 填写邮箱（必填，用于筛选团队成员）
   - 填写昵称（显示为姓名）
   - 关联岗位（显示为岗位信息）
   - 上传头像（显示为头像）
4. 用户可以关联多个岗位，会自动合并显示
5. 只显示状态正常（未停用）的岗位
