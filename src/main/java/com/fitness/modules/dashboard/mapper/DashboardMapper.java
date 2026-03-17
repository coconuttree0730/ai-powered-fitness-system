package com.fitness.modules.dashboard.mapper;

import com.fitness.modules.dashboard.model.vo.CourseStatsVO;
import com.fitness.modules.dashboard.model.vo.PeakHoursVO;
import org.apache.ibatis.annotations.Mapper;

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
}
