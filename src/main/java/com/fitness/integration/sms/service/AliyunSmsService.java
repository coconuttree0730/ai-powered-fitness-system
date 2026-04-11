package com.fitness.integration.sms.service;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.fitness.integration.sms.config.AliyunSmsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AliyunSmsService {

    private final Client dypnsApiClient;
    private final AliyunSmsProperties smsProperties;

    public boolean sendVerifyCode(String phone, String code, int validMinutes) {
        try {
            String templateParam = "{\"code\":\"" + code + "\",\"min\":\"" + validMinutes + "\"}";

            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(smsProperties.getSignName())
                    .setTemplateCode(smsProperties.getTemplateCode())
                    .setTemplateParam(templateParam)
                    .setValidTime((long) validMinutes * 60)
                    .setInterval(60L);

            SendSmsVerifyCodeResponse response = dypnsApiClient.sendSmsVerifyCode(request);

            if (response.getBody() != null && "OK".equals(response.getBody().getCode())) {
                log.info("阿里云短信验证码发送成功: phone={}", phone);
                return true;
            } else {
                log.error("阿里云短信验证码发送失败: phone={}, code={}, message={}",
                        phone,
                        response.getBody() != null ? response.getBody().getCode() : "null",
                        response.getBody() != null ? response.getBody().getMessage() : "null");
                return false;
            }
        } catch (Exception e) {
            log.error("阿里云短信验证码发送异常: phone={}", phone, e);
            return false;
        }
    }

    public boolean checkVerifyCode(String phone, String code) {
        try {
            CheckSmsVerifyCodeRequest request = new CheckSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setVerifyCode(code)
                    .setCountryCode("cn");

            CheckSmsVerifyCodeResponse response = dypnsApiClient.checkSmsVerifyCode(request);

            if (response.getBody() != null && "OK".equals(response.getBody().getCode())) {
                log.info("阿里云短信验证码校验成功: phone={}", phone);
                return true;
            } else {
                log.warn("阿里云短信验证码校验失败: phone={}, code={}, message={}",
                        phone,
                        response.getBody() != null ? response.getBody().getCode() : "null",
                        response.getBody() != null ? response.getBody().getMessage() : "null");
                return false;
            }
        } catch (Exception e) {
            log.error("阿里云短信验证码校验异常: phone={}", phone, e);
            return false;
        }
    }
}
