package com.fitness.modules.dashboard.mapper;

import com.fitness.modules.dashboard.model.vo.CourseStatsVO;
import com.fitness.modules.dashboard.model.vo.PeakHoursVO;
import com.fitness.modules.dashboard.model.vo.RepairStatsVO;
import com.fitness.modules.dashboard.model.vo.RevenueTrendVO;
import com.fitness.modules.dashboard.model.vo.UserGrowthVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 仪表盘统计Mapper接口
 */
@Mapper
public interface DashboardMapper {

    /**
     * 统计总会员数
     *
     * @return 会员总数
     */
    Integer countTotalMembers();

    /**
     * 统计活跃会员数
     * 活跃会员定义：近30天内有预约记录的用户
     *
     * @return 活跃会员数
     */
    Integer countActiveMembers();

    /**
     * 统计总课程数
     *
     * @return 课程总数
     */
    Integer countTotalCourses();

    /**
     * 统计总预约数
     *
     * @return 预约总数
     */
    Integer countTotalBookings();

    /**
     * 统计总器材数
     *
     * @return 器材总数
     */
    Integer countTotalEquipment();

    /**
     * 统计到店高峰时间
     * 基于预约记录，按课程开始时间的小时分组统计
     *
     * @return 高峰时间统计列表
     */
    List<PeakHoursVO> selectPeakHours();

    /**
     * 统计课程分类数据
     *
     * @return 课程统计列表
     */
    List<CourseStatsVO> selectCourseStats();

    /**
     * 统计正常状态器材数量
     *
     * @return 正常器材数量
     */
    Integer countNormalEquipment();

    /**
     * 统计维修中器材数量
     *
     * @return 维修中器材数量
     */
    Integer countMaintenanceEquipment();

    /**
     * 统计今日订单数
     *
     * @return 今日订单数量
     */
    Integer countTodayOrders();

    /**
     * 统计今日营收
     *
     * @return 今日营收金额
     */
    BigDecimal sumTodayRevenue();

    /**
     * 查询营收趋势数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param groupBy 分组方式：day-按天，hour-按小时
     * @return 营收趋势数据列表
     */
    List<RevenueTrendVO> selectRevenueTrend(@Param("startDate") String startDate,
                                             @Param("endDate") String endDate,
                                             @Param("groupBy") String groupBy);

    /**
     * 查询用户增长趋势
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户增长数据列表
     */
    List<UserGrowthVO> selectUserGrowth(@Param("startDate") String startDate,
                                        @Param("endDate") String endDate);

    /**
     * 统计待维修器材数量（维修中状态）
     *
     * @return 待维修器材数量
     */
    Integer countRepairEquipment();

    /**
     * 统计已停用/报废器材数量
     *
     * @return 已停用器材数量
     */
    Integer countOfflineEquipment();

    /**
     * 查询报修处理统计
     *
     * @return 报修统计列表
     */
    List<RepairStatsVO> selectRepairStats();
}
