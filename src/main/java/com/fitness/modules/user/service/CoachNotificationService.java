package com.fitness.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.user.model.entity.CoachNotification;
import com.fitness.modules.user.model.vo.CoachNotificationVO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CoachNotificationService extends IService<CoachNotification> {

    void createBookingNotification(Long studentId, Long coachId, Long bookingId,
                                   String studentName, LocalDate bookingDate,
                                   LocalTime startTime, LocalTime endTime);

    long getUnreadCount(Long coachId);

    List<CoachNotificationVO> getNotifications(Long coachId);

    void markAsRead(Long id, Long coachId);
}