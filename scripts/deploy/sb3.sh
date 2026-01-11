#!/bin/bash
# 后端服务部署脚本

set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
source "$SCRIPT_DIR/config.env"

JAR_NAME="ruoyi-admin.jar"
SB3_DIR="$PROJECT_ROOT/sb3"

log() { echo -e "\033[0;34m[INFO]\033[0m $1"; }
ok() { echo -e "\033[0;32m[OK]\033[0m $1"; }
err() { echo -e "\033[0;31m[ERROR]\033[0m $1"; }

get_pid() { pgrep -f "java.*$JAR_NAME" 2>/dev/null || true; }

cmd_build() {
    log "构建后端项目..."
    cd "$SB3_DIR"
    mvn clean package -DskipTests -q
    ok "构建完成: $SB3_DIR/ruoyi-admin/target/$JAR_NAME"
}

cmd_start() {
    PID=$(get_pid)
    if [ -n "$PID" ]; then
        err "服务已在运行, PID: $PID"
        exit 1
    fi
    
    log "启动服务..."
    mkdir -p "$(dirname "$BACKEND_LOG")" "$BACKEND_PATH"
    cp "$SB3_DIR/ruoyi-admin/target/$JAR_NAME" "$BACKEND_PATH/" 2>/dev/null || true
    
    cd "$BACKEND_PATH"
    nohup java $JAVA_OPTS -jar "$JAR_NAME" > "$BACKEND_LOG" 2>&1 &
    sleep 3
    
    PID=$(get_pid)
    [ -n "$PID" ] && ok "启动成功, PID: $PID" || { err "启动失败"; tail -20 "$BACKEND_LOG"; exit 1; }
}

cmd_stop() {
    PID=$(get_pid)
    if [ -z "$PID" ]; then
        log "服务未运行"
        return
    fi
    log "停止服务, PID: $PID"
    kill $PID 2>/dev/null || true
    sleep 2
    [ -z "$(get_pid)" ] && ok "已停止" || { kill -9 $PID 2>/dev/null; ok "已强制停止"; }
}

cmd_restart() {
    cmd_stop
    cmd_start
}

cmd_status() {
    PID=$(get_pid)
    if [ -n "$PID" ]; then
        ok "运行中, PID: $PID"
        ps -p $PID -o pid,%cpu,%mem,etime 2>/dev/null || true
    else
        log "未运行"
    fi
}

cmd_log() {
    [ -f "$BACKEND_LOG" ] && tail -f "$BACKEND_LOG" || err "日志不存在: $BACKEND_LOG"
}

cmd_deploy() {
    cmd_build
    cmd_stop
    cmd_start
}

case "${1:-help}" in
    build)   cmd_build ;;
    start)   cmd_start ;;
    stop)    cmd_stop ;;
    restart) cmd_restart ;;
    status)  cmd_status ;;
    log)     cmd_log ;;
    deploy)  cmd_deploy ;;
    *)
        echo "用法: $0 {build|start|stop|restart|status|log|deploy}"
        echo ""
        echo "  build    构建项目"
        echo "  start    启动服务"
        echo "  stop     停止服务"
        echo "  restart  重启服务"
        echo "  status   查看状态"
        echo "  log      查看日志"
        echo "  deploy   构建并启动"
        ;;
esac
