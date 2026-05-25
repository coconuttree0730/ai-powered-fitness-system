package com.fitness.modules.banner.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 轮播图DTO
 */
@Data
public class BannerDTO {

    private Long id;

    /**
     * 轮播图标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100字符")
    private String title;

    /**
     * 轮播图副标题
     */
    @Size(max = 200, message = "副标题长度不能超过200字符")
    private String subtitle;

    /**
     * 图片URL
     */
    @NotBlank(message = "图片不能为空")
    private String imageUrl;

    /**
     * 跳转链接
     */
    private String link;

    /**
     * 排序顺序
     */
    @NotNull(message = "排序不能为空")
    private Integer sortOrder;

    /**
     * 状态：0-隐藏，1-显示
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
