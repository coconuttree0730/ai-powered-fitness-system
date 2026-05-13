-- ========================================================
-- data.sql
-- ??????? Flyway ?????? PostgreSQL ???????
-- ??????????????????????? Flyway ????
-- ========================================================

-- ========================================================
-- 智能健身系统初始化数据脚本
-- 版本: V2.0.0
-- 说明: 插入基础角色、权限和默认数据
-- ========================================================

-- ========================================================
-- 1. 初始化角色数据
-- ========================================================

INSERT INTO sys_role (role_name, role_code, description, create_time, update_time) VALUES
('系统管理员', 'ADMIN', '拥有系统所有权限，负责系统管理和配置', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('教练', 'COACH', '负责课程管理和会员指导', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('会员', 'MEMBER', '普通会员用户，可预约课程和查看计划', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ========================================================
-- 2. 初始化权限数据
-- ========================================================

-- 系统管理权限
INSERT INTO sys_permission (permission_name, permission_code, type, parent_id, path, icon, sort_order, create_time, update_time) VALUES
('系统管理', 'system:manage', 1, 0, '/system', 'SettingOutlined', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('用户管理', 'user:manage', 1, 1, '/system/user', 'UserOutlined', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('用户查看', 'user:view', 3, 2, NULL, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('用户新增', 'user:create', 3, 2, NULL, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('用户编辑', 'user:update', 3, 2, NULL, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('用户删除', 'user:delete', 3, 2, NULL, NULL, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('角色管理', 'role:manage', 1, 1, '/system/role', 'TeamOutlined', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('角色查看', 'role:view', 3, 7, NULL, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('角色新增', 'role:create', 3, 7, NULL, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('角色编辑', 'role:update', 3, 7, NULL, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('角色删除', 'role:delete', 3, 7, NULL, NULL, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 课程管理权限
INSERT INTO sys_permission (permission_name, permission_code, type, parent_id, path, icon, sort_order, create_time, update_time) VALUES
('课程管理', 'course:manage', 1, 0, '/course', 'CalendarOutlined', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('课程查看', 'course:view', 3, 12, NULL, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('课程新增', 'course:create', 3, 12, NULL, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('课程编辑', 'course:update', 3, 12, NULL, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('课程删除', 'course:delete', 3, 12, NULL, NULL, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('预约管理', 'booking:manage', 1, 0, '/booking', 'BookOutlined', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('预约查看', 'booking:view', 3, 17, NULL, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('预约审核', 'booking:audit', 3, 17, NULL, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('我的预约', 'booking:my', 3, 17, NULL, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 器材管理权限
INSERT INTO sys_permission (permission_name, permission_code, type, parent_id, path, icon, sort_order, create_time, update_time) VALUES
('器材管理', 'equipment:manage', 1, 0, '/equipment', 'ToolOutlined', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('器材查看', 'equipment:view', 3, 21, NULL, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('器材新增', 'equipment:create', 3, 21, NULL, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('器材编辑', 'equipment:update', 3, 21, NULL, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('器材删除', 'equipment:delete', 3, 21, NULL, NULL, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('报修管理', 'repair:manage', 1, 0, '/repair', 'WarningOutlined', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('报修查看', 'repair:view', 3, 26, NULL, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('报修处理', 'repair:handle', 3, 26, NULL, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('提交报修', 'repair:create', 3, 26, NULL, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 健身计划权限
INSERT INTO sys_permission (permission_name, permission_code, type, parent_id, path, icon, sort_order, create_time, update_time) VALUES
('健身计划', 'plan:manage', 1, 0, '/plan', 'FileTextOutlined', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('计划查看', 'plan:view', 3, 30, NULL, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('计划新增', 'plan:create', 3, 30, NULL, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('计划编辑', 'plan:update', 3, 30, NULL, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('计划删除', 'plan:delete', 3, 30, NULL, NULL, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('AI计划生成', 'plan:ai-generate', 3, 30, NULL, NULL, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 数据统计权限
INSERT INTO sys_permission (permission_name, permission_code, type, parent_id, path, icon, sort_order, create_time, update_time) VALUES
('数据统计', 'statistics:manage', 1, 0, '/statistics', 'BarChartOutlined', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('运营概览', 'statistics:overview', 3, 36, NULL, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('课程统计', 'statistics:course', 3, 36, NULL, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('用户统计', 'statistics:user', 3, 36, NULL, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ========================================================
-- 3. 初始化角色权限关联
-- ========================================================

-- ADMIN角色拥有所有权限
INSERT INTO sys_role_permission (role_id, permission_id, create_time)
SELECT 1, id, CURRENT_TIMESTAMP FROM sys_permission WHERE deleted = FALSE;

-- COACH角色权限（课程管理、预约管理、器材查看、报修管理、健身计划、数据统计）
INSERT INTO sys_role_permission (role_id, permission_id, create_time) VALUES
-- 用户查看
(2, 3, CURRENT_TIMESTAMP),
-- 课程管理权限
(2, 12, CURRENT_TIMESTAMP),
(2, 13, CURRENT_TIMESTAMP),
(2, 14, CURRENT_TIMESTAMP),
(2, 15, CURRENT_TIMESTAMP),
-- 预约管理权限
(2, 17, CURRENT_TIMESTAMP),
(2, 18, CURRENT_TIMESTAMP),
(2, 19, CURRENT_TIMESTAMP),
-- 器材查看
(2, 21, CURRENT_TIMESTAMP),
(2, 22, CURRENT_TIMESTAMP),
-- 报修管理
(2, 26, CURRENT_TIMESTAMP),
(2, 27, CURRENT_TIMESTAMP),
(2, 28, CURRENT_TIMESTAMP),
-- 健身计划
(2, 30, CURRENT_TIMESTAMP),
(2, 31, CURRENT_TIMESTAMP),
(2, 32, CURRENT_TIMESTAMP),
(2, 33, CURRENT_TIMESTAMP),
(2, 35, CURRENT_TIMESTAMP),
-- 数据统计
(2, 36, CURRENT_TIMESTAMP),
(2, 37, CURRENT_TIMESTAMP),
(2, 38, CURRENT_TIMESTAMP),
(2, 39, CURRENT_TIMESTAMP);

-- MEMBER角色权限（课程查看、我的预约、提交报修、健身计划）
INSERT INTO sys_role_permission (role_id, permission_id, create_time) VALUES
-- 课程查看
(3, 12, CURRENT_TIMESTAMP),
(3, 13, CURRENT_TIMESTAMP),
-- 我的预约
(3, 20, CURRENT_TIMESTAMP),
-- 提交报修
(3, 29, CURRENT_TIMESTAMP),
-- 健身计划
(3, 30, CURRENT_TIMESTAMP),
(3, 31, CURRENT_TIMESTAMP),
(3, 32, CURRENT_TIMESTAMP),
(3, 33, CURRENT_TIMESTAMP),
(3, 35, CURRENT_TIMESTAMP);

-- ========================================================
-- 4. 初始化默认管理员账号
-- 密码: admin123 (BCrypt加密)
-- ========================================================

INSERT INTO sys_user (username, password, phone, email, status, deleted, create_time, update_time) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '13800138000', 'admin@fitness.com', 1, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 关联管理员角色
INSERT INTO sys_user_role (user_id, role_id, create_time) VALUES
(1, 1, CURRENT_TIMESTAMP);

-- ========================================================
-- 5. 初始化示例器材数据
-- ========================================================

INSERT INTO fitness_equipment (equipment_name, location, status, description, purchase_date, deleted, create_time, update_time) VALUES
('跑步机 Pro', '有氧区-A01', 1, '商用级跑步机，支持多种训练模式', '2024-01-15', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('椭圆机 Elite', '有氧区-A02', 1, '低冲击有氧训练设备', '2024-01-15', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('动感单车', '有氧区-A03', 1, '团体课程专用动感单车', '2024-02-01', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('史密斯机', '力量区-B01', 1, '多功能力量训练设备', '2024-01-20', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('哑铃套装', '力量区-B02', 1, '2.5kg-50kg全套哑铃', '2024-01-20', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('龙门架', '力量区-B03', 1, '综合训练龙门架', '2024-02-10', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('瑜伽垫', '瑜伽区-C01', 1, '专业防滑瑜伽垫', '2024-03-01', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('瑜伽球', '瑜伽区-C02', 1, '平衡训练瑜伽球', '2024-03-01', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ========================================================
-- 6. 初始化课程分类数据（用于后续课程创建）
-- 注意：课程数据需要教练用户创建，此处仅预留
-- ========================================================

-- 课程分类说明（供前端使用）：
-- - YOGA: 瑜伽
-- - PILATES: 普拉提
-- - SPINNING: 动感单车
-- - AEROBICS: 有氧操
-- - STRENGTH: 力量训练
-- - HIIT: 高强度间歇训练
-- - DANCE: 舞蹈健身
-- - BOXING: 拳击健身

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

-- ??????????????????
INSERT INTO fitness_course (
    course_name, description, coach_id, category, day_of_week, start_time, end_time,
    capacity, booked_count, total_booking_count, status, image_url,
    difficulty_level, duration_minutes, calories_min, calories_max,
    deleted, create_time, update_time
) VALUES
('????', '???????????????????????', (SELECT id FROM sys_user WHERE username = 'coach1'), 'YOGA', 1, '09:00:00', '10:00:00', 20, 0, 0, 1, NULL, '??', 60, 200, 350, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('????', '??????????????????????', (SELECT id FROM sys_user WHERE username = 'coach2'), 'SPINNING', 1, '14:00:00', '15:00:00', 15, 0, 0, 1, NULL, '??', 50, 400, 600, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('??????', '??????????????????', (SELECT id FROM sys_user WHERE username = 'coach1'), 'STRENGTH', 2, '10:00:00', '11:00:00', 10, 0, 0, 1, NULL, '??', 60, 300, 450, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HIIT??', '???????????????', (SELECT id FROM sys_user WHERE username = 'coach2'), 'HIIT', 2, '16:00:00', '16:30:00', 25, 0, 0, 1, NULL, '??', 45, 500, 700, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('?????', '??????????????', (SELECT id FROM sys_user WHERE username = 'coach1'), 'PILATES', 3, '11:00:00', '12:00:00', 18, 0, 0, 1, NULL, '??', 55, 250, 400, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ???????????
UPDATE sys_user SET points = 1000 WHERE points IS NULL;

-- ????????????????????????
UPDATE sys_user SET nickname = username WHERE nickname IS NULL;

-- ========================================================-- 智能健身系统数据库迁移脚本-- 版本：V11-- 说明：为已有的教练用户初始化 coach_detail 记录-- ========================================================

-- 为所有具有 COACH 角色但还没有 coach_detail 记录的用户创建详情记录
INSERT INTO coach_detail (
    user_id,
    personal_image_url,
    tags,
    work_years,
    specialties,
    teaching_style,
    education,
    training,
    languages,
    bio,
    experience,
    honors,
    emergency_contact,
    certifications,
    availability,
    student_count,
    rating,
    deleted,
    create_time,
    update_time
)
SELECT
    su.id,
    NULL,                           -- personal_image_url
    '[]'::jsonb,                    -- tags
    0,                              -- work_years
    '[]'::jsonb,                    -- specialties
    NULL,                           -- teaching_style
    NULL,                           -- education
    NULL,                           -- training
    '[]'::jsonb,                    -- languages
    NULL,                           -- bio
    NULL,                           -- experience
    '[]'::jsonb,                    -- honors
    '{}'::jsonb,                    -- emergency_contact
    '[]'::jsonb,                    -- certifications
    '{}'::jsonb,                    -- availability
    0,                              -- student_count
    '99%',                          -- rating
    FALSE,                          -- deleted
    CURRENT_TIMESTAMP,              -- create_time
    CURRENT_TIMESTAMP               -- update_time
FROM sys_user su
INNER JOIN sys_user_role sur ON su.id = sur.user_id
INNER JOIN sys_role sr ON sur.role_id = sr.id AND sr.role_code = 'COACH'
WHERE su.deleted = false
  AND NOT EXISTS (
    SELECT 1 FROM coach_detail cd WHERE cd.user_id = su.id
  );

-- 记录迁移信息
DO $$
DECLARE
    inserted_count INTEGER;
BEGIN
    GET DIAGNOSTICS inserted_count = ROW_COUNT;
    RAISE NOTICE '已为 % 名教练初始化 coach_detail 记录', inserted_count;
END $$;

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

-- ========================================================
-- 公告表初始化数据脚本
-- 版本：V17
-- 说明：添加公告模拟数据
-- ========================================================

-- 插入公告模拟数据
INSERT INTO sys_announcement (title, content, type, status, view_count, publish_time, deleted, create_time, update_time) VALUES
-- 重要提醒类型
('春节营业时间调整通知', '尊敬的会员：春节期间（2025年1月28日-2月4日）营业时间调整为10:00-18:00，2月5日起恢复正常营业。祝您新春快乐，身体健康！', 'IMPORTANT', 1, 1256, '2025-01-15 10:00:00', false, '2025-01-15 09:00:00', '2025-01-15 10:00:00'),

-- 系统公告类型
('新器械上线通知', '本月新增多台高端有氧器械，包括Technogym最新款跑步机、椭圆机等，现已全面投入使用，欢迎会员体验！', 'SYSTEM', 1, 892, '2025-01-10 14:30:00', false, '2025-01-10 14:00:00', '2025-01-10 14:30:00'),

('系统维护通知', '为提供更优质的服务，我们将于本周日凌晨2:00-6:00进行系统维护，期间APP部分功能可能无法使用，请提前安排您的训练计划。', 'SYSTEM', 0, 0, NULL, false, '2025-03-28 16:00:00', '2025-03-28 16:00:00'),

-- 活动通知类型
('会员积分兑换活动', '积分兑换商品8折优惠活动开始啦！即日起至3月31日，会员可使用积分兑换运动补剂、装备等商品，享受8折优惠，数量有限，先到先得！', 'ACTIVITY', 1, 567, '2025-03-20 09:00:00', false, '2025-03-20 08:00:00', '2025-03-20 09:00:00'),

('春季减脂挑战赛', '春暖花开，正是减脂好时节！参加我们的21天减脂挑战赛，专业教练全程指导，更有丰厚奖品等你来拿！报名截止4月5日。', 'ACTIVITY', 1, 234, '2025-03-25 10:00:00', false, '2025-03-25 09:00:00', '2025-03-25 10:00:00'),

('瑜伽课程升级', '我们的瑜伽课程全面升级，新增空中瑜伽、高温瑜伽等课程，由资深瑜伽导师授课，现已开放预约，欢迎体验！', 'ACTIVITY', 0, 0, NULL, false, '2025-03-28 15:00:00', '2025-03-28 15:00:00'),

-- 更多重要提醒
('会员有效期顺延通知', '因门店设备维护升级（3月19日-3月23日暂停营业），所有会员有效期将自动顺延5天，给您带来不便敬请谅解。', 'IMPORTANT', 1, 1890, '2025-03-18 08:00:00', false, '2025-03-18 07:00:00', '2025-03-18 08:00:00'),

('私教课程优惠活动', '限时优惠！购买私教课程满20节赠送2节，满30节赠送5节，活动仅限本月，详情请咨询前台或您的专属教练。', 'ACTIVITY', 1, 445, '2025-03-15 14:00:00', false, '2025-03-15 13:00:00', '2025-03-15 14:00:00');

-- ==========================================
-- 知识库分类初始化脚本
-- 版本: V35
-- 说明: 创建6个知识库分类，用于RAG智能客服系统
-- 作者: AI Assistant
-- 日期: 2026-04-12
-- ==========================================

-- 插入知识库分类数据（使用INSERT IGNORE语义，避免重复插入）
-- 注意：使用DO NOTHING确保幂等性，多次执行不会报错

INSERT INTO knowledge_category (name, code, description, sort_order, deleted, create_time, update_time)
VALUES 
    ('场馆基础信息', 'VENUE_INFO', '包含场馆介绍、位置营业时间、联系方式、交通指南、场馆设施等基础信息', 1, false, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO knowledge_category (name, code, description, sort_order, deleted, create_time, update_time)
VALUES 
    ('会员卡与收费政策', 'MEMBERSHIP_POLICY', '包含会员卡类型价格、开卡停卡退卡规则、老带新政策、节日优惠、企业团办等收费政策', 2, false, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO knowledge_category (name, code, description, sort_order, deleted, create_time, update_time)
VALUES 
    ('课程与预约服务', 'COURSE_BOOKING', '包含团课时间表、私教服务内容价格、预约取消爽约规则、课程推荐逻辑', 3, false, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO knowledge_category (name, code, description, sort_order, deleted, create_time, update_time)
VALUES 
    ('场馆使用规范', 'VENUE_RULES', '包含入场要求、器械使用规范、储物柜淋浴间规则、禁止事项、访客规则、安全应急流程', 4, false, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO knowledge_category (name, code, description, sort_order, deleted, create_time, update_time)
VALUES 
    ('常见问题FAQ', 'FAQ', '包含账号登录问题、卡与权益问题、课程预约问题、其他常见问题的解答', 5, false, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO knowledge_category (name, code, description, sort_order, deleted, create_time, update_time)
VALUES 
    ('教练团队介绍', 'COACH_TEAM', '包含教练资质、擅长方向、可预约时间、教练简介、预约方式', 6, false, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- ==========================================
-- 验证插入结果
-- ==========================================
-- 查询当前分类数据（调试用，生产环境可注释掉）
-- SELECT id, name, code, description, sort_order 
-- FROM knowledge_category 
-- WHERE deleted = false 
-- ORDER BY sort_order;

-- ==========================================
-- 使用说明：
-- 1. 此脚本使用 ON CONFLICT (code) DO NOTHING 确保幂等性
-- 2. 如果分类已存在（code重复），则跳过插入
-- 3. 重启应用后 Flyway 会自动执行此脚本
-- 4. 分类ID由数据库自动生成（通常从1开始递增）
-- ==========================================

-- ???? type_id ?? type_code
UPDATE membership_card mc
SET type_code = mct.code
FROM membership_card_type mct
WHERE mc.type_id = mct.id;

-- ???????????????
UPDATE coach_detail
SET real_name = COALESCE(
    (SELECT nickname FROM sys_user WHERE sys_user.id = coach_detail.user_id),
    (SELECT username FROM sys_user WHERE sys_user.id = coach_detail.user_id)
)
WHERE real_name IS NULL;
