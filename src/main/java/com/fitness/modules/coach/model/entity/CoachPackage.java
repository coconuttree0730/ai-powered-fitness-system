package com.fitness.modules.coach.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教练套餐实体类
 * 对应 coach_package 表
 */
@Data
@TableName("coach_package")
public class CoachPackage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 教练ID */
    private Long coachId;

    /** 套餐名称 */
    private String name;

    /** 套餐编码 */
    private String packageCode;

    /** 套餐描述 */
    private String description;

    /** 封面图片 */
    private String coverImage;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 总课时数 */
    private Integer totalSessions;

    /** 有效期（天） */
    private Integer validityDays;

    /** 状态：ACTIVE-上架, INACTIVE-下架 */
    private String status;

    /** 排序 */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}