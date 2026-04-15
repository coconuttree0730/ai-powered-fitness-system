package com.fitness.integration.payment.service;

import java.math.BigDecimal;
import java.util.Map;

public interface AlipayService {

    /**
     * 创建支付宝支付订单
     *
     * @param orderNo     订单号
     * @param amount      支付金额
     * @param subject     订单标题
     * @param body        订单描述
     * @return 支付表单HTML
     */
    String createPayOrder(String orderNo, BigDecimal amount, String subject, String body);

    /**
     * 验证支付宝异步通知签名
     *
     * @param params 通知参数
     * @return 验签结果
     */
    boolean verifyNotify(Map<String, String> params);

    /**
     * 查询订单支付状态
     *
     * @param orderNo 订单号
     * @return 支付状态
     */
    String queryOrderStatus(String orderNo);

    /**
     * 退款
     *
     * @param orderNo      订单号
     * @param refundNo     退款单号
     * @param refundAmount 退款金额
     * @param reason       退款原因
     * @return 退款结果
     */
    boolean refund(String orderNo, String refundNo, BigDecimal refundAmount, String reason);
}
