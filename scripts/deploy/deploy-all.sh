#!/bin/bash
#====================================================================
# 逅时代装修管理系统 - 统一发布脚本
# 支持: 本地部署 / 远程部署
#====================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

show_banner() {
    echo ""
    echo "╔══════════════════════════════════════════════════════════╗"
    echo "║       逅时代装修管理系统 - 统一发布脚本                  ║"
    echo "║                                                          ║"
    echo "║  sb3  - Spring Boot 3 后端服务                           ║"
    echo "║  vue3 - Vue3 管理后台                                    ║"
    echo "║  uni3 - UniApp 小程序/H5                                 ║"
    echo "╚══════════════════════════════════════════════════════════╝"
    echo ""
}

show_help() {
    show_banner
    cat << EOF
用法: $0 [选项] [服务...]

服务:
  sb3       部署后端服务
  vue3      部署管理后台
  uni3:mp   构建微信小程序
  uni3:h5   部署 H5 版本
  all       部署所有服务 (sb3 + vue3)

选项:
  -h, --help          显示帮助信息
  -m, --mode MODE     部署模式 (local/remote), 默认: local
  -b, --build-only    仅构建不部署
  -e, --env ENV       指定环境 (staging/production)
  --host HOST         远程服务器地址 (remote 模式)
  --user USER         SSH 用户名
  --port PORT         SSH 端口

示例:
  $0 sb3                                # 本地部署后端
  $0 vue3                               # 本地部署管理后台
  $0 -m remote --host 1.2.3.4 all       # 远程部署所有服务
  $0 -b sb3                             # 仅构建后端
  $0 uni3:mp                            # 构建微信小程序
EOF
}

BUILD_ONLY=false
DEPLOY_MODE="local"
ENV="production"
REMOTE_HOST=""
REMOTE_USER="root"
REMOTE_PORT="22"
SERVICES=()

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help) show_help; exit 0 ;;
        -m|--mode) DEPLOY_MODE="$2"; shift 2 ;;
        -b|--build-only) BUILD_ONLY=true; shift ;;
        -e|--env) ENV="$2"; shift 2 ;;
        --host) REMOTE_HOST="$2"; DEPLOY_MODE="remote"; shift 2 ;;
        --user) REMOTE_USER="$2"; shift 2 ;;
        --port) REMOTE_PORT="$2"; shift 2 ;;
        sb3|vue3|uni3:mp|uni3:h5|all) SERVICES+=("$1"); shift ;;
        *) log_error "未知参数: $1"; show_help; exit 1 ;;
    esac
done

if [ ${#SERVICES[@]} -eq 0 ]; then
    show_help
    exit 1
fi

show_banner
log_info "部署模式: $DEPLOY_MODE"
[ -n "$REMOTE_HOST" ] && log_info "目标服务器: $REMOTE_HOST"

# 展开 all 为具体服务
if [[ " ${SERVICES[*]} " =~ " all " ]]; then
    SERVICES=("sb3" "vue3")
fi

# 构建部署参数
DEPLOY_ARGS="-m $DEPLOY_MODE -e $ENV"
[ -n "$REMOTE_HOST" ] && DEPLOY_ARGS="$DEPLOY_ARGS --host $REMOTE_HOST --user $REMOTE_USER --port $REMOTE_PORT"

for service in "${SERVICES[@]}"; do
    echo ""
    log_info "========== 处理服务: $service =========="
    
    case $service in
        sb3)
            if [ "$BUILD_ONLY" = true ]; then
                "$SCRIPT_DIR/deploy-sb3.sh" $DEPLOY_ARGS build
            else
                "$SCRIPT_DIR/deploy-sb3.sh" $DEPLOY_ARGS deploy
            fi
            ;;
        vue3)
            if [ "$BUILD_ONLY" = true ]; then
                "$SCRIPT_DIR/deploy-vue3.sh" $DEPLOY_ARGS build
            else
                "$SCRIPT_DIR/deploy-vue3.sh" $DEPLOY_ARGS deploy
            fi
            ;;
        uni3:mp)
            "$SCRIPT_DIR/deploy-uni3.sh" build:mp
            ;;
        uni3:h5)
            if [ "$BUILD_ONLY" = true ]; then
                "$SCRIPT_DIR/deploy-uni3.sh" build:h5
            else
                "$SCRIPT_DIR/deploy-uni3.sh" $DEPLOY_ARGS deploy:h5
            fi
            ;;
    esac
    
    log_success "服务 $service 处理完成"
done

echo ""
log_success "所有服务处理完成!"
