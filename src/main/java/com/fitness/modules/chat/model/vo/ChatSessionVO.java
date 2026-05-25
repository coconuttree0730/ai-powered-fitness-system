package com.fitness.modules.chat.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatSessionVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String title;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<ChatMessageVO> messages;
}
