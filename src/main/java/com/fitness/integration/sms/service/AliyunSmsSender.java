package com.fitness.integration.sms.service;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.fitness.integration.sms.config.AliyunSmsProperties;
import com.fitness.integration.sms.exception.SmsIntegrationException;
import com.fitness.integration.sms.model.SmsSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AliyunSmsSender implements SmsSender {

    private final Client dypnsApiClient;
    private final AliyunSmsProperties smsProperties;

    @Override
    public SmsSendResult sendVerifyCode(String phone, String code, int validMinutes) {
        try {
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(smsProperties.getSignName())
                    .setTemplateCode(smsProperties.getTemplateCode())
                    .setTemplateParam(buildTemplateParam(code, validMinutes))
                    .setValidTime((long) validMinutes * 60)
                    .setInterval(60L);

            SendSmsVerifyCodeResponse response = dypnsApiClient.sendSmsVerifyCode(request);
            if (response.getBody() == null) {
                return new SmsSendResult(false, null, "empty response");
            }

            boolean success = "OK".equals(response.getBody().getCode());
            if (success) {
                log.info("阿里云短信验证码发送成功: phone={}", phone);
            } else {
                log.warn("阿里云短信验证码发送失败: phone={}, code={}, message={}",
                        phone, response.getBody().getCode(), response.getBody().getMessage());
            }
            return new SmsSendResult(success, response.getBody().getCode(), response.getBody().getMessage());
        } catch (Exception e) {
            throw new SmsIntegrationException("阿里云短信发送异常", e);
        }
    }

    private String buildTemplateParam(String code, int validMinutes) {
        return "{\"code\":\"" + code + "\",\"min\":\"" + validMinutes + "\"}";
    }
}
