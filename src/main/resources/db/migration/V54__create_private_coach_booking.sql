-- ========================================================
-- 智能健身系统数据库迁移脚本
-- 版本：V54
-- 说明：创建私教课程预约表（与公开课预约表 fitness_booking 完全独立）
-- ========================================================

CREATE TABLE fitness_private_coach_booking (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coach_id BIGINT NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    note VARCHAR(500),
    status SMALLINT DEFAULT 0 NOT NULL,
    cancel_reason VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE fitness_private_coach_booking IS '私教课程预约表';
COMMENT ON COLUMN fitness_private_coach_booking.user_id IS '会员用户ID';
COMMENT ON COLUMN fitness_private_coach_booking.coach_id IS '教练用户ID';
COMMENT ON COLUMN fitness_private_coach_booking.booking_date IS '预约上课日期';
COMMENT ON COLUMN fitness_private_coach_booking.start_time IS '上课开始时间';
COMMENT ON COLUMN fitness_private_coach_booking.end_time IS '上课结束时间';
COMMENT ON COLUMN fitness_private_coach_booking.note IS '会员备注';
COMMENT ON COLUMN fitness_private_coach_booking.status IS '状态：0-待确认，1-已确认，2-已取消，3-已完成';
COMMENT ON COLUMN fitness_private_coach_booking.cancel_reason IS '取消原因';
COMMENT ON COLUMN fitness_private_coach_booking.deleted IS '软删除标识';

CREATE INDEX idx_pcb_user_id ON fitness_private_coach_booking(user_id);
CREATE INDEX idx_pcb_coach_id ON fitness_private_coach_booking(coach_id);
CREATE INDEX idx_pcb_booking_date ON fitness_private_coach_booking(booking_date);
CREATE INDEX idx_pcb_coach_date ON fitness_private_coach_booking(coach_id, booking_date);
CREATE UNIQUE INDEX uk_pcb_user_coach_date_active
    ON fitness_private_coach_booking (user_id, coach_id, booking_date)
    WHERE status IN (0, 1) AND deleted = false;

ALTER TABLE fitness_private_coach_booking
    ADD CONSTRAINT fk_pcb_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_pcb_coach_id FOREIGN KEY (coach_id) REFERENCES sys_user(id) ON DELETE CASCADE;

CREATE TRIGGER update_fitness_private_coach_booking_update_time
    BEFORE UPDATE ON fitness_private_coach_booking
    FOR EACH ROW
    EXECUTE FUNCTION update_update_time_column();