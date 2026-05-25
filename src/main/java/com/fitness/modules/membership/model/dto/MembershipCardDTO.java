package com.fitness.modules.membership.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MembershipCardDTO {

    private Long id;

    @NotBlank(message = "会员卡类型不能为空")
    private String typeCode;

    @NotBlank(message = "会员卡名称不能为空")
    @Size(max = 200, message = "名称长度不能超过200")
    private String name;

    @Size(max = 500, message = "副标题长度不能超过500")
    private String subtitle;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotNull(message = "有效期天数不能为空")
    @Min(value = 1, message = "有效期至少1天")
    private Integer durationDays;

    @Min(value = 0, message = "赠送积分不能为负数")
    private Integer pointsReward;

    private BigDecimal dailyPrice;
    private String status;
    private Boolean isRecommend;
    private Integer sortOrder;
    private String coverImage;

    private List<MembershipCardContentDTO> contents;
}
