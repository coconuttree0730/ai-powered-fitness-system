package com.fitness.modules.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.product.model.dto.CalculatePriceDTO;
import com.fitness.modules.product.model.dto.ProductOrderDTO;
import com.fitness.modules.product.model.entity.ProductOrder;
import com.fitness.modules.product.model.vo.PriceCalculationVO;
import com.fitness.modules.product.model.vo.ProductOrderVO;

import java.util.List;

public interface ProductOrderService extends IService<ProductOrder> {
    
    PriceCalculationVO calculatePrice(CalculatePriceDTO dto, Long userId);
    
    ProductOrderVO createOrder(ProductOrderDTO dto, Long userId);
    
    ProductOrderVO payOrder(String orderNo, String payMethod);
    
    List<ProductOrderVO> getUserOrders(Long userId);
    
    ProductOrderVO getOrderDetail(String orderNo);
    
    void cancelOrder(String orderNo);
    
    void shipOrder(String orderNo, String trackingNo, String carrier);
    
    void completeOrder(String orderNo);
}
