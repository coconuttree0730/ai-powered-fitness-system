package com.fitness.modules.product.job;

import com.fitness.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductOrderTimeoutJob {

    private final OrderService orderService;

    @Scheduled(fixedRate = 60 * 1000)
    public void handleTimeoutOrders() {
        log.info("开始检查商品超时订单...");
        try {
            orderService.handleTimeoutOrders();
            log.info("商品超时订单检查完成");
        } catch (Exception e) {
            log.error("处理商品超时订单异常", e);
        }
    }
}