package com.fitness.integration.ai.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI文本润色请求DTO
 */
@Data
public class TextPolishDTO {

    /**
     * 待润色的文本内容
     */
    @NotBlank(message = "文本内容不能为空")
    @Size(min = 3, max = 100, message = "文本长度必须在3-100个字符之间")
    private String text;
}
