# 智享家Pro - 多角色小程序实现任务列表

> 基于 design-final.md 设计方案的实现任务

## 任务说明

- `[ ]` 表示未开始的任务
- `[x]` 表示已完成的任务
- `*` 后缀表示可选任务（测试相关）
- 每个任务包含需求引用和实现说明

---

## 第一阶段：基础设施搭建

### 1. 后端基础设施

- [ ] 1.1 创建小程序API包结构
  - 创建 `com.ruoyi.app` 包及子包（controller、dto、service、security、enums）
  - 创建基础的 DTO 类（AppLoginRequest、AppLoginResponse等）
  - 创建枚举类（UserTypeEnum）
  - _需求：架构设计_

- [ ] 1.2 配置小程序字典数据
  - 插入 app_config 字典类型和数据（Token有效期、验证码有效期等）
  - 插入 issue_type 字典类型和数据（安全、质量、进度、其他）
  - 插入 severity_level 字典类型和数据（低、中、高、紧急）
  - 实现 AppConfigUtil 配置读取工具类
  - _需求：字典配置设计_

- [ ] 1.3 创建数据库新增表
  - 创建 app_login_logs 表（登录日志）
  - 创建 app_tokens 表（Token管理）
  - 创建 app_sms_codes 表（短信验证码）
  - 创建 app_wechat_bindings 表（微信绑定）
  - 创建 app_audit_logs 表（审计日志）
  - _需求：数据库设计_

- [ ] 1.4 实现JWT Token管理
  - 实现 AppTokenManager 类（生成、验证、刷新、撤销Token）
  - 使用 RS256 算法
  - Token包含 userType、userId、projectIds 等信息
  - 支持 Access Token 和 Refresh Token
  - _需求：Token设计_

- [ ] 1.5 实现短信验证码服务
  - 实现 SmsService 类（发送验证码、验证验证码）
  - 验证码6位数字，有效期5分钟
  - 频率限制：1次/分钟，10次/小时
  - 保存到 app_sms_codes 表
  - _需求：短信验证码登录_

- [ ] 1.6 实现微信登录服务
  - 实现 WechatService 类（code换取openId、解密手机号）
  - 查询和创建 app_wechat_bindings 记录
  - 支持首次登录绑定手机号
  - _需求：微信登录_

- [ ] 1.7 实现认证拦截器
  - 实现 AppAuthInterceptor（Token验证、用户信息提取）
  - 实现 AppContext（ThreadLocal存储用户信息）
  - 验证 Token 有效性
  - 解析用户类型和项目列表
  - 将信息存入 ThreadLocal
  - _需求：权限控制_

- [ ] 1.8 实现角色权限拦截器
  - 实现 AppRoleInterceptor（员工专属功能拦截）
  - 实现 @RequireStaff 注解
  - 拦截客户访问员工专属接口
  - _需求：功能权限控制_

- [ ] 1.9 配置Spring Security
  - 放行 /app/* 路径
  - 配置拦截器顺序
  - 配置CORS跨域
  - _需求：安全配置_

- [ ] 1.10 实现审计日志切面
  - 实现 AppAuditAspect（AOP记录操作日志）
  - 记录请求URL、方法、参数、响应码、执行时间
  - 保存到 app_audit_logs 表
  - _需求：审计日志_



### 2. 认证模块实现

- [ ] 2.1 实现AppAuthController
  - 实现微信登录接口（/app/auth/wechat-login）
  - 实现短信登录接口（/app/auth/sms-login）
  - 实现密码登录接口（/app/auth/password-login）
  - 实现发送验证码接口（/app/auth/send-code）
  - 实现刷新Token接口（/app/auth/refresh-token）
  - 实现退出登录接口（/app/auth/logout）
  - _需求：认证模块API设计_

- [ ] 2.2 实现AppAuthService
  - 实现用户查询逻辑（先查customers，再查sys_user）
  - 实现项目列表查询（客户：customer_id，员工：project_members）
  - 实现登录日志记录
  - 实现Token生成和保存
  - _需求：认证业务逻辑_

- [ ]* 2.3 编写认证模块单元测试
  - 测试微信登录流程
  - 测试短信登录流程
  - 测试密码登录流程
  - 测试Token刷新
  - 测试权限验证
  - _需求：测试策略_

### 3. 前端基础设施

- [ ] 3.1 完善请求封装
  - 完善 request.js（Token管理、自动刷新、错误处理）
  - 实现请求拦截器（添加Token、项目ID）
  - 实现响应拦截器（处理401、业务错误）
  - 实现Token自动刷新机制
  - _需求：前端架构设计_

- [ ] 3.2 实现设备ID生成
  - 生成设备唯一标识
  - 存储到本地存储
  - 登录时携带设备ID
  - _需求：设备绑定_

- [ ] 3.3 创建API配置文件
  - 创建 config/api.js（环境配置）
  - 支持开发环境和生产环境切换
  - 配置API基础URL
  - _需求：API配置_

- [ ] 3.4 实现用户状态管理
  - 实现 stores/user.js
  - 管理 token、userType、userInfo、projects
  - 实现登录、退出登录方法
  - 实现状态持久化
  - _需求：状态管理设计_

- [ ] 3.5 实现项目状态管理
  - 实现 stores/project.js
  - 管理 currentProjectId、currentProject
  - 实现项目切换方法
  - _需求：状态管理设计_

- [ ] 3.6 实现权限判断工具
  - 实现 utils/permission.js
  - 实现 isStaff()、isCustomer() 方法
  - 实现 hasPagePermission() 方法
  - _需求：权限控制_

- [ ] 3.7 实现路由守卫
  - 在 main.js 中添加路由拦截器
  - 拦截客户访问员工专属页面
  - 显示友好的提示信息
  - _需求：权限控制_

- [ ] 3.8 实现登录页面
  - 实现 pages/login/index.vue
  - 支持微信登录（优先）
  - 支持短信验证码登录
  - 支持密码登录
  - 实现登录状态持久化
  - _需求：登录方式设计_

---

## 第二阶段：通用功能开发

### 4. 项目模块

- [ ] 4.1 实现AppProjectController
  - 实现获取项目列表接口（/app/project/list）
  - 实现获取项目详情接口（/app/project/{id}）
  - 实现获取项目统计接口（/app/project/{id}/statistics）
  - 根据用户类型查询项目（客户/员工）
  - _需求：项目模块API设计_

- [ ] 4.2 实现项目查询Service
  - 复用 IProjectsService
  - 实现客户项目查询（WHERE customer_id = ?）
  - 实现员工项目查询（INNER JOIN project_members）
  - 实现项目统计计算
  - _需求：数据权限控制_

- [ ] 4.3 实现前端项目API
  - 实现 api/project.js
  - 封装项目列表、详情、统计接口
  - _需求：前端API层_

- [ ] 4.4 实现客户首页
  - 实现 pages/dashboard/customer.vue
  - 显示项目概况
  - 显示项目统计数据
  - 显示功能菜单（设计、排期、预算、质检）
  - _需求：客户功能模块_

- [ ] 4.5 实现员工首页
  - 实现 pages/dashboard/staff.vue
  - 显示项目概况
  - 显示项目统计数据
  - 显示功能菜单（含员工专属功能）
  - _需求：员工功能模块_

- [ ] 4.6 实现项目选择器组件
  - 实现 components/ProjectSelector/index.vue
  - 显示用户的项目列表
  - 支持项目切换
  - 更新本地存储的 currentProjectId
  - _需求：项目切换机制_

- [ ]* 4.7 编写项目模块单元测试
  - 测试项目列表查询（客户/员工）
  - 测试项目详情查询
  - 测试项目统计计算
  - 测试数据权限隔离
  - _需求：测试策略_

### 5. 设计方案模块

- [ ] 5.1 实现AppDesignController
  - 实现获取房间列表接口（/app/design/rooms）
  - 实现获取房间详情接口（/app/design/rooms/{id}）
  - 基于 project_rooms 表
  - _需求：设计方案模块API设计_

- [ ] 5.2 实现设计方案查询Service
  - 复用 IProjectRoomsService
  - 查询项目的所有房间
  - 解析房间图片（JSON数组）
  - _需求：功能模块与表映射_

- [ ] 5.3 实现前端设计方案API
  - 实现 api/design.js
  - 封装房间列表、详情接口
  - _需求：前端API层_

- [ ] 5.4 实现设计方案页面
  - 实现 pages/design/list.vue（房间列表）
  - 实现 pages/design/detail.vue（房间详情）
  - 显示房间设计图片
  - 支持图片预览
  - _需求：设计方案功能_

- [ ]* 5.5 编写设计方案模块单元测试
  - 测试房间列表查询
  - 测试房间详情查询
  - 测试图片解析
  - _需求：测试策略_

### 6. 施工排期模块

- [ ] 6.1 实现AppScheduleController
  - 实现获取排期列表接口（/app/schedule/list）
  - 实现获取排期详情接口（/app/schedule/{id}）
  - 基于 project_schedules 表
  - _需求：施工排期模块API设计_

- [ ] 6.2 实现施工排期查询Service
  - 复用 IProjectSchedulesService
  - 查询项目的所有排期
  - 计算排期进度
  - _需求：功能模块与表映射_

- [ ] 6.3 实现前端施工排期API
  - 实现 api/schedule.js
  - 封装排期列表、详情接口
  - _需求：前端API层_

- [ ] 6.4 实现施工排期页面
  - 实现 pages/schedule/list.vue（排期列表）
  - 实现 pages/schedule/detail.vue（排期详情）
  - 显示排期时间轴
  - 显示进度百分比
  - _需求：施工排期功能_

- [ ]* 6.5 编写施工排期模块单元测试
  - 测试排期列表查询
  - 测试排期详情查询
  - 测试进度计算
  - _需求：测试策略_

### 7. 预算管理模块

- [ ] 7.1 实现AppBudgetController
  - 实现获取预算总览接口（/app/budget/overview）
  - 实现获取预算明细接口（/app/budget/items）
  - 基于 project_budgets 表
  - _需求：预算管理模块API设计_

- [ ] 7.2 实现预算管理查询Service
  - 复用 IProjectBudgetsService
  - 查询项目预算
  - 按类别分组统计
  - _需求：功能模块与表映射_

- [ ] 7.3 实现前端预算管理API
  - 实现 api/budget.js
  - 封装预算总览、明细接口
  - _需求：前端API层_

- [ ] 7.4 实现预算管理页面
  - 实现 pages/budget/overview.vue（预算总览）
  - 实现 pages/budget/detail.vue（预算明细）
  - 显示预算图表
  - 显示预算明细列表
  - _需求：预算管理功能_

- [ ]* 7.5 编写预算管理模块单元测试
  - 测试预算总览查询
  - 测试预算明细查询
  - 测试分类统计
  - _需求：测试策略_

### 8. 质检管理模块

- [ ] 8.1 实现AppQualityController
  - 实现获取质检列表接口（/app/quality/inspections）
  - 实现获取质检详情接口（/app/quality/inspections/{id}）
  - 基于 quality_inspections、quality_issues、quality_fixes 表
  - _需求：质检管理模块API设计_

- [ ] 8.2 实现质检管理查询Service
  - 复用 IQualityInspectionsService
  - 查询质检记录
  - 关联查询问题和整改记录
  - _需求：功能模块与表映射_

- [ ] 8.3 实现前端质检管理API
  - 实现 api/quality.js
  - 封装质检列表、详情接口
  - _需求：前端API层_

- [ ] 8.4 实现质检管理页面
  - 实现 pages/quality/list.vue（质检列表）
  - 实现 pages/quality/detail.vue（质检详情）
  - 显示质检结果
  - 显示问题和整改记录
  - _需求：质检管理功能_

- [ ]* 8.5 编写质检管理模块单元测试
  - 测试质检列表查询
  - 测试质检详情查询
  - 测试关联查询
  - _需求：测试策略_

### 9. 公共组件

- [ ] 9.1 实现图片预览组件
  - 实现 components/ImagePreview/index.vue
  - 支持图片全屏预览
  - 支持左右滑动切换
  - 支持双指缩放
  - 支持保存到相册
  - _需求：图片预览功能_

- [ ] 9.2 实现图片上传组件
  - 实现 components/ImageUpload/index.vue
  - 支持多图上传
  - 支持图片压缩
  - 显示上传进度
  - _需求：图片上传功能_

- [ ] 9.3 实现空状态组件
  - 实现 components/EmptyState/index.vue
  - 显示空状态提示
  - 支持自定义图标和文字
  - _需求：用户体验优化_

- [ ] 9.4 实现加载状态组件
  - 实现 components/LoadingState/index.vue
  - 显示骨架屏
  - 支持不同的加载样式
  - _需求：用户体验优化_

### 10. 文件上传

- [ ] 10.1 实现文件上传接口
  - 实现 /app/upload/image 接口
  - 支持图片上传
  - 图片压缩和格式转换
  - 返回图片URL和缩略图URL
  - _需求：文件上传_

- [ ] 10.2 实现前端上传API
  - 实现 api/upload.js
  - 封装图片上传方法
  - 处理上传进度
  - _需求：前端API层_

---

## 第三阶段：员工专属功能开发

### 11. 工地巡视模块（员工专属）

- [ ] 11.1 实现AppInspectionController
  - 实现提交巡视记录接口（/app/inspection/submit）
  - 实现获取巡视列表接口（/app/inspection/list）
  - 实现获取巡视详情接口（/app/inspection/{id}）
  - 添加 @RequireStaff 注解
  - 基于 project_schedule_records 表
  - _需求：工地巡视模块API设计_

- [ ] 11.2 实现工地巡视Service
  - 复用 IProjectScheduleRecordsService
  - 实现巡视记录创建
  - 实现巡视记录查询
  - 关联查询排期信息
  - _需求：功能模块与表映射_

- [ ] 11.3 实现前端工地巡视API
  - 实现 api/inspection.js
  - 封装巡视提交、列表、详情接口
  - _需求：前端API层_

- [ ] 11.4 实现工地巡视页面
  - 实现 pages/inspection/list.vue（巡视列表）
  - 实现 pages/inspection/detail.vue（巡视详情）
  - 实现 pages/inspection/submit.vue（提交巡视）
  - 添加角色检查混入（requireStaff: true）
  - _需求：工地巡视功能_

- [ ]* 11.5 编写工地巡视模块单元测试
  - 测试巡视记录创建
  - 测试巡视列表查询
  - 测试员工权限验证
  - _需求：测试策略_

### 12. 问题上报模块（员工专属）

- [ ] 12.1 实现AppIssueController
  - 实现上报问题接口（/app/issue/report）
  - 实现获取问题列表接口（/app/issue/list）
  - 实现获取问题详情接口（/app/issue/{id}）
  - 添加 @RequireStaff 注解
  - 基于 quality_issues 表
  - _需求：问题上报模块API设计_

- [ ] 12.2 实现问题上报Service
  - 复用 IQualityIssuesService
  - 实现问题创建
  - 实现问题查询
  - 关联查询整改记录
  - _需求：功能模块与表映射_

- [ ] 12.3 实现前端问题上报API
  - 实现 api/issue.js
  - 封装问题上报、列表、详情接口
  - _需求：前端API层_

- [ ] 12.4 实现问题上报页面
  - 实现 pages/issue/list.vue（问题列表）
  - 实现 pages/issue/detail.vue（问题详情）
  - 实现 pages/issue/report.vue（上报问题）
  - 添加角色检查混入（requireStaff: true）
  - _需求：问题上报功能_

- [ ]* 12.5 编写问题上报模块单元测试
  - 测试问题创建
  - 测试问题列表查询
  - 测试员工权限验证
  - _需求：测试策略_

### 13. 整改记录模块（员工专属）

- [ ] 13.1 实现AppRepairController
  - 实现提交整改记录接口（/app/repair/submit）
  - 实现获取整改列表接口（/app/repair/list）
  - 实现获取整改详情接口（/app/repair/{id}）
  - 添加 @RequireStaff 注解
  - 基于 quality_fixes 表
  - _需求：整改记录模块API设计_

- [ ] 13.2 实现整改记录Service
  - 复用 IQualityFixesService
  - 实现整改记录创建
  - 实现整改记录查询
  - 关联查询问题信息
  - _需求：功能模块与表映射_

- [ ] 13.3 实现前端整改记录API
  - 实现 api/repair.js
  - 封装整改提交、列表、详情接口
  - _需求：前端API层_

- [ ] 13.4 实现整改记录页面
  - 实现 pages/repair/list.vue（整改列表）
  - 实现 pages/repair/detail.vue（整改详情）
  - 实现 pages/repair/submit.vue（提交整改）
  - 添加角色检查混入（requireStaff: true）
  - _需求：整改记录功能_

- [ ]* 13.5 编写整改记录模块单元测试
  - 测试整改记录创建
  - 测试整改列表查询
  - 测试员工权限验证
  - _需求：测试策略_

### 14. 个人中心

- [ ] 14.1 实现个人中心页面
  - 实现 pages/profile/index.vue
  - 显示用户信息
  - 显示项目列表
  - 支持退出登录
  - _需求：个人中心功能_

---

## 第四阶段：优化与测试

### 15. 性能优化

- [ ] 15.1 实现数据缓存
  - 项目信息缓存（5分钟）
  - 列表数据缓存（3分钟）
  - 使用 Pinia 状态管理
  - _需求：性能优化_

- [ ] 15.2 实现图片优化
  - 图片懒加载
  - 缩略图预览
  - 图片压缩上传
  - _需求：性能优化_

- [ ] 15.3 实现列表优化
  - 分页加载（每页20条）
  - 下拉刷新/上拉加载
  - 骨架屏加载
  - _需求：性能优化_

### 16. 用户体验优化

- [ ] 16.1 完善加载状态
  - 统一的加载提示
  - 骨架屏加载
  - 加载进度显示
  - _需求：用户体验优化_

- [ ] 16.2 完善空状态处理
  - 统一的空状态组件
  - 友好的空状态提示
  - 引导用户操作
  - _需求：用户体验优化_

- [ ] 16.3 完善错误提示
  - 统一的错误处理
  - 友好的错误提示
  - 错误重试机制
  - _需求：用户体验优化_

### 17. 测试与验证

- [ ] 17.1 接口联调测试
  - 测试所有API接口
  - 验证请求参数和响应格式
  - 测试错误处理
  - _需求：测试策略_

- [ ] 17.2 功能测试
  - 测试客户功能（项目、设计、排期、预算、质检）
  - 测试员工功能（巡视、问题、整改）
  - 测试登录和权限控制
  - _需求：测试策略_

- [ ] 17.3 数据权限测试
  - 测试客户只能看自己的项目
  - 测试员工只能看关联的项目
  - 测试员工专属功能拦截
  - _需求：数据权限控制_

- [ ] 17.4 兼容性测试
  - 测试微信小程序
  - 测试不同设备（iOS/Android）
  - 测试不同屏幕尺寸
  - _需求：兼容性测试_

---

## 第五阶段：部署上线

### 18. 部署准备

- [ ] 18.1 生产环境配置
  - 配置生产环境API地址
  - 配置微信小程序AppID和AppSecret
  - 配置短信服务
  - _需求：部署配置_

- [ ] 18.2 数据库迁移
  - 执行数据库建表脚本
  - 插入字典数据
  - 数据备份
  - _需求：数据库设计_

- [ ] 18.3 后端服务部署
  - 打包后端服务
  - 部署到生产环境
  - 配置Nginx反向代理
  - 配置HTTPS证书
  - _需求：部署上线_

- [ ] 18.4 小程序发布
  - 小程序代码上传
  - 提交审核
  - 发布上线
  - _需求：部署上线_

- [ ] 18.5 用户培训文档
  - 编写用户使用手册
  - 编写管理员操作手册
  - 录制操作视频
  - _需求：用户培训_

---

**总计任务数：约90个任务（含可选测试任务）**
**核心任务数：约75个任务（不含可选测试任务）**
**预计开发周期：22-32天**

