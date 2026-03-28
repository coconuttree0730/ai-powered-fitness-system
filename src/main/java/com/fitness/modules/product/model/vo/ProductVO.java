package com.fitness.modules.product.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
    private Long id;
    private String name;
    private String code;
    private String category;
    private String categoryLabel;
    private String imageUrl;
    private String description;
    private BigDecimal originalPrice;
    private String pointsDiscountType;
    private BigDecimal pointsDiscountValue;
    private BigDecimal maxPointsDiscount;
    private Integer stock;
    private Integer sales;
    private String status;
    private Boolean isHot;
    private Boolean isNew;
    private Boolean isRecommend;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    
    private BigDecimal calculatedDiscount;
    private BigDecimal finalPrice;
    private Integer requiredPoints;
}
