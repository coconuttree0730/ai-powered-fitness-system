package com.fitness.integration.sms.exception;

import com.fitness.integration.common.exception.IntegrationException;

public class SmsIntegrationException extends IntegrationException {

    public SmsIntegrationException(String message) {
        super(message);
    }

    public SmsIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
