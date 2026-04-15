package com.fitness.modules.membership.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MembershipCardContentDTO {

    private Long id;

    private Long cardId;

    @NotBlank(message = "内容类型不能为空")
    private String contentType;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    private String description;
    private String icon;
    private Integer sortOrder;
}
