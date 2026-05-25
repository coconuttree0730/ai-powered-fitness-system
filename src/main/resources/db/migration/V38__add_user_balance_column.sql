-- 为用户表添加余额字段（用于余额支付）
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS balance DECIMAL(10, 2) DEFAULT 0.00;

COMMENT ON COLUMN sys_user.balance IS '账户余额';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_balance ON sys_user(balance);
