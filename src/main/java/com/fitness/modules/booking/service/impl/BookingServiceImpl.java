package com.fitness.modules.booking.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.booking.mapper.BookingMapper;
import com.fitness.modules.booking.model.dto.BookingCancelDTO;
import com.fitness.modules.booking.model.dto.BookingDTO;
import com.fitness.modules.booking.model.dto.BookingQueryDTO;
import com.fitness.modules.booking.model.entity.Booking;
import com.fitness.modules.booking.model.vo.BookingListVO;
import com.fitness.modules.booking.model.vo.BookingVO;
import com.fitness.modules.booking.service.BookingService;
import com.fitness.modules.course.mapper.CourseSessionMapper;
import com.fitness.modules.course.model.vo.CourseSessionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingMapper bookingMapper;
    private final CourseSessionMapper sessionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBooking(Long userId, BookingDTO dto) {
        log.info("创建预约: userId={}, sessionId={}", userId, dto.getSessionId());

        // 1. 检查课程实例是否存在且可预约
        CourseSessionVO session = sessionMapper.selectSessionDetail(dto.getSessionId());
        if (session == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND, "课程不存在");
        }

        // 2. 检查课程实例是否已开始或已结束
        if (isSessionStarted(session)) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND, "课程已开始，无法预约");
        }

        // 3. 检查是否已满员
        if (session.getBookedCount() >= session.getCapacity()) {
            throw new BusinessException(ErrorCode.COURSE_FULL);
        }

        // 4. 检查用户是否已预约该实例（同一个session不能重复预约）
        int existing = bookingMapper.countByUserIdAndSessionId(userId, dto.getSessionId());
        if (existing > 0) {
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_EXISTS, "您已预约了这节课");
        }

        // 5. 原子性增加实例预约人数
        int updated = sessionMapper.updateBookedCount(dto.getSessionId(), 1);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.COURSE_FULL, "名额已满，请选择其他时间");
        }

        // 6. 创建预约记录（绑定具体实例）
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setCourseId(session.getCourseId());
        booking.setSessionId(dto.getSessionId());
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(0);

        bookingMapper.insert(booking);

        log.info("预约创建成功: bookingId={}, sessionId={}", booking.getId(), dto.getSessionId());
        return booking.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long userId, Long bookingId, BookingCancelDTO dto) {
        log.info("取消预约: userId={}, bookingId={}", userId, bookingId);

        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null || Boolean.TRUE.equals(booking.getDeleted())) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (booking.getStatus() == 2) {
            throw new BusinessException(ErrorCode.BOOKING_CANNOT_CANCEL, "预约已取消");
        }

        // 基于实例判断是否可取消
        if (booking.getSessionId() != null) {
            CourseSessionVO session = sessionMapper.selectSessionDetail(booking.getSessionId());
            if (session != null && isSessionStarted(session)) {
                throw new BusinessException(ErrorCode.BOOKING_CANNOT_CANCEL, "课程已开始，无法取消");
            }
        }

        int updated = bookingMapper.cancelBooking(bookingId, dto.getCancelReason());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BOOKING_CANNOT_CANCEL);
        }

        // 减少实例预约人数
        if (booking.getSessionId() != null) {
            sessionMapper.updateBookedCount(booking.getSessionId(), -1);
        }

        log.info("预约取消成功: bookingId={}", bookingId);
    }

    @Override
    public List<BookingListVO> getMyBookings(Long userId) {
        log.info("获取我的预约列表: userId={}", userId);
        List<BookingListVO> list = bookingMapper.selectBookingListByUserId(userId);
        list.forEach(this::setStatusDesc);
        return list;
    }

    @Override
    public BookingVO getBookingDetail(Long userId, Long bookingId) {
        log.info("获取预约详情: userId={}, bookingId={}", userId, bookingId);

        BookingVO bookingVO = bookingMapper.selectBookingDetail(bookingId);
        if (bookingVO == null) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }
        if (!bookingVO.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        setStatusDesc(bookingVO);
        return bookingVO;
    }

    @Override
    public Page<BookingListVO> getBookingList(BookingQueryDTO query) {
        log.info("查询预约列表: userId={}, sessionId={}, status={}",
                query.getUserId(), query.getCourseId(), query.getStatus());

        Page<BookingListVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<BookingListVO> result = bookingMapper.selectBookingPage(
                page, query.getUserId(), query.getCourseId(), query.getStatus());

        result.getRecords().forEach(this::setStatusDesc);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmBooking(Long bookingId) {
        log.info("确认预约: bookingId={}", bookingId);

        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null || Boolean.TRUE.equals(booking.getDeleted())) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }
        if (booking.getStatus() != 0) {
            throw new BusinessException(ErrorCode.BOOKING_STATUS_ERROR, "只能确认待确认的预约");
        }

        int updated = bookingMapper.updateStatus(bookingId, 1);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BOOKING_STATUS_ERROR);
        }

        log.info("预约确认成功: bookingId={}", bookingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectBooking(Long bookingId) {
        log.info("拒绝预约: bookingId={}", bookingId);

        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null || Boolean.TRUE.equals(booking.getDeleted())) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }
        if (booking.getStatus() != 0) {
            throw new BusinessException(ErrorCode.BOOKING_STATUS_ERROR, "只能拒绝待确认的预约");
        }

        int updated = bookingMapper.updateStatus(bookingId, 2);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BOOKING_STATUS_ERROR);
        }

        if (booking.getSessionId() != null) {
            sessionMapper.updateBookedCount(booking.getSessionId(), -1);
        }

        log.info("预约拒绝成功: bookingId={}", bookingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeBooking(Long bookingId) {
        log.info("完成预约: bookingId={}", bookingId);

        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null || Boolean.TRUE.equals(booking.getDeleted())) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }
        if (booking.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BOOKING_STATUS_ERROR, "只能完成已确认的预约");
        }

        int updated = bookingMapper.updateStatus(bookingId, 3);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BOOKING_STATUS_ERROR);
        }

        log.info("预约完成: bookingId={}", bookingId);
    }

    private void setStatusDesc(BookingListVO vo) {
        if (vo.getStatus() == null) return;
        switch (vo.getStatus()) {
            case 0 -> vo.setStatusDesc("待确认");
            case 1 -> vo.setStatusDesc("已确认");
            case 2 -> vo.setStatusDesc("已取消");
            case 3 -> vo.setStatusDesc("已完成");
            default -> vo.setStatusDesc("未知");
        }
    }

    private void setStatusDesc(BookingVO vo) {
        if (vo.getStatus() == null) return;
        switch (vo.getStatus()) {
            case 0 -> vo.setStatusDesc("待确认");
            case 1 -> vo.setStatusDesc("已确认");
            case 2 -> vo.setStatusDesc("已取消");
            case 3 -> vo.setStatusDesc("已完成");
            default -> vo.setStatusDesc("未知");
        }
    }

    /**
     * 判断课程实例是否已开始
     * 基于具体的 session_date + start_time 判断
     */
    private boolean isSessionStarted(CourseSessionVO session) {
        if (session.getSessionDate() == null || session.getStartTime() == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 实例日期在今天之前 → 已结束
        if (session.getSessionDate().isBefore(today)) {
            return true;
        }
        // 实例日期在今天之后 → 未开始
        if (session.getSessionDate().isAfter(today)) {
            return false;
        }
        // 今天就是上课日 → 比较时间
        return !now.isBefore(session.getStartTime());
    }
}
