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

-- 插入会员卡类型字典
INSERT INTO sys_dict (dict_name, dict_code, description, sort_order) VALUES
('会员卡类型', 'membership_card_type', '会员卡类型定义，用于会员卡管理', 1);

-- 获取刚插入的字典ID（使用CTE）
WITH card_type_dict AS (
    SELECT id FROM sys_dict WHERE dict_code = 'membership_card_type' LIMIT 1
)
INSERT INTO sys_dict_item (dict_id, label, value, description, sort_order)
SELECT d.id, name, code, description, sort_order
FROM card_type_dict d CROSS JOIN (SELECT name, code, description, sort_order FROM membership_card_type WHERE status = 'ACTIVE') AS t;

-- 插入知识库分类字典
INSERT INTO sys_dict (dict_name, dict_code, description, sort_order) VALUES
('知识库分类', 'knowledge_category', '知识文档分类管理', 2);

WITH category_dict AS (
    SELECT id FROM sys_dict WHERE dict_code = 'knowledge_category' LIMIT 1
)
INSERT INTO sys_dict_item (dict_id, label, value, description, sort_order)
SELECT d.id, name, code, COALESCE(description, ''), COALESCE(sort_order, 0)
FROM category_dict d CROSS JOIN (SELECT name, code, description, sort_order FROM knowledge_category WHERE deleted = false) AS t;
