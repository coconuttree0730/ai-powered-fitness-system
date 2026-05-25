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
