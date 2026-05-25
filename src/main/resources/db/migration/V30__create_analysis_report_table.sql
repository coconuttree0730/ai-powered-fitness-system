-- 创建AI数据分析报告表
CREATE TABLE analysis_report (
    id BIGSERIAL PRIMARY KEY,
    report_title VARCHAR(255) NOT NULL,
    analysis_type VARCHAR(50) NOT NULL,
    report_content TEXT NOT NULL,
    suggestions TEXT,           -- Markdown格式文本
    generate_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建索引
CREATE INDEX idx_analysis_report_type ON analysis_report(analysis_type);
CREATE INDEX idx_analysis_report_generate_time ON analysis_report(generate_time DESC);
CREATE INDEX idx_analysis_report_create_by ON analysis_report(create_by);
CREATE INDEX idx_analysis_report_is_deleted ON analysis_report(is_deleted);
