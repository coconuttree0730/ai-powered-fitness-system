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
import com.fitness.modules.course.mapper.CourseMapper;
import com.fitness.modules.course.model.entity.Course;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 预约服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingMapper bookingMapper;
    private final CourseMapper courseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBooking(Long userId, BookingDTO dto) {
        log.info("创建预约: userId={}, courseId={}", userId, dto.getCourseId());

        // 1. 检查课程是否存在
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null || Boolean.TRUE.equals(course.getDeleted())) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        // 2. 检查课程是否已开始（周期性课程：对比今天星期几+当前时间）
        if (isCourseStarted(course)) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND, "课程已开始，无法预约");
        }

        // 3. 检查课程是否已满
        if (course.getBookedCount() >= course.getCapacity()) {
            throw new BusinessException(ErrorCode.COURSE_FULL);
        }

        // 4. 检查用户是否已预约该课程
        int existingBooking = bookingMapper.countByUserIdAndCourseId(userId, dto.getCourseId());
        if (existingBooking > 0) {
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_EXISTS);
        }

        // 5. 增加课程预约人数（当前预约数）
        int updated = courseMapper.updateBookedCount(dto.getCourseId(), 1);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.COURSE_FULL);
        }

        // 6. 原子性增加总预约人数（统计所有预约过该课程的独立会员数量）
        courseMapper.incrementTotalBookingCount(dto.getCourseId());

        // 7. 创建预约记录
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setCourseId(dto.getCourseId());
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(0); // 待确认

        bookingMapper.insert(booking);

        log.info("预约创建成功: bookingId={}", booking.getId());
        return booking.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long userId, Long bookingId, BookingCancelDTO dto) {
        log.info("取消预约: userId={}, bookingId={}", userId, bookingId);

        // 1. 检查预约是否存在
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null || Boolean.TRUE.equals(booking.getDeleted())) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }

        // 2. 检查是否是自己的预约
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 3. 检查预约状态是否可以取消
        if (booking.getStatus() == 2) {
            throw new BusinessException(ErrorCode.BOOKING_CANNOT_CANCEL, "预约已取消");
        }

        // 4. 获取课程信息，检查课程是否已开始
        Course course = courseMapper.selectById(booking.getCourseId());
        if (course != null && isCourseStarted(course)) {
            throw new BusinessException(ErrorCode.BOOKING_CANNOT_CANCEL, "课程已开始，无法取消");
        }

        // 5. 取消预约
        int updated = bookingMapper.cancelBooking(bookingId, dto.getCancelReason());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BOOKING_CANNOT_CANCEL);
        }

        // 6. 减少课程预约人数
        courseMapper.updateBookedCount(booking.getCourseId(), -1);

        log.info("预约取消成功: bookingId={}", bookingId);
    }

    @Override
    public List<BookingListVO> getMyBookings(Long userId) {
        log.info("获取我的预约列表: userId={}", userId);
        List<BookingListVO> list = bookingMapper.selectBookingListByUserId(userId);
        // 设置状态描述
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

        // 检查是否是自己的预约（管理员可以查看所有）
        if (!bookingVO.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        setStatusDesc(bookingVO);
        return bookingVO;
    }

    @Override
    public Page<BookingListVO> getBookingList(BookingQueryDTO query) {
        log.info("查询预约列表: userId={}, courseId={}, status={}",
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

        int updated = bookingMapper.updateStatus(bookingId, 1); // 已确认
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

        // 更新预约状态为已取消
        int updated = bookingMapper.updateStatus(bookingId, 2); // 已取消
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BOOKING_STATUS_ERROR);
        }

        // 减少课程预约人数
        courseMapper.updateBookedCount(booking.getCourseId(), -1);

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

        int updated = bookingMapper.updateStatus(bookingId, 3); // 已完成
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BOOKING_STATUS_ERROR);
        }

        log.info("预约完成: bookingId={}", bookingId);
    }

    /**
     * 设置状态描述
     */
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

    /**
     * 设置状态描述
     */
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

    /**
     * 判断周期性课程是否已开始
     * 逻辑：对比当前星期几 + 当前时间 与 课程的 dayOfWeek + startTime
     *
     * @param course 课程实体
     * @return true-课程已开始或正在进行中
     */
    private boolean isCourseStarted(Course course) {
        if (course.getDayOfWeek() == null || course.getStartTime() == null) {
            return false;
        }

        DayOfWeek today = LocalDateTime.now().getDayOfWeek();
        int todayValue = today.getValue(); // MONDAY=1, ..., SUNDAY=7
        LocalTime now = LocalTime.now();

        // 今天不是课程日 → 未开始
        if (todayValue != course.getDayOfWeek()) {
            return false;
        }

        // 今天是课程日，检查当前时间是否已过上课时间
        return !now.isBefore(course.getStartTime());
    }
}
