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
