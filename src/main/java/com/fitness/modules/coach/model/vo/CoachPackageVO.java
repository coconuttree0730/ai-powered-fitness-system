package com.fitness.modules.coach.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教练套餐视图对象
 */
@Data
public class CoachPackageVO {

    private Long id;
    private Long coachId;
    private String name;
    private String packageCode;
    private String description;
    private String coverImage;
    private BigDecimal originalPrice;
    private Integer totalSessions;
    private Integer validityDays;
    private String status;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 教练名称 */
    private String coachName;
}