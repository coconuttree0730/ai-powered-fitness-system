-- 为用户健身档案表添加专属教练字段
DO $$ BEGIN
    ALTER TABLE user_fitness_profile ADD COLUMN private_coach_id BIGINT;
EXCEPTION
    WHEN duplicate_column THEN
        NULL;
END $$;

-- 添加列注释
COMMENT ON COLUMN user_fitness_profile.private_coach_id IS '专属教练ID（关联sys_user表）';

-- 添加外键约束（先检查是否已存在）
DO $$ BEGIN
    ALTER TABLE user_fitness_profile
        ADD CONSTRAINT fk_user_fitness_profile_private_coach_id
        FOREIGN KEY (private_coach_id) REFERENCES sys_user(id);
EXCEPTION
    WHEN duplicate_object THEN
        NULL;
    WHEN undefined_table THEN
        NULL;
    WHEN others THEN
        NULL;
END $$;

-- 创建索引（使用 IF NOT EXISTS）
DO $$ BEGIN
    CREATE INDEX idx_user_fitness_profile_private_coach_id ON user_fitness_profile(private_coach_id);
EXCEPTION
    WHEN duplicate_table THEN
        NULL;
    WHEN others THEN
        NULL;
END $$;
