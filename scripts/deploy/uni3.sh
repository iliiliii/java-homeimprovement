#!/bin/bash
# 小程序部署脚本

set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
source "$SCRIPT_DIR/config.env"

UNI3_DIR="$PROJECT_ROOT/uni3"

log() { echo -e "\033[0;34m[INFO]\033[0m $1"; }
ok() { echo -e "\033[0;32m[OK]\033[0m $1"; }

cmd_mp() {
    log "构建微信小程序..."
    cd "$UNI3_DIR"
    npm install --silent
    npm run build:mp-weixin
    ok "构建完成: $UNI3_DIR/dist/build/mp-weixin"
    echo ""
    echo "后续步骤:"
    echo "  1. 打开微信开发者工具"
    echo "  2. 导入目录: $UNI3_DIR/dist/build/mp-weixin"
    echo "  3. 上传并提交审核"
}

cmd_h5() {
    log "构建 H5..."
    cd "$UNI3_DIR"
    npm install --silent
    npm run build:h5
    ok "构建完成: $UNI3_DIR/dist/build/h5"
}

cmd_deploy_h5() {
    cmd_h5
    log "部署到 $H5_PATH..."
    mkdir -p "$H5_PATH"
    rm -rf "$H5_PATH"/*
    cp -r "$UNI3_DIR/dist/build/h5"/* "$H5_PATH/"
    ok "部署完成"
}

case "${1:-help}" in
    mp)        cmd_mp ;;
    h5)        cmd_h5 ;;
    deploy:h5) cmd_deploy_h5 ;;
    *)
        echo "用法: $0 {mp|h5|deploy:h5}"
        echo ""
        echo "  mp         构建微信小程序"
        echo "  h5         构建 H5"
        echo "  deploy:h5  构建并部署 H5"
        ;;
esac
