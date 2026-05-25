package com.fitness.modules.dashboard.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 仪表盘统计数据VO
 */
@Data
public class DashboardStatsVO {

    /**
     * 总会员数
     */
    private Integer totalMembers;

    /**
     * 总课程数
     */
    private Integer totalCourses;

    /**
     * 总预约数
     */
    private Integer totalBookings;

    /**
     * 总器材数
     */
    private Integer totalEquipment;

    /**
     * 活跃会员数
     */
    private Integer activeMembers;

    /**
     * 今日订单数
     */
    private Integer todayOrders;

    /**
     * 今日营收
     */
    private BigDecimal todayRevenue;
}
