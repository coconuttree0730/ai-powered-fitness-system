-- ========================================================
-- 公开课程表结构优化：将具体日期时间改为周期性时间安排
-- 业务场景：公开课是固定的"一周中的某天由某个教练进行授课"
-- 版本：V44
-- ========================================================

-- 1. 新增 day_of_week 字段（星期几：1=周一, 2=周二, ..., 7=周日）
ALTER TABLE fitness_course ADD COLUMN IF NOT EXISTS day_of_week SMALLINT NOT NULL DEFAULT 1;

COMMENT ON COLUMN fitness_course.day_of_week IS '星期几：1-周一, 2-周二, 3-周三, 4-周四, 5-周五, 6-周六, 7-周日';

-- 2. 先从旧 TIMESTAMP 数据推算 day_of_week（必须在类型转换之前执行！）
UPDATE fitness_course SET day_of_week = EXTRACT(ISODOW FROM start_time)::SMALLINT
WHERE (day_of_week IS NULL OR day_of_week = 0)
  AND start_time IS NOT NULL;

-- 3. 将 start_time 从 TIMESTAMP 改为 TIME（仅保留时分秒）
ALTER TABLE fitness_course ALTER COLUMN start_time TYPE TIME USING (start_time::TIME);

COMMENT ON COLUMN fitness_course.start_time IS '开始时间（时分秒，如 14:00:00）';

-- 4. 将 end_time 从 TIMESTAMP 改为 TIME（仅保留时分秒）
ALTER TABLE fitness_course ALTER COLUMN end_time TYPE TIME USING (end_time::TIME);

COMMENT ON COLUMN fitness_course.end_time IS '结束时间（时分秒，如 15:30:00）';
