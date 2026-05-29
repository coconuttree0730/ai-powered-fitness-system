package com.fitness.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.user.model.entity.CoachNotification;
import com.fitness.modules.user.model.vo.CoachNotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CoachNotificationMapper extends BaseMapper<CoachNotification> {

    @Select("SELECT COUNT(*) FROM coach_notification " +
            "WHERE coach_id = #{coachId} AND is_read = false AND deleted = false")
    long countUnreadByCoachId(@Param("coachId") Long coachId);

    @Select("SELECT cn.*, su.nickname AS studentName, " +
            "pcb.booking_date AS bookingDate, pcb.start_time AS startTime, pcb.end_time AS endTime " +
            "FROM coach_notification cn " +
            "LEFT JOIN sys_user su ON cn.student_id = su.id " +
            "LEFT JOIN fitness_private_coach_booking pcb ON cn.booking_id = pcb.id " +
            "WHERE cn.coach_id = #{coachId} AND cn.deleted = false " +
            "ORDER BY cn.create_time DESC")
    List<CoachNotificationVO> selectByCoachId(@Param("coachId") Long coachId);
}