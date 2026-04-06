package com.fitness.modules.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "knowledge_document")
public class KnowledgeDocument {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String fileUrl;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private Integer status;

    private Integer chunkCount;

    private Long createBy;

    @TableLogic(value = "false", delval = "true")
    private Boolean deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
