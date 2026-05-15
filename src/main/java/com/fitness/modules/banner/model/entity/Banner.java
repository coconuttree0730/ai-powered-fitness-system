package com.fitness.modules.banner.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 轮播图实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_banner")
public class Banner extends BaseEntity {

    @TableId(type = IdType.AUTO)
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
}
