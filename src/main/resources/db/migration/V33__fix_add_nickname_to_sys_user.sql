-- ========================================================
-- V33: 修复添加 nickname 字段（V32 未实际生效）
-- 说明：为用户添加昵称字段，与用户名(username)区分
-- ========================================================

-- 检查并添加 nickname 字段（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'sys_user' AND column_name = 'nickname'
    ) THEN
        ALTER TABLE sys_user ADD COLUMN nickname VARCHAR(50);
        RAISE NOTICE 'Added nickname column to sys_user';
    ELSE
        RAISE NOTICE 'nickname column already exists in sys_user';
    END IF;
END $$;

-- 添加注释
COMMENT ON COLUMN sys_user.nickname IS '用户昵称/姓名，用于展示';

-- 为现有用户设置默认昵称（使用用户名作为默认昵称）
UPDATE sys_user SET nickname = username WHERE nickname IS NULL;

-- 创建索引（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE tablename = 'sys_user' AND indexname = 'idx_sys_user_nickname'
    ) THEN
        CREATE INDEX idx_sys_user_nickname ON sys_user(nickname);
        RAISE NOTICE 'Created index idx_sys_user_nickname';
    ELSE
        RAISE NOTICE 'Index idx_sys_user_nickname already exists';
    END IF;
END $$;
