package com.fitness.integration.ai.exception;

import com.fitness.integration.common.exception.IntegrationException;

public class AiIntegrationException extends IntegrationException {

    public AiIntegrationException(String message) {
        super(message);
    }

    public AiIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
