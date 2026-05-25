package com.fitness.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.product.model.entity.ProductOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductOrderMapper extends BaseMapper<ProductOrder> {
    
    @Select("SELECT * FROM product_order WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ProductOrder> selectByUserId(@Param("userId") Long userId);
    
    @Select("SELECT * FROM product_order WHERE order_no = #{orderNo}")
    ProductOrder selectByOrderNo(@Param("orderNo") String orderNo);
}
