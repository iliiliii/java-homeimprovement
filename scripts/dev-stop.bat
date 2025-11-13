@echo off
chcp 65001 > nul
:: ==========================================
:: 装修管理系统 - 开发环境停止脚本 (Windows)
:: ==========================================

setlocal enabledelayedexpansion

echo ==========================================
echo     装修管理系统 - 开发环境停止脚本
echo ==========================================
echo.

:: 停止进程函数
:stop_process
set SERVICE_NAME=%1
set PID_FILE=%2
set PORT=%3

echo [INFO] 检查 %SERVICE_NAME% 状态...

:: 检查PID文件
if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /FI "PID eq !PID!" 2>nul | find /I "!PID!" > nul
    if not errorlevel 1 (
        echo [INFO] 正在停止 %SERVICE_NAME% (PID: !PID!)...
        taskkill /PID !PID! /F > nul 2>&1
        timeout /t 2 /nobreak > nul
        tasklist /FI "PID eq !PID!" 2>nul | find /I "!PID!" > nul
        if errorlevel 1 (
            echo [SUCCESS] %SERVICE_NAME% 已停止
            del "%PID_FILE%" > nul 2>&1
        ) else (
            echo [WARNING] %SERVICE_NAME% 停止失败
        )
    ) else (
        echo [WARNING] %SERVICE_NAME% 未运行
        del "%PID_FILE%" > nul 2>&1
    )
) else (
    :: 通过端口查找进程
    if defined PORT (
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT%" ^| findstr "LISTENING"') do (
            set PID=%%a
            goto :found_process
        )
        echo [WARNING] %SERVICE_NAME% 未运行
        goto :end_check

        :found_process
        echo [INFO] 通过端口 %PORT% 找到进程 %PID%，正在停止 %SERVICE_NAME%...
        taskkill /PID %PID% /F > nul 2>&1
        timeout /t 2 /nobreak > nul
        tasklist /FI "PID eq %PID%" 2>nul | find /I "%PID%" > nul
        if errorlevel 1 (
            echo [SUCCESS] %SERVICE_NAME% 已停止
        ) else (
            echo [WARNING] %SERVICE_NAME% 停止失败
        )
    ) else (
        echo [WARNING] %SERVICE_NAME% 未启动
    )
)

:end_check
exit /b 0

:: 清理日志文件
:clean_logs
set "LOG_DIR=%~dp0..\logs"
echo [INFO] 清理日志文件...
if exist "%LOG_DIR%\*.log" (
    del /q "%LOG_DIR%\*.log" > nul 2>&1
    echo [SUCCESS] 日志文件已清理
) else (
    echo [INFO] 没有找到日志文件
)
if exist "%LOG_DIR%\*.pid" (
    del /q "%LOG_DIR%\*.pid" > nul 2>&1
    echo [SUCCESS] PID文件已清理
)
exit /b 0

:: 强制停止所有相关进程
:force_stop_all
echo [INFO] 强制停止所有相关进程...
taskkill /IM java.exe /F > nul 2>&1
taskkill /IM node.exe /F > nul 2>&1
taskkill /IM yarn.exe /F > nul 2>&1
echo [SUCCESS] 所有进程已强制停止
exit /b 0

:: 清理所有文件
:clean_all
call :clean_logs
call :force_stop_all
echo [SUCCESS] 清理完成
exit /b 0

:: 显示服务状态
:show_status
echo [INFO] 检查服务状态...

:: 检查后端服务
tasklist /FI "WINDOWTITLE eq Ruoyi Backend*" 2>nul | find /I "java.exe" > nul
if not errorlevel 1 (
    echo [SUCCESS] 后端服务运行中
) else (
    tasklist /IM java.exe 2>nul | find /I "ruoyi-admin" > nul
    if not errorlevel 1 (
        echo [SUCCESS] 后端服务运行中 (通过进程名检测)
    ) else (
        echo [WARNING] 后端服务未运行
    )
)

:: 检查前端服务
tasklist /FI "WINDOWTITLE eq Vue Frontend*" 2>nul | find /I "node.exe" > nul
if not errorlevel 1 (
    echo [SUCCESS] 前端服务运行中
) else (
    tasklist /IM node.exe 2>nul | find /I "vite" > nul
    if not errorlevel 1 (
        echo [SUCCESS] 前端服务运行中 (通过进程名检测)
    ) else (
        echo [WARNING] 前端服务未运行
    )
)

:: 检查端口占用
echo.
echo [INFO] 检查端口占用情况:
for /f "tokens=4" %%a in ('netstat -an ^| findstr ":8080" ^| findstr "LISTENING"') do echo [INFO] 端口8080被进程 %%a 占用
for /f "tokens=4" %%a in ('netstat -an ^| findstr ":80" ^| findstr "LISTENING"') do echo [INFO] 端口80被进程 %%a 占用

exit /b 0

:: 主菜单
:main
echo 请选择停止模式：
echo 1. 停止后端服务 (端口: 8080)
echo 2. 停止前端服务 (端口: 80)
echo 3. 停止所有服务
echo 4. 查看服务状态
echo 5. 强制停止所有进程
echo 6. 清理日志文件
echo 7. 完全清理 (停止所有进程 + 清理文件)
echo.

set /p choice="请输入选择 (1-7): "

if "%choice%"=="1" (
    call :stop_process "后端服务" "%~dp0..\logs\backend.pid" "8080"
    goto :end
)
if "%choice%"=="2" (
    call :stop_process "前端服务" "%~dp0..\logs\frontend.pid" "80"
    goto :end
)
if "%choice%"=="3" (
    echo [INFO] 正在停止所有服务...
    call :stop_process "前端服务" "%~dp0..\logs\frontend.pid" "80"
    call :stop_process "后端服务" "%~dp0..\logs\backend.pid" "8080"
    echo [SUCCESS] 所有服务已停止
    goto :end
)
if "%choice%"=="4" (
    call :show_status
    goto :end
)
if "%choice%"=="5" (
    call :force_stop_all
    goto :end
)
if "%choice%"=="6" (
    call :clean_logs
    goto :end
)
if "%choice%"=="7" (
    call :clean_all
    goto :end
)

echo [ERROR] 无效选择
echo.
goto :main

:: 执行主函数
call :main

:end
echo.
echo 按任意键继续...
pause > nul
endlocal
