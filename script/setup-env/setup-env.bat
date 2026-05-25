@echo off
setlocal EnableDelayedExpansion

echo ============================================
echo   AI Fitness System - Env Config Tool
echo ============================================
echo.
echo This script configures all env vars for the project.
echo Press Enter to accept defaults, or type a custom value.
echo All fields are optional - skip as needed.
echo.

echo ============================================
echo [1/7] Server
echo ============================================
set /p SERVER_PORT=Server port [default: 8080]: 
if "!SERVER_PORT!"=="" set SERVER_PORT=8080

echo.
echo ============================================
echo [2/7] PostgreSQL
echo ============================================
set /p DB_HOST=Host [default: localhost]: 
if "!DB_HOST!"=="" set DB_HOST=localhost

set /p DB_PORT=Port [default: 5432]: 
if "!DB_PORT!"=="" set DB_PORT=5432

set /p DB_NAME=DB name [default: fitness_ai_db]: 
if "!DB_NAME!"=="" set DB_NAME=fitness_ai_db

set /p DB_USERNAME=Username [default: fitness_user]: 
if "!DB_USERNAME!"=="" set DB_USERNAME=fitness_user

set /p DB_PASSWORD=Password [default: myPostgresPass123]: 
if "!DB_PASSWORD!"=="" set DB_PASSWORD=myPostgresPass123

echo.
echo ============================================
echo [3/7] Redis
echo ============================================
set /p REDIS_HOST=Host [default: localhost]: 
if "!REDIS_HOST!"=="" set REDIS_HOST=localhost

set /p REDIS_PORT=Port [default: 6379]: 
if "!REDIS_PORT!"=="" set REDIS_PORT=6379

set /p REDIS_PASSWORD=Password [default: myRedisPass123]: 
if "!REDIS_PASSWORD!"=="" set REDIS_PASSWORD=myRedisPass123

set /p REDIS_DATABASE=DB index [default: 0]: 
if "!REDIS_DATABASE!"=="" set REDIS_DATABASE=0

echo.
echo ============================================
echo [4/7] MinIO File Storage
echo ============================================
set /p MINIO_ENDPOINT=Endpoint [default: http://localhost:9000]: 
if "!MINIO_ENDPOINT!"=="" set MINIO_ENDPOINT=http://localhost:9000

set /p MINIO_ACCESS_KEY=Access Key [default: minioadmin]: 
if "!MINIO_ACCESS_KEY!"=="" set MINIO_ACCESS_KEY=minioadmin

set /p MINIO_SECRET_KEY=Secret Key [default: minioPass123]: 
if "!MINIO_SECRET_KEY!"=="" set MINIO_SECRET_KEY=minioPass123

set /p MINIO_BUCKET=Bucket name [default: fitness-bucket]: 
if "!MINIO_BUCKET!"=="" set MINIO_BUCKET=fitness-bucket

echo.
echo ============================================
echo [5/7] Alipay Payment
echo ============================================
echo Get keys from: https://open.alipay.com/
echo Press Enter to skip.
echo.
set /p ALIPAY_APP_ID=Alipay App ID [skip]: 

set /p ALIPAY_PRIVATE_KEY=App Private Key [skip]: 

set /p ALIPAY_PUBLIC_KEY=Alipay Public Key [skip]: 

set /p ALIPAY_SERVER_URL=Gateway URL [default: https://openapi.alipay.com/gateway.do]: 
if "!ALIPAY_SERVER_URL!"=="" set ALIPAY_SERVER_URL=https://openapi.alipay.com/gateway.do

set /p ALIPAY_NOTIFY_URL=Async Notify URL [skip]: 

set /p ALIPAY_RETURN_URL=Sync Return URL [skip]: 

echo.
echo ============================================
echo [6/7] Alibaba Cloud SMS
echo ============================================
echo Get AccessKey from: https://ram.console.aliyun.com/manage/ak
echo Press Enter to skip.
echo.
set /p ALIYUN_SMS_ACCESS_KEY_ID=Access Key ID [skip]: 

set /p ALIYUN_SMS_ACCESS_KEY_SECRET=Access Key Secret [skip]: 

echo.
echo ============================================
echo [7/7] AI Service ^& JWT
echo ============================================
set /p AI_DASHSCOPE_API_KEY=DashScope API Key [default: sk-7e9cad7dd0c240f19556d7bbf7b2acab]: 
if "!AI_DASHSCOPE_API_KEY!"=="" set AI_DASHSCOPE_API_KEY=sk-7e9cad7dd0c240f19556d7bbf7b2acab

set /p AI_DASHSCOPE_MODEL=DashScope Model [default: tongyi-xiaomi-analysis-flash]: 
if "!AI_DASHSCOPE_MODEL!"=="" set AI_DASHSCOPE_MODEL=tongyi-xiaomi-analysis-flash

set /p JWT_SECRET=JWT Secret [default: auto-generate]: 
if "!JWT_SECRET!"=="" (
    for /f "tokens=*" %%a in ('powershell -Command "-join ((65..90) + (97..122) + (48..57) | Get-Random -Count 32 | ForEach-Object {[char]$_})"') do set JWT_SECRET=%%a
    echo Auto-generated JWT secret: !JWT_SECRET!
)

set /p CORS_ALLOWED_ORIGINS=CORS origins (comma-separated) [default: http://localhost:3000]: 
if "!CORS_ALLOWED_ORIGINS!"=="" set CORS_ALLOWED_ORIGINS=http://localhost:3000

echo.
echo ============================================
echo Configuration Summary
echo ============================================
echo Server Port   : !SERVER_PORT!
echo Database      : !DB_HOST!:!DB_PORT!/!DB_NAME!
echo Redis         : !REDIS_HOST!:!REDIS_PORT!/!REDIS_DATABASE!
echo MinIO         : !MINIO_ENDPOINT!
if not "!ALIPAY_APP_ID!"=="" echo Alipay        : !ALIPAY_APP_ID!
if not "!ALIYUN_SMS_ACCESS_KEY_ID!"=="" echo Aliyun SMS    : !ALIYUN_SMS_ACCESS_KEY_ID!
echo AI Service    : !AI_DASHSCOPE_MODEL!
echo CORS Origins  : !CORS_ALLOWED_ORIGINS!
echo.
set /p CONFIRM=Apply these settings? (Y/N): 
if /i not "!CONFIRM!"=="Y" goto :cancel

echo.
echo ============================================
echo Applying environment variables...
echo ============================================

echo Setting current session vars ...
set SERVER_PORT=!SERVER_PORT!
set DB_HOST=!DB_HOST!
set DB_PORT=!DB_PORT!
set DB_NAME=!DB_NAME!
set DB_USERNAME=!DB_USERNAME!
set DB_PASSWORD=!DB_PASSWORD!
set REDIS_HOST=!REDIS_HOST!
set REDIS_PORT=!REDIS_PORT!
set REDIS_PASSWORD=!REDIS_PASSWORD!
set REDIS_DATABASE=!REDIS_DATABASE!
set MINIO_ENDPOINT=!MINIO_ENDPOINT!
set MINIO_ACCESS_KEY=!MINIO_ACCESS_KEY!
set MINIO_SECRET_KEY=!MINIO_SECRET_KEY!
set MINIO_BUCKET=!MINIO_BUCKET!
set MINIO_SECURE=false
set AI_DASHSCOPE_API_KEY=!AI_DASHSCOPE_API_KEY!
set AI_DASHSCOPE_MODEL=!AI_DASHSCOPE_MODEL!
set OLLAMA_BASE_URL=http://localhost:11434
set OLLAMA_EMBEDDING_MODEL=embeddinggemma:300m
set JWT_SECRET=!JWT_SECRET!
set JWT_ACCESS_EXPIRATION=1800000
set JWT_REFRESH_EXPIRATION=604800000
set JWT_REMEMBER_ME_EXPIRATION=2592000000
set CORS_ALLOWED_ORIGINS=!CORS_ALLOWED_ORIGINS!

echo Persisting to user env vars (setx) ...
setx SERVER_PORT "!SERVER_PORT!" >nul 2>&1
setx DB_HOST "!DB_HOST!" >nul 2>&1
setx DB_PORT "!DB_PORT!" >nul 2>&1
setx DB_NAME "!DB_NAME!" >nul 2>&1
setx DB_USERNAME "!DB_USERNAME!" >nul 2>&1
setx DB_PASSWORD "!DB_PASSWORD!" >nul 2>&1
setx REDIS_HOST "!REDIS_HOST!" >nul 2>&1
setx REDIS_PORT "!REDIS_PORT!" >nul 2>&1
setx REDIS_PASSWORD "!REDIS_PASSWORD!" >nul 2>&1
setx REDIS_DATABASE "!REDIS_DATABASE!" >nul 2>&1
setx MINIO_ENDPOINT "!MINIO_ENDPOINT!" >nul 2>&1
setx MINIO_ACCESS_KEY "!MINIO_ACCESS_KEY!" >nul 2>&1
setx MINIO_SECRET_KEY "!MINIO_SECRET_KEY!" >nul 2>&1
setx MINIO_BUCKET "!MINIO_BUCKET!" >nul 2>&1
setx MINIO_SECURE "false" >nul 2>&1

if not "!ALIPAY_APP_ID!"=="" (
    setx ALIPAY_APP_ID "!ALIPAY_APP_ID!" >nul 2>&1
    setx ALIPAY_PRIVATE_KEY "!ALIPAY_PRIVATE_KEY!" >nul 2>&1
    setx ALIPAY_PUBLIC_KEY "!ALIPAY_PUBLIC_KEY!" >nul 2>&1
    setx ALIPAY_SERVER_URL "!ALIPAY_SERVER_URL!" >nul 2>&1
    setx ALIPAY_NOTIFY_URL "!ALIPAY_NOTIFY_URL!" >nul 2>&1
    setx ALIPAY_RETURN_URL "!ALIPAY_RETURN_URL!" >nul 2>&1
)

if not "!ALIYUN_SMS_ACCESS_KEY_ID!"=="" (
    setx ALIYUN_SMS_ACCESS_KEY_ID "!ALIYUN_SMS_ACCESS_KEY_ID!" >nul 2>&1
    setx ALIYUN_SMS_ACCESS_KEY_SECRET "!ALIYUN_SMS_ACCESS_KEY_SECRET!" >nul 2>&1
)
setx ALIYUN_SMS_SIGN_NAME "SutongHulian-Verify" >nul 2>&1
setx ALIYUN_SMS_TEMPLATE_CODE "100001" >nul 2>&1
setx ALIYUN_SMS_ENDPOINT "dypnsapi.aliyuncs.com" >nul 2>&1

setx AI_DASHSCOPE_API_KEY "!AI_DASHSCOPE_API_KEY!" >nul 2>&1
setx AI_DASHSCOPE_MODEL "!AI_DASHSCOPE_MODEL!" >nul 2>&1
setx OLLAMA_BASE_URL "http://localhost:11434" >nul 2>&1
setx OLLAMA_EMBEDDING_MODEL "embeddinggemma:300m" >nul 2>&1

setx JWT_SECRET "!JWT_SECRET!" >nul 2>&1
setx JWT_ACCESS_EXPIRATION "1800000" >nul 2>&1
setx JWT_REFRESH_EXPIRATION "604800000" >nul 2>&1
setx JWT_REMEMBER_ME_EXPIRATION "2592000000" >nul 2>&1

setx CORS_ALLOWED_ORIGINS "!CORS_ALLOWED_ORIGINS!" >nul 2>&1

echo.
echo ============================================
echo  Done! Env vars configured.
echo ============================================
echo.
echo  Current session: applied
echo  User registry  : persisted (new terminals pick it up)
echo.
echo ============================================
echo Launch project now?
echo ============================================
