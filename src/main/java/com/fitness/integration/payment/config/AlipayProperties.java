package com.fitness.integration.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
}
