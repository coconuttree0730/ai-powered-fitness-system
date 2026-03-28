package com.fitness.modules.product.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductOrderDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    private Integer quantity;
    
    private Integer usePoints;
    private String address;
    private String remark;
}
