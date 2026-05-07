CREATE TABLE chat_long_term_memory (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    memory_key VARCHAR(100) NOT NULL,
    memory_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB DEFAULT '{}'::jsonb NOT NULL,
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (user_id, memory_key)
);

COMMENT ON TABLE chat_long_term_memory IS '健小助长期记忆表';
COMMENT ON COLUMN chat_long_term_memory.user_id IS '用户ID';
COMMENT ON COLUMN chat_long_term_memory.memory_key IS '记忆唯一键';
COMMENT ON COLUMN chat_long_term_memory.memory_type IS '记忆类型';
COMMENT ON COLUMN chat_long_term_memory.content IS '记忆内容';
COMMENT ON COLUMN chat_long_term_memory.metadata IS '扩展元数据';

CREATE INDEX idx_chat_long_term_memory_user_id ON chat_long_term_memory(user_id);
CREATE INDEX idx_chat_long_term_memory_type ON chat_long_term_memory(memory_type);

-- 创建专门的触发器函数
CREATE OR REPLACE FUNCTION update_chat_long_term_memory_update_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_chat_long_term_memory_update_time
BEFORE UPDATE ON chat_long_term_memory
    FOR EACH ROW EXECUTE FUNCTION update_chat_long_term_memory_update_time();
