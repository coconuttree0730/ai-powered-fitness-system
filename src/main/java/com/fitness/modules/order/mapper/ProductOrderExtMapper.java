package com.fitness.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.order.model.entity.ProductOrderExt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductOrderExtMapper extends BaseMapper<ProductOrderExt> {

    @Select("SELECT * FROM product_order_ext WHERE order_id = #{orderId}")
    ProductOrderExt selectByOrderId(@Param("orderId") Long orderId);

    @Select("<script>SELECT * FROM product_order_ext WHERE order_id IN <foreach collection='orderIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<ProductOrderExt> selectByOrderIds(@Param("orderIds") List<Long> orderIds);

    @Select("SELECT * FROM product_order_ext WHERE pickup_code = #{pickupCode}")
    ProductOrderExt selectByPickupCode(@Param("pickupCode") String pickupCode);
}
