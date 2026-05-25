-- ========================================================
-- 智能健身系统数据库迁移脚本
-- 版本：V28
-- 说明：补充报修系统缺失的字段和表（V24脚本内容被替换，此处补充）
-- ========================================================

-- 添加 handle_remark 字段到报修表（如果不存在）
ALTER TABLE fitness_equipment_repair
    ADD COLUMN IF NOT EXISTS handle_remark VARCHAR(500);

COMMENT ON COLUMN fitness_equipment_repair.handle_remark IS '处理备注';

-- 创建报修处理记录表（如果不存在）
CREATE TABLE IF NOT EXISTS fitness_repair_record (
    id BIGSERIAL PRIMARY KEY,
    repair_id BIGINT NOT NULL,
    handler_id BIGINT,
    record_type SMALLINT DEFAULT 1 NOT NULL,
    before_status SMALLINT,
    after_status SMALLINT,
    content TEXT,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE fitness_repair_record IS '报修处理记录表';
COMMENT ON COLUMN fitness_repair_record.repair_id IS '报修ID';
COMMENT ON COLUMN fitness_repair_record.handler_id IS '处理人ID（管理员）';
COMMENT ON COLUMN fitness_repair_record.record_type IS '处理类型：1-提交报修, 2-状态变更, 3-处理备注, 4-取消报修';
COMMENT ON COLUMN fitness_repair_record.before_status IS '处理前状态';
COMMENT ON COLUMN fitness_repair_record.after_status IS '处理后状态';
COMMENT ON COLUMN fitness_repair_record.content IS '处理内容/备注';
COMMENT ON COLUMN fitness_repair_record.deleted IS '软删除标识';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_fitness_repair_record_repair_id ON fitness_repair_record(repair_id);
CREATE INDEX IF NOT EXISTS idx_fitness_repair_record_handler_id ON fitness_repair_record(handler_id);
CREATE INDEX IF NOT EXISTS idx_fitness_repair_record_deleted ON fitness_repair_record(deleted);

-- 添加外键约束（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_fitness_repair_record_repair_id'
        AND table_name = 'fitness_repair_record'
    ) THEN
        ALTER TABLE fitness_repair_record
            ADD CONSTRAINT fk_fitness_repair_record_repair_id 
            FOREIGN KEY (repair_id) REFERENCES fitness_equipment_repair(id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_fitness_repair_record_handler_id'
        AND table_name = 'fitness_repair_record'
    ) THEN
        ALTER TABLE fitness_repair_record
            ADD CONSTRAINT fk_fitness_repair_record_handler_id 
            FOREIGN KEY (handler_id) REFERENCES sys_user(id) ON DELETE SET NULL;
    END IF;
END $$;

-- 创建更新时间触发器
DROP TRIGGER IF EXISTS update_fitness_repair_record_update_time ON fitness_repair_record;

CREATE TRIGGER update_fitness_repair_record_update_time 
    BEFORE UPDATE ON fitness_repair_record 
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();
