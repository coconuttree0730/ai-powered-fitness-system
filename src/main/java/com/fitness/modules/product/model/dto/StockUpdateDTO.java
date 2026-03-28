package com.fitness.modules.product.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockUpdateDTO {
    
    @NotBlank(message = "操作类型不能为空")
    private String type; // IN-入库, OUT-出库
    
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
    
    private String remark;
}
