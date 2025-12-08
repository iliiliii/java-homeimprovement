# 开发者模式使用指南

## 功能说明

为了方便开发调试，登录页面添加了**开发者模式**，提供三个快速入口按钮：

### 1. 👤 模拟客户登录
- 自动设置客户角色（`userType: 'customer'`）
- 模拟客户信息（张三）
- 提供2个测试项目
- 直接进入首页

### 2. 👨‍💼 模拟员工登录
- 自动设置员工角色（`userType: 'staff'`）
- 模拟员工信息（李工）
- 提供2个测试项目
- 直接进入首页
- 可以访问员工专属功能（工地巡视、问题上报、整改记录）

### 3. 🚀 直接进入首页
- 设置最小登录状态
- 快速跳过登录流程
- 用于测试首页和其他功能

## 使用方法

1. **打开登录页面**
   - 在页面底部会看到橙色的"🔧 开发者模式"面板

2. **展开面板**
   - 点击"🔧 开发者模式"展开/收起面板

3. **选择登录方式**
   - 点击对应的按钮即可快速登录

## 模拟数据说明

### 客户登录模拟数据
```javascript
{
  userType: 'customer',
  userInfo: {
    id: 'C001',
    name: '张三（测试客户）',
    phone: '138****8000'
  },
  projects: [
    {
      id: 'P001',
      code: 'P2025001',
      name: '万科城市花园A栋1001',
      status: 'construction',
      phase: 'construction'
    },
    {
      id: 'P002',
      code: 'P2025002',
      name: '碧桂园B栋2002',
      status: 'design',
      phase: 'design'
    }
  ]
}
```

### 员工登录模拟数据
```javascript
{
  userType: 'staff',
  userInfo: {
    id: 'S001',
    name: '李工（测试员工）',
    phone: '139****9000'
  },
  projects: [
    {
      id: 'P001',
      code: 'P2025001',
      name: '万科城市花园A栋1001',
      status: 'construction',
      phase: 'construction'
    },
    {
      id: 'P003',
      code: 'P2025003',
      name: '恒大御景C栋3003',
      status: 'construction',
      phase: 'construction'
    }
  ]
}
```

## 权限测试

使用开发者模式可以快速测试不同角色的权限：

### 客户权限
- ✅ 可以访问：首页、设计方案、施工排期、预算管理、质检记录、个人中心
- ❌ 不能访问：工地巡视、问题上报、整改记录

### 员工权限
- ✅ 可以访问：所有页面（包括员工专属功能）

## 开启/关闭开发者模式

### 开启（开发环境）
在 `uni3/src/pages/login/index.vue` 中：
```javascript
const showDevMode = ref(true)  // 设为 true
```

### 关闭（生产环境）
在 `uni3/src/pages/login/index.vue` 中：
```javascript
const showDevMode = ref(false)  // 设为 false
```

**⚠️ 重要提示**：
- 生产环境上线前，务必将 `showDevMode` 设为 `false`
- 或者直接删除开发者模式相关代码

## 注意事项

1. **仅用于开发调试**
   - 开发者模式仅用于本地开发和测试
   - 不要在生产环境中启用

2. **模拟Token**
   - 开发者模式生成的Token是模拟的
   - 不能用于真实的API请求
   - 如需测试API，请使用真实登录

3. **路由守卫**
   - 开发者模式会设置最小登录状态
   - 可以通过路由守卫的检查
   - 但API请求可能会失败（因为Token是模拟的）

4. **数据持久化**
   - 开发者模式的登录状态会保存到本地存储
   - 刷新页面后仍然保持登录状态
   - 如需清除，可以点击"退出登录"或清除缓存

## 调试技巧

### 1. 测试客户权限
```javascript
// 点击"模拟客户登录"
// 然后尝试访问员工专属页面
uni.navigateTo({ url: '/pages/inspection/list' })
// 应该被拦截，提示"该功能仅员工可用"
```

### 2. 测试员工权限
```javascript
// 点击"模拟员工登录"
// 然后访问员工专属页面
uni.navigateTo({ url: '/pages/inspection/list' })
// 应该可以正常访问
```

### 3. 测试项目切换
```javascript
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 查看当前项目
console.log(userStore.currentProject)

// 切换项目
userStore.switchProject('P002')
```

### 4. 查看用户状态
```javascript
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

console.log('用户类型:', userStore.userType)
console.log('是否客户:', userStore.isCustomer)
console.log('是否员工:', userStore.isStaff)
console.log('用户信息:', userStore.userInfo)
console.log('项目列表:', userStore.projects)
```

## 常见问题

### Q1: 点击按钮后没有反应？
**A**: 检查控制台是否有错误，确保 `useUserStore` 已正确导入。

### Q2: 进入首页后显示"请先登录"？
**A**: 检查路由守卫配置，确保 `main.js` 中的路由拦截器已正确设置。

### Q3: 如何清除开发者模式的登录状态？
**A**: 
- 方法1: 在个人中心点击"退出登录"
- 方法2: 在开发者工具中清除缓存
- 方法3: 调用 `userStore.logout()`

### Q4: 开发者模式的Token能用于API请求吗？
**A**: 不能。开发者模式的Token是模拟的，后端无法验证。如需测试API，请使用真实登录或配置后端的测试Token。

## 后续优化建议

1. **添加更多测试角色**
   - 可以添加更多不同权限的测试账号
   - 例如：设计师、项目经理、监理等

2. **自定义测试数据**
   - 可以添加输入框，让开发者自定义测试数据
   - 例如：自定义用户名、项目数量等

3. **环境自动检测**
   - 根据环境变量自动开启/关闭开发者模式
   - 例如：`process.env.NODE_ENV === 'development'`

4. **快捷键支持**
   - 添加快捷键快速切换角色
   - 例如：长按Logo 3秒显示开发者模式

---

**祝开发顺利！** 🚀
