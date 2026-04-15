package com.fitness.integration.payment.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AlipayConfig {

    private final AlipayProperties alipayProperties;

    @Bean
    public AlipayClient alipayClient() {
        log.debug("开始创建 AlipayClient Bean");
        log.debug("支付宝配置: serverUrl={}, appId={}, format={}, charset={}, signType={}",
                alipayProperties.getServerUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getFormat(),
                alipayProperties.getCharset(),
                alipayProperties.getSignType());

        AlipayClient client = new DefaultAlipayClient(
                alipayProperties.getServerUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                alipayProperties.getFormat(),
                alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getSignType()
        );

        log.info("AlipayClient Bean 创建成功, appId={}", alipayProperties.getAppId());
        return client;
    }
}
