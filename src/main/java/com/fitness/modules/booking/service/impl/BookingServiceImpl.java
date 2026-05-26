package com.fitness.modules.booking.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.cache.RedisCacheNames;
import com.fitness.common.cache.RedisTemplateCacheSupport;
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
import com.fitness.modules.course.mapper.CourseMapper;
import com.fitness.modules.course.mapper.CourseSessionMapper;
import com.fitness.modules.course.model.vo.CourseSessionVO;
import com.fitness.modules.ranking.service.RedisRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final CourseMapper courseMapper;
    private final BookingReservationService bookingReservationService;
    private final RedisRankingService redisRankingService;
    private final RedisTemplateCacheSupport redisTemplateCacheSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = RedisCacheNames.COURSE_PUBLIC_LIST, allEntries = true),
            @CacheEvict(value = RedisCacheNames.COURSE_HOME_CATEGORIES, allEntries = true),
            @CacheEvict(value = RedisCacheNames.COURSE_HOME_CARDS, allEntries = true),
            @CacheEvict(value = RedisCacheNames.UPCOMING_SESSIONS, allEntries = true)
    })
    public Long createBooking(Long userId, BookingDTO dto) {
        log.info("创建预约: userId={}, sessionId={}", userId, dto.getSessionId());

        CourseSessionVO session = sessionMapper.selectSessionDetail(dto.getSessionId());
        if (session == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND, "课程不存在");
        }
        if (isSessionStarted(session)) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND, "课程已开始，无法预约");
        }

        boolean reservedInRedis = false;
        try {
            BookingReservationResult reservationResult =
                    bookingReservationService.tryReserve(dto.getSessionId(), userId, session);
            if (reservationResult.status() == BookingReservationResult.Status.ALREADY_BOOKED) {
                throw new BusinessException(ErrorCode.BOOKING_ALREADY_EXISTS, "您已预约这节课");
            }
            if (reservationResult.status() == BookingReservationResult.Status.FULL) {
                throw new BusinessException(ErrorCode.COURSE_FULL, "名额已满，请选择其他时间");
            }
            reservedInRedis = reservationResult.successResult();
        } catch (BusinessException exception) {
            throw exception;
        } catch (DataAccessException exception) {
            log.warn("Redis预约预占失败，降级走数据库流程: sessionId={}, userId={}", dto.getSessionId(), userId, exception);
        }

        try {
            Long bookingId = persistBooking(userId, dto, session, reservedInRedis);
            clearCourseCaches();
            return bookingId;
        } catch (DataIntegrityViolationException exception) {
            if (reservedInRedis) {
                bookingReservationService.releaseReservation(dto.getSessionId(), userId, session);
            }
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_EXISTS, "您已预约这节课");
        } catch (RuntimeException exception) {
            if (reservedInRedis) {
                bookingReservationService.releaseReservation(dto.getSessionId(), userId, session);
            }
            throw exception;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = RedisCacheNames.COURSE_PUBLIC_LIST, allEntries = true),
            @CacheEvict(value = RedisCacheNames.COURSE_HOME_CATEGORIES, allEntries = true),
            @CacheEvict(value = RedisCacheNames.COURSE_HOME_CARDS, allEntries = true),
            @CacheEvict(value = RedisCacheNames.UPCOMING_SESSIONS, allEntries = true)
    })
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

        CourseSessionVO session = null;
        if (booking.getSessionId() != null) {
            session = sessionMapper.selectSessionDetail(booking.getSessionId());
            if (session != null && isSessionStarted(session)) {
                throw new BusinessException(ErrorCode.BOOKING_CANNOT_CANCEL, "课程已开始，无法取消");
            }
        }

        int updated = bookingMapper.cancelBooking(bookingId, dto.getCancelReason());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BOOKING_CANNOT_CANCEL);
        }

        if (booking.getSessionId() != null) {
            int stockUpdated = sessionMapper.updateBookedCount(booking.getSessionId(), -1);
            if (stockUpdated == 0) {
                throw new BusinessException(ErrorCode.BOOKING_CANNOT_CANCEL, "预约名额回滚失败");
            }
            if (session != null) {
                releaseReservationQuietly(booking.getSessionId(), userId, session);
            }
        }

        clearCourseCaches();
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

    private Long persistBooking(Long userId, BookingDTO dto, CourseSessionVO session, boolean reservedInRedis) {
        if (!reservedInRedis) {
            int existing = bookingMapper.countByUserIdAndSessionId(userId, dto.getSessionId());
            if (existing > 0) {
                throw new BusinessException(ErrorCode.BOOKING_ALREADY_EXISTS, "您已预约这节课");
            }
        }

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setCourseId(session.getCourseId());
        booking.setSessionId(dto.getSessionId());
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(1);
        bookingMapper.insert(booking);

        int updated = sessionMapper.updateBookedCount(dto.getSessionId(), 1);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.COURSE_FULL, "名额已满，请选择其他时间");
        }

        courseMapper.incrementTotalBookingCount(session.getCourseId());
        redisRankingService.incrementCourseBookingScore(session.getCourseId(), 1D);
        redisRankingService.incrementCoachBookingScore(session.getCoachId(), 1D);

        log.info("预约创建成功: bookingId={}, sessionId={}", booking.getId(), dto.getSessionId());
        return booking.getId();
    }

    private void releaseReservationQuietly(Long sessionId, Long userId, CourseSessionVO session) {
        try {
            bookingReservationService.releaseReservation(sessionId, userId, session);
        } catch (DataAccessException exception) {
            log.warn("Redis预约回滚失败，等待后续缓存重建: sessionId={}, userId={}", sessionId, userId, exception);
        }
    }

    private void setStatusDesc(BookingListVO vo) {
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

    private void setStatusDesc(BookingVO vo) {
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

    private boolean isSessionStarted(CourseSessionVO session) {
        if (session.getSessionDate() == null || session.getStartTime() == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (session.getSessionDate().isBefore(today)) {
            return true;
        }
        if (session.getSessionDate().isAfter(today)) {
            return false;
        }
        return !now.isBefore(session.getStartTime());
    }

    private void clearCourseCaches() {
        redisTemplateCacheSupport.evictAll(RedisCacheNames.COURSE_PUBLIC_LIST);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.COURSE_HOME_CATEGORIES);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.COURSE_HOME_CARDS);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.UPCOMING_SESSIONS);
    }
}
