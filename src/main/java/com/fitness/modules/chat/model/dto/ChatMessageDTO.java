package com.fitness.modules.chat.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatMessageDTO {

    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
