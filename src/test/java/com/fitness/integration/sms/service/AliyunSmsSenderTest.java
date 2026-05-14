package com.fitness.integration.sms.service;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.fitness.integration.sms.config.AliyunSmsProperties;
import com.fitness.integration.sms.model.SmsSendResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AliyunSmsSenderTest {

    @Mock
    private Client client;

    @Mock
    private AliyunSmsProperties properties;

    @Test
    void sendVerifyCodeShouldReturnSuccessResult() throws Exception {
        SendSmsVerifyCodeResponseBody body = new SendSmsVerifyCodeResponseBody();
        body.setCode("OK");
        SendSmsVerifyCodeResponse response = new SendSmsVerifyCodeResponse();
        response.setBody(body);

        when(properties.getSignName()).thenReturn("fitness");
        when(properties.getTemplateCode()).thenReturn("SMS_001");
        when(client.sendSmsVerifyCode(any())).thenReturn(response);

        AliyunSmsSender sender = new AliyunSmsSender(client, properties);

        SmsSendResult result = sender.sendVerifyCode("13800138000", "123456", 5);

        assertTrue(result.success());
    }
}
