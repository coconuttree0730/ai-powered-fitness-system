-- ========================================================-- 智能健身系统数据库迁移脚本-- 版本：V24-- 说明：更新报修系统，支持多张图片和处理记录-- ========================================================

-- 修改器材报修表：添加处理备注字段，修改图片字段支持多张图片
ALTER TABLE fitness_equipment_repair
    ADD COLUMN IF NOT EXISTS handle_remark VARCHAR(500),
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE NOT NULL;

-- 将原有的 image_url 字段改为 image_urls（如果存在）
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'fitness_equipment_repair' 
               AND column_name = 'image_url') THEN
        ALTER TABLE fitness_equipment_repair RENAME COLUMN image_url TO image_urls;
    ELSE
        -- 如果不存在 image_url 字段，则添加 image_urls 字段
        ALTER TABLE fitness_equipment_repair ADD COLUMN IF NOT EXISTS image_urls VARCHAR(2000);
    END IF;
END $$;

-- 修改 image_urls 字段长度以支持多张图片URL
ALTER TABLE fitness_equipment_repair ALTER COLUMN image_urls TYPE VARCHAR(2000);

-- 移除 equipment_id 和 issue_type 字段（如果存在）
ALTER TABLE fitness_equipment_repair DROP COLUMN IF EXISTS equipment_id;
ALTER TABLE fitness_equipment_repair DROP COLUMN IF EXISTS issue_type;

-- 创建报修处理记录表
CREATE TABLE IF NOT EXISTS fitness_repair_record (
    id BIGSERIAL PRIMARY KEY,
    repair_id BIGINT NOT NULL,
    handler_id BIGINT,
    record_type SMALLINT DEFAULT 1 NOT NULL,
    before_status SMALLINT,
    after_status SMALLINT,
    content TEXT,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE fitness_repair_record IS '报修处理记录表';
COMMENT ON COLUMN fitness_repair_record.repair_id IS '报修ID';
COMMENT ON COLUMN fitness_repair_record.handler_id IS '处理人ID（管理员）';
COMMENT ON COLUMN fitness_repair_record.record_type IS '处理类型：1-提交报修, 2-状态变更, 3-处理备注, 4-取消报修';
COMMENT ON COLUMN fitness_repair_record.before_status IS '处理前状态';
COMMENT ON COLUMN fitness_repair_record.after_status IS '处理后状态';
COMMENT ON COLUMN fitness_repair_record.content IS '处理内容/备注';
COMMENT ON COLUMN fitness_repair_record.deleted IS '软删除标识';

-- 更新字段注释
COMMENT ON COLUMN fitness_equipment_repair.image_urls IS '报修图片URL（多张图片以逗号分隔）';
COMMENT ON COLUMN fitness_equipment_repair.handle_remark IS '处理备注';
COMMENT ON COLUMN fitness_equipment_repair.deleted IS '软删除标识';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_fitness_repair_record_repair_id ON fitness_repair_record(repair_id);
CREATE INDEX IF NOT EXISTS idx_fitness_repair_record_handler_id ON fitness_repair_record(handler_id);
CREATE INDEX IF NOT EXISTS idx_fitness_repair_record_deleted ON fitness_repair_record(deleted);

-- 添加外键约束
ALTER TABLE fitness_repair_record
    ADD CONSTRAINT fk_fitness_repair_record_repair_id 
    FOREIGN KEY (repair_id) REFERENCES fitness_equipment_repair(id) ON DELETE CASCADE;

ALTER TABLE fitness_repair_record
    ADD CONSTRAINT fk_fitness_repair_record_handler_id 
    FOREIGN KEY (handler_id) REFERENCES sys_user(id) ON DELETE SET NULL;

-- 创建更新时间触发器
DROP TRIGGER IF EXISTS update_fitness_repair_record_update_time ON fitness_repair_record;

-- 为报修记录表添加更新时间字段（如果需要）
ALTER TABLE fitness_repair_record 
    ADD COLUMN IF NOT EXISTS update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE;

-- 创建触发器函数（如果不存在）
CREATE OR REPLACE FUNCTION update_update_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plgsql';

-- 创建更新时间触发器
CREATE TRIGGER update_fitness_repair_record_update_time 
    BEFORE UPDATE ON fitness_repair_record 
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

-- 确保器材报修表有更新时间触发器
DROP TRIGGER IF EXISTS update_fitness_equipment_repair_update_time ON fitness_equipment_repair;
CREATE TRIGGER update_fitness_equipment_repair_update_time 
    BEFORE UPDATE ON fitness_equipment_repair 
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();
