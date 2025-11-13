# 装修管理系统启动脚本说明

本文档说明如何使用项目根目录下 `scripts` 文件夹中的启动脚本。

## 📁 脚本结构

```
scripts/
├── 开发环境脚本/
│   ├── dev-start.sh           # Linux/Mac 开发环境启动
│   ├── dev-start.bat          # Windows 开发环境启动
│   ├── dev-stop.sh            # Linux/Mac 开发环境停止
│   └── dev-stop.bat           # Windows 开发环境停止
│
├── 生产环境脚本/
│   ├── prod-deploy.sh         # Linux/Mac 生产环境部署
│   ├── prod-service.sh        # Linux/Mac 生产环境服务管理
│   └── README.md              # 本说明文档
│
└── 数据库脚本/
    ├── init-db.sh             # Linux/Mac 数据库初始化
    └── init-db.bat            # Windows 数据库初始化
```

## 🚀 快速开始

### 1. 准备工作

确保系统已安装以下软件：

**开发环境必备：**
- Java 17+
- Maven 3.6+
- Node.js 16+
- npm 或 yarn
- MySQL 8.0+

**生产环境必备：**
- Java 17+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+
- Nginx (可选)
- systemd (Linux)

### 2. 初始化数据库

**Linux/Mac:**
```bash
# 使用默认配置
./scripts/init-db.sh

# 指定数据库参数
./scripts/init-db.sh -h localhost -u root -p123456 -d decoration_mgmt
```

**Windows:**
```cmd
rem 使用默认配置
scripts/init-db.bat

rem 指定数据库参数
scripts/init-db.bat -h localhost -u root -p123456 -d decoration_mgmt
```

### 3. 启动开发环境

**Linux/Mac:**
```bash
# 添加执行权限
chmod +x scripts/dev-*.sh

# 启动服务
./scripts/dev-start.sh
```

**Windows:**
```cmd
rem 启动服务
scripts\dev-start.bat
```

### 4. 启动生产环境（需要root权限）

**Linux/Mac:**
```bash
# 添加执行权限
chmod +x scripts/prod-*.sh

# 部署应用（首次执行）
sudo ./scripts/prod-deploy.sh

# 服务管理
./scripts/prod-service.sh
```

## 📖 详细使用说明

### 开发环境脚本

#### dev-start.sh / dev-start.bat
启动开发环境服务，支持交互式选择：

```
请选择启动模式：
1. 启动后端服务 (端口: 8080)
2. 启动前端服务 (端口: 80)
3. 同时启动后端和前端服务
4. 查看服务状态
```

**功能特点：**
- ✅ 自动检测端口占用
- ✅ 自动安装依赖
- ✅ 服务启动状态监控
- ✅ 彩色日志输出
- ✅ 后台运行支持

#### dev-stop.sh / dev-stop.bat
停止开发环境服务：

```
请选择停止模式：
1. 停止后端服务 (端口: 8080)
2. 停止前端服务 (端口: 80)
3. 停止所有服务
4. 清理日志文件
```

**功能特点：**
- ✅ 优雅停止服务
- ✅ 强制终止选项
- ✅ 日志文件清理
- ✅ 端口占用检测

### 生产环境脚本

#### prod-deploy.sh
生产环境自动化部署脚本，包含以下功能：

1. **环境检查** - 验证root权限和系统依赖
2. **用户创建** - 创建专用应用用户
3. **目录准备** - 创建应用目录结构
4. **后端构建** - Maven编译和打包
5. **前端构建** - Vue项目编译和打包
6. **应用部署** - 文件部署和权限设置
7. **服务配置** - 创建systemd服务
8. **Nginx配置** - 反向代理和静态文件
9. **防火墙配置** - 自动开放必要端口
10. **健康检查** - 验证部署结果

**执行要求：**
- 必须以root用户运行
- 系统需要安装Java、Maven、Node.js、Nginx

#### prod-service.sh
生产环境服务管理脚本，支持交互式和命令行模式：

**交互式模式：**
```bash
./scripts/prod-service.sh
```

**命令行模式：**
```bash
# 查看服务状态
./scripts/prod-service.sh status

# 启动服务
./scripts/prod-service.sh start

# 停止服务
./scripts/prod-service.sh stop

# 重启服务
./scripts/prod-service.sh restart

# 查看日志
./scripts/prod-service.sh logs 100

# 实时监控日志
./scripts/prod-service.sh tail

# 性能监控
./scripts/prod-service.sh monitor

# 健康检查
./scripts/prod-service.sh health

# 备份应用
./scripts/prod-service.sh backup
```

### 数据库初始化脚本

#### init-db.sh / init-db.bat
数据库初始化脚本，支持：

1. **连接测试** - 验证数据库连接
2. **数据库创建** - 自动创建数据库
3. **基础表初始化** - 若依框架基础表
4. **业务表初始化** - 装修管理系统业务表
5. **数据验证** - 检查表结构和完整性

**默认配置：**
- 主机：localhost
- 端口：3306
- 数据库：ruoyi_vue
- 用户：root
- 密码：（空）

**自定义配置示例：**
```bash
# 指定主机和用户
./scripts/init-db.sh -h 192.168.1.100 -u admin -p mypassword

# 指定数据库名称
./scripts/init-db.sh -d decoration_mgmt
```

## 🔧 配置说明

### 环境变量

脚本支持以下环境变量配置：

```bash
# Maven镜像加速
export MAVEN_OPTS="-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true"

# Node.js镜像加速
export NPM_CONFIG_REGISTRY=https://registry.npmmirror.com

# Java内存配置
export JAVA_OPTS="-Xms512m -Xmx1024m"
```

### 日志文件

开发环境日志位置：
```
project-root/
└── logs/
    ├── backend.log       # 后端服务日志
    ├── backend.pid       # 后端进程ID
    ├── frontend.log      # 前端服务日志
    └── frontend.pid      # 前端进程ID
```

生产环境日志位置：
```
/var/log/
├── decoration-mgmt.access.log  # Nginx访问日志
├── decoration-mgmt.error.log   # Nginx错误日志
└── journalctl -u decoration-mgmt  # 系统日志
```

### 应用目录结构

生产环境应用目录：
```
/opt/decoration-mgmt/
├── ruoyi-admin.jar              # 后端JAR文件
├── frontend/                    # 前端静态文件
├── logs/                        # 应用日志
├── config/                      # 配置文件
└── backups/                     # 备份文件
```

## 🌐 访问地址

### 开发环境
- 前端：http://localhost
- 后端API：http://localhost:8080/ruoyi-admin
- API文档：http://localhost:8080/ruoyi-admin/doc.html

### 生产环境
- 前端：http://your-server-ip
- 后端API：http://your-server-ip/ruoyi-admin
- API文档：http://your-server-ip/ruoyi-admin/doc.html

### 默认登录信息
- 用户名：admin
- 密码：admin123

**⚠️ 重要：请及时修改默认密码！**

## 🛠️ 故障排除

### 常见问题

**1. 端口被占用**
```bash
# 查看端口占用
netstat -tlnp | grep :8080

# 终止进程
kill -9 <PID>
```

**2. Maven依赖下载失败**
```bash
# 配置镜像加速
mvn -s ~/.m2/settings.xml compile

# 或者设置环境变量
export MAVEN_OPTS="-Dmaven.wagon.http.ssl.insecure=true"
```

**3. Node.js依赖安装失败**
```bash
# 清理缓存
npm cache clean --force
yarn cache clean

# 使用镜像源
npm install --registry=https://registry.npmmirror.com
```

**4. 数据库连接失败**
```bash
# 检查MySQL服务状态
systemctl status mysql

# 测试连接
mysql -h localhost -u root -p
```

**5. 权限问题**
```bash
# 赋予脚本执行权限
chmod +x scripts/*.sh

# 修复目录权限
chown -R app:app /opt/decoration-mgmt
```

### 日志查看

**开发环境：**
```bash
# 后端日志
tail -f logs/backend.log

# 前端日志
tail -f logs/frontend.log
```

**生产环境：**
```bash
# 应用日志
journalctl -u decoration-mgmt -f

# Nginx日志
tail -f /var/log/nginx/decoration-mgmt.error.log
```

### 服务管理

**生产环境服务管理：**
```bash
# 查看服务状态
systemctl status decoration-mgmt

# 启动服务
systemctl start decoration-mgmt

# 停止服务
systemctl stop decoration-mgmt

# 重启服务
systemctl restart decoration-mgmt

# 查看服务配置
systemctl cat decoration-mgmt
```

## 📞 支持与反馈

如果在脚本使用过程中遇到问题，请：

1. 查看本文档的故障排除部分
2. 检查日志文件获取详细错误信息
3. 确认系统环境是否符合要求
4. 联系技术支持团队

---

**祝您使用愉快！** 🎉
