package com.fitness.modules.chat.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String role;

    private String content;

    private LocalDateTime createTime;
}
