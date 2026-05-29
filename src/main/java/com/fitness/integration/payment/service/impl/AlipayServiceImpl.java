package com.fitness.integration.payment.service.impl;

import com.fitness.integration.payment.exception.PaymentIntegrationException;
import com.fitness.integration.payment.gateway.AlipayGateway;
import com.fitness.integration.payment.model.AlipayCreateOrderResult;
import com.fitness.integration.payment.model.AlipayQueryResult;
import com.fitness.integration.payment.model.AlipayRefundResult;
import com.fitness.integration.payment.service.AlipayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayServiceImpl implements AlipayService {

    private final AlipayGateway alipayGateway;

    @Override
    public String createPayOrder(String orderNo, BigDecimal amount, String subject, String body) {
        log.debug("开始创建支付宝订单: orderNo={}, amount={}, subject={}", orderNo, amount, subject);
        AlipayCreateOrderResult result = alipayGateway.createWapOrder(orderNo, amount, subject, body);
        if (!result.success()) {
            log.error("支付宝订单创建失败: orderNo={}, code={}, msg={}", orderNo, result.code(), result.message());
            throw new PaymentIntegrationException("支付宝订单创建失败: " + result.message());
        }
        return result.body();
    }

    @Override
    public boolean verifyNotify(Map<String, String> params) {
        log.debug("开始验证支付宝回调签名, params={}", params);
        try {
            boolean signVerified = alipayGateway.verifyNotify(params);
            log.info("支付宝回调验签结果: outTradeNo={}, signVerified={}", params.get("out_trade_no"), signVerified);
            return signVerified;
        } catch (PaymentIntegrationException e) {
            log.error("支付宝回调验签异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String queryOrderStatus(String orderNo) {
        log.debug("开始查询支付宝订单状态: orderNo={}", orderNo);
        try {
            AlipayQueryResult result = alipayGateway.queryOrder(orderNo);
            if (!result.success()) {
                log.error("支付宝订单查询失败: orderNo={}, code={}, msg={}", orderNo, result.code(), result.message());
                return null;
            }
            return result.tradeStatus();
        } catch (PaymentIntegrationException e) {
            log.error("支付宝订单查询异常: orderNo={}, message={}", orderNo, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean refund(String orderNo, String refundNo, BigDecimal refundAmount, String reason) {
        log.debug("开始支付宝退款: orderNo={}, refundNo={}, refundAmount={}, reason={}",
                orderNo, refundNo, refundAmount, reason);
        try {
            AlipayRefundResult result = alipayGateway.refund(orderNo, refundNo, refundAmount, reason);
            if (!result.success()) {
                log.error("支付宝退款失败: orderNo={}, refundNo={}, code={}, msg={}",
                        orderNo, refundNo, result.code(), result.message());
                return false;
            }
            return true;
        } catch (PaymentIntegrationException e) {
            log.error("支付宝退款异常: orderNo={}, refundNo={}, message={}",
                    orderNo, refundNo, e.getMessage(), e);
            return false;
        }
    }
}
