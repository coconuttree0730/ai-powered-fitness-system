

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
