package com.fitness.modules.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.mybatis.JsonbTypeHandler;
import lombok.Data;
import org.springframework.ai.document.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "knowledge_chunk", autoResultMap = true)
public class KnowledgeChunk {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;

    private Integer chunkIndex;

    private String content;

    private String contentHash;

    @TableField(typeHandler = com.fitness.modules.knowledge.handler.VectorTypeHandler.class)
    private float[] embedding;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> metadata;

    private Integer charCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Document toDocument() {
        Document document = new Document(id.toString(), content, metadata);
        return document;
    }
}
