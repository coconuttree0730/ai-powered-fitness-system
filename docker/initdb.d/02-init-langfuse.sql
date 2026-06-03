-- Langfuse 数据库初始化
-- 创建 langfuse_db 数据库（如果不存在）
DO
$do$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'langfuse_db') THEN
      CREATE DATABASE langfuse_db;
   END IF;
END
$do$;

-- 授权
GRANT ALL PRIVILEGES ON DATABASE langfuse_db TO fitness_user;

-- 连接到 langfuse_db 并创建扩展
\c langfuse_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
