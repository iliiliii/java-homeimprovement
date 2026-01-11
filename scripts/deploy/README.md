# 部署指南

## 快速开始

```bash
cd scripts/deploy
chmod +x *.sh

# 后端
./sb3.sh deploy      # 构建并启动
./sb3.sh status      # 查看状态
./sb3.sh log         # 查看日志

# 管理后台
./vue3.sh deploy     # 构建并部署

# 小程序
./uni3.sh mp         # 构建微信小程序
./uni3.sh deploy:h5  # 构建并部署 H5
```

## 配置

编辑 `config.env` 修改部署路径：

```bash
BACKEND_PATH=/opt/evs-home/backend
BACKEND_LOG=/opt/evs-home/logs/backend.log
ADMIN_PATH=/opt/evs-home/admin
H5_PATH=/opt/evs-home/h5
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
```

## 命令说明

### 后端 (sb3.sh)

| 命令 | 说明 |
|------|------|
| `build` | 构建 JAR 包 |
| `start` | 启动服务 |
| `stop` | 停止服务 |
| `restart` | 重启服务 |
| `status` | 查看状态 |
| `log` | 查看日志 |
| `deploy` | 构建并启动 |

### 管理后台 (vue3.sh)

| 命令 | 说明 |
|------|------|
| `build` | 构建静态文件 |
| `deploy` | 构建并部署 |
| `nginx` | 显示 Nginx 配置说明 |

### 小程序 (uni3.sh)

| 命令 | 说明 |
|------|------|
| `mp` | 构建微信小程序 |
| `h5` | 构建 H5 |
| `deploy:h5` | 构建并部署 H5 |

## Nginx 配置

```bash
# 管理后台
cp nginx/evs-admin.conf /etc/nginx/conf.d/
# 修改 server_name 和 root 路径

# H5
cp nginx/evs-h5.conf /etc/nginx/conf.d/

# 测试并重载
nginx -t && nginx -s reload
```

## 目录结构

```
/opt/evs-home/
├── backend/           # 后端 JAR
├── admin/             # 管理后台静态文件
├── h5/                # H5 静态文件
└── logs/              # 日志
```

## 环境要求

- Java 17+
- Node.js 18+
- Maven 3.6+
- Nginx 1.18+
