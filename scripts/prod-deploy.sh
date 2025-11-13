#!/bin/bash
# ==========================================
# 装修管理系统 - 生产环境部署脚本
# ==========================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 配置变量
PROJECT_NAME="decoration-mgmt"
DEPLOY_USER="app"
APP_DIR="/opt/decoration-mgmt"
BACKUP_DIR="/opt/backups/decoration-mgmt"
SERVICE_USER="decoration"
JAR_FILE="ruoyi-admin.jar"
PID_FILE="/var/run/decoration-mgmt.pid"

# 检查是否为root用户
check_root() {
    if [[ $EUID -ne 0 ]]; then
        log_error "此脚本需要root权限运行"
        exit 1
    fi
}

# 创建应用用户
create_app_user() {
    log_info "创建应用用户 $SERVICE_USER..."
    if ! id "$SERVICE_USER" &>/dev/null; then
        useradd -r -s /bin/false $SERVICE_USER
        log_success "用户 $SERVICE_USER 创建成功"
    else
        log_info "用户 $SERVICE_USER 已存在"
    fi
}

# 创建目录结构
create_directories() {
    log_info "创建应用目录结构..."
    mkdir -p $APP_DIR/{logs,config,backups}
    mkdir -p $BACKUP_DIR
    chmod 755 $APP_DIR
    chmod 755 $APP_DIR/logs
    chmod 755 $APP_DIR/config
    chmod 755 $APP_DIR/backups
    log_success "目录结构创建完成"
}

# 构建后端
build_backend() {
    log_info "构建后端应用..."
    cd "$(dirname "$0")/../sb3"

    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装"
        exit 1
    fi

    # 编译和打包
    mvn clean package -DskipTests -Pprod
    if [ $? -ne 0 ]; then
        log_error "后端构建失败"
        exit 1
    fi

    # 复制JAR文件
    cp ruoyi-admin/target/ruoyi-admin.jar $APP_DIR/$JAR_FILE
    chown $SERVICE_USER:$SERVICE_USER $APP_DIR/$JAR_FILE
    chmod 755 $APP_DIR/$JAR_FILE
    log_success "后端构建完成"
}

# 构建前端
build_frontend() {
    log_info "构建前端应用..."
    cd "$(dirname "$0")/../vue3"

    # 检查Node.js
    if ! command -v node &> /dev/null; then
        log_error "Node.js未安装"
        exit 1
    fi

    # 安装依赖
    if [ -f "yarn.lock" ]; then
        yarn install --production --registry=https://registry.npmmirror.com
        yarn build:prod
    else
        npm install --production --registry=https://registry.npmmirror.com
        npm run build:prod
    fi

    if [ $? -ne 0 ]; then
        log_error "前端构建失败"
        exit 1
    fi

    # 复制构建结果
    rm -rf $APP_DIR/frontend/*
    cp -r dist/* $APP_DIR/frontend/
    chown -R $SERVICE_USER:$SERVICE_USER $APP_DIR/frontend
    chmod -R 755 $APP_DIR/frontend
    log_success "前端构建完成"
}

# 备份当前版本
backup_current() {
    if [ -f "$APP_DIR/$JAR_FILE" ]; then
        log_info "备份当前版本..."
        BACKUP_NAME="decoration-mgmt-$(date +%Y%m%d_%H%M%S).tar.gz"
        tar -czf $BACKUP_DIR/$BACKUP_NAME -C $APP_DIR .
        log_success "备份完成: $BACKUP_DIR/$BACKUP_NAME"
    fi
}

# 部署应用
deploy_app() {
    log_info "部署应用..."
    backup_current

    # 设置权限
    chown -R $SERVICE_USER:$SERVICE_USER $APP_DIR
    chmod -R 755 $APP_DIR

    # 创建systemd服务文件
    create_systemd_service

    # 重载systemd
    systemctl daemon-reload

    # 启动服务
    systemctl enable decoration-mgmt
    systemctl start decoration-mgmt

    # 等待服务启动
    sleep 10
    if systemctl is-active --quiet decoration-mgmt; then
        log_success "应用部署成功"
    else
        log_error "应用启动失败"
        systemctl status decoration-mgmt
        exit 1
    fi
}

# 创建systemd服务文件
create_systemd_service() {
    log_info "创建systemd服务..."

    cat > /etc/systemd/system/decoration-mgmt.service << EOF
[Unit]
Description=Decoration Management System
After=network.target mysql.service

[Service]
Type=simple
User=$SERVICE_USER
ExecStart=/usr/bin/java -jar -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m $APP_DIR/$JAR_FILE
WorkingDirectory=$APP_DIR
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=decoration-mgmt

# 环境变量
Environment=JAVA_OPTS="-Duser.timezone=Asia/Shanghai"
Environment=SPRING_PROFILES_ACTIVE=prod

# 日志
LogRateLimitIntervalSec=0
LogRateLimitBurst=0

[Install]
WantedBy=multi-user.target
EOF

    log_success "systemd服务文件创建完成"
}

# 配置Nginx
setup_nginx() {
    log_info "配置Nginx..."

    # 检查Nginx是否安装
    if ! command -v nginx &> /dev/null; then
        log_warning "Nginx未安装，跳过Nginx配置"
        return 0
    fi

    # 创建Nginx配置文件
    cat > /etc/nginx/conf.d/decoration-mgmt.conf << EOF
server {
    listen 80;
    server_name localhost;

    # 前端静态文件
    location / {
        root $APP_DIR/frontend;
        index index.html;
        try_files \$uri \$uri/ /index.html;
    }

    # 后端API
    location /ruoyi-admin/ {
        proxy_pass http://127.0.0.1:8080/ruoyi-admin/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 30;
        proxy_send_timeout 30;
        proxy_read_timeout 30;
    }

    # 日志
    access_log /var/log/nginx/decoration-mgmt.access.log;
    error_log /var/log/nginx/decoration-mgmt.error.log;
}
EOF

    # 测试配置并重启Nginx
    nginx -t
    if [ $? -eq 0 ]; then
        systemctl restart nginx
        systemctl enable nginx
        log_success "Nginx配置完成"
    else
        log_error "Nginx配置测试失败"
        exit 1
    fi
}

# 配置防火墙
setup_firewall() {
    log_info "配置防火墙..."

    # 检查防火墙类型
    if command -v ufw &> /dev/null; then
        # Ubuntu/Debian UFW
        ufw allow 80/tcp
        ufw allow 443/tcp
        log_success "UFW防火墙配置完成"
    elif command -v firewall-cmd &> /dev/null; then
        # CentOS/RHEL firewalld
        firewall-cmd --permanent --add-service=http
        firewall-cmd --permanent --add-service=https
        firewall-cmd --reload
        log_success "firewalld防火墙配置完成"
    else
        log_warning "未检测到防火墙，请手动开放80和443端口"
    fi
}

# 健康检查
health_check() {
    log_info "执行健康检查..."

    # 检查应用状态
    if systemctl is-active --quiet decoration-mgmt; then
        log_success "应用服务运行正常"
    else
        log_error "应用服务未运行"
        return 1
    fi

    # 检查端口
    if netstat -tlnp | grep -q ":8080"; then
        log_success "应用端口监听正常"
    else
        log_error "应用端口未监听"
        return 1
    fi

    # HTTP健康检查
    sleep 5
    if curl -f http://localhost:8080/ruoyi-admin/doc.html > /dev/null 2>&1; then
        log_success "HTTP健康检查通过"
    else
        log_warning "HTTP健康检查失败"
    fi

    log_success "健康检查完成"
}

# 主函数
main() {
    echo "=========================================="
    echo "    装修管理系统 - 生产环境部署脚本"
    echo "=========================================="
    echo ""

    # 确认部署
    read -p "此脚本将执行生产环境部署，是否继续? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "部署已取消"
        exit 0
    fi

    # 检查依赖
    check_root
    create_app_user
    create_directories

    # 构建和部署
    build_backend
    build_frontend
    deploy_app
    setup_nginx
    setup_firewall
    health_check

    echo ""
    echo "=========================================="
    echo "            部署完成！"
    echo "=========================================="
    echo "前端地址: http://$(hostname -I | awk '{print $1}')"
    echo "API地址: http://$(hostname -I | awk '{print $1}')/ruoyi-admin"
    echo "API文档: http://$(hostname -I | awk '{print $1}')/ruoyi-admin/doc.html"
    echo ""
    echo "服务管理命令:"
    echo "启动: systemctl start decoration-mgmt"
    echo "停止: systemctl stop decoration-mgmt"
    echo "状态: systemctl status decoration-mgmt"
    echo "日志: journalctl -u decoration-mgmt -f"
    echo ""
    echo "默认登录账号: admin/admin123"
    echo "请及时修改默认密码！"
    echo "=========================================="
}

# 执行主函数
main "$@"
