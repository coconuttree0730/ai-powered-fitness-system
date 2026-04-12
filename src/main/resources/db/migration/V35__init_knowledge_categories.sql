-- ==========================================
-- 知识库分类初始化脚本
-- 版本: V35
-- 说明: 创建6个知识库分类，用于RAG智能客服系统
-- 作者: AI Assistant
-- 日期: 2026-04-12
-- ==========================================

-- 插入知识库分类数据（使用INSERT IGNORE语义，避免重复插入）
-- 注意：使用DO NOTHING确保幂等性，多次执行不会报错

INSERT INTO knowledge_category (name, code, description, sort_order, deleted, create_time, update_time)
VALUES 
    ('场馆基础信息', 'VENUE_INFO', '包含场馆介绍、位置营业时间、联系方式、交通指南、场馆设施等基础信息', 1, false, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO knowledge_category (name, code, description, sort_order, deleted, create_time, update_time)
VALUES 
    ('会员卡与收费政策', 'MEMBERSHIP_POLICY', '包含会员卡类型价格、开卡停卡退卡规则、老带新政策、节日优惠、企业团办等收费政策', 2, false, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO knowledge_category (name, code, description, sort_order, deleted, create_time, update_time)
VALUES 
    ('课程与预约服务', 'COURSE_BOOKING', '包含团课时间表、私教服务内容价格、预约取消爽约规则、课程推荐逻辑', 3, false, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO knowledge_category (name, code, description, sort_order, deleted, create_time, update_time)
VALUES 
    ('场馆使用规范', 'VENUE_RULES', '包含入场要求、器械使用规范、储物柜淋浴间规则、禁止事项、访客规则、安全应急流程', 4, false, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO knowledge_category (name, code, description, sort_order, deleted, create_time, update_time)
VALUES 
    ('常见问题FAQ', 'FAQ', '包含账号登录问题、卡与权益问题、课程预约问题、其他常见问题的解答', 5, false, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO knowledge_category (name, code, description, sort_order, deleted, create_time, update_time)
VALUES 
    ('教练团队介绍', 'COACH_TEAM', '包含教练资质、擅长方向、可预约时间、教练简介、预约方式', 6, false, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- ==========================================
-- 验证插入结果
-- ==========================================
-- 查询当前分类数据（调试用，生产环境可注释掉）
-- SELECT id, name, code, description, sort_order 
-- FROM knowledge_category 
-- WHERE deleted = false 
-- ORDER BY sort_order;

-- ==========================================
-- 使用说明：
-- 1. 此脚本使用 ON CONFLICT (code) DO NOTHING 确保幂等性
-- 2. 如果分类已存在（code重复），则跳过插入
-- 3. 重启应用后 Flyway 会自动执行此脚本
-- 4. 分类ID由数据库自动生成（通常从1开始递增）
-- ==========================================
