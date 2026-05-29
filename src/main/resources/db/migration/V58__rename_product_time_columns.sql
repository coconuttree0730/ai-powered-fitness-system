-- ========================================================
-- V58：修正 product 和 product_order 表时间字段命名
-- created_at → create_time, updated_at → update_time
-- 与项目中其他 24 张表的命名约定统一
-- ========================================================

ALTER TABLE product RENAME COLUMN created_at TO create_time;
ALTER TABLE product RENAME COLUMN updated_at TO update_time;

ALTER TABLE product_order RENAME COLUMN created_at TO create_time;
ALTER TABLE product_order RENAME COLUMN updated_at TO update_time;

DROP INDEX IF EXISTS idx_product_order_created_at;
CREATE INDEX idx_product_order_create_time ON product_order(create_time);