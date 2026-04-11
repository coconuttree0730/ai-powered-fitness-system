package com.fitness.integration.sms.config;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AliyunSmsConfig {

    private final AliyunSmsProperties aliyunSmsProperties;

    @Bean
    public Client dypnsApiClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(aliyunSmsProperties.getAccessKeyId())
                .setAccessKeySecret(aliyunSmsProperties.getAccessKeySecret());
        config.endpoint = aliyunSmsProperties.getEndpoint();
        return new Client(config);
    }
}
