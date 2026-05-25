-- ========================================================
-- 创建用户健身档案表
-- ========================================================

-- 使用 IF NOT EXISTS 确保幂等性
CREATE TABLE IF NOT EXISTS user_fitness_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    height DECIMAL(5,2),
    weight DECIMAL(5,2),
    age INTEGER,
    experience VARCHAR(20),
    fitness_goal VARCHAR(20),
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 添加列注释
COMMENT ON TABLE user_fitness_profile IS '用户健身档案表';
COMMENT ON COLUMN user_fitness_profile.user_id IS '用户 ID';
COMMENT ON COLUMN user_fitness_profile.height IS '身高 (cm)';
COMMENT ON COLUMN user_fitness_profile.weight IS '体重 (kg)';
COMMENT ON COLUMN user_fitness_profile.age IS '年龄';
COMMENT ON COLUMN user_fitness_profile.experience IS '健身经验：BEGINNER-初学者，INTERMEDIATE-中级，ADVANCED-高级';
COMMENT ON COLUMN user_fitness_profile.fitness_goal IS '健身目标：WEIGHT_LOSS-减脂，MUSCLE_GAIN-增肌，BODY_SHAPING-塑形，ENDURANCE-增强体能，HEALTH-保持健康';
COMMENT ON COLUMN user_fitness_profile.deleted IS '软删除标识';

-- 创建索引（使用 IF NOT EXISTS）
DO $$ BEGIN
    CREATE INDEX idx_user_fitness_profile_user_id ON user_fitness_profile(user_id);
EXCEPTION
    WHEN duplicate_table THEN
        NULL;
    WHEN others THEN
        NULL;
END $$;

-- 添加外键约束（先检查是否已存在）
DO $$ BEGIN
    ALTER TABLE user_fitness_profile
        ADD CONSTRAINT fk_user_fitness_profile_user_id
        FOREIGN KEY (user_id) REFERENCES sys_user(id);
EXCEPTION
    WHEN duplicate_object THEN
        NULL;
    WHEN undefined_table THEN
        NULL;
    WHEN others THEN
        NULL;
END $$;
