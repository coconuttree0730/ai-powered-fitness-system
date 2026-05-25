package com.fitness.modules.knowledge.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeCategoryDTO {
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最长50个字符")
    private String name;

    @NotBlank(message = "分类编码不能为空")
    @Size(max = 50, message = "分类编码最长50个字符")
    private String code;

    @Size(max = 255, message = "描述最长255个字符")
    private String description;

    private Integer sortOrder;
}
