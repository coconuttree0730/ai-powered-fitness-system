package com.fitness.modules.membership.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivateMembershipDTO {
    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}
