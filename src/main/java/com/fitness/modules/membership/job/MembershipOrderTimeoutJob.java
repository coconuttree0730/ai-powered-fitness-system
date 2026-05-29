package com.fitness.modules.membership.job;

import com.fitness.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipOrderTimeoutJob {

    private final OrderService orderService;

    /**
     * 每5分钟检查一次超时订单
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void handleTimeoutOrders() {
        log.info("开始检查超时订单...");
        try {
            orderService.handleTimeoutOrders();
            log.info("超时订单检查完成");
        } catch (Exception e) {
            log.error("处理超时订单异常", e);
        }
    }
}