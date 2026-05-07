-- 修复 chat_long_term_memory 表的时间字段命名
-- 从 created_at/updated_at 改为 create_time/update_time

-- 1. 删除旧的触发器
DROP TRIGGER IF EXISTS update_chat_long_term_memory_update_time ON chat_long_term_memory;
DROP FUNCTION IF EXISTS update_chat_long_term_memory_updated_at();

-- 2. 重命名字段
ALTER TABLE chat_long_term_memory RENAME COLUMN created_at TO create_time;
ALTER TABLE chat_long_term_memory RENAME COLUMN updated_at TO update_time;

-- 3. 创建新的触发器函数
CREATE OR REPLACE FUNCTION update_chat_long_term_memory_update_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4. 创建新的触发器
CREATE TRIGGER update_chat_long_term_memory_update_time
BEFORE UPDATE ON chat_long_term_memory
    FOR EACH ROW EXECUTE FUNCTION update_chat_long_term_memory_update_time();
