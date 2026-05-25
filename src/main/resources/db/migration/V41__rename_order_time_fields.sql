-- 修改 membership_order 表的时间字段命名，统一为 create_time 和 update_time

-- 先添加新字段
ALTER TABLE membership_order ADD COLUMN IF NOT EXISTS create_time TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE membership_order ADD COLUMN IF NOT EXISTS update_time TIMESTAMP WITHOUT TIME ZONE;

-- 将旧字段数据迁移到新字段
UPDATE membership_order SET create_time = created_at WHERE created_at IS NOT NULL;
UPDATE membership_order SET update_time = updated_at WHERE updated_at IS NOT NULL;

-- 删除旧字段
ALTER TABLE membership_order DROP COLUMN IF EXISTS created_at;
ALTER TABLE membership_order DROP COLUMN IF EXISTS updated_at;

-- 添加注释
COMMENT ON COLUMN membership_order.create_time IS '创建时间';
COMMENT ON COLUMN membership_order.update_time IS '更新时间';
