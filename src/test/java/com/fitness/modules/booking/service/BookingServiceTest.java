package com.fitness.modules.booking.service;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.booking.mapper.BookingMapper;
import com.fitness.modules.booking.model.dto.BookingCancelDTO;
import com.fitness.modules.booking.model.dto.BookingDTO;
import com.fitness.modules.booking.model.entity.Booking;
import com.fitness.modules.booking.service.impl.BookingReservationResult;
import com.fitness.modules.booking.service.impl.BookingReservationService;
import com.fitness.modules.booking.service.impl.BookingServiceImpl;
import com.fitness.modules.course.mapper.CourseMapper;
import com.fitness.modules.course.mapper.CourseSessionMapper;
import com.fitness.modules.course.model.vo.CourseSessionVO;
import com.fitness.common.cache.RedisTemplateCacheSupport;
import com.fitness.modules.ranking.service.RedisRankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private CourseSessionMapper sessionMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private BookingReservationService bookingReservationService;

    @Mock
    private RedisRankingService redisRankingService;

    @Mock
    private RedisTemplateCacheSupport redisTemplateCacheSupport;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private CourseSessionVO validSession;
    private Booking validBooking;

    @BeforeEach
    void setUp() {
        validSession = new CourseSessionVO();
        validSession.setId(11L);
        validSession.setCourseId(21L);
        validSession.setCoachId(31L);
        validSession.setCapacity(20);
        validSession.setBookedCount(5);
        validSession.setSessionDate(LocalDate.now().plusDays(1));
        validSession.setStartTime(LocalTime.of(10, 0));
        validSession.setEndTime(LocalTime.of(11, 0));

        validBooking = new Booking();
        validBooking.setId(101L);
        validBooking.setUserId(1L);
        validBooking.setCourseId(21L);
        validBooking.setSessionId(11L);
        validBooking.setStatus(0);
        validBooking.setDeleted(false);
    }

    @Test
    void createBookingSuccess() {
        BookingDTO dto = new BookingDTO();
        dto.setSessionId(11L);

        when(sessionMapper.selectSessionDetail(11L)).thenReturn(validSession);
        when(bookingReservationService.tryReserve(eq(11L), eq(1L), any(CourseSessionVO.class)))
                .thenReturn(BookingReservationResult.success());
        when(sessionMapper.updateBookedCount(11L, 1)).thenReturn(1);
        when(bookingMapper.insert(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1001L);
            return 1;
        });
        when(courseMapper.incrementTotalBookingCount(21L)).thenReturn(1);

        Long bookingId = bookingService.createBooking(1L, dto);

        assertEquals(1001L, bookingId);
        verify(bookingReservationService).tryReserve(eq(11L), eq(1L), any(CourseSessionVO.class));
        verify(sessionMapper).updateBookedCount(11L, 1);
        verify(courseMapper).incrementTotalBookingCount(21L);
        verify(redisRankingService).incrementCourseBookingScore(21L, 1D);
        verify(redisRankingService).incrementCoachBookingScore(31L, 1D);
    }

    @Test
    void createBookingDuplicateReservationRejectedByRedis() {
        BookingDTO dto = new BookingDTO();
        dto.setSessionId(11L);

        when(sessionMapper.selectSessionDetail(11L)).thenReturn(validSession);
        when(bookingReservationService.tryReserve(eq(11L), eq(1L), any(CourseSessionVO.class)))
                .thenReturn(BookingReservationResult.alreadyBooked());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.createBooking(1L, dto));

        assertEquals(ErrorCode.BOOKING_ALREADY_EXISTS.getCode(), exception.getCode());
        verify(bookingMapper, never()).insert(any(Booking.class));
    }

    @Test
    void createBookingFullReservationRejectedByRedis() {
        BookingDTO dto = new BookingDTO();
        dto.setSessionId(11L);

        when(sessionMapper.selectSessionDetail(11L)).thenReturn(validSession);
        when(bookingReservationService.tryReserve(eq(11L), eq(1L), any(CourseSessionVO.class)))
                .thenReturn(BookingReservationResult.full());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.createBooking(1L, dto));

        assertEquals(ErrorCode.COURSE_FULL.getCode(), exception.getCode());
        verify(bookingMapper, never()).insert(any(Booking.class));
    }

    @Test
    void createBookingDuplicateViolationTriggersCompensation() {
        BookingDTO dto = new BookingDTO();
        dto.setSessionId(11L);

        when(sessionMapper.selectSessionDetail(11L)).thenReturn(validSession);
        when(bookingReservationService.tryReserve(eq(11L), eq(1L), any(CourseSessionVO.class)))
                .thenReturn(BookingReservationResult.success());
        when(sessionMapper.updateBookedCount(11L, 1)).thenReturn(1);
        when(bookingMapper.insert(any(Booking.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.createBooking(1L, dto));

        assertEquals(ErrorCode.BOOKING_ALREADY_EXISTS.getCode(), exception.getCode());
        verify(bookingReservationService).releaseReservation(11L, 1L, validSession);
        verify(sessionMapper, never()).updateBookedCount(11L, -1);
    }

    @Test
    void cancelBookingSuccessReleasesRedisReservation() {
        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setCancelReason("changed");

        when(bookingMapper.selectById(101L)).thenReturn(validBooking);
        when(sessionMapper.selectSessionDetail(11L)).thenReturn(validSession);
        when(bookingMapper.cancelBooking(101L, "changed")).thenReturn(1);
        when(sessionMapper.updateBookedCount(11L, -1)).thenReturn(1);

        assertDoesNotThrow(() -> bookingService.cancelBooking(1L, 101L, dto));

        verify(sessionMapper).updateBookedCount(11L, -1);
        verify(bookingReservationService).releaseReservation(11L, 1L, validSession);
    }
}
