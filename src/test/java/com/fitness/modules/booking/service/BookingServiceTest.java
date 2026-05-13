package com.fitness.modules.booking.service;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.booking.mapper.BookingMapper;
import com.fitness.modules.booking.model.dto.BookingCancelDTO;
import com.fitness.modules.booking.model.dto.BookingDTO;
import com.fitness.modules.booking.model.entity.Booking;
import com.fitness.modules.booking.service.impl.BookingServiceImpl;
import com.fitness.modules.course.mapper.CourseMapper;
import com.fitness.modules.course.model.entity.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 预约服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Course validCourse;
    private Booking validBooking;

    @BeforeEach
    void setUp() {
        // 准备有效的课程数据
        validCourse = new Course();
        validCourse.setId(1L);
        validCourse.setCourseName("瑜伽课程");
        validCourse.setCapacity(20);
        validCourse.setBookedCount(5);
        validCourse.setStartTime(LocalTime.now());
        validCourse.setDeleted(false);

        // 准备有效的预约数据
        validBooking = new Booking();
        validBooking.setId(1L);
        validBooking.setUserId(1L);
        validBooking.setCourseId(1L);
        validBooking.setStatus(0);
        validBooking.setDeleted(false);
    }

    @Test
    void createBooking_Success() {
        // Given
        Long userId = 1L;
        BookingDTO dto = new BookingDTO();
        dto.setCourseId(1L);

        when(courseMapper.selectById(1L)).thenReturn(validCourse);
        when(bookingMapper.countByUserIdAndCourseId(userId, 1L)).thenReturn(0);
        when(courseMapper.updateBookedCount(1L, 1)).thenReturn(1);
        when(bookingMapper.insert(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return 1;
        });

        // When
        Long bookingId = bookingService.createBooking(userId, dto);

        // Then
        assertNotNull(bookingId);
        assertEquals(1L, bookingId);
        verify(courseMapper).selectById(1L);
        verify(bookingMapper).countByUserIdAndCourseId(userId, 1L);
        verify(courseMapper).updateBookedCount(1L, 1);
        verify(bookingMapper).insert(any(Booking.class));
    }

    @Test
    void createBooking_CourseNotFound() {
        // Given
        Long userId = 1L;
        BookingDTO dto = new BookingDTO();
        dto.setCourseId(999L);

        when(courseMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.createBooking(userId, dto));
        assertEquals(ErrorCode.COURSE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void createBooking_CourseAlreadyStarted() {
        // Given
        Long userId = 1L;
        BookingDTO dto = new BookingDTO();
        dto.setCourseId(1L);

        Course startedCourse = new Course();
        startedCourse.setId(1L);
        startedCourse.setStartTime(LocalTime.now());
        startedCourse.setDeleted(false);

        when(courseMapper.selectById(1L)).thenReturn(startedCourse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.createBooking(userId, dto));
        assertEquals(ErrorCode.COURSE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void createBooking_CourseFull() {
        // Given
        Long userId = 1L;
        BookingDTO dto = new BookingDTO();
        dto.setCourseId(1L);

        Course fullCourse = new Course();
        fullCourse.setId(1L);
        fullCourse.setCapacity(20);
        fullCourse.setBookedCount(20);
        fullCourse.setStartTime(LocalTime.now());
        fullCourse.setDeleted(false);

        when(courseMapper.selectById(1L)).thenReturn(fullCourse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.createBooking(userId, dto));
        assertEquals(ErrorCode.COURSE_FULL.getCode(), exception.getCode());
    }

    @Test
    void createBooking_AlreadyExists() {
        // Given
        Long userId = 1L;
        BookingDTO dto = new BookingDTO();
        dto.setCourseId(1L);

        when(courseMapper.selectById(1L)).thenReturn(validCourse);
        when(bookingMapper.countByUserIdAndCourseId(userId, 1L)).thenReturn(1);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.createBooking(userId, dto));
        assertEquals(ErrorCode.BOOKING_ALREADY_EXISTS.getCode(), exception.getCode());
    }

    @Test
    void cancelBooking_Success() {
        // Given
        Long userId = 1L;
        Long bookingId = 1L;
        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setCancelReason("临时有事");

        when(bookingMapper.selectById(bookingId)).thenReturn(validBooking);
        when(courseMapper.selectById(1L)).thenReturn(validCourse);
        when(bookingMapper.cancelBooking(bookingId, "临时有事")).thenReturn(1);
        when(courseMapper.updateBookedCount(1L, -1)).thenReturn(1);

        // When
        assertDoesNotThrow(() -> bookingService.cancelBooking(userId, bookingId, dto));

        // Then
        verify(bookingMapper).selectById(bookingId);
        verify(bookingMapper).cancelBooking(bookingId, "临时有事");
        verify(courseMapper).updateBookedCount(1L, -1);
    }

    @Test
    void cancelBooking_NotFound() {
        // Given
        Long userId = 1L;
        Long bookingId = 999L;
        BookingCancelDTO dto = new BookingCancelDTO();

        when(bookingMapper.selectById(bookingId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.cancelBooking(userId, bookingId, dto));
        assertEquals(ErrorCode.BOOKING_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void cancelBooking_NotOwner() {
        // Given
        Long userId = 2L; // 不是预约的拥有者
        Long bookingId = 1L;
        BookingCancelDTO dto = new BookingCancelDTO();

        when(bookingMapper.selectById(bookingId)).thenReturn(validBooking);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.cancelBooking(userId, bookingId, dto));
        assertEquals(ErrorCode.FORBIDDEN.getCode(), exception.getCode());
    }

    @Test
    void cancelBooking_AlreadyCancelled() {
        // Given
        Long userId = 1L;
        Long bookingId = 1L;
        BookingCancelDTO dto = new BookingCancelDTO();

        Booking cancelledBooking = new Booking();
        cancelledBooking.setId(1L);
        cancelledBooking.setUserId(1L);
        cancelledBooking.setCourseId(1L);
        cancelledBooking.setStatus(2); // 已取消
        cancelledBooking.setDeleted(false);

        when(bookingMapper.selectById(bookingId)).thenReturn(cancelledBooking);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.cancelBooking(userId, bookingId, dto));
        assertEquals(ErrorCode.BOOKING_CANNOT_CANCEL.getCode(), exception.getCode());
    }

    @Test
    void confirmBooking_Success() {
        // Given
        Long bookingId = 1L;

        when(bookingMapper.selectById(bookingId)).thenReturn(validBooking);
        when(bookingMapper.updateStatus(bookingId, 1)).thenReturn(1);

        // When
        assertDoesNotThrow(() -> bookingService.confirmBooking(bookingId));

        // Then
        verify(bookingMapper).selectById(bookingId);
        verify(bookingMapper).updateStatus(bookingId, 1);
    }

    @Test
    void confirmBooking_WrongStatus() {
        // Given
        Long bookingId = 1L;

        Booking confirmedBooking = new Booking();
        confirmedBooking.setId(1L);
        confirmedBooking.setStatus(1); // 已确认
        confirmedBooking.setDeleted(false);

        when(bookingMapper.selectById(bookingId)).thenReturn(confirmedBooking);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.confirmBooking(bookingId));
        assertEquals(ErrorCode.BOOKING_STATUS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void rejectBooking_Success() {
        // Given
        Long bookingId = 1L;

        when(bookingMapper.selectById(bookingId)).thenReturn(validBooking);
        when(bookingMapper.updateStatus(bookingId, 2)).thenReturn(1);
        when(courseMapper.updateBookedCount(1L, -1)).thenReturn(1);

        // When
        assertDoesNotThrow(() -> bookingService.rejectBooking(bookingId));

        // Then
        verify(bookingMapper).selectById(bookingId);
        verify(bookingMapper).updateStatus(bookingId, 2);
        verify(courseMapper).updateBookedCount(1L, -1);
    }

    @Test
    void completeBooking_Success() {
        // Given
        Long bookingId = 1L;

        Booking confirmedBooking = new Booking();
        confirmedBooking.setId(1L);
        confirmedBooking.setStatus(1); // 已确认
        confirmedBooking.setDeleted(false);

        when(bookingMapper.selectById(bookingId)).thenReturn(confirmedBooking);
        when(bookingMapper.updateStatus(bookingId, 3)).thenReturn(1);

        // When
        assertDoesNotThrow(() -> bookingService.completeBooking(bookingId));

        // Then
        verify(bookingMapper).selectById(bookingId);
        verify(bookingMapper).updateStatus(bookingId, 3);
    }
}
