-- ========================================================-- V32: 添加 nickname 字段到 sys_user 表-- 说明：为用户添加昵称字段，与用户名(username)区分-- ========================================================

-- 添加 nickname 字段ALTER TABLE sys_user ADD COLUMN nickname VARCHAR(50);

-- 添加注释COMMENT ON COLUMN sys_user.nickname IS '用户昵称/姓名，用于展示';

-- 为现有用户设置默认昵称（使用用户名作为默认昵称）UPDATE sys_user SET nickname = username WHERE nickname IS NULL;

-- 创建索引CREATE INDEX idx_sys_user_nickname ON sys_user(nickname);