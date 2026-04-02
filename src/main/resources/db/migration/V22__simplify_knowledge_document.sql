-- 简化 knowledge_document 表，移除不需要的字段
-- 只保留：id, title, file_url, file_name, file_type, file_size, status, chunk_count, create_by, create_time, update_time, deleted

-- 移除不需要的字段
ALTER TABLE knowledge_document DROP COLUMN IF EXISTS content;
ALTER TABLE knowledge_document DROP COLUMN IF EXISTS summary;
ALTER TABLE knowledge_document DROP COLUMN IF EXISTS category_id;
ALTER TABLE knowledge_document DROP COLUMN IF EXISTS tags;
