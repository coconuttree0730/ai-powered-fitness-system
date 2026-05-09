-- 为 coach_detail 表添加姓名、性别、年龄字段
-- 用于教练个人信息的基本信息保存

ALTER TABLE coach_detail
ADD COLUMN IF NOT EXISTS real_name VARCHAR(50) DEFAULT NULL,
ADD COLUMN IF NOT EXISTS gender VARCHAR(20) DEFAULT 'male',
ADD COLUMN IF NOT EXISTS age INTEGER DEFAULT NULL;

COMMENT ON COLUMN coach_detail.real_name IS '真实姓名';
COMMENT ON COLUMN coach_detail.gender IS '性别（male/female/other）';
COMMENT ON COLUMN coach_detail.age IS '年龄';
