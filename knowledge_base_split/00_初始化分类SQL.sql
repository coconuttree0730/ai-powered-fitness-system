-- ==========================================
-- 知识库分类初始化SQL
-- 数据库：PostgreSQL
-- 说明：创建6个知识库分类，用于RAG智能客服系统
-- ==========================================

-- 清空现有分类数据（可选，谨慎使用）
-- TRUNCATE TABLE knowledge_category RESTART IDENTITY CASCADE;

-- 插入知识库分类数据
INSERT INTO knowledge_category (name, code, description, sort_order, deleted, create_time, update_time) VALUES
('场馆基础信息', 'VENUE_INFO', '包含场馆介绍、位置营业时间、联系方式、交通指南、场馆设施等基础信息', 1, false, NOW(), NOW()),
('会员卡与收费政策', 'MEMBERSHIP_POLICY', '包含会员卡类型价格、开卡停卡退卡规则、老带新政策、节日优惠、企业团办等收费政策', 2, false, NOW(), NOW()),
('课程与预约服务', 'COURSE_BOOKING', '包含团课时间表、私教服务内容价格、预约取消爽约规则、课程推荐逻辑', 3, false, NOW(), NOW()),
('场馆使用规范', 'VENUE_RULES', '包含入场要求、器械使用规范、储物柜淋浴间规则、禁止事项、访客规则、安全应急流程', 4, false, NOW(), NOW()),
('常见问题FAQ', 'FAQ', '包含账号登录问题、卡与权益问题、课程预约问题、其他常见问题的解答', 5, false, NOW(), NOW()),
('教练团队介绍', 'COACH_TEAM', '包含教练资质、擅长方向、可预约时间、教练简介、预约方式', 6, false, NOW(), NOW());

-- 查询插入结果
SELECT id, name, code, description, sort_order 
FROM knowledge_category 
WHERE deleted = false 
ORDER BY sort_order;

-- ==========================================
-- 说明：
-- 1. 执行此SQL前，请确保 knowledge_category 表已存在
-- 2. 如果表中有数据，建议先备份或使用 UPDATE 而非 INSERT
-- 3. 分类ID将自动生成（从1开始递增）
-- 4. code字段用于程序中快速识别分类
-- ==========================================
