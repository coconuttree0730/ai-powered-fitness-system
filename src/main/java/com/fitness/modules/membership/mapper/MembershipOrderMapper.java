package com.fitness.modules.membership.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.membership.model.entity.MembershipOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MembershipOrderMapper extends BaseMapper<MembershipOrder> {

    @Select("SELECT * FROM membership_order WHERE order_no = #{orderNo}")
    MembershipOrder selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM membership_order WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<MembershipOrder> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM membership_order WHERE status = 'PENDING' AND create_time < #{time}")
    List<MembershipOrder> selectTimeoutOrders(@Param("time") java.time.LocalDateTime time);
}
