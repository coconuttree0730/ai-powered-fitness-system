#!/bin/bash

# AI Fitness System - 环境变量配置工具
# Linux/macOS 版本

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  AI Fitness System - 环境变量配置工具${NC}"
echo -e "${BLUE}  Linux/macOS 版本${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""
echo "本脚本将引导您配置系统所需的环境变量"
echo "所有输入均可跳过，缺失的变量可在后续手动补充"
echo ""

if [ ! -t 0 ]; then
    echo -e "${RED}错误: 请在交互式终端中运行此脚本${NC}"
    exit 1
fi

read_input() {
    local prompt="$1"
    local default="$2"
    local value=""
    while true; do
        if [ -n "$default" ]; then
            read -rp "$prompt [默认: $default]: " value
            if [ -z "$value" ]; then
                value="$default"
            fi
        else
            read -rp "$prompt: " value
        fi
        echo "$value"
        return
    done
}

echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}【1/7】服务器配置${NC}"
echo -e "${YELLOW}============================================${NC}"
SERVER_PORT=$(read_input "应用服务端口" "8080")

echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}【2/7】数据库配置 (PostgreSQL)${NC}"
echo -e "${YELLOW}============================================${NC}"
DB_HOST=$(read_input "数据库主机地址" "localhost")
DB_PORT=$(read_input "数据库端口" "5432")
DB_NAME=$(read_input "数据库名称" "fitness_ai_db")
DB_USERNAME=$(read_input "数据库用户名" "fitness_user")
DB_PASSWORD=$(read_input "数据库密码" "myPostgresPass123")

echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}【3/7】Redis 配置${NC}"
echo -e "${YELLOW}============================================${NC}"
REDIS_HOST=$(read_input "Redis 主机地址" "localhost")
REDIS_PORT=$(read_input "Redis 端口" "6379")
REDIS_PASSWORD=$(read_input "Redis 密码" "myRedisPass123")
REDIS_DATABASE=$(read_input "Redis 数据库编号" "0")

echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}【4/7】MinIO 配置 (文件存储)${NC}"
echo -e "${YELLOW}============================================${NC}"
MINIO_ENDPOINT=$(read_input "MinIO 服务地址" "http://localhost:9000")
MINIO_ACCESS_KEY=$(read_input "MinIO Access Key" "minioadmin")
MINIO_SECRET_KEY=$(read_input "MinIO Secret Key" "minioPass123")
MINIO_BUCKET=$(read_input "MinIO Bucket 名称" "fitness-bucket")

echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}【5/7】支付宝支付配置${NC}"
echo -e "${YELLOW}============================================${NC}"
echo "请从支付宝开放平台获取: https://open.alipay.com/"
echo "直接回车跳过，后续可手动补充"
echo ""
ALIPAY_APP_ID=$(read_input "支付宝 App ID" "")
ALIPAY_PRIVATE_KEY=$(read_input "应用私钥" "")
ALIPAY_PUBLIC_KEY=$(read_input "支付宝公钥" "")
ALIPAY_SERVER_URL=$(read_input "支付宝网关地址" "https://openapi.alipay.com/gateway.do")
ALIPAY_NOTIFY_URL=$(read_input "异步通知地址" "")
ALIPAY_RETURN_URL=$(read_input "同步跳转地址" "")

echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}【6/7】阿里云短信服务配置${NC}"
echo -e "${YELLOW}============================================${NC}"
echo "请从阿里云控制台获取: https://ram.console.aliyun.com/manage/ak"
echo "直接回车跳过，后续可手动补充"
echo ""
ALIYUN_SMS_ACCESS_KEY_ID=$(read_input "阿里云 Access Key ID" "")
ALIYUN_SMS_ACCESS_KEY_SECRET=$(read_input "阿里云 Access Key Secret" "")

echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}【7/7】AI 服务与 JWT 配置${NC}"
echo -e "${YELLOW}============================================${NC}"
AI_DASHSCOPE_API_KEY=$(read_input "DashScope API Key" "sk-7e9cad7dd0c240f19556d7bbf7b2acab")
AI_DASHSCOPE_MODEL=$(read_input "DashScope 模型" "tongyi-xiaomi-analysis-flash")

JWT_SECRET=$(read_input "JWT 密钥 (用于Token签名)" "")
if [ -z "$JWT_SECRET" ]; then
    JWT_SECRET=$(openssl rand -base64 32 2>/dev/null || head -c 32 /dev/urandom | base64)
    echo -e "${GREEN}已自动生成JWT密钥${NC}"
fi

CORS_ALLOWED_ORIGINS=$(read_input "跨域允许来源（多个用逗号分隔）" "http://localhost:3000")

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}配置汇总确认${NC}"
echo -e "${BLUE}============================================${NC}"
echo "服务器端口: $SERVER_PORT"
echo "数据库: $DB_HOST:$DB_PORT/$DB_NAME"
echo "Redis: $REDIS_HOST:$REDIS_PORT/$REDIS_DATABASE"
echo "MinIO: $MINIO_ENDPOINT"
if [ -n "$ALIPAY_APP_ID" ]; then echo "支付宝: $ALIPAY_APP_ID"; fi
if [ -n "$ALIYUN_SMS_ACCESS_KEY_ID" ]; then echo "阿里云短信: $ALIYUN_SMS_ACCESS_KEY_ID"; fi
echo "AI服务: $AI_DASHSCOPE_MODEL"
echo "跨域来源: $CORS_ALLOWED_ORIGINS"
echo ""

read -rp "确认设置以上环境变量? (Y/N): " CONFIRM
if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}已取消设置${NC}"
    exit 0
fi

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}正在设置环境变量...${NC}"
echo -e "${BLUE}============================================${NC}"

# === 1. 设置当前会话（export，立刻生效）===
echo "设置当前会话环境变量..."
export SERVER_PORT="$SERVER_PORT"
export DB_HOST="$DB_HOST"
export DB_PORT="$DB_PORT"
export DB_NAME="$DB_NAME"
export DB_USERNAME="$DB_USERNAME"
export DB_PASSWORD="$DB_PASSWORD"
export REDIS_HOST="$REDIS_HOST"
export REDIS_PORT="$REDIS_PORT"
export REDIS_PASSWORD="$REDIS_PASSWORD"
export REDIS_DATABASE="$REDIS_DATABASE"
export MINIO_ENDPOINT="$MINIO_ENDPOINT"
export MINIO_ACCESS_KEY="$MINIO_ACCESS_KEY"
export MINIO_SECRET_KEY="$MINIO_SECRET_KEY"
export MINIO_BUCKET="$MINIO_BUCKET"
export MINIO_SECURE="false"
if [ -n "$ALIPAY_APP_ID" ]; then
    export ALIPAY_APP_ID="$ALIPAY_APP_ID"
    export ALIPAY_PRIVATE_KEY="$ALIPAY_PRIVATE_KEY"
    export ALIPAY_PUBLIC_KEY="$ALIPAY_PUBLIC_KEY"
    export ALIPAY_SERVER_URL="$ALIPAY_SERVER_URL"
    export ALIPAY_NOTIFY_URL="$ALIPAY_NOTIFY_URL"
    export ALIPAY_RETURN_URL="$ALIPAY_RETURN_URL"
fi
if [ -n "$ALIYUN_SMS_ACCESS_KEY_ID" ]; then
    export ALIYUN_SMS_ACCESS_KEY_ID="$ALIYUN_SMS_ACCESS_KEY_ID"
    export ALIYUN_SMS_ACCESS_KEY_SECRET="$ALIYUN_SMS_ACCESS_KEY_SECRET"
fi
export ALIYUN_SMS_SIGN_NAME="SutongHulian-Verify"
export ALIYUN_SMS_TEMPLATE_CODE="100001"
export ALIYUN_SMS_ENDPOINT="dypnsapi.aliyuncs.com"
export AI_DASHSCOPE_API_KEY="$AI_DASHSCOPE_API_KEY"
export AI_DASHSCOPE_MODEL="$AI_DASHSCOPE_MODEL"
export OLLAMA_BASE_URL="http://localhost:11434"
export OLLAMA_EMBEDDING_MODEL="embeddinggemma:300m"
export JWT_SECRET="$JWT_SECRET"
export JWT_ACCESS_EXPIRATION="1800000"
export JWT_REFRESH_EXPIRATION="604800000"
export JWT_REMEMBER_ME_EXPIRATION="2592000000"
export CORS_ALLOWED_ORIGINS="$CORS_ALLOWED_ORIGINS"

# === 2. 持久化到 shell 配置文件 ===
SHELL_TYPE=""
if [ -n "$ZSH_VERSION" ]; then
    SHELL_TYPE="zsh"
    SHELL_RC="$HOME/.zshrc"
elif [ -n "$BASH_VERSION" ]; then
    SHELL_TYPE="bash"
    SHELL_RC="$HOME/.bashrc"
else
    SHELL_TYPE="sh"
    SHELL_RC="$HOME/.profile"
fi

echo "检测到 shell 类型: $SHELL_TYPE"
echo "将写入配置文件: $SHELL_RC"
echo ""

if [ -f "$SHELL_RC" ]; then
    cp "$SHELL_RC" "$SHELL_RC.backup.$(date +%Y%m%d%H%M%S)"
    echo "已备份原配置文件"
fi

cat >> "$SHELL_RC" << EOF

# AI Fitness System 环境变量 (生成时间: $(date))
export SERVER_PORT="$SERVER_PORT"
export DB_HOST="$DB_HOST"
export DB_PORT="$DB_PORT"
export DB_NAME="$DB_NAME"
export DB_USERNAME="$DB_USERNAME"
export DB_PASSWORD="$DB_PASSWORD"
export REDIS_HOST="$REDIS_HOST"
export REDIS_PORT="$REDIS_PORT"
export REDIS_PASSWORD="$REDIS_PASSWORD"
export REDIS_DATABASE="$REDIS_DATABASE"
export MINIO_ENDPOINT="$MINIO_ENDPOINT"
export MINIO_ACCESS_KEY="$MINIO_ACCESS_KEY"
export MINIO_SECRET_KEY="$MINIO_SECRET_KEY"
export MINIO_BUCKET="$MINIO_BUCKET"
export MINIO_SECURE="false"
EOF

if [ -n "$ALIPAY_APP_ID" ]; then
    cat >> "$SHELL_RC" << EOF
export ALIPAY_APP_ID="$ALIPAY_APP_ID"
export ALIPAY_PRIVATE_KEY="$ALIPAY_PRIVATE_KEY"
export ALIPAY_PUBLIC_KEY="$ALIPAY_PUBLIC_KEY"
export ALIPAY_SERVER_URL="$ALIPAY_SERVER_URL"
export ALIPAY_NOTIFY_URL="$ALIPAY_NOTIFY_URL"
export ALIPAY_RETURN_URL="$ALIPAY_RETURN_URL"
EOF
fi

if [ -n "$ALIYUN_SMS_ACCESS_KEY_ID" ]; then
    cat >> "$SHELL_RC" << EOF
export ALIYUN_SMS_ACCESS_KEY_ID="$ALIYUN_SMS_ACCESS_KEY_ID"
export ALIYUN_SMS_ACCESS_KEY_SECRET="$ALIYUN_SMS_ACCESS_KEY_SECRET"
EOF
fi

cat >> "$SHELL_RC" << EOF
export ALIYUN_SMS_SIGN_NAME="SutongHulian-Verify"
export ALIYUN_SMS_TEMPLATE_CODE="100001"
export ALIYUN_SMS_ENDPOINT="dypnsapi.aliyuncs.com"
export AI_DASHSCOPE_API_KEY="$AI_DASHSCOPE_API_KEY"
export AI_DASHSCOPE_MODEL="$AI_DASHSCOPE_MODEL"
export OLLAMA_BASE_URL="http://localhost:11434"
export OLLAMA_EMBEDDING_MODEL="embeddinggemma:300m"
export JWT_SECRET="$JWT_SECRET"
export JWT_ACCESS_EXPIRATION="1800000"
export JWT_REFRESH_EXPIRATION="604800000"
export JWT_REMEMBER_ME_EXPIRATION="2592000000"
export CORS_ALLOWED_ORIGINS="$CORS_ALLOWED_ORIGINS"
EOF

echo -e "${GREEN}环境变量已写入 $SHELL_RC${NC}"
echo ""

echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}环境变量已设置完成!${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
echo "  当前会话已生效（export）"
echo "  已持久化到 $SHELL_RC（永久生效）"
echo ""
echo -e "${GREEN}============================================${NC}"
echo -e "是否立即启动项目？${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
read -rp "启动项目? (Y/N) [默认: N]: " LAUNCH_CHOICE
if [[ "$LAUNCH_CHOICE" =~ ^[Yy]$ ]]; then
    echo ""
    echo "正在启动项目..."
    echo "提示: 按 Ctrl+C 可停止项目"
    echo ""
    cd "$(dirname "$0")/../.."
    mvn spring-boot:run
fi

echo ""
echo -e "${GREEN}配置完成!${NC}"