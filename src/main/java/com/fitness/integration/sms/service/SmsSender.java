package com.fitness.integration.sms.service;

import com.fitness.integration.sms.model.SmsSendResult;

public interface SmsSender {

    SmsSendResult sendVerifyCode(String phone, String code, int validMinutes);
}
