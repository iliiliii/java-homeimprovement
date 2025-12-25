# 智享家 Pro - 客户端小程序

基于 UniApp + Vue3 + uView-Plus 的装修管理客户端微信小程序。

## 技术栈

- **框架**: UniApp (Vue3)
- **构建工具**: Vite
- **UI 组件库**: uView-Plus 3.x
- **状态管理**: Pinia
- **样式**: SCSS

## 项目结构

```
uni3/
├── src/
│   ├── api/              # API 接口定义
│   ├── components/       # 公共组件
│   │   └── CustomTabBar.vue  # 自定义底部导航
│   ├── pages/            # 页面文件
│   │   ├── login/        # 登录页
│   │   ├── dashboard/    # 首页/概览
│   │   ├── schedule/     # 施工排期
│   │   ├── design/       # 设计方案
│   │   ├── brand/        # 品牌介绍
│   │   ├── profile/      # 个人中心
│   │   ├── budget/       # 预算详情
│   │   ├── shopping/     # 物料清单
│   │   └── log/          # 项目日志
│   ├── store/            # Pinia 状态管理
│   ├── styles/           # 全局样式
│   │   ├── variables.scss    # 样式变量
│   │   └── global.scss       # 全局样式
│   ├── utils/            # 工具函数
│   ├── App.vue           # 根组件
│   ├── main.js           # 入口文件
│   ├── pages.json        # 页面路由配置
│   ├── manifest.json     # 应用配置
│   └── uni.scss          # uView 主题定制
├── index.html            # H5 入口
├── package.json          # 依赖配置
├── vite.config.js        # Vite 配置
└── README.md             # 项目说明
```

## 设计风格

采用 **黑红白** 简约设计风格：

- **品牌红**: `#C40016` - 主色调，源自逅时代 Logo
- **黑色系**: `#212121` - 主要文字和深色背景
- **白色系**: `#FFFFFF` - 背景和卡片
- **灰色系**: `#757575` - 次要文字
- **成功色**: `#10B981` - 成功状态
- **圆角**: 大圆角设计 (24rpx)
- **阴影**: 柔和投影效果

## 快速开始

### 安装依赖

```bash
cd uni3
npm install
```

### 开发模式

```bash
# 微信小程序
npm run dev:mp-weixin

# H5
npm run dev:h5
```

### 构建生产版本

```bash
# 微信小程序
npm run build:mp-weixin

# H5
npm run build:h5
```

### 微信小程序开发

1. 运行 `npm run dev:mp-weixin`
2. 打开微信开发者工具
3. 导入项目，选择 `dist/dev/mp-weixin` 目录
4. 在 `manifest.json` 中配置你的小程序 AppID

## 页面说明

| 页面 | 路径 | 说明 |
|------|------|------|
| 登录 | `/pages/login/index` | 手机号 + 项目编号登录 |
| 概览 | `/pages/dashboard/index` | 项目总览、进度展示 |
| 排期 | `/pages/schedule/index` | 施工阶段时间线 |
| 设计 | `/pages/design/index` | 设计图库展示 |
| 品牌 | `/pages/brand/index` | 公司品牌介绍 |
| 我的 | `/pages/profile/index` | 个人中心 |
| 预算 | `/pages/budget/index` | 费用预算明细 |
| 清单 | `/pages/shopping/index` | 物料采购清单 |
| 日志 | `/pages/log/index` | 施工日志时间线 |

## API 接口

接口定义在 `src/api/index.js`，包含：

- 用户登录/信息
- 项目详情/进度
- 施工排期
- 设计方案
- 预算管理
- 物料清单
- 项目日志
- 品牌信息

## 自定义组件

### CustomTabBar

自定义底部导航组件，支持毛玻璃效果。

```vue
<template>
  <CustomTabBar :current="0" />
</template>
```

## 样式变量

在 `src/styles/variables.scss` 中定义：

```scss
$color-brand: #C40016;       // 品牌红
$color-black: #000000;       // 纯黑
$color-gray-900: #212121;    // 主文字
$color-gray-600: #757575;    // 次要文字
$color-white: #FFFFFF;       // 白色
$color-success: #10B981;     // 成功色
```

## 注意事项

1. **小程序 AppID**: 在 `src/manifest.json` 的 `mp-weixin.appid` 中配置
2. **API 地址**: 在 `src/utils/request.js` 的 `BASE_URL` 中配置
3. **样式兼容**: 毛玻璃效果在部分机型可能不支持，会自动降级

## License

MIT

