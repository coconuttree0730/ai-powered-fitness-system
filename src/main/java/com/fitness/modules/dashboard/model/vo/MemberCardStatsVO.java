package com.fitness.modules.dashboard.model.vo;

import lombok.Data;

/**
 * 会员卡销量统计VO
 */
@Data
public class MemberCardStatsVO {

    /**
     * 月卡销量
     */
    private Integer monthCard;

    /**
     * 季卡销量
     */
    private Integer quarterCard;

    /**
     * 年卡销量
     */
    private Integer yearCard;
}
