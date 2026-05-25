package com.fitness.modules.equipment.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * 器材创建/更新请求DTO
 */
@Data
public class EquipmentDTO {

    /**
     * 器材名称
     */
    @NotBlank(message = "器材名称不能为空")
    private String equipmentName;

    /**
     * 器材位置
     */
    @NotBlank(message = "器材位置不能为空")
    private String location;

    /**
     * 状态：0-维修中, 1-正常, 2-已报废
     */
    private Integer status;

    /**
     * 器材描述
     */
    private String description;

    /**
     * 器材图片URL
     */
    private String imageUrl;

    /**
     * 购买日期
     */
    private LocalDate purchaseDate;

    /**
     * 器材类型编码
     */
    @NotBlank(message = "器材类型不能为空")
    private String typeCode;

    /**
     * 器材编号
     */
    private String equipmentNo;
}
