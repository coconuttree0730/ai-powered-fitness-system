-- ========================================================-- 添加课程详情字段（用于首页课程体系展示）-- ========================================================-- 难度等级：入门/初级/中级/高级/进阶
ALTER TABLE fitness_course ADD COLUMN IF NOT EXISTS difficulty_level VARCHAR(20) DEFAULT '初级';
COMMENT ON COLUMN fitness_course.difficulty_level IS '难度等级：入门/初级/中级/高级/进阶';

-- 课程时长（分钟）
ALTER TABLE fitness_course ADD COLUMN IF NOT EXISTS duration_minutes INT DEFAULT 60;
COMMENT ON COLUMN fitness_course.duration_minutes IS '课程时长（分钟）';

-- 最小卡路里消耗
ALTER TABLE fitness_course ADD COLUMN IF NOT EXISTS calories_min INT DEFAULT 200;
COMMENT ON COLUMN fitness_course.calories_min IS '最小卡路里消耗';

-- 最大卡路里消耗
ALTER TABLE fitness_course ADD COLUMN IF NOT EXISTS calories_max INT DEFAULT 400;
COMMENT ON COLUMN fitness_course.calories_max IS '最大卡路里消耗';

-- 更新现有课程数据（根据课程分类设置合理的默认值）
UPDATE fitness_course SET 
    difficulty_level = CASE 
        WHEN category = 'YOGA' THEN '初级'
        WHEN category = 'PILATES' THEN '初级'
        WHEN category = 'STRENGTH' THEN '中级'
        WHEN category = 'HIIT' THEN '高级'
        WHEN category = 'SPINNING' THEN '中级'
        WHEN category = 'CARDIO' THEN '中级'
        WHEN category = 'BOXING' THEN '高级'
        ELSE '初级'
    END,
    duration_minutes = CASE 
        WHEN category = 'YOGA' THEN 60
        WHEN category = 'PILATES' THEN 55
        WHEN category = 'STRENGTH' THEN 60
        WHEN category = 'HIIT' THEN 45
        WHEN category = 'SPINNING' THEN 50
        WHEN category = 'CARDIO' THEN 45
        WHEN category = 'BOXING' THEN 50
        ELSE 60
    END,
    calories_min = CASE 
        WHEN category = 'YOGA' THEN 200
        WHEN category = 'PILATES' THEN 250
        WHEN category = 'STRENGTH' THEN 300
        WHEN category = 'HIIT' THEN 500
        WHEN category = 'SPINNING' THEN 400
        WHEN category = 'CARDIO' THEN 350
        WHEN category = 'BOXING' THEN 450
        ELSE 200
    END,
    calories_max = CASE 
        WHEN category = 'YOGA' THEN 350
        WHEN category = 'PILATES' THEN 400
        WHEN category = 'STRENGTH' THEN 450
        WHEN category = 'HIIT' THEN 700
        WHEN category = 'SPINNING' THEN 600
        WHEN category = 'CARDIO' THEN 550
        WHEN category = 'BOXING' THEN 650
        ELSE 400
    END
WHERE difficulty_level IS NULL OR duration_minutes IS NULL OR calories_min IS NULL OR calories_max IS NULL;
