-- 商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE,
    category VARCHAR(50) NOT NULL,
    image_url VARCHAR(500),
    description TEXT,
    original_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    points_discount_type VARCHAR(20) DEFAULT 'FIXED',
    points_discount_value DECIMAL(10, 2) DEFAULT 0,
    max_points_discount DECIMAL(10, 2) DEFAULT 0,
    stock INTEGER NOT NULL DEFAULT 0,
    sales INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_hot BOOLEAN DEFAULT FALSE,
    is_new BOOLEAN DEFAULT FALSE,
    is_recommend BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE product IS '商品表';
COMMENT ON COLUMN product.name IS '商品名称';
COMMENT ON COLUMN product.code IS '商品编号';
COMMENT ON COLUMN product.category IS '商品分类：EQUIPMENT-运动装备，SUPPLEMENT-营养补剂，COURSE-课程优惠，OTHER-其他';
COMMENT ON COLUMN product.image_url IS '商品图片 URL';
COMMENT ON COLUMN product.description IS '商品描述';
COMMENT ON COLUMN product.original_price IS '商品原价';
COMMENT ON COLUMN product.points_discount_type IS '积分抵扣类型：FIXED-固定金额，PERCENT-比例，NONE-不支持';
COMMENT ON COLUMN product.points_discount_value IS '积分抵扣值 (固定金额或比例)';
COMMENT ON COLUMN product.max_points_discount IS '最大积分抵扣金额';
COMMENT ON COLUMN product.stock IS '库存数量';
COMMENT ON COLUMN product.sales IS '销量';
COMMENT ON COLUMN product.status IS '状态：ACTIVE-上架，INACTIVE-下架';
COMMENT ON COLUMN product.is_hot IS '是否热销';
COMMENT ON COLUMN product.is_new IS '是否新品';
COMMENT ON COLUMN product.is_recommend IS '是否推荐';
COMMENT ON COLUMN product.sort_order IS '排序';

-- 商品订单表
CREATE TABLE IF NOT EXISTS product_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    original_price DECIMAL(10, 2) NOT NULL,
    points_used INTEGER DEFAULT 0,
    points_discount DECIMAL(10, 2) DEFAULT 0,
    final_price DECIMAL(10, 2) NOT NULL,
    pay_amount DECIMAL(10, 2) NOT NULL,
    pay_method VARCHAR(50),
    pay_time TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(20) DEFAULT 'PENDING',
    tracking_no VARCHAR(100),
    carrier VARCHAR(50),
    address TEXT,
    remark TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE product_order IS '商品订单表';
COMMENT ON COLUMN product_order.order_no IS '订单编号';
COMMENT ON COLUMN product_order.user_id IS '用户 ID';
COMMENT ON COLUMN product_order.product_id IS '商品 ID';
COMMENT ON COLUMN product_order.product_name IS '商品名称';
COMMENT ON COLUMN product_order.quantity IS '购买数量';
COMMENT ON COLUMN product_order.original_price IS '商品原价';
COMMENT ON COLUMN product_order.points_used IS '使用积分数量';
COMMENT ON COLUMN product_order.points_discount IS '积分抵扣金额';
COMMENT ON COLUMN product_order.final_price IS '最终支付价格';
COMMENT ON COLUMN product_order.pay_amount IS '实际支付金额';
COMMENT ON COLUMN product_order.pay_method IS '支付方式';
COMMENT ON COLUMN product_order.pay_time IS '支付时间';
COMMENT ON COLUMN product_order.status IS '订单状态：PENDING-待支付，PAID-已支付，PROCESSING-处理中，SHIPPED-已发货，COMPLETED-已完成，CANCELLED-已取消';
COMMENT ON COLUMN product_order.tracking_no IS '物流单号';
COMMENT ON COLUMN product_order.carrier IS '物流公司';
COMMENT ON COLUMN product_order.address IS '收货地址';
COMMENT ON COLUMN product_order.remark IS '备注';

CREATE INDEX idx_product_order_user_id ON product_order(user_id);
CREATE INDEX idx_product_order_status ON product_order(status);
CREATE INDEX idx_product_order_created_at ON product_order(created_at);

-- 初始化商品数据
INSERT INTO product (name, code, category, description, original_price, points_discount_type, points_discount_value, max_points_discount, stock, sales, status, is_hot, is_new, is_recommend, sort_order) VALUES
('运动水壶 750ml', 'YP-001', 'EQUIPMENT', '大容量防漏设计，适合健身房使用', 68.00, 'FIXED', 20.00, 20.00, 156, 89, 'ACTIVE', true, false, true, 10),
('乳清蛋白粉 2 磅', 'SP-001', 'SUPPLEMENT', '高纯度乳清蛋白，快速补充蛋白质', 398.00, 'PERCENT', 10.00, 39.80, 88, 156, 'ACTIVE', true, false, true, 9),
('专业瑜伽垫', 'YP-002', 'EQUIPMENT', '防滑加厚环保材质，舒适耐用', 128.00, 'FIXED', 30.00, 30.00, 200, 45, 'ACTIVE', true, true, true, 8),
('私教体验课 1 节', 'CK-001', 'COURSE', '一对一专业指导，定制训练计划', 299.00, 'FIXED', 50.00, 50.00, 50, 23, 'ACTIVE', true, false, true, 7),
('健身背包', 'YP-003', 'EQUIPMENT', '干湿分离大容量，多隔层设计', 168.00, 'FIXED', 25.00, 25.00, 120, 67, 'ACTIVE', false, true, false, 6),
('复合维生素 60 粒', 'SP-002', 'SUPPLEMENT', '全面营养补充，每日一粒', 128.00, 'FIXED', 15.00, 15.00, 95, 34, 'ACTIVE', false, true, false, 5),
('月卡抵扣券 ¥50', 'CK-002', 'COURSE', '购买月卡立减 50 元', 50.00, 'NONE', 0, 0, 100, 89, 'ACTIVE', false, false, true, 4),
('健身手套', 'YP-004', 'EQUIPMENT', '透气防滑护掌，保护双手', 58.00, 'FIXED', 10.00, 10.00, 180, 234, 'ACTIVE', false, false, false, 3),
('跑步机润滑油', 'YP-005', 'EQUIPMENT', '专业跑步机保养润滑油', 38.00, 'NONE', 0, 0, 60, 12, 'ACTIVE', false, false, false, 2),
('能量棒 12 支装', 'SP-003', 'SUPPLEMENT', '运动前后能量补充', 88.00, 'FIXED', 10.00, 10.00, 150, 78, 'ACTIVE', false, true, false, 1)
ON CONFLICT (code) DO NOTHING;
