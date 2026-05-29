package com.fitness.integration.payment.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    private String appId;
    private String privateKey;
    private String publicKey;
    private String alipayPublicKey;
    private String serverUrl;
    private String format = "json";
    private String charset = "UTF-8";
    private String signType = "RSA2";
    private String notifyUrl;
    private String returnUrl;

    @PostConstruct
    public void validateAndLog() {
        String maskedAppId = (appId != null && appId.length() >= 4)
                ? appId.substring(0, 4) + "****"
                : "未配置";

        log.info("支付宝配置信息: serverUrl={}, notifyUrl={}, returnUrl={}, appId={}",
                serverUrl, notifyUrl, returnUrl, maskedAppId);

        if (appId == null || appId.isBlank()) {
            log.warn("支付宝 appId 未配置，支付功能将不可用！");
        }

        if (privateKey == null || privateKey.isBlank()) {
            log.warn("支付宝 privateKey 未配置，支付功能将不可用！");
        }
    }
}
