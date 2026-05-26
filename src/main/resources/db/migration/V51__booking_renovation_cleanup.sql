-- V51: 预约系统改造 - 数据清理
-- 1. 删除 fitness_course 表的死字段 booked_count（预约数已下沉到 session 级别）
ALTER TABLE fitness_course DROP COLUMN IF EXISTS booked_count;

-- 2. 将现有待确认预约更新为已确认（status 0 → 1）
UPDATE fitness_booking SET status = 1 WHERE status = 0 AND deleted = false;

-- 3. 将已过期且仍为待开始的 session 标记为已结束
UPDATE fitness_course_session SET status = 2, update_time = CURRENT_TIMESTAMP
WHERE session_date < CURRENT_DATE AND status = 0 AND deleted = false;

-- 4. 将已结束 session 关联的已确认预约标记为已完成
UPDATE fitness_booking b SET status = 3, update_time = CURRENT_TIMESTAMP
FROM fitness_course_session s
WHERE b.session_id = s.id AND s.status = 2 AND b.status = 1 AND b.deleted = false;
