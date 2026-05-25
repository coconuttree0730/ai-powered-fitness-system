-- 报修记录关联器械：增加equipment_id字段
ALTER TABLE fitness_equipment_repair ADD COLUMN IF NOT EXISTS equipment_id BIGINT;

-- 添加外键索引（加速按器械查询报修记录）
CREATE INDEX IF NOT EXISTS idx_repair_equipment_id ON fitness_equipment_repair(equipment_id);

-- 添加外键约束关联fitness_equipment表
ALTER TABLE fitness_equipment_repair ADD CONSTRAINT fk_repair_equipment 
    FOREIGN KEY (equipment_id) REFERENCES fitness_equipment(id) ON DELETE SET NULL;
