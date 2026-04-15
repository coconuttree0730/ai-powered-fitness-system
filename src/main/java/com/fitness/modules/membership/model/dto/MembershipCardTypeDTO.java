package com.fitness.modules.membership.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MembershipCardTypeDTO {

    private Long id;

    @NotBlank(message = "类型名称不能为空")
    @Size(max = 100, message = "类型名称长度不能超过100")
    private String name;

    @NotBlank(message = "类型编码不能为空")
    @Size(max = 50, message = "类型编码长度不能超过50")
    private String code;

    private String description;
    private String status;
    private Integer sortOrder;
}
