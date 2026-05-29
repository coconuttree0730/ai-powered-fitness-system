package com.fitness.modules.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.user.mapper.CoachNotificationMapper;
import com.fitness.modules.user.model.entity.CoachNotification;
import com.fitness.modules.user.model.vo.CoachNotificationVO;
import com.fitness.modules.user.service.CoachNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoachNotificationServiceImpl extends ServiceImpl<CoachNotificationMapper, CoachNotification>
        implements CoachNotificationService {

    private final CoachNotificationMapper coachNotificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBookingNotification(Long studentId, Long coachId, Long bookingId,
                                           String studentName, LocalDate bookingDate,
                                           LocalTime startTime, LocalTime endTime) {
        log.info("创建教练预约通知: studentId={}, coachId={}, bookingId={}", studentId, coachId, bookingId);

        String content = "学员" + studentName + "预约了" + bookingDate + " "
                + startTime + "-" + endTime + "的私教课";

        CoachNotification notification = new CoachNotification();
        notification.setCoachId(coachId);
        notification.setStudentId(studentId);
        notification.setBookingId(bookingId);
        notification.setType("BOOKING");
        notification.setContent(content);
        notification.setIsRead(false);

        coachNotificationMapper.insert(notification);
        log.info("教练通知创建成功: notificationId={}", notification.getId());
    }

    @Override
    public long getUnreadCount(Long coachId) {
        return coachNotificationMapper.countUnreadByCoachId(coachId);
    }

    @Override
    public List<CoachNotificationVO> getNotifications(Long coachId) {
        return coachNotificationMapper.selectByCoachId(coachId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id, Long coachId) {
        log.info("标记通知已读: id={}, coachId={}", id, coachId);

        CoachNotification notification = coachNotificationMapper.selectById(id);
        if (notification == null || Boolean.TRUE.equals(notification.getDeleted())) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!notification.getCoachId().equals(coachId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        notification.setIsRead(true);
        coachNotificationMapper.updateById(notification);
        log.info("通知已标记为已读: id={}", id);
    }
}