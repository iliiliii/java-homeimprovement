#!/bin/bash
#====================================================================
# 后端服务 (sb3) 发布脚本
# 项目: 逅时代装修管理系统 - Spring Boot 3 后端
# 版本: 3.9.0
# 支持: 本地部署 / 远程部署
#====================================================================

set -e

# 本地路径 (需要先定义，用于加载配置文件)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
SB3_DIR="$PROJECT_ROOT/sb3"

# ==================== 加载配置文件 ====================
CONFIG_FILE="$SCRIPT_DIR/config.env"
if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
    echo -e "\033[0;34m[INFO]\033[0m 已加载配置文件: $CONFIG_FILE"
fi

# ==================== 配置区域 ====================
PROJECT_NAME="ruoyi-admin"
JAR_NAME="ruoyi-admin.jar"
VERSION="3.9.0"

# 部署模式: local / remote
DEPLOY_MODE="${DEPLOY_MODE:-local}"

# 远程服务器配置
REMOTE_HOST="${REMOTE_HOST:-}"
REMOTE_USER="${REMOTE_USER:-root}"
REMOTE_PORT="${REMOTE_PORT:-22}"
SSH_KEY="${SSH_KEY:-~/.ssh/id_rsa}"

# 部署路径 (优先使用 BACKEND_* 变量，兼容通用 DEPLOY_PATH)
DEPLOY_PATH="${BACKEND_DEPLOY_PATH:-${DEPLOY_PATH:-/opt/evs-home/backend}}"
BACKUP_PATH="${BACKUP_PATH:-/opt/evs-home/backup}"
LOG_PATH="${BACKEND_LOG_PATH:-${LOG_PATH:-/opt/evs-home/logs}}"

# JVM 配置
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+UseG1GC}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"

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
  restart     重启服务
  stop        停止服务
  status      查看服务状态
  logs        查看服务日志
  rollback    回滚到上一版本

选项:
  -h, --help          显示帮助信息
  -m, --mode MODE     部署模式 (local/remote), 默认: local
  -e, --env ENV       Spring 环境 (dev/test/prod), 默认: prod
  -s, --skip-build    跳过构建步骤
  --host HOST         远程服务器地址 (remote 模式必需)
  --user USER         SSH 用户名, 默认: root
  --port PORT         SSH 端口, 默认: 22
  --path PATH         部署路径, 默认: /opt/evs-home/backend

示例:
  $0 build                              # 仅构建
  $0 deploy                             # 本地部署
  $0 -m remote --host 1.2.3.4 deploy    # 远程部署
  $0 restart                            # 重启服务
  $0 logs                               # 查看日志
EOF
}

check_requirements() {
    log_info "检查环境依赖..."
    
    if ! command -v java &> /dev/null; then
        log_error "未找到 Java，请安装 JDK 17+"
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        log_error "未找到 Maven，请安装 Maven 3.6+"
        exit 1
    fi
    
    if [ ! -f "$SB3_DIR/pom.xml" ]; then
        log_error "未找到 sb3 项目目录: $SB3_DIR"
        exit 1
    fi
    
    log_success "环境检查通过"
}

build_project() {
    log_info "开始构建后端项目..."
    cd "$SB3_DIR"
    
    log_info "执行 Maven 构建..."
    mvn clean package -DskipTests -P$SPRING_PROFILE
    
    JAR_FILE="$SB3_DIR/ruoyi-admin/target/$JAR_NAME"
    if [ ! -f "$JAR_FILE" ]; then
        log_error "构建失败，未找到 JAR 文件: $JAR_FILE"
        exit 1
    fi
    
    log_success "构建完成: $JAR_FILE"
    echo "文件大小: $(du -h "$JAR_FILE" | cut -f1)"
}

# ==================== 本地部署函数 ====================

local_deploy() {
    log_info "本地部署模式..."
    
    JAR_FILE="$SB3_DIR/ruoyi-admin/target/$JAR_NAME"
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    
    # 创建目录
    sudo mkdir -p "$DEPLOY_PATH" "$BACKUP_PATH" "$LOG_PATH"
    
    # 备份旧版本
    if [ -f "$DEPLOY_PATH/$JAR_NAME" ]; then
        log_info "备份当前版本..."
        sudo cp "$DEPLOY_PATH/$JAR_NAME" "$BACKUP_PATH/${JAR_NAME%.jar}_$TIMESTAMP.jar"
    fi
    
    # 复制新版本
    log_info "复制 JAR 文件..."
    sudo cp "$JAR_FILE" "$DEPLOY_PATH/"
    
    # 重启服务
    local_restart
    
    log_success "本地部署完成!"
}

local_restart() {
    log_info "重启本地服务..."
    
    # 停止旧进程 (排除 grep 自身)
    PID=$(pgrep -f "java.*$JAR_NAME" || true)
    if [ -n "$PID" ]; then
        log_info "停止进程: $PID"
        sudo kill $PID 2>/dev/null || true
        sleep 3
        # 检查是否还在运行
        if pgrep -f "java.*$JAR_NAME" > /dev/null 2>&1; then
            sudo kill -9 $PID 2>/dev/null || true
            sleep 1
        fi
    fi
    
    # 启动新进程
    log_info "启动服务..."
    cd "$DEPLOY_PATH"
    
    # 使用 sudo -u 或直接 sudo 启动，确保 nohup 正常工作
    sudo bash -c "nohup java $JAVA_OPTS -jar $DEPLOY_PATH/$JAR_NAME --spring.profiles.active=$SPRING_PROFILE > $LOG_PATH/app.log 2>&1 &"
    
    # 等待服务启动
    sleep 5
    
    # 检查启动状态
    NEW_PID=$(pgrep -f "java.*$JAR_NAME" || true)
    if [ -n "$NEW_PID" ]; then
        log_success "服务启动成功, PID: $NEW_PID"
    else
        log_error "服务启动失败，请检查日志: $LOG_PATH/app.log"
        echo "--- 最近日志 ---"
        tail -50 "$LOG_PATH/app.log" 2>/dev/null || echo "日志文件不存在"
        exit 1
    fi
}

local_stop() {
    log_info "停止本地服务..."
    PID=$(pgrep -f "java.*$JAR_NAME" || true)
    if [ -n "$PID" ]; then
        sudo kill $PID
        sleep 2
        if pgrep -f "java.*$JAR_NAME" > /dev/null 2>&1; then
            sudo kill -9 $PID 2>/dev/null || true
        fi
        log_success "服务已停止"
    else
        log_warn "服务未运行"
    fi
}

local_status() {
    PID=$(pgrep -f "java.*$JAR_NAME" || true)
    if [ -n "$PID" ]; then
        log_success "服务运行中, PID: $PID"
        ps -p $PID -o pid,ppid,%cpu,%mem,etime,cmd 2>/dev/null || true
    else
        log_warn "服务未运行"
    fi
}

local_logs() {
    if [ -f "$LOG_PATH/app.log" ]; then
        tail -f "$LOG_PATH/app.log"
    else
        log_error "日志文件不存在: $LOG_PATH/app.log"
    fi
}

local_rollback() {
    log_info "回滚到上一版本..."
    LATEST_BACKUP=$(ls -t "$BACKUP_PATH"/*.jar 2>/dev/null | head -1)
    if [ -z "$LATEST_BACKUP" ]; then
        log_error "未找到备份文件"
        exit 1
    fi
    
    log_info "回滚到: $LATEST_BACKUP"
    sudo cp "$LATEST_BACKUP" "$DEPLOY_PATH/$JAR_NAME"
    local_restart
    log_success "回滚完成"
}

# ==================== 远程部署函数 ====================

get_ssh_opts() {
    SSH_OPTS="-o StrictHostKeyChecking=no -p $REMOTE_PORT"
    if [ -f "$SSH_KEY" ]; then
        SSH_OPTS="$SSH_OPTS -i $SSH_KEY"
    fi
    echo "$SSH_OPTS"
}

remote_deploy() {
    log_info "远程部署模式: $REMOTE_HOST..."
    
    if [ -z "$REMOTE_HOST" ]; then
        log_error "远程模式需要指定 --host 参数"
        exit 1
    fi
    
    JAR_FILE="$SB3_DIR/ruoyi-admin/target/$JAR_NAME"
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    SSH_OPTS=$(get_ssh_opts)
    
    # 创建远程目录
    log_info "创建远程目录..."
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "mkdir -p $DEPLOY_PATH $BACKUP_PATH $LOG_PATH"
    
    # 备份旧版本
    log_info "备份当前版本..."
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "
        if [ -f '$DEPLOY_PATH/$JAR_NAME' ]; then
            cp '$DEPLOY_PATH/$JAR_NAME' '$BACKUP_PATH/${JAR_NAME%.jar}_$TIMESTAMP.jar'
        fi
    "
    
    # 上传新版本
    log_info "上传 JAR 文件..."
    scp $SSH_OPTS "$JAR_FILE" "$REMOTE_USER@$REMOTE_HOST:$DEPLOY_PATH/"
    
    # 重启服务
    remote_restart
    
    log_success "远程部署完成!"
}

remote_restart() {
    log_info "重启远程服务..."
    SSH_OPTS=$(get_ssh_opts)
    
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "
        PID=\$(pgrep -f 'java.*$JAR_NAME' || true)
        if [ -n \"\$PID\" ]; then
            echo '停止进程: '\$PID
            kill \$PID 2>/dev/null || true
            sleep 3
            pgrep -f 'java.*$JAR_NAME' > /dev/null 2>&1 && kill -9 \$PID 2>/dev/null || true
            sleep 1
        fi
        
        cd $DEPLOY_PATH
        nohup java $JAVA_OPTS -jar $JAR_NAME --spring.profiles.active=$SPRING_PROFILE > $LOG_PATH/app.log 2>&1 &
        sleep 5
        
        NEW_PID=\$(pgrep -f 'java.*$JAR_NAME' || true)
        if [ -n \"\$NEW_PID\" ]; then
            echo '服务启动成功, PID: '\$NEW_PID
        else
            echo '服务启动失败，查看日志:'
            tail -50 $LOG_PATH/app.log 2>/dev/null || echo '日志文件不存在'
            exit 1
        fi
    "
    log_success "远程服务重启完成"
}

remote_stop() {
    log_info "停止远程服务..."
    SSH_OPTS=$(get_ssh_opts)
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "
        PID=\$(pgrep -f 'java.*$JAR_NAME' || true)
        if [ -n \"\$PID\" ]; then
            kill \$PID 2>/dev/null || true
            sleep 2
            pgrep -f 'java.*$JAR_NAME' > /dev/null 2>&1 && kill -9 \$PID 2>/dev/null || true
            echo '服务已停止'
        else
            echo '服务未运行'
        fi
    "
}

remote_status() {
    SSH_OPTS=$(get_ssh_opts)
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "
        PID=\$(pgrep -f 'java.*$JAR_NAME' || true)
        if [ -n \"\$PID\" ]; then
            echo '服务运行中, PID: '\$PID
            ps -p \$PID -o pid,ppid,%cpu,%mem,etime,cmd 2>/dev/null || true
        else
            echo '服务未运行'
        fi
    "
}

remote_logs() {
    SSH_OPTS=$(get_ssh_opts)
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "tail -f $LOG_PATH/app.log"
}

remote_rollback() {
    log_info "远程回滚..."
    SSH_OPTS=$(get_ssh_opts)
    ssh $SSH_OPTS "$REMOTE_USER@$REMOTE_HOST" "
        LATEST=\$(ls -t $BACKUP_PATH/*.jar 2>/dev/null | head -1)
        if [ -z \"\$LATEST\" ]; then echo '未找到备份'; exit 1; fi
        echo '回滚到: '\$LATEST
        cp \"\$LATEST\" '$DEPLOY_PATH/$JAR_NAME'
    "
    remote_restart
    log_success "远程回滚完成"
}

# ==================== 主程序 ====================

SKIP_BUILD=false
COMMAND=""

# 解析参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help) show_help; exit 0 ;;
        -m|--mode) DEPLOY_MODE="$2"; shift 2 ;;
        -e|--env) SPRING_PROFILE="$2"; shift 2 ;;
        -s|--skip-build) SKIP_BUILD=true; shift ;;
        --host) REMOTE_HOST="$2"; DEPLOY_MODE="remote"; shift 2 ;;
        --user) REMOTE_USER="$2"; shift 2 ;;
        --port) REMOTE_PORT="$2"; shift 2 ;;
        --path) DEPLOY_PATH="$2"; shift 2 ;;
        build|deploy|restart|stop|status|logs|rollback) COMMAND="$1"; shift ;;
        *) log_error "未知参数: $1"; show_help; exit 1 ;;
    esac
done

[ -z "$COMMAND" ] && COMMAND="deploy"

log_info "部署模式: $DEPLOY_MODE | 环境: $SPRING_PROFILE"

case $COMMAND in
    build)
        check_requirements
        build_project
        ;;
    deploy)
        check_requirements
        [ "$SKIP_BUILD" = false ] && build_project
        if [ "$DEPLOY_MODE" = "remote" ]; then
            remote_deploy
        else
            local_deploy
        fi
        ;;
    restart)
        [ "$DEPLOY_MODE" = "remote" ] && remote_restart || local_restart
        ;;
    stop)
        [ "$DEPLOY_MODE" = "remote" ] && remote_stop || local_stop
        ;;
    status)
        [ "$DEPLOY_MODE" = "remote" ] && remote_status || local_status
        ;;
    logs)
        [ "$DEPLOY_MODE" = "remote" ] && remote_logs || local_logs
        ;;
    rollback)
        [ "$DEPLOY_MODE" = "remote" ] && remote_rollback || local_rollback
        ;;
esac
