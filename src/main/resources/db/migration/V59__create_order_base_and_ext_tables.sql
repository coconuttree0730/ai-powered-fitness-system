-- ========================================================
-- V59：创建 orders 基表 + 扩展表 + 迁移现有订单数据
-- 采用基表+扩展表（Class Table Inheritance）模式
-- ========================================================

-- 1. 创建 orders 基表
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(64) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    order_type VARCHAR(30) NOT NULL,
    original_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    pay_amount DECIMAL(10,2) NOT NULL,
    pay_method VARCHAR(50),
    pay_time TIMESTAMP WITHOUT TIME ZONE,
    alipay_trade_no VARCHAR(128),
    status VARCHAR(20) DEFAULT 'PENDING' NOT NULL,
    remark VARCHAR(500),
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE orders IS '统一订单基表';
COMMENT ON COLUMN orders.order_no IS '统一订单编号';
COMMENT ON COLUMN orders.user_id IS '用户ID';
COMMENT ON COLUMN orders.order_type IS '订单类型：PRODUCT-商品，COACH_PACKAGE-私教套餐，MEMBERSHIP-会员卡';
COMMENT ON COLUMN orders.original_amount IS '原始金额';
COMMENT ON COLUMN orders.discount_amount IS '优惠金额';
COMMENT ON COLUMN orders.pay_amount IS '实付金额';
COMMENT ON COLUMN orders.pay_method IS '支付方式：ALIPAY-支付宝，BALANCE-余额';
COMMENT ON COLUMN orders.pay_time IS '支付时间';
COMMENT ON COLUMN orders.alipay_trade_no IS '支付宝交易号';
COMMENT ON COLUMN orders.status IS '状态：PENDING-待支付，PAID-已支付，COMPLETED-已完成，CANCELLED-已取消，TIMEOUT-已超时';
COMMENT ON COLUMN orders.remark IS '备注';

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_type ON orders(order_type);
CREATE INDEX idx_orders_create_time ON orders(create_time);

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id);

CREATE TRIGGER update_orders_update_time
    BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

-- 2. 创建 product_order_ext 扩展表（商品订单专属字段）
CREATE TABLE product_order_ext (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT UNIQUE NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100),
    quantity INT DEFAULT 1,
    points_used INT DEFAULT 0,
    points_discount DECIMAL(10,2) DEFAULT 0,
    tracking_no VARCHAR(100),
    carrier VARCHAR(50),
    address TEXT,
    pickup_type VARCHAR(20) DEFAULT 'IN_STORE',
    pickup_code VARCHAR(20),
    pickup_status VARCHAR(20) DEFAULT 'NOT_PICKED',
    pickup_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_poe_order_id FOREIGN KEY (order_id) REFERENCES orders(id)
);

COMMENT ON TABLE product_order_ext IS '商品订单扩展表';
COMMENT ON COLUMN product_order_ext.order_id IS '关联 orders.id';
COMMENT ON COLUMN product_order_ext.product_id IS '商品ID';
COMMENT ON COLUMN product_order_ext.product_name IS '商品名称快照';
COMMENT ON COLUMN product_order_ext.quantity IS '购买数量';
COMMENT ON COLUMN product_order_ext.points_used IS '使用积分数量';
COMMENT ON COLUMN product_order_ext.points_discount IS '积分抵扣金额';
COMMENT ON COLUMN product_order_ext.tracking_no IS '物流单号';
COMMENT ON COLUMN product_order_ext.carrier IS '物流公司';
COMMENT ON COLUMN product_order_ext.address IS '收货地址';
COMMENT ON COLUMN product_order_ext.pickup_type IS '取货方式：IN_STORE-到店自取，DELIVERY-物流配送';
COMMENT ON COLUMN product_order_ext.pickup_code IS '取货验证码';
COMMENT ON COLUMN product_order_ext.pickup_status IS '取货状态：NOT_PICKED-未取货，PICKED-已取货';
COMMENT ON COLUMN product_order_ext.pickup_time IS '取货时间';

CREATE INDEX idx_poe_product_id ON product_order_ext(product_id);

-- 3. 创建 coach_package_order_ext 扩展表（私教套餐订单专属字段）
CREATE TABLE coach_package_order_ext (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT UNIQUE NOT NULL,
    coach_package_id BIGINT,
    coach_id BIGINT NOT NULL,
    package_name VARCHAR(100),
    expire_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_cpoe_order_id FOREIGN KEY (order_id) REFERENCES orders(id)
);

COMMENT ON TABLE coach_package_order_ext IS '私教套餐订单扩展表';
COMMENT ON COLUMN coach_package_order_ext.order_id IS '关联 orders.id';
COMMENT ON COLUMN coach_package_order_ext.coach_package_id IS '私教套餐ID（关联 coach_package 表，V60 迁移后回填）';
COMMENT ON COLUMN coach_package_order_ext.coach_id IS '教练ID';
COMMENT ON COLUMN coach_package_order_ext.package_name IS '套餐名称快照';
COMMENT ON COLUMN coach_package_order_ext.expire_time IS '订单过期时间（超时取消用）';

CREATE INDEX idx_cpoe_coach_id ON coach_package_order_ext(coach_id);

-- 4. 创建 membership_order_ext 扩展表（会员卡订单专属字段）
CREATE TABLE membership_order_ext (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT UNIQUE NOT NULL,
    card_id BIGINT NOT NULL,
    card_name VARCHAR(200),
    expire_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_moe_order_id FOREIGN KEY (order_id) REFERENCES orders(id)
);

COMMENT ON TABLE membership_order_ext IS '会员卡订单扩展表';
COMMENT ON COLUMN membership_order_ext.order_id IS '关联 orders.id';
COMMENT ON COLUMN membership_order_ext.card_id IS '会员卡ID';
COMMENT ON COLUMN membership_order_ext.card_name IS '会员卡名称快照';
COMMENT ON COLUMN membership_order_ext.expire_time IS '会员到期时间';

CREATE INDEX idx_moe_card_id ON membership_order_ext(card_id);

-- 5. 迁移 membership_order 数据 → orders + membership_order_ext
INSERT INTO orders (order_no, user_id, order_type, original_amount, discount_amount, pay_amount, pay_method, pay_time, alipay_trade_no, status, remark, create_time, update_time)
SELECT
    order_no, user_id, 'MEMBERSHIP',
    COALESCE(price, 0), 0, COALESCE(pay_amount, price, 0),
    pay_method, pay_time, alipay_trade_no,
    status, remark,
    COALESCE(create_time, CURRENT_TIMESTAMP),
    COALESCE(update_time, CURRENT_TIMESTAMP)
FROM membership_order;

INSERT INTO membership_order_ext (order_id, card_id, card_name, expire_time)
SELECT
    o.id, mo.card_id, mo.card_name, mo.expire_time
FROM membership_order mo
JOIN orders o ON o.order_no = mo.order_no;

-- 6. 迁移 product_order（coach_id IS NULL，即商品订单）→ orders + product_order_ext
INSERT INTO orders (order_no, user_id, order_type, original_amount, discount_amount, pay_amount, pay_method, pay_time, alipay_trade_no, status, remark, create_time, update_time)
SELECT
    order_no, user_id, 'PRODUCT',
    COALESCE(original_price, 0), COALESCE(points_discount, 0), COALESCE(pay_amount, final_price, 0),
    pay_method, pay_time, alipay_trade_no,
    status, remark,
    COALESCE(create_time, CURRENT_TIMESTAMP),
    COALESCE(update_time, CURRENT_TIMESTAMP)
FROM product_order
WHERE coach_id IS NULL;

INSERT INTO product_order_ext (order_id, product_id, product_name, quantity, points_used, points_discount, tracking_no, carrier, address, pickup_type, pickup_code, pickup_status, pickup_time)
SELECT
    o.id, po.product_id, po.product_name, po.quantity, COALESCE(po.points_used, 0), COALESCE(po.points_discount, 0),
    po.tracking_no, po.carrier, po.address,
    COALESCE(po.pickup_type, 'IN_STORE'), po.pickup_code, COALESCE(po.pickup_status, 'NOT_PICKED'), po.pickup_time
FROM product_order po
JOIN orders o ON o.order_no = po.order_no
WHERE po.coach_id IS NULL;

-- 7. 备份旧表（重命名，暂不删除，V60 完成后再清理）
ALTER TABLE product_order RENAME TO product_order_old;
ALTER TABLE membership_order RENAME TO membership_order_old;