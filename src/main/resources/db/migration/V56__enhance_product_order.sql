ALTER TABLE product_order
    ADD COLUMN IF NOT EXISTS pickup_code VARCHAR(20),
    ADD COLUMN IF NOT EXISTS pickup_status VARCHAR(20) DEFAULT 'NOT_PICKED',
    ADD COLUMN IF NOT EXISTS pickup_time TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS expire_time TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS alipay_trade_no VARCHAR(100),
    ADD COLUMN IF NOT EXISTS pickup_type VARCHAR(20) DEFAULT 'IN_STORE';

COMMENT ON COLUMN product_order.pickup_code IS '取货验证码（6位）';
COMMENT ON COLUMN product_order.pickup_status IS '取货状态：NOT_PICKED-未取货，PICKED-已取货';
COMMENT ON COLUMN product_order.pickup_time IS '取货时间';
COMMENT ON COLUMN product_order.expire_time IS '订单过期时间';
COMMENT ON COLUMN product_order.alipay_trade_no IS '支付宝交易号';
COMMENT ON COLUMN product_order.pickup_type IS '取货方式：IN_STORE-到店自取，DELIVERY-物流配送';

CREATE INDEX IF NOT EXISTS idx_product_order_pickup_code ON product_order(pickup_code);
CREATE INDEX IF NOT EXISTS idx_product_order_expire_time ON product_order(expire_time);