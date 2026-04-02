# 知识库管理模块设计方案

## 1. 概述

### 1.1 背景
为了提升"健小助"AI助手的回答准确性，避免模型幻觉问题，需要构建一个知识库管理系统，将健身房的业务知识文档进行向量化存储，通过RAG（Retrieval-Augmented Generation）技术实现精准问答。

### 1.2 目标
- 实现文档上传、解析、切片、向量化全流程
- 支持多路召回检索（关键词检索 + 向量检索）
- 集成RAG对话，提升AI回答准确性
- 支持文档状态管理（草稿/发布/归档）

---

## 2. 技术方案

### 2.1 方案选型：多路召回范式

经过对比分析，最终选择**方案三：多路召回**方案。

#### 方案对比

| 方案 | 复杂度 | 准确率 | 响应速度 | 说明 |
|------|--------|--------|----------|------|
| 方案一：简单向量检索 | ⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | MVP快速上线 |
| 方案三：多路召回 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 追求准确率（选择） |

#### 多路召回架构

```
用户查询
    ↓
┌───────────────────────────────────┐
│        多路召回（并行）              │
├─────────────┬─────────────────────┤
│  第一路      │      第二路          │
│ 关键词检索   │    向量检索          │
│ (全文搜索)   │   (语义相似)         │
│  Top-K     │      Top-K          │
└─────────────┴─────────────────────┘
    ↓
┌───────────────────────────────────┐
│        融合排序（RRF）              │
│  Reciprocal Rank Fusion           │
└───────────────────────────────────┘
    ↓
    最终结果 → AI生成回答
```

#### RRF融合算法

```python
# RRF公式
score(doc) = Σ 1/(k + rank_i)
# k通常取60

# 示例：
# 文档A: 关键词排名第2，向量排名第5
# score(A) = 1/(60+2) + 1/(60+5) = 0.0161 + 0.0154 = 0.0315
```

### 2.2 技术栈

| 组件 | 技术选型 | 说明 |
|------|---------|------|
| 向量数据库 | PostgreSQL + pgvector | 利用现有基础设施 |
| Embedding模型 | Ollama + embeddinggemma:300m | 本地部署，768维 |
| 切片策略 | 固定大小 + 语义边界 | 500字符，重叠50字符 |
| 全文搜索 | PostgreSQL tsvector | 原生支持中文分词 |

---

## 3. 数据库设计

### 3.1 表结构

#### knowledge_category（分类表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| name | VARCHAR(50) | 分类名称 |
| code | VARCHAR(50) | 分类编码（唯一） |
| description | VARCHAR(255) | 描述 |
| sort_order | INT | 排序 |
| deleted | BOOLEAN | 软删除标识 |

#### knowledge_document（文档表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| title | VARCHAR(200) | 文档标题 |
| content | TEXT | 原始文本内容 |
| summary | VARCHAR(500) | 摘要 |
| file_url | VARCHAR(500) | MinIO文件URL |
| file_name | VARCHAR(255) | 原始文件名 |
| file_type | VARCHAR(50) | 文件类型（md/txt） |
| file_size | BIGINT | 文件大小 |
| category_id | BIGINT | 分类ID（可选） |
| tags | JSONB | 标签（JSON数组，可选） |
| status | SMALLINT | 状态：0-草稿，1-已发布，2-已归档 |
| chunk_count | INT | 切片数量 |
| create_by | BIGINT | 创建人ID |

#### knowledge_chunk（切片表 - 核心RAG表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| document_id | BIGINT | 关联文档ID |
| chunk_index | INT | 切片序号 |
| content | TEXT | 切片文本内容 |
| content_hash | VARCHAR(64) | 内容MD5哈希 |
| embedding | vector(768) | 向量嵌入（768维） |
| metadata | JSONB | 元数据 |
| char_count | INT | 字符数 |

### 3.2 索引设计

```sql
-- 向量索引（HNSW，用于近似最近邻搜索）
CREATE INDEX idx_knowledge_chunk_embedding ON knowledge_chunk 
USING hnsw (embedding vector_cosine_ops);

-- 全文搜索索引（用于关键词检索）
CREATE INDEX idx_knowledge_chunk_content_fts ON knowledge_chunk 
USING gin(to_tsvector('simple', content));
```

---

## 4. 核心功能实现

### 4.1 文档处理流程

```
文件上传 → 文件解析 → 文档切片 → 向量化 → 存储
    ↓           ↓           ↓          ↓       ↓
  MinIO     提取文本    500字符/段   Ollama   PostgreSQL
```

### 4.2 切片策略

- **切片大小**：500字符
- **重叠大小**：50字符
- **语义边界**：优先在段落、句子边界切分
- **元数据保留**：文档ID、标题、分类、标签

### 4.3 多路召回实现

#### 向量检索

```sql
SELECT *, 1 - (embedding <=> query_vector) as similarity
FROM knowledge_chunk
WHERE 1 - (embedding <=> query_vector) >= threshold
ORDER BY embedding <=> query_vector
LIMIT topK;
```

#### 关键词检索

```sql
SELECT *, ts_rank_cd(to_tsvector('simple', content), plainto_tsquery('simple', keyword)) as score
FROM knowledge_chunk
WHERE to_tsvector('simple', content) @@ plainto_tsquery('simple', keyword)
ORDER BY score DESC
LIMIT topK;
```

#### RRF融合

```java
// 向量检索排名
for (int i = 0; i < vectorResults.size(); i++) {
    double rrfScore = 1.0 / (RRF_K + i + 1);
    // 累加到文档得分
}

// 关键词检索排名
for (int i = 0; i < keywordResults.size(); i++) {
    double rrfScore = 1.0 / (RRF_K + i + 1);
    // 累加到文档得分
}

// 按总分排序，取Top-K
```

### 4.4 RAG对话流程

```
用户问题
    ↓
多路召回检索
    ↓
构建上下文（Top-5切片）
    ↓
Prompt模板 + 上下文 + 问题
    ↓
大模型生成回答
```

#### Prompt模板

```
你是健身房的智能助手"健小助"，请根据以下知识库内容回答用户问题。

要求：
1. 只使用提供的知识库内容回答问题，不要编造信息
2. 如果知识库中没有相关信息，请明确告知用户
3. 回答要简洁、准确、友好
4. 如果涉及具体数据（如价格、时间等），请准确引用

知识库内容：
{context}

用户问题：{query}

请回答：
```

---

## 5. API接口设计

### 5.1 分类管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/knowledge/categories` | GET | 获取所有分类 |
| `/api/v1/knowledge/categories` | POST | 创建分类 |
| `/api/v1/knowledge/categories/{id}` | PUT | 更新分类 |
| `/api/v1/knowledge/categories/{id}` | DELETE | 删除分类 |

### 5.2 文档管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/knowledge/documents` | GET | 文档列表（分页） |
| `/api/v1/knowledge/documents/upload` | POST | 上传文档 |
| `/api/v1/knowledge/documents/{id}` | GET | 文档详情 |
| `/api/v1/knowledge/documents/{id}` | PUT | 更新文档 |
| `/api/v1/knowledge/documents/{id}` | DELETE | 删除文档 |
| `/api/v1/knowledge/documents/{id}/publish` | PATCH | 发布文档 |
| `/api/v1/knowledge/documents/{id}/archive` | PATCH | 归档文档（设为草稿） |
| `/api/v1/knowledge/documents/{id}/reindex` | POST | 重新索引 |

### 5.3 RAG检索

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/knowledge/rag/search` | POST | RAG检索（返回切片+答案） |
| `/api/v1/knowledge/rag/chat` | POST | RAG对话（返回答案） |

---

## 6. 配置说明

### 6.1 application.yml

```yaml
spring:
  ai:
    ollama:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
      embedding:
        model: ${OLLAMA_EMBEDDING_MODEL:embeddinggemma:300m}

knowledge:
  chunk:
    size: 500      # 切片大小
    overlap: 50    # 切片重叠
```

### 6.2 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| OLLAMA_BASE_URL | http://localhost:11434 | Ollama服务地址 |
| OLLAMA_EMBEDDING_MODEL | embeddinggemma:300m | Embedding模型名称 |

---

## 7. 预置分类

| 分类名称 | 编码 | 说明 |
|---------|------|------|
| 课程相关 | COURSE | 健身课程介绍、课程安排、课程内容等 |
| 会员政策 | MEMBERSHIP | 会员权益、会员等级、会员服务等 |
| 商品相关 | PRODUCT | 健身商品、营养补剂、运动装备等 |
| 售后服务 | AFTER_SALES | 售后服务、退换货政策、投诉建议等 |
| 场馆信息 | VENUE | 场馆介绍、设施说明、开放时间等 |
| 其他 | OTHER | 其他类型的知识文档 |

---

## 8. 扩展性设计

### 8.1 未来可扩展方向

1. **添加重排序模型**
   - 在融合排序后添加神经重排序
   - 提升最终排序质量

2. **Learning to Rank (LTR)**
   - 将RRF替换为LTR模型
   - 基于用户反馈持续优化

3. **更多召回通道**
   - 知识图谱检索
   - BM25检索
   - 混合检索

4. **更多文档格式**
   - PDF解析
   - Word文档
   - 网页抓取

### 8.2 演进路线

```
阶段1（当前）：双路召回 + RRF融合
    ↓
阶段2：添加重排序模型
    ↓
阶段3：Learning to Rank（LTR）
    ↓
阶段4：添加更多召回通道
```

---

## 9. 项目结构

```
src/main/java/com/fitness/modules/knowledge/
├── controller/
│   ├── KnowledgeCategoryController.java
│   ├── KnowledgeDocumentController.java
│   └── RAGController.java
├── handler/
│   └── VectorTypeHandler.java
├── mapper/
│   ├── KnowledgeCategoryMapper.java
│   ├── KnowledgeChunkMapper.java
│   └── KnowledgeDocumentMapper.java
├── model/
│   ├── dto/
│   │   ├── KnowledgeCategoryDTO.java
│   │   ├── KnowledgeDocumentDTO.java
│   │   ├── KnowledgeDocumentQueryDTO.java
│   │   └── RAGQueryDTO.java
│   ├── entity/
│   │   ├── KnowledgeCategory.java
│   │   ├── KnowledgeChunk.java
│   │   └── KnowledgeDocument.java
│   ├── enums/
│   │   └── DocumentStatus.java
│   └── vo/
│       ├── KnowledgeCategoryVO.java
│       ├── KnowledgeChunkVO.java
│       ├── KnowledgeDocumentDetailVO.java
│       ├── KnowledgeDocumentVO.java
│       └── RAGSearchResultVO.java
└── service/
    ├── impl/
    │   ├── DocumentProcessorServiceImpl.java
    │   ├── EmbeddingServiceImpl.java
    │   ├── KnowledgeCategoryServiceImpl.java
    │   ├── KnowledgeChunkServiceImpl.java
    │   ├── KnowledgeDocumentServiceImpl.java
    │   └── RAGServiceImpl.java
    ├── DocumentProcessorService.java
    ├── EmbeddingService.java
    ├── KnowledgeCategoryService.java
    ├── KnowledgeChunkService.java
    ├── KnowledgeDocumentService.java
    └── RAGService.java
```

---

## 10. 使用流程

1. **启动 Ollama**
   ```bash
   ollama pull embeddinggemma:300m
   ollama serve
   ```

2. **运行数据库迁移**
   - Flyway 自动执行 V21 脚本

3. **上传文档**
   - 管理员后台上传 md/txt 文件
   - 可选择"草稿"或"立即发布"

4. **发布文档**
   - 发布后自动切片和向量化
   - 草稿状态不会进行向量化

5. **RAG对话**
   - 会员端"健小助"基于知识库回答

---

## 11. 日志说明

### 11.1 文档处理日志

文档上传和索引过程中会输出详细日志：

```
【文档上传】开始上传文档，文件名: xxx.md, 标题: 文档标题
【文档上传】文件验证通过，文件类型: md
【文档上传】文件上传至MinIO成功，URL: http://...
【文档上传】文档记录创建成功，文档ID: 123

【文档索引】开始重建文档索引，文档ID: 123
【文档索引】已清除旧索引数据，文档ID: 123
【文档索引】从文件解析内容，文件URL: http://...
【文档索引】文件内容解析完成，内容长度: 5000 字符
【文档索引】开始文档切片，文档ID: 123, 内容长度: 5000 字符
【文档索引】文档切片完成，共生成 10 个切片
【文档索引】切片 #0 - 长度: 500 字符，内容预览: xxx...
【文档索引】切片 #1 - 长度: 500 字符，内容预览: xxx...
【文档索引】开始向量化处理，共 10 个切片
【文档索引】正在处理切片 #1/10 的向量化
【文档索引】切片 #0 向量化完成，向量维度: 768
【文档索引】所有切片已保存到数据库
【文档索引】文档重新索引完成，文档ID: 123, 切片数量: 10
```

### 11.2 RAG检索日志

RAG检索过程中会输出多路召回的详细过程：

```
【RAG搜索】开始处理查询，查询内容: '健身房营业时间'
【RAG搜索】参数: topK=5, similarityThreshold=0.6, categoryId=null
【混合检索】开始多路召回，查询: '健身房营业时间', topK: 5
【混合检索】开始向量检索...
【混合检索】查询向量化完成，维度: 768，耗时: 150ms
【混合检索】向量检索完成，获取 10 个结果
【混合检索】向量结果 #1 - 文档: '场馆信息.md', 相似度: 0.8523, 内容预览: 健身房营业时间为早6点至晚10点...
【混合检索】向量结果 #2 - 文档: '会员政策.md', 相似度: 0.7234, 内容预览: ...
【混合检索】开始关键词检索...
【混合检索】关键词检索完成，获取 8 个结果，耗时: 50ms
【混合检索】关键词结果 #1 - 文档: '场馆信息.md', 内容预览: ...
【混合检索】开始RRF融合排序...
【混合检索】RRF融合完成，最终返回 5 个结果
【混合检索】========== Top-5 匹配结果 ==========
【混合检索】Top-1 [来源: 混合] 文档: '场馆信息.md'
【混合检索】Top-1 相似度分数: 0.852300
【混合检索】Top-1 匹配文本: 健身房营业时间为早6点至晚10点，周末照常营业...
【混合检索】----------------------------------------
【混合检索】Top-2 [来源: 向量] 文档: '会员政策.md'
【混合检索】Top-2 相似度分数: 0.723400
【混合检索】Top-2 匹配文本: ...
【RAG搜索】检索阶段完成，耗时: 220ms，获取到 5 个相关切片
【RAG搜索】构建的上下文长度: 2500 字符
【RAG搜索】AI回答生成完成，回答长度: 150 字符
【RAG搜索】搜索完成，总耗时: 2500ms (检索: 220ms, 生成: 2280ms)
```

### 11.3 日志级别建议

| 场景 | 日志级别 | 说明 |
|------|---------|------|
| 文档上传/发布 | INFO | 正常业务流程 |
| 切片处理 | INFO | 显示切片数量和预览 |
| 向量化过程 | INFO | 显示进度和维度 |
| RAG检索 | INFO | 显示召回过程和Top-K结果 |
| RRF计算 | DEBUG | 详细的分数计算过程 |
| 错误处理 | ERROR | 异常和错误信息 |

---

## 12. 注意事项

1. **文件格式限制**：当前仅支持 md、txt 格式
2. **文件大小限制**：最大 10MB
3. **向量维度**：768维（embeddinggemma:300m）
4. **切片策略**：500字符，重叠50字符
5. **检索阈值**：默认相似度阈值 0.6
6. **发布流程**：只有"已发布"状态的文档会被向量化并用于RAG检索
7. **草稿状态**：上传时选择草稿不会立即向量化，需手动发布

---

## 13. 前端界面

### 13.1 知识库管理页面

路径：`/admin/knowledge`

功能：
- 文档列表展示（标题、切片数、状态、更新时间）
- 搜索功能（按标题、状态筛选）
- 上传文档（支持选择草稿/立即发布）
- 编辑文档标题
- 发布/设为草稿操作
- 重建索引
- 删除文档

### 13.2 状态流转

```
上传文档 → 草稿状态 → 发布 → 已发布状态 → 归档 → 草稿状态
              ↓
         立即发布 → 已发布状态（上传后直接发布）
```

状态说明：
- **草稿**（0）：文档已上传但未向量化，不可用于RAG检索
- **已发布**（1）：文档已切片并向量化，可用于RAG检索
- **已归档**（2）：文档已下架，不再用于RAG检索
