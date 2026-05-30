package com.fitness.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.order.model.entity.CoachPackageOrderExt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CoachPackageOrderExtMapper extends BaseMapper<CoachPackageOrderExt> {

    @Select("SELECT * FROM coach_package_order_ext WHERE order_id = #{orderId}")
    CoachPackageOrderExt selectByOrderId(@Param("orderId") Long orderId);

    @Select("<script>SELECT * FROM coach_package_order_ext WHERE order_id IN <foreach collection='orderIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<CoachPackageOrderExt> selectByOrderIds(@Param("orderIds") List<Long> orderIds);
}