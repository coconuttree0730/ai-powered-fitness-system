package com.fitness.modules.membership.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MembershipOrderDTO {

    @NotNull(message = "会员卡ID不能为空")
    private Long cardId;

    private String remark;
}
