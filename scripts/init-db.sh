#!/bin/bash
# ==========================================
# 装修管理系统 - 数据库初始化脚本
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

# 配置变量
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="ruoyi_vue"
DB_USER="root"
DB_PASS=""
SCRIPT_DIR="$(dirname "$0")"

# 读取配置参数
while getopts "h:P:u:p:d:" opt; do
    case $opt in
        h) DB_HOST="$OPTARG" ;;
        P) DB_PORT="$OPTARG" ;;
        u) DB_USER="$OPTARG" ;;
        p) DB_PASS="$OPTARG" ;;
        d) DB_NAME="$OPTARG" ;;
        \?) log_error "无效选项: -$OPTARG" ;;
    esac
done

# 检查MySQL客户端
check_mysql() {
    if ! command -v mysql &> /dev/null; then
        log_error "MySQL客户端未安装"
        exit 1
    fi
}

# 测试数据库连接
test_connection() {
    log_info "测试数据库连接..."

    if [ -n "$DB_PASS" ]; then
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" > /dev/null 2>&1
    else
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -e "SELECT 1;" > /dev/null 2>&1
    fi

    if [ $? -eq 0 ]; then
        log_success "数据库连接成功"
        return 0
    else
        log_error "数据库连接失败，请检查连接参数"
        exit 1
    fi
}

# 创建数据库
create_database() {
    log_info "创建数据库 $DB_NAME..."

    if [ -n "$DB_PASS" ]; then
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    else
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    fi

    if [ $? -eq 0 ]; then
        log_success "数据库创建成功"
    else
        log_error "数据库创建失败"
        exit 1
    fi
}

# 执行SQL文件
execute_sql_file() {
    local file_path="$1"
    local description="$2"

    if [ ! -f "$file_path" ]; then
        log_error "SQL文件不存在: $file_path"
        return 1
    fi

    log_info "执行 $description..."
    log_info "SQL文件: $file_path"

    if [ -n "$DB_PASS" ]; then
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$file_path"
    else
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" < "$file_path"
    fi

    if [ $? -eq 0 ]; then
        log_success "$description 执行成功"
        return 0
    else
        log_error "$description 执行失败"
        return 1
    fi
}

# 初始化若依基础表
init_ruoyi_tables() {
    log_info "初始化若依基础表..."

    local ruoyi_sql="$SCRIPT_DIR/../sb3/sql/ry_20250522.sql"
    if [ -f "$ruoyi_sql" ]; then
        execute_sql_file "$ruoyi_sql" "若依基础表初始化"
    else
        log_warning "若依基础表SQL文件未找到，跳过基础表初始化"
    fi
}

# 初始化装修业务表
init_decoration_tables() {
    log_info "初始化装修业务表..."

    local decoration_sql="$SCRIPT_DIR/../docs/decoration_business.sql"
    if [ -f "$decoration_sql" ]; then
        execute_sql_file "$decoration_sql" "装修业务表初始化"
    else
        log_error "装修业务表SQL文件未找到"
        exit 1
    fi
}

# 验证表结构
verify_tables() {
    log_info "验证表结构..."

    if [ -n "$DB_PASS" ]; then
        local tables=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "SHOW TABLES;" 2>/dev/null)
    else
        local tables=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "SHOW TABLES;" 2>/dev/null)
    fi

    if [ $? -eq 0 ]; then
        log_success "数据库表结构验证通过"
        echo ""
        echo "已创建的表："
        echo "$tables" | grep -v "Tables_in_"
        return 0
    else
        log_error "表结构验证失败"
        return 1
    fi
}

# 检查数据完整性
check_data_integrity() {
    log_info "检查数据完整性..."

    # 检查若依基础表
    local required_tables=(
        "sys_user"
        "sys_role"
        "sys_menu"
        "sys_dict_type"
        "sys_dict_data"
    )

    # 检查装修业务表
    local decoration_tables=(
        "customers"
        "projects"
        "project_members"
        "project_schedules"
        "quality_inspections"
    )

    local missing_tables=()

    for table in "${required_tables[@]}" "${decoration_tables[@]}"; do
        if [ -n "$DB_PASS" ]; then
            local count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "SHOW TABLES LIKE '$table';" 2>/dev/null | wc -l)
        else
            local count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "SHOW TABLES LIKE '$table';" 2>/dev/null | wc -l)
        fi

        if [ $count -eq 0 ]; then
            missing_tables+=("$table")
        fi
    done

    if [ ${#missing_tables[@]} -eq 0 ]; then
        log_success "所有必要的数据表都存在"
        return 0
    else
        log_error "缺少以下数据表："
        for table in "${missing_tables[@]}"; do
            echo "  - $table"
        done
        return 1
    fi
}

# 显示数据库信息
show_db_info() {
    log_info "数据库信息："

    echo ""
    echo "数据库名称: $DB_NAME"
    echo "数据库主机: $DB_HOST:$DB_PORT"

    if [ -n "$DB_PASS" ]; then
        local table_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "SHOW TABLES;" 2>/dev/null | wc -l)
        local data_size=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 1) AS 'DB Size (MB)' FROM information_schema.tables WHERE table_schema='$DB_NAME';" 2>/dev/null | tail -1)
    else
        local table_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "SHOW TABLES;" 2>/dev/null | wc -l)
        local data_size=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 1) AS 'DB Size (MB)' FROM information_schema.tables WHERE table_schema='$DB_NAME';" 2>/dev/null | tail -1)
    fi

    echo "表数量: $((table_count - 1))"
    echo "数据大小: ${data_size} MB"
    echo ""

    log_info "重要表列表："
    echo "若依基础表："
    echo "  - sys_user (用户表)"
    echo "  - sys_role (角色表)"
    echo "  - sys_menu (菜单表)"
    echo "  - sys_dict_type (字典类型表)"
    echo ""
    echo "装修业务表："
    echo "  - customers (客户表)"
    echo "  - projects (项目表)"
    echo "  - project_schedules (进度表)"
    echo "  - quality_inspections (质检表)"
    echo ""
}

# 主函数
main() {
    echo "=========================================="
    echo "    装修管理系统 - 数据库初始化脚本"
    echo "=========================================="
    echo ""

    # 显示当前配置
    echo "当前配置："
    echo "数据库主机: $DB_HOST"
    echo "数据库端口: $DB_PORT"
    echo "数据库名称: $DB_NAME"
    echo "数据库用户: $DB_USER"
    echo ""

    # 确认初始化
    read -p "是否继续初始化数据库? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "初始化已取消"
        exit 0
    fi

    # 检查和初始化
    check_mysql
    test_connection

    # 询问是否创建数据库
    read -p "是否创建数据库 $DB_NAME? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        create_database
    fi

    # 初始化表
    echo ""
    echo "开始初始化数据表..."
    echo ""

    # 初始化若依基础表
    read -p "是否初始化若依基础表? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        init_ruoyi_tables
    fi

    # 初始化装修业务表
    echo ""
    read -p "是否初始化装修业务表? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        init_decoration_tables
    fi

    # 验证初始化结果
    echo ""
    echo "验证初始化结果..."
    verify_tables
    check_data_integrity

    # 显示数据库信息
    show_db_info

    echo ""
    echo "=========================================="
    echo "            初始化完成！"
    echo "=========================================="
    echo "默认登录信息："
    echo "用户名: admin"
    echo "密码: admin123"
    echo ""
    echo "请及时修改默认密码！"
    echo "=========================================="
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h HOST     数据库主机 (默认: localhost)"
    echo "  -P PORT     数据库端口 (默认: 3306)"
    echo "  -u USER     数据库用户 (默认: root)"
    echo "  -p PASS     数据库密码 (默认: 空)"
    echo "  -d NAME     数据库名称 (默认: ruoyi_vue)"
    echo ""
    echo "示例:"
    echo "  $0                           # 使用默认配置"
    echo "  $0 -h 192.168.1.100 -u root -p123456 -d mydb"
    echo "  $0 -p mypassword             # 仅指定密码"
}

# 检查是否需要显示帮助
if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    show_help
    exit 0
fi

# 执行主函数
main "$@"
