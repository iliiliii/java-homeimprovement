# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个装修管理系统的开发仓库，包含两个主要子项目：

- **sb3/**: Spring Boot 3 后端项目（基于若依 RuoYi v3.9.0 框架）
- **vue3/**: Vue 3 前端项目（基于 Element Plus UI 框架）
- **docs/**: 项目文档，包含业务需求说明（装修管理系统业务说明书.md）和数据库设计（schema.prisma）

## 技术栈

### 后端 (sb3/)
- **框架**: Spring Boot 3.5.4 + MyBatis + Spring Security
- **Java 版本**: 17
- **数据库**: MySQL 8.2.0
- **连接池**: Druid 1.2.23
- **认证**: JWT (io.jsonwebtoken)
- **API 文档**: SpringDoc OpenAPI 2.8.9
- **构建工具**: Maven

### 前端 (vue3/)
- **框架**: Vue 3.5.16
- **UI 库**: Element Plus 2.10.7
- **状态管理**: Pinia 3.0.2
- **路由**: Vue Router 4.5.1
- **HTTP 客户端**: Axios 1.9.0
- **构建工具**: Vite 6.3.5
- **图表**: ECharts 5.6.0

## 项目架构

### 后端模块结构
```
sb3/
├── ruoyi-admin/      # 启动模块，包含主应用入口和 Web 层
├── ruoyi-framework/  # 框架核心模块（Security、JWT、Redis等）
├── ruoyi-system/     # 系统模块（用户、角色、权限等）
├── ruoyi-common/     # 通用工具模块
├── ruoyi-quartz/     # 定时任务模块
└── ruoyi-generator/  # 代码生成器模块
```

### 前端目录结构
```
vue3/src/
├── api/          # API 接口定义
├── assets/       # 静态资源
├── components/   # 公共组件
├── layout/       # 布局组件
├── router/       # 路由配置
├── store/        # Pinia 状态管理
├── utils/        # 工具函数
└── views/        # 页面视图
```

## 常用开发命令

### 后端开发

#### Maven 构建
```bash
# 在 sb3/ 目录下执行

# 编译项目
mvn clean compile

# 打包（跳过测试）
mvn clean package -DskipTests

# 运行测试
mvn test

# 安装到本地仓库
mvn clean install
```

#### 启动应用
```bash
# 开发模式（使用 IDE 或 Maven）
cd sb3/ruoyi-admin
mvn spring-boot:run

# 生产模式（使用脚本）
cd sb3/
./ry.sh start      # 启动
./ry.sh stop       # 停止
./ry.sh restart    # 重启
./ry.sh status     # 状态
```

#### 数据库
```bash
# SQL 脚本位置
sb3/sql/

# 首次部署需要执行数据库初始化脚本
# 在 MySQL 中依次执行 sql/ 目录下的脚本
```

### 前端开发

```bash
# 在 vue3/ 目录下执行

# 安装依赖（使用国内镜像）
yarn --registry=https://registry.npmmirror.com
# 或
npm install --registry=https://registry.npmmirror.com

# 启动开发服务器（默认 http://localhost:80）
yarn dev
# 或
npm run dev

# 构建生产环境
yarn build:prod
# 或
npm run build:prod

# 构建测试环境
yarn build:stage
# 或
npm run build:stage

# 预览构建结果
yarn preview
```

## 代码规范

### 后端
- 使用 MyBatis XML 方式编写 SQL（位于各模块的 resources/mapper/ 目录）
- Controller 层使用 `@RestController` 注解，统一返回 `AjaxResult` 类型
- Service 层接口和实现分离，实现类使用 `@Service` 注解
- Mapper 层使用 `@Mapper` 注解
- 实体类位于各模块的 domain/ 目录

### 前端
- 组件使用 `<script setup>` 语法（Vue 3 Composition API）
- API 调用统一使用 `src/api/` 目录下的接口定义
- 使用 Element Plus 组件库，避免自定义样式覆盖
- 路由配置支持动态权限菜单

## 业务功能模块

根据 `docs/装修管理系统-业务说明书.md`，系统包含以下核心模块：

1. **客户管理 (CRM)**: 客户档案、项目关联、重点客户识别
2. **项目管理**: 项目档案、预算管理（7大类）、进度计划（8大阶段）、团队分配
3. **进度跟踪**: 施工阶段管理（拆除→水电→泥瓦→木工→油漆→安装→软装→验收）
4. **质量管控**: 质检记录、问题上报、整改管理、闭环跟踪
5. **团队管理**: 人员档案（设计师、项目经理、工长、监理）
6. **数据看板**: 核心指标统计、重点客户展示、在建项目监控、待办提醒
7. **系统管理**: 用户、角色、权限、菜单、字典、参数配置（继承自若依框架）

## 数据库设计

参考 `docs/schema.prisma` 文件，主要数据表包括：

- `customers`: 客户表
- `projects`: 项目表
- `project_budgets`: 项目预算表
- `project_schedules`: 项目进度表
- `quality_inspections`: 质检记录表
- `inspection_issues`: 质检问题表
- `team_members`: 团队成员表
- 若依框架自带的系统表（sys_user, sys_role, sys_menu 等）

## 开发注意事项

### 认证与权限
- 后端使用 JWT Token 认证，Token 存储在 Redis 中
- 前端请求需在 Header 中携带 `Authorization: Bearer {token}`
- 使用 `@PreAuthorize` 注解进行方法级权限控制
- 数据权限通过注解 `@DataScope` 实现

### 跨域配置
- 开发环境前端代理配置在 `vue3/vite.config.js` 中
- 生产环境需在 Nginx 或后端 CORS 配置中处理

### 代码生成器
- 若依框架自带代码生成器（ruoyi-generator 模块）
- 访问系统管理 -> 代码生成菜单
- 可一键生成前后端 CRUD 代码

### 环境配置
- 后端配置文件: `sb3/ruoyi-admin/src/main/resources/application.yml`
- 前端环境变量: `vue3/.env.development` (开发) 和 `vue3/.env.production` (生产)

## 部署

### 后端部署
```bash
# 1. 打包
cd sb3/
mvn clean package -DskipTests

# 2. 上传 ruoyi-admin/target/ruoyi-admin.jar 到服务器

# 3. 使用启动脚本
./ry.sh start
```

### 前端部署
```bash
# 1. 构建
cd vue3/
yarn build:prod

# 2. 将 dist/ 目录内容部署到 Nginx
# 参考 nginx 配置：
# - 静态资源目录指向 dist/
# - API 请求代理到后端服务
```

## 文档参考

- 若依官方文档: http://doc.ruoyi.vip
- Spring Boot 文档: https://spring.io/projects/spring-boot
- Vue 3 文档: https://cn.vuejs.org
- Element Plus 文档: https://element-plus.org/zh-CN
