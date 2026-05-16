#!/bin/bash

# AI Fitness System - 环境变量配置工具
# Linux/macOS 版本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  AI Fitness System - 环境变量配置工具${NC}"
echo -e "${BLUE}  Linux/macOS 版本${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""
echo "本脚本将引导您配置系统所需的环境变量"
echo "按提示输入值，直接回车将使用默认值（如有）"
echo ""

# 检查是否为交互式终端
if [ ! -t 0 ]; then
    echo -e "${RED}错误: 请在交互式终端中运行此脚本${NC}"
    exit 1
fi

# 函数：读取用户输入
read_input() {
    local prompt="$1"
    local default="$2"
    local required="$3"
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
        
        if [ "$required" = "true" ] && [ -z "$value" ]; then
            echo -e "${RED}此项为必填项，不能为空!${NC}"
        else
            echo "$value"
            return
        fi
    done
}

echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}【1/6】数据库配置 (PostgreSQL)${NC}"
echo -e "${YELLOW}============================================${NC}"
DB_HOST=$(read_input "数据库主机地址" "localhost")
DB_PORT=$(read_input "数据库端口" "5432")
DB_NAME=$(read_input "数据库名称" "fitness_ai_db")
DB_USERNAME=$(read_input "数据库用户名" "fitness_user")
DB_PASSWORD=$(read_input "数据库密码" "myPostgresPass123")

echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}【2/6】Redis 配置${NC}"
echo -e "${YELLOW}============================================${NC}"
REDIS_HOST=$(read_input "Redis 主机地址" "localhost")
REDIS_PORT=$(read_input "Redis 端口" "6379")
REDIS_PASSWORD=$(read_input "Redis 密码" "myRedisPass123")

echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}【3/6】MinIO 配置 (文件存储)${NC}"
echo -e "${YELLOW}============================================${NC}"
MINIO_ENDPOINT=$(read_input "MinIO 服务地址" "http://localhost:9000")
MINIO_ACCESS_KEY=$(read_input "MinIO Access Key" "minioadmin")
MINIO_SECRET_KEY=$(read_input "MinIO Secret Key" "minioPass123")
MINIO_BUCKET=$(read_input "MinIO Bucket 名称" "fitness-bucket")

echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}【4/6】阿里云短信服务配置 (重要!)${NC}"
echo -e "${YELLOW}============================================${NC}"
echo "请从阿里云控制台获取 AccessKey"
echo "访问: https://ram.console.aliyun.com/manage/ak"
echo ""
ALIYUN_SMS_ACCESS_KEY_ID=$(read_input "阿里云 Access Key ID" "" "true")
ALIYUN_SMS_ACCESS_KEY_SECRET=$(read_input "阿里云 Access Key Secret" "" "true")

echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}【5/6】AI 服务配置 (通义千问)${NC}"
echo -e "${YELLOW}============================================${NC}"
AI_DASHSCOPE_API_KEY=$(read_input "DashScope API Key" "sk-7e9cad7dd0c240f19556d7bbf7b2acab")
AI_DASHSCOPE_MODEL=$(read_input "DashScope 模型" "tongyi-xiaomi-analysis-flash")

echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}【6/6】JWT 密钥配置${NC}"
echo -e "${YELLOW}============================================${NC}"
JWT_SECRET=$(read_input "JWT 密钥 (用于Token签名，建议32位以上)" "")
if [ -z "$JWT_SECRET" ]; then
    JWT_SECRET=$(openssl rand -base64 32 2>/dev/null || head -c 32 /dev/urandom | base64)
    echo -e "${GREEN}已自动生成JWT密钥${NC}"
fi

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}配置汇总确认${NC}"
echo -e "${BLUE}============================================${NC}"
echo "数据库: $DB_HOST:$DB_PORT/$DB_NAME"
echo "Redis: $REDIS_HOST:$REDIS_PORT"
echo "MinIO: $MINIO_ENDPOINT"
echo "阿里云短信: $ALIYUN_SMS_ACCESS_KEY_ID"
echo "AI服务: $AI_DASHSCOPE_MODEL"
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

# 检测 shell 类型
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

# 备份原配置文件
if [ -f "$SHELL_RC" ]; then
    cp "$SHELL_RC" "$SHELL_RC.backup.$(date +%Y%m%d%H%M%S)"
    echo "已备份原配置文件"
fi

# 写入环境变量到配置文件
cat >> "$SHELL_RC" << EOF

# ============================================
# AI Fitness System 环境变量配置
# 生成时间: $(date)
# ============================================

# 数据库配置 (PostgreSQL)
export DB_HOST="$DB_HOST"
export DB_PORT="$DB_PORT"
export DB_NAME="$DB_NAME"
export DB_USERNAME="$DB_USERNAME"
export DB_PASSWORD="$DB_PASSWORD"

# Redis 配置
export REDIS_HOST="$REDIS_HOST"
export REDIS_PORT="$REDIS_PORT"
export REDIS_PASSWORD="$REDIS_PASSWORD"

# MinIO 配置
export MINIO_ENDPOINT="$MINIO_ENDPOINT"
export MINIO_ACCESS_KEY="$MINIO_ACCESS_KEY"
export MINIO_SECRET_KEY="$MINIO_SECRET_KEY"
export MINIO_BUCKET="$MINIO_BUCKET"
export MINIO_SECURE="false"

# 阿里云短信服务配置
export ALIYUN_SMS_ACCESS_KEY_ID="$ALIYUN_SMS_ACCESS_KEY_ID"
export ALIYUN_SMS_ACCESS_KEY_SECRET="$ALIYUN_SMS_ACCESS_KEY_SECRET"
export ALIYUN_SMS_SIGN_NAME="速通互联验证码"
export ALIYUN_SMS_TEMPLATE_CODE="100001"
export ALIYUN_SMS_ENDPOINT="dypnsapi.aliyuncs.com"

# AI 服务配置
export AI_DASHSCOPE_API_KEY="$AI_DASHSCOPE_API_KEY"
export AI_DASHSCOPE_MODEL="$AI_DASHSCOPE_MODEL"
export OLLAMA_BASE_URL="http://localhost:11434"
export OLLAMA_EMBEDDING_MODEL="embeddinggemma:300m"

# JWT 配置
export JWT_SECRET="$JWT_SECRET"
export JWT_ACCESS_EXPIRATION="1800000"
export JWT_REFRESH_EXPIRATION="604800000"
export JWT_REMEMBER_ME_EXPIRATION="2592000000"

# ============================================
EOF

echo -e "${GREEN}环境变量已写入 $SHELL_RC${NC}"
echo ""

# 同时写入 /etc/environment (需要 sudo)
if command -v sudo &> /dev/null; then
    echo "尝试写入系统级环境变量 (需要 sudo 权限)..."
    sudo tee -a /etc/environment > /dev/null << EOF

# AI Fitness System 环境变量
DB_HOST="$DB_HOST"
DB_PORT="$DB_PORT"
DB_NAME="$DB_NAME"
DB_USERNAME="$DB_USERNAME"
DB_PASSWORD="$DB_PASSWORD"
REDIS_HOST="$REDIS_HOST"
REDIS_PORT="$REDIS_PORT"
REDIS_PASSWORD="$REDIS_PASSWORD"
MINIO_ENDPOINT="$MINIO_ENDPOINT"
MINIO_ACCESS_KEY="$MINIO_ACCESS_KEY"
MINIO_SECRET_KEY="$MINIO_SECRET_KEY"
MINIO_BUCKET="$MINIO_BUCKET"
ALIYUN_SMS_ACCESS_KEY_ID="$ALIYUN_SMS_ACCESS_KEY_ID"
ALIYUN_SMS_ACCESS_KEY_SECRET="$ALIYUN_SMS_ACCESS_KEY_SECRET"
JWT_SECRET="$JWT_SECRET"
EOF
    echo -e "${GREEN}系统级环境变量已设置${NC}"
fi

echo ""
echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}环境变量设置完成!${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
echo -e "${YELLOW}重要提示:${NC}"
echo "1. 请执行以下命令使环境变量生效:"
echo -e "   ${BLUE}source $SHELL_RC${NC}"
echo ""
echo "2. 或者重新打开终端窗口"
echo ""
echo "3. 如果使用 IDE，请重启 IDE 使环境变量生效"
echo ""
echo "验证命令:"
echo -e "   ${BLUE}echo \$ALIYUN_SMS_ACCESS_KEY_ID${NC}"
echo ""

# 提示是否立即 source
read -rp "是否立即 source 配置文件? (Y/N): " SOURCE_NOW
if [[ "$SOURCE_NOW" =~ ^[Yy]$ ]]; then
    source "$SHELL_RC"
    echo -e "${GREEN}已加载环境变量${NC}"
    echo ""
    echo "验证阿里云配置:"
    echo "ALIYUN_SMS_ACCESS_KEY_ID=$ALIYUN_SMS_ACCESS_KEY_ID"
fi

echo ""
echo -e "${GREEN}配置完成!${NC}"
