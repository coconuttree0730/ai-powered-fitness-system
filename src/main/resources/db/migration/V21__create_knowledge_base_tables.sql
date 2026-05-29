-- ========================================================
-- 知识库管理模块数据库表
-- 版本：V21
-- 说明：支持RAG多路召回的知识库系统
-- ========================================================

-- 启用 pgvector 扩展（如果未启用）
CREATE EXTENSION IF NOT EXISTS vector;

-- ========================================================
-- 1. 知识库分类表
-- ========================================================
CREATE TABLE knowledge_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    sort_order INT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
COMMENT ON TABLE knowledge_category IS '知识库分类表';
COMMENT ON COLUMN knowledge_category.name IS '分类名称';
COMMENT ON COLUMN knowledge_category.code IS '分类编码';
COMMENT ON COLUMN knowledge_category.description IS '分类描述';
COMMENT ON COLUMN knowledge_category.sort_order IS '排序';
COMMENT ON COLUMN knowledge_category.deleted IS '软删除标识';

-- ========================================================
-- 2. 知识库文档表
-- ========================================================
CREATE TABLE knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    summary VARCHAR(500),
    file_url VARCHAR(500),
    file_name VARCHAR(255),
    file_type VARCHAR(50),
    file_size BIGINT DEFAULT 0,
    category_id BIGINT,
    tags JSONB DEFAULT '[]'::jsonb,
    status SMALLINT DEFAULT 0 NOT NULL,
    chunk_count INT DEFAULT 0,
    create_by BIGINT,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
COMMENT ON TABLE knowledge_document IS '知识库文档表';
COMMENT ON COLUMN knowledge_document.title IS '文档标题';
COMMENT ON COLUMN knowledge_document.content IS '原始文本内容';
COMMENT ON COLUMN knowledge_document.summary IS '文档摘要';
COMMENT ON COLUMN knowledge_document.file_url IS 'MinIO文件URL';
COMMENT ON COLUMN knowledge_document.file_name IS '原始文件名';
COMMENT ON COLUMN knowledge_document.file_type IS '文件类型(md/txt)';
COMMENT ON COLUMN knowledge_document.file_size IS '文件大小(字节)';
COMMENT ON COLUMN knowledge_document.category_id IS '分类ID';
COMMENT ON COLUMN knowledge_document.tags IS '标签(JSON数组)';
COMMENT ON COLUMN knowledge_document.status IS '状态：0-草稿，1-已发布，2-已归档';
COMMENT ON COLUMN knowledge_document.chunk_count IS '切片数量';
COMMENT ON COLUMN knowledge_document.create_by IS '创建人ID';
COMMENT ON COLUMN knowledge_document.deleted IS '软删除标识';

-- ========================================================
-- 3. 知识库切片表（核心RAG表）
-- ========================================================
CREATE TABLE knowledge_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    content_hash VARCHAR(64),
    embedding vector(768),
    metadata JSONB DEFAULT '{}'::jsonb,
    char_count INT DEFAULT 0,
    create_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
COMMENT ON TABLE knowledge_chunk IS '知识库切片表';
COMMENT ON COLUMN knowledge_chunk.document_id IS '关联文档ID';
COMMENT ON COLUMN knowledge_chunk.chunk_index IS '切片序号(从0开始)';
COMMENT ON COLUMN knowledge_chunk.content IS '切片文本内容';
COMMENT ON COLUMN knowledge_chunk.content_hash IS '内容哈希(MD5)';
COMMENT ON COLUMN knowledge_chunk.embedding IS '向量嵌入(768维)';
COMMENT ON COLUMN knowledge_chunk.metadata IS '元数据(JSONB)';
COMMENT ON COLUMN knowledge_chunk.char_count IS '字符数';

-- ========================================================
-- 4. 创建索引
-- ========================================================

-- 分类表索引
CREATE INDEX idx_knowledge_category_code ON knowledge_category(code);
CREATE INDEX idx_knowledge_category_deleted ON knowledge_category(deleted);

-- 文档表索引
CREATE INDEX idx_knowledge_document_category_id ON knowledge_document(category_id);
CREATE INDEX idx_knowledge_document_status ON knowledge_document(status);
CREATE INDEX idx_knowledge_document_create_by ON knowledge_document(create_by);
CREATE INDEX idx_knowledge_document_deleted ON knowledge_document(deleted);
CREATE INDEX idx_knowledge_document_create_time ON knowledge_document(create_time);

-- 切片表索引
CREATE INDEX idx_knowledge_chunk_document_id ON knowledge_chunk(document_id);
CREATE INDEX idx_knowledge_chunk_content_hash ON knowledge_chunk(content_hash);

-- 向量索引（HNSW，用于近似最近邻搜索）
CREATE INDEX idx_knowledge_chunk_embedding ON knowledge_chunk 
USING hnsw (embedding vector_cosine_ops);

-- 全文搜索索引（用于__关键词__检索）
CREATE INDEX idx_knowledge_chunk_content_fts ON knowledge_chunk 
USING gin(to_tsvector('simple', content));

-- ========================================================
-- 5. 创建外键约束
-- ========================================================
ALTER TABLE knowledge_document
    ADD CONSTRAINT fk_knowledge_document_category_id 
    FOREIGN KEY (category_id) REFERENCES knowledge_category(id) ON DELETE SET NULL;

ALTER TABLE knowledge_chunk
    ADD CONSTRAINT fk_knowledge_chunk_document_id 
    FOREIGN KEY (document_id) REFERENCES knowledge_document(id) ON DELETE CASCADE;

-- ========================================================
-- 6. 创建更新时间触发器
-- ========================================================
CREATE TRIGGER update_knowledge_category_update_time 
BEFORE UPDATE ON knowledge_category
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_knowledge_document_update_time 
BEFORE UPDATE ON knowledge_document
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

-- ========================================================
-- 7. 初始化分类数据
-- ========================================================
INSERT INTO knowledge_category (name, code, description, sort_order) VALUES
('课程相关', 'COURSE', '健身课程介绍、课程安排、课程内容等相关文档', 1),
('会员政策', 'MEMBERSHIP', '会员权益、会员等级、会员服务等相关文档', 2),
('商品相关', 'PRODUCT', '健身商品、营养补剂、运动装备等相关文档', 3),
('售后服务', 'AFTER_SALES', '售后服务、退换货政策、投诉建议等相关文档', 4),
('场馆信息', 'VENUE', '场馆介绍、设施说明、开放时间等相关文档', 5),
('其他', 'OTHER', '其他类型的知识文档', 99);
