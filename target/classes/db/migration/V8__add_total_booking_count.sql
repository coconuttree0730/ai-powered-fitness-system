-- ========================================================-- 添加课程总预约人数字段（用于统计独立会员预约数量）-- ========================================================-- 总预约人数：统计所有预约过该课程的独立会员数量
ALTER TABLE fitness_course ADD COLUMN IF NOT EXISTS total_booking_count INT DEFAULT 0;
COMMENT ON COLUMN fitness_course.total_booking_count IS '总预约人数（统计所有预约过该课程的独立会员数量）';

-- 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_fitness_course_total_booking_count ON fitness_course(total_booking_count);

-- ========================================================-- 数据同步：将现有已预约人数同步到总预约人数字段-- ========================================================-- 注意：这里将booked_count作为初始值，实际业务中total_booking_count是累计值
UPDATE fitness_course SET total_booking_count = booked_count WHERE total_booking_count = 0;
