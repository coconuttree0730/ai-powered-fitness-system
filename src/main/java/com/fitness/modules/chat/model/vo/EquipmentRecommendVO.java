package com.fitness.modules.chat.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 器械推荐VO
 * 根据健身计划推荐的器械
 */
@Data
public class EquipmentRecommendVO {

    /**
     * 器械ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long equipmentId;

    /**
     * 器械名称
     */
    private String equipmentName;

    /**
     * 器械类型名称
     */
    private String typeName;

    /**
     * 器械位置
     */
    private String location;

    /**
     * 器械图片URL
     */
    private String imageUrl;

    /**
     * 器械描述
     */
    private String description;

    /**
     * 使用建议
     */
    private String usageAdvice;

    /**
     * 推荐理由
     */
    private String recommendReason;
}
