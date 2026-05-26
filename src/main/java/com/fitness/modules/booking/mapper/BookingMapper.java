package com.fitness.modules.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.booking.model.entity.Booking;
import com.fitness.modules.booking.model.vo.BookingListVO;
import com.fitness.modules.booking.model.vo.BookingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 预约Mapper接口
 */
@Mapper
public interface BookingMapper extends BaseMapper<Booking> {

    /**
     * 根据用户ID查询预约列表
     *
     * @param userId 用户ID
     * @return 预约列表
     */
    @Select("SELECT b.id, b.session_id, b.course_id, c.course_name, u.username as coach_name, " +
            "s.session_date, s.day_of_week, s.start_time, s.end_time, b.booking_time, b.status " +
            "FROM fitness_booking b " +
            "LEFT JOIN fitness_course_session s ON b.session_id = s.id " +
            "LEFT JOIN fitness_course c ON b.course_id = c.id " +
            "LEFT JOIN sys_user u ON c.coach_id = u.id " +
            "WHERE b.user_id = #{userId} AND b.deleted = false " +
            "ORDER BY s.session_date ASC, s.start_time ASC")
    List<BookingListVO> selectBookingListByUserId(@Param("userId") Long userId);

    /**
     * 查询预约详情
     *
     * @param bookingId 预约ID
     * @return 预约详情
     */
    @Select("SELECT b.id, b.user_id, su.username, b.course_id, c.course_name, " +
            "c.coach_id, u.username as coach_name, b.session_id, " +
            "s.session_date, s.day_of_week, s.start_time, s.end_time, " +
            "b.booking_time, b.status, b.cancel_reason, b.create_time " +
            "FROM fitness_booking b " +
            "LEFT JOIN fitness_course c ON b.course_id = c.id " +
            "LEFT JOIN fitness_course_session s ON b.session_id = s.id " +
            "LEFT JOIN sys_user u ON c.coach_id = u.id " +
            "LEFT JOIN sys_user su ON b.user_id = su.id " +
            "WHERE b.id = #{bookingId} AND b.deleted = false")
    BookingVO selectBookingDetail(@Param("bookingId") Long bookingId);

    /**
     * 分页查询预约列表（管理端）
     *
     * @param page     分页对象
     * @param userId   用户ID
     * @param courseId 课程ID
     * @param status   状态
     * @return 预约列表分页
     */
    Page<BookingListVO> selectBookingPage(@Param("page") Page<BookingListVO> page,
                                          @Param("userId") Long userId,
                                          @Param("courseId") Long courseId,
                                          @Param("status") Integer status);

    /**
     * 检查用户是否已预约课程实例
     *
     * @param userId    用户ID
     * @param sessionId 课程实例ID
     * @return 已存在的预约记录数
     */
    @Select("SELECT COUNT(*) FROM fitness_booking " +
            "WHERE user_id = #{userId} AND session_id = #{sessionId} " +
            "AND status IN (0, 1) AND deleted = false")
    int countByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") Long sessionId);

    @Select("SELECT user_id FROM fitness_booking " +
            "WHERE session_id = #{sessionId} AND status IN (0, 1) AND deleted = false")
    List<Long> selectActiveUserIdsBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 检查用户是否已预约课程（兼容旧数据）
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 已存在的预约记录数
     */
    @Select("SELECT COUNT(*) FROM fitness_booking " +
            "WHERE user_id = #{userId} AND course_id = #{courseId} " +
            "AND status IN (0, 1) AND deleted = false")
    int countByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /**
     * 更新预约状态
     *
     * @param bookingId 预约ID
     * @param status    状态
     * @return 影响行数
     */
    @Update("UPDATE fitness_booking SET status = #{status} " +
            "WHERE id = #{bookingId} AND deleted = false")
    int updateStatus(@Param("bookingId") Long bookingId, @Param("status") Integer status);

    /**
     * 取消预约
     *
     * @param bookingId    预约ID
     * @param cancelReason 取消原因
     * @return 影响行数
     */
    @Update("UPDATE fitness_booking SET status = 2, cancel_reason = #{cancelReason} " +
            "WHERE id = #{bookingId} AND status IN (0, 1) AND deleted = false")
    int cancelBooking(@Param("bookingId") Long bookingId, @Param("cancelReason") String cancelReason);

    /**
     * 统计课程的预约数量
     *
     * @param courseId 课程ID
     * @return 预约数量
     */
    @Select("SELECT COUNT(*) FROM fitness_booking " +
            "WHERE course_id = #{courseId} AND status IN (0, 1) AND deleted = false")
    int countByCourseId(@Param("courseId") Long courseId);

    int batchCompleteBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    int batchCancelBySessionIds(@Param("sessionIds") List<Long> sessionIds, @Param("cancelReason") String cancelReason);
}
