#!/bin/bash
# ==========================================
# 装修管理系统 - 开发环境停止脚本
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

# 停止进程
stop_process() {
    local service_name=$1
    local pid_file=$2
    local port=$3

    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            log_info "正在停止 $service_name (PID: $pid)..."
            kill $pid

            # 等待进程结束
            for i in {1..10}; do
                if ! ps -p $pid > /dev/null 2>&1; then
                    log_success "$service_name 已停止"
                    rm -f "$pid_file"
                    return 0
                fi
                sleep 1
            done

            # 强制杀死
            log_warning "$service_name 未正常停止，强制终止..."
            kill -9 $pid 2>/dev/null || true
            log_success "$service_name 已强制停止"
            rm -f "$pid_file"
        else
            log_warning "$service_name 未运行"
            rm -f "$pid_file"
        fi
    else
        # 通过端口查找并停止进程
        if [ -n "$port" ]; then
            local pid=$(lsof -ti:$port 2>/dev/null || true)
            if [ -n "$pid" ]; then
                log_info "通过端口 $port 找到进程 $pid，正在停止 $service_name..."
                kill $pid
                sleep 2
                if ps -p $pid > /dev/null 2>&1; then
                    kill -9 $pid 2>/dev/null || true
                fi
                log_success "$service_name 已停止"
            else
                log_warning "$service_name 未运行"
            fi
        else
            log_warning "$service_name 未启动"
        fi
    fi
}

# 清理日志文件
clean_logs() {
    log_info "清理日志文件..."
    rm -f ../logs/*.log
    rm -f ../logs/*.pid
    log_success "日志文件已清理"
}

# 主函数
main() {
    echo "=========================================="
    echo "    装修管理系统 - 开发环境停止脚本"
    echo "=========================================="
    echo ""

    # 询问停止模式
    echo "请选择停止模式："
    echo "1. 停止后端服务 (端口: 8080)"
    echo "2. 停止前端服务 (端口: 80)"
    echo "3. 停止所有服务"
    echo "4. 清理日志文件"
    echo ""

    read -p "请输入选择 (1-4): " choice

    case $choice in
        1)
            stop_process "后端服务" "../logs/backend.pid" "8080"
            ;;
        2)
            stop_process "前端服务" "../logs/frontend.pid" "80"
            ;;
        3)
            log_info "正在停止所有服务..."
            stop_process "前端服务" "../logs/frontend.pid" "80"
            stop_process "后端服务" "../logs/backend.pid" "8080"
            log_success "所有服务已停止"
            ;;
        4)
            clean_logs
            ;;
        *)
            log_error "无效选择"
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
