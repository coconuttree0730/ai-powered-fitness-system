package com.fitness.integration.payment.gateway;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.fitness.integration.payment.config.AlipayProperties;
import com.fitness.integration.payment.exception.PaymentIntegrationException;
import com.fitness.integration.payment.model.AlipayCreateOrderResult;
import com.fitness.integration.payment.model.AlipayQueryResult;
import com.fitness.integration.payment.model.AlipayRefundResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
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

            log.info("支付宝创建订单请求: orderNo={}, amount={}, subject={}, body={}, notifyUrl={}, returnUrl={}",
                    orderNo, amount, subject, body,
                    alipayProperties.getNotifyUrl(), alipayProperties.getReturnUrl());

            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);

            if (!response.isSuccess()) {
                log.error("支付宝创建订单失败: orderNo={}, code={}, msg={}, subCode={}, subMsg={}",
                        orderNo, response.getCode(), response.getMsg(),
                        response.getSubCode(), response.getSubMsg());
            }

            return new AlipayCreateOrderResult(
                    response.isSuccess(),
                    response.getBody(),
                    response.getCode(),
                    response.getMsg()
            );
        } catch (AlipayApiException e) {
            log.error("支付宝订单创建异常: orderNo={}, errCode={}, errMsg={}",
                    orderNo, e.getErrCode(), e.getErrMsg(), e);
            throw new PaymentIntegrationException("支付宝订单创建异常", e);
        } catch (Exception e) {
            log.error("支付宝订单创建未知异常: orderNo={}, message={}", orderNo, e.getMessage(), e);
            throw new PaymentIntegrationException("支付宝服务调用失败，请稍后重试", e);
        }
    }

    public AlipayCreateOrderResult createWapOrder(String orderNo, BigDecimal amount, String subject, String body) {
        try {
            AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
            request.setNotifyUrl(alipayProperties.getNotifyUrl());
            request.setReturnUrl(alipayProperties.getReturnUrl());

            AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
            model.setOutTradeNo(orderNo);
            model.setTotalAmount(amount.toString());
            model.setSubject(subject);
            model.setBody(body);
            model.setProductCode("QUICK_WAP_WAY");
            model.setQuitUrl(alipayProperties.getReturnUrl());
            request.setBizModel(model);

            log.info("支付宝创建WapPay订单请求: orderNo={}, amount={}, subject={}, body={}, notifyUrl={}, returnUrl={}, quitUrl={}",
                    orderNo, amount, subject, body,
                    alipayProperties.getNotifyUrl(), alipayProperties.getReturnUrl(),
                    alipayProperties.getReturnUrl());

            AlipayTradeWapPayResponse response = alipayClient.pageExecute(request);

            if (!response.isSuccess()) {
                log.error("支付宝创建WapPay订单失败: orderNo={}, code={}, msg={}, subCode={}, subMsg={}",
                        orderNo, response.getCode(), response.getMsg(),
                        response.getSubCode(), response.getSubMsg());
            }

            return new AlipayCreateOrderResult(
                    response.isSuccess(),
                    response.getBody(),
                    response.getCode(),
                    response.getMsg()
            );
        } catch (AlipayApiException e) {
            log.error("支付宝WapPay订单创建异常: orderNo={}, errCode={}, errMsg={}",
                    orderNo, e.getErrCode(), e.getErrMsg(), e);
            throw new PaymentIntegrationException("支付宝WapPay订单创建异常", e);
        } catch (Exception e) {
            log.error("支付宝WapPay订单创建未知异常: orderNo={}, message={}", orderNo, e.getMessage(), e);
            throw new PaymentIntegrationException("支付宝服务调用失败，请稍后重试", e);
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
