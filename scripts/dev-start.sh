#!/bin/bash
# ==========================================
# 装修管理系统 - 开发环境启动脚本
# 支持同时启动后端和前端服务
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

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        log_error "$1 未安装或未添加到PATH"
        return 1
    fi
    return 0
}

# 检查端口是否被占用
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        log_warning "端口 $port 已被占用"
        return 1
    fi
    return 0
}

# 启动后端服务
start_backend() {
    log_info "正在启动后端服务..."
    cd "$(dirname "$0")/../sb3"

    # 检查Maven
    if ! check_command mvn; then
        log_error "Maven未安装，请先安装Maven"
        exit 1
    fi

    # 检查端口8080
    if ! check_port 8080; then
        log_error "后端端口8080被占用，请先释放该端口"
        exit 1
    fi

    # 编译项目
    log_info "编译后端项目..."
    mvn clean compile -q

    # 启动应用（后台运行）
    log_info "启动Spring Boot应用..."
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > ../logs/backend.log 2>&1 &
    BACKEND_PID=$!

    # 等待服务启动
    log_info "等待后端服务启动..."
    for i in {1..30}; do
        if curl -s http://localhost:8080/ruoyi-admin/doc.html > /dev/null 2>&1; then
            log_success "后端服务启动成功 (PID: $BACKEND_PID)"
            echo $BACKEND_PID > ../logs/backend.pid
            return 0
        fi
        sleep 2
    done

    log_error "后端服务启动失败，请检查日志"
    return 1
}

# 启动前端服务
start_frontend() {
    log_info "正在启动前端服务..."
    cd "$(dirname "$0")/../vue3"

    # 检查Node.js
    if ! check_command node; then
        log_error "Node.js未安装，请先安装Node.js"
        exit 1
    fi

    # 检查npm/yarn
    if ! check_command yarn && ! check_command npm; then
        log_error "npm和yarn都未安装，请先安装任一包管理器"
        exit 1
    fi

    # 检查端口80
    if ! check_port 80; then
        log_error "前端端口80被占用，请先释放该端口"
        exit 1
    fi

    # 安装依赖
    if [ -f "yarn.lock" ]; then
        log_info "使用yarn安装依赖..."
        yarn install --registry=https://registry.npmmirror.com
    else
        log_info "使用npm安装依赖..."
        npm install --registry=https://registry.npmmirror.com
    fi

    # 启动开发服务器（后台运行）
    log_info "启动Vue开发服务器..."
    if [ -f "yarn.lock" ]; then
        nohup yarn dev > ../logs/frontend.log 2>&1 &
        FRONTEND_PID=$!
    else
        nohup npm run dev > ../logs/frontend.log 2>&1 &
        FRONTEND_PID=$!
    fi

    # 等待服务启动
    log_info "等待前端服务启动..."
    for i in {1..30}; do
        if curl -s http://localhost:80 > /dev/null 2>&1; then
            log_success "前端服务启动成功 (PID: $FRONTEND_PID)"
            echo $FRONTEND_PID > ../logs/frontend.pid
            return 0
        fi
        sleep 2
    done

    log_error "前端服务启动失败，请检查日志"
    return 1
}

# 主函数
main() {
    echo "=========================================="
    echo "    装修管理系统 - 开发环境启动脚本"
    echo "=========================================="
    echo ""

    # 创建日志目录
    mkdir -p "$(dirname "$0")/../logs"

    # 询问启动模式
    echo "请选择启动模式："
    echo "1. 启动后端服务 (端口: 8080)"
    echo "2. 启动前端服务 (端口: 80)"
    echo "3. 同时启动后端和前端服务"
    echo "4. 查看服务状态"
    echo ""

    read -p "请输入选择 (1-4): " choice

    case $choice in
        1)
            start_backend
            ;;
        2)
            start_frontend
            ;;
        3)
            log_info "正在同时启动后端和前端服务..."
            start_backend &
            BACKEND_PID=$!
            start_frontend &
            FRONTEND_PID=$!

            # 等待两个服务都启动
            wait $BACKEND_PID
            wait $FRONTEND_PID

            log_success "所有服务启动完成！"
            echo ""
            echo "=========================================="
            echo "    服务访问地址"
            echo "=========================================="
            echo "前端地址: http://localhost"
            echo "后端API: http://localhost:8080/ruoyi-admin"
            echo "API文档: http://localhost:8080/ruoyi-admin/doc.html"
            echo ""
            echo "默认登录账号: admin/admin123"
            echo "=========================================="
            ;;
        4)
            # 查看服务状态
            if [ -f "../logs/backend.pid" ]; then
                BACKEND_PID=$(cat ../logs/backend.pid)
                if ps -p $BACKEND_PID > /dev/null; then
                    log_success "后端服务运行中 (PID: $BACKEND_PID)"
                else
                    log_warning "后端服务未运行"
                fi
            else
                log_warning "后端服务未启动"
            fi

            if [ -f "../logs/frontend.pid" ]; then
                FRONTEND_PID=$(cat ../logs/frontend.pid)
                if ps -p $FRONTEND_PID > /dev/null; then
                    log_success "前端服务运行中 (PID: $FRONTEND_PID)"
                else
                    log_warning "前端服务未运行"
                fi
            else
                log_warning "前端服务未启动"
            fi
            ;;
        *)
            log_error "无效选择"
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
