@echo off
chcp 65001 > nul
:: ==========================================
:: 装修管理系统 - 开发环境启动脚本 (Windows)
:: ==========================================

setlocal enabledelayedexpansion

echo ==========================================
echo     装修管理系统 - 开发环境启动脚本
echo ==========================================
echo.

:: 设置变量
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%..
set "BACKEND_DIR=%PROJECT_ROOT%\sb3"
set "FRONTEND_DIR=%PROJECT_ROOT%\vue3"
set "LOG_DIR=%PROJECT_ROOT%\logs"

:: 创建日志目录
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

:: 检查Java
echo [INFO] 检查Java环境...
java -version > nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java未安装或未配置到PATH
    pause
    exit /b 1
)

:: 检查Maven
echo [INFO] 检查Maven环境...
mvn -version > nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven未安装或未配置到PATH
    pause
    exit /b 1
)

:: 检查Node.js
echo [INFO] 检查Node.js环境...
node --version > nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js未安装或未配置到PATH
    pause
    exit /b 1
)

:: 检查端口函数
:check_port
set PORT=%1
netstat -an | find ":%PORT%" | find "LISTENING" > nul
if not errorlevel 1 (
    echo [WARNING] 端口 %PORT% 已被占用
    exit /b 1
)
exit /b 0

:: 启动后端服务
:start_backend
echo [INFO] 正在启动后端服务...
cd /d "%BACKEND_DIR%"

:: 检查端口8080
call :check_port 8080
if errorlevel 1 (
    echo [ERROR] 后端端口8080被占用，请先释放该端口
    pause
    exit /b 1
)

:: 编译项目
echo [INFO] 编译后端项目...
mvn clean compile -q

:: 启动应用
echo [INFO] 启动Spring Boot应用...
start "Ruoyi Backend" /min cmd /c "mvn spring-boot:run -Dspring-boot.run.profiles=dev > ..\logs\backend.log 2>&1"

:: 等待服务启动
echo [INFO] 等待后端服务启动...
set /a counter=0
:wait_backend
set /a counter+=1
timeout /t 2 /nobreak > nul
curl -s http://localhost:8080/ruoyi-admin/doc.html > nul 2>&1
if not errorlevel 1 (
    echo [SUCCESS] 后端服务启动成功
    goto :start_frontend
)
if %counter% GEQ 30 (
    echo [ERROR] 后端服务启动失败，请检查日志
    goto :show_logs
)
goto :wait_backend

:: 启动前端服务
:start_frontend
echo [INFO] 正在启动前端服务...
cd /d "%FRONTEND_DIR%"

:: 检查端口80
call :check_port 80
if errorlevel 1 (
    echo [ERROR] 前端端口80被占用，请先释放该端口
    pause
    exit /b 1
)

:: 检查包管理器
set "PKG_MANAGER="
if exist "yarn.lock" (
    set "PKG_MANAGER=yarn"
) else (
    where yarn > nul 2>&1
    if not errorlevel 1 (
        set "PKG_MANAGER=yarn"
    ) else (
        set "PKG_MANAGER=npm"
    )
)

:: 安装依赖
if "%PKG_MANAGER%"=="yarn" (
    echo [INFO] 使用yarn安装依赖...
    yarn install --registry=https://registry.npmmirror.com
) else (
    echo [INFO] 使用npm安装依赖...
    npm install --registry=https://registry.npmmirror.com
)

:: 启动开发服务器
echo [INFO] 启动Vue开发服务器...
start "Vue Frontend" cmd /c "%PKG_MANAGER% dev > ..\logs\frontend.log 2>&1"

:: 等待服务启动
echo [INFO] 等待前端服务启动...
set /a counter=0
:wait_frontend
set /a counter+=1
timeout /t 2 /nobreak > nul
curl -s http://localhost:80 > nul 2>&1
if not errorlevel 1 (
    echo [SUCCESS] 前端服务启动成功
    goto :show_success
)
if %counter% GEQ 30 (
    echo [ERROR] 前端服务启动失败，请检查日志
    goto :show_logs
)
goto :wait_frontend

:: 显示启动成功信息
:show_success
echo.
echo ==========================================
echo             服务启动完成！
echo ==========================================
echo 前端地址: http://localhost
echo 后端API: http://localhost:8080/ruoyi-admin
echo API文档: http://localhost:8080/ruoyi-admin/doc.html
echo.
echo 默认登录账号: admin/admin123
echo ==========================================
echo.
echo 按任意键继续...
pause > nul

:: 显示服务状态
:show_status
echo.
echo [INFO] 检查服务状态...
tasklist /FI "WINDOWTITLE eq Ruoyi Backend*" 2>nul | find /I "java.exe" > nul
if not errorlevel 1 (
    echo [SUCCESS] 后端服务运行中
) else (
    echo [WARNING] 后端服务未运行
)

tasklist /FI "WINDOWTITLE eq Vue Frontend*" 2>nul | find /I "node.exe" > nul
if not errorlevel 1 (
    echo [SUCCESS] 前端服务运行中
) else (
    echo [WARNING] 前端服务未运行
)
goto :end

:: 显示日志信息
:show_logs
echo.
echo [INFO] 服务日志位置：
echo 后端日志: %LOG_DIR%\backend.log
echo 前端日志: %LOG_DIR%\frontend.log
echo.
goto :end

:: 主菜单
:main
echo 请选择启动模式：
echo 1. 启动后端服务 (端口: 8080)
echo 2. 启动前端服务 (端口: 80)
echo 3. 同时启动后端和前端服务
echo 4. 查看服务状态
echo.

set /p choice="请输入选择 (1-4): "

if "%choice%"=="1" goto :start_backend_only
if "%choice%"=="2" goto :start_frontend_only
if "%choice%"=="3" goto :start_backend
if "%choice%"=="4" goto :show_status

echo [ERROR] 无效选择
goto :main

:: 只启动后端
:start_backend_only
echo [INFO] 正在启动后端服务...
cd /d "%BACKEND_DIR%"
call :check_port 8080
if errorlevel 1 exit /b 1
mvn clean compile -q
start "Ruoyi Backend" cmd /c "mvn spring-boot:run -Dspring-boot.run.profiles=dev"
echo [SUCCESS] 后端服务已启动
pause
goto :end

:: 只启动前端
:start_frontend_only
echo [INFO] 正在启动前端服务...
cd /d "%FRONTEND_DIR%"
call :check_port 80
if errorlevel 1 exit /b 1
if exist "yarn.lock" (
    yarn install --registry=https://registry.npmmirror.com
    start "Vue Frontend" cmd /c "yarn dev"
) else (
    npm install --registry=https://registry.npmmirror.com
    start "Vue Frontend" cmd /c "npm run dev"
)
echo [SUCCESS] 前端服务已启动
pause
goto :end

:: 执行主函数
call :main

:end
endlocal
