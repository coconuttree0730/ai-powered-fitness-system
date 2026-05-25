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
