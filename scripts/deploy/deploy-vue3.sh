#!/bin/bash
#====================================================================
# 管理后台 (vue3) 发布脚本
# 项目: 逅时代装修管理系统 - Vue3 管理后台
# 版本: 3.9.0
# 支持: 本地部署 / 远程部署
#====================================================================

set -e

# ==================== 配置区域 ====================
PROJECT_NAME="evs-home-admin"
VERSION="3.9.0"

# 部署模式: local / remote
DEPLOY_MODE="${DEPLOY_MODE:-local}"

# 远程服务器配置
REMOTE_HOST="${REMOTE_HOST:-}"
REMOTE_USER="${REMOTE_USER:-root}"
REMOTE_PORT="${REMOTE_PORT:-22}"
SSH_KEY="${SSH_KEY:-~/.ssh/id_rsa}"

# 部署路径
DEPLOY_PATH="${DEPLOY_PATH:-/opt/evs-home/admin}"
BACKUP_PATH="${BACKUP_PATH:-/opt/evs-home/backup/admin}"
NGINX_CONF_PATH="${NGINX_CONF_PATH:-/etc/nginx/conf.d}"

# 本地路径
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
VUE3_DIR="$PROJECT_ROOT/vue3"

# ==================== 颜色输出 ====================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ==================== 函数定义 ====================

show_help() {
    cat << EOF
用法: $0 [选项] [命令]

命令:
  build       仅构建项目
  deploy      构建并部署
  nginx       部署 Nginx 配置
  rollback    回滚到上一版本

选项:
  -h, --help          显示帮助信息
  -m, --mode MODE     部署模式 (local/remote), 默认: local
  -e, --env ENV       构建环境 (staging/production), 默认: production
  -s, --skip-build    跳过构建步骤
  --host HOST         远程服务器地址 (remote 模式必需)
  --user USER         SSH 用户名, 默认: root
  --port PORT         SSH 端口, 默认: 22
  --path PATH         部署路径, 默认: /opt/evs-home/admin

示例:
  $0 build                              # 仅构建
  $0 deploy                             # 本地部署
  $0 -m remote --host 1.2.3.4 deploy    # 远程部署
  $0 nginx                              # 部署 Nginx 配置
EOF
}

check_requirements() {
    log_info "检查环境依赖..."
    
    if ! command -v node &> /dev/null; then
        log_error "未找到 Node.js，请安装 Node.js 18+"
        exit 1
    fi
    
    if [ ! -f "$VUE3_DIR/package.json" ]; then
        log_error "未找到 vue3 项目目录: $VUE3_DIR"
        exit 1
    fi
    
    log_success "环境检查通过"
}

install_dependencies() {
    log_info "安装依赖..."
    cd "$VUE3_DIR"
    [ -f "package-lock.json" ] && npm ci || npm install
    log_success "依赖安装完成"
}

build_project() {
    log_info "开始构建管理后台..."
    cd "$VUE3_DIR"
    
    install_dependencies
    
    log_info "执行构建..."
    [ "$BUILD_ENV" = "staging" ] && npm run build:stage || npm run build:prod
    
    DIST_DIR="$VUE3_DIR/dist"
    if [ ! -d "$DIST_DIR" ] || [ -z "$(ls -A $DIST_DIR)" ]; then
        log_error "构建失败，dist 目录为空"
        exit 1
    fi
    
    log_success "构建完成: $DIST_DIR"
    echo "文件数量: $(find $DIST_DIR -type f | wc -l)"
    echo "总大小: $(du -sh $DIST_DIR | cut -f1)"
}

# ==================== 本地部署函数 ====================

local_deploy() {
    log_info "本地部署模式..."
    
    DIST_DIR="$VUE3_DIR/dist"
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    
    # 创建目录
    sudo mkdir -p "$DEPLOY_PATH" "$BACKUP_PATH"
    
    # 备份旧版本
    if [ -d "$DEPLOY_PATH" ] && [ "$(ls -A $DEPLOY_PATH 2>/dev/null)" ]; then
        log_info "备份当前版本..."
        sudo tar -czf "$BACKUP_PATH/admin_$TIMESTAMP.tar.gz" -C "$DEPLOY_PATH" .
    fi
    
    # 清空并复制新版本
    log_info "部署静态文件..."
    sudo rm -rf "$DEPLOY_PATH"/*
    sudo cp -r "$DIST_DIR"/* "$DEPLOY_PATH/"
    sudo chmod -R 755 "$DEPLOY_PATH"
    
    log_success "本地部署完成!"
    log_info "静态文件已部署到: $DEPLOY_PATH"
    log_info "请确保 Nginx 配置正确指向此目录"
}

local_nginx() {
    log_info "部署本地 Nginx 配置..."
    
    NGINX_CONF_FILE="$SCRIPT_DIR/nginx/evs-admin.conf"
    
    if [ ! -f "$NGINX_CONF_FILE" ]; then
        log_error "未找到 Nginx 配置文件: $NGINX_CONF_FILE"
        exit 1
    fi
    
    sudo cp "$NGINX_CONF_FILE" "$NGINX_CONF_PATH/"
    
    log_info "测试 Nginx 配置..."
    if sudo nginx -t; then
        sudo nginx -s reload
        log_success "Nginx 配置已重载"
    else
        log_error "Nginx 配置测试失败"
        exit 1
    fi
}

local_rollback() {
    log_info "回滚到上一版本..."
    
    LATEST_BACKUP=$(ls -t "$BACKUP_PATH"/*.tar.gz 2>/dev/null | head -1)
    if [ -z "$LATEST_BACKUP" ]; then
        log_error "未找到备份文件"
        exit 1
    fi
    
    log_info "回滚到: $LATEST_BACKUP"
    sudo rm -rf "$DEPLOY_PATH"/*
    sudo tar -xzf "$LATEST_BACKUP" -C "$DEPLOY_PATH"
    sudo chmod -R 755 "$DEPLOY_PATH"
    
    log_success "回滚完成"
}

# ==================== 远程部署函数 ====================

get_ssh_opts() {
    SSH_OPTS="-o StrictHostKeyChecking=no -p $REMOTE_PORT"
    [ -f "$SSH_KEY" ] && SSH_OPTS="$SSH_OPTS -i $SSH_KEY"
    echo "$SSH_OPTS"
}

remote_deploy() {
    log_info "远程部署模式: $REMOTE_HOST..."
    
    if [ -z "$REMOTE_HOST" ]; then
        log_error "远程模式需要指定 --host 参数"
        exit 1
    fi
    
    DIST_DIR="$VUE3_DIR/dist"
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    SSH_OPTS=$(get_ssh_opts)
    
    # 创建远程目录
    log_info "创建远程目录..."
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "mkdir -p $DEPLOY_PATH $BACKUP_PATH"
    
    # 备份旧版本
    log_info "备份当前版本..."
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "
        if [ -d '$DEPLOY_PATH' ] && [ \"\$(ls -A $DEPLOY_PATH 2>/dev/null)\" ]; then
            tar -czf '$BACKUP_PATH/admin_$TIMESTAMP.tar.gz' -C '$DEPLOY_PATH' .
        fi
        rm -rf $DEPLOY_PATH/*
    "
    
    # 上传新版本
    log_info "上传静态文件..."
    scp -r $SSH_OPTS "$DIST_DIR"/* "$REMOTE_USER@$REMOTE_HOST:$DEPLOY_PATH/"
    
    # 设置权限
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "
        chmod -R 755 $DEPLOY_PATH
        chown -R nginx:nginx $DEPLOY_PATH 2>/dev/null || true
    "
    
    log_success "远程部署完成!"
}

remote_nginx() {
    log_info "部署远程 Nginx 配置..."
    
    if [ -z "$REMOTE_HOST" ]; then
        log_error "远程模式需要指定 --host 参数"
        exit 1
    fi
    
    SSH_OPTS=$(get_ssh_opts)
    NGINX_CONF_FILE="$SCRIPT_DIR/nginx/evs-admin.conf"
    
    if [ ! -f "$NGINX_CONF_FILE" ]; then
        log_error "未找到 Nginx 配置文件: $NGINX_CONF_FILE"
        exit 1
    fi
    
    scp $SSH_OPTS "$NGINX_CONF_FILE" "$REMOTE_USER@$REMOTE_HOST:$NGINX_CONF_PATH/"
    
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "
        nginx -t && nginx -s reload && echo 'Nginx 配置已重载' || exit 1
    "
    
    log_success "远程 Nginx 配置部署完成"
}

remote_rollback() {
    log_info "远程回滚..."
    SSH_OPTS=$(get_ssh_opts)
    
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "
        LATEST=\$(ls -t $BACKUP_PATH/*.tar.gz 2>/dev/null | head -1)
        if [ -z \"\$LATEST\" ]; then echo '未找到备份'; exit 1; fi
        echo '回滚到: '\$LATEST
        rm -rf $DEPLOY_PATH/*
        tar -xzf \"\$LATEST\" -C $DEPLOY_PATH
        chmod -R 755 $DEPLOY_PATH
    "
    
    log_success "远程回滚完成"
}

# ==================== 主程序 ====================

SKIP_BUILD=false
BUILD_ENV="production"
COMMAND=""

# 解析参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help) show_help; exit 0 ;;
        -m|--mode) DEPLOY_MODE="$2"; shift 2 ;;
        -e|--env) BUILD_ENV="$2"; shift 2 ;;
        -s|--skip-build) SKIP_BUILD=true; shift ;;
        --host) REMOTE_HOST="$2"; DEPLOY_MODE="remote"; shift 2 ;;
        --user) REMOTE_USER="$2"; shift 2 ;;
        --port) REMOTE_PORT="$2"; shift 2 ;;
        --path) DEPLOY_PATH="$2"; shift 2 ;;
        build|deploy|nginx|rollback) COMMAND="$1"; shift ;;
        *) log_error "未知参数: $1"; show_help; exit 1 ;;
    esac
done

[ -z "$COMMAND" ] && COMMAND="deploy"

log_info "部署模式: $DEPLOY_MODE | 构建环境: $BUILD_ENV"

case $COMMAND in
    build)
        check_requirements
        build_project
        ;;
    deploy)
        check_requirements
        [ "$SKIP_BUILD" = false ] && build_project
        [ "$DEPLOY_MODE" = "remote" ] && remote_deploy || local_deploy
        ;;
    nginx)
        [ "$DEPLOY_MODE" = "remote" ] && remote_nginx || local_nginx
        ;;
    rollback)
        [ "$DEPLOY_MODE" = "remote" ] && remote_rollback || local_rollback
        ;;
esac
