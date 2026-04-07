-- ========================================================
-- 更新健身计划表结构以支持 AI 生成的计划
-- ========================================================

-- 1. 为 fitness_plan 表添加 level 字段
ALTER TABLE fitness_plan
    ADD COLUMN IF NOT EXISTS level VARCHAR(50);

COMMENT ON COLUMN fitness_plan.level IS '难度等级: BEGINNER-初级, INTERMEDIATE-中级, ADVANCED-高级';

-- 2. 修改 fitness_plan_detail 表结构以支持新的 AI 计划格式
-- 添加新字段
ALTER TABLE fitness_plan_detail
    ADD COLUMN IF NOT EXISTS day_index SMALLINT,
    ADD COLUMN IF NOT EXISTS day_name VARCHAR(20),
    ADD COLUMN IF NOT EXISTS focus VARCHAR(100),
    ADD COLUMN IF NOT EXISTS course_id BIGINT,
    ADD COLUMN IF NOT EXISTS course_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS equipment_json TEXT,
    ADD COLUMN IF NOT EXISTS exercises_json TEXT;

COMMENT ON COLUMN fitness_plan_detail.day_index IS '天数索引(1-7)';
COMMENT ON COLUMN fitness_plan_detail.day_name IS '星期名称(周一、周二等)';
COMMENT ON COLUMN fitness_plan_detail.focus IS '训练重点(胸部、背部等)';
COMMENT ON COLUMN fitness_plan_detail.course_id IS '课程ID';
COMMENT ON COLUMN fitness_plan_detail.course_name IS '课程名称';
COMMENT ON COLUMN fitness_plan_detail.equipment_json IS '器械信息(JSON格式)';
COMMENT ON COLUMN fitness_plan_detail.exercises_json IS '训练动作(JSON格式)';

-- 3. 将旧数据迁移到新字段（如果存在）
UPDATE fitness_plan_detail
SET day_index = day_of_week,
    day_name = CASE day_of_week
        WHEN 1 THEN '周一'
        WHEN 2 THEN '周二'
        WHEN 3 THEN '周三'
        WHEN 4 THEN '周四'
        WHEN 5 THEN '周五'
        WHEN 6 THEN '周六'
        WHEN 7 THEN '周日'
    END,
    focus = '综合训练'
WHERE day_index IS NULL;

-- 4. 删除旧索引
DROP INDEX IF EXISTS idx_fitness_plan_detail_day_of_week;

-- 5. 创建新索引
CREATE INDEX idx_fitness_plan_detail_day_index ON fitness_plan_detail(day_index);

-- 6. 将旧字段标记为废弃（可选，保留数据兼容性）
-- 注意：day_of_week, exercise_name, sets, reps 字段保留以确保向后兼容性
-- 新代码应该使用 day_index, day_name, focus, course_id, course_name, equipment_json, exercises_json 字段
