package com.fitness.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.order.model.entity.MembershipOrderExt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MembershipOrderExtMapper extends BaseMapper<MembershipOrderExt> {

    @Select("SELECT * FROM membership_order_ext WHERE order_id = #{orderId}")
    MembershipOrderExt selectByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT COUNT(1) FROM membership_order_ext WHERE card_id = #{cardId}")
    long countByCardId(@Param("cardId") Long cardId);
}
