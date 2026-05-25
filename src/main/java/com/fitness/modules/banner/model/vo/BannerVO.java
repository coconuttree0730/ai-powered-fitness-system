package com.fitness.modules.banner.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图VO
 */
@Data
public class BannerVO {

    private Long id;

    /**
     * 轮播图标题
     */
    private String title;

    /**
     * 轮播图副标题
     */
    private String subtitle;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 跳转链接
     */
    private String link;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 状态：0-隐藏，1-显示
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
