@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

echo ============================================
echo   AI Fitness System - 环境变量配置工具
echo   Windows 版本
echo ============================================
echo.
echo 本脚本将引导您配置系统所需的环境变量
echo 按提示输入值，直接回车将使用默认值（如有）
echo.

:: 数据库配置
echo ============================================
echo 【1/6】数据库配置 (PostgreSQL)
echo ============================================
set /p DB_HOST="数据库主机地址 [默认: localhost]: "
if "!DB_HOST!"=="" set DB_HOST=localhost

set /p DB_PORT="数据库端口 [默认: 5432]: "
if "!DB_PORT!"=="" set DB_PORT=5432

set /p DB_NAME="数据库名称 [默认: fitness_ai_db]: "
if "!DB_NAME!"=="" set DB_NAME=fitness_ai_db

set /p DB_USERNAME="数据库用户名 [默认: fitness_user]: "
if "!DB_USERNAME!"=="" set DB_USERNAME=fitness_user

set /p DB_PASSWORD="数据库密码 [默认: myPostgresPass123]: "
if "!DB_PASSWORD!"=="" set DB_PASSWORD=myPostgresPass123

echo.
echo ============================================
echo 【2/6】Redis 配置
echo ============================================
set /p REDIS_HOST="Redis 主机地址 [默认: localhost]: "
if "!REDIS_HOST!"=="" set REDIS_HOST=localhost

set /p REDIS_PORT="Redis 端口 [默认: 6379]: "
if "!REDIS_PORT!"=="" set REDIS_PORT=6379

set /p REDIS_PASSWORD="Redis 密码 [默认: myRedisPass123]: "
if "!REDIS_PASSWORD!"=="" set REDIS_PASSWORD=myRedisPass123

echo.
echo ============================================
echo 【3/6】MinIO 配置 (文件存储)
echo ============================================
set /p MINIO_ENDPOINT="MinIO 服务地址 [默认: http://localhost:9000]: "
if "!MINIO_ENDPOINT!"=="" set MINIO_ENDPOINT=http://localhost:9000

set /p MINIO_ACCESS_KEY="MinIO Access Key [默认: minioadmin]: "
if "!MINIO_ACCESS_KEY!"=="" set MINIO_ACCESS_KEY=minioadmin

set /p MINIO_SECRET_KEY="MinIO Secret Key [默认: minioPass123]: "
if "!MINIO_SECRET_KEY!"=="" set MINIO_SECRET_KEY=minioPass123

set /p MINIO_BUCKET="MinIO Bucket 名称 [默认: fitness-bucket]: "
if "!MINIO_BUCKET!"=="" set MINIO_BUCKET=fitness-bucket

echo.
echo ============================================
echo 【4/6】阿里云短信服务配置 (重要!)
echo ============================================
echo 请从阿里云控制台获取 AccessKey
echo 访问: https://ram.console.aliyun.com/manage/ak
echo.

set /p ALIYUN_SMS_ACCESS_KEY_ID="阿里云 Access Key ID (必填): "
if "!ALIYUN_SMS_ACCESS_KEY_ID!"=="" (
    echo 错误: Access Key ID 不能为空!
    pause
    exit /b 1
)

set /p ALIYUN_SMS_ACCESS_KEY_SECRET="阿里云 Access Key Secret (必填): "
if "!ALIYUN_SMS_ACCESS_KEY_SECRET!"=="" (
    echo 错误: Access Key Secret 不能为空!
    pause
    exit /b 1
)

echo.
echo ============================================
echo 【5/6】AI 服务配置 (通义千问)
echo ============================================
set /p AI_DASHSCOPE_API_KEY="DashScope API Key [默认: sk-7e9cad7dd0c240f19556d7bbf7b2acab]: "
if "!AI_DASHSCOPE_API_KEY!"=="" set AI_DASHSCOPE_API_KEY=sk-7e9cad7dd0c240f19556d7bbf7b2acab

set /p AI_DASHSCOPE_MODEL="DashScope 模型 [默认: tongyi-xiaomi-analysis-flash]: "
if "!AI_DASHSCOPE_MODEL!"=="" set AI_DASHSCOPE_MODEL=tongyi-xiaomi-analysis-flash

echo.
echo ============================================
echo 【6/6】JWT 密钥配置
echo ============================================
set /p JWT_SECRET="JWT 密钥 (用于Token签名) [默认: 自动生成]: "
if "!JWT_SECRET!"=="" (
    :: 生成随机JWT密钥
    for /f "tokens=*" %%a in ('powershell -Command "-join ((65..90) + (97..122) + (48..57) | Get-Random -Count 32 | ForEach-Object {[char]$_})"') do set JWT_SECRET=%%a
    echo 已自动生成JWT密钥: !JWT_SECRET!
)

echo.
echo ============================================
echo 配置汇总确认
echo ============================================
echo 数据库: !DB_HOST!:!DB_PORT!/!DB_NAME!
echo Redis: !REDIS_HOST!:!REDIS_PORT!
echo MinIO: !MINIO_ENDPOINT!
echo 阿里云短信: !ALIYUN_SMS_ACCESS_KEY_ID!
echo AI服务: !AI_DASHSCOPE_MODEL!
echo.
set /p CONFIRM="确认设置以上环境变量? (Y/N): "
if /i not "!CONFIRM!"=="Y" (
    echo 已取消设置
    pause
    exit /b 0
)

echo.
echo ============================================
echo 正在设置环境变量...
echo ============================================

:: 设置用户级环境变量
echo 设置数据库环境变量...
setx DB_HOST "!DB_HOST!" >nul 2>&1
setx DB_PORT "!DB_PORT!" >nul 2>&1
setx DB_NAME "!DB_NAME!" >nul 2>&1
setx DB_USERNAME "!DB_USERNAME!" >nul 2>&1
setx DB_PASSWORD "!DB_PASSWORD!" >nul 2>&1

echo 设置 Redis 环境变量...
setx REDIS_HOST "!REDIS_HOST!" >nul 2>&1
setx REDIS_PORT "!REDIS_PORT!" >nul 2>&1
setx REDIS_PASSWORD "!REDIS_PASSWORD!" >nul 2>&1

echo 设置 MinIO 环境变量...
setx MINIO_ENDPOINT "!MINIO_ENDPOINT!" >nul 2>&1
setx MINIO_ACCESS_KEY "!MINIO_ACCESS_KEY!" >nul 2>&1
setx MINIO_SECRET_KEY "!MINIO_SECRET_KEY!" >nul 2>&1
setx MINIO_BUCKET "!MINIO_BUCKET!" >nul 2>&1
setx MINIO_SECURE "false" >nul 2>&1

echo 设置阿里云短信环境变量...
setx ALIYUN_SMS_ACCESS_KEY_ID "!ALIYUN_SMS_ACCESS_KEY_ID!" >nul 2>&1
setx ALIYUN_SMS_ACCESS_KEY_SECRET "!ALIYUN_SMS_ACCESS_KEY_SECRET!" >nul 2>&1
setx ALIYUN_SMS_SIGN_NAME "速通互联验证码" >nul 2>&1
setx ALIYUN_SMS_TEMPLATE_CODE "100001" >nul 2>&1
setx ALIYUN_SMS_ENDPOINT "dypnsapi.aliyuncs.com" >nul 2>&1

echo 设置 AI 服务环境变量...
setx AI_DASHSCOPE_API_KEY "!AI_DASHSCOPE_API_KEY!" >nul 2>&1
setx AI_DASHSCOPE_MODEL "!AI_DASHSCOPE_MODEL!" >nul 2>&1
setx OLLAMA_BASE_URL "http://localhost:11434" >nul 2>&1
setx OLLAMA_EMBEDDING_MODEL "embeddinggemma:300m" >nul 2>&1

echo 设置 JWT 环境变量...
setx JWT_SECRET "!JWT_SECRET!" >nul 2>&1
setx JWT_ACCESS_EXPIRATION "1800000" >nul 2>&1
setx JWT_REFRESH_EXPIRATION "604800000" >nul 2>&1
setx JWT_REMEMBER_ME_EXPIRATION "2592000000" >nul 2>&1

echo.
echo ============================================
echo 环境变量设置完成!
echo ============================================
echo.
echo 重要提示:
echo 1. 请重启 IDE (IntelliJ IDEA / Eclipse) 使环境变量生效
echo 2. 重启后启动项目即可读取配置
echo.
echo 验证命令 (在CMD中执行):
echo   echo %%ALIYUN_SMS_ACCESS_KEY_ID%%
echo.
pause
