package com.fitness.modules.dashboard.model.vo;

import lombok.Data;

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
}
