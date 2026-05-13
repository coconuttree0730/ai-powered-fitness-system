-- ========================================================-- V47: 添加 real_name 字段到 coach_detail 表-- 说明：为教练详情添加真实姓名字段，用于前端展示-- ========================================================

-- 检查并添加 real_name 字段（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'coach_detail' AND column_name = 'real_name'
    ) THEN
        ALTER TABLE coach_detail ADD COLUMN real_name VARCHAR(50);
        RAISE NOTICE 'Added real_name column to coach_detail';
    ELSE
        RAISE NOTICE 'real_name column already exists in coach_detail';
    END IF;
END $$;

-- 添加注释
COMMENT ON COLUMN coach_detail.real_name IS '教练真实姓名，用于前端展示';

-- 为现有教练数据设置默认真实姓名（从 sys_user 表的 nickname 获取，如果没有则使用 username）
UPDATE coach_detail 
SET real_name = COALESCE(
    (SELECT nickname FROM sys_user WHERE sys_user.id = coach_detail.user_id),
    (SELECT username FROM sys_user WHERE sys_user.id = coach_detail.user_id)
)
WHERE real_name IS NULL;

-- 创建索引（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE tablename = 'coach_detail' AND indexname = 'idx_coach_detail_real_name'
    ) THEN
        CREATE INDEX idx_coach_detail_real_name ON coach_detail(real_name);
        RAISE NOTICE 'Created index idx_coach_detail_real_name';
    ELSE
        RAISE NOTICE 'Index idx_coach_detail_real_name already exists';
    END IF;
END $$;
