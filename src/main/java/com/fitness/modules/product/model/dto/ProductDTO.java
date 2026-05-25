package com.fitness.modules.product.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过100个字符")
    private String name;
    
    @Size(max = 50, message = "商品编号不能超过50个字符")
    private String code;
    
    @NotBlank(message = "商品分类不能为空")
    private String category;
    
    private String imageUrl;
    private String description;
    
    @NotNull(message = "商品原价不能为空")
    @DecimalMin(value = "0.01", message = "商品原价必须大于0")
    private BigDecimal originalPrice;
    
    private String pointsDiscountType;
    private BigDecimal pointsDiscountValue;
    private BigDecimal maxPointsDiscount;
    
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;
    
    private String status;
    private Boolean isHot;
    private Boolean isNew;
    private Boolean isRecommend;
    private Integer sortOrder;
    private Long coachId;
}
