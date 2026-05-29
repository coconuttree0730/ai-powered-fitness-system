package com.fitness.integration.payment.controller;

import com.fitness.modules.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment/alipay")
@RequiredArgsConstructor
public class AlipayNotifyController {

    private final OrderService orderService;

    /**
     * 支付宝异步通知回调
     */
    @PostMapping("/notify")
    public String alipayNotify(HttpServletRequest request) {
        log.info("收到支付宝异步通知");
        log.debug("支付宝通知请求: remoteAddr={}, contentType={}", request.getRemoteAddr(), request.getContentType());

        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        log.debug("支付宝通知参数数量: {}", requestParams.size());

        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(key, valueStr);
            log.debug("支付宝通知参数: {}={}", key, valueStr);
        }

        log.info("支付宝通知参数汇总: out_trade_no={}, trade_no={}, trade_status={}, total_amount={}",
                params.get("out_trade_no"), params.get("trade_no"), params.get("trade_status"),
                params.get("total_amount"));

        try {
            orderService.handleAlipayCallback(params);
            log.info("支付宝回调处理成功，返回success");
            return "success";
        } catch (Exception e) {
            log.error("处理支付宝通知失败: errorMsg={}", e.getMessage(), e);
            return "fail";
        }
    }
}