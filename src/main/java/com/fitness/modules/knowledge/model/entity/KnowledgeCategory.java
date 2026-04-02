package com.fitness.modules.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("knowledge_category")
public class KnowledgeCategory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private String description;

    private Integer sortOrder;

    @TableLogic
    private Boolean deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
