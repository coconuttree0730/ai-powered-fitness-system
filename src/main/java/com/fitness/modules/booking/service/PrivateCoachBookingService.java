package com.fitness.modules.booking.service;

import com.fitness.modules.booking.model.dto.PrivateCoachBookingDTO;
import com.fitness.modules.booking.model.vo.PrivateCoachBookingVO;

import java.time.LocalDate;
import java.util.List;

public interface PrivateCoachBookingService {

    Long createBooking(Long userId, PrivateCoachBookingDTO dto);

    void cancelBooking(Long userId, Long bookingId, String reason);

    void confirmBooking(Long coachId, Long bookingId);

    void completeBooking(Long bookingId);

    List<PrivateCoachBookingVO> getMyBookings(Long userId);

    List<PrivateCoachBookingVO> getCoachBookingsByDate(Long coachId, LocalDate date);

    List<PrivateCoachBookingVO> getCoachBookingsByDateRange(Long coachId, LocalDate startDate, LocalDate endDate);
}