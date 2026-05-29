-- 为数据字典表添加逻辑删除字段
ALTER TABLE sys_dict ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE sys_dict_item ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE NOT NULL;

COMMENT ON COLUMN sys_dict.deleted IS '逻辑删除';
COMMENT ON COLUMN sys_dict_item.deleted IS '逻辑删除';

CREATE INDEX IF NOT EXISTS idx_sys_dict_deleted ON sys_dict(deleted);
CREATE INDEX IF NOT EXISTS idx_sys_dict_item_deleted ON sys_dict_item(deleted);

-- 重命名时间列，符合 create_time / update_time 命名规范
ALTER TABLE sys_dict RENAME COLUMN created_at TO create_time;
ALTER TABLE sys_dict RENAME COLUMN updated_at TO update_time;

ALTER TABLE sys_dict_item RENAME COLUMN created_at TO create_time;
ALTER TABLE sys_dict_item RENAME COLUMN updated_at TO update_time;