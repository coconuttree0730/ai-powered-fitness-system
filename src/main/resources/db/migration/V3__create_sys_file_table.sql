-- 添加 create_by 列到 sys_file 表
ALTER TABLE sys_file ADD COLUMN IF NOT EXISTS create_by BIGINT;

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_sys_file_create_by ON sys_file(create_by);
CREATE INDEX IF NOT EXISTS idx_sys_file_create_time ON sys_file(create_time);

-- 添加列注释
COMMENT ON COLUMN sys_file.create_by IS '创建人ID';
