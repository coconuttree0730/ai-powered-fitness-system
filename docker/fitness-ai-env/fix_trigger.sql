-- 删除旧的触发器
DROP TRIGGER IF EXISTS update_chat_long_term_memory_update_time ON chat_long_term_memory;

-- 创建正确的触发器函数
CREATE OR REPLACE FUNCTION update_chat_long_term_memory_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建新的触发器
CREATE TRIGGER update_chat_long_term_memory_update_time
BEFORE UPDATE ON chat_long_term_memory
    FOR EACH ROW EXECUTE FUNCTION update_chat_long_term_memory_updated_at();
