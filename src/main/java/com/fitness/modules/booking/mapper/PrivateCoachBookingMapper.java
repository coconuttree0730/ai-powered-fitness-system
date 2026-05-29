package com.fitness.modules.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.booking.model.entity.PrivateCoachBooking;
import com.fitness.modules.booking.model.vo.PrivateCoachBookingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PrivateCoachBookingMapper extends BaseMapper<PrivateCoachBooking> {

    PrivateCoachBookingVO selectDetailById(@Param("id") Long id);

    List<PrivateCoachBookingVO> selectByUserId(@Param("userId") Long userId);

    List<PrivateCoachBookingVO> selectByCoachIdAndDate(
            @Param("coachId") Long coachId,
            @Param("date") LocalDate date);

    List<PrivateCoachBookingVO> selectByCoachIdAndDateRange(
            @Param("coachId") Long coachId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    int cancelBooking(
            @Param("id") Long id,
            @Param("reason") String reason);

    int updateStatus(
            @Param("id") Long id,
            @Param("status") Integer status);
}