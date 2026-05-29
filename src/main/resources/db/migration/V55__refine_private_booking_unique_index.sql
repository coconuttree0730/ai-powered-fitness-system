-- ========================================================
-- 智能健身系统数据库迁移脚本
-- 版本：V55
-- 说明：修改私教课程预约唯一索引，从按天去重改为按时段去重
--       允许同一会员同一教练同一天预约不同时段
-- ========================================================

-- 删除旧的按天唯一索引
DROP INDEX IF EXISTS uk_pcb_user_coach_date_active;

-- 创建新的按时段唯一索引：同一会员同一教练同一天同一时段不能重复预约
CREATE UNIQUE INDEX uk_pcb_user_coach_date_time_active
    ON fitness_private_coach_booking (user_id, coach_id, booking_date, start_time)
    WHERE status IN (0, 1) AND deleted = false;