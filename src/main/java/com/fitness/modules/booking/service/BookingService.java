package com.fitness.modules.booking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.booking.model.dto.BookingCancelDTO;
import com.fitness.modules.booking.model.dto.BookingDTO;
import com.fitness.modules.booking.model.dto.BookingQueryDTO;
import com.fitness.modules.booking.model.vo.BookingListVO;
import com.fitness.modules.booking.model.vo.BookingVO;

import java.util.List;

/**
 * 预约服务接口
 */
public interface BookingService {

    /**
     * 创建预约
     *
     * @param userId 用户ID
     * @param dto    预约信息
     * @return 预约ID
     */
    Long createBooking(Long userId, BookingDTO dto);

    /**
     * 取消预约
     *
     * @param userId    用户ID
     * @param bookingId 预约ID
     * @param dto       取消信息
     */
    void cancelBooking(Long userId, Long bookingId, BookingCancelDTO dto);

    /**
     * 获取我的预约列表
     *
     * @param userId 用户ID
     * @return 预约列表
     */
    List<BookingListVO> getMyBookings(Long userId);

    /**
     * 获取预约详情
     *
     * @param userId    用户ID
     * @param bookingId 预约ID
     * @return 预约详情
     */
    BookingVO getBookingDetail(Long userId, Long bookingId);

    /**
     * 分页查询预约列表（管理端）
     *
     * @param query 查询条件
     * @return 分页结果
     */
    Page<BookingListVO> getBookingList(BookingQueryDTO query);

    /**
     * 完成预约
     *
     * @param bookingId 预约ID
     */
    void completeBooking(Long bookingId);
}
