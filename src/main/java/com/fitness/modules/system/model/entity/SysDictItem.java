package com.fitness.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "sys_dict_item", autoResultMap = true)
public class SysDictItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dictId;

    private String label;

    private String value;

    private String description;

    private String status;

    private Integer sortOrder;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
