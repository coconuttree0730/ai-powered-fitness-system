-- 为 membership_card 表添加 type_code 字段
ALTER TABLE membership_card ADD COLUMN IF NOT EXISTS type_code VARCHAR(50);

COMMENT ON COLUMN membership_card.type_code IS '会员卡类型编码（冗余字段，便于查询）';

-- 根据现有 type_id 回填 type_code
UPDATE membership_card mc
SET type_code = mct.code
FROM membership_card_type mct
WHERE mc.type_id = mct.id;

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_membership_card_type_code ON membership_card(type_code);
