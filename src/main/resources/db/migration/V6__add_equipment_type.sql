-- ========================================================-- 器材类型管理功能升级-- ========================================================

-- 1. 创建器材类型表
CREATE TABLE IF NOT EXISTS fitness_equipment_type (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(50) NOT NULL UNIQUE,
    type_name VARCHAR(100) NOT NULL,
    icon VARCHAR(255),
    sort_order INT DEFAULT 0,
    description TEXT,
    deleted BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 为器材表添加类型字段
ALTER TABLE fitness_equipment 
ADD COLUMN IF NOT EXISTS type_code VARCHAR(50),
ADD COLUMN IF NOT EXISTS equipment_no VARCHAR(100);

-- 3. 插入默认器材类型数据
INSERT INTO fitness_equipment_type (type_code, type_name, icon, sort_order, description, deleted) VALUES
('CARDIO', '有氧器械', ' cardio-icon', 1, '跑步机、椭圆机、动感单车等有氧训练设备', FALSE),
('STRENGTH', '力量器械', 'strength-icon', 2, '史密斯机、龙门架、综合训练器等力量设备', FALSE),
('FREE_WEIGHT', '自由重量', 'dumbbell-icon', 3, '哑铃、杠铃、壶铃等自由重量器材', FALSE),
('FUNCTIONAL', '功能性训练', 'functional-icon', 4, '瑜伽垫、瑜伽球、弹力带等功能性训练器材', FALSE),
('ACCESSORY', '辅助配件', 'accessory-icon', 5, '其他健身辅助配件', FALSE)
ON CONFLICT (type_code) DO NOTHING;

-- 4. 为现有器材数据设置默认类型
UPDATE fitness_equipment 
SET type_code = CASE 
    WHEN equipment_name LIKE '%跑步机%' OR equipment_name LIKE '%椭圆机%' OR equipment_name LIKE '%单车%' THEN 'CARDIO'
    WHEN equipment_name LIKE '%史密斯%' OR equipment_name LIKE '%龙门架%' THEN 'STRENGTH'
    WHEN equipment_name LIKE '%哑铃%' OR equipment_name LIKE '%杠铃%' THEN 'FREE_WEIGHT'
    WHEN equipment_name LIKE '%瑜伽%' OR equipment_name LIKE '%球%' OR equipment_name LIKE '%垫%' THEN 'FUNCTIONAL'
    ELSE 'ACCESSORY'
END
WHERE type_code IS NULL;
