package com.fitness.integration.sms.model;

public record SmsSendResult(
        boolean success,
        String code,
        String message
) {
}
