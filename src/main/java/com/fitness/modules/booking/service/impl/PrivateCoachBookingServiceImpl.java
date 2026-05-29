package com.fitness.modules.booking.service.impl;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.booking.mapper.PrivateCoachBookingMapper;
import com.fitness.modules.booking.model.dto.PrivateCoachBookingDTO;
import com.fitness.modules.booking.model.entity.PrivateCoachBooking;
import com.fitness.modules.booking.model.vo.PrivateCoachBookingVO;
import com.fitness.modules.booking.service.PrivateCoachBookingService;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.entity.User;
import com.fitness.modules.user.service.CoachNotificationService;
import com.fitness.modules.user.service.CoachStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateCoachBookingServiceImpl implements PrivateCoachBookingService {

    private final PrivateCoachBookingMapper mapper;
    private final CoachStudentService coachStudentService;
    private final CoachNotificationService coachNotificationService;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBooking(Long userId, PrivateCoachBookingDTO dto) {
        log.info("创建私教预约: userId={}, coachId={}, date={}, startTime={}",
                userId, dto.getCoachId(), dto.getBookingDate(), dto.getStartTime());

        checkTimeConflict(dto.getCoachId(), dto.getBookingDate(), dto.getStartTime(), dto.getEndTime());

        PrivateCoachBooking booking = new PrivateCoachBooking();
        booking.setUserId(userId);
        booking.setCoachId(dto.getCoachId());
        booking.setBookingDate(dto.getBookingDate());
        booking.setStartTime(dto.getStartTime());
        booking.setEndTime(dto.getEndTime());
        booking.setNote(dto.getNote());
        booking.setStatus(0);

        mapper.insert(booking);

        // 通知逻辑：如果学员已绑定该教练，则创建预约通知
        if (coachStudentService.isBound(userId, dto.getCoachId())) {
            User student = userMapper.selectById(userId);
            String studentName = student != null ? student.getNickname() : null;
            coachNotificationService.createBookingNotification(userId, dto.getCoachId(),
                    booking.getId(), studentName, dto.getBookingDate(),
                    dto.getStartTime(), dto.getEndTime());
        }

        log.info("私教预约创建成功: bookingId={}", booking.getId());
        return booking.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long userId, Long bookingId, String reason) {
        log.info("取消私教预约: userId={}, bookingId={}", userId, bookingId);

        PrivateCoachBooking booking = mapper.selectById(bookingId);
        if (booking == null || Boolean.TRUE.equals(booking.getDeleted())) {
            throw new BusinessException(ErrorCode.PRIVATE_BOOKING_NOT_FOUND);
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (booking.getStatus() == 2) {
            throw new BusinessException(ErrorCode.BOOKING_CANNOT_CANCEL, "预约已取消");
        }
        if (booking.getStatus() == 3) {
            throw new BusinessException(ErrorCode.BOOKING_CANNOT_CANCEL, "预约已完成，无法取消");
        }

        int updated = mapper.cancelBooking(bookingId, reason);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.PRIVATE_BOOKING_NOT_FOUND);
        }

        log.info("私教预约取消成功: bookingId={}", bookingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmBooking(Long coachId, Long bookingId) {
        log.info("教练确认预约: coachId={}, bookingId={}", coachId);

        PrivateCoachBooking booking = mapper.selectById(bookingId);
        if (booking == null || Boolean.TRUE.equals(booking.getDeleted())) {
            throw new BusinessException(ErrorCode.PRIVATE_BOOKING_NOT_FOUND);
        }
        if (!booking.getCoachId().equals(coachId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (booking.getStatus() != 0) {
            throw new BusinessException(ErrorCode.BOOKING_STATUS_ERROR, "只能确认待确认的预约");
        }

        mapper.updateStatus(bookingId, 1);
        log.info("预约确认成功: bookingId={}", bookingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeBooking(Long bookingId) {
        log.info("完成预约: bookingId={}", bookingId);

        PrivateCoachBooking booking = mapper.selectById(bookingId);
        if (booking == null || Boolean.TRUE.equals(booking.getDeleted())) {
            throw new BusinessException(ErrorCode.PRIVATE_BOOKING_NOT_FOUND);
        }
        if (booking.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BOOKING_STATUS_ERROR, "只能完成已确认的预约");
        }

        mapper.updateStatus(bookingId, 3);
        log.info("预约完成: bookingId={}", bookingId);
    }

    @Override
    public List<PrivateCoachBookingVO> getMyBookings(Long userId) {
        log.info("获取会员私教预约列表: userId={}", userId);
        List<PrivateCoachBookingVO> list = mapper.selectByUserId(userId);
        list.forEach(this::setStatusDesc);
        return list;
    }

    @Override
    public List<PrivateCoachBookingVO> getCoachBookingsByDate(Long coachId, LocalDate date) {
        log.info("获取教练预约: coachId={}, date={}", coachId, date);
        List<PrivateCoachBookingVO> list = mapper.selectByCoachIdAndDate(coachId, date);
        list.forEach(this::setStatusDesc);
        return list;
    }

    @Override
    public List<PrivateCoachBookingVO> getCoachBookingsByDateRange(Long coachId, LocalDate startDate,
            LocalDate endDate) {
        log.info("获取教练预约范围: coachId={}, {} ~ {}", coachId, startDate, endDate);
        List<PrivateCoachBookingVO> list = mapper.selectByCoachIdAndDateRange(coachId, startDate, endDate);
        list.forEach(this::setStatusDesc);
        return list;
    }

    private void checkTimeConflict(Long coachId, LocalDate date, java.time.LocalTime startTime,
            java.time.LocalTime endTime) {
        List<PrivateCoachBookingVO> bookings = mapper.selectByCoachIdAndDate(coachId, date);
        for (PrivateCoachBookingVO existing : bookings) {
            if (existing.getStatus() == 2 || existing.getStatus() == 3) {
                continue;
            }
            boolean overlaps = startTime.isBefore(existing.getEndTime()) && endTime.isAfter(existing.getStartTime());
            if (overlaps) {
                throw new BusinessException(ErrorCode.PRIVATE_BOOKING_TIME_CONFLICT);
            }
        }
    }

    private void setStatusDesc(PrivateCoachBookingVO vo) {
        if (vo.getStatus() == null) {
            return;
        }
        switch (vo.getStatus()) {
            case 0 -> vo.setStatusDesc("待确认");
            case 1 -> vo.setStatusDesc("已确认");
            case 2 -> vo.setStatusDesc("已取消");
            case 3 -> vo.setStatusDesc("已完成");
            default -> vo.setStatusDesc("未知");
        }
    }
}