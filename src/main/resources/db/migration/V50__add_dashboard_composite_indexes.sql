-- Dashboard查询优化：复合索引
-- 覆盖 countTodayOrders, sumTodayRevenue, selectRevenueTrend
CREATE INDEX IF NOT EXISTS idx_product_order_created_at_status
    ON product_order(created_at, status)
    WHERE status IN ('PAID', 'SHIPPED', 'COMPLETED');

-- 覆盖 countActiveMembers (booking_time范围查询)
CREATE INDEX IF NOT EXISTS idx_fitness_booking_user_time
    ON fitness_booking(user_id, booking_time)
    WHERE deleted = false;

-- 覆盖 selectPeakHours (JOIN course, GROUP BY hour)
CREATE INDEX IF NOT EXISTS idx_fitness_booking_course_status
    ON fitness_booking(course_id, status)
    WHERE deleted = false;

-- 覆盖 selectUserGrowth
CREATE INDEX IF NOT EXISTS idx_sys_user_create_time
    ON sys_user(create_time)
    WHERE deleted = false;
