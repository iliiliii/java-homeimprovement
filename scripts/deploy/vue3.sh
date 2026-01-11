#!/bin/bash
# 管理后台部署脚本

set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
source "$SCRIPT_DIR/config.env"

VUE3_DIR="$PROJECT_ROOT/vue3"

log() { echo -e "\033[0;34m[INFO]\033[0m $1"; }
ok() { echo -e "\033[0;32m[OK]\033[0m $1"; }
err() { echo -e "\033[0;31m[ERROR]\033[0m $1"; }

cmd_build() {
    log "构建管理后台..."
    cd "$VUE3_DIR"
    npm install --silent
    npm run build:prod
    ok "构建完成: $VUE3_DIR/dist"
}

cmd_deploy() {
    cmd_build
    log "部署到 $ADMIN_PATH..."
    mkdir -p "$ADMIN_PATH"
    rm -rf "$ADMIN_PATH"/*
    cp -r "$VUE3_DIR/dist"/* "$ADMIN_PATH/"
    ok "部署完成"
    echo "请确保 Nginx 配置指向: $ADMIN_PATH"
}

cmd_nginx() {
    log "Nginx 配置文件: $SCRIPT_DIR/nginx/evs-admin.conf"
    echo "部署命令:"
    echo "  cp $SCRIPT_DIR/nginx/evs-admin.conf /etc/nginx/conf.d/"
    echo "  nginx -t && nginx -s reload"
}

case "${1:-help}" in
    build)  cmd_build ;;
    deploy) cmd_deploy ;;
    nginx)  cmd_nginx ;;
    *)
        echo "用法: $0 {build|deploy|nginx}"
        echo ""
        echo "  build   构建项目"
        echo "  deploy  构建并部署"
        echo "  nginx   显示 Nginx 配置说明"
        ;;
esac
