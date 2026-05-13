-- ========================================================
-- init.sql
-- ??????? Flyway ?????? PostgreSQL ???????
-- ??????????????????????? Flyway ????
-- ========================================================

-- ========================================================
-- 智能健身系统数据库初始化脚本
-- 版本：V1.0.0
-- 说明：创建核心表结构
-- ========================================================

-- 启用 UUID 扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================================================
-- 1. 系统管理模块
-- ========================================================

-- 用户表
CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(500),
    status SMALLINT DEFAULT 1 NOT NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE sys_user IS '用户表';

COMMENT ON COLUMN sys_user.status IS '状态：0-禁用，1-启用';

COMMENT ON COLUMN sys_user.deleted IS '软删除标识';

-- 角色表
CREATE TABLE sys_role (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE sys_role IS '角色表';

COMMENT ON COLUMN sys_role.role_name IS '角色名称';

COMMENT ON COLUMN sys_role.role_code IS '角色编码';

COMMENT ON COLUMN sys_role.description IS '角色描述';

COMMENT ON COLUMN sys_role.deleted IS '软删除标识';

-- 用户角色关联表
CREATE TABLE sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE(user_id, role_id)
);

COMMENT ON TABLE sys_user_role IS '用户角色关联表';

-- 权限表
CREATE TABLE sys_permission (
    id BIGSERIAL PRIMARY KEY,
    permission_name VARCHAR(50) NOT NULL,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    type SMALLINT DEFAULT 1 NOT NULL,
    parent_id BIGINT DEFAULT 0,
    path VARCHAR(200),
    icon VARCHAR(100),
    sort_order INT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE sys_permission IS '权限表';

COMMENT ON COLUMN sys_permission.permission_name IS '权限名称';

COMMENT ON COLUMN sys_permission.permission_code IS '权限编码';

COMMENT ON COLUMN sys_permission.type IS '类型：1-菜单，2-按钮，3-接口';

COMMENT ON COLUMN sys_permission.parent_id IS '父权限 ID';

COMMENT ON COLUMN sys_permission.path IS '路由路径';

COMMENT ON COLUMN sys_permission.icon IS '图标';

COMMENT ON COLUMN sys_permission.sort_order IS '排序';

COMMENT ON COLUMN sys_permission.deleted IS '软删除标识';

-- 角色权限关联表
CREATE TABLE sys_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE(role_id, permission_id)
);

COMMENT ON TABLE sys_role_permission IS '角色权限关联表';

-- 文件记录表
CREATE TABLE sys_file (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE sys_file IS '文件记录表';

COMMENT ON COLUMN sys_file.file_name IS '存储文件名';

COMMENT ON COLUMN sys_file.original_name IS '原始文件名';

COMMENT ON COLUMN sys_file.file_url IS '文件访问 URL';

COMMENT ON COLUMN sys_file.file_type IS '文件类型';

COMMENT ON COLUMN sys_file.file_size IS '文件大小 (字节)';

COMMENT ON COLUMN sys_file.deleted IS '软删除标识';

-- ========================================================
-- 2. 健身课程模块
-- ========================================================

-- 课程表
CREATE TABLE fitness_course (
    id BIGSERIAL PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    description TEXT,
    coach_id BIGINT NOT NULL,
    category VARCHAR(50),
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    capacity INT DEFAULT 20 NOT NULL,
    booked_count INT DEFAULT 0 NOT NULL,
    status SMALLINT DEFAULT 1 NOT NULL,
    image_url VARCHAR(500),
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE fitness_course IS '课程表';

COMMENT ON COLUMN fitness_course.course_name IS '课程名称';

COMMENT ON COLUMN fitness_course.description IS '课程描述';

COMMENT ON COLUMN fitness_course.coach_id IS '教练 ID(关联 sys_user)';

COMMENT ON COLUMN fitness_course.category IS '课程分类';

COMMENT ON COLUMN fitness_course.start_time IS '开始时间';

COMMENT ON COLUMN fitness_course.end_time IS '结束时间';

COMMENT ON COLUMN fitness_course.capacity IS '容量 (最大人数)';

COMMENT ON COLUMN fitness_course.booked_count IS '已预约人数';

COMMENT ON COLUMN fitness_course.status IS '状态：0-已取消，1-可预约，2-已满员，3-已结束';

COMMENT ON COLUMN fitness_course.image_url IS '课程图片 URL';

COMMENT ON COLUMN fitness_course.deleted IS '软删除标识';

-- 预约表
CREATE TABLE fitness_booking (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    booking_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,
    status SMALLINT DEFAULT 0 NOT NULL,
    cancel_reason VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE fitness_booking IS '预约表';

COMMENT ON COLUMN fitness_booking.user_id IS '用户 ID';

COMMENT ON COLUMN fitness_booking.course_id IS '课程 ID';

COMMENT ON COLUMN fitness_booking.booking_time IS '预约时间';

COMMENT ON COLUMN fitness_booking.status IS '状态：0-待确认，1-已确认，2-已取消，3-已完成';

COMMENT ON COLUMN fitness_booking.cancel_reason IS '取消原因';

COMMENT ON COLUMN fitness_booking.deleted IS '软删除标识';

-- ========================================================
-- 3. 器材管理模块
-- ========================================================

-- 器材表
CREATE TABLE fitness_equipment (
    id BIGSERIAL PRIMARY KEY,
    equipment_name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    status SMALLINT DEFAULT 1 NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    purchase_date DATE,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE fitness_equipment IS '器材表';

COMMENT ON COLUMN fitness_equipment.equipment_name IS '器材名称';

COMMENT ON COLUMN fitness_equipment.location IS '存放位置';

COMMENT ON COLUMN fitness_equipment.status IS '状态：0-维修中，1-正常，2-报废';

COMMENT ON COLUMN fitness_equipment.description IS '器材描述';

COMMENT ON COLUMN fitness_equipment.image_url IS '器材图片 URL';

COMMENT ON COLUMN fitness_equipment.purchase_date IS '购买日期';

COMMENT ON COLUMN fitness_equipment.deleted IS '软删除标识';

-- 器材报修表
CREATE TABLE fitness_equipment_repair (
    id BIGSERIAL PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    user_id BIGINT,
    description TEXT NOT NULL,
    image_url VARCHAR(500),
    status SMALLINT DEFAULT 0 NOT NULL,
    handle_time TIMESTAMP WITH TIME ZONE,
    handle_remark VARCHAR(500),
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE fitness_equipment_repair IS '器材报修表';

COMMENT ON COLUMN fitness_equipment_repair.equipment_id IS '器材 ID';

COMMENT ON COLUMN fitness_equipment_repair.user_id IS '报修人 ID';

COMMENT ON COLUMN fitness_equipment_repair.description IS '问题描述';

COMMENT ON COLUMN fitness_equipment_repair.image_url IS '问题图片 URL';

COMMENT ON COLUMN fitness_equipment_repair.status IS '状态：0-待处理，1-处理中，2-已完成';

COMMENT ON COLUMN fitness_equipment_repair.handle_time IS '处理完成时间';

COMMENT ON COLUMN fitness_equipment_repair.handle_remark IS '处理备注';

COMMENT ON COLUMN fitness_equipment_repair.deleted IS '软删除标识';

-- ========================================================
-- 4. 健身计划模块
-- ========================================================

-- 健身计划表
CREATE TABLE fitness_plan (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    goal VARCHAR(255),
    duration INT,
    status SMALLINT DEFAULT 1 NOT NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE fitness_plan IS '健身计划表';

COMMENT ON COLUMN fitness_plan.user_id IS '用户 ID';

COMMENT ON COLUMN fitness_plan.plan_name IS '计划名称';

COMMENT ON COLUMN fitness_plan.goal IS '健身目标';

COMMENT ON COLUMN fitness_plan.duration IS '计划周期 (天)';

COMMENT ON COLUMN fitness_plan.status IS '状态：0-已停止，1-进行中';

COMMENT ON COLUMN fitness_plan.deleted IS '软删除标识';

-- 健身计划详情表
CREATE TABLE fitness_plan_detail (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    day_of_week SMALLINT NOT NULL,
    exercise_name VARCHAR(100) NOT NULL,
    sets INT,
    reps INT,
    duration INT,
    notes VARCHAR(500),
    sort_order INT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE fitness_plan_detail IS '健身计划详情表';

COMMENT ON COLUMN fitness_plan_detail.plan_id IS '计划 ID';

COMMENT ON COLUMN fitness_plan_detail.day_of_week IS '星期几 (1-7)';

COMMENT ON COLUMN fitness_plan_detail.exercise_name IS '运动名称';

COMMENT ON COLUMN fitness_plan_detail.sets IS '组数';

COMMENT ON COLUMN fitness_plan_detail.reps IS '每组次数';

COMMENT ON COLUMN fitness_plan_detail.duration IS '持续时间 (分钟)';

COMMENT ON COLUMN fitness_plan_detail.notes IS '备注';

COMMENT ON COLUMN fitness_plan_detail.sort_order IS '排序';

COMMENT ON COLUMN fitness_plan_detail.deleted IS '软删除标识';

-- ========================================================
-- 5. 创建索引
-- ========================================================

-- 用户表索引
CREATE INDEX idx_sys_user_username ON sys_user(username);

CREATE INDEX idx_sys_user_phone ON sys_user(phone);

CREATE INDEX idx_sys_user_status ON sys_user(status);

CREATE INDEX idx_sys_user_deleted ON sys_user(deleted);

-- 角色表索引
CREATE INDEX idx_sys_role_role_code ON sys_role(role_code);

CREATE INDEX idx_sys_role_deleted ON sys_role(deleted);

-- 用户角色关联表索引
CREATE INDEX idx_sys_user_role_user_id ON sys_user_role(user_id);

CREATE INDEX idx_sys_user_role_role_id ON sys_user_role(role_id);

-- 权限表索引
CREATE INDEX idx_sys_permission_parent_id ON sys_permission(parent_id);

CREATE INDEX idx_sys_permission_type ON sys_permission(type);

CREATE INDEX idx_sys_permission_deleted ON sys_permission(deleted);

-- 角色权限关联表索引
CREATE INDEX idx_sys_role_permission_role_id ON sys_role_permission(role_id);

CREATE INDEX idx_sys_role_permission_permission_id ON sys_role_permission(permission_id);

-- 课程表索引
CREATE INDEX idx_fitness_course_coach_id ON fitness_course(coach_id);

CREATE INDEX idx_fitness_course_category ON fitness_course(category);

CREATE INDEX idx_fitness_course_status ON fitness_course(status);

CREATE INDEX idx_fitness_course_start_time ON fitness_course(start_time);

CREATE INDEX idx_fitness_course_deleted ON fitness_course(deleted);

-- 预约表索引
CREATE INDEX idx_fitness_booking_user_id ON fitness_booking(user_id);

CREATE INDEX idx_fitness_booking_course_id ON fitness_booking(course_id);

CREATE INDEX idx_fitness_booking_status ON fitness_booking(status);

CREATE INDEX idx_fitness_booking_deleted ON fitness_booking(deleted);

-- 器材表索引
CREATE INDEX idx_fitness_equipment_status ON fitness_equipment(status);

CREATE INDEX idx_fitness_equipment_deleted ON fitness_equipment(deleted);

-- 器材报修表索引
CREATE INDEX idx_fitness_equipment_repair_equipment_id ON fitness_equipment_repair(equipment_id);

CREATE INDEX idx_fitness_equipment_repair_user_id ON fitness_equipment_repair(user_id);

CREATE INDEX idx_fitness_equipment_repair_status ON fitness_equipment_repair(status);

CREATE INDEX idx_fitness_equipment_repair_deleted ON fitness_equipment_repair(deleted);

-- 健身计划表索引
CREATE INDEX idx_fitness_plan_user_id ON fitness_plan(user_id);

CREATE INDEX idx_fitness_plan_status ON fitness_plan(status);

CREATE INDEX idx_fitness_plan_deleted ON fitness_plan(deleted);

-- 健身计划详情表索引
CREATE INDEX idx_fitness_plan_detail_plan_id ON fitness_plan_detail(plan_id);

CREATE INDEX idx_fitness_plan_detail_day_of_week ON fitness_plan_detail(day_of_week);

CREATE INDEX idx_fitness_plan_detail_deleted ON fitness_plan_detail(deleted);

-- 文件记录表索引
CREATE INDEX idx_sys_file_file_name ON sys_file(file_name);

CREATE INDEX idx_sys_file_deleted ON sys_file(deleted);

-- ========================================================
-- 6. 创建外键约束
-- ========================================================

-- 用户角色关联表外键
ALTER TABLE sys_user_role
    ADD CONSTRAINT fk_sys_user_role_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_sys_user_role_role_id FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE;

-- 角色权限关联表外键
ALTER TABLE sys_role_permission
    ADD CONSTRAINT fk_sys_role_permission_role_id FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_sys_role_permission_permission_id FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE;

-- 课程表外键
ALTER TABLE fitness_course
    ADD CONSTRAINT fk_fitness_course_coach_id FOREIGN KEY (coach_id) REFERENCES sys_user(id) ON DELETE RESTRICT;

-- 预约表外键
ALTER TABLE fitness_booking
    ADD CONSTRAINT fk_fitness_booking_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_fitness_booking_course_id FOREIGN KEY (course_id) REFERENCES fitness_course(id) ON DELETE CASCADE;

-- 器材报修表外键
ALTER TABLE fitness_equipment_repair
    ADD CONSTRAINT fk_fitness_equipment_repair_equipment_id FOREIGN KEY (equipment_id) REFERENCES fitness_equipment(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_fitness_equipment_repair_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL;

-- 健身计划表外键
ALTER TABLE fitness_plan
    ADD CONSTRAINT fk_fitness_plan_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;

-- 健身计划详情表外键
ALTER TABLE fitness_plan_detail
    ADD CONSTRAINT fk_fitness_plan_detail_plan_id FOREIGN KEY (plan_id) REFERENCES fitness_plan(id) ON DELETE CASCADE;

-- ========================================================
-- 7. 创建更新时间触发器函数
-- ========================================================

CREATE OR REPLACE FUNCTION update_update_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为需要自动更新 update_time 的表创建触发器
CREATE TRIGGER update_sys_user_update_time BEFORE UPDATE ON sys_user
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_sys_role_update_time BEFORE UPDATE ON sys_role
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_sys_permission_update_time BEFORE UPDATE ON sys_permission
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_fitness_course_update_time BEFORE UPDATE ON fitness_course
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_fitness_booking_update_time BEFORE UPDATE ON fitness_booking
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_fitness_equipment_update_time BEFORE UPDATE ON fitness_equipment
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_fitness_equipment_repair_update_time BEFORE UPDATE ON fitness_equipment_repair
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_fitness_plan_update_time BEFORE UPDATE ON fitness_plan
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_fitness_plan_detail_update_time BEFORE UPDATE ON fitness_plan_detail
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

-- 添加 create_by 列到 sys_file 表
ALTER TABLE sys_file ADD COLUMN IF NOT EXISTS create_by BIGINT;

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_sys_file_create_by ON sys_file(create_by);

CREATE INDEX IF NOT EXISTS idx_sys_file_create_time ON sys_file(create_time);

-- 添加列注释
COMMENT ON COLUMN sys_file.create_by IS '创建人ID';

-- ========================================================
-- 修改时间字段类型：TIMESTAMP WITH TIME ZONE -> TIMESTAMP
-- 说明: 将带时区的时间戳改为不带时区，以支持 Java LocalDateTime
-- ========================================================

-- 1. 修改 sys_user 表
ALTER TABLE sys_user 
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 2. 修改 sys_role 表
ALTER TABLE sys_role 
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 3. 修改 sys_user_role 表
ALTER TABLE sys_user_role 
    ALTER COLUMN create_time TYPE TIMESTAMP;

-- 4. 修改 sys_permission 表
ALTER TABLE sys_permission 
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 5. 修改 sys_role_permission 表
ALTER TABLE sys_role_permission 
    ALTER COLUMN create_time TYPE TIMESTAMP;

-- 6. 修改 sys_file 表
ALTER TABLE sys_file 
    ALTER COLUMN create_time TYPE TIMESTAMP;

-- 7. 修改 fitness_course 表
ALTER TABLE fitness_course 
    ALTER COLUMN start_time TYPE TIMESTAMP,
    ALTER COLUMN end_time TYPE TIMESTAMP,
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 8. 修改 fitness_booking 表
ALTER TABLE fitness_booking 
    ALTER COLUMN booking_time TYPE TIMESTAMP,
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 9. 修改 fitness_equipment 表
ALTER TABLE fitness_equipment 
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 10. 修改 fitness_equipment_repair 表
ALTER TABLE fitness_equipment_repair 
    ALTER COLUMN handle_time TYPE TIMESTAMP,
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 11. 修改 fitness_plan 表
ALTER TABLE fitness_plan 
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 12. 修改 fitness_plan_detail 表
ALTER TABLE fitness_plan_detail 
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 13. 更新触发器函数中的时间类型
CREATE OR REPLACE FUNCTION update_update_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- ========================================================-- 器材类型管理功能升级-- ========================================================

-- 1. 创建器材类型表
CREATE TABLE IF NOT EXISTS fitness_equipment_type (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(50) NOT NULL UNIQUE,
    type_name VARCHAR(100) NOT NULL,
    icon VARCHAR(255),
    sort_order INT DEFAULT 0,
    description TEXT,
    deleted BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 为器材表添加类型字段
ALTER TABLE fitness_equipment 
ADD COLUMN IF NOT EXISTS type_code VARCHAR(50),
ADD COLUMN IF NOT EXISTS equipment_no VARCHAR(100);

-- ========================================================-- 添加健身器材模拟数据-- 说明：插入20条各类健身器材数据，使用 ON CONFLICT 避免重复插入-- ========================================================

-- 1. 先为 equipment_no 添加唯一约束（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'uk_fitness_equipment_no' 
        AND conrelid = 'fitness_equipment'::regclass
    ) THEN
        ALTER TABLE fitness_equipment 
        ADD CONSTRAINT uk_fitness_equipment_no UNIQUE (equipment_no);
    END IF;
END $$;

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

-- ========================================================-- 添加课程总预约人数字段（用于统计独立会员预约数量）-- ========================================================-- 总预约人数：统计所有预约过该课程的独立会员数量
ALTER TABLE fitness_course ADD COLUMN IF NOT EXISTS total_booking_count INT DEFAULT 0;

COMMENT ON COLUMN fitness_course.total_booking_count IS '总预约人数（统计所有预约过该课程的独立会员数量）';

-- 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_fitness_course_total_booking_count ON fitness_course(total_booking_count);

-- ========================================================-- 轮播图表创建脚本-- 版本：V9-- 说明：创建首页轮播图表结构-- ========================================================-- 轮播图表CREATE TABLE sys_banner (    id BIGSERIAL PRIMARY KEY,    title VARCHAR(100) NOT NULL,    subtitle VARCHAR(200),    image_url VARCHAR(500) NOT NULL,    link VARCHAR(500),    sort_order INT DEFAULT 0 NOT NULL,    status SMALLINT DEFAULT 1 NOT NULL,    deleted BOOLEAN DEFAULT FALSE NOT NULL,    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL);COMMENT ON TABLE sys_banner IS '轮播图表';COMMENT ON COLUMN sys_banner.title IS '轮播图标题';COMMENT ON COLUMN sys_banner.subtitle IS '轮播图副标题';COMMENT ON COLUMN sys_banner.image_url IS '图片URL';COMMENT ON COLUMN sys_banner.link IS '跳转链接';COMMENT ON COLUMN sys_banner.sort_order IS '排序顺序';COMMENT ON COLUMN sys_banner.status IS '状态：0-隐藏，1-显示';COMMENT ON COLUMN sys_banner.deleted IS '软删除标识';-- 创建索引CREATE INDEX idx_sys_banner_status ON sys_banner(status);CREATE INDEX idx_sys_banner_sort_order ON sys_banner(sort_order);CREATE INDEX idx_sys_banner_deleted ON sys_banner(deleted);-- 创建更新时间触发器CREATE TRIGGER update_sys_banner_update_time BEFORE UPDATE ON sys_banner    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

-- ========================================================-- 智能健身系统数据库迁移脚本-- 版本：V10-- 说明：创建教练详情表，支持个人图片和标签管理-- ========================================================-- 教练详情表CREATE TABLE coach_detail (    id BIGSERIAL PRIMARY KEY,    user_id BIGINT NOT NULL UNIQUE,    -- 个人展示图片（非头像，用于首页展示）    personal_image_url VARCHAR(500),    -- 教练标签（JSON数组存储，最多5个）    tags JSONB DEFAULT '[]'::jsonb,    -- 从业年限    work_years INT DEFAULT 0,    -- 专业领域（JSON数组）    specialties JSONB DEFAULT '[]'::jsonb,    -- 教学风格    teaching_style VARCHAR(50),    -- 教育背景    education TEXT,    -- 培训经历    training TEXT,    -- 语言能力（JSON数组）    languages JSONB DEFAULT '[]'::jsonb,    -- 个人简介    bio TEXT,    -- 教学经验    experience TEXT,    -- 获得荣誉（JSON数组）    honors JSONB DEFAULT '[]'::jsonb,    -- 紧急联系人信息（JSON对象）    emergency_contact JSONB DEFAULT '{}'::jsonb,    -- 资格认证（JSON数组）    certifications JSONB DEFAULT '[]'::jsonb,    -- 可用时间段（JSON对象）    availability JSONB DEFAULT '{}'::jsonb,    -- 学员数量（统计）    student_count INT DEFAULT 0,    -- 好评率    rating VARCHAR(10) DEFAULT '99%',    deleted BOOLEAN DEFAULT FALSE NOT NULL,    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL,    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP WITH TIME ZONE NOT NULL);COMMENT ON TABLE coach_detail IS '教练详情表';COMMENT ON COLUMN coach_detail.user_id IS '用户ID（关联sys_user）';COMMENT ON COLUMN coach_detail.personal_image_url IS '个人展示图片URL（非头像）';COMMENT ON COLUMN coach_detail.tags IS '教练标签JSON数组，如["增肌塑形","体态矫正"]';COMMENT ON COLUMN coach_detail.work_years IS '从业年限';COMMENT ON COLUMN coach_detail.specialties IS '专业领域JSON数组';COMMENT ON COLUMN coach_detail.teaching_style IS '教学风格';COMMENT ON COLUMN coach_detail.education IS '教育背景';COMMENT ON COLUMN coach_detail.training IS '培训经历';COMMENT ON COLUMN coach_detail.languages IS '语言能力JSON数组';COMMENT ON COLUMN coach_detail.bio IS '个人简介';COMMENT ON COLUMN coach_detail.experience IS '教学经验';COMMENT ON COLUMN coach_detail.honors IS '获得荣誉JSON数组';COMMENT ON COLUMN coach_detail.emergency_contact IS '紧急联系人信息JSON对象';COMMENT ON COLUMN coach_detail.certifications IS '资格认证JSON数组';COMMENT ON COLUMN coach_detail.availability IS '可用时间段JSON对象';COMMENT ON COLUMN coach_detail.student_count IS '学员数量统计';COMMENT ON COLUMN coach_detail.rating IS '好评率';COMMENT ON COLUMN coach_detail.deleted IS '软删除标识';-- 创建索引CREATE INDEX idx_coach_detail_user_id ON coach_detail(user_id);CREATE INDEX idx_coach_detail_deleted ON coach_detail(deleted);-- 添加外键约束ALTER TABLE coach_detail    ADD CONSTRAINT fk_coach_detail_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;-- 创建更新时间触发器CREATE TRIGGER update_coach_detail_update_time BEFORE UPDATE ON coach_detail    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

-- 商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE,
    category VARCHAR(50) NOT NULL,
    image_url VARCHAR(500),
    description TEXT,
    original_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    points_discount_type VARCHAR(20) DEFAULT 'FIXED',
    points_discount_value DECIMAL(10, 2) DEFAULT 0,
    max_points_discount DECIMAL(10, 2) DEFAULT 0,
    stock INTEGER NOT NULL DEFAULT 0,
    sales INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_hot BOOLEAN DEFAULT FALSE,
    is_new BOOLEAN DEFAULT FALSE,
    is_recommend BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE product IS '商品表';

COMMENT ON COLUMN product.name IS '商品名称';

COMMENT ON COLUMN product.code IS '商品编号';

COMMENT ON COLUMN product.category IS '商品分类：EQUIPMENT-运动装备，SUPPLEMENT-营养补剂，COURSE-课程优惠，OTHER-其他';

COMMENT ON COLUMN product.image_url IS '商品图片 URL';

COMMENT ON COLUMN product.description IS '商品描述';

COMMENT ON COLUMN product.original_price IS '商品原价';

COMMENT ON COLUMN product.points_discount_type IS '积分抵扣类型：FIXED-固定金额，PERCENT-比例，NONE-不支持';

COMMENT ON COLUMN product.points_discount_value IS '积分抵扣值 (固定金额或比例)';

COMMENT ON COLUMN product.max_points_discount IS '最大积分抵扣金额';

COMMENT ON COLUMN product.stock IS '库存数量';

COMMENT ON COLUMN product.sales IS '销量';

COMMENT ON COLUMN product.status IS '状态：ACTIVE-上架，INACTIVE-下架';

COMMENT ON COLUMN product.is_hot IS '是否热销';

COMMENT ON COLUMN product.is_new IS '是否新品';

COMMENT ON COLUMN product.is_recommend IS '是否推荐';

COMMENT ON COLUMN product.sort_order IS '排序';

-- 商品订单表
CREATE TABLE IF NOT EXISTS product_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    original_price DECIMAL(10, 2) NOT NULL,
    points_used INTEGER DEFAULT 0,
    points_discount DECIMAL(10, 2) DEFAULT 0,
    final_price DECIMAL(10, 2) NOT NULL,
    pay_amount DECIMAL(10, 2) NOT NULL,
    pay_method VARCHAR(50),
    pay_time TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(20) DEFAULT 'PENDING',
    tracking_no VARCHAR(100),
    carrier VARCHAR(50),
    address TEXT,
    remark TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE product_order IS '商品订单表';

COMMENT ON COLUMN product_order.order_no IS '订单编号';

COMMENT ON COLUMN product_order.user_id IS '用户 ID';

COMMENT ON COLUMN product_order.product_id IS '商品 ID';

COMMENT ON COLUMN product_order.product_name IS '商品名称';

COMMENT ON COLUMN product_order.quantity IS '购买数量';

COMMENT ON COLUMN product_order.original_price IS '商品原价';

COMMENT ON COLUMN product_order.points_used IS '使用积分数量';

COMMENT ON COLUMN product_order.points_discount IS '积分抵扣金额';

COMMENT ON COLUMN product_order.final_price IS '最终支付价格';

COMMENT ON COLUMN product_order.pay_amount IS '实际支付金额';

COMMENT ON COLUMN product_order.pay_method IS '支付方式';

COMMENT ON COLUMN product_order.pay_time IS '支付时间';

COMMENT ON COLUMN product_order.status IS '订单状态：PENDING-待支付，PAID-已支付，PROCESSING-处理中，SHIPPED-已发货，COMPLETED-已完成，CANCELLED-已取消';

COMMENT ON COLUMN product_order.tracking_no IS '物流单号';

COMMENT ON COLUMN product_order.carrier IS '物流公司';

COMMENT ON COLUMN product_order.address IS '收货地址';

COMMENT ON COLUMN product_order.remark IS '备注';

CREATE INDEX idx_product_order_user_id ON product_order(user_id);

CREATE INDEX idx_product_order_status ON product_order(status);

CREATE INDEX idx_product_order_created_at ON product_order(created_at);

-- 为用户表添加积分字段
DO $$ BEGIN
    ALTER TABLE sys_user ADD COLUMN points INTEGER DEFAULT 0;
EXCEPTION
    WHEN duplicate_column THEN
        NULL;
END $$;

-- 添加列注释
COMMENT ON COLUMN sys_user.points IS '积分余额';

-- ========================================================
-- 创建用户健身档案表
-- ========================================================

-- 使用 IF NOT EXISTS 确保幂等性
CREATE TABLE IF NOT EXISTS user_fitness_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    height DECIMAL(5,2),
    weight DECIMAL(5,2),
    age INTEGER,
    experience VARCHAR(20),
    fitness_goal VARCHAR(20),
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 添加列注释
COMMENT ON TABLE user_fitness_profile IS '用户健身档案表';

COMMENT ON COLUMN user_fitness_profile.user_id IS '用户 ID';

COMMENT ON COLUMN user_fitness_profile.height IS '身高 (cm)';

COMMENT ON COLUMN user_fitness_profile.weight IS '体重 (kg)';

COMMENT ON COLUMN user_fitness_profile.age IS '年龄';

COMMENT ON COLUMN user_fitness_profile.experience IS '健身经验：BEGINNER-初学者，INTERMEDIATE-中级，ADVANCED-高级';

COMMENT ON COLUMN user_fitness_profile.fitness_goal IS '健身目标：WEIGHT_LOSS-减脂，MUSCLE_GAIN-增肌，BODY_SHAPING-塑形，ENDURANCE-增强体能，HEALTH-保持健康';

COMMENT ON COLUMN user_fitness_profile.deleted IS '软删除标识';

-- 创建索引（使用 IF NOT EXISTS）
DO $$ BEGIN
    CREATE INDEX idx_user_fitness_profile_user_id ON user_fitness_profile(user_id);
EXCEPTION
    WHEN duplicate_table THEN
        NULL;
    WHEN others THEN
        NULL;
END $$;

-- 添加外键约束（先检查是否已存在）
DO $$ BEGIN
    ALTER TABLE user_fitness_profile
        ADD CONSTRAINT fk_user_fitness_profile_user_id
        FOREIGN KEY (user_id) REFERENCES sys_user(id);
EXCEPTION
    WHEN duplicate_object THEN
        NULL;
    WHEN undefined_table THEN
        NULL;
    WHEN others THEN
        NULL;
END $$;

-- ========================================================
-- 公告表创建脚本
-- 版本：V16
-- 说明：创建公告管理表结构
-- ========================================================

-- 公告表
CREATE TABLE sys_announcement (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    status SMALLINT DEFAULT 0 NOT NULL,
    view_count INT DEFAULT 0 NOT NULL,
    publish_time TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE sys_announcement IS '公告表';

COMMENT ON COLUMN sys_announcement.title IS '公告标题';

COMMENT ON COLUMN sys_announcement.content IS '公告内容';

COMMENT ON COLUMN sys_announcement.type IS '公告类型：SYSTEM-系统公告, ACTIVITY-活动通知, IMPORTANT-重要提醒';

COMMENT ON COLUMN sys_announcement.status IS '状态：0-草稿，1-已发布';

COMMENT ON COLUMN sys_announcement.view_count IS '浏览量';

COMMENT ON COLUMN sys_announcement.publish_time IS '发布时间';

COMMENT ON COLUMN sys_announcement.deleted IS '软删除标识';

-- 创建索引
CREATE INDEX idx_sys_announcement_type ON sys_announcement(type);

CREATE INDEX idx_sys_announcement_status ON sys_announcement(status);

CREATE INDEX idx_sys_announcement_deleted ON sys_announcement(deleted);

CREATE INDEX idx_sys_announcement_publish_time ON sys_announcement(publish_time);

-- 创建更新时间触发器
CREATE TRIGGER update_sys_announcement_update_time BEFORE UPDATE ON sys_announcement
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

-- 智能客服对话会话表
CREATE TABLE chat_session (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE chat_session IS '智能客服对话会话表';

COMMENT ON COLUMN chat_session.id IS '会话ID';

COMMENT ON COLUMN chat_session.user_id IS '用户ID';

COMMENT ON COLUMN chat_session.title IS '会话标题';

COMMENT ON COLUMN chat_session.created_at IS '创建时间';

COMMENT ON COLUMN chat_session.updated_at IS '更新时间';

COMMENT ON COLUMN chat_session.is_deleted IS '是否删除';

CREATE INDEX idx_chat_session_user_id ON chat_session(user_id);

CREATE INDEX idx_chat_session_created_at ON chat_session(created_at);

-- 智能客服对话消息表
CREATE TABLE chat_message (
    id BIGINT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE chat_message IS '智能客服对话消息表';

COMMENT ON COLUMN chat_message.id IS '消息ID';

COMMENT ON COLUMN chat_message.session_id IS '会话ID';

COMMENT ON COLUMN chat_message.role IS '角色：user/assistant';

COMMENT ON COLUMN chat_message.content IS '消息内容';

COMMENT ON COLUMN chat_message.created_at IS '创建时间';

CREATE INDEX idx_chat_message_session_id ON chat_message(session_id);

CREATE INDEX idx_chat_message_created_at ON chat_message(created_at);

-- 为用户健身档案表添加专属教练字段
DO $$ BEGIN
    ALTER TABLE user_fitness_profile ADD COLUMN private_coach_id BIGINT;
EXCEPTION
    WHEN duplicate_column THEN
        NULL;
END $$;

-- 添加列注释
COMMENT ON COLUMN user_fitness_profile.private_coach_id IS '专属教练ID（关联sys_user表）';

-- 添加外键约束（先检查是否已存在）
DO $$ BEGIN
    ALTER TABLE user_fitness_profile
        ADD CONSTRAINT fk_user_fitness_profile_private_coach_id
        FOREIGN KEY (private_coach_id) REFERENCES sys_user(id);
EXCEPTION
    WHEN duplicate_object THEN
        NULL;
    WHEN undefined_table THEN
        NULL;
    WHEN others THEN
        NULL;
END $$;

-- 创建索引（使用 IF NOT EXISTS）
DO $$ BEGIN
    CREATE INDEX idx_user_fitness_profile_private_coach_id ON user_fitness_profile(private_coach_id);
EXCEPTION
    WHEN duplicate_table THEN
        NULL;
    WHEN others THEN
        NULL;
END $$;

-- 添加唯一约束
ALTER TABLE fitness_equipment
    ADD CONSTRAINT uk_equipment_name UNIQUE (equipment_name);

COMMENT ON CONSTRAINT uk_equipment_name ON fitness_equipment IS '器械名称唯一约束';

-- ========================================================
-- 知识库管理模块数据库表
-- 版本：V21
-- 说明：支持RAG多路召回的知识库系统
-- ========================================================

-- 启用 pgvector 扩展（如果未启用）
CREATE EXTENSION IF NOT EXISTS vector;

-- ========================================================
-- 1. 知识库分类表
-- ========================================================
CREATE TABLE knowledge_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    sort_order INT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE knowledge_category IS '知识库分类表';

COMMENT ON COLUMN knowledge_category.name IS '分类名称';

COMMENT ON COLUMN knowledge_category.code IS '分类编码';

COMMENT ON COLUMN knowledge_category.description IS '分类描述';

COMMENT ON COLUMN knowledge_category.sort_order IS '排序';

COMMENT ON COLUMN knowledge_category.deleted IS '软删除标识';

-- ========================================================
-- 2. 知识库文档表
-- ========================================================
CREATE TABLE knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    summary VARCHAR(500),
    file_url VARCHAR(500),
    file_name VARCHAR(255),
    file_type VARCHAR(50),
    file_size BIGINT DEFAULT 0,
    category_id BIGINT,
    tags JSONB DEFAULT '[]'::jsonb,
    status SMALLINT DEFAULT 0 NOT NULL,
    chunk_count INT DEFAULT 0,
    create_by BIGINT,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE knowledge_document IS '知识库文档表';

COMMENT ON COLUMN knowledge_document.title IS '文档标题';

COMMENT ON COLUMN knowledge_document.content IS '原始文本内容';

COMMENT ON COLUMN knowledge_document.summary IS '文档摘要';

COMMENT ON COLUMN knowledge_document.file_url IS 'MinIO文件URL';

COMMENT ON COLUMN knowledge_document.file_name IS '原始文件名';

COMMENT ON COLUMN knowledge_document.file_type IS '文件类型(md/txt)';

COMMENT ON COLUMN knowledge_document.file_size IS '文件大小(字节)';

COMMENT ON COLUMN knowledge_document.category_id IS '分类ID';

COMMENT ON COLUMN knowledge_document.tags IS '标签(JSON数组)';

COMMENT ON COLUMN knowledge_document.status IS '状态：0-草稿，1-已发布，2-已归档';

COMMENT ON COLUMN knowledge_document.chunk_count IS '切片数量';

COMMENT ON COLUMN knowledge_document.create_by IS '创建人ID';

COMMENT ON COLUMN knowledge_document.deleted IS '软删除标识';

-- ========================================================
-- 3. 知识库切片表（核心RAG表）
-- ========================================================
CREATE TABLE knowledge_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    content_hash VARCHAR(64),
    embedding vector(768),
    metadata JSONB DEFAULT '{}'::jsonb,
    char_count INT DEFAULT 0,
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE knowledge_chunk IS '知识库切片表';

COMMENT ON COLUMN knowledge_chunk.document_id IS '关联文档ID';

COMMENT ON COLUMN knowledge_chunk.chunk_index IS '切片序号(从0开始)';

COMMENT ON COLUMN knowledge_chunk.content IS '切片文本内容';

COMMENT ON COLUMN knowledge_chunk.content_hash IS '内容哈希(MD5)';

COMMENT ON COLUMN knowledge_chunk.embedding IS '向量嵌入(768维)';

COMMENT ON COLUMN knowledge_chunk.metadata IS '元数据(JSONB)';

COMMENT ON COLUMN knowledge_chunk.char_count IS '字符数';

-- ========================================================
-- 4. 创建索引
-- ========================================================

-- 分类表索引
CREATE INDEX idx_knowledge_category_code ON knowledge_category(code);

CREATE INDEX idx_knowledge_category_deleted ON knowledge_category(deleted);

-- 文档表索引
CREATE INDEX idx_knowledge_document_category_id ON knowledge_document(category_id);

CREATE INDEX idx_knowledge_document_status ON knowledge_document(status);

CREATE INDEX idx_knowledge_document_create_by ON knowledge_document(create_by);

CREATE INDEX idx_knowledge_document_deleted ON knowledge_document(deleted);

CREATE INDEX idx_knowledge_document_create_time ON knowledge_document(create_time);

-- 切片表索引
CREATE INDEX idx_knowledge_chunk_document_id ON knowledge_chunk(document_id);

CREATE INDEX idx_knowledge_chunk_content_hash ON knowledge_chunk(content_hash);

-- 向量索引（HNSW，用于近似最近邻搜索）
CREATE INDEX idx_knowledge_chunk_embedding ON knowledge_chunk 
USING hnsw (embedding vector_cosine_ops);

-- 全文搜索索引（用于关键词检索）
CREATE INDEX idx_knowledge_chunk_content_fts ON knowledge_chunk 
USING gin(to_tsvector('simple', content));

-- ========================================================
-- 5. 创建外键约束
-- ========================================================
ALTER TABLE knowledge_document
    ADD CONSTRAINT fk_knowledge_document_category_id 
    FOREIGN KEY (category_id) REFERENCES knowledge_category(id) ON DELETE SET NULL;

ALTER TABLE knowledge_chunk
    ADD CONSTRAINT fk_knowledge_chunk_document_id 
    FOREIGN KEY (document_id) REFERENCES knowledge_document(id) ON DELETE CASCADE;

-- ========================================================
-- 6. 创建更新时间触发器
-- ========================================================
CREATE TRIGGER update_knowledge_category_update_time 
BEFORE UPDATE ON knowledge_category
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_knowledge_document_update_time 
BEFORE UPDATE ON knowledge_document
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

-- 简化 knowledge_document 表，移除不需要的字段
-- 只保留：id, title, file_url, file_name, file_type, file_size, status, chunk_count, create_by, create_time, update_time, deleted

-- 移除不需要的字段
ALTER TABLE knowledge_document DROP COLUMN IF EXISTS content;

ALTER TABLE knowledge_document DROP COLUMN IF EXISTS summary;

ALTER TABLE knowledge_document DROP COLUMN IF EXISTS category_id;

ALTER TABLE knowledge_document DROP COLUMN IF EXISTS tags;

-- ========================================================
-- 用户健身档案表添加性别字段
-- ========================================================

-- 添加性别字段
ALTER TABLE user_fitness_profile ADD COLUMN IF NOT EXISTS gender VARCHAR(10);

-- 添加列注释
COMMENT ON COLUMN user_fitness_profile.gender IS '性别：MALE-男，FEMALE-女';

-- ========================================================
-- ????????????????
-- ???????? image_url ????? image_urls???????
-- ========================================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'fitness_equipment_repair' AND column_name = 'image_url'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'fitness_equipment_repair' AND column_name = 'image_urls'
    ) THEN
        ALTER TABLE fitness_equipment_repair RENAME COLUMN image_url TO image_urls;
    END IF;
END $$;

ALTER TABLE fitness_equipment_repair
    ADD COLUMN IF NOT EXISTS image_urls VARCHAR(2000);

ALTER TABLE fitness_equipment_repair
    ALTER COLUMN image_urls TYPE VARCHAR(2000);

COMMENT ON COLUMN fitness_equipment_repair.image_urls IS '????URL???????????';

-- ========================================================
-- 智能健身系统数据库迁移脚本
-- 版本：V28
-- 说明：补充报修系统缺失的字段和表（V24脚本内容被替换，此处补充）
-- ========================================================

-- 添加 handle_remark 字段到报修表（如果不存在）
ALTER TABLE fitness_equipment_repair
    ADD COLUMN IF NOT EXISTS handle_remark VARCHAR(500);

COMMENT ON COLUMN fitness_equipment_repair.handle_remark IS '处理备注';

-- 创建报修处理记录表（如果不存在）
CREATE TABLE IF NOT EXISTS fitness_repair_record (
    id BIGSERIAL PRIMARY KEY,
    repair_id BIGINT NOT NULL,
    handler_id BIGINT,
    record_type SMALLINT DEFAULT 1 NOT NULL,
    before_status SMALLINT,
    after_status SMALLINT,
    content TEXT,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE fitness_repair_record IS '报修处理记录表';

COMMENT ON COLUMN fitness_repair_record.repair_id IS '报修ID';

COMMENT ON COLUMN fitness_repair_record.handler_id IS '处理人ID（管理员）';

COMMENT ON COLUMN fitness_repair_record.record_type IS '处理类型：1-提交报修, 2-状态变更, 3-处理备注, 4-取消报修';

COMMENT ON COLUMN fitness_repair_record.before_status IS '处理前状态';

COMMENT ON COLUMN fitness_repair_record.after_status IS '处理后状态';

COMMENT ON COLUMN fitness_repair_record.content IS '处理内容/备注';

COMMENT ON COLUMN fitness_repair_record.deleted IS '软删除标识';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_fitness_repair_record_repair_id ON fitness_repair_record(repair_id);

CREATE INDEX IF NOT EXISTS idx_fitness_repair_record_handler_id ON fitness_repair_record(handler_id);

CREATE INDEX IF NOT EXISTS idx_fitness_repair_record_deleted ON fitness_repair_record(deleted);

-- 添加外键约束（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_fitness_repair_record_repair_id'
        AND table_name = 'fitness_repair_record'
    ) THEN
        ALTER TABLE fitness_repair_record
            ADD CONSTRAINT fk_fitness_repair_record_repair_id 
            FOREIGN KEY (repair_id) REFERENCES fitness_equipment_repair(id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_fitness_repair_record_handler_id'
        AND table_name = 'fitness_repair_record'
    ) THEN
        ALTER TABLE fitness_repair_record
            ADD CONSTRAINT fk_fitness_repair_record_handler_id 
            FOREIGN KEY (handler_id) REFERENCES sys_user(id) ON DELETE SET NULL;
    END IF;
END $$;

-- 创建更新时间触发器
DROP TRIGGER IF EXISTS update_fitness_repair_record_update_time ON fitness_repair_record;

CREATE TRIGGER update_fitness_repair_record_update_time 
    BEFORE UPDATE ON fitness_repair_record 
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

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

-- 4. 删除旧索引
DROP INDEX IF EXISTS idx_fitness_plan_detail_day_of_week;

-- 5. 创建新索引
CREATE INDEX idx_fitness_plan_detail_day_index ON fitness_plan_detail(day_index);

-- 6. 将旧字段标记为废弃（可选，保留数据兼容性）
-- 注意：day_of_week, exercise_name, sets, reps 字段保留以确保向后兼容性
-- 新代码应该使用 day_index, day_name, focus, course_id, course_name, equipment_json, exercises_json 字段

-- 创建AI数据分析报告表
CREATE TABLE analysis_report (
    id BIGSERIAL PRIMARY KEY,
    report_title VARCHAR(255) NOT NULL,
    analysis_type VARCHAR(50) NOT NULL,
    report_content TEXT NOT NULL,
    suggestions TEXT,           -- Markdown格式文本
    generate_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建索引
CREATE INDEX idx_analysis_report_type ON analysis_report(analysis_type);

CREATE INDEX idx_analysis_report_generate_time ON analysis_report(generate_time DESC);

CREATE INDEX idx_analysis_report_create_by ON analysis_report(create_by);

CREATE INDEX idx_analysis_report_is_deleted ON analysis_report(is_deleted);

-- 健身计划表扩展：增加用户身体属性字段和完整JSON数据存储
-- 用于会员端健身计划的保存和展示

ALTER TABLE fitness_plan
    ADD COLUMN IF NOT EXISTS height DECIMAL(5,2) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS weight DECIMAL(5,2) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS age INTEGER DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS gender VARCHAR(10) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS experience VARCHAR(20) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS fitness_goal VARCHAR(50) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS plan_data_json JSONB DEFAULT NULL;

COMMENT ON COLUMN fitness_plan.height IS '身高(cm)';

COMMENT ON COLUMN fitness_plan.weight IS '体重(kg)';

COMMENT ON COLUMN fitness_plan.age IS '年龄';

COMMENT ON COLUMN fitness_plan.gender IS '性别';

COMMENT ON COLUMN fitness_plan.experience IS '健身经验';

COMMENT ON COLUMN fitness_plan.fitness_goal IS '健身目标';

COMMENT ON COLUMN fitness_plan.plan_data_json IS 'LLM返回的完整健身计划JSON数据';

CREATE INDEX IF NOT EXISTS idx_fitness_plan_user_time ON fitness_plan(user_id, create_time DESC);

-- ========================================================
-- V33: 修复添加 nickname 字段（V32 未实际生效）
-- 说明：为用户添加昵称字段，与用户名(username)区分
-- ========================================================

-- 检查并添加 nickname 字段（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'sys_user' AND column_name = 'nickname'
    ) THEN
        ALTER TABLE sys_user ADD COLUMN nickname VARCHAR(50);
        RAISE NOTICE 'Added nickname column to sys_user';
    ELSE
        RAISE NOTICE 'nickname column already exists in sys_user';
    END IF;
END $$;

-- 添加注释
COMMENT ON COLUMN sys_user.nickname IS '用户昵称/姓名，用于展示';

-- 创建索引（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE tablename = 'sys_user' AND indexname = 'idx_sys_user_nickname'
    ) THEN
        CREATE INDEX idx_sys_user_nickname ON sys_user(nickname);
        RAISE NOTICE 'Created index idx_sys_user_nickname';
    ELSE
        RAISE NOTICE 'Index idx_sys_user_nickname already exists';
    END IF;
END $$;

-- 报修记录关联器械：增加equipment_id字段
ALTER TABLE fitness_equipment_repair ADD COLUMN IF NOT EXISTS equipment_id BIGINT;

-- 添加外键索引（加速按器械查询报修记录）
CREATE INDEX IF NOT EXISTS idx_repair_equipment_id ON fitness_equipment_repair(equipment_id);

-- 添加外键约束关联fitness_equipment表
ALTER TABLE fitness_equipment_repair ADD CONSTRAINT fk_repair_equipment 
    FOREIGN KEY (equipment_id) REFERENCES fitness_equipment(id) ON DELETE SET NULL;

-- ==========================================
-- 恢复知识库文档分类字段
-- 版本: V36
-- 说明: 为 knowledge_document 表添加 category_id 字段
-- 原因: V22 误删了该字段，现恢复以支持文档分类功能
-- 作者: AI Assistant
-- 日期: 2026-04-12
-- ==========================================

-- 1. 添加 category_id 字段（如果不存在）
ALTER TABLE knowledge_document 
ADD COLUMN IF NOT EXISTS category_id BIGINT;

-- 2. 添加外键约束（先删除可能存在的旧约束，再添加）
ALTER TABLE knowledge_document 
DROP CONSTRAINT IF EXISTS fk_knowledge_document_category_id;

ALTER TABLE knowledge_document 
ADD CONSTRAINT fk_knowledge_document_category_id 
FOREIGN KEY (category_id) REFERENCES knowledge_category(id) ON DELETE SET NULL;

-- 3. 添加索引（如果不存在）
CREATE INDEX IF NOT EXISTS idx_knowledge_document_category_id 
ON knowledge_document(category_id);

-- 4. 添加字段注释
COMMENT ON COLUMN knowledge_document.category_id IS '分类ID，关联 knowledge_category 表';

-- ==========================================
-- 验证脚本
-- ==========================================
-- 查询表结构验证
-- SELECT column_name, data_type, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'knowledge_document' 
-- AND column_name = 'category_id';

-- 查询外键约束验证
-- SELECT tc.constraint_name, tc.table_name, kcu.column_name, ccu.table_name AS foreign_table_name
-- FROM information_schema.table_constraints AS tc
-- JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name
-- JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name
-- WHERE tc.constraint_type = 'FOREIGN KEY' 
-- AND tc.table_name = 'knowledge_document';

-- ==========================================
-- 使用说明：
-- 1. 此脚本恢复 V22 误删的 category_id 字段
-- 2. 使用 IF NOT EXISTS 和 DROP IF EXISTS 确保幂等性
-- 3. 外键设置 ON DELETE SET NULL，删除分类时文档变为未分类
-- 4. 重启应用后 Flyway 会自动执行此脚本
-- ==========================================

-- 会员卡类型表
CREATE TABLE membership_card_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE membership_card_type IS '会员卡类型表';

COMMENT ON COLUMN membership_card_type.name IS '类型名称，如：月卡、季卡、年卡';

COMMENT ON COLUMN membership_card_type.code IS '类型编码';

COMMENT ON COLUMN membership_card_type.description IS '类型描述';

COMMENT ON COLUMN membership_card_type.status IS '状态：ACTIVE-启用，INACTIVE-禁用';

COMMENT ON COLUMN membership_card_type.sort_order IS '排序号';

-- 会员卡定义表
CREATE TABLE membership_card (
    id BIGSERIAL PRIMARY KEY,
    type_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    subtitle VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2),
    duration_days INT NOT NULL,
    points_reward INT DEFAULT 0,
    daily_price DECIMAL(10, 2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_recommend BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    cover_image VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_membership_card_type FOREIGN KEY (type_id) REFERENCES membership_card_type(id)
);

COMMENT ON TABLE membership_card IS '会员卡定义表';

COMMENT ON COLUMN membership_card.type_id IS '会员卡类型ID';

COMMENT ON COLUMN membership_card.name IS '会员卡名称';

COMMENT ON COLUMN membership_card.subtitle IS '副标题/简介';

COMMENT ON COLUMN membership_card.price IS '售价';

COMMENT ON COLUMN membership_card.original_price IS '原价';

COMMENT ON COLUMN membership_card.duration_days IS '有效期天数';

COMMENT ON COLUMN membership_card.points_reward IS '购买赠送积分';

COMMENT ON COLUMN membership_card.daily_price IS '日均价格（用于展示）';

COMMENT ON COLUMN membership_card.status IS '状态：ACTIVE-上架，INACTIVE-下架';

COMMENT ON COLUMN membership_card.is_recommend IS '是否推荐';

COMMENT ON COLUMN membership_card.sort_order IS '排序号';

COMMENT ON COLUMN membership_card.cover_image IS '封面图片URL';

-- 会员卡内容项表（动态多条）
CREATE TABLE membership_card_content (
    id BIGSERIAL PRIMARY KEY,
    card_id BIGINT NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    icon VARCHAR(200),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_content_card FOREIGN KEY (card_id) REFERENCES membership_card(id) ON DELETE CASCADE
);

COMMENT ON TABLE membership_card_content IS '会员卡内容项表';

COMMENT ON COLUMN membership_card_content.card_id IS '会员卡ID';

COMMENT ON COLUMN membership_card_content.content_type IS '内容类型：BENEFIT-权益说明, RULE-使用规则, PRIVILEGE-特权列表, OTHER-其他';

COMMENT ON COLUMN membership_card_content.title IS '标题';

COMMENT ON COLUMN membership_card_content.description IS '详细描述';

COMMENT ON COLUMN membership_card_content.icon IS '图标';

COMMENT ON COLUMN membership_card_content.sort_order IS '排序号';

-- 会员卡订单表
CREATE TABLE membership_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(64) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    card_name VARCHAR(200),
    price DECIMAL(10, 2) NOT NULL,
    pay_amount DECIMAL(10, 2),
    pay_method VARCHAR(50),
    pay_time TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(20) DEFAULT 'PENDING',
    expire_time TIMESTAMP WITHOUT TIME ZONE,
    alipay_trade_no VARCHAR(128),
    remark VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_card FOREIGN KEY (card_id) REFERENCES membership_card(id),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

COMMENT ON TABLE membership_order IS '会员卡订单表';

COMMENT ON COLUMN membership_order.order_no IS '订单编号';

COMMENT ON COLUMN membership_order.user_id IS '用户ID';

COMMENT ON COLUMN membership_order.card_id IS '会员卡ID';

COMMENT ON COLUMN membership_order.card_name IS '会员卡名称（快照）';

COMMENT ON COLUMN membership_order.price IS '订单金额';

COMMENT ON COLUMN membership_order.pay_amount IS '实际支付金额';

COMMENT ON COLUMN membership_order.pay_method IS '支付方式：ALIPAY-支付宝, BALANCE-余额';

COMMENT ON COLUMN membership_order.pay_time IS '支付时间';

COMMENT ON COLUMN membership_order.status IS '状态：PENDING-待支付, PAID-已支付, CANCELLED-已取消, TIMEOUT-已超时';

COMMENT ON COLUMN membership_order.expire_time IS '会员到期时间';

COMMENT ON COLUMN membership_order.alipay_trade_no IS '支付宝交易号';

COMMENT ON COLUMN membership_order.remark IS '备注';

-- 用户会员信息表（记录当前会员状态）
CREATE TABLE user_membership (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    membership_type VARCHAR(50),
    start_time TIMESTAMP WITHOUT TIME ZONE,
    expire_time TIMESTAMP WITHOUT TIME ZONE,
    is_active BOOLEAN DEFAULT FALSE,
    total_orders INT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_membership_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

COMMENT ON TABLE user_membership IS '用户会员信息表';

COMMENT ON COLUMN user_membership.user_id IS '用户ID';

COMMENT ON COLUMN user_membership.membership_type IS '当前会员类型';

COMMENT ON COLUMN user_membership.start_time IS '会员开始时间';

COMMENT ON COLUMN user_membership.expire_time IS '会员到期时间';

COMMENT ON COLUMN user_membership.is_active IS '是否有效';

COMMENT ON COLUMN user_membership.total_orders IS '累计购买次数';

-- 给 sys_user 表添加 balance 字段（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'sys_user' AND column_name = 'balance') THEN
        ALTER TABLE sys_user ADD COLUMN balance DECIMAL(10, 2) DEFAULT 0.00;
    END IF;
END $$;

COMMENT ON COLUMN sys_user.balance IS '账户余额';

-- 创建索引
CREATE INDEX idx_membership_card_type_id ON membership_card(type_id);

CREATE INDEX idx_membership_card_status ON membership_card(status);

CREATE INDEX idx_card_content_card_id ON membership_card_content(card_id);

CREATE INDEX idx_membership_order_user_id ON membership_order(user_id);

CREATE INDEX idx_membership_order_status ON membership_order(status);

CREATE INDEX idx_membership_order_no ON membership_order(order_no);

-- 为用户表添加余额字段（用于余额支付）
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS balance DECIMAL(10, 2) DEFAULT 0.00;

COMMENT ON COLUMN sys_user.balance IS '账户余额';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_balance ON sys_user(balance);

-- 数据字典表
CREATE TABLE sys_dict (
    id BIGSERIAL PRIMARY KEY,
    dict_name VARCHAR(100) NOT NULL,
    dict_code VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_dict IS '数据字典表';

COMMENT ON COLUMN sys_dict.dict_name IS '字典名称';

COMMENT ON COLUMN sys_dict.dict_code IS '字典编码（唯一标识）';

COMMENT ON COLUMN sys_dict.description IS '描述';

COMMENT ON COLUMN sys_dict.status IS '状态：ACTIVE-启用，INACTIVE-禁用';

COMMENT ON COLUMN sys_dict.sort_order IS '排序号';

-- 数据字典项表
CREATE TABLE sys_dict_item (
    id BIGSERIAL PRIMARY KEY,
    dict_id BIGINT NOT NULL,
    label VARCHAR(200) NOT NULL,
    value VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    sort_order INT DEFAULT 0,
    extra JSONB,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dict_item_dict FOREIGN KEY (dict_id) REFERENCES sys_dict(id) ON DELETE CASCADE
);

COMMENT ON TABLE sys_dict_item IS '数据字典项表';

COMMENT ON COLUMN sys_dict_item.dict_id IS '所属字典ID';

COMMENT ON COLUMN sys_dict_item.label IS '显示名称';

COMMENT ON COLUMN sys_dict_item.value IS '实际值';

COMMENT ON COLUMN sys_dict_item.description IS '描述';

COMMENT ON COLUMN sys_dict_item.status IS '状态：ACTIVE-启用，INACTIVE-禁用';

COMMENT ON COLUMN sys_dict_item.sort_order IS '排序号';

COMMENT ON COLUMN sys_dict_item.extra IS '扩展属性（JSON格式）';

-- 创建索引
CREATE INDEX idx_sys_dict_code ON sys_dict(dict_code);

CREATE INDEX idx_sys_dict_status ON sys_dict(status);

CREATE INDEX idx_dict_item_dict_id ON sys_dict_item(dict_id);

CREATE INDEX idx_dict_item_value ON sys_dict_item(value);

-- 为 membership_card 表添加 type_code 字段
ALTER TABLE membership_card ADD COLUMN IF NOT EXISTS type_code VARCHAR(50);

COMMENT ON COLUMN membership_card.type_code IS '会员卡类型编码（冗余字段，便于查询）';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_membership_card_type_code ON membership_card(type_code);

-- 修改 membership_order 表的时间字段命名，统一为 create_time 和 update_time

-- 先添加新字段
ALTER TABLE membership_order ADD COLUMN IF NOT EXISTS create_time TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE membership_order ADD COLUMN IF NOT EXISTS update_time TIMESTAMP WITHOUT TIME ZONE;

-- 删除旧字段
ALTER TABLE membership_order DROP COLUMN IF EXISTS created_at;

ALTER TABLE membership_order DROP COLUMN IF EXISTS updated_at;

-- 添加注释
COMMENT ON COLUMN membership_order.create_time IS '创建时间';

COMMENT ON COLUMN membership_order.update_time IS '更新时间';

CREATE TABLE chat_long_term_memory (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    memory_key VARCHAR(100) NOT NULL,
    memory_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB DEFAULT '{}'::jsonb NOT NULL,
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (user_id, memory_key)
);

COMMENT ON TABLE chat_long_term_memory IS '健小助长期记忆表';

COMMENT ON COLUMN chat_long_term_memory.user_id IS '用户ID';

COMMENT ON COLUMN chat_long_term_memory.memory_key IS '记忆唯一键';

COMMENT ON COLUMN chat_long_term_memory.memory_type IS '记忆类型';

COMMENT ON COLUMN chat_long_term_memory.content IS '记忆内容';

COMMENT ON COLUMN chat_long_term_memory.metadata IS '扩展元数据';

CREATE INDEX idx_chat_long_term_memory_user_id ON chat_long_term_memory(user_id);

CREATE INDEX idx_chat_long_term_memory_type ON chat_long_term_memory(memory_type);

-- 创建专门的触发器函数
CREATE OR REPLACE FUNCTION update_chat_long_term_memory_update_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_chat_long_term_memory_update_time
BEFORE UPDATE ON chat_long_term_memory
    FOR EACH ROW EXECUTE FUNCTION update_chat_long_term_memory_update_time();

-- ========================================================
-- 视频课程管理表
-- ========================================================

CREATE TABLE fitness_video_course (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    cover_url VARCHAR(500),
    video_url VARCHAR(500) NOT NULL,
    duration_seconds INT DEFAULT 0,
    file_size BIGINT DEFAULT 0,
    difficulty_level VARCHAR(20),
    coach_id BIGINT,
    status SMALLINT DEFAULT 1 NOT NULL,
    view_count INT DEFAULT 0,
    sort_order INT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE fitness_video_course IS '视频课程表';

COMMENT ON COLUMN fitness_video_course.title IS '视频标题';

COMMENT ON COLUMN fitness_video_course.description IS '视频描述';

COMMENT ON COLUMN fitness_video_course.category IS '分类';

COMMENT ON COLUMN fitness_video_course.cover_url IS '封面图片URL';

COMMENT ON COLUMN fitness_video_course.video_url IS '视频文件URL';

COMMENT ON COLUMN fitness_video_course.duration_seconds IS '视频时长(秒)';

COMMENT ON COLUMN fitness_video_course.file_size IS '文件大小(字节)';

COMMENT ON COLUMN fitness_video_course.difficulty_level IS '难度等级';

COMMENT ON COLUMN fitness_video_course.coach_id IS '教练ID';

COMMENT ON COLUMN fitness_video_course.status IS '状态：0-下架，1-上架';

COMMENT ON COLUMN fitness_video_course.view_count IS '观看次数';

COMMENT ON COLUMN fitness_video_course.sort_order IS '排序';

COMMENT ON COLUMN fitness_video_course.deleted IS '软删除标识';

CREATE INDEX idx_video_course_category ON fitness_video_course(category);

CREATE INDEX idx_video_course_coach_id ON fitness_video_course(coach_id);

CREATE INDEX idx_video_course_status ON fitness_video_course(status);

-- 触发器函数
CREATE OR REPLACE FUNCTION update_fitness_video_course_update_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_fitness_video_course_update_time
BEFORE UPDATE ON fitness_video_course
    FOR EACH ROW EXECUTE FUNCTION update_fitness_video_course_update_time();

-- ========================================================
-- 公开课程表结构优化：将具体日期时间改为周期性时间安排
-- 业务场景：公开课是固定的"一周中的某天由某个教练进行授课"
-- 版本：V44
-- ========================================================

-- 1. 新增 day_of_week 字段（星期几：1=周一, 2=周二, ..., 7=周日）
ALTER TABLE fitness_course ADD COLUMN IF NOT EXISTS day_of_week SMALLINT NOT NULL DEFAULT 1;

COMMENT ON COLUMN fitness_course.day_of_week IS '星期几：1-周一, 2-周二, 3-周三, 4-周四, 5-周五, 6-周六, 7-周日';

-- 3. 将 start_time 从 TIMESTAMP 改为 TIME（仅保留时分秒）
ALTER TABLE fitness_course ALTER COLUMN start_time TYPE TIME USING (start_time::TIME);

COMMENT ON COLUMN fitness_course.start_time IS '开始时间（时分秒，如 14:00:00）';

-- 4. 将 end_time 从 TIMESTAMP 改为 TIME（仅保留时分秒）
ALTER TABLE fitness_course ALTER COLUMN end_time TYPE TIME USING (end_time::TIME);

COMMENT ON COLUMN fitness_course.end_time IS '结束时间（时分秒，如 15:30:00）';

-- ========================================================
-- 课程周实例表：将周期性课程模板展开为每周具体实例
-- 业务场景：公开课"周一14:00"需要展开为"3月9日周一"、"3月16日周一"...
-- 版本：V45
-- ========================================================

CREATE TABLE IF NOT EXISTS fitness_course_session (
    id BIGSERIAL PRIMARY KEY,
    
    -- 关联的课程模板
    course_id BIGINT NOT NULL,
    
    -- 本次实例的具体日期（哪一天上课）
    session_date DATE NOT NULL,
    
    -- 从课程模板继承的字段（冗余存储，避免频繁JOIN）
    day_of_week SMALLINT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    
    -- 本次实例的状态
    status SMALLINT DEFAULT 0 NOT NULL,  -- 0-待开始, 1-进行中, 2-已结束, 3-已取消
    
    -- 容量管理（每次实例独立计算）
    capacity INT NOT NULL,
    booked_count INT DEFAULT 0 NOT NULL,
    
    -- 时间戳
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    deleted BOOLEAN DEFAULT FALSE NOT NULL
);

COMMENT ON TABLE fitness_course_session IS '课程周实例表（周期性课程的具体某一次）';

COMMENT ON COLUMN fitness_course_session.course_id IS '关联课程模板ID';

COMMENT ON COLUMN fitness_course_session.session_date IS '本次上课的具体日期';

COMMENT ON COLUMN fitness_course_session.day_of_week IS '星期几（冗余）';

COMMENT ON COLUMN fitness_course_session.start_time IS '开始时间（时分秒，冗余）';

COMMENT ON COLUMN fitness_course_session.end_time IS '结束时间（时分秒，冗余）';

COMMENT ON COLUMN fitness_course_session.status IS '状态：0-待开始, 1-进行中, 2-已结束, 3-已取消';

COMMENT ON COLUMN fitness_course_session.capacity IS '本次最大容量';

COMMENT ON COLUMN fitness_course_session.booked_count IS '本次已预约数';

CREATE INDEX idx_session_course_id ON fitness_course_session(course_id);

CREATE INDEX idx_session_date ON fitness_course_session(session_date);

CREATE UNIQUE INDEX idx_session_unique ON fitness_course_session(course_id, session_date) WHERE deleted = FALSE;

-- 预约表增加 session_id 字段
ALTER TABLE fitness_booking ADD COLUMN IF NOT EXISTS session_id BIGINT;

COMMENT ON COLUMN fitness_booking.session_id IS '关联课程实例ID（周期性课程的某一次具体实例）';

CREATE INDEX idx_booking_session_id ON fitness_booking(session_id);

-- 商品表新增教练ID字段，用于标记私教套餐商品所属教练
ALTER TABLE product ADD COLUMN IF NOT EXISTS coach_id BIGINT;

COMMENT ON COLUMN product.coach_id IS '关联教练用户ID，仅私教套餐类商品使用';

-- 商品订单表新增教练ID字段，用于记录购买私教套餐时的教练绑定关系
ALTER TABLE product_order ADD COLUMN IF NOT EXISTS coach_id BIGINT;

COMMENT ON COLUMN product_order.coach_id IS '关联教练用户ID，仅私教套餐订单使用';

-- ========================================================-- V47: 添加 real_name 字段到 coach_detail 表-- 说明：为教练详情添加真实姓名字段，用于前端展示-- ========================================================

-- 检查并添加 real_name 字段（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'coach_detail' AND column_name = 'real_name'
    ) THEN
        ALTER TABLE coach_detail ADD COLUMN real_name VARCHAR(50);
        RAISE NOTICE 'Added real_name column to coach_detail';
    ELSE
        RAISE NOTICE 'real_name column already exists in coach_detail';
    END IF;
END $$;

-- 添加注释
COMMENT ON COLUMN coach_detail.real_name IS '教练真实姓名，用于前端展示';

-- 创建索引（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE tablename = 'coach_detail' AND indexname = 'idx_coach_detail_real_name'
    ) THEN
        CREATE INDEX idx_coach_detail_real_name ON coach_detail(real_name);
        RAISE NOTICE 'Created index idx_coach_detail_real_name';
    ELSE
        RAISE NOTICE 'Index idx_coach_detail_real_name already exists';
    END IF;
END $$;

-- ========================================================
-- ??????
-- ?????????????/???? PostgreSQL ??
-- ========================================================
COMMENT ON TABLE analysis_report IS 'AI???????';

COMMENT ON COLUMN analysis_report.id IS '??ID';

COMMENT ON COLUMN analysis_report.report_title IS '????';

COMMENT ON COLUMN analysis_report.analysis_type IS '????';

COMMENT ON COLUMN analysis_report.report_content IS '????';

COMMENT ON COLUMN analysis_report.suggestions IS '?????Markdown???';

COMMENT ON COLUMN analysis_report.generate_time IS '????';

COMMENT ON COLUMN analysis_report.create_time IS '????';

COMMENT ON COLUMN analysis_report.update_time IS '????';

COMMENT ON COLUMN analysis_report.create_by IS '???ID';

COMMENT ON COLUMN analysis_report.is_deleted IS '?????';

COMMENT ON TABLE fitness_equipment_type IS '?????';

COMMENT ON COLUMN fitness_equipment_type.id IS '??ID';

COMMENT ON COLUMN fitness_equipment_type.type_code IS '??????';

COMMENT ON COLUMN fitness_equipment_type.type_name IS '??????';

COMMENT ON COLUMN fitness_equipment_type.icon IS '????';

COMMENT ON COLUMN fitness_equipment_type.sort_order IS '???';

COMMENT ON COLUMN fitness_equipment_type.description IS '??????';

COMMENT ON COLUMN fitness_equipment_type.deleted IS '?????';

COMMENT ON COLUMN fitness_equipment_type.create_time IS '????';

COMMENT ON COLUMN fitness_equipment_type.update_time IS '????';

COMMENT ON COLUMN sys_user.id IS '??ID';

COMMENT ON COLUMN sys_user.username IS '???';

COMMENT ON COLUMN sys_user.password IS '????';

COMMENT ON COLUMN sys_user.phone IS '???';

COMMENT ON COLUMN sys_user.email IS '??';

COMMENT ON COLUMN sys_user.avatar IS '??URL';

COMMENT ON COLUMN sys_user.create_time IS '????';

COMMENT ON COLUMN sys_user.update_time IS '????';

COMMENT ON COLUMN sys_user.points IS '????';

COMMENT ON COLUMN sys_user.nickname IS '????/???????';

COMMENT ON COLUMN sys_role.id IS '??ID';

COMMENT ON COLUMN sys_role.create_time IS '????';

COMMENT ON COLUMN sys_role.update_time IS '????';

COMMENT ON COLUMN sys_user_role.id IS '??ID';

COMMENT ON COLUMN sys_user_role.user_id IS '??ID';

COMMENT ON COLUMN sys_user_role.role_id IS '??ID';

COMMENT ON COLUMN sys_user_role.create_time IS '????';

COMMENT ON COLUMN sys_permission.id IS '??ID';

COMMENT ON COLUMN sys_permission.create_time IS '????';

COMMENT ON COLUMN sys_permission.update_time IS '????';

COMMENT ON COLUMN sys_role_permission.id IS '??ID';

COMMENT ON COLUMN sys_role_permission.role_id IS '??ID';

COMMENT ON COLUMN sys_role_permission.permission_id IS '??ID';

COMMENT ON COLUMN sys_role_permission.create_time IS '????';

COMMENT ON COLUMN sys_file.id IS '??ID';

COMMENT ON COLUMN sys_file.create_time IS '????';

COMMENT ON COLUMN fitness_course.id IS '??ID';

COMMENT ON COLUMN fitness_course.create_time IS '????';

COMMENT ON COLUMN fitness_course.update_time IS '????';

COMMENT ON COLUMN fitness_booking.id IS '??ID';

COMMENT ON COLUMN fitness_booking.create_time IS '????';

COMMENT ON COLUMN fitness_booking.update_time IS '????';

COMMENT ON COLUMN fitness_equipment.id IS '??ID';

COMMENT ON COLUMN fitness_equipment.create_time IS '????';

COMMENT ON COLUMN fitness_equipment.update_time IS '????';

COMMENT ON COLUMN fitness_equipment.type_code IS '????????? fitness_equipment_type.type_code';

COMMENT ON COLUMN fitness_equipment.equipment_no IS '????';

COMMENT ON COLUMN fitness_equipment_repair.id IS '??ID';

COMMENT ON COLUMN fitness_equipment_repair.create_time IS '????';

COMMENT ON COLUMN fitness_equipment_repair.update_time IS '????';

COMMENT ON COLUMN fitness_plan.id IS '??ID';

COMMENT ON COLUMN fitness_plan.create_time IS '????';

COMMENT ON COLUMN fitness_plan.update_time IS '????';

COMMENT ON COLUMN fitness_plan_detail.id IS '??ID';

COMMENT ON COLUMN fitness_plan_detail.create_time IS '????';

COMMENT ON COLUMN fitness_plan_detail.update_time IS '????';

COMMENT ON COLUMN sys_banner.id IS '??ID';

COMMENT ON COLUMN sys_banner.create_time IS '????';

COMMENT ON COLUMN sys_banner.update_time IS '????';

COMMENT ON COLUMN coach_detail.id IS '??ID';

COMMENT ON COLUMN coach_detail.create_time IS '????';

COMMENT ON COLUMN coach_detail.update_time IS '????';

COMMENT ON COLUMN product.id IS '??ID';

COMMENT ON COLUMN product.created_at IS '????';

COMMENT ON COLUMN product.updated_at IS '????';

COMMENT ON COLUMN product.coach_id IS '??????ID???????????';

COMMENT ON COLUMN product_order.id IS '??ID';

COMMENT ON COLUMN product_order.created_at IS '????';

COMMENT ON COLUMN product_order.updated_at IS '????';

COMMENT ON COLUMN product_order.coach_id IS '??????ID??????????';

COMMENT ON COLUMN user_fitness_profile.id IS '??ID';

COMMENT ON COLUMN user_fitness_profile.create_time IS '????';

COMMENT ON COLUMN user_fitness_profile.update_time IS '????';

COMMENT ON COLUMN sys_announcement.id IS '??ID';

COMMENT ON COLUMN sys_announcement.create_time IS '????';

COMMENT ON COLUMN sys_announcement.update_time IS '????';

COMMENT ON COLUMN chat_session.created_at IS '????';

COMMENT ON COLUMN chat_session.updated_at IS '????';

COMMENT ON COLUMN chat_session.is_deleted IS '????';

COMMENT ON COLUMN chat_message.created_at IS '????';

COMMENT ON COLUMN knowledge_category.id IS '??ID';

COMMENT ON COLUMN knowledge_category.create_time IS '????';

COMMENT ON COLUMN knowledge_category.update_time IS '????';

COMMENT ON COLUMN knowledge_document.id IS '??ID';

COMMENT ON COLUMN knowledge_document.create_time IS '????';

COMMENT ON COLUMN knowledge_document.update_time IS '????';

COMMENT ON COLUMN knowledge_chunk.id IS '??ID';

COMMENT ON COLUMN knowledge_chunk.create_time IS '????';

COMMENT ON COLUMN fitness_repair_record.id IS '??ID';

COMMENT ON COLUMN fitness_repair_record.create_time IS '????';

COMMENT ON COLUMN fitness_repair_record.update_time IS '????';

COMMENT ON COLUMN fitness_plan.level IS '????: BEGINNER-??, INTERMEDIATE-??, ADVANCED-??';

COMMENT ON COLUMN membership_card_type.id IS '??ID';

COMMENT ON COLUMN membership_card_type.created_at IS '????';

COMMENT ON COLUMN membership_card_type.updated_at IS '????';

COMMENT ON COLUMN membership_card.id IS '??ID';

COMMENT ON COLUMN membership_card.created_at IS '????';

COMMENT ON COLUMN membership_card.updated_at IS '????';

COMMENT ON COLUMN membership_card.type_code IS '??????????????????';

COMMENT ON COLUMN membership_card_content.id IS '??ID';

COMMENT ON COLUMN membership_card_content.created_at IS '????';

COMMENT ON COLUMN membership_card_content.updated_at IS '????';

COMMENT ON COLUMN membership_order.id IS '??ID';

COMMENT ON COLUMN user_membership.id IS '??ID';

COMMENT ON COLUMN user_membership.created_at IS '????';

COMMENT ON COLUMN user_membership.updated_at IS '????';

COMMENT ON COLUMN sys_dict.id IS '??ID';

COMMENT ON COLUMN sys_dict.created_at IS '????';

COMMENT ON COLUMN sys_dict.updated_at IS '????';

COMMENT ON COLUMN sys_dict_item.id IS '??ID';

COMMENT ON COLUMN sys_dict_item.created_at IS '????';

COMMENT ON COLUMN sys_dict_item.updated_at IS '????';

COMMENT ON COLUMN chat_long_term_memory.id IS '??ID';

COMMENT ON COLUMN chat_long_term_memory.create_time IS '????';

COMMENT ON COLUMN chat_long_term_memory.update_time IS '????';

COMMENT ON COLUMN fitness_video_course.id IS '??ID';

COMMENT ON COLUMN fitness_video_course.create_time IS '????';

COMMENT ON COLUMN fitness_video_course.update_time IS '????';

COMMENT ON COLUMN fitness_course_session.id IS '??ID';

COMMENT ON COLUMN fitness_course_session.create_time IS '????';

COMMENT ON COLUMN fitness_course_session.update_time IS '????';

COMMENT ON COLUMN fitness_course_session.deleted IS '?????';
