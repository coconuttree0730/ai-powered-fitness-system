package com.fitness.modules.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysDictItemDTO {

    @NotBlank(message = "显示名称不能为空")
    private String label;

    @NotBlank(message = "值不能为空")
    private String value;

    private String description;

    private String status;

    private Integer sortOrder;
}
