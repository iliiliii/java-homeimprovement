@echo off
chcp 65001 > nul
:: ==========================================
:: 装修管理系统 - 数据库初始化脚本 (Windows)
:: ==========================================

setlocal enabledelayedexpansion

echo ==========================================
echo     装修管理系统 - 数据库初始化脚本
echo ==========================================
echo.

:: 设置默认参数
set "DB_HOST=localhost"
set "DB_PORT=3306"
set "DB_NAME=ruoyi_vue"
set "DB_USER=root"
set "DB_PASS="

:: 解析命令行参数
:parse_args
if "%1"=="-h" (
    set "DB_HOST=%2"
    shift
    shift
    goto parse_args
)
if "%1"=="-P" (
    set "DB_PORT=%2"
    shift
    shift
    goto parse_args
)
if "%1"=="-u" (
    set "DB_USER=%2"
    shift
    shift
    goto parse_args
)
if "%1"=="-p" (
    set "DB_PASS=%2"
    shift
    shift
    goto parse_args
)
if "%1"=="-d" (
    set "DB_NAME=%2"
    shift
    shift
    goto parse_args
)

:: 显示帮助
if "%1"=="-h" goto show_help
if "%1"=="--help" goto show_help

:: 检查MySQL客户端
echo [INFO] 检查MySQL客户端...
mysql --version > nul 2>&1
if errorlevel 1 (
    echo [ERROR] MySQL客户端未安装或未添加到PATH
    pause
    exit /b 1
)

:: 显示当前配置
echo 当前配置：
echo 数据库主机: %DB_HOST%
echo 数据库端口: %DB_PORT%
echo 数据库名称: %DB_NAME%
echo 数据库用户: %DB_USER%
echo.

:: 确认初始化
set /p confirm="是否继续初始化数据库? (y/N): "
if /i not "%confirm%"=="y" (
    echo [INFO] 初始化已取消
    pause
    exit /b 0
)

:: 测试数据库连接
echo [INFO] 测试数据库连接...
if defined DB_PASS (
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -e "SELECT 1;" > nul 2>&1
) else (
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -e "SELECT 1;" > nul 2>&1
)

if errorlevel 1 (
    echo [ERROR] 数据库连接失败，请检查连接参数
    pause
    exit /b 1
) else (
    echo [SUCCESS] 数据库连接成功
)

:: 询问是否创建数据库
set /p create_db="是否创建数据库 %DB_NAME%? (y/N): "
if /i "%create_db%"=="y" (
    echo [INFO] 创建数据库 %DB_NAME%...
    if defined DB_PASS (
        mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -e "CREATE DATABASE IF NOT EXISTS `%DB_NAME%` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    ) else (
        mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -e "CREATE DATABASE IF NOT EXISTS `%DB_NAME%` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    )

    if errorlevel 1 (
        echo [ERROR] 数据库创建失败
        pause
        exit /b 1
    ) else (
        echo [SUCCESS] 数据库创建成功
    )
)

:: 执行SQL文件的函数
:execute_sql_file
set "SQL_FILE=%1"
set "DESCRIPTION=%2"

if not exist "%SQL_FILE%" (
    echo [ERROR] SQL文件不存在: %SQL_FILE%
    exit /b 1
)

echo [INFO] 执行 %DESCRIPTION%...
echo [INFO] SQL文件: %SQL_FILE%

if defined DB_PASS (
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < "%SQL_FILE%"
) else (
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% %DB_NAME% < "%SQL_FILE%"
)

if errorlevel 1 (
    echo [ERROR] %DESCRIPTION% 执行失败
    exit /b 1
) else (
    echo [SUCCESS] %DESCRIPTION% 执行成功
)
exit /b 0

:: 初始化若依基础表
echo.
set /p init_ruoyi="是否初始化若依基础表? (y/N): "
if /i "%init_ruoyi%"=="y" (
    set "RUOYI_SQL=%~dp0..\sb3\sql\ry_20250522.sql"
    call :execute_sql_file "%RUOYI_SQL%" "若依基础表初始化"
)

:: 初始化装修业务表
echo.
set /p init_decoration="是否初始化装修业务表? (y/N): "
if /i "%init_decoration%"=="y" (
    set "DECORATION_SQL=%~dp0..\docs\decoration_business.sql"
    call :execute_sql_file "%DECORATION_SQL%" "装修业务表初始化"
)

:: 验证表结构
echo.
echo [INFO] 验证表结构...
if defined DB_PASS (
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% -e "SHOW TABLES;" > temp_tables.txt 2>&1
) else (
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% %DB_NAME% -e "SHOW TABLES;" > temp_tables.txt 2>&1
)

if errorlevel 1 (
    echo [ERROR] 表结构验证失败
    del temp_tables.txt > nul 2>&1
    pause
    exit /b 1
) else (
    echo [SUCCESS] 数据库表结构验证通过
    echo.
    echo 已创建的表：
    type temp_tables.txt | find /v "Tables_in_%DB_NAME%"
    del temp_tables.txt > nul 2>&1
)

:: 检查必要表
echo.
echo [INFO] 检查数据完整性...

:: 检查若依基础表
set "REQUIRED_TABLES=sys_user sys_role sys_menu sys_dict_type sys_dict_data"
:: 检查装修业务表
set "DECORATION_TABLES=customers projects project_members project_schedules quality_inspections"

set "MISSING_TABLES="

for %%t in (%REQUIRED_TABLES% %DECORATION_TABLES%) do (
    if defined DB_PASS (
        mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% -e "SHOW TABLES LIKE '%%t%%';" 2>nul | find "%%t" > nul
    ) else (
        mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% %DB_NAME% -e "SHOW TABLES LIKE '%%t%%';" 2>nul | find "%%t" > nul
    )

    if errorlevel 1 (
        set "MISSING_TABLES=!MISSING_TABLES! %%t"
    )
)

if defined MISSING_TABLES (
    echo [WARNING] 缺少以下数据表：
    for %%t in (%MISSING_TABLES%) do echo   - %%t
) else (
    echo [SUCCESS] 所有必要的数据表都存在
)

:: 显示数据库信息
echo.
echo [INFO] 数据库信息：
echo 数据库名称: %DB_NAME%
echo 数据库主机: %DB_HOST%:%DB_PORT%

if defined DB_PASS (
    for /f "tokens=*" %%a in ('mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% -e "SHOW TABLES;" 2^>nul ^| find /c /v ""') do set "TABLE_COUNT=%%a"
    for /f "tokens=*" %%a in ('mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% -e "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 1) AS 'DB Size (MB)' FROM information_schema.tables WHERE table_schema='%DB_NAME%';" 2^>nul ^| find /v "DB Size"') do set "DATA_SIZE=%%a"
) else (
    for /f "tokens=*" %%a in ('mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% %DB_NAME% -e "SHOW TABLES;" 2^>nul ^| find /c /v ""') do set "TABLE_COUNT=%%a"
    for /f "tokens=*" %%a in ('mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% %DB_NAME% -e "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 1) AS 'DB Size (MB)' FROM information_schema.tables WHERE table_schema='%DB_NAME%';" 2^>nul ^| find /v "DB Size"') do set "DATA_SIZE=%%a"
)

echo 表数量: %TABLE_COUNT%
echo 数据大小: %DATA_SIZE% MB
echo.

echo [INFO] 重要表列表：
echo 若依基础表：
echo   - sys_user (用户表)
echo   - sys_role (角色表)
echo   - sys_menu (菜单表)
echo   - sys_dict_type (字典类型表)
echo.
echo 装修业务表：
echo   - customers (客户表)
echo   - projects (项目表)
echo   - project_schedules (进度表)
echo   - quality_inspections (质检表)

echo.
echo ==========================================
echo             初始化完成！
echo ==========================================
echo 默认登录信息：
echo 用户名: admin
echo 密码: admin123
echo.
echo 请及时修改默认密码！
echo ==========================================

pause
exit /b 0

:: 显示帮助信息
:show_help
echo 用法: %0 [选项]
echo.
echo 选项:
echo   -h HOST     数据库主机 (默认: localhost)
echo   -P PORT     数据库端口 (默认: 3306)
echo   -u USER     数据库用户 (默认: root)
echo   -p PASS     数据库密码 (默认: 空)
echo   -d NAME     数据库名称 (默认: ruoyi_vue)
echo.
echo 示例:
echo   %0                            # 使用默认配置
echo   %0 -h 192.168.1.100 -u root -p123456 -d mydb
echo   %0 -p mypassword              # 仅指定密码
echo.
pause
exit /b 0
