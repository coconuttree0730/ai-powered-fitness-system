-- ========================================================
-- 智能健身系统数据库迁移脚本
-- 版本：V57
-- 说明：新增套餐编码数据字典、product 扩展字段、coach_student 绑定表、coach_notification 通知表
-- ========================================================

-- 1. 新增数据字典：套餐编码管理
INSERT INTO sys_dict (dict_name, dict_code, description, sort_order) VALUES
('套餐编码管理', 'coach_package_code', '私教套餐类型编码，供教练端创建套餐时选择', 5);

WITH pkg_dict AS (
    SELECT id FROM sys_dict WHERE dict_code = 'coach_package_code' LIMIT 1
)
INSERT INTO sys_dict_item (dict_id, label, value, description, sort_order)
SELECT d.id, t.label, t.value, t.description, t.sort_order
FROM pkg_dict d CROSS JOIN (
    VALUES
        ('一对一私教', 'PT_1V1', '一对一专属私教课程，量身定制训练计划', 1),
        ('小团课私教', 'PT_GROUP', '3-5人小团课，互动氛围好，性价比高', 2),
        ('线上指导', 'PT_ONLINE', '远程线上训练指导与饮食建议', 3),
        ('包月训练', 'PT_MONTHLY', '包月无限次训练，适合长期坚持', 4),
        ('综合训练营', 'PT_COMPREHENSIVE', '全方位训练营，含体测+饮食+训练计划', 5)
) AS t(label, value, description, sort_order);

-- 2. Product 表新增套餐扩展字段
ALTER TABLE product
    ADD COLUMN IF NOT EXISTS package_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS total_sessions INT,
    ADD COLUMN IF NOT EXISTS validity_days INT;

COMMENT ON COLUMN product.package_code IS '套餐编码，关联数据字典 coach_package_code';
COMMENT ON COLUMN product.total_sessions IS '总课时数';
COMMENT ON COLUMN product.validity_days IS '有效期天数，用于计算 coach_student.expire_time';

-- 3. 教练-学员绑定表
CREATE TABLE coach_student (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    coach_id BIGINT NOT NULL,
    product_id BIGINT,
    package_code VARCHAR(50),
    bind_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expire_time TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE coach_student IS '教练-学员绑定表';
COMMENT ON COLUMN coach_student.member_id IS '学员用户ID（关联 sys_user）';
COMMENT ON COLUMN coach_student.coach_id IS '教练用户ID（关联 sys_user）';
COMMENT ON COLUMN coach_student.product_id IS '购买的套餐商品ID（关联 product）';
COMMENT ON COLUMN coach_student.package_code IS '套餐编码（快照）';
COMMENT ON COLUMN coach_student.bind_time IS '绑定时间（支付成功时间）';
COMMENT ON COLUMN coach_student.expire_time IS '绑定到期时间';
COMMENT ON COLUMN coach_student.status IS '状态：ACTIVE-有效，EXPIRED-已过期';

CREATE INDEX idx_cs_coach_id ON coach_student(coach_id);
CREATE INDEX idx_cs_member_id ON coach_student(member_id);
CREATE INDEX idx_cs_coach_status ON coach_student(coach_id, status) WHERE deleted = false;
CREATE UNIQUE INDEX uk_cs_member_coach_active
    ON coach_student(member_id, coach_id)
    WHERE status = 'ACTIVE' AND deleted = false;

ALTER TABLE coach_student
    ADD CONSTRAINT fk_cs_member_id FOREIGN KEY (member_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_cs_coach_id FOREIGN KEY (coach_id) REFERENCES sys_user(id) ON DELETE CASCADE;

CREATE TRIGGER update_coach_student_update_time
    BEFORE UPDATE ON coach_student
    FOR EACH ROW
    EXECUTE FUNCTION update_update_time_column();

-- 4. 教练通知表
CREATE TABLE coach_notification (
    id BIGSERIAL PRIMARY KEY,
    coach_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    booking_id BIGINT,
    type VARCHAR(50) DEFAULT 'BOOKING' NOT NULL,
    content VARCHAR(500) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE NOT NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE coach_notification IS '教练通知表';
COMMENT ON COLUMN coach_notification.coach_id IS '教练用户ID';
COMMENT ON COLUMN coach_notification.student_id IS '学员用户ID';
COMMENT ON COLUMN coach_notification.booking_id IS '关联预约ID';
COMMENT ON COLUMN coach_notification.type IS '通知类型：BOOKING-预约提醒';
COMMENT ON COLUMN coach_notification.content IS '通知内容摘要';
COMMENT ON COLUMN coach_notification.is_read IS '是否已读';

CREATE INDEX idx_cn_coach_id ON coach_notification(coach_id);
CREATE INDEX idx_cn_coach_unread ON coach_notification(coach_id, is_read) WHERE is_read = false AND deleted = false;
CREATE INDEX idx_cn_create_time ON coach_notification(create_time DESC);

ALTER TABLE coach_notification
    ADD CONSTRAINT fk_cn_coach_id FOREIGN KEY (coach_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_cn_student_id FOREIGN KEY (student_id) REFERENCES sys_user(id) ON DELETE CASCADE;

CREATE TRIGGER update_coach_notification_update_time
    BEFORE UPDATE ON coach_notification
    FOR EACH ROW
    EXECUTE FUNCTION update_update_time_column();