# 逅时代装修管理系统 - 部署指南

## 目录

- [项目概述](#项目概述)
- [快速开始](#快速开始)
- [部署模式](#部署模式)
- [各服务部署说明](#各服务部署说明)
- [Nginx 配置](#nginx-配置)
- [环境要求](#环境要求)
- [目录结构](#目录结构)
- [环境变量配置](#环境变量配置)
- [常见问题](#常见问题)

---

## 项目概述

本项目包含三个独立部署的服务：

| 服务 | 目录 | 技术栈 | 部署方式 |
|------|------|--------|----------|
| 后端 API | `sb3/` | Spring Boot 3.5 + Java 17 + MySQL 8 | JAR 包运行 |
| 管理后台 | `vue3/` | Vue 3 + Vite + Element Plus | Nginx 静态托管 |
| 小程序/H5 | `uni3/` | UniApp + Vue 3 + uView Plus | 微信上传 / Nginx |

```
项目根目录/
├── sb3/                    # 后端服务源码
├── vue3/                   # 管理后台源码
├── uni3/                   # 小程序源码
└── scripts/deploy/         # 部署脚本
    ├── deploy-all.sh       # 统一部署入口
    ├── deploy-sb3.sh       # 后端部署脚本
    ├── deploy-vue3.sh      # 管理后台部署脚本
    ├── deploy-uni3.sh      # 小程序部署脚本
    ├── config.env.example  # 配置模板
    └── nginx/              # Nginx 配置文件
```

---

## 快速开始

### 1. 添加执行权限

```bash
cd scripts/deploy
chmod +x *.sh
```

### 2. 本地部署 (默认模式)

```bash
# 部署所有服务 (后端 + 管理后台)
./deploy-all.sh all

# 仅部署后端
./deploy-all.sh sb3

# 仅部署管理后台
./deploy-all.sh vue3

# 构建微信小程序 (需手动上传)
./deploy-all.sh uni3:mp

# 部署 H5 版本
./deploy-all.sh uni3:h5
```

### 3. 远程部署

```bash
# 部署所有服务到远程服务器
./deploy-all.sh -m remote --host 192.168.1.100 all

# 指定 SSH 用户和端口
./deploy-all.sh -m remote --host 192.168.1.100 --user deploy --port 2222 all

# 仅部署后端到远程
./deploy-sb3.sh --host 192.168.1.100 deploy

# 仅部署管理后台到远程
./deploy-vue3.sh --host 192.168.1.100 deploy
```

### 4. 仅构建不部署

```bash
# 仅构建后端 JAR 包
./deploy-all.sh -b sb3

# 仅构建管理后台
./deploy-all.sh -b vue3
```

---

## 部署模式

### 本地模式 (local)

- **默认模式**，无需额外参数
- 将服务部署到本机 `/opt/evs-home/` 目录
- 需要 `sudo` 权限
- 适用于：单机部署、开发测试

```bash
./deploy-sb3.sh deploy           # 本地部署后端
./deploy-vue3.sh deploy          # 本地部署前端
```

### 远程模式 (remote)

- 通过 SSH 将服务部署到远程服务器
- 需要配置 SSH 免密登录或指定密钥
- 适用于：生产环境、多服务器部署

```bash
./deploy-sb3.sh -m remote --host 192.168.1.100 deploy
./deploy-vue3.sh --host 192.168.1.100 deploy  # --host 自动切换到 remote 模式
```

---

## 各服务部署说明

### 后端服务 (sb3)

**技术栈:** Spring Boot 3.5.4 + Java 17 + MySQL 8.2  
**构建工具:** Maven  
**构建产物:** `sb3/ruoyi-admin/target/ruoyi-admin.jar`

#### 可用命令

| 命令 | 说明 |
|------|------|
| `build` | 仅构建 JAR 包 |
| `deploy` | 构建并部署 |
| `restart` | 重启服务 |
| `stop` | 停止服务 |
| `status` | 查看服务状态 |
| `logs` | 查看实时日志 |
| `rollback` | 回滚到上一版本 |

#### 使用示例

```bash
# 构建
./deploy-sb3.sh build

# 本地部署
./deploy-sb3.sh deploy

# 远程部署
./deploy-sb3.sh --host 192.168.1.100 deploy

# 指定 Spring 环境
./deploy-sb3.sh -e prod deploy
./deploy-sb3.sh -e test deploy

# 跳过构建直接部署 (使用已有 JAR)
./deploy-sb3.sh -s deploy

# 服务管理
./deploy-sb3.sh restart
./deploy-sb3.sh stop
./deploy-sb3.sh status
./deploy-sb3.sh logs

# 远程服务管理
./deploy-sb3.sh --host 192.168.1.100 restart
./deploy-sb3.sh --host 192.168.1.100 logs

# 回滚
./deploy-sb3.sh rollback
```

### 管理后台 (vue3)

**技术栈:** Vue 3.5 + Vite 6 + Element Plus 2.10  
**构建工具:** npm / Vite  
**构建产物:** `vue3/dist/` 静态文件目录

#### 可用命令

| 命令 | 说明 |
|------|------|
| `build` | 仅构建静态文件 |
| `deploy` | 构建并部署 |
| `nginx` | 部署 Nginx 配置 |
| `rollback` | 回滚到上一版本 |

#### 使用示例

```bash
# 构建
./deploy-vue3.sh build

# 本地部署
./deploy-vue3.sh deploy

# 远程部署
./deploy-vue3.sh --host 192.168.1.100 deploy

# 指定构建环境
./deploy-vue3.sh -e production deploy  # 生产环境
./deploy-vue3.sh -e staging deploy     # 测试环境

# 跳过构建直接部署
./deploy-vue3.sh -s deploy

# 部署 Nginx 配置
./deploy-vue3.sh nginx
./deploy-vue3.sh --host 192.168.1.100 nginx

# 回滚
./deploy-vue3.sh rollback
```

### 小程序 (uni3)

**技术栈:** UniApp + Vue 3.4 + uView Plus 3.2  
**构建工具:** npm / uni-cli  
**构建产物:**
- 微信小程序: `uni3/dist/build/mp-weixin/`
- H5: `uni3/dist/build/h5/`

#### 可用命令

| 命令 | 说明 |
|------|------|
| `build:mp` | 构建微信小程序 |
| `build:h5` | 构建 H5 版本 |
| `deploy:h5` | 构建并部署 H5 |

#### 使用示例

```bash
# 构建微信小程序
./deploy-uni3.sh build:mp
# 构建完成后，使用微信开发者工具打开 uni3/dist/build/mp-weixin 目录上传

# 构建 H5
./deploy-uni3.sh build:h5

# 本地部署 H5
./deploy-uni3.sh deploy:h5

# 远程部署 H5
./deploy-uni3.sh --host 192.168.1.100 deploy:h5
```

#### 微信小程序发布流程

1. 运行 `./deploy-uni3.sh build:mp` 构建
2. 打开微信开发者工具
3. 导入项目，选择 `uni3/dist/build/mp-weixin` 目录
4. 检查 AppID 配置 (在 `uni3/src/manifest.json` 中修改)
5. 点击"上传"提交审核

---

## 命令行参数

### 通用参数

| 参数 | 简写 | 说明 | 默认值 |
|------|------|------|--------|
| `--help` | `-h` | 显示帮助信息 | - |
| `--mode` | `-m` | 部署模式 (local/remote) | local |
| `--env` | `-e` | 环境 (dev/test/prod) | prod |
| `--skip-build` | `-s` | 跳过构建步骤 | false |
| `--host` | - | 远程服务器地址 | - |
| `--user` | - | SSH 用户名 | root |
| `--port` | - | SSH 端口 | 22 |
| `--path` | - | 自定义部署路径 | /opt/evs-home/* |

### 环境变量

也可以通过环境变量配置：

```bash
export DEPLOY_MODE=remote
export REMOTE_HOST=192.168.1.100
export REMOTE_USER=deploy
export REMOTE_PORT=22
export SSH_KEY=~/.ssh/id_rsa
export DEPLOY_PATH=/opt/evs-home/backend
export JAVA_OPTS="-Xms512m -Xmx1024m"
export SPRING_PROFILE=prod

./deploy-sb3.sh deploy
```

---

## Nginx 配置

配置文件位于 `nginx/` 目录：

| 文件 | 说明 |
|------|------|
| `evs-admin.conf` | 管理后台 HTTP 配置 |
| `evs-admin-ssl.conf` | 管理后台 HTTPS 配置 |
| `evs-h5.conf` | H5 小程序配置 |

### 配置说明

#### evs-admin.conf (管理后台)

```nginx
server {
    listen 80;
    server_name admin.example.com;  # ← 修改为实际域名
    
    root /opt/evs-home/admin;       # ← 静态文件目录
    
    # Vue Router History 模式
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API 代理
    location /prod-api/ {
        proxy_pass http://127.0.0.1:8080/;  # ← 后端服务地址
    }
}
```

### 部署 Nginx 配置

**方式一：使用脚本部署**

```bash
# 本地部署
./deploy-vue3.sh nginx

# 远程部署
./deploy-vue3.sh --host 192.168.1.100 nginx
```

**方式二：手动部署**

```bash
# 1. 编辑配置文件，修改域名和路径
vim nginx/evs-admin.conf

# 2. 复制到 Nginx 配置目录
sudo cp nginx/evs-admin.conf /etc/nginx/conf.d/

# 3. 测试配置
sudo nginx -t

# 4. 重载 Nginx
sudo nginx -s reload
```

### HTTPS 配置

1. 获取 SSL 证书 (推荐 Let's Encrypt)
2. 修改 `evs-admin-ssl.conf` 中的证书路径：

```nginx
ssl_certificate /etc/nginx/ssl/admin.example.com.pem;
ssl_certificate_key /etc/nginx/ssl/admin.example.com.key;
```

3. 部署配置：

```bash
sudo cp nginx/evs-admin-ssl.conf /etc/nginx/conf.d/evs-admin.conf
sudo nginx -t && sudo nginx -s reload
```

---

## 环境要求

### 构建环境 (开发机)

| 工具 | 版本要求 | 用途 |
|------|----------|------|
| Node.js | >= 18.0 | 前端构建 |
| npm | >= 9.0 | 包管理 |
| Java JDK | >= 17 | 后端构建 |
| Maven | >= 3.6 | 后端构建 |

### 运行环境 (服务器)

#### 后端服务器

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| Java JRE | >= 17 | 运行 JAR |
| MySQL | >= 8.0 | 数据库 |
| 内存 | >= 2GB | 推荐 4GB |
| 磁盘 | >= 10GB | 含日志空间 |

#### 前端服务器

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| Nginx | >= 1.18 | Web 服务器 |
| 磁盘 | >= 1GB | 静态文件 |

### 端口规划

| 服务 | 端口 | 说明 |
|------|------|------|
| 后端 API | 8080 | Spring Boot 默认端口 |
| 管理后台 | 80/443 | Nginx HTTP/HTTPS |
| H5 | 80/443 | Nginx HTTP/HTTPS |

---

## 目录结构

### 服务器目录结构

```
/opt/evs-home/
├── backend/                # 后端服务
│   └── ruoyi-admin.jar     # JAR 包
├── admin/                  # 管理后台静态文件
│   ├── index.html
│   ├── assets/
│   └── ...
├── h5/                     # H5 静态文件
│   ├── index.html
│   └── ...
├── logs/                   # 日志目录
│   └── app.log             # 后端日志
└── backup/                 # 备份目录
    ├── ruoyi-admin_20260111_120000.jar
    ├── admin/
    │   └── admin_20260111_120000.tar.gz
    └── h5/
        └── h5_20260111_120000.tar.gz
```

### 自定义部署路径

```bash
# 通过参数指定
./deploy-sb3.sh --path /data/app/backend deploy
./deploy-vue3.sh --path /data/app/admin deploy

# 通过环境变量指定
export DEPLOY_PATH=/data/app/backend
./deploy-sb3.sh deploy
```

---

## 环境变量配置

### 配置文件

脚本会自动加载 `scripts/deploy/config.env` 配置文件（如果存在）。

```bash
# 1. 复制配置模板
cp config.env.example config.env

# 2. 编辑配置
vim config.env

# 3. 运行脚本 (会自动加载 config.env)
./deploy-sb3.sh deploy
```

### 配置项说明

```bash
# ==================== 部署模式 ====================
DEPLOY_MODE=local              # local 或 remote

# ==================== 远程服务器配置 ====================
REMOTE_HOST=192.168.1.100      # 服务器 IP
REMOTE_USER=root               # SSH 用户
REMOTE_PORT=22                 # SSH 端口
SSH_KEY=~/.ssh/id_rsa          # SSH 私钥路径

# ==================== 部署路径 ====================
# 后端
BACKEND_DEPLOY_PATH=/opt/evs-home/backend
BACKEND_LOG_PATH=/opt/evs-home/logs

# 管理后台
ADMIN_DEPLOY_PATH=/opt/evs-home/admin

# H5
H5_DEPLOY_PATH=/opt/evs-home/h5

# 备份
BACKUP_PATH=/opt/evs-home/backup

# ==================== JVM 配置 ====================
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
SPRING_PROFILE=prod            # Spring 环境 (dev/test/prod)

# ==================== Nginx 配置 ====================
NGINX_CONF_PATH=/etc/nginx/conf.d
```

### 配置优先级

命令行参数 > config.env 配置文件 > 脚本默认值

```bash
# config.env 中设置了 REMOTE_HOST=192.168.1.100
# 但命令行指定 --host 会覆盖配置文件
./deploy-sb3.sh --host 192.168.1.200 deploy
```

---

## 常见问题

### 部署相关

**Q: 本地部署提示权限不足？**

本地部署需要 sudo 权限，确保当前用户在 sudoers 中：
```bash
sudo ./deploy-sb3.sh deploy
# 或将用户加入 sudo 组
```

**Q: 远程部署 SSH 连接失败？**

1. 检查服务器 IP 和端口是否正确
2. 确认 SSH 服务已启动
3. 检查防火墙是否开放 SSH 端口
4. 配置 SSH 免密登录：
```bash
ssh-copy-id -i ~/.ssh/id_rsa.pub user@server
```

**Q: 构建失败？**

1. 检查 Node.js/Java 版本是否满足要求
2. 清理缓存后重试：
```bash
# 前端
cd vue3 && rm -rf node_modules && npm install

# 后端
cd sb3 && mvn clean
```

### 运行相关

**Q: 后端服务启动失败？**

1. 检查 Java 版本：`java -version`
2. 检查端口占用：`lsof -i:8080`
3. 查看日志：`./deploy-sb3.sh logs`
4. 检查数据库连接配置

**Q: 管理后台无法访问？**

1. 检查 Nginx 是否启动：`systemctl status nginx`
2. 检查配置是否正确：`nginx -t`
3. 检查防火墙：`firewall-cmd --list-ports`
4. 检查静态文件是否存在：`ls /opt/evs-home/admin/`

**Q: API 请求返回 404？**

1. 检查后端服务是否运行：`./deploy-sb3.sh status`
2. 检查 Nginx 代理配置中的 `proxy_pass` 地址
3. 确认 API 路径前缀配置正确 (`/prod-api/`)

**Q: 如何回滚到上一版本？**

```bash
# 后端回滚
./deploy-sb3.sh rollback

# 前端回滚
./deploy-vue3.sh rollback

# 远程回滚
./deploy-sb3.sh --host 192.168.1.100 rollback
```

### 微信小程序相关

**Q: 小程序如何发布？**

1. 构建：`./deploy-uni3.sh build:mp`
2. 打开微信开发者工具
3. 导入 `uni3/dist/build/mp-weixin` 目录
4. 修改 AppID（首次需要）
5. 点击"上传"按钮

**Q: 小程序 AppID 在哪里配置？**

编辑 `uni3/src/manifest.json`：
```json
{
  "mp-weixin": {
    "appid": "wx1234567890abcdef"  // 替换为真实 AppID
  }
}
```

---

## 版本信息

| 组件 | 版本 |
|------|------|
| 后端 (sb3) | 3.9.0 |
| 管理后台 (vue3) | 3.9.0 |
| 小程序 (uni3) | 1.0.0 |
| 部署脚本 | 1.0.0 |

---

## 联系支持

如有问题，请检查日志或联系开发团队。
