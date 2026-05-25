-- ========================================================
-- 初始化课程测试数据
-- ========================================================

-- 先插入教练用户（如果不存在）
INSERT INTO sys_user (username, password, phone, email, status, deleted, create_time, update_time)
SELECT 'coach1', '$2a$10$3TxT4gAogxACOvUI80FxdOYNGKXD5U5JhFRKzF1FMgOiWYQI/kMcG', '13900139001', 'coach1@fitness.com', 1, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'coach1');

INSERT INTO sys_user (username, password, phone, email, status, deleted, create_time, update_time)
SELECT 'coach2', '$2a$10$3TxT4gAogxACOvUI80FxdOYNGKXD5U5JhFRKzF1FMgOiWYQI/kMcG', '13900139002', 'coach2@fitness.com', 1, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'coach2');

-- 给教练用户分配 COACH 角色
INSERT INTO sys_user_role (user_id, role_id, create_time)
SELECT u.id, r.id, CURRENT_TIMESTAMP
FROM sys_user u, sys_role r
WHERE u.username = 'coach1' AND r.role_code = 'COACH'
AND NOT EXISTS (
    SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

INSERT INTO sys_user_role (user_id, role_id, create_time)
SELECT u.id, r.id, CURRENT_TIMESTAMP
FROM sys_user u, sys_role r
WHERE u.username = 'coach2' AND r.role_code = 'COACH'
AND NOT EXISTS (
    SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

-- 插入课程数据
INSERT INTO fitness_course (course_name, description, coach_id, category, start_time, end_time, capacity, booked_count, status, image_url, deleted, create_time, update_time) VALUES
('晨间瑜伽', '适合初学者的晨间瑜伽课程，帮助您开启美好的一天', (SELECT id FROM sys_user WHERE username = 'coach1'), 'YOGA', CURRENT_TIMESTAMP + INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '1 day 1 hour', 20, 0, 1, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('动感单车', '高强度的有氧骑行训练，燃烧脂肪，提升心肺功能', (SELECT id FROM sys_user WHERE username = 'coach2'), 'SPINNING', CURRENT_TIMESTAMP + INTERVAL '1 day 2 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 3 hours', 15, 0, 1, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('力量训练基础', '学习正确的力量训练姿势，打造强健体魄', (SELECT id FROM sys_user WHERE username = 'coach1'), 'STRENGTH', CURRENT_TIMESTAMP + INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '2 days 1 hour', 10, 0, 1, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HIIT燃脂', '高强度间歇训练，快速燃烧卡路里', (SELECT id FROM sys_user WHERE username = 'coach2'), 'HIIT', CURRENT_TIMESTAMP + INTERVAL '2 days 2 hours', CURRENT_TIMESTAMP + INTERVAL '2 days 2 hours 30 minutes', 25, 0, 1, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('普拉提塑形', '普拉提核心训练，塑造完美体态', (SELECT id FROM sys_user WHERE username = 'coach1'), 'PILATES', CURRENT_TIMESTAMP + INTERVAL '3 days', CURRENT_TIMESTAMP + INTERVAL '3 days 1 hour', 18, 0, 1, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
