package com.fitness.integration.ai.model.vo;

import lombok.Data;

/**
 * AI文本润色响应VO
 */
@Data
public class TextPolishVO {

    /**
     * 润色后的文本
     */
    private String polishedText;

    /**
     * 原始文本
     */
    private String originalText;
}
