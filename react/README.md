# 装修行业项目管理后台系统

<div align="center">

**面向装修行业垂直领域的全流程项目管理与客户关系管理平台**

一个专为装修公司、工程队和项目经理设计的综合管理系统，覆盖从客户签约到项目验收的全生命周期管理。

[![React](https://img.shields.io/badge/React-18.3.1-61dafb.svg)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.8+-3178c6.svg)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-5.4+-646cff.svg)](https://vitejs.dev/)
[![Ant Design](https://img.shields.io/badge/Ant%20Design-5.27+-1890ff.svg)](https://ant.design/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

</div>

---

## 📋 项目概述

### 业务背景

本项目是**装修行业数字化管理解决方案**，针对传统装修行业存在的项目管理混乱、客户信息分散、进度跟踪困难、质量管控不严等痛点，提供一站式数字化管理平台。

### 核心价值

- **提效降本**：数字化管理减少沟通成本，提高施工效率
- **质量管控**：标准化质检流程，确保施工质量
- **客户满意**：透明的进度展示，提升客户体验
- **数据驱动**：实时数据分析，支持业务决策

---

## 🎯 产品功能

### 🏗️ 项目全生命周期管理

**工地信息管理**
- 工地基本信息（名称、地址、面积、房型）
- 项目档案管理（设计图、合同、许可证）
- 施工团队配置（项目经理、工长、技术员）

**项目状态跟踪**
```
规划中 → 进行中 → 已完成 → 已暂停
   ↓         ↓         ↓         ↓
方案设计   施工管理   竣工验收   项目归档
```

**财务管理系统**
- 预算编制与审批
- 费用分类统计（材料、人工、设备、其他）
- 成本控制与预警
- 付款节点管理

**进度时间线管理**
- 8大标准施工阶段：拆除→水电→泥瓦→木工→油漆→安装→软装→验收
- 关键节点里程碑设置
- 进度延期预警
- 依赖关系管理

### 👥 客户关系管理 (CRM)

**客户档案**
- 基本信息（姓名、电话、邮箱、地址）
- 客户分类（个人、企业、设计师推荐）
- 客户标签和画像
- 沟通记录历史

**客户项目视图**
- 客户所有项目汇总
- 项目状态总览
- 客户满意度跟踪
- 续约和推荐管理

**营销工具**
- 潜在客户跟进
- 报价单管理
- 合同模板库
- 客户反馈收集

### 📊 智能数据看板

**经营指标概览**
- 项目数量统计（按状态、类型、地区）
- 财务指标（合同总额、回款率、利润率）
- 客户分析（新增、活跃、流失率）
- 质量指标（合格率、返工率）

**实时运营监控**
- 进行中项目地图分布
- 今日日程和待办事项
- 异常情况预警（延期、超支、质量问题）
- 团队工作负荷分析

**自定义报表**
- 项目进度报表
- 财务收支报表
- 客户分析报表
- 质量统计报表

### ✅ 质量管控系统

**标准化质检流程**
- 分阶段质量检查点
- 质检标准库
- 检查清单模板
- 质检员资质管理

**质量问题管理**
- 问题发现与记录（文字、图片、视频）
- 问题分级与分类
- 整改任务分配
- 复检验证闭环

**质量评估体系**
- 质量评分算法
- 供应商质量评级
- 工队质量档案
- 客户满意度调研

### 👥 团队与人员管理

**组织架构管理**
- 部门与岗位设置
- 员工档案管理
- 权限角色配置
- 考勤与绩效

**技能与认证**
- 技能标签管理
- 资质证书管理
- 培训记录
- 技能评级体系

**工作调度**
- 人员排班管理
- 项目任务分配
- 工作量统计
- 效率分析

### 🎨 设计管理

**设计稿管理**
- 按房间分类设计稿
- 版本历史追踪
- 设计变更记录
- 客户确认流程

**材料与配色**
- 材料库管理
- 配色方案
- 品牌产品库
- 成本估算

### ⚙️ 系统配置

**内容管理**
- 公司资讯发布
- 案例展示管理
- 活动促销设置
- 轮播图配置

**基础数据**
- 工程类别设置
- 材料品牌库
- 供应商管理
- 服务项目配置

---

## 🛠️ 技术架构

### 前端技术栈

**核心框架**
- **React 18.3.1** - 现代化前端框架，支持并发特性
- **TypeScript 5.8+** - 类型安全，提升开发效率和代码质量
- **Vite 5.4+** - 极速构建工具，支持 HMR 和优化

**路由与状态管理**
- **React Router v6.30+** - 基于组件的路由系统
- **TanStack Query v5.83+** - 强大的服务端状态管理
- **Context API** - 轻量级全局状态管理

**UI 组件库**
- **Ant Design 5.27+** - 企业级 UI 设计语言
- **@ant-design/icons** - 丰富的图标库
- **dayjs 1.11+** - 轻量级日期处理库

### 开发工具链

**代码质量**
- **ESLint 9.32+** - 代码规范检查
- **TypeScript ESLint** - TypeScript 语法检查
- **React Hooks ESLint Plugin** - Hooks 使用规范

**构建优化**
- **SWC 编译** - 轻量级 TypeScript/JavaScript 编译器
- **代码分割** - 按路由自动代码分割
- **资源压缩** - 生产环境资源优化

### 项目配置

**路径别名**
```typescript
// 配置 @/ 为 src/ 目录别名
import Component from '@/components/Component';
import { mockData } from '@/data/mockData';
```

**TypeScript 配置**
- 宽松模式配置，快速开发
- 路径映射支持
- 多项目引用结构

**Vite 特性**
- 开发服务器端口：8080
- 支持所有网络接口访问
- 开发模式组件标记器

---

## 🚀 快速开始

### 环境要求

**必需环境**
- Node.js >= 16.x (推荐 18.x 或更高版本)
- npm >= 8.x 或 yarn >= 1.22.x

**推荐工具**
- [nvm](https://github.com/nvm-sh/nvm) - Node.js 版本管理
- [VS Code](https://code.visualstudio.com/) - 代码编辑器
- React Developer Tools - 浏览器扩展

### 安装与启动

**1. 克隆项目**
```bash
git clone https://github.com/your-username/homeimprovement-management.git
cd homeimprovement-management
```

**2. 安装依赖**
```bash
npm install
# 或
yarn install
```

**3. 启动开发服务器**
```bash
npm run dev
# 或
yarn dev
```

访问 http://localhost:8080 查看应用

**4. 登录系统**
- 默认用户名：admin
- 默认密码：任意输入（演示模式）

### 构建与部署

**生产环境构建**
```bash
npm run build
```

**开发模式构建**
```bash
npm run build:dev
```

**预览构建结果**
```bash
npm run preview
```

**代码检查**
```bash
npm run lint
```

---

## 📁 项目结构详解

```
homeimprovement-87756/
├── 📁 public/                  # 静态资源
│   ├── favicon.ico            # 网站图标
│   ├── placeholder.svg        # 占位图片
│   └── robots.txt             # 搜索引擎配置
├── 📁 src/                    # 源代码目录
│   ├── 📁 components/         # 通用组件
│   │   ├── Layout.tsx         # 🏛️ 主布局组件（侧边栏 + 头部 + 内容区）
│   │   ├── ProjectForm.tsx    # 📝 项目表单组件
│   │   ├── CustomerForm.tsx   # 👤 客户表单组件
│   │   ├── BudgetManagement.tsx # 💰 预算管理组件
│   │   ├── TeamAssignment.tsx # 👥 团队分配组件
│   │   ├── TimelineManagement.tsx # 📅 时间线管理
│   │   ├── DesignManagement.tsx  # 🎨 设计稿管理
│   │   ├── InspectionReport.tsx # ✅ 质检报告组件
│   │   ├── QualityIssueReport.tsx # 🚨 质量问题报告
│   │   └── IssueResolution.tsx    # 🔧 问题整改组件
│   ├── 📁 pages/              # 页面组件
│   │   ├── Login.tsx          # 🔐 登录页面
│   │   ├── Dashboard.tsx      # 📊 数据看板（首页）
│   │   ├── Projects.tsx       # 🏗️ 项目管理页面
│   │   ├── Customers.tsx      # 👥 客户管理页面
│   │   ├── Progress.tsx       # 📈 进度跟踪页面
│   │   ├── Quality.tsx        # ✅ 质量检查页面
│   │   ├── TeamManagement.tsx # 👨‍💼 团队管理页面
│   │   ├── NewsSettings.tsx   # 📰 资讯设置页面
│   │   ├── CategorySettings.tsx # 🏷️ 分类设置页面
│   │   └── NotFound.tsx       # ❌ 404页面
│   ├── 📁 data/               # 数据层
│   │   └── mockData.ts        # 🗄️ 模拟数据（包含所有业务实体）
│   ├── 📁 constants/          # 常量定义
│   │   └── index.ts           # 📋 业务常量
│   ├── 📁 utils/              # 工具函数
│   │   └── helpers.ts         # 🛠️ 辅助函数
│   ├── App.tsx                # 🌳 应用根组件
│   ├── main.tsx               # 🚀 应用入口
│   ├── vite-env.d.ts          # 🔧 Vite 类型声明
│   └── index.css              # 🎨 全局样式
├── 📄 package.json            # 📦 项目依赖配置
├── 📄 tsconfig.json           # ⚙️ TypeScript 配置
├── 📄 tsconfig.app.json       # ⚙️ 应用 TypeScript 配置
├── 📄 tsconfig.node.json      # ⚙️ Node.js TypeScript 配置
├── 📄 vite.config.ts          # ⚡ Vite 构建配置
├── 📄 eslint.config.js        # 📏 ESLint 代码规范
├── 📄 components.json         # 🧩 组件配置（shadcn）
└── 📄 README.md               # 📖 项目文档
```

---

## 🎯 核业务模块详解

### 1. 项目管理模块

**功能特性**
- 项目 CRUD 操作
- 项目状态流转管理
- 预算与费用跟踪
- 施工进度可视化

**数据模型**
```typescript
interface Project {
  id: string;                    // 项目唯一标识
  siteName: string;              // 工地名称
  projectName: string;            // 项目名称
  siteAddress: string;           // 工地地址
  customerId: string;            // 关联客户ID
  customerName: string;          // 客户姓名
  status: 'planning' | 'inProgress' | 'completed' | 'suspended';
  startDate: string;              // 开工日期
  endDate: string;                // 预计完工日期
  totalBudget: number;            // 总预算
  expenses: Expense[];             // 费用明细
  timeline: TimelineItem[];       // 施工时间线
  team?: ProjectTeam;             // 施工团队
}
```

**关键页面**
- `Projects.tsx` - 项目列表与管理
- `ProjectForm.tsx` - 项目创建/编辑表单

### 2. 客户管理模块

**功能特性**
- 客户信息管理
- 客户项目关联
- 客户沟通记录
- 客户分类与标签

**数据模型**
```typescript
interface Customer {
  id: string;          // 客户ID
  name: string;        // 客户姓名
  phone: string;       // 联系电话
  email: string;       // 邮箱地址
  address: string;     // 详细地址
  createdAt: string;   // 创建时间
}
```

**关键页面**
- `Customers.tsx` - 客户列表与管理
- `CustomerForm.tsx` - 客户创建/编辑表单

### 3. 进度跟踪模块

**功能特性**
- 施工阶段管理
- 进度可视化展示
- 延期预警
- 里程碑管理

**施工阶段标准化**
```
1️⃣ 拆除工程 → 2️⃣ 水电改造 → 3️⃣ 泥瓦工程 → 4️⃣ 木工工程
         ↓                        ↓                         ↓
    旧墙拆除                    水电布线                  墙面找平
    地板拆除                    开关定位                  地砖铺贴
    垃圾清运                    管道铺设                  ...

5️⃣ 油漆工程 → 6️⃣ 安装工程 → 7️⃣ 软装配饰 → 8️⃣ 竣工验收
         ↓                        ↓                         ↓
    墙面乳胶漆                  灯具安装                  家具进场
    木器漆处理                  洁具安装                  窗帘布艺
    ...                        ...                        ...
```

**关键页面**
- `Progress.tsx` - 项目进度总览
- `TimelineManagement.tsx` - 时间线详细管理

### 4. 质量检查模块

**功能特性**
- 质检记录管理
- 质量问题跟踪
- 整改闭环管理
- 质检标准库

**数据模型**
```typescript
interface QualityCheck {
  id: string;              // 质检ID
  projectId: string;       // 关联项目ID
  projectName: string;     // 项目名称
  checkDate: string;       // 质检日期
  inspector: string;       // 质检员
  category: string;        // 质检类别
  status: 'passed' | 'failed' | 'pending';
  issues: QualityIssue[];  // 质量问题列表
  inspections: InspectionRecord[];  // 验收记录
}

interface QualityIssue {
  id: string;              // 问题ID
  title: string;           // 问题标题
  description: string;     // 问题描述
  images: string[];        // 问题图片
  reportTime: string;      // 上报时间
  reporter: string;        // 上报人
  status: 'pending' | 'resolved';  // 处理状态
  resolution?: Resolution; // 整改方案
}
```

**关键页面**
- `Quality.tsx` - 质检总览
- `InspectionReport.tsx` - 质检报告
- `QualityIssueReport.tsx` - 问题报告
- `IssueResolution.tsx` - 整改管理

---

## 🔐 身份验证与权限

### 当前实现

**简单认证系统**
- 基于 localStorage 的会话管理
- 登录状态：`localStorage.isLoggedIn`
- 用户信息：`localStorage.username`
- 路由守卫：`Layout.tsx:34-39`

**权限控制**
```typescript
// Layout.tsx 中的认证检查
useEffect(() => {
  const isLoggedIn = localStorage.getItem('isLoggedIn');
  if (!isLoggedIn && location.pathname !== '/login') {
    navigate('/login');
  }
}, [navigate, location]);
```

### 后续规划

**JWT 认证**
- 后端 JWT Token 验证
- Token 自动刷新机制
- 安全退出与单点登录

**角色权限系统**
- 管理员：全部权限
- 项目经理：项目相关权限
- 质检员：质检相关权限
- 客户：查看权限

---

## 📊 数据管理策略

### 当前状态：Mock 数据

**数据源文件**
- `src/data/mockData.ts` - 完整的业务模拟数据

**数据实体**
- `mockCustomers` - 客户数据（3条示例）
- `mockProjects` - 项目数据（3个不同状态的项目）
- `mockQualityChecks` - 质检数据（包含通过和失败案例）
- `mockCategories` - 工程类别
- `mockNews` - 资讯数据

**数据特点**
- 真实业务场景模拟
- 包含完整的数据关联
- 支持各种 UI 状态展示

### 后端集成方案

**API 设计原则**
- RESTful API 设计
- 统一的响应格式
- 错误处理机制
- 请求拦截器

**推荐架构**
```typescript
// API 接口设计示例
interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  code?: number;
}

// 资源接口
GET    /api/v1/projects              # 项目列表
POST   /api/v1/projects              # 创建项目
GET    /api/v1/projects/:id          # 项目详情
PUT    /api/v1/projects/:id          # 更新项目
DELETE /api/v1/projects/:id          # 删除项目
GET    /api/v1/projects/:id/timeline # 项目时间线
POST   /api/v1/projects/:id/progress # 更新进度

GET    /api/v1/customers             # 客户列表
POST   /api/v1/customers             # 创建客户
GET    /api/v1/customers/:id/projects # 客户项目

GET    /api/v1/quality-checks        # 质检记录
POST   /api/v1/quality-checks        # 创建质检
PUT    /api/v1/quality-issues/:id     # 更新问题状态
```

**TanStack Query 集成**
```typescript
// 示例：项目管理 API
export const useProjects = () => {
  return useQuery({
    queryKey: ['projects'],
    queryFn: () => api.get('/projects'),
    staleTime: 5 * 60 * 1000, // 5分钟缓存
  });
};

export const useCreateProject = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (projectData) => api.post('/projects', projectData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      message.success('项目创建成功');
    },
  });
};
```

---

## 🎨 UI/UX 设计系统

### Ant Design 主题定制

**全局主题配置**
```typescript
// App.tsx 主题配置
<ConfigProvider
  locale={zhCN}
  theme={{
    token: {
      colorPrimary: '#1677ff',    // 主色调
      borderRadius: 6,              // 圆角大小
      fontSize: 14,                 // 基础字体大小
    },
    components: {
      Layout: {
        siderBg: '#001529',         // 侧边栏背景色
        headerBg: '#ffffff',        // 头部背景色
      },
    },
  }}
>
```

**设计原则**
- **一致性**：统一的视觉语言和交互模式
- **效率性**：减少用户操作步骤，提高工作效率
- **专业性**：符合装修行业用户的操作习惯
- **响应式**：适配不同屏幕尺寸

### 布局系统

**整体布局**
- 侧边栏导航：固定宽度，可折叠
- 顶部导航：用户信息、操作按钮
- 内容区域：自适应宽度，滚动展示

**响应式适配**
```typescript
// Ant Design Grid 系统
<Row gutter={[16, 16]}>
  <Col xs={24} sm={12} lg={6}>    // 响应式列宽
    <Card>内容</Card>
  </Col>
</Row>
```

---

## 🌐 国际化支持

### 当前状态：简体中文

**语言配置**
- UI 文本：简体中文
- Ant Design：`zhCN` 语言包
- 日期格式：dayjs 中文本地化

**本地化特性**
- 符合中国用户习惯
- 装修行业术语本土化
- 日期时间格式本地化

### 多语言扩展方案

**i18n 集成**
```typescript
// 未来可集成 react-i18next
import i18n from 'i18next';
import { useTranslation } from 'react-i18next';

// 使用示例
const { t } = useTranslation();
<h1>{t('dashboard.title')}</h1>
```

---

## 🔧 开发工作流

### 代码规范

**编码风格**
- 使用函数式组件 + Hooks
- TypeScript 接口优先
- ESLint 自动检查
- Prettier 代码格式化

**命名规范**
- 组件：PascalCase (`ProjectCard`)
- 函数/变量：camelCase (`getUserInfo`)
- 常量：UPPER_SNAKE_CASE (`API_BASE_URL`)
- 文件名：与组件名一致

### Git 工作流

**分支策略**
```
main          # 主分支，生产环境代码
develop       # 开发分支，集成测试
feature/*     # 功能分支
hotfix/*      # 热修复分支
release/*     # 发布分支
```

**提交规范**
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码样式调整
refactor: 重构
test: 测试相关
chore: 构建/工具相关
```

### 开发环境

**推荐 VS Code 插件**
- ES7+ React/Redux/React-Native snippets
- TypeScript Importer
- Prettier - Code formatter
- ESLint
- Auto Rename Tag
- Bracket Pair Colorizer

---

## 🗺️ 产品发展路线图

### ✅ 已完成功能 (MVP)

**核心业务模块**
- [x] 用户认证与权限框架
- [x] 项目管理（CRUD + 状态管理）
- [x] 客户关系管理
- [x] 进度跟踪与时间线
- [x] 质量检查管理
- [x] 数据看板与统计
- [x] 系统配置（资讯、分类）

**技术基础设施**
- [x] React 18 + TypeScript 架构
- [x] Ant Design UI 组件库集成
- [x] React Router 路由系统
- [x] TanStack Query 状态管理
- [x] 响应式布局系统
- [x] Mock 数据体系

### 🚧 开发中功能

**后端集成**
- [ ] RESTful API 开发
- [ ] JWT 认证系统
- [ ] 数据库设计与集成
- [ ] 文件上传服务

**功能增强**
- [ ] 数据导出（Excel/PDF）
- [ ] 高级搜索与筛选
- [ ] 批量操作功能
- [ ] 操作日志记录

### 📅 2024 Q4 计划

**移动端支持**
- [ ] 响应式设计优化
- [ ] PWA 支持
- [ ] 微信小程序版本

**业务增强**
- [ ] 财务报表系统
- [ ] 供应商管理
- [ ] 库存管理
- [ ] 合同管理

### 📅 2025 Q1 计划

**高级功能**
- [ ] 项目甘特图
- [ ] 实时消息推送
- [ ] 视频监控集成
- [ ] AI 图像识别质检

**平台化**
- [ ] 多租户支持
- [ ] 开放 API
- [ ] 第三方集成（设计软件、ERP）
- [ ] 数据分析与 BI

---

## 🚀 部署指南

### 生产环境部署

**构建优化**
```bash
# 生产环境构建
npm run build

# 构建分析（可选）
npm run build -- --analyze
```

**静态部署**
项目构建后生成纯静态文件，可部署到：

- **Nginx**：配置静态文件服务
- **CDN**：加速静态资源访问
- **Vercel/Netlify**：自动化部署平台

**Nginx 配置示例**
```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /path/to/dist;
    index index.html;

    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 静态资源缓存
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### Docker 部署

**Dockerfile**
```dockerfile
FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## 📈 性能优化

### 代码分割

**路由级分割**
```typescript
// 路由懒加载
const Dashboard = lazy(() => import('./pages/Dashboard'));
const Projects = lazy(() => import('./pages/Projects'));

// 使用 Suspense 包装
<Suspense fallback={<Loading />}>
  <Routes>
    <Route path="/dashboard" element={<Dashboard />} />
  </Routes>
</Suspense>
```

**组件级分割**
```typescript
// 动态导入大型组件
const HeavyComponent = lazy(() => import('./components/HeavyComponent'));
```

### 缓存策略

**数据缓存**
```typescript
// TanStack Query 缓存配置
useQuery({
  queryKey: ['projects'],
  queryFn: fetchProjects,
  staleTime: 5 * 60 * 1000,     // 5分钟内认为数据新鲜
  cacheTime: 10 * 60 * 1000,    // 10分钟后清除缓存
});
```

**静态资源缓存**
- 构建文件名哈希化
- 长期缓存策略
- CDN 加速

---

## 🔒 安全考虑

### 前端安全

**XSS 防护**
- React 自动转义 JSX 中的内容
- 使用 DOMPurify 清理用户输入
- 避免使用 dangerouslySetInnerHTML

**CSRF 防护**
- SameSite Cookie 配置
- CSRF Token 验证
- 验证 HTTP Referer

**数据保护**
- 敏感信息不存储在 localStorage
- API 请求使用 HTTPS
- 定期更新依赖包

### 权限控制

**路由级权限**
```typescript
// 高阶组件权限验证
const ProtectedRoute = ({ children, requiredPermission }) => {
  const { user } = useAuth();

  if (!hasPermission(user, requiredPermission)) {
    return <NoPermission />;
  }

  return children;
};
```

**组件级权限**
```typescript
// 条件渲染
{canEdit && <Button>编辑</Button>}
```

---

## 🤝 贡献指南

### 参与贡献

**1. Fork 项目**
```bash
git clone https://github.com/your-username/homeimprovement-management.git
```

**2. 创建功能分支**
```bash
git checkout -b feature/amazing-feature
```

**3. 提交代码**
```bash
git commit -m 'feat: add amazing feature'
git push origin feature/amazing-feature
```

**4. 创建 Pull Request**
- 详细描述变更内容
- 关联相关 Issue
- 确保 CI 检查通过

### 代码贡献规范

**PR 要求**
- 通过所有 ESLint 检查
- 包含必要的测试用例
- 更新相关文档
- 遵循代码提交规范

**Issue 报告**
- 使用 Issue 模板
- 详细描述问题或建议
- 提供复现步骤
- 包含环境信息

---

## 📄 许可证

本项目采用 **MIT 许可证**，详情请查看 [LICENSE](LICENSE) 文件。

**许可证要点**
- ✅ 商业使用
- ✅ 修改
- ✅ 分发
- ✅ 私人使用
- ❗ 需要包含许可证和版权声明
- ❗ 提供责任担保

---

## 💬 联系与支持

### 获取帮助

**问题反馈**
- [GitHub Issues](https://github.com/your-username/homeimprovement-management/issues)
- [GitHub Discussions](https://github.com/your-username/homeimprovement-management/discussions)

**技术支持**
- 📧 邮箱：support@example.com
- 💬 微信群：扫描二维码加入
- 📱 QQ群：123456789

### 商业合作

**定制开发**
- 企业版本定制
- 私有化部署
- 功能定制开发

**技术咨询**
- 系统架构设计
- 技术方案评审
- 团队培训服务

---

## 🙏 致谢

### 技术栈致谢

感谢以下优秀的开源项目：

- **[React](https://react.dev/)** - 现代化前端框架
- **[TypeScript](https://www.typescriptlang.org/)** - 类型安全的 JavaScript
- **[Vite](https://vitejs.dev/)** - 下一代前端构建工具
- **[Ant Design](https://ant.design/)** - 企业级 UI 设计语言
- **[TanStack Query](https://tanstack.com/query)** - 强大的服务端状态管理
- **[dayjs](https://day.js.org/)** - 轻量级日期处理库

特别感谢 **[Lovable.dev](https://lovable.dev/)** 提供的初始项目脚手架和 AI 辅助开发支持。

### 社区贡献

感谢所有为装修行业数字化发展做出贡献的开发者和设计师朋友们。

---

<div align="center">

## 🌟 如果这个项目对你有帮助，请给个 Star 支持一下！

**Made with ❤️ for the Home Improvement Industry**

**专注于装修行业数字化转型的开源项目**

[![Star History Chart](https://api.star-history.com/svg?repos=your-username/homeimprovement-management&type=Date)](https://star-history.com/#your-username/homeimprovement-management&Date)

</div>