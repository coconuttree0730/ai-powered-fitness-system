package com.fitness.modules.chat.memory.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.common.mybatis.JsonbTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "chat_long_term_memory", autoResultMap = true)
public class ChatLongTermMemory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String memoryKey;

    private String memoryType;

    private String content;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> metadata;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
