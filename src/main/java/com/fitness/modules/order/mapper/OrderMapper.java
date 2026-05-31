package com.fitness.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.order.model.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM orders WHERE status = 'PENDING' AND create_time < #{time}")
    List<Order> selectTimeoutOrders(@Param("time") LocalDateTime time);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} AND order_type = #{orderType} AND status = 'PAID' ORDER BY pay_time DESC LIMIT 1")
    Order selectLatestPaidOrderByType(@Param("userId") Long userId, @Param("orderType") String orderType);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} AND order_type = #{orderType} AND status = 'PENDING' ORDER BY create_time DESC LIMIT 1")
    Order selectLatestPendingOrderByType(@Param("userId") Long userId, @Param("orderType") String orderType);

    @Select("<script>"
            + "SELECT * FROM orders WHERE order_type = #{orderType}"
            + "<if test='status != null and status != \"\"'> AND status = #{status}</if>"
            + "<if test='keyword != null and keyword != \"\"'> AND order_no LIKE CONCAT('%', #{keyword}, '%')</if>"
            + " ORDER BY create_time DESC"
            + "</script>")
    List<Order> selectByType(@Param("orderType") String orderType,
                             @Param("status") String status,
                             @Param("keyword") String keyword);

    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM orders "
            + "WHERE order_type = 'MEMBERSHIP' AND status IN ('PAID', 'ACTIVATED') AND pay_time >= #{monthStart}")
    BigDecimal sumMembershipRevenueSince(@Param("monthStart") LocalDateTime monthStart);

    @Select("SELECT COUNT(1) FROM orders WHERE order_type = 'MEMBERSHIP' AND status IN ('PAID', 'ACTIVATED')")
    long countPaidMembershipOrders();

    @Select("SELECT COUNT(1) FROM orders WHERE order_type = 'MEMBERSHIP'")
    long countMembershipOrders();

    @Select("<script>"
            + "SELECT * FROM orders WHERE 1=1"
            + "<if test='orderType != null and orderType != \"\"'> AND order_type = #{orderType}</if>"
            + "<if test='status != null and status != \"\"'> AND status = #{status}</if>"
            + "<if test='keyword != null and keyword != \"\"'> AND order_no LIKE CONCAT('%', #{keyword}, '%')</if>"
            + "<if test='startTime != null'> AND create_time &gt;= #{startTime}</if>"
            + "<if test='endTime != null'> AND create_time &lt;= #{endTime}</if>"
            + " ORDER BY create_time DESC"
            + "</script>")
    IPage<Order> selectAdminOrders(Page<Order> page,
                                   @Param("orderType") String orderType,
                                   @Param("status") String status,
                                   @Param("keyword") String keyword,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(1) FROM orders WHERE create_time >= #{todayStart}")
    long countTodayOrders(@Param("todayStart") LocalDateTime todayStart);

    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM orders "
            + "WHERE status IN ('PAID', 'COMPLETED', 'ACTIVATED', 'SHIPPED') AND pay_time >= #{todayStart}")
    BigDecimal sumTodayRevenue(@Param("todayStart") LocalDateTime todayStart);

    @Select("SELECT COUNT(1) FROM orders WHERE status = 'PENDING'")
    long countPendingOrders();

    @Select("SELECT COUNT(1) FROM orders WHERE order_type = 'PRODUCT'")
    long countProductOrders();
}
