package com.fitness.integration.payment.exception;

import com.fitness.integration.common.exception.IntegrationException;

public class PaymentIntegrationException extends IntegrationException {

    public PaymentIntegrationException(String message) {
        super(message);
    }

    public PaymentIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
