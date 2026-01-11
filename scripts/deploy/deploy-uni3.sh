#!/bin/bash
#====================================================================
# 小程序 (uni3) 发布脚本
# 项目: 逅时代装修管理系统 - UniApp 小程序
# 版本: 1.0.0
# 支持: 本地部署 / 远程部署
#====================================================================

set -e

# 本地路径 (需要先定义，用于加载配置文件)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
UNI3_DIR="$PROJECT_ROOT/uni3"

# ==================== 加载配置文件 ====================
CONFIG_FILE="$SCRIPT_DIR/config.env"
if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
    echo -e "\033[0;34m[INFO]\033[0m 已加载配置文件: $CONFIG_FILE"
fi

# ==================== 配置区域 ====================
PROJECT_NAME="evs-home-mp"
VERSION="1.0.0"

# 部署模式: local / remote
DEPLOY_MODE="${DEPLOY_MODE:-local}"

# 远程服务器配置 (H5 部署时使用)
REMOTE_HOST="${REMOTE_HOST:-}"
REMOTE_USER="${REMOTE_USER:-root}"
REMOTE_PORT="${REMOTE_PORT:-22}"
SSH_KEY="${SSH_KEY:-~/.ssh/id_rsa}"

# 部署路径 (优先使用 H5_* 变量)
DEPLOY_PATH="${H5_DEPLOY_PATH:-${DEPLOY_PATH:-/opt/evs-home/h5}}"
BACKUP_PATH="${BACKUP_PATH:-/opt/evs-home/backup}/h5"

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
  build:mp      构建微信小程序
  build:h5      构建 H5 版本
  deploy:h5     构建并部署 H5

选项:
  -h, --help          显示帮助信息
  -m, --mode MODE     部署模式 (local/remote), 默认: local
  -s, --skip-build    跳过构建步骤
  --host HOST         远程服务器地址 (remote 模式)
  --user USER         SSH 用户名, 默认: root
  --path PATH         部署路径, 默认: /opt/evs-home/h5

示例:
  $0 build:mp                           # 构建微信小程序
  $0 build:h5                           # 构建 H5 版本
  $0 deploy:h5                          # 本地部署 H5
  $0 -m remote --host 1.2.3.4 deploy:h5 # 远程部署 H5
EOF
}

check_requirements() {
    log_info "检查环境依赖..."
    
    if ! command -v node &> /dev/null; then
        log_error "未找到 Node.js，请安装 Node.js 18+"
        exit 1
    fi
    
    if [ ! -f "$UNI3_DIR/package.json" ]; then
        log_error "未找到 uni3 项目目录: $UNI3_DIR"
        exit 1
    fi
    
    log_success "环境检查通过"
}

install_dependencies() {
    log_info "安装依赖..."
    cd "$UNI3_DIR"
    [ -f "package-lock.json" ] && npm ci || npm install
    log_success "依赖安装完成"
}

build_mp_weixin() {
    log_info "开始构建微信小程序..."
    cd "$UNI3_DIR"
    
    install_dependencies
    npm run build:mp-weixin
    
    DIST_DIR="$UNI3_DIR/dist/build/mp-weixin"
    if [ ! -d "$DIST_DIR" ]; then
        log_error "构建失败，未找到输出目录"
        exit 1
    fi
    
    log_success "微信小程序构建完成: $DIST_DIR"
    echo ""
    echo "后续步骤:"
    echo "1. 打开微信开发者工具"
    echo "2. 导入项目: $DIST_DIR"
    echo "3. 点击上传按钮提交审核"
}

build_h5() {
    log_info "开始构建 H5 版本..."
    cd "$UNI3_DIR"
    
    install_dependencies
    npm run build:h5
    
    DIST_DIR="$UNI3_DIR/dist/build/h5"
    if [ ! -d "$DIST_DIR" ]; then
        log_error "构建失败，未找到输出目录"
        exit 1
    fi
    
    log_success "H5 构建完成: $DIST_DIR"
}

# ==================== 本地部署函数 ====================

local_deploy_h5() {
    log_info "本地部署 H5..."
    
    DIST_DIR="$UNI3_DIR/dist/build/h5"
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    
    # 创建目录
    sudo mkdir -p "$DEPLOY_PATH" "$BACKUP_PATH"
    
    # 备份旧版本
    if [ -d "$DEPLOY_PATH" ] && [ "$(ls -A $DEPLOY_PATH 2>/dev/null)" ]; then
        log_info "备份当前版本..."
        sudo tar -czf "$BACKUP_PATH/h5_$TIMESTAMP.tar.gz" -C "$DEPLOY_PATH" .
    fi
    
    # 清空并复制新版本
    log_info "部署静态文件..."
    sudo rm -rf "$DEPLOY_PATH"/*
    sudo cp -r "$DIST_DIR"/* "$DEPLOY_PATH/"
    sudo chmod -R 755 "$DEPLOY_PATH"
    
    log_success "本地 H5 部署完成!"
    log_info "静态文件已部署到: $DEPLOY_PATH"
}

# ==================== 远程部署函数 ====================

get_ssh_opts() {
    SSH_OPTS="-o StrictHostKeyChecking=no -p $REMOTE_PORT"
    [ -f "$SSH_KEY" ] && SSH_OPTS="$SSH_OPTS -i $SSH_KEY"
    echo "$SSH_OPTS"
}

remote_deploy_h5() {
    log_info "远程部署 H5: $REMOTE_HOST..."
    
    if [ -z "$REMOTE_HOST" ]; then
        log_error "远程模式需要指定 --host 参数"
        exit 1
    fi
    
    DIST_DIR="$UNI3_DIR/dist/build/h5"
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    SSH_OPTS=$(get_ssh_opts)
    
    # 创建远程目录
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "mkdir -p $DEPLOY_PATH $BACKUP_PATH"
    
    # 备份旧版本
    log_info "备份当前版本..."
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "
        if [ -d '$DEPLOY_PATH' ] && [ \"\$(ls -A $DEPLOY_PATH 2>/dev/null)\" ]; then
            tar -czf '$BACKUP_PATH/h5_$TIMESTAMP.tar.gz' -C '$DEPLOY_PATH' .
        fi
        rm -rf $DEPLOY_PATH/*
    "
    
    # 上传新版本
    log_info "上传静态文件..."
    scp -r $SSH_OPTS "$DIST_DIR"/* "$REMOTE_USER@$REMOTE_HOST:$DEPLOY_PATH/"
    
    log_success "远程 H5 部署完成!"
}

# ==================== 主程序 ====================

SKIP_BUILD=false
COMMAND=""

# 解析参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help) show_help; exit 0 ;;
        -m|--mode) DEPLOY_MODE="$2"; shift 2 ;;
        -s|--skip-build) SKIP_BUILD=true; shift ;;
        --host) REMOTE_HOST="$2"; DEPLOY_MODE="remote"; shift 2 ;;
        --user) REMOTE_USER="$2"; shift 2 ;;
        --port) REMOTE_PORT="$2"; shift 2 ;;
        --path) DEPLOY_PATH="$2"; shift 2 ;;
        build:mp|build:h5|deploy:h5) COMMAND="$1"; shift ;;
        *) log_error "未知参数: $1"; show_help; exit 1 ;;
    esac
done

if [ -z "$COMMAND" ]; then
    show_help
    exit 1
fi

log_info "部署模式: $DEPLOY_MODE"

case $COMMAND in
    build:mp)
        check_requirements
        build_mp_weixin
        ;;
    build:h5)
        check_requirements
        build_h5
        ;;
    deploy:h5)
        check_requirements
        [ "$SKIP_BUILD" = false ] && build_h5
        [ "$DEPLOY_MODE" = "remote" ] && remote_deploy_h5 || local_deploy_h5
        ;;
esac
