-- 商品表新增教练ID字段，用于标记私教套餐商品所属教练
ALTER TABLE product ADD COLUMN IF NOT EXISTS coach_id BIGINT;

COMMENT ON COLUMN product.coach_id IS '关联教练用户ID，仅私教套餐类商品使用';

-- 商品订单表新增教练ID字段，用于记录购买私教套餐时的教练绑定关系
ALTER TABLE product_order ADD COLUMN IF NOT EXISTS coach_id BIGINT;

COMMENT ON COLUMN product_order.coach_id IS '关联教练用户ID，仅私教套餐订单使用';