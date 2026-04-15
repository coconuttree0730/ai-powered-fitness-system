package com.fitness.modules.membership.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("membership_card_content")
public class MembershipCardContent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cardId;
    private String contentType;
    private String title;
    private String description;
    private String icon;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
