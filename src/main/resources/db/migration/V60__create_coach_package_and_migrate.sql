-- ========================================================
-- V60：创建 coach_package 表 + 迁移产品私教数据 + 迁移私教套餐订单
-- ========================================================

-- 1. 创建 coach_package 表
CREATE TABLE coach_package (
    id BIGSERIAL PRIMARY KEY,
    coach_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    package_code VARCHAR(50),
    description TEXT,
    cover_image VARCHAR(500),
    original_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_sessions INT,
    validity_days INT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE coach_package IS '私教套餐表';
COMMENT ON COLUMN coach_package.coach_id IS '所属教练用户ID';
COMMENT ON COLUMN coach_package.name IS '套餐名称';
COMMENT ON COLUMN coach_package.package_code IS '套餐类型编码';
COMMENT ON COLUMN coach_package.description IS '套餐描述';
COMMENT ON COLUMN coach_package.cover_image IS '封面图片URL';
COMMENT ON COLUMN coach_package.original_price IS '套餐价格';
COMMENT ON COLUMN coach_package.total_sessions IS '总课时数';
COMMENT ON COLUMN coach_package.validity_days IS '有效期天数';
COMMENT ON COLUMN coach_package.status IS '状态：ACTIVE-上架，INACTIVE-下架';
COMMENT ON COLUMN coach_package.sort_order IS '排序号';

CREATE INDEX idx_cp_coach_id ON coach_package(coach_id);
CREATE INDEX idx_cp_status ON coach_package(status);
CREATE INDEX idx_cp_package_code ON coach_package(package_code);

ALTER TABLE coach_package
    ADD CONSTRAINT fk_cp_coach_id FOREIGN KEY (coach_id) REFERENCES sys_user(id);

CREATE TRIGGER update_coach_package_update_time
    BEFORE UPDATE ON coach_package
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

-- 2. 迁移 product 表中私教套餐数据（coach_id IS NOT NULL）→ coach_package
INSERT INTO coach_package (coach_id, name, package_code, description, cover_image, original_price, total_sessions, validity_days, status, sort_order, create_time, update_time)
SELECT
    coach_id, name, package_code, description, image_url,
    original_price, total_sessions, validity_days, status, sort_order,
    COALESCE(create_time, CURRENT_TIMESTAMP),
    COALESCE(update_time, CURRENT_TIMESTAMP)
FROM product
WHERE coach_id IS NOT NULL;

-- 3. 迁移 product_order_old 中私教套餐订单 → orders + coach_package_order_ext
-- 回填 coach_package_id（通过 coach_id + name + price 匹配）
INSERT INTO orders (order_no, user_id, order_type, original_amount, discount_amount, pay_amount, pay_method, pay_time, alipay_trade_no, status, remark, create_time, update_time)
SELECT
    order_no, user_id, 'COACH_PACKAGE',
    COALESCE(original_price, 0), COALESCE(points_discount, 0), COALESCE(pay_amount, final_price, 0),
    pay_method, pay_time, alipay_trade_no,
    status, remark,
    COALESCE(create_time, CURRENT_TIMESTAMP),
    COALESCE(update_time, CURRENT_TIMESTAMP)
FROM product_order_old
WHERE coach_id IS NOT NULL;

INSERT INTO coach_package_order_ext (order_id, coach_package_id, coach_id, package_name, expire_time)
SELECT
    o.id, cp.id, po.coach_id, po.product_name, po.expire_time
FROM product_order_old po
JOIN orders o ON o.order_no = po.order_no
JOIN coach_package cp ON cp.coach_id = po.coach_id AND cp.name = po.product_name AND cp.original_price = po.original_price
WHERE po.coach_id IS NOT NULL;

-- 4. 更新 coach_student 表的 product_id 引用 → coach_package_id
-- 先添加新字段
ALTER TABLE coach_student ADD COLUMN IF NOT EXISTS coach_package_id BIGINT;

-- 回填 coach_package_id
UPDATE coach_student cs
SET coach_package_id = cp.id
FROM coach_package cp, product p
WHERE p.id = cs.product_id
  AND p.coach_id = cp.coach_id
  AND p.name = cp.name
  AND p.original_price = cp.original_price
  AND cs.product_id IS NOT NULL;

-- 删除旧的 product_id 外键约束（如果存在）
ALTER TABLE coach_student DROP CONSTRAINT IF EXISTS fk_cs_product_id;

-- product_id 暂时保留但不再使用（后续 Java 代码不再引用）

-- 5. 删除 product 表中已迁移的私教套餐行
DELETE FROM product WHERE coach_id IS NOT NULL;

-- 6. 删除 product 表私教专属字段
ALTER TABLE product
    DROP COLUMN IF EXISTS coach_id,
    DROP COLUMN IF EXISTS package_code,
    DROP COLUMN IF EXISTS total_sessions,
    DROP COLUMN IF EXISTS validity_days;

-- 7. 删除 product_order_old 中的 coach_id 字段（已在扩展表迁移中处理）
ALTER TABLE product_order_old DROP COLUMN IF EXISTS coach_id;