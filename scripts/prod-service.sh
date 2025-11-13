#!/bin/bash
# ==========================================
# 装修管理系统 - 生产环境服务管理脚本
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

# 服务名称
SERVICE_NAME="decoration-mgmt"

# 检查服务状态
check_status() {
    log_info "检查服务状态..."

    if systemctl is-active --quiet $SERVICE_NAME; then
        log_success "服务运行中"
        echo ""
        systemctl status $SERVICE_NAME --no-pager -l
        return 0
    else
        log_warning "服务未运行"
        echo ""
        systemctl status $SERVICE_NAME --no-pager -l
        return 1
    fi
}

# 启动服务
start_service() {
    log_info "启动服务..."

    if systemctl is-active --quiet $SERVICE_NAME; then
        log_warning "服务已经在运行中"
        return 0
    fi

    systemctl start $SERVICE_NAME

    # 等待服务启动
    for i in {1..30}; do
        if systemctl is-active --quiet $SERVICE_NAME; then
            log_success "服务启动成功"
            return 0
        fi
        sleep 2
    done

    log_error "服务启动失败"
    return 1
}

# 停止服务
stop_service() {
    log_info "停止服务..."

    if ! systemctl is-active --quiet $SERVICE_NAME; then
        log_warning "服务未运行"
        return 0
    fi

    systemctl stop $SERVICE_NAME
    log_success "服务已停止"
}

# 重启服务
restart_service() {
    log_info "重启服务..."
    systemctl restart $SERVICE_NAME

    # 等待服务启动
    for i in {1..30}; do
        if systemctl is-active --quiet $SERVICE_NAME; then
            log_success "服务重启成功"
            return 0
        fi
        sleep 2
    done

    log_error "服务重启失败"
    return 1
}

# 重新加载配置
reload_service() {
    log_info "重新加载服务配置..."
    systemctl reload $SERVICE_NAME
    log_success "服务配置已重新加载"
}

# 查看日志
show_logs() {
    local lines=${1:-100}
    log_info "显示最近 $lines 行日志..."
    echo ""
    journalctl -u $SERVICE_NAME --no-pager -n $lines
}

# 实时日志
tail_logs() {
    log_info "实时监控服务日志 (按Ctrl+C退出)..."
    echo ""
    journalctl -u $SERVICE_NAME --no-pager -f
}

# 性能监控
monitor_performance() {
    log_info "服务性能监控..."

    if ! systemctl is-active --quiet $SERVICE_NAME; then
        log_error "服务未运行，无法监控"
        return 1
    fi

    # 获取Java进程信息
    local pid=$(systemctl show -p MainPID --value $SERVICE_NAME)
    if [ "$pid" != "0" ]; then
        echo "进程ID: $pid"
        echo ""
        echo "=== CPU和内存使用情况 ==="
        ps -p $pid -o pid,ppid,pcpu,pmem,cmd --no-headers
        echo ""
        echo "=== 内存详细信息 ==="
        pmap -d $pid | tail -1
    fi

    echo ""
    echo "=== 系统资源使用 ==="
    echo "内存使用:"
    free -h
    echo ""
    echo "磁盘使用:"
    df -h /
    echo ""
    echo "CPU负载:"
    uptime
}

# 日志分析
analyze_logs() {
    log_info "分析服务日志..."

    echo "=== 最近24小时错误统计 ==="
    journalctl -u $SERVICE_NAME --since "24 hours ago" --priority=err --no-pager | wc -l
    echo "条错误日志"

    echo ""
    echo "=== 最近24小时警告统计 ==="
    journalctl -u $SERVICE_NAME --since "24 hours ago" --priority=warning --no-pager | wc -l
    echo "条警告日志"

    echo ""
    echo "=== 最近10条错误日志 ==="
    journalctl -u $SERVICE_NAME --since "24 hours ago" --priority=err --no-pager -n 10

    echo ""
    echo "=== 服务重启历史 ==="
    journalctl -u $SERVICE_NAME --since "7 days ago" --grep="Started decoration-mgmt" --no-pager
}

# 健康检查
health_check() {
    log_info "执行健康检查..."

    local issues=0

    # 检查服务状态
    if systemctl is-active --quiet $SERVICE_NAME; then
        log_success "✓ 服务运行正常"
    else
        log_error "✗ 服务未运行"
        issues=$((issues + 1))
    fi

    # 检查端口监听
    if netstat -tlnp | grep -q ":8080"; then
        log_success "✓ 端口8080监听正常"
    else
        log_error "✗ 端口8080未监听"
        issues=$((issues + 1))
    fi

    # 检查HTTP响应
    if curl -f -s -o /dev/null http://localhost:8080/ruoyi-admin/doc.html; then
        log_success "✓ HTTP健康检查通过"
    else
        log_warning "⚠ HTTP健康检查失败"
        issues=$((issues + 1))
    fi

    # 检查磁盘空间
    local disk_usage=$(df / | awk 'NR==2 {print $5}' | sed 's/%//')
    if [ $disk_usage -lt 80 ]; then
        log_success "✓ 磁盘空间充足 (使用率: ${disk_usage}%)"
    elif [ $disk_usage -lt 90 ]; then
        log_warning "⚠ 磁盘空间较少 (使用率: ${disk_usage}%)"
    else
        log_error "✗ 磁盘空间不足 (使用率: ${disk_usage}%)"
        issues=$((issues + 1))
    fi

    # 检查内存使用
    local mem_usage=$(free | awk 'NR==2{printf "%.0f", $3*100/$2}')
    if [ $mem_usage -lt 80 ]; then
        log_success "✓ 内存使用正常 (使用率: ${mem_usage}%)"
    elif [ $mem_usage -lt 90 ]; then
        log_warning "⚠ 内存使用较高 (使用率: ${mem_usage}%)"
    else
        log_error "✗ 内存使用过高 (使用率: ${mem_usage}%)"
        issues=$((issues + 1))
    fi

    echo ""
    if [ $issues -eq 0 ]; then
        log_success "健康检查通过 - 系统运行正常"
    else
        log_warning "健康检查发现问题 - 发现 $issues 个问题"
    fi

    return $issues
}

# 备份应用
backup_app() {
    log_info "备份应用..."

    local backup_dir="/opt/backups/decoration-mgmt"
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_file="$backup_dir/decoration-mgmt-$timestamp.tar.gz"

    mkdir -p $backup_dir

    if [ -f "/opt/decoration-mgmt/ruoyi-admin.jar" ]; then
        tar -czf $backup_file -C /opt decoration-mgmt
        log_success "备份完成: $backup_file"
    else
        log_error "应用JAR文件不存在"
        return 1
    fi

    # 清理7天前的备份
    find $backup_dir -name "decoration-mgmt-*.tar.gz" -mtime +7 -delete 2>/dev/null || true
    log_info "已清理7天前的备份文件"
}

# 主菜单
show_menu() {
    echo "=========================================="
    echo "    装修管理系统 - 服务管理"
    echo "=========================================="
    echo ""
    echo "1. 查看服务状态"
    echo "2. 启动服务"
    echo "3. 停止服务"
    echo "4. 重启服务"
    echo "5. 重新加载配置"
    echo "6. 查看日志 (最近100行)"
    echo "7. 实时监控日志"
    echo "8. 性能监控"
    echo "9. 日志分析"
    echo "10. 健康检查"
    echo "11. 备份应用"
    echo "0. 退出"
    echo ""
}

# 主函数
main() {
    if [ $# -eq 0 ]; then
        # 交互模式
        while true; do
            show_menu
            read -p "请选择操作 (0-11): " choice

            case $choice in
                1) check_status ;;
                2) start_service ;;
                3) stop_service ;;
                4) restart_service ;;
                5) reload_service ;;
                6) show_logs 100 ;;
                7) tail_logs ;;
                8) monitor_performance ;;
                9) analyze_logs ;;
                10) health_check ;;
                11) backup_app ;;
                0) log_info "退出服务管理"; exit 0 ;;
                *) log_error "无效选择";;
            esac

            echo ""
            read -p "按Enter键继续..."
        done
    else
        # 命令行模式
        case $1 in
            status) check_status ;;
            start) start_service ;;
            stop) stop_service ;;
            restart) restart_service ;;
            reload) reload_service ;;
            logs) show_logs ${2:-100} ;;
            tail) tail_logs ;;
            monitor) monitor_performance ;;
            analyze) analyze_logs ;;
            health) health_check ;;
            backup) backup_app ;;
            *)
                echo "用法: $0 [command]"
                echo ""
                echo "命令列表:"
                echo "  status        - 查看服务状态"
                echo "  start         - 启动服务"
                echo "  stop          - 停止服务"
                echo "  restart       - 重启服务"
                echo "  reload        - 重新加载配置"
                echo "  logs [lines]  - 查看日志 (默认100行)"
                echo "  tail          - 实时监控日志"
                echo "  monitor       - 性能监控"
                echo "  analyze       - 日志分析"
                echo "  health        - 健康检查"
                echo "  backup        - 备份应用"
                exit 1
                ;;
        esac
    fi
}

# 执行主函数
main "$@"
