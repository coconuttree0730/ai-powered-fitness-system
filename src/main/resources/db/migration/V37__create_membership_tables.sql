-- 会员卡类型表
CREATE TABLE membership_card_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE membership_card_type IS '会员卡类型表';
COMMENT ON COLUMN membership_card_type.name IS '类型名称，如：月卡、季卡、年卡';
COMMENT ON COLUMN membership_card_type.code IS '类型编码';
COMMENT ON COLUMN membership_card_type.description IS '类型描述';
COMMENT ON COLUMN membership_card_type.status IS '状态：ACTIVE-启用，INACTIVE-禁用';
COMMENT ON COLUMN membership_card_type.sort_order IS '排序号';

-- 会员卡定义表
CREATE TABLE membership_card (
    id BIGSERIAL PRIMARY KEY,
    type_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    subtitle VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2),
    duration_days INT NOT NULL,
    points_reward INT DEFAULT 0,
    daily_price DECIMAL(10, 2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_recommend BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    cover_image VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_membership_card_type FOREIGN KEY (type_id) REFERENCES membership_card_type(id)
);

COMMENT ON TABLE membership_card IS '会员卡定义表';
COMMENT ON COLUMN membership_card.type_id IS '会员卡类型ID';
COMMENT ON COLUMN membership_card.name IS '会员卡名称';
COMMENT ON COLUMN membership_card.subtitle IS '副标题/简介';
COMMENT ON COLUMN membership_card.price IS '售价';
COMMENT ON COLUMN membership_card.original_price IS '原价';
COMMENT ON COLUMN membership_card.duration_days IS '有效期天数';
COMMENT ON COLUMN membership_card.points_reward IS '购买赠送积分';
COMMENT ON COLUMN membership_card.daily_price IS '日均价格（用于展示）';
COMMENT ON COLUMN membership_card.status IS '状态：ACTIVE-上架，INACTIVE-下架';
COMMENT ON COLUMN membership_card.is_recommend IS '是否推荐';
COMMENT ON COLUMN membership_card.sort_order IS '排序号';
COMMENT ON COLUMN membership_card.cover_image IS '封面图片URL';

-- 会员卡内容项表（动态多条）
CREATE TABLE membership_card_content (
    id BIGSERIAL PRIMARY KEY,
    card_id BIGINT NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    icon VARCHAR(200),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_content_card FOREIGN KEY (card_id) REFERENCES membership_card(id) ON DELETE CASCADE
);

COMMENT ON TABLE membership_card_content IS '会员卡内容项表';
COMMENT ON COLUMN membership_card_content.card_id IS '会员卡ID';
COMMENT ON COLUMN membership_card_content.content_type IS '内容类型：BENEFIT-权益说明, RULE-使用规则, PRIVILEGE-特权列表, OTHER-其他';
COMMENT ON COLUMN membership_card_content.title IS '标题';
COMMENT ON COLUMN membership_card_content.description IS '详细描述';
COMMENT ON COLUMN membership_card_content.icon IS '图标';
COMMENT ON COLUMN membership_card_content.sort_order IS '排序号';

-- 会员卡订单表
CREATE TABLE membership_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(64) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    card_name VARCHAR(200),
    price DECIMAL(10, 2) NOT NULL,
    pay_amount DECIMAL(10, 2),
    pay_method VARCHAR(50),
    pay_time TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(20) DEFAULT 'PENDING',
    expire_time TIMESTAMP WITHOUT TIME ZONE,
    alipay_trade_no VARCHAR(128),
    remark VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_card FOREIGN KEY (card_id) REFERENCES membership_card(id),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

COMMENT ON TABLE membership_order IS '会员卡订单表';
COMMENT ON COLUMN membership_order.order_no IS '订单编号';
COMMENT ON COLUMN membership_order.user_id IS '用户ID';
COMMENT ON COLUMN membership_order.card_id IS '会员卡ID';
COMMENT ON COLUMN membership_order.card_name IS '会员卡名称（快照）';
COMMENT ON COLUMN membership_order.price IS '订单金额';
COMMENT ON COLUMN membership_order.pay_amount IS '实际支付金额';
COMMENT ON COLUMN membership_order.pay_method IS '支付方式：ALIPAY-支付宝, BALANCE-余额';
COMMENT ON COLUMN membership_order.pay_time IS '支付时间';
COMMENT ON COLUMN membership_order.status IS '状态：PENDING-待支付, PAID-已支付, CANCELLED-已取消, TIMEOUT-已超时';
COMMENT ON COLUMN membership_order.expire_time IS '会员到期时间';
COMMENT ON COLUMN membership_order.alipay_trade_no IS '支付宝交易号';
COMMENT ON COLUMN membership_order.remark IS '备注';

-- 用户会员信息表（记录当前会员状态）
CREATE TABLE user_membership (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    membership_type VARCHAR(50),
    start_time TIMESTAMP WITHOUT TIME ZONE,
    expire_time TIMESTAMP WITHOUT TIME ZONE,
    is_active BOOLEAN DEFAULT FALSE,
    total_orders INT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_membership_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

COMMENT ON TABLE user_membership IS '用户会员信息表';
COMMENT ON COLUMN user_membership.user_id IS '用户ID';
COMMENT ON COLUMN user_membership.membership_type IS '当前会员类型';
COMMENT ON COLUMN user_membership.start_time IS '会员开始时间';
COMMENT ON COLUMN user_membership.expire_time IS '会员到期时间';
COMMENT ON COLUMN user_membership.is_active IS '是否有效';
COMMENT ON COLUMN user_membership.total_orders IS '累计购买次数';

-- 给 sys_user 表添加 balance 字段（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'sys_user' AND column_name = 'balance') THEN
        ALTER TABLE sys_user ADD COLUMN balance DECIMAL(10, 2) DEFAULT 0.00;
    END IF;
END $$;

COMMENT ON COLUMN sys_user.balance IS '账户余额';

-- 创建索引
CREATE INDEX idx_membership_card_type_id ON membership_card(type_id);
CREATE INDEX idx_membership_card_status ON membership_card(status);
CREATE INDEX idx_card_content_card_id ON membership_card_content(card_id);
CREATE INDEX idx_membership_order_user_id ON membership_order(user_id);
CREATE INDEX idx_membership_order_status ON membership_order(status);
CREATE INDEX idx_membership_order_no ON membership_order(order_no);

-- 插入默认会员卡类型数据
INSERT INTO membership_card_type (name, code, description, sort_order) VALUES
('月卡', 'MONTHLY', '月度会员卡，享受30天会员权益', 1),
('季卡', 'QUARTERLY', '季度会员卡，享受90天会员权益，更优惠', 2),
('年卡', 'YEARLY', '年度会员卡，享受365天会员权益，最超值', 3),
('体验卡', 'TRIAL', '体验会员卡，限时体验会员权益', 4);

-- 插入示例会员卡数据
INSERT INTO membership_card (type_id, name, subtitle, price, original_price, duration_days, points_reward, daily_price, is_recommend, sort_order) VALUES
(1, '普通月卡', '适合短期体验', 99.00, 129.00, 30, 100, 3.30, FALSE, 1),
(1, '高级月卡', '月卡首选，性价比高', 149.00, 199.00, 30, 200, 4.97, TRUE, 2),
(2, '季度会员卡', '买二送一，超值选择', 299.00, 447.00, 90, 500, 3.32, TRUE, 3),
(3, '年度会员卡', '年度最优惠，日均不到2元', 699.00, 1197.00, 365, 2000, 1.92, TRUE, 4),
(4, '7天体验卡', '新用户专享，限时体验', 9.90, 29.90, 7, 50, 1.41, FALSE, 5);

-- 插入示例会员卡内容
INSERT INTO membership_card_content (card_id, content_type, title, description, sort_order) VALUES
(1, 'BENEFIT', '权益说明', '享受健身房所有器械使用权限；免费参与团课；享受私教课程8折优惠', 1),
(1, 'RULE', '使用规则', '开卡后30天内有效；每日不限次数入场；可预约私教课程', 2),
(1, 'PRIVILEGE', '专属特权', '专属储物柜；免费饮用水；免费毛巾服务', 3),

(2, 'BENEFIT', '权益说明', '享受健身房所有器械使用权限；免费参与所有团课；享受私教课程7折优惠；免费体测一次', 1),
(2, 'RULE', '使用规则', '开卡后30天内有效；每日不限次数入场；可预约私教课程；可带一名亲友免费体验', 2),
(2, 'PRIVILEGE', '专属特权', 'VIP专属储物柜；免费饮用水；免费毛巾服务；免费停车2小时', 3),

(3, 'BENEFIT', '权益说明', '享受健身房所有器械使用权限；免费参与所有团课；享受私教课程6折优惠；免费体测三次', 1),
(3, 'RULE', '使用规则', '开卡后90天内有效；每日不限次数入场；可预约私教课程；每月可带两名亲友免费体验', 2),
(3, 'PRIVILEGE', '专属特权', 'VIP专属储物柜；免费饮用水；免费毛巾服务；免费停车4小时；专属休息区', 3),

(4, 'BENEFIT', '权益说明', '享受健身房所有器械使用权限；免费参与所有团课；享受私教课程5折优惠；免费体测每月一次', 1),
(4, 'RULE', '使用规则', '开卡后365天内有效；每日不限次数入场；无限次预约私教课程；每月可带四名亲友免费体验', 2),
(4, 'PRIVILEGE', '专属特权', 'VIP专属储物柜；免费饮用水；免费毛巾服务；免费停车全天；专属休息区；生日礼包；专属客服', 3),

(5, 'BENEFIT', '权益说明', '享受健身房所有器械使用权限；免费参与基础团课', 1),
(5, 'RULE', '使用规则', '开卡后7天内有效；每日限一次入场；限新用户首次购买', 2),
(5, 'PRIVILEGE', '专属特权', '公共储物柜；免费饮用水', 3);
