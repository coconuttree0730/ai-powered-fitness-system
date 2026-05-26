-- V52: 修复 membership_order 表缺少 deleted 列的问题
-- MembershipOrder 实体继承 BaseEntity，BaseEntity 有 @TableLogic 注解 deleted 字段
-- 但 membership_order 建表时遗漏了 deleted 列，导致 MyBatis-Plus 自动添加 WHERE deleted=false 时报错

ALTER TABLE membership_order ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE NOT NULL;

COMMENT ON COLUMN membership_order.deleted IS '逻辑删除标志';
