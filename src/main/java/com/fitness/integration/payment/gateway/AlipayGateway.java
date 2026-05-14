package com.fitness.integration.payment.gateway;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.fitness.integration.payment.config.AlipayProperties;
import com.fitness.integration.payment.exception.PaymentIntegrationException;
import com.fitness.integration.payment.model.AlipayCreateOrderResult;
import com.fitness.integration.payment.model.AlipayQueryResult;
import com.fitness.integration.payment.model.AlipayRefundResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AlipayGateway {

    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;

    public AlipayCreateOrderResult createPageOrder(String orderNo, BigDecimal amount, String subject, String body) {
        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayProperties.getNotifyUrl());
            request.setReturnUrl(alipayProperties.getReturnUrl());

            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(orderNo);
            model.setTotalAmount(amount.toString());
            model.setSubject(subject);
            model.setBody(body);
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            request.setBizModel(model);

            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            return new AlipayCreateOrderResult(
                    response.isSuccess(),
                    response.getBody(),
                    response.getCode(),
                    response.getMsg()
            );
        } catch (AlipayApiException e) {
            throw new PaymentIntegrationException("支付宝订单创建异常", e);
        }
    }

    public boolean verifyNotify(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );
        } catch (AlipayApiException e) {
            throw new PaymentIntegrationException("支付宝回调验签异常", e);
        }
    }

    public AlipayQueryResult queryOrder(String orderNo) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(orderNo);
            request.setBizModel(model);

            AlipayTradeQueryResponse response = alipayClient.execute(request);
            return new AlipayQueryResult(
                    response.isSuccess(),
                    response.getTradeStatus(),
                    response.getCode(),
                    response.getMsg()
            );
        } catch (AlipayApiException e) {
            throw new PaymentIntegrationException("支付宝订单查询异常", e);
        }
    }

    public AlipayRefundResult refund(String orderNo, String refundNo, BigDecimal refundAmount, String reason) {
        try {
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(orderNo);
            model.setRefundAmount(refundAmount.toString());
            model.setRefundReason(reason);
            model.setOutRequestNo(refundNo);
            request.setBizModel(model);

            AlipayTradeRefundResponse response = alipayClient.execute(request);
            return new AlipayRefundResult(
                    response.isSuccess(),
                    response.getCode(),
                    response.getMsg()
            );
        } catch (AlipayApiException e) {
            throw new PaymentIntegrationException("支付宝退款异常", e);
        }
    }
}
