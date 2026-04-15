package com.fitness.integration.payment.service.impl;

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

    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;

    @Override
    public String createPayOrder(String orderNo, BigDecimal amount, String subject, String body) {
        log.debug("开始创建支付宝订单: orderNo={}, amount={}, subject={}", orderNo, amount, subject);
        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayProperties.getNotifyUrl());
            request.setReturnUrl(alipayProperties.getReturnUrl());
            log.debug("支付宝回调配置: notifyUrl={}, returnUrl={}", alipayProperties.getNotifyUrl(), alipayProperties.getReturnUrl());

            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(orderNo);
            model.setTotalAmount(amount.toString());
            model.setSubject(subject);
            model.setBody(body);
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            log.debug("支付宝订单参数: outTradeNo={}, totalAmount={}, productCode={}", 
                    model.getOutTradeNo(), model.getTotalAmount(), model.getProductCode());

            request.setBizModel(model);

            log.debug("开始调用支付宝接口创建订单...");
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            log.debug("支付宝接口响应: success={}, code={}, msg={}", 
                    response.isSuccess(), response.getCode(), response.getMsg());

            if (response.isSuccess()) {
                log.info("支付宝订单创建成功: orderNo={}, tradeNo={}", orderNo, response.getTradeNo());
                return response.getBody();
            } else {
                log.error("支付宝订单创建失败: orderNo={}, code={}, msg={}, subCode={}, subMsg={}", 
                        orderNo, response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
                throw new RuntimeException("支付宝订单创建失败: " + response.getMsg());
            }
        } catch (AlipayApiException e) {
            log.error("支付宝订单创建异常: orderNo={}, errCode={}, errMsg={}", 
                    orderNo, e.getErrCode(), e.getErrMsg(), e);
            throw new RuntimeException("支付宝订单创建异常", e);
        }
    }

    @Override
    public boolean verifyNotify(Map<String, String> params) {
        log.debug("开始验证支付宝回调签名, params={}", params);
        try {
            String sign = params.get("sign");
            String tradeNo = params.get("trade_no");
            String outTradeNo = params.get("out_trade_no");
            log.debug("支付宝回调关键参数: sign={}, tradeNo={}, outTradeNo={}", sign, tradeNo, outTradeNo);

            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );
            log.info("支付宝回调验签结果: outTradeNo={}, signVerified={}", outTradeNo, signVerified);
            return signVerified;
        } catch (AlipayApiException e) {
            log.error("支付宝回调验签异常: errCode={}, errMsg={}", e.getErrCode(), e.getErrMsg(), e);
            return false;
        }
    }

    @Override
    public String queryOrderStatus(String orderNo) {
        log.debug("开始查询支付宝订单状态: orderNo={}", orderNo);
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(orderNo);
            request.setBizModel(model);
            log.debug("支付宝订单查询参数: outTradeNo={}", model.getOutTradeNo());

            AlipayTradeQueryResponse response = alipayClient.execute(request);
            log.debug("支付宝订单查询响应: success={}, code={}, msg={}", 
                    response.isSuccess(), response.getCode(), response.getMsg());

            if (response.isSuccess()) {
                log.info("支付宝订单查询成功: orderNo={}, tradeNo={}, status={}, buyerLogonId={}", 
                        orderNo, response.getTradeNo(), response.getTradeStatus(), response.getBuyerLogonId());
                return response.getTradeStatus();
            } else {
                log.error("支付宝订单查询失败: orderNo={}, code={}, msg={}, subCode={}, subMsg={}", 
                        orderNo, response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
                return null;
            }
        } catch (AlipayApiException e) {
            log.error("支付宝订单查询异常: orderNo={}, errCode={}, errMsg={}", 
                    orderNo, e.getErrCode(), e.getErrMsg(), e);
            return null;
        }
    }

    @Override
    public boolean refund(String orderNo, String refundNo, BigDecimal refundAmount, String reason) {
        log.debug("开始支付宝退款: orderNo={}, refundNo={}, refundAmount={}, reason={}", 
                orderNo, refundNo, refundAmount, reason);
        try {
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(orderNo);
            model.setRefundAmount(refundAmount.toString());
            model.setRefundReason(reason);
            model.setOutRequestNo(refundNo);

            request.setBizModel(model);
            log.debug("支付宝退款参数: outTradeNo={}, refundAmount={}, outRequestNo={}", 
                    model.getOutTradeNo(), model.getRefundAmount(), model.getOutRequestNo());

            AlipayTradeRefundResponse response = alipayClient.execute(request);
            log.debug("支付宝退款响应: success={}, code={}, msg={}", 
                    response.isSuccess(), response.getCode(), response.getMsg());

            if (response.isSuccess()) {
                log.info("支付宝退款成功: orderNo={}, refundNo={}, tradeNo={}, refundFee={}", 
                        orderNo, refundNo, response.getTradeNo(), response.getRefundFee());
                return true;
            } else {
                log.error("支付宝退款失败: orderNo={}, refundNo={}, code={}, msg={}, subCode={}, subMsg={}", 
                        orderNo, refundNo, response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
                return false;
            }
        } catch (AlipayApiException e) {
            log.error("支付宝退款异常: orderNo={}, refundNo={}, errCode={}, errMsg={}", 
                    orderNo, refundNo, e.getErrCode(), e.getErrMsg(), e);
            return false;
        }
    }
}
