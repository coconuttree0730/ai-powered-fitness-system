-- ========================================================
-- 用户健身档案表添加性别字段
-- ========================================================

-- 添加性别字段
ALTER TABLE user_fitness_profile ADD COLUMN IF NOT EXISTS gender VARCHAR(10);

-- 添加列注释
COMMENT ON COLUMN user_fitness_profile.gender IS '性别：MALE-男，FEMALE-女';
