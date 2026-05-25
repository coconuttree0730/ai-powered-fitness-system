package com.fitness.modules.booking.service.impl;

import com.fitness.modules.course.model.vo.CourseSessionVO;

public interface BookingReservationService {

    BookingReservationResult tryReserve(Long sessionId, Long userId, CourseSessionVO session);

    void releaseReservation(Long sessionId, Long userId, CourseSessionVO session);
}
