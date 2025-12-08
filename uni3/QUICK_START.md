# 快速开始 - 小程序登录功能

## 🎯 立即体验

### 方式1：使用开发者模式（推荐）

1. **启动项目**
   ```bash
   cd uni3
   npm run dev:mp-weixin
   ```

2. **打开微信开发者工具**
   - 导入项目
   - 选择 `uni3/dist/dev/mp-weixin` 目录

3. **使用开发者模式快速登录**
   - 在登录页面底部，点击"🔧 开发者模式"
   - 选择登录方式：
     - **👤 模拟客户登录** - 测试客户功能
     - **👨‍💼 模拟员工登录** - 测试员工功能（含专属功能）
     - **🚀 直接进入首页** - 快速跳过登录

4. **开始开发**
   - 现在你可以自由访问所有页面
   - 测试不同角色的权限
   - 开发新功能

### 方式2：使用真实登录（需要后端）

1. **配置API地址**
   
   编辑 `uni3/src/utils/request.js`：
   ```javascript
   const BASE_URL = 'http://localhost:8080'  // 改成你的后端地址
   ```

2. **启动后端服务**
   ```bash
   cd sb3
   mvn spring-boot:run
   ```

3. **使用真实登录**
   - 短信验证码登录
   - 密码登录
   - 微信登录（需要真机）

---

## 📁 项目结构

```
uni3/
├── src/
│   ├── pages/
│   │   └── login/
│   │       ├── index.vue          # 当前登录页（含开发者模式）
│   │       └── index-new.vue      # 新版登录页（三种登录方式）
│   ├── api/
│   │   └── auth.js                # 认证API
│   ├── store/
│   │   └── user.js                # 用户状态管理
│   ├── utils/
│   │   ├── request.js             # 请求封装（含Token刷新）
│   │   ├── device.js              # 设备ID管理
│   │   └── permission.js          # 权限判断
│   └── main.js                    # 路由守卫
├── DEV_MODE_GUIDE.md              # 开发者模式指南
└── QUICK_START.md                 # 本文件
```

---

## 🔧 开发者模式详解

### 三个快速入口

#### 1. 👤 模拟客户登录
```javascript
// 自动设置的数据
{
  userType: 'customer',
  userInfo: {
    id: 'C001',
    name: '张三（测试客户）',
    phone: '138****8000'
  },
  projects: [
    { id: 'P001', name: '万科城市花园A栋1001', phase: 'construction' },
    { id: 'P002', name: '碧桂园B栋2002', phase: 'design' }
  ]
}
```

**可以访问**：
- ✅ 首页
- ✅ 设计方案
- ✅ 施工排期
- ✅ 预算管理
- ✅ 质检记录
- ✅ 个人中心

**不能访问**：
- ❌ 工地巡视
- ❌ 问题上报
- ❌ 整改记录

#### 2. 👨‍💼 模拟员工登录
```javascript
// 自动设置的数据
{
  userType: 'staff',
  userInfo: {
    id: 'S001',
    name: '李工（测试员工）',
    phone: '139****9000'
  },
  projects: [
    { id: 'P001', name: '万科城市花园A栋1001', phase: 'construction' },
    { id: 'P003', name: '恒大御景C栋3003', phase: 'construction' }
  ]
}
```

**可以访问**：
- ✅ 所有客户功能
- ✅ 工地巡视（员工专属）
- ✅ 问题上报（员工专属）
- ✅ 整改记录（员工专属）

#### 3. 🚀 直接进入首页
- 设置最小登录状态
- 快速跳过登录流程
- 用于快速测试

---

## 🎨 测试不同角色权限

### 测试客户权限

1. 点击"👤 模拟客户登录"
2. 尝试访问员工专属页面：
   ```javascript
   uni.navigateTo({ url: '/pages/inspection/list' })
   ```
3. 应该被拦截，提示"该功能仅员工可用"

### 测试员工权限

1. 点击"👨‍💼 模拟员工登录"
2. 访问员工专属页面：
   ```javascript
   uni.navigateTo({ url: '/pages/inspection/list' })
   ```
3. 应该可以正常访问

---

## 🔐 用户状态管理

### 获取用户信息

```vue
<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 用户类型
const userType = computed(() => userStore.userType)  // 'customer' 或 'staff'

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 是否客户
const isCustomer = computed(() => userStore.isCustomer)

// 是否员工
const isStaff = computed(() => userStore.isStaff)

// 项目列表
const projects = computed(() => userStore.projects)

// 当前项目
const currentProject = computed(() => userStore.currentProject)
</script>

<template>
  <view>
    <text>用户类型: {{ userType }}</text>
    <text>用户名: {{ userInfo.name }}</text>
    
    <!-- 根据角色显示不同内容 -->
    <view v-if="isCustomer">客户专属内容</view>
    <view v-if="isStaff">员工专属内容</view>
  </view>
</template>
```

### 切换项目

```javascript
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 切换到项目P002
userStore.switchProject('P002')

// 获取当前项目
console.log(userStore.currentProject)
```

### 退出登录

```javascript
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 退出登录（会清除所有状态并跳转登录页）
userStore.logout()
```

---

## 🚀 开发新功能

### 1. 创建新页面

```vue
<!-- pages/example/index.vue -->
<template>
  <view class="container">
    <text>{{ userInfo.name }}</text>
    
    <!-- 只有员工可见 -->
    <button v-if="isStaff" @click="handleStaffAction">
      员工专属操作
    </button>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const isStaff = computed(() => userStore.isStaff)

const handleStaffAction = () => {
  console.log('员工专属操作')
}
</script>
```

### 2. 添加API接口

```javascript
// api/example.js
import { get, post } from '@/utils/request'

// 获取数据
export const getData = (params) => {
  return get('/app/example/data', params)
}

// 提交数据
export const submitData = (data) => {
  return post('/app/example/submit', data)
}
```

### 3. 使用API

```vue
<script setup>
import { ref } from 'vue'
import { getData, submitData } from '@/api/example'

const data = ref([])

// 获取数据
const fetchData = async () => {
  try {
    data.value = await getData({ page: 1 })
  } catch (error) {
    uni.showToast({ title: error.message, icon: 'none' })
  }
}

// 提交数据
const handleSubmit = async () => {
  try {
    await submitData({ name: 'test' })
    uni.showToast({ title: '提交成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message, icon: 'none' })
  }
}
</script>
```

---

## 🔍 调试技巧

### 1. 查看用户状态

在控制台执行：
```javascript
import { useUserStore } from '@/store/user'
const userStore = useUserStore()

console.log('用户类型:', userStore.userType)
console.log('用户信息:', userStore.userInfo)
console.log('项目列表:', userStore.projects)
console.log('当前项目:', userStore.currentProject)
console.log('是否客户:', userStore.isCustomer)
console.log('是否员工:', userStore.isStaff)
```

### 2. 测试权限

```javascript
import { hasPagePermission } from '@/utils/permission'

// 测试页面权限
console.log('客户能访问工地巡视吗?', hasPagePermission('/pages/inspection/list'))
```

### 3. 清除登录状态

```javascript
import { useUserStore } from '@/store/user'
const userStore = useUserStore()

// 清除登录状态
userStore.logout()
```

---

## ⚠️ 注意事项

### 1. 生产环境关闭开发者模式

在 `pages/login/index.vue` 中：
```javascript
const showDevMode = ref(false)  // 生产环境设为 false
```

### 2. 配置真实API地址

在 `utils/request.js` 中：
```javascript
// 生产环境
const BASE_URL = 'https://api.yourdomain.com'
```

### 3. 微信小程序配置

- 在微信公众平台配置服务器域名
- 开通"手机号快速验证组件"权限
- 配置AppID和AppSecret

---

## 📚 相关文档

- [DEV_MODE_GUIDE.md](./DEV_MODE_GUIDE.md) - 开发者模式详细指南
- [login-implementation-plan.md](../.kiro/specs/uniapp-customer-features/login-implementation-plan.md) - 完整实现计划
- [login-frontend-implementation.md](../.kiro/specs/uniapp-customer-features/login-frontend-implementation.md) - 前端实现说明
- [design-final.md](../.kiro/specs/uniapp-customer-features/design-final.md) - 技术设计方案

---

## 🆘 常见问题

### Q: 点击开发者模式按钮没反应？
**A**: 检查控制台错误，确保 `useUserStore` 已正确导入。

### Q: 进入首页后提示"请先登录"？
**A**: 检查 `main.js` 中的路由守卫是否正确配置。

### Q: 如何切换到新版登录页？
**A**: 
```bash
mv uni3/src/pages/login/index.vue uni3/src/pages/login/index-old.vue
mv uni3/src/pages/login/index-new.vue uni3/src/pages/login/index.vue
```

### Q: 开发者模式的Token能用于API请求吗？
**A**: 不能。开发者模式的Token是模拟的，需要配置后端才能真实请求。

---

**开始愉快地开发吧！** 🎉
