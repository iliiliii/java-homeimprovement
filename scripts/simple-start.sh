#!/bin/bash
# ==========================================
# 装修管理系统 - 简化启动脚本
# ==========================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 创建日志目录
mkdir -p "$PROJECT_ROOT/logs"

# 启动后端服务
start_backend() {
    log_info "启动后端服务..."
    cd "$PROJECT_ROOT/sb3"

    # 检查是否有mvn
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装，请先安装Maven"
        return 1
    fi

    # 编译项目
    log_info "编译项目..."
    mvn clean compile -q

    # 启动Spring Boot应用
    log_info "启动Spring Boot应用..."
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > "$PROJECT_ROOT/logs/backend.log" 2>&1 &
    echo $! > "$PROJECT_ROOT/logs/backend.pid"

    log_success "后端服务已启动，PID: $(cat "$PROJECT_ROOT/logs/backend.pid")"
    log_info "日志文件: $PROJECT_ROOT/logs/backend.log"
}

# 启动前端服务
start_frontend() {
    log_info "启动前端服务..."
    cd "$PROJECT_ROOT/vue3"

    # 检查是否有node
    if ! command -v node &> /dev/null; then
        log_error "Node.js未安装，请先安装Node.js"
        return 1
    fi

    # 检查包管理器
    if [ -f "yarn.lock" ] && command -v yarn &> /dev/null; then
        PKG_MANAGER="yarn"
    elif command -v npm &> /dev/null; then
        PKG_MANAGER="npm"
    else
        log_error "未找到npm或yarn"
        return 1
    fi

    log_info "使用$PKG_MANAGER安装依赖..."
    if [ "$PKG_MANAGER" = "yarn" ]; then
        yarn install --registry=https://registry.npmmirror.com
        nohup yarn dev > "$PROJECT_ROOT/logs/frontend.log" 2>&1 &
    else
        npm install --registry=https://registry.npmmirror.com
        nohup npm run dev > "$PROJECT_ROOT/logs/frontend.log" 2>&1 &
    fi

    echo $! > "$PROJECT_ROOT/logs/frontend.pid"
    log_success "前端服务已启动，PID: $(cat "$PROJECT_ROOT/logs/frontend.pid")"
    log_info "日志文件: $PROJECT_ROOT/logs/frontend.log"
}

# 停止服务
stop_services() {
    log_info "停止所有服务..."

    # 停止后端
    if [ -f "$PROJECT_ROOT/logs/backend.pid" ]; then
        PID=$(cat "$PROJECT_ROOT/logs/backend.pid")
        if kill $PID 2>/dev/null; then
            log_success "后端服务已停止"
        else
            log_warning "后端服务未运行"
        fi
        rm -f "$PROJECT_ROOT/logs/backend.pid"
    fi

    # 停止前端
    if [ -f "$PROJECT_ROOT/logs/frontend.pid" ]; then
        PID=$(cat "$PROJECT_ROOT/logs/frontend.pid")
        if kill $PID 2>/dev/null; then
            log_success "前端服务已停止"
        else
            log_warning "前端服务未运行"
        fi
        rm -f "$PROJECT_ROOT/logs/frontend.pid"
    fi
}

# 查看状态
show_status() {
    log_info "检查服务状态..."

    # 检查后端
    if [ -f "$PROJECT_ROOT/logs/backend.pid" ]; then
        PID=$(cat "$PROJECT_ROOT/logs/backend.pid")
        if ps -p $PID > /dev/null 2>&1; then
            log_success "后端服务运行中 (PID: $PID)"
        else
            log_warning "后端服务未运行"
            rm -f "$PROJECT_ROOT/logs/backend.pid"
        fi
    else
        log_warning "后端服务未启动"
    fi

    # 检查前端
    if [ -f "$PROJECT_ROOT/logs/frontend.pid" ]; then
        PID=$(cat "$PROJECT_ROOT/logs/frontend.pid")
        if ps -p $PID > /dev/null 2>&1; then
            log_success "前端服务运行中 (PID: $PID)"
        else
            log_warning "前端服务未运行"
            rm -f "$PROJECT_ROOT/logs/frontend.pid"
        fi
    else
        log_warning "前端服务未启动"
    fi
}

# 查看日志
show_logs() {
    echo "请选择查看的日志："
    echo "1. 后端日志"
    echo "2. 前端日志"
    echo "3. 实时监控后端日志"
    echo "4. 实时监控前端日志"
    read -p "请输入选择 (1-4): " choice

    case $choice in
        1)
            if [ -f "$PROJECT_ROOT/logs/backend.log" ]; then
                tail -50 "$PROJECT_ROOT/logs/backend.log"
            else
                log_warning "后端日志文件不存在"
            fi
            ;;
        2)
            if [ -f "$PROJECT_ROOT/logs/frontend.log" ]; then
                tail -50 "$PROJECT_ROOT/logs/frontend.log"
            else
                log_warning "前端日志文件不存在"
            fi
            ;;
        3)
            if [ -f "$PROJECT_ROOT/logs/backend.log" ]; then
                tail -f "$PROJECT_ROOT/logs/backend.log"
            else
                log_warning "后端日志文件不存在"
            fi
            ;;
        4)
            if [ -f "$PROJECT_ROOT/logs/frontend.log" ]; then
                tail -f "$PROJECT_ROOT/logs/frontend.log"
            else
                log_warning "前端日志文件不存在"
            fi
            ;;
        *)
            log_error "无效选择"
            ;;
    esac
}

# 主菜单
show_menu() {
    echo ""
    echo "=========================================="
    echo "    装修管理系统 - 启动脚本"
    echo "=========================================="
    echo "项目路径: $PROJECT_ROOT"
    echo ""
    echo "请选择操作："
    echo "1. 启动后端服务 (端口: 8080)"
    echo "2. 启动前端服务 (端口: 80)"
    echo "3. 同时启动后端和前端"
    echo "4. 停止所有服务"
    echo "5. 查看服务状态"
    echo "6. 查看日志"
    echo "0. 退出"
    echo ""
}

# 主函数
main() {
    while true; do
        show_menu
        read -p "请输入选择 (0-6): " choice

        case $choice in
            1) start_backend ;;
            2) start_frontend ;;
            3)
                log_info "启动所有服务..."
                start_backend
                sleep 2
                start_frontend
                echo ""
                log_success "所有服务启动完成！"
                echo ""
                echo "访问地址："
                echo "前端: http://localhost"
                echo "后端: http://localhost:8080/ruoyi-admin"
                echo "API文档: http://localhost:8080/ruoyi-admin/doc.html"
                echo "默认登录: admin/admin123"
                ;;
            4) stop_services ;;
            5) show_status ;;
            6) show_logs ;;
            0)
                log_info "退出脚本"
                exit 0
                ;;
            *)
                log_error "无效选择，请重新输入"
                ;;
        esac

        echo ""
        read -p "按Enter键继续..."
    done
}

# 执行主函数
main "$@"
