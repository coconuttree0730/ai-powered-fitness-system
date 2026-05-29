package com.fitness.modules.order.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderDTO {

    @NotNull(message = "订单类型不能为空")
    private String orderType;

    private Long productId;
    private Long coachPackageId;
    private Long cardId;
    private Integer quantity;
    private Integer usePoints;
    private String remark;
}