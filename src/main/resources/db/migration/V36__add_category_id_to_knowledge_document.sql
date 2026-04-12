-- ==========================================
-- 恢复知识库文档分类字段
-- 版本: V36
-- 说明: 为 knowledge_document 表添加 category_id 字段
-- 原因: V22 误删了该字段，现恢复以支持文档分类功能
-- 作者: AI Assistant
-- 日期: 2026-04-12
-- ==========================================

-- 1. 添加 category_id 字段（如果不存在）
ALTER TABLE knowledge_document 
ADD COLUMN IF NOT EXISTS category_id BIGINT;

-- 2. 添加外键约束（先删除可能存在的旧约束，再添加）
ALTER TABLE knowledge_document 
DROP CONSTRAINT IF EXISTS fk_knowledge_document_category_id;

ALTER TABLE knowledge_document 
ADD CONSTRAINT fk_knowledge_document_category_id 
FOREIGN KEY (category_id) REFERENCES knowledge_category(id) ON DELETE SET NULL;

-- 3. 添加索引（如果不存在）
CREATE INDEX IF NOT EXISTS idx_knowledge_document_category_id 
ON knowledge_document(category_id);

-- 4. 添加字段注释
COMMENT ON COLUMN knowledge_document.category_id IS '分类ID，关联 knowledge_category 表';

-- ==========================================
-- 验证脚本
-- ==========================================
-- 查询表结构验证
-- SELECT column_name, data_type, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'knowledge_document' 
-- AND column_name = 'category_id';

-- 查询外键约束验证
-- SELECT tc.constraint_name, tc.table_name, kcu.column_name, ccu.table_name AS foreign_table_name
-- FROM information_schema.table_constraints AS tc
-- JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name
-- JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name
-- WHERE tc.constraint_type = 'FOREIGN KEY' 
-- AND tc.table_name = 'knowledge_document';

-- ==========================================
-- 使用说明：
-- 1. 此脚本恢复 V22 误删的 category_id 字段
-- 2. 使用 IF NOT EXISTS 和 DROP IF EXISTS 确保幂等性
-- 3. 外键设置 ON DELETE SET NULL，删除分类时文档变为未分类
-- 4. 重启应用后 Flyway 会自动执行此脚本
-- ==========================================
