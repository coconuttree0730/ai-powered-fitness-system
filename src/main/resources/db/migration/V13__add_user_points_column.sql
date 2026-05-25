-- 为用户表添加积分字段
DO $$ BEGIN
    ALTER TABLE sys_user ADD COLUMN points INTEGER DEFAULT 0;
EXCEPTION
    WHEN duplicate_column THEN
        NULL;
END $$;

-- 添加列注释
COMMENT ON COLUMN sys_user.points IS '积分余额';

-- 为现有用户设置默认积分
UPDATE sys_user SET points = 1000 WHERE points IS NULL;
