-- 显式启用 pgvector 扩展（即使已预装，CREATE EXTENSION 是幂等的）
CREATE EXTENSION IF NOT EXISTS vector;
