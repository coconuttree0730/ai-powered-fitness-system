package com.fitness.modules.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class SysDictDTO {

    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    @NotBlank(message = "字典编码不能为空")
    private String dictCode;

    private String description;

    private String status;

    private Integer sortOrder;

    private List<SysDictItemDTO> items;
}
