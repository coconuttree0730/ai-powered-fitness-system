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
