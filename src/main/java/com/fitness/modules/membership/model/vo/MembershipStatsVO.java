package com.fitness.modules.membership.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MembershipStatsVO {

    private Long cardTypeCount;
    private Long activeMemberCount;
    private BigDecimal monthlyRevenue;
    private BigDecimal renewalRate;

    private Double cardTypeTrend;
    private Double memberTrend;
    private Double revenueTrend;
    private Double renewalRateTrend;
}
