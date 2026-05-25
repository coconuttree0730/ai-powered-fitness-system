-- ========================================================-- 为 fitness_equipment 表的 equipment_name 字段添加唯一约束
-- 说明：防止插入重复名称的器械
-- ========================================================

-- 先删除可能存在的重复数据（保留id最小的记录）
WITH duplicates AS (
    SELECT id, equipment_name,
           ROW_NUMBER() OVER (PARTITION BY equipment_name ORDER BY id) as rn
    FROM fitness_equipment
)
DELETE FROM fitness_equipment
WHERE id IN (
    SELECT id FROM duplicates WHERE rn > 1
);

-- 添加唯一约束
ALTER TABLE fitness_equipment
    ADD CONSTRAINT uk_equipment_name UNIQUE (equipment_name);

COMMENT ON CONSTRAINT uk_equipment_name ON fitness_equipment IS '器械名称唯一约束';
