-- ========================================================-- 添加健身器材模拟数据-- 说明：插入20条各类健身器材数据，使用 ON CONFLICT 避免重复插入-- ========================================================

-- 1. 先为 equipment_no 添加唯一约束（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'uk_fitness_equipment_no' 
        AND conrelid = 'fitness_equipment'::regclass
    ) THEN
        ALTER TABLE fitness_equipment 
        ADD CONSTRAINT uk_fitness_equipment_no UNIQUE (equipment_no);
    END IF;
END $$;

-- 2. 插入器材数据，冲突时跳过
INSERT INTO fitness_equipment (equipment_name, location, status, description, purchase_date, deleted, type_code, equipment_no, create_time, update_time) VALUES
-- 有氧器械 (CARDIO)
('商用跑步机 X1', '有氧区-A01', 1, '高端商用跑步机，配备15.6寸触控屏，支持虚拟场景跑步', '2024-01-15', false, 'CARDIO', 'CARDIO-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('椭圆机 E500', '有氧区-A02', 1, '磁控椭圆机，20档阻力调节，心率监测功能', '2024-01-20', false, 'CARDIO', 'CARDIO-002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('动感单车 S200', '有氧区-A03', 1, '商用动感单车，皮带传动静音设计，可调节座椅和把手', '2024-02-01', false, 'CARDIO', 'CARDIO-003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('划船机 R300', '有氧区-A04', 1, '风阻划船机，模拟真实划船体验，全身有氧训练', '2024-02-10', false, 'CARDIO', 'CARDIO-004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('登山机 C100', '有氧区-A05', 1, '商用登山机，模拟爬楼梯运动，高效燃脂', '2024-02-15', false, 'CARDIO', 'CARDIO-005', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 力量器械 (STRENGTH)
('史密斯训练架', '力量区-B01', 1, '多功能史密斯机，安全锁止设计，适合深蹲、卧推等训练', '2024-01-10', false, 'STRENGTH', 'STRENGTH-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('龙门架综合训练器', '力量区-B02', 1, '双滑轮龙门架，可进行多种拉力训练，配重80kg', '2024-01-12', false, 'STRENGTH', 'STRENGTH-002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('腿举训练器', '力量区-B03', 1, '45度倒蹬机，专业腿部力量训练设备', '2024-01-25', false, 'STRENGTH', 'STRENGTH-003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('蝴蝶机', '力量区-B04', 1, '胸肌训练专用，双轴设计，动作轨迹更自然', '2024-02-05', false, 'STRENGTH', 'STRENGTH-004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('坐姿划船器', '力量区-B05', 1, '背部训练器械，可调节座椅和胸垫', '2024-02-08', false, 'STRENGTH', 'STRENGTH-005', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 自由重量 (FREE_WEIGHT)
('可调节哑铃套装', '自由重量区-C01', 1, '2.5-25kg可调节哑铃，快速调节重量设计', '2024-01-18', false, 'FREE_WEIGHT', 'FREE-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('奥林匹克杠铃杆', '自由重量区-C02', 1, '2.2米奥林匹克标准杠铃杆，承重500kg', '2024-01-22', false, 'FREE_WEIGHT', 'FREE-002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('橡胶包胶杠铃片套装', '自由重量区-C03', 1, '2.5kg-25kg全套橡胶包胶杠铃片，保护地板', '2024-01-28', false, 'FREE_WEIGHT', 'FREE-003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('壶铃套装', '自由重量区-C04', 1, '4-24kg壶铃套装，铸铁材质，彩色标识', '2024-02-12', false, 'FREE_WEIGHT', 'FREE-004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('平板卧推凳', '自由重量区-C05', 1, '商用级卧推凳，承重400kg，防滑皮革', '2024-02-18', false, 'FREE_WEIGHT', 'FREE-005', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 功能性训练 (FUNCTIONAL)
('瑜伽球 65cm', '功能训练区-D01', 1, '加厚防爆瑜伽球，适合核心训练和平衡训练', '2024-01-30', false, 'FUNCTIONAL', 'FUNC-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('弹力带套装', '功能训练区-D02', 1, '5种阻力级别弹力带，天然乳胶材质', '2024-02-03', false, 'FUNCTIONAL', 'FUNC-002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('泡沫轴', '功能训练区-D03', 1, '高密度EVA泡沫轴，肌肉放松和筋膜松解', '2024-02-14', false, 'FUNCTIONAL', 'FUNC-003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('战绳 15米', '功能训练区-D04', 1, '尼龙战绳，直径38mm，爆发力训练', '2024-02-20', false, 'FUNCTIONAL', 'FUNC-004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TRX悬挂训练带', '功能训练区-D05', 1, '专业悬挂训练系统，全身自重训练', '2024-02-25', false, 'FUNCTIONAL', 'FUNC-005', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (equipment_no) DO NOTHING;
