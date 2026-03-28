package com.fitness.modules.product.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceCalculationVO {
    private Long productId;
    private Integer quantity;
    private BigDecimal originalTotalPrice;
    private Integer userAvailablePoints;
    private Integer usePoints;
    private BigDecimal pointsDiscount;
    private BigDecimal finalPrice;
    private Boolean pointsSufficient;
    private String message;
}
